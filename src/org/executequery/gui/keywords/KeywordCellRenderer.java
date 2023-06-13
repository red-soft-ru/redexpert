/*
 * KeywordCellRenderer.java
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

package org.executequery.gui.keywords;

import org.executequery.Constants;
import org.executequery.GUIUtilities;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;


/**
 * @author Takis Diakoumis
 */
public class KeywordCellRenderer extends JLabel
        implements TableCellRenderer {

    /**
     * icon for a SQL92 keyword
     */
    private Icon sql92;

    /**
     * icon for a user defined keyword
     */
    private Icon userDefined;

    /**
     * icon for a database specific keyword
     */
    private Icon databaseSpecific;

    /**
     * Creates a new instance of KeywordCellRenderer
     */
    public KeywordCellRenderer() {
        sb = new StringBuffer();
        setFont(new Font("Dialog", Font.PLAIN, 11));
        sql92 = GUIUtilities.loadIcon("Sql92.svg", true);
        userDefined = GUIUtilities.loadIcon("User16.svg", true);
        databaseSpecific = GUIUtilities.loadIcon("DatabaseKeyword16.svg", true);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean cellHasFocus,
                                                   int row,
                                                   int col) {

        SqlKeyword keyword = (SqlKeyword) value;
        if (keyword.isSql92()) {
            setIcon(sql92);
        } else if (keyword.isDatabaseSpecific()) {
            setIcon(databaseSpecific);
        } else if (keyword.isUserDefined()) {
            setIcon(userDefined);
        }
        setToolTipText(buildToolTip(keyword));
        setText(keyword.getText());

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }

    /**
     * tool tip concat buffer
     */
    private StringBuffer sb;

    private String buildToolTip(SqlKeyword keyword) {
        // reset
        sb.setLength(0);

        // build the html display
        sb.append("<html>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td><b>");
        sb.append(keyword.getText());
        sb.append("</b></td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("<hr>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td>");
        if (keyword.isSql92()) {
            sb.append("SQL92 Standard Keyword");
        } else if (keyword.isDatabaseSpecific()) {
            setIcon(databaseSpecific);
            sb.append("Database Defined Keyword:  ");
            sb.append(keyword.getDatabaseProductName());
        } else if (keyword.isUserDefined()) {
            sb.append("User Defined Keyword");
        }
        sb.append("</td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("</html>");
        return sb.toString();
    }

    public boolean isOpaque() {
        return true;
    }

}















