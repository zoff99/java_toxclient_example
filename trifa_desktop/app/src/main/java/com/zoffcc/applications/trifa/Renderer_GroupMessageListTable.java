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
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import static com.zoffcc.applications.trifa.HelperGeneric.darkenColor;
import static com.zoffcc.applications.trifa.HelperGeneric.hash_to_bucket;
import static com.zoffcc.applications.trifa.HelperGeneric.isColorDarkBrightness;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGroup.get_group_peernum_from_peer_pubkey;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_peer_get_name__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_PAGING_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_peer_id;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_self_get_public_key;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_CHATCOLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static java.awt.Font.PLAIN;

public class Renderer_GroupMessageListTable extends JPanel implements TableCellRenderer
{
    private static final String TAG = "trifa.Rndr_GroupMessageList";

    final JLabel m_date_time = new JLabel();
    final JTextArea m_text = new JTextArea();
    final JPanel date_line = new JPanel(true);

    Renderer_GroupMessageListTable()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int col)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));

        GroupMessage m = (GroupMessage) value;

        String message__text = m.text;

        if (m.private_message == 1)
        {
            message__text = "Private Message:\n" + m.text;
        }

        String message__tox_peername = m.tox_group_peername;
        String message__tox_peerpubkey = m.tox_group_peer_pubkey;

        boolean is_system_message = message__tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY);

        if (is_system_message)
        {
            if ((m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)) ||
                (m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)))
            {
                message__tox_peername = "";
            }
            else
            {
                message__tox_peername = "-system-";
            }
        }

        boolean handle_special_name = false;

        name_test_pk res = correct_pubkey(m);
        if (res.changed)
        {
            try
            {
                message__tox_peername = res.tox_peername;
                message__text = res.text;
                if (m.private_message == 1)
                {
                    message__text = "Private Message:\n" + res.text;
                }
                message__tox_peerpubkey = res.tox_peerpubkey;
                handle_special_name = true;
            }
            catch (Exception e)
            {
            }
        }

        m_text.setText(message__text);
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
                Color peer_color_bg;
                if (is_system_message)
                {
                    if ((m.msg_id_hash != null) && ((m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)) ||
                                                    (m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_NEWER_HASH))))
                    {
                        peer_color_bg = ChatColors.SystemColors[1];
                    }
                    else
                    {
                        peer_color_bg = TRIFA_SYSTEM_MESSAGE_PEER_CHATCOLOR;
                    }
                }
                else
                {
                    peer_color_bg = ChatColors.get_shade(
                            ChatColors.PeerAvatarColors[hash_to_bucket(message__tox_peerpubkey, ChatColors.get_size())],
                            message__tox_peerpubkey);
                    // peer_color_bg_with_alpha = (peer_color_bg & 0x00FFFFFF) | (alpha_value << 24);
                    m_text.setForeground(Color.BLACK);
                }

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

        if (m.direction == 0) // incoming message
        {
            date_time_text = (long_date_time_format(m.sent_timestamp));
        }
        else // outgoing message
        {
            date_time_text = (long_date_time_format(m.rcvd_timestamp));
        }

        try
        {
            String peer_name = tox_group_peer_get_name__wrapper(m.group_identifier, message__tox_peerpubkey);
            if (m.direction == 1)
            {
                peer_name = tox_group_peer_get_name__wrapper(m.group_identifier, tox_group_self_get_public_key(
                        tox_group_by_groupid__wrapper(m.group_identifier)));
            }

            String peerpubkey_short = "";
            if (!is_system_message)
            {
                peerpubkey_short = message__tox_peerpubkey.substring((message__tox_peerpubkey.length() - 6),
                                                                     message__tox_peerpubkey.length());
            }

            if (peer_name == null)
            {
                peer_name = message__tox_peername;
                if ((peer_name == null) || (message__tox_peername.equals("")) || (peer_name.equals("-1")))
                {
                    peer_name = "Unknown";
                }
            }
            else
            {
                if (peer_name.equals("-1"))
                {
                    if ((message__tox_peername == null) || (message__tox_peername.equals("")))
                    {
                        peer_name = "Unknown";
                    }
                    else
                    {
                        peer_name = message__tox_peername;
                    }
                }
            }

            try
            {
                if (is_system_message)
                {
                    date_time_text = date_time_text + " -system-";
                }
                else
                {
                    date_time_text = date_time_text + " " + (peer_name + " / " + peerpubkey_short);
                }
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

        m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));

        try
        {
            if (is_system_message)
            {
                if ((m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)) ||
                    (m.msg_id_hash.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)))
                {
                    date_time_text = "";
                    m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_PAGING_BUTTON_SIZE));
                }
            }
        }
        catch (Exception ignored)
        {
        }

        m_date_time.setText(date_time_text);

        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
        m_date_time.setIconTextGap(0);

        // m_date_time.setBorder(new LineBorder(Color.GREEN));
        m_date_time.setHorizontalAlignment(SwingConstants.LEFT);
        // setBorder(new LineBorder(Color.BLUE));

        date_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));

        date_line.add(m_date_time);

        add(m_text);

        add(date_line);

        setSize(table.getColumnModel().getColumn(col).getWidth(), Short.MAX_VALUE);
        // Now get the fitted height for the given width
        int rowHeight = this.getPreferredSize().height;

        // Get the current table row height
        int actualRowHeight = table.getRowHeight(row);

        // Set table row height to fitted height.
        // Important to check if this has been done already
        // to prevent a never-ending loop.
        if (rowHeight != actualRowHeight)
        {
            table.setRowHeight(row, rowHeight);
        }

        return this;
    }

    class name_test_pk
    {
        boolean changed;
        String tox_peername;
        String text;
        String tox_peerpubkey;
    }

    name_test_pk correct_pubkey(GroupMessage m)
    {
        name_test_pk ret = new name_test_pk();
        ret.changed = false;
        return ret;
    }
}
