/*
 * DefaultTabViewActionPanel.java
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

package org.executequery.base;

import java.awt.LayoutManager;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.ActionPanel;

/**
 * Default implementation for a tab panel view as an action panel.
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultTabViewActionPanel extends ActionPanel
                                       implements TabView {
    
    public DefaultTabViewActionPanel() {
        super();
    }
    
    public DefaultTabViewActionPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    public DefaultTabViewActionPanel(LayoutManager layout) {
        super(layout);
    }

    public DefaultTabViewActionPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This will trigger the glass pane on or off 
     * and set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    protected void setInProcess(final boolean inProcess) {
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
    
    /**
     * Returns whether the glass pane is currently visible.
     */
    public boolean isGlassPaneVisible() {
        return GUIUtilities.isGlassPaneVisible();
    }
    
    /**
     * Indicates the panel is being removed from the pane.
     *
     * @return true if all ok to proceed, false otherwise
     */
    public boolean tabViewClosing() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane.
     *
     * @return true if all ok to proceed, false otherwise
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     *
     *  @return true if all ok to proceed, false otherwise
     */
    public boolean tabViewDeselected() {
        return true;
    }

    
}











