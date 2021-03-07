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

import javax.swing.JPanel;
import javax.swing.JTextPane;

import static com.zoffcc.applications.trifa.MainActivity.blueSmallStyle;
import static com.zoffcc.applications.trifa.MainActivity.mainStyle;

public class MessageListFragmentJInfo extends JPanel
{
    private static final String TAG = "trifa.MsgListInfo";

    static final JTextPane MessageTextArea = new JTextPane();

    public MessageListFragmentJInfo()
    {
        add(MessageTextArea);
        show_info_text();
    }

    public static void show_info_text()
    {
        MessageTextArea.setSelectionStart(0);
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(mainStyle, true);
        // @formatter:off
        MessageTextArea.replaceSelection("\n\n\n\n\n\n" +
                                         "                 Welcome to\n" +
                                         "                 TRIfA - Desktop\n" +
                                         "\n" +
                                         "                 Your Tox Client for the Desktop\n" +
                                         "                 v" + MainActivity.Version +
                                         "\n");

        MessageTextArea.setSelectionStart(MessageTextArea.getText().length());
        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
        MessageTextArea.setCharacterAttributes(blueSmallStyle, true);

        MessageTextArea.replaceSelection("\n" +
                                         "\n" +
                                         "           https://github.com/zoff99/java_toxclient_example/tree/master/trifa_desktop/app/src/main/java\n" +
                                         "\n");


        // @formatter:on
    }
}
