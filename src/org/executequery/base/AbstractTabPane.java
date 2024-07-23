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

import org.executequery.gui.menu.EditToolsManager;
import org.executequery.gui.browser.BrowserViewPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract tab pane base.
 *
 * @author Takis Diakoumis
 */
public abstract class AbstractTabPane extends JPanel
        implements TabPane {

    private Color tabBackground;
    private Color tabForeground;
    private Color selectedTabBackground;

    protected JPanel componentPanel;
    protected CardLayout cardLayout;
    protected DockedTabContainer parent;

    protected int selectedIndex;
    protected boolean isFocusedTabPane;
    protected List<TabComponent> components;

    protected void initComponents() {
        selectedIndex = -1;
        cardLayout = new CardLayout();
        components = new ArrayList<>();
        componentPanel = new JPanel(cardLayout);

        add(componentPanel, BorderLayout.CENTER);
    }

    /**
     * Returns the active selection background colour for a tab.
     *
     * @return the selected background colour
     */
    protected Color getSelectedTabBackground() {

        if (selectedTabBackground != null)
            return selectedTabBackground;

        if ((UIUtils.isMetalLookAndFeel()) || UIUtils.isWindowsLookAndFeel()) {
            selectedTabBackground = UIUtils.getDefaultActiveBackgroundColour();

        } else if (UIUtils.isNativeMacLookAndFeel()) {
            selectedTabBackground = UIManager.getColor("Focus.color");

        } else {
            double darker = 0.9;
            Color selectionColor = UIManager.getColor("TabbedPane.selected");
            Color backgroundColor = UIManager.getColor("TabbedPane.background");

            if (selectionColor != null && backgroundColor != null) {
                selectedTabBackground = selectionColor.getRGB() == backgroundColor.getRGB() ?
                        UIUtils.getDarker(selectionColor, darker) :
                        selectionColor;

            } else if (selectionColor == null && backgroundColor != null) {
                selectedTabBackground = UIUtils.getDarker(backgroundColor, darker);

            } else if (selectionColor != null)
                selectedTabBackground = selectionColor;
        }

        return selectedTabBackground;
    }

    /**
     * Returns the default foreground colour for a tab.
     *
     * @return the foreground colour
     */
    protected Color getTabForeground() {

        if (tabForeground == null) {
            tabForeground = UIUtils.isNativeMacLookAndFeel() ?
                    UIManager.getColor("text") :
                    UIManager.getColor("TabbedPane.foreground");
        }

        return tabForeground;
    }

    /**
     * Returns the default background colour for a tab.
     *
     * @return the background colour
     */
    protected Color getTabBackground() {
        if (tabBackground == null)
            tabBackground = getBackground();
        return tabBackground;
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
     * @param component the component where the title should be set
     * @param title     the title to be displayed in the tab
     */
    public void setTabTitleForComponent(Component component, String title) {
        int index = indexOfComponent(component);
        if (index == -1)
            throw new IndexOutOfBoundsException(bundledString("error.notFound"));

        setTabTitleAt(index, title);
    }

    /**
     * Sets the title at index to title which can be null.
     * An internal exception is raised if there is no tab at that index.
     *
     * @param index the tab index where the title should be set
     * @param title the title to be displayed in the tab
     */
    public void setTabTitleAt(int index, String title) {

        if (components == null || components.isEmpty())
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.paneEmpty"));

        TabComponent tabComponent = components.get(index);
        tabComponent.setTitle(title);

        String suffix = getTitleSuffix(tabComponent);
        if (suffix != null)
            tabComponent.setTitleSuffix(suffix);
    }

    /**
     * Returns a unique title for the specified tab component.
     */
    protected String getTitleSuffix(TabComponent tabComponent) {

        int componentCount = components.size();
        if (componentCount < 2)
            return null;

        int counterIndex = 0;
        String title = tabComponent.getTitle();

        for (TabComponent tempComponent : components) {
            if (tempComponent != tabComponent) {

                String tempComponentTitle = tempComponent.getTitle();
                if (tempComponentTitle.equals(title)) {

                    int sameTitleIndex = tempComponent.getSameTitleIndex();
                    counterIndex = sameTitleIndex > 0 ? Math.max(counterIndex, sameTitleIndex) : 1;
                }
            }
        }

        if (counterIndex > 0) {
            counterIndex++;
            tabComponent.setSameTitleIndex(counterIndex);
            return " [" + counterIndex + "]";
        }

        return null;
    }

    /**
     * Sets the tool tip at index to toolTipText which can be null.
     * An internal exception is raised if there is no tab at that index.
     *
     * @param index       the tab index where the tool tip should be set
     * @param toolTipText the tool tip text to be displayed in the tab
     */
    public void setToolTipTextAt(int index, String toolTipText) {

        if (components == null || components.isEmpty())
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.paneEmpty"));

        components.get(index).setToolTip(toolTipText);
    }

    /**
     * Sets the tool tip for the specified component to toolTipText
     * which can be null. An internal exception is raised if there
     * is no tab for the specified component.
     *
     * @param component   the component where the tool tip should be set
     * @param toolTipText the tool tip text to be displayed in the tab
     */
    public void setToolTipTextForComponent(Component component, String toolTipText) {

        int index = indexOfComponent(component);
        if (index == -1)
            throw new IndexOutOfBoundsException(Bundles.get(AbstractTabPane.class, "error.notFound"));

        setToolTipTextAt(index, toolTipText);
    }

    /**
     * Returns the index of the tab for the specified component.
     *
     * @return of the index of component or -1 if not found
     */
    public int indexOfComponent(Component component) {

        if (components == null || components.isEmpty())
            return -1;

        for (int i = 0, k = components.size(); i < k; i++)
            if (components.get(i).getComponent() == component)
                return i;

        return -1;
    }

    /**
     * Removes the tab with the specified name from the pane.
     */
    public void closeTabComponent(String name) {
        int index = indexOfTab(name);
        if (index != -1)
            removeIndex(index);
    }

    /**
     * Returns the index of the tab for the specified title.
     *
     * @return of the index of component or -1 if not found
     */
    public int indexOfTab(String title) {

        if (components == null || components.isEmpty())
            return -1;

        for (int i = 0, k = components.size(); i < k; i++)
            if (Objects.equals(components.get(i).getDisplayName(), title))
                return i;

        return -1;
    }

    /**
     * Sets the specified panel as the actual tab display.
     */
    protected void setTabPanel(JPanel panel) {
        add(panel, BorderLayout.NORTH);
    }

    public abstract void addTab(TabComponent tabComponent);

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
     * Returns the tab component at the specified index.
     *
     * @return the component at the specified index
     */
    protected TabComponent getTabComponentAt(int index) {
        return index >= 0 ? components.get(index) : null;
    }

    /**
     * Returns the tab count for this component.
     *
     * @return the tab count
     */
    public int getTabCount() {
        return components != null ? components.size() : 0;
    }

    /**
     * Notifies all registered listeners of a tab minimised event.
     */
    protected void fireTabMinimised(DockedTabEvent e) {
        parent.fireTabMinimised(e);
    }

    /**
     * Notifies all registered listeners of a tab selected event.
     */
    protected void fireTabSelected(DockedTabEvent e) {

        TabComponent tabComponent = (TabComponent) e.getSource();
        if (tabComponent.getComponent() instanceof TabView)
            ((TabView) tabComponent.getComponent()).tabViewSelected();

        parent.fireTabSelected(e);
        EditToolsManager.checkEnable();
    }

    /**
     * Notifies all registered listeners of a tab deselected event.
     */
    protected void fireTabDeselected(DockedTabEvent e) {
        parent.fireTabDeselected(e);
    }

    /**
     * Notifies all registered listeners of a tab closed event.
     */
    protected void fireTabClosed(DockedTabEvent e) {

        TabComponent tabComponent = (TabComponent) e.getSource();
        if (tabComponent.getComponent() instanceof BrowserViewPanel)
            if (((BrowserViewPanel) tabComponent.getComponent()).getCurrentView() != null)
                ((BrowserViewPanel) tabComponent.getComponent()).getCurrentView().cleanup();

        parent.fireTabClosed(e);
    }

    /**
     * Sets the selected tab component as that specified.
     */
    public void setSelectedTab(TabComponent tabComponent) {
        setSelectedIndex(components.indexOf(tabComponent));
        focusGained();
    }

    /**
     * Sets the selected index to that specified.
     */
    public void setSelectedIndex(int index) {

        if (index == -1)
            return;

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
     * Checks whether a close of the panel will not be
     * vetoed by the panel itself.
     *
     * @param tabComponent tab component to be closed
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
     * Returns the currently selected tab component
     * or null if nothing is selected.
     *
     * @return the currently selected tab component
     */
    public TabComponent getSelectedComponent() {
        return selectedIndex != -1 ? components.get(selectedIndex) : null;
    }

    protected final Insets tabInsets() {

        Insets insets = UIManager.getInsets("TabbedPane.tabInsets");
        if (insets == null)
            insets = new Insets(0, 9, 1, 9);

        return insets;
    }

    protected String bundledString(String key) {
        return Bundles.get(getClass(), key);
    }

    /**
     * Indicates a top-level focus change.
     */
    protected abstract void focusChanged();

    /**
     * Removes the tab from the panel at the specified index.
     */
    @Override
    public abstract void removeIndex(int index);

    // --- TabPane impl ---

    @Override
    public boolean isFocused() {
        return isFocusedTabPane;
    }

    @Override
    public void focusGained() {

        if (isFocusedTabPane)
            return;

        isFocusedTabPane = true;
        focusChanged();
        parent.tabPaneFocusChange(this);
        parent.setSelectedTabPane(this);
    }

    @Override
    public void focusLost() {

        if (!isFocusedTabPane)
            return;

        isFocusedTabPane = false;
        focusChanged();
    }

    @Override
    public void selectNextTab() {
        int tabCount = getTabCount();
        if (tabCount > 0)
            setSelectedIndex(selectedIndex < tabCount - 1 ? selectedIndex + 1 : 0);
    }

    @Override
    public void selectPreviousTab() {
        int tabCount = getTabCount();
        if (tabCount > 0)
            setSelectedIndex(selectedIndex > 0 ? selectedIndex - 1 : tabCount - 1);
    }

    @Override
    public void removeSelectedTab() {
        if (selectedIndex != -1)
            removeIndex(selectedIndex);
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndex;
    }

}
