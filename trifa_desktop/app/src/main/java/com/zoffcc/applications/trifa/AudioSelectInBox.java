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
import java.nio.ByteBuffer;

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
import static com.zoffcc.applications.trifa.AudioSelectOutBox.semaphore_audio_device_changes;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.set_JNI_audio_buffer;
import static com.zoffcc.applications.trifa.MainActivity.toxav_audio_send_frame;
import static java.awt.Font.PLAIN;

/*
 *
 *  This selects the Audio Recording Device, and sends outgoing Audio to toxcore
 *
 */
public class AudioSelectInBox extends JComboBox implements ItemListener, LineListener
{
    private static final String TAG = "trifa.AudioSelectInBox";
    public static final float AUDIO_VU_MIN_VALUE = -20;

    static TargetDataLine targetDataLine = null;
    static AudioFormat audioformat = null;
    static PCMWaveFormDisplay pcm_wave_rec = null;

    final int AUDIO_REC_SAMPLE_RATE = 48000;
    final int AUDIO_REC_CHANNELS = 2;
    final int AUDIO_REC_SAMPLE_SIZE_BIT = 16;

    public AudioSelectInBox()
    {
        super();
        setFont(new java.awt.Font("monospaced", PLAIN, 7));

        audioformat = new AudioFormat(AUDIO_REC_SAMPLE_RATE, AUDIO_REC_SAMPLE_SIZE_BIT, AUDIO_REC_CHANNELS, true,
                                      false);

        setRenderer(new AudioSelectionRenderer());

        reload_device_list(this);
        addItemListener(this);
        final Thread t_audio_rec = new Thread()
        {
            @Override
            public void run()
            {
                this.setName("t_va_rec");

                int sample_count = 0;
                int audio_send_res = 1;
                int numBytesRead = 0;

                int frameduration_ms = 40;
                int sample_count2 = ((AUDIO_REC_SAMPLE_RATE * frameduration_ms) / 1000);
                int want_numBytesRead = sample_count2 * AUDIO_REC_CHANNELS * 2;

                byte[] data = new byte[want_numBytesRead];
                ByteBuffer _recBuffer = ByteBuffer.allocateDirect(want_numBytesRead);
                set_JNI_audio_buffer(_recBuffer);

                try
                {
                    Log.i(TAG, "t_audio_rec:sleep11");
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                    Log.i(TAG, "t_audio_rec:EE000");
                }

                while ((audio_in_select == null) || (!audio_in_select.isShowing()))
                {
                    try
                    {
                        Log.i(TAG, "t_audio_rec:sleep");
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "t_audio_rec:EE00");
                    }
                }

                Log.i(TAG, "Priority of thread is CUR: " + Thread.currentThread().getPriority());
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                Log.i(TAG, "Priority of thread is NEW: " + Thread.currentThread().getPriority());

                while (true)
                {
                    try
                    {
                        // Log.i(TAG, "t_audio_rec:001");
                        if (targetDataLine != null)
                        {
                            // Log.i(TAG, "t_audio_rec:002");
                            if (targetDataLine.isOpen())
                            {
                                // Log.i(TAG, "t_audio_rec:003:Callstate.state" + Callstate.state);
                                if (Callstate.state != 0)
                                {
                                    // Log.i(TAG, "t_audio_rec:003a")

                                    // HINT: this may block. but it's ok it will not block any Tox or UI threads
                                    numBytesRead = targetDataLine.read(data, 0, data.length);
                                    sample_count = ((numBytesRead / 2) / AUDIO_REC_CHANNELS);

                                    // Log.i(TAG, "sample_count=" + sample_count + " sample_count2=" + sample_count2 +
                                    //           " frameduration_ms=" + frameduration_ms + " want_numBytesRead=" +
                                    //           want_numBytesRead);

                                    // Log.i(TAG, "t_audio_rec:read:" + numBytesRead + " isRunning=" +
                                    //           targetDataLine.isRunning());

                                    _recBuffer.rewind();
                                    _recBuffer.put(data, 0, data.length);

                                    audio_send_res = toxav_audio_send_frame(
                                            tox_friend_by_public_key__wrapper(Callstate.friend_pubkey), sample_count,
                                            AUDIO_REC_CHANNELS, AUDIO_REC_SAMPLE_RATE);

                                    // Log.i(TAG, "t_audio_rec:res=" + audio_send_res);

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

                                    if (sample_count > 1)
                                    {
                                        for (int i = 0; i < sample_count; i = i + 2)
                                        {
                                            short s = (short) ((data[i] & 0xff) | (data[i + 1] << 8));
                                            pcm_wave_rec.add_pcm((int) s);
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
                    Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());

                    for (Line t : currentMixer.getTargetLines())
                    {
                        Log.i(TAG, "T:mixer_line:" + t.getLineInfo());
                    }

                    for (Line t : currentMixer.getSourceLines())
                    {
                        Log.i(TAG, "S:mixer_line:" + t.getLineInfo());
                    }

                    if (currentMixer.isLineSupported(targetDLInfo))
                    {
                        // Log.i(TAG, "ADD:" + cnt);
                        a.addItem(mixerInfo[cnt]);
                    }
                }

                a.addItem("-----------------------------------");

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    // Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (!currentMixer.isLineSupported(targetDLInfo))
                    {
                        // Log.i(TAG, "ADD:+++:" + cnt);
                        a.addItem(mixerInfo[cnt]);
                    }
                }
            }
        };
        t.start();
    }

    public synchronized void change_device(Mixer.Info i)
    {
        try
        {
            semaphore_audio_device_changes.acquire();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.i(TAG, "AA::IN::change_device:001:" + i.getDescription());
        Log.i(TAG, "select audio in:" + i.getDescription());

        try
        {
            Mixer currentMixer = AudioSystem.getMixer(i);
            Log.i(TAG, "select audio in:" + "sel:" + i.getDescription());

            if (targetDataLine != null)
            {
                try
                {
                    targetDataLine.stop();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                try
                {
                    targetDataLine.flush();
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                try
                {
                    targetDataLine.close();
                    Log.i(TAG, "select audio in:" + "close old line");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }

                targetDataLine = null;
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

                // targetDataLine = (TargetDataLine) currentMixer.getLine(dataLineInfo);
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

                if (targetDataLine.isRunning())
                {
                    Log.i(TAG, "isRunning:TRUE");
                }
                else
                {
                    Log.i(TAG, "isRunning:**false**");
                }

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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        semaphore_audio_device_changes.release();
        Log.i(TAG, "AA::IN::change_device:099");
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
            change_device((Mixer.Info) e.getItem());
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
