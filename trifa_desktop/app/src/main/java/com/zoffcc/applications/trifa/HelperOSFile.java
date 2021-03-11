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
import java.io.File;

public class HelperOSFile
{
    private static final String TAG = "trifa.Hlp.HelperOSFile";

    public static void run_file(String filename_with_path)
    {
        Log.i(TAG, "run_file:OS:" + OperatingSystem.getCurrent());

        if (OperatingSystem.getCurrent() == OperatingSystem.WINDOWS)
        {
            Log.i(TAG, "run_file:OS:WINDOWS");
            try
            {
                Desktop.getDesktop().open(new File(filename_with_path.replace("/", "\\")));
                return;
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
                // Runtime.getRuntime().exec("xdg-open " + filename_with_path);
                return;
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
                // Runtime.getRuntime().exec("xdg-open " + filename_with_path);
                return;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
