package com.zoffcc.applications.trifa;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JComboBox;

import static java.awt.Font.PLAIN;

public class AudioSelectInBox extends JComboBox implements ItemListener
{
    private static final String TAG = "trifa.AudioSelectInBox";

    static TargetDataLine targetDataLine = null;
    AudioFormat audioformat = null;

    public AudioSelectInBox()
    {
        super();

        setFont(new java.awt.Font("monospaced", PLAIN, 7));

        audioformat = new AudioFormat(48000, 16, 1, true, false);

        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
                Line.Info targetDLInfo = new Line.Info(TargetDataLine.class);

                removeAllItems();
                revalidate();

                for (int cnt = 0; cnt < mixerInfo.length; cnt++)
                {
                    Mixer currentMixer = AudioSystem.getMixer(mixerInfo[cnt]);
                    Log.i(TAG, "" + cnt + ":" + mixerInfo[cnt].getDescription());
                    if (currentMixer.isLineSupported(targetDLInfo))
                    {
                        Log.i(TAG, "ADD:" + cnt);
                        addItem(mixerInfo[cnt].getDescription());
                    }
                }
            }
        };
        t.start();

        addItemListener(this);
    }

    public void change_device(String device_description)
    {
        Mixer.Info[] mixerInfo;
        mixerInfo = AudioSystem.getMixerInfo();
        // Line.Info targetDLInfo = new Line.Info(TargetDataLine.class);

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
                    targetDataLine = (TargetDataLine) currentMixer.getLine(dataLineInfo);
                    targetDataLine.open(audioformat);
                    targetDataLine.flush();
                    targetDataLine.start();
                    Log.i(TAG, "select audio in:" + "started line");
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    Log.i(TAG, "select audio in:EE2:" + e1.getMessage());
                }
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        Log.i(TAG, "input: " + e.getItem());
        change_device(e.getItem().toString());
    }
}
