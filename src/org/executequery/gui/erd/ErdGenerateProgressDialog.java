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
import org.executequery.gui.GenerateErdPanel;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ErdGenerateProgressDialog extends AbstractBaseDialog {

    /**
     * The ERD parent panel
     */
    private ErdViewerPanel parent;

    /**
     * The generate progress bar
     */
    private JProgressBar progressBar;

    /**
     * The selected table names
     */
    private Vector selectedTables;


    /**
     * Worker thread for process
     */
    private SwingWorker worker;

    /**
     * The process cancel button
     */
    private JButton cancelButton;

    /**
     * the connection props object
     */
    private final DatabaseConnection databaseConnection;
    private ErdViewerPanel erdViewerPanel;

    public ErdGenerateProgressDialog(DatabaseConnection databaseConnection, Vector selectedTables, ErdViewerPanel erdViewerPanel) {
        this(databaseConnection, selectedTables);
        this.erdViewerPanel = erdViewerPanel;
    }

    public ErdGenerateProgressDialog(DatabaseConnection databaseConnection, Vector selectedTables) {
        super(GUIUtilities.getParentFrame(), "Progress", false);

        this.databaseConnection = databaseConnection;
        this.selectedTables = selectedTables;

        jbInit();
        display();
    }

    public ErdGenerateProgressDialog(Vector selectedTables, ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), "Adding Tables", false);

        this.databaseConnection = parent.getDatabaseConnection();
        this.selectedTables = selectedTables;
        this.parent = parent;

        jbInit();
        display();
    }

    public ErdGenerateProgressDialog(Vector selectedTables, ErdViewerPanel parent, DatabaseConnection connection) {
        super(GUIUtilities.getParentFrame(), "Adding Tables", false);

        this.databaseConnection = connection;
        this.selectedTables = selectedTables;
        this.parent = parent;

        jbInit();
        display();
    }

    private void display() {
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    private void jbInit() {
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
            public Object construct() {
                return doWork();
            }

            public void finished() {
                processComplete((Vector) get());
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();

    }

    private Object doWork() {

        int v_size = selectedTables.size();

        Vector<ErdTableInfo> erdTableInfos = new Vector(v_size);

        try {

            int count = 0;

            for (int i = 0; i < v_size; i++) {
                progressBar.setValue(count++);

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    ErdTableInfo etf = new ErdTableInfo();
                    etf.setName((String) selectedTables.get(i));
                    etf.setColumns(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getColumnDataArrayFromTableName(etf.getName()));
                    AbstractTableObject tableObject = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getTableFromName(etf.getName());
                    if (tableObject != null)
                        etf.setComment(tableObject.getRemarks());
                    erdTableInfos.add(etf);
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

    private void processComplete(Vector<ErdTableInfo> tableInfoList) {

        if (parent == null) { // mapping a new erd

            if (tableInfoList.size() != selectedTables.size()) {
                dispose();
                return;
            }

            GUIUtilities.showWaitCursor();

            new ErdViewerPanel(selectedTables, tableInfoList, false);

            erdViewerPanel.setDatabaseConnection(databaseConnection);
            erdViewerPanel.cleanup();
            erdViewerPanel.setTables(selectedTables, columnData);
            erdViewerPanel.setTableDependencies(erdViewerPanel.buildTableRelationships());

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

            for (int i = 0, n = selectedTables.size(); i < n; i++) {

                ColumnData[] cds = tableInfoList.elementAt(i).getColumns();
                if (cds == null || cds.length == 0) {

                    for (ErdTable t : parent.getAllTablesArray()) {
                        if (t.getTableName().contentEquals((String) selectedTables.elementAt(i))) {
                            parent.removeTable(t);
                            break;
                        }
                    }

                } else {

                    ErdTable table = new ErdTable(
                            (String) selectedTables.elementAt(i),
                            tableInfoList.elementAt(i).getColumns(),
                            parent
                    );
                    table.setEditable(parent.isEditable());
                    table.setDescriptionTable(tableInfoList.elementAt(i).getComment());

                    parent.addNewTable(table, false);
                }
            }

            parent.updateTableRelationships();
            GUIUtilities.showNormalCursor();
            selectedTables = null;
            dispose();
        }
    }

}
