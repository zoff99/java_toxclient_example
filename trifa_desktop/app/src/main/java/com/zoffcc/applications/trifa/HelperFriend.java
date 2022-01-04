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

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.FriendListFragmentJ.add_all_friends_clear;
import static com.zoffcc.applications.trifa.HelperRelay.get_pushurl_for_friend;
import static com.zoffcc.applications.trifa.HelperRelay.is_valid_pushurl_for_friend_with_whitelist;
import static com.zoffcc.applications.trifa.TRIFAGlobals.LAST_ONLINE_TIMSTAMP_ONLINE_NOW;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_name;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.time.temporal.ChronoUnit.SECONDS;

public class HelperFriend
{
    private static final String TAG = "trifa.Hlp.Friend";

    static FriendList main_get_friend(long friendnum)
    {
        FriendList f = null;

        try
        {
            String pubkey_temp = tox_friend_get_public_key__wrapper(friendnum);
            // Log.i(TAG, "main_get_friend:pubkey=" + pubkey_temp + " fnum=" + friendnum);
            List<FriendList> fl = orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList();

            // Log.i(TAG, "main_get_friend:fl=" + fl + " size=" + fl.size());

            if (fl.size() > 0)
            {
                f = fl.get(0);
                // Log.i(TAG, "main_get_friend:f=" + f);
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

    static FriendList main_get_friend(String friend_pubkey)
    {
        FriendList f = null;

        try
        {
            List<FriendList> fl = orma.selectFromFriendList().
                    tox_public_key_stringEq(friend_pubkey).
                    toList();

            if (fl.size() > 0)
            {
                f = fl.get(0);
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
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                name(f.name).
                execute();
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
            if (orma.selectFromFriendList().tox_public_key_stringEq(f.tox_public_key_string).count() == 0)
            {
                f.added_timestamp = System.currentTimeMillis();
                orma.insertIntoFriendList(f);
                // Log.i(TAG, "friend added to DB: " + f.tox_public_key_string);
            }
            else
            {
                // friend already in DB
                // Log.i(TAG, "friend already in DB: " + f.tox_public_key_string);
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
                // Log.d(TAG, "add_friend_to_system:fnum add=" + friendnum);

                if (friendnum == 4294967295L) // 0xffffffff == UINT32_MAX
                {
                    // Log.d(TAG, "add_friend_to_system:fnum add res=0xffffffff as_friends_relay=" + as_friends_relay);
                    // adding friend failed
                    // if its a relay still try to update it in our DB
                    if (as_friends_relay)
                    {
                        // add relay for friend to DB
                        // Log.d(TAG, "add_friend_to_system:add_or_update_friend_relay");
                        HelperRelay.add_or_update_friend_relay(friend_public_key, owner_public_key);
                        add_all_friends_clear_wrapper(10);
                    }
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
                f.capabilities = 0;

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

            SwingUtilities.invokeLater(myRunnable);

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
            orma.updateFriendList().
                    tox_public_key_stringEq(f.tox_public_key_string).
                    TOX_CONNECTION_real(f.TOX_CONNECTION_real).
                    TOX_CONNECTION_on_off_real(f.TOX_CONNECTION_on_off_real).
                    execute();
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
            orma.updateFriendList().
                    tox_public_key_stringEq(f.tox_public_key_string).
                    last_online_timestamp_real(f.last_online_timestamp_real).
                    execute();
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
            orma.updateFriendList().
                    tox_public_key_stringEq(f.tox_public_key_string).
                    last_online_timestamp(f.last_online_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_friend_in_db_capabilities(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                capabilities(f.capabilities).
                execute();
    }

    synchronized static void update_friend_in_db_connection_status(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                TOX_CONNECTION(f.TOX_CONNECTION).
                TOX_CONNECTION_on_off(f.TOX_CONNECTION_on_off).
                execute();
    }

    static String get_friend_name_from_pubkey(String friend_pubkey)
    {
        String ret = "Unknown";
        String friend_alias_name = "";
        String friend_name = "";

        try
        {
            friend_alias_name = orma.selectFromFriendList().
                    tox_public_key_stringEq(friend_pubkey).
                    get(0).alias_name;
        }
        catch (Exception e)
        {
            friend_alias_name = "";
            e.printStackTrace();
        }

        if ((friend_alias_name == null) || (friend_alias_name.equals("")))
        {
            try
            {
                friend_name = orma.selectFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        get(0).name;
            }
            catch (Exception e)
            {
                friend_name = "";
                e.printStackTrace();
            }

            if ((friend_name != null) && (!friend_name.equals("")))
            {
                ret = friend_name;
            }
        }
        else
        {
            ret = friend_alias_name;
        }

        return ret;
    }

    static void send_friend_msg_receipt_v2_wrapper(final long friend_number, final int msg_type, final ByteBuffer msg_id_buffer, long t_sec_receipt)
    {
        // (msg_type == 1) msgV2 direct message
        // (msg_type == 2) msgV2 relay message
        // (msg_type == 3) msgV2 group confirm msg received message
        // (msg_type == 4) msgV2 confirm unknown received message
        if (msg_type == 1)
        {
            // send message receipt v2
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
            final Thread t = new Thread()
            {
                @Override
                public void run()
                {

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

                    SwingUtilities.invokeLater(myRunnable);
                }
            };
            t.start();
        }
        else if (msg_type == 3)
        {
            // send message receipt v2
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
        else if (msg_type == 4)
        {
            // send message receipt v2 for unknown message
            MainActivity.tox_util_friend_send_msg_receipt_v2(friend_number, t_sec_receipt, msg_id_buffer);
        }
    }

    static int is_friend_online_real(long friendnum)
    {
        try
        {
            return (orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0).TOX_CONNECTION_real);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static int is_friend_online_real_pk(String friend_pk)
    {
        try
        {
            return (orma.selectFromFriendList().
                    tox_public_key_stringEq(friend_pk).
                    toList().get(0).TOX_CONNECTION_real);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    synchronized static void set_all_friends_offline()
    {
        try
        {
            orma.updateFriendList().
                    TOX_CONNECTION(0).
                    execute();
        }
        catch (Exception e)
        {
        }

        try
        {
            orma.updateFriendList().
                    TOX_CONNECTION_real(0).
                    execute();
        }
        catch (Exception e)
        {
        }

        try
        {
            orma.updateFriendList().
                    TOX_CONNECTION_on_off(0).
                    execute();
        }
        catch (Exception e)
        {
        }

        try
        {
            orma.updateFriendList().
                    TOX_CONNECTION_on_off_real(0).
                    execute();
        }
        catch (Exception e)
        {
        }

        try
        {
            orma.updateFriendList().
                    last_online_timestampEq(LAST_ONLINE_TIMSTAMP_ONLINE_NOW).
                    last_online_timestamp(System.currentTimeMillis()).
                    execute();
        }
        catch (Exception e)
        {
        }

        try
        {
            orma.updateFriendList().
                    last_online_timestamp_realEq(LAST_ONLINE_TIMSTAMP_ONLINE_NOW).
                    last_online_timestamp_real(System.currentTimeMillis()).
                    execute();
        }
        catch (Exception e)
        {
        }
    }

    static int is_friend_online(long friendnum)
    {
        try
        {
            return (orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0).TOX_CONNECTION);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static int is_friend_online_real_and_has_msgv3(long friendnum)
    {
        try
        {
            final FriendList f = orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0);
            if ((f.TOX_CONNECTION_real != 0) && (f.msgv3_capability == 1))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static int is_friend_online_real_and_hasnot_msgv3(long friendnum)
    {
        try
        {
            final FriendList f = orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0);
            if ((f.TOX_CONNECTION_real != 0) && (f.msgv3_capability != 1))
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    static void del_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        try
        {
            boolean avatar_filesize_non_zero = false;
            File f1 = null;

            try
            {
                f1 = new File(avatar_path_name + "/" + avatar_file_name);

                if (f1.length() > 0)
                {
                    avatar_filesize_non_zero = true;
                }

                f1.delete();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                    avatar_pathname(null).
                    avatar_filename(null).
                    avatar_update(false).
                    avatar_update_timestamp(System.currentTimeMillis()).
                    execute();

            HelperGeneric.update_display_friend_avatar(friend_pubkey, avatar_path_name, avatar_file_name);
        }
        catch (Exception e)
        {
            Log.i(TAG, "set_friend_avatar:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void set_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        try
        {
            boolean avatar_filesize_non_zero = false;
            File f1 = null;

            try
            {
                f1 = new File(avatar_path_name + "/" + avatar_file_name);

                if (f1.length() > 0)
                {
                    avatar_filesize_non_zero = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "set_friend_avatar:EE01:" + e.getMessage());
            }

            // Log.i(TAG, "set_friend_avatar:update:pubkey=" + friend_pubkey.substring(0,4) + " path=" + avatar_path_name + " file=" +
            // avatar_file_name);

            if (avatar_filesize_non_zero)
            {
                orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                        avatar_pathname(avatar_path_name).
                        avatar_filename(avatar_file_name).
                        avatar_update(false).
                        avatar_update_timestamp(System.currentTimeMillis()).
                        execute();
            }
            else
            {
                orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                        avatar_pathname(null).
                        avatar_filename(null).
                        avatar_update(false).
                        avatar_update_timestamp(System.currentTimeMillis()).
                        execute();
            }

            HelperGeneric.update_display_friend_avatar(friend_pubkey, avatar_path_name, avatar_file_name);
        }
        catch (Exception e)
        {
            Log.i(TAG, "set_friend_avatar:EE02:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void add_pushurl_for_friend(final String friend_push_url, final String friend_pubkey)
    {
        try
        {
            orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).push_url(friend_push_url).execute();
        }
        catch (Exception e)
        {
            Log.i(TAG, "add_pushurl_for_friend:EE:" + e.getMessage());
        }
    }

    static void remove_pushurl_for_friend(final String friend_pubkey)
    {
        try
        {
            orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).push_url(null).execute();
        }
        catch (Exception e)
        {
            Log.i(TAG, "remove_pushurl_for_friend:EE:" + e.getMessage());
        }
    }

    static void friend_call_push_url(final String friend_pubkey)
    {
        try
        {
            final String pushurl_for_friend = get_pushurl_for_friend(friend_pubkey);

            if (pushurl_for_friend != null)
            {
                if (pushurl_for_friend.length() > "https://".length())
                {
                    if (is_valid_pushurl_for_friend_with_whitelist(pushurl_for_friend))
                    {

                        Thread t = new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    HttpClient client = null;

                                    client = HttpClient.newBuilder().
                                            connectTimeout(Duration.of(5, SECONDS)).
                                            build();

                                    //                                   cacheControl(new CacheControl.Builder().noCache().build()).

                                    HttpRequest request = HttpRequest.newBuilder().
                                            uri(URI.create(pushurl_for_friend)).
                                            timeout(Duration.of(5, SECONDS)).
                                            POST(HttpRequest.BodyPublishers.ofString("ping=1")).
                                            build();

                                    try
                                    {
                                        HttpResponse<String> response = client.send(request,
                                                                                    HttpResponse.BodyHandlers.ofString());
                                        Log.i(TAG, "friend_call_push_url:url=" + pushurl_for_friend + " RES=" +
                                                   response.statusCode());
                                    }
                                    catch (Exception ignored)
                                    {
                                    }
                                }
                                catch (Exception e)
                                {
                                    Log.i(TAG, "friend_call_push_url:001:EE:" + e.getMessage());
                                }
                            }
                        };
                        t.start();
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    static void delete_friend_all_files(final long friendnum)
    {
        try
        {
            Iterator<FileDB> i1 = orma.selectFromFileDB().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friendnum)).
                    directionEq(TRIFA_FT_DIRECTION_INCOMING.value).
                    toList().iterator();
            MainActivity.selected_messages.clear();
            MainActivity.selected_messages_text_only.clear();
            MainActivity.selected_messages_incoming_file.clear();

            while (i1.hasNext())
            {
                try
                {
                    long file_id = i1.next().id;
                    long msg_id = orma.selectFromMessage().filedb_idEq(file_id).directionEq(0).
                            tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).get(0).id;
                    MainActivity.selected_messages.add(msg_id);
                    MainActivity.selected_messages_incoming_file.add(msg_id);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }

            HelperMessage.delete_selected_messages(null, false, false, "deleting Messages ...");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            orma.deleteFromFileDB().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_filetransfers(final long friendnum)
    {
        try
        {
            Log.i(TAG, "delete_ft:ALL for friend=" + friendnum);
            orma.deleteFromFiletransfer().tox_public_key_stringEq(
                    tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_messages(final long friendnum)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
            }
        };
        t.start();
    }

    static void delete_friend(final String friend_pubkey)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        execute();
            }
        };
        t.start();
    }

    static String resolve_name_for_pubkey(String pub_key, String default_name)
    {
        String ret = default_name;

        try
        {
            try
            {
                if (pub_key.equals(global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2))))
                {
                    // its our own key
                    ret = global_my_name;
                    return ret;
                }
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }

            FriendList fl = orma.selectFromFriendList().
                    tox_public_key_stringEq(pub_key).
                    toList().get(0);

            if (fl.name != null)
            {
                if (fl.name.length() > 0)
                {
                    ret = fl.name;
                }
            }

            if (fl.alias_name != null)
            {
                if (fl.alias_name.length() > 0)
                {
                    ret = fl.alias_name;
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            ret = default_name;
        }

        return ret;
    }

    static String get_friend_name_from_num(long friendnum)
    {
        String result = "Unknown";

        try
        {
            if (orma != null)
            {
                try
                {
                    String result_alias = orma.selectFromFriendList().
                            tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                            toList().get(0).alias_name;

                    if (result_alias != null)
                    {
                        if (result_alias.length() > 0)
                        {
                            result = result_alias;
                            return result;
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                result = orma.selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        toList().get(0).name;
            }
        }
        catch (Exception e)
        {
            result = "Unknown";
            e.printStackTrace();
        }

        return result;
    }

    static void update_friend_msgv3_capability(long friend_number, int new_value)
    {
        try
        {
            if ((new_value == 0) || (new_value == 1))
            {
                FriendList f = orma.selectFromFriendList().
                        tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                        get(0);
                if (f != null)
                {
                    if (f.msgv3_capability != new_value)
                    {
                        Log.i(TAG,
                              "update_friend_msgv3_capability f=" + get_friend_name_from_num(friend_number) + " new=" +
                              new_value + " old=" + f.msgv3_capability);
                        orma.updateFriendList().
                                tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                                msgv3_capability(new_value).
                                execute();
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    static long get_friend_msgv3_capability(long friend_number)
    {
        long ret = 0;
        try
        {
            FriendList f = orma.selectFromFriendList().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    get(0);
            if (f != null)
            {
                // Log.i(TAG, "get_friend_msgv3_capability:f=" +
                //           get_friend_name_from_pubkey(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)) +
                //           " f=" + f.msgv3_capability);
                ret = f.msgv3_capability;
            }

            return ret;
        }
        catch (Exception e)
        {
            return 0;
        }
    }
}
