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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
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

import static com.zoffcc.applications.trifa.HelperFiletransfer.get_filetransfer_filenum_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.insert_into_filetransfer_db;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_accepted_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.set_filetransfer_state_from_id;
import static com.zoffcc.applications.trifa.HelperFiletransfer.update_filetransfer_db_full;
import static com.zoffcc.applications.trifa.HelperFriend.get_friend_name_from_pubkey;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_by_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperFriend.tox_friend_get_public_key__wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.set_message_accepted_from_id;
import static com.zoffcc.applications.trifa.HelperGeneric.tox_friend_send_message_wrapper;
import static com.zoffcc.applications.trifa.HelperGeneric.trim_to_utf8_length_bytes;
import static com.zoffcc.applications.trifa.HelperMessage.insert_into_message_db;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_queueing_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.set_message_state_from_id;
import static com.zoffcc.applications.trifa.HelperMessage.update_single_message_from_messge_id;
import static com.zoffcc.applications.trifa.HelperOSFile.run_file;
import static com.zoffcc.applications.trifa.HelperOSFile.show_file_in_explorer;
import static com.zoffcc.applications.trifa.MainActivity.MainFrame;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.TTF_FONT_FAMILY_BORDER_TITLE;
import static com.zoffcc.applications.trifa.MainActivity.lo;
import static com.zoffcc.applications.trifa.MainActivity.messageInputTextField;
import static com.zoffcc.applications.trifa.MainActivity.tox_file_control;
import static com.zoffcc.applications.trifa.MainActivity.tox_self_set_typing;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.update_all_messages;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_LAST_PAGE_MARGIN;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_NUM_MSGS_PER_PAGE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_NEWER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.MESSAGE_PAGING_SHOW_OLDER_HASH;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MSGV3_MAX_MESSAGE_LENGTH;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static java.awt.Font.PLAIN;

public class MessageListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.MsgListFrgnt";

    static int global_typing = 0;
    static Thread typing_flag_thread = null;
    final static int TYPING_FLAG_DEACTIVATE_DELAY_IN_MILLIS = 1000; // 1 second

    static String current_pk = null;
    static long friendnum = -1;
    static long friendnum_prev = -1;
    static boolean attachemnt_instead_of_send = false;
    static boolean is_at_bottom = true;
    static boolean show_only_files = false;

    static JTable table = null;
    static MessageTableModel messagelistitems_model;
    static JScrollPane MessageScrollPane = null;
    static long scroll_to_bottom_time_window = -1;
    static long scroll_to_bottom_time_delta = 320;
    static int current_page_offset = -1;

    public MessageListFragmentJ()
    {
        Log.i(TAG, "MessageListFragmentJ:start");
        friendnum = -1;
        current_pk = null;

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "..."));
        ((TitledBorder) getBorder()).setTitleFont(new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));

        messagelistitems_model = new MessageTableModel();
        table = new JTable(messagelistitems_model);
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // table.setRowHeight(40);
        table.setTableHeader(null);
        table.setSelectionModel(new DisabledItemSelectionModel());
        table.setDragEnabled(false);
        table.setShowHorizontalLines(false);
        table.setDefaultRenderer(JPanel.class, new Renderer_MessageListTable());
        table.setDefaultEditor(Object.class, new PanelCellEditorRenderer());

        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(final MouseEvent e)
            {
                final Point point = e.getPoint();
                final int index = table.rowAtPoint(point);
                // Log.i(TAG, "MMMMMM:" + point + " IIIII:" + index);
                if (index != -1)
                {
                    // Next calculations assume that text is aligned to left, but are easy to adjust
                    final Message element = (Message) messagelistitems_model.getValueAt(index, 0);
                    final Rectangle cellBounds = table.getCellRect(index, 0, true);
                    // final Renderer_MessageList renderer = (Renderer_MessageList) messagelistitems.getCellRenderer();
                    final Insets insets = new Insets(0, 0, 0, 0); // renderer.getInsets();

                    boolean button_pressed = false;

                    // Log.i(TAG,
                    //      "cellBounds.x=" + cellBounds.x + " cellBounds.y=" + cellBounds.y + "  cellBounds.width=" +
                    //      cellBounds.width + "  cellBounds.height=" + cellBounds.height + " element._swing_ok=" +
                    //      element._swing_ok.getBounds());

                    // Ensure that mouse press happened within top/bottom insets
                    if (cellBounds.y + insets.top <= point.y &&
                        point.y <= cellBounds.y + cellBounds.height - insets.bottom)
                    {
                        // msg is FT
                        if (element.TRIFA_MESSAGE_TYPE == TRIFA_MSG_FILE.value)
                        {
                            // FT (not-started or in progress) and outgoing
                            if ((element.direction == 1) && ((element.state == TOX_FILE_CONTROL_PAUSE.value) ||
                                                             (element.state == TOX_FILE_CONTROL_RESUME.value)))
                            {
                                Rectangle ok_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    ok_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_ok.getLocation().x +
                                            element._swing_ok.getParent().getLocation().x,
                                            cellBounds.y + element._swing_ok.getLocation().y +
                                            element._swing_ok.getParent().getLocation().y,
                                            element._swing_ok.getBounds().width, element._swing_ok.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                Rectangle cancel_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    cancel_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_cancel.getLocation().x +
                                            element._swing_cancel.getParent().getLocation().x,
                                            cellBounds.y + element._swing_cancel.getLocation().y +
                                            element._swing_cancel.getParent().getLocation().y,
                                            element._swing_cancel.getBounds().width,
                                            element._swing_cancel.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                // ok and cancel button
                                try
                                {
                                    if (ok_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "OK button pressed");
                                        button_pressed = true;

                                        // queue FT
                                        set_message_queueing_from_id(element.id, true);
                                        try
                                        {
                                            element._swing_ok.setVisible(false);
                                        }
                                        catch (Exception ee)
                                        {
                                        }

                                        // update message view
                                        update_single_message_from_messge_id(element.id, true);

                                        Log.i(TAG, "button_ok:OnTouch:009");

                                    }
                                    else if (cancel_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "CANCEL button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            set_message_queueing_from_id(element.id, false);

                                            // cancel FT
                                            Log.i(TAG, "button_cancel:OnTouch:001");
                                            // values.get(position).state = TOX_FILE_CONTROL_CANCEL.value;
                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_CANCEL.value);

                                            try
                                            {
                                                element._swing_cancel.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e4)
                                        {
                                        }
                                    }
                                    else
                                    {

                                        Log.i(TAG, "button:" + element._swing_ok.getBounds().contains(point) + " " +
                                                   element._swing_ok.getLocation().x + " " +
                                                   element._swing_ok.getLocation().y + " " +
                                                   element._swing_ok.getParent().getLocation().x + " " +
                                                   element._swing_ok.getParent().getLocation().y);

                                        Log.i(TAG, "button:" + element._swing_cancel.getBounds().contains(point) + " " +
                                                   element._swing_cancel.getLocation().x + " " +
                                                   element._swing_cancel.getLocation().y + " " +
                                                   element._swing_cancel.getParent().getLocation().x + " " +
                                                   element._swing_cancel.getParent().getLocation().y);

                                    }
                                }
                                catch (Exception e2)
                                {
                                }
                            }
                            // FT (not-started or in progress) and incoming
                            else if ((element.direction == 0) && ((element.state == TOX_FILE_CONTROL_PAUSE.value) ||
                                                                  (element.state == TOX_FILE_CONTROL_RESUME.value)))
                            {
                                Rectangle ok_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    ok_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_ok.getLocation().x +
                                            element._swing_ok.getParent().getLocation().x,
                                            cellBounds.y + element._swing_ok.getLocation().y +
                                            element._swing_ok.getParent().getLocation().y,
                                            element._swing_ok.getBounds().width, element._swing_ok.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                Rectangle cancel_button_rect_absolute = new Rectangle(-1, -1, 0, 0);
                                try
                                {
                                    cancel_button_rect_absolute = new Rectangle(
                                            cellBounds.x + element._swing_cancel.getLocation().x +
                                            element._swing_cancel.getParent().getLocation().x,
                                            cellBounds.y + element._swing_cancel.getLocation().y +
                                            element._swing_cancel.getParent().getLocation().y,
                                            element._swing_cancel.getBounds().width,
                                            element._swing_cancel.getBounds().height);
                                }
                                catch (Exception e4)
                                {
                                }

                                // ok and cancel button
                                try
                                {
                                    if (ok_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "OK button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            // accept FT
                                            set_filetransfer_accepted_from_id(element.filetransfer_id);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_RESUME.value);
                                            set_message_accepted_from_id(element.id);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_RESUME.value);
                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_RESUME.value);

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e2)
                                        {
                                            e2.printStackTrace();
                                            Log.i(TAG, "MM2MM:EE1:" + e2.getMessage());
                                        }

                                    }
                                    else if (cancel_button_rect_absolute.contains(point))
                                    {
                                        Log.i(TAG, "CANCEL button pressed");
                                        button_pressed = true;

                                        try
                                        {
                                            // cancel FT
                                            Log.i(TAG, "button_cancel:OnTouch:001");


                                            tox_file_control(
                                                    tox_friend_by_public_key__wrapper(element.tox_friendpubkey),
                                                    get_filetransfer_filenum_from_id(element.filetransfer_id),
                                                    TOX_FILE_CONTROL_CANCEL.value);
                                            set_filetransfer_state_from_id(element.filetransfer_id,
                                                                           TOX_FILE_CONTROL_CANCEL.value);
                                            set_message_state_from_id(element.id, TOX_FILE_CONTROL_CANCEL.value);

                                            try
                                            {
                                                element._swing_cancel.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            try
                                            {
                                                element._swing_ok.setVisible(false);
                                            }
                                            catch (Exception ee)
                                            {
                                            }

                                            // update message view
                                            update_single_message_from_messge_id(element.id, true);
                                        }
                                        catch (Exception e4)
                                        {
                                        }
                                    }
                                    else
                                    {

                                        Log.i(TAG, "button:" + element._swing_ok.getBounds().contains(point) + " " +
                                                   element._swing_ok.getLocation().x + " " +
                                                   element._swing_ok.getLocation().y + " " +
                                                   element._swing_ok.getParent().getLocation().x + " " +
                                                   element._swing_ok.getParent().getLocation().y);

                                        Log.i(TAG, "button:" + element._swing_cancel.getBounds().contains(point) + " " +
                                                   element._swing_cancel.getLocation().x + " " +
                                                   element._swing_cancel.getLocation().y + " " +
                                                   element._swing_cancel.getParent().getLocation().x + " " +
                                                   element._swing_cancel.getParent().getLocation().y);

                                    }
                                }
                                catch (Exception e2)
                                {
                                }
                            }

                            if (button_pressed)
                            {
                                return;
                            }

                            // FT is done and file is here
                            if (element.filedb_id > 0)
                            {
                                if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                                {
                                    if (SwingUtilities.isLeftMouseButton(e))
                                    {
                                        Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                                        run_file(element.filename_fullpath);
                                    }
                                    else
                                    {
                                        Toast.makeToast(MainFrame, lo.getString("show_file_in_explorer_"), 800);
                                        show_file_in_explorer(element.filename_fullpath);
                                    }
                                }
                            }
                            // FT (canceled or in progress) and outgoing
                            else if (((element.state == TOX_FILE_CONTROL_CANCEL.value) ||
                                      (element.state == TOX_FILE_CONTROL_RESUME.value)) && (element.direction == 1))
                            {
                                if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                                {
                                    if (SwingUtilities.isLeftMouseButton(e))
                                    {
                                        Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                                        run_file(element.filename_fullpath);
                                    }
                                    else
                                    {
                                        Toast.makeToast(MainFrame, lo.getString("show_file_in_explorer_"), 800);
                                        show_file_in_explorer(element.filename_fullpath);
                                    }
                                }
                            }
                            //else if (element.direction == 1)
                            //{
                            //    if ((element.filename_fullpath != null) && (element.filename_fullpath.length() > 0))
                            //    {
                            //        run_file(element.filename_fullpath);
                            //        Toast.makeToast(MainFrame, lo.getString("opening_file_"), 800);
                            //    }
                            //}
                        }
                        // msg is TEXT
                        else
                        {
                            // message for paging
                            if ((element.tox_friendpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                                (element.msg_idv3_hash.equals(MESSAGE_PAGING_SHOW_OLDER_HASH)))
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
                            else if ((element.tox_friendpubkey.equals(TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY)) &&
                                     (element.msg_idv3_hash.equals(MESSAGE_PAGING_SHOW_NEWER_HASH)))
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

        table.getColumnModel().addColumnModelListener(new TableColumnModelListener()
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
                    if (!columnHeightWillBeCalculated && table.getTableHeader().getResizingColumn() != null)
                    {
                        columnHeightWillBeCalculated = true;
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                // textTable.getTableHeader().getResizingColumn() is != null as long as the user still is holding the mouse down
                                // To avoid going over all data every few milliseconds wait for user to release
                                if (table.getTableHeader().getResizingColumn() != null)
                                {
                                    SwingUtilities.invokeLater(this);
                                }
                                else
                                {
                                    tableChanged(null);
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

        MessageScrollPane = new JScrollPane(table);
        add(MessageScrollPane);

        table.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                if (scroll_to_bottom_time_window != -1)
                {
                    final long now = System.currentTimeMillis();
                    final long pre = scroll_to_bottom_time_window;
                    final long time_delta = scroll_to_bottom_time_delta;
                    // Log.i(TAG, "____________ BOTTOM:" + now + " " + pre + " " + (now - pre));

                    if ((now > pre) && (now - time_delta < pre))
                    {
                        // Log.i(TAG, "____________ FIRE BOTTOM");
                        table.scrollRectToVisible(table.getCellRect(table.getRowCount() - 1, 0, true));
                    }
                }
            }
        });

        revalidate();
    }

    public void tableChanged(TableModelEvent e)
    {
        final int first;
        final int last;
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW)
        {
            // assume everything changed
            first = 0;
            last = table.getModel().getRowCount();
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
            updateRowHeights(first, last);
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateRowHeights(first, last);
                }
            });
        }
    }

    private void updateRowHeights(final int first, final int last)
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
            for (int column = 0; column < table.getColumnCount(); column++)
            {
                Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }
            if (rowHeight != table.getRowHeight(row))
            {
                table.setRowHeight(row, rowHeight);
            }
        }
    }

    static void setFriendName()
    {
        try
        {
            if (MessagePanel != null)
            {
                EventQueue.invokeLater(() -> {
                    try
                    {
                        MessagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                                                                                get_friend_name_from_pubkey(
                                                                                        MessagePanel.current_pk))

                        );
                        ((TitledBorder) MessagePanel.getBorder()).setTitleFont(
                                new Font("default", PLAIN, TTF_FONT_FAMILY_BORDER_TITLE));
                        MessagePanel.revalidate();
                        MessagePanel.repaint();
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

    class DisabledItemSelectionModel extends DefaultListSelectionModel
    {
        @Override
        public void setSelectionInterval(int index0, int index1)
        {
            super.setSelectionInterval(-1, -1);
        }
    }

    /* HINT: send a message to a friend */
    synchronized public static void send_message_onclick(final String msg2)
    {
        // Log.i(TAG, "send_message_onclick:---start");

        if (friendnum == -1)
        {
            return;
        }

        String msg = "";
        try
        {
            if (attachemnt_instead_of_send)
            {
                // TODO: no attachments yet
            }
            else
            {
                // send typed message to friend
                msg = trim_to_utf8_length_bytes(msg2, TOX_MSGV3_MAX_MESSAGE_LENGTH);

                Message m = new Message();
                m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
                m.direction = 1; // msg sent
                m.TOX_MESSAGE_TYPE = 0;
                m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
                m.rcvd_timestamp = 0L;
                m.is_new = false; // own messages are always "not new"
                m.sent_timestamp = System.currentTimeMillis();
                m.read = false;
                m.text = msg;
                m.msg_version = 0;
                m.resend_count = 0; // we have tried to resend this message "0" times
                m.sent_push = 0;
                m.msg_idv3_hash = "";
                m.msg_id_hash = "";
                m.raw_msgv2_bytes = "";

                if ((msg != null) && (!msg.equalsIgnoreCase("")))
                {
                    MainActivity.send_message_result result = tox_friend_send_message_wrapper(
                            tox_friend_get_public_key__wrapper(friendnum), 0, msg, (m.sent_timestamp / 1000));


                    if (result == null)
                    {
                        return;
                    }


                    long res = result.msg_num;

                    if (res > -1)
                    {
                        m.resend_count = 1; // we sent the message successfully
                        m.message_id = res;
                    }
                    else
                    {
                        m.resend_count = 0; // sending was NOT successfull
                        m.message_id = -1;
                    }

                    if (result.msg_v2)
                    {
                        m.msg_version = 1;
                    }
                    else
                    {
                        m.msg_version = 0;
                    }

                    if ((result.msg_hash_hex != null) && (!result.msg_hash_hex.equalsIgnoreCase("")))
                    {
                        // msgV2 message -----------
                        m.msg_id_hash = result.msg_hash_hex;
                        // msgV2 message -----------
                    }

                    if ((result.msg_hash_v3_hex != null) && (!result.msg_hash_v3_hex.equalsIgnoreCase("")))
                    {
                        // msgV3 message -----------
                        m.msg_idv3_hash = result.msg_hash_v3_hex;
                        // msgV3 message -----------
                    }

                    if ((result.raw_message_buf_hex != null) && (!result.raw_message_buf_hex.equalsIgnoreCase("")))
                    {
                        // save raw message bytes of this v2 msg into the database
                        // we need it if we want to resend it later
                        m.raw_msgv2_bytes = result.raw_message_buf_hex;
                    }

                    Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                messageInputTextField.setText("");
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    };
                    SwingUtilities.invokeLater(myRunnable);
                    stop_self_typing_indicator_s();

                    long row_id = insert_into_message_db(m, true);
                    m.id = row_id;
                }
            }
        }
        catch (Exception e)
        {
            msg = "";
            e.printStackTrace();
        }

        // Log.i(TAG,"send_message_onclick:---end");
    }

    static void stop_self_typing_indicator_s()
    {
        stop_self_typing_indicator();
    }

    static void stop_self_typing_indicator()
    {
        if (global_typing == 1)
        {
            global_typing = 0;  // typing = 0
            try
            {
                // Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":stop_self_typing_indicator");
                tox_self_set_typing(get_current_friendnum(), global_typing);
            }
            catch (Exception e)
            {
                Log.i(TAG, "typing:fn#" + get_current_friendnum() + ":EE2.b" + e.getMessage());
            }
        }
    }

    static void add_message(final Message m, final boolean no_block)
    {
        Runnable myRunnable = () -> {
            try
            {
                add_item(m, no_block);
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_message:EE1:" + e.getMessage());
                e.printStackTrace();
            }
            if ((is_at_bottom) && (!no_block))
            {
                scroll_to_bottom_time_window = System.currentTimeMillis();
            }
        };
        SwingUtilities.invokeLater(myRunnable);
    }

    static void update_all_messages(boolean always, boolean paging)
    {
        // Log.i(TAG, "update_all_messages");

        try
        {
            // reset "new" flags for messages -------
            orma.updateMessage().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                    is_new(false).
                    execute();
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
                messagelistitems_model.removeAllElements();
                // MessagePanel.revalidate();
                // long t2 = System.currentTimeMillis();
                if (show_only_files)
                {
                    // TODO:
                }
                else
                {
                    try
                    {
                        boolean later_messages = false;
                        boolean older_messages = false;
                        List<Message> ml = null;
                        if (paging)
                        {
                            later_messages = true;
                            older_messages = true;
                            int count_messages = orma.selectFromMessage().
                                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                                    orderBySent_timestampAsc().
                                    orderBySent_timestamp_msAsc().
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

                            ml = orma.selectFromMessage().
                                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                                    orderBySent_timestampAsc().
                                    orderBySent_timestamp_msAsc().
                                    limit(rowcount, offset).
                                    toList();
                        }
                        else
                        {
                            ml = orma.selectFromMessage().
                                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).
                                    orderBySent_timestampAsc().
                                    orderBySent_timestamp_msAsc().
                                    toList();
                        }

                        if (ml != null)
                        {
                            if (older_messages)
                            {
                                Message m_older = new Message();
                                m_older.tox_friendpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                                m_older.is_new = false;
                                m_older.direction = 0;
                                m_older.msg_idv3_hash = MESSAGE_PAGING_SHOW_OLDER_HASH;
                                m_older.text = "^^^ show older Messages ^^^";
                                add_message(m_older, false);
                            }

                            for (Message message : ml)
                            {
                                if (message == ml.get(ml.size() - 1))
                                {
                                    add_message(message, false);
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
                                Message m_later = new Message();
                                m_later.tox_friendpubkey = TRIFA_SYSTEM_MESSAGE_PEER_PUBKEY;
                                m_later.is_new = false;
                                m_later.direction = 0;
                                m_later.msg_idv3_hash = MESSAGE_PAGING_SHOW_NEWER_HASH;
                                m_later.text = "vvv show newer Messages vvv";
                                add_message(m_later, true);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                // long t3 = System.currentTimeMillis();
                // Log.i(TAG, "data_values:005c:" + (t3 - t2) + "ms");
            }
            // Log.i(TAG, "data_values:005d");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "data_values:005:EE1:" + e.getMessage());
        }

    }

    public static long get_current_friendnum()
    {
        return friendnum;
    }

    public static void add_item(Message new_item, final boolean no_block)
    {
        messagelistitems_model.addElement(new_item);
        if (!no_block)
        {
            MessagePanel.revalidate();
        }
    }

    static void modify_message(final Message m)
    {
        try
        {
            Iterator<Message> it = messagelistitems_model.elements();

            while (it.hasNext())
            {
                Message msg = (Message) it.next();
                if (msg != null)
                {
                    if (msg.id == m.id)
                    {
                        int pos = messagelistitems_model.indexOf(msg);
                        // Log.i(TAG, "modify_message:m.id=" + m.id);

                        EventQueue.invokeLater(() -> {
                            messagelistitems_model.set(pos, m);
                        });
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public void setCurrentPK(String current_pk_)
    {
        current_pk = current_pk_;
        current_page_offset = -1; // reset paging when we change friend that is shown
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

    static void add_outgoing_file(String filepath, String filename)
    {
        Log.i(TAG, "add_outgoing_file:regular file");

        long file_size = -1;
        try
        {
            file_size = new java.io.File(filepath + "/" + filename).length();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // file length unknown?
            return;
        }

        if (file_size < 1)
        {
            // file length "zero"?
            return;
        }

        Log.i(TAG, "add_outgoing_file:friendnum=" + friendnum);

        if (friendnum == -1)
        {
            // sorry, still no friendnum
            Log.i(TAG, "add_outgoing_file:sorry, still no friendnum");
            return;
        }

        Log.i(TAG, "add_outgoing_file:friendnum(2)=" + friendnum);

        Filetransfer f = new Filetransfer();
        f.tox_public_key_string = tox_friend_get_public_key__wrapper(friendnum);
        f.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
        f.file_number = -1; // add later when we actually have the number
        f.kind = TOX_FILE_KIND_DATA.value;
        f.state = TOX_FILE_CONTROL_PAUSE.value;
        f.path_name = filepath;
        f.file_name = filename;
        f.filesize = file_size;
        f.ft_accepted = false;
        f.ft_outgoing_started = false;
        f.current_position = 0;

        Log.i(TAG, "add_outgoing_file:tox_public_key_string=" + f.tox_public_key_string);

        long ft_id = insert_into_filetransfer_db(f);
        f.id = ft_id;

        // Message m_tmp = orma.selectFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(3)).orderByMessage_idDesc().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:2:" + ft_id);


        // ---------- DEBUG ----------
        Filetransfer ft_tmp = orma.selectFromFiletransfer().idEq(ft_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4a:" + "fid=" + ft_tmp.id + " mid=" + ft_tmp.message_id);
        // ---------- DEBUG ----------


        // add FT message to UI
        Message m = new Message();

        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friendnum);
        m.direction = 1; // msg outgoing
        m.TOX_MESSAGE_TYPE = 0;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
        m.filetransfer_id = ft_id;
        m.filedb_id = -1;
        m.state = TOX_FILE_CONTROL_PAUSE.value;
        m.ft_accepted = false;
        m.ft_outgoing_started = false;
        m.ft_outgoing_queued = false;
        m.filename_fullpath = new java.io.File(filepath + "/" + filename).getAbsolutePath();
        m.sent_timestamp = System.currentTimeMillis();
        m.text = filename + "\n" + file_size + " bytes";
        m.is_new = false; // no notification for outgoing filetransfers
        m.filetransfer_kind = TOX_FILE_KIND_DATA.value;

        long new_msg_id = insert_into_message_db(m, true);
        m.id = new_msg_id;

        // ---------- DEBUG ----------
        Log.i(TAG, "add_outgoing_file:MM2MM:3:" + new_msg_id);
        Message m_tmp = orma.selectFromMessage().idEq(new_msg_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

        f.message_id = new_msg_id;
        // ** // update_filetransfer_db_messageid_from_id(f, ft_id);
        update_filetransfer_db_full(f);

        // ---------- DEBUG ----------
        Filetransfer ft_tmp2 = orma.selectFromFiletransfer().idEq(ft_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:4b:" + "fid=" + ft_tmp2.id + " mid=" + ft_tmp2.message_id);
        // ---------- DEBUG ----------

        // ---------- DEBUG ----------
        m_tmp = orma.selectFromMessage().idEq(new_msg_id).toList().get(0);
        Log.i(TAG, "add_outgoing_file:MM2MM:5:" + m.filetransfer_id + "::" + m_tmp);
        // ---------- DEBUG ----------

        // --- ??? should we do this here?
        //        try
        //        {
        //            // update "new" status on friendlist fragment
        //            FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
        //            friend_list_fragment.modify_friend(f2, friendnum);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
        //        }
        // --- ??? should we do this here?
    }

    static class JlistCustom<M> extends JList
    {
        public JlistCustom()
        {
            super();
        }

        @Override
        public int getFixedCellWidth()
        {
            //Log.i(TAG, "getFixedCellWidth");
            return super.getFixedCellWidth();
        }

        @Override
        public int getFixedCellHeight()
        {
            int ret = super.getFixedCellHeight();
            //Log.i(TAG, "getFixedCellHeight=" + ret);
            return ret;
        }

        @Override
        public int getLeadSelectionIndex()
        {
            int ret = super.getLeadSelectionIndex();
            //Log.i(TAG, "getLeadSelectionIndex=" + ret);
            return ret;
        }

        @Override
        public int getMinSelectionIndex()
        {
            //Log.i(TAG, "getMinSelectionIndex");
            return super.getMinSelectionIndex();
        }

        @Override
        public int getMaxSelectionIndex()
        {
            //Log.i(TAG, "getMaxSelectionIndex");
            return super.getMaxSelectionIndex();
        }

        @Override
        public boolean getValueIsAdjusting()
        {
            //Log.i(TAG, "paintChildren");
            return super.getValueIsAdjusting();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize()
        {
            //Log.i(TAG, "paintChildren");
            return super.getPreferredScrollableViewportSize();
        }

        @Override
        protected void paintChildren(Graphics g)
        {
            //Log.i(TAG, "paintChildren");
            super.paintChildren(g);
        }

        @Override
        public Rectangle getVisibleRect()
        {
            Rectangle r = super.getVisibleRect();
            //Log.i(TAG, "paintChildren=" + r);
            return r;
        }

        @Override
        public int getVisibleRowCount()
        {
            int ret = super.getVisibleRowCount();
            //Log.i(TAG, "getVisibleRowCount=" + ret);
            return ret;
        }

        @Override
        public int getLastVisibleIndex()
        {
            int ret = super.getLastVisibleIndex();
            //Log.i(TAG, "getLastVisibleIndex=" + ret);
            return ret;
        }

        @Override
        public int getFirstVisibleIndex()
        {
            int ret = super.getFirstVisibleIndex();
            //Log.i(TAG, "getFirstVisibleIndex=" + ret);
            return ret;
        }
    }
}
