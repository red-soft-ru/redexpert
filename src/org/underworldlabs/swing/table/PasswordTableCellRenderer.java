/*
 * PasswordTableCellRenderer.java
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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class PasswordTableCellRenderer extends JLabel
                                       implements TableCellRenderer {
    
    private final char echoChar;

    public PasswordTableCellRenderer() {
        this('*');
    }
    
    public PasswordTableCellRenderer(char echoChar) {
        this.echoChar = echoChar;
    }

    public Component getTableCellRendererComponent(JTable table, 
                                                   Object value,
                                                   boolean isSelected, 
                                                   boolean hasFocus,
                                                   int row, 
                                                   int column) {
        
        if (isSelected) {

            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());

        } else {
        
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }

        if (value != null) {
        
            setText(passwordChars(value.toString()));
        
        } else {
            
            setText("");
        }
        return this;
    }

    private String passwordChars(String string) {

        StringBuilder sb = new StringBuilder();

        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {

            sb.append(echoChar);
        }
        
        return sb.toString();
    }
    
}






