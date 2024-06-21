/*
 * DriverListPanel.java
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

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.datasource.DatabaseDefinition;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DatabaseDriverEvent;
import org.executequery.event.DatabaseDriverListener;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseDefinitionCache;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Driver root node view panel.
 *
 * @author Takis Diakoumis
 */
public class DriversPanel extends JPanel
        implements DatabaseDriverListener {

    public static final String TITLE = Bundles.getCommon("drivers");
    public static final String FRAME_ICON = "icon_db_driver";

    private JButton addDriverButton;
    private JButton removeDriverButton;

    private JTable driversTable;
    private DriversTableModel model;
    private List<DatabaseDriver> drivers;

    public DriversPanel() {
        super();

        init();
        arrange();
        updateTable();
        EventMediator.registerListener(this);
    }

    private void init() {
        drivers = loadDrivers();

        // --- drivers table ---

        driversTable = new DefaultTable(new DriversTableModel());
        driversTable.setColumnSelectionAllowed(false);
        driversTable.getTableHeader().setReorderingAllowed(false);
        driversTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                maybeEditDriver(e);
            }
        });

        TableColumnModel tcm = driversTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(80);
        tcm.getColumn(1).setPreferredWidth(140);
        tcm.getColumn(2).setPreferredWidth(70);

        // ---  buttons ---

        addDriverButton = WidgetFactory.createButton(
                "addDriverButton",
                bundleString("addDriver"),
                e -> addDriver()
        );

        removeDriverButton = WidgetFactory.createButton(
                "removeDriverButton",
                bundleString("removeDriver"),
                e -> removeDriver()
        );

    }

    private void arrange() {
        GridBagHelper gbh;

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).fillHorizontally();
        mainPanel.add(new JLabel(bundleString("driversLabel")), gbh.setMinWeightY().spanX().get());
        mainPanel.add(addDriverButton, gbh.nextRowFirstCol().setMinWeightX().setWidth(1).fillNone().get());
        mainPanel.add(removeDriverButton, gbh.nextCol().leftGap(0).get());
        mainPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().fillHorizontally().spanX().get());
        mainPanel.add(new JScrollPane(driversTable), gbh.nextRowFirstCol().setMaxWeightY().leftGap(5).bottomGap(5).fillBoth().get());

        // --- base ---

        gbh = new GridBagHelper().fillBoth();
        setLayout(new GridBagLayout());
        add(mainPanel, gbh.spanX().spanY().get());
    }

    private List<DatabaseDriver> loadDrivers() {
        Repository repo = RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseDriverRepository)
            return ((DatabaseDriverRepository) repo).findAll();

        return new ArrayList<>();
    }

    public void addDriver() {
        new CreateDriverDialog();
        updateTable();
        saveDrivers();
    }

    private void removeDriver() {

        int row = driversTable.getSelectedRow();
        if (row < 0 || row >= model.getRowCount())
            return;

        DatabaseDriver driver = (DatabaseDriver) model.getValueAt(row, 0);

        int result = GUIUtilities.displayConfirmDialog(String.format(bundleString("confirmRemoving"), driver));
        if (result != JOptionPane.YES_OPTION)
            return;

        drivers.remove(driver);
        updateTable();
        saveDrivers();
    }

    private void maybeEditDriver(MouseEvent e) {

        if (e.getClickCount() < 2)
            return;

        try {
            GUIUtilities.showWaitCursor();

            int row = driversTable.rowAtPoint(new Point(e.getX(), e.getY()));
            if (row < 0 || row >= model.getRowCount())
                return;

            DatabaseDriver driver = (DatabaseDriver) model.getValueAt(row, 0);
            if (driver != null)
                new CreateDriverDialog(driver);

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private void updateTable() {
        model = new DriversTableModel(drivers);
        driversTable.setModel(model);
    }

    private void saveDrivers() {
        Repository repo = RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID);
        if (repo instanceof DatabaseDriverRepository)
            ((DatabaseDriverRepository) repo).save();
    }

    private static String bundleString(String key) {
        return Bundles.get(DriversPanel.class, key);
    }

    private static class DriversTableModel extends AbstractTableModel {

        private static final String[] HEADERS = {
                bundleString("driverName"),
                bundleString("description"),
                bundleString("database"),
                bundleString("class"),
        };

        private final List<DatabaseDriver> values;

        public DriversTableModel() {
            this(new ArrayList<>());
        }

        public DriversTableModel(List<DatabaseDriver> values) {
            this.values = values;
        }

        @Override
        public int getRowCount() {
            return values != null ? values.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return HEADERS.length;
        }

        @Override
        public String getColumnName(int col) {
            return HEADERS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return values.get(row);
                case 1:
                    return values.get(row).getDescription();
                case 2:
                    DatabaseDefinition definition = DatabaseDefinitionCache.getDatabaseDefinition(values.get(row).getType());
                    return definition != null ? definition.getName() : Constants.EMPTY;
                case 3:
                    return values.get(row).getClassName();
            }

            return null;
        }

    } // DriversTableModel class

    // --- DatabaseDriverListener impl ---

    @Override
    public void driversUpdated(DatabaseDriverEvent databaseDriverEvent) {
        if (databaseDriverEvent.getSource() instanceof DatabaseDriver)
            updateTable();
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof DatabaseDriverEvent;
    }

}
