/*
 * AbstractDockedTabActionPanel.java
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

package org.executequery.gui;

import java.awt.LayoutManager;
import org.executequery.GUIUtilities;
import org.executequery.base.DockedTabView;
import org.underworldlabs.swing.ActionPanel;

/**
 * Abstract tab action panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class AbstractDockedTabActionPanel extends ActionPanel 
                                                   implements DockedTabView {
    
    /** Creates a new instance of AbstractDockedTabActionPanel */
    public AbstractDockedTabActionPanel() {
        super();
    }
    
    public AbstractDockedTabActionPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    public AbstractDockedTabActionPanel(LayoutManager layout) {
        super(layout);
    }
    
    public AbstractDockedTabActionPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This will trigger the glass pane on or off 
     * and set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
        GUIUtilities.setGlassPaneVisible(inProcess);
        if (inProcess) {
            GUIUtilities.showWaitCursor();
        } else {
            GUIUtilities.showNormalCursor();
        }
    }

    /**
     * Toggles the visibility of the glass pane on the 
     * enclosing frame as specified.
     *
     * @param visible - true | false
     */
    public void setGlassPaneVisible(boolean visible) {
        GUIUtilities.setGlassPaneVisible(visible);
    }

    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    
    public int getUserPreferencePosition() {
        return GUIUtilities.getDockedComponentPosition(getPropertyKey());
    }

    public abstract String getPropertyKey();
    
    public abstract String getMenuItemKey();

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    
}














