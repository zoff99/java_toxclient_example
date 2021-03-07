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

import static com.zoffcc.applications.trifa.HelperGeneric.get_last_rowid;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanel;
import static com.zoffcc.applications.trifa.MainActivity.MessagePanelConferences;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.current_pk;
import static com.zoffcc.applications.trifa.MessageListFragmentJ.modify_message;
import static com.zoffcc.applications.trifa.OrmaDatabase.b;
import static com.zoffcc.applications.trifa.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;

public class HelperMessage
{
    private static final String TAG = "trifa.Hlp.Message";

    static long insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        Statement statement = null;
        long row_id = -1;

        try
        {
            // @formatter:off
            statement = sqldb.createStatement();
            final String sql_str="insert into Message" +
                              "(" +
                              "message_id," +
                              "tox_friendpubkey," +
                              "direction," +
                              "TOX_MESSAGE_TYPE," +
                              "TRIFA_MESSAGE_TYPE," +
                              "state," +
                              "ft_accepted," +
                              "ft_outgoing_started," +
                              "filedb_id," +
                              "filetransfer_id," +
                              "sent_timestamp," +
                              "sent_timestamp_ms," +
                              "rcvd_timestamp," +
                              "rcvd_timestamp_ms," +
                              "read," +
                              "send_retries," +
                              "is_new," +
                              "text," +
                              "filename_fullpath," +
                              "msg_id_hash," +
                              "msg_version," +
                              "raw_msgv2_bytes," +
                              "resend_count" +
                              ")" +
                              "values" +
                              "(" +
                              "'"+s(""+m.message_id)+"'," +
                              "'"+s(m.tox_friendpubkey)+"'," +
                              "'"+s(""+m.direction)+"'," +
                              "'"+s(""+m.TOX_MESSAGE_TYPE)+"'," +
                              "'"+s(""+m.TRIFA_MESSAGE_TYPE)+"'," +
                              "'"+s(""+m.state)+"'," +
                              "'"+b(m.ft_accepted)+"'," +
                              "'"+b(m.ft_outgoing_started)+"'," +
                              "'"+s(""+m.filedb_id)+"'," +
                              "'"+s(""+m.filedb_id)+"'," +
                              "'"+s(""+m.sent_timestamp)+"'," +
                              "'"+s(""+m.sent_timestamp_ms)+"'," +
                              "'"+s(""+m.rcvd_timestamp)+"'," +
                              "'"+s(""+m.rcvd_timestamp_ms)+"'," +
                              "'"+b(m.read)+"'," +
                              "'"+s(""+m.send_retries)+"'," +
                              "'"+b(m.is_new)+"'," +
                              "'"+s(m.text)+"'," +
                              "'"+s(m.filename_fullpath)+"'," +
                              "'"+s(m.msg_id_hash)+"'," +
                              "'"+s(""+m.msg_version)+"'," +
                              "'"+s(m.raw_msgv2_bytes)+"'," +
                              "'"+s(""+m.resend_count)+"'" +
                              ")";

            //  Log.i(TAG, "sql="+ sql_str);

            statement.execute(sql_str);
            row_id = get_last_rowid(statement);
            // @formatter:on

            //  Log.i(TAG, "row_id=" + row_id + ":" + sql_str);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            long msg_id = -1;

            try
            {
                ResultSet rs = statement.executeQuery("SELECT id FROM Message where rowid='" + row_id + "'");
                if (rs.next())
                {
                    msg_id = rs.getLong("id");
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
                            Message m = get_message_from_db(message_id);

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
            // Log.i(TAG, "sql=" + sql);
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
}
