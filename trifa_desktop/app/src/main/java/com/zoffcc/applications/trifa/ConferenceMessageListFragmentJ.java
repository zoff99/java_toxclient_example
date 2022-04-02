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
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.insert_into_conference_message_db;
import static com.zoffcc.applications.trifa.HelperConference.is_conference_active;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.resolve_name_for_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BORDER_TITLE;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_offline_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_offline_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_conference_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class ConferenceMessageListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.CnfMsgListFrgnt";

    private static JList<ConferenceMessage> conf_messagelistitems;
    static DefaultListModel<ConferenceMessage> conf_messagelistitems_model;
    static JScrollPane Conf_MessageScrollPane = null;

    static String current_conf_id = "-1";
    static boolean is_at_bottom = true;
    static boolean peerlist_in_update = false;

    public ConferenceMessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");

        current_conf_id = "-1";

        conf_messagelistitems_model = new DefaultListModel<>();

        conf_messagelistitems = new JList<>();
        conf_messagelistitems.setModel(conf_messagelistitems_model);
        conf_messagelistitems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        conf_messagelistitems.setSelectedIndex(0);
        conf_messagelistitems.setSelectionModel(new DisabledItemSelectionModel());
        conf_messagelistitems.setCellRenderer(new Renderer_ConfMessageList());
        conf_messagelistitems.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = conf_messagelistitems.locationToIndex(point);
                if (index != -1)
                {
                    // Next calculations assume that text is aligned to left, but are easy to adjust
                    final ConferenceMessage element = conf_messagelistitems.getModel().getElementAt(index);
                    final Rectangle cellBounds = conf_messagelistitems.getCellBounds(index, index);
                    final Renderer_ConfMessageList renderer = (Renderer_ConfMessageList) conf_messagelistitems.getCellRenderer();
                    final Insets insets = renderer.getInsets();

                    // Ensure that mouse press happened within top/bottom insets
                    if (cellBounds.y + insets.top <= point.y &&
                        point.y <= cellBounds.y + cellBounds.height - insets.bottom)
                    {


                        if (SwingUtilities.isLeftMouseButton(e))
                        {
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                                    new StringSelection(element.text), null);
                            Toast.makeToast(MainFrame, lo.getString("copied_msg_to_clipboard"), 800);
                        }
                        else
                        {
                            Log.i(TAG, "popup dialog");
                            textAreaDialog(null, element.text, "Message");
                        }
                    }
                    else
                    {
                    }
                }
                else
                {
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        Conf_MessageScrollPane = new JScrollPane(conf_messagelistitems);
        add(Conf_MessageScrollPane);

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

        revalidate();
    }

    static synchronized public void send_message_onclick(final String msg2)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";

        try
        {
            if (is_conference_active(current_conf_id))
            {
                // send typed message to friend
                msg = msg2.substring(0, (int) Math.min(tox_max_message_length(), msg2.length()));

                try
                {
                    ConferenceMessage m = new ConferenceMessage();
                    m.is_new = false; // own messages are always "not new"
                    m.tox_peerpubkey = global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2));
                    m.direction = 1; // msg sent
                    m.TOX_MESSAGE_TYPE = 0;
                    m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
                    m.tox_peername = null;
                    m.conference_identifier = current_conf_id;
                    m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                    m.sent_timestamp = System.currentTimeMillis();
                    m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
                    m.text = msg;
                    m.was_synced = false;

                    if ((msg != null) && (!msg.equalsIgnoreCase("")))
                    {
                        int res = tox_conference_send_message(tox_conference_by_confid__wrapper(current_conf_id), 0,
                                                              msg);
                        // Log.i(TAG, "tox_conference_send_message:result=" + res + " m=" + m);
                        if (PREF__X_battery_saving_mode)
                        {
                            Log.i(TAG, "global_last_activity_for_battery_savings_ts:001:*PING*");
                        }
                        global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

                        if (res > -1)
                        {
                            // message was sent OK
                            Runnable myRunnable = () -> {
                                try
                                {
                                    messageInputTextField.setText("");
                                }
                                catch (Exception e)
                                {
                                }
                            };
                            SwingUtilities.invokeLater(myRunnable);

                            insert_into_conference_message_db(m, true);
                        }
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

    String get_current_conf_id()
    {
        return current_conf_id;
    }

    class DisabledItemSelectionModel extends DefaultListSelectionModel
    {
        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            super.setSelectionInterval(-1, -1);
        }
    }

    void update_all_messages(boolean always)
    {
        // Log.i(TAG, "update_all_messages:conf");

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                orma.updateConferenceMessage().
                        conference_identifierEq(current_conf_id).
                        is_new(false).
                        execute();
            }
            // reset "new" flags for messages -------
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if (always)
            {
                conf_messagelistitems_model.removeAllElements();
                // -------------------------------------------------
                // HINT: this one does not respect ordering?!
                // -------------------------------------------------
                try
                {
                    List<ConferenceMessage> ml = orma.selectFromConferenceMessage().
                            conference_identifierEq(current_conf_id).
                            tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                            orderBySent_timestampAsc().
                            toList();

                    for (ConferenceMessage message : ml)
                    {
                        if (message == ml.get(ml.size() - 1))
                        {
                            add_message(message, true);
                        }
                        else
                        {
                            add_message(message, false);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                Log.i(TAG, "data_values:005c");
            }
            Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

    void add_message(final ConferenceMessage m, final boolean no_block)
    {
        Runnable myRunnable = () -> {
            try
            {
                add_item(m, no_block);
                if ((is_at_bottom) && (!no_block))
                {
                    // Log.i(TAG, "scroll:" + MessageScrollPane.getVerticalScrollBar().getValue());
                    // Log.i(TAG, "scroll:max:" + MessageScrollPane.getVerticalScrollBar().getMaximum());
                    EventQueue.invokeLater(() -> {
                        Conf_MessageScrollPane.getVerticalScrollBar().setValue(
                                Conf_MessageScrollPane.getVerticalScrollBar().getMaximum());
                    });
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_message:conf:EE1:" + e.getMessage());
                e.printStackTrace();
            }
        };
        SwingUtilities.invokeLater(myRunnable);
    }

    public static void add_item(ConferenceMessage new_item, final boolean no_block)
    {
        conf_messagelistitems_model.addElement(new_item);
        if (!no_block)
        {
            MessagePanelConferences.revalidate();
        }
    }

    static void setConfName()
    {
        try
        {
            if (MessagePanelConferences != null)
            {
                EventQueue.invokeLater(() -> {
                    try
                    {
                        MessagePanelConferences.setBorder(
                                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 (get_conference_title_from_confid(current_conf_id) +
                                                                  " " + current_conf_id.substring(0, 5))));
                        ((TitledBorder) MessagePanelConferences.getBorder()).setTitleFont(
                                new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

                        MessagePanelConferences.revalidate();
                        MessagePanelConferences.repaint();
                    }
                    catch (Exception e)
                    {
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String textAreaDialog(Object obj, String text, String title)
    {
        if (title == null)
        {
            title = "Your input";
        }
        JTextArea textArea = new JTextArea(text);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(textArea.getPreferredSize().width, textArea.getPreferredSize().height);
        int ret = JOptionPane.showConfirmDialog((Component) obj, new JScrollPane(textArea), title,
                                                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ret == 0)
        {
            return textArea.getText();
        }
        else
        {
        }
        return null;
    }

    static void update_group_all_users()
    {
        if (!peerlist_in_update)
        {
            peerlist_in_update = true;

            EventQueue.invokeLater(() -> {
                try
                {
                    set_peer_count_header();
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }

                try
                {
                    set_peer_names_and_avatars();
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                }

                peerlist_in_update = false;
            });
        }
    }

    static void set_peer_count_header()
    {
        if (current_conf_id.equals("-1"))
        {
            return;
        }

        final String f_name = HelperConference.get_conference_title_from_confid(current_conf_id);
        final long conference_num = tox_conference_by_confid__wrapper(current_conf_id);
        // Log.i(TAG, "set_peer_count_header:1:conf_id=" + current_conf_id + " conference_num=" + conference_num);
        EventQueue.invokeLater(() -> {
            try
            {
                long peer_count = tox_conference_peer_count(conference_num);
                long frozen_peer_count = tox_conference_offline_peer_count(conference_num);
                // Log.i(TAG, "set_peer_count_header:2:conf_id=" + conf_id + " conference_num=" + conference_num);

                if (peer_count > -1)
                {
                    // ml_maintext.setText(
                    //         f_name + "\n" + getString(R.string.GroupActivityActive) + " " + peer_count +
                    //         " " + getString(R.string.GroupActivityOffline) + " " + frozen_peer_count);
                }
                else
                {
                    // ml_maintext.setText(f_name);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    static void set_peer_names_and_avatars()
    {
        if (is_conference_active(current_conf_id))
        {
            // Log.d(TAG, "set_peer_names_and_avatars:001");

            try
            {
                remove_group_all_users();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Log.d(TAG, "set_peer_names_and_avatars:002");

            final long conference_num = tox_conference_by_confid__wrapper(current_conf_id);
            long num_peers = tox_conference_peer_count(conference_num);
            long offline_num_peers = tox_conference_offline_peer_count(conference_num);

            // Log.d(TAG, "set_peer_names_and_avatars:003:peer count=" + num_peers);

            if (num_peers > 0)
            {
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_conference_peer_get_public_key(conference_num, i);
                        String peer_name_temp = tox_conference_peer_get_name(conference_num, i);
                        if (peer_name_temp.equals(""))
                        {
                            peer_name_temp = null;
                        }
                        // Log.d(TAG, "set_peer_names_and_avatars:004:add:" + peer_name_temp);
                        add_group_user(peer_pubkey_temp, i, peer_name_temp, false);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }

            if (offline_num_peers > 0)
            {
                long i = 0;
                for (i = 0; i < offline_num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_conference_offline_peer_get_public_key(conference_num, i);
                        String peer_name_temp = tox_conference_offline_peer_get_name(conference_num, i);
                        if (peer_name_temp.equals(""))
                        {
                            peer_name_temp = null;
                        }
                        // Log.d(TAG, "set_peer_names_and_avatars:005:add:" + peer_name_temp);
                        add_group_user(peer_pubkey_temp, i, peer_name_temp, true);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
    }

    static void remove_group_all_users()
    {
        PeerListFragmentJ.peer_list_model.clear();
    }

    static long peer_pubkey_to_long_in_list(String peer_pubkey)
    {
        long ret = -1L;

        if (lookup_peer_listnum_pubkey.containsKey(peer_pubkey))
        {
            ret = lookup_peer_listnum_pubkey.get(peer_pubkey);
        }

        return ret;
    }

    static void add_group_user(final String peer_pubkey, final long peernum, String name, boolean offline)
    {
        // Log.i(TAG, "add_group_user:peernum=" + peernum + " name=" + name + " offline=" + offline);

        try
        {
            if (peernum != -1)
            {
                // -- ADD --
                // Log.i(TAG, "add_group_user:ADD:peernum=" + peernum);
                String name2 = "";
                if (name != null)
                {
                    name2 = name;
                }
                else
                {
                    name2 = peer_pubkey.substring(peer_pubkey.length() - 5, peer_pubkey.length());
                }

                try
                {
                    name2 = resolve_name_for_pubkey(peer_pubkey, name2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                final String name3 = name2;

                PeerModel pm = new PeerModel();
                pm.offline = offline;
                pm.name = name3;
                pm.peernum = peernum;
                pm.pubkey = peer_pubkey;
                PeerListFragmentJ.peer_list_model.addElement(pm);
            }
            else
            {
                Log.i(TAG, "add_group_user:EE999:!!please report this!!");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "add_group_user:EE:" + e.getMessage());
        }
    }
}