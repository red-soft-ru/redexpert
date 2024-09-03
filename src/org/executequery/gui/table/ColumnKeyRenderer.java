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

import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.impl.DatabaseTableColumn;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author takisd
 */
public class ColumnKeyRenderer extends DefaultTableCellRenderer
        implements ColumnKeyStates {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        if (col > 0)
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        Icon icon = null;
        String tooltip = null;

        if (value instanceof DatabaseColumn) {
            DatabaseColumn databaseColumn = (DatabaseColumn) value;

            if (databaseColumn.isPrimaryKey()) {
                if (databaseColumn.isForeignKey()) {
                    tooltip = primaryForeignTooltip;
                    icon = primaryForeignImage;

                } else {
                    tooltip = primaryTooltip;
                    icon = primaryImage;
                }

            } else if (databaseColumn.isForeignKey()) {
                tooltip = foreignTooltip;
                icon = foreignImage;
            }

            if (databaseColumn instanceof DatabaseTableColumn) {
                DatabaseTableColumn databaseTableColumn = (DatabaseTableColumn) databaseColumn;

                if (databaseTableColumn.isMarkedDeleted()) {
                    tooltip = deleteTooltip;
                    icon = deleteImage;

                } else if (databaseTableColumn.isNewColumn()) {
                    tooltip = newTooltip;
                    icon = newImage;
                }
            }
        }

        setIcon(icon);
        setToolTipText(tooltip);
        setHorizontalAlignment(JLabel.CENTER);
        return this;
    }

}
