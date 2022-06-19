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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_GROUP;
import static com.zoffcc.applications.trifa.HelperGeneric.fourbytes_of_long_to_hex;
import static com.zoffcc.applications.trifa.HelperGeneric.getUnsignedInt;
import static com.zoffcc.applications.trifa.HelperGroup.get_group_title_from_groupid;
import static com.zoffcc.applications.trifa.HelperGroup.insert_into_group_message_db;
import static com.zoffcc.applications.trifa.HelperGroup.tox_group_by_groupid__wrapper;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelGroups;
import static com.zoffcc.applications.trifa.MainActivity.PREF__X_battery_saving_mode;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BORDER_TITLE;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.lookup_peer_listnum_pubkey;
import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_offline_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_get_peerlist;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_offline_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_count;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_connection_status;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_name;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_peer_get_public_key;
import static com.zoffcc.applications.trifa.MainActivity.tox_group_send_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_max_message_length;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_last_activity_for_battery_savings_ts;
import static com.zoffcc.applications.trifa.TRIFAGlobals.global_my_toxid;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class GroupMessageListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.CnfMsgListFrgnt";

    // private static JList<ConferenceMessage> conf_messagelistitems;
    // static DefaultListModel<ConferenceMessage> conf_messagelistitems_model;
    static JTable group_table = null;
    static GroupMessageTableModel group_messagelistitems_model;
    static JScrollPane Group_MessageScrollPane = null;
    static long group_scroll_to_bottom_time_window = -1;
    static long group_scroll_to_bottom_time_delta = 320;

    static String current_group_id = "-1";
    static boolean is_at_bottom = true;
    static boolean peerlist_in_update = false;

    public GroupMessageListFragmentJ()
    {
        Log.i(TAG, "GroupMessageListFragmentJ:start");

        current_group_id = "-1";

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

        group_messagelistitems_model = new GroupMessageTableModel();
        group_table = new JTable(group_messagelistitems_model);
        group_table.setTableHeader(null);
        group_table.setSelectionModel(new GroupMessageListFragmentJ.DisabledItemSelectionModel());
        group_table.setDragEnabled(false);
        group_table.setShowHorizontalLines(false);
        group_table.setDefaultRenderer(JPanel.class, new Renderer_GroupMessageListTable());
        group_table.setDefaultEditor(Object.class, new PanelCellEditorRenderer());

        group_table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = group_table.rowAtPoint(point);
                if (index != -1)
                {
                    // Next calculations assume that text is aligned to left, but are easy to adjust
                    final GroupMessage element = (GroupMessage) group_messagelistitems_model.getValueAt(index, 0);
                    final Rectangle cellBounds = group_table.getCellRect(index, 0, true);
                    final Insets insets = new Insets(0, 0, 0, 0); // renderer.getInsets();

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

        group_table.getColumnModel().addColumnModelListener(new TableColumnModelListener()
        {
            /**
             * We only need to recalculate once; so track if we are already going to do it.
             */
            boolean columnHeightWillBeCalculated = false;

            @Override
            public void columnAdded(TableColumnModelEvent e)
            {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e)
            {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e)
            {
            }

            @Override
            public void columnMarginChanged(ChangeEvent e)
            {
                try
                {
                    if (!columnHeightWillBeCalculated && group_table.getTableHeader().getResizingColumn() != null)
                    {
                        columnHeightWillBeCalculated = true;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // textTable.getTableHeader().getResizingColumn() is != null as long as the user still is holding the mouse down
                                // To avoid going over all data every few milliseconds wait for user to release
                                if (group_table.getTableHeader().getResizingColumn() != null)
                                {
                                    SwingUtilities.invokeLater(this);
                                }
                                else
                                {
                                    conf_tableChanged(null);
                                    columnHeightWillBeCalculated = false;
                                }
                            }
                        });
                    }
                }
                catch (Exception e2)
                {
                }
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e)
            {
            }
        });

        Group_MessageScrollPane = new JScrollPane(group_table);
        add(Group_MessageScrollPane);

        group_table.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                if (group_scroll_to_bottom_time_window != -1)
                {
                    final long now = System.currentTimeMillis();
                    final long pre = group_scroll_to_bottom_time_window;
                    final long time_delta = group_scroll_to_bottom_time_delta;
                    // Log.i(TAG, "____________ BOTTOM:" + now + " " + pre + " " + (now - pre));

                    if ((now > pre) && (now - time_delta < pre))
                    {
                        // Log.i(TAG, "____________ FIRE BOTTOM");
                        group_table.scrollRectToVisible(
                                group_table.getCellRect(group_table.getRowCount() - 1, 0, true));
                    }
                }
            }
        });

        revalidate();
    }

    public void conf_tableChanged(TableModelEvent e)
    {
        final int first;
        final int last;
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW)
        {
            // assume everything changed
            first = 0;
            last = group_table.getModel().getRowCount();
        }
        else
        {
            first = e.getFirstRow();
            last = e.getLastRow() + 1;
        }
        // GUI-Changes should be done through the EventDispatchThread which ensures all pending events were processed
        // Also this way nobody will change the text of our RowHeightCellRenderer because a cell is to be rendered
        if (SwingUtilities.isEventDispatchThread())
        {
            conf_updateRowHeights(first, last);
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    conf_updateRowHeights(first, last);
                }
            });
        }
    }

    private void conf_updateRowHeights(final int first, final int last)
    {
        /*
         * Auto adjust the height of rows in a JTable.
         * The only way to know the row height for sure is to render each cell
         * to determine the rendered height. After your table is populated with
         * data you can do:
         *
         */
        for (int row = first; row < last; row++)
        {
            int rowHeight = 20;
            for (int column = 0; column < group_table.getColumnCount(); column++)
            {
                Component comp = group_table.prepareRenderer(group_table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if (rowHeight != group_table.getRowHeight(row))
            {
                group_table.setRowHeight(row, rowHeight);
            }
        }
    }

    static synchronized public void send_message_onclick(final String msg2)
    {
        // Log.i(TAG,"send_message_onclick:---start");

        String msg = "";

        try
        {
            // send typed message to friend
            msg = msg2.substring(0, (int) Math.min(tox_max_message_length(), msg2.length()));

            try
            {
                GroupMessage m = new GroupMessage();
                m.is_new = false; // own messages are always "not new"
                m.tox_group_peer_pubkey = global_my_toxid.substring(0, (TOX_PUBLIC_KEY_SIZE * 2));
                m.direction = 1; // msg sent
                m.TOX_MESSAGE_TYPE = 0;
                m.read = true; // !!!! there is not "read status" with conferences in Tox !!!!
                m.tox_group_peername = null;
                m.private_message = 0;
                m.group_identifier = current_group_id;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.sent_timestamp = System.currentTimeMillis();
                m.rcvd_timestamp = System.currentTimeMillis(); // since we do not have anything better assume "now"
                m.text = msg;
                m.was_synced = false;


                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    long message_id = tox_group_send_message(tox_group_by_groupid__wrapper(current_group_id), 0, msg);
                    //Log.i(TAG, "tox_group_send_message:result=" + message_id + " hex=" +
                    //           fourbytes_of_long_to_hex(message_id) + " unsigned=" + getUnsignedInt(message_id));

                    if (PREF__X_battery_saving_mode)
                    {
                        Log.i(TAG, "global_last_activity_for_battery_savings_ts:001:*PING*");
                    }
                    global_last_activity_for_battery_savings_ts = System.currentTimeMillis();

                    if (message_id > -1L)
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

                        m.message_id_tox = fourbytes_of_long_to_hex(message_id);
                        insert_into_group_message_db(m, true);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    String get_current_group_id()
    {
        return current_group_id;
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
                orma.updateGroupMessage().
                        group_identifierEq(current_group_id.toLowerCase()).
                        is_new(false).execute();
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
                group_messagelistitems_model.removeAllElements();
                // -------------------------------------------------
                // HINT: this one does not respect ordering?!
                // -------------------------------------------------
                try
                {
                    List<GroupMessage> ml = orma.selectFromGroupMessage().
                            group_identifierEq(current_group_id.toLowerCase()).
                            orderBySent_timestampAsc().
                            toList();

                    for (GroupMessage message : ml)
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

                // Log.i(TAG, "data_values:005c");
            }
            // Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

    void add_message(final GroupMessage m, final boolean no_block)
    {
        Runnable myRunnable = () -> {
            try
            {
                add_item(m, no_block);
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_message:conf:EE1:" + e.getMessage());
                e.printStackTrace();
            }
            if ((is_at_bottom) && (!no_block))
            {
                group_scroll_to_bottom_time_window = System.currentTimeMillis();
            }
        };
        SwingUtilities.invokeLater(myRunnable);
    }

    public static void add_item(GroupMessage new_item, final boolean no_block)
    {
        group_messagelistitems_model.addElement(new_item);
        if (!no_block)
        {
            MessagePanelGroups.revalidate();
        }
    }

    static void setGroupName()
    {
        try
        {
            if (MessagePanelGroups != null)
            {
                EventQueue.invokeLater(() -> {
                    try
                    {
                        MessagePanelGroups.setBorder(
                                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                 (get_group_title_from_groupid(current_group_id) + " " +
                                                                  current_group_id.substring(0, 5))));
                        ((TitledBorder) MessagePanelGroups.getBorder()).setTitleFont(
                                new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

                        MessagePanelGroups.revalidate();
                        MessagePanelGroups.repaint();
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
        if (current_group_id.equals("-1"))
        {
            return;
        }

        final long conference_num = tox_group_by_groupid__wrapper(current_group_id);
        String group_topic = tox_group_get_name(conference_num);
        if (group_topic == null)
        {
            group_topic = "";
        }
        final String f_name = group_topic;

        EventQueue.invokeLater(() -> {
            try
            {
                long peer_count = tox_group_peer_count(conference_num);
                long frozen_peer_count = tox_group_offline_peer_count(conference_num);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    static void set_peer_names_and_avatars()
    {
        try
        {
            remove_group_all_users();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Log.d(TAG, "set_peer_names_and_avatars:002");

        final long conference_num = tox_group_by_groupid__wrapper(current_group_id);
        long num_peers = tox_group_peer_count(conference_num);

        // Log.d(TAG, "set_peer_names_and_avatars:003:peer count=" + num_peers);

        if (num_peers > 0)
        {
            long[] peers = tox_group_get_peerlist(conference_num);
            if (peers != null)
            {
                long i = 0;
                for (i = 0; i < num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, peers[(int) i]);
                        // Log.i(TAG,
                        //      "groupnum=" + conference_num + " peernum=" + peers[(int) i] + " peer_name=" + peer_name);
                        String peer_name_temp =
                                "" + peer_name + " :" + peers[(int) i] + ": " + peer_pubkey_temp.substring(0, 6);

                        add_group_user(peer_pubkey_temp, i, peer_name_temp,
                                       tox_group_peer_get_connection_status(conference_num, peers[(int) i]));
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        long offline_num_peers = tox_group_offline_peer_count(conference_num);

        if (offline_num_peers > 0)
        {
            long[] offline_peers = tox_group_get_offline_peerlist(conference_num);
            if (offline_peers != null)
            {
                long i = 0;
                for (i = 0; i < offline_num_peers; i++)
                {
                    try
                    {
                        String peer_pubkey_temp = tox_group_peer_get_public_key(conference_num, offline_peers[(int) i]);
                        String peer_name = tox_group_peer_get_name(conference_num, offline_peers[(int) i]);
                        // Log.i(TAG, "groupnum=" + conference_num + " peernum=" + offline_peers[(int) i] + " peer_name=" +
                        //           peer_name);
                        String peer_name_temp = "" + peer_name + " :" + offline_peers[(int) i] + ": " +
                                                peer_pubkey_temp.substring(0, 6);

                        add_group_user(peer_pubkey_temp, i, peer_name_temp,
                                       ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE.value);
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

    static void add_group_user(final String peer_pubkey, final long peernum, String name, int connection_status)
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

                final String name3 = name2;

                PeerModel pm = new PeerModel();
                if (connection_status == 0)
                {
                    pm.offline = true;
                }
                else
                {
                    pm.offline = false;
                }
                pm.name = name3;
                pm.peernum = peernum;
                pm.pubkey = peer_pubkey;
                pm.type = COMBINED_IS_GROUP;
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