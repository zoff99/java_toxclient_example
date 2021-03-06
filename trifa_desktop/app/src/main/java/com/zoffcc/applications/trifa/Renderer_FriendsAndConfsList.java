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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import static java.awt.Font.PLAIN;

public class Renderer_FriendsAndConfsList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_FriendsAndConfsList";

    final JLabel l = new JLabel("_"); //<-- this will be an icon instead of a text
    final JLabel lt = new JLabel();

    Renderer_FriendsAndConfsList()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(l);
        add(lt);
        lt.setBorder(new EmptyBorder(0, 3, 0, 0));
        lt.setFont(new java.awt.Font("SansSerif", PLAIN, 8));

        l.setFont(new java.awt.Font("monospaced", PLAIN, 12));
        l.setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        FriendList f = ((CombinedFriendsAndConferences) value).friend_item;

        String pk = f.tox_public_key_string;

        Log.i(TAG, "pk=" + pk);
        Log.i(TAG, "alias_name=" + f.alias_name);
        Log.i(TAG, "name=" + f.name);
        Log.i(TAG, "TOX_CONNECTION=" + f.TOX_CONNECTION);

        lt.setText(f.name);

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

        try
        {
            if (f.TOX_CONNECTION == 2)
            {
                l.setText("U");
                l.setBackground(Color.GREEN);
                l.setForeground(Color.BLACK);
            }
            else if (f.TOX_CONNECTION == 1)
            {
                l.setText("T");
                l.setBackground(Color.ORANGE);
                l.setForeground(Color.BLACK);
            }
            else
            {
                l.setText("_");
                l.setBackground(Color.GRAY);
                l.setForeground(Color.GRAY);
            }
        }
        catch (Exception e)
        {
            l.setText("_");
            l.setBackground(Color.GRAY);
            l.setForeground(Color.GRAY);
        }

        return this;
    }
}
