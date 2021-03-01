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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JComboBox;

import static java.awt.Font.PLAIN;

public class AudioSelectOutBox extends JComboBox implements ItemListener
{
    private static final String TAG = "trifa.AudioSelectOutBox";

    static SourceDataLine sourceDataLine = null;
    AudioFormat audioformat = null;

    public AudioSelectOutBox()
    {
        super();
        setFont(new java.awt.Font("monospaced", PLAIN, 7));
        audioformat = new AudioFormat(48000, 16, 1, true, false);
        reload_device_list(this);
        addItemListener(this);
    }

    public static void reload_device_list(AudioSelectOutBox a)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                Line.Info sourceDLInfo = new Line.Info(SourceDataLine.class);

                a.removeAllItems();
                a.revalidate();

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (currentMixer.isLineSupported(sourceDLInfo))
                    {
                        // Log.i(TAG, "ADD:" + cnt);
                        a.addItem(mixerInfo[cnt].getDescription());
                    }
                }

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (!currentMixer.isLineSupported(sourceDLInfo))
                    {
                        // Log.i(TAG, "ADD:+++:" + cnt);
                        a.addItem(mixerInfo[cnt].getDescription());
                    }
                }
            }
        };
        t.start();
    }

    public void change_device(String device_description)
    {
        Mixer.Info[] mixerInfo;
        mixerInfo = AudioSystem.getMixerInfo();

        // Log.i(TAG, "select audio out:" + device_description);

        for (int cnt = 0; cnt < mixerInfo.length; cnt++)
        {
            // Log.i(TAG, "select audio in:?:" + mixerInfo[cnt].getDescription());
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
            if (mixerInfo[cnt].getDescription().equals(device_description))
            {
                // Log.i(TAG, "select audio out:" + "sel:" + cnt);

                try
                {
                    sourceDataLine.close();
                    // Log.i(TAG, "select audio out:" + "close old line");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioformat);
                try
                {
                    sourceDataLine = (SourceDataLine) currentMixer.getLine(dataLineInfo);
                    sourceDataLine.open(audioformat);
                    sourceDataLine.flush();
                    sourceDataLine.start();
                    // Log.i(TAG, "select audio out:" + "started line");
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    // Log.i(TAG, "select audio out:EE2:" + e1.getMessage());
                }
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            // Log.i(TAG, "output: " + e.getItem());
            change_device(e.getItem().toString());
        }
    }
}
