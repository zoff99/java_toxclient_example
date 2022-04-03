package com.zoffcc.applications.trifa;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class PanelCellEditorRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
{
    private static final long serialVersionUID = 1L;

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent)
    {
        return false;
    }

    @Override
    public Component getTableCellEditorComponent(JTable jTable, Object o, boolean b, int i, int i1)
    {
        return null;
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1)
    {
        return null;
    }
}
