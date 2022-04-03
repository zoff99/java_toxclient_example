package com.zoffcc.applications.trifa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class MessageTableModel extends AbstractTableModel
{
    private List<Message> messages = new ArrayList<>();
    private Vector listeners = new Vector();

    public void addElement(Message m)
    {
        int index = messages.size();
        messages.add(m);

        TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS,
                                                TableModelEvent.INSERT);

        for (int i = 0, n = listeners.size(); i < n; i++)
        {
            ((TableModelListener) listeners.get(i)).tableChanged(e);
        }
    }

    public int getColumnCount()
    {
        return 1;
    }

    public int getRowCount()
    {
        return messages.size();
    }

    public Iterator<Message> elements()
    {
        return messages.iterator();
    }

    public String getColumnName(int column)
    {
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return messages.get(rowIndex);
    }

    public Class getColumnClass(int columnIndex)
    {
        return JPanel.class;
    }

    public void addTableModelListener(TableModelListener l)
    {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        listeners.remove(l);
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
        /*
        final int last = messages.size();
        TableModelEvent e = new TableModelEvent(this, 0, last, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);

        for (int i = 0, n = listeners.size(); i < n; i++)
        {
            ((TableModelListener) listeners.get(i)).tableChanged(e);
        }
        */
        messages.clear();
    }

    public int indexOf(Message msg)
    {
        return messages.indexOf(msg);
    }

    public void set(int pos, Message m)
    {
        messages.set(pos, m);
        TableModelEvent e = new TableModelEvent(this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);

        for (int i = 0, n = listeners.size(); i < n; i++)
        {
            ((TableModelListener) listeners.get(i)).tableChanged(e);
        }
    }
}
