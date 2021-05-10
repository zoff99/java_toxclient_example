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
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.ORMA_TRACE;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.current_pk;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.modify_message;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperMessage
{
    private static final String TAG = "trifa.Hlp.Message";

    static long insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        long row_id = orma.insertIntoMessage(m);

        try
        {
            long msg_id = -1;

            try
            {
                Statement statement = sqldb.createStatement();
                ResultSet rs = statement.executeQuery("SELECT id FROM Message where rowid='" + row_id + "'");
                if (rs.next())
                {
                    msg_id = rs.getLong("id");
                    // Log.i(TAG, "msg_id=" + msg_id);
                }
            }
            catch (Exception e)
            {
            }

            if (update_message_view_flag)
            {
                add_single_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            Log.i(TAG, "insert_into_message_db:EE:" + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static void add_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            // Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    if (message_id != -1)
                    {
                        // Log.i(TAG, "add_single_message_from_messge_id:message_id=" + message_id);

                        try
                        {
                            Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().toList().get(0);

                            if (m.id != -1)
                            {
                                // Log.i(TAG, "add_single_message_from_messge_id:m.id=" + m.id);

                                if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    // Log.i(TAG,
                                    //       "add_single_message_from_messge_id:add_message() pk=" + m.tox_friendpubkey);

                                    MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                    MessagePanel.add_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                            Log.i(TAG, "add_single_message_from_messge_id:EE1:" + e2.getMessage());
                        }
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "add_single_message_from_messge_id:EE2:" + e.getMessage());
        }
    }

    static Message get_message_from_db(long m_id)
    {
        Message m = null;
        Statement statement = null;

        try
        {
            statement = sqldb.createStatement();
            final String sql = "select * from Message where id='" + m_id + "'";
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next())
            {
                m = new Message();
                m.text = rs.getString("text");
                m.tox_friendpubkey = rs.getString("tox_friendpubkey");
                m.direction = rs.getInt("direction");
                m.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                m.sent_timestamp = rs.getLong("sent_timestamp");
                // Log.i(TAG, "m=" + m);
                // TODO: read all fields
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return m;
    }

    static void update_message_in_db_messageid(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    message_id(m.message_id).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    synchronized static void update_message_in_db_no_read_recvedts(final Message m)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateMessage().
                            idEq(m.id).
                            text(m.text).
                            sent_timestamp(m.sent_timestamp).
                            msg_version(m.msg_version).
                            filename_fullpath(m.filename_fullpath).
                            raw_msgv2_bytes(m.raw_msgv2_bytes).
                            msg_id_hash(m.msg_id_hash).
                            execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    static void update_message_in_db_resend_count(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    resend_count(m.resend_count).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void update_single_message(Message m, boolean force)
    {
        try
        {
            if ((current_pk != null) && (current_pk.equals(m.tox_friendpubkey)))
            {
                if ((force) ||
                    (MainActivity.update_all_messages_global_timestamp + MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                     System.currentTimeMillis()))
                {
                    Log.i(TAG, "update_single_message:friend:008");

                    MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                    modify_message(m);
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static void add_single_conference_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (!MessagePanelConferences.get_current_conf_id().equals("-1"))
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                ConferenceMessage m = orma.selectFromConferenceMessage().idEq(
                                        message_id).orderByIdDesc().toList().get(0);

                                if (m.id != -1)
                                {
                                    if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                    MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                    System.currentTimeMillis()))
                                    {
                                        MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MessagePanelConferences.add_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    static void update_message_in_db_read_rcvd_timestamp_rawmsgbytes(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    read(m.read).
                    raw_msgv2_bytes(m.raw_msgv2_bytes).
                    rcvd_timestamp(m.rcvd_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void update_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    if (message_id != -1)
                    {
                        try
                        {
                            Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().toList().get(0);

                            if (m.id != -1)
                            {
                                if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                                MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS <
                                                System.currentTimeMillis()))
                                {
                                    MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                    modify_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                        }
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            List<Message> m = orma.selectFromMessage().
                    filetransfer_idEq(filetransfer_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().toList();

            if (m.size() == 0)
            {
                return -1;
            }

            return m.get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().toList().get(0).id;

            update_message_in_db_filename_fullpath_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).toList().
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            orma.updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().toList().get(0).id;
            // Log.i(TAG,
            //       "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_state_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    toList().get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            orma.updateMessage().idEq(message_id).state(state).execute();
            // Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_queueing_from_id(long message_id, boolean ft_outgoing_queued)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_queued(ft_outgoing_queued).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_queueing_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id;
            // Log.i(TAG,
            //       "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_filedb_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(HelperFriend.tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().toList().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long message_id, long filedb_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).filedb_id(filedb_id).execute();
            // Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    public static void update_single_message_from_ftid(final long filetransfer_id, final boolean force)
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Message m = orma.selectFromMessage().
                                filetransfer_idEq(filetransfer_id).
                                orderByIdDesc().toList().get(0);

                        if (m.id != -1)
                        {
                            if ((force) || (MainActivity.update_all_messages_global_timestamp +
                                            MainActivity.UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                            {
                                MainActivity.update_all_messages_global_timestamp = System.currentTimeMillis();
                                modify_message(m);
                            }
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static void set_message_start_sending_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_sending_from_id:EE:" + e.getMessage());
        }
    }
}
