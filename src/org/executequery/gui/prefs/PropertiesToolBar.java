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

import org.executequery.GUIUtilities;
import org.executequery.gui.IconManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class PropertiesToolBar extends AbstractPropertiesBasePanel {

    private ToolBarWrapper toolBar;
    private final String toolBarName;
    private Vector<ToolBarButton> selections;

    private JTable table;
    private ToolBarButtonModel toolButtonModel;

    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton addSeparatorButton;
    private JButton removeSeparatorButton;

    public PropertiesToolBar(PropertiesPanel parent, String toolBarName) {
        super(parent);
        this.toolBarName = toolBarName;

        init();
        arrange();
        checkEnableButtons();
    }

    private void init() {

        moveUpButton = WidgetFactory.createButton(
                "moveUpButton",
                IconManager.getIcon("icon_move_up"),
                e -> moveButtonUp()
        );

        moveDownButton = WidgetFactory.createButton(
                "moveDownButton",
                IconManager.getIcon("icon_move_down"),
                e -> moveButtonDown()
        );

        addSeparatorButton = WidgetFactory.createButton(
                "addSeparatorButton",
                bundledString("AddSeparator"),
                e -> addSeparator()
        );

        removeSeparatorButton = WidgetFactory.createButton(
                "removeSeparatorButton",
                bundledString("RemoveSeparator"),
                e -> removeSeparator()
        );

        ToolBarWrapper originalToolBar = ToolBarProperties.getToolBar(toolBarName);
        if (originalToolBar != null) {
            toolBar = (ToolBarWrapper) originalToolBar.clone();
            selections = toolBar.getButtonsVector();
            selections.sort(new ButtonComparator());
        }

        toolButtonModel = new ToolBarButtonModel();
        table = new JTable(toolButtonModel);
        setTableProperties();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- scroll pane ---

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(table.getBackground());

        // --- tools panel ---

        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().bottomGap(5).fillHorizontally();
        toolsPanel.add(moveUpButton, gbh.get());
        toolsPanel.add(new JLabel(Bundles.getCommon("move"), null, SwingConstants.CENTER), gbh.nextRow().get());
        toolsPanel.add(moveDownButton, gbh.nextRow().bottomGap(30).get());
        toolsPanel.add(addSeparatorButton, gbh.nextRow().bottomGap(5).get());
        toolsPanel.add(new JLabel(bundledString("Separator"), null, SwingConstants.CENTER), gbh.nextRow().get());
        toolsPanel.add(removeSeparatorButton, gbh.nextRow().bottomGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally().anchorNorthWest().setMaxWeightY();
        mainPanel.add(scrollPane, gbh.setMaxWeightX().fillBoth().get());
        mainPanel.add(toolsPanel, gbh.nextCol().setMinWeightX().leftGap(5).get());

        // --- base ---

        addContent(mainPanel);
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

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                checkEnableButtons();
            }
        });

        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(30);

        TableColumn column = columnModel.getColumn(1);
        column.setPreferredWidth(40);
        column.setCellRenderer(new IconCellRenderer());

        column = columnModel.getColumn(2);
        column.setPreferredWidth(251);
        column.setCellRenderer(new NameCellRenderer());
    }

    private void checkEnableButtons() {

        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            enableButtons(false, false);
            return;
        }

        ToolBarButton selectedButton = selections.elementAt(selectedRow);
        enableButtons(true, selectedButton.isSeparator());
    }

    private void enableButtons(boolean enableMove, boolean enableSeparator) {
        moveUpButton.setEnabled(enableMove);
        moveDownButton.setEnabled(enableMove);
        addSeparatorButton.setEnabled(enableMove);
        removeSeparatorButton.setEnabled(enableMove && enableSeparator);
    }

    // --- button handlers ---

    private void moveButtonUp() {

        int selection = table.getSelectedRow();
        if (selection <= 0)
            return;

        int newPosition = selection - 1;
        ToolBarButton toolBarButton = selections.elementAt(selection);
        selections.removeElementAt(selection);
        selections.add(newPosition, toolBarButton);
        table.setRowSelectionInterval(newPosition, newPosition);
        toolButtonModel.fireTableRowsUpdated(newPosition, selection);
    }

    private void moveButtonDown() {

        int selection = table.getSelectedRow();
        if (selection == -1 || selection == selections.size() - 1)
            return;

        int newPosition = selection + 1;
        ToolBarButton toolBarButton = selections.elementAt(selection);
        selections.removeElementAt(selection);
        selections.add(newPosition, toolBarButton);
        table.setRowSelectionInterval(newPosition, newPosition);
        toolButtonModel.fireTableRowsUpdated(selection, newPosition);
    }

    private void addSeparator() {

        int selection = table.getSelectedRow();
        if (selection == -1)
            return;

        ToolBarButton toolBarButton = new ToolBarButton(ToolBarButton.SEPARATOR_ID);
        toolBarButton.setOrder(selection);
        toolBarButton.setVisible(true);

        selections.insertElementAt(toolBarButton, selection);
        toolButtonModel.fireTableRowsInserted(
                selection == 0 ? 0 : selection - 1,
                selection == 0 ? 1 : selection
        );

        table.setRowSelectionInterval(selection, selection);
        checkEnableButtons();
    }

    private void removeSeparator() {

        int selection = table.getSelectedRow();
        if (selection == -1)
            return;

        ToolBarButton remove = selections.elementAt(selection);
        if (!remove.isSeparator())
            return;

        selections.removeElementAt(selection);
        toolButtonModel.fireTableRowsDeleted(selection, selection);

        table.setRowSelectionInterval(selection, selection);
        checkEnableButtons();
    }

    // --- UserPreferenceFunction impl ---

    @Override
    public void save() {

        for (int i = 0; i < selections.size(); i++) {
            ToolBarButton toolBarButton = selections.elementAt(i);
            toolBarButton.setOrder(i);
        }

        toolBar.setButtonsVector(selections);
        ToolBarProperties.resetToolBar(toolBarName, toolBar);
    }

    @Override
    public void restoreDefaults() {

        ToolBarWrapper originalToolBar = ToolBarProperties.getToolBar(toolBarName);
        if (originalToolBar == null)
            return;

        toolBar = (ToolBarWrapper) originalToolBar.clone();
        selections = toolBar.getButtonsVector();
        selections.sort(new ButtonComparator());
        toolButtonModel.fireTableRowsUpdated(0, selections.size() - 1);
    }

    // ---

    private class ToolBarButtonModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return selections.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            ToolBarButton toolBarButton = selections.elementAt(row);

            switch (col) {
                case 0:
                    return toolBarButton.isVisible();
                case 1:
                    return toolBarButton.getIcon();
                case 2:
                    return toolBarButton.getName();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0)
                selections.elementAt(row).setVisible((Boolean) value);
            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return col == 0 ? Boolean.class : String.class;
        }

    } // CreateTableModel

    private static class NameCellRenderer extends JLabel
            implements TableCellRenderer {

        public NameCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setText(value.toString());
            setBorder(null);

            return this;
        }

    } // NameCellRenderer class


    private static class IconCellRenderer extends JLabel
            implements TableCellRenderer {

        public IconCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setHorizontalAlignment(JLabel.CENTER);
            setIcon((ImageIcon) value);

            return this;
        }

    } // IconCellRenderer class

}
