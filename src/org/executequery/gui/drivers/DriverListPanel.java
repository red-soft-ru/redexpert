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

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.print.TablePrinter;
import org.executequery.repository.DatabaseDefinitionCache;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.util.List;

/**
 * Driver root node view panel.
 *
 * @author Takis Diakoumis
 */
public class DriverListPanel extends AbstractFormObjectViewPanel
        implements MouseListener,
        ActionListener {

    public static final String NAME = "DriverListPanel";

    /**
     * the table display
     */
    private JTable table;

    /**
     * the table model
     */
    private DriversTableModel model;

    /**
     * the parent panel containing the selection tree
     */
    private DriverViewPanel parent;

    /**
     * Creates a new instance of DriverListPanel
     */
    public DriverListPanel(DriverViewPanel parent) {

        super();

        this.parent = parent;

        init();
    }

    private void init() {

        model = new DriversTableModel(loadDrivers());
        table = new DefaultTable(model);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // add the mouse listener for selection clicks
        table.addMouseListener(this);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(80);
        tcm.getColumn(1).setPreferredWidth(140);
        tcm.getColumn(2).setPreferredWidth(70);

        // new connection button
        JButton button = WidgetFactory.createButton("newDriverButton", "New Driver");
        button.addActionListener(this);

        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.add(new JScrollPane(table), getPanelConstraints());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Drivers"));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("User defined JDBC drivers."), gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Select the New Driver button to register a new " +
                "driver with the system"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.insets.bottom = 0;
        panel.add(button, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 10;
        gbc.insets.top = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(tablePanel, gbc);

        setHeaderText("JDBC Drivers");
        setHeaderIcon(GUIUtilities.loadIcon("DatabaseDriver24.svg"));
        setContentPanel(panel);
    }

    private List<DatabaseDriver> loadDrivers() {

        return ((DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID)).findAll();
    }

    public void actionPerformed(ActionEvent e) {
        GUIUtilities.ensureDockedTabVisible(DriversTreePanel.PROPERTY_KEY);
        parent.addNewDriver();
    }

    // ----------------------------------
    // MouseListener implementation
    // ----------------------------------

    public void mouseClicked(MouseEvent e) {

        // only interested in double clicks
        if (e.getClickCount() < 2) {
            return;
        }

        // get the row double-clicked
        int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
        if (row == -1) {
            return;
        }

        // select the driver in the tree
        if (row < model.getRowCount()) {
            DatabaseDriver driver = (DatabaseDriver) model.getValueAt(row, 0);
            GUIUtilities.ensureDockedTabVisible(DriversTreePanel.PROPERTY_KEY);
            parent.setSelectedDriver(driver);
        }


    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public String getLayoutName() {

        return NAME;
    }

    public void refresh() {
    }

    public void cleanup() {
    }

    public Printable getPrintable() {
        return new TablePrinter(table, "JDBC Drivers");
    }

    private class DriversTableModel extends AbstractTableModel {

        private List<DatabaseDriver> values;
        private String[] header = {"Driver Name", "Description",
                "Database", "Class"};

        DriversTableModel(List<DatabaseDriver> values) {

            this.values = values;
        }
        
        /*
        public void setValues(List<DatabaseDriver> values) {
            
            this.values = values;
            
            fireTableDataChanged();
        }
        */

        public int getRowCount() {

            if (values != null) {

                return values.size();
            }
            return 0;
        }

        public int getColumnCount() {

            return header.length;
        }

        public String getColumnName(int col) {

            return header[col];
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return values.get(row);
                case 1:
                    return values.get(row).getDescription();
                case 2:
                    return DatabaseDefinitionCache.
                            getDatabaseDefinition(values.get(row).getType()).getName();
                case 3:
                    return values.get(row).getClassName();
            }
            return values.get(row);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

}











