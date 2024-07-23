/*
 * DialogDriverPanel.java
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

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.SimpleButtonsPanel;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.spi.DefaultDatabaseDriver;
import org.executequery.event.DatabaseDriverEvent;
import org.executequery.event.DefaultDatabaseDriverEvent;
import org.executequery.gui.ActionDialog;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.RepositoryException;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class CreateDriverDialog extends ActionDialog {

    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 375;

    private final DriverFieldsPanel panel;
    private final boolean isNewDriver;

    public CreateDriverDialog() {
        this(new DefaultDatabaseDriver(System.currentTimeMillis(), bundleString("newDriver")), bundleString("title"));
    }

    public CreateDriverDialog(DatabaseDriver driver) {
        this(driver, bundleString("edit-title"));
    }

    private CreateDriverDialog(DatabaseDriver driver, String title) {
        super(title, true);
        isNewDriver = Objects.equals(title, bundleString("title"));

        panel = new DriverFieldsPanel(driver);
        panel.setBorder(BorderFactory.createEtchedBorder());

        JPanel base = new JPanel(new BorderLayout());
        base.add(panel, BorderLayout.CENTER);
        base.add(createButtonsPanel(), BorderLayout.SOUTH);

        addDisplayComponentWithEmptyBorder(base);
        setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        display();
    }

    private JPanel createButtonsPanel() {
        return new SimpleButtonsPanel(
                this,
                Bundles.get("common.save.button"),
                "populateAndSave",
                Bundles.get("common.cancel.button"),
                "dispose"
        );
    }

    @SuppressWarnings("unused")
    public void populateAndSave() {
        panel.populateDriverObject();

        DatabaseDriver driver = panel.getDriver();
        if (driver == null)
            return;

        if (isNewDriver && driverNameExists(driver)) {
            String message = String.format(bundleString("DriverExists"), driver.getName());
            GUIUtilities.displayErrorMessage(message);
            return;
        }

        if (isNewDriver)
            addDriver(driver);

        if (save(driver))
            dispose();
    }

    private boolean save(DatabaseDriver driver) {

        try {
            databaseDriverRepository().save();
            EventMediator.fireEvent(new DefaultDatabaseDriverEvent(driver, DatabaseDriverEvent.DRIVERS_UPDATED));

        } catch (RepositoryException e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
            return false;
        }

        return true;
    }

    private void addDriver(DatabaseDriver driver) {
        databaseDriverRepository().findAll().add(driver);
    }

    private boolean driverNameExists(DatabaseDriver driver) {
        return databaseDriverRepository().nameExists(driver, driver.getName());
    }

    private DatabaseDriverRepository databaseDriverRepository() {
        return ((DatabaseDriverRepository) RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID));
    }

    private static String bundleString(String key) {
        return Bundles.get(CreateDriverDialog.class, key);
    }

    private class DriverFieldsPanel extends AbstractDriverPanel {

        private DriverFieldsPanel(DatabaseDriver driver) {
            setDriver(driver);
        }

        @Override
        public void driverNameChanged() {
        }

        @Override
        public boolean saveDrivers() {
            populateDriverObject();
            return save(getDriver());
        }

    } // DriverFieldsPanel class

}
