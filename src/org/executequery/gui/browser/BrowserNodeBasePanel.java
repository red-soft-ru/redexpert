/*
 * BrowserNodeBasePanel.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.print.Printable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.executequery.gui.SortableColumnsTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.print.TablePrinter;
import org.underworldlabs.swing.DisabledField;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
abstract class BrowserNodeBasePanel extends AbstractFormObjectViewPanel {
    
    private DisabledField typeField;
    
    private JPanel tablePanel;
    
    private JScrollPane scroller;
    
    private JTable table;
    
    private DisabledField rowCountField;

    BrowserNodeBasePanel(String labelText) {

        super();
        try {

            init(labelText);

        } catch (Exception e) {

            e.printStackTrace();
        }
        
    }
    
    private void init(String labelText) throws Exception {
        
        JPanel base = new JPanel(new GridBagLayout());
        typeField = new DisabledField();
        
        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(10,5,5,5);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        base.add(new JLabel(labelText), gbc);
        gbc.insets.top = 8;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        base.add(typeField, gbc);
        
        table = createTable();

        tablePanel = new JPanel(new GridBagLayout());
        scroller = new JScrollPane(table,
                                   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        tablePanel.add(scroller, getPanelConstraints());
        
        rowCountField = new DisabledField();
        
        // add to the panel
        gbc.insets.top = 10;
        gbc.weighty = 1.0;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(tablePanel, gbc);
        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 0;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.insets.bottom = 5;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.ipady = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        base.add(creatCountPanel(), gbc);
        
        setContentPanel(base);
    }

    private JPanel creatCountPanel() {
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0;
        gbc.insets.top = 1;
        gbc.insets.left = 1;
        gbc.insets.right = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Object Count:"), gbc);
        gbc.gridx = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        panel.add(rowCountField, gbc);

        return panel;
    }
    
    private JTable createTable() {
        
        final JTable table = new SortableColumnsTable() {
            
            @Override
            public void setModel(final TableModel dataModel) {
                dataModel.addTableModelListener(new TableModelListener() {
                    public void tableChanged(TableModelEvent e) {
                        rowCountField.setText(String.valueOf(dataModel.getRowCount()));
                    }
                });
                super.setModel(dataModel);
            }
            
        };

        table.getTableHeader().setReorderingAllowed(false);
        table.setColumnSelectionAllowed(false);

        return table;
    }
    
    protected final DisabledField typeField() {
        
        return typeField;
    }

    protected final JPanel tablePanel() {
        
        return tablePanel;
    }

    protected final JScrollPane scroller() {
        
        return scroller;
    }

    protected final JTable table() {
        
        return table;
    }

    public JTable getTable() {

        return table();
    }

    public final Printable getPrintable() {

        return new TablePrinter(
                table(), getPrintablePrefixLabel() + typeField().getText());
    }

    protected abstract String getPrintablePrefixLabel();
    
    /** Performs some cleanup and releases resources before being closed. */
    public abstract void cleanup();
    
    /** Refreshes the data and clears the cache */
    public abstract void refresh();
    
    /** Returns the name of this panel */
    public abstract String getLayoutName();

}


