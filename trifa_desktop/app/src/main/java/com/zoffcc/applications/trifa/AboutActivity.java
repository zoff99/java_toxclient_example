/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2022 Zoff <zoff@zoff.cc>
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import static com.zoffcc.applications.trifa.MainActivity.jnictoxcore_version;
import static com.zoffcc.applications.trifa.MainActivity.lo;

public class AboutActivity extends JFrame
{
    public static int width = 1200;
    public static int height = 700;

    private JLabel label_name = new JLabel("JNI Version:  ");
    private JLabel text_name = new JLabel("");

    private JPanel about_main_panel = null;

    AboutActivity()
    {
        super("TRIfA - " + lo.getString("about_title"));

        setSize(width / 2, height / 2);
        setPreferredSize(new Dimension(width / 2, height / 2));

        about_main_panel = new JPanel(true);
        about_main_panel.setLayout(new BoxLayout(about_main_panel, BoxLayout.PAGE_AXIS));

        JPanel panel_general = new JPanel(new GridBagLayout());

        GridBagConstraints constraints_tox = new GridBagConstraints();
        constraints_tox.anchor = GridBagConstraints.WEST;
        constraints_tox.insets = new Insets(0, 0, 0, 0);

        constraints_tox.gridx = 0;
        constraints_tox.gridy = 0;
        panel_general.add(label_name, constraints_tox);

        constraints_tox.gridx = 1;
        panel_general.add(text_name, constraints_tox);

        try
        {
            text_name.setText(jnictoxcore_version());
        }
        catch (Exception ignored)
        {
        }

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        about_main_panel.add(panel_general);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.add(about_main_panel);
        scrollPane1.setViewportView(about_main_panel);
        add(scrollPane1);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.isVisible();
    }
}
