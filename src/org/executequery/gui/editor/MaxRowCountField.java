/*
 * MaxRowCountField.java
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

package org.executequery.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.TextFieldFocusController;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
final class MaxRowCountField extends NumberTextField 
                              implements TextFieldFocusController,
                                         FocusListener,
                                         ActionListener {
    
    private QueryEditor queryEditor;
    
    MaxRowCountField(QueryEditor queryEditor) {
        super();
        
        this.queryEditor = queryEditor;
        
        setValue(SystemProperties.getIntProperty("user", "editor.max.records"));
        setToolTipText("Set the maximum rows returned (-1 for all)");
        setFocusAccelerator('r');
        
        addFocusListener(this);
        addActionListener(this);
    }
 
    public void actionPerformed(ActionEvent e) {
        queryEditor.resetCaretPositionToLast();
    }

    public void focusGained(FocusEvent e) {
        selectAll();
    }

    public void focusLost(FocusEvent e) {}

}


