/*
 * ErdEditTableDialog.java
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

package org.executequery.gui.erd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.executequery.databasemediators.MetaDataValues;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTableToolBar;
import org.executequery.gui.table.EditTableConstraintsPanel;
import org.executequery.gui.table.EditTablePanel;
import org.executequery.gui.table.TableConstraintFunction;
import org.executequery.gui.table.TableFunction;
import org.executequery.gui.table.TableModifier;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.table.ComboBoxCellEditor;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ErdEditTableDialog extends ErdPrintableDialog
                                implements TableFunction,
                                           TableConstraintFunction {
    
    /** The ERD parent panel */
    private ErdViewerPanel parent;
    
    /** The <code>ErdTable</code> representing this dialog */
    private ErdTable erdTable;
    
    /** Contains the column descriptions for a selected table */
    private EditTablePanel columnDataTable;
    
    /** The panel displaying the table's constraints */
    private EditErdTableConstraintsPanel conPanel;
    
    /** The table name field (non-editable) */
    private DisabledField tableNameField;
    
    /** The buffer off all SQL generated */
    private StringBuffer sqlBuffer;
    
    /** The table currently in focus */
    private JTable focusTable;
    
    /** The tool bar */
    private CreateTableToolBar tools;
    
    /** The tabbed pane display */
    private JTabbedPane tabs;
    
    public ErdEditTableDialog(ErdViewerPanel parent, ErdTable erdTable) {
        super("Table Description: " + erdTable.getTableName());
        
        this.parent = parent;
        this.erdTable = erdTable;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        display();
        
    }
    
    private void jbInit() throws Exception {
        
        JButton closeButton = new DefaultPanelButton(Bundles.get("common.close.button"));
        JButton applyButton = new DefaultPanelButton(Bundles.get("common.apply.button"));
        
        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e); }
        };
        
        closeButton.addActionListener(btnListener);
        applyButton.addActionListener(btnListener);
        
        ColumnData[]  columns = erdTable.getTableColumns();
        // make a copy of the current columns
        ColumnData[]  _columns = new ColumnData[columns.length];
        
        for (int i = 0; i < columns.length; i++) {
            _columns[i] = new ColumnData();
            _columns[i].setValues(columns[i]);
        }
        
        columnDataTable = new EditTablePanel(this);
        columnDataTable.setColumnDataArray(_columns, null);
        columnDataTable.setOriginalData(erdTable.getOriginalTableColumns());
        columnDataTable.setSQLChangesHash(erdTable.getAlterTableHash());
        
        conPanel = new EditErdTableConstraintsPanel(this, _columns);
        
        JPanel conBase = new JPanel(new GridBagLayout());
        conBase.add(conPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
        GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0));
        
        // set up and add the focus listener for the tables
        FocusListener tableFocusListener = new FocusListener() {
            public void focusGained(FocusEvent e) {
                focusTable = (JTable)e.getSource(); }
            public void focusLost(FocusEvent e) {}
        };
        
        columnDataTable.addTableFocusListener(tableFocusListener);
        conPanel.addTableFocusListener(tableFocusListener);
        
        tabs = new JTabbedPane();
        tabs.add("Columns", columnDataTable);
        tabs.add("Constraints", conBase);
        
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (tabs.getSelectedIndex() == 1)
                    tools.enableButtons(false);
                else
                    tools.enableButtons(true);
            }
        });
        
        tools = new CreateTableToolBar(this, false);
        
        sqlText.setSQLText(erdTable.getAlterTableScript());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        
        tableNameField = new DisabledField(erdTable.getTableName());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 2;
        mainPanel.add(new JLabel("Table Name:"), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.left = 0;
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(tableNameField, gbc);
        gbc.insets.left = 10;
        gbc.weightx = 0;
        gbc.insets.top = 0;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets.right = 0;
        mainPanel.add(tools, gbc);
        gbc.insets.left = 0;
        gbc.insets.right = 10;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets.left = 5;
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.8;
        mainPanel.add(tabs, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.6;
        gbc.insets.left = 10;
        gbc.insets.bottom = 0;
        mainPanel.add(sqlText, gbc);
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.gridy = 4;
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets.left = 7;
        gbc.insets.top = 7;
        gbc.insets.bottom = 10;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        mainPanel.add(closeButton, gbc);
        gbc.insets.right = 0;
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        mainPanel.add(applyButton, gbc);
        
        mainPanel.setPreferredSize(new Dimension(600, 450));
        
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(mainPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                                new Insets(7, 7, 7, 7), 0, 0));
        
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        sqlBuffer = new StringBuffer();
    }
    
    // -----------------------------------------------
    // -------- TableFunction implementations --------
    // -----------------------------------------------
    
    public void setSQLText() {
        sqlText.setSQLText(columnDataTable.getSQLText() +
                            conPanel.getSQLText());
    }
    
    public void setSQLText(String values, int type) {
        sqlBuffer.delete(0, sqlBuffer.length());
        
        if (type == TableModifier.COLUMN_VALUES)
            sqlBuffer.append(values).
            append(conPanel.getSQLText());
        
        else if (type == TableModifier.CONSTRAINT_VALUES)
            sqlBuffer.append(columnDataTable.getSQLText()).
            append(values);
        
        sqlText.setSQLText(sqlBuffer.toString());
    }
    
    public String getSQLText() {
        return sqlText.getSQLText();
    }
    
    public String getTableName() {
        return erdTable.getTableName();
    }
    
    /** <p>Inserts a row before the selection. */
    public void insertBefore() {
        if (focusTable == columnDataTable.getTable())
            columnDataTable.insertBefore();
    }
    
    /** <p>Inserts a row after the selection. */
    public void insertAfter() {
        
        if (focusTable == columnDataTable.getTable())
            columnDataTable.insertAfter();
        
        else if (focusTable == conPanel.getTable()) {
            conPanel.insertRowAfter();
            conPanel.setCellEditor(2, new ComboBoxCellEditor(
            columnDataTable.getTableColumnData()));
        }
        
    }
    
    /** <p>Deletes the selected row. */
    public void deleteRow() {
        
        if (focusTable == columnDataTable.getTable())
            columnDataTable.deleteRow();
        
        else if (focusTable == conPanel.getTable())
            conPanel.dropConstraint();
        
    }
    
    /** <p>Moves the selected row up. */
    public void moveColumnUp() {}
    /** <p>Moves the selected row down. */
    public void moveColumnDown() {}
    
    // -----------------------------------------------
    
    /**
     * Retrieves the meta data retrieval object for the 
     * selected/current connection as associated with this 
     * table edit function.
     *
     * @return the meta data values object
     */
    public MetaDataValues getMetaDataValues() {
        // TODO: COMPLETE ME
        return null;
    }

    public Vector getHostedSchemasVector() {
        return new Vector(0);
    }
    
    public Vector getSchemaTables(String schemaName) {
        Vector _tables = parent.getAllComponentsVector();
        
        int size = _tables.size();
        Vector tableNames = new Vector(size);
        
        for (int i = 0; i < size; i++) {
            tableNames.add(_tables.elementAt(i).toString());
        }
        
        return tableNames;
    }
    
    public Vector getTableColumnDataVector() {
        return columnDataTable.getTableColumnDataVector();
    }
    
    public ColumnData[] getTableColumnData() {
        return columnDataTable.getTableColumnData();
    }
    
    public Vector getColumnNamesVector(String tableName, String schemaName) {
        return parent.getTableColumnsVector(tableName);
    }
    
    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Close"))
            dispose();
        
        else if (command.equals("Apply")) {
            columnDataTable.fireEditingStopped();
            conPanel.fireEditingStopped();
            
            String tableName = tableNameField.getText();
            
            String conColumn = null;
            ColumnConstraint[] constraints = conPanel.getColumnConstraintArray();
            ColumnData[] cda = columnDataTable.getTableColumnData();
            
            for (int i = 0; i < cda.length; i++) {
                
                // reset the keys
                cda[i].setPrimaryKey(false);
                cda[i].setForeignKey(false);
                cda[i].resetConstraints();
                
                for (int j = 0; j < constraints.length; j++) {
                    
                    conColumn = constraints[j].getColumn();
                    
                    if (conColumn.equalsIgnoreCase(cda[i].getColumnName())) {
                        
                        constraints[j].setTable(tableName);
                        
                        if (constraints[j].isPrimaryKey())
                            cda[i].setPrimaryKey(true);
                        else if (constraints[j].isForeignKey())
                            cda[i].setForeignKey(true);
                        
                        cda[i].addConstraint(constraints[j]);
                        
                    }
                    
                }
                
                cda[i].setTableName(tableName);
                cda[i].setNamesToUpper();
                
            }
            
            erdTable.setTableColumns(cda);
            erdTable.setAlterTableHash(columnDataTable.getSQLChangesHash());
            erdTable.setAlterTableScript(sqlText.getSQLText());
            erdTable.tableColumnsChanged();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    parent.updateTableRelationships();
                }
            });
            
            columnDataTable = null;
            conPanel = null;
            sqlText = null;
            tabs = null;
            
            dispose();
            
        }
        
    }
    
    class EditErdTableConstraintsPanel extends EditTableConstraintsPanel {
        
        private ColumnData[] columnData;
        
        public EditErdTableConstraintsPanel(TableConstraintFunction creator,
        ColumnData[] columnData) {
            
            super(creator);
            this.columnData = columnData;
            
            Vector constraints = new Vector();
            
            for (int i = 0; i < columnData.length; i++) {
                
                Vector _constraints = columnData[i].getColumnConstraintsVector();
                
                if (_constraints == null)
                    continue;
                
                for (int j = 0, k = _constraints.size(); j < k; j++) {
                    constraints.add(_constraints.elementAt(j));
                }
                
            }
            
            setData(constraints, true);
            setOriginalData();
            
        }
        
        public void dropConstraint() {
            int row = table.getSelectedRow();
            
            if (row == -1)
                return;
            
            table.editingStopped(null);
            
            if (table.isEditing())
                table.removeEditor();
            
            ColumnConstraint cc = getConstraintAt(row);
            
            for (int i = 0; i < columnData.length; i++) {
                
                Vector _constraints = columnData[i].getColumnConstraintsVector();
                
                if (_constraints == null)
                    continue;
                
                for (int j = 0, k = _constraints.size(); j < k; j++) {
                    
                    if(_constraints.elementAt(j) == cc) {
                        deleteSelectedRow();
                        columnData[i].removeConstraint(cc);
                        columnData[i].setForeignKey(false);
                        break;
                    }
                    
                }
                
            }
            
            if (cc.isNewConstraint())
                removeFromBuffer(ADD_CONSTRAINT + row);
            else {
                String query = ALTER_TABLE + erdTable.getTableName() +
                DROP_CONSTRAINT + cc.getName() + SEMI_COLON + NEW_LINE;
                
                addToBuffer(DROP_CONSTRAINT + row, query);
            }
            
            generateSQL();
            setSQLText();
            cc = null;
        }
        
    } // class EditErdTableConstraintsPanel
    
}


















