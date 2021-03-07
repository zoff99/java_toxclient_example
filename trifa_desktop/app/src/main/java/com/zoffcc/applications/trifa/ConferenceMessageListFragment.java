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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class ConferenceMessageListFragment extends JPanel
{
    private static final String TAG = "trifa.CnfMsgListFrgnt";

    private static JList<ConferenceMessage> conf_messagelistitems;
    static DefaultListModel<ConferenceMessage> conf_messagelistitems_model;
    static JScrollPane Conf_MessageScrollPane = null;

    static String current_conf_id = "-1";
    static boolean is_at_bottom = true;

    public ConferenceMessageListFragment()
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

        revalidate();
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
}