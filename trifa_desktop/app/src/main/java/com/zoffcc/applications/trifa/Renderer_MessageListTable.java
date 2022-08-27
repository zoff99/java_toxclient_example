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
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import static com.zoffcc.applications.trifa.HelperFiletransfer.file_is_image;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.newColorWithAlpha;
import static com.zoffcc.applications.trifa.MainActivity.PLACEHOLDER_IMG_RESIZED;
import static com.zoffcc.applications.trifa.MainActivity.PREF__show_image_thumbnails;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_MSG_DATE_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_NAME_REGULAR_SIZE;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_PAGING_BUTTON_SIZE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_OTHER_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CHAT_MSG_BG_SELF_COLOR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_IMAGE_THUMBNAIL_HEIGHT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FT_IMAGE_THUMBNAIL_WIDTH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_FTV2;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class Renderer_MessageListTable extends JPanel implements TableCellRenderer
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

    Renderer_MessageListTable()
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
    public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int col)
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

        // public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean hasFocus)
        Message m = (Message) value;

        m_text.setText(m.text);
        m_text.setLineWrap(true);
        m_text.setWrapStyleWord(true);
        // m_text.setOpaque(true);

        if (m.tox_friendpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY))
        {
            m_text.setBackground(ChatColors.SystemColors[1]);
        }
        else
        {
            if (m.direction == 0)
            {
                m_text.setBackground(CHAT_MSG_BG_OTHER_COLOR);
            }
            else
            {
                m_text.setBackground(CHAT_MSG_BG_SELF_COLOR);
            }
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
            if (m.direction == 0) // receive (incoming)
            {
                m_date_time.setText(
                        is_read + unicode_ARROW_LEFT + " " + sent_ts + " : " + unicode_Mobile_Phone_With_Arrow + " " +
                        rcvd_ts);
            }
            else // send (outgoing)
            {
                if ((m.msg_at_relay) && (!m.read))
                {
                    m_date_time.setText(is_read + unicode_ARROW_LEFT + " " + sent_ts + " : ==>> " +
                                        unicode_Mobile_Phone_With_Arrow + " " + rcvd_ts);
                }
                else
                {
                    m_date_time.setText(
                            is_read + unicode_ARROW_LEFT + " " + sent_ts + " : " + unicode_Mobile_Phone_With_Arrow +
                            " " + rcvd_ts);
                }
            }
        }
        else
        {
            if (m.direction == 0) // receive (incoming)
            {
                m_date_time.setText(is_read + sent_ts);
            }
            else // send (outgoing)
            {
                m_date_time.setText(is_read + rcvd_ts);
            }
        }

        m_date_time.setFont(new java.awt.Font("monospaced", PLAIN, TTF_FONT_FAMILY_MSG_DATE_SIZE));
        m_date_time.setIconTextGap(0);

        m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_NAME_REGULAR_SIZE));

        try
        {
            if (m.tox_friendpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY))
            {
                if ((m.msg_idv3_hash.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)) ||
                    (m.msg_idv3_hash.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)))
                {
                    m_date_time.setText("");
                    m_text.setFont(new java.awt.Font(TTF_FONT_FAMILY_NAME, PLAIN, TTF_FONT_FAMILY_PAGING_BUTTON_SIZE));
                }
            }
        }
        catch (Exception ignored)
        {
        }


        m_date_time.setHorizontalAlignment(SwingConstants.LEFT);

        date_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));

        // ------- DEBUG -------
        // ------- DEBUG -------
        // setBorder(new LineBorder(Color.BLUE));
        // m_date_time.setBorder(new LineBorder(Color.GREEN));
        // date_line.setBorder(new LineBorder(Color.ORANGE));
        // ------- DEBUG -------
        // ------- DEBUG -------


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
            // FT done
            if (m.filedb_id > 0)
            {
                // FT complete, file is here
                progress_bar.setValue(1000);

                if (file_is_image(m.filename_fullpath))
                {
                    // show image on component

                    try
                    {
                        if (PREF__show_image_thumbnails)
                        {
                            BufferedImage bi = ImageIO.read(new File(m.filename_fullpath));
                            Dimension newMaxSize = new Dimension(FT_IMAGE_THUMBNAIL_WIDTH, FT_IMAGE_THUMBNAIL_HEIGHT);
                            BufferedImage resizedImg = Scalr.resize(bi, Scalr.Method.SPEED, newMaxSize.width,
                                                                    newMaxSize.height);
                            message_image.setImage(resizedImg);
                        }
                        else
                        {
                            message_image.setImage(PLACEHOLDER_IMG_RESIZED);
                        }

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
            // FT not done (meaning [not started] or [in progress] or [canceled])
            else
            {
                add(progress_bar);
                progress_bar.setMaximum(1000);

                // FT is image and direction outgoing
                if (file_is_image(m.filename_fullpath) && (m.direction == 1))
                {
                    // show image on component
                    try
                    {
                        if (PREF__show_image_thumbnails)
                        {
                            BufferedImage bi = ImageIO.read(new File(m.filename_fullpath));
                            Dimension newMaxSize = new Dimension(FT_IMAGE_THUMBNAIL_WIDTH, FT_IMAGE_THUMBNAIL_HEIGHT);
                            BufferedImage resizedImg = Scalr.resize(bi, Scalr.Method.SPEED, newMaxSize.width,
                                                                    newMaxSize.height);
                            message_image.setImage(resizedImg);
                        }
                        else
                        {
                            message_image.setImage(PLACEHOLDER_IMG_RESIZED);
                        }
                        message_image_label_line.setBackground(CHAT_MSG_BG_SELF_COLOR);
                        message_image_label.setIcon(message_image);
                        add(message_image_label_line);
                        message_image_label_line.add(message_image_label);
                        message_image_label_line.setBorder(new EmptyBorder(new Insets(-5, -5, -5, -5)));
                    }
                    catch (Exception ie)
                    {
                    }
                }

                // FT is direction outgoing
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
                                m_text.setText("" + m.text + "\n\nWaiting for Friend to accept ...");
                            }
                            else
                            {
                                ok_button.setVisible(true);
                                button_line.add(ok_button);
                                m_text.setText("" + m.text + "\n\nSend this file?");
                            }

                            if (m.ft_outgoing_queued)
                            {
                                ok_button.setVisible(false);
                                button_line.remove(ok_button);
                                m_text.setText("" + m.text + "\n\nqueued ...");
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
                // FT is direction incoming
                else if (m.direction == 0)
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

                            ok_button.setVisible(true);
                            button_line.add(ok_button);

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
                    // FT is in progress (update progress bar)
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
                    // FT is NOT in progress (set progress bar to zero)
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

        if (m.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
        {
            if (m.filetransfer_kind == TOX_FILE_KIND_FTV2.value)
            {
                try
                {
                    m_text.setText("*ftV2*\n" + m_text.getText());
                }
                catch (Exception ignored)
                {
                }
            }
        }

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
}
