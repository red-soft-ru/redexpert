/*
 * AutoCompleteListItemCellRenderer.java
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

package org.executequery.gui.editor.autocomplete;

import org.executequery.gui.IconManager;

import javax.swing.*;
import java.awt.*;

public class AutoCompleteListItemCellRenderer extends DefaultListCellRenderer {

    private static final int TEXT_ICON_GAP = 10;

    private static final ImageIcon animatedSpinner;
    private static final Icon databaseTable;
    private static final Icon databaseTableColumn;
    private static final Icon systemFunction;
    private static final Icon databaseFunction;
    private static final Icon databaseProcedure;
    private static final Icon databasePackage;
    private static final Icon variable;
    private static final Icon parameter;
    private static final Icon databaseTableView;

    static {
        animatedSpinner = IconManager.getIcon("icon_loading");
        databaseTable = IconManager.getIcon("icon_db_table");
        databaseTableColumn = IconManager.getIcon("icon_db_table_column");
        databaseTableView = IconManager.getIcon("icon_db_view");
        systemFunction = IconManager.getIcon("icon_db_function_system");
        databaseFunction = IconManager.getIcon("icon_db_function");
        databaseProcedure = IconManager.getIcon("icon_db_procedure");
        databasePackage = IconManager.getIcon("icon_db_package");
        variable = IconManager.getIcon("icon_variable");
        parameter = IconManager.getIcon("icon_function");
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        JLabel listLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        listLabel.setIconTextGap(TEXT_ICON_GAP);

        AutoCompleteListItem item = (AutoCompleteListItem) value;
        switch (item.getType()) {

            case DATABASE_TABLE:
                setIcon(databaseTable);
                break;

            case DATABASE_VIEW:
                setIcon(databaseTableView);
                break;

            case DATABASE_TABLE_COLUMN:
                setIcon(databaseTableColumn);
                break;

            case DATABASE_FUNCTION:
                setIcon(databaseFunction);
                break;

            case DATABASE_PROCEDURE:
                setIcon(databaseProcedure);
                break;

            case DATABASE_PACKAGE:
                setIcon(databasePackage);
                break;

            case SYSTEM_FUNCTION:
                setIcon(systemFunction);
                break;

            case VARIABLE:
                setIcon(variable);
                break;

            case PARAMETER:
                setIcon(parameter);
                break;

            case GENERATING_LIST:
                setBackground(list.getBackground());
                setForeground(list.getForeground());
                setIcon(animatedSpinner(list, index));
                setBorder(noFocusBorder);
                break;

            default:
                setIcon(null);
                break;
        }

        return listLabel;
    }

    private ImageIcon animatedSpinner(final JList list, final int row) {

        AutoCompleteListItemCellRenderer.animatedSpinner.setImageObserver((img, infoflags, x, y, width, height) -> {

            if (list.isShowing() && (infoflags & (FRAMEBITS | ALLBITS)) != 0) {

                if (list.getSelectedIndex() == row)
                    list.repaint();

                if (list.isShowing())
                    list.repaint(list.getCellBounds(row, row));
            }

            return (infoflags & (ALLBITS | ABORT)) == 0;
        });

        return AutoCompleteListItemCellRenderer.animatedSpinner;
    }

}
