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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_FRIEND;
import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_GROUP;
import static com.zoffcc.applications.trifa.ConferenceMessageListFragmentJ.setConfName;
import static com.zoffcc.applications.trifa.FriendList.deep_copy;
import static com.zoffcc.applications.trifa.GroupMessageListFragmentJ.setGroupName;
import static com.zoffcc.applications.trifa.HelperConference.delete_conference;
import static com.zoffcc.applications.trifa.HelperConference.delete_conference_all_messages;
import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.set_conference_inactive;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_files;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_filetransfers;
import static com.zoffcc.applications.trifa.HelperFriend.delete_friend_all_messages;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.update_savedata_file_wrapper;
import static com.zoffcc.applications.trifa.HelperGroup.delete_group;
import static com.zoffcc.applications.trifa.HelperGroup.delete_group_all_messages;
import static com.zoffcc.applications.trifa.HelperGroup.get_group_title_from_groupid;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.HelperRelay.have_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.invite_to_all_conferences_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.invite_to_all_groups_own_relay;
import static com.zoffcc.applications.trifa.HelperRelay.send_all_friend_pubkeys_to_relay;
import static com.zoffcc.applications.trifa.HelperRelay.send_relay_pubkey_to_all_friends;
import static com.zoffcc.applications.trifa.HelperRelay.set_friend_as_own_relay_in_db;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelGroups;
import static com.zoffcc.applications.trifa.MainActivity.cache_confid_confnum;
import static com.zoffcc.applications.trifa.MainActivity.cache_fnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.cache_pubkey_fnum;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.set_message_panel;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_delete;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_delete;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_leave;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.current_pk;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.setFriendName;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.update_all_messages;
import static com.zoffcc.applications.trifa.TRIFAGlobals.CONFERENCE_NAME_DISPLAY_MENU_MAXLEN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.FRIEND_NAME_DISPLAY_MENU_MAXLEN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ONE_HOUR_IN_MS;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class FriendListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.FriendListFrgnt";

    private static JList<CombinedFriendsAndConferences> friends_and_confs_list;
    static DefaultListModel<CombinedFriendsAndConferences> friends_and_confs_list_model;
    JScrollPane FriendScrollPane;
    static JPopupMenu popup_friends;
    static JPopupMenu popup_confs;
    static JPopupMenu popup_groups;

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

        popup_friends = new JPopupMenu();
        popup_confs = new JPopupMenu();
        popup_groups = new JPopupMenu();

        Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0, popup_friends.getForeground());
        TitledBorder labelBorder = BorderFactory.createTitledBorder(titleUnderline, "...", TitledBorder.CENTER,
                                                                    TitledBorder.ABOVE_TOP, popup_friends.getFont(),
                                                                    popup_friends.getForeground());
        popup_friends.setBorder(labelBorder);
        popup_confs.setBorder(labelBorder);
        popup_groups.setBorder(labelBorder);

        JMenuItem menuItem_friend_info = new JMenuItem(lo.getString("info_friend"));
        menuItem_friend_info.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final JMenuItem mitem = (JMenuItem) ev.getSource();
                final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                final JList<CombinedFriendsAndConferences> b = (JList<CombinedFriendsAndConferences>) a;
                final String f2_tox_public_key_string = b.getSelectedValue().friend_item.tox_public_key_string;

                FriendInfoActivity FriendInfoFrame = new FriendInfoActivity(f2_tox_public_key_string);
                FriendInfoFrame.setVisible(true);
            }
        });
        popup_friends.add(menuItem_friend_info);

        JMenuItem menuItem = new JMenuItem(lo.getString("delete_friend"));
        menuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final JMenuItem mitem = (JMenuItem) ev.getSource();
                final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                final JList<CombinedFriendsAndConferences> b = (JList<CombinedFriendsAndConferences>) a;
                Log.i(TAG, "delete friend:name=" + b.getSelectedValue().friend_item.name);
                Log.i(TAG,
                      "delete friend:tox_public_key_string=" + b.getSelectedValue().friend_item.tox_public_key_string);

                final String f2_tox_public_key_string = b.getSelectedValue().friend_item.tox_public_key_string;

                int selected_answer = JOptionPane.showConfirmDialog(mitem, lo.getString("delete_friend_msg"),
                                                                    lo.getString("delete_friend_title"), YES_NO_OPTION);
                if (selected_answer == YES_OPTION)
                {
                    try
                    {
                        Log.i(TAG, "do delete friend:tox_public_key_string=" +
                                   b.getSelectedValue().friend_item.tox_public_key_string);

                        Runnable myRunnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    long friend_num_temp = tox_friend_by_public_key__wrapper(f2_tox_public_key_string);

                                    Log.i(TAG,
                                          "onMenuItemClick:1:fn=" + friend_num_temp + " fn_safety=" + friend_num_temp);

                                    // delete friends files -------
                                    Log.i(TAG, "onMenuItemClick:1.c:fnum=" + friend_num_temp);
                                    delete_friend_all_files(friend_num_temp);
                                    // delete friend  files -------

                                    // delete friends FTs -------
                                    Log.i(TAG, "onMenuItemClick:1.d:fnum=" + friend_num_temp);
                                    delete_friend_all_filetransfers(friend_num_temp);
                                    // delete friend  FTs -------

                                    // delete friends messages -------
                                    Log.i(TAG, "onMenuItemClick:1.b:fnum=" + friend_num_temp);
                                    delete_friend_all_messages(friend_num_temp);
                                    // delete friend  messages -------

                                    // delete friend -------
                                    // Log.i(TAG, "onMenuItemClick:1.a:pubkey=" + f2.tox_public_key_string);
                                    delete_friend(f2_tox_public_key_string);
                                    // delete friend -------

                                    // delete friend - tox ----
                                    Log.i(TAG, "onMenuItemClick:4");
                                    if (friend_num_temp > -1)
                                    {
                                        int res = tox_friend_delete(friend_num_temp);
                                        cache_pubkey_fnum.clear();
                                        cache_fnum_pubkey.clear();
                                        update_savedata_file_wrapper(); // save toxcore datafile (friend removed)
                                        Log.i(TAG, "onMenuItemClick:5:res=" + res);
                                    }
                                    // delete friend - tox ----

                                    // load all friends into data list ---
                                    Log.i(TAG, "onMenuItemClick:6");
                                    try
                                    {
                                        // reload friendlist
                                        add_all_friends_clear(200);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                    Log.i(TAG, "onMenuItemClick:7");
                                    // load all friends into data list ---
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "onMenuItemClick:8:EE:" + e.getMessage());
                                }
                            }
                        };

                        SwingUtilities.invokeLater(myRunnable);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        });
        popup_friends.add(menuItem);

        JMenuItem menuItem_del_conf = new JMenuItem(lo.getString("delete_conference"));
        menuItem_del_conf.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final JMenuItem mitem = (JMenuItem) ev.getSource();
                final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                final JList<CombinedFriendsAndConferences> b = (JList<CombinedFriendsAndConferences>) a;

                final String c2_conf_id_string = b.getSelectedValue().conference_item.conference_identifier;
                final ConferenceDB c2_conf = b.getSelectedValue().conference_item;

                int selected_answer = JOptionPane.showConfirmDialog(mitem, lo.getString("delete_conference_msg"),
                                                                    lo.getString("delete_conference_title"),
                                                                    YES_NO_OPTION);
                if (selected_answer == YES_OPTION)
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
                                    if ((c2_conf.tox_conference_number > -1) && (c2_conf.conference_active))
                                    {
                                        tox_conference_delete(c2_conf.tox_conference_number);
                                        cache_confid_confnum.clear();
                                        update_savedata_file_wrapper(); // after deleteing a conference
                                    }

                                    Log.i(TAG, "onMenuItemClick:info:33");
                                    delete_conference_all_messages(c2_conf_id_string);
                                    delete_conference(c2_conf_id_string);
                                    Log.i(TAG, "onMenuItemClick:info:34");

                                    set_conference_inactive(c2_conf_id_string);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "onMenuItemClick:9:EE:" + e.getMessage());
                                }

                                try
                                {
                                    // reload friendlist
                                    add_all_friends_clear(200);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                        SwingUtilities.invokeLater(myRunnable);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        });
        popup_confs.add(menuItem_del_conf);

        JMenuItem menuItem_del_group = new JMenuItem(lo.getString("delete_group"));
        menuItem_del_group.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final JMenuItem mitem = (JMenuItem) ev.getSource();
                final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                final JList<CombinedFriendsAndConferences> b = (JList<CombinedFriendsAndConferences>) a;

                final String c2_conf_id_string = b.getSelectedValue().group_item.group_identifier;
                final GroupDB c2_conf = b.getSelectedValue().group_item;

                int selected_answer = JOptionPane.showConfirmDialog(mitem, lo.getString("delete_group_msg"),
                                                                    lo.getString("delete_group_title"), YES_NO_OPTION);
                if (selected_answer == YES_OPTION)
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
                                    if (c2_conf.group_identifier != null)
                                    {
                                        final long group_num = tox_group_by_groupid__wrapper(c2_conf.group_identifier);
                                        tox_group_leave(group_num, "bye");
                                        update_savedata_file_wrapper(); // after deleteing a conference
                                    }

                                    Log.i(TAG, "onMenuItemClick:info:33");
                                    delete_group_all_messages(c2_conf.group_identifier);
                                    delete_group(c2_conf.group_identifier);
                                    Log.i(TAG, "onMenuItemClick:info:34");
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    Log.i(TAG, "onMenuItemClick:9:EE:" + e.getMessage());
                                }

                                try
                                {
                                    // reload friendlist
                                    add_all_friends_clear(200);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        };

                        SwingUtilities.invokeLater(myRunnable);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        });
        popup_groups.add(menuItem_del_group);

        JMenuItem menuItem_alias_name = new JMenuItem(lo.getString("set_alias_name_friend"));
        menuItem_alias_name.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final JMenuItem mitem = (JMenuItem) ev.getSource();
                final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                final CombinedFriendsAndConferences c_fac = ((JList<CombinedFriendsAndConferences>) a).getSelectedValue();

                try
                {
                    ComplexDialogPanel set_aliasname_panel = new ComplexDialogPanel(
                            c_fac.friend_item.tox_public_key_string);

                    int optionType = JOptionPane.DEFAULT_OPTION;
                    int messageType = JOptionPane.PLAIN_MESSAGE;
                    Icon icon = null;
                    String[] options = {"Ok", "Cancel"};
                    Object initialValue = options[0];
                    int reply = JOptionPane.showOptionDialog(null, set_aliasname_panel,
                                                             lo.getString("set_alias_name_friend"), optionType,
                                                             messageType, icon, options, initialValue);
                    if (reply == YES_OPTION)
                    {
                        try
                        {
                            if (c_fac.friend_item.tox_public_key_string.length() > 1)
                            {
                                String new_alias_name = set_aliasname_panel.getText("panel_" + "new_alias_name");
                                if ((new_alias_name != null) && (new_alias_name.length() > 0))
                                {
                                    // set new aliaa name
                                    orma.updateFriendList().
                                            tox_public_key_stringEq(c_fac.friend_item.tox_public_key_string).
                                            alias_name(new_alias_name).execute();
                                }
                                else
                                {
                                    // remove alias name
                                    orma.updateFriendList().
                                            tox_public_key_stringEq(c_fac.friend_item.tox_public_key_string).
                                            alias_name("").execute();
                                }

                                EventQueue.invokeLater(() -> {
                                    MessagePanel.setFriendName();
                                    MessagePanel.revalidate();
                                });
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        popup_friends.add(menuItem_alias_name);

        if (!have_own_relay())
        {
            JMenuItem menuItem_as_relay = new JMenuItem(lo.getString("friend_as_relay"));
            menuItem_as_relay.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    if (!have_own_relay())
                    {
                        final JMenuItem mitem = (JMenuItem) ev.getSource();
                        final Component a = ((JPopupMenu) mitem.getParent()).getInvoker();
                        final JList<CombinedFriendsAndConferences> b = (JList<CombinedFriendsAndConferences>) a;
                        Log.i(TAG, "add_as_relay:tox_public_key_string=" +
                                   b.getSelectedValue().friend_item.tox_public_key_string);

                        final String f2_tox_public_key_string = b.getSelectedValue().friend_item.tox_public_key_string;

                        int selected_answer = JOptionPane.showConfirmDialog(mitem, lo.getString("add_as_relay_msg"),
                                                                            lo.getString("add_as_relay_title"),
                                                                            YES_NO_OPTION);
                        if (selected_answer == YES_OPTION)
                        {
                            try
                            {
                                if (set_friend_as_own_relay_in_db(f2_tox_public_key_string))
                                {
                                    // load all friends into data list ---
                                    Log.i(TAG, "onMenuItemClick:6");
                                    try
                                    {
                                        // reload friendlist
                                        add_all_friends_clear(200);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                    Log.i(TAG, "onMenuItemClick:7");
                                    // load all friends into data list ---
                                }

                                send_all_friend_pubkeys_to_relay(f2_tox_public_key_string);
                                send_relay_pubkey_to_all_friends(f2_tox_public_key_string);
                                invite_to_all_conferences_own_relay(f2_tox_public_key_string);
                                invite_to_all_groups_own_relay(f2_tox_public_key_string);
                            }
                            catch (Exception e3)
                            {
                                e3.printStackTrace();
                            }
                        }
                    }
                }
            });
            popup_friends.add(menuItem_as_relay);
        }

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

                    if (friends_and_confs_list_model.elementAt(friends_and_confs_list.getSelectedIndex()).is_friend ==
                        COMBINED_IS_FRIEND)
                    {

                        String pk = friends_and_confs_list_model.elementAt(
                                friends_and_confs_list.getSelectedIndex()).friend_item.tox_public_key_string;

                        if (pk.length() == (TOX_PUBLIC_KEY_SIZE * 2))
                        {
                            if ((current_pk != null) && (pk.compareTo(current_pk) == 0))
                            {
                                // this friend is already selected
                                // Log.i(TAG, "this friend is already selected");
                                return;
                            }

                            set_message_panel(1);

                            MessagePanel.setCurrentPK(pk);
                            MessagePanel.friendnum = tox_friend_by_public_key__wrapper(pk);

                            //System.out.println(
                            //        "ListSelectionListener:setCurrentPK:" + pk + " fnum=" + MessagePanel.friendnum);

                            update_all_messages(true, true);
                            setFriendName();
                        }
                    }
                    else if (friends_and_confs_list_model.elementAt(
                            friends_and_confs_list.getSelectedIndex()).is_friend == COMBINED_IS_GROUP)
                    {
                        set_message_panel(3);
                        String group_id = friends_and_confs_list_model.elementAt(
                                friends_and_confs_list.getSelectedIndex()).group_item.group_identifier;
                        MessagePanelGroups.current_group_id = group_id;

                        MessagePanelGroups.reset_paging();
                        MessagePanelGroups.update_all_messages(true, true);
                        setGroupName();
                    }
                    else // --- conferences ---
                    {
                        set_message_panel(2);
                        String conf_id = friends_and_confs_list_model.elementAt(
                                friends_and_confs_list.getSelectedIndex()).conference_item.conference_identifier;
                        MessagePanelConferences.current_conf_id = conf_id;

                        MessagePanelConferences.reset_paging();
                        MessagePanelConferences.update_all_messages(true, true);
                        setConfName();
                    }
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }

        });

        friends_and_confs_list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = friends_and_confs_list.locationToIndex(point);
                // Log.i(TAG, "mousePressed");
                if (index != -1)
                {
                    show_popup_menu(e);
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = friends_and_confs_list.locationToIndex(point);
                // Log.i(TAG, "mouseReleased");
                if (index != -1)
                {
                    show_popup_menu(e);
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FriendScrollPane = new JScrollPane();
        add(FriendScrollPane);
        FriendScrollPane.setViewportView(friends_and_confs_list);

        add_all_friends_clear(1);
    }

    public void show_popup_menu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            final int index = friends_and_confs_list.locationToIndex(e.getPoint());

            if (friends_and_confs_list_model.getElementAt(index).is_friend == COMBINED_IS_CONFERENCE)
            // ---------- conference ----------
            {
                EventQueue.invokeLater(() -> {
                    TitledBorder labelBorder = null;
                    try
                    {
                        final String name = get_conference_title_from_confid(friends_and_confs_list_model.getElementAt(
                                index).conference_item.conference_identifier) + " " +
                                            friends_and_confs_list_model.getElementAt(
                                                    index).conference_item.conference_identifier.substring(0, 5);
                        String name_shortened = name;
                        if ((name == null) || (name.length() == 0))
                        {
                            name_shortened = "...";
                        }
                        else if (name.length() > CONFERENCE_NAME_DISPLAY_MENU_MAXLEN)
                        {
                            name_shortened = name.substring(0, CONFERENCE_NAME_DISPLAY_MENU_MAXLEN);
                        }
                        Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0,
                                                                                popup_confs.getForeground());
                        labelBorder = BorderFactory.createTitledBorder(titleUnderline, name_shortened,
                                                                       TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
                                                                       popup_confs.getFont(),
                                                                       popup_confs.getForeground());
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                    friends_and_confs_list.setSelectedIndex(index);
                    popup_confs.setBorder(labelBorder);
                    popup_confs.show(friends_and_confs_list, e.getX(), e.getY());
                    popup_confs.revalidate();
                    popup_confs.getParent().revalidate();
                    popup_confs.repaint();
                });
            }
            else if (friends_and_confs_list_model.getElementAt(index).is_friend == COMBINED_IS_GROUP)
            // ---------- conference ----------
            {
                EventQueue.invokeLater(() -> {
                    TitledBorder labelBorder = null;
                    try
                    {
                        final String name = get_group_title_from_groupid(
                                friends_and_confs_list_model.getElementAt(index).group_item.group_identifier) + " " +
                                            friends_and_confs_list_model.getElementAt(
                                                    index).group_item.group_identifier.substring(0, 5);
                        String name_shortened = name;
                        if ((name == null) || (name.length() == 0))
                        {
                            name_shortened = "...";
                        }
                        else if (name.length() > CONFERENCE_NAME_DISPLAY_MENU_MAXLEN)
                        {
                            name_shortened = name.substring(0, CONFERENCE_NAME_DISPLAY_MENU_MAXLEN);
                        }
                        Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0,
                                                                                popup_groups.getForeground());
                        labelBorder = BorderFactory.createTitledBorder(titleUnderline, name_shortened,
                                                                       TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
                                                                       popup_groups.getFont(),
                                                                       popup_groups.getForeground());
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                    friends_and_confs_list.setSelectedIndex(index);
                    popup_groups.setBorder(labelBorder);
                    popup_groups.show(friends_and_confs_list, e.getX(), e.getY());
                    popup_groups.revalidate();
                    popup_groups.getParent().revalidate();
                    popup_groups.repaint();
                });
            }
            else if (friends_and_confs_list_model.getElementAt(index).is_friend == COMBINED_IS_FRIEND)
            // ---------- friend ----------
            {
                EventQueue.invokeLater(() -> {
                    TitledBorder labelBorder = null;
                    try
                    {
                        final String name = get_friend_name_from_pubkey(
                                friends_and_confs_list_model.getElementAt(index).friend_item.tox_public_key_string);
                        String name_shortened = name;
                        if ((name == null) || (name.length() == 0))
                        {
                            name_shortened = "...";
                        }
                        else if (name.length() > FRIEND_NAME_DISPLAY_MENU_MAXLEN)
                        {
                            name_shortened = name.substring(0, FRIEND_NAME_DISPLAY_MENU_MAXLEN);
                        }
                        Border titleUnderline = BorderFactory.createMatteBorder(1, 0, 0, 0,
                                                                                popup_friends.getForeground());
                        labelBorder = BorderFactory.createTitledBorder(titleUnderline, name_shortened,
                                                                       TitledBorder.CENTER, TitledBorder.ABOVE_TOP,
                                                                       popup_friends.getFont(),
                                                                       popup_friends.getForeground());
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                    friends_and_confs_list.setSelectedIndex(index);
                    popup_friends.setBorder(labelBorder);
                    popup_friends.show(friends_and_confs_list, e.getX(), e.getY());
                    popup_friends.revalidate();
                    popup_friends.getParent().revalidate();
                    popup_friends.repaint();
                });
            }
        }
    }

    synchronized static void fix_selected_item()
    {
        try
        {
            if (current_pk != null)
            {
                friends_and_confs_list.clearSelection();
                int pos = find_friend_pubkey_in_friend_list(current_pk);
                // Log.i(TAG, "fix_selected_item=" + pos);
                friends_and_confs_list.setSelectedIndex(pos);
            }
        }
        catch (Exception e)
        {
        }
    }

    static void add_all_friends_clear(final int delay)
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
                        // Log.i(TAG, "add_all_friends_clear:fl.size=" + fl.size());
                        if (fl.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl.size(); i++)
                            {
                                FriendList n = FriendList.deep_copy(fl.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = COMBINED_IS_FRIEND;
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
                        // Log.i(TAG, "add_all_friends_clear:fl.size=" + fl2.size());
                        if (fl2.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < fl2.size(); i++)
                            {
                                FriendList n = FriendList.deep_copy(fl2.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = COMBINED_IS_FRIEND;
                                cfac.friend_item = n;
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add rest of friends  -------------

                    // ------------- add conferences -------------
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
                                cfac.is_friend = COMBINED_IS_CONFERENCE;
                                cfac.conference_item = n;
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add conferences -------------

                    // ------------- add groups -------------
                    List<GroupDB> groups = orma.selectFromGroupDB().
                            orderByNotification_silentAsc().
                            toList();

                    if (groups != null)
                    {
                        if (groups.size() > 0)
                        {
                            int i = 0;
                            for (i = 0; i < groups.size(); i++)
                            {
                                GroupDB n = GroupDB.deep_copy(groups.get(i));
                                CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                                cfac.is_friend = COMBINED_IS_GROUP;
                                cfac.group_item = n;
                                friends_and_confs_list_model.addElement(cfac);
                                // Log.i(TAG, "add_all_friends_clear:add:" + n);
                            }
                        }
                    }
                    // ------------- add groups -------------

                }
            }

            fix_selected_item();
        }
        catch (Exception e)
        {
            Log.i(TAG, "add_all_friends_clear:EE:" + e.getMessage());
            e.printStackTrace();
        }

        in_update_data = false;
        // Log.i(TAG, "add_all_friends_clear:READY");
    }

    void modify_friend(final CombinedFriendsAndConferences c, int is_friend)
    {
        // Log.i(TAG, "modify_friend");

        if (is_friend == COMBINED_IS_FRIEND)
        {
            final FriendList f = c.friend_item;

            if (f == null)
            {
                Log.i(TAG, "modify_friend:EE02:" + f + " FRIEND is NULL, this should not happen!!");
                return;
            }

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
                    cfac.is_friend = COMBINED_IS_FRIEND;
                    cfac.friend_item = n;

                    boolean found_friend = update_item(cfac, cfac.is_friend);

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
        else if (is_friend == COMBINED_IS_GROUP)
        {
            final GroupDB cc = c.group_item;

            try
            {
                // who_invited__tox_public_key_stringEq(cc.who_invited__tox_public_key_string).
                // and().
                final GroupDB conf2 = orma.selectFromGroupDB().
                        group_identifierEq(cc.group_identifier.toLowerCase()).
                        toList().get(0);

                if (conf2 != null)
                {
                    GroupDB n = GroupDB.deep_copy(conf2);
                    CombinedFriendsAndConferences cfac = new CombinedFriendsAndConferences();
                    cfac.is_friend = COMBINED_IS_GROUP;
                    cfac.group_item = n;
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
                    cfac.is_friend = COMBINED_IS_CONFERENCE;
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

    private static int find_friend_pubkey_in_friend_list(String pubkey)
    {
        int ret = -1;

        Iterator<CombinedFriendsAndConferences> it = friends_and_confs_list_model.elements().asIterator();
        while (it.hasNext())
        {
            CombinedFriendsAndConferences f_combined = (CombinedFriendsAndConferences) it.next();
            if (f_combined.is_friend == COMBINED_IS_FRIEND)
            {
                FriendList f = f_combined.friend_item;
                if (f.tox_public_key_string.equals(pubkey))
                {
                    ret = friends_and_confs_list_model.indexOf(f_combined);
                    break;
                }
            }
        }

        return ret;
    }

    private boolean update_item(CombinedFriendsAndConferences new_item_combined, int is_friend)
    {
        boolean found_item = false;

        Iterator<CombinedFriendsAndConferences> it = friends_and_confs_list_model.elements().asIterator();

        while (it.hasNext())
        {
            CombinedFriendsAndConferences f_combined = (CombinedFriendsAndConferences) it.next();

            if (is_friend == COMBINED_IS_FRIEND)
            {
                if (f_combined.is_friend == COMBINED_IS_FRIEND)
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
            else if (is_friend == COMBINED_IS_GROUP)
            {
                if (f_combined.is_friend == COMBINED_IS_GROUP)
                {
                    GroupDB f = f_combined.group_item;
                    GroupDB new_item = new_item_combined.group_item;

                    if (f.group_identifier.equals(new_item.group_identifier))
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
                if (f_combined.is_friend == COMBINED_IS_CONFERENCE)
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

    public class ComplexDialogPanel extends JPanel
    {
        public final String[] LABEL_TEXTS = {"new_alias_name"};
        public static final int COLS = 8;
        private Map<String, JTextField> labelFieldMap = new HashMap<>();

        public ComplexDialogPanel(String friend_pubkey)
        {
            setLayout(new GridBagLayout());
            for (int i = 0; i < LABEL_TEXTS.length; i++)
            {
                String labelTxt = lo.getString("panel_" + LABEL_TEXTS[i]);
                add(new JLabel(labelTxt), createGbc(0, i));

                JTextField textField = new JTextField(COLS);
                textField.setText(get_friend_name_from_pubkey(friend_pubkey));
                labelFieldMap.put("panel_" + LABEL_TEXTS[i], textField);
                add(textField, createGbc(1, i));
            }

            setBorder(BorderFactory.createTitledBorder(get_friend_name_from_pubkey(friend_pubkey)));
        }

        public String getText(String labelText)
        {
            JTextField textField = labelFieldMap.get(labelText);
            if (textField != null)
            {
                return textField.getText();
            }
            else
            {
                throw new IllegalArgumentException(labelText);
            }
        }

        public GridBagConstraints createGbc(int x, int y)
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = x;
            gbc.gridy = y;
            gbc.weightx = 1.0;
            gbc.weighty = gbc.weightx;
            if (x == 0)
            {
                gbc.anchor = GridBagConstraints.LINE_START;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.insets = new Insets(3, 3, 3, 8);
            }
            else
            {
                gbc.anchor = GridBagConstraints.LINE_END;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(3, 3, 3, 3);
            }
            return gbc;
        }
    }
}
