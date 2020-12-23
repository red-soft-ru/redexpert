/*
 * AbstractTabPane.java
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

import org.executequery.gui.browser.BrowserViewPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract tab pane base.
 *
 * @author Takis Diakoumis
 */
public abstract class AbstractTabPane extends JPanel
        implements TabPane {

    /**
     * the components added to this panel
     */
    protected List<TabComponent> components;

    /**
     * the display panel layout manager
     */
    protected CardLayout cardLayout;

    /**
     * the currently selected index
     */
    protected int selectedIndex;

    /**
     * this pane's parent container
     */
    protected DockedTabContainer parent;

    /**
     * the tab component panel
     */
    protected JPanel componentPanel;

    /**
     * Whether this tab pane is the focused tab pane
     */
    protected boolean isFocusedTabPane;

    /**
     * Initialises the state of this object
     */
    protected void initComponents() {
        selectedIndex = -1;

        // repo for added components
        components = new ArrayList<TabComponent>();

        // panel and layout for components
        cardLayout = new CardLayout();
        componentPanel = new JPanel(cardLayout);

        add(componentPanel, BorderLayout.CENTER);
    }

    /**
     * the selected tab background
     */
    private Color selectedTabBackground;

    /**
     * Returns the active selection background colour for a tab.
     *
     * @return the selected background colour
     */
    protected Color getSelectedTabBackground() {
        if (selectedTabBackground == null) {
            if ((UIUtils.isMetalLookAndFeel()) ||// && !UIUtils.usingOcean()) ||
                    UIUtils.isMotifLookAndFeel() || UIUtils.isWindowsLookAndFeel()) {

                selectedTabBackground = UIUtils.getDefaultActiveBackgroundColour();

            } else if (UIUtils.isNativeMacLookAndFeel()) {

                selectedTabBackground = UIManager.getColor("Focus.color");

            } else {

                double darker = 0.9;
                // check that the normal panel bg is not 
                // the same as the tab selection bg
                Color color1 = UIManager.getColor("TabbedPane.selected");
                Color color2 = UIManager.getColor("TabbedPane.background");
                if (color1 != null && color2 != null) {
                    if (color1.getRGB() == color2.getRGB()) {
                        selectedTabBackground = UIUtils.getDarker(color1, darker);
                    } else {
                        selectedTabBackground = color1;
                    }
                } else if (color1 == null && color2 != null) {
                    selectedTabBackground = UIUtils.getDarker(color2, darker);
                } else if (color2 == null && color1 != null) {
                    selectedTabBackground = color1;
                }
            }
        }
        return selectedTabBackground;
    }

    /**
     * Returns the active selection foreground colour for a tab.
     *
     * @return the selected foreground colour
     */
    protected Color getSelectedTabForeground() {
        return getTabForeground();
    }

    /**
     * the selected tab foreground
     */
    private Color tabForeground;

    /**
     * Returns the default foreground colour for a tab.
     *
     * @return the foreground colour
     */
    protected Color getTabForeground() {
        if (tabForeground == null) {

            if (!UIUtils.isNativeMacLookAndFeel()) {
                tabForeground = UIManager.getColor("TabbedPane.foreground");
            } else {
                tabForeground = UIManager.getColor("text");
            }

        }
        return tabForeground;
    }

    /**
     * the default tab background
     */
    private Color tabBackground;

    /**
     * Returns the default background colour for a tab.
     *
     * @return the background colour
     */
    protected Color getTabBackground() {
        if (tabBackground == null) {
            tabBackground = getBackground();
        }
        return tabBackground;
    }

    /**
     * the selected no-focus tab background
     */
    private Color nofocusTabBackground;

    /**
     * Returns the no-focus tab background.
     *
     * @return the no-focus background
     */
    protected Color getNofocusTabBackground() {
        if (nofocusTabBackground == null) {
            nofocusTabBackground = UIUtils.getBrighter(getTabBackground(), 0.9);
        }
        return nofocusTabBackground;
    }

    /**
     * Indicates a top-level focus change.
     */
    protected abstract void focusChanged();

    /**
     * Indicates whether this tab pane has focus.
     *
     * @return true | false
     */
    public boolean isFocused() {
        return isFocusedTabPane;
    }

    /**
     * Indicates a focus gain.
     */
    public void focusGained() {
        if (isFocusedTabPane) {
            return;
        }
        isFocusedTabPane = true;
        focusChanged();
        parent.tabPaneFocusChange(this);
        parent.setSelectedTabPane(this);
    }

    /**
     * Indicates a focus loss.
     */
    public void focusLost() {
        if (!isFocusedTabPane) {
            return;
        }
        isFocusedTabPane = false;
        focusChanged();
    }

    /**
     * Returns the position of this tab pane.
     */
    public int getPosition() {
        return parent.getOrientation();
    }

    /**
     * Sets the title of the specified component to title which can be null.
     * An internal exception is raised if there is no tab for the
     * specified component.
     *
     * @param the component where the title should be set
     * @param the title to be displayed in the tab
     */
    public void setTabTitleForComponent(Component component, String title) {
        int index = indexOfComponent(component);
        if (index == -1) {
            throw new IndexOutOfBoundsException(bundledString("error.notFound"));
        }
        setTabTitleAt(index, title);
    }

    /**
     * Selects the next tab from the current selection.
     */
    public void selectNextTab() {

        int tabCount = getTabCount();

        if (tabCount > 0) {

            if (selectedIndex < tabCount - 1) {

                setSelectedIndex(selectedIndex + 1);

            } else {

                setSelectedIndex(0);
            }

        }
    }

    /**
     * Selects the previous tab from the current selection.
     */
    public void selectPreviousTab() {

        int tabCount = getTabCount();

        if (tabCount > 0) {

            if (selectedIndex > 0) {

                setSelectedIndex(selectedIndex - 1);

            } else {

                setSelectedIndex(tabCount - 1);
            }

        }
    }

    /**
     * Sets the title at index to title which can be null.
     * An internal exception is raised if there is no tab at that index.
     *
     * @param the tab index where the title should be set
     * @param the title to be displayed in the tab
     */
    public void setTabTitleAt(int index, String title) {

        //Log.debug("Setting tab title at: " + index + " to: " + title);

        if (components == null || components.isEmpty()) {
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.paneEmpty"));
        }
        TabComponent tabComponent = components.get(index);
        tabComponent.setTitle(title);

        // make sure the title is unique
        String suffix = getTitleSuffix(tabComponent);
        if (suffix != null) {
            tabComponent.setTitleSuffix(suffix);
        }

        //TODO: implement property change event stuff
    }

    /**
     * Returns a unique title for the specified tab component.
     */
    protected String getTitleSuffix(TabComponent tabComponent) {
        int componentCount = components.size();
        // make sure the title is unique
        if (componentCount > 1) {

            int counterIndex = 0;
            String title = tabComponent.getTitle();
            for (int i = 0; i < componentCount; i++) {
                TabComponent _tabComponent = components.get(i);
                if (_tabComponent != tabComponent) {
                    String _title = _tabComponent.getTitle();
                    if (_title.equals(title)) {
                        int sameTitleIndex = _tabComponent.getSameTitleIndex();
                        if (sameTitleIndex > 0) {
                            counterIndex = Math.max(counterIndex, sameTitleIndex);
                        } else {
                            counterIndex = 1;
                        }
                    }
                }
            }

            if (counterIndex > 0) {
                counterIndex++;
                tabComponent.setSameTitleIndex(counterIndex);
                return " [" + counterIndex + "]";
            }

        }
        return null;
    }

    /**
     * Sets the tool tip at index to toolTipText which can be null.
     * An internal exception is raised if there is no tab at that index.
     *
     * @param the tab index where the tool tip should be set
     * @param the tool tip text to be displayed in the tab
     */
    public void setToolTipTextAt(int index, String toolTipText) {
        if (components == null || components.isEmpty()) {
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.paneEmpty"));
        }
        TabComponent tabComponent = components.get(index);
        tabComponent.setToolTip(toolTipText);
        //TODO: implement property change event stuff
    }

    /**
     * Sets the tool tip for the specified component to toolTipText
     * which can be null. An internal exception is raised if there
     * is no tab for the specified component.
     *
     * @param the component where the tool tip should be set
     * @param the tool tip text to be displayed in the tab
     */
    public void setToolTipTextForComponent(Component component, String toolTipText) {
        int index = indexOfComponent(component);
        if (index == -1) {
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.notFound"));
        }
        setToolTipTextAt(index, toolTipText);
    }

    /**
     * Returns the index of the tab for the specified component.
     *
     * @param the component
     * @return of the index of component or -1 if not found
     */
    public int indexOfComponent(Component component) {
        if (components == null || components.isEmpty()) {
            return -1;
        }
        for (int i = 0, k = components.size(); i < k; i++) {
            TabComponent tabComponent = components.get(i);
            if (tabComponent.getComponent() == component) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Removes the tab with the specified name from the pane.
     *
     * @param the name
     */
    public void closeTabComponent(String name) {
        int index = indexOfTab(name);
        if (index != -1) {
            removeIndex(index);
        }
    }

    /**
     * Returns the index of the tab for the specified title.
     *
     * @param the title
     * @return of the index of component or -1 if not found
     */
    public int indexOfTab(String title) {
        if (components == null || components.isEmpty()) {
            return -1;
        }
        for (int i = 0, k = components.size(); i < k; i++) {
            TabComponent tabComponent = components.get(i);
            if (tabComponent.getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets the specified panel as the actual tab display.
     *
     * @param the panel displaying the actual tabs
     */
    protected void setTabPanel(JPanel panel) {
        add(panel, BorderLayout.NORTH);
    }

    public void addTab(String title, Component component) {
        addTab(-1, title, null, component, null);
    }

    public void addTab(String title, Icon icon, Component component) {
        addTab(-1, title, icon, component, null);
    }

    public void addTab(int position, String title, Icon icon, Component component, String tip) {
        addTab(new TabComponent(position, component, title, icon, tip));
    }

    /**
     * Adds the specified tab component to the pane.
     *
     * @param the component to be added
     */
    public abstract void addTab(TabComponent tabComponent);

    /**
     * Returns the tab component at the specified index.
     *
     * @param the index of the component
     * @return the component at the specified index
     */
    protected TabComponent getTabComponentAt(int index) {
        if (index < 0) {
            return null;
        }
        return components.get(index);
    }

    /**
     * Returns the tab count for this component.
     *
     * @return the tab count
     */
    public int getTabCount() {
        if (components != null) {
            return components.size();
        }
        return 0;
    }

    /**
     * Notifies all registered listeners of a tab minimised event.
     *
     * @param the event
     */
    protected void fireTabMinimised(DockedTabEvent e) {
        parent.fireTabMinimised(e);
    }

    /**
     * Notifies all registered listeners of a tab restored event.
     *
     * @param the event
     */
    protected void fireTabRestored(DockedTabEvent e) {
        parent.fireTabRestored(e);
    }

    /**
     * Notifies all registered listeners of a tab selected event.
     *
     * @param the event
     */
    protected void fireTabSelected(DockedTabEvent e) {
        TabComponent tabComponent = (TabComponent) e.getSource();
        if (tabComponent.getComponent() instanceof TabView) {
            ((TabView) tabComponent.getComponent()).tabViewSelected();
        }
        parent.fireTabSelected(e);
    }

    /**
     * Notifies all registered listeners of a tab deselected event.
     *
     * @param the event
     */
    protected void fireTabDeselected(DockedTabEvent e) {
        parent.fireTabDeselected(e);
    }

    /**
     * Notifies all registered listeners of a tab closed event.
     *
     * @param the event
     */
    protected void fireTabClosed(DockedTabEvent e) {
        /*
        TabComponent tabComponent = (TabComponent)e.getSource();
        if (tabComponent.getComponent() instanceof DockedTabView) {
            ((DockedTabView)tabComponent.getComponent()).tabViewClosing();
        }
         */
        TabComponent tabComponent = (TabComponent)e.getSource();
        if (tabComponent.getComponent() instanceof BrowserViewPanel) {
            ((BrowserViewPanel)tabComponent.getComponent()).getCurrentView().cleanup();
        }
        parent.fireTabClosed(e);
    }

    /**
     * Sets the selected tab component as that specified.
     *
     * @param the tab component to set selected
     */
    public void setSelectedTab(TabComponent tabComponent) {
        setSelectedIndex(components.indexOf(tabComponent));
        focusGained();
    }

    /**
     * Sets the selected index to that specified.
     *
     * @param the index to set selected
     */
    public void setSelectedIndex(int index) {
        if (index == -1) {
            return;
        }

        if (selectedIndex != -1) {
            // fire the deselected event
            TabComponent tabComponent = components.get(selectedIndex);
            if (tabComponent.getComponent() instanceof TabView) {
                TabView dockedView = (TabView) tabComponent.getComponent();
                if (dockedView.tabViewDeselected()) {
                    fireTabDeselected(new DockedTabEvent(tabComponent));
                } else {
                    return;
                }
            }
        }

        selectedIndex = index;
        TabComponent tabComponent = components.get(index);
        cardLayout.show(componentPanel, tabComponent.getLayoutName());
    }

    /**
     * Removes the tab from the panel at the specified index.
     *
     * @param the index to be removed
     */
    public abstract void removeIndex(int index);

    public void removeSelectedTab() {

        if (selectedIndex != -1) {

            removeIndex(selectedIndex);
        }
    }

    /**
     * Checks whether a close of the panel will not be
     * vetoed by the panel itself.
     *
     * @param the tab component to be closed
     * @return true if ok to close, false otherwise
     */
    protected boolean okToClose(TabComponent tabComponent) {
        if (tabComponent.getComponent() instanceof TabView) {
            TabView dockedView = (TabView) tabComponent.getComponent();
            return dockedView.tabViewClosing();
        }
        return true;
    }

    /**
     * Returns the tab components within list.
     *
     * @return the tab components
     */
    public List<TabComponent> getTabComponents() {
        return components;
    }

    /**
     * Returns the currently selected index or -1 if nothing is selected.
     *
     * @return the currently selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Returns the currently selected tab component
     * or null if nothing is selected.
     *
     * @return the currently selected tab component
     */
    public TabComponent getSelectedComponent() {
        if (selectedIndex == -1) {
            return null;
        } else {
            return components.get(selectedIndex);
        }
    }

    protected final Insets tabInsets() {

        Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
        if (insets == null) {

            insets = new Insets(0, 9, 1, 9);
        }

        return insets;
    }

    protected String bundledString(String key) {
        return Bundles.get(getClass(), key);
    }

}

