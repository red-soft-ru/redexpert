/*
 * CreateTablePanel.java
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

package org.executequery.gui.table;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.AbstractSQLSecurityObjectPanel;
import org.executequery.gui.databaseobjects.CreateGlobalTemporaryTable;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * The Creation Table base panel.
 *
 * @author Takis Diakoumis
 */
public class CreateTablePanel extends AbstractSQLSecurityObjectPanel
        implements FocusComponentPanel,
        ItemListener,
        ChangeListener,
        TableModifier,
        TableConstraintFunction,
        TextEditorContainer {

    public static final String TITLE = Bundles.get(CreateTablePanel.class, "title");

    public static final String FRAME_ICON = "NewTable16.svg";

    protected JComboBox tablespacesCombo;

    protected DynamicComboBoxModel tablespaceComboModel;

    /**
     * The components for creating EXTERNAL table
     */
    protected JCheckBox isExternalTable;    //checking for creating EXTERNAL table
    protected JPanel externalTablePropsPanel;   //panel with components for creating EXTERNAL table
    protected JTextField externalTableFilePathField;    //path to table data file
    protected JButton browseExternalTableFileButton;    //button for open selectFileDialog
    protected JCheckBox isAdapterNeeded;    //checking for using ADAPTER table

    /**
     * The table column definition panel
     */
    protected NewTablePanel tablePanel;

    /**
     * The table constraints panel
     */
    protected NewTableConstraintsPanel consPanel;

    /**
     * The text pane showing SQL generated
     */
    protected SimpleSqlTextPanel sqlText;
    /**
     * The buffer off all SQL generated
     */
    protected StringBuffer sqlBuffer;

    /**
     * The toolbar
     */
    private CreateTableToolBar colTools;
    private CreateTableToolBar conTools;

    /**
     * The base panel
     */
    protected JPanel mainPanel;
    private JComboBox typeTemporaryBox;

    /**
     * <p> Constructs a new instance.
     */
    public CreateTablePanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null, null);
    }

    @Override
    protected void reset() {

    }

    protected void init() {

        initSQLSecurity(false);
        sqlSecurityCombo.addActionListener(actionEvent -> setSQLText());

        connectionsCombo.addItemListener(this);
        colTools = new CreateTableToolBar(this);
        conTools = new CreateTableToolBar(this);
        tablespaceComboModel = new DynamicComboBoxModel(new Vector<>());
        tablespacesCombo = WidgetFactory.createComboBox("tablespaceCombo", tablespaceComboModel);
        tablespacesCombo.addItemListener(this);
        JPanel columnsPanel = new JPanel(new GridBagLayout());
        tablePanel = new NewTablePanel(this);
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic();
        columnsPanel.add(colTools, gbh.setLabelDefault().fillVertical().spanY().get());
        columnsPanel.add(tablePanel, gbh.nextCol().fillBoth().spanX().spanY().get());
        tabbedPane.add(bundledString("Columns"), columnsPanel);

        // create the constraints table and model
        JPanel constraintsPanel = new JPanel(new GridBagLayout());
        consPanel = new NewTableConstraintsPanel(this);
        consPanel.setData(new Vector(0), true);
        typeTemporaryBox = new JComboBox(new DefaultComboBoxModel(new String[]{"DELETE ROWS", "PRESERVE ROWS"}));
        typeTemporaryBox.addActionListener(actionEvent -> setSQLText());
        gbh.setDefaultsStatic();
        constraintsPanel.add(conTools, gbh.setLabelDefault().fillVertical().spanY().get());
        constraintsPanel.add(consPanel, gbh.nextCol().fillBoth().spanX().spanY().get());

        tabbedPane.add(bundledString("Constraints"), constraintsPanel);

        addCommentTab(null);
        simpleCommentPanel.getCommentField().getTextAreaComponent().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                setSQLText();
            }
            @Override
            public void keyPressed(KeyEvent e) {
                setSQLText();
            }
            @Override
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        });

        sqlText = new SimpleSqlTextPanel();

        // ----- components for creating EXTERNAL table -----

        isExternalTable = new JCheckBox(bundledString("IsExternalTableText"));
        isExternalTable.addActionListener(e -> externalTablePropsChanged());

        externalTableFilePathField = WidgetFactory.createTextField("externalTableFilePathField");
        externalTableFilePathField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                externalTablePropsChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                externalTablePropsChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                externalTablePropsChanged();
            }

        });

        browseExternalTableFileButton = WidgetFactory.createInlineFieldButton(bundledString("BrowseButtonText"));
        browseExternalTableFileButton.addActionListener(e -> browseExternalTableFile());

        isAdapterNeeded = new JCheckBox(bundledString("IsAdapterNeededText"));
        isAdapterNeeded.addActionListener(e -> externalTablePropsChanged());

        // ------ components arranging -----
        if (!(this instanceof CreateGlobalTemporaryTable))
            topGbh.addLabelFieldPair(topPanel, bundledString("Tablespace"), tablespacesCombo, null, false);
        tablespacesCombo.setToolTipText(Bundles.get(TableDefinitionPanel.class, "Tablespace"));
        tablespacesCombo.addActionListener(actionEvent -> setSQLText());
        if (this instanceof CreateGlobalTemporaryTable) {
            topGbh.addLabelFieldPair(topPanel, bundledString("TypeTemporaryTable"), typeTemporaryBox, null, false);
        }

        if (getDatabaseVersion() >= 3 && !(this instanceof CreateGlobalTemporaryTable)) {

            topPanel.add(isExternalTable, topGbh.nextRowFirstCol().setLabelDefault().get());

            topPanel.add(isAdapterNeeded, topGbh.nextCol().get());
        }

        // ----- external panel -----
        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setDefaultsStatic();
        externalTablePropsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(externalTablePropsPanel,
                bundledString("ExternalTableDataFileLabel"), externalTableFilePathField,
                null, true, false);

        externalTablePropsPanel.add(browseExternalTableFileButton,
                gridBagHelper.nextCol().setLabelDefault().get());

        // -----

        topPanel.add(externalTablePropsPanel, topGbh.nextRowFirstCol().spanX().spanY().fillHorizontally().get());
        tabbedPane.addTab("SQL", sqlText);

        // ------

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        //add(mainPanel, BorderLayout.CENTER);

        tabbedPane.addChangeListener(this);
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        });

        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);

        // check initial values for possible value inits

        tablePanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        tablePanel.setDomains(getDomains());
        tablePanel.setGenerators(getGenerators());
        tablePanel.setDatabaseConnection(connection);
        populateTablespaces(connection);

        externalTablePropsChanged();
        conTools.enableButtons(false);
        colTools.enableButtons(true);
        centralPanel.setVisible(false);
    }

    @Override
    protected void initEdited() {

    }

    @Override
    public void createObject() {
        if (!checkFullType())
            return;
        String queries = getSQLText();
        displayExecuteQueryDialog(queries, "^");
    }

    @Override
    public String getCreateTitle() {
        return TITLE;
    }

    @Override
    public String getEditTitle() {
        return TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.TABLE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
    }

    @Override
    public void setParameters(Object[] params) {
    }

    @Override
    protected String generateQuery() {

        String tablespace = null;
        if (tablespacesCombo.getSelectedItem() != null)
            tablespace = ((NamedObject) tablespacesCombo.getSelectedItem()).getName();

        String externalFile = null;
        String adapter = null;
        if (isExternalTable.isSelected()) {
            externalFile = externalTableFilePathField.getText();

            if (isAdapterNeeded.isSelected())
                adapter = "CSV";
        }

        String comment = null;
        if (!Objects.equals(simpleCommentPanel.getComment(), ""))
            comment = simpleCommentPanel.getComment().trim();

        return SQLUtils.generateCreateTable(
                nameField.getText(), tablePanel.getTableColumnDataVector(), consPanel.getKeys(),
                false, this instanceof CreateGlobalTemporaryTable, true, true, true,
                "ON COMMIT " + typeTemporaryBox.getSelectedItem(),
                externalFile, adapter, (String) sqlSecurityCombo.getSelectedItem(), tablespace, comment, "^");
    }

    private void externalTablePropsChanged() {

        if (isExternalTable.isSelected()) {

            externalTablePropsPanel.setVisible(true);
            isAdapterNeeded.setVisible(true);
            setSQLText();

        } else {

            externalTablePropsPanel.setVisible(false);
            isAdapterNeeded.setVisible(false);
            setSQLText();
        }

    }

    public void sqlSecurityChanged(ActionEvent e) {
        setSQLText();
    }


    public void browseExternalTableFile() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle(bundledString("OpenFileDialogText"));
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(
                GUIUtilities.getInFocusDialogOrWindow(), bundledString("OpenFileDialogButton"));
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();

        if (!file.exists()) {

            GUIUtilities.displayWarningMessage(
                    bundledString("FileDoesNotExistMessage"));
            return;
        }

        externalTableFilePathField.setText(file.getAbsolutePath());
        externalTablePropsChanged();

    }

    String[] getDomains() {
        java.util.List<String> domains = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.DOMAIN]);
        return domains.toArray(new String[domains.size()]);
    }

    String[] getGenerators() {
        java.util.List<String> generators = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.SEQUENCE]);
        return generators.toArray(new String[generators.size()]);
    }

    /**
     * Returns the selected connection from the panel's
     * connections combo selection box.
     *
     * @return the selected connection properties object
     */
    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionsCombo.getSelectedItem();
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
        if (event.getSource() == tablespacesCombo) {
            setSQLText();

        } else
            GUIUtils.startWorker(() -> {
                try {
                    setInProcess(true);
                    if (source == connectionsCombo)
                        connectionChanged();

                } finally {
                    setInProcess(false);
                }
            });
    }

    private void columnChangeConnection(DatabaseConnection dc) {
        Vector<ColumnData> cd = getTableColumnDataVector();
        for (ColumnData c : cd) {
            c.setDatabaseConnection(dc);
        }
    }

    private void connectionChanged() {
        // retrieve connection selection
        DatabaseConnection connection =
                (DatabaseConnection) connectionsCombo.getSelectedItem();

        // reset meta data
        tablePanel.setDatabaseConnection(connection);
        columnChangeConnection(connection);

        // reset schema values

        // reset data types
        try {
            populateDataTypes(getDatabaseConnection().getDataTypesArray(), getDatabaseConnection().getIntDataTypesArray());
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("error.retrieving", bundledString("data-types"), bundledString("selected-connection"), e.getExtendedMessage()),
                    e);
            populateDataTypes(new String[0], new int[0]);
        }
        populateTablespaces(connection);


    }

    private void populateTablespaces(DatabaseConnection connection) {
        List<NamedObject> tss = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLESPACE]);
        if (tss == null)
            tablespacesCombo.setEnabled(false);
        else {
            Vector<NamedObject> vector = new Vector<>();
            vector.add(null);
            vector.addAll(tss);
            tablespaceComboModel.setElements(vector);
        }
    }

    private void populateDataTypes(final String[] dataTypes, final int[] intDataTypes) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                tablePanel.setDataTypes(dataTypes, intDataTypes);
                tablePanel.setDomains(getDomains());
                tablePanel.setGenerators(getGenerators());
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
        Collections.addAll(ccv, cca);
        consPanel.setData(ccv, fillCombos);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
    }

    public void resetSQLText() {
        tablePanel.resetSQLText();
        consPanel.resetSQLText();
    }

    @Override
    public void setSQLText() {
        setSQLText(generateQuery());
    }

    private void setSQLText(final String text) {
        GUIUtils.invokeLater(() -> sqlText.setSQLText(text));
    }

    public String getSQLText() {
        return sqlText.getSQLText();
    }

    @Override
    public String getTableName() {
        return nameField.getText();
    }

    // -----------------------------------------------


    // constraints panel only
    public void updateCellEditor(int col, int row, String value) {
    }

    public void columnValuesChanging(int col, int row, String value) {
    }

    public Vector getTableColumnDataVector() {
        return tablePanel.getTableColumnDataVector();
    }

    public void stateChanged(ChangeEvent e) {
        if (tabbedPane.getSelectedIndex() == 1) {
            checkFullType();
        }
        if (e.getSource() == sqlText)
            setSQLText();
    }

    protected boolean checkFullType() {
        for (int i = 0; i < getTableColumnData().length; i++) {
            if (getTableColumnData()[i].getColumnType() == null) {
                GUIUtilities.displayErrorMessage(bundledString("error.select-type"));
                tabbedPane.setSelectedIndex(0);
                return false;
            }
        }
        return true;
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
        String tableName;
        ColumnData[] cda = tablePanel.getTableColumnData();
        ColumnConstraint[] cca = consPanel.getColumnConstraintArray();

        for (ColumnData columnData : cda) {

            // reset the keys
            columnData.setPrimaryKey(false);
            columnData.setForeignKey(false);
            columnData.resetConstraints();

            tableName = columnData.getTableName();

            String columnName = columnData.getColumnName();

            for (ColumnConstraint columnConstraint : cca) {

                String constraintColumn = columnConstraint.getColumn();

                if (constraintColumn != null
                        && constraintColumn.equalsIgnoreCase(columnName)) {

                    if (columnConstraint.isPrimaryKey()) {
                        columnData.setPrimaryKey(true);
                    } else if (columnConstraint.isForeignKey()) {
                        columnData.setForeignKey(true);
                    }

                    columnConstraint.setTable(tableName);
                    columnConstraint.setNewConstraint(true);
                    columnData.addConstraint(columnConstraint);
                }

            }

        }

        return cda;

    }

    public void columnValuesChanging() {
    }

    @Override
    public List<String> getTables() {
        return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getTableNames();
    }

    @Override
    public List<String> getColumns(String table) {
        return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getColumnNames(table);
    }

    public ColumnData[] getTableColumnData() {
        return tablePanel.getTableColumnData();
    }

    // -----------------------------------------------
    // -------- TableFunction implementations --------
    // -----------------------------------------------

    public void moveColumnUp() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) {
            tablePanel.moveColumnUp();
        }
    }

    public void moveColumnDown() {
        int index = tabbedPane.getSelectedIndex();
        if (index == 0) {
            tablePanel.moveColumnDown();
        }
    }

    public void deleteRow() {
        if (tabbedPane.getSelectedIndex() == 0) {
            tablePanel.deleteRow();
        } else if (tabbedPane.getSelectedIndex() == 1) {
            consPanel.deleteSelectedRow();
        }
    }

    public void insertBefore() {
        tablePanel.insertBefore();
    }

    public void insertAfter() {
        if (tabbedPane.getSelectedIndex() == 0) {
            tablePanel.insertAfter();
        } else if (tabbedPane.getSelectedIndex() == 1) {
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

    protected String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

    private String bundledString(String key) {
        return Bundles.get("CreateTableFunctionPanel." + key);
    }

    private String bundledString(String key, Object... args) {
        return Bundles.get("CreateTableFunctionPanel." + key, args);
    }

}















