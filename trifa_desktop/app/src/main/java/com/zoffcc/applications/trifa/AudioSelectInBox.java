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
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JComboBox;

import static com.zoffcc.applications.trifa.AudioBar.audio_vu;
import static com.zoffcc.applications.trifa.AudioFrame.audio_in_select;
import static com.zoffcc.applications.trifa.AudioFrame.set_audio_in_bar_level;
import static java.awt.Font.PLAIN;

public class AudioSelectInBox extends JComboBox implements ItemListener, LineListener
{
    private static final String TAG = "trifa.AudioSelectInBox";
    private static final float AUDIO_VU_MIN_VALUE = -20;

    static TargetDataLine targetDataLine = null;
    static AudioFormat audioformat = null;

    final float SAMPLE_RATE = 48000.0f;
    final int SAMPLE_SIZE_BIT = 16;
    final int CHANNELS = 2;

    public AudioSelectInBox()
    {
        super();
        setFont(new java.awt.Font("monospaced", PLAIN, 7));
        //audioformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS,
        //                              CHANNELS * 2, SAMPLE_RATE, false);

        audioformat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BIT, CHANNELS, true, false);
        reload_device_list(this);
        addItemListener(this);
        final Thread t_audio_rec = new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        while (!audio_in_select.isShowing())
                        {
                            try
                            {
                                Thread.sleep(20);
                            }
                            catch (Exception e)
                            {
                            }
                        }

                        // Log.i(TAG, "t_audio_rec:001");
                        if (targetDataLine != null)
                        {
                            // Log.i(TAG, "t_audio_rec:002");
                            if (targetDataLine.isOpen())
                            {
                                // Log.i(TAG, "t_audio_rec:003");
                                int numBytesRead;
                                byte[] data = new byte[targetDataLine.getBufferSize() / 5];
                                numBytesRead = targetDataLine.read(data, 0, data.length);
                                //Log.i(TAG,
                                //      "t_audio_rec:read:" + numBytesRead + " isRunning=" + targetDataLine.isRunning());

                                int sample_count = ((numBytesRead / 2) / CHANNELS);
                                float global_audio_out_vu = AUDIO_VU_MIN_VALUE;
                                if (sample_count > 0)
                                {
                                    float vu_value = audio_vu(data, sample_count);
                                    if (vu_value > AUDIO_VU_MIN_VALUE)
                                    {
                                        global_audio_out_vu = vu_value;
                                    }
                                    else
                                    {
                                        global_audio_out_vu = 0;
                                    }
                                }

                                final float global_audio_out_vu_ = global_audio_out_vu;
                                final Thread t_audio_bar_set = new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        //Log.i(TAG, "set_audio_in_bar_level:" + global_audio_out_vu_);
                                        set_audio_in_bar_level((int) global_audio_out_vu_);
                                    }
                                };
                                t_audio_bar_set.start();
                                // Thread.sleep(4);
                            }
                            else
                            {
                                Thread.sleep(50);
                            }
                        }
                        else
                        {
                            // Log.i(TAG, "t_audio_rec:null");
                        }
                    }
                    catch (Exception e)
                    {
                        // Log.i(TAG, "t_audio_rec:EE01:" + e.getMessage());
                    }
                }
            }
        };
        t_audio_rec.start();
    }

    public static void reload_device_list(AudioSelectInBox a)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                DataLine.Info targetDLInfo = new DataLine.Info(TargetDataLine.class, audioformat);

                a.removeAllItems();
                a.revalidate();

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (currentMixer.isLineSupported(targetDLInfo))
                    {
                        // Log.i(TAG, "ADD:" + cnt);
                        a.addItem(mixerInfo[cnt].getDescription());
                    }
                }

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (!currentMixer.isLineSupported(targetDLInfo))
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

        Log.i(TAG, "select audio in:" + device_description);

        for (int cnt = 0; cnt < mixerInfo.length; cnt++)
        {
            // Log.i(TAG, "select audio in:?:" + mixerInfo[cnt].getDescription());
            Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
            if (mixerInfo[cnt].getDescription().equals(device_description))
            {
                Log.i(TAG, "select audio in:" + "sel:" + cnt);

                try
                {
                    targetDataLine.stop();
                    targetDataLine.flush();
                    targetDataLine.close();
                    Log.i(TAG, "select audio in:" + "close old line");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }


                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioformat);
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

                    targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

                    if (targetDataLine.isRunning())
                    {
                        Log.i(TAG, "isRunning:TRUE");
                    }
                    else
                    {
                        Log.i(TAG, "isRunning:**false**");
                    }

                    // targetDataLine = (TargetDataLine) currentMixer.getLine(dataLineInfo);
                    targetDataLine.addLineListener(this);
                    targetDataLine.open(audioformat);
                    targetDataLine.start();
                    Log.i(TAG, "getBufferSize=" + targetDataLine.getBufferSize());
                }
                catch (SecurityException se1)
                {
                    se1.printStackTrace();
                    Log.i(TAG, "select audio in:EE3:" + se1.getMessage());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    Log.i(TAG, "select audio in:EE2:" + e1.getMessage());
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
            Log.i(TAG, "input: " + e.paramString());
            change_device(e.getItem().toString());
            Log.i(TAG, "*************************");
            Log.i(TAG, "********  END  **********");
            Log.i(TAG, "*************************");
        }
    }

    static void showLineInfoFormats(final Line.Info lineInfo)
    {
        if (lineInfo instanceof DataLine.Info)
        {
            final DataLine.Info dataLineInfo = (DataLine.Info) lineInfo;
            Arrays.stream(dataLineInfo.getFormats()).forEach(format -> System.out.println("    " + format.toString()));
        }
    }

    @Override
    public void update(LineEvent lineEvent)
    {
        Log.i(TAG, "update:getType:" + lineEvent.getType());
    }
}
