/*
 * NotepadDockedPanel.java
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

package org.executequery.gui;

import java.awt.BorderLayout;

public class NotepadDockedPanel extends AbstractDockedTabActionPanel {

    public static final String TITLE = "Notepad";

    public static final String MENU_ITEM_KEY = "viewNotepad";
    
    public static final String PROPERTY_KEY = "system.display.notepad";

    private ScratchPadPanel scratchPadPanel;
    
    public NotepadDockedPanel() {

        super(new BorderLayout());
        
        scratchPadPanel = new ScratchPadPanel();
        scratchPadPanel.getPanelToolBar().remove(0);
        add(scratchPadPanel, BorderLayout.CENTER);
    }

    public boolean tabViewClosing() {
        return true;
    }

    public boolean tabViewSelected() {
        return true;
    }

    public boolean tabViewDeselected() {
        return true;
    }

    public String toString() {
        return TITLE;
    }

    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    public String getTitle() {
        return TITLE;
    }

    
    
}






