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
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JComboBox;

import static com.zoffcc.applications.trifa.AudioFrame.audio_out_select;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_SMALL_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.jni_iterate_videocall_audio;
import static java.awt.Font.PLAIN;

/*
 *
 *  This selects the Audio Playback Device, and iterates (processes to be ready to play) incoming Audio
 *
 */
public class AudioSelectOutBox extends JComboBox implements ItemListener, LineListener
{
    private static final String TAG = "trifa.AudioSelectOutBox";

    static SourceDataLine sourceDataLine = null;
    static AudioFormat audioformat = null;
    static PCMWaveFormDisplay pcm_wave_play = null;

    final static Semaphore semaphore_audio_out_convert = new Semaphore(1);
    static int semaphore_audio_out_convert_active_threads = 0;
    static int semaphore_audio_out_convert_max_active_threads = 2;
    final static Semaphore semaphore_audio_device_changes = new Semaphore(1);


    final static int SAMPLE_RATE_DEFAULT = 48000;
    final static int CHANNELS_DEFAULT = 1;
    static int SAMPLE_RATE = SAMPLE_RATE_DEFAULT;
    static int CHANNELS = CHANNELS_DEFAULT;
    static int SAMPLE_SIZE_BIT = 16;
    public final static int n_buf_iterate_ms = 60; // fixed ms interval for audio play (call and groups)

    public AudioSelectOutBox()
    {
        super();
        setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);

        setRenderer(new AudioSelectionRenderer());

        reload_device_list(this);
        addItemListener(this);

        final Thread t_audio_play_iterate = new Thread()
        {
            @Override
            public void run()
            {
                this.setName("t_a_play");
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
                // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Log.i(TAG, "Priority of thread is NEW: " + Thread.currentThread().getPriority());

                int delta = 0;
                final int sleep_millis = n_buf_iterate_ms; // "x" ms is what native audio wants
                int sleep_millis_current = sleep_millis;
                long d1 = 0;
                long d2 = 0;
                int res = 0;

                /*
                while (true)
                {
                    try
                    {
                        //Log.i(TAG, "t_audio_play:001");
                        if (Callstate.state != 0)
                        {
                            // Log.i(TAG, "t_audio_play:002:sourceDataLine.isOpen=" + sourceDataLine.isOpen() +
                            //           " sourceDataLine.isActive=" + sourceDataLine.isActive());
                            if (sourceDataLine != null)
                            {
                                // Log.i(TAG, "t_audio_play:003:Callstate.state=" + Callstate.state);
                                if (sourceDataLine.isOpen())
                                {
                                    //Log.i(TAG, "t_audio_play:jni_iterate_videocall_audio:" +
                                    //           (System.currentTimeMillis() - d1) + " ms");
                                    //d1 = System.currentTimeMillis();
                                    res = jni_iterate_videocall_audio(0, sleep_millis, CHANNELS, SAMPLE_RATE, 0);
                                    if (res == -1)
                                    {
                                        Thread.sleep(1);
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

                                    Thread.sleep(sleep_millis_current); // sleep
                                }
                                else
                                {
                                    Thread.sleep(60);
                                }
                            }
                            else
                            {
                                Thread.sleep(60);
                            }
                        }
                        else
                        {
                            Thread.sleep(60);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "t_audio_play:EE01:" + e.getMessage());
                    }
                }
                */
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
                        a.addItem(mixerInfo[cnt]);
                    }

                    for (Line t : currentMixer.getTargetLines())
                    {
                        Log.i(TAG, "T:mixer_line:" + t.getLineInfo());
                    }

                    for (Line t : currentMixer.getSourceLines())
                    {
                        Log.i(TAG, "S:mixer_line:" + t.getLineInfo());
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
                        a.addItem(mixerInfo[cnt]);
                    }
                }
            }
        };
        t.start();
    }

    public static void change_audio_format(int sample_rate, int channels)
    {
        try
        {
            Log.i(TAG, "AA::OUT::change_audio_format:001:" + sample_rate + " " + channels);
            Log.i(TAG, "change_audio_format:sample_rate=" + sample_rate + " SAMPLE_RATE=" + SAMPLE_RATE + " channels=" +
                       channels + " CHANNELS=" + CHANNELS);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            SAMPLE_RATE = sample_rate;
            CHANNELS = channels;
            change_device((Mixer.Info) audio_out_select.getSelectedItem());
            Log.i(TAG, "AA::OUT::change_audio_format:099");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized static void change_device(Mixer.Info i)
    {
        try
        {
            semaphore_audio_device_changes.acquire();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "AA::OUT::change_device:001:" + i.getDescription() + " " + SAMPLE_RATE + " " + CHANNELS);

        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);

        Log.i(TAG, "change_device:001");

        Log.i(TAG, "select audio out:" + i.getDescription());

        // Log.i(TAG, "select audio in:?:" + mixerInfo[cnt].getDescription());
        Mixer currentMixer = AudioSystem.getMixer(i);

        Log.i(TAG, "select audio out:" + "sel:" + i.getDescription());

        if (sourceDataLine != null)
        {
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
                sourceDataLine.flush();
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

            sourceDataLine = null;
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

            // sourceDataLine = (SourceDataLine) currentMixer.getLine(dataLineInfo);
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

            if (sourceDataLine.isRunning())
            {
                Log.i(TAG, "isRunning:2:TRUE");
            }
            else
            {
                Log.i(TAG, "isRunning:2:**false**");
            }
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

        Log.i(TAG, "change_device:099");

        semaphore_audio_device_changes.release();
        Log.i(TAG, "AA::OUT::change_device:099");
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
            try
            {
                change_device((Mixer.Info) e.getItem());
            }
            catch (Exception e2)
            {
                // e2.printStackTrace();
            }
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
