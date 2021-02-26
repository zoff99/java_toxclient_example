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

}
