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

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.Statement;

import static com.zoffcc.applications.trifa.HelperFriend.main_get_friend;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.s;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MAX_FILETRANSFER_SIZE_MSGV2;

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
            String ret = null;
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select key, value from TRIFADatabaseGlobalsNew where key='" + s(key) + "'");
            if (rs.next())
            {
                ret = rs.getString("value");
            }
            return ret;
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
                Statement statement = sqldb.createStatement();
                statement.execute("insert into TRIFADatabaseGlobalsNew (key, value) values('" + s(g_opts.key) + "', '" +
                                  s(g_opts.value) + "')");
                Log.i(TAG, "set_g_opts:(INSERT):key=" + key + " value=" + g_opts.value);
            }
            catch (Exception e)
            {
                try
                {
                    Statement statement = sqldb.createStatement();
                    statement.executeUpdate(
                            "update TRIFADatabaseGlobalsNew set key='" + s(g_opts.key) + "' where value='" +
                            s(g_opts.value) + "'");

                    Log.i(TAG, "set_g_opts:(UPDATE):key=" + key + " value=" + g_opts.value);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e.getMessage());
        }
    }

    static void update_savedata_file_wrapper(String password_hash_2)
    {
        try
        {
            MainActivity.semaphore_tox_savedata.acquire();
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

    public static MainActivity.send_message_result tox_friend_send_message_wrapper(long friendnum, int a_TOX_MESSAGE_TYPE, String message)
    {
        Log.d(TAG, "tox_friend_send_message_wrapper:" + friendnum);
        long friendnum_to_use = friendnum;
        FriendList f = main_get_friend(friendnum);
        Log.d(TAG, "tox_friend_send_message_wrapper:f=" + f);

        if (f != null)
        {
            Log.d(TAG, "tox_friend_send_message_wrapper:f conn" + f.TOX_CONNECTION_real);

            if (f.TOX_CONNECTION_real == TOX_CONNECTION_NONE.value)
            {
                String relay_pubkey = HelperRelay.get_relay_for_friend(f.tox_public_key_string);

                if (relay_pubkey != null)
                {
                    // friend has a relay
                    friendnum_to_use = tox_friend_by_public_key__wrapper(relay_pubkey);
                    Log.d(TAG, "tox_friend_send_message_wrapper:friendnum_to_use=" + friendnum_to_use);
                }
            }
        }

        MainActivity.send_message_result result = new MainActivity.send_message_result();
        ByteBuffer raw_message_buf = ByteBuffer.allocateDirect((int) TOX_MAX_FILETRANSFER_SIZE_MSGV2);
        ByteBuffer raw_message_length_buf = ByteBuffer.allocateDirect((int) 2); // 2 bytes for length
        ByteBuffer msg_id_buffer = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
        // use msg V2 API Call
        long t_sec = (System.currentTimeMillis() / 1000);
        long res = MainActivity.tox_util_friend_send_message_v2(friendnum_to_use, a_TOX_MESSAGE_TYPE, t_sec, message,
                                                                message.length(), raw_message_buf,
                                                                raw_message_length_buf, msg_id_buffer);
        if (PREF__X_battery_saving_mode)
        {
            Log.i(TAG, "global_last_activity_for_battery_savings_ts:002:*PING*");
        }
        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();
        Log.d(TAG, "tox_friend_send_message_wrapper:res=" + res);

        //** workaround **//
        byte[] tmp_buf_ = new byte[raw_message_length_buf.remaining()];
        Log.d(TAG, "tox_friend_send_message_wrapper:raw_message_length_buf.remaining()=" + raw_message_length_buf.remaining());
        raw_message_length_buf.slice().get(tmp_buf_);
        int raw_message_length_int = tmp_buf_[0] & 0xFF + (tmp_buf_[1] & 0xFF) * 256;
        Log.d(TAG, "tox_friend_send_message_wrapper:raw_message_length_int=" + raw_message_length_int);

        //int raw_message_length_int = raw_message_length_buf.
        //        array()[raw_message_length_buf.arrayOffset()] & 0xFF + (raw_message_length_buf.
        //        array()[raw_message_length_buf.arrayOffset() + 1] & 0xFF) * 256;
        //** workaround **//


        // Log.i(TAG,
        //      "tox_friend_send_message_wrapper:message=" + message + " res=" + res + " len=" + raw_message_length_int);
        result.error_num = res;

        if (res == -9999)
        {
            // msg V2 OK
            result.msg_num = (Long.MAX_VALUE - 1);
            result.msg_v2 = true;
            result.msg_hash_hex = bytesToHex(msg_id_buffer.array(), msg_id_buffer.arrayOffset(), msg_id_buffer.limit());
            result.raw_message_buf_hex = bytesToHex(raw_message_buf.array(), raw_message_buf.arrayOffset(),
                                                    raw_message_length_int);
            Log.i(TAG, "tox_friend_send_message_wrapper:hash_hex=" + result.msg_hash_hex + " raw_msg_hex" +
                       result.raw_message_buf_hex);
            return result;
        }
        else if (res == -9991)
        {
            // msg V2 error
            result.msg_num = -1;
            result.msg_v2 = true;
            result.msg_hash_hex = "";
            result.raw_message_buf_hex = "";
            return result;
        }
        else
        {
            // old message
            result.msg_num = res;
            result.msg_v2 = false;
            result.msg_hash_hex = "";
            result.raw_message_buf_hex = "";
            return result;
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
            Log.i(TAG, "get_last_rowid:ret=" + ret);
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_last_rowid:EE1:" + e.getMessage());
            return -1;
        }
    }
}
