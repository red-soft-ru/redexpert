/*
 * SQLStateCodesDockedPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.sqlstates;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.AbstractDockedTabActionPanel;
import org.executequery.gui.DefaultTable;

/**
 * Docked SQL State Codes panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SQLStateCodesDockedPanel extends AbstractDockedTabActionPanel {
    
    public static final String TITLE = "SQL State Codes";
    
    /** sql keywords */
    private List<SQLStateCode> codes;
    
    /** the table display */
    //private JTable table;
    
    /** Creates a new instance of SQLStateCodesDockedPanel */
    public SQLStateCodesDockedPanel() {
        super(new BorderLayout());
        init();
    }
    
    private void init() {
        loadStateCodes();
        
        JTable table = new DefaultTable(new StateCodesModel());
        table.setFont(new Font("Dialog", Font.PLAIN, Constants.DEFAULT_FONT_SIZE));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // init the cell renderer component
        TableColumnModel tcm = table.getColumnModel();
        for (int i = 0, n = tcm.getColumnCount(); i < n; i++) {
            tcm.getColumn(i).setCellRenderer(new SQLStateCodesCellRenderer());
        }
        
        // size the columns
        tcm.getColumn(0).setPreferredWidth(40);
        tcm.getColumn(1).setPreferredWidth(55);
        tcm.getColumn(2).setPreferredWidth(160);

        add(new JScrollPane(table));
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    }
    
    private void loadStateCodes() {
        String path = "org/executequery/sql-state.codes";
        codes = new ArrayList<SQLStateCode>();

        InputStream input = null;
        try {
            ClassLoader cl = getClass().getClassLoader();
            
            if (cl != null) {
                input = cl.getResourceAsStream(path);
            }
            else {
                input = ClassLoader.getSystemResourceAsStream(path);
            }

            int i = 0;
            StringBuffer buf = new StringBuffer();
            
            char PIPE = '|';
            char NEW_LINE = '\n';
            
            int count = 0;
            
            String stateClass = null;
            String stateSubclass = null;
            String description = null;

            try {
                while ((i = input.read()) != -1) {
                    char _char = (char)i;
                    if (_char == PIPE) {

                        // only picking first 2 values - 
                        // description picked up on line-feed
                        switch (count) {
                            case 0:
                                stateClass = buf.toString();
                                break;
                            case 1:
                                stateSubclass = buf.toString();
                                break;
                        }
                        count++;
                        buf.setLength(0);
                    }
                    else if (_char == NEW_LINE) {
                        count = 0;
                        description = buf.toString();
                        codes.add(new SQLStateCode(
                                stateClass, stateSubclass, description));
                        buf.setLength(0);
                    }
                    else {
                        buf.append(_char);
                    }
                }
            }
            catch (IOException e) {
                GUIUtilities.displayExceptionErrorDialog(
                        "Error loading SQL State Codes:\n" + e.getMessage(), e);
            }

        }
        finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {}
            }
        }
    }
    
    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    public static final String MENU_ITEM_KEY = "viewSqlStateCodes";
    
    public static final String PROPERTY_KEY = "system.display.state-codes";

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    public String toString() {
        return TITLE;
    }
    
    
    /**
     * State codes table model.
     */
    private class StateCodesModel extends AbstractTableModel {

        private String[] columnNames = {"Class", "Subclass", "Description"};
        
        public StateCodesModel() {}
        
        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        public int getRowCount() {
            if (codes == null) {
                return 0;
            }
            return codes.size();
        }
        
        public Object getValueAt(int row, int col) {
            return codes.get(row);
        }
        
        public boolean isCellEditable(int row, int col) {
            return false;
        }
        
        public Class getColumnClass(int col) {
            return String.class;
        }

    } // class StateCodesModel

}






