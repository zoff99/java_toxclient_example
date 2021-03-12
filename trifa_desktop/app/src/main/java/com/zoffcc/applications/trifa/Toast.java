/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

/*
Thanks to:

https://stackoverflow.com/users/590437/holger-brandl

https://stackoverflow.com/a/32817660

 */


class Toast extends JFrame
{
    private final float MAX_OPACITY = 0.8f;
    private final float OPACITY_INCREMENT = 0.05f;
    private final int FADE_REFRESH_RATE = 20;

    private final int WINDOW_RADIUS = 15;
    private final int CHARACTER_LENGTH_MULTIPLIER = 16;
    private final int DISTANCE_FROM_PARENT_BOTTOM = 100;


    public Toast(JFrame owner, String toastText)
    {
        setTitle("");
        setLayout(new GridBagLayout());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true);
        setFocusableWindowState(false);

        setOpacity(0.4f);

        // setup the toast lable
        JLabel b1 = new JLabel(toastText);
        b1.setForeground(Color.WHITE);
        b1.setOpaque(false);
        add(b1);

        setSize(toastText.length() * CHARACTER_LENGTH_MULTIPLIER, 50);

        int x = (int) (owner.getLocation().getX() + (owner.getWidth() / 2));
        int y = (int) (owner.getLocation().getY() + owner.getHeight() - DISTANCE_FROM_PARENT_BOTTOM);
        setLocation(new Point(x, y));

        // configure frame
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), WINDOW_RADIUS, WINDOW_RADIUS));
        getContentPane().setBackground(new Color(0, 0, 0, 170));
    }

    public void fadeIn()
    {
        setOpacity(0);
        setVisible(true);

        final Timer timer = new Timer(FADE_REFRESH_RATE, null);
        timer.setRepeats(true);
        timer.addActionListener(new ActionListener()
        {
            private float opacity = 0;


            @Override
            public void actionPerformed(ActionEvent e)
            {
                opacity += OPACITY_INCREMENT;
                setOpacity(Math.min(opacity, MAX_OPACITY));
                if (opacity >= MAX_OPACITY)
                {
                    timer.stop();
                }
            }
        });

        timer.start();
    }

    public void fadeOut()
    {
        final Timer timer = new Timer(FADE_REFRESH_RATE, null);
        timer.setRepeats(true);
        timer.addActionListener(new ActionListener()
        {
            private float opacity = MAX_OPACITY;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                opacity -= OPACITY_INCREMENT;
                setOpacity(Math.max(opacity, 0));
                if (opacity <= 0)
                {
                    timer.stop();
                    setVisible(false);
                    dispose();
                }
            }
        });

        setOpacity(MAX_OPACITY);
        timer.start();
    }

    public static void makeToast(final JFrame owner, final String toastText, final int durationmillis)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Toast toastFrame = new Toast(owner, toastText);
                    toastFrame.fadeIn();
                    Thread.sleep(durationmillis);
                    toastFrame.fadeOut();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}