/*
 * TableColumnPanel.java
 *
 * Copyright (C) 2002-2013 Takis Diakoumis
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.print.Printable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.executequery.databaseobjects.DatabaseObjectElement;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.print.TablePrinter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class SimpleMetaDataPanel extends AbstractFormObjectViewPanel {
    
    public static final String NAME = "SimpleMetaDataPanel";

    private static final String INDEX_HEADER_TEXT = "Table Index";

    private static final String FOREIGN_KEY_HEADER_TEXT = "Foreign Key";
    
    private DisabledField nameField;
    
    private JTable table;

    private SimpleMetaDataModel model;

    /** the browser's control object */
    private BrowserController controller;

    public SimpleMetaDataPanel(BrowserController controller) {
        super();
        this.controller = controller;
        try {
            init();
        }
        catch (Exception e) {
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
        paramPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        paramPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Properties", paramPanel);
        
        nameField = new DisabledField();
        //tableNameField = new DisabledField();
        
        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(12,5,5,5);
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
        base.add(nameField, gbc);

        setContentPanel(base);
    }
    
    public String getLayoutName() {
        return NAME;
    }
    
    public void refresh() {
        // nothing to do here
    }
    
    public void cleanup() {
        // nothing to do here
    }
    
    public Printable getPrintable() {

        return new TablePrinter(table, getHeaderText() + ": " + nameField.getText());
    }
    
    public void setValues(NamedObject namedObject) {

        if (!(namedObject instanceof DatabaseObjectElement)) {
            
            throw new IllegalArgumentException("Requires valid DatabaseObjectElement instance");
        }
        
        try {

            nameField.setText(namedObject.getName());
            model.setValues(((DatabaseObjectElement) namedObject).getMetaData());
            setHeaderText(headerTextForType(namedObject));

        } catch (DataSourceException e) {

            controller.handleException(e);
            model.setValues(null);
        }

    }

    private String headerTextForType(NamedObject namedObject) {
        
        if (namedObject.getType() == NamedObject.TABLE_INDEX) {
            
            return INDEX_HEADER_TEXT;

        } else {
            
            return FOREIGN_KEY_HEADER_TEXT;                
        }

    }
    
}
