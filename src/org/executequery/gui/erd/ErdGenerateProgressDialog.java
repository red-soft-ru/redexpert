/*
 * ErdGenerateProgressDialog.java
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
import org.executequery.databaseobjects.impl.AbstractTableObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.browser.ColumnConstraint;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.PRIMARY_KEY;


/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdGenerateProgressDialog extends AbstractBaseDialog {

    private final DatabaseConnection connection;

    private JButton cancelButton;
    private ErdViewerPanel parent;
    private JProgressBar progressBar;

    private SwingWorker worker;
    private Vector selectedTables;

    public ErdGenerateProgressDialog(DatabaseConnection connection, Vector selectedTables) {
        super(GUIUtilities.getParentFrame(), "Progress", false);

        this.connection = connection;
        this.selectedTables = selectedTables;

        init();
        display();
    }

    public ErdGenerateProgressDialog(Vector selectedTables, ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), "Adding Tables", false);

        this.connection = parent.getDatabaseConnection();
        this.selectedTables = selectedTables;
        this.parent = parent;

        init();
        display();
    }

    public ErdGenerateProgressDialog(Vector selectedTables, ErdViewerPanel parent, DatabaseConnection connection) {
        super(GUIUtilities.getParentFrame(), "Adding Tables", false);

        this.selectedTables = selectedTables;
        this.connection = connection;
        this.parent = parent;

        init();
        display();
    }

    private void init() {
        JPanel base = new JPanel(new GridBagLayout());

        cancelButton = new JButton(Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(e -> worker.interrupt());

        progressBar = new JProgressBar(0, selectedTables.size() * 2);
        progressBar.setPreferredSize(new Dimension(250, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        base.add(new JLabel("Generating ERD..."), gbc);
        gbc.gridy = 1;
        gbc.insets.top = 0;
        base.add(progressBar, gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        base.add(cancelButton, gbc);

        base.setBorder(BorderFactory.createEtchedBorder());

        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(base, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));

        setResizable(false);

        worker = new SwingWorker(ErdGenerateProgressDialog.class.getSimpleName()) {

            @Override
            public Object construct() {
                return doWork();
            }

            @Override
            public void finished() {
                processComplete((Vector) get());
                GUIUtilities.scheduleGC();
            }
        };

        worker.start();
    }

    private void display() {
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    private Object doWork() {

        int count = 0;
        Vector<ErdTableInfo> erdTableInfos = new Vector(selectedTables.size());

        try {

            for (Object selectedTable : selectedTables) {
                progressBar.setValue(count++);

                if (Thread.interrupted())
                    throw new InterruptedException();

                try {
                    DefaultDatabaseHost host = ConnectionsTreePanel.getPanelFromBrowser()
                            .getDefaultDatabaseHostFromConnection(connection);

                    ErdTableInfo tableInfo = new ErdTableInfo();
                    tableInfo.setName((String) selectedTable);
                    tableInfo.setColumns(host.getColumnDataArrayFromTableName(tableInfo.getName()));

                    AbstractTableObject tableObject = host.getTableFromName(tableInfo.getName());
                    if (tableObject != null)
                        tableInfo.setComment(tableObject.getRemarks());

                    erdTableInfos.add(tableInfo);

                } catch (DataSourceException e) {
                    erdTableInfos.add(new ErdTableInfo());
                }

                progressBar.setValue(count++);
            }

        } catch (InterruptedException intExc) {
            GUIUtilities.displayWarningMessage("Process cancelled");

        } finally {
            progressBar.setValue(selectedTables.size() * 2);
            cancelButton.setEnabled(false);
        }

        return (erdTableInfos);
    }

    Vector<ErdTableInfo> sort(Vector<ErdTableInfo> tableInfoList) {
        Map<ErdTableInfo, List<ErdTableInfo>> links = buildTableRelationships(tableInfoList);
        Vector<ErdTableInfo> result = new Vector<>();
        if (tableInfoList != null) {
            while (tableInfoList.size() > 0) {
                result = addTableToSortVector(tableInfoList.elementAt(0), result, tableInfoList, links);
            }
        }
        return result;
    }

    Vector<ErdTableInfo> addTableToSortVector(ErdTableInfo etf, Vector<ErdTableInfo> result, Vector<ErdTableInfo> source, Map<ErdTableInfo, List<ErdTableInfo>> links) {
        result.add(etf);
        source.remove(etf);
        for (ErdTableInfo table : links.get(etf)) {
            if (result.contains(table))
                continue;
            addTableToSortVector(table, result, source, links);
        }
        return result;
    }

    private void processComplete(Vector<ErdTableInfo> tableInfoList) {

        tableInfoList = sort(tableInfoList);
        if (parent == null) {

            if (tableInfoList.size() != selectedTables.size()) {
                dispose();
                return;
            }

            GUIUtilities.showWaitCursor();

            ErdViewerPanel viewerPanel = new ErdViewerPanel(tableInfoList, false);
            viewerPanel.setDatabaseConnection(databaseConnection);

            GUIUtilities.closeDialog(GenerateErdPanel.TITLE);
            dispose();
            GUIUtilities.showNormalCursor();
            selectedTables = null;

        } else {

            if (tableInfoList.size() != selectedTables.size()) {
                dispose();
                return;
            }

            GUIUtilities.showWaitCursor();

            for (ErdTableInfo tableInfo : tableInfoList) {

                ColumnData[] columnData = tableInfo.getColumns();
                if (columnData == null || columnData.length == 0) {

                    for (ErdTable t : parent.getAllTablesArray()) {
                        if (t.getTableName().contentEquals(tableInfo.getName())) {
                            parent.removeTable(t);
                            break;
                        }
                    }

                } else {

                    ErdTable table = new ErdTable(
                            tableInfo.getName(),
                            tableInfo.getColumns(),
                            parent
                    );
                    table.setEditable(parent.isEditable());
                    table.setDescriptionTable(tableInfo.getComment());

                    parent.addNewTable(table, false);
                }
            }

            parent.updateTableRelationships();
            GUIUtilities.showNormalCursor();
            selectedTables = null;
            dispose();
        }
    }

    public Map<ErdTableInfo, List<ErdTableInfo>> buildTableRelationships(Vector<ErdTableInfo> tables) {

        String referencedTable = null;
        ColumnData[] cda = null;
        ColumnConstraint[] cca = null;
        Map<ErdTableInfo, List<ErdTableInfo>> resMap = new HashMap<>();
        for (ErdTableInfo etf : tables) {
            resMap.put(etf, new ArrayList<>());
        }

        ErdTableInfo[] tables_array = tables.toArray(new ErdTableInfo[0]);

        ErdTableInfo table = null;

        for (int k = 0, m = tables.size(); k < m; k++) {

            cda = tables_array[k].getColumns();
            if (cda == null)
                continue;

            for (ColumnData columnData : cda) {

                if (!columnData.isForeignKey())
                    continue;

                cca = columnData.getColumnConstraintsArray();
                for (ColumnConstraint columnConstraint : cca) {

                    if (columnConstraint.getType() == PRIMARY_KEY)
                        continue;

                    referencedTable = columnConstraint.getRefTable();

                    for (int j = 0; j < m; j++) {

                        if (referencedTable.equalsIgnoreCase(
                                tables.elementAt(j).getName())) {
                            table = tables.elementAt(j);
                            if (!resMap.get(tables_array[k]).contains(table))
                                resMap.get(tables_array[k]).add(table);
                            if (!resMap.get(table).contains(tables_array[k]))
                                resMap.get(table).add(tables_array[k]);
                            break;
                        }
                    }
                }
            }
        }

        return resMap;
    }

}
