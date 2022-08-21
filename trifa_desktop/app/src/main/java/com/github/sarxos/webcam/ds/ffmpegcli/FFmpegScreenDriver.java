package com.github.sarxos.webcam.ds.ffmpegcli;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamDiscoverySupport;
import com.github.sarxos.webcam.WebcamDriver;

import org.bridj.Platform;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class FFmpegScreenDriver implements WebcamDriver, WebcamDiscoverySupport
{

    static final String TAG = "ffmpeg";
    // private static final Logger LOG = null;
    private String path = "";
    public static final int FFMPEG_SCREEN_GRAB_SCREEN_RES_4k = 0;
    public static final int FFMPEG_SCREEN_GRAB_SCREEN_RES_1080p = 1;
    public static final int FFMPEG_SCREEN_GRAB_SCREEN_RES_720p = 2;
    public static final int FFMPEG_SCREEN_GRAB_SCREEN_RES_CAMOVERLAY_1080p = 3;
    private int ffmpeg_screengrab_screen_res_used = 0;

    public FFmpegScreenDriver(int wanted_screen_res)
    {
        ffmpeg_screengrab_screen_res_used = wanted_screen_res;
    }

    @Override
    public List<WebcamDevice> getDevices()
    {
        List<WebcamDevice> devices;
        devices = getUnixDevices();
        return devices;
    }

    public String get_capture_screen_res()
    {
        if (ffmpeg_screengrab_screen_res_used == FFMPEG_SCREEN_GRAB_SCREEN_RES_4k)
        {
            return "3840x2160";
        }
        else if (ffmpeg_screengrab_screen_res_used == FFMPEG_SCREEN_GRAB_SCREEN_RES_1080p)
        {
            return "1920x1080";
        }
        else if (ffmpeg_screengrab_screen_res_used == FFMPEG_SCREEN_GRAB_SCREEN_RES_CAMOVERLAY_1080p)
        {
            return "/dev/video0:1920x1080";
        }
        else // ffmpeg_screengrab_screen_res_used == 2
        {
            return "1280x720";
        }
    }

    private List<WebcamDevice> getUnixDevices()
    {

        List<WebcamDevice> devices = new ArrayList<WebcamDevice>();

        String line = null;
        BufferedReader br = null;

        FFmpegScreenDevice ffsd = new FFmpegScreenDevice("/dev/null", "Screen", "640x480 1280x720 1920x1080");
        ffsd.setScreenRes(get_capture_screen_res());
        devices.add(ffsd);

        return devices;
    }

    private Process startProcess(String[] cmd)
    {
        Process process = null;

        OutputStream os;

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
            return "gdigrab";
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
