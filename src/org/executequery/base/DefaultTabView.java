/*
 * DefaultTabView.java
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

package org.executequery.base;

import java.awt.LayoutManager;
import javax.swing.JPanel;
import org.executequery.GUIUtilities;

/**
 * Default implementation for a tab panel view.
 * 
 * @author   Takis Diakoumis
 */
public class DefaultTabView extends JPanel 
                            implements TabView {
    
    public DefaultTabView() {
        super();
    }
    
    public DefaultTabView(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    public DefaultTabView(LayoutManager layout) {
        super(layout);
    }
    
    public DefaultTabView(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
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












