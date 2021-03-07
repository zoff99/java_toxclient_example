/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
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
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.TRIFAGlobals.SEE_THRU;
import static java.awt.Font.PLAIN;

public class Renderer_FriendsAndConfsList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_FriendsAndConfsList";

    final JLabel type = new JLabel("_");
    final JLabel status = new JLabel("_");
    final JLabel name = new JLabel();

    Renderer_FriendsAndConfsList()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(type);
        add(status);
        add(name);
        name.setBorder(new EmptyBorder(0, 3, 0, 0));
        name.setFont(new java.awt.Font("SansSerif", PLAIN, 8));

        type.setFont(new java.awt.Font("monospaced", PLAIN, 12));
        type.setOpaque(true);

        status.setFont(new java.awt.Font("monospaced", PLAIN, 12));
        status.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        try
        {
            if (isSelected)
            {
                setBackground(Color.LIGHT_GRAY);
            }
            else
            {
                setBackground(Color.WHITE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (((CombinedFriendsAndConferences) value).is_friend)
        {
            FriendList f = ((CombinedFriendsAndConferences) value).friend_item;

            name.setText(f.name);

            try
            {
                if (f.TOX_CONNECTION == 2)
                {
                    status.setText("U");
                    status.setBackground(Color.GREEN);
                    status.setForeground(Color.BLACK);
                }
                else if (f.TOX_CONNECTION == 1)
                {
                    status.setText("T");
                    status.setBackground(Color.ORANGE);
                    status.setForeground(Color.BLACK);
                }
                else
                {
                    status.setText("_");
                    status.setBackground(Color.GRAY);
                    status.setForeground(Color.GRAY);
                }
            }
            catch (Exception e)
            {
                status.setText("_");
                status.setBackground(Color.GRAY);
                status.setForeground(Color.GRAY);
            }

            type.setText(" ");
            type.setBackground(SEE_THRU);
            type.setForeground(Color.BLACK);
        }
        else // --- conference ---
        {
            ConferenceDB c = ((CombinedFriendsAndConferences) value).conference_item;

            name.setText(c.name + " " + c.conference_identifier.substring(0, 5));

            status.setText(" ");
            status.setBackground(SEE_THRU);
            status.setForeground(Color.BLACK);

            type.setText("G");
            type.setBackground(Color.GREEN);
            type.setForeground(Color.BLACK);
        }

        return this;
    }
}