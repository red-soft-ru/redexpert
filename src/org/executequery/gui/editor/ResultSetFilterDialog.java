/*
 * VisibleResultSetColumnsDialog.java
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

package org.executequery.gui.editor;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.resultset.ResultSetColumnHeader;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ResultSetFilterDialog extends BaseDialog {

    public static final String TITLE = bundleString("title");

    private final ResultSetTable table;
    private final QueryEditorResultsPanel resultsPanel;
    private List<ResultSetColumn> columns;
    private JList fullList;

    private JButton okButton;
    private JButton cancelButton;
    private JButton selectAllButton;
    private JTextField filterField;
    private JTextField searchField;

    public ResultSetFilterDialog(ResultSetTable table, QueryEditorResultsPanel resultsPanel) {
        super(TITLE, true);
        this.table = table;
        this.resultsPanel = resultsPanel;

        init();
        arrane();
    }

    private void init() {

        this.columns = getColumns(getTableModel());
        this.fullList = new ResultSetColumnList(columns);

        okButton = WidgetFactory.createButton(
                "okButton",
                Bundles.get("common.ok.button"),
                e -> updateResultSet()
        );

        cancelButton = WidgetFactory.createButton(
                "cancelButton",
                Bundles.get("common.cancel.button"),
                e -> dispose()
        );

        selectAllButton = WidgetFactory.createButton(
                "selectAllButton",
                bundleString("selectAll"),
                e -> selectAll(fullList)
        );

        filterField = WidgetFactory.createTextField("filterField");

        searchField = WidgetFactory.createTextField("searchField");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filter(searchField.getText());
            }
        });

    }

    private void arrane() {
        GridBagHelper gbh;

        // --- filter panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 12, 5, 5).anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("filterLabel")), gbh.setMinWeightX().get());
        mainPanel.add(filterField, gbh.nextCol().leftGap(0).topGap(10).setMaxWeightX().spanX().get());
        mainPanel.add(new JLabel(bundleString("searchLabel")), gbh.nextRowFirstCol().setWidth(1).leftGap(7).topGap(7).setMinWeightX().get());
        mainPanel.add(searchField, gbh.nextCol().leftGap(0).topGap(5).setMaxWeightX().get());
        mainPanel.add(selectAllButton, gbh.nextCol().setMinWeightX().get());
        mainPanel.add(new JScrollPane(fullList), gbh.nextRowFirstCol().leftGap(5).topGap(0).fillBoth().spanX().spanY().get());

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(0, 5, 5, 5).anchorEast();
        buttonPanel.add(okButton, gbh.get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

        // --- base panel ---

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.setPreferredSize(new Dimension(400, 350));

        basePanel.add(mainPanel, BorderLayout.CENTER);
        basePanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- base ---

        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        add(basePanel);

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    public void updateResultSet() {

        String filterString = filterField.getText();
        if (!MiscUtils.isNull(filterString))
            resultsPanel.filter(filterString);

        int visibleCount = 0;
        for (ResultSetColumn column : columns) {
            for (ResultSetColumnHeader resultSetColumnHeader : getTableModel().getColumnHeaders()) {

                if (column.id.equals(resultSetColumnHeader.getId())) {
                    if (column.visible)
                        visibleCount++;

                    resultSetColumnHeader.setVisible(column.visible);
                    break;
                }
            }
        }

        if (visibleCount != 0) {
            table.columnVisibilityChanged();
            dispose();

        } else
            GUIUtilities.displayErrorMessage("At least one column from this result set must be visible");
    }

    private void selectAll(JList list) {

        boolean curFlag = true;
        for (ResultSetColumn column : columns)
            curFlag = curFlag && column.visible;

        for (ResultSetColumn column : columns)
            column.visible = !curFlag;

        list.repaint();
    }

    private void filter(String searchText) {

        try {

            if (searchText == null || searchText.isEmpty()) {
                fullList.setModel(new ResultSetColumnList(columns).getModel());
                return;
            }

            List<ResultSetFilterDialog.ResultSetColumn> filteredList = new ArrayList<>();
            for (ResultSetColumn column : columns)
                if (column.toString().toLowerCase().contains(searchText.toLowerCase()))
                    filteredList.add(column);

            fullList.setModel(new ResultSetColumnList(filteredList).getModel());

        } finally {
            repaint();
        }
    }

    private ResultSetTableModel getTableModel() {
        return (ResultSetTableModel) ((TableSorter) table.getModel()).getTableModel();
    }

    private List<ResultSetColumn> getColumns(ResultSetTableModel tableModel) {

        List<ResultSetColumn> list = new ArrayList<>();
        for (ResultSetColumnHeader resultSetColumnHeader : tableModel.getColumnHeaders())
            list.add(new ResultSetColumn(resultSetColumnHeader.getId(), resultSetColumnHeader.getLabel(), resultSetColumnHeader.isVisible()));

        return list;
    }

    private static String bundleString(String key) {
        return Bundles.get(ResultSetFilterDialog.class, key);
    }

    static class ResultSetColumnList extends JList {

        public ResultSetColumnList(List<ResultSetColumn> columns) {
            super(columns.toArray(new ResultSetColumn[0]));
            setCellRenderer(new CheckboxListRenderer());

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE)
                        setCheckBoxesVisible(getSelectedIndex());
                }
            });

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    setCheckBoxesVisible(locationToIndex(e.getPoint()));
                }
            });

        }

        private void setCheckBoxesVisible(int index) {
            if (index != -1) {
                ResultSetColumn checkbox = (ResultSetColumn) getModel().getElementAt(index);
                checkbox.setVisible(!checkbox.visible);
                repaint();
            }
        }

    } // class ResultSetColumnList

    static class CheckboxListRenderer extends JCheckBox implements ListCellRenderer {

        private final Border noFocusBorder = new EmptyBorder(3, 5, 3, 3);

        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            ResultSetColumn resultSetColumn = (ResultSetColumn) value;

            setText(resultSetColumn.name);
            setSelected(resultSetColumn.visible);

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setFocusPainted(false);

            setBackground(resultSetColumn.highlight ? Color.YELLOW : list.getBackground());
            setBorderPainted(true);
            setBorder(noFocusBorder);

            return this;
        }

    } // class CheckboxListRenderer

    static class ResultSetColumn {

        private final String id;
        private final String name;
        private boolean visible;
        private final boolean highlight;

        public ResultSetColumn(String id, String name, boolean visible) {
            this.id = id;
            this.name = name;
            this.visible = visible;
            this.highlight = false;
        }

        public String getId() {
            return id;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        @Override
        public String toString() {
            return name;
        }

    } // class ResultSetColumn

}
