/*
 * CreateIndexPanel.java
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

package org.executequery.gui;

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.components.ItemSelectionListener;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.TableIndex;
import org.executequery.databaseobjects.impl.DefaultTableIndex;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.MoveListItemStrategy;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Create Index panel.
 *
 * @author Takis Diakoumis
 */
@Deprecated
public class CreateIndexPanel extends ActionPanel
        implements FocusComponentPanel,
        ActiveComponent,
        TableModelListener,
        KeywordListener,
        TextEditorContainer,
        ItemSelectionListener {

    public static final String TITLE = "Create Index";
    public static final String FRAME_ICON = "NewIndex16.svg";

    private JComboBox schemaCombo;
    private JComboBox connectionsCombo;
    private JComboBox tableCombo;

    private JTextField nameField;

    private JCheckBox normalCheck;
    private JCheckBox uniqueCheck;
    private JCheckBox bitmapCheck;
    private JCheckBox unsortedCheck;

    private SimpleSqlTextPanel sqlText;

    private JTable selectedTable;

    private CreateIndexModel model;

    private JButton moveUpButton;
    private JButton moveDownButton;

    /**
     * the parent container
     */
    private ActionContainer parent;

    private TableIndex tableIndex;

    private TableSelectionCombosGroup combosGroup;

    private MoveListItemStrategy<IndexedTableColumn> moveStrategy;

    /*public CreateIndexPanel(ActionContainer parent) {

        super(new BorderLayout());

        this.parent = parent;

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }*/

    private void init() throws Exception {

        nameField = WidgetFactory.createTextField("nameField");

        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo");
        schemaCombo = WidgetFactory.createComboBox("schemaCombo");
        tableCombo = WidgetFactory.createComboBox("tableCombo");

        combosGroup = new TableSelectionCombosGroup(
                connectionsCombo, schemaCombo, tableCombo);
        combosGroup.addItemSelectionListener(this);

        sqlText = new SimpleSqlTextPanel();

        // build the table panel
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setPreferredSize(new Dimension(480, 175));

        createTableAndModel();

        TableColumnModel tcm = selectedTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(250);
        tcm.getColumn(1).setPreferredWidth(120);
        tcm.getColumn(2).setMaxWidth(70);

        JScrollPane tableScroller = new JScrollPane(selectedTable);

        // build the table's tools panel
        moveUpButton = ActionUtilities.createToolbarButton(
                this,
                "Up16.svg",
                "Move the selection up",
                "moveColumnUp");

        moveDownButton = ActionUtilities.createToolbarButton(
                this,
                "Down16.svg",
                "Move the selection down",
                "moveColumnDown");

        // add table panel components
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(0, 0, 5, 5);
        gbc.insets = ins;
        tablePanel.add(moveUpButton, gbc);
        gbc.gridy = 1;
        gbc.insets.top = 1;
        tablePanel.add(moveDownButton, gbc);
        gbc.insets.top = 0;
        gbc.insets.right = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 3;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        tablePanel.add(tableScroller, gbc);

        // add all components
        gbc = new GridBagConstraints();
        JPanel mainPanel = new JPanel(new GridBagLayout());

        WidgetFactory.addLabelFieldPair(mainPanel, "Connection:", connectionsCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Schema:", schemaCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Table:", tableCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Index Name:", nameField, gbc);

        gbc.insets.left = 0;
        gbc.insets.right = 0;
        gbc.insets.top = 0;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(indexTypeOptionsPanel(), gbc);
        gbc.gridy++;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.weighty = 0.4;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(tablePanel, gbc);
        gbc.gridy++;
        gbc.weighty = 0.6;
        gbc.insets.bottom = 5;
        mainPanel.add(sqlText, gbc);

        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        JPanel base = new JPanel(new BorderLayout());

        base.add(mainPanel, BorderLayout.CENTER);

        BottomButtonPanel buttonPanel = new BottomButtonPanel(this, "Create", "create-index", true);
        buttonPanel.setOkButtonActionCommand("doCreateIndex");

        base.add(buttonPanel, BorderLayout.SOUTH);

        // add the base to the panel
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(base, BorderLayout.CENTER);

        nameField.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyReleased(KeyEvent e) {

                if (hasTableIndex()) {

                    tableIndex.setName(nameField.getText());
                    updateSqlText();
                }

            }

        });

        setPreferredSize(new Dimension(750, 480));

        // register as a keyword listener
        EventMediator.registerListener(this);

        itemStateChanged(null);
    }

    private void createTableAndModel() {

        model = new CreateIndexModel();
        model.addTableModelListener(this);

        selectedTable = new DefaultTable(model);
        //selectedTable.setRowSelectionAllowed(false);
        selectedTable.setColumnSelectionAllowed(false);
        selectedTable.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel indexTypeOptionsPanel() {

        normalCheck = new JCheckBox("Normal", true);
        uniqueCheck = new JCheckBox("Unique");
        bitmapCheck = new JCheckBox("Bitmap");
        unsortedCheck = new JCheckBox("Unsorted");

        List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
        checkBoxes.add(normalCheck);
        checkBoxes.add(uniqueCheck);
        checkBoxes.add(bitmapCheck);
        checkBoxes.add(unsortedCheck);

        JPanel checkPanel = new JPanel();
        ButtonGroup bg = new ButtonGroup();

        String actionCommand = "indexTypeSelected";

        for (JCheckBox checkBox : checkBoxes) {

            bg.add(checkBox);

            checkPanel.add(checkBox);

            checkBox.setActionCommand(actionCommand);
            checkBox.addActionListener(this);
        }

        return checkPanel;
    }

    public void cleanup() {

        combosGroup.close();

        EventMediator.deregisterListener(this);
    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DefaultKeywordEvent);
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {

        sqlText.setSQLKeywords(true);
    }

    /**
     * Returns the index name field.
     */
    public Component getDefaultFocusComponent() {

        return nameField;
    }

    public void indexTypeSelected() {

        if (hasTableIndex()) {

            int indexType = TableIndex.NORMAL_INDEX;

            if (uniqueCheck.isSelected()) {

                indexType = TableIndex.UNIQUE_INDEX;

            } else if (bitmapCheck.isSelected()) {

                indexType = TableIndex.BITMAP_INDEX;

            } else if (unsortedCheck.isSelected()) {

                indexType = TableIndex.UNSORTED_INDEX;
            }

            tableIndex.setIndexType(indexType);

            updateSqlText();
        }

    }

    private boolean hasTableIndex() {

        return tableIndex != null;
    }

    public void doCreateIndex() {

        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {

                    parent.block();
                    createIndex();

                } finally {

                    parent.unblock();
                }
            }
        });

    }

    private void createIndex() {

        DatabaseConnection dc = combosGroup.getSelectedHost().getDatabaseConnection();

        try {

            StatementExecutor qs = new DefaultStatementExecutor(dc);

            SqlStatementResult result = qs.updateRecords(createIndexStatement());

            if (result.getUpdateCount() >= 0) {

                GUIUtilities.displayInformationMessage(
                        "Index " + nameField.getText() + " created.");

                parent.finished();

            } else {

                SQLException e = result.getSqlException();

                if (e != null) {

                    StringBuilder sb = new StringBuilder();
                    sb.append("An error occurred applying the specified changes.").
                            append("\n\nThe system returned:\n").
                            append(MiscUtils.formatSQLError(e));

                    GUIUtilities.displayExceptionErrorDialog(sb.toString(), e);

                } else {

                    GUIUtilities.displayErrorMessage(result.getErrorMessage());
                }

            }

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog(
                    "Error:\n" + e.getMessage(), e);
        }

    }

    private String createIndexStatement() {

        return sqlText.getSQLText();
    }

    public void tableChanged(TableModelEvent event) {

        if (hasTableIndex()) {

            if (event.getType() == TableModelEvent.UPDATE) {

                tableIndex.clearColumns();

                List<IndexedTableColumn> columns = model.getIndexedTableColumns();

                for (IndexedTableColumn tableColumn : columns) {

                    if (tableColumn.indexed) {

                        tableIndex.addColumn(tableColumn.column);
                    }
                }

            }

            updateSqlText();

        } else {

            sqlText.setSQLText("");
        }

    }

    private void updateSqlText() {

        if (hasTableIndex()) {

            GUIUtils.invokeLater(new Runnable() {

                public void run() {

                    sqlText.setSQLText(tableIndex.getCreateSQLText());
                }

            });

        }
    }

    public void moveColumnUp() {

        moveStrategy().setList(model.getIndexedTableColumns());

        int selectedIndex = getSelectedRow();

        int newIndex = moveStrategy().moveUp(selectedIndex);

        model.fireTableRowsUpdated(newIndex, selectedIndex);

        setSelectedRow(newIndex);
    }

    public void moveColumnDown() {

        moveStrategy().setList(model.getIndexedTableColumns());

        int selectedIndex = getSelectedRow();

        int newIndex = moveStrategy().moveDown(selectedIndex);

        model.fireTableRowsUpdated(selectedIndex, newIndex);

        setSelectedRow(newIndex);
    }

    private MoveListItemStrategy<IndexedTableColumn> moveStrategy() {

        if (moveStrategy == null) {

            moveStrategy = new MoveListItemStrategy<IndexedTableColumn>();
        }

        return moveStrategy;
    }

    private void setSelectedRow(int row) {

        selectedTable.getSelectionModel().setSelectionInterval(row, row);
    }

    private int getSelectedRow() {

        return selectedTable.getSelectedRow();
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


    private class CreateIndexModel extends AbstractTableModel {

        private TableIndex tableIndex;

        private List<IndexedTableColumn> columns;

        private String[] header = {"Column Name", "Datatype", "Select"};

        public List<IndexedTableColumn> getIndexedTableColumns() {

            return columns;
        }

        public void setTableIndex(TableIndex tableIndex) throws DataSourceException {

            this.tableIndex = tableIndex;

            if (columns != null) {

                columns.clear();

            } else {

                columns = new ArrayList<IndexedTableColumn>();
            }

            for (DatabaseColumn column : tableIndex.getTable().getColumns()) {

                columns.add(new IndexedTableColumn(column, false));
            }

            fireTableDataChanged();
        }

        public void clear() {

            tableIndex = null;

            if (columns != null) {

                columns.clear();
            }

            fireTableDataChanged();
        }

        public TableIndex getTableIndex() {

            return tableIndex;
        }

        public int getColumnCount() {

            return header.length;
        }

        public int getRowCount() {

            if (columns != null) {

                return columns.size();
            }

            return 0;
        }

        public Object getValueAt(int row, int col) {

            IndexedTableColumn indexedColumn = columns.get(row);

            switch (col) {
                case 0:
                    return indexedColumn.column.getName();
                case 1:
                    return indexedColumn.column.getTypeName();
                case 2:
                    return indexedColumn.indexed;
            }

            return indexedColumn;
        }

        public void setValueAt(Object value, int row, int col) {

            if (col < 2) {

                return;
            }

            IndexedTableColumn indexedColumn = columns.get(row);
            indexedColumn.indexed = ((Boolean) value).booleanValue();

            fireTableRowsUpdated(row, row);
        }

        public boolean isCellEditable(int row, int col) {

            return (col == 2);
        }

        public String getColumnName(int col) {

            return header[col];
        }

        public Class<?> getColumnClass(int col) {

            if (col == 2) {

                return Boolean.class;
            }

            return String.class;
        }

    } // CreateIndexModel


    private static class IndexedTableColumn {

        String columnName;
        String dataType;

        boolean indexed;
        DatabaseColumn column;

        IndexedTableColumn(DatabaseColumn column, boolean indexed) {
            this.column = column;
            this.indexed = indexed;
        }

    } // IndexTableColumn


    public String getDisplayName() {

        return "";
    }

    public String toString() {

        return TITLE;
    }

    public void itemStateChanging(ItemEvent e) {

        parent.block();
    }

    public void itemStateChanged(ItemEvent event) {

        try {

            DatabaseTable table = combosGroup.getSelectedTable();

            if (table != null) {

                tableIndex = new DefaultTableIndex(table);
                tableIndex.setName(nameField.getText());

                model.setTableIndex(tableIndex);

                indexTypeSelected();

                updateSqlText();

            } else {

                if (model != null) {

                    model.clear();
                }

            }

        } catch (DataSourceException e) {

            Log.error("Error on table selection for index", e);

        } finally {

            parent.unblock();
        }

    }

}











