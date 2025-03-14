/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2020 Zoff <zoff@zoff.cc>
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
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import static com.zoffcc.applications.trifa.HelperFriend.friend_call_push_url;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.update_friend_msgv3_capability;
import static com.zoffcc.applications.trifa.HelperMessage.process_msgv3_high_level_ack;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_messageid;
import static com.zoffcc.applications.trifa.HelperMessage.update_message_in_db_resend_count;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message;
import static com.zoffcc.applications.trifa.HelperNotification.displayMessage;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.mainwindow_has_focus;
import static com.zoffcc.applications.trifa.MainActivity.ownProfileShort;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.get_current_friendnum;
import static com.zoffcc.applications.trifa.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_V2_MSG_SENT_OK;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CAPABILITY_MSGV2;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_TCP;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILETRANSFER_SIZE_MSGV2;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperGeneric
{
    /*
     all stuff here should be moved somewhere else at some point
     */

    private static final String TAG = "trifa.Hlp.Generic";

    static long video_frame_age_mean = 0;
    static int video_frame_age_values_cur_index = 0;
    final static int video_frame_age_values_cur_index_count = 10;
    static long[] video_frame_age_values = new long[video_frame_age_values_cur_index_count];
    static byte[] buf_video_send_frame = null;
    static long last_log_battery_savings_criteria_ts = -1;

    static String get_g_opts(String key)
    {
        try
        {
            if (orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobalsNew g_opts = orma.selectFromTRIFADatabaseGlobalsNew().keyEq(key).get(0);
                // Log.i(TAG, "get_g_opts:(SELECT):key=" + key);
                return g_opts.value;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_g_opts:EE1:" + e.getMessage());
            return null;
        }
    }

    static void set_g_opts(String key, String value)
    {
        try
        {
            TRIFADatabaseGlobalsNew g_opts = new TRIFADatabaseGlobalsNew();
            g_opts.key = key;
            g_opts.value = value;

            try
            {
                orma.insertIntoTRIFADatabaseGlobalsNew(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + "xxxxxxxxxxxxx");
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                try
                {
                    orma.updateTRIFADatabaseGlobalsNew().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + "xxxxxxxxxxxxx");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        }
        catch (Exception e4)
        {
            e4.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e4.getMessage());
        }
    }

    static void del_g_opts(String key)
    {
        try
        {
            orma.deleteFromTRIFADatabaseGlobalsNew().keyEq(key).execute();
            Log.i(TAG, "del_g_opts:(DELETE):key=" + key);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "del_g_opts:EE2:" + e.getMessage());
        }
    }

    static void update_savedata_file_wrapper()
    {
        try
        {
            MainActivity.semaphore_tox_savedata.acquire();
            String password_hash_2 = MainActivity.password_hash;
            long start_timestamp = System.currentTimeMillis();
            MainActivity.update_savedata_file(password_hash_2);
            long end_timestamp = System.currentTimeMillis();
            MainActivity.semaphore_tox_savedata.release();
            Log.i(TAG, "update_savedata_file() took:" + (((float) (end_timestamp - start_timestamp)) / 1000f) + "s");
        }
        catch (InterruptedException e)
        {
            MainActivity.semaphore_tox_savedata.release();
            e.printStackTrace();
        }
    }

    static int get_toxconnection_wrapper(int TOX_CONNECTION_)
    {
        if (TOX_CONNECTION_ == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    public static boolean tox_friend_resend_msgv3_wrapper(Message m)
    {
        if (m.msg_idv3_hash == null)
        {
            m.resend_count++;
            update_message_in_db_resend_count(m);
            return false;
        }

        if (m.msg_idv3_hash.length() < TOX_HASH_LENGTH)
        {
            m.resend_count++;
            update_message_in_db_resend_count(m);
            return false;
        }
        ByteBuffer hash_bytes = hexstring_to_bytebuffer(m.msg_idv3_hash);
        long res = MainActivity.tox_messagev3_friend_send_message(tox_friend_by_public_key__wrapper(m.tox_friendpubkey),
                                                                  TRIFA_MSG_TYPE_TEXT.value, m.text, hash_bytes,
                                                                  (m.sent_timestamp / 1000));

        m.resend_count++;
        m.message_id = res;
        update_message_in_db_resend_count(m);
        update_message_in_db_messageid(m);
        update_single_message(m, true);

        return true;
    }

    /*
     * returns: send_message_result class
     *
     *    send_message_result: NULL (if friend does not exist)
     *
     *    long msg_num: -98 (msgv3 error), -1 -2 -3 -4 -5 -6 (msgv3 error), -99 (msgv3 error),
     *                  "0..(UINT32_MAX-1)" (message sent OK)
     *
     *    long msg_num: -1 -2 -3 -4 -5 -6 -99 (msgv2 error),
     *                  MESSAGE_V2_MSG_SENT_OK (msgv2 sent OK), "0..(UINT32_MAX-1)" (message "old" sent OK)
     *
     *    boolean msg_v2: true if msgv2 was used, false otherwise
     *
     */
    public static MainActivity.send_message_result tox_friend_send_message_wrapper(final String friend_pubkey, int a_TOX_MESSAGE_TYPE, String message, long timestamp_unixtime_seconds)
    {
        // Log.d(TAG, "tox_friend_send_message_wrapper:" + friend_pubkey);
        FriendList f = main_get_friend(friend_pubkey);
        long friendnum_to_use = tox_friend_by_public_key__wrapper(friend_pubkey);
        boolean need_call_push_url = false;

        boolean msgv1 = true;

        if (f == null)
        {
            return null;
        }

        if ((f.capabilities & TOX_CAPABILITY_MSGV2) != 0)
        {
            msgv1 = false;
        }

        // Log.d(TAG, "tox_friend_send_message_wrapper:f conn" + f.TOX_CONNECTION_real);
        if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
        {
            String relay_pubkey = HelperRelay.get_relay_for_friend(f.tox_public_key_string);

            if (relay_pubkey != null)
            {
                // friend has a relay
                friendnum_to_use = tox_friend_by_public_key__wrapper(relay_pubkey);
                msgv1 = false;
                // Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
            }
            else // if friend is NOT online and does not have a relay, try if he has a push url
            {
                need_call_push_url = true;
            }
        }

        if (msgv1)
        {
            // old msgV1 message (but always send msgV3 format)
            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev3_get_new_message_id(hash_bytes);
            MainActivity.send_message_result result = new MainActivity.send_message_result();

            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_messagev3_friend_send_message(friendnum_to_use, a_TOX_MESSAGE_TYPE, message,
                                                                      hash_bytes, timestamp_unixtime_seconds);
            // Log.i(TAG, "tox_friend_send_message_wrapper:msg=" + message + " " + timestamp_unixtime_seconds + " " +
            //           long_date_time_format(timestamp_unixtime_seconds * 1000));

            result.msg_num = res;
            result.msg_v2 = false;
            result.msg_hash_hex = "";
            result.msg_hash_v3_hex = bytebuffer_to_hexstring(hash_bytes, true);
            result.raw_message_buf_hex = "";

            if (need_call_push_url)
            {
                friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
            }

            return result;
        }
        else // use msgV2
        {
            MainActivity.send_message_result result = new MainActivity.send_message_result();
            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
            ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            // use msg V2 API Call
            // long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE,
                                                                    timestamp_unixtime_seconds, message,
                                                                    message.length(), raw_message_buf,
                                                                    raw_message_length_buf, msg_id_buffer);
            if (PREF__X_battery_saving_mode)
            {
                Log.i(TAG, "global_last_activity_for_battery_savings_ts:002:*PING*");
            }

            ByteBufferCompat raw_message_length_buf_compat = new ByteBufferCompat(raw_message_length_buf);
            final int len_low_byte = raw_message_length_buf_compat.
                    array()[raw_message_length_buf_compat.arrayOffset()] & 0xFF;
            final int len_high_byte = (raw_message_length_buf_compat.
                    array()[raw_message_length_buf_compat.arrayOffset() + 1] & 0xFF) * 256;
            final int raw_message_length_int = len_low_byte + len_high_byte;

            result.error_num = res;

            if (res == -9999)
            {
                // msg V2 OK
                result.msg_num = MESSAGE_V2_MSG_SENT_OK;
                result.msg_v2 = true;

                ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
                result.msg_hash_hex = bytesToHex(msg_id_buffer_compat.array(), msg_id_buffer_compat.arrayOffset(),
                                                 msg_id_buffer_compat.limit());
                ByteBufferCompat raw_message_buf_compat = new ByteBufferCompat(raw_message_buf);
                result.raw_message_buf_hex = bytesToHex(raw_message_buf_compat.array(),
                                                        raw_message_buf_compat.arrayOffset(), raw_message_length_int);
                if (need_call_push_url)
                {
                    friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
                }
                return result;
            }
            else
            {
                if (res == -9991)
                {
                    result.msg_num = -1;
                }
                else
                {
                    result.msg_num = res;
                }

                result.msg_v2 = true;
                result.msg_hash_hex = "";
                result.raw_message_buf_hex = "";
                if (need_call_push_url)
                {
                    friend_call_push_url(f.tox_public_key_string, System.currentTimeMillis());
                }
                return result;
            }
        }
    }

    public static String bytesToHex(byte[] bytes, int start, int len)
    {
        char[] hexChars = new char[(len) * 2];
        // System.out.println("blen=" + (len));

        for (int j = start; j < (start + len); j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j - start) * 2] = MainActivity.hexArray[v >>> 4];
            hexChars[(j - start) * 2 + 1] = MainActivity.hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    static void receive_incoming_message(int msg_type, int tox_message_type, long friend_number, String friend_message_text_utf8, byte[] raw_message, long raw_message_length, String original_sender_pubkey, byte[] msgV3hash_bin, long message_timestamp)
    {
        // incoming msg can be:
        // (msg_type == 0) msgV1 text only message -> msg_type, friend_number, friend_message_text_utf8 [, msgV3hash_bin, message_timestamp]
        // (msg_type == 1) msgV2 direct message    -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length
        // (msg_type == 2) msgV2 relay message     -> msg_type, friend_number, friend_message_text_utf8, raw_message, raw_message_length, original_sender_pubkey
        if (msg_type == 0)
        {
            // msgV1 text only message
            // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash:" + msgV3hash_bin);

            String msgV3hash_hex_string = null;
            if (msgV3hash_bin != null)
            {
                msgV3hash_hex_string = HelperGeneric.bytesToHex(msgV3hash_bin, 0, msgV3hash_bin.length);

                int got_messages = orma.selectFromMessage().
                        tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                        directionEq(0).
                        msg_idv3_hashEq(msgV3hash_hex_string).count();

                // Log.i(TAG, "friend_message:friend:" + friend_number + " msgV3hash_hex_string:" + msgV3hash_hex_string +
                //            " got_messages=" + got_messages);

                if (got_messages > 0)
                {
                    // HINT: we already have received a message with this hash
                    // still send the msgV3 high level ACK, and then ignore
                    HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
                    return;
                }
            }

            if (tox_message_type == TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value)
            {
                // TODO: ack message in database and update messagelist UI
                process_msgv3_high_level_ack(friend_number, msgV3hash_hex_string, message_timestamp);
                return;
            }

            if (msgV3hash_bin != null)
            {
                int got_messages_mirrored = orma.selectFromMessage().
                        tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                        directionEq(1).
                        msg_idv3_hashEq(msgV3hash_hex_string).count();

                // Log.i(TAG, "update_friend_msgv3_capability:got_messages_mirrored=" + got_messages_mirrored + " hash1=" +
                //           msgV3hash_bin + " " + msgV3hash_hex_string);
                if (got_messages_mirrored > 0)
                {
                    update_friend_msgv3_capability(friend_number, 0);
                }
                else
                {
                    update_friend_msgv3_capability(friend_number, 1);
                }
            }
            else
            {
                // Log.i(TAG, "update_friend_msgv3_capability:hash0=" + msgV3hash_bin + " " + msgV3hash_hex_string);
                update_friend_msgv3_capability(friend_number, 0);
            }

            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
            if ((get_current_friendnum() == friend_number) && (mainwindow_has_focus))
            {
                // Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }

            Message m = new Message();

            if (!do_badge_update)
            {
                // Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                // Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            // m.tox_friendnum = friend_number;
            m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.read = false;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.rcvd_timestamp = System.currentTimeMillis();
            m.rcvd_timestamp_ms = 0;
            if (message_timestamp > 0)
            {
                m.sent_timestamp = message_timestamp * 1000;
            }
            else
            {
                m.sent_timestamp = System.currentTimeMillis();
            }
            m.sent_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 0;
            m.msg_idv3_hash = msgV3hash_hex_string;
            m.sent_push = 0;

            long db_result = -1;

            if (get_current_friendnum() == friend_number)
            {
                // Log.i(TAG, "insert_into_message_db:true:fn=" + friend_number);
                db_result = HelperMessage.insert_into_message_db(m, true);
            }
            else
            {
                // Log.i(TAG, "insert_into_message_db:false");
                db_result = HelperMessage.insert_into_message_db(m, false);
            }

            if (db_result == -1)
            {
                // error on inserting message into db, most likely msgV3 double message
                return;
            }

            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                // Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                //**//change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey);
                displayMessage("new Message from: " +
                               get_friend_name_from_pubkey(tox_friend_get_public_key__wrapper(friend_number)));
            }

            if (msgV3hash_hex_string != null)
            {
                HelperMessage.send_msgv3_high_level_ack(friend_number, msgV3hash_hex_string);
            }
        }
        else if (msg_type == 1)
        {
            // msgV2 direct message
            // Log.i(TAG,
            //      "friend_message_v2:friend:" + friend_number + " ts:" + ts_sec + " systime" + System.currentTimeMillis() +
            //      " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            if ((get_current_friendnum() == friend_number) && (mainwindow_has_focus))
            {
                // Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }

            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
            raw_message_buf.put(raw_message, 0, (int) raw_message_length);
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = MainActivity.tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = MainActivity.tox_messagev2_get_ts_ms(raw_message_buf);

            ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
            String msg_id_as_hex_string = bytesToHex(msg_id_buffer_compat.array(), msg_id_buffer_compat.arrayOffset(),
                                                     msg_id_buffer_compat.limit());
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);
            int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).msg_id_hashEq(
                    msg_id_as_hex_string).count();

            long pin_timestamp = System.currentTimeMillis();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                // TODO: use received timstamp, not "now" here!
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer,
                                                                (pin_timestamp / 1000));
                return;
            }

            // add FT message to UI
            Message m = new Message();

            if (!do_badge_update)
            {
                // Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                // Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.filetransfer_id = -1;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_RESUME.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false;
            m.ft_outgoing_queued = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = pin_timestamp;
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            m.sent_push = 0;
            // Log.i(TAG, "TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

            if (get_current_friendnum() == friend_number)
            {
                HelperMessage.insert_into_message_db(m, true);
            }
            else
            {
                HelperMessage.insert_into_message_db(m, false);
            }

            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number, msg_type, msg_id_buffer,
                                                            (pin_timestamp / 1000));

            try
            {
                // update "new" status on friendlist fragment

                FriendList f = main_get_friend(tox_friend_by_public_key__wrapper(m.tox_friendpubkey));
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                // Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                //**//change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey);
                displayMessage("new Message from: " +
                               get_friend_name_from_pubkey(tox_friend_get_public_key__wrapper(friend_number)));
            }
        }
        else if (msg_type == 2)
        {
            // msgV2 relay message
            long friend_number_real_sender = tox_friend_by_public_key__wrapper(original_sender_pubkey);
            // Log.i(TAG,
            //      "friend_message_v2:friend:" + friend_number + " ts:" + ts_sec + " systime" + System.currentTimeMillis() +
            //      " message:" + friend_message);
            // if message list for this friend is open, then don't do notification and "new" badge
            boolean do_notification = true;
            boolean do_badge_update = true;

            // Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
            // Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
            if ((get_current_friendnum() == friend_number_real_sender) && (mainwindow_has_focus))
            {
                // Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }

            ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) raw_message_length);
            raw_message_buf.put(raw_message, 0, (int) raw_message_length);
            ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
            MainActivity.tox_messagev2_get_message_id(raw_message_buf, msg_id_buffer);
            long ts_sec = MainActivity.tox_messagev2_get_ts_sec(raw_message_buf);
            long ts_ms = MainActivity.tox_messagev2_get_ts_ms(raw_message_buf);
            ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
            // Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:raw_msg=" + bytes_to_hex(raw_message));
            String msg_id_as_hex_string = bytesToHex(msg_id_buffer_compat.array(), msg_id_buffer_compat.arrayOffset(),
                                                     msg_id_buffer_compat.limit());
            // Log.i(TAG, "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:MSGv2HASH:2=" + msg_id_as_hex_string);
            int already_have_message = orma.selectFromMessage().tox_friendpubkeyEq(
                    HelperFriend.tox_friend_get_public_key__wrapper(friend_number_real_sender)).msg_id_hashEq(
                    msg_id_as_hex_string).count();

            long pin_timestamp = System.currentTimeMillis();

            if (already_have_message > 0)
            {
                // it's a double send, ignore it
                // send message receipt v2, most likely the other party did not get it yet
                HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer,
                                                                (pin_timestamp / 1000));
                return;
            }

            // add FT message to UI
            Message m = new Message();

            if (!do_badge_update)
            {
                // Log.i(TAG, "noti_and_badge:004a:");
                m.is_new = false;
            }
            else
            {
                // Log.i(TAG, "noti_and_badge:004b:");
                m.is_new = true;
            }

            m.tox_friendpubkey = original_sender_pubkey;
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
            m.filetransfer_id = -1;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_RESUME.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false;
            m.ft_outgoing_queued = false;
            m.sent_timestamp = (ts_sec * 1000); // sent time as unix timestamp -> convert to milliseconds
            m.sent_timestamp_ms = ts_ms; // "ms" part of timestamp (could be just an increasing number)
            m.rcvd_timestamp = pin_timestamp;
            m.rcvd_timestamp_ms = 0;
            m.text = friend_message_text_utf8;
            m.msg_version = 1;
            m.msg_id_hash = msg_id_as_hex_string;
            m.sent_push = 0;
            // Log.i(TAG,
            //       "receive_incoming_message:TOX_FILE_KIND_MESSAGEV2_SEND:" + long_date_time_format(m.rcvd_timestamp));

            if (get_current_friendnum() == friend_number_real_sender)
            {
                HelperMessage.insert_into_message_db(m, true);
            }
            else
            {
                HelperMessage.insert_into_message_db(m, false);
            }

            // send message receipt v2 to the relay
            HelperFriend.send_friend_msg_receipt_v2_wrapper(friend_number_real_sender, msg_type, msg_id_buffer,
                                                            (pin_timestamp / 1000));

            try
            {
                // update "new" status on friendlist fragment
                FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                HelperFriend.update_single_friend_in_friendlist_view(f);

                if (f.notification_silent)
                {
                    do_notification = false;
                }
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                // Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }

            if (do_notification)
            {
                //**// change_msg_notification(NOTIFICATION_EDIT_ACTION_ADD.value, m.tox_friendpubkey);
                displayMessage("new Message from: " + get_friend_name_from_pubkey(original_sender_pubkey));
            }
        }
    }

    static String long_date_time_format(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_time_long.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "_Datetime_ERROR_";
        }
    }

    static String long_date_time_format_or_empty(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_time_long.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    static String only_date_time_format(long timestamp_in_millis)
    {
        try
        {
            return MainActivity.df_date_only.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "_Datetime_ERROR_";
        }
    }

    public static void update_friend_connection_status_helper(int a_TOX_CONNECTION, FriendList f, boolean from_relay)
    {
        // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:ENTER");

        final long friend_number_ = tox_friend_by_public_key__wrapper(f.tox_public_key_string);
        boolean went_online = false;

        if (f.TOX_CONNECTION != a_TOX_CONNECTION)
        {
            if ((!from_relay) && (!HelperRelay.is_any_relay(f.tox_public_key_string)))
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {
                    //**//send_avatar_to_friend(tox_friend_by_public_key__wrapper(f.tox_public_key_string));
                }
            }

            if (a_TOX_CONNECTION == TOX_CONNECTION_NONE.value)
            {
                // ******** friend going offline ********
                // Log.i(TAG, "friend_connection_status:friend going offline:" + System.currentTimeMillis());
            }
            else
            {
                went_online = true;
                // ******** friend coming online ********
                // Log.i(TAG, "friend_connection_status:friend coming online:" + LAST_ONLINE_TIMSTAMP_ONLINE_NOW);
            }
        }

        if (!from_relay)
        {
            if (went_online)
            {
                f.last_online_timestamp_real = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
            }
            else
            {
                f.last_online_timestamp_real = System.currentTimeMillis();
            }
            HelperFriend.update_friend_in_db_last_online_timestamp_real(f);
        }

        if (went_online)
        {
            // Log.i(TAG, "friend_connection_status:friend status seems: ONLINE");
            f.last_online_timestamp = LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
            HelperFriend.update_friend_in_db_last_online_timestamp(f);
            f.TOX_CONNECTION = a_TOX_CONNECTION;
            f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
            HelperFriend.update_friend_in_db_connection_status(f);

            HelperFriend.add_all_friends_clear_wrapper(0);
        }
        else // went offline -------------------
        {
            // check for combined online status of (friend + possible relay)
            int status_new = a_TOX_CONNECTION;
            int combined_connection_status_ = get_combined_connection_status(f.tox_public_key_string, status_new);
            // Log.i(TAG, "friend_connection_status:friend status combined con status:" + combined_connection_status_);

            if (get_toxconnection_wrapper(combined_connection_status_) == TOX_CONNECTION_NONE.value)
            {
                // Log.i(TAG, "friend_connection_status:friend status combined: OFFLINE");
                f.last_online_timestamp = System.currentTimeMillis();
                HelperFriend.update_friend_in_db_last_online_timestamp(f);
                f.TOX_CONNECTION = combined_connection_status_;
                f.TOX_CONNECTION_on_off = get_toxconnection_wrapper(f.TOX_CONNECTION);
                HelperFriend.update_friend_in_db_connection_status(f);

                HelperFriend.add_all_friends_clear_wrapper(0);
            }
            else
            {
                // Log.i(TAG, "friend or relay offline, combined still ONLINE");
                HelperFriend.update_single_friend_in_friendlist_view(f);
            }
        }
    }

    static int get_combined_connection_status(String friend_pubkey, int a_TOX_CONNECTION)
    {
        int ret = TOX_CONNECTION_NONE.value;

        if (HelperRelay.is_any_relay(friend_pubkey))
        {
            ret = a_TOX_CONNECTION;
        }
        else
        {
            String relay_ = HelperRelay.get_relay_for_friend(friend_pubkey);

            if (relay_ == null)
            {
                // friend has no relay
                ret = a_TOX_CONNECTION;
            }
            else
            {
                // friend with relay
                if (a_TOX_CONNECTION != TOX_CONNECTION_NONE.value)
                {
                    ret = a_TOX_CONNECTION;
                }
                else
                {
                    Statement statement = null;
                    try
                    {
                        statement = sqldb.createStatement();
                        ResultSet rs = statement.executeQuery(
                                "select TOX_CONNECTION_real from FriendList where tox_public_key_string='" +
                                s(friend_pubkey) + "'");
                        rs.next();
                        int friend_con_status = rs.getInt("TOX_CONNECTION_real");

                        ResultSet rs2 = statement.executeQuery(
                                "select TOX_CONNECTION_real from FriendList where tox_public_key_string='" + s(relay_) +
                                "'");
                        rs2.next();
                        int relay_con_status = rs2.getInt("TOX_CONNECTION_real");

                        if ((friend_con_status != TOX_CONNECTION_NONE.value) ||
                            (relay_con_status != TOX_CONNECTION_NONE.value))
                        {
                            // if one of them is online, return combined "online" as status
                            ret = TOX_CONNECTION_TCP.value;
                        }

                        try
                        {
                            statement.close();
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        return ret;
    }

    public static void update_bitrates()
    {
        // these were updated: Callstate.audio_bitrate, Callstate.video_bitrate
        try
        {
            /*
            CallingActivity.ca.right_top_text_1.setText(
                    "O:" + Callstate.codec_to_str(Callstate.video_out_codec) + ":" + Callstate.video_bitrate);
            CallingActivity.ca.right_top_text_1b.setText(
                    "I:" + Callstate.codec_to_str(Callstate.video_in_codec) + ":" + Callstate.video_in_bitrate);
            if (native_aec_lib_ready)
            {
                CallingActivity.ca.right_top_text_2.setText(
                        "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay + "e");
            }
            else
            {
                CallingActivity.ca.right_top_text_2.setText(
                        "AO:" + Callstate.audio_bitrate + " " + Callstate.play_delay);
            }
           */
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            /*
            CallingActivity.top_text_line.setText(
                    Callstate.friend_alias_name + " " + Callstate.round_trip_time + "/" + Callstate.play_delay + "/" +
                    Callstate.play_buffer_entries);

            */
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static long get_last_rowid(Statement statement)
    {
        try
        {
            long ret = -1;
            ResultSet rs = statement.executeQuery("select last_insert_rowid() as lastrowid");
            if (rs.next())
            {
                ret = rs.getLong("lastrowid");
            }
            // Log.i(TAG, "get_last_rowid:ret=" + ret);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_last_rowid:EE1:" + e.getMessage());
            return -1;
        }
    }

    public static long get_last_rowid_pstmt(PreparedStatement statement)
    {
        try
        {
            long ret = -1;

            ResultSet rs = statement.getGeneratedKeys();
            while (rs.next())
            {
                ret = rs.getLong(1);
            }
            rs.close();
            // Log.i(TAG, "get_last_rowid:ret=" + ret);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_last_rowid_pstmt:EE1:" + e.getMessage());
            return -1;
        }
    }

    public static byte[] hex_to_bytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String bytes_to_hex(byte[] in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();

            for (byte b : in)
            {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    static int hash_to_bucket(String hash_value, int number_of_buckets)
    {
        try
        {
            int ret = 0;
            int value = (Integer.parseInt(hash_value.substring(hash_value.length() - 1, hash_value.length() - 0), 16) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 2, hash_value.length() - 1), 16) *
                          16) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 3, hash_value.length() - 2), 16) *
                          (16 * 2)) +
                         (Integer.parseInt(hash_value.substring(hash_value.length() - 4, hash_value.length() - 3), 16) *
                          (16 * 3)));

            // Log.i(TAG, "hash_to_bucket:value=" + value);

            ret = (value % number_of_buckets);

            // BigInteger bigInt = new BigInteger(1, hash_value.getBytes());
            // int ret = (int) (bigInt.longValue() % (long) number_of_buckets);
            // // Log.i(TAG, "hash_to_bucket:" + "ret=" + ret + " hash_as_int=" + bigInt + " hash=" + hash_value);
            return ret;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            // Log.i(TAG, "hash_to_bucket:EE:" + e.getMessage());
            return 0;
        }
    }

    public static double getColorDarkBrightness(Color color)
    {
        double luminosity = Math.sqrt(Math.pow(color.getRed(), 2) * 0.299 + Math.pow(color.getGreen(), 2) * 0.587 +
                                      Math.pow(color.getBlue(), 2) * 0.114);

        return luminosity;
    }

    public static boolean isColorDarkBrightness(Color color)
    {
        double luminosity = Math.sqrt(Math.pow(color.getRed(), 2) * 0.299 + Math.pow(color.getGreen(), 2) * 0.587 +
                                      Math.pow(color.getBlue(), 2) * 0.114);

        if (luminosity > 146)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public static Color darkenColor(Color inColor, float inAmount)
    {
        return new Color((int) Math.max(0, inColor.getRed() - 255 * inAmount),
                         (int) Math.max(0, inColor.getGreen() - 255 * inAmount),
                         (int) Math.max(0, inColor.getBlue() - 255 * inAmount), inColor.getAlpha());
    }

    public static void set_message_accepted_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void move_tmp_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        // Log.i(TAG, "move_tmp_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" +
        //           dst_file_name);
        try
        {
            File f1 = new File(src_path_name + "/" + src_file_name);
            File f2 = new File(dst_path_name + "/" + dst_file_name);
            File dst_dir = new File(dst_path_name + "/");
            dst_dir.mkdirs();
            f1.renameTo(f2);

            // Log.i(TAG, "move_tmp_file_to_real_file:OK");
        }
        catch (Exception e)
        {
            // Log.i(TAG, "move_tmp_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Color newColorWithAlpha(Color original, int alpha)
    {
        return new Color(original.getRed(), original.getGreen(), original.getBlue(), alpha);
    }

    static byte[] read_chunk_from_SD_file(String file_name_with_path, long position, long file_chunk_length)
    {
        final byte[] out = new byte[(int) file_chunk_length];

        try
        {
            final FileInputStream fis = new FileInputStream(new File(file_name_with_path));
            fis.getChannel().position(position);
            final int actually_read = fis.read(out, 0, (int) file_chunk_length);

            try
            {
                fis.close();
            }
            catch (Exception e2)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return out;
    }

    static void conference_message_add_from_sync(long conference_number, long peer_number2, String peer_pubkey, int a_TOX_MESSAGE_TYPE, String message, long length, long sent_timestamp_in_ms, String message_id)
    {
        // Log.i(TAG, "conference_message_add_from_sync:cf_num=" + conference_number + " pnum=" + peer_number2 + " msg=" +
        //            message);

        int res = -1;
        if (peer_number2 == -1)
        {
            res = -1;
        }
        else
        {
            res = MainActivity.tox_conference_peer_number_is_ours(conference_number, peer_number2);
        }

        if (res == 1)
        {
            // HINT: do not add our own messages, they are already in the DB!
            // Log.i(TAG, "conference_message_add_from_sync:own peer");
            return;
        }

        boolean do_notification = true;
        boolean do_badge_update = true;
        String conf_id = "-1";
        ConferenceDB conf_temp = null;

        try
        {
            // TODO: cache me!!
            conf_temp = orma.selectFromConferenceDB().
                    tox_conference_numberEq(conference_number).
                    conference_activeEq(true).toList().get(0);
            conf_id = conf_temp.conference_identifier;
            // Log.i(TAG, "conference_message_add_from_sync:conf_id=" + conf_id);
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        try
        {
            if (conf_temp.notification_silent)
            {
                do_notification = false;
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

        if ((MessagePanelConferences.get_current_conf_id().equals(conf_id)) && (mainwindow_has_focus))
        {
            // Log.i(TAG, "noti_and_badge:003:");
            // no notifcation and no badge update
            do_notification = false;
            do_badge_update = false;
        }

        ConferenceMessage m = new ConferenceMessage();
        m.is_new = do_badge_update;
        // m.tox_friendnum = friend_number;
        m.tox_peerpubkey = peer_pubkey;
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.tox_peername = null;
        m.conference_identifier = conf_id;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.sent_timestamp = sent_timestamp_in_ms;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = message;
        m.message_id_tox = message_id;
        m.was_synced = true;

        try
        {
            m.tox_peername = HelperConference.tox_conference_peer_get_name__wrapper(m.conference_identifier,
                                                                                    m.tox_peerpubkey);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (MessagePanelConferences.get_current_conf_id().equals(conf_id))
        {
            HelperConference.insert_into_conference_message_db(m, true);
        }
        else
        {
            HelperConference.insert_into_conference_message_db(m, false);
        }

        HelperConference.update_single_conference_in_friendlist_view(conf_temp);

        if (do_notification)
        {
            displayMessage("new Group Message [synced] from: " + conf_temp.name);
        }
    }

    /**
     * Get an image off the system clipboard.
     *
     * @return Returns an Image if successful; otherwise returns null.
     */
    public static Image getImageFromClipboard()
    {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
        {
            try
            {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            }
            catch (UnsupportedFlavorException e)
            {
                // handle this as desired
                e.printStackTrace();
            }
            catch (IOException e)
            {
                // handle this as desired
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    static void update_display_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        // TODO: update entry in main friendlist (if visible)
        //       or in chat view (if visible)
        HelperFriend.update_single_friend_in_friendlist_view(
                main_get_friend(tox_friend_by_public_key__wrapper(friend_pubkey)));
    }

    static void draw_main_top_icon(int blur_color, boolean is_fg)
    {
        EventQueue.invokeLater(() -> {
            try
            {
                Color col = new Color(blur_color, false);
                Border ownProfileShortBorder = BorderFactory.createLineBorder(col, 3);
                ownProfileShort.setBorder(BorderFactory.createCompoundBorder(ownProfileShortBorder,
                                                                             BorderFactory.createEmptyBorder(2, 0, 0,
                                                                                                             0)));
            }
            catch (Exception e)
            {
            }
        });
    }

    static void appendToJTextPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        // aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        // aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    static void appendToJEditorPane(JEditorPane ep, final String msg, final Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        int len = ep.getDocument().getLength(); // same value as getText().length();
        ep.setCaretPosition(len); // place caret at the end (with no selection)
        // ep.setCharacterAttributes(aset, false);
        String msg2 = "";
        if (c == Color.BLACK)
        {
            msg2 = msg;
        }
        else
        {
            //msg2 = "<font color = FF0000>" + msg + "</font>";
            msg2 = msg;
        }
        ep.replaceSelection(msg2); // there is no selection, so inserts at caret
    }

    public static ByteBuffer hexstring_to_bytebuffer(String in)
    {
        try
        {
            byte[] in_bytes = in.getBytes(StandardCharsets.US_ASCII);
            ByteBuffer ret = ByteBuffer.allocateDirect(in_bytes.length / 2);
            for (int i = 0; i < in_bytes.length; i = i + 2)
            {
                // Log.i(TAG, "hexstring_to_bytebuffer:i=" + i + " byte=" + in_bytes[i] + " byte2=" + in_bytes[i + 1]);
                byte b1 = in_bytes[i + 1];
                byte b2 = in_bytes[i];
                // Log.i(TAG, "hexstring_to_bytebuffer:res=" + two_hex_bytes_to_dec_int(b1, b2));
                ret.put((byte) two_hex_bytes_to_dec_int(b1, b2));
            }

            ret.rewind();
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static int two_hex_bytes_to_dec_int(byte b1, byte b2)
    {
        int res = 0;
        // ascii:0 .. 9 -> byte:48 ..  57
        // ascii:A .. F -> byte:65 ..  70
        // ascii:a .. f -> byte:97 .. 102
        if (48 <= b1 && b1 <= 57)
        {
            res = b1 - 48;
        }
        else if ('A' <= b1 && b1 <= 'F')
        {
            res = b1 - 65 + 10;
        }
        else if ('a' <= b1 && b1 <= 'f')
        {
            res = b1 - 97 + 10;
        }

        if (48 <= b2 && b2 <= 57)
        {
            res = res + ((b2 - 48) * 16);
        }
        else if ('A' <= b2 && b2 <= 'F')
        {
            res = res + ((b2 - 65 + 10) * 16);
        }
        else if ('a' <= b2 && b2 <= 'f')
        {
            res = res + ((b2 - 97 + 10) * 16);
        }

        return res;
    }

    public static String bytebuffer_to_hexstring(ByteBuffer in, boolean upper_case)
    {
        try
        {
            in.rewind();
            StringBuilder sb = new StringBuilder("");
            while (in.hasRemaining())
            {
                if (upper_case)
                {
                    sb.append(String.format("%02X", in.get()));
                }
                else
                {
                    sb.append(String.format("%02x", in.get()));
                }
            }
            in.rewind();
            return sb.toString();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static String trim_to_utf8_length_bytes(String input_string, final int max_length_in_bytes)
    {
        try
        {
            do
            {
                byte[] valueInBytes = null;
                valueInBytes = input_string.getBytes(StandardCharsets.UTF_8);

                if (valueInBytes.length > max_length_in_bytes)
                {
                    input_string = input_string.substring(0, input_string.length() - 1);
                }
                else
                {
                    return input_string;
                }
            }
            while (input_string.length() > 0);
        }
        catch (Exception e)
        {
        }

        return null;
    }

    static void set_new_random_nospam_value()
    {
        // Log.i(TAG, "old ToxID=" + MainActivity.get_my_toxid());
        // Log.i(TAG, "old NOSPAM=" + MainActivity.tox_self_get_nospam());
        Random random = new Random();
        long new_nospam = (long) random.nextInt() + (1L << 31);
        // Log.i(TAG, "generated NOSPAM=" + new_nospam);
        MainActivity.tox_self_set_nospam(new_nospam);
        update_savedata_file_wrapper(); // set new random nospam
    }

    public static void send_avatar_to_all_friends()
    {
        try
        {
            List<FriendList> fl = orma.selectFromFriendList().
                    toList();

            if (fl != null)
            {
                if (fl.size() > 0)
                {
                    int i = 0;
                    for (i = 0; i < fl.size(); i++)
                    {
                        FriendList n = fl.get(i);
                        // iterate over all online friends, and send them our new avatar
                        if (n.TOX_CONNECTION != TOX_CONNECTION_NONE.value)
                        {
                            // Log.i(TAG, "select_avatar:send_avatar_to_friend:online:i=" + i);
                            send_avatar_to_friend(
                                    HelperFriend.tox_friend_by_public_key__wrapper(n.tox_public_key_string));
                        }
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    public static void send_avatar_to_friend(final long friend_number_)
    {
        // TODO: write me
    }

    public static void del_own_avatar()
    {
        // TODO: write me
    }

    static void hack_outgoing_ft(final String filename_full_path, final String friend_pubkey)
    {
        Log.i(TAG, "hack_outgoing_ft:" + filename_full_path);

        long file_size = -1;
        try
        {
            file_size = new java.io.File(filename_full_path).length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            return;
        }

        final int MAX_CRYPTO_PACKET_SIZE = 1400;
        final int CRYPTO_MAC_SIZE = 16;
        final int CRYPTO_DATA_PACKET_MIN_SIZE = (1 + 2 + (4 + 4) + CRYPTO_MAC_SIZE);
        final int MAX_CRYPTO_DATA_SIZE = (MAX_CRYPTO_PACKET_SIZE - CRYPTO_DATA_PACKET_MIN_SIZE);
        // HINT: MAX_CRYPTO_DATA_SIZE = 1373

        // HINT: 32 bytes FT id, 1 byte FT protocol version, 1 byte FT function (e.g. send, data, cancel, ...)
        //       4 byte start position of data in the file
        //       +1 byte for the packet ID (yes, that also counts)
        final int overhead = 32 + 1 + 1 + 4 + 1;

        final int MAX_FT_DATA_CHUNK = MAX_CRYPTO_DATA_SIZE - overhead;

        Log.i(TAG, "hack_outgoing_ft:MAX_CRYPTO_DATA_SIZE=" + MAX_CRYPTO_DATA_SIZE);
        Log.i(TAG, "hack_outgoing_ft:MAX_FT_DATA_CHUNK=" + MAX_FT_DATA_CHUNK);

        int i = 0;
        long friend_num = HelperFriend.tox_friend_by_public_key__wrapper(friend_pubkey);
        // byte[] data = new byte[MAX_CRYPTO_DATA_SIZE];
        // data[0] = (byte) 185;

        if (file_size <= MAX_FT_DATA_CHUNK)
        {
            Log.i(TAG, "hack_outgoing_ft:small file");
            final byte[] bytes_chunck; //= HelperGeneric.read_chunk_from_SD_file(filename_full_path, 0, file_size);
            final ByteBuffer file_chunk = ByteBuffer.allocateDirect((int) (file_size + overhead));
            file_chunk.put((byte) 185); // Custom packet ID: 185
            file_chunk.put(new byte[32]); // FT ID 32bytes hash -> dummy
            file_chunk.put((byte) 1); // Version: 1
            file_chunk.put((byte) 3); // Function: 3 -> dummy
            file_chunk.putInt(0); // Position: 0
            // file_chunk.put(bytes_chunck);
            file_chunk.rewind();

            int result = MainActivity.tox_friend_send_lossless_packet(friend_num, file_chunk.array(),
                                                                      (int) (file_size + overhead));
            Log.i(TAG, "hack_outgoing_ft:result=" + result);
        }
        else
        {
            int chunks = (int) (file_size / MAX_FT_DATA_CHUNK);
            final int mod = (int) (file_size % MAX_FT_DATA_CHUNK);
            Log.i(TAG, "hack_outgoing_ft:chunks=" + chunks);

            if (mod > 0)
            {
                Log.i(TAG, "hack_outgoing_ft:mod=" + mod);
            }

            int pos = 0;
            for (int j = 0; j < chunks; j++)
            {
                // Log.i(TAG, "hack_outgoing_ft:j=" + j);

                /*
                try
                {
                    Thread.sleep(5);
                }
                catch (Exception ignored)
                {
                }
                */

                pos = j * MAX_FT_DATA_CHUNK;
                final byte[] bytes_chunck = HelperGeneric.read_chunk_from_SD_file(filename_full_path, pos,
                                                                                  MAX_FT_DATA_CHUNK);

                final ByteBuffer file_chunk = ByteBuffer.allocateDirect((int) (MAX_FT_DATA_CHUNK + overhead));
                file_chunk.put((byte) 185); // Custom packet ID: 185
                file_chunk.put(new byte[32]); // FT ID 32bytes hash -> dummy
                file_chunk.put((byte) 1); // Version: 1
                file_chunk.put((byte) 3); // Function: 3 -> dummy
                file_chunk.putInt(pos); // Position: pos
                // file_chunk.put(bytes_chunck);
                file_chunk.rewind();

                byte[] data = new byte[(int) (MAX_FT_DATA_CHUNK + overhead)];
                data[0] = (byte) 185;

                int result = MainActivity.tox_friend_send_lossless_packet(friend_num, data,
                                                                          (int) (MAX_FT_DATA_CHUNK + overhead));

                while (result == -99)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (Exception ignored)
                    {
                    }

                    result = MainActivity.tox_friend_send_lossless_packet(friend_num, data,
                                                                          (int) (MAX_FT_DATA_CHUNK + overhead));
                }

                Log.i(TAG, "hack_outgoing_ft:send result1=" + result + " pos=" + pos + " size=" +
                           (int) (MAX_FT_DATA_CHUNK + overhead));
            }

            if (mod > 0)
            {
                final byte[] bytes_chunck = HelperGeneric.read_chunk_from_SD_file(filename_full_path, pos, mod);
                final ByteBuffer file_chunk = ByteBuffer.allocateDirect((int) (mod + overhead));
                file_chunk.put((byte) 185); // Custom packet ID: 185
                file_chunk.put(new byte[32]); // FT ID 32bytes hash -> dummy
                file_chunk.put((byte) 1); // Version: 1
                file_chunk.put((byte) 3); // Function: 3 -> dummy
                file_chunk.putInt(pos); // Position: pos
                file_chunk.put(bytes_chunck);
                file_chunk.rewind();

                byte[] data = new byte[(int) (MAX_FT_DATA_CHUNK + overhead)];
                data[0] = (byte) 185;

                int result = MainActivity.tox_friend_send_lossless_packet(friend_num, data, (int) (mod + overhead));

                while (result == -99)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (Exception ignored)
                    {
                    }

                    result = MainActivity.tox_friend_send_lossless_packet(friend_num, data, (int) (mod + overhead));
                }

                Log.i(TAG,
                      "hack_outgoing_ft:send result2=" + result + " pos=" + pos + " size=" + (int) (mod + overhead));
            }
        }
    }

    static boolean int_to_boolean(final int i)
    {
        if (i == 1)
        {
            return true;
        }

        return false;
    }

    public static String fourbytes_of_long_to_hex(final long in)
    {
        return String.format("%08x", in);
    }

    public static long getUnsignedInt(long x)
    {
        return x & 0x00000000ffffffffL;
    }
}
