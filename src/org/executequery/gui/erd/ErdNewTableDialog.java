/*
 * ErdNewTableDialog.java
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

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.CreateTablePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class ErdNewTableDialog extends ErdPrintableDialog
        implements ActionContainer {

    private final ErdViewerPanel erdViewerPanel;
    private CreateTableERDPanel createPanel;
    private ErdTable erdTable;

    public ErdNewTableDialog(ErdViewerPanel parent) {
        super(bundleString("title"), false);
        this.erdViewerPanel = parent;

        init();
        display();
        createPanel.setFocusComponent();
        sqlText = createPanel.getSQLTextAreal();
    }

    public ErdNewTableDialog(ErdViewerPanel parent, ErdTable erdTable) {
        this(parent);
        this.setTitle(bundleString("editableTitle") + erdTable.getTableName());
        this.erdTable = erdTable;

        ColumnData[] tableColumns = erdTable.getTableColumns();

        createPanel.setTableName(erdTable.getTableName());
        createPanel.getSimpleCommentPanel().setComment(erdTable.getDescriptionTable());
        createPanel.setColumnDataArray(tableColumns);
        createPanel.setColumnConstraintVector(getColumnConstraints(tableColumns));
        createPanel.resetSQLText();
        createPanel.setSQLTextCaretPosition(0);
    }

    private void init() {

        JButton cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> dispose()
        );

        JButton createButton = WidgetFactory.createButton(
                "createButton",
                Bundles.get("common.create.button"),
                e -> createTable()
        );

        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper().setInsets(5, 5, 0, 5).anchorEast();
        buttonsPanel.add(new JPanel(), gbh.setMaxWeightX().fillHorizontally().get());
        buttonsPanel.add(createButton, gbh.nextCol().setMinWeightX().fillNone().get());
        buttonsPanel.add(cancelButton, gbh.nextCol().rightGap(5).get());

        createPanel = new CreateTableERDPanel(erdViewerPanel.getDatabaseConnection(), this);
        createPanel.addButtonsPanel(buttonsPanel);
        createPanel.setPreferredSize(new Dimension(700, 550));

        add(createPanel, BorderLayout.CENTER);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public void createTable() {

        String tableName = createPanel.getTableName();
        if (MiscUtils.isNull(tableName)) {
            GUIUtilities.displayErrorMessage(bundleString("TableNameEmptyError"));
            return;
        }

        createPanel.fireEditingStopped();

        ColumnData[] columnDataArray = createPanel.getTableColumnDataAndConstraints();
        for (ColumnData columnData : columnDataArray) {
            columnData.setTableName(tableName);
            columnData.setNamesToUpper();
        }

        if (erdTable == null) {
            ErdTable table = new ErdTable(tableName, columnDataArray, erdViewerPanel);
            table.setCreateTableScript(sqlText.getSQLText());
            table.setNewTable(true);
            table.setEditable(true);
            table.setDescriptionTable(createPanel.getSimpleCommentPanel().getComment());

            erdTable.setShowCommentOnTable(createPanel.isShowCommentOnTable());
            erdTable.setShowCommentOnFields(createPanel.isShowCommentOnFields());
            erdTable.tableColumnsChanged(true);
            if (!erdViewerPanel.addNewTable(table, true)) {
                GUIUtilities.displayErrorMessage(bundleString("TableExistsError"));
                return;
            }

        } else {
            erdTable.setTableColumns(columnDataArray);
            erdTable.setTableName(tableName);
            erdTable.setCreateTableScript(sqlText.getSQLText());
            erdTable.setNewTable(true);
            erdTable.setEditable(true);
            erdTable.setDescriptionTable(createPanel.getSimpleCommentPanel().getComment());
            erdTable.setShowCommentOnTable(createPanel.isShowCommentOnTable());
            erdTable.setShowCommentOnFields(createPanel.isShowCommentOnFields());
            erdTable.tableColumnsChanged(true);
        }

        SwingUtilities.invokeLater(erdViewerPanel::updateTableRelationships);
        dispose();
    }

    private static Vector<ColumnConstraint> getColumnConstraints(ColumnData[] columnDataArray) {

        if (columnDataArray == null)
            return new Vector<>();

        Vector<ColumnConstraint> columnConstraintVector = new Vector<>();
        for (ColumnData columnData : columnDataArray) {
            Vector<ColumnConstraint> tempConstraintsVector = columnData.getColumnConstraintsVector();
            if (tempConstraintsVector != null)
                columnConstraintVector.addAll(tempConstraintsVector);
        }

        return columnConstraintVector;
    }

    // --- ActionContainer impl ---

    @Override
    public void block() {
        GUIUtils.invokeLater(() -> {
            if (!getRootPane().getGlassPane().isVisible())
                getRootPane().getGlassPane().setVisible(true);
        });

        GUIUtilities.showWaitCursor();
    }

    @Override
    public void unblock() {
        GUIUtils.invokeLater(() -> {
            if (getRootPane().getGlassPane().isVisible())
                getRootPane().getGlassPane().setVisible(false);
        });

        GUIUtilities.showNormalCursor();
    }

    @Override
    public boolean isDialog() {
        return true;
    }

    @Override
    public void finished() {
    }

    // ---

    private static String bundleString(String key) {
        return Bundles.get(ErdNewTableDialog.class, key);
    }

    public class CreateTableERDPanel extends CreateTablePanel {

        public CreateTableERDPanel(DatabaseConnection dc, ActionContainer dialog) {
            super(dc, dialog);
        }

        public void setColumnConstraintVector(Vector<ColumnConstraint> ccv) {
            super.setColumnConstraintVector(ccv, true);
        }

        public void setTableName(String tableName) {
            nameField.setText(tableName);
        }

        public SimpleSqlTextPanel getSQLTextAreal() {
            return sqlText;
        }

        public List<ErdTable> getErdTables() {
            return erdViewerPanel.getAllTablesVector();
        }

        public boolean isShowCommentOnTable() {
            return showCommentOnTableBox.isSelected();
        }

        public boolean isShowCommentOnFields() {
            return showCommentOnFieldsBox.isSelected();
        }

        @Override
        protected void addButtonsPanel(JPanel buttonsPanel) {
            super.addButtonsPanel(buttonsPanel);
        }

        @Override
        public List<String> getColumns(String table) {

            List<ErdTable> erdTables = getErdTables();
            if (erdTables == null)
                return new ArrayList<>();

            List<String> columns = new ArrayList<>();
            for (ErdTable erd : erdTables) {
                if (erd.getTableName() != null && erd.getTableName().contentEquals(table)) {
                    for (ColumnData cd : erd.getTableColumns()) {
                        columns.add(cd.getColumnName());
                    }
                }
            }

            return columns;
        }

    } // CreateTableERDPanel class

}
