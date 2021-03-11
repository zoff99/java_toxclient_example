/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_video_buffer2;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;
import static com.zoffcc.applications.trifa.MainActivity.toxav_video_send_frame;
import static com.zoffcc.applications.trifa.MainActivity.video_buffer_2;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.current_pk;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.friendnum;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_ended_actions;
import static java.awt.Font.PLAIN;

public class VideoOutFrame extends JFrame implements ItemListener, WindowListener, WebcamListener, WebcamDiscoveryListener, Thread.UncaughtExceptionHandler, WebcamImageTransformer
{
    private static final String TAG = "trifa.VideoOutFrame";

    private static WebcamPicker picker = null;
    private static WebcamPanel panel = null;
    static JButton VideoCallStartButton = null;
    static JButton VideoCallStopButton = null;
    static JPanel ButtonPanel = null;
    private static Webcam webcam = null;
    private static BufferedImage IMAGE_FRAME = null;
    private static final String VIDEO_OVERLAY_ASSET_NAME = "video_overlay.png";

    public static int width = 640;
    public static int height = 480;

    public VideoOutFrame()
    {
        super("TRIfA - Camera");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        IMAGE_FRAME = getImage();

        // Webcam.setDriver(new FFmpegCliDriver());
        Webcam.addDiscoveryListener(this);

        picker = new WebcamPicker();
        picker.addItemListener(this);

        webcam = picker.getSelectedWebcam();

        ButtonPanel = new JPanel(true);
        ButtonPanel.setLayout(new GridLayout());
        add(ButtonPanel, BorderLayout.SOUTH);

        VideoCallStartButton = new JButton("start Video Call");
        VideoCallStartButton.setFont(new java.awt.Font("monospaced", PLAIN, 7));
        ButtonPanel.add(VideoCallStartButton);

        VideoCallStopButton = new JButton("stop Video Call");
        VideoCallStopButton.setFont(new java.awt.Font("monospaced", PLAIN, 7));
        ButtonPanel.add(VideoCallStopButton);

        VideoCallStartButton.setVisible(true);
        VideoCallStopButton.setVisible(true);

        VideoCallStartButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "VideoCallStartButton pressed");
                do_start_video_call();
            }
        });

        VideoCallStopButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "VideoCallStopButton pressed");
                do_stop_video_call();
            }
        });

        if (webcam != null)
        {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            width = webcam.getViewSize().width;
            height = webcam.getViewSize().height;
            webcam.setImageTransformer(this);
            webcam.addWebcamListener(this);
            panel = new WebcamPanel(webcam, false);
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(true);
            panel.setImageSizeDisplayed(true);
            panel.setMirrored(true);

            add(picker, BorderLayout.NORTH);
            add(panel, BorderLayout.CENTER);
        }
        else
        {
            add(picker, BorderLayout.NORTH);
            picker.setVisible(true);
        }

        setVisible(true);

        if (webcam != null)
        {
            Thread t = new Thread()
            {

                @Override
                public void run()
                {
                    panel.start();
                }
            };
            t.setName("trifa_cam1");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(this);
            t.start();
        }

        ButtonPanel.setVisible(true);
        VideoCallStartButton.setVisible(true);
        VideoCallStopButton.setVisible(true);
        ButtonPanel.revalidate();
        revalidate();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        System.err.println(String.format("Exception in thread %s", t.getName()));
        e.printStackTrace();
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (e.getItem() != webcam)
            {
                Log.i(TAG, "webcam=" + webcam);
                if (webcam != null)
                {
                    panel.stop();
                    remove(panel);

                    webcam.removeWebcamListener(this);
                    webcam.close();

                    webcam = (Webcam) e.getItem();
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.setImageTransformer(this);
                    webcam.addWebcamListener(this);

                    Log.i(TAG, "selected " + webcam.getName());

                    panel = new WebcamPanel(webcam, false);
                    panel.setFPSDisplayed(true);
                    panel.setDisplayDebugInfo(true);
                    panel.setImageSizeDisplayed(true);
                    panel.setMirrored(true);

                    add(panel, BorderLayout.CENTER);
                    revalidate();

                    Thread t = new Thread()
                    {

                        @Override
                        public void run()
                        {
                            panel.start();
                        }
                    };
                    t.setName("trifa_cam2");
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler(this);
                    t.start();
                }
                else if ((e.getItem() != null) && (webcam == null))
                {
                    webcam = (Webcam) e.getItem();
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    width = webcam.getViewSize().width;
                    height = webcam.getViewSize().height;
                    webcam.setImageTransformer(this);
                    webcam.addWebcamListener(this);

                    Log.i(TAG, "selected " + webcam.getName());

                    panel = new WebcamPanel(webcam, false);
                    panel.setFPSDisplayed(true);
                    panel.setDisplayDebugInfo(true);
                    panel.setImageSizeDisplayed(true);
                    panel.setMirrored(true);

                    add(panel, BorderLayout.CENTER);
                    revalidate();

                    Thread t = new Thread()
                    {

                        @Override
                        public void run()
                        {
                            panel.start();
                        }
                    };
                    t.setName("trifa_cam2");
                    t.setDaemon(true);
                    t.setUncaughtExceptionHandler(this);
                    t.start();
                }
            }
        }
    }

    @Override
    public void webcamOpen(WebcamEvent we)
    {

    }

    @Override
    public void webcamClosed(WebcamEvent we)
    {

    }

    @Override
    public void webcamDisposed(WebcamEvent we)
    {

    }

    @Override
    public void webcamImageObtained(WebcamEvent we)
    {
        try
        {
            // Log.i(TAG, "webcam_image:captured:001");
            BufferedImage buf = we.getImage();
            if (buf != null)
            {
                // Log.i(TAG, "webcam_image:captured:002");
                if (Callstate.state != 0)
                {
                    // Log.i(TAG, "webcam_image:captured:003");
                    if ((Callstate.friend_pubkey != null) && (Callstate.friend_pubkey.length() > 0))
                    {
                        // Log.i(TAG, "webcam_image:captured:004");
                        int frame_width_px = buf.getWidth();
                        int frame_height_px = buf.getHeight();

                        if ((video_buffer_2 == null) || (frame_width_px != width) || (frame_height_px != height))
                        {
                            Log.i(TAG, "webcam_image:captured:005:w=" + frame_width_px + " h=" + frame_height_px);
                            int buffer_size_in_bytes2 = (int) (frame_width_px * frame_height_px * 1.5f);
                            video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2);
                            set_JNI_video_buffer2(video_buffer_2, frame_width_px, frame_height_px);
                        }

                        video_buffer_2.rewind();
                        byte[] b = rgb2yuv(buf, frame_width_px, frame_height_px);
                        // Log.i(TAG, "webcam_image:captured:005a:r=" + video_buffer_2.remaining() + " p=" +
                        //            video_buffer_2.capacity() + " b=" + b.length);
                        int len = video_buffer_2.remaining();
                        if (len > b.length)
                        {
                            len = b.length;
                        }

                        video_buffer_2.put(b, 0, len);
                        int res = toxav_video_send_frame(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                         buf.getWidth(), buf.getHeight());
                        // Log.i(TAG, "webcam_image:captured:006:fn=" +
                        //            tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) + " res=" + res);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "webcam_image:captured:EE01:" + e.getMessage());
        }
    }

    @Override
    public void webcamFound(WebcamDiscoveryEvent event)
    {
        if (picker != null)
        {
            picker.addItem(event.getWebcam());
        }
    }

    @Override
    public void webcamGone(WebcamDiscoveryEvent event)
    {
        if (picker != null)
        {
            picker.removeItem(event.getWebcam());
        }
    }

    @Override
    public void windowOpened(WindowEvent windowEvent)
    {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent)
    {

    }

    @Override
    public void windowClosed(WindowEvent windowEvent)
    {
        webcam.close();
    }

    @Override
    public void windowIconified(WindowEvent windowEvent)
    {
        Log.i(TAG, "webcam viewer paused");
        panel.pause();
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent)
    {
        Log.i(TAG, "webcam viewer resumed");
        panel.resume();
    }

    @Override
    public void windowActivated(WindowEvent windowEvent)
    {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent)
    {

    }

    public static byte[] rgb2yuv(BufferedImage bi, int width1, int height1)
    {
        int w = bi.getWidth();
        int h = bi.getHeight();
        byte[] ret = new byte[(int) (w * h * 1.5f)];

        try
        {
            boolean s = false;

            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    int color = bi.getRGB(i, j);

                    int alpha = color >> 24 & 0xff;
                    int R = color >> 16 & 0xff;
                    int G = color >> 8 & 0xff;
                    int B = color & 0xff;

                    //~ int y = (int) ((0.257 * red) + (0.504 * green) + (0.098 * blue) + 16);
                    //~ int v = (int) ((0.439 * red) - (0.368 * green) - (0.071 * blue) + 128);
                    //~ int u = (int) (-(0.148 * red) - (0.291 * green) + (0.439 * blue) + 128);

                    int Y = (int) (R * .299000 + G * .587000 + B * 0.114000);
                    int U = (int) (R * -.168736 + G * -.331264 + B * 0.500000 + 128);
                    int V = (int) (R * .500000 + G * -.418688 + B * -0.081312 + 128);


                    int arraySize = height1 * width1;
                    int yLoc = j * width1 + i;
                    int uLoc = (j / 2) * (width1 / 2) + i / 2 + arraySize;
                    int vLoc = (j / 2) * (width1 / 2) + i / 2 + arraySize + arraySize / 4;

                    ret[yLoc] = (byte) Y;
                    ret[uLoc] = (byte) U;
                    ret[vLoc] = (byte) V;

                    s = !s;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    static void do_start_video_call()
    {
        if (current_pk != null)
        {
            try
            {
                Log.i(TAG, "CALL:start:(2.0):Callstate.state=" + Callstate.state);

                if (Callstate.state == 0)
                {
                    Callstate.state = 1;
                    Callstate.accepted_call = 1; // we started the call, so it's already accepted on our side
                    Callstate.call_first_video_frame_received = -1;
                    Callstate.call_start_timestamp = -1;
                    Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(friendnum);
                    Callstate.camera_opened = false;
                    Callstate.audio_speaker = true;
                    Callstate.other_audio_enabled = 1;
                    Callstate.other_video_enabled = 1;
                    Callstate.my_audio_enabled = 1;
                    Callstate.my_video_enabled = 1;
                    MainActivity.set_av_call_status(Callstate.state);

                    Callstate.audio_bitrate = GLOBAL_AUDIO_BITRATE;
                    Callstate.video_bitrate = GLOBAL_VIDEO_BITRATE;
                    VIDEO_FRAME_RATE_OUTGOING = 0;
                    last_video_frame_sent = -1;
                    VIDEO_FRAME_RATE_INCOMING = 0;
                    last_video_frame_received = -1;
                    count_video_frame_received = 0;
                    count_video_frame_sent = 0;

                    int res2 = MainActivity.toxav_call(friendnum, GLOBAL_AUDIO_BITRATE, GLOBAL_VIDEO_BITRATE);
                    if (res2 != 1)
                    {
                        Log.i(TAG, "toxav_call:video_call:RES=" + res2);
                    }
                    Log.i(TAG, "CALL_OUT:002");
                }
                Callstate.call_init_timestamp = System.currentTimeMillis();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "CALL:start:(2):EE:" + e.getMessage());
            }
        }
    }

    static void do_stop_video_call()
    {
        if (!Callstate.friend_pubkey.equals("-1"))
        {
            if (Callstate.state != 0)
            {
                try
                {
                    toxav_call_control(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                       ToxVars.TOXAV_CALL_CONTROL.TOXAV_CALL_CONTROL_CANCEL.value);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
                Log.i(TAG, "decline_button_pressed:on_call_ended_actions");
                on_call_ended_actions();
            }
        }
    }

    @Override
    public BufferedImage transform(BufferedImage image)
    {
        int w = image.getWidth();
        int h = image.getHeight();

        BufferedImage modified = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = modified.createGraphics();
        g2.drawImage(image, null, 0, 0);
        g2.drawImage(IMAGE_FRAME, null, 0, 0);
        g2.dispose();

        modified.flush();

        return modified;
    }

    private static final BufferedImage getImage()
    {
        try
        {
            String asset_filename = "." + File.separator + "assets" + File.separator + VIDEO_OVERLAY_ASSET_NAME;
            return ImageIO.read(new File(asset_filename));
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
