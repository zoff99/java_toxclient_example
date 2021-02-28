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
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class VideoOutFrame extends JFrame implements ItemListener, WindowListener, WebcamListener, WebcamDiscoveryListener, Thread.UncaughtExceptionHandler
{
    private static final String TAG = "trifa.VideoOutFrame";

    private static WebcamPicker picker = null;
    private static WebcamPanel panel = null;
    private static Webcam webcam = null;

    public static int width = 640;
    public static int height = 480;

    public VideoOutFrame()
    {
        super("TRIfA - Camera");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);

        Webcam.addDiscoveryListener(this);

        picker = new WebcamPicker();
        picker.addItemListener(this);

        webcam = picker.getSelectedWebcam();

        if (webcam != null)
        {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
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
        revalidate();

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
        // Log.i(TAG, "webcam_image:captured");
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
}
