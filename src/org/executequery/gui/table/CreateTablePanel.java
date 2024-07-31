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
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.T;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.AbstractSQLSecurityObjectPanel;
import org.executequery.gui.databaseobjects.CreateGlobalTemporaryTable;
import org.executequery.gui.erd.ErdNewTableDialog;
import org.executequery.gui.erd.ErdPopupMenu;
import org.executequery.gui.procedure.DefinitionPanel;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
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
        DefinitionPanel,
        TableConstraintFunction,
        TextEditorContainer {

    public static final String TITLE = bundledString("title");
    public static final String FRAME_ICON = "icon_table_add";

    // --- GUI components ---

    private JComboBox<?> tablespacesCombo;
    private JComboBox<?> temporaryTypeCombo;

    private JCheckBox isExternalCheck;
    private JCheckBox isAdapterNeededCheck;
    protected JCheckBox showTableCommentCheck;
    protected JCheckBox showFieldCommentCheck;

    private DynamicComboBoxModel tablespaceComboModel;

    private JButton browseExternalFileButton;
    private JTextField externalFileField;

    private JPanel externalPanel;
    private NewTablePanel tablePanel;
    private SimpleSqlTextPanel sqlText;
    private NewTableConstraintsPanel consPanel;

    private CreateTableToolBar columnsToolBar;
    private CreateTableToolBar constraintsToolBar;

    // ---

    public CreateTablePanel(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog, null, null);
    }

    // --- AbstractCreateObjectPanel impl ---

    @Override
    protected void init() {
        initSQLSecurity();

        securityCombo.addActionListener(e -> setSQLText());
        connectionsCombo.addItemListener(this);

        columnsToolBar = new CreateTableToolBar(this);
        constraintsToolBar = new CreateTableToolBar(this);

        tablespaceComboModel = new DynamicComboBoxModel(new Vector<>());
        tablespacesCombo = WidgetFactory.createComboBox("tablespaceCombo", tablespaceComboModel);
        tablespacesCombo.addItemListener(this);

        sqlText = new SimpleSqlTextPanel();
        tablePanel = new NewTablePanel(this);

        consPanel = new NewTableConstraintsPanel(this);
        consPanel.setData(new Vector<>(0), true);

        temporaryTypeCombo = WidgetFactory.createComboBox("temporaryTypeCombo", new String[]{"DELETE ROWS", "PRESERVE ROWS"});
        temporaryTypeCombo.addActionListener(e -> setSQLText());

        isExternalCheck = WidgetFactory.createCheckBox("isExternalCheck", bundledString("IsExternalTableText"));
        isExternalCheck.addActionListener(e -> externalTablePropsChanged());

        showTableCommentCheck = WidgetFactory.createCheckBox("showTableCommentCheck", Bundles.get(ErdPopupMenu.class, "DisplayCommentOnTable"));
        showFieldCommentCheck = WidgetFactory.createCheckBox("showFieldCommentCheck", Bundles.get(ErdPopupMenu.class, "DisplayCommentsOnFields"));

        externalFileField = WidgetFactory.createTextField("externalFileField");
        externalFileField.getDocument().addDocumentListener(new DocumentListener() {

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

        browseExternalFileButton = WidgetFactory.createButton("browseExternalTableFileButton", Bundles.get("common.browse.button"));
        browseExternalFileButton.addActionListener(e -> browseExternalTableFile());

        isAdapterNeededCheck = WidgetFactory.createCheckBox("isAdapterNeededCheck", bundledString("IsAdapterNeededText"));
        isAdapterNeededCheck.addActionListener(e -> externalTablePropsChanged());

        arrange();
        addListeners();

        if (connection != null) {
            tablePanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
            tablePanel.setDomains(getDomains());
            tablePanel.setGenerators(getGenerators());
        } else
            tablePanel.setDataTypes(T.DEFAULT_TYPES, DatabaseTypeConverter.getSQLDataTypesFromNames(T.DEFAULT_TYPES));

        tablePanel.setDatabaseConnection(connection);
        if (connection != null)
            populateTablespaces(connection);

        externalTablePropsChanged();
        constraintsToolBar.enableButtons(true);
        columnsToolBar.enableButtons(true);
        centralPanel.setVisible(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected String generateQuery() {

        String tablespace = null;
        if (tablespacesCombo.getSelectedItem() != null)
            tablespace = ((NamedObject) tablespacesCombo.getSelectedItem()).getName();

        String adapter = null;
        String externalFile = null;

        if (isExternalCheck.isSelected()) {
            externalFile = externalFileField.getText();
            if (isAdapterNeededCheck.isSelected())
                adapter = "CSV";
        }

        String comment = null;
        if (!Objects.equals(simpleCommentPanel.getComment(), ""))
            comment = simpleCommentPanel.getComment().trim();

        return SQLUtils.generateCreateTable(
                nameField.getText(),
                tablePanel.getTableColumnDataVector(),
                consPanel.getKeys(),
                false,
                this instanceof CreateGlobalTemporaryTable,
                true,
                true,
                true,
                "ON COMMIT " + temporaryTypeCombo.getSelectedItem(),
                externalFile,
                adapter,
                (String) securityCombo.getSelectedItem(),
                tablespace,
                comment,
                "^"
        );
    }

    @Override
    public void createObject() {
        if (checkFullType())
            displayExecuteQueryDialog(getSQLText(), "^");
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.TABLE];
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
    protected void initEdited() {
    }

    @Override
    protected void reset() {
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
    }

    @Override
    public void setParameters(Object[] params) {
    }

    // ---

    private void arrange() {
        GridBagHelper gbh;

        // --- columns panel ---

        JPanel columnsPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setDefaultsStatic();
        columnsPanel.add(columnsToolBar, gbh.setLabelDefault().fillVertical().spanY().get());
        columnsPanel.add(tablePanel, gbh.nextCol().fillBoth().spanX().spanY().get());

        // --- constraints panel ---

        JPanel constraintsPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setDefaultsStatic();
        constraintsPanel.add(constraintsToolBar, gbh.setLabelDefault().fillVertical().spanY().get());
        constraintsPanel.add(consPanel, gbh.nextCol().fillBoth().spanX().spanY().get());

        // --- erd panel ---

        JPanel erdPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().rightGap(5).fillHorizontally();
        erdPanel.add(showTableCommentCheck, gbh.get());
        erdPanel.add(showFieldCommentCheck, gbh.nextCol().rightGap(0).get());
        erdPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().get());

        // --- external panel ---

        externalPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().rightGap(5).leftGap(2).fillHorizontally();
        externalPanel.add(new JLabel(bundledString("ExternalTableDataFileLabel")), gbh.setMinWeightX().get());
        externalPanel.add(externalFileField, gbh.nextCol().leftGap(0).setMaxWeightX().get());
        externalPanel.add(browseExternalFileButton, gbh.nextCol().setMinWeightX().rightGap(0).get());

        // --- top panel ---

        if (!(this instanceof CreateGlobalTemporaryTable)) {
            topPanel.add(new JLabel(bundledString("Tablespace")), topGbh.nextCol().topGap(3).setMinWeightX().get());
            topPanel.add(tablespacesCombo, topGbh.nextCol().topGap(0).setMaxWeightX().get());

            if (connection != null && getDatabaseVersion() >= 3) {
                topPanel.add(isExternalCheck, topGbh.nextRowFirstCol().leftGap(2).setMinWeightX().setWidth(1).get());
                topPanel.add(isAdapterNeededCheck, topGbh.nextCol().get());
                topPanel.add(externalPanel, topGbh.nextRowFirstCol().fillHorizontally().spanX().get());
            }
        }

        if (this instanceof CreateGlobalTemporaryTable) {
            topPanel.add(new JLabel(bundledString("TypeTemporaryTable")), topGbh.nextCol().topGap(3).setMinWeightX().get());
            topPanel.add(temporaryTypeCombo, topGbh.nextCol().topGap(0).setMaxWeightX().get());
        }

        if (this instanceof ErdNewTableDialog.CreateTableERDPanel)
            topPanel.add(erdPanel, topGbh.nextRowFirstCol().fillHorizontally().spanX().get());

        // --- tabbed pane filling ---

        tabbedPane.add(bundledString("Columns"), columnsPanel);
        tabbedPane.add(bundledString("Constraints"), constraintsPanel);
        addCommentTab(null);
        tabbedPane.addTab("SQL", sqlText);
        tabbedPane.addChangeListener(this);

        // --- base ---

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    private void addListeners() {

        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        };

        nameField.addKeyListener(keyAdapter);
        simpleCommentPanel.getCommentField().getTextAreaComponent().addKeyListener(keyAdapter);
    }

    protected void addButtonsPanel(JPanel buttonsPanel) {
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void externalTablePropsChanged() {
        boolean isExternal = isExternalCheck.isSelected();
        externalPanel.setVisible(isExternal);
        isAdapterNeededCheck.setVisible(isExternal);

        setSQLText();
    }

    public void browseExternalTableFile() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setDialogTitle(bundledString("OpenFileDialogText"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setMultiSelectionEnabled(false);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), Bundles.get("common.select.button"));
        if (result == JFileChooser.CANCEL_OPTION)
            return;

        File file = fileChooser.getSelectedFile();
        if (!file.exists()) {
            GUIUtilities.displayWarningMessage(bundledString("FileDoesNotExistMessage"));
            return;
        }

        externalFileField.setText(file.getAbsolutePath());
        externalTablePropsChanged();
    }

    private String[] getDomains() {
        List<String> domains = getDatabaseObjectNames(getSelectedConnection(), NamedObject.DOMAIN);
        return domains.toArray(new String[0]);
    }

    private String[] getGenerators() {
        List<String> generators = getDatabaseObjectNames(getSelectedConnection(), NamedObject.SEQUENCE);
        return generators.toArray(new String[0]);
    }

    private void columnChangeConnection(DatabaseConnection dc) {
        for (Object object : getTableColumnDataVector())
            if (object instanceof ColumnData)
                ((ColumnData) object).setConnection(dc);
    }

    private void connectionChanged() {

        DatabaseConnection selectedConnection = getSelectedConnection();
        tablePanel.setDatabaseConnection(selectedConnection);
        columnChangeConnection(selectedConnection);

        try {
            DatabaseConnection databaseConnection = getDatabaseConnection();
            populateDataTypes(databaseConnection.getDataTypesArray(), databaseConnection.getIntDataTypesArray());

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("error.retrieving",
                            bundledString("data-types"),
                            bundledString("selected-connection"),
                            e.getExtendedMessage()),
                    e,
                    this.getClass()
            );

            populateDataTypes(new String[0], new int[0]);
        }

        populateTablespaces(selectedConnection);
    }

    private void populateTablespaces(DatabaseConnection connection) {

        List<NamedObject> tablespacesList = getDatabaseObjects(connection, NamedObject.TABLESPACE);
        if (tablespacesList == null) {
            tablespacesCombo.setEnabled(false);
            return;
        }

        Vector<NamedObject> vector = new Vector<>();
        vector.add(null);
        vector.addAll(tablespacesList);
        tablespaceComboModel.setElements(vector);
    }

    private void populateDataTypes(final String[] dataTypes, final int[] intDataTypes) {
        GUIUtils.invokeAndWait(() -> {
            tablePanel.setDataTypes(dataTypes, intDataTypes);
            tablePanel.setDomains(getDomains());
            tablePanel.setGenerators(getGenerators());
        });
    }

    public void setFocusComponent() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    public void setSQLTextCaretPosition(int position) {
        sqlText.setCaretPosition(position);
    }

    public void fireEditingStopped() {
        tablePanel.fireEditingStopped();
        consPanel.fireEditingStopped();
    }

    public void setColumnDataArray(ColumnData[] columnData) {
        tablePanel.setColumnDataArray(columnData);
    }

    public void setColumnConstraintVector(Vector<?> constraintsVector, boolean fillCombos) {
        consPanel.setData(constraintsVector, fillCombos);
    }

    public void resetSQLText() {
        tablePanel.resetSQLText();
        consPanel.resetSQLText();
    }

    private void setSQLText(final String text) {
        GUIUtils.invokeLater(() -> sqlText.setSQLText(text));
    }

    public String getSQLText() {
        return sqlText.getSQLText();
    }

    private boolean checkFullType() {

        for (ColumnData columnData : getTableColumnData()) {
            if (columnData.getTypeName() == null) {
                GUIUtilities.displayErrorMessage(bundledString("error.select-type"));
                tabbedPane.setSelectedIndex(0);
                return false;
            }
        }

        return true;
    }

    public ColumnData[] getTableColumnDataAndConstraints() {

        ColumnData[] tableColumnData = tablePanel.getTableColumnData();
        ColumnConstraint[] constraintsArray = consPanel.getColumnConstraintArray();

        for (ColumnData columnData : tableColumnData) {
            columnData.setForeignKey(false);
            columnData.resetConstraints();

            String tableName = columnData.getTableName();
            String columnName = columnData.getColumnName();

            for (ColumnConstraint constraint : constraintsArray) {

                String constraintColumn = constraint.getColumn();
                if (constraintColumn == null || !constraintColumn.equalsIgnoreCase(columnName))
                    continue;

                columnData.setPrimaryKey(constraint.isPrimaryKey());
                columnData.setForeignKey(constraint.isForeignKey());
                columnData.setUniqueKey(constraint.isUniqueKey());

                constraint.setTable(tableName);
                constraint.setNewConstraint(true);
                columnData.addConstraint(constraint);
            }
        }

        return tableColumnData;
    }

    private static DefaultDatabaseHost getDefaultDatabaseHost(DatabaseConnection connection) {
        return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection);
    }

    @SuppressWarnings("SameParameterValue")
    private static List<NamedObject> getDatabaseObjects(DatabaseConnection connection, int metaTag) {
        return getDefaultDatabaseHost(connection).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[metaTag]);
    }

    private static List<String> getDatabaseObjectNames(DatabaseConnection connection, int metaTag) {
        return getDefaultDatabaseHost(connection).getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[metaTag]);
    }

    // --- ItemListener impl ---

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    @Override
    public void itemStateChanged(ItemEvent event) {

        if (event.getStateChange() == ItemEvent.DESELECTED)
            return;

        final Object source = event.getSource();
        if (event.getSource() == tablespacesCombo) {
            setSQLText();
            return;
        }

        GUIUtils.startWorker(() -> {
            if (source == connectionsCombo)
                connectionChanged();
        });
    }

    // --- TableModifier impl ---

    @Override
    public void setSQLText() {
        setSQLText(generateQuery());
    }

    @Override
    public String getTableName() {
        return nameField.getText();
    }

    // --- ChangeListener impl ---

    @Override
    public void stateChanged(ChangeEvent e) {

        if (tabbedPane.getSelectedIndex() == 1)
            checkFullType();

        if (e.getSource() == sqlText)
            setSQLText();
    }

    // --- TableConstraintFunction impl ---

    @Override
    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionsCombo.getSelectedItem();
    }

    @Override
    public List<String> getTables() {
        return getDefaultDatabaseHost(connection).getTableNames();
    }

    @Override
    public List<String> getColumns(String table) {
        return getDefaultDatabaseHost(connection).getColumnNames(table);
    }

    @Override
    public ColumnData[] getTableColumnData() {
        return tablePanel.getTableColumnData();
    }

    @Override
    public Vector<?> getTableColumnDataVector() {
        return tablePanel.getTableColumnDataVector();
    }

    // --- DefinitionPanel impl ---

    @Override
    public void moveRowUp() {
        if (tabbedPane.getSelectedIndex() == 0)
            tablePanel.moveColumnUp();
    }

    @Override
    public void moveRowDown() {
        if (tabbedPane.getSelectedIndex() == 0)
            tablePanel.moveColumnDown();
    }

    @Override
    public void deleteRow() {
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex == 0)
            tablePanel.deleteRow();
        else if (selectedIndex == 1)
            consPanel.deleteSelectedRow();
    }

    @Override
    public void addRow() {
        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex == 0)
            tablePanel.insertAfter();
        else if (selectedIndex == 1)
            consPanel.insertRowAfter();
    }

    // --- TextEditorContainer impl ---

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    @Override
    public TextEditor getTextEditor() {
        return sqlText;
    }

    // --- FocusComponentPanel impl ---

    /**
     * Returns the table name field.
     */
    @Override
    public Component getDefaultFocusComponent() {
        return nameField;
    }

    // ---

    private static String bundledString(String key, Object... args) {
        return Bundles.get(CreateTablePanel.class, key, args);
    }

}
