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
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JComboBox;

import static com.zoffcc.applications.trifa.AudioFrame.audio_out_select;
import static com.zoffcc.applications.trifa.MainActivity.jni_iterate_videocall_audio;
import static java.awt.Font.PLAIN;

public class AudioSelectOutBox extends JComboBox implements ItemListener, LineListener
{
    private static final String TAG = "trifa.AudioSelectOutBox";

    static SourceDataLine sourceDataLine = null;
    static AudioFormat audioformat = null;

    static int SAMPLE_RATE = 48000;
    static int SAMPLE_SIZE_BIT = 16;
    static int CHANNELS = 2;
    public final static int n_buf_iterate_ms = 40; // fixed ms interval for audio play (call and groups)

    public AudioSelectOutBox()
    {
        super();
        setFont(new java.awt.Font("monospaced", PLAIN, 7));
        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);
        reload_device_list(this);
        addItemListener(this);

        final Thread t_audio_play_iterate = new Thread()
        {
            @Override
            public void run()
            {
                this.setName("t_va_play");
                Log.i(TAG, "Videocall_audio_play_thread:starting ...");

                try
                {
                    Log.i(TAG, "t_audio_play:sleep11");
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "t_audio_play:EE000");
                }

                Log.i(TAG, "Videocall_audio_play_thread:starting ... PART 2");

                while ((audio_out_select == null) || (!audio_out_select.isShowing()))
                {
                    try
                    {
                        Log.i(TAG, "t_audio_play:sleep");
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "t_audio_play:EE00");
                    }
                }

                Log.i(TAG, "Videocall_audio_play_thread:starting ... PART 3");

                Log.i(TAG, "Priority of thread is CUR: " + Thread.currentThread().getPriority());
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Log.i(TAG, "Priority of thread is NEW: " + Thread.currentThread().getPriority());

                int delta = 0;
                final int sleep_millis = n_buf_iterate_ms; // "x" ms is what native audio wants
                int sleep_millis_current = sleep_millis;
                long d1 = 0;
                long d2 = 0;
                int res = 0;

                while (true)
                {
                    try
                    {
                        //Log.i(TAG, "t_audio_play:001");
                        if (sourceDataLine != null)
                        {
                            // Log.i(TAG, "t_audio_play:002:sourceDataLine.isOpen=" + sourceDataLine.isOpen() +
                            //           " sourceDataLine.isActive=" + sourceDataLine.isActive());
                            if (sourceDataLine.isOpen())
                            {
                                // Log.i(TAG, "t_audio_play:003:Callstate.state=" + Callstate.state);
                                if (Callstate.state != 0)
                                {

                                    d1 = System.currentTimeMillis();
                                    res = jni_iterate_videocall_audio(0, sleep_millis, CHANNELS, SAMPLE_RATE, 0);
                                    // Log.i(TAG, "t_audio_play:jni_iterate_videocall_audio");
                                    if (res == -1)
                                    {
                                        Thread.sleep(5);
                                        jni_iterate_videocall_audio(0, sleep_millis, CHANNELS, SAMPLE_RATE, 1);
                                    }

                                    delta = (int) (System.currentTimeMillis() - d1);

                                    sleep_millis_current = sleep_millis - delta;
                                    if (sleep_millis_current < 1)
                                    {
                                        sleep_millis_current = 1;
                                    }
                                    else if (sleep_millis_current > sleep_millis + 5)
                                    {
                                        sleep_millis_current = sleep_millis + 5;
                                    }

                                    Thread.sleep(sleep_millis_current - 1, (1000000 - 300)); // sleep
                                }
                                else
                                {
                                    Thread.sleep(50);
                                }
                            }
                            else
                            {
                                Thread.sleep(50);
                            }
                        }
                        else
                        {
                            Thread.sleep(50);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "t_audio_play:EE01:" + e.getMessage());
                    }
                }
            }
        };
        t_audio_play_iterate.start();
    }

    public static void reload_device_list(AudioSelectOutBox a)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                DataLine.Info sourceDLInfo = new DataLine.Info(SourceDataLine.class, audioformat);

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

                a.addItem("-----------------------------------");

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

    public static void change_audio_format(int sample_rate, int channels)
    {
        Log.i(TAG, "change_audio_format:sample_rate=" + sample_rate + " SAMPLE_RATE=" + SAMPLE_RATE + " channels=" +
                   channels + " CHANNELS=" + CHANNELS);
        SAMPLE_RATE = sample_rate;
        CHANNELS = channels;
        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);
        change_device(audio_out_select.getSelectedItem().toString());
    }

    public static void change_device(String device_description)
    {
        Mixer.Info[] mixerInfo;
        mixerInfo = AudioSystem.getMixerInfo();

        Log.i(TAG, "select audio out:" + device_description);

        for (int cnt = 0; cnt < mixerInfo.length; cnt++)
        {
            // Log.i(TAG, "select audio in:?:" + mixerInfo[cnt].getDescription());
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
            if (mixerInfo[cnt].getDescription().equals(device_description))
            {
                Log.i(TAG, "select audio out:" + "sel:" + cnt);

                if (sourceDataLine != null)
                {
                    try
                    {
                        sourceDataLine.flush();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    try
                    {
                        sourceDataLine.stop();
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }

                    try
                    {
                        sourceDataLine.close();
                        Log.i(TAG, "select out out:" + "close old line");
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }

                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioformat);
                try
                {
                    if (currentMixer.isLineSupported(dataLineInfo))
                    {
                        Log.i(TAG, "linesupported:TRUE");
                    }
                    else
                    {
                        Log.i(TAG, "linesupported:**false**");
                    }

                    if (dataLineInfo.isFormatSupported(audioformat))
                    {
                        Log.i(TAG, "linesupported:TRUE");
                    }
                    else
                    {
                        Log.i(TAG, "linesupported:**false**");
                    }

                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

                    if (sourceDataLine.isRunning())
                    {
                        Log.i(TAG, "isRunning:TRUE");
                    }
                    else
                    {
                        Log.i(TAG, "isRunning:**false**");
                    }

                    sourceDataLine.addLineListener(audio_out_select);
                    sourceDataLine.open(audioformat);
                    sourceDataLine.start();
                    Log.i(TAG, "getBufferSize=" + sourceDataLine.getBufferSize());
                }
                catch (SecurityException se1)
                {
                    se1.printStackTrace();
                    Log.i(TAG, "select audio out:EE3:" + se1.getMessage());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    Log.i(TAG, "select audio out:EE2:" + e1.getMessage());
                }
                break;
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            Log.i(TAG, "*************************");
            Log.i(TAG, "******** START **********");
            Log.i(TAG, "*************************");
            Log.i(TAG, "output: " + e.paramString());
            change_device(e.getItem().toString());
            Log.i(TAG, "*************************");
            Log.i(TAG, "********  END  **********");
            Log.i(TAG, "*************************");
        }
    }

    @Override
    public void update(LineEvent lineEvent)
    {
        Log.i(TAG, "update:getType:" + lineEvent.getType());
    }
}