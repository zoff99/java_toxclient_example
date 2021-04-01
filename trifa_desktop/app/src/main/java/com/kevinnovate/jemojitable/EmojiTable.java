
package com.kevinnovate.jemojitable;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Creates a table to display Emojis with the user specified number of columns and font. Optionally, prune the 
 * emojis that don't render properly with the built-in font
 * 
 * @author locutus
 */
public class EmojiTable extends JTable {
    
    /**
     * Listener that receives a callback when the user double-clicks an emoji from the table
     */
    public interface EmojiSelectListener {
        
        /**
         * User double-clicked on an emoji in the table
         * @param e the Emoji the user clicked
         */
        public void userSelectedEmoji(Emoji e);
    }
    

    //Collection of all the Emoji objects from com.vdurmont.emoji.Emoji
    private static final Collection<Emoji> allEmojis = EmojiManager.getAll();
  

    //The default model extended to specify the column count and the column class.  Cells are not editable
    private static class EmojiTableModel extends DefaultTableModel {
       
        @SuppressWarnings("unchecked")
        public EmojiTableModel(int columnCount) {
            super(0, columnCount);
            fireTableStructureChanged();
        }
        @Override
        public Class getColumnClass(int column) {  //Use the EmojiColumnElement as the class for all cells
            return EmojiColumnElement.class;
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int mColIndex) {
            return false;
        }
    
    }
    
    
    //Holder class for all cells that holds the Emoji
    private static class EmojiColumnElement {
        
        protected final Emoji e;

        public EmojiColumnElement(Emoji e) {
            this.e = e;
        }

        @Override
        public String toString() {  //Return the unicode representation to render the emoji in the cell
            return e.getUnicode();
        }

    }
    
    //Custom render class for the EmojiColumnElement
    private class EmojiCellRenderer extends DefaultTableCellRenderer {
        
        private final Font font;
        
        public EmojiCellRenderer(Font font) {
            this.font = font;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            //Cells are by default rendered as a JLabel.
            JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setFont(font);
            label.setHorizontalAlignment(CENTER);
            label.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            label.setForeground(Color.BLACK);
            label.setBorder(null);
            
            if (value != null) {  //Set the tooltip to the description of the emoji
                Emoji e = ((EmojiColumnElement)value).e;
                setToolTipText(e.getDescription());
            }
            else 
                setToolTipText(null);
                            

            //Return the JLabel which renders the cell.
            return label;

        }
    }
    
    public Emoji getSelectedEmoji() {
         
        int row = getSelectedRow();
        int col = getSelectedColumn();
        
        if (row < 0 || col < 0)
            return null;
        
              
        int rowVal = convertRowIndexToModel(row);
        int colVal = convertColumnIndexToModel(col);

        EmojiTableModel model = (EmojiTableModel)getModel();
        
        EmojiColumnElement ece = (EmojiColumnElement)model.getValueAt(rowVal, colVal);
        return ece.e;
    }
    
    
    private EmojiSelectListener listener = null;
    
    
    /**
     * Set the double-click listener for this table. 
     * @param listener the listener to receive double-click selection events, if null, removes the listener
     */
    public void setDoubleClickListener(EmojiSelectListener listener) {
         this.listener = listener;
    }
    
    
    /**
     * Create a JTable for displaying Emojis. 
     * @param columnCount the number of columns wide for the table
     * @param font the font to use (should be a font capable of rendering unicode emojis)
     * @param pruneRegionalIndicator many fonts don't render emojis with the description staring with "regional indicator".  If set, do not insert these emojis in the table
     */
    public EmojiTable(int columnCount, Font font, boolean pruneRegionalIndicator) {
        super(new EmojiTableModel(columnCount > allEmojis.size() ? allEmojis.size() : columnCount));  //Create a model with columns that's a min of column count or num emojis
        
       
        this.setDoubleBuffered(true);
        setDoubleBuffered(true);
        setTableHeader(null);  //remove the header column
        EmojiCellRenderer renderer = new EmojiCellRenderer(font);
        setDefaultRenderer(EmojiColumnElement.class, renderer);
        
        
        setCellSelectionEnabled(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        EmojiTableModel model = (EmojiTableModel)getModel();
        
        
        //Add all the emojis to the table
        Iterator<Emoji> it = allEmojis.iterator();
        int colCount = 0;
        EmojiColumnElement[] columns = null;
        
        while (it.hasNext()) {

            if (colCount == 0) //Create a column of emojis
                columns = new EmojiColumnElement[model.getColumnCount()];
            
           
            Emoji e = it.next();  
            
            //Optionally, do not insert emojis with the regional indicator
            if (pruneRegionalIndicator && e.getDescription().startsWith("regional indicator symbol"))
                continue;
   
            columns[colCount] = new EmojiColumnElement(e);    
            colCount++;    
            
            if (colCount == 8) {
                model.insertRow(model.getRowCount(), columns);
                colCount = 0;
                columns = null;
            }
        }
        
        if (columns != null)
            model.insertRow(model.getRowCount(), columns);
 
         
        
        model.fireTableDataChanged();
        revalidate();
        
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                
                if (me.getClickCount() == 2) {
                    if (listener != null)
                        listener.userSelectedEmoji(getSelectedEmoji());
                }
            }
        });

    }
    
    
}
