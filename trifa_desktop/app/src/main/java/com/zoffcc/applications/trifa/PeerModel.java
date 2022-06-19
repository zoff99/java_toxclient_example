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

import static com.zoffcc.applications.trifa.CombinedFriendsAndConferences.COMBINED_IS_CONFERENCE;

public class PeerModel
{
    String pubkey = "-1";
    String name = "";
    long peernum = -1;
    boolean offline = false;
    int type = COMBINED_IS_CONFERENCE;

    @Override
    public String toString()
    {
        return "pubkey=" + pubkey.substring(0, 5) + "*****";
    }
}
