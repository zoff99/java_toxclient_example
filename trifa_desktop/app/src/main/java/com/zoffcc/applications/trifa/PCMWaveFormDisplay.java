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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class PCMWaveFormDisplay extends JPanel
{
    private static final String TAG = "trifa.PCMWaveFormDisplay";

    List<Integer> pcm_values = null;
    int w_est = 320;
    int h_est = 100;

    PCMWaveFormDisplay()
    {
        super();

        pcm_values = new ArrayList<>();

        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                w_est = e.getComponent().getSize().width;
                h_est = e.getComponent().getSize().height;
            }
        });

        final Thread t_pcm_wave = new Thread()
        {
            @Override
            public void run()
            {
                this.setName("t_pcmwave");

                while (true)
                {
                    try
                    {
                        repaint();
                        Thread.sleep(20);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        t_pcm_wave.start();
    }

    public int normalize(int in)
    {
        return (int) ((in + 32767.0f) * (h_est / 65545.0f));
    }

    public void add_pcm(int pcm_signed_16bit_value)
    {
        pcm_values.add(0, pcm_signed_16bit_value);
        while (pcm_values.size() > w_est)
        {
            pcm_values.remove(pcm_values.size() - 1);
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        setBackground(Color.gray);
        g.setColor(Color.RED);
        for (int i = 0; i < pcm_values.size() - 1; i++)
        {
            g.drawLine(i, normalize(pcm_values.get(i)), i + 1, normalize(pcm_values.get(i + 1)));
        }
    }

}
