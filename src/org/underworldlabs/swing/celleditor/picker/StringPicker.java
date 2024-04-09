/*
 * StringCellEditor.java
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

package org.underworldlabs.swing.celleditor.picker;

import org.underworldlabs.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Simple string value table column cell editor.
 *
 * @author Takis Diakoumis
 */
public class StringPicker extends JTextField
        implements DefaultPicker {

    public StringPicker() {
        super();

        setHorizontalAlignment(JTextField.LEFT);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    }

    @Override
    public String getValue() {
        return getText();
    }

    @Override
    public void setValue(Object value) {
        setText(value != null ? value.toString() : Constants.EMPTY);
    }

    @Override
    public JTextField getEditorComponent() {
        return this;
    }

}
