/*
 * PropertiesToolBar.java
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

package org.executequery.gui.prefs;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.toolbar.ButtonComparator;
import org.underworldlabs.swing.toolbar.ToolBarButton;
import org.underworldlabs.swing.toolbar.ToolBarProperties;
import org.underworldlabs.swing.toolbar.ToolBarWrapper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class PropertiesToolBar extends AbstractPropertiesBasePanel {

    private Vector selections;

    private JTable table;

    private ToolBarButtonModel toolButtonModel;
    private static IconCellRenderer iconRenderer;
    private static NameCellRenderer nameRenderer;

    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton addSeparatorButton;
    private JButton removeSeparatorButton;

    /**
     * The tool bar name
     */
    private String toolBarName;
    /**
     * The tool bar wrapper
     */
    private ToolBarWrapper toolBar;

    public PropertiesToolBar(String toolBarName) {
        this.toolBarName = toolBarName;

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() {
        ReflectiveAction action = new ReflectiveAction(this);

        moveUpButton = ActionUtilities.createButton(
                action,
                GUIUtilities.loadIcon("Up16.svg", true),
                null,
                "moveUp");

        moveDownButton = ActionUtilities.createButton(
                action,
                GUIUtilities.loadIcon("Down16.svg", true),
                null,
                "moveDown");

        moveUpButton.setMargin(Constants.EMPTY_INSETS);
        moveDownButton.setMargin(Constants.EMPTY_INSETS);

        addSeparatorButton = ActionUtilities.createButton(
                action,
                bundledString("AddSeparator"),
                "addSeparator");
        addSeparatorButton.setToolTipText(bundleString("AddSeparatorToolTip"));

        removeSeparatorButton = ActionUtilities.createButton(
                action,
                bundledString("RemoveSeparator"),
                "removeSeparator");
        removeSeparatorButton.setToolTipText(bundleString("RemoveSeparatorToolTip"));

        ToolBarWrapper _toolBar = ToolBarProperties.getToolBar(toolBarName);
        toolBar = (ToolBarWrapper) _toolBar.clone();
        selections = toolBar.getButtonsVector();
        setInitialValues();

        iconRenderer = new IconCellRenderer();
        nameRenderer = new NameCellRenderer();

        toolButtonModel = new ToolBarButtonModel();
        table = new JTable(toolButtonModel);
        setTableProperties();

        JScrollPane scroller = new JScrollPane(table);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getViewport().setBackground(table.getBackground());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets.bottom = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel(bundleString(toolBarName.replace(" ", ""))), gbc);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scroller, gbc);
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.insets.bottom = 10;
        gbc.insets.right = 10;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(addSeparatorButton, gbc);
        gbc.gridx++;
        gbc.insets.right = 0;
        panel.add(removeSeparatorButton, gbc);

        JPanel movePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridy = 0;
        gbc2.insets.bottom = 10;
        gbc2.anchor = GridBagConstraints.CENTER;
        movePanel.add(moveUpButton, gbc2);
        gbc2.gridy++;
        gbc2.insets.bottom = 5;
        gbc2.insets.top = 5;
        movePanel.add(new JLabel(Bundles.getCommon("move")), gbc2);
        gbc2.gridy++;
        gbc2.insets.bottom = 10;
        movePanel.add(moveDownButton, gbc2);

        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 0;
        gbc.insets.left = 5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(movePanel, gbc);

        addContent(panel);
    }

    private void setTableProperties() {
        table.setTableHeader(null);
        table.setColumnSelectionAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setRowHeight(28);
        table.doLayout();

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);

        TableColumn col = tcm.getColumn(1);
        col.setPreferredWidth(40);
        col.setCellRenderer(iconRenderer);

        col = tcm.getColumn(2);
        col.setPreferredWidth(251);
        col.setCellRenderer(nameRenderer);
    }

    private void setInitialValues() {
        Collections.sort(selections, new ButtonComparator());
    }

    public void restoreDefaults() {
        ToolBarWrapper _toolBar = ToolBarProperties.getDefaultToolBar(toolBarName);
        toolBar = (ToolBarWrapper) _toolBar.clone();

        selections = toolBar.getButtonsVector();
        Collections.sort(selections, new ButtonComparator());
        toolButtonModel.fireTableRowsUpdated(0, selections.size() - 1);
    }

    public void save() {
        int size = selections.size();
        Vector buttons = new Vector(selections.size());

        // update the buttons
        for (int i = 0; i < size; i++) {
            ToolBarButton tb = (ToolBarButton) selections.elementAt(i);

            if (tb.isVisible())
                tb.setOrder(i);
            else
                tb.setOrder(1000);

            buttons.add(tb);

        }

        toolBar.setButtonsVector(buttons);
        ToolBarProperties.resetToolBar(toolBarName, toolBar);
    }

    public void addSeparator(ActionEvent e) {
        int selection = table.getSelectedRow();
        if (selection == -1) {
            return;
        }

        ToolBarButton tb = new ToolBarButton(ToolBarButton.SEPARATOR_ID);
        tb.setOrder(selection);
        tb.setVisible(true);

        selections.insertElementAt(tb, selection);
        toolButtonModel.fireTableRowsInserted(selection == 0 ? 0 : selection - 1,
                selection == 0 ? 1 : selection);
    }

    public void removeSeparator(ActionEvent e) {
        int selection = table.getSelectedRow();
        if (selection == -1) {
            return;
        }

        ToolBarButton remove = (ToolBarButton) selections.elementAt(selection);
        if (!remove.isSeparator()) {
            return;
        }

        selections.removeElementAt(selection);
        toolButtonModel.fireTableRowsDeleted(selection, selection);
    }

    public void moveUp(ActionEvent e) {
        int selection = table.getSelectedRow();
        if (selection <= 0) {
            return;
        }

        int newPostn = selection - 1;
        ToolBarButton move = (ToolBarButton) selections.elementAt(selection);
        selections.removeElementAt(selection);
        selections.add(newPostn, move);
        table.setRowSelectionInterval(newPostn, newPostn);
        toolButtonModel.fireTableRowsUpdated(newPostn, selection);

    }

    public void moveDown(ActionEvent e) {
        int selection = table.getSelectedRow();
        if (selection == -1 || selection == selections.size() - 1) {
            return;
        }

        int newPostn = selection + 1;
        ToolBarButton move = (ToolBarButton) selections.elementAt(selection);
        selections.removeElementAt(selection);
        selections.add(newPostn, move);
        table.setRowSelectionInterval(newPostn, newPostn);
        toolButtonModel.fireTableRowsUpdated(selection, newPostn);
    }

    private class ToolBarButtonModel extends AbstractTableModel {

        public ToolBarButtonModel() {
        }

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return selections.size();
        }

        public Object getValueAt(int row, int col) {
            ToolBarButton tbb = (ToolBarButton) selections.elementAt(row);

            switch (col) {
                case 0:
                    return new Boolean(tbb.isVisible());
                case 1:
                    return tbb.getIcon();
                case 2:
                    return tbb.getName();
                default:
                    return null;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            ToolBarButton tbb = (ToolBarButton) selections.elementAt(row);

            if (col == 0)
                tbb.setVisible(((Boolean) value).booleanValue());

            fireTableRowsUpdated(row, row);
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        public Class getColumnClass(int col) {
            if (col == 0)
                return Boolean.class;
            else
                return String.class;
        }

        public void addNewRow() {

        }

    } // CreateTableModel

    public class NameCellRenderer extends JLabel
            implements TableCellRenderer {

        public NameCellRenderer() {
            //setFont(panelFont);
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {

            setBackground(isSelected ? table.getSelectionBackground() :
                    table.getBackground());

            setForeground(isSelected ? table.getSelectionForeground() :
                    table.getForeground());

            setText(value.toString());
            setBorder(null);

            return this;
        }

    } // class NameCellRenderer


    public class IconCellRenderer extends JLabel
            implements TableCellRenderer {

        public IconCellRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {

            setBackground(isSelected ? table.getSelectionBackground() :
                    table.getBackground());

            setForeground(isSelected ? table.getSelectionForeground() :
                    table.getForeground());

            setHorizontalAlignment(JLabel.CENTER);

            setIcon((ImageIcon) value);

            return this;
        }

    } // class IconCellRenderer

}















