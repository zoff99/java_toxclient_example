package com.github.sarxos.webcam.ds.ffmpegcli;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;

import org.bridj.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class FFmpegScreenDriver implements WebcamDriver, WebcamDiscoverySupport
{

    static final String TAG = "ffmpeg";
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegScreenDriver.class);
    private String path = "";

    @Override
    public List<WebcamDevice> getDevices()
    {
        List<WebcamDevice> devices;
        devices = getUnixDevices();
        return devices;
    }

    private List<WebcamDevice> getUnixDevices()
    {

        List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

        String line = null;
        BufferedReader br = null;

        devices.add(new FFmpegScreenDevice("/dev/null", "Screen", "640x480 1280x720 1920x1080"));
        return devices;
    }

    private Process startProcess(String[] cmd)
    {
        Process process = null;

        OutputStream os;
        if (LOG.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            for (String c : cmd)
            {
                sb.append(c).append(' ');
            }
            LOG.debug("Executing command: {}", sb.toString());
        }

        try
        {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            process = builder.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        os = process.getOutputStream();

        try
        {
            os.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return process;
    }

    public static String getCaptureDriver()
    {
        if (Platform.isLinux())
        {
            return "x11grab";
        }
        else if (Platform.isWindows())
        {
            return "dshow";
        }
        else if (Platform.isMacOSX())
        {
            return "avfoundation";
        }

        // Platform not supported
        return null;
    }

    public FFmpegScreenDriver withPath(String path)
    {
        this.path = path;
        return this;
    }

    private String getCommand()
    {
        return getCommand(path);
    }

    public static String getCommand(String path)
    {
        return "ffmpeg";
    }

    @Override
    public boolean isThreadSafe()
    {
        return false;
    }

    @Override
    public long getScanInterval()
    {
        return 3000;
    }

    @Override
    public boolean isScanPossible()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

}