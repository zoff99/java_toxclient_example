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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static com.zoffcc.applications.trifa.AudioFrame.reset_audio_bars;
import static com.zoffcc.applications.trifa.AudioSelectOutBox.change_audio_format;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.PREF__audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.VideoInFrame1;
import static com.zoffcc.applications.trifa.MainActivity.addKeyBinding;
import static com.zoffcc.applications.trifa.MainActivity.set_audio_play_volume_percent;
import static com.zoffcc.applications.trifa.MainActivity.tox_set_onion_active;
import static com.zoffcc.applications.trifa.MainActivity.toxav_bit_rate_set;
import static com.zoffcc.applications.trifa.MainActivity.toxav_option_set;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.VideoOutFrame.VideoCallStopButton;
import static com.zoffcc.applications.trifa.VideoOutFrame.VideoOutBitRate_text;
import static com.zoffcc.applications.trifa.VideoOutFrame.screengrab_active;

/*
 *
 * This Plays incoming Video
 *
 */
public class VideoInFrame extends JFrame
{
    private static final String TAG = "trifa.VideoInFrame";

    static String Videoinframe_title_prefix = "TRIfA - Video in";
    static String Videoinframe_title_part1 = " - [";
    static String Videoinframe_title_part2 = "]";

    static JPictureBox video_in_frame = null;
    static JPanel wrapperPanel = null;
    public static int width = 640;
    public static int height = 480;
    static byte[] imageInByte = null;
    static BufferedImage imageIn = null;

    final static Semaphore semaphore_video_in_convert = new Semaphore(1);
    static int semaphore_video_in_convert_active_threads = 0;
    static int semaphore_video_in_convert_max_active_threads = 1;

    public VideoInFrame()
    {
        super(Videoinframe_title_prefix);

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        video_in_frame = new JPictureBox();
        video_in_frame.setSize(width / 2, height / 2);
        video_in_frame.setBackground(Color.ORANGE);

        wrapperPanel = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
        wrapperPanel.add(video_in_frame);
        getContentPane().add(wrapperPanel);

        setup_video_in_resolution(width, height, (int) (width * height * 1.5f));

        addKeyBinding(getRootPane(), "F11", new FullscreenToggleAction(this));
    }

    public static void new_video_in_frame(ByteBuffer vbuf, int w, int h)
    {
        try
        {
            semaphore_video_in_convert.acquire();
            if (semaphore_video_in_convert_active_threads >= semaphore_video_in_convert_max_active_threads)
            {
                semaphore_video_in_convert.release();
                //Log.i(TAG,
                //      "semaphore_video_in_convert_active_threads:" + semaphore_video_in_convert_active_threads + " " +
                //      semaphore_video_in_convert_max_active_threads);
                return;
            }
            semaphore_video_in_convert.release();
        }
        catch (Exception e)
        {
        }

        try
        {
            vbuf.rewind();
            vbuf.get(imageInByte, 0, imageInByte.length);

            final Thread paint_thread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        semaphore_video_in_convert.acquire();
                        semaphore_video_in_convert_active_threads++;
                        semaphore_video_in_convert.release();
                    }
                    catch (Exception e)
                    {
                    }

                    // final long tt1 = System.currentTimeMillis();

                    try
                    {
                        final int h0 = 0;
                        final int h1 = (h / 3);
                        final int h2 = 2 * (h / 3);
                        final int h3 = h;

                        final Thread t1 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h0; j < h1; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t1.start();

                        final Thread t2 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h1; j < h2; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t2.start();

                        final Thread t3 = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                int rColor;
                                int i;
                                for (int j = h2; j < h3; j++)
                                {
                                    for (i = 0; i < w; i++)
                                    {
                                        try
                                        {
                                            rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                            imageIn.setRGB(i, j, rColor);
                                        }
                                        catch (Exception e)
                                        {
                                            // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                            // e.printStackTrace();
                                            try
                                            {
                                                imageIn.setRGB(i, j, 0);
                                            }
                                            catch (Exception e2)
                                            {
                                            }
                                        }
                                    }
                                }
                            }
                        };
                        t3.start();

                        t1.join();
                        t2.join();
                        t3.join();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // Log.i(TAG, "new_video_in_frame:006:" + (System.currentTimeMillis() - tt1) + " ms");

                    if (Callstate.state != 0)
                    {
                        //final long tt2 = System.currentTimeMillis();
                        ImageIcon i = new ImageIcon(imageIn);
                        //Log.i(TAG, "new_video_in_frame:007:" + (System.currentTimeMillis() - tt2) + " ms");
                        try
                        {
                            if (i != null)
                            {
                                //final long tt3 = System.currentTimeMillis();
                                video_in_frame.setIcon(i);
                                //Log.i(TAG, "new_video_in_frame:008:" + (System.currentTimeMillis() - tt3) + " ms");
                                EventQueue.invokeLater(() -> {
                                    try
                                    {
                                        if (i != null)
                                        {
                                            //final long tt4 = System.currentTimeMillis();
                                            video_in_frame.repaint();
                                            //Log.i(TAG, "new_video_in_frame:009:" + (System.currentTimeMillis() - tt4) +
                                            //           " ms");
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                    }
                                });

                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }

                    try
                    {
                        semaphore_video_in_convert.acquire();
                        semaphore_video_in_convert_active_threads--;
                        semaphore_video_in_convert.release();
                    }
                    catch (Exception e)
                    {
                    }
                }
            };
            paint_thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_video_in_frame:007:EE02:" + e.getMessage());
        }
        // Log.i(TAG, "new_video_in_frame:099");
    }

    public static void setup_video_in_resolution(int w, int h, int num_bytes)
    {
        Log.i(TAG, "w=" + w + " h=" + h + " num_bytes=" + num_bytes);
        imageInByte = null;
        imageInByte = new byte[num_bytes];
        Log.i(TAG, "w=" + w + " h=" + h + " len=" + (int) ((float) (w * h) * (float) (1.5)));
        imageIn = null;
        imageIn = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        width = w;
        height = h;

        video_in_frame.setSize(width, height);
        video_in_frame.setPreferredSize(new Dimension(width, height));
        video_in_frame.revalidate();
        video_in_frame.repaint();
    }

    public static int unsignedByteToInt(final byte b)
    {
        return (int) b & 0xFF;
    }

    public static int getRGBFromStream(final int x, final int y, final int width, final int height, final byte[] buf)
    {
        final int arraySize = height * width;
        final int Y = unsignedByteToInt(buf[y * width + x]);
        final int U = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize]);
        final int V = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize + arraySize / 4]);

        //~ int R = (int)(Y + 1.370705 * (V-128));
        //~ int G = (int)(Y - 0.698001 * (V-128) - 0.337633 * (U-128));
        //~ int B = (int)(Y + 1.732446 * (U-128));

        int R = (int) (Y + 1.4075 * (V - 128));
        int G = (int) (Y - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
        int B = (int) (Y + 1.7790 * (U - 128));


        if (R > 255)
        {
            R = 255;
        }
        if (G > 255)
        {
            G = 255;
        }
        if (B > 255)
        {
            B = 255;
        }

        if (R < 0)
        {
            R = 0;
        }
        if (G < 0)
        {
            G = 0;
        }
        if (B < 0)
        {
            B = 0;
        }

        return (0xff << 24) | (R << 16) | (G << 8) | B;
    }

    static void on_call_started_actions()
    {
        Log.i(TAG, "on_call_started_actions");
        if (screengrab_active == 1)
        {
            final Thread t_set_bitrates = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(3000);
                        int res1 = toxav_bit_rate_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                      GLOBAL_AUDIO_BITRATE, 6000);

                        int res2 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_MAX_BITRATE.value,
                                                    6000);

                        int res3 = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                                    ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_ENCODER_VIDEO_BITRATE_AUTOSET.value,
                                                    0);

                        EventQueue.invokeLater(() -> {
                            VideoOutBitRate_text.setText("" + 6000);
                        });

                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "on_call_started_actions:EE01:" + e.getMessage());
                    }
                }
            };
            t_set_bitrates.start();
        }

        EventQueue.invokeLater(() -> {
            try
            {
                VideoInFrame1.setTitle(Videoinframe_title_prefix + Videoinframe_title_part1 +
                                       get_friend_name_from_pubkey(Callstate.friend_pubkey) + Videoinframe_title_part2);
            }
            catch (Exception e)
            {
                VideoInFrame1.setTitle(Videoinframe_title_prefix + Videoinframe_title_part1 + "*unknown caller*" +
                                       Videoinframe_title_part2);
            }

            try
            {
                VideoCallStopButton.setBackground(Color.RED);
            }
            catch (Exception e)
            {
            }
        });

        tox_set_onion_active(0);

        try
        {
            set_audio_play_volume_percent(PREF__audio_play_volume_percent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            int res = toxav_option_set(tox_friend_by_public_key__wrapper(Callstate.friend_pubkey),
                                       ToxVars.TOXAV_OPTIONS_OPTION.TOXAV_DECODER_VIDEO_BUFFER_MS.value, 5);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "TOXAV_DECODER_VIDEO_BUFFER_MS:EE:" + e.getMessage());
        }

        // reset audio play format
        change_audio_format((int) AudioSelectOutBox.SAMPLE_RATE_DEFAULT, AudioSelectOutBox.CHANNELS_DEFAULT);
    }

    static void on_call_ended_actions()
    {
        EventQueue.invokeLater(() -> {
            try
            {
                VideoInFrame1.setTitle(Videoinframe_title_prefix);
            }
            catch (Exception e)
            {
            }

            try
            {
                VideoCallStopButton.setBackground(Color.LIGHT_GRAY.brighter());
            }
            catch (Exception e)
            {
            }
        });

        Callstate.reset_values();
        tox_set_onion_active(1);

        // reset audio play format
        change_audio_format((int) AudioSelectOutBox.SAMPLE_RATE_DEFAULT, AudioSelectOutBox.CHANNELS_DEFAULT);

        // TODO: clear videoIn Window contents
        int w = 10;
        int h = 10;
        int num_bytes = (int) ((float) (w * h) * (float) (1.5));

        Log.i(TAG, "w=" + w + " h=" + h + " num_bytes=" + num_bytes);
        Log.i(TAG, "w=" + w + " h=" + h + " len=" + (int) ((float) (w * h) * (float) (1.5)));
        BufferedImage imageIn2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        width = w;
        height = h;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                imageIn.setRGB(i, j, Color.LIGHT_GRAY.getRGB());
            }
        }

        ImageIcon i = new ImageIcon(imageIn2);
        if (i != null)
        {
            video_in_frame.setIcon(i);
            video_in_frame.repaint();
        }

        reset_audio_bars();
    }
}