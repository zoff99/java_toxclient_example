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
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static com.zoffcc.applications.trifa.AudioFrame.reset_audio_bars;
import static com.zoffcc.applications.trifa.MainActivity.addKeyBinding;

public class VideoInFrame extends JFrame
{
    private static final String TAG = "trifa.VideoInFrame";

    static JPictureBox video_in_frame = null;
    static JPanel wrapperPanel = null;
    public static int width = 640;
    public static int height = 480;
    static byte[] imageInByte = null;
    static BufferedImage imageIn = null;

    public VideoInFrame()
    {
        super("TRIfA - Video in");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        video_in_frame = new JPictureBox();
        video_in_frame.setSize(width / 2, height / 2);
        video_in_frame.setBackground(Color.ORANGE);

        wrapperPanel = new JPanel(new SingleComponentAspectRatioKeeperLayout());
        wrapperPanel.add(video_in_frame);
        getContentPane().add(wrapperPanel);

        setup_video_in_resolution(width, height, (int) (width * height * 1.5f));

        addKeyBinding(getRootPane(), "F11", new FullscreenToggleAction(this));
    }

    public static void new_video_in_frame(ByteBuffer vbuf, int w, int h)
    {
        if (!video_in_frame.isValid())
        {
            return;
        }

        try
        {
            vbuf.rewind();
            // Log.i(TAG, "new_video_in_frame:1:vbuf:" + vbuf.limit() + " len=" + vbuf.remaining() + " imageInByte:" +
            //            imageInByte.length);

            vbuf.slice().get(imageInByte);

            final Thread paint_thread = new Thread()
            {
                @Override
                public void run()
                {
                    for (int j = 0; j < h; j++)
                    {
                        for (int i = 0; i < w; i++)
                        {
                            try
                            {
                                int rColor = getRGBFromStream(i, j, w, h, imageInByte);
                                imageIn.setRGB(i, j, rColor);
                            }
                            catch (Exception e)
                            {
                                // Log.i(TAG, "new_video_in_frame:EE01:" + e.getMessage());
                                // e.printStackTrace();
                                imageIn.setRGB(i, j, 0);
                            }
                        }
                    }
                    // Log.i(TAG, "new_video_in_frame:006:bImageFromConvert=" + imageIn);
                    ImageIcon i = new ImageIcon(imageIn);
                    if (i != null)
                    {
                        if (Callstate.state != 0)
                        {
                            video_in_frame.setIcon(i);
                            video_in_frame.repaint();
                        }
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

    public static int unsignedByteToInt(byte b)
    {
        return (int) b & 0xFF;
    }

    public static int getRGBFromStream(int x, int y, int width, int height, byte[] buf)
    {
        int arraySize = height * width;
        int Y = unsignedByteToInt(buf[y * width + x]);
        int U = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize]);
        int V = unsignedByteToInt(buf[(y / 2) * (width / 2) + x / 2 + arraySize + arraySize / 4]);

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

        int rColor = (0xff << 24) | (R << 16) | (G << 8) | B;

        return rColor;
    }

    static void on_call_started_actions()
    {
    }

    static void on_call_ended_actions()
    {
        Callstate.reset_values();

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