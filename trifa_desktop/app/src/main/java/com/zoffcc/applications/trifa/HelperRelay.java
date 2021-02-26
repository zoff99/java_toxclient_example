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

import static com.zoffcc.applications.trifa.MainActivity.s;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;

public class HelperRelay
{
    private static final String TAG = "trifa.Hlp.Relay";

    static String get_relay_for_friend(String friend_pubkey)
    {
        try
        {
            String ret = null;
            Statement statement = sqldb.createStatement();

            ResultSet rs = statement.executeQuery(
                    "select tox_public_key_string from RelayListDB where own_relay='0' and tox_public_key_string_of_owner='" +
                    s(friend_pubkey) + "'");
            if (rs.next())
            {
                ret = rs.getString("tox_public_key_string");
            }
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_relay_for_friend:EE1:" + e.getMessage());
            return null;
        }
    }
}
