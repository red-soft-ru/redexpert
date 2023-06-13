/*
 * ExportConnectionsPanel.java
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

package org.executequery.gui.connections;

import org.apache.commons.lang.StringUtils;
import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsFolder;
import org.executequery.localization.Bundles;
import org.executequery.repository.ConnectionExporter;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.tree.CheckTreeSelectionModel;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;
import org.underworldlabs.swing.wizard.WizardProcessPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ExportConnectionsPanel extends WizardProcessPanel implements ActiveComponent {

    public static final String TITLE = Bundles.get("ExportConnectionsPanel.Title");
    public static final String FRAME_ICON = "ExportConnections16.svg";

    private static final String[] STEPS = {
            Bundles.get("ExportConnectionsPanel.Step-1"),
            Bundles.get("ExportConnectionsPanel.Step-2"),
            Bundles.get("ExportConnectionsPanel.Step-3")
    };

    private static final String[] TITLES = {
            Bundles.get("ExportConnectionsPanel.Titles-1"),
            Bundles.get("ExportConnectionsPanel.Titles-2"),
            Bundles.get("ExportConnectionsPanel.Titles-3")

    };

    private ExportConnectionsPanelOne firstPanel;

    private ExportConnectionsPanelTwo secondPanel;

    private ExportConnectionsModel model;

    private ExportConnectionsPanelThree thirdPanel;

    private ActionContainer parent;

    public ExportConnectionsPanel(ActionContainer parent) {

        this.parent = parent;

        firstPanel = new ExportConnectionsPanelOne();
        secondPanel = new ExportConnectionsPanelTwo();
        thirdPanel = new ExportConnectionsPanelThree();

        List<JPanel> panels = new ArrayList<JPanel>();
        panels.add(firstPanel);
        panels.add(secondPanel);
        panels.add(thirdPanel);

        model = new ExportConnectionsModel(panels);
        setModel(model);

        prepare();
    }

    private boolean doNext() {

        int index = model.getSelectedIndex();
        if (index == 0) {

            return firstPanel.canProceed();

        } else if (index == 1) {

            if (!secondPanel.canProceed()) {

                return false;
            }
            thirdPanel.start();
            doExport();
        }
        return true;
    }
    protected String bundledString(String key, Object... args) {
        return Bundles.get(this.getClass(), key, args);
    }
    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {

        parent.finished();
    }

    private void doExport() {

        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {

                    System.out.println(bundledString("ExportingConnectionsToFile"));

                    setButtonsEnabled(false);

                    parent.block();
                    boolean result = create();

                    if (result) {

                        setButtonsEnabled(true);
                        setNextButtonEnabled(false);
                        setBackButtonEnabled(true);
                        setCancelButtonEnabled(true);
                        setCancelButtonText(Bundles.get("common.finish.button"));

                        thirdPanel.append(Bundles.get("ExportConnectionsPanel.done"));
                        thirdPanel.append(bundledString("TheSelectedConnectionsHaveBeenExportedTo", secondPanel.getExportPath()));

                        System.out.println(bundledString("FinishedExportingConnectionsToFile"));
                    }

                } finally {

                    parent.unblock();
                }
            }
        });

    }


    private boolean create() {

        String fileName = secondPanel.getExportPath();
        CheckTreeSelectionModel selectionModel = firstPanel.getSelectionModel();

        List<ConnectionsFolder> folders = new ArrayList<>();
        List<DatabaseConnection> connections = new ArrayList<>();

        TreePath[] selectionPaths = selectionModel.getSelectionPaths();
        for (TreePath treePath : selectionPaths) {

            Object object = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
            if (object instanceof ConnectionsFolder) {

                ConnectionsFolder folder = (ConnectionsFolder) object;
                folders.add(folder);
                connections.addAll(folder.getConnections());

            } else if (object instanceof DatabaseConnection) {

                connections.add((DatabaseConnection) object);

            } else if (treePath.getParentPath() == null) {

                folders.addAll(firstPanel.folders());
                connections.addAll(firstPanel.connections());
                break;
            }

        }

        try {

            thirdPanel.append(StringUtils.EMPTY);
            for (ConnectionsFolder folder : folders) {

                thirdPanel.append(bundledString("ExportingFolder",folder.getName()));
            }

            thirdPanel.append(StringUtils.EMPTY);
            for (DatabaseConnection connection : connections) {

                thirdPanel.append(bundledString("ExportingConnection", connection.getName()));
            }

            new ConnectionExporter().write(fileName, folders, connections);
            return true;

        } catch (RepositoryException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundledString("ErrorWritingConnectionsToFileTheSystemReturned") + e.getMessage(), e);
            return false;
        }

    }


    class ExportConnectionsModel extends DefaultWizardProcessModel {

        ExportConnectionsModel(List<JPanel> panels) {

            super(panels, STEPS, TITLES);
        }

        @Override
        public boolean next() {

            if (doNext()) {

                return super.next();
            }
            return false;
        }

    }

}

