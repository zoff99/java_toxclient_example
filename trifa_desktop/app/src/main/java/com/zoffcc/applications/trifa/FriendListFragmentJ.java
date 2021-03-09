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

import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static com.zoffcc.applications.trifa.ConferenceMessageListFragmentJ.setConfName;
import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.set_message_panel;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.setFriendName;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.update_all_messages;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ONE_HOUR_IN_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class FriendListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.FriendListFrgnt";

    private final JList<CombinedFriendsAndConferences> friends_and_confs_list;
    static DefaultListModel<CombinedFriendsAndConferences> friends_and_confs_list_model;
    JScrollPane FriendScrollPane;

    static Boolean in_update_data = false;
    static final Boolean in_update_data_lock = false;

    public FriendListFragmentJ()
    {
        friends_and_confs_list_model = new DefaultListModel<>();

        friends_and_confs_list = new JList<>();
        friends_and_confs_list.setModel(friends_and_confs_list_model);
        friends_and_confs_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friends_and_confs_list.setSelectedIndex(0);
        friends_and_confs_list.setCellRenderer(new Renderer_FriendsAndConfsList());

        friends_and_confs_list.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                try
                {
                    if (e.getValueIsAdjusting())
                    {
                        return;
                    }

                    //System.out.println("ListSelectionListener:e.getFirstIndex()" + e.getFirstIndex());
                    //System.out.println("ListSelectionListener:friends_and_confs_list.getSelectedIndex()" +
                    //                   friends_and_confs_list.getSelectedIndex());

                    if (friends_and_confs_list.getSelectedIndex() == -1)
                    {
                        return;
                    }

                    if (friends_and_confs_list_model.elementAt(friends_and_confs_list.getSelectedIndex()).is_friend)
                    {

                        String pk = friends_and_confs_list_model.elementAt(
                                friends_and_confs_list.getSelectedIndex()).friend_item.tox_public_key_string;

                        // friends_and_confs_list.clearSelection();

                        if (pk.length() == (TOX_PUBLIC_KEY_SIZE * 2))
                        {

                            set_message_panel(1);

                            MessagePanel.setCurrentPK(pk);
                            MessagePanel.friendnum = tox_friend_by_public_key__wrapper(pk);

                            //System.out.println(
                            //        "ListSelectionListener:setCurrentPK:" + pk + " fnum=" + MessagePanel.friendnum);

                            update_all_messages(true);
                            setFriendName();
                        }
                    }
                    else // --- conferences ---
                    {
                        set_message_panel(2);
                        String conf_id = friends_and_confs_list_model.elementAt(
                                friends_and_confs_list.getSelectedIndex()).conference_item.conference_identifier;
                        MessagePanelConferences.current_conf_id = conf_id;

                        MessagePanelConferences.update_all_messages(true);
                        setConfName();
                    }
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }

        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FriendScrollPane = new JScrollPane();
        add(FriendScrollPane);
        FriendScrollPane.setViewportView(friends_and_confs_list);

        add_all_friends_clear(1);
    }

    synchronized static void add_all_friends_clear(final int delay)
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

                    long time_now = System.currentTimeMillis();

                    // ------------- add friends that were added recently first -------------
                    List<FriendList> fl = orma.selectFromFriendList().
                            is_relayNotEq(true).
                            added_timestampGt(time_now - ONE_HOUR_IN_MS).
                            orderByTOX_CONNECTION_on_offDesc().
                            orderByNotification_silentAsc().
                            orderByLast_online_timestampDesc().
                            toList();

                    if (fl != null)
                    {
                        Log.i(TAG, "add_all_friends_clear:fl.size=" + fl.size());
                        if (fl.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl.size(); i++)
                            {
                                FriendList n = FriendList.deep_copy(fl.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = true;
                                cfac.friend_item = n;
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add friends that were added recently first -------------


                    // ------------- add rest of friends  -------------
                    List<FriendList> fl2 = orma.selectFromFriendList().
                            is_relayNotEq(true).
                            added_timestampLe(time_now - ONE_HOUR_IN_MS).
                            orderByTOX_CONNECTION_on_offDesc().
                            orderByNotification_silentAsc().
                            orderByLast_online_timestampDesc().
                            toList();

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
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add rest of friends  -------------

                    List<ConferenceDB> confs = orma.selectFromConferenceDB().
                            orderByConference_activeDesc().
                            orderByNotification_silentAsc().
                            toList();

                    if (confs != null)
                    {
                        if (confs.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < confs.size(); i++)
                            {
                                ConferenceDB n = ConferenceDB.deep_copy(confs.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = false;
                                cfac.conference_item = n;
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
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
                final FriendList f2 = orma.selectFromFriendList().
                        tox_public_key_stringEq(f.tox_public_key_string).
                        toList().get(0);

                if (f2 != null)
                {
                    FriendList n = deep_copy(f2);
                    CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                    cfac.is_friend = true;
                    cfac.friend_item = n;

                    boolean found_friend = false;

                    found_friend = update_item(cfac, cfac.is_friend);

                    if (!found_friend)
                    {
                        friends_and_confs_list_model.addElement(cfac);
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
            final ConferenceDB cc = c.conference_item;

            try
            {
                // who_invited__tox_public_key_stringEq(cc.who_invited__tox_public_key_string).
                // and().
                final ConferenceDB conf2 = orma.selectFromConferenceDB().
                        conference_identifierEq(cc.conference_identifier).
                        toList().get(0);

                if (conf2 != null)
                {
                    ConferenceDB n = ConferenceDB.deep_copy(conf2);
                    CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                    cfac.is_friend = false;
                    cfac.conference_item = n;
                    boolean found_friend = update_item(cfac, cfac.is_friend);
                    // Log.i(TAG, "modify_friend:found_friend=" + found_friend + " n=" + n);

                    if (!found_friend)
                    {
                        friends_and_confs_list_model.addElement(cfac);
                        // Log.i(TAG, "modify_friend:add_item");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    private boolean update_item(CombinedFriendsAndConferences new_item_combined, boolean is_friend)
    {
        boolean found_item = false;

        Iterator<CombinedFriendsAndConferences> it = friends_and_confs_list_model.elements().asIterator();

        while (it.hasNext())
        {
            CombinedFriendsAndConferences f_combined = (CombinedFriendsAndConferences) it.next();

            if (is_friend)
            {
                if (f_combined.is_friend)
                {
                    FriendList f = f_combined.friend_item;
                    FriendList new_item = new_item_combined.friend_item;
                    if (f.tox_public_key_string.equals(new_item.tox_public_key_string))
                    {
                        found_item = true;
                        int pos = this.friends_and_confs_list_model.indexOf(f_combined);
                        friends_and_confs_list_model.set(pos, new_item_combined);
                        break;
                    }
                }
            }
            else // is conference
            {
                if (!f_combined.is_friend)
                {
                    ConferenceDB f = f_combined.conference_item;
                    ConferenceDB new_item = new_item_combined.conference_item;

                    if (f.conference_identifier.equals(new_item.conference_identifier))
                    {
                        found_item = true;
                        int pos = this.friends_and_confs_list_model.indexOf(f_combined);
                        friends_and_confs_list_model.set(pos, new_item_combined);
                        break;
                    }
                }
            }
        }

        return found_item;
    }
}
