/*
 * RowNumberHeader.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.underworldlabs.swing.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Provides row numbers for a <code>JTable</code>.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class RowNumberHeader extends JList
                             implements ListSelectionListener {

    /** The table to apply the row header */
    protected JTable table;

    /** the row count to be displayed */
    protected int rowCount;

    /** the list model */
    protected RowHeaderListModel model;

    /** the default min width */
    private static final int MINIMUM_WIDTH = 20;

    public RowNumberHeader(JTable table) {
        this.table = table;
        initRowHeaderView();

        model = new RowHeaderListModel();
        setModel(model);

        addListSelectionListener(this);
        setCellRenderer(new RowHeaderRenderer(table));
    }

    protected void initRowHeaderView() {
        if (table == null) {
            return;
        }
        rowCount = table.getRowCount();

        // determine the width based on the largest number displayed
        JTableHeader header = table.getTableHeader();
        Font headerFont = header.getFont();
        FontMetrics metrics = header.getFontMetrics(headerFont);

        String rowValueString = String.valueOf(rowCount) + " ";
        int width = Math.max(MINIMUM_WIDTH, metrics.stringWidth(rowValueString));
        // add a couple of pixels left/right
        width += 10;
        setFixedCellWidth(width);
        setFixedCellHeight(table.getRowHeight());

        // force an update of the model
        if (model != null) {
            model.contentsChanged();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        int[] selections = getSelectedIndices();
        if (selections != null && selections.length > 0) {
            table.clearSelection();
            table.setColumnSelectionAllowed(false);
            table.setRowSelectionAllowed(true);

            for (int i = 0; i < selections.length; i++) {
                table.addRowSelectionInterval(selections[i], selections[i]);
            }

        }
    }

    public void setTable(JTable table) {
        this.table = table;
        initRowHeaderView();
    }


    class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            setOpaque(true);
            setHorizontalAlignment(RIGHT);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, UIUtils.getDefaultBorderColour()));
            setFont(UIManager.getFont("TableHeader.font"));
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("TableHeader.background"));
            //setToolTipText("Add this to the row selection");
        }

        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {
            
            setText((value == null) ? "" : value.toString() + " ");
            
            return this;
        }

    } // class RowHeaderRenderer


    class RowHeaderListModel extends AbstractListModel {

        RowHeaderListModel() {}

        public int getSize() {
            return rowCount;
        }

        public Object getElementAt(int index) {
            return String.valueOf(index + 1);
        }

        protected void contentsChanged() {
            fireContentsChanged(this, -1, -1);
        }

    } // class RowHeaderListModel

}


