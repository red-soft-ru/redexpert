/*
 * ConstraintCellRenderer.java
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
import org.executequery.gui.browser.ColumnConstraint;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class ConstraintCellRenderer extends JLabel
        implements TableCellRenderer {

    private static ImageIcon deleteImage;
    private static ImageIcon newImage;

    static {
        deleteImage = GUIUtilities.loadIcon("MarkDeleted16.svg", true);
        newImage = GUIUtilities.loadIcon("MarkNew16.svg", true);
    }

    /**
     * Creates a new instance of ConstraintCellRenderer
     */
    public ConstraintCellRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {


        ColumnConstraint cc = (ColumnConstraint) value;
        if (cc.isMarkedDeleted()) {
            setIcon(deleteImage);
            setToolTipText("This value marked to be dropped");
        } else if (cc.isNewConstraint()) {
            setIcon(newImage);
            setToolTipText("This value marked new");
        } else {
            setIcon(null);
        }

        setHorizontalAlignment(JLabel.CENTER);

        return this;
    }

}







