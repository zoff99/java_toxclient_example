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

import java.awt.EventQueue;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.insert_into_conference_message_db;
import static com.zoffcc.applications.trifa.HelperConference.is_conference_active;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.sendTextField;
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

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        Conf_MessageScrollPane = new JScrollPane(conf_messagelistitems);
        add(Conf_MessageScrollPane);

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, 8));

        revalidate();
    }


    static synchronized public void send_message_onclick()
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";
        try
        {
            if (is_conference_active(current_conf_id))
            {
                // send typed message to friend
                msg = sendTextField.getText().substring(0, (int) Math.min(tox_max_message_length(),
                                                                          sendTextField.getText().length()));

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
                            insert_into_conference_message_db(m, true);
                            Runnable myRunnable = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        sendTextField.setText("");
                                    }
                                    catch (Exception e)
                                    {
                                    }
                                }
                            };
                            SwingUtilities.invokeLater(myRunnable);
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
        Log.i(TAG, "update_all_messages:conf");

        try
        {
            // reset "new" flags for messages -------
            if (orma != null)
            {
                //orma.updateConferenceMessage().
                //        conference_identifierEq(current_conf_id).
                //        is_new(false).execute();
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
                conf_messagelistitems_model.clear();
                MessagePanelConferences.revalidate();

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
                        add_message(message);
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

    void add_message(final ConferenceMessage m)
    {
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    add_item(m);
                    if (is_at_bottom)
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
            }
        };

        SwingUtilities.invokeLater(myRunnable);
    }

    public static void add_item(ConferenceMessage new_item)
    {
        conf_messagelistitems_model.addElement(new_item);
        MessagePanel.revalidate();
    }

    static void setConfName()
    {
        try
        {
            if (MessagePanel != null)
            {
                EventQueue.invokeLater(() -> {
                    try
                    {
                        MessagePanelConferences.setBorder(
                                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 (get_conference_title_from_confid(current_conf_id) +
                                                                  " " + current_conf_id.substring(0, 5))));
                        ((TitledBorder) MessagePanelConferences.getBorder()).setTitleFont(
                                new Font("default", PLAIN, 8));

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

}