/*
 * BaseApplicationPane.java
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.underworldlabs.swing.FlatSplitPane;

/**
 *
 * @author   Takis Diakoumis
 */
public class BaseApplicationPane extends JPanel
                                 implements PropertyChangeListener {
   
    /** The outline panel shown when dragging */
    private DragPanel dragPanel;
    
    /** Temp value for the tab pane a move is occurring from */
    private DockedTabPane fromTabPane;
    
    /** Temp value for the tab pane a move is occurring to */
    private DockedTabPane toTabPane;

    /** The tab pane position for a new tab */
    private int newTabPanePosition;
    
    /** The mediator/controller class */
    private DesktopMediator desktopMediator;

    // ---------------------------------------
    // primary desktop components
    // ---------------------------------------
    
    /** the left main split pane */
    private FlatSplitPane leftSplitPane;

    /** the right main split pane */
    private FlatSplitPane rightSplitPane;

    
    /** Creates a new instance of BaseApplicationPane */
    public BaseApplicationPane(DesktopMediator desktopMediator) {
        super(new BorderLayout());
        this.desktopMediator = desktopMediator;
        init();
    }
    
    private void init() {
        // init the main split panes
        leftSplitPane = new FlatSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitPane.setDividerSize(0);
        //configureSplitPane(leftSplitPane);
        
        rightSplitPane = new FlatSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplitPane.setResizeWeight(1.0);
        rightSplitPane.setDividerSize(0);
        //configureSplitPane(rightSplitPane);

        leftSplitPane.setBorder(null);
        rightSplitPane.setBorder(null);

//        leftSplitPane.setBorder(BorderFactory.createEmptyBorder(1,3,0,0));
//        rightSplitPane.setBorder(BorderFactory.createEmptyBorder(1,0,0,1));
        
        // add the left main pane to the right main pane
        rightSplitPane.setLeftComponent(leftSplitPane);

        add(rightSplitPane, BorderLayout.CENTER);
        
        leftSplitPane.addPropertyChangeListener(this);
        rightSplitPane.addPropertyChangeListener(this);
    }
    
    /**
     * Provides notification of split pane divider movement events.
     *
     * @param the change event
     */
    public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if ("dividerLocation".equals(name)) {
                int position = -1;
                String value = e.getNewValue().toString();
                Object source = e.getSource();
                if (source == leftSplitPane) {
                    if (leftSplitPane.getLeftComponent() == null) {
                        return;
                    }
                    position = SwingConstants.LEFT;
                } 
                else if (source == rightSplitPane) {
                    if (rightSplitPane.getRightComponent() == null) {
                        return;
                    }
                    position = SwingConstants.RIGHT;
                }
                desktopMediator.splitPaneDividerMoved(position, Integer.parseInt(value));
/*
            Log.debug("property change: " + e.getPropertyName() +
            " old value: " + e.getOldValue() + " new value: " + e.getNewValue());
*/

            }
        
    }

    /**
     * Sets the split pane divider location for the split pane
     * at the specified location <code>SwingConstants.LEFT | 
     * SwingConstants.RIGHT</code> to the specified value.
     *
     * @param the split pane location
     * @param the divider location
     */
    public void setSplitPaneDividerLocation(int position, int location) {
        if (position == SwingConstants.LEFT) {
            if (leftSplitPane != null) {
                if (leftSplitPane.getLeftComponent() != null &&
                        leftSplitPane.getRightComponent() != null) {                
                    leftSplitPane.setDividerLocation(location);
                }
            }
        }
        else if (position == SwingConstants.RIGHT) {
            if (rightSplitPane != null) {
                if (rightSplitPane.getLeftComponent() != null &&
                        rightSplitPane.getRightComponent() != null) {                
                    rightSplitPane.setDividerLocation(location);
                }
            }
        }
    }
    
    /**
     * Notifies all registered listeners of a tab minimised event.
     *
     * @param the event 
     */
    public void fireTabMinimised(DockedTabEvent e) {
        desktopMediator.fireTabMinimised(e);
    }

    /**
     * Notifies all registered listeners of a tab selected event.
     *
     * @param the event 
     */
    public void fireTabSelected(DockedTabEvent e) {
        desktopMediator.fireTabSelected(e);
    }

    /**
     * Notifies all registered listeners of a tab deselected event.
     *
     * @param the event 
     */
    public void fireTabDeselected(DockedTabEvent e) {
        desktopMediator.fireTabDeselected(e);
    }
    
    /**
     * Notifies all registered listeners of a tab closed event.
     *
     * @param the event 
     */
    public void fireTabClosed(DockedTabEvent e) {
        desktopMediator.fireTabClosed(e);
    }

    /**
     * Common split pane configuration routines.
     */
    private void configureSplitPane(JSplitPane splitPane) {
        splitPane.setDividerSize(ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE);
    }

    /**
     * Resets the split pane component in the specified 
     * position to the preferred sizes of the split pane 
     * children components.
     *
     * @param the position of the pane
     */
    public void resetPaneToPreferredSizes(int position, boolean restore) {
        switch (position) {
            case SwingConstants.WEST:
                if (!restore) {
                    leftSplitPane.storeDividerLocation();
                    leftSplitPane.setDividerLocation(
                            leftSplitPane.getMinimumDividerLocation());
                } else {
                    leftSplitPane.restoreDividerLocation();
                }
                break;
            case SwingConstants.EAST:
                if (!restore) {
                    rightSplitPane.storeDividerLocation();
                    rightSplitPane.setDividerLocation(
                            rightSplitPane.getMaximumDividerLocation());
                } else {
                    rightSplitPane.restoreDividerLocation();
                }
                break;
        }        
    }

    /** 
     * Indicates whether the split pane at the specified 
     * position (left or right) is visible.
     * This will always return true for a center position.
     *
     * @param SwingConstants.LEFT | SwingConstants.RIGHT
     * @return true if visible, false otherwise
     */
    public boolean isSplitPaneVisible(int position) {
        switch (position) {
            case SwingConstants.WEST:
                return leftSplitPane.getLeftComponent() != null;
            case SwingConstants.EAST:
                return rightSplitPane.getRightComponent() != null;
        }
        return true;
    }

    public void removeComponent(int position) {
        switch (position) {
            case SwingConstants.WEST:
                leftSplitPane.storeDividerLocation();
                leftSplitPane.setLeftComponent(null);
                leftSplitPane.setDividerSize(0);
                break;
            case SwingConstants.CENTER:
                leftSplitPane.setRightComponent(null);
                leftSplitPane.setDividerSize(0);
                break;
            case SwingConstants.EAST:
                rightSplitPane.storeDividerLocation();
                rightSplitPane.setRightComponent(null);
                rightSplitPane.setDividerSize(0);
                break;
        }
    }

    /**
     * Adds the specified component in the specified position.
     *
     * @param the component to add
     * @param the position this component is to be added<br>
     *        one of: <code>SwingConstants.WEST | CENTER | EAST</code>
     */
    public void addComponent(Component c, int position) {
        switch (position) {
            case SwingConstants.WEST:
                leftSplitPane.setLeftComponent(c);
                configureSplitPane(leftSplitPane);
                break;
            case SwingConstants.CENTER:
                leftSplitPane.setRightComponent(c);
                configureSplitPane(leftSplitPane);
                break;
            case SwingConstants.EAST:
                rightSplitPane.setRightComponent(c);
                configureSplitPane(rightSplitPane);
                rightSplitPane.setDividerLocation(0.8);
                break;
            /*
            case SwingConstants.WEST:
                if (leftSplitPane == null) {
                    leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                }
                
                // check if we have a component at this position
                // and move al if required
                if (leftSplitPane.getLeftComponent() != null) {
                    
                }
                
                leftSplitPane.setLeftComponent(c);
                break;
            case SwingConstants.CENTER:
                if (leftSplitPane == null) {
                    leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                }
                leftSplitPane.setRightComponent(c);
                break;
            case SwingConstants.EAST:
                if (rightSplitPane == null) {
                    rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                }
                leftSplitPane.setRightComponent(c);
                break;
             */
                
        }        
    }

    /**
     *  Invoked when a mouse button has been released on a tab.
     *
     * @param the encapsulating event object
     */
    public void dockedTabReleased(DockedDragEvent e) {
        try {
            if (dragPanel != null) {
                desktopMediator.removeDragPanel(dragPanel);
                repaint();

                int x = e.getX();
                int y = e.getY();
                
                if (fromTabPane == null) {
                    fromTabPane = e.getSourceTabPane();
                }
                int fromIndex = fromTabPane.getDraggingIndex();

                // check if we are moving to an area with no tab pane
                if (newTabPanePosition != -1) {
                    TabComponent tabComponent = fromTabPane.getTabComponentAt(fromIndex);
                    if (tabComponent == null) {
                        return;
                    }
                    fromTabPane.removeIndex(fromIndex);
                    tabComponent.setPosition(newTabPanePosition);
                    desktopMediator.addDockedTab(tabComponent, newTabPanePosition, true);
                    return;
                }
                
                // move tab if required
                if (fromTabPane != null && toTabPane != null) {
                    if (fromTabPane == toTabPane) {
                        int toIndex = fromTabPane.getTabAtLocation(x, y);
                        fromTabPane.moveTab(fromIndex, toIndex);
                    }
                    else {
                        Rectangle tabRect = dragPanel.getTabRectangle();
                        int toIndex = toTabPane.getTabRectangleIndex(tabRect);

                        TabComponent tabComponent = fromTabPane.getTabComponentAt(fromIndex);
                        if (tabComponent == null) {
                            return;
                        }

                        fromTabPane.removeIndex(fromIndex);
                        tabComponent.setPosition(toTabPane.getTabPanePosition());
                        tabComponent.setIndex(toIndex);
                        toTabPane.insertTab(tabComponent, toIndex);
                    }
                }
            }
        }
        // make sure we clean up and reset the cursor
        finally {
            dragPanel = null;
            fromTabPane = null;
            toTabPane = null;
            newTabPanePosition = -1;
            desktopMediator.setFrameCursor(
                    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    /**
     * Invoked when a mouse button is pressed on a tab and then dragged.
     *
     * @param the encapsulating event object
     */
    public void dockedTabDragged(DockedDragEvent e) {
        desktopMediator.setFrameCursor(
                Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        int x = e.getX();
        int y = e.getY();

        // convert the point for this base component - 
        // starting search from the bottom up
        Point point = SwingUtilities.convertPoint(e.getSourceTabPane(), x, y, this);
        x = point.x;
        y = point.y;
        
        int height = getHeight();
        int width = getWidth();
        
        double placementFactor = 0.2;
        int heightRange = (int)(height * placementFactor);
        int widthRange =  (int)(width * placementFactor);
        
        int leftPaneWidth = desktopMediator.getPaneWidth(SwingConstants.WEST);
        int rightPaneWidth = desktopMediator.getPaneWidth(SwingConstants.EAST);
        int centerPaneWidth = desktopMediator.getPaneWidth(SwingConstants.CENTER);
        
        //Log.debug("west: "+desktopMediator.isPaneVisible(SwingConstants.WEST));
        
        // check left/right extremes where there may no tab and split panes
        if (x < widthRange) { // left side
            //Log.debug("AA");
            if (!desktopMediator.isPaneVisible(SwingConstants.WEST)) {
                //Log.debug("BB");
                Rectangle tabPaneRect = new Rectangle(0, 
                                                      0,
                                                      widthRange,
                                                      height);

                // add the y offset for this panel
                tabPaneRect.y += getYOffset();

                if (dragPanel == null) {
                    dragPanel = new DragPanel(tabPaneRect);
                    desktopMediator.addDragPanel(dragPanel);
                }
                else {
                    dragPanel.reset(tabPaneRect);
                }
                newTabPanePosition = SwingConstants.NORTH_WEST;
                return;
            }
        }
        else if (x > (width - widthRange)) { // right side
            //Log.debug("CC");
            if (!desktopMediator.isPaneVisible(SwingConstants.EAST)) {
                //Log.debug("DD");
                Rectangle tabPaneRect = new Rectangle(width - widthRange, 
                                                      0,
                                                      widthRange,
                                                      height);

                // add the y offset for this panel
                tabPaneRect.y += getYOffset();

                if (dragPanel == null) {
                    dragPanel = new DragPanel(tabPaneRect);
                    desktopMediator.addDragPanel(dragPanel);
                }
                else {
                    dragPanel.reset(tabPaneRect);
                }
                newTabPanePosition = SwingConstants.NORTH_EAST;
                return;
            }
        }

        // bottom region
        if (y > (height - heightRange)) {
            //Log.debug("ZZ");
            // bottom-left region
            if (x < leftPaneWidth && 
                    !desktopMediator.hasDockedComponentAtPosition(SwingConstants.SOUTH_WEST)) {
                if (leftSplitPane == null || leftSplitPane.getLeftComponent() == null) return;
                Rectangle tabPaneRect = 
                        new Rectangle(leftSplitPane.getLeftComponent().getX(),
                                      height - heightRange - ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE,
                                      leftPaneWidth,
                                      heightRange);

                // add the y offset for this panel
                tabPaneRect.y += getYOffset();

                if (dragPanel == null) {
                    dragPanel = new DragPanel(tabPaneRect);
                    desktopMediator.addDragPanel(dragPanel);
                }
                else {
                    dragPanel.reset(tabPaneRect);
                }
                newTabPanePosition = SwingConstants.SOUTH_WEST;
                return;
            }
            // bottom-right region
            else if (x > (width - rightPaneWidth) && 
                        !desktopMediator.hasDockedComponentAtPosition(SwingConstants.SOUTH_EAST)) {
                //Log.debug("YY");
                Rectangle tabPaneRect = 
                        new Rectangle(rightSplitPane.getRightComponent().getX(),
                                      height - heightRange - ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE,
                                      rightPaneWidth - ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE,
                                      heightRange);

                // add the y offset for this panel
                tabPaneRect.y += getYOffset();

                if (dragPanel == null) {
                    dragPanel = new DragPanel(tabPaneRect);
                    desktopMediator.addDragPanel(dragPanel);
                }
                else {
                    dragPanel.reset(tabPaneRect);
                }
                newTabPanePosition = SwingConstants.SOUTH_EAST;
                return;
            }
            // bottom-center region
            else if (x > leftPaneWidth && x < (width - rightPaneWidth) && 
                        !desktopMediator.hasDockedComponentAtPosition(SwingConstants.SOUTH)) {
                //Log.debug("XX");
                Rectangle tabPaneRect = 
                        new Rectangle(
                            leftPaneWidth + 
                                ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE + 
                                ApplicationConstants.TAB_COMPONENT_BORDER_THICKNESS, 
                            height - heightRange,
                            centerPaneWidth,
                            heightRange - ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE);

                // add the y offset for this panel
                tabPaneRect.y += getYOffset();
                
                if (dragPanel == null) {
                    dragPanel = new DragPanel(tabPaneRect);
                    desktopMediator.addDragPanel(dragPanel);
                }
                else {
                    dragPanel.reset(tabPaneRect);
                }
                newTabPanePosition = SwingConstants.SOUTH;
                return;
            }
            
        }
        
        //Log.debug("XXXXXXXXXX");
        
        // ------------------------------------------------------------
        // otherwise we must be in the region with existing tab panes
        
        newTabPanePosition = -1;
        DockedTabPane tabPane = null;
        Component component = null;
        Component lastComponent = this;

        //Log.debug("BEFORE - x: " + x + " y: " + y);
        
        // need to loop through the split pane layers
        // until the end or when we locate a tab pane
        
        while ((component = lastComponent.getComponentAt(x, y)) != null) {

            if (component == lastComponent) { // short-circuit
                //Log.debug("component: " + component.getClass().getName());
                
                if (component instanceof DockedTabPane) {
                    tabPane = (DockedTabPane)component;
                    break;
                }
                //Log.debug("KK");
                return;
            }

            point = SwingUtilities.convertPoint(lastComponent, x, y, component);
            x = point.x;
            y = point.y;

            // check if we're over a glass pane on the tab pane
            if (component instanceof BaseRootPane.GlassPanePanel) {
                component = ((BaseRootPane.GlassPanePanel)component).getComponentBelow();
            }

            if (component instanceof DockedTabPane) {
                tabPane = (DockedTabPane)component;
                break;
            }
            lastComponent = component;
        }

        // return if we have nothing
        if (tabPane == null) {
            //Log.debug("LL");
            return;
        }
        
        //Log.debug("AFTER - x: " + x + " y: " + y);
        
        if (tabPane.intersectsTabArea(x, y)) {
            Rectangle tabRect = tabPane.getTabRectangleAtLocation(x, y);
            if (tabRect == null) {
                return;
            }

            // recalculate based on the position of the tab pane
            point = SwingUtilities.convertPoint(
                        tabPane, tabPane.getLocation(), this);

            //Log.debug("point calced: "+point);
            
            Rectangle tabPaneRect = new Rectangle(tabPane.getBounds());

            // reset the origin
            tabPaneRect.x = point.x;// - ApplicationConstants.TAB_COMPONENT_BORDER_THICKNESS;
            tabPaneRect.y = point.y;
            
            // add the y offset for this panel
            tabPaneRect.y += getYOffset();// + tabRect.height;
            
            //Log.debug("setting rect: " + tabPaneRect);
            
            // ----------
            // this works but need to do same where no tab pane above
            // tabPaneRect.y += getYOffset() + tabRect.height;
            // ----------
            
            if (dragPanel == null) {
                dragPanel = new DragPanel(tabRect, tabPaneRect);
                desktopMediator.addDragPanel(dragPanel);
            } 
            else {
                dragPanel.reset(tabRect, tabPaneRect);
            }

            //Log.debug("bounds: " + dragPanel.getBounds());
            
        }
        else {
            if (dragPanel != null) {
                desktopMediator.removeDragPanel(dragPanel);
                dragPanel = null;
                repaint();
            }
        }
        fromTabPane = e.getSourceTabPane();
        toTabPane = tabPane;
    }    

    private int getYOffset() {
        //Log.debug("base location " + getLocation());

        //Point offsetPoint = SwingUtilities.convertPoint(
        //                            this, getLocation(), desktopMediator.getFrame().getContentPane());
        
        //Log.debug("offset calc: " + offsetPoint.y);
        return getLocation().y + 20;//offsetPoint.y;// + 17;
        //return getBounds().y + 34; //17;
    }
    
}















