/*
 * DockedDragEvent.java
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

import java.awt.event.MouseEvent;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Defines a docked tab panel mouse event.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DockedDragEvent {
    
    /** the associated mouse event */
    private MouseEvent mouseEvent;
    
    /** the tab pane where this event began */
    private DockedTabPane sourceTabPane;
    
    /** the tab pane where this event will (may) end */
    private DockedTabPane destinationTabPane;
    
    /** the component being dragged */
    private TabComponent tabComponent;
    
    /** Creates a new instance of DockedDragEvent */
    protected DockedDragEvent(DockedTabPane tabPane,
                              MouseEvent mouseEvent,
                              TabComponent tabComponent) {
        this.sourceTabPane = tabPane;
        this.tabComponent = tabComponent;
        this.mouseEvent = mouseEvent;
    }

    protected void translatePoint(int x, int y) {
        if (mouseEvent == null) {
            return;
        }
        mouseEvent.translatePoint(x, y);
    }
    
    protected int getX() {
        if (mouseEvent == null) {
            return -1;
        }
        return mouseEvent.getX();
    }

    protected int getY() {
        if (mouseEvent == null) {
            return -1;
        }
        return mouseEvent.getY();
    }

    protected MouseEvent getMouseEvent() {
        return mouseEvent;
    }

    protected void setMouseEvent(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
    }

    protected DockedTabPane getSourceTabPane() {
        return sourceTabPane;
    }

    protected void setSourceTabPane(DockedTabPane tabPane) {
        this.sourceTabPane = tabPane;
    }

    public DockedTabPane getDestinationTabPane() {
        return destinationTabPane;
    }

    public void setDestinationTabPane(DockedTabPane destinationTabPane) {
        this.destinationTabPane = destinationTabPane;
    }

    public TabComponent getTabComponent() {
        return tabComponent;
    }

    public void setTabComponent(TabComponent tabComponent) {
        this.tabComponent = tabComponent;
    }
    
}














