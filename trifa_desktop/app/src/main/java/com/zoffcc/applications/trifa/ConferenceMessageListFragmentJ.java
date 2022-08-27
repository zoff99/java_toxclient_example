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

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;
import static com.zoffcc.applications.trifa.HelperConference.get_conference_title_from_confid;
import static com.zoffcc.applications.trifa.HelperConference.insert_into_conference_message_db;
import static com.zoffcc.applications.trifa.HelperConference.is_conference_active;
import static com.zoffcc.applications.trifa.HelperConference.tox_conference_by_confid__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.resolve_name_for_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
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
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LAST_PAGE_MARGIN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
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

    // private static JList<ConferenceMessage> conf_messagelistitems;
    // static DefaultListModel<ConferenceMessage> conf_messagelistitems_model;
    static JTable conf_table = null;
    static ConferenceMessageTableModel conf_messagelistitems_model;
    static JScrollPane Conf_MessageScrollPane = null;
    static long conf_scroll_to_bottom_time_window = -1;
    static long conf_scroll_to_bottom_time_delta = 320;
    static int current_page_offset = -1;

    static String current_conf_id = "-1";
    static boolean is_at_bottom = true;
    static boolean peerlist_in_update = false;

    public ConferenceMessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");

        current_conf_id = "-1";

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

        conf_messagelistitems_model = new ConferenceMessageTableModel();
        conf_table = new JTable(conf_messagelistitems_model);
        conf_table.setTableHeader(null);
        conf_table.setSelectionModel(new ConferenceMessageListFragmentJ.DisabledItemSelectionModel());
        conf_table.setDragEnabled(false);
        conf_table.setShowHorizontalLines(false);
        conf_table.setDefaultRenderer(JPanel.class, new Renderer_ConfMessageListTable());
        conf_table.setDefaultEditor(Object.class, new PanelCellEditorRenderer());

        conf_table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = conf_table.rowAtPoint(point);
                if (index != -1)
                {
                    // Next calculations assume that text is aligned to left, but are easy to adjust
                    final ConferenceMessage element = (ConferenceMessage) conf_messagelistitems_model.getValueAt(index,
                                                                                                                 0);
                    final Rectangle cellBounds = conf_table.getCellRect(index, 0, true);
                    final Insets insets = new Insets(0, 0, 0, 0); // renderer.getInsets();

                    // Ensure that mouse press happened within top/bottom insets
                    if (cellBounds.y + insets.top <= point.y &&
                        point.y <= cellBounds.y + cellBounds.height - insets.bottom)
                    {

                        // message for paging
                        if ((element.tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                            (element.message_id_tox.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)))
                        {
                            if (SwingUtilities.isLeftMouseButton(e))
                            {
                                if ((current_page_offset - MESSAGE_PAGING_NUM_MSGS_PER_PAGE) < 1)
                                {
                                    current_page_offset = 0;
                                }
                                else
                                {
                                    current_page_offset = current_page_offset - MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
                                }
                                update_all_messages(true, true);
                            }
                        }
                        // message for paging
                        else if ((element.tox_peerpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                                 (element.message_id_tox.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)))
                        {
                            if (SwingUtilities.isLeftMouseButton(e))
                            {
                                current_page_offset = current_page_offset + MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
                                update_all_messages(true, true);
                            }
                        }
                        else
                        {
                            if (SwingUtilities.isLeftMouseButton(e))
                            {
                                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(element.text), null);
                                Toast.makeToast(MainFrame, lo.getString("copied_msg_to_clipboard"), 800);
                            }
                            else
                            {
                                Log.i(TAG, "popup dialog");
                                textAreaDialog(null, element.text, "Message");
                            }
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

        conf_table.getColumnModel().addColumnModelListener(new TableColumnModelListener()
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
                    if (!columnHeightWillBeCalculated && conf_table.getTableHeader().getResizingColumn() != null)
                    {
                        columnHeightWillBeCalculated = true;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // textTable.getTableHeader().getResizingColumn() is != null as long as the user still is holding the mouse down
                                // To avoid going over all data every few milliseconds wait for user to release
                                if (conf_table.getTableHeader().getResizingColumn() != null)
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

        Conf_MessageScrollPane = new JScrollPane(conf_table);
        add(Conf_MessageScrollPane);

        conf_table.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                if (conf_scroll_to_bottom_time_window != -1)
                {
                    final long now = System.currentTimeMillis();
                    final long pre = conf_scroll_to_bottom_time_window;
                    final long time_delta = conf_scroll_to_bottom_time_delta;
                    // Log.i(TAG, "____________ BOTTOM:" + now + " " + pre + " " + (now - pre));

                    if ((now > pre) && (now - time_delta < pre))
                    {
                        // Log.i(TAG, "____________ FIRE BOTTOM");
                        conf_table.scrollRectToVisible(conf_table.getCellRect(conf_table.getRowCount() - 1, 0, true));
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
            last = conf_table.getModel().getRowCount();
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
            for (int column = 0; column < conf_table.getColumnCount(); column++)
            {
                Component comp = conf_table.prepareRenderer(conf_table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if (rowHeight != conf_table.getRowHeight(row))
            {
                conf_table.setRowHeight(row, rowHeight);
            }
        }
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

    public void reset_paging()
    {
        current_page_offset = -1; // reset paging when we change friend that is shown
    }

    class DisabledItemSelectionModel extends DefaultListSelectionModel
    {
        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            super.setSelectionInterval(-1, -1);
        }
    }

    void update_all_messages(boolean always, boolean paging)
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
                try
                {
                    boolean later_messages = false;
                    boolean older_messages = false;
                    List<ConferenceMessage> ml = null;
                    if (paging)
                    {
                        later_messages = true;
                        older_messages = true;

                        int count_messages = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                orderBySent_timestampAsc().
                                count();

                        int offset = 0;
                        int rowcount = MESSAGE_PAGING_NUM_MSGS_PER_PAGE;

                        if (current_page_offset == -1) // HINT: page at the bottom (latest messages shown)
                        {
                            later_messages = false;
                            offset = count_messages - MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
                            if (offset < 0)
                            {
                                offset = 0;
                            }
                            current_page_offset = offset;
                            // HINT: we need MESSAGE_PAGING_LAST_PAGE_MARGIN in case new messages arrived
                            //       since "count_messages" was calculated above
                            rowcount = MESSAGE_PAGING_NUM_MSGS_PER_PAGE + MESSAGE_PAGING_LAST_PAGE_MARGIN;
                        }
                        else
                        {
                            if ((count_messages - current_page_offset) < MESSAGE_PAGING_NUM_MSGS_PER_PAGE)
                            {
                                current_page_offset = count_messages - MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
                                rowcount = MESSAGE_PAGING_NUM_MSGS_PER_PAGE + MESSAGE_PAGING_LAST_PAGE_MARGIN;
                            }
                            offset = current_page_offset;
                        }

                        if ((count_messages - offset) <= MESSAGE_PAGING_NUM_MSGS_PER_PAGE)
                        {
                            later_messages = false;
                        }

                        if (offset < 1)
                        {
                            older_messages = false;
                        }

                        ml = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                orderBySent_timestampAsc().
                                limit(rowcount, offset).
                                toList();
                    }
                    else
                    {
                        ml = orma.selectFromConferenceMessage().
                                conference_identifierEq(current_conf_id).
                                tox_peerpubkeyNotEq(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY).
                                orderBySent_timestampAsc().
                                toList();
                    }

                    if (ml != null)
                    {
                        if (older_messages)
                        {
                            ConferenceMessage m_older = new ConferenceMessage();
                            m_older.tox_peerpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                            m_older.is_new = false;
                            m_older.direction = 0;
                            m_older.message_id_tox = MESSAGE_PAGING_SHOW_OLDER_HASH;
                            m_older.text = "^^^ show older Messages ^^^";
                            add_message(m_older, false);
                        }

                        for (ConferenceMessage message : ml)
                        {
                            if (message == ml.get(ml.size() - 1))
                            {
                                add_message(message, true);
                            }
                            else
                            {
                                if (later_messages)
                                {
                                    add_message(message, false);
                                }
                                else
                                {
                                    add_message(message, true);
                                }
                            }
                        }

                        if (later_messages)
                        {
                            ConferenceMessage m_later = new ConferenceMessage();
                            m_later.tox_peerpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                            m_later.is_new = false;
                            m_later.direction = 0;
                            m_later.message_id_tox = MESSAGE_PAGING_SHOW_NEWER_HASH;
                            m_later.text = "vvv show newer Messages vvv";
                            add_message(m_later, true);
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

    void add_message(final ConferenceMessage m, final boolean no_block)
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
                conf_scroll_to_bottom_time_window = System.currentTimeMillis();
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
                pm.type = COMBINED_IS_CONFERENCE;
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