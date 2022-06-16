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

import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static com.zoffcc.applications.trifa.HelperGeneric.int_to_boolean;
import static com.zoffcc.applications.trifa.HelperGeneric.set_g_opts;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.MainActivity.PREF__ipv6_enabled;
import static com.zoffcc.applications.trifa.MainActivity.PREF__local_discovery_enabled;
import static com.zoffcc.applications.trifa.MainActivity.PREF__orbot_enabled_to_int;
import static com.zoffcc.applications.trifa.MainActivity.PREF__show_image_thumbnails;
import static com.zoffcc.applications.trifa.MainActivity.PREF__udp_enabled;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.ownProfileShort;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;

public class SettingsActivity extends JFrame
{
    public static int width = 1200;
    public static int height = 700;

    private JLabel label_name = new JLabel(lo.getString("settings_name"));
    private JTextField text_name = new JTextField(20);

    private Checkbox chkbox_001;
    private Checkbox chkbox_002;
    private Checkbox chkbox_003;
    private Checkbox chkbox_004;
    private Checkbox chkbox_005;
    private JTextArea text_001 = new JTextArea(lo.getString("settings_show_image_thumbnails_desc"));

    private JPanel setting_main_panel = null;

    SettingsActivity()
    {
        super("TRIfA - " + lo.getString("settings_title"));

        setSize(width / 2, height / 2);
        setPreferredSize(new Dimension(width / 2, height / 2));

        setting_main_panel = new JPanel(true);
        setting_main_panel.setLayout(new BoxLayout(setting_main_panel, BoxLayout.PAGE_AXIS));

        // ----------- Tox settings -----------
        JPanel panel_tox = new JPanel(new GridBagLayout());

        GridBagConstraints constraints_tox = new GridBagConstraints();
        constraints_tox.anchor = GridBagConstraints.WEST;
        constraints_tox.insets = new Insets(0, 0, 0, 0);

        constraints_tox.gridx = 0;
        constraints_tox.gridy = 0;
        panel_tox.add(label_name, constraints_tox);

        constraints_tox.gridx = 1;
        panel_tox.add(text_name, constraints_tox);

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

        chkbox_002 = new Checkbox(lo.getString("settings_ipv6_enabled_title"),
                                  int_to_boolean(PREF__ipv6_enabled));
        chkbox_002.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    set_g_opts("PREF__ipv6_enabled", "true");
                    PREF__ipv6_enabled = 1;
                }
                else
                {
                    set_g_opts("PREF__ipv6_enabled", "false");
                    PREF__ipv6_enabled = 0;
                }
            }
        });

        constraints_tox.gridx = 1;
        constraints_tox.gridy = 1;
        panel_tox.add(chkbox_002, constraints_tox);

        chkbox_003 = new Checkbox(lo.getString("settings_udp_enabled_title"),
                                  int_to_boolean(PREF__udp_enabled));
        chkbox_003.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    set_g_opts("PREF__udp_enabled", "true");
                    PREF__udp_enabled = 1;
                }
                else
                {
                    set_g_opts("PREF__udp_enabled", "false");
                    PREF__udp_enabled = 0;
                }
            }
        });
        constraints_tox.gridx = 1;
        constraints_tox.gridy = 2;
        panel_tox.add(chkbox_003, constraints_tox);


        chkbox_004 = new Checkbox(lo.getString("settings_local_discovery_title"),
                                  int_to_boolean(PREF__local_discovery_enabled));
        chkbox_004.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    set_g_opts("PREF__local_discovery_enabled", "true");
                    PREF__local_discovery_enabled = 1;
                }
                else
                {
                    set_g_opts("PREF__local_discovery_enabled", "false");
                    PREF__local_discovery_enabled = 0;
                }
            }
        });
        constraints_tox.gridx = 1;
        constraints_tox.gridy = 3;
        panel_tox.add(chkbox_004, constraints_tox);

        chkbox_005 = new Checkbox(lo.getString("settings_orbot_enabled_to_int_title"),
                                  int_to_boolean(PREF__orbot_enabled_to_int));
        chkbox_005.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    set_g_opts("PREF__orbot_enabled_to_int", "true");
                    PREF__orbot_enabled_to_int = 1;
                }
                else
                {
                    set_g_opts("PREF__orbot_enabled_to_int", "false");
                    PREF__orbot_enabled_to_int = 0;
                }
            }
        });
        constraints_tox.gridx = 1;
        constraints_tox.gridy = 4;
        panel_tox.add(chkbox_005, constraints_tox);

        panel_tox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                             lo.getString("settings_category_001")));


        // ----------- General settings -----------

        JPanel panel_general = new JPanel(new GridBagLayout());

        GridBagConstraints constraints_general = new GridBagConstraints();
        constraints_general.anchor = GridBagConstraints.WEST;
        constraints_general.insets = new Insets(0, 0, 0, 0);


        chkbox_001 = new Checkbox(lo.getString("settings_show_image_thumbnails_title"), PREF__show_image_thumbnails);
        chkbox_001.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                {
                    set_g_opts("PREF__show_image_thumbnails", "true");
                    PREF__show_image_thumbnails = true;
                }
                else
                {
                    set_g_opts("PREF__show_image_thumbnails", "false");
                    PREF__show_image_thumbnails = false;
                }
            }
        });

        text_001.setEditable(false);
        text_001.setLineWrap(true);
        text_001.setWrapStyleWord(true);

        constraints_tox.gridx = 0;
        constraints_tox.gridy = 0;
        panel_general.add(text_001, constraints_general);

        constraints_tox.gridy = 1;
        panel_general.add(chkbox_001, constraints_general);


        panel_general.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 lo.getString("settings_category_002")));

        // ----------------------------------------

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        setting_main_panel.add(panel_general);
        setting_main_panel.add(panel_tox);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.add(setting_main_panel);
        scrollPane1.setViewportView(setting_main_panel);
        add(scrollPane1);

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.isVisible();
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
