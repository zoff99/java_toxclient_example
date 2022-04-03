package com.zoffcc.applications.trifa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class ConferenceMessageTableModel extends AbstractTableModel
{
    private List<ConferenceMessage> conf_messages = new ArrayList<>();
    private Vector conf_listeners = new Vector();

    public void addElement(ConferenceMessage m)
    {
        int index = conf_messages.size();
        conf_messages.add(m);

        TableModelEvent e = new TableModelEvent(this, index, index, TableModelEvent.ALL_COLUMNS,
                                                TableModelEvent.INSERT);

        for (int i = 0, n = conf_listeners.size(); i < n; i++)
        {
            ((TableModelListener) conf_listeners.get(i)).tableChanged(e);
        }
    }

    public int getColumnCount()
    {
        return 1;
    }

    public int getRowCount()
    {
        return conf_messages.size();
    }

    public Iterator<ConferenceMessage> elements()
    {
        return conf_messages.iterator();
    }

    public String getColumnName(int column)
    {
        return "";
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        return conf_messages.get(rowIndex);
    }

    public Class getColumnClass(int columnIndex)
    {
        return JPanel.class;
    }

    public void addTableModelListener(TableModelListener l)
    {
        conf_listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        conf_listeners.remove(l);
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
        conf_messages.clear();
    }

    public int indexOf(ConferenceMessage msg)
    {
        return conf_messages.indexOf(msg);
    }

    public void set(int pos, ConferenceMessage cm)
    {
        conf_messages.set(pos, cm);
        TableModelEvent e = new TableModelEvent(this, pos, pos, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);

        for (int i = 0, n = conf_listeners.size(); i < n; i++)
        {
            ((TableModelListener) conf_listeners.get(i)).tableChanged(e);
        }
    }
}
