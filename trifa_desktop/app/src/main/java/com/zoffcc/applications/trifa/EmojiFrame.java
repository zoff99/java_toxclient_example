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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static java.awt.Font.PLAIN;

public class EmojiFrame extends JFrame implements EmojiTable.EmojiSelectListener
{
    private static final String TAG = "trifa.EmojiFrame";

    public static int width = 500;
    public static int height = 60;

    public EmojiFrame()
    {
        super("Emoji");

        setSize(width / 2, height / 2);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // HINT: for some reason "columnCount" needs to be exactly "8"
        EmojiTable table = new EmojiTable(8, new java.awt.Font("Twitter Color Emoji", PLAIN, 14), true);

        table.setRowHeight(19);
        table.setDoubleClickListener(this);

        JPanel panel1 = new JPanel(true);
        JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.add(table);  //scroll pane is a JScrollPane added to a JPanel somewhere
        scrollPane1.setViewportView(table);

        panel1.add(scrollPane1);
        scrollPane1.setPreferredSize(new Dimension(width, height));
        this.add(panel1);
        this.pack();
        this.setVisible(true);

        // scrollPane1.getVerticalScrollBar().setValue(200);

        try
        {
            JViewport viewport = (JViewport) table.getParent();
            // HINT: row 16 seems to be the regular smileys
            Rectangle rect = table.getCellRect(16 - 3, 0, true);
            Rectangle r2 = viewport.getVisibleRect();
            table.scrollRectToVisible(new Rectangle(rect.x, rect.y, (int) r2.getWidth(), (int) r2.getHeight()));
            this.revalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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
