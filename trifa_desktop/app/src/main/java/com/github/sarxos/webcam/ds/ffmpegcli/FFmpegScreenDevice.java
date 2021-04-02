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
    private String captured_screen_res = "3840x2160";

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

        try
        {
            // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            process = builder.start();
            Log.i(TAG, "process=" + process);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private byte[] readNextFrame() throws IOException
    {
        // Log.i(TAG, "readNextFrame");

        InputStream out = process.getInputStream();

        final int SIZE = arraySize();
        int CHUNK_SIZE = 16384;

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
                    if (actually_read > 0)
                    {
                        cursor += actually_read;
                    }
                    // Log.i(TAG, "readNextFrame:1:actually_read=" + actually_read + " cursor=" + cursor);
                }
                else
                {
                    // Log.i(TAG, "readNextFrame:2a:cursor=" + cursor + " SIZE=" + SIZE + " CHUNK_SIZE=" + CHUNK_SIZE);
                    int actually_read = out.read(buffer, cursor, CHUNK_SIZE);
                    if (actually_read > 0)
                    {
                        cursor += actually_read;

                        // try (FileOutputStream fos = new FileOutputStream("./out.txt"))
                        // {
                        //     fos.write(buffer);
                        // }

                    }
                    // Log.i(TAG, "readNextFrame:2:actually_read=" + actually_read + " cursor=" + cursor);
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
        String[] cmd_array = null;

        if (captured_screen_res.equals("/dev/video0:1920x1080"))
        {
            // @formatter:off
            cmd_array = new String[]{
                    FFmpegScreenDriver.getCommand(path),
                    "-loglevel", "panic",
                    "-show_region", "1",
                    "-framerate", "25",
                    "-video_size", "1920x1080",
                    "-f", captureDriver,
                    "-follow_mouse", "centered",
                    "-thread_queue_size", "64",
                    "-i", ":0.0",
                    // -----
                    "-f", "v4l2",
                    "-video_size", "640x480",
                    "-framerate", "25",
                    "-i", "/dev/video0",
                    // -----
                    "-filter_complex",
                      "[0:v] setpts=PTS-STARTPTS [screen];" +
                      "[1:v] setpts=PTS-STARTPTS, scale=320x240 [cam];" +
                      "[cam] pad=w=10+iw:h=10+ih:x=5:y=5:color=0x0000FF88 [camwborder];" +
                      "[screen][camwborder]overlay=main_w-overlay_w-10:main_h-overlay_h-10",
                    // -----
                    "-vcodec", "rawvideo",
                    "-f", "rawvideo",
                    "-vsync", "vfr", // avoid frame duplication
                    "-pix_fmt", "bgr24", // output format as bgr24
                    "-", // output to stdout
            };
            // @formatter:on
        }
        else
        {
            String driver_options1 = "-i";
            String driver_options2 = ":0.0";
            String driver_options3 = "-draw_mouse";
            String driver_options4 = "0";
            String driver_options5 = "-follow_mouse";
            String driver_options6 = "centered";
            String driver_options7 = "-show_region";
            String driver_options8 = "1";
            if (Platform.isWindows())
            {
                // ffmpeg -loglevel panic -show_region 1 -framerate 33 -video_size 1280x720
                // -f gdigrab -i desktop -vcodec rawvideo -filter:v scale=1920:1080:flags=neighbor
                // -f rawvideo -vsync vfr -pix_fmt bgr24 video.dat
                driver_options1 = "-i";
                driver_options2 = "desktop";
                driver_options3 = "";
                driver_options4 = "";
                driver_options5 = "";
                driver_options6 = "";
                driver_options7 = "-show_region";
                driver_options8 = "1";
            }
            else if (Platform.isMacOSX())
            {
                driver_options1 = "-i";
                driver_options2 = "default:none";
                driver_options3 = "";
                driver_options4 = "";
                driver_options5 = "";
                driver_options6 = "";
                driver_options7 = "-show_region";
                driver_options8 = "1";
            }

            // @formatter:off
            cmd_array = new String[]{
                    FFmpegScreenDriver.getCommand(path),
                    "-loglevel", "panic",
                    driver_options7, driver_options8,
                    "-framerate", "30",
                    "-video_size", captured_screen_res,
                    "-f", captureDriver,
                    driver_options5, driver_options6,
                    driver_options3, driver_options4,
                    "-threads", "5",
                    "-thread_queue_size", "64",
                    driver_options1, driver_options2,
                    "-vcodec", "rawvideo",
                    "-filter:v", "scale=" + getResolutionString().replace("x", ":")+ ":flags=neighbor",
                    "-f", "rawvideo",
                    "-vsync", "vfr", // avoid frame duplication
                    "-pix_fmt", "bgr24", // output format as bgr24
                    "-", // output to stdout
            };
            // @formatter:on
        }

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

    public void setScreenRes(String wanted_capture_screen_res)
    {
        captured_screen_res = wanted_capture_screen_res;
    }
}