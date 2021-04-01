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

import com.kevinnovate.jemojitable.EmojiTable;
import com.vdurmont.emoji.Emoji;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static java.awt.Font.PLAIN;

public class EmojiFrame extends JFrame implements EmojiTable.EmojiSelectListener
{
    private static final String TAG = "trifa.EmojiFrame";

    public static int width = 300;
    public static int height = 300;

    public EmojiFrame()
    {
        super("Emoji");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // HINT: for some reason "columnCount" needs to be exactly "8"
        EmojiTable table = new EmojiTable(8, new java.awt.Font("default", PLAIN, 18), true);

        table.setRowHeight(22);
        table.setDoubleClickListener(this);

        JPanel panel1 = new JPanel(true);
        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.add(table);  //scroll pane is a JScrollPane added to a JPanel somewhere
        scrollPane1.setViewportView(table);

        panel1.add(scrollPane1);
        this.add(panel1);
        this.pack();
        this.setVisible(true);
    }

    @Override
    public void userSelectedEmoji(Emoji selected_emoji)
    {
        // add emoji to text input field at current position
        EventQueue.invokeLater(() -> {
            try
            {
                messageInputTextField.insert(selected_emoji.getUnicode(), messageInputTextField.getCaretPosition());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
