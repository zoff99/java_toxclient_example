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

import static com.zoffcc.applications.trifa.MainActivity.sqldb;
import static com.zoffcc.applications.trifa.OrmaDatabase.b;
import static com.zoffcc.applications.trifa.OrmaDatabase.s;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;

@Table
public class ConferenceMessage
{
    private static final String TAG = "DB.ConferenceMessage";

    @PrimaryKey(autoincrement = true, auto = true)
    long id; // uniqe message id!!

    @Column(indexed = true, helpers = Column.Helpers.ALL, defaultExpr = "")
    @Nullable
    String message_id_tox = ""; // Tox Group Message_ID
    // this rolls over at UINT32_MAX
    // its unique for "tox_peerpubkey + message_id_tox"
    // it only increases (until it rolls over) but may increase by more than 1

    @Column(indexed = true, defaultExpr = "-1", helpers = Column.Helpers.ALL)
    String conference_identifier = "-1"; // f_key -> ConferenceDB.conference_identifier

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String tox_peerpubkey;

    @Column(indexed = true, defaultExpr = "", helpers = Column.Helpers.ALL)
    @Nullable
    String tox_peername = ""; // saved for backup, when conference is offline!

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    int direction = 0; // 0 -> msg received, 1 -> msg sent

    @Column(indexed = true)
    int TOX_MESSAGE_TYPE = 0; // 0 -> normal, 1 -> action

    @Column(indexed = true, defaultExpr = "0")
    int TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    long sent_timestamp = 0L;

    @Column(indexed = true)
    @Nullable
    long rcvd_timestamp = 0L;

    @Column(helpers = Column.Helpers.ALL)
    boolean read = false;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean is_new = true;

    @Column(helpers = Column.Helpers.ALL)
    @Nullable
    String text = null;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    @Nullable
    boolean was_synced = false;

    static ConferenceMessage deep_copy(ConferenceMessage in)
    {
        ConferenceMessage out = new ConferenceMessage();
        out.id = in.id; // TODO: is this a good idea???
        out.message_id_tox = in.message_id_tox;
        out.tox_peerpubkey = in.tox_peerpubkey;
        out.direction = in.direction;
        out.TOX_MESSAGE_TYPE = in.TOX_MESSAGE_TYPE;
        out.TRIFA_MESSAGE_TYPE = in.TRIFA_MESSAGE_TYPE;
        out.sent_timestamp = in.sent_timestamp;
        out.rcvd_timestamp = in.rcvd_timestamp;
        out.read = in.read;
        out.is_new = in.is_new;
        out.text = in.text;
        out.tox_peername = in.tox_peername;
        out.was_synced = in.was_synced;

        return out;
    }

    @Override
    public String toString()
    {
        return "id=" + id + ", message_id_tox=" + message_id_tox + ", tox_peername=" + tox_peername +
               ", tox_peerpubkey=" + "*tox_peerpubkey*" + ", direction=" + direction + ", TRIFA_MESSAGE_TYPE=" +
               TRIFA_MESSAGE_TYPE + ", TOX_MESSAGE_TYPE=" + TOX_MESSAGE_TYPE + ", sent_timestamp=" + sent_timestamp +
               ", rcvd_timestamp=" + rcvd_timestamp + ", read=" + read + ", text=" + "xxxxxx" + ", is_new=" + is_new +
               ", was_synced=" + was_synced;
    }

    String sql_start = "";
    String sql_set = "";
    String sql_where = "where 1=1 "; // where
    String sql_orderby = ""; // order by
    String sql_limit = ""; // limit

    public ConferenceMessage conference_identifierEq(String current_conf_id)
    {
        this.sql_where = this.sql_where + " and conference_identifier='" + s(current_conf_id) + "' ";
        return this;
    }

    public ConferenceMessage is_new(boolean b)
    {
        this.sql_where = this.sql_where + " and is_new='" + b(b) + "' ";
        return this;
    }

    public ConferenceMessage tox_peerpubkeyNotEq(String trifaSystemMessagePeerPubkey)
    {
        this.sql_where = this.sql_where + " and tox_peerpubkey<>'" + s(trifaSystemMessagePeerPubkey) + "' ";
        return this;
    }

    public ConferenceMessage orderBySent_timestampAsc()
    {
        if (this.sql_orderby.equals(""))
        {
            this.sql_orderby = " order by ";
        }
        else
        {
            this.sql_orderby = this.sql_orderby + " , ";
        }
        this.sql_orderby = this.sql_orderby + " Sent_timestamp ASC ";
        return this;
    }

    public List<ConferenceMessage> toList()
    {
        List<ConferenceMessage> list = null;

        try
        {
            Statement statement = sqldb.createStatement();
            ResultSet rs = statement.executeQuery(
                    this.sql_start + " " + this.sql_where + " " + this.sql_orderby + " " + this.sql_limit);
            while (rs.next())
            {
                ConferenceMessage out = new ConferenceMessage();

                out.id = rs.getLong("id");
                out.message_id_tox = rs.getString("message_id_tox");
                out.tox_peerpubkey = rs.getString("tox_peerpubkey");
                out.direction = rs.getInt("direction");
                out.TOX_MESSAGE_TYPE = rs.getInt("TOX_MESSAGE_TYPE");
                out.TRIFA_MESSAGE_TYPE = rs.getInt("TRIFA_MESSAGE_TYPE");
                out.sent_timestamp = rs.getLong("sent_timestamp");
                out.rcvd_timestamp = rs.getLong("rcvd_timestamp");
                out.read = rs.getBoolean("read");
                out.is_new = rs.getBoolean("is_new");
                out.text = rs.getString("text");
                out.tox_peername = rs.getString("tox_peername");
                out.was_synced =rs.getBoolean("was_synced");

                if (list == null)
                {
                    list = new ArrayList<ConferenceMessage>();
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

}
