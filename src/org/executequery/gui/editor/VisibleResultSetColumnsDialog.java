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
import org.executequery.gui.resultset.ResultSetColumnHeader;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.table.TableSorter;

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
public class VisibleResultSetColumnsDialog extends BaseDialog {

    public static final String TITLE = bundleString("title");

    private final ResultSetTable table;
    private final List<ResultSetColumn> columns;
    private final JList fullList;

    public VisibleResultSetColumnsDialog(ResultSetTable table) {

        super(TITLE, true);
        this.table = table;
        this.columns = getColumns(getTableModel());
        this.fullList = new ResultSetColumnList(columns);

        init();
        pack();

        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
    }

    private void init() {

        GridBagHelper gridBagHelper;

        // --- swing components ---

        JButton okButton = new JButton(Bundles.get("common.ok.button"));
        okButton.addActionListener(e -> updateResultSet());

        JButton cancelButton = new JButton(Bundles.get("common.cancel.button"));
        cancelButton.addActionListener(e -> dispose());

        JButton selectAllCheck = new JButton(bundleString("selectAll"));
        selectAllCheck.addActionListener(e -> selectAll(fullList));

        // --- checkBoxPanel ---

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        checkBoxPanel.setBorder(BorderFactory.createEtchedBorder());

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(7, 10, 5, 5).anchorNorthWest();

        checkBoxPanel.add(selectAllCheck, gridBagHelper.nextCol().setMinWeightX().get());
        checkBoxPanel.add(new JScrollPane(fullList), gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        // --- buttonPanel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(3, 5, 0, 5).anchorEast();

        buttonPanel.add(okButton, gridBagHelper.get());
        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());

        // --- mainPanel ---

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(400, 350));

        mainPanel.add(checkBoxPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- base ---

        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        add(mainPanel);

        // ---

    }

    public void updateResultSet() {

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
        return Bundles.get(VisibleResultSetColumnsDialog.class, key);
    }

    static class ResultSetColumnList extends JList {

        public ResultSetColumnList(List<ResultSetColumn> columns) {

            super(columns.toArray(new ResultSetColumn[columns.size()]));
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

            setBackground(list.getBackground());
            setBorderPainted(true);
            setBorder(noFocusBorder);

            return this;
        }

    } // class CheckboxListRenderer

    static class ResultSetColumn {

        private final String id;
        private final String name;
        private boolean visible;

        public ResultSetColumn(String id, String name, boolean visible) {
            this.id = id;
            this.name = name;
            this.visible = visible;
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
