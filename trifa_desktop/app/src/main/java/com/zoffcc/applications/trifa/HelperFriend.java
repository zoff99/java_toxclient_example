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

import java.sql.ResultSet;
import java.sql.Statement;

import static com.zoffcc.applications.trifa.MainActivity.sqldb;

public class HelperFriend
{
    private static final String TAG = "trifa.Hlp.Friend";

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

            // TODO: update_single_friend_in_friendlist_view(f);
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
                    "select count(*) as count from FriendList where tox_public_key_string='" + f.tox_public_key_string +
                    "'");
            rs.next();
            count = rs.getInt("count");
            Log.i(TAG, "friend to DB: count=" + count);

            if (count == 0)
            {
                f.added_timestamp = System.currentTimeMillis();
                statement.executeUpdate("insert into FriendList (" + "tox_public_key_string" + ")" + " values(" + "'" +
                                        f.tox_public_key_string + "'" + ")");
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

}
