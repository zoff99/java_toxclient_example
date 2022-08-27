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

import java.awt.Color;

public class ChatColors
{
    private static final String TAG = "trifa.ChatCols";

    static Color[] SystemColors = {
            new Color(0xff0000, false),
            new Color(0xaaaaaa, false),
    };

    static int get_size_systemcolors()
    {
        return SystemColors.length;
    }

    static Color[] PeerAvatarColors = {
            //
            // https://www.w3schools.com/colors/colors_picker.asp
            //
            // ** too dark ** // Color.parseColor("#0000FF"), // Blue
            // new Color(0xff33b5e5, true);
            new Color(0x6666ff, false),
            new Color(0x00FFFF, false),
            new Color(0x08000, false),
            new Color(0xdce775, false),
            new Color(0xf06292, false),
            new Color(0x42a5f5, false),
            new Color(0x808000, false),
            new Color(0x800080, false),
            new Color(0xff4d4d, false),
            new Color(0x008080, false),
            new Color(0xcccc00, false),
    };

    static int get_size()
    {
        return PeerAvatarColors.length;
    }

    static Color get_shade(Color color, String pubkey)
    {
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(0, 1)=" + pubkey.substring(0, 1));
        // Log.i(TAG, "get_shade:pubkey=" + pubkey + " pubkey.substring(1, 2)=" + pubkey.substring(1, 2));

        float factor =
                (Integer.parseInt(pubkey.substring(0, 1), 16) + (Integer.parseInt(pubkey.substring(1, 2), 16) * 16)) /
                255.0f;

        final float range = 0.5f;
        final float min_value = 1.0f - (range * 0.6f);
        factor = (factor * range) + min_value;

        return manipulateColor(color, factor);
    }

    public static Color manipulateColor(Color color, float factor)
    {
        // Log.i(TAG, "manipulateColor:color=" + color + " factor=" + factor);

        int a = color.getAlpha();
        int r = Math.round(color.getRed() * factor);
        int g = Math.round(color.getGreen() * factor);
        int b = Math.round(color.getBlue() * factor);
        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255), a);
    }
}
