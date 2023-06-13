/*
 * TabViewDriverPanel.java
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

package org.executequery.gui.drivers;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.RepositoryException;

import javax.swing.*;
import java.awt.print.Printable;

public class TabViewDriverPanel extends AbstractFormObjectViewPanel {

    private DriverFieldsPanel panel;

    private final DriverViewPanel parent;

    public TabViewDriverPanel(DriverViewPanel parent) {

        this.parent = parent;

        panel = new DriverFieldsPanel();

        setHeaderText("Database Driver");
        setHeaderIcon(GUIUtilities.loadIcon("DatabaseDriver24.svg"));

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);

        setContentPanel(scrollPane);
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    private boolean panelSelected = true;

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {

        panelSelected = false;
        return populateAndSave();
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {

        panelSelected = true;
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {

        return tabViewClosing();
    }

    // --------------------------------------------

    public void setDriver(DatabaseDriver driver) {

        if (populateAndSave()) {

            panel.setDriver(driver);
        }
    }

    public boolean saveDrivers() {

        return panel.saveDrivers();
    }

    private boolean populateAndSave() {

        panel.populateDriverObject();
        DatabaseDriver driver = panel.getDriver();

        if (driver != null) {

            if (driverNameExists(driver)) {

                String message = String.format(
                        "The driver name %s already exists.", driver.getName());
                GUIUtilities.displayErrorMessage(message);

                return false;
            }

            parent.nodeNameValueChanged(driver);

            return panel.saveDrivers();
        }

        return true;
    }

    private boolean driverNameExists(DatabaseDriver driver) {

        return databaseDriverRepository().nameExists(driver, driver.getName());
    }

    private DatabaseDriverRepository databaseDriverRepository() {

        return ((DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID));
    }

    public void cleanup() {

        // nothing to do here
    }

    public String getLayoutName() {

        return DriverPanel.TITLE;
    }

    public Printable getPrintable() {

        return null;
    }


    public class DriverFieldsPanel extends AbstractDriverPanel {

        private DriverFieldsPanel() {
        }

        public void driverNameChanged() {

            if (panelSelected) {

                populateAndSave();
            }

        }

        public boolean saveDrivers() {

            try {

                databaseDriverRepository().save();
                return true;

            } catch (RepositoryException e) {

                GUIUtilities.displayErrorMessage(e.getMessage());

                return false;
            }

        }

    }

}







