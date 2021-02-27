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

@Table
public class BootstrapNodeEntryDB
{
    static final String TAG = "trifa.BtpNodeEDB";

    @PrimaryKey(autoincrement = true, auto = true)
    long id;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long num;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    boolean udp_node; // true -> UDP bootstrap node, false -> TCP relay node

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String ip;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    long port;

    @Column(indexed = true, helpers = Column.Helpers.ALL)
    String key_hex;

    @Override
    public String toString()
    {
        // return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + " key_hex=" + key_hex;
        // return "" + num + ":" + ip + " port=" + port + " udp_node="+  udp_node;
        return "" + num + ":" + ip + " port=" + port + " udp_node=" + udp_node + "\n";
    }

    static void insert_node_into_db_real(BootstrapNodeEntryDB n)
    {
        try
        {
            // orma.insertIntoBootstrapNodeEntryDB(n);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void insert_default_udp_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        n = BootstrapNodeEntryDB_(true, num_, "85.172.30.117", 33445,
                                  "8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.abilinski.com", 33445,
                                  "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.verdict.gg", 33445,
                                  "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.novg.net", 33445,
                                  "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "205.185.115.131", 53,
                                  "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(true, num_, "tox.initramfs.io", 33445,
                                  "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
        insert_node_into_db_real(n);
        num_++;
    }

    public static void insert_default_tcprelay_nodes_into_db()
    {
        BootstrapNodeEntryDB n;
        int num_ = 0;
        n = BootstrapNodeEntryDB_(false, num_, "85.172.30.117", 33445,
                                  "8E7D0B859922EF569298B4D261A8CCB5FEA14FB91ED412A7603A585A25698832");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.abilinski.com", 33445,
                                  "10C00EB250C3233E343E2AEBA07115A5C28920E9C8D29492F6D00B29049EDC7E");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.verdict.gg", 33445,
                                  "1C5293AEF2114717547B39DA8EA6F1E331E5E358B35F9B6B5F19317911C5F976");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.novg.net", 33445,
                                  "D527E5847F8330D628DAB1814F0A422F6DC9D0A300E6C357634EE2DA88C35463");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "205.185.115.131", 443,
                                  "3091C6BEB2A993F1C6300C16549FABA67098FF3D62C6D253828B531470B53D68");
        insert_node_into_db_real(n);
        num_++;
        n = BootstrapNodeEntryDB_(false, num_, "tox.initramfs.io", 3389,
                                  "3F0A45A268367C1BEA652F258C85F4A66DA76BCAA667A49E770BCC4917AB6A25");
        insert_node_into_db_real(n);
        num_++;
    }

    public static BootstrapNodeEntryDB BootstrapNodeEntryDB_(boolean udp_node_, int num_, String ip_, long port_, String key_hex_)
    {
        BootstrapNodeEntryDB n = new BootstrapNodeEntryDB();
        n.num = num_;
        n.udp_node = udp_node_;
        n.ip = ip_;
        n.port = port_;
        n.key_hex = key_hex_;

        return n;
    }

    public static String dns_lookup_via_tor(String host_or_ip)
    {
        return null;
    }

    public static void get_tcprelay_nodelist_from_db()
    {
    }

    public static void get_udp_nodelist_from_db()
    {
    }
}
