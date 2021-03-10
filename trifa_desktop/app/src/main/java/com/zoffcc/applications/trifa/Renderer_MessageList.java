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
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_OTHER_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class Renderer_MessageList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_MessageList";

    final JLabel m_date_time = new JLabel();
    final JTextArea m_text = new JTextArea();
    final JPanel date_line = new JPanel(true);
    final JProgressBar progress_bar = new JProgressBar();

    Renderer_MessageList()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));
        progress_bar.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        Message m = (Message) value;

        m_text.setText(m.text);
        m_text.setLineWrap(true);
        m_text.setWrapStyleWord(true);
        // m_text.setOpaque(true);

        if (m.direction == 0)
        {
            m_text.setBackground(CHAT_MSG_BG_OTHER_COLOR);
        }
        else
        {
            m_text.setBackground(CHAT_MSG_BG_SELF_COLOR);
        }

        //if (isSelected)
        //{
        //    m_text.setBackground(Color.BLUE);
        //}

        //m_text.setEditable(true);

        final String unicode_PERSONAL_COMPUTER = "\uD83D\uDCBB";
        final String unicode_INCOMING_ENVELOPE = "\uD83D\uDCE8";
        final String unicode_Mobile_Phone_With_Arrow = "\uD83D\uDCF2";
        final String unicode_MEMO = "\uD83D\uDCDD";
        final String unicode_ARROW_LEFT = "←";

        String is_read = "     ";

        if (m.read)
        {
            is_read = "READ ";
        }

        String sent_ts = "";
        String rcvd_ts = "";

        if (m.sent_timestamp > 0)
        {
            sent_ts = long_date_time_format(m.sent_timestamp);
        }

        if (m.rcvd_timestamp > 0)
        {
            rcvd_ts = long_date_time_format(m.rcvd_timestamp);
        }

        if (m.msg_version == 1)
        {
            if (m.direction == 0)
            {
                m_date_time.setText(
                        is_read + unicode_ARROW_LEFT + " " + sent_ts + " : " + unicode_Mobile_Phone_With_Arrow + " " +
                        rcvd_ts);
            }
            else
            {
                m_date_time.setText(
                        is_read + unicode_ARROW_LEFT + " " + sent_ts + " : " + unicode_Mobile_Phone_With_Arrow + " " +
                        rcvd_ts);
            }
        }
        else
        {
            if (m.direction == 0)
            {
                m_date_time.setText(is_read + rcvd_ts);
            }
            else
            {
                m_date_time.setText(is_read + sent_ts);
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

        if (m.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
        {
            progress_bar.setMaximum(1000);

            try
            {
                Filetransfer ft = orma.selectFromFiletransfer().idEq(m.filetransfer_id).toList().get(0);
                float progress_1000 = ((float) ft.current_position / (float) ft.filesize) * 1000.0f;
                //Log.i(TAG,
                //      "ftid=" + ft.id + " progress:" + progress_1000 + " ft.current_position=" + ft.current_position +
                //      " ft.filesize=" + ft.filesize);
                progress_bar.setValue((int) progress_1000);
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }
            add(progress_bar);
        }
        else
        {
            remove(progress_bar);
        }

        return this;
    }
}
