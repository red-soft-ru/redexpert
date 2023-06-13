/*
 * ImportConnectionsPanel.java
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

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.ConnectionImport;
import org.executequery.repository.ConnectionImporter;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.wizard.DefaultWizardProcessModel;
import org.underworldlabs.swing.wizard.WizardProcessPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ImportConnectionsPanel extends WizardProcessPanel implements ActiveComponent, ImportProcessMonitor {

    public static final String TITLE = Bundles.get("ImportConnectionPanel.ImportConnections");
    public static final String FRAME_ICON = "ImportConnections16.svg";

    private static final String[] STEPS = {
            Bundles.get("ImportConnectionPanel.StepsOne"),
            Bundles.get("ImportConnectionPanel.StepsTwo")
    };

    private static final String[] TITLES = {
            Bundles.get("ImportConnectionPanel.TitlesOne"),
            Bundles.get("ImportConnectionPanel.TitlesTwo")
    };

    private ImportConnectionsPanelOne firstPanel;

    private ImportConnectionsPanelTwo secondPanel;

    private ImportConnectionsModel model;

    private ActionContainer parent;

    public ImportConnectionsPanel(ActionContainer parent) {

        this.parent = parent;

        firstPanel = new ImportConnectionsPanelOne();
        secondPanel = new ImportConnectionsPanelTwo();

        List<JPanel> panels = new ArrayList<JPanel>();
        panels.add(firstPanel);
        panels.add(secondPanel);

        model = new ImportConnectionsModel(panels);
        setModel(model);

        prepare();
    }

    private boolean doNext() {

        int index = model.getSelectedIndex();
        if (index == 0) {

            if (!firstPanel.canProceed()) {

                return false;
            }

            secondPanel.start();
            doImport();
        }
        return true;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {

        parent.finished();
    }

    private void doImport() {

        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {

                    System.out.println(Bundles.get("ImportConnectionPanel.ImportingConnectionsFromFile"));

                    setButtonsEnabled(false);

                    parent.block();
                    boolean result = save();

                    if (result) {

                        setButtonsEnabled(true);
                        setNextButtonEnabled(false);
                        setBackButtonEnabled(true);
                        setCancelButtonEnabled(true);
                        setCancelButtonText(Bundles.get("common.finish.button"));

                        secondPanel.stop();

                        secondPanel.append(Bundles.get("ExportConnectionsPanel.done"));
                        secondPanel.append(Bundles.get("ImportConnectionPanel.TheImportFileWasProcessedSuccessfully"));

                        System.out.println(Bundles.get("ImportConnectionPanel.FinishedImportingConnectionsFromFile"));
                    }

                } finally {

                    ConnectionsTreePanel.getPanelFromBrowser().connectionAdded(null);
                    parent.unblock();
                }
            }
        });

    }

    @Override
    public void progress(String message) {

        secondPanel.append(message);
    }

    private boolean save() {

        try {

            String importFile = firstPanel.getImportPath();

            ConnectionImport connectionImport = new ConnectionImporter().read(importFile, this);

            EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(
                    this, ConnectionRepositoryEvent.CONNECTION_IMPORTED, connectionImport.getConnections()));

            secondPanel.append(getString("ImportConnectionPanel.ProcessedFoldersAndConnections", connectionImport.getFolderCount(), connectionImport.getConnectionCount()));

            return true;

        } catch (RepositoryException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    bundleString("ExceptionErrorMessage", e.getMessage()), e);
            return false;
        }

    }


    class ImportConnectionsModel extends DefaultWizardProcessModel {

        ImportConnectionsModel(List<JPanel> panels) {

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

