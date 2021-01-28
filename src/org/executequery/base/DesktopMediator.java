/*
 * DesktopMediator.java
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

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DesktopMediator implements DockedTabDragListener {

    // ----------------------------------
    // property names for divider locations

    // left-hand left-right split
    public static final String LEFT_DIVIDER_LOCATION = "divider.location.left";

    // right-hand left-right split
    public static final String RIGHT_DIVIDER_LOCATION = "divider.location.right";

    // left-hand top-bottom split
    public static final String WEST_DIVIDER_LOCATION = "divider.location.west";

    // center top-bottom split
    public static final String CENTER_DIVIDER_LOCATION = "divider.location.center";

    // right-hand top-bottom split
    public static final String EAST_DIVIDER_LOCATION = "divider.location.east";

    public static final String[] DIVIDER_LOCATION_KEYS = {
            LEFT_DIVIDER_LOCATION,
            RIGHT_DIVIDER_LOCATION,
            WEST_DIVIDER_LOCATION,
            CENTER_DIVIDER_LOCATION,
            EAST_DIVIDER_LOCATION};


    // ----------------------------------

    /**
     * the application frame
     */
    private JFrame frame;

    /**
     * the application base panel
     */
    private BaseApplicationPane baseWindowPane;

    /**
     * tab pane event listeners
     */
    private List<DockedTabListener> tabListeners;

    /**
     * tab pane drag event listeners
     */
    private List<DockedTabDragListener> tabDragListeners;

    /**
     * tab pane event listeners
     */
    private List<PropertyChangeListener> propertyListeners;

    // -------------------------------------------
    // the application tab pane containers
    // -------------------------------------------

    /**
     * the container at the WEST position
     */
    private DockedTabContainer westContainer;

    /**
     * the container at the CENTER position
     */
    private DockedTabContainer centerContainer;

    /**
     * the container at the EAST position
     */
    private DockedTabContainer eastContainer;

    // -------------------------------------------

    /**
     * prevent instantiation
     */
    public DesktopMediator(JFrame frame) {
        this.frame = frame;
        baseWindowPane = new BaseApplicationPane(this);
        /*
        JPanel p = new JPanel();
        p.setBackground(Color.RED);
        frame.add(p, BorderLayout.CENTER);
         */
        baseWindowPane.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

        //BaseRootPane rootPane = new BaseRootPane(baseWindowPane);
        //frame.add(rootPane, BorderLayout.CENTER);
        frame.add(baseWindowPane, BorderLayout.CENTER);
    }

    /**
     * Removes the specified docked tab listener.
     *
     * @param the listener
     */
    public void removeDockedTabListener(DockedTabListener listener) {
        if (tabListeners == null) {
            return;
        }
        tabListeners.remove(listener);
    }

    /**
     * Adds the specified docked tab listener.
     *
     * @param the listener
     */
    public void addDockedTabListener(DockedTabListener listener) {
        if (tabListeners == null) {
            tabListeners = new ArrayList<DockedTabListener>();
        }
        tabListeners.add(listener);
    }

    /**
     * Adds the specified docked tab listener.
     *
     * @param the listener
     */
    public void addDockedTabDragListener(DockedTabDragListener listener) {
        if (tabDragListeners == null) {
            tabDragListeners = new ArrayList<DockedTabDragListener>();
        }
        tabDragListeners.add(listener);
    }

    /**
     * Notifies all registered listeners of a tab minimised event.
     *
     * @param the event
     */
    public void fireTabMinimised(DockedTabEvent e) {
        if (tabListeners == null || tabListeners.size() == 0) {
            return;
        }
        for (int i = 0, k = tabListeners.size(); i < k; i++) {
            tabListeners.get(i).tabMinimised(e);
        }
    }

    /**
     * Notifies all registered listeners of a tab selected event.
     *
     * @param the event
     */
    public void fireTabSelected(DockedTabEvent e) {
        if (tabListeners == null || tabListeners.isEmpty()) {
            return;
        }
        for (int i = 0, k = tabListeners.size(); i < k; i++) {
            tabListeners.get(i).tabSelected(e);
        }
    }

    /**
     * Notifies all registered listeners of a tab deselected event.
     *
     * @param the event
     */
    public void fireTabDeselected(DockedTabEvent e) {
        if (tabListeners == null || tabListeners.isEmpty()) {
            return;
        }
        for (int i = 0, k = tabListeners.size(); i < k; i++) {
            tabListeners.get(i).tabSelected(e);
        }
    }

    /**
     * Notifies all registered listeners of a tab restored event.
     *
     * @param the event
     */
    protected void fireTabRestored(DockedTabEvent e) {
        if (tabListeners == null || tabListeners.isEmpty()) {
            return;
        }
        for (int i = 0, k = tabListeners.size(); i < k; i++) {
            tabListeners.get(i).tabRestored(e);
        }
    }

    /**
     * Notifies all registered listeners of a tab closed event.
     *
     * @param the event
     */
    public void fireTabClosed(DockedTabEvent e) {
        if (tabListeners == null || tabListeners.isEmpty()) {
            return;
        }
        for (int i = 0, k = tabListeners.size(); i < k; i++) {
            tabListeners.get(i).tabClosed(e);
        }
    }

    /**
     * Invoked when a mouse button is pressed on a tab and then dragged.
     *
     * @param the encapsulating event object
     */
    public void fireDockedTabDragged(DockedDragEvent e) {
        if (tabDragListeners == null || tabDragListeners.size() == 0) {
            return;
        }
        for (int i = 0, k = tabDragListeners.size(); i < k; i++) {
            tabDragListeners.get(i).dockedTabDragged(e);
        }
    }

    /**
     * Invoked when a mouse button has been released on a tab.
     *
     * @param the encapsulating event object
     */
    public void fireDockedTabReleased(DockedDragEvent e) {
        if (tabDragListeners == null || tabDragListeners.size() == 0) {
            return;
        }
        for (int i = 0, k = tabDragListeners.size(); i < k; i++) {
            tabDragListeners.get(i).dockedTabReleased(e);
        }
    }

    public ActionMap getActionMap() {
        return baseWindowPane.getActionMap();
    }

    /**
     * Retrieves the applications <code>InputMap</code>.
     *
     * @return the <code>InputMap</code>
     */
    public InputMap getInputMap(int condition) {
        return baseWindowPane.getInputMap(condition);
    }

    /**
     * Sets the frame's cursor to that specified.
     *
     * @param the cursor to be set on the frame
     */
    public void setFrameCursor(Cursor cursor) {
        frame.setCursor(cursor);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (propertyListeners == null) {
            propertyListeners = new ArrayList<PropertyChangeListener>();
        }
        propertyListeners.add(l);
    }

    public void firePropertyChange(String name, int oldValue, int newValue) {
        if (propertyListeners == null || propertyListeners.isEmpty()) {
            return;
        }
        PropertyChangeEvent e = new PropertyChangeEvent(
                this, name, Integer.valueOf(oldValue), Integer.valueOf(newValue));
        for (int i = 0, k = propertyListeners.size(); i < k; i++) {
            propertyListeners.get(i).propertyChange(e);
        }
    }

    /**
     * Sets the split pane divider location for the split pane
     * with the specified key name to the specified value.
     *
     * @param the split pane location
     * @param the divider location
     */
    public void setSplitPaneDividerLocation(String key, int location) {
        if (location <= 0) {
            return;
        }

        int position = -1;
        if (key.equals(LEFT_DIVIDER_LOCATION)) {
            position = SwingConstants.LEFT;
        } else if (key.equals(RIGHT_DIVIDER_LOCATION)) {
            position = SwingConstants.RIGHT;
        } else if (key.equals(WEST_DIVIDER_LOCATION)) {
            position = SwingConstants.WEST;
        } else if (key.equals(CENTER_DIVIDER_LOCATION)) {
            position = SwingConstants.CENTER;
        } else if (key.equals(EAST_DIVIDER_LOCATION)) {
            position = SwingConstants.EAST;
        }

        if (position != -1) {
            setSplitPaneDividerLocation(position, location);
        }

    }

    /**
     * Indicates a focus change on the tab components and their containers.
     *
     * @param tabPane - the new focused tab container
     */
    protected void tabPaneFocusChange(DockedTabContainer tabContainer) {
        DockedTabContainer[] dtca = {westContainer, centerContainer, eastContainer};
        for (int i = 0; i < dtca.length; i++) {
            DockedTabContainer dtc = dtca[i];
            if (dtc != null && dtc != tabContainer) {
                dtc.tabPaneFocusLost();
            }
        }
    }

    /**
     * Sets the split pane divider location for the split pane
     * at the specified location to the specified value.
     *
     * @param the split pane location
     * @param the divider location
     */
    public void setSplitPaneDividerLocation(int position, int location) {
        DockedTabContainer tabContainer = null;
        switch (position) {
            case SwingConstants.LEFT:
            case SwingConstants.RIGHT:
                baseWindowPane.setSplitPaneDividerLocation(position, location);
                return;
            case SwingConstants.WEST:
                if (westContainer != null) {
                    tabContainer = westContainer;
                }
                //tabContainer = initTabContainer(SwingConstants.WEST);
                break;

            case SwingConstants.CENTER:
                if (centerContainer != null) {
                    tabContainer = centerContainer;
                }
                //tabContainer = initTabContainer(SwingConstants.CENTER);
                break;

            case SwingConstants.EAST:
                if (eastContainer != null) {
                    tabContainer = eastContainer;//initTabContainer(SwingConstants.EAST);
                }
                break;
        }

        if (tabContainer != null) {
            tabContainer.setSplitPaneDividerLocation(location);
        }
    }

    public void splitPaneDividerMoved(int position, int newValue) {

        switch (position) {
            case SwingConstants.LEFT:
                firePropertyChange(LEFT_DIVIDER_LOCATION, -1, newValue);
                break;
            case SwingConstants.RIGHT:
                firePropertyChange(RIGHT_DIVIDER_LOCATION, -1, newValue);
                break;

            case SwingConstants.WEST:
                firePropertyChange(WEST_DIVIDER_LOCATION, -1, newValue);
                break;

            case SwingConstants.CENTER:
                firePropertyChange(CENTER_DIVIDER_LOCATION, -1, newValue);
                break;

            case SwingConstants.EAST:
                firePropertyChange(EAST_DIVIDER_LOCATION, -1, newValue);
                break;

        }
    }

    /**
     * Adds the specified drag panel to the frame's
     * layered pane.
     *
     * @param the drag panel
     */
    public void addDragPanel(DragPanel dragPanel) {
        frame.getLayeredPane().add(dragPanel, JLayeredPane.DRAG_LAYER);
    }

    /**
     * Removes the specified drag panel from the frame's
     * layered pane.
     *
     * @param the drag panel to be removed
     */
    public void removeDragPanel(DragPanel dragPanel) {
        frame.getLayeredPane().remove(dragPanel);
        frame.getLayeredPane().repaint();
    }

    /**
     * Adds the specified component as a docked tab component
     * in the specified position.
     *
     * @param the tab title
     * @param the component
     * @param the position
     */
    public void addDockedTab(String title,
                             Component component,
                             int position,
                             boolean selected) {
        addDockedTab(title, null, component, null, position, selected);
    }

    /**
     * Adds the specified component as a docked tab component
     * in the specified position.
     *
     * @param the tab title
     * @param the tab icon
     * @param the component
     * @param the position
     */
    public void addDockedTab(String title,
                             Icon icon,
                             Component component,
                             int position,
                             boolean selected) {
        addDockedTab(title, icon, component, null, position, selected);
    }

    /**
     * Adds the specified component as a docked tab component
     * in the specified position.
     *
     * @param the tab title
     * @param the tab icon
     * @param the component
     * @param the position
     */
    public void addDockedTab(TabComponent tabComponent,
                             int position,
                             boolean selected) {
        addDockedTab(tabComponent.getTitle(),
                tabComponent.getIcon(),
                tabComponent.getComponent(),
                tabComponent.getToolTip(),
                position,
                selected);
    }

    /**
     * Adds the specified component as a docked tab component in the
     * specified position.
     *
     * @param the tab title
     * @param the tab icon
     * @param the component
     * @param the tab's tool tip
     * @param the position
     */
    public void addDockedTab(String title,
                             Icon icon,
                             Component component,
                             String tip,
                             int position,
                             boolean selected) {

        DockedTabContainer tabContainer = null;

        switch (position) {

            case SwingConstants.NORTH_WEST:
                tabContainer = initTabContainer(SwingConstants.WEST);
                break;

            case SwingConstants.SOUTH_WEST:
                tabContainer = initTabContainer(SwingConstants.WEST);
                // if there is nothing in the north pane, add there
                if (!tabContainer.isTabPaneVisible(SwingConstants.NORTH)) {
                    position = SwingConstants.NORTH;
                }
                break;

            case SwingConstants.CENTER:
            case SwingConstants.SOUTH:
                tabContainer = initTabContainer(SwingConstants.CENTER);
                break;

            case SwingConstants.NORTH_EAST:
                tabContainer = initTabContainer(SwingConstants.EAST);
                break;

            case SwingConstants.SOUTH_EAST:
                tabContainer = initTabContainer(SwingConstants.EAST);
                // if there is nothing in the north pane, add there
                if (!tabContainer.isTabPaneVisible(SwingConstants.NORTH)) {
                    position = SwingConstants.NORTH;
                }
                break;

        }

        if (tabContainer != null) {

            tabContainer.addDockedTab(title, icon, component, tip, position);

            if (selected) {

                int tabCount = tabContainer.getTabCount(position);
                tabContainer.setSelectedIndex(position, tabCount - 1);
            }

        }

    }

    /**
     * Returns the open tab components in a list at the specified position.
     *
     * @param the tab pane position
     * @return a list of tab components
     */
    public List<TabComponent> getOpenTabs(int position) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            return container.getOpenTabs(position);
        }
        return null;
    }

    /**
     * Sets the tool tip for the specified component to toolTipText
     * which can be null. An internal exception is raised if there
     * is no tab for the specified component.
     *
     * @param the tab pane position
     * @param the component where the tool tip should be set
     * @param the tool tip text to be displayed in the tab
     */
    public void setToolTipTextForComponent(int position,
                                           Component component, String toolTipText) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            container.setToolTipTextForComponent(position, component, toolTipText);
        }
    }

    /**
     * Selects the next tab from the current selection.
     */
    public void selectNextTabContainer() {

        DockedTabContainer nextContainer = null;
        DockedTabContainer focusedContainer = getTabContainerInFocus();

        if (focusedContainer == westContainer) {

            nextContainer = centerContainer;

        } else if (focusedContainer == centerContainer) {

            if (eastContainer != null) {

                nextContainer = eastContainer;

            } else {

                nextContainer = westContainer;
            }

        } else if (focusedContainer == eastContainer) {

            nextContainer = westContainer;
        }

        if (nextContainer != null) {

            nextContainer.getSelectedTabPane().focusGained();
        }

    }

    private DockedTabContainer getTabContainerInFocus() {

        if (westContainer != null && westContainer.hasFocusedTabPane()) {

            return westContainer;

        } else if (centerContainer != null && centerContainer.hasFocusedTabPane()) {

            return centerContainer;

        } else if (eastContainer != null && eastContainer.hasFocusedTabPane()) {

            return eastContainer;
        }

        return null;
    }


    /**
     * Selects the next tab from the current selection.
     */
    public void selectNextTab() {

        DockedTabContainer container = getTabContainerInFocus();

        if (container != null) {

            TabPane tabPane = container.getTabPaneInFocus();
            tabPane.selectNextTab();
        }

    }

    /**
     * Selects the previous tab from the current selection.
     */
    public void selectPreviousTab() {

        DockedTabContainer container = getTabContainerInFocus();

        if (container != null) {

            TabPane tabPane = container.getTabPaneInFocus();
            tabPane.selectPreviousTab();
        }

    }

    /**
     * Sets the title of the specified component to title which can be null.
     * An internal exception is raised if there is no tab for the
     * specified component.
     *
     * @param the tab pane position
     * @param the component where the title should be set
     * @param the title to be displayed in the tab
     */
    public void setTabTitleForComponent(int position,
                                        Component component, String title) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            container.setTabTitleForComponent(position, component, title);
        }
    }

    /**
     * Returns the open tab count at the specified position.
     *
     * @param the tab pane position
     * @return the tab count
     */
    public int getTabCount(int position) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            return container.getTabCount(position);
        }
        return 0;
    }

    /**
     * Returns the selected tab component at the specified position.
     *
     * @param the position
     * @return the component at position
     */
    public TabComponent getSelectedComponent(int position) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            return container.getComponentAt(position);
        }
        return null;
    }

    /**
     * Closed the specfied tab component with name at the specified position.
     *
     * @param the name of the tab component
     * @param the position
     */
    public void closeTabComponent(String name, int position) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            container.closeTabComponent(name, position);
        }
    }

    public void closeSelectedTab() {

        DockedTabContainer container = getTabContainerInFocus();
        container.getSelectedTabPane().removeSelectedTab();
    }

    public void closeAllTabs() {

        if (westContainer != null) {

            westContainer.removeAllTabsInAllContainers();
        }

        if (centerContainer != null) {

            centerContainer.removeAllTabsInAllContainers();

        }

        if (eastContainer != null) {

            eastContainer.removeAllTabsInAllContainers();
        }

    }

    public void closeAllTabsInSelectedContainer() {

        DockedTabContainer container = getTabContainerInFocus();
        container.closeAllTabsInSelectedContainer();
    }

    /**
     * Initialises a docked tab container in the specified
     * position.
     *
     * @param the position of this tab container
     */
    private DockedTabContainer initTabContainer(int position) {
        DockedTabContainer dockedTabContainer = null;
        switch (position) {
            case SwingConstants.WEST:
                if (westContainer == null) {
                    westContainer = new DockedTabContainer(this, position);
                    dockedTabContainer = westContainer;
                    break;
                }
                return westContainer;
            case SwingConstants.CENTER:
                if (centerContainer == null) {
                    centerContainer = new DockedTabContainer(this, position);
                    dockedTabContainer = centerContainer;
                    break;
                }
                return centerContainer;
            case SwingConstants.EAST:
                if (eastContainer == null) {
                    eastContainer = new DockedTabContainer(this, position);
                    dockedTabContainer = eastContainer;
                    break;
                }
                return eastContainer;
        }
        // add to the base pane and attach listeners
        if (dockedTabContainer != null) {
            dockedTabContainer.addDockedTabListener(this);
            baseWindowPane.addComponent(dockedTabContainer, position);
        }
        return dockedTabContainer;
    }

    /**
     * Indicates whether the pane at the specified
     * position (left or right) is visible.
     *
     * @param SwingConstants.LEFT | SwingConstants.RIGHT
     * @return true if visible, false otherwise
     */
    public boolean isPaneVisible(int position) {
        switch (position) {
            case SwingConstants.WEST:
                return westContainer != null &&
                        (!westContainer.isButtonPanelVisible() ||
                                westContainer.isTabPaneVisible(-1));
            case SwingConstants.CENTER:
                return centerContainer != null;
            case SwingConstants.EAST:
                return eastContainer != null &&
                        (!eastContainer.isButtonPanelVisible() ||
                                eastContainer.isTabPaneVisible(-1));
        }
        return false;
    }

    /**
     * Indicates whether the pane at the specified
     * position (left or right) is visible.
     *
     * @param SwingConstants.LEFT | SwingConstants.RIGHT
     * @return true if visible, false otherwise
     */
    public boolean hasDockedComponentAtPosition(int position) {
        switch (position) {
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.WEST:
                if (isPaneVisible(SwingConstants.WEST)) {
                    return westContainer.isTabPaneVisible(position);
                }
            case SwingConstants.SOUTH:
                if (isPaneVisible(SwingConstants.CENTER)) {
                    return centerContainer.isTabPaneVisible(SwingConstants.SOUTH);
                }
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.EAST:
                if (isPaneVisible(SwingConstants.EAST)) {
                    return eastContainer.isTabPaneVisible(position);
                }
        }
        return false;
    }

    /**
     * Returns the width of the specified pane.
     *
     * @return the pane width
     */
    public int getPaneWidth(int position) {
        switch (position) {
            case SwingConstants.WEST:
                if (westContainer != null) {
                    return westContainer.getPaneWidth();
                }
            case SwingConstants.CENTER:
                if (centerContainer != null) {
                    return centerContainer.getPaneWidth();
                }
            case SwingConstants.EAST:
                if (eastContainer != null) {
                    return eastContainer.getPaneWidth();
                }
        }
        return -1;
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
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.CENTER:
            case SwingConstants.WEST:
                baseWindowPane.resetPaneToPreferredSizes(SwingConstants.WEST, restore);
                break;
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.EAST:
                baseWindowPane.resetPaneToPreferredSizes(SwingConstants.EAST, restore);
                break;
        }
    }

    /**
     * Indicates whether the pane at the specified 
     * position (left or right) is visible.
     *
     * @param SwingConstants.LEFT | SwingConstants.RIGHT
     * @return true if visible, false otherwise
     */
    /*
    public static boolean isSplitPaneVisible(int position) {
        boolean visible = frame.isSplitPaneVisible(position);
        if (visible && position != SwingConstants.CENTER) {
            switch (position) {
                case SwingConstants.LEFT:
                    // check we have only the top-left pane
                    if (bottomLeftTabPane == null) {
                        if (topLeftTabPane.isButtonPanelVisible() &&
                                topLeftTabPane.hasTabsVisible()) {
                            return true;
                        }
                    } 
                    break;
                case SwingConstants.RIGHT:
                    // check we have only the top-left pane
                    if (bottomRightTabPane == null) {
                        if (topRightTabPane.isButtonPanelVisible() &&
                                topRightTabPane.hasTabsVisible()) {
                            return true;
                        }
                    }
                    break;
            }
        } 
        return visible;
    } */

    /**
     * Removes the specified docked tab pane from the
     * parent split pane and application frame.
     *
     * @param the tab pane to remove
     */
    public void removeDockedContainer(DockedTabContainer tabContainer) {
        int position = tabContainer.getOrientation();
        baseWindowPane.removeComponent(position);
        switch (position) {
            case SwingConstants.WEST:
                westContainer = null;
                break;
            case SwingConstants.CENTER:
                centerContainer = null;
                break;
            case SwingConstants.EAST:
                eastContainer = null;
                break;
        }        

        
        /*
        if (tabPane == topLeftTabPane) {
            frame.setTopLeftComponent(null);
            topLeftTabPane = null;
            // move bottom components to top
            if (frame.hasBottomLeftComponent()) {
                frame.setBottomLeftComponent(null);
                frame.setTopLeftComponent(bottomLeftTabPane);
                topLeftTabPane = bottomLeftTabPane;
                bottomLeftTabPane = null;
            }
            else { // hide the split pane
                frame.setLeftSplitPaneVisible(false);
            }
        }
        else if (tabPane == bottomLeftTabPane) {
            frame.setBottomLeftComponent(null);
            bottomLeftTabPane = null;
        }
        else if (tabPane == bottomCenterTabPane) {
            frame.setBottomCenterComponent(null);
            bottomCenterTabPane = null;
        }
        else if (tabPane == topRightTabPane) {
            frame.setTopRightComponent(null);
            topRightTabPane = null;
            // move bottom components to top
            if (frame.hasBottomRightComponent()) {
                frame.setBottomRightComponent(null);
                frame.setTopRightComponent(bottomLeftTabPane);
                topRightTabPane = bottomRightTabPane;
                bottomRightTabPane = null;
            }
            else { // hide the split pane
                frame.setRightSplitPaneVisible(false);
            }            
        }
        else if (tabPane == bottomRightTabPane) {
            frame.setBottomRightComponent(null);
            bottomRightTabPane = null;
        } */

    }

    /**
     * Restores the tab component with the specified name.
     *
     * @param position - the component position in the pane
     * @param name     - the component's name (tab title)
     */
    public void restore(int position, String name) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            container.restore(name);
        }
    }

    /**
     * Returns if the tab at the specified position with the
     * specfied name is currently minimised.
     *
     * @param position - the component position in the pane
     * @param name     - the component's name (tab title)
     */
    public boolean isMinimised(int position, String name) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            return container.isMinimised(name);
        }
        return false;
    }

    /**
     * Minimises the tab component with the specified name in
     * the specified position.
     *
     * @param position - the component position in the pane
     * @param name     - the component's name (tab title)
     */
    public void minimiseDockedTab(int position, String name) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            TabComponent tab = container.getTabComponent(position, name);
            if (tab != null) {
                container.minimiseComponent(position, tab.getIndex());
            }
        }
    }

    protected DockedTabContainer getContainerAt(int position) {
        switch (position) {
            case SwingConstants.NORTH_WEST:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.WEST:
                return westContainer;
            case SwingConstants.CENTER:
            case SwingConstants.SOUTH:
                return centerContainer;
            case SwingConstants.NORTH_EAST:
            case SwingConstants.SOUTH_EAST:
            case SwingConstants.EAST:
                return eastContainer;
        }
        return null;
    }

    /**
     * Sets the selected panel at the specified index for the tab pane
     * at the specified position.
     *
     * @param position - the tab pane position
     * @param index    - the index to be selected
     */
    public void setSelectedPane(int position, int index) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            container.setSelectedIndex(position, index);
        }
    }

    /**
     * Sets the selected panel with the specified title for the tab pane
     * at the specified position.
     *
     * @param position - the tab pane position
     * @param name     - the name/title of the tab to select
     */
    public void setSelectedPane(int position, String name) {

        DockedTabContainer container = getContainerAt(position);

        // check we are not already selected
        // if so - verify focus and return
        TabComponent tabComponent = getSelectedComponent(position);
        if (tabComponent != null && name.equals(tabComponent.getTitle())) {
            /*
            final TabPane tabPane = container.getTabPaneForPosition(position);
            if (tabPane != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        tabPane.focusGained();
                    }
                });
            }
             */
            return;
        }

        if (container != null) {
            container.setSelectedPane(position, name);
        }
    }

    /**
     * Returns the tab component (or null) at the specified position
     * with the specified name.
     *
     * @param position - the position of the component
     * @param name     - the name of the component (tab title)
     */
    public TabComponent getTabComponent(int position, String name) {
        DockedTabContainer container = getContainerAt(position);
        if (container != null) {
            return container.getTabComponent(position, name);
        }
        return null;
    }

    /**
     * Tab drag event listener implementation.
     *
     * @param the tab drag event
     */
    public void dockedTabDragged(DockedDragEvent e) {
        //baseWindowPane.dockedTabDragged(e);
    }

    /**
     * Tab drag event listener implementation.
     *
     * @param the tab drag event
     */
    public void dockedTabReleased(DockedDragEvent e) {
        //baseWindowPane.dockedTabReleased(e);
    }

    public void registerBaseWindowPane(BaseApplicationPane _baseWindowPane) {
        baseWindowPane = _baseWindowPane;
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame aFrame) {
        frame = aFrame;
    }

}















