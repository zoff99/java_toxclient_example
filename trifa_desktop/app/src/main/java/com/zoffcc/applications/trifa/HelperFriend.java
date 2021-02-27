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

import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.FriendListFragmentJ.add_all_friends_clear;
import static com.zoffcc.applications.trifa.MainActivity.s;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.TRIFAGlobals.DELAY_SENDING_FRIEND_RECEIPT_TO_RELAY_MS;

public class HelperFriend
{
    private static final String TAG = "trifa.Hlp.Friend";

    static FriendList main_get_friend(long friendnum)
    {
        FriendList f = null;

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery("select * from FriendList where tox_public_key_string='" +
                                                  s(tox_friend_get_public_key__wrapper(friendnum)) + "'");
            if (rs.next())
            {
                f = new FriendList();
                f.tox_public_key_string = rs.getString("tox_public_key_string");
                f.name = rs.getString("name");
                f.alias_name = rs.getString("alias_name");
                f.TOX_CONNECTION = rs.getInt("TOX_CONNECTION");
                f.TOX_CONNECTION_real = rs.getInt("TOX_CONNECTION_real");
                f.TOX_CONNECTION_on_off = rs.getInt("TOX_CONNECTION_on_off");
                f.TOX_CONNECTION_on_off_real = rs.getInt("TOX_CONNECTION_on_off_real");
                f.TOX_USER_STATUS = rs.getInt("TOX_USER_STATUS");
                f.last_online_timestamp = rs.getLong("last_online_timestamp");
                f.last_online_timestamp_real = rs.getLong("last_online_timestamp_real");
                if (rs.getInt("is_relay") == 0)
                {
                    f.is_relay = false;
                }
                else
                {
                    f.is_relay = true;
                }
            }
            else
            {
                f = null;
            }
        }
        catch (Exception e)
        {
            f = null;
        }

        return f;
    }

    synchronized static void update_friend_in_db_name(FriendList f)
    {
        try
        {
            Statement statement = sqldb.createStatement();
            statement.executeUpdate("update FriendList set name='" + s(f.name) + "' where tox_public_key_string = '" +
                                    s(f.tox_public_key_string) + "'");
        }
        catch (Exception e)
        {

        }
    }

    static void add_friend_real(String friend_tox_id)
    {
        // Log.i(TAG, "add_friend_real:add friend ID:" + friend_tox_id);
        // add friend ---------------
        long friendnum = MainActivity.tox_friend_add(friend_tox_id, "please add me"); // add friend
        Log.i(TAG, "add_friend_real:add friend  #:" + friendnum);
        HelperGeneric.update_savedata_file_wrapper(
                MainActivity.password_hash); // save toxcore datafile (new friend added)

        if (friendnum > -1)
        {
            // nospam=8 chars, checksum=4 chars
            String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
            // Log.i(TAG, "add_friend_real:add friend PK:" + friend_public_key);
            FriendList f = new FriendList();
            f.tox_public_key_string = friend_public_key;

            try
            {
                // set name as the last 5 char of TOXID (until we get a name sent from friend)
                f.name = friend_public_key.substring(friend_public_key.length() - 5, friend_public_key.length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                f.name = "Unknown";
            }

            f.TOX_USER_STATUS = 0;
            f.TOX_CONNECTION = 0;
            f.TOX_CONNECTION_on_off = HelperGeneric.get_toxconnection_wrapper(f.TOX_CONNECTION);
            f.avatar_filename = null;
            f.avatar_pathname = null;

            try
            {
                insert_into_friendlist_db(f);
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }

            update_single_friend_in_friendlist_view(f);
        }

        if (friendnum == -1)
        {
            Log.i(TAG, "add_friend_real:friend already added, or request already sent");

            /*
            // still add the friend to the DB
            String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
            add_friend_to_system(friend_public_key, false, null);
            */
        }
        else if (friendnum < -1)
        {
            Log.i(TAG, "add_friend_real:some error occured");
        }

        // add friend ---------------
    }

    static void update_single_friend_in_friendlist_view(final FriendList f)
    {
        try
        {
            if (MainActivity.FriendPanel != null)
            {
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = true;
                cc.friend_item = f;
                MainActivity.FriendPanel.modify_friend(cc, cc.is_friend);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void insert_into_friendlist_db(final FriendList f)
    {
        //        Thread t = new Thread()
        //        {
        //            @Override
        //            public void run()
        //            {
        try
        {
            int count = 0;
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    "select count(*) as count from FriendList where tox_public_key_string='" +
                    s(f.tox_public_key_string) + "'");
            rs.next();
            count = rs.getInt("count");
            Log.i(TAG, "friend to DB: count=" + count);

            if (count == 0)
            {
                f.added_timestamp = System.currentTimeMillis();
                statement.executeUpdate(
                        "insert into FriendList (" + "tox_public_key_string , is_relay, name" + ")" + " values(" + "'" +
                        f.tox_public_key_string + "' , '0', '" + f.name + "' " + ")");
                Log.i(TAG, "friend added to DB: " + f.tox_public_key_string);
            }
            else
            {
                // friend already in DB
                Log.i(TAG, "friend already in DB: " + f.tox_public_key_string);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "friend added to DB:EE:" + e.getMessage());
        }

        //            }
        //        };
        //        t.start();
    }

    public static String tox_friend_get_public_key__wrapper(long friend_number)
    {
        if (MainActivity.cache_fnum_pubkey.containsKey(friend_number))
        {
            // Log.i(TAG, "cache hit:2");
            return MainActivity.cache_fnum_pubkey.get(friend_number);
        }
        else
        {
            if (MainActivity.cache_fnum_pubkey.size() >= 180)
            {
                // TODO: bad!
                MainActivity.cache_fnum_pubkey.clear();
            }

            String result = MainActivity.tox_friend_get_public_key(friend_number);
            MainActivity.cache_fnum_pubkey.put(friend_number, result);
            return result;
        }
    }

    public static long tox_friend_by_public_key__wrapper(String friend_public_key_string)
    {
        if (MainActivity.cache_pubkey_fnum.containsKey(friend_public_key_string))
        {
            // Log.i(TAG, "cache hit:1");
            return MainActivity.cache_pubkey_fnum.get(friend_public_key_string);
        }
        else
        {
            if (MainActivity.cache_pubkey_fnum.size() >= 180)
            {
                // TODO: bad!
                MainActivity.cache_pubkey_fnum.clear();
            }

            long result = MainActivity.tox_friend_by_public_key(friend_public_key_string);
            MainActivity.cache_pubkey_fnum.put(friend_public_key_string, result);
            return result;
        }
    }

    static void add_friend_to_system(final String friend_public_key, final boolean as_friends_relay, final String owner_public_key)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // toxcore needs this!!
                    Thread.sleep(10);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                long friendnum = MainActivity.tox_friend_add_norequest(friend_public_key); // add friend
                Log.d(TAG, "add_friend_to_system:fnum add=" + friendnum);

                if (friendnum == 0xffffffff) // 0xffffffff == UINT32_MAX
                {
                    // adding friend failed
                    return;
                }

                try
                {
                    Thread.sleep(20);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                HelperGeneric.update_savedata_file_wrapper(
                        MainActivity.password_hash); // save toxcore datafile (new friend added)
                final FriendList f = new FriendList();
                f.tox_public_key_string = friend_public_key;
                f.TOX_USER_STATUS = 0;
                f.TOX_CONNECTION = 0;
                f.TOX_CONNECTION_on_off = HelperGeneric.get_toxconnection_wrapper(f.TOX_CONNECTION);
                // set name as the last 5 char of the publickey (until we get a proper name)
                f.name = friend_public_key.substring(friend_public_key.length() - 5, friend_public_key.length());
                f.avatar_pathname = null;
                f.avatar_filename = null;

                try
                {
                    // Log.i(TAG, "friend_request:insert:001:f=" + f);
                    f.added_timestamp = System.currentTimeMillis();
                    insert_into_friendlist_db(f);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "friend_request:insert:EE2:" + e.getMessage());
                    return;
                }

                if (as_friends_relay)
                {
                    // add relay for friend to DB
                    // Log.d(TAG, "add_friend_to_system:add_or_update_friend_relay");
                    HelperRelay.add_or_update_friend_relay(friend_public_key, owner_public_key);
                    // update friendlist on screen
                    add_all_friends_clear_wrapper(10);
                }
                else
                {
                    update_single_friend_in_friendlist_view(f);
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----

                try
                {
                    Thread.sleep(100);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                //**//if (MainActivity.PREF__U_keep_nospam == false)
                {
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    //**//HelperGeneric.set_new_random_nospam_value();
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                    // ---- set new random nospam value after each added friend ----
                }
            }
        };
        t.start();
    }

    static void add_all_friends_clear_wrapper(int delay)
    {
        try
        {
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        add_all_friends_clear(delay);
                    }
                    catch (Exception e)
                    {
                    }
                }
            };

            Log.i(TAG, "invokeLater:001:s");
            SwingUtilities.invokeLater(myRunnable);
            Log.i(TAG, "invokeLater:001:e");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_connection_status_real(FriendList f)
    {
        try
        {
            Statement statement = sqldb.createStatement();
            statement.executeUpdate(
                    "update FriendList set " + " TOX_CONNECTION_real='" + s(f.TOX_CONNECTION_real) + "'," +
                    " TOX_CONNECTION_on_off_real='" + s(f.TOX_CONNECTION_on_off_real) + "'" +
                    " where tox_public_key_string = '" + s(f.tox_public_key_string) + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_last_online_timestamp_real(FriendList f)
    {
        try
        {
            Statement statement = sqldb.createStatement();
            statement.executeUpdate(
                    "update FriendList set " + " last_online_timestamp_real='" + s(f.last_online_timestamp_real) + "'" +
                    " where tox_public_key_string = '" + s(f.tox_public_key_string) + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_last_online_timestamp(FriendList f)
    {
        // Log.i(TAG, "update_friend_in_db_last_online_timestamp");
        try
        {
            Statement statement = sqldb.createStatement();
            statement.executeUpdate(
                    "update FriendList set " + " last_online_timestamp='" + s(f.last_online_timestamp) + "'" +
                    " where tox_public_key_string = '" + s(f.tox_public_key_string) + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_connection_status(FriendList f)
    {
        try
        {
            Statement statement = sqldb.createStatement();
            statement.executeUpdate("update FriendList set " + " TOX_CONNECTION='" + s(f.TOX_CONNECTION) + "'," +
                                    " TOX_CONNECTION_on_off='" + s(f.TOX_CONNECTION_on_off) + "'" +
                                    " where tox_public_key_string = '" + s(f.tox_public_key_string) + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    static void send_friend_msg_receipt_v2_wrapper(final long friend_number, final int msg_type, final ByteBuffer msg_id_buffer)
    {
        // (msg_type == 1) msgV2 direct message
        // (msg_type == 2) msgV2 relay message
        // (msg_type == 3) msgV2 group confirm msg received message
        // (msg_type == 4) msgV2 confirm unknown received message
        if (msg_type == 1)
        {
            // send message receipt v2
            long t_sec_receipt = (System.currentTimeMillis() / 1000);
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);

            try
            {
                String relay_for_friend = HelperRelay.get_relay_for_friend(
                        tox_friend_get_public_key__wrapper(friend_number));

                if (relay_for_friend != null)
                {
                    // if friend has a relay, send the "msg receipt" also to the relay. just to be sure.
                    MainActivity.tox_util_friend_send_msg_receipt_v2(
                            tox_friend_by_public_key__wrapper(relay_for_friend), t_sec_receipt, msg_id_buffer);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (msg_type == 2)
        {
            // send message receipt v2
            final long t_sec_receipt = (System.currentTimeMillis() / 1000);
            final Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    // delay sending of msg receipt for x milliseconds
                    try
                    {
                        Thread.sleep(DELAY_SENDING_FRIEND_RECEIPT_TO_RELAY_MS);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    // send msg receipt on main thread
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                ByteBufferCompat msg_id_buffer_compat = new ByteBufferCompat(msg_id_buffer);
                                String msg_id_as_hex_string = HelperGeneric.bytesToHex(msg_id_buffer_compat.array(),
                                                                                       msg_id_buffer_compat.arrayOffset(),
                                                                                       msg_id_buffer_compat.limit());
                                // Log.i(TAG, "send_friend_msg_receipt_v2_wrapper:send delayed -> now msgid=" +
                                //            msg_id_as_hex_string);

                                try
                                {
                                    MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt,
                                                                                     msg_id_buffer);

                                    try
                                    {
                                        String relay_for_friend = HelperRelay.get_relay_for_friend(
                                                tox_friend_get_public_key__wrapper(friend_number));

                                        if (relay_for_friend != null)
                                        {
                                            // if friend has a relay, send the "msg receipt" also to the relay. just to be sure.
                                            MainActivity.tox_util_friend_send_msg_receipt_v2(
                                                    tox_friend_by_public_key__wrapper(relay_for_friend), t_sec_receipt,
                                                    msg_id_buffer);
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };

                    Log.i(TAG, "invokeLater:010:s");
                    SwingUtilities.invokeLater(myRunnable);
                    Log.i(TAG, "invokeLater:010:e");
                }
            };
            t.start();
        }
        else if (msg_type == 3)
        {
            // send message receipt v2
            long t_sec_receipt = (System.currentTimeMillis() / 1000);
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
        else if (msg_type == 4)
        {
            // send message receipt v2 for unknown message
            long t_sec_receipt = (System.currentTimeMillis() / 1000);
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
    }
}
