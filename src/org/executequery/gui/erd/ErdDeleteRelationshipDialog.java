/*
 * ErdDeleteRelationshipDialog.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui.erd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1766 $
 * @date     $Date: 2017-08-14 23:34:37 +1000 (Mon, 14 Aug 2017) $
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ErdDeleteRelationshipDialog extends ErdPrintableDialog
                                         implements ActionListener {
    
    /** The controller for the ERD viewer */
    private ErdViewerPanel parent;
    /** The table listing constraints */
    private JTable table;
    /** The two related tables */
    private ErdTable[] erdTables;
    /** The SQL text string buffer */
    private StringBuffer sqlBuffer;
    /** The constraints */
    private Vector constraints;
    
    /** The literal 'ALTER TABLE ' */
    private static final String ALTER_TABLE = "ALTER TABLE ";
    /** The literal ' ADD CONSTRAINT ' */
    private static final String DROP_CONSTRAINT = " DROP CONSTRAINT ";
    /** The literal ';' */
    private static final String CLOSE_END = ";\n";
    
    public ErdDeleteRelationshipDialog(ErdViewerPanel parent, ErdTable[] erdTables) {
        super("Delete Table Relationship");
        
        this.parent = parent;
        this.erdTables = erdTables;
        
        ColumnData[] cd1 = erdTables[0].getTableColumns();
        ColumnData[] cd2 = erdTables[1].getTableColumns();
        
        String tableName1 = erdTables[0].getTableName();
        String tableName2 = erdTables[1].getTableName();
        ColumnConstraint[] tableConstraints = null;
        
        constraints = new Vector();
        
        for (int i = 0; i < cd1.length; i++) {
            
            if (!cd1[i].isForeignKey()) {
                continue;
            }

            tableConstraints = cd1[i].getColumnConstraintsArray();            
            if (tableConstraints == null || tableConstraints.length == 0) {
                break;
            }
            
            for (int j = 0; j < tableConstraints.length; j++) {
                if (tableConstraints[j].isPrimaryKey()) {
                    continue;
                }
                
                if (tableConstraints[j].getRefTable().equalsIgnoreCase(tableName2)) {
                    constraints.add(new ColumnConstraintDrop(cd1[i], erdTables[0], j));
                }
            }
            
        }
        
        for (int i = 0; i < cd2.length; i++) {
            
            if (!cd2[i].isForeignKey()) {
                continue;
            }
            
            tableConstraints = cd2[i].getColumnConstraintsArray();
            if (tableConstraints == null || tableConstraints.length == 0) {
                continue;
            }
            
            for (int j = 0; j < tableConstraints.length; j++) {
                if (tableConstraints[j].isPrimaryKey()) {
                    continue;
                }
                
                if (tableConstraints[j].getRefTable().equalsIgnoreCase(tableName1)) {
                    constraints.add(new ColumnConstraintDrop(cd2[i], erdTables[1], j));
                }
            }
            
        }
        
        if (constraints.size() == 0) {
            GUIUtilities.displayErrorMessage(
            "No relation exists between the selected tables");
            super.dispose();
            return;
        }
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        display();
        
    }
    
    private void jbInit() throws Exception {
        JButton deleteButton = new DefaultPanelButton(Bundles.get("common.delete.button"));
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        
        cancelButton.addActionListener(this);
        deleteButton.addActionListener(this);
        
        sqlText.setPreferredSize(new Dimension(480, 90));
        
        table = new DefaultTable(new ConstraintTableModel());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(25);
        tcm.getColumn(1).setPreferredWidth(100);
        tcm.getColumn(2).setPreferredWidth(125);
        tcm.getColumn(3).setPreferredWidth(125);
        tcm.getColumn(4).setPreferredWidth(125);
        tcm.getColumn(5).setPreferredWidth(125);
        
        JScrollPane tableScroller = new JScrollPane(table);
        tableScroller.setPreferredSize(new Dimension(640,130));
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Select the constraints to be dropped:"), gbc);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.7;
        gbc.insets.top = 0;
        panel.add(tableScroller, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.3;
        panel.add(sqlText, gbc);
        gbc.gridy = 3;
        gbc.gridx = 1;
        gbc.weighty = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(deleteButton, gbc);
        gbc.gridx = 2;
        gbc.insets.left = 0;
        gbc.weightx = 0;
        panel.add(cancelButton, gbc);
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        
        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                            new Insets(7, 7, 7, 7), 0, 0));
        
        sqlBuffer = new StringBuffer();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);        
    }
    
    private void setSQLText() {
        sqlBuffer.setLength(0);
        ColumnConstraintDrop ccd = null;
        for (int i = 0, n = constraints.size(); i < n; i++) {
            ccd = (ColumnConstraintDrop)constraints.elementAt(i);
            if (ccd.isDropped()) {
                sqlBuffer.append(ccd.getSql());
            }
        }
        sqlText.setSQLText(sqlBuffer.toString());
    }
    
    private void delete() {
        ColumnConstraintDrop ccd = null;        
        for (int i = 0, n = constraints.size(); i < n; i++) {
            ccd = (ColumnConstraintDrop)constraints.elementAt(i);
            ccd.dropConstraint();
        }
        
        constraints = null;
        erdTables = null;
        table = null;
        parent.updateTableRelationships();
        dispose();
    }
    
    public void actionPerformed(ActionEvent e) {
        Object button = e.getSource();
        
        if (button instanceof JButton) {
            String command = e.getActionCommand();
            
            if (command.equals("Cancel"))
                dispose();
            
            else if (command.equals("Delete"))
                delete();
            
        }
        
    }
    
    private class ConstraintTableModel extends AbstractTableModel {
        
        protected String[] header = {" ", "Name", "Referencing Table",
        "Referencing Column", "Referenced Table",
        "Referenced Column"};
        
        public int getColumnCount() {
            return 6;
        }
        
        public int getRowCount() {
            return constraints.size();
        }
        
        public Object getValueAt(int row, int col) {
            ColumnConstraintDrop ccd = (ColumnConstraintDrop)constraints.elementAt(row);
            ColumnConstraint cc = ccd.getColumnConstraint();
            
            switch(col) {
                
                case 0:
                    return Boolean.valueOf(ccd.isDropped());
                    
                case 1:
                    return cc.getName();
                    
                case 2:
                    return cc.getTable();
                    
                case 3:
                    return cc.getColumn();
                    
                case 4:
                    return cc.getRefTable();
                    
                case 5:
                    return cc.getRefColumn();
                    
                default:
                    return null;
                    
            }
        }
        
        public void setValueAt(Object value, int row, int col) {
            ColumnConstraintDrop ccd = (ColumnConstraintDrop)constraints.elementAt(row);
            ColumnConstraint cc = ccd.getColumnConstraint();
            
            switch (col) {
                case 0:
                    ccd.setDropped(((Boolean)value).booleanValue());
                    setSQLText();
                    break;
                case 1:
                    cc.setName((String)value);
                    break;
                case 2:
                    cc.setTable((String)value);
                    break;
                case 3:
                    cc.setColumn((String)value);
                    break;
                case 4:
                    cc.setRefTable((String)value);
                    break;
                case 5:
                    cc.setRefColumn((String)value);
                    break;
            }
            
            fireTableRowsUpdated(row, row);
        }
        
        public boolean isCellEditable(int row, int col) {
            return (col == 0);
        }
        
        public String getColumnName(int col) {
            return header[col];
        }
        
        public Class getColumnClass(int col) {
            if (col == 0) {
                return Boolean.class;
            }
            return String.class;
        }
        
    } // ConstraintTableModel
    
    
    class ColumnConstraintDrop {
        
        private boolean dropped;
        private ErdTable erdTable;
        private ColumnConstraint columnConstraint;
        private ColumnData columnData;
        
        public ColumnConstraintDrop(ColumnData columnData,
        ErdTable erdTable, int constraintIndex) {
            columnConstraint = columnData.getColumnConstraintsArray()[constraintIndex];
            this.erdTable = erdTable;
            this.columnData = columnData;
            dropped = false;
        }
        
        public void dropConstraint() {
            
            if (dropped) {
                columnData.removeConstraint(columnConstraint);
                columnData.setForeignKey(false);
                erdTable.setDropConstraintsScript(getSql());
            }
            
        }
        
        public void setColumnConstraint(ColumnConstraint columnConstraint) {
            this.columnConstraint = columnConstraint;
        }
        
        public ColumnConstraint getColumnConstraint() {
            return columnConstraint;
        }
        
        public String getSql() {
            
            if (dropped)
                return ALTER_TABLE + erdTable.getTableName() +
                DROP_CONSTRAINT + columnConstraint.getName() + CLOSE_END;
            else
                return "";
            
        }
        
        public boolean isDropped() {
            return dropped;
        }
        
        public void setDropped(boolean dropped) {
            this.dropped = dropped;
        }
        
    } // ColumnConstraintDrop
   
}











