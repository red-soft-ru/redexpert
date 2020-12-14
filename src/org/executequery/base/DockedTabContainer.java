/*
 * DockedTabContainer.java
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

import org.apache.commons.lang.StringUtils;
import org.underworldlabs.swing.VerticalTextIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The base component for a docked tab panel.
 * This will control the docked tab in addition to
 * provide minimised button panels, action controls
 * and tool tip creation.
 *
 * @author Takis Diakoumis
 */
public class DockedTabContainer extends JPanel
        implements SwingConstants,
        PropertyChangeListener {

    /**
     * the container's position
     */
    private int orientation;

    /**
     * The mediator/controller class
     */
    private DesktopMediator desktopMediator;

    /**
     * the north docked tab pane
     */
    private DockedTabPane northTabPane;

    /**
     * the south docked tab pane
     */
    private DockedTabPane southTabPane;

    /**
     * scrolling tab pane if this is a central container
     */
    private ScrollingTabPane scrollingTabPane;

    /**
     * the split pane for this container
     */
    private TabContainerSplitPane splitPane;

    /**
     * the minimised tabs button panel
     */
    private ButtonPanel buttonPanel;

    /**
     * the minimised tab components added to this panel
     */
    private List<TabComponent> minimisedComponents;

    /**
     * Registered tab pane listeners
     */
    private List<DockedTabDragListener> listeners;

    private TabPane selectedTabPane;

    /**
     * Creates a new instance of DockedTabContainer
     */
    public DockedTabContainer(DesktopMediator desktopMediator, int orientation) {
        super(new BorderLayout());
        this.desktopMediator = desktopMediator;
        this.orientation = orientation;
        init();
    }

    /**
     * Initialises the state of this component
     */
    private void init() {
        initSplitPane();
        add(splitPane, BorderLayout.CENTER);
        splitPane.addPropertyChangeListener(this);
    }

    /**
     * Indicates whether this tab container has the focused tab pane.
     *
     * @return true | false
     */
    protected boolean hasFocusedTabPane() {
        if (northTabPane != null && northTabPane.isFocused()) {
            return true;
        } else if (southTabPane != null && southTabPane.isFocused()) {
            return true;
        } else if (scrollingTabPane != null && scrollingTabPane.isFocused()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the focused tab pane or null if no pane in this
     * container currently has the focus.
     *
     * @return the tab pane in focus
     */
    protected TabPane getTabPaneInFocus() {
        if (northTabPane != null && northTabPane.isFocused()) {
            return northTabPane;
        } else if (southTabPane != null && southTabPane.isFocused()) {
            return southTabPane;
        } else if (scrollingTabPane != null && scrollingTabPane.isFocused()) {
            return scrollingTabPane;
        }
        return null;
    }

    protected void setSelectedTabPane(TabPane tabPane) {
        this.selectedTabPane = tabPane;
    }

    public TabPane getSelectedTabPane() {
        return selectedTabPane;
    }

    protected void closeSelectedTab() {

        if (selectedTabPane != null) {

            int selectedIndex = selectedTabPane.getSelectedIndex();
            selectedTabPane.removeIndex(selectedIndex);
        }

    }

    protected void closeAllTabsInSelectedContainer() {

        if (selectedTabPane != null) {

            selectedTabPane.removeAllTabs();
        }

    }

    protected void removeAllTabsInAllContainers() {

        if (northTabPane != null) {

            northTabPane.removeAllTabs();
        }

        if (scrollingTabPane != null) {

            scrollingTabPane.removeAllTabs();
        }

        if (southTabPane != null) {

            southTabPane.removeAllTabs();
        }

    }

    /**
     * Indicates a focus change on the tab components.<br>
     * This is propagated to the mediator object.
     *
     * @param tabPane - the new focused tab pane
     */
    protected void tabPaneFocusChange(TabPane tabPane) {
        if (tabPane == northTabPane || tabPane == scrollingTabPane) {

            // turn on the glass pane on non-selected components
            splitPane.setGlassPaneVisible(SwingUtilities.BOTTOM, true);

            // turn off the selected one
            splitPane.setGlassPaneVisible(SwingUtilities.TOP, false);

            // inform the other tab pane in the split
            if (southTabPane != null) {
                southTabPane.focusLost();
            }

        } else if (tabPane == southTabPane) {

            splitPane.setGlassPaneVisible(SwingUtilities.TOP, true);
            splitPane.setGlassPaneVisible(SwingUtilities.BOTTOM, false);

            // inform the other tab panes in the split
            if (northTabPane != null) {
                northTabPane.focusLost();
            }
            if (scrollingTabPane != null) {
                scrollingTabPane.focusLost();
            }
        }
        desktopMediator.tabPaneFocusChange(this);
    }

    /**
     * Switches on the glass pane's of this container's tab panes.
     */
    protected void tabPaneFocusLost() {
        if (northTabPane != null) {
            northTabPane.focusLost();
        }

        if (scrollingTabPane != null) {
            scrollingTabPane.focusLost();
        }

        if (southTabPane != null) {
            southTabPane.focusLost();
        }

        splitPane.setGlassPaneVisible(SwingUtilities.TOP, true);
        splitPane.setGlassPaneVisible(SwingUtilities.BOTTOM, true);
    }

    /**
     * Sets the split pane divider location to that specified.
     *
     * @param the divider location
     */
    public void setSplitPaneDividerLocation(int location) {
        if (splitPane != null) {
            splitPane.setDividerLocation(location);
        }
    }

    /**
     * Provides notification of split pane divider movement events.
     *
     * @param the change event
     */
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if ("dividerLocation".equals(name)) {
            String value = e.getNewValue().toString();
            desktopMediator.splitPaneDividerMoved(orientation, Integer.parseInt(value));
            /*
            Log.debug("property change: " + e.getPropertyName() +
            " old value: " + e.getOldValue() + " new value: " + e.getNewValue());
            */
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
     * Notifies all registered listeners of a tab restored event.
     *
     * @param the event
     */
    protected void fireTabRestored(DockedTabEvent e) {
        desktopMediator.fireTabRestored(e);
    }

    /**
     * Invoked when a mouse button is pressed on a tab and then dragged.
     *
     * @param the encapsulating event object
     */
    public void fireDockedTabDragged(DockedDragEvent e) {
        desktopMediator.fireDockedTabDragged(e);
    }

    /**
     * Invoked when a mouse button has been released on a tab.
     *
     * @param the encapsulating event object
     */
    public void fireDockedTabReleased(DockedDragEvent e) {
        desktopMediator.fireDockedTabReleased(e);
    }

    /**
     * Selects the next tab from the current selection.
     */
    public void selectNextTab(int position) {
        TabPane tabPane = getTabPaneForPosition(position);
        tabPane.selectNextTab();
    }

    /**
     * Selects the previous tab from the current selection.
     */
    public void selectPreviousTab(int position) {
        TabPane tabPane = getTabPaneForPosition(position);
        tabPane.selectPreviousTab();
    }

    /**
     * Closed the specfied tab component with name at the specified position.
     *
     * @param the name of the tab component
     * @param the position
     */
    public void closeTabComponent(String name, int position) {

        TabPane tabPane = getTabPaneForPosition(position);

        if (position == SwingConstants.SOUTH
                || position == SwingConstants.SOUTH_WEST
                || position == SwingConstants.SOUTH_EAST) {

            if (tabPane != null) {

                ((AbstractTabPane) tabPane).closeTabComponent(name);

            } else {

                closeTabComponent(name, NORTH);
            }

        }

        if (tabPane != null) {

            ((AbstractTabPane) tabPane).closeTabComponent(name);
        }

    }

    /**
     * Returns the tab pane at the specified position.
     *
     * @param position - the position of the pane
     */
    protected TabPane getTabPaneForPosition(int position) {
        switch (position) {
            case SwingConstants.NORTH:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.NORTH_EAST:
                return northTabPane;
            case SwingConstants.SOUTH:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH_EAST:
                return southTabPane;
            case SwingConstants.CENTER:
                return scrollingTabPane;
        }
        return null;
    }

    /**
     * Returns the selected tab component at the specified position.
     *
     * @param the component position - <br>
     *            SwingConstants.NORTH<br>
     *            SwingConstants.NORTH_WEST<br>
     *            SwingConstants.NORTH_EAST<br>
     *            SwingConstants.SOUTH<br>
     *            SwingConstants.SOUTH_WEST<br>
     *            SwingConstants.SOUTH_EAST<br>
     *            SwingConstants.CENTER<br>
     */
    public TabComponent getComponentAt(int position) {

        TabPane tabPane = getTabPaneForPosition(position);

        if (tabPane != null) {

            return ((AbstractTabPane) tabPane).getSelectedComponent();
        }

        return null;
    }

    /**
     * Adds the specified listener to all tabbed panes within
     * this component to notify of tab events.
     *
     * @param the tab listener
     */
    public void addDockedTabListener(DockedTabDragListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<DockedTabDragListener>();
        }
        listeners.add(listener);
    }

    /**
     * Propagates a tab drag event to registered
     * listeners of this type.
     *
     * @param the drag event
     */
    protected void dockedTabDragged(DockedDragEvent e) {
        if (listeners == null || listeners.size() == 0) {
            return;
        }
        for (int i = 0, k = listeners.size(); i < k; i++) {
            listeners.get(i).dockedTabDragged(e);
        }
        desktopMediator.fireDockedTabDragged(e);
    }

    /**
     * Propagates a tab released event to registered
     * listeners of this type.
     *
     * @param the drag event
     */
    protected void dockedTabReleased(DockedDragEvent e) {
        if (listeners == null || listeners.size() == 0) {
            return;
        }
        for (int i = 0, k = listeners.size(); i < k; i++) {
            listeners.get(i).dockedTabReleased(e);
        }
        desktopMediator.fireDockedTabReleased(e);
    }

    /**
     * Returns this panel's orientation (position).
     *
     * @return the orientation/position of this component
     */
    public int getOrientation() {
        return orientation;
    }

    protected int getTabPanePosition(DockedTabPane tabPane) {
        if (tabPane == northTabPane) {
            if (orientation == SwingConstants.WEST) {
                return SwingConstants.NORTH_WEST;
            } else {
                return SwingConstants.NORTH_EAST;
            }
        } else if (tabPane == southTabPane) {
            if (orientation == SwingConstants.WEST) {
                return SwingConstants.SOUTH_WEST;
            } else if (orientation == SwingConstants.CENTER) {
                return SwingConstants.SOUTH;
            } else {
                return SwingConstants.SOUTH_EAST;
            }
        }
        return SwingConstants.NORTH_WEST; // default
    }

    /**
     * Overrides here to return an appropriate size
     * depneding on orinetation/position and visibility
     * of the minimised buttons panel.
     *
     * @return this components preferred size
     */
    public Dimension getPreferredSize() {
        if (buttonPanel == null) {
            return new Dimension(splitPane.getWidth(), getHeight());
        } else {
            if (orientation == SwingConstants.CENTER) {
                return new Dimension(getWidth(), buttonPanel.getHeight());
            } else {
                return new Dimension(buttonPanel.getWidth(), getHeight());
            }
        }
    }

    /**
     * Returns the width of this pane.
     *
     * @return the pane width
     */
    public int getPaneWidth() {
        if (buttonPanel == null) {
            return getWidth();
        } else if (splitPane != null) {
            return splitPane.getWidth() + buttonPanel.getWidth();
        }
        return getWidth();
    }

    /**
     * Adds the specified component as a docked tab component
     * in the specified position.
     *
     * @param the tab title
     * @param the tab icon
     * @param the component
     * @param the tab's tool tip
     * @param the position - one of SwingConstants.NORTH | SOUTH
     */
    public void addDockedTab(String title, Icon icon, Component component,
                             String tip, int position) {

        // make sure the split pane is visible
        splitPane.setVisible(true);

        DockedTabPane tabPane = null;

        // check if we have a north tab pane.
        // if not, add there regardless of specified position
        if (northTabPane == null && orientation != CENTER) {

            northTabPane = new DockedTabPane(this, false);
            splitPane.setLeftComponent(northTabPane);
            tabPane = northTabPane;

            // if we have minimised tabs but added a tab pane
            // restore it to its previous size
            //if (buttonPanel != null) {
            //desktopMediator.resetPaneToPreferredSizes(orientation, true);
            //}

        } else {

            switch (position) {
                case SwingConstants.NORTH:
                case SwingConstants.NORTH_WEST:
                case SwingConstants.NORTH_EAST:
                    tabPane = northTabPane;
                    break;
                case SwingConstants.SOUTH:
                case SwingConstants.SOUTH_WEST:
                case SwingConstants.SOUTH_EAST:
                    if (southTabPane == null) {
                        southTabPane = new DockedTabPane(this, true);
                        southPaneCreated();
                    }
                    tabPane = southTabPane;
                    break;
                case SwingConstants.CENTER:
                    if (scrollingTabPane == null) {
                        scrollingTabPane = new ScrollingTabPane(this);
                        splitPane.setLeftComponent(scrollingTabPane);
                        splitPane.setGlassPaneVisible(SwingUtilities.BOTTOM, true);
                        splitPane.setGlassPaneVisible(SwingUtilities.TOP, false);
                        splitPane.setResizeWeight(1.0);
                        if (southTabPane != null) {
                            splitPane.setDividerSize(
                                    ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE);
                        }
                    }
                    scrollingTabPane.addTab(position, title, icon, component, tip);
                    return;
            }

        }

        if (tabPane != null) {
            tabPane.addTab(position, title, icon, component, tip);
        }

        if (orientation != SwingConstants.CENTER) {
            desktopMediator.resetPaneToPreferredSizes(orientation, true);
        }

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

        TabPane tabPane = getTabPaneForPosition(position);
        ((AbstractTabPane) tabPane).setToolTipTextForComponent(component, toolTipText);
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

        TabPane tabPane = getTabPaneForPosition(position);
        ((AbstractTabPane) tabPane).setTabTitleForComponent(component, title);
    }

    public void setSelectedIndex(int position, int index) {

        TabPane tabPane = getTabPaneForPosition(position);
        if (tabPane != null) {
            ((AbstractTabPane) tabPane).setSelectedIndex(index);
        }
    }

    public int getSelectedIndex(int position) {

        TabPane tabPane = getTabPaneForPosition(position);
        if (tabPane != null) {

            return ((AbstractTabPane) tabPane).getSelectedIndex();
        }

        return -1;
    }

    public void setSelectedPane(int position, String name) {

        TabComponent tabComponent = getTabComponent(position, name);
        if (tabComponent == null) {
            return;
        }

        TabPane tabPane = getTabPaneForPosition(position);
        ((AbstractTabPane) tabPane).setSelectedTab(tabComponent);
    }

    /**
     * Returns the open tab count at the specified position.
     *
     * @param the tab pane position
     * @return the tab count
     */
    public int getTabCount(int position) {

        TabPane tabPane = getTabPaneForPosition(position);
        if (tabPane != null) {

            return ((AbstractTabPane) tabPane).getTabCount();
        }

        return 0;
    }

    /**
     * Returns the open tab components in a list at
     * the specified position.
     *
     * @param the tab pane position
     * @return a list of tab components
     */
    public List<TabComponent> getOpenTabs(int position) {

        TabPane tabPane = getTabPaneForPosition(position);
        if (tabPane != null) {

            return ((AbstractTabPane) tabPane).getTabComponents();
        }

        return null;
    }

    public TabComponent getTabComponent(int position, String title) {

        String tabTitle = title.toUpperCase();
        List<TabComponent> components = getOpenTabs(position);
        if (components != null) {
            int tabCount = components.size();
            for (int i = 0; i < tabCount; i++) {
                TabComponent tabComponent = components.get(i);
                String _title = tabComponent.getTitle();
                if (_title != null && _title.toUpperCase().startsWith(tabTitle)) {
                    return tabComponent;
                }
            }
        }

        return getTabComponent(title);
    }

    private TabComponent getTabComponent(String title) {

        String tabTitle = title.toUpperCase();
        int[] positions = {SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.CENTER};
        for (int tabPosition : positions) {

            List<TabComponent> tabs = getOpenTabs(tabPosition);
            if (tabs != null) {

                for (TabComponent tab : tabs) {

                    if (StringUtils.startsWithIgnoreCase(tab.getTitle(), tabTitle)) {

                        return tab;
                    }

                }
            }

        }

        return null;
    }

    /**
     * Minimises the tab from the panel at the specified index.
     *
     * @param the index to be removed
     */
    protected void minimiseComponent(int position, int index) {
        TabPane tabPane = getTabPaneForPosition(position);
        if (tabPane != null && tabPane instanceof DockedTabPane) {
            ((DockedTabPane) tabPane).minimiseIndex(index);
        }
    }

    /**
     * Minimises the tab from the panel at the specified index.
     *
     * @param the index to be removed
     */
    protected void minimiseComponent(TabComponent tabComponent) {

        if (minimisedComponents == null) {
            minimisedComponents = new ArrayList<TabComponent>();
        }

        // add the component to the minimised cache
        minimisedComponents.add(tabComponent);

        if (buttonPanel == null) {
            buttonPanel = new ButtonPanel();

            // add the button panel in the required position
            switch (orientation) {
                case WEST:
                    add(buttonPanel, BorderLayout.WEST);
                    break;
                case CENTER:
                    add(buttonPanel, BorderLayout.SOUTH);
                    break;
                case EAST:
                    add(buttonPanel, BorderLayout.EAST);
                    break;
            }

        }
        buttonPanel.addButton(tabComponent);
        validate();
        repaint();
    }

    /**
     * Removes the south tab pane component and
     * changes the state of the split pane.
     */
    private void southPaneRemoved() {
        // store the current div location
        splitPane.storeDividerLocation();
        // remove the south pane
        splitPane.setRightComponent(null);

        // set divider to 0 so it doesn't 'look' like a split pane
        splitPane.setDividerSize(0);
        southTabPane = null;
    }

    /**
     * Adds the south tab pane component and
     * changes the state of the split pane.
     */
    private void southPaneCreated() {
        splitPane.setRightComponent(southTabPane);
        splitPane.restoreDividerLocation();
        //splitPane.setDividerLocation(defaultDividerLocation);
//        if (orientation == SwingConstants.CENTER && scrollingTabPane != null) {
        splitPane.setDividerSize(ApplicationConstants.SPLIT_PANE_DIVIDER_SIZE);
//        }
    }

    /**
     * Removes the specified tab pane from this component.
     *
     * @param the tab pane to be removed
     */
    protected void removeTabPane(DockedTabPane tabPane) {
        if (tabPane == northTabPane && orientation != SwingConstants.CENTER) {
            splitPane.setLeftComponent(null);
            northTabPane = null;
            // if we have a south pane, switch it
            if (southTabPane != null) {
                northTabPane = southTabPane;
                southPaneRemoved();
                splitPane.setLeftComponent(northTabPane);
            }
        } else if (tabPane == southTabPane) {
            southPaneRemoved();
        }

        // if they're both null and this is not the 
        // center component - hide the split pane
        if (orientation != SwingConstants.CENTER &&
                northTabPane == null && southTabPane == null) {
            splitPane.setVisible(false);

            // if the button panel is null also, remove this panel
            if (buttonPanel == null) {

                if (minimisedComponents != null) {
                    minimisedComponents.clear();
                    minimisedComponents = null;
                }
                desktopMediator.removeDockedContainer(this);
                return;
            }
        }

        if (splitPane.isVisible()) {
            splitPane.validate();
        } else {
            desktopMediator.resetPaneToPreferredSizes(orientation, false);
        }
        validate();
        repaint();
    }

    /**
     * Restores the specified tab component from a minimised state.
     *
     * @param the tab component to restore
     */
    protected void restoreTabComponent(TabComponent tabComponent) {
        //boolean restore = false;
        minimisedComponents.remove(tabComponent);

        if (minimisedComponents.size() == 0) {
            remove(buttonPanel);
            buttonPanel = null;
        }

        if (orientation != SwingConstants.CENTER) {
            // a restored tab is always added to the north pane
            if (northTabPane == null) {
                northTabPane = new DockedTabPane(this, false);
                // check the split pane
                if (splitPane == null) {
                    initSplitPane();
                } else if (!splitPane.isVisible()) {
                    splitPane.setVisible(true);
                }
                splitPane.setLeftComponent(northTabPane);
                desktopMediator.resetPaneToPreferredSizes(orientation, true);
            }
            northTabPane.addTab(tabComponent);
            northTabPane.setSelectedTab(tabComponent);
        } else { // for center only
            if (southTabPane == null) {
                southTabPane = new DockedTabPane(this, true);
                southPaneCreated();
            }
            southTabPane.addTab(tabComponent);
            southTabPane.setSelectedTab(tabComponent);
        }

        validate();
        repaint();

        // fire the event
        fireTabRestored(new DockedTabEvent(tabComponent));
    }

    /**
     * Initialises the state of the split pane component.
     */
    private void initSplitPane() {
        splitPane = new TabContainerSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(0);
    }

    /**
     * Indicates whether the tab pane in the specified
     * position is visible.
     *
     * @return <code>true | false</code>
     */
    public boolean isTabPaneVisible(int position) {
        switch (position) {
            case SwingConstants.NORTH:
            case SwingConstants.NORTH_WEST:
            case SwingConstants.NORTH_EAST:
                return (northTabPane != null);
            case SwingConstants.SOUTH:
            case SwingConstants.SOUTH_WEST:
            case SwingConstants.SOUTH_EAST:
                return (southTabPane != null);
        }
        return (northTabPane != null || southTabPane != null);
    }

    /**
     * Returns if the tab with the specfied name is currently minimised.
     *
     * @param name - the component's name (tab title)
     */
    public boolean isMinimised(String name) {
        if (minimisedComponents == null || minimisedComponents.isEmpty()) {
            return false;
        }

        for (int i = 0, k = minimisedComponents.size(); i < k; i++) {
            TabComponent tab = minimisedComponents.get(i);
            if (tab.getTitle().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restores the tab component with the specified name.
     *
     * @param name - the component's name (tab title)
     */
    public void restore(String name) {
        if (minimisedComponents == null || minimisedComponents.isEmpty()) {
            return;
        }

        for (int i = 0, k = minimisedComponents.size(); i < k; i++) {
            TabComponent tab = minimisedComponents.get(i);
            if (tab.getTitle().equals(name)) {
                restoreTabComponent(tab);
                return;
            }
        }
    }

    /**
     * Indicates whether the minimised tab button
     * panel is visible.
     *
     * @return <code>true | false</code>
     */
    public boolean isButtonPanelVisible() {
        return buttonPanel != null;
    }

    /**
     * The button panel to containing the minimised tab buttons.
     */
    @SuppressWarnings("deprecation")
    private class ButtonPanel extends JPanel
            implements ActionListener {

        private int height;

        protected ButtonPanel() {
            super(new FlowLayout(FlowLayout.LEADING, 0, 2));
            Font font = UIManager.getFont("Label.font");
            FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
            height = fm.getHeight() + 10;
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public Dimension getPreferredSize() {
            return new Dimension(getWidth(), getHeight());
        }

        public int getHeight() {
            if (orientation == CENTER) {
                return height;
            } else {
                return super.getHeight();
            }
        }

        public int getWidth() {
            if (orientation == CENTER) {
                return super.getWidth();
            } else {
                return height;
            }
        }

        protected void addButton(TabComponent tabComponent) {
            MinimiseTabButton button = new MinimiseTabButton(tabComponent);
            button.addActionListener(this);
            add(button);
        }

        public void actionPerformed(ActionEvent e) {
            MinimiseTabButton button = (MinimiseTabButton) e.getSource();
            remove(button);
            restoreTabComponent(button.getTabComponent());
        }

    }

    /**
     * Defines a minimised tab button.
     */
    private class MinimiseTabButton extends JButton {

        private TabComponent tabComponent;

        protected MinimiseTabButton(TabComponent tabComponent) {
            this.setMargin(ApplicationConstants.EMPTY_INSETS);
            this.tabComponent = tabComponent;
            setToolTipText(tabComponent.getToolTip());

            String titleText = tabComponent.getDisplayName();
            // set the icon/text for this button
            switch (orientation) {
                case WEST:
                    setIcon(new VerticalTextIcon(titleText, false));
                    break;
                case CENTER:
                    setText(titleText);
                    break;
                case EAST:
                    setIcon(new VerticalTextIcon(titleText, true));
                    break;
            }

        }

        public int getHeight() {
            switch (orientation) {
                case WEST:
                    return getIcon().getIconHeight() + 8;
                case EAST:
                    return getIcon().getIconHeight() + 8;
            }
            return super.getHeight();
        }

        public int getWidth() {
            switch (orientation) {
                case WEST:
                    return getIcon().getIconWidth() + 8;
                case EAST:
                    return getIcon().getIconWidth() + 8;
            }
            return super.getWidth();
        }

        protected TabComponent getTabComponent() {
            return tabComponent;
        }

    }

    public DockedTabPane getNorthTabPane() {
        return northTabPane;
    }

    public DockedTabPane getSouthTabPane() {
        return southTabPane;
    }

    public ScrollingTabPane getScrollingTabPane() {
        return scrollingTabPane;
    }

}











