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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.ConferenceMessageListFragmentJ.current_conf_id;
import static com.zoffcc.applications.trifa.GroupMessageListFragmentJ.current_group_id;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDLIST_H;
import static com.zoffcc.applications.trifa.MainActivity.AVATAR_FRIENDLIST_W;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_number_is_ours;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_peer_id;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.SEE_THRU;
import static java.awt.Font.PLAIN;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Renderer_ConfPeerList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_ConfPeerList";


    final JPanel avatar_container = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
    final JPictureBox avatar = new JPictureBox();
    final JPanel peercolor_container = new JPanel(new SingleComponentAspectRatioKeeperLayout(), true);
    final JPictureBox peercolor = new JPictureBox();
    final JLabel name = new JLabel();

    Renderer_ConfPeerList()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        avatar.setSize(AVATAR_FRIENDLIST_W / 2, AVATAR_FRIENDLIST_H);

        avatar_container.add(avatar);
        avatar_container.setPreferredSize(new Dimension(AVATAR_FRIENDLIST_W / 2, AVATAR_FRIENDLIST_H));
        avatar_container.setMaximumSize(new Dimension(AVATAR_FRIENDLIST_W / 2, AVATAR_FRIENDLIST_H));
        avatar_container.setMinimumSize(new Dimension(AVATAR_FRIENDLIST_W / 2, AVATAR_FRIENDLIST_H));

        peercolor.setSize(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H);

        peercolor_container.add(peercolor);
        peercolor_container.setPreferredSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));
        peercolor_container.setMaximumSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));
        peercolor_container.setMinimumSize(new Dimension(AVATAR_FRIENDLIST_W, AVATAR_FRIENDLIST_H));

        // add(avatar_container);
        add(peercolor_container);
        add(name);

        name.setBorder(new EmptyBorder(0, 3, 0, 0));
        name.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));
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

        PeerModel p = (PeerModel) value;

        String peerpubkey_short = "";
        try
        {
            peerpubkey_short = p.pubkey.substring((p.pubkey.length() - 6), p.pubkey.length());
        }
        catch (Exception e)
        {
        }

        if (p.offline)
        {
            name.setText("_ " + p.peernum + " " + p.name + " / " + peerpubkey_short);
        }
        else
        {
            name.setText("+ " + p.peernum + " " + p.name + " / " + peerpubkey_short);
        }

        final BufferedImage img1 = new BufferedImage(1, 1, TYPE_INT_ARGB);

        int res = 0;
        if (!p.offline)
        {
            // Log.d(TAG, current_conf_id + " " + tox_conference_by_confid__wrapper(current_conf_id) + " " + p.peernum);
            if (p.type == COMBINED_IS_CONFERENCE)
            {
                res = tox_conference_peer_number_is_ours(tox_conference_by_confid__wrapper(current_conf_id), p.peernum);
            }
            else
            {
                if (tox_group_self_get_peer_id(tox_group_by_groupid__wrapper(current_group_id)) == p.peernum)
                {
                    res = 1;
                }
                else
                {
                    res = 0;
                }
            }
        }

        if (res == 1)
        {
            img1.setRGB(0, 0, CHAT_MSG_BG_SELF_COLOR.getRGB());
        }
        else
        {
            try
            {
                Color peer_color_bg = ChatColors.get_shade(
                        ChatColors.PeerAvatarColors[hash_to_bucket(p.pubkey, ChatColors.get_size())], p.pubkey);
                img1.setRGB(0, 0, peer_color_bg.getRGB());

            }
            catch (Exception e)
            {
                img1.setRGB(0, 0, Color.LIGHT_GRAY.getRGB());
            }
        }

        final ImageIcon icon1 = new ImageIcon(img1);
        peercolor.setBackground(SEE_THRU);
        peercolor.setIcon(icon1);
        peercolor.repaint();

        if (p.offline)
        {
            final BufferedImage img2 = new BufferedImage(1, 1, TYPE_INT_ARGB);
            img2.setRGB(0, 0, Color.ORANGE.getRGB());
            final ImageIcon icon2 = new ImageIcon(img2);
            avatar.setBackground(SEE_THRU);
            avatar.setIcon(icon2);
            avatar.repaint();
        }
        else
        {
            final BufferedImage img3 = new BufferedImage(1, 1, TYPE_INT_ARGB);
            img3.setRGB(0, 0, Color.GREEN.getRGB());
            final ImageIcon icon3 = new ImageIcon(img3);
            avatar.setBackground(SEE_THRU);
            avatar.setIcon(icon3);
            avatar.repaint();
        }

        return this;
    }
}
