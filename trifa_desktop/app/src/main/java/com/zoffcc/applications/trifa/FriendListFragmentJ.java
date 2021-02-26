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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.MainActivity.s;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;

public class FriendListFragmentJ extends JPanel
{

    private static final String TAG = "trifa.FriendListFrgnt";

    private JList<String> friends_and_confs_list;
    DefaultListModel<String> friends_and_confs_list_model;
    JScrollPane FriendScrollPane;

    static Boolean in_update_data = false;
    static final Boolean in_update_data_lock = false;

    public FriendListFragmentJ()
    {
        friends_and_confs_list_model = new DefaultListModel<>();

        friends_and_confs_list = new JList<>(friends_and_confs_list_model);
        friends_and_confs_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friends_and_confs_list.setSelectedIndex(0);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FriendScrollPane = new JScrollPane();
        add(FriendScrollPane);
        FriendScrollPane.setViewportView(friends_and_confs_list);

        add_all_friends_clear(1);
    }

    synchronized void add_all_friends_clear(final int delay)
    {
        // Log.i(TAG, "add_all_friends_clear");
        try
        {
            synchronized (in_update_data_lock)
            {
                if (in_update_data == true)
                {
                    // Log.i(TAG, "add_all_friends_clear:already updating!");
                }
                else
                {
                    in_update_data = true;
                    friends_and_confs_list_model.clear();

                    // ------------- add rest of friends  -------------
                    List<FriendList> fl2 = new ArrayList<FriendList>();
                    Statement statement = sqldb.createStatement();
                    ResultSet rs = statement.executeQuery(
                            "select * from FriendList where is_relay <> '1' order by TOX_CONNECTION_on_off desc ," +
                            "Notification_silent asc, Last_online_timestamp desc");
                    while (rs.next())
                    {
                        Log.i(TAG, "add_all_friends_clear:rs=" + rs);

                        FriendList temp = new FriendList();
                        temp.tox_public_key_string = rs.getString("tox_public_key_string");
                        temp.name = rs.getString("name");
                        fl2.add(temp);
                    }

                    if (fl2 != null)
                    {
                        Log.i(TAG, "add_all_friends_clear:fl.size=" + fl2.size());
                        if (fl2.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl2.size(); i++)
                            {
                                FriendList n = FriendList.deep_copy(fl2.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = true;
                                cfac.friend_item = n;

                                if ((cfac.friend_item.name == null) || (cfac.friend_item.name.length() < 1))
                                {
                                    friends_and_confs_list_model.addElement(
                                            "" + cfac.friend_item.tox_public_key_string + ":\n");
                                }
                                else
                                {
                                    friends_and_confs_list_model.addElement(
                                            cfac.friend_item.tox_public_key_string + ":\n" + cfac.friend_item.name);
                                }
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add rest of friends  -------------

                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "add_all_friends_clear:EE:" + e.getMessage());
            e.printStackTrace();
        }

        in_update_data = false;
        // Log.i(TAG, "add_all_friends_clear:READY");
    }


    synchronized void modify_friend(final CombinedFriendsAndConferences c, boolean is_friend)
    {
        // Log.i(TAG, "modify_friend");

        if (is_friend)
        {
            final FriendList f = c.friend_item;

            if (f.is_relay == true)
            {
                // do not update anything if this is a relay
                return;
            }


            try
            {
                FriendList f2 = null;
                Statement statement = sqldb.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select * from FriendList where tox_public_key_string == '" + s(f.tox_public_key_string) + "'");
                if (rs.next())
                {
                    f2 = new FriendList();
                    f2.tox_public_key_string = rs.getString("tox_public_key_string");
                    f2.name = rs.getString("name");
                }

                if (f2 != null)
                {
                    FriendList n = deep_copy(f2);
                    CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                    cfac.is_friend = true;
                    cfac.friend_item = n;

                    boolean found_friend = false;
                    Enumeration<String> e = friends_and_confs_list_model.elements();
                    while (e.hasMoreElements())
                    {
                        String full_f_entry_text = e.nextElement();
                        if (full_f_entry_text.substring(0, TOX_PUBLIC_KEY_SIZE * 2).equals(f.tox_public_key_string))
                        {
                            found_friend = true;
                            if ((f.name == null) || (f.name.length() < 1))
                            {
                                friends_and_confs_list_model.set(
                                        friends_and_confs_list_model.indexOf(full_f_entry_text),
                                        "" + f.tox_public_key_string + ":\n");
                            }
                            else
                            {
                                friends_and_confs_list_model.set(
                                        friends_and_confs_list_model.indexOf(full_f_entry_text),
                                        f.tox_public_key_string + ":\n" + f.name);
                            }
                            break;
                        }
                    }

                    if (!found_friend)
                    {
                        if ((f.name == null) || (f.name.length() < 1))
                        {
                            friends_and_confs_list_model.addElement("" + f.tox_public_key_string + ":\n");
                        }
                        else
                        {
                            friends_and_confs_list_model.addElement(f.tox_public_key_string + ":\n" + f.name);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else // is conference -----------------------------
        {
        }
    }

}
