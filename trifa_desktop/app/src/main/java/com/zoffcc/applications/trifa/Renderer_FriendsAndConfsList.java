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

import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_FLIST_STATS_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SEE_THRU;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class Renderer_FriendsAndConfsList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_FriendsAndConfsList";

    final JLabel notification = new JLabel("__");
    final JLabel type = new JLabel("_");
    final JLabel status = new JLabel("_");
    final JLabel name = new JLabel();

    Renderer_FriendsAndConfsList()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(notification);
        add(type);
        add(status);
        add(name);
        name.setBorder(new EmptyBorder(0, 3, 0, 0));
        name.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));

        type.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_FLIST_STATS_SIZE));
        type.setOpaque(true);

        status.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_FLIST_STATS_SIZE));
        status.setOpaque(true);

        notification.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_FLIST_STATS_SIZE));
        notification.setOpaque(true);
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

        if (((CombinedFriendsAndConferences) value).is_friend) // --- friend ---
        {
            FriendList f = ((CombinedFriendsAndConferences) value).friend_item;

            name.setText(get_friend_name_from_pubkey(f.tox_public_key_string));

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

            if (get_relay_for_friend(f.tox_public_key_string) != null)
            {
                type.setText("*");
            }
            else
            {
                type.setText(" ");
            }
            type.setBackground(SEE_THRU);
            type.setForeground(Color.BLACK);

            notification.setText("  ");
            notification.setBackground(SEE_THRU);
            notification.setForeground(Color.BLACK);

            try
            {
                int new_messages_count = orma.selectFromMessage().tox_friendpubkeyEq(f.tox_public_key_string).
                        is_newEq(true).count();
                if (new_messages_count > 0)
                {
                    if (new_messages_count > 99)
                    {
                        notification.setText(" +"); //("∞");
                    }
                    else
                    {
                        if (new_messages_count > 9)
                        {
                            notification.setText("" + new_messages_count);
                        }
                        else
                        {
                            notification.setText(" " + new_messages_count);
                        }
                    }
                    notification.setBackground(Color.RED);
                    notification.setForeground(Color.WHITE);
                }
                else
                {
                    notification.setText("  ");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();

                notification.setText("  ");
            }
        }
        else // --- conference ---
        {
            ConferenceDB c = ((CombinedFriendsAndConferences) value).conference_item;

            name.setText(get_conference_title_from_confid(c.conference_identifier) + " " +
                         c.conference_identifier.substring(0, 5));

            status.setText(" ");
            status.setBackground(SEE_THRU);
            status.setForeground(Color.BLACK);

            type.setText("G");
            type.setBackground(Color.GREEN);
            type.setForeground(Color.BLACK);

            notification.setText("  ");
            notification.setBackground(SEE_THRU);
            notification.setForeground(Color.BLACK);

            try
            {
                int new_messages_count = orma.selectFromConferenceMessage().
                        conference_identifierEq(c.conference_identifier).is_newEq(true).count();

                if (new_messages_count > 0)
                {
                    if (new_messages_count > 99)
                    {
                        notification.setText(" +"); //("∞");
                    }
                    else
                    {
                        if (new_messages_count > 9)
                        {
                            notification.setText("" + new_messages_count);
                        }
                        else
                        {
                            notification.setText(" " + new_messages_count);
                        }
                    }
                    notification.setBackground(Color.RED);
                    notification.setForeground(Color.WHITE);
                }
                else
                {
                    notification.setText("  ");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();

                notification.setText("  ");
            }
        }

        return this;
    }
}
