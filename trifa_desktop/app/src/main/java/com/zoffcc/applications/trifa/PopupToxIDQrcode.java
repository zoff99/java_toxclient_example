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

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import io.nayuki.qrcodegen.QrCode;

import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;

public class PopupToxIDQrcode extends JFrame
{
    public static int width = 300;
    public static int height = 340;

    PopupToxIDQrcode()
    {
        super("TRIfA - " + lo.getString("toxid"));

        setSize(width / 2, height / 2);
        setPreferredSize(new Dimension(width / 2, height / 2));
        this.isVisible();

        try
        {
            ImageIcon icon = null;

            if ((global_my_toxid != null) && (global_my_toxid.length() > 1))
            {
                QrCode qr0 = QrCode.encodeText(global_my_toxid, QrCode.Ecc.MEDIUM);
                BufferedImage img = qr0.toImage(10, 1);
                icon = new ImageIcon(img);
            }

            JPictureBox qrcode_image_frame = new JPictureBox();
            qrcode_image_frame.setSize(width / 2, height / 2);
            qrcode_image_frame.setBackground(Color.ORANGE);

            JPanel panel = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
            panel.setBackground(Color.WHITE);
            panel.add(qrcode_image_frame);

            getContentPane().add(panel);
            pack();

            qrcode_image_frame.setIcon(icon);
            qrcode_image_frame.repaint();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
