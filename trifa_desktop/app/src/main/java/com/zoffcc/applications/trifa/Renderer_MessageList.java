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

import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_OTHER_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static java.awt.Font.PLAIN;

public class Renderer_MessageList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_MessageList";

    final JLabel m_date_time = new JLabel();
    final JTextArea m_text = new JTextArea();
    final JPanel date_line = new JPanel();

    Renderer_MessageList()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        Message m = (Message) value;

        m_text.setText(m.text);
        m_text.setLineWrap(true);
        m_text.setWrapStyleWord(true);
        m_text.setOpaque(true);

        if (m.direction == 0)
        {
            // m_name.setText(get_friend_name_from_pubkey(m.tox_friendpubkey));
            m_text.setBackground(CHAT_MSG_BG_OTHER_COLOR);
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
        final String unicode_ARROW_LEFT = "←";

        if (m.msg_version == 1)
        {
            if (m.direction == 0)
            {
                m_date_time.setText(unicode_ARROW_LEFT + " " + long_date_time_format(m.sent_timestamp) + " : " +
                                    unicode_Mobile_Phone_With_Arrow + " " + long_date_time_format(m.rcvd_timestamp));
            }
            else
            {
                m_date_time.setText(unicode_ARROW_LEFT + " " + long_date_time_format(m.sent_timestamp) + " : " +
                                    unicode_Mobile_Phone_With_Arrow + " " + long_date_time_format(m.rcvd_timestamp));
            }
        }
        else
        {
            if (m.direction == 0)
            {
                m_date_time.setText(long_date_time_format(m.rcvd_timestamp));
            }
            else
            {
                m_date_time.setText(long_date_time_format(m.sent_timestamp));
            }
        }

        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, 6));
        m_date_time.setIconTextGap(0);

        m_text.setFont(new java.awt.Font("monospaced", PLAIN, 9));

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
