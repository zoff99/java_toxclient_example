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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TrifaSetPatternActivity
{
    private static final String TAG = "trifa.TrifaSetPattrnAcy";

    public static String filter_out_specials_from_filepath(String path)
    {
        try
        {
            // TODO: be less strict here, but really actually test it then!
            // update: just to be safe, do NOT allow more than 1 "." in a row
            return path.replaceAll("[^a-zA-Z0-9_.]", "_").replaceAll("\\.+",".");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return path;
        }
    }

    public static String filter_out_specials(String in)
    {
        try
        {
            return in.replaceAll("[^a-zA-Z0-9]", "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return in;
        }
    }

    public static String bytesToString(byte[] bytes)
    {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] StringToBytes2(String in)
    {
        try
        {
            return in.getBytes(Charset.forName("UTF-8"));
        }
        catch (Exception e)
        {
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            e.printStackTrace();
            return null;
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
            // TODO: fix me!!!!!
        }
    }

    public static byte[] sha256(byte[] input)
    {
        try
        {
            return MessageDigest.getInstance("SHA-256").digest(input);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
}
