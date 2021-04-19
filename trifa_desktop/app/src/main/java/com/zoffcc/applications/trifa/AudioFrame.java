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

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import static com.zoffcc.applications.trifa.AudioSelectInBox.pcm_wave_rec;
import static com.zoffcc.applications.trifa.AudioSelectOutBox.pcm_wave_play;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_SMALL_SIZE;
import static java.awt.Font.PLAIN;

public class AudioFrame extends JFrame
{
    private static final String TAG = "trifa.AudioFrame";

    public static int width = 640;
    public static int height = 300;
    static JPanel audio_panel = null;
    static JPanel audio_in_panel = null;
    static JPanel audio_out_panel = null;
    static AudioBar audio_in_bar = null;
    static AudioBar audio_out_bar = null;
    static AudioSelectInBox audio_in_select = null;
    static AudioSelectOutBox audio_out_select = null;
    static JButton audio_in_refesh;
    static JButton audio_out_refesh;

    public AudioFrame()
    {
        super("TRIfA - Audio");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        audio_panel = new JPanel(true);
        audio_panel.setLayout(new BoxLayout(audio_panel, BoxLayout.PAGE_AXIS));

        audio_in_panel = new JPanel(true);
        audio_in_panel.setLayout(new BoxLayout(audio_in_panel, BoxLayout.X_AXIS));
        audio_out_panel = new JPanel(true);
        audio_out_panel.setLayout(new BoxLayout(audio_out_panel, BoxLayout.X_AXIS));

        JLabel audio_in_text = new JLabel("Audio - Mic", SwingConstants.LEFT);
        audio_in_text.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        JLabel audio_out_text = new JLabel("Audio - Speaker", SwingConstants.LEFT);
        audio_out_text.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));

        audio_in_refesh = new JButton("\uD83D\uDD01"); // unicode Clockwise Rightwards and Leftwards Open Circle Arrows
        audio_in_refesh.setMargin(new Insets(0, 5, 0, 5));
        audio_in_refesh.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        audio_out_refesh = new JButton("\uD83D\uDD01"); // unicode Clockwise Rightwards and Leftwards Open Circle Arrows
        audio_out_refesh.setMargin(new Insets(0, 5, 0, 5));
        audio_out_refesh.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));

        audio_in_bar = new AudioBar();
        audio_in_bar.setPreferredSize(new Dimension(5, 1));
        audio_in_bar.setSize(5, 1);
        audio_out_bar = new AudioBar();
        audio_out_bar.setPreferredSize(new Dimension(5, 1));
        audio_out_bar.setSize(5, 1);

        pcm_wave_rec = new PCMWaveFormDisplay();
        pcm_wave_rec.setPreferredSize(new Dimension(200, 100));

        pcm_wave_play = new PCMWaveFormDisplay();
        pcm_wave_play.setPreferredSize(new Dimension(200, 100));

        JPanel dummy = new JPanel();
        dummy.setPreferredSize(new Dimension(200, 2));

        audio_panel.add(audio_in_text);
        audio_panel.add(audio_in_panel);
        audio_panel.add(audio_in_bar);
        audio_panel.add(audio_out_text);
        audio_panel.add(audio_out_panel);
        audio_panel.add(audio_out_bar);
        // audio_panel.add(pcm_wave_rec);
        audio_panel.add(dummy);
        // audio_panel.add(pcm_wave_play);

        audio_in_select = new AudioSelectInBox();
        audio_in_panel.add(audio_in_refesh);
        audio_in_panel.add(audio_in_select);
        audio_in_select.setVisible(true);

        audio_in_refesh.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "audio_in_refesh pressed");
                AudioSelectInBox.reload_device_list(audio_in_select);
            }
        });

        audio_out_select = new AudioSelectOutBox();
        audio_out_panel.add(audio_out_refesh);
        audio_out_panel.add(audio_out_select);

        audio_out_refesh.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Log.i(TAG, "audio_out_refesh pressed");
                AudioSelectOutBox.reload_device_list(audio_out_select);
            }
        });

        this.add(audio_panel);
        this.setVisible(true);
        revalidate();

        reset_audio_bars();
    }

    static void set_audio_in_bar_level(int percent_level)
    {
        AudioBar.set_cur_value(percent_level, audio_in_bar);
    }

    static void set_audio_out_bar_level(int percent_level)
    {
        AudioBar.set_cur_value(percent_level, audio_out_bar);
    }

    static void reset_audio_bars()
    {
        AudioBar.set_cur_value(0, audio_in_bar);
        AudioBar.set_cur_value(0, audio_out_bar);
    }
}
