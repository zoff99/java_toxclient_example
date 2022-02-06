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

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import static com.zoffcc.applications.trifa.TrifaSetPatternActivity.bytesToString;

public class HelperOSFile
{
    private static final String TAG = "trifa.Hlp.HelperOSFile";

    public static void show_containing_dir_in_explorer(final String filename_with_path)
    {
        try
        {
            String containing_dir = new File(filename_with_path).getParent();
            if (containing_dir != null)
            {
                if (!containing_dir.equals(""))
                {
                    File c_dir = new File(containing_dir);
                    if (c_dir.isDirectory())
                    {
                        Desktop.getDesktop().open(c_dir);
                    }
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

    public static void show_file_in_explorer(String filename_with_path)
    {
        if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            final String filename_for_windows = filename_with_path.replace("/", "\\");
            try
            {
                Desktop.getDesktop().browseFileDirectory(new File(filename_for_windows));
            }
            catch (Exception e)
            {
                show_containing_dir_in_explorer(filename_for_windows);
            }
        }
        else if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
        {
            try
            {
                Desktop.getDesktop().browseFileDirectory(new File(filename_with_path));
            }
            catch (Exception e)
            {
                show_containing_dir_in_explorer(filename_with_path);
            }
        }
        else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            try
            {
                Desktop.getDesktop().browseFileDirectory(new File(filename_with_path));
            }
            catch (Exception e)
            {
                show_containing_dir_in_explorer(filename_with_path);
            }
        }
    }

    public static void run_file(String filename_with_path)
    {
        Log.i(TAG, "run_file:OS:" + OperatingSystem.getCurrent());

        if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            Log.i(TAG, "run_file:OS:WINDOWS");
            try
            {
                Desktop.getDesktop().open(new File(filename_with_path.replace("/", "\\")));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (OperatingSystem.getCurrent() == OperatingSystem.LINUX)
        {
            Log.i(TAG, "run_file:OS:LINUX");
            try
            {
                Desktop.getDesktop().open(new File(filename_with_path));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (OperatingSystem.getCurrent() == OperatingSystem.MACOS)
        {
            Log.i(TAG, "run_file:OS:MACOS");
            try
            {
                Desktop.getDesktop().open(new File(filename_with_path));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String sha256sum_of_file(String filename_with_path)
    {
        try
        {
            byte[] buffer = new byte[8192];
            int count;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename_with_path));
            while ((count = bis.read(buffer)) > 0)
            {
                digest.update(buffer, 0, count);
            }
            bis.close();

            byte[] hash = digest.digest();
            return (bytesToString(hash));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
