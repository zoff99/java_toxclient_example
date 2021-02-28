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

import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.long_date_time_format;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperMessage.insert_into_message_db;
import static com.zoffcc.applications.trifa.MainActivity.MessageTextArea;
import static com.zoffcc.applications.trifa.MainActivity.add_message_ml;
import static com.zoffcc.applications.trifa.MainActivity.blueSmallStyle;
import static com.zoffcc.applications.trifa.MainActivity.mainStyle;
import static com.zoffcc.applications.trifa.MainActivity.s;
import static com.zoffcc.applications.trifa.MainActivity.sendTextField;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;


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

    public MessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");
        friendnum = -1;
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

                msg = sendTextField.getText().toString().substring(0, (int) Math.min(tox_max_message_length(),
                                                                                     sendTextField.getText().toString().length()));

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
                        Log.i(TAG, "MESSAGEV2_SEND:row_id=" + row_id);
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
                        Log.i(TAG, "invokeLater:004:s");
                        SwingUtilities.invokeLater(myRunnable);
                        Log.i(TAG, "invokeLater:004:e");

                        //**//stop_self_typing_indicator_s();
                    }
                    else
                    {
                        // sending was NOT ok

                        Log.i(TAG, "tox_friend_send_message_wrapper:store pending message" + m);

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
                        Log.i(TAG, "invokeLater:005:s");
                        SwingUtilities.invokeLater(myRunnable);
                        Log.i(TAG, "invokeLater:005:e");

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
                        //**// listingsView.scrollToPosition(adapter.getItemCount() - 1);
                    }
                }
                catch (Exception e)
                {
                    Log.i(TAG, "add_message:EE1:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        // Log.i(TAG, "invokeLater:006:s");
        SwingUtilities.invokeLater(myRunnable);
        // Log.i(TAG, "invokeLater:006:e");
    }

    static void update_all_messages(boolean always)
    {
        Log.i(TAG, "update_all_messages");

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
                        MessageTextArea.setSelectionStart(0);
                        MessageTextArea.setSelectionEnd(MessageTextArea.getText().length());
                        MessageTextArea.setCharacterAttributes(mainStyle, true);
                        MessageTextArea.replaceSelection("");
                        // Log.i(TAG, "data_values:005b");

                        if (show_only_files)
                        {
                            // TODO:
                        }
                        else
                        {

                            try
                            {
                                Statement statement = sqldb.createStatement();
                                final String sql = "select * from Message where tox_friendpubkey='" +
                                                   s(tox_friend_get_public_key__wrapper(friendnum)) +
                                                   "' order by Sent_timestamp asc, Sent_timestamp_ms asc";
                                // Log.i(TAG, "sql=" + sql);
                                ResultSet rs = statement.executeQuery(sql);

                                while (rs.next())
                                {
                                    Message m = new Message();
                                    m.text = rs.getString("text");
                                    m.tox_friendpubkey = rs.getString("tox_friendpubkey");
                                    m.direction = rs.getInt("direction");
                                    m.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                                    m.rcvd_timestamp_ms = rs.getLong("rcvd_timestamp_ms");
                                    m.sent_timestamp = rs.getLong("sent_timestamp");
                                    m.sent_timestamp_ms = rs.getLong("sent_timestamp_ms");
                                    m.state = rs.getInt("state");
                                    // Log.i(TAG, "XXXX->m=" + m);
                                    add_message(m);
                                    // TODO: read all fields
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
                Log.i(TAG, "invokeLater:007:s");
                SwingUtilities.invokeLater(myRunnable);
                Log.i(TAG, "invokeLater:007:e");
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
        if (new_item.direction == 1)
        {
            add_message_ml(long_date_time_format(new_item.sent_timestamp), new_item.tox_friendpubkey, new_item.text,
                           true);
        }
        else
        {
            add_message_ml(long_date_time_format(new_item.rcvd_timestamp), new_item.tox_friendpubkey, new_item.text,
                           false);
        }
    }

    public void setCurrentPK(String current_pk_)
    {
        current_pk = current_pk_;
    }
}
