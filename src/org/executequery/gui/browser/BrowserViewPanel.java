/*
 * BrowserViewPanel.java
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

package org.executequery.gui.browser;
import java.awt.print.Printable;

import org.executequery.EventMediator;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.forms.FormObjectViewContainer;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.localization.Bundles;
import org.executequery.print.PrintFunction;

/**
 * Base panel for browser tree selection views.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1783 $
 * @date     $Date: 2017-09-19 00:04:44 +1000 (Tue, 19 Sep 2017) $
 */
public class BrowserViewPanel extends FormObjectViewContainer 
                              implements TabView,
                                         PrintFunction,
                                         TextEditorContainer {
    
    /** The title to be applied to the <code>JInternalFrame</code> */
    public static final String TITLE = Bundles.getCommon("database-browser");

    /** The icon to be applied to the <code>JInternalFrame</code> */
    public static final String FRAME_ICON = "DBmag16.png";
    
    /** the browser's control object */
    private BrowserController controller;

    /** Creates a new instance of DatabaseViewPanel */
    public BrowserViewPanel(BrowserController controller) {
        this.controller = controller;
    }

    /**
     * Informs any panels of a new selection being made.
     */
    protected void selectionChanging() {
        if (isEmpty()) {
            return;
        }
        if (containsPanel(HostPanel.NAME)) {
            HostPanel hostPanel = (HostPanel)getFormObjectView(HostPanel.NAME);
            hostPanel.selectionChanging();
        }        
    }
    
    /**
     * Performs the drop database object action.
     */
    public void dropSelectedObject() {
        controller.dropSelectedObject();
    }
    
    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        
        EventMediator.fireEvent(
                new DefaultConnectionRepositoryEvent(
                        this, ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) null));

        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        // update the driver list on the host panel
        if (containsPanel(HostPanel.NAME)) {
            HostPanel hostPanel = (HostPanel)getFormObjectView(HostPanel.NAME);
            hostPanel.tabViewSelected();
        }
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        if (isEmpty()) {
            return true;
        }

        if (currentView instanceof HostPanel) {
            HostPanel hostPanel = (HostPanel)getFormObjectView(HostPanel.NAME);
            return hostPanel.tabViewDeselected();
        }
        return true;
    }

    // --------------------------------------------
    
    protected BrowserTableEditingPanel getEditingPanel() {
        BrowserTableEditingPanel panel = null;
        if (!containsPanel(BrowserTableEditingPanel.NAME)) {
            panel = new BrowserTableEditingPanel(controller);
            addToLayout(panel);
        } 
        else {
            panel = (BrowserTableEditingPanel)
                getFormObjectView(BrowserTableEditingPanel.NAME);
        }
        return panel;
    }

    public void displayConnectionList() {
        displayConnectionList(null);
    }

    public void displayConnectionList(ConnectionsFolder folder) {
        ConnectionsListPanel panel = null;
        if (!containsPanel(ConnectionsListPanel.NAME)) {
            panel = new ConnectionsListPanel(controller);
            addToLayout(panel);
        } 
        else {
            panel = (ConnectionsListPanel)getFormObjectView(ConnectionsListPanel.NAME);
        }
        panel.selected(folder);
        setView(panel);
    }
    
    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------
    
    /**
     * Returns the SQL text pane as the TextEditor component 
     * that this container holds.
     */
    public TextEditor getTextEditor() {
        if (currentView instanceof BrowserTableEditingPanel) {
            return ((BrowserTableEditingPanel)currentView).getFocusedTextEditor();
        }
        return null;
    }

    // --------------------------------------------------
    // PrintFunction implementation
    // --------------------------------------------------

    /** 
     * The name for this print job.
     *
     * @return the print job's name
     */
    public String getPrintJobName() {
        return bundleString("JobName");
    }
    
    /** 
     * Returns whether the current browser panel has a printable.
     *
     *  @return true | false
     */
    public boolean canPrint() {
        return getPrintable() != null;
    }
    
    /** 
     * Returns the <code>Printable</code> object. 
     */
    public Printable getPrintable() {
        if (currentView != null) {
            return currentView.getPrintable();
        } else {
            return null;
        }        
    }

    // --------------------------------------------------
    
}


