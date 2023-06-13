/*
 * ColumnKeyRenderer.java
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

package org.executequery.gui.table;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author takisd
 */
public class ColumnKeyRenderer extends DefaultTableCellRenderer {

    /**
     * foreign key image icon
     */
    private ImageIcon fkImage;

    /**
     * primary key image icon
     */
    private ImageIcon pkImage;

    /**
     * primary/foreign key image icon
     */
    private ImageIcon pkfkImage;

    /**
     * deleted flag icon
     */
    private ImageIcon deleteImage;

    /**
     * new column flag icon
     */
    private ImageIcon newImage;

    public ColumnKeyRenderer() {
        deleteImage = GUIUtilities.loadIcon("MarkDeleted16.svg", true);
        newImage = GUIUtilities.loadIcon("MarkNew16.svg", true);
        fkImage = GUIUtilities.loadIcon("ForeignKeyImage.svg", true);
        pkImage = GUIUtilities.loadIcon("PrimaryKeyImage.svg", true);
        pkfkImage = GUIUtilities.loadIcon("PrimaryForeignKeyImage.svg", true);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int col) {

        if (col > 0) {
            return super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);
        }

        if (value != null) {
            DatabaseColumn column = (DatabaseColumn) value;
            if (column.isPrimaryKey()) {
                if (column.isForeignKey()) {
                    setIcon(pkfkImage);
                    setToolTipText("Primary Key/Foreign Key");
                } else {
                    setIcon(pkImage);
                    setToolTipText("Primary Key");
                }
            } else if (column.isForeignKey()) {
                setIcon(fkImage);
                setToolTipText("Foreign Key");
            } else {
                setIcon(null);
                setToolTipText(null);
            }

            // if its an editable column - check its state
            // and reset icons and tooltips accordingly
            if (column instanceof DatabaseTableColumn) {
                DatabaseTableColumn _column = (DatabaseTableColumn) column;
                if (_column.isMarkedDeleted()) {
                    setIcon(deleteImage);
                    setToolTipText("This column marked to be dropped");
                } else if (_column.isNewColumn()) {
                    setIcon(newImage);
                    setToolTipText("This column marked new");
                }
            }

        }

        setHorizontalAlignment(JLabel.CENTER);

        return this;
    }

}







