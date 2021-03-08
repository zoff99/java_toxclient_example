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

import java.awt.EventQueue;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.sendTextField;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;


public class MessageListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.MsgListFrgnt";

    static int global_typing = 0;
    static Thread typing_flag_thread = null;
    final static int TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS = 1000; // 1 second

    static String current_pk = null;
    static long friendnum = -1;
    static long friendnum_prev = -1;
    static boolean attachemnt_instead_of_send = false;
    static boolean is_at_bottom = true;
    static boolean show_only_files = false;

    private static JList<Message> messagelistitems;
    static DefaultListModel<Message> messagelistitems_model;
    static JScrollPane MessageScrollPane = null;

    public MessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");
        friendnum = -1;
        current_pk = null;

        messagelistitems_model = new DefaultListModel<>();

        messagelistitems = new JList<>();
        messagelistitems.setModel(messagelistitems_model);
        messagelistitems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messagelistitems.setSelectedIndex(0);
        messagelistitems.setSelectionModel(new DisabledItemSelectionModel());
        messagelistitems.setCellRenderer(new Renderer_MessageList());

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        MessageScrollPane = new JScrollPane(messagelistitems);
        add(MessageScrollPane);

        /*
        MessageScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                Log.i(TAG, "scroll2:" + e.getAdjustable().getMaximum() + " : " + e.getAdjustable().getValue() + " : " +
                           e.getAdjustable().getVisibleAmount() + " : " + e.getAdjustmentType() + " : " +
                           e.getValueIsAdjusting());
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });
         */

        revalidate();
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

                msg = sendTextField.getText().substring(0, (int) Math.min(tox_max_message_length(),
                                                                          sendTextField.getText().length()));

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
                                    sendTextField.setText("");
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        };
                        SwingUtilities.invokeLater(myRunnable);

                        //**//stop_self_typing_indicator_s();
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
                                    sendTextField.setText("");
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        };
                        SwingUtilities.invokeLater(myRunnable);

                        //**//stop_self_typing_indicator_s();
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

    synchronized static void add_message(final Message m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
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
            }
        };

        SwingUtilities.invokeLater(myRunnable);
    }

    static void update_all_messages(boolean always)
    {
        Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            //orma.updateMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(current_friendnum)).is_new(
            //        false).execute();
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

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        messagelistitems_model.clear();
                        MessagePanel.revalidate();
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

                };
                SwingUtilities.invokeLater(myRunnable);
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
            Iterator it = messagelistitems_model.elements().asIterator();

            while (it.hasNext())
            {
                Message msg = (Message) it.next();
                if (msg != null)
                {
                    if (msg.id == m.id)
                    {
                        int pos = messagelistitems_model.indexOf(msg);

                        EventQueue.invokeLater(() -> {
                            messagelistitems_model.set(pos, m);
                        });
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
}
