/*
 * TabContainerSplitPane.java
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

import java.awt.Component;
import javax.swing.SwingConstants;
import org.underworldlabs.swing.FlatSplitPane;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TabContainerSplitPane extends FlatSplitPane {
    
    /** the left/north root pane */
    private BaseRootPane northRootPane;
    
    /** the right/south root pane */
    private BaseRootPane southRootPane;    
    
    public TabContainerSplitPane() {
        super();
    }
    
    public TabContainerSplitPane(int newOrientation) {
        super(newOrientation);
    }
    
    public TabContainerSplitPane(int newOrientation, boolean newContinuousLayout) {
        super(newOrientation, newContinuousLayout);
    }
    
    public TabContainerSplitPane(int newOrientation,
                         Component newLeftComponent, Component newRightComponent) {
        super(newOrientation, newLeftComponent, newRightComponent);
    }
    
    public TabContainerSplitPane(int newOrientation, boolean newContinuousLayout,
                         Component newLeftComponent, Component newRightComponent) {
        
        super(newOrientation, false, newLeftComponent, newRightComponent);
    }
    
    public void setGlassPaneVisible(int position, boolean visible) {
        BaseRootPane rootPane = null;
        switch (position) {
            case SwingConstants.TOP:
            case SwingConstants.LEFT:
                rootPane = northRootPane;
                break;
            case SwingConstants.BOTTOM:
            case SwingConstants.RIGHT:
                rootPane = southRootPane;
                break;
        }
        if (rootPane != null) {
            rootPane.setGlassPaneVisible(visible);
        }
    }
    
    /**
     * Override to add the component to the root pane first.
     *
     * @param c - the component to add
     */
    public void setLeftComponent(Component c) {
        if (c == null) {
            super.setLeftComponent(c);
            northRootPane = null;
            return;
        }
        if (northRootPane == null) {
            northRootPane = new BaseRootPane(c);
        }
        super.setLeftComponent(northRootPane);
    }

    /**
     * Override to add the component to the root pane first.
     *
     * @param c - the component to add
     */
    public void setTopComponent(Component c) {
        if (c == null) {
            super.setTopComponent(c);
            northRootPane = null;
            return;
        }
        if (northRootPane == null) {
            northRootPane = new BaseRootPane(c);
        }
        super.setTopComponent(northRootPane);
    }

    /**
     * Override to add the component to the root pane first.
     *
     * @param c - the component to add
     */
    public void setRightComponent(Component c) {
        if (c == null) {
            super.setRightComponent(c);
            southRootPane = null;
            return;
        }
        if (southRootPane == null) {
            southRootPane = new BaseRootPane(c);
        }
        super.setRightComponent(southRootPane);
    }

    /**
     * Override to add the component to the root pane first.
     *
     * @param c - the component to add
     */
    public void setBottomComponent(Component c) {
        if (c == null) {
            super.setBottomComponent(c);
            southRootPane = null;
            return;
        }
        if (southRootPane == null) {
            southRootPane = new BaseRootPane(c);
        }
        super.setBottomComponent(southRootPane);
    }

    /**
     * Override to set the divider location to the mid-point
     * where the stored value is <= 0.
     */
    public void restoreDividerLocation() {
        if (getStoredDividerLocation() > 0) {
            setDividerLocation(getStoredDividerLocation());
        } else {
            setDividerLocation(getHeight()/2);
        }
    }

}















