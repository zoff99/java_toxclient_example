/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 - 2021 Zoff <zoff@zoff.cc>
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

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;

import static com.zoffcc.applications.trifa.TrifaSetPatternActivity.filter_out_specials_2;

public class HelperNotification
{
    private static final String TAG = "trifa.HelperNotification";

    static TrayIcon trayIcon = null;

    public static void displayMessage(String message)
    {
        try
        {
            String title = "TRIfA";

            String os = System.getProperty("os.name");
            if (os.contains("Linux"))
            {
                ProcessBuilder builder = new ProcessBuilder("zenity", "--notification",
                                                            "--title=" + filter_out_specials_2(title),
                                                            "--text=" + filter_out_specials_2(message));
                builder.inheritIO().start();
            }
            else if (os.contains("Mac"))
            {
                ProcessBuilder builder = new ProcessBuilder("osascript", "-e",
                                                            "display notification \"" + filter_out_specials_2(message) +
                                                            "\"" + " with title \"" + filter_out_specials_2(title) +
                                                            "\"");
                builder.inheritIO().start();
            }
            else if (SystemTray.isSupported())
            {
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void init_system_tray()
    {
        try
        {
            if (SystemTray.isSupported())
            {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().getImage("trifa_icon.png");
                trayIcon = new TrayIcon(image, "TRIfA");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
