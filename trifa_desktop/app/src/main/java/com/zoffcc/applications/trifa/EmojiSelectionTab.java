/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2021 Zoff <zoff@zoff.cc>
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

import org.w3c.dom.Document;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.zoffcc.applications.trifa.HelperOSFile.sha256sum_of_file;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_EMOJI_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_SMALL_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static java.awt.Font.PLAIN;

public class EmojiSelectionTab extends JFrame
{
    private static final String TAG = "trifa.EmojiSelectionTab";

    private static final String EMOJI_GROUP_FILE_SHA256SUM = "57nrWKpiKdhyY9RuGPtiQDjqB4wK1Z271pZDzgHI67U=";

    public static int width = 590;
    public static int height = 114;

    public EmojiSelectionTab()
    {
        super("Emoji");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        UIDefaults def = UIManager.getLookAndFeelDefaults();
        // def.put("TabbedPane.tabInsets", new Insets(0, 0, 0, 0));
        // def.put("TabbedPane.selectedTabPadInsets", new Insets(0, 0, 0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            String asset_filename = "." + File.separator + "assets" + File.separator + "emoji_grouping.xml";
            String sha256sum_of_file = sha256sum_of_file(asset_filename);
            Log.i(TAG, "EmojiSelectionTab:sha256sum_of_file=" + sha256sum_of_file);
            // TODO: on some windows systems the checksum does not seem to match?
            // maybe "\r\n" or the file is not read as UTF-8 ?
            if ((sha256sum_of_file.equals(EMOJI_GROUP_FILE_SHA256SUM)) ||
                (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS))
            {
                Document doc = db.parse(new File(asset_filename));
                doc.getDocumentElement().normalize();
                //Log.i(TAG, "Root Element :" + doc.getDocumentElement().getNodeName());
                //Log.i(TAG, "------");
                //Log.i(TAG, "keys:" + doc.getElementsByTagName("key").getLength());
                for (int i = 0; i < doc.getElementsByTagName("key").getLength(); i++)
                {
                    //Log.i(TAG, "key:#" + i + ":" + doc.getElementsByTagName("key").item(i).getTextContent());
                    tabbedPane.addTab(doc.getElementsByTagName("key").item(i).getTextContent(), makePanel(doc, i));
                }
            }
            else
            {
                Log.i(TAG, "EmojiSelectionTab:input file sha256 hash does not match!");
                System.exit(5);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "EE:" + e.getMessage());
        }

        tabbedPane.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_SMALL_SIZE));
        setPreferredSize(new Dimension(width, height + 55));

        this.add(tabbedPane);
        this.pack();
        this.setVisible(true);
    }

    private static JPanel makePanel(Document doc1, int index)
    {
        JPanel p = new JPanel(true);
        JPanel inner_panel = new JPanel(true);

        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setPreferredSize(new Dimension(width, height));
        scrollPane1.add(inner_panel);
        scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane1.setViewportView(inner_panel);
        p.add(scrollPane1);

        int count_emojis_in_this_category_actual = 0;
        int count_emojis_in_this_category = doc1.getElementsByTagName("array").item(index).getChildNodes().getLength();
        Log.i(TAG, "num emojis:" + count_emojis_in_this_category);

        inner_panel.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_EMOJI_REGULAR_SIZE));

        boolean toggle = true;
        for (int i = 0; i < count_emojis_in_this_category; i++)
        {
            String cur_emoji = doc1.getElementsByTagName("array").item(index).getChildNodes().item(i).getTextContent();
            if (toggle)
            {
                toggle = false;
            }
            else
            {
                toggle = true;
                if (cur_emoji != null)
                {
                    // Log.i(TAG, "emoji=X" + cur_emoji + "Y " + cur_emoji.length());
                    JButton b = new JButton(cur_emoji);
                    b.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_EMOJI_REGULAR_SIZE));
                    b.setPreferredSize(new Dimension((width - 10) / 8, TTF_FONT_FAMILY_NAME_EMOJI_REGULAR_SIZE + 8));

                    b.addMouseListener(new MouseAdapter()
                    {
                        @Override
                        public void mousePressed(MouseEvent me)
                        {
                            if (me.getClickCount() == 1)
                            {
                                try
                                {
                                    userSelectedEmoji(b.getText());
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        }
                    });

                    inner_panel.add(b);
                    count_emojis_in_this_category_actual++;
                }
            }
        }

        inner_panel.setLayout(new GridLayout((count_emojis_in_this_category_actual / 8) + 1, 8));
        p.setPreferredSize(new Dimension(width, height));
        return p;
    }

    public static void userSelectedEmoji(String selected_emoji)
    {
        // add emoji to text input field at current position
        EventQueue.invokeLater(() -> {
            try
            {
                messageInputTextField.insert(selected_emoji, messageInputTextField.getCaretPosition());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
