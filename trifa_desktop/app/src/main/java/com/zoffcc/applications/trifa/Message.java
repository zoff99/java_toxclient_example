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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import static com.zoffcc.applications.trifa.HelperGeneric.get_last_rowid;
import static com.zoffcc.applications.trifa.MainActivity.ORMA_TRACE;
import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.OrmaDatabase.b;
import static com.zoffcc.applications.trifa.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;

@Table
public class Message
{
    private static final String TAG = "DB.Message";

    @PrimaryKey(autoincrement = true, auto = true)
    long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long message_id = -1; // ID given from toxcore!!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_friendpubkey;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(indexed = true, defaultExpr = "1", helpers = Column.Helpers.ALL)
    int state = TOX_FILE_CONTROL_PAUSE.value;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_accepted = false;

    @Column(indexed = true, defaultExpr = "false", helpers = Column.Helpers.ALL)
    boolean ft_outgoing_started = false;

    @Column(indexed = true, defaultExpr = "-1")
    long filedb_id; // f_key -> FileDB.id

    @Column(indexed = true, defaultExpr = "-1")
    long filetransfer_id; // f_key -> Filetransfer.id

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    long sent_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL, defaultExpr = "0")
    @Nullable
    long sent_timestamp_ms = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp = 0L;

    @Column(indexed = true, defaultExpr = "0")
    @Nullable
    long rcvd_timestamp_ms = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(indexed = true, defaultExpr = "0", helpers = Column.Helpers.ALL)
    int send_retries = 0;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean is_new = true;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String filename_fullpath = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String msg_id_hash = null; // 32byte hash, used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    String raw_msgv2_bytes = null; // used for MessageV2 Messages! and otherwise NULL

    @Column(indexed = true, defaultExpr = "0")
    int msg_version; // 0 -> old Message, 1 -> for MessageV2 Message

    @Column(indexed = true, defaultExpr = "2")
    int resend_count; // 2 -> do not resend msg anymore, 0 or 1 -> resend count

    // ------- SWING UI elements ------- //
    JButton _swing_ok = null;
    JButton _swing_cancel = null;
    // ------- SWING UI elements ------- //

    static Message deep_copy(Message in)
    {
        Message out = new Message();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id = in.message_id;
        out.tox_friendpubkey = in.tox_friendpubkey;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.state = in.state;
        out.ft_accepted = in.ft_accepted;
        out.ft_outgoing_started = in.ft_outgoing_started;
        out.filedb_id = in.filedb_id;
        out.filetransfer_id = in.filetransfer_id;
        out.sent_timestamp = in.sent_timestamp;
        out.sent_timestamp_ms = in.sent_timestamp_ms;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.rcvd_timestamp_ms = in.rcvd_timestamp_ms;
        out.read = in.read;
        out.send_retries = in.send_retries;
        out.is_new = in.is_new;
        out.text = in.text;
        out.filename_fullpath = in.filename_fullpath;
        out.msg_id_hash = in.msg_id_hash;
        out.msg_version = in.msg_version;
        out.raw_msgv2_bytes = in.raw_msgv2_bytes;
        out.resend_count = in.resend_count;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id=" + message_id + ", filetransfer_id=" + filetransfer_id + ", filedb_id=" +
               filedb_id + ", tox_friendpubkey=" + "*pubkey*" + ", direction=" + direction + ", state=" + state +
               ", TRIFA_MESSAGE_TYPE=" + TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE +
               ", sent_timestamp=" + sent_timestamp + ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read +
               ", send_retries=" + send_retries + ", text=" + "xxxxxx" + ", filename_fullpath=" + filename_fullpath +
               ", is_new=" + is_new + ", msg_id_hash=" + msg_id_hash + ", msg_version=" + msg_version +
               ", resend_count=" + resend_count + ", raw_msgv2_bytes=" + "xxxxxx";
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public List<Message> toList()
    {
        List<Message> list = null;

        try
        {
            Statement statement = sqldb.createStatement();

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next())
            {
                Message out = new Message();

                out.id = rs.getLong("id");
                out.message_id = rs.getLong("message_id");
                out.tox_friendpubkey = rs.getString("tox_friendpubkey");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.state = rs.getInt("state");
                out.ft_accepted = rs.getBoolean("ft_accepted");
                out.ft_outgoing_started = rs.getBoolean("ft_outgoing_started");
                out.filedb_id = rs.getLong("filedb_id");
                out.filetransfer_id = rs.getLong("filetransfer_id");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.sent_timestamp_ms = rs.getLong("sent_timestamp_ms");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.rcvd_timestamp_ms = rs.getLong("rcvd_timestamp_ms");
                out.read = rs.getBoolean("read");
                out.send_retries = rs.getInt("send_retries");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.filename_fullpath = rs.getString("filename_fullpath");
                out.msg_id_hash = rs.getString("msg_id_hash");
                out.msg_version = rs.getInt("msg_version");
                out.raw_msgv2_bytes = rs.getString("raw_msgv2_bytes");
                out.resend_count = rs.getInt("resend_count");

                if (list == null)
                {
                    list = new ArrayList<Message>();
                }
                list.add(out);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return list;
    }

    public long insert()
    {
        long ret = -1;

        try
        {
            // @formatter:off
            Statement statement = sqldb.createStatement();

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
                                 "'"+s(""+this.message_id)+"'," +
                                 "'"+s(this.tox_friendpubkey)+"'," +
                                 "'"+s(""+this.direction)+"'," +
                                 "'"+s(""+this.TOX_MESSAGE_TYPE)+"'," +
                                 "'"+s(""+this.TRIFA_MESSAGE_TYPE)+"'," +
                                 "'"+s(""+this.state)+"'," +
                                 "'"+b(this.ft_accepted)+"'," +
                                 "'"+b(this.ft_outgoing_started)+"'," +
                                 "'"+s(""+this.filedb_id)+"'," +
                                 "'"+s(""+this.filetransfer_id)+"'," +
                                 "'"+s(""+this.sent_timestamp)+"'," +
                                 "'"+s(""+this.sent_timestamp_ms)+"'," +
                                 "'"+s(""+this.rcvd_timestamp)+"'," +
                                 "'"+s(""+this.rcvd_timestamp_ms)+"'," +
                                 "'"+b(this.read)+"'," +
                                 "'"+s(""+this.send_retries)+"'," +
                                 "'"+b(this.is_new)+"'," +
                                 "'"+s(this.text)+"'," +
                                 "'"+s(this.filename_fullpath)+"'," +
                                 "'"+s(this.msg_id_hash)+"'," +
                                 "'"+s(""+this.msg_version)+"'," +
                                 "'"+s(this.raw_msgv2_bytes)+"'," +
                                 "'"+s(""+this.resend_count)+"'" +
                                 ")";

            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql_str);
            }

            statement.execute(sql_str);
            ret = get_last_rowid(statement);
            // @formatter:on

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public Message tox_friendpubkeyEq(String tox_friendpubkey)
    {
        this.sql_where = this.sql_where + " and tox_friendpubkey='" + s(tox_friendpubkey) + "' ";
        return this;
    }

    public Message orderBySent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp ASC ";
        return this;
    }

    public Message orderBySent_timestamp_msAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " sent_timestamp_ms ASC ";
        return this;
    }

    public Message directionEq(int i)
    {
        this.sql_where = this.sql_where + " and direction='" + s(i) + "' ";
        return this;
    }

    public Message TRIFA_MESSAGE_TYPEEq(int value)
    {
        this.sql_where = this.sql_where + " and TRIFA_MESSAGE_TYPE='" + s(value) + "' ";
        return this;
    }

    public Message resend_countEq(int i)
    {
        this.sql_where = this.sql_where + " and resend_count='" + s(i) + "' ";
        return this;
    }

    public Message readEq(boolean b)
    {
        this.sql_where = this.sql_where + " and read='" + b(b) + "' ";
        return this;
    }

    public Message idEq(long id)
    {
        this.sql_where = this.sql_where + " and id='" + s(id) + "' ";
        return this;
    }

    public Message message_id(long message_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " message_id='" + s(message_id) + "' ";
        return this;
    }

    public void execute()
    {
        try
        {
            Statement statement = sqldb.createStatement();
            final String sql = this.sql_start + " " + this.sql_set + " " + this.sql_where;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }
            statement.executeUpdate(sql);
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
            Log.i(TAG, "EE1:" + e2.getMessage());
        }
    }

    public Message text(String text)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " text='" + s(text) + "' ";
        return this;
    }

    public Message sent_timestamp(long sent_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " sent_timestamp='" + s(sent_timestamp) + "' ";
        return this;
    }

    public Message msg_version(int msg_version)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_version='" + s(msg_version) + "' ";
        return this;
    }

    public Message filename_fullpath(String filename_fullpath)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filename_fullpath='" + s(filename_fullpath) + "' ";
        return this;
    }

    public Message raw_msgv2_bytes(String raw_msgv2_bytes)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " raw_msgv2_bytes='" + s(raw_msgv2_bytes) + "' ";
        return this;
    }

    public Message msg_id_hash(String msg_id_hash)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " msg_id_hash='" + s(msg_id_hash) + "' ";
        return this;
    }

    public Message resend_count(int resend_count)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " resend_count='" + s(resend_count) + "' ";
        return this;
    }

    public Message msg_versionEq(int msg_version)
    {
        this.sql_where = this.sql_where + " and  msg_version='" + s(msg_version) + "' ";
        return this;
    }

    public Message message_idEq(long message_id)
    {
        this.sql_where = this.sql_where + " and  message_id='" + s(message_id) + "' ";
        return this;
    }

    public Message orderByIdDesc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " id DESC ";
        return this;
    }

    public Message read(boolean read)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " read='" + b(read) + "' ";
        return this;
    }

    public Message rcvd_timestamp(long rcvd_timestamp)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " rcvd_timestamp='" + s(rcvd_timestamp) + "' ";
        return this;
    }

    public Message msg_id_hashEq(String msg_id_hash)
    {
        this.sql_where = this.sql_where + " and  msg_id_hash='" + s(msg_id_hash) + "' ";
        return this;
    }

    public Message ft_accepted(boolean ft_accepted)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_accepted='" + b(ft_accepted) + "' ";
        return this;
    }

    public Message state(int state)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " state='" + s(state) + "' ";
        return this;
    }

    public Message filetransfer_idEq(long filetransfer_id)
    {
        this.sql_where = this.sql_where + " and  filetransfer_id='" + s(filetransfer_id) + "' ";
        return this;
    }

    public Message filedb_id(long filedb_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filedb_id='" + s(filedb_id) + "' ";
        return this;
    }

    public Message filetransfer_id(long filetransfer_id)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " filetransfer_id='" + s(filetransfer_id) + "' ";
        return this;
    }

    public int count()
    {
        int ret = 0;

        try
        {
            Statement statement = sqldb.createStatement();
            this.sql_start = "SELECT count(*) as count FROM Message";

            final String sql = this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit;
            if (ORMA_TRACE)
            {
                Log.i(TAG, "sql=" + sql);
            }

            ResultSet rs = statement.executeQuery(sql);

            if (rs.next())
            {
                ret = rs.getInt("count");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public Message ft_outgoing_started(boolean ft_outgoing_started)
    {
        if (this.sql_set.equals(""))
        {
            this.sql_set = " set ";
        }
        else
        {
            this.sql_set = this.sql_set + " , ";
        }
        this.sql_set = this.sql_set + " ft_outgoing_started='" + b(ft_outgoing_started) + "' ";
        return this;
    }
}
