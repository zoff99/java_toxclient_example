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

import java.nio.ByteBuffer;

import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_ID_LENGTH;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperConference
{
    private static final String TAG = "trifa.Hlp.Conf";

    static void set_all_conferences_inactive()
    {
        try
        {
            orma.updateConferenceDB().
                    conference_active(false).
                    tox_conference_number(-1).
                    execute();

            Log.i(TAG, "set_all_conferences_inactive");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_all_conferences_inactive:EE:" + e.getMessage());
        }
    }

    static void add_conference_wrapper(final long friend_number, long conference_num, String conference_identifier_in, final int a_TOX_CONFERENCE_TYPE, boolean has_conference_identifier)
    {
        if (conference_num < 0)
        {
            Log.d(TAG, "add_conference_wrapper:ERR:conference number less than zero:" + conference_num);
            return;
        }

        //Log.d(TAG, "add_conference_wrapper:confnum=" + conference_num + " conference_identifier_in=" +
        //           conference_identifier_in);
        String conference_identifier = conference_identifier_in;

        if (has_conference_identifier != true)
        {
            //Log.d(TAG, "add_conference_wrapper:need to get conference_identifier");
            // we need to get the conference identifier
            ByteBuffer cookie_buf3 = ByteBuffer.allocateDirect((int) CONFERENCE_ID_LENGTH * 2);
            cookie_buf3.clear();
            if (MainActivity.tox_conference_get_id(conference_num, cookie_buf3) == 0)
            {
                byte[] cookie_buffer = new byte[CONFERENCE_ID_LENGTH];
                cookie_buf3.get(cookie_buffer, 0, CONFERENCE_ID_LENGTH);
                conference_identifier = HelperGeneric.bytes_to_hex(cookie_buffer);
            }
            else
            {
                Log.d(TAG, "add_conference_wrapper:ERR:error getting conference identifier");
                return;
            }
        }

        //Log.d(TAG, "add_conference_wrapper:conference_identifier=" + conference_identifier);

        if (conference_num >= 0)
        {
            new_or_updated_conference(conference_num, HelperFriend.tox_friend_get_public_key__wrapper(friend_number),
                                      conference_identifier, a_TOX_CONFERENCE_TYPE); // joining new conference
        }
        else
        {
            //Log.i(TAG, "add_conference_wrapper:error=" + conference_num + " joining conference");
        }

        // save tox savedate file
        HelperGeneric.update_savedata_file_wrapper(MainActivity.password_hash);
    }

    static void new_or_updated_conference(long conference_number, String who_invited_public_key, String conference_identifier, int conference_type)
    {
        try
        {
            // Log.i(TAG, "new_or_updated_conference:" + "conference_number=" + conference_identifier);
            final ConferenceDB conf2 = orma.selectFromConferenceDB().
                    conference_identifierEq(conference_identifier).toList().get(0);
            // conference already exists -> update and connect
            orma.updateConferenceDB().
                    conference_identifierEq(conference_identifier).
                    conference_active(true).
                    kind(conference_type).
                    tox_conference_number(conference_number).execute();

            try
            {
                Log.i(TAG, "new_or_updated_conference:*update*");
                final ConferenceDB conf3 = orma.selectFromConferenceDB().
                        conference_identifierEq(conference_identifier).toList().get(0);
                // update or add to "friendlist"
                CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                cc.is_friend = false;
                cc.conference_item = ConferenceDB.deep_copy(conf3);
                MainActivity.FriendPanel.modify_friend(cc, cc.is_friend);
            }
            catch (Exception e3)
            {
                Log.i(TAG, "new_or_updated_conference:EE3:" + e3.getMessage());
            }

            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "new_or_updated_conference:EE1:" + e.getMessage());

            // conference is new -> add
            try
            {
                ConferenceDB conf_new = new ConferenceDB();
                conf_new.conference_identifier = conference_identifier;
                conf_new.who_invited__tox_public_key_string = who_invited_public_key;
                conf_new.peer_count = -1;
                conf_new.own_peer_number = -1;
                conf_new.kind = conference_type;
                conf_new.tox_conference_number = conference_number;
                conf_new.conference_active = true;
                //
                orma.insertIntoConferenceDB(conf_new);
                Log.i(TAG, "new_or_updated_conference:+ADD+");

                try
                {
                    // update or add to "friendlist"
                    CombinedFriendsAndConferences cc = new CombinedFriendsAndConferences();
                    cc.is_friend = false;
                    cc.conference_item = ConferenceDB.deep_copy(conf_new);
                    MainActivity.FriendPanel.modify_friend(cc, cc.is_friend);
                }
                catch (Exception e4)
                {
                    Log.i(TAG, "new_or_updated_conference:EE4:" + e4.getMessage());
                }

                return;
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
                Log.i(TAG, "new_or_updated_conference:EE2:" + e1.getMessage());
            }
        }
    }

}
