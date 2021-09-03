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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.is_friend_online_real_pk;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.get_relay_for_friend;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDLIST_H;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDLIST_W;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_FLIST_STATS_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SEE_THRU;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Renderer_FriendsAndConfsList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_FriendsAndConfsList";

    final JPanel avatar_container = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
    final JPictureBox avatar = new JPictureBox();
    final JLabel notification = new JLabel("__");
    final JLabel type = new JLabel("_");
    final JLabel status = new JLabel("_");
    final JLabel name = new JLabel();

    Renderer_FriendsAndConfsList()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        avatar.setSize(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H);

        avatar_container.add(avatar);
        avatar_container.setPreferredSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));
        avatar_container.setMaximumSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));
        avatar_container.setMinimumSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));

        add(avatar_container);
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

            try
            {
                final BufferedImage img = ImageIO.read(
                        new File(f.avatar_pathname + File.separator + f.avatar_filename));
                final ImageIcon icon = new ImageIcon(img);
                avatar.setIcon(icon);
            }
            catch (Exception e)
            {
                final BufferedImage img = new BufferedImage(1, 1, TYPE_INT_ARGB);
                img.setRGB(0, 0, SEE_THRU.getRGB());
                final ImageIcon icon = new ImageIcon(img);
                avatar.setIcon(icon);
            }

            avatar.setBackground(Color.ORANGE);
            avatar.repaint();

            name.setText(get_friend_name_from_pubkey(f.tox_public_key_string));

            try
            {
                if (f.TOX_CONNECTION_real == 2)
                {
                    status.setText("U");
                    status.setBackground(Color.GREEN);
                    status.setForeground(Color.BLACK);
                }
                else if (f.TOX_CONNECTION_real == 1)
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

            final String pushurl_for_friend = get_pushurl_for_friend(f.tox_public_key_string);

            if (get_relay_for_friend(f.tox_public_key_string) != null)
            {
                if ((pushurl_for_friend != null) && (pushurl_for_friend.length() > "https://".length()))
                {
                    type.setText("#");
                }
                else
                {
                    type.setText("*");
                }
                int relay_connection_status = is_friend_online_real_pk(get_relay_for_friend(f.tox_public_key_string));
                if (relay_connection_status == 2)
                {
                    type.setBackground(Color.GREEN);
                }
                else if (relay_connection_status == 1)
                {
                    type.setBackground(Color.ORANGE);
                }
                else // == 0
                {
                    type.setBackground(SEE_THRU);
                }
            }
            else
            {
                if ((pushurl_for_friend != null) && (pushurl_for_friend.length() > "https://".length()))
                {
                    type.setText("_");
                    type.setBackground(Color.PINK);
                }
                else
                {
                    type.setText(" ");
                    type.setBackground(SEE_THRU);
                }
            }
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

            final BufferedImage img = new BufferedImage(1, 1, TYPE_INT_ARGB);
            img.setRGB(0, 0, SEE_THRU.getRGB());
            final ImageIcon icon = new ImageIcon(img);
            avatar.setBackground(SEE_THRU);
            avatar.setIcon(icon);
            avatar.repaint();

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
