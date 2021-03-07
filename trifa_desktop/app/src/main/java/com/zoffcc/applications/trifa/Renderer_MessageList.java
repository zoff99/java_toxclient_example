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

import org.swingk.multiline.MultilineLabel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.MainActivity.mainStyle;
import static java.awt.Font.PLAIN;

public class Renderer_MessageList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_MessageList";

    final JLabel m_name = new JLabel();
    final JLabel m_date_time = new JLabel();
    MultilineLabel m_text = null;

    Renderer_MessageList()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // setLayout(new GridBagLayout());

        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        Style defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        mainStyle = sc.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "monospaced");
        StyleConstants.setFontSize(mainStyle, 9);

        m_text = new MultilineLabel(); // new JTextPane(doc);
    }

    @Override
    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
    {
        Message m = (Message) value;

        /*
        m_text.setEditable(true);
        m_text.setSelectionStart(0);
        m_text.setSelectionEnd(m_text.getText().length());
        m_text.setCharacterAttributes(mainStyle, true);
        m_text.replaceSelection(m.text);
        m_text.setEditable(false);
        */

        m_text.setText(m.text);
        // m_text.setPreferredWidthLimit(20); // the label's preferred width won't exceed 330 pixels
        m_text.setLineSpacing(1.0f); // relative spacing between adjacent text lines
        m_text.setBorder(new LineBorder(Color.RED));


        if (m.direction == 0)
        {
            m_name.setText(get_friend_name_from_pubkey(m.tox_friendpubkey));
        }
        else
        {
            m_name.setText("self");
        }

        final String unicode_PERSONAL_COMPUTER = "\uD83D\uDCBB";
        final String unicode_INCOMING_ENVELOPE = "\uD83D\uDCE8";
        final String unicode_Mobile_Phone_With_Arrow = "\uD83D\uDCF2";
        final String unicode_MEMO = "\uD83D\uDCDD";
        final String unicode_ARROW_LEFT = "←";

        if (m.msg_version == 1)
        {
            m_date_time.setText(unicode_ARROW_LEFT + long_date_time_format(m.sent_timestamp) + "\n" +
                                unicode_Mobile_Phone_With_Arrow + long_date_time_format(m.rcvd_timestamp));
        }
        else
        {
            m_date_time.setText(long_date_time_format(m.rcvd_timestamp));
        }


        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, 7));
        m_name.setFont(new java.awt.Font("monospaced", PLAIN, 7));
        m_text.setFont(new java.awt.Font("default", PLAIN, 9));

        m_date_time.setBorder(new LineBorder(Color.GREEN));
        setBorder(new LineBorder(Color.BLUE));
        
        add(m_text);
        add(m_date_time);
        // add(m_name);

        this.setVisible(true);

        return this;
    }
}
