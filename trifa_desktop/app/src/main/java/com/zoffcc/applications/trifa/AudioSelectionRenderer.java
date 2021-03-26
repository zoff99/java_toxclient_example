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

import java.awt.Component;

import javax.sound.sampled.Mixer;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class AudioSelectionRenderer extends BasicComboBoxRenderer
{
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        try
        {
            Mixer.Info item = (Mixer.Info) value;

            if (index == -1)
            {
            }
            else
            {
                setText(item.getName() + " : " + item.getDescription() + " : " + item.getVendor());
                setIcon(null);
            }
        }
        catch (Exception e)
        {
            setText(" ============================================ ");
            setIcon(null);
        }
        return this;
    }
}
