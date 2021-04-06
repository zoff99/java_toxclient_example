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

import org.imgscalr.Scalr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import static com.zoffcc.applications.trifa.HelperFiletransfer.file_is_image;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.newColorWithAlpha;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_MSG_DATE_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_OTHER_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class Renderer_MessageList extends JPanel implements ListCellRenderer
{
    private static final String TAG = "trifa.Rndr_MessageList";

    final JLabel m_date_time = new JLabel();
    final JTextArea m_text = new JTextArea();
    final JPanel date_line = new JPanel(true);
    final JProgressBar progress_bar = new JProgressBar();
    final ImageIcon message_image = new ImageIcon();
    final JPanel message_image_label_line = new JPanel(true);
    final JLabel message_image_label = new JLabel();
    final JPanel button_line = new JPanel(true);
    final JButton ok_button = new JButton("Ok");
    final JButton cancel_button = new JButton("Cancel");

    Renderer_MessageList()
    {
        date_line.setLayout(new FlowLayout(FlowLayout.LEFT));
        message_image_label_line.setLayout(new FlowLayout(FlowLayout.LEFT));
        progress_bar.setLayout(new FlowLayout(FlowLayout.LEFT));
        // message_image_label.setHorizontalAlignment(SwingConstants.LEFT);
        message_image_label.setIconTextGap(0);
        //message_image_label.setMaximumSize(new Dimension(80, 80));
        //message_image_label.setPreferredSize(new Dimension(80, 80));
        button_line.setLayout(new FlowLayout(FlowLayout.LEFT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
        final String unicode_ARROW_LEFT = "\u2190";

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

        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_MSG_DATE_SIZE));
        m_date_time.setIconTextGap(0);

        m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));

        // m_date_time.setBorder(new LineBorder(Color.GREEN));
        m_date_time.setHorizontalAlignment(SwingConstants.LEFT);
        // setBorder(new LineBorder(Color.BLUE));

        date_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));

        date_line.add(m_date_time);
        add(m_text);

        progress_bar.setValue(0);
        progress_bar.setIndeterminate(false);

        message_image_label.setIcon(null);
        remove(message_image_label_line);

        m._swing_ok = null;
        m._swing_cancel = null;

        remove(button_line);

        if (m.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
        {
            if ((m.filedb_id > 0) && (m.direction == 0))
            {
                // FT complete, file is here
                progress_bar.setValue(1000);

                if (file_is_image(m.filename_fullpath))
                {
                    // show image on component

                    try
                    {
                        BufferedImage bi = ImageIO.read(new File(m.filename_fullpath));
                        Dimension newMaxSize = new Dimension(80, 80);
                        BufferedImage resizedImg = Scalr.resize(bi, Scalr.Method.QUALITY, newMaxSize.width,
                                                                newMaxSize.height);
                        message_image.setImage(resizedImg);

                        if (m.direction == 0)
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_OTHER_COLOR);
                        }
                        else
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_SELF_COLOR);
                        }

                        message_image_label.setIcon(message_image);
                        add(message_image_label_line);
                        message_image_label_line.add(message_image_label);
                        message_image_label_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));
                    }
                    catch (Exception ie)
                    {
                    }
                }

                add(progress_bar);
                progress_bar.setMaximum(1000);
            }
            else if ((m.filedb_id > 0) && (m.direction == 1))
            {
                // FT complete, file is here
                progress_bar.setValue(1000);

                if (file_is_image(m.filename_fullpath))
                {
                    // show image on component

                    try
                    {
                        BufferedImage bi = ImageIO.read(new File(m.filename_fullpath));
                        Dimension newMaxSize = new Dimension(80, 80);
                        BufferedImage resizedImg = Scalr.resize(bi, Scalr.Method.QUALITY, newMaxSize.width,
                                                                newMaxSize.height);
                        message_image.setImage(resizedImg);

                        if (m.direction == 0)
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_OTHER_COLOR);
                        }
                        else
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_SELF_COLOR);
                        }

                        message_image_label.setIcon(message_image);
                        add(message_image_label_line);
                        message_image_label_line.add(message_image_label);
                        message_image_label_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));
                    }
                    catch (Exception ie)
                    {
                    }
                }

                add(progress_bar);
                progress_bar.setMaximum(1000);
            }
            else
            {
                add(progress_bar);
                progress_bar.setMaximum(1000);

                if (file_is_image(m.filename_fullpath) && (m.direction == 1))
                {
                    // show image on component
                    try
                    {
                        BufferedImage bi = ImageIO.read(new File(m.filename_fullpath));
                        Dimension newMaxSize = new Dimension(80, 80);
                        BufferedImage resizedImg = Scalr.resize(bi, Scalr.Method.QUALITY, newMaxSize.width,
                                                                newMaxSize.height);
                        message_image.setImage(resizedImg);

                        if (m.direction == 0)
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_OTHER_COLOR);
                        }
                        else
                        {
                            message_image_label_line.setBackground(CHAT_MSG_BG_SELF_COLOR);
                        }

                        message_image_label.setIcon(message_image);
                        add(message_image_label_line);
                        message_image_label_line.add(message_image_label);
                        message_image_label_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));
                    }
                    catch (Exception ie)
                    {
                    }
                }

                if (m.direction == 1)
                {
                    try
                    {
                        if (m.state == TOX_FILE_CONTROL_PAUSE.value)
                        {
                            m._swing_ok = ok_button;
                            m._swing_cancel = cancel_button;

                            ok_button.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
                            ok_button.setBackground(newColorWithAlpha(Color.decode("0x389A3A"), 150));
                            cancel_button.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
                            cancel_button.setBackground(newColorWithAlpha(Color.decode("0xFE2424"), 150));

                            cancel_button.setVisible(true);

                            if (m.ft_outgoing_started)
                            {
                                ok_button.setVisible(false);
                                button_line.remove(ok_button);
                            }
                            else
                            {
                                ok_button.setVisible(true);
                                button_line.add(ok_button);
                            }

                            button_line.add(cancel_button);
                            add(button_line);
                            button_line.setVisible(true);
                        }
                        else if (m.state == TOX_FILE_CONTROL_RESUME.value)
                        {
                            m._swing_ok = null;
                            m._swing_cancel = cancel_button;

                            ok_button.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
                            ok_button.setBackground(newColorWithAlpha(Color.decode("0x389A3A"), 150));
                            cancel_button.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_BUTTON_SIZE));
                            cancel_button.setBackground(newColorWithAlpha(Color.decode("0xFE2424"), 150));
                            // ***
                            ok_button.setVisible(false);
                            button_line.remove(ok_button);
                            // ***
                            cancel_button.setVisible(true);
                            button_line.add(cancel_button);
                            add(button_line);
                            button_line.setVisible(true);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

                try
                {
                    if ((m.filetransfer_id > 0) && (m.state == TOX_FILE_CONTROL_RESUME.value))
                    {
                        Filetransfer ft = orma.selectFromFiletransfer().idEq(m.filetransfer_id).toList().get(0);

                        if (ft.current_position == ft.filesize)
                        {
                            progress_bar.setValue(1000);
                        }
                        else
                        {
                            float progress_1000 = ((float) ft.current_position / (float) ft.filesize) * 1000.0f;
                            // Log.i(TAG, "m.id=" + m.id + " ftid=" + ft.id + " progress:" + progress_1000 +
                            //            " ft.current_position=" + ft.current_position + " ft.filesize=" + ft.filesize);
                            progress_bar.setValue((int) progress_1000);
                        }
                    }
                    else
                    {
                        progress_bar.setValue(0);
                    }
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                    progress_bar.setIndeterminate(true);
                }
            }
        }
        else
        {
            remove(progress_bar);
        }

        add(date_line);

        return this;
    }
}
