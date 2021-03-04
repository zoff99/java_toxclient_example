/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
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

/*
 https://stackoverflow.com/users/974186/ren%c3%a9-link

 https://stackoverflow.com/questions/35846727/when-f11-is-pressed-how-can-i-make-the-window-fullscreen
 */

package com.zoffcc.applications.trifa;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

class FullscreenToggleAction extends AbstractAction
{

    private JFrame frame;
    private GraphicsDevice fullscreenDevice;

    public FullscreenToggleAction(JFrame frame)
    {
        this(frame, GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    public FullscreenToggleAction(JFrame frame, GraphicsDevice fullscreenDevice)
    {
        this.frame = frame;
        this.fullscreenDevice = fullscreenDevice;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        frame.dispose();

        if (frame.isUndecorated())
        {
            fullscreenDevice.setFullScreenWindow(null);
            frame.setUndecorated(false);
        }
        else
        {
            frame.setUndecorated(true);
            fullscreenDevice.setFullScreenWindow(frame);
        }

        frame.setVisible(true);
        frame.repaint();
    }
}