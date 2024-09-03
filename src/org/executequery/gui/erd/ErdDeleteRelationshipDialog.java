/*
 * ErdDeleteRelationshipDialog.java
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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdDeleteRelationshipDialog extends ErdPrintableDialog {
    private static final String TITLE = bundleString("title");

    private JTable table;
    private JButton deleteButton;
    private JButton cancelButton;

    private Vector constraints;
    private final ErdViewerPanel parent;

    public ErdDeleteRelationshipDialog(ErdViewerPanel parent, ErdTable[] erdTables) {
        super(TITLE);
        this.parent = parent;
        this.constraints = new Vector();

        if (!preInit(erdTables)) {
            dispose();
            return;
        }

        init();
        arrange();
        display();
    }

    private boolean preInit(ErdTable[] erdTables) {

        String tableName1 = erdTables[0].getTableName();
        String tableName2 = erdTables[1].getTableName();

        ColumnData[] columnData1 = erdTables[0].getTableColumns();
        ColumnData[] columnData2 = erdTables[1].getTableColumns();

        for (ColumnData columnData : columnData1) {
            if (!columnData.isForeignKey())
                continue;

            ColumnConstraint[] tableConstraints = columnData.getColumnConstraintsArray();
            if (tableConstraints == null || tableConstraints.length == 0)
                break;

            for (int j = 0; j < tableConstraints.length; j++) {
                ColumnConstraint constraint = tableConstraints[j];
                if (!constraint.isPrimaryKey() && constraint.getRefTable().equalsIgnoreCase(tableName2))
                    constraints.add(new ColumnConstraintDrop(columnData, erdTables[0], j));
            }
        }

        for (ColumnData columnData : columnData2) {
            if (!columnData.isForeignKey())
                continue;

            ColumnConstraint[] tableConstraints = columnData.getColumnConstraintsArray();
            if (tableConstraints == null || tableConstraints.length == 0)
                continue;

            for (int j = 0; j < tableConstraints.length; j++) {
                ColumnConstraint constraint = tableConstraints[j];
                if (!constraint.isPrimaryKey() && constraint.getRefTable().equalsIgnoreCase(tableName1))
                    constraints.add(new ColumnConstraintDrop(columnData, erdTables[1], j));
            }
        }

        if (constraints.isEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("noRelation"));
            return false;
        }

        return true;
    }

    private void init() {

        deleteButton = WidgetFactory.createButton("deleteButton", Bundles.get("common.delete.button"), e -> delete());
        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"), e -> dispose());

        sqlText.setSQLTextEditable(false);
        sqlText.setPreferredSize(new Dimension(450, 200));

        table = new DefaultTable(new ConstraintTableModel());
        table.getTableHeader().setReorderingAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setFillsViewportHeight(true);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(25);
        tcm.getColumn(0).setMaxWidth(25);
        tcm.getColumn(0).setMinWidth(25);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(deleteButton, gbh.nextCol().setMinWeightX().get());
        buttonPanel.add(cancelButton, gbh.nextCol().leftGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().spanX();
        mainPanel.add(new JLabel(bundleString("selectConstraints")), gbh.get());
        mainPanel.add(new JScrollPane(table), gbh.nextRow().topGap(5).setMaxWeightY().fillBoth().get());
        mainPanel.add(sqlText, gbh.nextRow().get());
        mainPanel.add(buttonPanel, gbh.nextRow().setMinWeightY().get());

        // --- base ---

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(800, 500));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());
    }

    private void setSQLText() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = constraints.size(); i < n; i++) {
            ColumnConstraintDrop constraintToDrop = (ColumnConstraintDrop) constraints.elementAt(i);
            if (constraintToDrop.isDropped())
                sb.append(constraintToDrop.getSql());
        }

        sqlText.setSQLText(sb.toString());
    }

    private void delete() {

        for (int i = 0, n = constraints.size(); i < n; i++) {
            ColumnConstraintDrop constraintToDrop = (ColumnConstraintDrop) constraints.elementAt(i);
            constraintToDrop.dropConstraint();
        }

        constraints = null;
        table = null;
        parent.updateTableRelationships();
        dispose();
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(ErdDeleteRelationshipDialog.class, key, args);
    }

    private class ConstraintTableModel extends AbstractTableModel {

        private final String[] headers = {
                " ",
                bundleString("Name"),
                bundleString("ReferencingTable"),
                bundleString("ReferencingColumn"),
                bundleString("ReferencedTable"),
                bundleString("ReferencedColumn")
        };

        @Override
        public int getColumnCount() {
            return headers.length;
        }

        @Override
        public int getRowCount() {
            return constraints.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            ColumnConstraintDrop constraintToDrop = (ColumnConstraintDrop) constraints.elementAt(row);
            ColumnConstraint constraint = constraintToDrop.getColumnConstraint();

            switch (col) {
                case 0:
                    return constraintToDrop.isDropped();
                case 1:
                    return constraint.getName();
                case 2:
                    return constraint.getTable();
                case 3:
                    return constraint.getColumn();
                case 4:
                    return constraint.getRefTable();
                case 5:
                    return constraint.getRefColumn();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            ColumnConstraintDrop constraintToDrop = (ColumnConstraintDrop) constraints.elementAt(row);
            ColumnConstraint constraint = constraintToDrop.getColumnConstraint();

            switch (col) {
                case 0:
                    constraintToDrop.setDropped((Boolean) value);
                    setSQLText();
                    break;
                case 1:
                    constraint.setName((String) value);
                    break;
                case 2:
                    constraint.setTable((String) value);
                    break;
                case 3:
                    constraint.setColumn((String) value);
                    break;
                case 4:
                    constraint.setRefTable((String) value);
                    break;
                case 5:
                    constraint.setRefColumn((String) value);
                    break;
            }

            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public String getColumnName(int col) {
            return headers[col];
        }

        @Override
        public Class getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }

    } // ConstraintTableModel class

    private static class ColumnConstraintDrop {

        private boolean dropped;
        private final ErdTable erdTable;
        private final ColumnData columnData;
        private final ColumnConstraint columnConstraint;

        public ColumnConstraintDrop(ColumnData columnData, ErdTable erdTable, int constraintIndex) {
            this.columnConstraint = columnData.getColumnConstraintsArray()[constraintIndex];
            this.columnData = columnData;
            this.erdTable = erdTable;
            this.dropped = false;
        }

        public void dropConstraint() {
            if (dropped) {
                columnData.removeConstraint(columnConstraint);
                columnData.setForeignKey(false);
                erdTable.setDropConstraintsScript(getSql());
            }
        }

        public ColumnConstraint getColumnConstraint() {
            return columnConstraint;
        }

        public String getSql() {

            if (dropped) {
                return String.format(
                        "ALTER TABLE %s DROP CONSTRAINT %s;\n",
                        erdTable.getTableName(),
                        columnConstraint.getName()
                );
            }

            return Constants.EMPTY;
        }

        public boolean isDropped() {
            return dropped;
        }

        public void setDropped(boolean dropped) {
            this.dropped = dropped;
        }

    } // ColumnConstraintDrop class

}
