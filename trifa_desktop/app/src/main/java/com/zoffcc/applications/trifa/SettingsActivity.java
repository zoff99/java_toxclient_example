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

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.ownProfileShort;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;

public class SettingsActivity extends JFrame
{
    public static int width = 640;
    public static int height = 480;

    private JLabel label_name = new JLabel(lo.getString("settings_name"));
    private JTextField text_name = new JTextField(20);

    SettingsActivity()
    {
        super("TRIfA - " + lo.getString("settings_title"));

        setSize(width / 2, height / 2);
        this.isVisible();

        JPanel newPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);

        constraints.gridx = 0;
        constraints.gridy = 0;
        newPanel.add(label_name, constraints);

        constraints.gridx = 1;
        newPanel.add(text_name, constraints);

        String current_name = global_my_name;
        text_name.setText(current_name);
        text_name.getDocument().addDocumentListener(new DocumentListener()
        {
            public void changedUpdate(DocumentEvent e)
            {
                process_name_change(e);
            }

            public void removeUpdate(DocumentEvent e)
            {
                process_name_change(e);
            }

            public void insertUpdate(DocumentEvent e)
            {
                process_name_change(e);
            }
        });

        newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                            lo.getString("settings_category_001")));

        add(newPanel);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    void process_name_change(DocumentEvent e)
    {
        if (text_name.getText() == null)
        {
            global_my_name = "";
        }
        else
        {
            global_my_name = text_name.getText();
        }
        tox_self_set_name(global_my_name);
        update_savedata_file_wrapper(MainActivity.password_hash);
        ownProfileShort.setEditable(true);
        ownProfileShort.setText(global_my_name);

        EventQueue.invokeLater(() -> {
            ownProfileShort.setEditable(false);
        });
    }

}
