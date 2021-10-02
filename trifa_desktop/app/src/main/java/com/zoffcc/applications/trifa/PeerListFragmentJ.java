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

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class PeerListFragmentJ extends JPanel
{
    private static final String TAG = "trifa.PeerListFrgnt";

    private static JList<PeerModel> peer_list;
    static DefaultListModel<PeerModel> peer_list_model;
    JScrollPane FriendScrollPane;

    public PeerListFragmentJ()
    {
        peer_list_model = new DefaultListModel<>();


        peer_list = new JList<>();
        peer_list.setModel(peer_list_model);
        peer_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        peer_list.setSelectedIndex(0);
        peer_list.setCellRenderer(new Renderer_ConfPeerList());

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        FriendScrollPane = new JScrollPane();
        add(FriendScrollPane);
        FriendScrollPane.setViewportView(peer_list);

        add_all_peers_clear(1);
    }

    synchronized static void add_all_peers_clear(final int delay)
    {
    }
}
