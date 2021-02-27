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

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class VideoInFrame extends JFrame
{
    private static final String TAG = "trifa.VideoInFrame";

    static JPictureBox video_in_frame = null;
    static int width = 640;
    static int height = 480;

    public VideoInFrame()
    {
        super("TRIfA - Video in");

        setSize(640 / 2, 480 / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        Log.i(TAG, "001");
        video_in_frame = new JPictureBox();
        Log.i(TAG, "002");
        add(video_in_frame);
        Log.i(TAG, "003");
        setup_video_in_resolution(640, 480);
        Log.i(TAG, "004");
    }

    public static void setup_video_in_resolution(int w, int h)
    {
        while (!video_in_frame.isShowing())
        {
            try
            {
                Log.i(TAG, "sleep");
                Thread.sleep(10);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        byte[] imageInByte = new byte[w * h * 3];
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        try
        {
            for (int j = 0; j < h; j++)
            {
                for (int i = 0; i < w; i++)
                {
                    int rColor = getRGBFromStream(i, j, w, h, imageInByte);
                    image.setRGB(i, j, rColor);
                }
            }
            Log.i(TAG, "bImageFromConvert=" + image);
            ImageIcon i = new ImageIcon(image);
            if (i != null)
            {
                video_in_frame.setIcon(i);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        width = w;
        height = h;
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
}