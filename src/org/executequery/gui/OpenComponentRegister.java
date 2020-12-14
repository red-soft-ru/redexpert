/*
 * OpenComponentRegister.java
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

package org.executequery.gui;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DockedTabEvent;
import org.executequery.base.DockedTabListener;
import org.executequery.base.DockedTabView;
import org.executequery.base.TabComponent;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.menu.MainCheckBoxMenuItem;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a register of open central tab panels
 * and dialogs for quick determination of what is/isn't open,
 * how many etc...
 *
 * @author Takis Diakoumis
 */
public class OpenComponentRegister implements DockedTabListener {

    /**
     * open dialog cache
     */
    private List<JDialog> openDialogs;

    /**
     * open main panel cache
     */
    private List<ComponentPanel> componentPanels;

    /**
     * the currently selected main component
     */
    private Component selectedComponent;

    /**
     * Ensures the dialog cache is created.
     */
    private List<JDialog> openDialogs() {

        if (openDialogs == null) {

            openDialogs = new ArrayList<JDialog>();
        }
        return openDialogs;
    }

    /**
     * Ensures the panel cache is created.
     */
    private List<ComponentPanel> componentPanels() {

        if (componentPanels == null) {

            componentPanels = new ArrayList<ComponentPanel>();
        }
        return componentPanels;
    }

    /**
     * Adds the specified component to the open panel cache.
     *
     * @param the dialog to be added
     */
    public void addOpenPanel(String name, Component component) {
        componentPanels().add(new ComponentPanel(name, component));
    }

    /**
     * Returns the open panel count as registered with this object .
     */
    public int getOpenPanelCount() {
        return (componentPanels == null) ? 0 : componentPanels.size();
    }

    /**
     * Returns the open panels registered with this object
     * as a list of <code>PanelCacheObject</code>s.
     *
     * @return the list of <code>PanelCacheObject</code>s
     */
    public List<ComponentPanel> getOpenPanels() {
        return componentPanels;
    }

    /**
     * Returns the open dialog count as registered with this object .
     */
    public int getOpenDialogCount() {
        return (openDialogs == null) ? 0 : openDialogs.size();
    }

    /**
     * Returns the open dialogs registered with this object
     * as a list of <code>JDialog</code>s.
     *
     * @return the list of <code>JDialog</code>s
     */
    public List<JDialog> getOpenDialogs() {
        return openDialogs;
    }

    /**
     * Returns whether the panel with the specified
     * name is open - within the opne panel cache.
     *
     * @param the name of the panel
     * @return true | false
     */
    public boolean isPanelOpen(String name) {

        if (componentPanels == null || componentPanels.isEmpty()) {

            return false;
        }

        for (ComponentPanel object : componentPanels) {

            if (object.getName().startsWith(name)) {

                return true;
            }

        }

        return false;
    }

    /**
     * Returns the open panel component with the specified name.
     *
     * @param the name to search for
     * @return the component, or null if not found
     */
    public Component getOpenPanel(String name) {

        if (componentPanels == null || componentPanels.isEmpty()) {

            return null;
        }

        for (ComponentPanel object : componentPanels) {

            if (object.getName().startsWith(name)) {

                return object.getComponent();
            }

        }
        return null;
    }

    /**
     * Returns whether the dialog with the specified
     * name is open - within the open dialog cache.
     *
     * @param the name of the dialog
     * @return true | false
     */
    public boolean isDialogOpen(String name) {

        if (openDialogs == null || openDialogs.isEmpty()) {

            return false;
        }

        for (int i = 0, k = openDialogs.size(); i < k; i++) {

            JDialog dialog = openDialogs.get(i);

            if (dialog.getTitle().startsWith(name)) {

                return true;
            }

        }

        return false;
    }

    /**
     * Returns whether the dialog with the specified
     * name is open - within the open dialog cache.
     *
     * @param the name of the dialog
     * @return true | false
     */
    public JDialog getOpenDialog(String name) {

        if (openDialogs == null || openDialogs.isEmpty()) {

            return null;
        }

        for (int i = 0, k = openDialogs.size(); i < k; i++) {

            JDialog dialog = openDialogs.get(i);

            if (dialog.getTitle().startsWith(name)) {

                return dialog;
            }

        }

        return null;
    }

    /**
     * Adds the specified dialog to the open cache.
     *
     * @param the dialog to be added
     */
    public void addDialog(JDialog dialog) {
        openDialogs().add(dialog);
    }

    /**
     * Removes the specified dialog from the open cache.
     *
     * @param the dialog to be removed
     */
    public void removeDialog(JDialog dialog) {
        if (openDialogs != null && !openDialogs.isEmpty()) {
            openDialogs.remove(dialog);
        }
    }

    /**
     * Indicates a tab minimised event.
     *
     * @param the event
     */
    public void tabMinimised(DockedTabEvent e) {
    }

    /**
     * Indicates a tab restored from minimised event.
     *
     * @param the event
     */
    public void tabRestored(DockedTabEvent e) {
    }

    /**
     * Indicates a tab selected event.
     *
     * @param the event
     */
    public void tabSelected(DockedTabEvent e) {

        TabComponent tabComponent = (TabComponent) e.getSource();

        int position = tabComponent.getPosition();

        if (position == SwingConstants.CENTER) {

            selectedComponent = tabComponent.getComponent();

        } else {

            Component component = tabComponent.getComponent();

            switch (position) {
                case SwingConstants.NORTH_WEST:
                case SwingConstants.SOUTH_WEST:
                case SwingConstants.SOUTH:
                case SwingConstants.NORTH_EAST:
                case SwingConstants.SOUTH_EAST:

                    if (component instanceof DockedTabView) {

                        String key = keyFromComponent(component);

                        markDockedComponentVisible(key, true);

                        fireComponentViewOpened(key);
                    }

                    break;
            }

        }

    }

    /**
     * Indicates a tab deselected event.
     *
     * @param the event
     */
    public void tabDeselected(DockedTabEvent e) {

        TabComponent tabComponent = (TabComponent) e.getSource();

        int position = tabComponent.getPosition();

        if (position == SwingConstants.CENTER) {

            selectedComponent = null;
        }

    }

    /**
     * Returns the selected component as registered within
     * this object cache.
     *
     * @return the selected main component or null if there is
     * no selected component
     */
    public Component getSelectedComponent() {
        return selectedComponent;
    }

    /**
     * Indicates a tab closed event.
     *
     * @param the event
     */
    public void tabClosed(DockedTabEvent e) {

        TabComponent tabComponent = (TabComponent) e.getSource();

        // retrieve the position and component
        int position = tabComponent.getPosition();
        Component component = tabComponent.getComponent();

        if (position == SwingConstants.CENTER) {

            ComponentPanel object = getPanelCacheObject(component);
            componentPanels.remove(object);
            object.clear();

            if (selectedComponent == component) {

                selectedComponent = null;
            }

        } else { // check if its docked view and reset associated items

            switch (position) {
                case SwingConstants.NORTH_WEST:
                case SwingConstants.SOUTH_WEST:
                case SwingConstants.SOUTH:
                case SwingConstants.NORTH_EAST:
                case SwingConstants.SOUTH_EAST:

                    if (component instanceof DockedTabView) {

                        String key = keyFromComponent(component);

                        markDockedComponentVisible(key, false);

                        key = keyMenuFromComponent(component);

                        markMenuItemSelected(key, false);

                        fireComponentViewClosed(key);
                    }

                    break;
            }

        }

        component = null;

        GUIUtils.scheduleGC();
    }

    private void markDockedComponentVisible(String key, boolean visible) {
        SystemProperties.setBooleanProperty(
                Constants.USER_PROPERTIES_KEY, key, visible);
    }

    private void markMenuItemSelected(String key, boolean visible) {
        JMenuItem item = GUIUtilities.getExecuteQueryMenu().getMenuMap().get(key);
        if (item instanceof MainCheckBoxMenuItem) {
            MainCheckBoxMenuItem menuItem = (MainCheckBoxMenuItem) item;
            menuItem.setSelected(visible);
        }
    }

    private String keyFromComponent(Component component) {

        return ((DockedTabView) component).getPropertyKey();
    }

    private String keyMenuFromComponent(Component component) {

        return ((DockedTabView) component).getMenuItemKey();
    }

    private void fireComponentViewOpened(final String key) {

        ThreadUtils.invokeLater(new Runnable() {

            public void run() {

                EventMediator.fireEvent(new DefaultUserPreferenceEvent(
                        this, key, UserPreferenceEvent.DOCKED_COMPONENT_OPENED));
            }

        });

    }

    private void fireComponentViewClosed(final String key) {

        ThreadUtils.invokeLater(new Runnable() {

            public void run() {

                EventMediator.fireEvent(new DefaultUserPreferenceEvent(
                        this, key, UserPreferenceEvent.DOCKED_COMPONENT_CLOSED));
            }

        });

    }

    /**
     * Returns the cache object containing the specified
     * component or null if not found.
     *
     * @param the component to search for
     * @return the cache object
     */
    private ComponentPanel getPanelCacheObject(Component component) {
        if (componentPanels == null || componentPanels.size() == 0) {
            return null;
        }
        for (int i = 0, k = componentPanels.size(); i < k; i++) {
            ComponentPanel object = componentPanels.get(i);
            if (object.getComponent() == component) {
                return object;
            }
        }
        return null;
    }

    /**
     * Returns the cache object containing the specified
     * name or null if not found.
     *
     * @param the name to search for
     * @return the cache object
     */

//    private PanelCacheObject getPanelCacheObject(String name) {
//        if (openPanels == null || openPanels.size() == 0) {
//            return null;
//        }
//        for (int i = 0, k = openPanels.size(); i < k; i++) {
//            PanelCacheObject object = openPanels.get(i);
//            if (object.getName().startsWith(name)) {
//                return object;
//            }
//        }
//        return null;
//    }

    /**
     * Extracts and returns the component of the specified
     * docked tab event.
     *
     * @param the event
     * @return the encapsulated component
     */
//    private Component getComponent(DockedTabEvent e) {
//        TabComponent source = (TabComponent)e.getSource();
//        return source.getComponent();
//    }

}











