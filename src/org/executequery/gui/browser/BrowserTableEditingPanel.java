/*
 * BrowserTableEditingPanel.java
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

package org.executequery.gui.browser;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.table.DoubleCellRenderer;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.*;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DatabaseTableEvent;
import org.executequery.event.DatabaseTableEventListener;
import org.executequery.gui.*;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.databaseobjects.*;
import org.executequery.gui.erd.ErdTableInfo;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.procedure.DefinitionPanel;
import org.executequery.gui.table.EditConstraintPanel;
import org.executequery.gui.table.InsertColumnPanel;
import org.executequery.gui.table.KeyCellRenderer;
import org.executequery.gui.table.TableConstraintFunction;
import org.executequery.gui.text.SimpleCommentPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.print.TablePrinter;
import org.executequery.toolbars.AbstractTableIndexesToolBar;
import org.executequery.toolbars.AbstractTableToolBar;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * @author Takis Diakoumis
 */
public class BrowserTableEditingPanel extends AbstractFormObjectViewPanel
        implements ActionListener,
        DatabaseTableEventListener,
        FocusListener,
        TableConstraintFunction,
        DefinitionPanel,
        ChangeListener,
        VetoableChangeListener {

    public static final String NAME = "BrowserTableEditingPanel";
    private static final int TABLE_COLUMNS_INDEX = 0;
    private static final int TABLE_CONSTRAINTS_INDEX = TABLE_COLUMNS_INDEX + 1;
    private static final int TABLE_INDEXES_INDEX = TABLE_CONSTRAINTS_INDEX + 1;
    private static final int TABLE_TRIGGERS_INDEX = TABLE_INDEXES_INDEX + 1;
    private static final int TABLE_PRIVILEGES_INDEX = TABLE_TRIGGERS_INDEX + 1;
    private static final int TABLE_REFERENCES_INDEX = TABLE_PRIVILEGES_INDEX + 1;
    private static final int TABLE_DATA_TAB_INDEX = TABLE_REFERENCES_INDEX + 1;
    private static final int TABLE_SQL_INDEX = TABLE_DATA_TAB_INDEX + 1;
    private static final int TABLE_METADATA_INDEX = TABLE_SQL_INDEX + 1;
    private static final int TABLE_DEPENDENCIES_INDEX = TABLE_METADATA_INDEX + 1;
    private static final int TABLE_PROPERTIES_INDEX = TABLE_DEPENDENCIES_INDEX + 1;
    private static final int SQL_PANE_INDEX = 6;

    // --- GUI components ---

    private DisabledField adapterField;
    private DisabledField rowCountField;
    private DisabledField tableNameField;
    private DisabledField externalFileField;

    private JTabbedPane tabPane;
    private TableDataTab tableDataPanel;

    private JPanel indexesPanel;
    private JPanel triggersPanel;
    private JPanel descriptionPanel;
    private JPanel constraintsPanel;
    private JPanel buttonsEditingColumnPanel;
    private JPanel buttonsEditingIndexesPanel;
    private JPanel buttonsEditingTriggersPanel;
    private JPanel buttonsEditingConstraintPanel;

    private PropertiesPanel propertiesPanel;
    private SimpleSqlTextPanel alterSqlText;
    private SimpleSqlTextPanel createSqlText;
    private DependenciesPanel dependenciesPanel;
    private ReferencesDiagramPanel referencesPanel;
    private DatabaseObjectMetaDataPanel metaDataPanel;

    private JTable triggersTable;
    private TableTriggersTableModel triggersTableModel;

    private JTable columnIndexTable;
    private TableColumnIndexTableModel columnIndexTableModel;

    private EditableDatabaseTable descriptionTable;
    private EditableColumnConstraintTable constraintsTable;

    private JButton applyButton;
    private JButton cancelButton;
    private JButton rowsCountButton;

    // ---

    private final BrowserController controller;

    private Timer timer;
    private Semaphore lock;
    private SwingWorker worker;
    private DatabaseTable table;
    private StringBuffer sbTemp;
    private TextEditor lastFocusEditor;
    private List<JComponent> externalFileComponents;

    /**
     * Indicates that the references tab has been previously displayed
     * for the current selection. Aims to keep any changes made to the
     * references ERD (moving tables around) stays as was previously set.
     */
    private boolean referencesLoaded;
    private boolean loadingRowCount;
    private boolean tableResetEventTriggered;

    public BrowserTableEditingPanel(BrowserController controller) {
        this.controller = controller;
        init();
        arrange();

        EventMediator.registerListener(this);
    }

    private void init() {

        lock = new Semaphore(1);
        sbTemp = new StringBuffer(100);
        externalFileComponents = new ArrayList<>();

        // --- columnIndexTable ---

        columnIndexTableModel = new TableColumnIndexTableModel();

        columnIndexTable = new DefaultTable();
        columnIndexTable.setModel(new TableSorter(columnIndexTableModel, columnIndexTable.getTableHeader()));
        columnIndexTable.setColumnSelectionAllowed(false);
        columnIndexTable.getTableHeader().setReorderingAllowed(false);
        columnIndexTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editColumnIndex(e);
            }
        });
        columnIndexTable.setDefaultRenderer(Double.class, new DoubleCellRenderer());

        // --- triggersTable ---

        triggersTableModel = new TableTriggersTableModel();
        triggersTable = new DefaultTable();
        triggersTable.setModel(new TableSorter(triggersTableModel, triggersTable.getTableHeader()));
        triggersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editTrigger(e);
            }
        });

        // --- constraintsTable ---

        constraintsTable = new EditableColumnConstraintTable();
        constraintsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editConstraint(e);
            }
        });

        // --- descriptionTable ---

        descriptionTable = new EditableDatabaseTable();
        descriptionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                editDescription(e);
            }
        });

        // --- alterSqlText ---

        alterSqlText = new SimpleSqlTextPanel();
        alterSqlText.setBorder(BorderFactory.createTitledBorder(bundleString("alter-table")));
        alterSqlText.setPreferredSize(new Dimension(100, 100));
        alterSqlText.getEditorTextComponent().addFocusListener(this);

        // --- createSqlText ---

        createSqlText = new SimpleSqlTextPanel();
        createSqlText.setBorder(BorderFactory.createTitledBorder(bundleString("create-table")));
        createSqlText.getEditorTextComponent().addFocusListener(this);

        // --- sqlSplitPane ---

        FlatSplitPane sqlSplitPane = new FlatSplitPane(FlatSplitPane.VERTICAL_SPLIT);
        sqlSplitPane.setTopComponent(alterSqlText);
        sqlSplitPane.setBottomComponent(createSqlText);
        sqlSplitPane.setDividerSize(7);
        sqlSplitPane.setResizeWeight(0.25);

        // --- panels ---

        indexesPanel = new JPanel(new GridBagLayout());
        indexesPanel.setBorder(BorderFactory.createTitledBorder(bundleString("table-indexes")));

        triggersPanel = new JPanel(new GridBagLayout());
        triggersPanel.setBorder(BorderFactory.createTitledBorder(bundleString("table-triggers")));

        constraintsPanel = new JPanel(new GridBagLayout());
        constraintsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("table-keys")));

        descriptionPanel = new JPanel(new GridBagLayout());
        descriptionPanel.setBorder(BorderFactory.createTitledBorder(bundleString("table-columns")));

        propertiesPanel = new PropertiesPanel();
        dependenciesPanel = new DependenciesPanel();
        referencesPanel = new ReferencesDiagramPanel();
        metaDataPanel = new DatabaseObjectMetaDataPanel();
        tableDataPanel = new TableDataTab(true);

        // --- disabled fields ---

        adapterField = new DisabledField();
        rowCountField = new DisabledField();
        tableNameField = new DisabledField();
        externalFileField = new DisabledField();

        // --- tabPane --

        VetoableSingleSelectionModel model = new VetoableSingleSelectionModel();
        model.addVetoableChangeListener(this);

        tabPane = new JTabbedPane();
        tabPane.setModel(model);
        tabPane.addChangeListener(this);

        tabPane.add(Bundles.getCommon("columns"), descriptionPanel);
        tabPane.add(Bundles.getCommon("constraints"), constraintsPanel);
        tabPane.add(Bundles.getCommon("indexes"), indexesPanel);
        tabPane.add(Bundles.getCommon("triggers"), triggersPanel);
        addPrivilegesTab(tabPane, null);
        tabPane.add(Bundles.getCommon("references"), referencesPanel);
        tabPane.add(Bundles.getCommon("data"), tableDataPanel);
        tabPane.add(Bundles.getCommon("SQL"), sqlSplitPane);
        tabPane.add(Bundles.getCommon("metadata"), metaDataPanel);
        tabPane.add(Bundles.getCommon("dependencies"), dependenciesPanel);
        tabPane.add(bundleString("preferences-panel-label"), propertiesPanel);

        // --- buttons ---

        applyButton = new DefaultPanelButton(Bundles.get("common.apply.button"));
        applyButton.addActionListener(this);

        cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(this);

        rowsCountButton = new DefaultPanelButton(Bundles.getCommon("get-rows-count"));
        rowsCountButton.addActionListener(this);

        // ---

        createButtonsEditingIndexesPanel();
        createButtonsEditingTriggersPanel();
        createButtonsEditingColumnPanel();
        createButtonsEditingConstraintPanel();
    }

    private void createButtonsEditingIndexesPanel() {

        buttonsEditingIndexesPanel = new AbstractTableIndexesToolBar(
                bundledString("toolbar.create.index"),
                bundledString("toolbar.delete.index"),
                bundledString("toolbar.Refresh"),
                bundledString("toolbar.Selectivity")
        ) {

            @Override
            public void insert(ActionEvent e) {
                addRow();
            }

            @Override
            public void delete(ActionEvent e) {
                deleteRow();
            }

            @Override
            public void refresh(ActionEvent e) {
                if (table instanceof DefaultDatabaseTable)
                    ((DefaultDatabaseTable) table).clearIndexes();
                loadIndexes();
            }

            @Override
            public void reselectivity(ActionEvent e) {
                reselectAllIndexes();
            }
        };
    }

    private void createButtonsEditingTriggersPanel() {
        buttonsEditingTriggersPanel = new AbstractTableToolBar(
                bundledString("toolbar.create.trigger"),
                bundledString("toolbar.delete.trigger"),
                bundledString("toolbar.Refresh")
        ) {
            @Override
            public void insert(ActionEvent e) {
                addRow();
            }

            @Override
            public void delete(ActionEvent e) {
                deleteRow();
            }

            @Override
            public void refresh(ActionEvent e) {
                if (table instanceof DefaultDatabaseTable)
                    ((DefaultDatabaseTable) table).clearTriggers();
                loadTriggers();
            }
        };
    }

    private void createButtonsEditingColumnPanel() {

        RolloverButton addColumnButton = WidgetFactory.createRolloverButton(
                "addColumnButton",
                bundledString("toolbar.create.column"),
                "icon_add",
                actionEvent -> addRow()
        );

        RolloverButton deleteColumnButton = WidgetFactory.createRolloverButton(
                "deleteColumnButton",
                bundledString("toolbar.delete.column"),
                "icon_delete",
                actionEvent -> deleteRow()
        );

        RolloverButton moveUpColumnButton = WidgetFactory.createRolloverButton(
                "moveUpColumnButton",
                bundledString("toolbar.MoveUp"),
                "icon_move_up",
                actionEvent -> moveRowUp()
        );

        RolloverButton moveDownColumnButton = WidgetFactory.createRolloverButton(
                "moveDownColumnButton",
                bundledString("toolbar.MoveDown"),
                "icon_move_down",
                actionEvent -> moveRowDown()
        );

        RolloverButton commitColumnsButton = WidgetFactory.createRolloverButton(
                "commitColumnsButton",
                bundledString("toolbar.Commit"),
                "icon_commit",
                actionEvent -> commitColumnsChanges()
        );

        RolloverButton rollbackColumnsButton = WidgetFactory.createRolloverButton(
                "rollbackColumnsButton",
                bundledString("toolbar.Rollback"),
                "icon_rollback",
                actionEvent -> refresh()
        );

        PanelToolBar bar = new PanelToolBar();
        bar.add(addColumnButton);
        bar.add(deleteColumnButton);
        bar.add(moveUpColumnButton);
        bar.add(moveDownColumnButton);
        bar.add(commitColumnsButton);
        bar.add(rollbackColumnsButton);

        buttonsEditingColumnPanel = new JPanel(new GridBagLayout());
        buttonsEditingColumnPanel.add(bar, new GridBagConstraints(
                4, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0),
                0, 0
        ));
    }

    private void createButtonsEditingConstraintPanel() {

        RolloverButton addConstraintButton = WidgetFactory.createRolloverButton(
                "addConstraintButton",
                bundledString("toolbar.create.constraint"),
                "icon_add",
                actionEvent -> addRow()
        );

        RolloverButton deleteConstraintButton = WidgetFactory.createRolloverButton(
                "deleteConstraintButton",
                bundledString("toolbar.delete.constraint"),
                "icon_delete",
                actionEvent -> deleteRow()
        );

        RolloverButton commitConstraintsButton = WidgetFactory.createRolloverButton(
                "commitConstraintsButton",
                bundledString("toolbar.Commit"),
                "icon_commit",
                actionEvent -> commitColumnsChanges()
        );

        RolloverButton rollbackConstraintsButton = WidgetFactory.createRolloverButton(
                "rollbackConstraintsButton",
                bundledString("toolbar.Rollback"),
                "icon_rollback",
                actionEvent -> refresh()
        );

        PanelToolBar bar = new PanelToolBar();
        bar.add(addConstraintButton);
        bar.add(deleteConstraintButton);
        bar.add(commitConstraintsButton);
        bar.add(rollbackConstraintsButton);

        buttonsEditingConstraintPanel = new JPanel(new GridBagLayout());
        buttonsEditingConstraintPanel.add(bar, new GridBagConstraints(
                4, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0),
                0, 0
        ));
    }

    private void arrange() {

        GridBagHelper gbh;

        // --- column indexes panel ---

        gbh = new GridBagHelper().setInsets(2, 2, 2, 2).anchorNorthWest().fillHorizontally();
        indexesPanel.add(buttonsEditingIndexesPanel, gbh.nextCol().setMinWeightX().setMinWeightY().get());
        indexesPanel.add(new JScrollPane(columnIndexTable), gbh.nextRow().anchorSouth().fillBoth().spanX().spanY().get());

        // --- table triggers panel ---

        gbh = new GridBagHelper().setDefaults(GridBagHelper.DEFAULT_CONSTRAINTS).defaults();
        triggersPanel.add(buttonsEditingTriggersPanel, gbh.get());
        triggersPanel.add(new JScrollPane(triggersTable), gbh.anchorSouthEast().fillBoth().spanX().spanY().nextRow().get());

        // --- constraints panel ---

        gbh = new GridBagHelper().setInsets(2, 2, 2, 2).anchorNorthWest().fillHorizontally();
        constraintsPanel.add(buttonsEditingConstraintPanel, gbh.nextCol().setMinWeightX().setMinWeightY().get());
        constraintsPanel.add(new JScrollPane(constraintsTable), gbh.nextRow().anchorSouth().fillBoth().spanX().spanY().get());

        // --- description panel ---

        gbh = new GridBagHelper().setInsets(2, 2, 2, 2).anchorNorthWest().fillHorizontally();
        descriptionPanel.add(buttonsEditingColumnPanel, gbh.nextCol().setMinWeightX().setMinWeightY().get());
        descriptionPanel.add(new JScrollPane(descriptionTable), gbh.nextRow().anchorSouth().fillBoth().spanX().spanY().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());
        JLabel externalFileLabel = new JLabel(bundleString("external-file"));
        JLabel adapterLabel = new JLabel(bundleString("adapter"));

        gbh = new GridBagHelper().setInsets(5, 10, 5, 0).anchorNorthWest().fillBoth();
        gbh.setDefaults(gbh.get());

        gbh.addLabelFieldPair(mainPanel, bundleString("table-name"), tableNameField, null);
        gbh.addLabelFieldPair(mainPanel, externalFileLabel, externalFileField, null);
        gbh.addLabelFieldPair(mainPanel, adapterLabel, adapterField, null);
        mainPanel.add(tabPane, gbh.nextRowFirstCol().fillBoth().spanY().get());

        // --- hide external components ---

        externalFileComponents.add(externalFileLabel);
        externalFileComponents.add(externalFileField);
        externalFileComponents.add(adapterLabel);
        externalFileComponents.add(adapterField);
        for (JComponent component : externalFileComponents)
            component.setVisible(false);

        // --- base ---

        setContentPanel(mainPanel);
    }

    private void editColumnIndex(MouseEvent e) {

        if (e.getClickCount() < 2)
            return;

        if (columnIndexTable.getSelectedRow() >= 0) {

            int row = ((TableSorter) columnIndexTable.getModel()).modelIndex(columnIndexTable.getSelectedRow());
            DefaultDatabaseIndex index = ((TableColumnIndexTableModel) ((TableSorter) columnIndexTable.getModel()).getTableModel()).getIndexes().get(row);
            BaseDialog dialog = new BaseDialog("Edit Index", true);
            CreateIndexPanel panel = new CreateIndexPanel(table.getHost().getDatabaseConnection(), dialog, index, table.getName());
            dialog.addDisplayComponent(panel);
            dialog.display();

            refresh();
        }
    }

    private void editTrigger(MouseEvent e) {

        if (e.getClickCount() < 2 || triggersTable.getSelectedRow() < 0)
            return;

        TableSorter tableSorter = (TableSorter) triggersTable.getModel();
        int triggerIndex = tableSorter.modelIndex(triggersTable.getSelectedRow());
        TableTriggersTableModel tableModel = (TableTriggersTableModel) tableSorter.getTableModel();

        DefaultDatabaseTrigger trigger = tableModel.getTriggers().get(triggerIndex);
        DatabaseConnection connection = table.getHost().getDatabaseConnection();

        String triggerName = trigger.getShortName().trim();
        String title = triggerName + ":TRIGGER:" + connection.getName();

        if (GUIUtilities.getCentralPane(title) == null) {
            CreateTriggerPanel panel = new CreateTriggerPanel(connection, null, trigger, DefaultDatabaseTrigger.TABLE_TRIGGER);
            panel.setDatabaseObjectNode(ConnectionsTreePanel.getPanelFromBrowser().findNode(connection, triggerName, NamedObject.TRIGGER));
            panel.setCurrentPath(panel.getDatabaseObjectNode().getTreePath());

            GUIUtilities.addCentralPane(title, BrowserViewPanel.FRAME_ICON, panel, title, true);
        } else
            GUIUtilities.setSelectedCentralPane(title);
    }

    private void editConstraint(MouseEvent e) {

        if (e.getClickCount() < 2)
            return;

        if (constraintsTable.getSelectedRow() >= 0) {

            int row = ((TableSorter) constraintsTable.getModel()).modelIndex(constraintsTable.getSelectedRow());
            ColumnConstraint constraint = constraintsTable.getColumnConstraintTableModel().getConstraints().get(row);
            BaseDialog dialog = new BaseDialog(EditConstraintPanel.EDIT_TITLE, true);
            EditConstraintPanel panel = new EditConstraintPanel(table, dialog, constraint);
            dialog.addDisplayComponent(panel);
            dialog.display();

            refresh();
        }
    }

    private void editDescription(MouseEvent e) {

        if (e.getClickCount() < 2)
            return;

        if (descriptionTable.getSelectedRow() >= 0) {

            int row = ((TableSorter) descriptionTable.getModel()).modelIndex(descriptionTable.getSelectedRow());
            DatabaseColumn column = descriptionTable.getDatabaseTableModel().getDatabaseColumns().get(row);
            BaseDialog dialog = new BaseDialog(InsertColumnPanel.EDIT_TITLE, true);
            InsertColumnPanel panel = new InsertColumnPanel(table, dialog, column);

            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.display();
            refresh();
        }
    }

    /**
     * Invoked when a SQL text pane gains the keyboard focus.
     */
    @Override
    public void focusGained(FocusEvent e) {

        Object source = e.getSource();
        if (createSqlText.getEditorTextComponent() == source)
            lastFocusEditor = createSqlText;
        else if (alterSqlText.getEditorTextComponent() == source)
            lastFocusEditor = alterSqlText;
    }

    /**
     * Invoked when a SQL text pane loses the keyboard focus.
     * Does nothing here.
     */
    @Override
    public void focusLost(FocusEvent e) {
        if (e.getSource() == alterSqlText)
            table.setModifiedSQLText(alterSqlText.getSQLText());
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (table.isAltered()) {

            Object source = event.getSource();
            if (source == applyButton) {

                try {

                    if (tabPane.getSelectedComponent() == tableDataPanel)
                        tableDataPanel.stopEditing();

                    DatabaseObjectChangeProvider changeProvider = new DatabaseObjectChangeProvider(table);
                    changeProvider.applyChanges();
                    if (changeProvider.applied)
                        setValues(table);

                } catch (DataSourceException e) {
                    GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());
                }

            } else if (source == cancelButton) {
                table.revert();
                setValues(table);
            }

        }

        if (event.getSource() == rowsCountButton) {
            table.resetRowsCount();
            updateRowCount(bundleString("quering"));
            reloadDataRowCount();
        }
    }

    @Override
    public String getLayoutName() {
        return NAME;
    }

    @Override
    public Printable getPrintable() {

        switch (tabPane.getSelectedIndex()) {

            case TABLE_COLUMNS_INDEX:
                return new TablePrinter(descriptionTable,
                        bundleString("description-table") + table.getName());

            case TABLE_CONSTRAINTS_INDEX:
                return new TablePrinter(constraintsTable,
                        bundleString("constraints-table") + table.getName(), false);

            case TABLE_INDEXES_INDEX:
                return new TablePrinter(columnIndexTable,
                        bundleString("indexes-table") + table.getName());

            case TABLE_TRIGGERS_INDEX:
                return new TablePrinter(triggersTable,
                        bundleString("triggers-table") + table.getName());

            case TABLE_REFERENCES_INDEX:
                return referencesPanel.getPrintable();

            case TABLE_DATA_TAB_INDEX:
                return new TablePrinter(tableDataPanel.getTable(),
                        bundleString("data-table") + table.getName());

            case TABLE_METADATA_INDEX:
                return new TablePrinter(metaDataPanel.getTable(),
                        bundleString("metadata-table") + table.getName());

            default:
                return null;
        }
    }

    @Override
    public void cleanup() {

        if (getDatabaseConnection().isConnected()) {
            if (tabPane.getSelectedIndex() == TABLE_DATA_TAB_INDEX) {
                if (tableDataPanel.hasChanges()) {
                    DatabaseObjectChangeProvider provider = new DatabaseObjectChangeProvider(table);
                    provider.applyChanges(true, false);
                }
            }
        }

        super.cleanup();

        if (worker != null) {
            worker.interrupt();
            worker = null;
        }

        if (tableDataPanel != null) {
            tableDataPanel.closeResultSet();
            tableDataPanel.cleanup();
        }

        if (referencesPanel != null)
            referencesPanel.cleanup();
    }


    @Override
    public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {

        if (Integer.parseInt(e.getOldValue().toString()) == TABLE_DATA_TAB_INDEX) {
            tableDataPanel.stopEditing();
            if (tableDataPanel.hasChanges()) {

                DatabaseObjectChangeProvider provider = new DatabaseObjectChangeProvider(table);
                if (!provider.applyChanges(true))
                    throw new PropertyVetoException("User cancelled", e);

                if (provider.lastOption == JOptionPane.NO_OPTION) {
                    tableDataPanel.reset();
                    tableDataPanel.markedForReload = false;
                }
            }
        }
    }

    /**
     * Handles a change tab selection.
     */
    @Override
    public void stateChanged(ChangeEvent e) {

        final int index = tabPane.getSelectedIndex();

        if (index == TABLE_PROPERTIES_INDEX)
            propertiesPanel.update();

        if (index == TABLE_DATA_TAB_INDEX && tableDataPanel.markedForReload)
            tableDataPanel.loadDataForTable(table);

        if (index != TABLE_DATA_TAB_INDEX && tableDataPanel.isExecuting()) {
            tableDataPanel.cancelStatement();
            return;
        }

        tabIndexSelected(index);
    }

    private void tabIndexSelected(int index) {

        switch (index) {

            case TABLE_INDEXES_INDEX:
                loadIndexes();
                break;

            case TABLE_TRIGGERS_INDEX:
                loadTriggers();
                break;

            case TABLE_REFERENCES_INDEX:
                loadReferences();
                break;

            case TABLE_DATA_TAB_INDEX:
                if (!tableDataPanel.isLoaded())
                    tableDataPanel.loadDataForTable(table);
                break;

            case TABLE_SQL_INDEX:
                alterSqlText.setSQLText(table.isAltered() ? table.getAlteredSQLText().trim() : EMPTY);
                break;

            case TABLE_METADATA_INDEX:
                loadTableMetaData();
                break;
        }
    }

    /**
     * Loads database table references.
     */
    private void loadReferences() {

        if (referencesLoaded)
            return;

        GUIUtilities.showWaitCursor();
        try {

            List<ColumnData[]> columns = new ArrayList<>();
            List<String> tableNames = new ArrayList<>();

            List<ColumnConstraint> constraints = table.getConstraints();
            Set<String> tableNamesAdded = new HashSet<>();

            for (ColumnConstraint constraint : constraints) {

                String tableName = constraint.getTableName();
                if (constraint.isPrimaryKey()) {

                    if (!tableNamesAdded.contains(tableName)) {

                        tableNames.add(tableName);
                        tableNamesAdded.add(tableName);
                        columns.add(((DefaultDatabaseHost) table.getHost()).getColumnDataArrayFromTableName(tableName));
                    }

                } else if (constraint.isForeignKey()) {

                    String referencedTable = constraint.getReferencedTable();
                    if (!tableNamesAdded.contains(referencedTable)) {

                        tableNames.add(referencedTable);
                        tableNamesAdded.add(referencedTable);
                        columns.add(((DefaultDatabaseHost) table.getHost()).getColumnDataArrayFromTableName(referencedTable));
                    }

                    //noinspection SuspiciousMethodCalls
                    if (!tableNames.contains(tableName) && !columns.contains(constraint.getColumnName())) {

                        tableNames.add(tableName);
                        tableNamesAdded.add(tableName);
                        columns.add(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getColumnDataArrayFromTableName(tableName));
                    }
                }
            }

            DefaultStatementExecutor querySender = new DefaultStatementExecutor();
            querySender.setDatabaseConnection(table.getHost().getDatabaseConnection());
            String query = "SELECT DISTINCT RC.RDB$RELATION_NAME FROM\n" +
                    "RDB$RELATION_CONSTRAINTS RC \n" +
                    "LEFT JOIN RDB$INDICES I ON RC.RDB$INDEX_NAME=I.RDB$INDEX_NAME \n" +
                    "LEFT JOIN RDB$INDICES I2 ON I.RDB$FOREIGN_KEY=I2.RDB$INDEX_NAME\n" +
                    "WHERE RC.RDB$CONSTRAINT_TYPE='FOREIGN KEY' AND I2.RDB$RELATION_NAME = '" + table.getName() + "'";
            try {
                ResultSet rs = querySender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    tableName = MiscUtils.trimEnd(tableName);
                    if (!tableNames.contains(tableName))
                        tableNames.add(tableName);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                querySender.releaseResources();
            }

           /* List<DatabaseColumn> exportedKeys = table.getExportedKeys();
            for (DatabaseColumn column : exportedKeys) {

                String parentsName = column.getParentsName();
                if (!tableNamesAdded.contains(parentsName)) {

                    tableNames.add(parentsName);
                    tableNamesAdded.add(parentsName);
                    columns.add(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getColumnDataArrayFromTableName(parentsName));
                }
            }*/

            if (tableNames.isEmpty()) {
                tableNames.add(table.getName());
                columns.add(new ColumnData[0]);
            }
            Vector<ErdTableInfo> erdTableInfos = new Vector<>();
            for (String tableName : tableNames) {
                ErdTableInfo etf = new ErdTableInfo();
                etf.setName(tableName);
                etf.setColumns(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getColumnDataArrayFromTableName(etf.getName()));
                AbstractTableObject tableObject = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).getTableFromName(etf.getName());
                if (tableObject != null)
                    etf.setComment(tableObject.getRemarks());
                erdTableInfos.add(etf);
            }

            referencesPanel.setTables(erdTableInfos);

        } catch (DataSourceException e) {
            controller.handleException(e);

        } finally {
            GUIUtilities.showNormalCursor();
        }

        referencesLoaded = true;
    }

    /**
     * Loads database table indexes.
     */
    private void loadTableMetaData() {

        try {
            metaDataPanel.setData(table.getColumnMetaData());

        } catch (DataSourceException e) {
            controller.handleException(e);
            metaDataPanel.setData(null);
        }
    }

    /**
     * Loads database table indexes.
     */
    private synchronized void loadIndexes() {
        try {

            columnIndexTableModel.setIndexData(table.getIndexes());
            columnIndexTable.setCellSelectionEnabled(true);

            TableColumnModel tcm = columnIndexTable.getColumnModel();
            tcm.getColumn(0).setPreferredWidth(25);
            tcm.getColumn(0).setMaxWidth(25);
            tcm.getColumn(0).setMinWidth(25);
            tcm.getColumn(0).setCellRenderer(new KeyCellRenderer());
            tcm.getColumn(1).setPreferredWidth(130);
            tcm.getColumn(2).setPreferredWidth(130);
            tcm.getColumn(3).setPreferredWidth(130);
            tcm.getColumn(4).setPreferredWidth(90);

        } catch (DataSourceException e) {
            controller.handleException(e);
            columnIndexTableModel.setIndexData(null);
        }
    }

    private synchronized void loadTriggers() {
        try {

            triggersTableModel.setTriggersData(table.getTriggers());
            columnIndexTable.setCellSelectionEnabled(true);

        } catch (DataSourceException e) {
            controller.handleException(e);
            triggersTableModel.setTriggersData(null);
        }
    }

    public void setValues(DatabaseTable table) {
        setValues(table, true);
    }

    private void setValues(DatabaseTable table, boolean stateChanged) {

        this.table = table;
        if (!MiscUtils.isNull(table.getExternalFile())) {
            externalFileField.setText(table.getExternalFile());

            if (!MiscUtils.isNull(table.getAdapter()))
                adapterField.setText(table.getAdapter());

            for (JComponent component : externalFileComponents)
                component.setVisible(true);
        }

        if (propertiesPanel != null) {

            SimpleCommentPanel simpleCommentPanel = new SimpleCommentPanel(table);
            simpleCommentPanel.getCommentPanel().setBorder(BorderFactory.createTitledBorder(bundleString("comment-field-label")));
            simpleCommentPanel.addActionForCommentUpdateButton(action -> {
                createSqlText.setSQLText(createTableStatementFormatted());
                setValues(table);
            });

            propertiesPanel.setCommentPanel(simpleCommentPanel);
            propertiesPanel.update();
        }

        reloadView();
        if (SystemProperties.getBooleanProperty("user", "browser.query.row.count"))
            reloadDataRowCount();

        if (stateChanged)
            stateChanged(null);
    }


    protected void reloadView() {

        try {

            updateRowCount(SystemProperties.getBooleanProperty("user", "browser.query.row.count") ?
                    bundleString("quering") :
                    bundleString("option-disabled")
            );

            referencesLoaded = false;
            tableNameField.setText(table.getName());
            descriptionTable.setDatabaseTable(table);

            // --- load constraints ---

            SwingWorker sw = new SwingWorker("loadingConstraintsFor'" + table.getName() + "'") {
                @Override
                public Object construct() {
                    constraintsTable.setDatabaseTable(table);
                    return null;
                }
            };
            sw.start();

            // --- load indices ---

            sw = new SwingWorker("loadingIndicesFor'" + table.getName() + "'") {
                @Override
                public Object construct() {
                    loadIndexes();
                    return null;
                }
            };
            sw.start();

            // --- load triggers ---

            sw = new SwingWorker("loadingTriggersFor'" + table.getName() + "'") {
                @Override
                public Object construct() {
                    loadTriggers();
                    return null;
                }
            };
            sw.start();

            // --- load dependencies ---

            sw = new SwingWorker("loadingDependenciesFor'" + table.getName() + "'") {
                @Override
                public Object construct() {
                    dependenciesPanel.setDatabaseObject(table);
                    return null;
                }
            };
            sw.start();

            // ---

            alterSqlText.setSQLText(EMPTY);
            createSqlText.setSQLText(createTableStatementFormatted());

        } catch (DataSourceException e) {

            Log.error("Error load table:", e);

            descriptionTable.resetDatabaseTable();
            constraintsTable.resetConstraintsTable();
        }
    }

    private String createTableStatementFormatted() {
        return table.getCreateSQLText();
    }

    private void reloadDataRowCount() {

        if (timer != null)
            timer.cancel();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDataRowCount();
            }
        }, 600);
    }

    private void updateDataRowCount() {

        if (worker != null) {
            if (loadingRowCount)
                Log.debug("Interrupting worker for data row count");

            worker.interrupt();
        }

        worker = new SwingWorker("loadingRowCountFor'" + table.getName() + "'") {
            @Override
            public Object construct() {
                try {

                    loadingRowCount = true;
                    try {
                        Thread.sleep(200);

                    } catch (InterruptedException e) {
                        Log.error("Error load data row count:", e);
                    }

                    Log.debug("Retrieving data row count for table - " + table.getName());
                    return String.valueOf(table.getDataRowCount());

                } catch (DataSourceException e) {
                    return "Error: " + e.getMessage();

                } finally {
                    loadingRowCount = false;
                }
            }

            @Override
            public void finished() {
                updateRowCount(get().toString());
            }
        };
        worker.start();
    }

    /**
     * Returns the contents of the SQL text pane.
     */
    public String getSQLText() {
        return alterSqlText.getSQLText();
    }

    // -----------------------------------------------
    // --- TableConstraintFunction implementations ---
    // -----------------------------------------------

    /**
     * Deletes the selected row on the currently selected table.
     */
    @Override
    public void deleteRow() {

        int metaType = -1;
        int tabIndex = tabPane.getSelectedIndex();
        if (tabIndex == TABLE_COLUMNS_INDEX) {
            descriptionTable.deleteSelectedColumn();

        } else if (tabIndex == TABLE_CONSTRAINTS_INDEX) {
            constraintsTable.deleteSelectedConstraint();

        } else {

            String query = null;
            if (tabIndex == TABLE_INDEXES_INDEX) {
                metaType = NamedObject.INDEX;

                if (columnIndexTable.getSelectedRow() >= 0) {
                    int row = ((TableSorter) columnIndexTable.getModel()).modelIndex(columnIndexTable.getSelectedRow());
                    DefaultDatabaseIndex index = ((TableColumnIndexTableModel) ((TableSorter) columnIndexTable.getModel()).getTableModel()).getIndexes().get(row);
                    query = "DROP INDEX " + MiscUtils.getFormattedObject(index.getName(), table.getHost().getDatabaseConnection());
                }

            } else if (tabIndex == TABLE_TRIGGERS_INDEX) {
                metaType = NamedObject.TRIGGER;

                if (triggersTable.getSelectedRow() >= 0) {
                    int row = ((TableSorter) triggersTable.getModel()).modelIndex(triggersTable.getSelectedRow());
                    DefaultDatabaseTrigger trigger = ((TableTriggersTableModel) ((TableSorter) triggersTable.getModel()).getTableModel()).getTriggers().get(row);
                    query = "DROP TRIGGER " + MiscUtils.getFormattedObject(trigger.getName(), table.getHost().getDatabaseConnection());
                }
            }

            if (query != null) {

                ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog(
                        "Dropping object",
                        query,
                        table.getHost().getDatabaseConnection(),
                        true
                );
                executeQueryDialog.display();

                if (executeQueryDialog.getCommit()) {
                    reloadNodes(metaType);
                    maybeRefresh();
                }
            }
        }

        setSQLText();
    }

    /**
     * Inserts a row after the selected row on the currently selected table.
     */
    @Override
    public void addRow() {

        BaseDialog dialog = null;
        JPanel panelForDialog = null;

        int tabIndex = tabPane.getSelectedIndex();
        if (tabIndex == TABLE_COLUMNS_INDEX) {
            dialog = new BaseDialog(InsertColumnPanel.CREATE_TITLE, true);
            panelForDialog = new InsertColumnPanel(table, dialog);

        } else if (tabIndex == TABLE_CONSTRAINTS_INDEX) {
            dialog = new BaseDialog(EditConstraintPanel.CREATE_TITLE, true);
            panelForDialog = new EditConstraintPanel(table, dialog);

        } else if (tabIndex == TABLE_INDEXES_INDEX) {
            dialog = new BaseDialog(CreateIndexPanel.CREATE_TITLE, true);
            panelForDialog = new CreateIndexPanel(table.getHost().getDatabaseConnection(), dialog, table.getName());

        } else if (tabIndex == TABLE_TRIGGERS_INDEX) {
            dialog = new BaseDialog(CreateTriggerPanel.CREATE_TITLE, true);
            panelForDialog = new CreateTriggerPanel(table.getHost().getDatabaseConnection(), dialog, DefaultDatabaseTrigger.TABLE_TRIGGER, table.getName());
            ((DefaultDatabaseHost) table.getHost()).reloadMetaTag(NamedObject.TRIGGER);
        }

        if (panelForDialog != null) {
            dialog.addDisplayComponent(panelForDialog);
            dialog.display();
            maybeRefresh();
        }
    }

    public void reselectAllIndexes() {

        StringBuilder sb = new StringBuilder();
        DatabaseConnection dc = table.getHost().getDatabaseConnection();

        for (DefaultDatabaseIndex node : table.getIndexes()) {
            sb.append("SET STATISTICS INDEX ").append(MiscUtils.getFormattedObject(node.getName(), dc)).append(";\n");
            node.reset();
        }

        ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog(
                bundledString("Recompute"),
                sb.toString(),
                dc,
                true,
                ";",
                true,
                false
        );
        executeQueryDialog.display();
    }

    @Override
    public void setSQLText() {
        sbTemp.setLength(0);
        alterSqlText.setSQLText("");
    }

    @Override
    public String getTableName() {
        return tableNameField.getText();
    }

    @Override
    public List<String> getTables() {
        return controller.getTables();
    }

    @Override
    public List<String> getColumns(String tableName) {
        return controller.getColumnNamesVector(tableName);
    }

    @Override
    public void moveRowUp() {
        if (tabPane.getSelectedIndex() == TABLE_COLUMNS_INDEX)
            descriptionTable.moveUpSelectedColumn();
    }

    @Override
    public void moveRowDown() {
        if (tabPane.getSelectedIndex() == TABLE_COLUMNS_INDEX)
            descriptionTable.moveDownSelectedColumn();
    }

    @Override
    public ColumnData[] getTableColumnData() {
        return null;
    }

    @Override
    public Vector<ColumnData> getTableColumnDataVector() {
        return null;
    }

    @Override
    public DatabaseConnection getSelectedConnection() {
        return table.getHost().getDatabaseConnection();
    }

    /**
     * Returns the focused TextEditor panel where the selected
     * tab is the SQL text pane.
     */
    protected TextEditor getFocusedTextEditor() {

        if (tabPane.getSelectedIndex() == SQL_PANE_INDEX)
            if (lastFocusEditor != null)
                return lastFocusEditor;

        return null;
    }

    private synchronized void updateRowCount(final String text) {
        if (lock.tryAcquire()) {
            GUIUtils.invokeLater(() -> {
                rowCountField.setText(text);
                lock.release();
            });
        }
    }

    public boolean commitResultSet() {
        try {

            if (tableDataPanel.resultSet != null) {
                if (tableDataPanel.resultSet instanceof TransactionAgnosticResultSet) {

                    Connection con = ((TransactionAgnosticResultSet) tableDataPanel.resultSet).getConnection();
                    if (con != null && !con.isClosed()) {
                        con.commit();
                        con.close();
                    }
                }
            }
            return true;

        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
            return false;
        }
    }

    private void commitColumnsChanges() {
        try {

            DatabaseObjectChangeProvider changeProvider = new DatabaseObjectChangeProvider(table);
            if (changeProvider.applyDefinitionChanges())
                setValues(table);

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e, this.getClass());
        }
    }

    private void reloadNodes(int type) {

        if (type < NamedObject.DOMAIN)
            return;

        ConnectionsTreePanel treePanel = ConnectionsTreePanel.getPanelFromBrowser();
        if (treePanel == null)
            return;

        TreePath pathToReload = treePanel.getMetaTagNodePath(getSelectedConnection(), type);
        if (pathToReload != null) {
            treePanel.reloadPath(pathToReload);
            treePanel.reloadRelatedNodes(
                    (DatabaseObjectNode) pathToReload.getLastPathComponent(),
                    getTableName(),
                    table.isGlobalTemporary()
            );
        }
    }

    @Override
    public void refresh() {
        table.reset();
        setValues(table);
    }

    private void maybeRefresh() {
        GUIUtils.invokeLater(() -> {
            if (!tableResetEventTriggered)
                refresh();
            tableResetEventTriggered = false;
        });
    }

    // --- DatabaseTableEventListener impl ---

    @Override
    public void processTableReset(DatabaseTableEvent e) {
        if (!Objects.equals(e.getSource(), this)) {
            tableResetEventTriggered = true;
            setValues(table, false);
        }
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event.isDatabaseTableEvent();
    }

    // ---

    private class PropertiesPanel extends JPanel {

        private static final String NONE = "NONE";
        private static final String PRIMARY = "PRIMARY";

        private List<Object> oldValues;
        private DynamicComboBoxModel tablespaceComboModel;

        private JComboBox<?> sqlSecurityComboBox;
        private JComboBox<?> tablespaceComboBox;
        private JCheckBox externalCheckBox;
        private JCheckBox adapterCheckBox;
        private JTextField externalFileTextField;
        private SimpleCommentPanel commentPanel;
        private JButton savePreferencesButton;

        protected PropertiesPanel() {
            init();
        }

        private void init() {

            sqlSecurityComboBox = new JComboBox<>(new String[]{NONE, "DEFINER", "INVOKER"});
            sqlSecurityComboBox.setMinimumSize(new Dimension(300, 0));

            tablespaceComboModel = new DynamicComboBoxModel(new Vector<>());
            tablespaceComboBox = WidgetFactory.createComboBox("tablespaceComboBox", tablespaceComboModel);
            tablespaceComboBox.setMinimumSize(new Dimension(300, 0));

            externalCheckBox = new JCheckBox(bundledString("externalCheckBox"));
            externalCheckBox.setEnabled(false);

            adapterCheckBox = new JCheckBox(bundledString("adapterCheckBox"));
            adapterCheckBox.setEnabled(false);

            externalFileTextField = new JTextField();
            externalFileTextField.setMinimumSize(new Dimension(500, 0));
            externalFileTextField.setEditable(false);

            savePreferencesButton = new JButton(Bundles.get("common.save.button"));
            savePreferencesButton.addActionListener(e -> saveChanges());

            commentPanel = new SimpleCommentPanel(null);

            arrange();
        }

        private void arrange() {

            GridBagHelper gbh;

            // properties panel
            gbh = new GridBagHelper()
                    .anchorNorthWest().setInsets(5, 5, 5, 5).fillHorizontally();

            JPanel propertiesPanel = new JPanel(new GridBagLayout());
            gbh.addLabelFieldPair(propertiesPanel, bundledString("sqlSecurityComboBox"), sqlSecurityComboBox, null, true, false);
            gbh.addLabelFieldPair(propertiesPanel, bundledString("tablespaceComboBox"), tablespaceComboBox, null, false, true);
            gbh.addLabelFieldPair(propertiesPanel, bundledString("externalFileTextField"), externalFileTextField, null, true, false);
            propertiesPanel.add(adapterCheckBox, gbh.nextCol().setMinWeightX().spanX().get());

            // main panel
            gbh = new GridBagHelper()
                    .anchorNorthWest().setInsets(5, 5, 5, 5).fillHorizontally();

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.add(propertiesPanel, gbh.spanX().get());
            mainPanel.add(commentPanel.getCommentPanel(), gbh.nextRowFirstCol().setMaxWeightY().fillBoth().get());
            mainPanel.add(savePreferencesButton, gbh.nextRowFirstCol().anchorSouthEast().fillNone().setMinWeightY().get());

            setLayout(new GridBagLayout());
            add(mainPanel, gbh.anchorNorthWest().fillBoth().spanX().spanY().get());
        }

        private void populateTablespace() {

            List<NamedObject> tablespaceList = ConnectionsTreePanel.getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(table.getHost().getDatabaseConnection())
                    .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLESPACE]);

            List<String> tablespaceNameList = new ArrayList<>();
            tablespaceNameList.add(0, PRIMARY);
            if (tablespaceList != null && !tablespaceList.isEmpty())
                tablespaceNameList.addAll(tablespaceList.stream().map(Named::getName).collect(Collectors.toList()));

            tablespaceComboModel.setElements(tablespaceNameList);
        }

        protected void update() {

            populateTablespace();

            DefaultDatabaseTable databaseTable = (DefaultDatabaseTable) table;
            String sqlSecurity = databaseTable.getSqlSecurity();
            String tablespace = databaseTable.getTablespace();
            String externalFile = databaseTable.getExternalFile();
            String adapter = databaseTable.getAdapter();

            if (databaseTable.getDatabaseMajorVersion() < 5 || !databaseTable.getHost().getDatabaseProductName().toLowerCase().contains("reddatabase"))
                tablespaceComboBox.setEnabled(false);

            sqlSecurityComboBox.setSelectedItem(Objects.equals(sqlSecurity, "") ? NONE : sqlSecurity);
            tablespaceComboBox.setSelectedItem(Objects.equals(tablespace, "") ? PRIMARY : tablespace);
            externalCheckBox.setSelected(externalFile != null && !externalFile.isEmpty());
            adapterCheckBox.setSelected(adapter != null && !adapter.isEmpty());
            externalFileTextField.setText(externalFile);

            oldValues = Arrays.asList(
                    sqlSecurityComboBox.getSelectedItem(),
                    tablespaceComboBox.getSelectedItem()
            );

        }

        private void saveChanges() {

            commentPanel.updateComment();

            String query = "ALTER TABLE " + MiscUtils.getFormattedObject(table.getName(), table.getHost().getDatabaseConnection());
            String noChangesCheckString = query;

            String sqlSecurity = (String) sqlSecurityComboBox.getSelectedItem();
            if (!oldValues.get(0).equals(sqlSecurity)) {
                query += !Objects.equals(sqlSecurity, NONE) ?
                        "\nALTER SQL SECURITY " + sqlSecurity :
                        "\nDROP SQL SECURITY";
            }

            String tablespace = (String) tablespaceComboBox.getSelectedItem();
            if (!oldValues.get(1).equals(tablespace)) {
                if (!query.equals(noChangesCheckString)) query += ",";
                query += "\nSET TABLESPACE TO " + (!Objects.equals(tablespace, NONE) ? tablespace : "PRIMARY");
            }

            if (!query.equals(noChangesCheckString)) {

                ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog(
                        bundledString("AlterTable"), query, table.getHost().getDatabaseConnection(), true);

                executeQueryDialog.display();
                if (executeQueryDialog.getCommit())
                    refresh();
            }

        }

        protected void setCommentPanel(SimpleCommentPanel commentPanel) {
            this.commentPanel = commentPanel;
            super.removeAll();
            arrange();
        }

        private String bundledString(String key) {
            return BrowserTableEditingPanel.bundledString("PreferencesPanel." + key);
        }

    } // PropertiesPanel class

    public static String bundledString(String key) {
        return Bundles.get(BrowserTableEditingPanel.class, key);
    }

}
