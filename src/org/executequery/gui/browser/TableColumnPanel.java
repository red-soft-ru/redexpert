/*
 * TableColumnPanel.java
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

package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.print.TablePrinter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;

import javax.swing.*;
import java.awt.*;
import java.awt.print.Printable;

/**
 * @author Takis Diakoumis
 */
public class TableColumnPanel extends AbstractFormObjectViewPanel {

    private static final String DEFAULT_HEADER_TEXT = "Table Column";

    private static final String PK_HEADER_TEXT = "Table Column - Primary Key";

    private static final String FK_HEADER_TEXT = "Table Column - Foreign Key";

    public static final String NAME = "TableColumnPanel";

    private DisabledField colNameField;

    private JTable table;

    private SimpleMetaDataModel model;

    /**
     * the browser's control object
     */
    private final BrowserController controller;

    public TableColumnPanel(BrowserController controller) {
        super();
        this.controller = controller;
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        model = new SimpleMetaDataModel();
        table = new DefaultTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        JPanel paramPanel = new JPanel(new BorderLayout());
        paramPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        paramPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Properties", paramPanel);

        colNameField = new DisabledField();
        //tableNameField = new DisabledField();

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(12, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        gbc.insets = insets;
        gbc.gridy++;
        base.add(new JLabel("Name:"), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        //base.add(new JLabel("Table Name:"), gbc);
        gbc.insets.right = 10;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(tabs, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.bottom = 5;
        gbc.insets.left = 5;
        gbc.insets.top = 10;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        base.add(colNameField, gbc);

        setContentPanel(base);
        setHeaderText(DEFAULT_HEADER_TEXT);
        setHeaderIcon(GUIUtilities.loadIcon("TableColumn24.svg"));

    }

    public String getLayoutName() {
        return NAME;
    }

    public void refresh() {
        // nothing to do here
    }

    public void cleanup() {
        super.cleanup();
        // nothing to do here
    }

    public Printable getPrintable() {

        return new TablePrinter(table, "Table Column: " + colNameField.getText());
    }

    public void setValues(DatabaseColumn column) {

        try {

            colNameField.setText(column.getName());
            model.setValues(column.getMetaData());

            if (column.isPrimaryKey()) {

                setHeaderText(PK_HEADER_TEXT);

            } else if (column.isForeignKey()) {

                setHeaderText(FK_HEADER_TEXT);

            } else {

                setHeaderText(DEFAULT_HEADER_TEXT);
            }

        } catch (DataSourceException e) {

            controller.handleException(e);
            model.setValues(null);
        }

    }

}


