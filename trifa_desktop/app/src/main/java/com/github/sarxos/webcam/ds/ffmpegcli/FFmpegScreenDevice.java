package com.github.sarxos.webcam.ds.ffmpegcli;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamException;
import com.zoffcc.applications.trifa.Log;

import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.sarxos.webcam.ds.ffmpegcli.FFmpegScreenDriver.TAG;


public class FFmpegScreenDevice implements WebcamDevice, WebcamDevice.BufferAccess
{

    private static final Logger LOG = LoggerFactory.getLogger(FFmpegScreenDevice.class);

    private volatile Process process = null;

    private String path = "";
    private String name = null;
    private Dimension[] resolutions = null;
    private Dimension resolution = null;

    private AtomicBoolean open = new AtomicBoolean(false);
    private AtomicBoolean disposed = new AtomicBoolean(false);

    protected FFmpegScreenDevice(String path, File vfile, String resolutions)
    {
        this(path, vfile.getAbsolutePath(), resolutions);
    }

    protected FFmpegScreenDevice(String path, String name, String resolutions)
    {
        this.path = path;
        this.name = name;
        this.resolutions = readResolutions(resolutions);
    }

    public void startProcess() throws IOException
    {
        Log.i(TAG, "startProcess");
        ProcessBuilder builder = new ProcessBuilder(buildCommand());
        builder.redirectErrorStream(true); // so we can ignore the error stream

        process = builder.start();
    }

    private byte[] readNextFrame() throws IOException
    {
        // Log.i(TAG, "readNextFrame");

        InputStream out = process.getInputStream();

        final int SIZE = arraySize();
        int CHUNK_SIZE = 16484;

        int cursor = 0;
        byte[] buffer = new byte[SIZE];

        while (isAlive(process))
        {
            int no = out.available();

            // If buffer is not full yet
            if (cursor < SIZE)
            {
                if ((SIZE - cursor) < CHUNK_SIZE)
                {
                    int actually_read = out.read(buffer, cursor, (SIZE - cursor));
                    cursor += actually_read;
                    // Log.i(TAG, "readNextFrame:actually_read=" + actually_read + " cursor=" + cursor);
                }
                else
                {
                    int actually_read = out.read(buffer, cursor, CHUNK_SIZE);
                    cursor += actually_read;
                    // Log.i(TAG, "readNextFrame:actually_read=" + actually_read + " cursor=" + cursor);
                }
            }
            else
            {
                break;
            }

        }

        // Log.i(TAG, "readNextFrame:ret=" + buffer);

        return buffer;
    }

    /**
     * Based on answer: https://stackoverflow.com/a/12062505/7030976
     *
     * @param bgr - byte array in bgr format
     * @return new image
     */
    private BufferedImage buildImage(byte[] bgr)
    {
        // Log.i(TAG, "buildImage");

        final BufferedImage image = new BufferedImage(resolution.width, resolution.height,
                                                      BufferedImage.TYPE_3BYTE_BGR);
        final byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(bgr, 0, imageData, 0, bgr.length);

        return image;
    }

    private static boolean isAlive(Process p)
    {
        try
        {
            p.exitValue();
            return false;
        }
        catch (IllegalThreadStateException e)
        {
            return true;
        }
    }

    private Dimension[] readResolutions(String res)
    {
        // Log.i(TAG, "readResolutions:" + res);
        List<Dimension> resolutions = new ArrayList<Dimension>();
        String[] parts = res.split(" ");

        for (String part : parts)
        {
            String[] xy = part.split("x");
            resolutions.add(new Dimension(Integer.parseInt(xy[0]), Integer.parseInt(xy[1])));
        }

        Dimension[] d = resolutions.toArray(new Dimension[resolutions.size()]);

        // Log.i(TAG, "readResolutions:ret=" + d);

        return d;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Dimension[] getResolutions()
    {
        return resolutions;
    }

    @Override
    public Dimension getResolution()
    {
        Log.i(TAG, "getResolution");

        if (resolution == null)
        {
            resolution = getResolutions()[0];
        }
        return resolution;
    }

    private String getResolutionString()
    {
        Dimension d = getResolution();
        return String.format("%dx%d", d.width, d.height);
    }

    @Override
    public void setResolution(Dimension resolution)
    {
        this.resolution = resolution;
    }

    @Override
    public void open()
    {
        Log.i(TAG, "open");

        if (!open.compareAndSet(false, true))
        {
            return;
        }

        try
        {
            startProcess();
        }
        catch (IOException e)
        {
            throw new WebcamException(e);
        }
    }

    @Override
    public void close()
    {
        Log.i(TAG, "close");

        if (!open.compareAndSet(true, false))
        {
            return;
        }

        process.destroy();

        try
        {
            process.waitFor();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose()
    {
        Log.i(TAG, "dispose");

        if (disposed.compareAndSet(false, true) && open.get())
        {
            close();
        }
    }

    @Override
    public boolean isOpen()
    {
        return open.get();
    }

    public String[] buildCommand()
    {
        Log.i(TAG, "buildCommand");

        String captureDriver = FFmpegScreenDriver.getCaptureDriver();

        // String deviceInput = name;
        if (Platform.isWindows())
        {
            // deviceInput = "\"video=" + name + "\"";
        }

        // ffmpeg -video_size 1024x768 -framerate 25 -f x11grab -i :0.0
        //        -vcodec rawvideo -f rawvideo -pix_fmt bgr24 -

        // @formatter:off
        String[] cmd_array = new String[]{FFmpegScreenDriver.getCommand(
                path),
                "-loglevel", "panic",
                "-show_region", "0",
                "-framerate", "30",
                "-video_size", "3840x2160",
                // "-video_size", "1920x1080",
                // "-video_size", getResolutionString(),
                "-f", captureDriver,
                "-i", ":0.0",
                "-vcodec", "rawvideo",
                "-filter:v", "scale=" + getResolutionString().replace("x", ":")+ ":flags=neighbor",
                "-sws_dither", "none",
                "-f", "rawvideo",
                "-vsync", "vfr", // avoid frame duplication
                "-pix_fmt", "bgr24", // output format as bgr24
                "-", // output to stdout
        };
        // @formatter:on

        Log.i(TAG, "cmd_array=" + Arrays.toString(cmd_array));

        return cmd_array;
    }

    @Override
    public BufferedImage getImage()
    {
        // Log.i(TAG, "getImage");

        if (!open.get())
        {
            // Log.i(TAG, "getImage:null");
            return null;
        }

        try
        {
            //final long ts1 = System.currentTimeMillis();
            //Log.i(TAG, "getImage:001");
            // final byte b[] = readNextFrame();
            // final BufferedImage bufi = buildImage(b);
            //Log.i(TAG, "getImage:001:" + (System.currentTimeMillis() - ts1));
            // return bufi;
            return buildImage(readNextFrame());
        }
        catch (IOException e)
        {
            Log.i(TAG, "getImage:EE01:" + e.getMessage());
            throw new WebcamException(e);
        }
    }

    @Override
    public ByteBuffer getImageBytes()
    {
        // Log.i(TAG, "getImageBytes");

        if (!open.get())
        {
            return null;
        }

        final ByteBuffer buffer;
        try
        {
            buffer = ByteBuffer.allocate(arraySize());
            buffer.put(readNextFrame());
        }
        catch (IOException e)
        {
            throw new WebcamException(e);
        }

        return buffer;
    }

    @Override
    public void getImageBytes(ByteBuffer byteBuffer)
    {
        // Log.i(TAG, "getImageBytes2");

        try
        {
            byteBuffer.put(readNextFrame());
        }
        catch (IOException e)
        {
            throw new WebcamException(e);
        }
    }

    private int arraySize()
    {
        // Log.i(TAG, "arraySize=" + (resolution.width * resolution.height * 3));
        return resolution.width * resolution.height * 3;
    }
}