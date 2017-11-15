/*
 * PasswordCellEditor.java
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

package org.underworldlabs.swing.table;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.underworldlabs.Constants;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 */
public class PasswordCellEditor extends JPasswordField
                              implements TableCellEditorValue {
    
    public PasswordCellEditor() {
        super();
        setBorder(null);
        setHorizontalAlignment(JTextField.LEFT);
    }
    
    /**
     * Returns the current editor value from the component
     * defining this object.
     *
     * @return the editor's value
     */
    public String getEditorValue() {
        return MiscUtils.charsToString(getPassword());
    }

    /**
     * Resets the editor's value to an empty string.
     */
    public void resetValue() {
        setText(Constants.EMPTY);
    }
    
    /**
     * Returns the current editor value string.
     */
    public String getValue() {
        return getEditorValue();
    }
    
    /**
     * Sets the editor's value to that specified.
     *
     * @param value - the value to be set
     */
    public void setValue(String value) {
        setText(value);
    }
    
}












