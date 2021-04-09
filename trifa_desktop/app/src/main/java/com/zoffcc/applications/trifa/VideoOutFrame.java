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
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.dummy.WebcamDummyDriver;
import com.github.sarxos.webcam.ds.ffmpegcli.FFmpegScreenDriver;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.VideoOutFrame1;
import static com.zoffcc.applications.trifa.MainActivity.crgb2yuv;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_video_buffer2;
import static com.zoffcc.applications.trifa.MainActivity.toxav_bit_rate_set;
import static com.zoffcc.applications.trifa.MainActivity.toxav_call_control;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.MainActivity.toxav_video_send_frame_age;
import static com.zoffcc.applications.trifa.MainActivity.video_buffer_2;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.current_pk;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.friendnum;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.NORMAL_GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VIDEO_FRAME_RATE_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.count_video_frame_sent;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_received;
import static com.zoffcc.applications.trifa.TRIFAGlobals.last_video_frame_sent;
import static com.zoffcc.applications.trifa.VideoInFrame.on_call_ended_actions;
import static java.awt.Font.PLAIN;

public class VideoOutFrame extends JFrame implements ItemListener, WindowListener, WebcamListener, WebcamDiscoveryListener, Thread.UncaughtExceptionHandler, WebcamImageTransformer, ChangeListener
{
    private static final String TAG = "trifa.VideoOutFrame";

    private static WebcamPicker picker = null;
    private static WebcamPanel panel = null;
    static JButton VideoCallStartButton = null;
    static JButton VideoCallStopButton = null;
    static JPanel ButtonPanels_Panel = null;
    static JPanel ButtonPanel = null;
    static JPanel ButtonPanel_2 = null;
    static JComboBox VideoToggleScreengrab = null;
    static JSlider VideoOutBitRate = null;
    static JButton VideoOutBitRate_text = null;
    static JButton VideoInBitRate_text = null;
    private static Webcam webcam = null;
    private static BufferedImage IMAGE_FRAME = null;
    private static final String VIDEO_OVERLAY_ASSET_NAME = "video_overlay.png";

    public static int width = 640;
    public static int height = 480;

    public final static int width_screengrab = 1920; // 1280;
    public final static int height_screengrab = 1080; // 720;

    public static int screengrab_active = 0;
    final static Semaphore semaphore_video_out_convert = new Semaphore(1);
    static int semaphore_video_out_convert_active_threads = 0;
    static int semaphore_video_out_convert_max_active_threads = 3;

    final static String DRIVER_OFF = "off";
    final static String DRIVER_DEFAULT = "default";
    final static String DRIVER_720P = "720p";
    final static String DRIVER_DUMMY = "dummy";
    final static String DRIVER_SCREENGRAB_4K = "screengrab_4k";
    final static String DRIVER_SCREENGRAB_1080P = "screengrab_1080p";
    final static String DRIVER_SCREENGRAB_CAMOVERLAY_1080p = "screengrab_camoverlay_1080p";
    final static String DRIVER_SCREENGRAB_720P = "screengrab_720p";

    static String current_driver = DRIVER_OFF;

    public VideoOutFrame()
    {
        super("TRIfA - Camera");

        setSize(width / 2, (int) ((height * 1.3f) / 2.0f));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        IMAGE_FRAME = getImage();

        change_webcam_driver(DRIVER_OFF, false);

        Webcam.addDiscoveryListener(this);

        if (1 == 2)
        {
            picker = new WebcamPicker();
            picker.addItemListener(this);
            webcam = picker.getSelectedWebcam();
        }
        else
        {
            webcam = null;
        }

        ButtonPanel_2 = new JPanel(true);
        ButtonPanel_2.setLayout(new GridLayout());

        VideoOutBitRate = new JSlider();
        VideoOutBitRate.setPaintTicks(true);
        VideoOutBitRate.setSnapToTicks(true);
        VideoOutBitRate.setMinimum(0);
        VideoOutBitRate.setMaximum(10000);
        VideoOutBitRate.setMajorTickSpacing(1000);
        VideoOutBitRate.setMinorTickSpacing(200);
        VideoOutBitRate.addChangeListener(this);
        ButtonPanel_2.add(VideoOutBitRate);

        ButtonPanel = new JPanel(true);
        ButtonPanel.setLayout(new GridLayout());

        VideoCallStartButton = new JButton("start Video Call");
        VideoCallStartButton.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        ButtonPanel.add(VideoCallStartButton);

        VideoCallStopButton = new JButton("stop Video Call");
        VideoCallStopButton.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        ButtonPanel.add(VideoCallStopButton);

        final String[] VideoToggleScreengrab_items = {"Off", "Cam", "Dummy", "Screengrab 4K", "Screengrab 1080p", "Screengrab 720p", "Screengrab 1080p with cam overlay", "Cam 720p"};

        VideoToggleScreengrab = new JComboBox<>(VideoToggleScreengrab_items);
        VideoToggleScreengrab.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        VideoToggleScreengrab.setMaximumRowCount(VideoToggleScreengrab_items.length);
        ButtonPanel.add(VideoToggleScreengrab);

        VideoCallStartButton.setVisible(true);
        VideoCallStopButton.setVisible(true);
        VideoToggleScreengrab.setVisible(true);

        VideoOutBitRate_text = new JButton("0");
        VideoOutBitRate_text.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        VideoInBitRate_text = new JButton("0");
        VideoInBitRate_text.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        ButtonPanels_Panel = new JPanel(true);
        ButtonPanels_Panel.setLayout(new BoxLayout(ButtonPanels_Panel, BoxLayout.PAGE_AXIS));
        ButtonPanels_Panel.add(ButtonPanel);
        ButtonPanels_Panel.add(ButtonPanel_2);
        ButtonPanels_Panel.add(VideoOutBitRate_text);
        ButtonPanels_Panel.add(VideoInBitRate_text);

        add(ButtonPanels_Panel, BorderLayout.SOUTH);

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

        VideoToggleScreengrab.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "VideoToggleScreengrab pressed:index=" + VideoToggleScreengrab.getSelectedIndex());

                if (VideoToggleScreengrab.getSelectedIndex() == 1)
                {
                    change_webcam_driver(DRIVER_DEFAULT, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 2)
                {
                    change_webcam_driver(DRIVER_DUMMY, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 7)
                {
                    change_webcam_driver(DRIVER_720P, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 3)
                {
                    change_webcam_driver(DRIVER_SCREENGRAB_4K, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 4)
                {
                    change_webcam_driver(DRIVER_SCREENGRAB_1080P, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 5)
                {
                    change_webcam_driver(DRIVER_SCREENGRAB_720P, true);
                }
                else if (VideoToggleScreengrab.getSelectedIndex() == 6)
                {
                    change_webcam_driver(DRIVER_SCREENGRAB_CAMOVERLAY_1080p, true);
                }
                else // == 0 [OFF]
                {
                    change_webcam_driver(DRIVER_OFF, true);
                }
            }
        });

        if (webcam != null)
        {
            if (screengrab_active == 1)
            {
                webcam.setViewSize(
                        new Dimension(width_screengrab, height_screengrab)); //(WebcamResolution.VGA.getSize());
            }
            else
            {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
            }
            width = webcam.getViewSize().width;
            height = webcam.getViewSize().height;
            if (screengrab_active != 1)
            {
                webcam.setImageTransformer(this);
            }
            webcam.addWebcamListener(this);
            panel = new WebcamPanel(webcam, false);
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(false);
            panel.setImageSizeDisplayed(true);
            if (screengrab_active != 1)
            {
                panel.setMirrored(true);
            }

            add(picker, BorderLayout.NORTH);
            add(panel, BorderLayout.CENTER);
        }
        else
        {
            // add(picker, BorderLayout.NORTH);
            // picker.setVisible(true);
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

        ButtonPanels_Panel.setVisible(true);
        VideoCallStartButton.setVisible(true);
        VideoCallStopButton.setVisible(true);
        ButtonPanels_Panel.revalidate();
        revalidate();
        repaint();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e)
    {
        System.err.println(String.format("Exception in thread %s", t.getName()));
        e.printStackTrace();
    }

    /**
     * dropdown to select what camera to use
     */
    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            if (e.getItem() != webcam)
            {
                Log.i(TAG, "itemStateChanged:2:webcam=" + webcam + " e=" + e.getItem());

                try
                {
                    panel.stop();
                }
                catch (Exception e2)
                {
                }

                try
                {
                    remove(panel);
                }
                catch (Exception e2)
                {
                }

                if (webcam != null)
                {
                    webcam.removeWebcamListener(this);
                    webcam.close();

                    webcam = (Webcam) e.getItem();
                    if (screengrab_active == 1)
                    {
                        webcam.setViewSize(
                                new Dimension(width_screengrab, height_screengrab)); //(WebcamResolution.VGA.getSize());
                        Log.i(TAG, "size1");
                    }
                    else
                    {
                        if (current_driver.equals(DRIVER_720P))
                        {
                            webcam.setViewSize(WebcamResolution.HD.getSize());
                        }
                        else
                        {
                            webcam.setViewSize(WebcamResolution.VGA.getSize());
                        }
                        Log.i(TAG, "size2");
                    }
                    width = webcam.getViewSize().width;
                    height = webcam.getViewSize().height;

                    Log.i(TAG, "size2:w=" + width + " h=" + height);

                    if (screengrab_active != 1)
                    {
                        webcam.setImageTransformer(this);
                    }
                    webcam.addWebcamListener(this);

                    Log.i(TAG, "selected " + webcam.getName());

                    panel = new WebcamPanel(webcam, false);
                    panel.setFPSDisplayed(true);
                    panel.setDisplayDebugInfo(false);
                    panel.setImageSizeDisplayed(true);
                    if (screengrab_active != 1)
                    {
                        panel.setMirrored(true);
                    }

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
                    try
                    {
                        panel.stop();
                    }
                    catch (Exception e2)
                    {
                    }

                    try
                    {
                        remove(panel);
                    }
                    catch (Exception e2)
                    {
                    }

                    webcam = (Webcam) e.getItem();
                    if (screengrab_active == 1)
                    {
                        webcam.setViewSize(
                                new Dimension(width_screengrab, height_screengrab)); //(WebcamResolution.VGA.getSize());
                    }
                    else
                    {
                        if (current_driver.equals(DRIVER_720P))
                        {
                            webcam.setViewSize(WebcamResolution.HD.getSize());
                        }
                        else
                        {
                            webcam.setViewSize(WebcamResolution.VGA.getSize());
                        }
                    }
                    width = webcam.getViewSize().width;
                    height = webcam.getViewSize().height;
                    Log.i(TAG, "size3:w=" + width + " h=" + height);
                    if (screengrab_active != 1)
                    {
                        webcam.setImageTransformer(this);
                    }
                    webcam.addWebcamListener(this);

                    Log.i(TAG, "selected " + webcam.getName());

                    panel = new WebcamPanel(webcam, false);
                    panel.setFPSDisplayed(true);
                    panel.setDisplayDebugInfo(false);
                    panel.setImageSizeDisplayed(true);
                    if (screengrab_active != 1)
                    {
                        panel.setMirrored(true);
                    }

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
            final long ts0 = System.currentTimeMillis();
            final BufferedImage buf = we.getImage();
            if (buf != null)
            {
                try
                {
                    semaphore_video_out_convert.acquire();
                    if (semaphore_video_out_convert_active_threads > semaphore_video_out_convert_max_active_threads)
                    {
                        semaphore_video_out_convert.release();
                        return;
                    }
                    semaphore_video_out_convert.release();
                }
                catch (Exception e)
                {
                }

                final Thread t_convert_frame_and_send = new Thread()
                {
                    @Override
                    public void run()
                    {

                        try
                        {
                            semaphore_video_out_convert.acquire();
                            semaphore_video_out_convert_active_threads++;
                            semaphore_video_out_convert.release();
                        }
                        catch (Exception e)
                        {
                        }

                        try
                        {
                            // Log.i(TAG, "webcam_image:captured:002:start");
                            if (Callstate.state != 0)
                            {
                                // Log.i(TAG, "webcam_image:captured:003");
                                if ((Callstate.friend_pubkey != null) && (Callstate.friend_pubkey.length() > 0))
                                {
                                    // Log.i(TAG, "webcam_image:captured:004");
                                    int frame_width_px = buf.getWidth();
                                    int frame_height_px = buf.getHeight();

                                    if ((video_buffer_2 == null) || (frame_width_px != width) ||
                                        (frame_height_px != height))
                                    {
                                        //Log.i(TAG, "webcam_image:captured:005:w=" + frame_width_px + " h=" +
                                        //           frame_height_px);
                                        int buffer_size_in_bytes2 = (int) (frame_width_px * frame_height_px * 1.5f);
                                        video_buffer_2 = ByteBuffer.allocateDirect(buffer_size_in_bytes2);
                                        set_JNI_video_buffer2(video_buffer_2, frame_width_px, frame_height_px);
                                    }

                                    //final long ts1 = System.currentTimeMillis();
                                    //Log.i(TAG, "webcam_image:captured:002:001");
                                    video_buffer_2.rewind();
                                    //Log.i(TAG, "webcam_image:captured:002:002:" + (System.currentTimeMillis() - ts1));
                                    byte[] b = rgb2yuv(buf, frame_width_px, frame_height_px);
                                    //Log.i(TAG, "webcam_image:captured:002:003:" + (System.currentTimeMillis() - ts1));
                                    // Log.i(TAG, "webcam_image:captured:005a:r=" + video_buffer_2.remaining() + " p=" +
                                    //            video_buffer_2.capacity() + " b=" + b.length);
                                    int len = video_buffer_2.remaining();
                                    if (len > b.length)
                                    {
                                        len = b.length;
                                    }

                                    //Log.i(TAG, "webcam_image:captured:002:004:" + (System.currentTimeMillis() - ts1));
                                    video_buffer_2.put(b, 0, len);
                                    //Log.i(TAG, "webcam_image:captured:002:005:" + (System.currentTimeMillis() - ts1));
                                    int age = (int) (System.currentTimeMillis() - ts0);
                                    if ((age < 0) || (age > 100))
                                    {
                                        age = 0;
                                    }
                                    int res = toxav_video_send_frame_age(
                                            tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), buf.getWidth(),
                                            buf.getHeight(), age);
                                    //Log.i(TAG, "webcam_image:captured:002:006:" + (System.currentTimeMillis() - ts1));
                                    // Log.i(TAG, "webcam_image:captured:006:fn=" +
                                    //            tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) + " res=" + res);
                                }
                            }
                            //Log.i(TAG, "webcam_image:captured:003:done:" + (System.currentTimeMillis() - ts0));

                        }
                        catch (Exception e)
                        {
                        }

                        try
                        {
                            semaphore_video_out_convert.acquire();
                            semaphore_video_out_convert_active_threads--;
                            semaphore_video_out_convert.release();
                        }
                        catch (Exception e)
                        {
                        }

                    }
                };
                t_convert_frame_and_send.start();
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
        try
        {
            if (picker != null)
            {
                picker.addItem(event.getWebcam());
            }
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void webcamGone(WebcamDiscoveryEvent event)
    {
        try
        {
            if (picker != null)
            {
                picker.removeItem(event.getWebcam());
            }
        }
        catch (Exception e)
        {
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
        try
        {
            webcam.close();
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void windowIconified(WindowEvent windowEvent)
    {
        Log.i(TAG, "webcam viewer paused");
        try
        {
            panel.pause();
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent)
    {
        Log.i(TAG, "webcam viewer resumed");
        try
        {
            panel.resume();
        }
        catch (Exception e)
        {
        }
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
        // Log.i(TAG, "rgb2yuv:000");
        // final long ts0 = System.currentTimeMillis();

        byte[] ret = null;

        int w = bi.getWidth();
        int h = bi.getHeight();

        if (1 == 2)
        {
            final byte[] buf_rgba = ((DataBufferByte) bi.getData().getDataBuffer()).getData();

            // Log.i(TAG, "rgb2yuv:runtime:002:" + (System.currentTimeMillis() - ts0));

            //Log.i(TAG, "buf_rgba.len=" + buf_rgba.length);
            //Log.i(TAG, "w * h * 4=" + (w * h * 4));
            //Log.i(TAG, "width1 * height1 * 1.5f=" + (int) (width1 * height1 * 1.5f));

            final java.nio.ByteBuffer rgba_buf = ByteBuffer.allocateDirect(w * h * 4);
            final java.nio.ByteBuffer yuv_buf = ByteBuffer.allocateDirect((int) (width1 * height1 * 1.5f));

            rgba_buf.put(buf_rgba);
            // Log.i(TAG, "rgb2yuv:runtime:007:" + (System.currentTimeMillis() - ts0));
            crgb2yuv(rgba_buf, yuv_buf, width1, height1, w, h);
            // Log.i(TAG, "rgb2yuv:runtime:008:" + (System.currentTimeMillis() - ts0));

            yuv_buf.rewind();
            ret = new byte[(int) (w * h * 1.5f)];
            yuv_buf.get(ret);
        }
        else // ------------------------
        {
            ret = new byte[(int) (w * h * 1.5f)];
            final byte[] ret2 = ret;
            try
            {
                final int arraySize = height1 * width1;

                final int h0 = 0;
                final int h1 = (h / 5);
                final int h2 = 2 * (h / 5);
                final int h3 = 3 * (h / 5);
                final int h4 = 4 * (h / 5);
                final int h5 = h;

                final Thread t1 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        try
                        {
                            for (int j = h0; j < h1; j++)
                            {
                                final int j_w = j * width1;
                                final int factor2 = (j / 2) * (width1 / 2);
                                final int arr_4 = arraySize / 4;
                                for (int i = 0; i < w; i++)
                                {
                                    // final long ts1_1 = System.currentTimeMillis();
                                    int color = bi.getRGB(i, j);
                                    //Log.i(TAG, "rgb2yuv:runtime:01:" + (System.currentTimeMillis() - ts1_1));

                                    // int alpha = color >> 24 & 0xff;
                                    int R = color >> 16 & 0xff;
                                    int G = color >> 8 & 0xff;
                                    int B = color & 0xff;

                                    // int Y = (int) ((0.257 * R) + (0.504 * G) + (0.098 * B) + 16);
                                    // int U = (int) (-(0.148 * R) - (0.291 * G) + (0.439 * B) + 128);
                                    // int V = (int) ((0.439 * R) - (0.368 * G) - (0.071 * B) + 128);

                                    int Y = (int) (R * .299000f + G * .587000f + B * 0.114000f);
                                    int U = (int) (R * -.168736f + G * -.331264f + B * 0.500000f + 128);
                                    int V = (int) (R * .500000f + G * -.418688f + B * -0.081312f + 128);

                                    // Log.i(TAG, "rgb2yuv:runtime:02:" + (System.currentTimeMillis() - ts1_1));

                                    int yLoc = j_w + i;
                                    int uLoc = factor2 + (i / 2) + arraySize;
                                    int vLoc = uLoc + arr_4;

                                    // Log.i(TAG, "rgb2yuv:runtime:03:" + (System.currentTimeMillis() - ts1_1));

                                    ret2[yLoc] = (byte) Y;
                                    ret2[uLoc] = (byte) U;
                                    ret2[vLoc] = (byte) V;

                                    // Log.i(TAG, "rgb2yuv:runtime:04:" + (System.currentTimeMillis() - ts1_1));
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t1.start();

                final Thread t2 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        try
                        {
                            for (int j = h1; j < h2; j++)
                            {
                                final int j_w = j * width1;
                                final int factor2 = (j / 2) * (width1 / 2);
                                final int arr_4 = arraySize / 4;
                                for (int i = 0; i < w; i++)
                                {
                                    // final long ts1_1 = System.currentTimeMillis();
                                    int color = bi.getRGB(i, j);
                                    //Log.i(TAG, "rgb2yuv:runtime:01:" + (System.currentTimeMillis() - ts1_1));

                                    // int alpha = color >> 24 & 0xff;
                                    int R = color >> 16 & 0xff;
                                    int G = color >> 8 & 0xff;
                                    int B = color & 0xff;

                                    // int Y = (int) ((0.257 * R) + (0.504 * G) + (0.098 * B) + 16);
                                    // int U = (int) (-(0.148 * R) - (0.291 * G) + (0.439 * B) + 128);
                                    // int V = (int) ((0.439 * R) - (0.368 * G) - (0.071 * B) + 128);

                                    int Y = (int) (R * .299000f + G * .587000f + B * 0.114000f);
                                    int U = (int) (R * -.168736f + G * -.331264f + B * 0.500000f + 128);
                                    int V = (int) (R * .500000f + G * -.418688f + B * -0.081312f + 128);

                                    // Log.i(TAG, "rgb2yuv:runtime:02:" + (System.currentTimeMillis() - ts1_1));

                                    int yLoc = j_w + i;
                                    int uLoc = factor2 + (i / 2) + arraySize;
                                    int vLoc = uLoc + arr_4;

                                    // Log.i(TAG, "rgb2yuv:runtime:03:" + (System.currentTimeMillis() - ts1_1));

                                    ret2[yLoc] = (byte) Y;
                                    ret2[uLoc] = (byte) U;
                                    ret2[vLoc] = (byte) V;

                                    // Log.i(TAG, "rgb2yuv:runtime:04:" + (System.currentTimeMillis() - ts1_1));
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t2.start();

                final Thread t3 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        try
                        {
                            for (int j = h2; j < h3; j++)
                            {
                                final int j_w = j * width1;
                                final int factor2 = (j / 2) * (width1 / 2);
                                final int arr_4 = arraySize / 4;
                                for (int i = 0; i < w; i++)
                                {
                                    // final long ts1_1 = System.currentTimeMillis();
                                    int color = bi.getRGB(i, j);
                                    //Log.i(TAG, "rgb2yuv:runtime:01:" + (System.currentTimeMillis() - ts1_1));

                                    // int alpha = color >> 24 & 0xff;
                                    int R = color >> 16 & 0xff;
                                    int G = color >> 8 & 0xff;
                                    int B = color & 0xff;

                                    // int Y = (int) ((0.257 * R) + (0.504 * G) + (0.098 * B) + 16);
                                    // int U = (int) (-(0.148 * R) - (0.291 * G) + (0.439 * B) + 128);
                                    // int V = (int) ((0.439 * R) - (0.368 * G) - (0.071 * B) + 128);

                                    int Y = (int) (R * .299000f + G * .587000f + B * 0.114000f);
                                    int U = (int) (R * -.168736f + G * -.331264f + B * 0.500000f + 128);
                                    int V = (int) (R * .500000f + G * -.418688f + B * -0.081312f + 128);

                                    // Log.i(TAG, "rgb2yuv:runtime:02:" + (System.currentTimeMillis() - ts1_1));

                                    int yLoc = j_w + i;
                                    int uLoc = factor2 + (i / 2) + arraySize;
                                    int vLoc = uLoc + arr_4;

                                    // Log.i(TAG, "rgb2yuv:runtime:03:" + (System.currentTimeMillis() - ts1_1));

                                    ret2[yLoc] = (byte) Y;
                                    ret2[uLoc] = (byte) U;
                                    ret2[vLoc] = (byte) V;

                                    // Log.i(TAG, "rgb2yuv:runtime:04:" + (System.currentTimeMillis() - ts1_1));
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t3.start();

                final Thread t4 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        try
                        {
                            for (int j = h3; j < h4; j++)
                            {
                                final int j_w = j * width1;
                                final int factor2 = (j / 2) * (width1 / 2);
                                final int arr_4 = arraySize / 4;
                                for (int i = 0; i < w; i++)
                                {
                                    // final long ts1_1 = System.currentTimeMillis();
                                    int color = bi.getRGB(i, j);
                                    //Log.i(TAG, "rgb2yuv:runtime:01:" + (System.currentTimeMillis() - ts1_1));

                                    // int alpha = color >> 24 & 0xff;
                                    int R = color >> 16 & 0xff;
                                    int G = color >> 8 & 0xff;
                                    int B = color & 0xff;

                                    // int Y = (int) ((0.257 * R) + (0.504 * G) + (0.098 * B) + 16);
                                    // int U = (int) (-(0.148 * R) - (0.291 * G) + (0.439 * B) + 128);
                                    // int V = (int) ((0.439 * R) - (0.368 * G) - (0.071 * B) + 128);

                                    int Y = (int) (R * .299000f + G * .587000f + B * 0.114000f);
                                    int U = (int) (R * -.168736f + G * -.331264f + B * 0.500000f + 128);
                                    int V = (int) (R * .500000f + G * -.418688f + B * -0.081312f + 128);

                                    // Log.i(TAG, "rgb2yuv:runtime:02:" + (System.currentTimeMillis() - ts1_1));

                                    int yLoc = j_w + i;
                                    int uLoc = factor2 + (i / 2) + arraySize;
                                    int vLoc = uLoc + arr_4;

                                    // Log.i(TAG, "rgb2yuv:runtime:03:" + (System.currentTimeMillis() - ts1_1));

                                    ret2[yLoc] = (byte) Y;
                                    ret2[uLoc] = (byte) U;
                                    ret2[vLoc] = (byte) V;

                                    // Log.i(TAG, "rgb2yuv:runtime:04:" + (System.currentTimeMillis() - ts1_1));
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t4.start();

                final Thread t5 = new Thread()
                {
                    @Override
                    public void run()
                    {
                        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                        try
                        {
                            for (int j = h4; j < h5; j++)
                            {
                                final int j_w = j * width1;
                                final int factor2 = (j / 2) * (width1 / 2);
                                final int arr_4 = arraySize / 4;
                                for (int i = 0; i < w; i++)
                                {
                                    // final long ts1_1 = System.currentTimeMillis();
                                    int color = bi.getRGB(i, j);
                                    //Log.i(TAG, "rgb2yuv:runtime:01:" + (System.currentTimeMillis() - ts1_1));

                                    // int alpha = color >> 24 & 0xff;
                                    int R = color >> 16 & 0xff;
                                    int G = color >> 8 & 0xff;
                                    int B = color & 0xff;

                                    // int Y = (int) ((0.257 * R) + (0.504 * G) + (0.098 * B) + 16);
                                    // int U = (int) (-(0.148 * R) - (0.291 * G) + (0.439 * B) + 128);
                                    // int V = (int) ((0.439 * R) - (0.368 * G) - (0.071 * B) + 128);

                                    int Y = (int) (R * .299000f + G * .587000f + B * 0.114000f);
                                    int U = (int) (R * -.168736f + G * -.331264f + B * 0.500000f + 128);
                                    int V = (int) (R * .500000f + G * -.418688f + B * -0.081312f + 128);

                                    // Log.i(TAG, "rgb2yuv:runtime:02:" + (System.currentTimeMillis() - ts1_1));

                                    int yLoc = j_w + i;
                                    int uLoc = factor2 + (i / 2) + arraySize;
                                    int vLoc = uLoc + arr_4;

                                    // Log.i(TAG, "rgb2yuv:runtime:03:" + (System.currentTimeMillis() - ts1_1));

                                    ret2[yLoc] = (byte) Y;
                                    ret2[uLoc] = (byte) U;
                                    ret2[vLoc] = (byte) V;

                                    // Log.i(TAG, "rgb2yuv:runtime:04:" + (System.currentTimeMillis() - ts1_1));
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                t5.start();

                t1.join();
                t2.join();
                t3.join();
                t4.join();
                t5.join();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Log.i(TAG, "rgb2yuv:runtime:099:" + (System.currentTimeMillis() - ts0));

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
        if (screengrab_active != 1)
        {
            g2.drawImage(IMAGE_FRAME, null, 0, 0);
        }
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

    static void change_webcam_driver(String driver_name, boolean start_cam)
    {
        if (start_cam)
        {
            try
            {
                panel.stop();
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
            try
            {
                VideoOutFrame1.remove(picker);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                VideoOutFrame1.remove(panel);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                webcam.removeWebcamListener(VideoOutFrame1);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                webcam.close();
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }

        current_driver = driver_name;

        if (driver_name.equals(DRIVER_SCREENGRAB_4K))
        {
            try
            {
                Log.i(TAG, "DRIVER_SCREENGRAB_4K");
                screengrab_active = 1;
                Webcam.setDriver(new FFmpegScreenDriver(FFmpegScreenDriver.FFMPEG_SCREEN_GRAB_SCREEN_RES_4k));
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_SCREENGRAB_1080P))
        {
            try
            {
                Log.i(TAG, "DRIVER_SCREENGRAB_1080P");
                screengrab_active = 1;
                Webcam.setDriver(new FFmpegScreenDriver(FFmpegScreenDriver.FFMPEG_SCREEN_GRAB_SCREEN_RES_1080p));
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_SCREENGRAB_720P))
        {
            try
            {
                Log.i(TAG, "DRIVER_SCREENGRAB_720P");
                screengrab_active = 1;
                Webcam.setDriver(new FFmpegScreenDriver(FFmpegScreenDriver.FFMPEG_SCREEN_GRAB_SCREEN_RES_720p));
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_SCREENGRAB_CAMOVERLAY_1080p))
        {
            try
            {
                Log.i(TAG, "DRIVER_SCREENGRAB_CAMOVERLAY_1080p");
                screengrab_active = 1;
                Webcam.setDriver(
                        new FFmpegScreenDriver(FFmpegScreenDriver.FFMPEG_SCREEN_GRAB_SCREEN_RES_CAMOVERLAY_1080p));
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_DUMMY))
        {
            try
            {
                Log.i(TAG, "DRIVER_DUMMY");
                screengrab_active = 2;
                Webcam.setDriver(new WebcamDummyDriver(1));
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_DEFAULT))
        {
            try
            {
                Log.i(TAG, "DRIVER_DEFAULT");
                screengrab_active = 0;
                Webcam.setDriver(new WebcamDefaultDriver());
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else if (driver_name.equals(DRIVER_720P))
        {
            try
            {
                Log.i(TAG, "DRIVER_720P");
                screengrab_active = 0;
                Webcam.setDriver(new WebcamDefaultDriver());
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
        }
        else // DRIVER_OFF ------------
        {
            Log.i(TAG, "DRIVER_OFF");
            screengrab_active = 0;

            try
            {
                panel.stop();
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                VideoOutFrame1.remove(picker);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
            try
            {
                VideoOutFrame1.remove(panel);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                webcam.removeWebcamListener(VideoOutFrame1);
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }

            try
            {
                webcam.close();
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }


            EventQueue.invokeLater(() -> {
                if ((VideoOutFrame1 != null) && (VideoOutFrame1.isShowing()))
                {
                    VideoOutFrame1.revalidate();
                    VideoOutFrame1.repaint();
                }
            });

            return;
        }

        if (start_cam)
        {
            picker = new WebcamPicker();
            picker.addItemListener(VideoOutFrame1);

            webcam = picker.getSelectedWebcam();
            try
            {
                if (screengrab_active == 1)
                {
                    webcam.setViewSize(
                            new Dimension(width_screengrab, height_screengrab)); //(WebcamResolution.VGA.getSize());
                }
                else
                {
                    if (driver_name.equals(DRIVER_720P))
                    {
                        Log.i(TAG, "available_resolutions_for_cam:===============================");
                        for (Dimension d : webcam.getViewSizes())
                        {
                            Log.i(TAG, "available_resolutions_for_cam:" + d.width + "x" + d.height);
                        }
                        Log.i(TAG, "available_resolutions_for_cam:===============================");
                        webcam.setViewSize(WebcamResolution.HD.getSize());
                    }
                    else
                    {
                        webcam.setViewSize(WebcamResolution.VGA.getSize());
                    }
                }
            }
            catch (Exception e2)
            {
            }

            try
            {
                width = webcam.getViewSize().width;
                height = webcam.getViewSize().height;
                Log.i(TAG, "size5:w=" + width + " h=" + height);
            }
            catch (Exception e2)
            {
                width = 640;
                height = 480;
            }

            try
            {
                if (screengrab_active != 1)
                {
                    webcam.setImageTransformer(VideoOutFrame1);
                }
                webcam.addWebcamListener(VideoOutFrame1);
            }
            catch (Exception e2)
            {
            }

            try
            {
                Log.i(TAG, "selected " + webcam.getName());

                panel = new WebcamPanel(webcam, false);
            }
            catch (Exception e2)
            {
            }

            try
            {
                panel.setFPSDisplayed(true);
                panel.setDisplayDebugInfo(false);
                panel.setImageSizeDisplayed(true);
                if (screengrab_active != 1)
                {
                    panel.setMirrored(true);
                }
            }
            catch (Exception e2)
            {
            }

            try
            {
                VideoOutFrame1.add(picker, BorderLayout.NORTH);
            }
            catch (Exception e2)
            {
            }

            try
            {
                VideoOutFrame1.add(panel, BorderLayout.CENTER);
            }
            catch (Exception e2)
            {
            }

            try
            {
                VideoOutFrame1.revalidate();
            }
            catch (Exception e2)
            {
            }

            Thread t = new Thread()
            {

                @Override
                public void run()
                {
                    try
                    {
                        panel.start();
                    }
                    catch (Exception e2)
                    {
                    }
                }
            };

            t.setName("trifa_cam2");
            t.setDaemon(true);
            t.setUncaughtExceptionHandler(VideoOutFrame1);
            t.start();
        }

        Log.i(TAG, "change_webcam_driver:w=" + width + " h=" + height);

        EventQueue.invokeLater(() -> {
            if ((VideoOutFrame1 != null) && (VideoOutFrame1.isShowing()))
            {
                VideoOutFrame1.revalidate();
                VideoOutFrame1.repaint();
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent)
    {
        JSlider source = (JSlider) changeEvent.getSource();
        if (source == VideoOutBitRate)
        {
            if (!source.getValueIsAdjusting())
            {

                final int vbitrate_wanted = source.getValue();

                final Thread t_set_video_out_bitrate = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (vbitrate_wanted == 0)
                            {
                                toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                 ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_BITRATE_AUTOSET.value,
                                                 1);
                            }
                            else
                            {
                                int res1 = toxav_bit_rate_set(
                                        tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                        NORMAL_GLOBAL_AUDIO_BITRATE, vbitrate_wanted);

                                int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                            vbitrate_wanted);

                                int res3 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                            ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_BITRATE_AUTOSET.value,
                                                            0);
                            }
                        }
                        catch (Exception e)
                        {
                            Log.i(TAG, "stateChanged:EE01:" + e.getMessage());
                        }
                    }
                };
                t_set_video_out_bitrate.start();
            }
            EventQueue.invokeLater(() -> {
                VideoOutBitRate_text.setText("" + source.getValue());
            });
        }
    }
}
