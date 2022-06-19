package com.zoffcc.applications.trifa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class GroupMessageTableModel extends AbstractTableModel
{
    private List<GroupMessage> group_messages = new ArrayList<>();
    private Vector group_listeners = new Vector();

    public void addElement(GroupMessage m)
    {
        int index = group_messages.size();
        group_messages.add(m);

        TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS,
                                                TableModelEvent.INSERT);

        for (int i = 0, n = group_listeners.size(); i < n; i++)
        {
            ((TableModelListener) group_listeners.get(i)).tableChanged(e);
        }
    }

    public int getColumnCount()
    {
        return 1;
    }

    public int getRowCount()
    {
        return group_messages.size();
    }

    public Iterator<GroupMessage> elements()
    {
        return group_messages.iterator();
    }

    public String getColumnName(int column)
    {
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return group_messages.get(rowIndex);
    }

    public Class getColumnClass(int columnIndex)
    {
        return JPanel.class;
    }

    public void addTableModelListener(TableModelListener l)
    {
        group_listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        group_listeners.remove(l);
    }


    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
    }

    public void removeAllElements()
    {
        group_messages.clear();
    }

    public int indexOf(GroupMessage msg)
    {
        return group_messages.indexOf(msg);
    }

    public void set(int pos, GroupMessage cm)
    {
        group_messages.set(pos, cm);
        TableModelEvent e = new TableModelEvent(this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);

        for (int i = 0, n = group_listeners.size(); i < n; i++)
        {
            ((TableModelListener) group_listeners.get(i)).tableChanged(e);
        }
    }
}
