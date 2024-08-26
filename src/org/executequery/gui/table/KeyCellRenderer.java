/*
 * KeyCellRenderer.java
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

import org.executequery.gui.browser.ColumnData;
import org.executequery.localization.Bundles;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class KeyCellRenderer extends JLabel
        implements TableCellRenderer,
        ColumnKeyStates {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Icon icon = null;
        String tooltip = null;

        if (value instanceof ColumnData) {
            ColumnData formattedValue = (ColumnData) value;

            if (formattedValue.isMarkedDeleted()) {
                tooltip = deleteTooltip;
                icon = deleteImage;

            } else if (formattedValue.isNewColumn()) {
                tooltip = newTooltip;
                icon = newImage;

            } else if (formattedValue.isPrimaryKey()) {
                if (formattedValue.isForeignKey()) {
                    tooltip = primaryForeignTooltip;
                    icon = primaryForeignImage;

                } else {
                    tooltip = primaryTooltip;
                    icon = primaryImage;
                }

            } else if (formattedValue.isForeignKey()) {
                tooltip = foreignTooltip;
                icon = foreignImage;
            }

        } else if (value instanceof String) {
            String formattedValue = (String) value;

            if (formattedValue.trim().equals("PRIMARY KEY")) {
                tooltip = primaryTooltip;
                icon = primaryImage;

            } else if (formattedValue.trim().equals("FOREIGN KEY")) {
                tooltip = foreignTooltip;
                icon = foreignImage;
            }
        }

        setIcon(icon);
        setToolTipText(tooltip);
        setHorizontalAlignment(JLabel.CENTER);
        return this;
    }

    public static String bundleString(String key, Object... args) {
        return Bundles.get(KeyCellRenderer.class, key, args);
    }

}
