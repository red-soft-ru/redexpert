/*
 * CreateTableFunctionPanel.java
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

package org.executequery.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;

/** 
 * The Create Table base panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class CreateTableFunctionPanel extends JPanel
                                               implements FocusComponentPanel,
                                                          ItemListener,
                                                          ChangeListener,
                                                          TableModifier,
                                                          TableConstraintFunction,
                                                          TextEditorContainer {
    
    /** The table name field */
    protected JTextField nameField;
    
    /** The schema combo box */
    protected JComboBox schemaCombo;
    
    /** the schema combo box model */
    protected DynamicComboBoxModel schemaModel;
    
    /** The connection combo selection */
    protected JComboBox connectionsCombo; 

    /** the schema combo box model */
    protected DynamicComboBoxModel connectionsModel;

    /** The table column definition panel */
    protected NewTablePanel tablePanel;
    
    /** The table constraints panel */
    protected NewTableConstraintsPanel consPanel;
    
    /** The text pane showing SQL generated */
    protected SimpleSqlTextPanel sqlText;
    
    /** The tabbed pane containing definition and constraints */
    private JTabbedPane tableTabs;
    
    /** The buffer off all SQL generated */
    protected StringBuffer sqlBuffer;
    
    /** The tool bar */
    private CreateTableToolBar tools;
    
    /** Utility to retrieve database meta data */
    protected MetaDataValues metaData;
    
    /** The base panel */
    protected JPanel mainPanel;
    
    /** <p> Constructs a new instance. */
    public CreateTableFunctionPanel() {
        super(new BorderLayout());
        
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    private void init() throws Exception {
        
        nameField = WidgetFactory.createTextField();
        //initialise the schema label
        metaData = new MetaDataValues(true);

        // combo boxes
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(this);

        schemaModel = new DynamicComboBoxModel();
        schemaCombo = WidgetFactory.createComboBox(schemaModel);
        schemaCombo.addItemListener(this);

        // create tab pane
        tableTabs = new JTabbedPane();
        // create the column definition panel
        // and add this to the tabbed pane
        tablePanel = new NewTablePanel(this);
        tableTabs.add("Columns", tablePanel);
        
        // create the constraints table and model
        JPanel constraintsPanel = new JPanel(new GridBagLayout());
        consPanel = new NewTableConstraintsPanel(this);
        consPanel.setData(new Vector(0), true);
        
        constraintsPanel.add(consPanel, new GridBagConstraints(
                                                1, 1, 1, 1, 1.0, 1.0, 
                                                GridBagConstraints.SOUTHEAST,
                                                GridBagConstraints.BOTH, 
                                                new Insets(2, 2, 2, 2), 0, 0));
        
        tableTabs.add("Constraints", constraintsPanel);
        
        sqlText = new SimpleSqlTextPanel();
        tools = new CreateTableToolBar(this);
        
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;

        WidgetFactory.addLabelFieldPair(mainPanel, "Connection:", connectionsCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Schema:", schemaCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Table Name:", nameField, gbc);

        JPanel definitionPanel = new JPanel(new GridBagLayout());
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.insets.top = 20;
        gbc.fill = GridBagConstraints.VERTICAL;
        definitionPanel.add(tools, gbc);
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        gbc.insets.top = 0;
        gbc.gridx = 1;
        gbc.weighty = 0.4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        definitionPanel.add(tableTabs, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 10;
        mainPanel.add(definitionPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 0.6;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.insets.top = 5;
        mainPanel.add(sqlText, gbc);

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(mainPanel, BorderLayout.CENTER);
        
        tableTabs.addChangeListener(this);
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setSQLText(); 
            }
        });
        
        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);
        
        // check initial values for possible value inits
        if (connections == null || connections.isEmpty()) {
            schemaCombo.setEnabled(false);
            connectionsCombo.setEnabled(false);
        } else {
            DatabaseConnection connection = 
                    (DatabaseConnection)connections.elementAt(0);
            metaData.setDatabaseConnection(connection);
            Vector schemas = metaData.getHostedSchemasVector();
            if (schemas == null || schemas.size() == 0) {
                schemas = metaData.getHostedCatalogsVector();
            }
            schemaModel.setElements(schemas);
            schemaCombo.setSelectedIndex(0);
            tablePanel.setDataTypes(metaData.getDataTypesArray());
        }
        
    }
    
    /**
     * Returns the selected connection from the panel's
     * connections combo selection box.
     * 
     * @return the selected connection properties object
     */
    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection)connectionsCombo.getSelectedItem();
    }
    
    /**
     * Returns the table name field.
     */
    public Component getDefaultFocusComponent() {
        return nameField;
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */    
    public void itemStateChanged(ItemEvent event) {
        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }

        final Object source = event.getSource();
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    setInProcess(true);
                    if (source == connectionsCombo) {
                        connectionChanged();
                    }
                    else if (source == schemaCombo) {
                        setSQLText();
                    }
                }
                finally {
                    setInProcess(false);
                }
            }
        });
    }
    
    private void connectionChanged() {
        // retrieve connection selection
        DatabaseConnection connection = 
                (DatabaseConnection)connectionsCombo.getSelectedItem();

        // reset meta data
        metaData.setDatabaseConnection(connection);

        // reset schema values
        try {
            Vector schemas = metaData.getHostedSchemasVector();
            if (schemas == null || schemas.isEmpty()) {
                // try catalogs (ie. for mysql and others where schema not used)
                schemas = metaData.getHostedCatalogsVector();
            }
            populateSchemaValues(schemas);
        }
        catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the catalog/schema names for the " +
                    "selected connection.\n\nThe system returned:\n" + 
                    e.getExtendedMessage(), e);
            populateSchemaValues(new Vector<String>(0));
        }

        // reset data types
        try {
            populateDataTypes(metaData.getDataTypesArray());
        }
        catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the data types for the " +
                    "selected connection.\n\nThe system returned:\n" + 
                    e.getExtendedMessage(), e);
            populateDataTypes(new String[0]);
        }

    }
    
    private void populateDataTypes(final String[] dataTypes) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                tablePanel.setDataTypes(dataTypes);
            }
        });
    }
    
    private void populateSchemaValues(final Vector schemas) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                schemaModel.setElements(schemas);
                schemaCombo.setSelectedIndex(0);
                schemaCombo.setEnabled(true);
            }
        });
    }
    
    public void setFocusComponent() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }
    
    public void setSQLTextCaretPosition(int position) {
        sqlText.setCaretPosition(position);
    }
    
    protected void addButtonsPanel(JPanel buttonsPanel) {
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void fireEditingStopped() {
        tablePanel.fireEditingStopped();
        consPanel.fireEditingStopped();
    }
    
    public void setColumnDataArray(ColumnData[] cda) {
        tablePanel.setColumnDataArray(cda, null);
    }
    
    public void setColumnConstraintVector(Vector ccv, boolean fillCombos) {
        consPanel.setData(ccv, fillCombos);
    }
    
    public void setColumnConstraintsArray(ColumnConstraint[] cca, boolean fillCombos) {
        Vector ccv = new Vector(cca.length);
        for (int i = 0; i < cca.length; i++) {
            ccv.add(cca[i]);
        }        
        consPanel.setData(ccv, fillCombos);
    }
    
    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off 
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {}
    
    // -----------------------------------------------
    // --- TableConstraintFunction implementations ---
    // -----------------------------------------------
    
    public abstract Vector<String> getHostedSchemasVector();
    
    public abstract Vector<String> getSchemaTables(String schemaName);
    
    public abstract Vector<String> getColumnNamesVector(String tableName, String schemaName);
    
    public void resetSQLText() {
        tablePanel.resetSQLText();
        consPanel.resetSQLText();
    }
    
    public void setSQLText() {
        sqlBuffer.setLength(0);
        sqlBuffer.append(CreateTableSQLSyntax.CREATE_TABLE);

        // check for a valid schema name
        if (schemaModel.getSize() > 0) {
            String schema = schemaCombo.getSelectedItem().toString();
            if (!MiscUtils.isNull(schema)) {
                sqlBuffer.append(schemaCombo.getSelectedItem()).
                          append(CreateTableSQLSyntax.DOT);

            }
        }
        
        sqlBuffer.append(nameField.getText()).
                  append(CreateTableSQLSyntax.SPACE).
                  append(CreateTableSQLSyntax.B_OPEN).
                  append(tablePanel.getSQLText()).
                  append(consPanel.getSQLText());

        sqlBuffer.append(CreateTableSQLSyntax.B_CLOSE).
                  append(CreateTableSQLSyntax.SEMI_COLON);
        
        setSQLText(sqlBuffer.toString());
    }
    
    public void setSQLText(String values, int type) {
        sqlBuffer.setLength(0);
        sqlBuffer.append(CreateTableSQLSyntax.CREATE_TABLE);

        // check for a valid schema name
        if (schemaModel.getSize() > 0) {
            String schema = schemaCombo.getSelectedItem().toString();
            if (!MiscUtils.isNull(schema)) {
                sqlBuffer.append(schemaCombo.getSelectedItem()).
                          append(CreateTableSQLSyntax.DOT);

            }
        }

        sqlBuffer.append(nameField.getText()).
                  append(CreateTableSQLSyntax.SPACE).
                  append(CreateTableSQLSyntax.B_OPEN);
        
        if (type == TableModifier.COLUMN_VALUES) {
            sqlBuffer.append(values).
                      append(consPanel.getSQLText());
        }
        else if (type == TableModifier.CONSTRAINT_VALUES) {
            sqlBuffer.append(tablePanel.getSQLText()).
                      append(values);
        }
        
        sqlBuffer.append(CreateTableSQLSyntax.B_CLOSE).
                  append(CreateTableSQLSyntax.SEMI_COLON);
        setSQLText(sqlBuffer.toString());
    }
    
    private void setSQLText(final String text) {
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                sqlText.setSQLText(text);
            }
        });
    }
    
    public String getSQLText() {
        return sqlText.getSQLText();
    }
    
    public String getTableName() {
        return nameField.getText();
    }
    
    // -----------------------------------------------
    
    
    // constraints panel only
    public void updateCellEditor(int col, int row, String value) {}
    
    public void columnValuesChanging(int col, int row, String value) {}
    
    public Vector getTableColumnDataVector() {
        return tablePanel.getTableColumnDataVector();
    }
    
    public void stateChanged(ChangeEvent e) {
        if (tableTabs.getSelectedIndex() == 1) {
            tools.enableButtons(false);
            
            //          if (table.isEditing())
            //            table.removeEditor();
            
        }
        else {
            tools.enableButtons(true);
        }
    }

    /*
    private void tableTabs_changed() {
        
        if (tableTabs.getSelectedIndex() == 1) {
            tools.enableButtons(false);
            
            //          if (table.isEditing())
            //            table.removeEditor();
            
        }
        else {
            tools.enableButtons(true);
        }
        
    }
    */

    public ColumnData[] getTableColumnDataAndConstraints() {
        String tableName = null;
        ColumnData[] cda = tablePanel.getTableColumnData();
        ColumnConstraint[] cca = consPanel.getColumnConstraintArray();
        
        for (int i = 0; i < cda.length; i++) {
            
            // reset the keys
            cda[i].setPrimaryKey(false);
            cda[i].setForeignKey(false);
            cda[i].resetConstraints();
            
            tableName = cda[i].getTableName();

            String columnName = cda[i].getColumnName();
            
            for (int j = 0; j < cca.length; j++) {
                
                String constraintColumn = cca[j].getColumn();

                if (constraintColumn!= null 
                        && constraintColumn.equalsIgnoreCase(columnName)) {
                    
                    if (cca[j].isPrimaryKey()) {
                        cda[i].setPrimaryKey(true);
                    } else if (cca[j].isForeignKey()) {
                        cda[i].setForeignKey(true);
                    }
                    
                    cca[j].setTable(tableName);
                    cca[j].setNewConstraint(true);
                    cda[i].addConstraint(cca[j]);
                }
                
            }
            
        }
        
        return cda;
        
    }
    
    public void columnValuesChanging() {}
    
    public ColumnData[] getTableColumnData() {
        return tablePanel.getTableColumnData();
    }
    
    // -----------------------------------------------
    // -------- TableFunction implementations --------
    // -----------------------------------------------
    
    public void moveColumnUp() {
        int index = tableTabs.getSelectedIndex();        
        if (index == 0) {
            tablePanel.moveColumnUp();
        }        
    }
    
    public void moveColumnDown() {
        int index = tableTabs.getSelectedIndex();
        if (index == 0) {
            tablePanel.moveColumnDown();
        }
    }
    
    public void deleteRow() {        
        if (tableTabs.getSelectedIndex() == 0) {
            tablePanel.deleteRow();
        }
        else if (tableTabs.getSelectedIndex() == 1) {
            consPanel.deleteSelectedRow();
        }        
    }
    
    public void insertBefore() {
        tablePanel.insertBefore();
    }
    
    public void insertAfter() {        
        if (tableTabs.getSelectedIndex() == 0) {
            tablePanel.insertAfter();
        }
        else if (tableTabs.getSelectedIndex() == 1) {
            consPanel.insertRowAfter();
        }
    }
    
    // -----------------------------------------------
    
    public String getDisplayName() {
        return "";
    }
    
    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------
    
    /**
     * Returns the SQL text pane as the TextEditor component 
     * that this container holds.
     */
    public TextEditor getTextEditor() {
        return sqlText;
    }
    
}














