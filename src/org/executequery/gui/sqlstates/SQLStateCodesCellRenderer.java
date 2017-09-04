/*
 * SQLStateCodesCellRenderer.java
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

package org.executequery.gui.sqlstates;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.executequery.Constants;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SQLStateCodesCellRenderer extends JLabel 
                                       implements TableCellRenderer {

    private static Font classHeaderFont;
    
    /** Creates a new instance of KeywordCellRenderer */
    public SQLStateCodesCellRenderer() {
        sb = new StringBuilder();
    }
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean cellHasFocus,
                                                   int row, 
                                                   int col) {

        if (classHeaderFont == null) {
            Font font = table.getFont();
            classHeaderFont = font.deriveFont(Font.BOLD);
        }
        
        SQLStateCode code = (SQLStateCode)value;
        setToolTipText(buildToolTip(code));
        
        String subClass = code.getSqlStateSubClass();
        if (subClass.equals("000")) {
            setFont(classHeaderFont);
        } else {
            setFont(table.getFont());
        }
        
        switch (col) {
            case 0:
                setText(code.getSqlStateClass());
                setHorizontalAlignment(JLabel.CENTER);
                break;
            case 1:
                setText(code.getSqlStateSubClass());
                setHorizontalAlignment(JLabel.CENTER);
                break;
            case 2:
                setText(code.getDescription());
                break;
        }

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        return this;
    }
    
    /** tool tip concat buffer */
    private StringBuilder sb;
    
    private String buildToolTip(SQLStateCode code) {
        // reset
        sb.setLength(0);
        
        // build the html display
        sb.append("<html>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append("<tr><td><b>SQL State Code: ");
        sb.append(code.getSqlStateClass());
        sb.append("-");
        sb.append(code.getSqlStateSubClass());
        sb.append("</b></td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("<hr>");
        sb.append(Constants.TABLE_TAG_START);
        sb.append(code.getDescription());
        sb.append("</td></tr>");
        sb.append(Constants.TABLE_TAG_END);
        sb.append("</html>");
        return sb.toString();
    }
    
    public boolean isOpaque() {
        return true;
    }

}


