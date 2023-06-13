/*
 * ColumnConstraintRenderer.java
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
import org.executequery.databaseobjects.impl.ColumnConstraint;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author takisd
 */
public class ColumnConstraintRenderer extends DefaultTableCellRenderer {

    /**
     * deleted flag icon
     */
    private ImageIcon deleteImage;

    /**
     * new column flag icon
     */
    private ImageIcon newImage;

    /**
     * Creates a new instance of ColumnConstraintRenderer
     */
    public ColumnConstraintRenderer() {
        deleteImage = GUIUtilities.loadIcon("MarkDeleted16.svg", true);
        newImage = GUIUtilities.loadIcon("MarkNew16.svg", true);
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
            ColumnConstraint constraint = (ColumnConstraint) value;
            if (constraint.isMarkedDeleted()) {
                setIcon(deleteImage);
                setToolTipText("This constraint marked to be dropped");
            } else if (constraint.isNewConstraint()) {
                setIcon(newImage);
                setToolTipText("This constraint marked new");
            } else {
                setIcon(null);
                setToolTipText(null);
            }
        }

        setHorizontalAlignment(JLabel.CENTER);
        return this;
    }
}












