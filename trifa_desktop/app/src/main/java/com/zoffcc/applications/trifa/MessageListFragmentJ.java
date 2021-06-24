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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import static com.zoffcc.applications.trifa.HelperFiletransfer.get_filetransfer_filenum_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.insert_into_filetransfer_db;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_accepted_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_state_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.update_filetransfer_db_full;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.set_message_accepted_from_id;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.insert_into_message_db;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_state_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.HelperOSFile.run_file;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BORDER_TITLE;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_typing;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class MessageListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.MsgListFrgnt";

    private static final boolean USE_TABLE = false;

    static int global_typing = 0;
    static Thread typing_flag_thread = null;
    final static int TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS = 1000; // 1 second

    static String current_pk = null;
    static long friendnum = -1;
    static long friendnum_prev = -1;
    static boolean attachemnt_instead_of_send = false;
    static boolean is_at_bottom = true;
    static boolean show_only_files = false;

    private static JlistCustom<Message> messagelistitems;
    static DefaultListModel<Message> messagelistitems_model;
    // private static JTable messagelistitems_t;
    // static DefaultTableModel messagelistitems_model_t;
    static JScrollPane MessageScrollPane = null;

    public MessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");
        friendnum = -1;
        current_pk = null;

        // messagelistitems_model_t = new DefaultTableModel();
        // messagelistitems_t = new JTable(messagelistitems_model_t);
        // messagelistitems_t.setShowGrid(false);
        // messagelistitems_t.setShowHorizontalLines(false);
        // messagelistitems_t.setShowVerticalLines(false);
        // messagelistitems_t.setRowMargin(0);
        // messagelistitems_t.setIntercellSpacing(new Dimension(0, 0));
        // messagelistitems_t.setFillsViewportHeight(true);

        messagelistitems_model = new DefaultListModel<>();
        messagelistitems = new JlistCustom<>();
        messagelistitems.setModel(messagelistitems_model);
        messagelistitems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messagelistitems.setSelectedIndex(0);
        messagelistitems.setSelectionModel(new DisabledItemSelectionModel());
        messagelistitems.setCellRenderer(new Renderer_MessageList());
        messagelistitems.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = messagelistitems.locationToIndex(point);
                if (index != -1)
                {
                    // Next calculations assume that text is aligned to left, but are easy to adjust
                    final Message element = (Message) messagelistitems.getModel().getElementAt(index);
                    final Rectangle cellBounds = messagelistitems.getCellBounds(index, index);
                    final Renderer_MessageList renderer = (Renderer_MessageList) messagelistitems.getCellRenderer();
                    final Insets insets = renderer.getInsets();

                    boolean button_pressed = false;

                    // Log.i(TAG,
                    //      "cellBounds.x=" + cellBounds.x + " cellBounds.y=" + cellBounds.y + "  cellBounds.width=" +
                    //      cellBounds.width + "  cellBounds.height=" + cellBounds.height + " element._swing_ok=" +
                    //      element._swing_ok.getBounds());

                    // Ensure that mouse press happened within top/bottom insets
                    if (cellBounds.y + insets.top <= point.y &&
                        point.y <= cellBounds.y + cellBounds.height - insets.bottom)
                    {
                        // msg is FT
                        if (element.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                        {
                            // FT (not-started or in progress) and outgoing
                            if ((element.direction == 1) && ((element.state == TOX_FILE_CONTROL_PAUSE.value) ||
                                                             (element.state == TOX_FILE_CONTROL_RESUME.value)))
                            {
                                Rectangle ok_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    ok_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_ok.getLocation().x +
                                            element._swing_ok.getParent().getLocation().x,
                                            cellBounds.y + element._swing_ok.getLocation().y +
                                            element._swing_ok.getParent().getLocation().y,
                                            element._swing_ok.getBounds().width, element._swing_ok.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                Rectangle cancel_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    cancel_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_cancel.getLocation().x +
                                            element._swing_cancel.getParent().getLocation().x,
                                            cellBounds.y + element._swing_cancel.getLocation().y +
                                            element._swing_cancel.getParent().getLocation().y,
                                            element._swing_cancel.getBounds().width,
                                            element._swing_cancel.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                // ok and cancel button
                                try
                                {
                                    if (ok_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "OK button pressed");
                                        button_pressed = true;

                                        // queue FT
                                        set_message_queueing_from_id(element.id, true);
                                        try
                                        {
                                            element._swing_ok.setVisible(false);
                                        }
                                        catch (Exception ee)
                                        {
                                        }

                                        // update message view
                                        update_single_message_from_messge_id(element.id, true);

                                        Log.i(TAG, "button_ok:OnTouch:009");

                                    }
                                    else if (cancel_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "CANCEL button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            set_message_queueing_from_id(element.id, false);

                                            // cancel FT
                                            Log.i(TAG, "button_cancel:OnTouch:001");
                                            // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_CANCEL.value);

                                            try
                                            {
                                                element._swing_cancel.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e4)
                                        {
                                        }
                                    }
                                    else
                                    {

                                        Log.i(TAG, "button:" + element._swing_ok.getBounds().contains(point) + " " +
                                                   element._swing_ok.getLocation().x + " " +
                                                   element._swing_ok.getLocation().y + " " +
                                                   element._swing_ok.getParent().getLocation().x + " " +
                                                   element._swing_ok.getParent().getLocation().y);

                                        Log.i(TAG, "button:" + element._swing_cancel.getBounds().contains(point) + " " +
                                                   element._swing_cancel.getLocation().x + " " +
                                                   element._swing_cancel.getLocation().y + " " +
                                                   element._swing_cancel.getParent().getLocation().x + " " +
                                                   element._swing_cancel.getParent().getLocation().y);

                                    }
                                }
                                catch (Exception e2)
                                {
                                }
                            }
                            // FT (not-started or in progress) and incoming
                            else if ((element.direction == 0) && ((element.state == TOX_FILE_CONTROL_PAUSE.value) ||
                                                                  (element.state == TOX_FILE_CONTROL_RESUME.value)))
                            {
                                Rectangle ok_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    ok_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_ok.getLocation().x +
                                            element._swing_ok.getParent().getLocation().x,
                                            cellBounds.y + element._swing_ok.getLocation().y +
                                            element._swing_ok.getParent().getLocation().y,
                                            element._swing_ok.getBounds().width, element._swing_ok.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                Rectangle cancel_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    cancel_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_cancel.getLocation().x +
                                            element._swing_cancel.getParent().getLocation().x,
                                            cellBounds.y + element._swing_cancel.getLocation().y +
                                            element._swing_cancel.getParent().getLocation().y,
                                            element._swing_cancel.getBounds().width,
                                            element._swing_cancel.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                // ok and cancel button
                                try
                                {
                                    if (ok_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "OK button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            // accept FT
                                            set_filetransfer_accepted_from_id(element.filetransfer_id);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_RESUME.value);
                                            set_message_accepted_from_id(element.id);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_RESUME.value);
                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_RESUME.value);

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e2)
                                        {
                                            e2.printStackTrace();
                                            Log.i(TAG, "MM2MM:EE1:" + e2.getMessage());
                                        }

                                    }
                                    else if (cancel_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "CANCEL button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            // cancel FT
                                            Log.i(TAG, "button_cancel:OnTouch:001");


                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_CANCEL.value);

                                            try
                                            {
                                                element._swing_cancel.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e4)
                                        {
                                        }
                                    }
                                    else
                                    {

                                        Log.i(TAG, "button:" + element._swing_ok.getBounds().contains(point) + " " +
                                                   element._swing_ok.getLocation().x + " " +
                                                   element._swing_ok.getLocation().y + " " +
                                                   element._swing_ok.getParent().getLocation().x + " " +
                                                   element._swing_ok.getParent().getLocation().y);

                                        Log.i(TAG, "button:" + element._swing_cancel.getBounds().contains(point) + " " +
                                                   element._swing_cancel.getLocation().x + " " +
                                                   element._swing_cancel.getLocation().y + " " +
                                                   element._swing_cancel.getParent().getLocation().x + " " +
                                                   element._swing_cancel.getParent().getLocation().y);

                                    }
                                }
                                catch (Exception e2)
                                {
                                }
                            }

                            if (button_pressed)
                            {
                                return;
                            }

                            // FT is done and file is here
                            if (element.filedb_id > 0)
                            {
                                if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                                {
                                    run_file(element.filename_fullpath);
                                    Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                                }
                            }
                            // FT (canceled or in progress) and outgoing
                            else if (((element.state == TOX_FILE_CONTROL_CANCEL.value) ||
                                      (element.state == TOX_FILE_CONTROL_RESUME.value)) && (element.direction == 1))
                            {
                                if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                                {
                                    run_file(element.filename_fullpath);
                                    Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                                }
                            }
                            //else if (element.direction == 1)
                            //{
                            //    if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                            //    {
                            //        run_file(element.filename_fullpath);
                            //        Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                            //    }
                            //}
                        }
                        // msg is TEXT
                        else
                        {
                            if (SwingUtilities.isLeftMouseButton(e))
                            {
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                        new StringSelection(element.text), null);
                                Toast.makeToast(MainFrame, lo.getString("copied_msg_to_clipboard"), 800);
                            }
                            else
                            {
                                Log.i(TAG, "popup dialog");
                                textAreaDialog(null, element.text, "Message");
                            }
                        }
                    }
                    else
                    {
                    }
                }
                else
                {
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

        MessageScrollPane = new JScrollPane(messagelistitems);
        add(MessageScrollPane);

        revalidate();
    }

    static void setFriendName()
    {
        try
        {
            if (MessagePanel != null)
            {
                EventQueue.invokeLater(() -> {
                    try
                    {
                        MessagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                                get_friend_name_from_pubkey(
                                                                                        MessagePanel.current_pk))

                        );
                        ((TitledBorder) MessagePanel.getBorder()).setTitleFont(
                                new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));
                        MessagePanel.revalidate();
                        MessagePanel.repaint();
                    }
                    catch (Exception e)
                    {
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class DisabledItemSelectionModel extends DefaultListSelectionModel
    {
        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            super.setSelectionInterval(-1, -1);
        }
    }

    /* HINT: send a message to a friend */
    synchronized public static void send_message_onclick()
    {
        Log.i(TAG, "send_message_onclick:---start");

        if (friendnum == -1)
        {
            return;
        }

        String msg = "";
        try
        {
            if (attachemnt_instead_of_send)
            {
                // TODO: no attachments yet
            }
            else
            {
                // send typed message to friend

                msg = messageInputTextField.getText().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                  messageInputTextField.getText().length()));

                Message m = new Message();
                m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
                m.direction = 1; // msg sent
                m.TOX_MESSAGE_TYPE = 0;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.rcvd_timestamp = 0L;
                m.is_new = false; // own messages are always "not new"
                m.sent_timestamp = System.currentTimeMillis();
                m.read = false;
                m.text = msg;
                m.msg_version = 0;
                m.resend_count = 0; // we have tried to resend this message "0" times

                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    MainActivity.send_message_result result = tox_friend_send_message_wrapper(friendnum, 0, msg);
                    long res = result.msg_num;
                    // Log.i(TAG, "tox_friend_send_message_wrapper:result=" + res + " m=" + m);

                    if (res > -1) // sending was OK
                    {
                        m.message_id = res;
                        if (!result.msg_hash_hex.equalsIgnoreCase(""))
                        {
                            // msgV2 message -----------
                            m.msg_id_hash = result.msg_hash_hex;
                            m.msg_version = 1;
                            // msgV2 message -----------
                        }

                        if (!result.raw_message_buf_hex.equalsIgnoreCase(""))
                        {
                            // save raw message bytes of this v2 msg into the database
                            // we need it if we want to resend it later
                            m.raw_msgv2_bytes = result.raw_message_buf_hex;
                        }

                        m.resend_count = 1; // we sent the message successfully

                        long row_id = insert_into_message_db(m, true);
                        m.id = row_id;
                        // Log.i(TAG, "MESSAGEV2_SEND:row_id=" + row_id);
                        // Log.i(TAG, "MESSAGEV2_SEND:MSGv2HASH:3=" + m.msg_id_hash);
                        // Log.i(TAG, "MESSAGEV2_SEND:MSGv2HASH:3raw=" + m.raw_msgv2_bytes);

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    messageInputTextField.setText("");
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        };
                        SwingUtilities.invokeLater(myRunnable);

                        stop_self_typing_indicator_s();
                    }
                    else
                    {
                        // sending was NOT ok

                        // Log.i(TAG, "tox_friend_send_message_wrapper:store pending message" + m);

                        m.message_id = -1;
                        long row_id = insert_into_message_db(m, true);
                        m.id = row_id;

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    messageInputTextField.setText("");
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        };
                        SwingUtilities.invokeLater(myRunnable);

                        stop_self_typing_indicator_s();
                    }
                }
            }
        }
        catch (Exception e)
        {
            msg = "";
            e.printStackTrace();
        }

        // Log.i(TAG,"send_message_onclick:---end");
    }

    static void stop_self_typing_indicator_s()
    {
        stop_self_typing_indicator();
    }

    static void stop_self_typing_indicator()
    {
        if (global_typing == 1)
        {
            global_typing = 0;  // typing = 0
            try
            {
                Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":stop_self_typing_indicator");
                tox_self_set_typing(get_current_friendnum(), global_typing);
            }
            catch (Exception e)
            {
                Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":EE2.b" + e.getMessage());
            }
        }
    }

    synchronized static void add_message(final Message m)
    {
        Runnable myRunnable = () -> {
            try
            {
                add_item(m);
                if (is_at_bottom)
                {
                    // Log.i(TAG, "scroll:" + MessageScrollPane.getVerticalScrollBar().getValue());
                    // Log.i(TAG, "scroll:max:" + MessageScrollPane.getVerticalScrollBar().getMaximum());
                    EventQueue.invokeLater(() -> {
                        MessageScrollPane.getVerticalScrollBar().setValue(
                                MessageScrollPane.getVerticalScrollBar().getMaximum());
                    });
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_message:EE1:" + e.getMessage());
                e.printStackTrace();
            }
        };
        SwingUtilities.invokeLater(myRunnable);
    }

    static void update_all_messages(boolean always)
    {
        // Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            orma.updateMessage().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                    is_new(false).
                    execute();
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (always)
            {
                // Log.i(TAG, "data_values:005a");

                messagelistitems_model.removeAllElements();
                // MessagePanel.revalidate();
                // Log.i(TAG, "data_values:005b");
                if (show_only_files)
                {
                    // TODO:
                }
                else
                {
                    try
                    {
                        List<Message> ml = orma.selectFromMessage().
                                tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                                orderBySent_timestampAsc().
                                orderBySent_timestamp_msAsc().
                                toList();

                        if (ml != null)
                        {
                            for (Message message : ml)
                            {
                                add_message(message);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                // Log.i(TAG, "data_values:005c");
            }
            // Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

    public static long get_current_friendnum()
    {
        return friendnum;
    }

    public static void add_item(Message new_item)
    {
        messagelistitems_model.addElement(new_item);
        MessagePanel.revalidate();
    }

    synchronized static void modify_message(final Message m)
    {
        try
        {
            Iterator<Message> it = messagelistitems_model.elements().asIterator();

            while (it.hasNext())
            {
                Message msg = (Message) it.next();
                if (msg != null)
                {
                    if (msg.id == m.id)
                    {
                        int pos = messagelistitems_model.indexOf(msg);
                        // Log.i(TAG, "modify_message:m.id=" + m.id);

                        EventQueue.invokeLater(() -> {
                            messagelistitems_model.set(pos, m);
                        });
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setCurrentPK(String current_pk_)
    {
        current_pk = current_pk_;
    }

    public static String textAreaDialog(Object obj, String text, String title)
    {
        if (title == null)
        {
            title = "Your input";
        }
        JTextArea textArea = new JTextArea(text);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        int ret = JOptionPane.showConfirmDialog((Component) obj, new JScrollPane(textArea), title,
                                                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == 0)
        {
            return textArea.getText();
        }
        else
        {
        }
        return null;
    }

    static void add_outgoing_file(String filepath, String filename)
    {
        Log.i(TAG, "add_outgoing_file:regular file");

        long file_size = -1;
        try
        {
            file_size = new java.io.File(filepath + "/" + filename).length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            // file length "zero"?
            return;
        }

        Log.i(TAG, "add_outgoing_file:friendnum=" + friendnum);

        if (friendnum == -1)
        {
            // sorry, still no friendnum
            Log.i(TAG, "add_outgoing_file:sorry, still no friendnum");
            return;
        }

        Log.i(TAG, "add_outgoing_file:friendnum(2)=" + friendnum);

        Filetransfer f = new Filetransfer();
        f.tox_public_key_string = tox_friend_get_public_key__wrapper(friendnum);
        f.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
        f.file_number = -1; // add later when we actually have the number
        f.kind = TOX_FILE_KIND_DATA.value;
        f.state = TOX_FILE_CONTROL_PAUSE.value;
        f.path_name = filepath;
        f.file_name = filename;
        f.filesize = file_size;
        f.ft_accepted = false;
        f.ft_outgoing_started = false;
        f.current_position = 0;

        Log.i(TAG, "add_outgoing_file:tox_public_key_string=" + f.tox_public_key_string);

        long ft_id = insert_into_filetransfer_db(f);
        f.id = ft_id;

        // Message m_tmp = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(3)).orderByMessage_idDesc().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:2:" + ft_id);


        // ---------- DEBUG ----------
        Filetransfer ft_tmp = orma.selectFromFiletransfer().idEq(ft_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4a:" + "fid=" + ft_tmp.id + " mid=" + ft_tmp.message_id);
        // ---------- DEBUG ----------


        // add FT message to UI
        Message m = new Message();

        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
        m.direction = 1; // msg outgoing
        m.TOX_MESSAGE_TYPE = 0;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
        m.filetransfer_id = ft_id;
        m.filedb_id = -1;
        m.state = TOX_FILE_CONTROL_PAUSE.value;
        m.ft_accepted = false;
        m.ft_outgoing_started = false;
        m.ft_outgoing_queued = false;
        m.filename_fullpath = new java.io.File(filepath + "/" + filename).getAbsolutePath();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = filename + "\n" + file_size + " bytes";

        long new_msg_id = insert_into_message_db(m, true);
        m.id = new_msg_id;

        // ---------- DEBUG ----------
        Log.i(TAG, "add_outgoing_file:MM2MM:3:" + new_msg_id);
        Message m_tmp = orma.selectFromMessage().idEq(new_msg_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

        f.message_id = new_msg_id;
        // ** // update_filetransfer_db_messageid_from_id(f, ft_id);
        update_filetransfer_db_full(f);

        // ---------- DEBUG ----------
        Filetransfer ft_tmp2 = orma.selectFromFiletransfer().idEq(ft_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4b:" + "fid=" + ft_tmp2.id + " mid=" + ft_tmp2.message_id);
        // ---------- DEBUG ----------

        // ---------- DEBUG ----------
        m_tmp = orma.selectFromMessage().idEq(new_msg_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:5:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

        // --- ??? should we do this here?
        //        try
        //        {
        //            // update "new" status on friendlist fragment
        //            FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
        //            friend_list_fragment.modify_friend(f2, friendnum);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
        //        }
        // --- ??? should we do this here?
    }


    static class JlistCustom<M> extends JList
    {
        public JlistCustom()
        {
            super();
        }

        @Override
        public int getFixedCellWidth()
        {
            //Log.i(TAG, "getFixedCellWidth");
            return super.getFixedCellWidth();
        }

        @Override
        public int getFixedCellHeight()
        {
            int ret = super.getFixedCellHeight();
            //Log.i(TAG, "getFixedCellHeight=" + ret);
            return ret;
        }

        @Override
        public int getLeadSelectionIndex()
        {
            int ret = super.getLeadSelectionIndex();
            //Log.i(TAG, "getLeadSelectionIndex=" + ret);
            return ret;
        }

        @Override
        public int getMinSelectionIndex()
        {
            //Log.i(TAG, "getMinSelectionIndex");
            return super.getMinSelectionIndex();
        }

        @Override
        public int getMaxSelectionIndex()
        {
            //Log.i(TAG, "getMaxSelectionIndex");
            return super.getMaxSelectionIndex();
        }

        @Override
        public boolean getValueIsAdjusting()
        {
            //Log.i(TAG, "paintChildren");
            return super.getValueIsAdjusting();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize()
        {
            //Log.i(TAG, "paintChildren");
            return super.getPreferredScrollableViewportSize();
        }

        @Override
        protected void paintChildren(Graphics g)
        {
            //Log.i(TAG, "paintChildren");
            super.paintChildren(g);
        }

        @Override
        public Rectangle getVisibleRect()
        {
            Rectangle r = super.getVisibleRect();
            //Log.i(TAG, "paintChildren=" + r);
            return r;
        }

        @Override
        public int getVisibleRowCount()
        {
            int ret = super.getVisibleRowCount();
            //Log.i(TAG, "getVisibleRowCount=" + ret);
            return ret;
        }

        @Override
        public int getLastVisibleIndex()
        {
            int ret = super.getLastVisibleIndex();
            //Log.i(TAG, "getLastVisibleIndex=" + ret);
            return ret;
        }

        @Override
        public int getFirstVisibleIndex()
        {
            int ret = super.getFirstVisibleIndex();
            //Log.i(TAG, "getFirstVisibleIndex=" + ret);
            return ret;
        }
    }
}
