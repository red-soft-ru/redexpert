/*
 * DriverViewPanel.java
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

package org.executequery.gui.drivers;

import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.gui.forms.FormObjectViewContainer;
import org.executequery.localization.Bundles;

/**
 * @author Takis Diakoumis
 */
public class DriverViewPanel extends FormObjectViewContainer
        implements TabView {

    public static final String TITLE = Bundles.getCommon("drivers");
    public static final String FRAME_ICON = "DatabaseDrivers16.svg";

    /**
     * the parent panel containing the selection tree
     */
    private DriversTreePanel parent;

    /**
     * Creates a new instance of DriverViewPanel
     */
    public DriverViewPanel(DriversTreePanel parent) {
        super();
        this.parent = parent;
    }

    public void displayRootPanel() {
        DriverListPanel panel = null;
        if (!containsPanel(DriverListPanel.NAME)) {
            panel = new DriverListPanel(this);
            addToLayout(panel);
        } else {
            panel = (DriverListPanel) getFormObjectView(DriverListPanel.NAME);
        }

        setView(panel);
    }

    /**
     * Adds a new driver.
     */
    protected void addNewDriver() {
        parent.newDriver();
    }

    /**
     * Sets the selected driver tree node to the specified driver.
     *
     * @param driver - the driver to select
     */
    public void setSelectedDriver(DatabaseDriver driver) {
        parent.setSelectedDriver(driver);
    }

    /**
     * Indicates that a node name has changed and fires a call
     * to repaint the tree display.
     */
    protected void nodeNameValueChanged(DatabaseDriver driver) {
        parent.nodeNameValueChanged(driver);
    }

    public void valueChanged(DatabaseDriverNode node) {
        //DriversPanel panel = null;

        TabViewDriverPanel panel = null;

        if (!containsPanel(DriverPanel.TITLE)) {

            panel = new TabViewDriverPanel(this);
            addToLayout(panel);

        } else {

            panel = (TabViewDriverPanel) getFormObjectView(DriverPanel.TITLE);
        }

        panel.setDriver(node.getDriver());
        setView(panel);
    }

    protected boolean saveDrivers() {

        if (containsPanel(DriverPanel.TITLE)) {

            return tabViewDriverPanel().saveDrivers();
        }

        return true;
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {

        if (containsPanel(DriverPanel.TITLE)) {

            return tabViewDriverPanel().tabViewClosing();
        }

        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {

        if (containsPanel(DriverPanel.TITLE)) {

            return tabViewDriverPanel().tabViewSelected();
        }

        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {

        if (containsPanel(DriverPanel.TITLE)) {

            return tabViewDriverPanel().tabViewDeselected();
        }

        return true;
    }

    private TabViewDriverPanel tabViewDriverPanel() {

        return (TabViewDriverPanel) getFormObjectView(DriverPanel.TITLE);
    }

    // --------------------------------------------

}







