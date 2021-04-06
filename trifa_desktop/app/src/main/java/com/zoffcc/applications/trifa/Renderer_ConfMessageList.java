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
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.HelperConference.tox_conference_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.darkenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGeneric.isColorDarkBrightness;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static java.awt.Font.PLAIN;

public class Renderer_ConfMessageList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_ConfMessageList";

    final JLabel m_date_time = new JLabel();
    final JTextArea m_text = new JTextArea();
    final JPanel date_line = new JPanel(true);

    Renderer_ConfMessageList()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        ConferenceMessage m = (ConferenceMessage) value;

        m_text.setText(m.text);
        m_text.setLineWrap(true);
        m_text.setWrapStyleWord(true);
        m_text.setOpaque(true);

        m_text.setForeground(Color.BLACK);

        if (m.direction == 0)
        {
            // m_name.setText(get_friend_name_from_pubkey(m.tox_friendpubkey));
            // m_text.setBackground(CHAT_MSG_BG_OTHER_COLOR);

            try
            {
                Color peer_color_bg = ChatColors.get_shade(
                        ChatColors.PeerAvatarColors[hash_to_bucket(m.tox_peerpubkey, ChatColors.get_size())],
                        m.tox_peerpubkey);
                // peer_color_bg_with_alpha = (peer_color_bg & 0x00FFFFFF) | (alpha_value << 24);
                m_text.setForeground(Color.BLACK);

                if (isColorDarkBrightness(peer_color_bg))
                {
                    m_text.setForeground(darkenColor(Color.WHITE, 0.1f));
                }

                m_text.setBackground(peer_color_bg);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "color:EE01:" + e.getMessage());
                m_text.setBackground(Color.LIGHT_GRAY);
                m_text.setForeground(Color.BLACK);
            }
        }
        else
        {
            // m_name.setText("self");
            m_text.setBackground(CHAT_MSG_BG_SELF_COLOR);
        }

        final String unicode_PERSONAL_COMPUTER = "\uD83D\uDCBB";
        final String unicode_INCOMING_ENVELOPE = "\uD83D\uDCE8";
        final String unicode_Mobile_Phone_With_Arrow = "\uD83D\uDCF2";
        final String unicode_MEMO = "\uD83D\uDCDD";
        final String unicode_ARROW_LEFT = "\u2190";

        String date_time_text = "";

        if (m.direction == 0)
        {
            date_time_text = (long_date_time_format(m.rcvd_timestamp));
        }
        else
        {
            date_time_text = (long_date_time_format(m.sent_timestamp));
        }


        try
        {
            String peer_name = tox_conference_peer_get_name__wrapper(m.conference_identifier, m.tox_peerpubkey);
            if (peer_name == null)
            {
                peer_name = m.tox_peername;
                if ((peer_name == null) || (m.tox_peername.equals("")) || (peer_name.equals("-1")))
                {
                    peer_name = "Unknown";
                }
            }
            else
            {
                if (peer_name.equals("-1"))
                {
                    if ((m.tox_peername == null) || (m.tox_peername.equals("")))
                    {
                        peer_name = "Unknown";
                    }
                    else
                    {
                        peer_name = m.tox_peername;
                    }
                }
            }

            try
            {
                date_time_text = date_time_text + " " + (peer_name + " / " +
                                                         m.tox_peerpubkey.substring((m.tox_peerpubkey.length() - 6),
                                                                                    m.tox_peerpubkey.length()));
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "bindMessageList:EE2:" + e2.getMessage());

                date_time_text = date_time_text + " " + peer_name;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        m_date_time.setText(date_time_text);

        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        m_date_time.setIconTextGap(0);

        m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));

        // m_date_time.setBorder(new LineBorder(Color.GREEN));
        m_date_time.setHorizontalAlignment(SwingConstants.LEFT);
        // setBorder(new LineBorder(Color.BLUE));

        date_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));

        date_line.add(m_date_time);

        add(m_text);

        add(date_line);

        return this;
    }
}
