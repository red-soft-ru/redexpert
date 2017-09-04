/*
 * ErdPopupMenu.java
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

package org.executequery.gui.erd;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.util.MenuBuilder;

public class ErdPopupMenu extends JPopupMenu implements ActionListener {

    private ErdViewerPanel parent;
    
    private JCheckBoxMenuItem[] scaleChecks;
    private JCheckBoxMenuItem gridCheck;

    private JMenu viewMenu;
    
    public ErdPopupMenu(ErdViewerPanel parent) {
        
        this.parent = parent;

        MenuBuilder builder = new MenuBuilder();
        
        JMenu newMenu = MenuItemFactory.createMenu("New");
        JMenuItem newTable = builder.createMenuItem(newMenu, "Database Table",
                MenuBuilder.ITEM_PLAIN, "Create a new database table");
        JMenuItem newRelation = builder.createMenuItem(newMenu, "Relationship",
                MenuBuilder.ITEM_PLAIN, "Create a new table relationship");
        
        JMenuItem fontProperties = MenuItemFactory.createMenuItem("Font Style");
        JMenuItem lineProperties = MenuItemFactory.createMenuItem("Line Style");
        
        viewMenu = MenuItemFactory.createMenu("View");
        
        JMenuItem zoomIn = builder.createMenuItem(viewMenu, "Zoom In",
                MenuBuilder.ITEM_PLAIN, null);
        JMenuItem zoomOut = builder.createMenuItem(viewMenu, "Zoom Out",
                MenuBuilder.ITEM_PLAIN, null);
        viewMenu.addSeparator();

        JMenuItem reset = builder.createMenuItem(viewMenu, "Layout",
                MenuBuilder.ITEM_PLAIN, null);
        viewMenu.addSeparator();
        
        ButtonGroup bg = new ButtonGroup();
        String[] scaleValues = ErdViewerPanel.scaleValues;
        scaleChecks = new JCheckBoxMenuItem[scaleValues.length];
        
        String defaultZoom = "75%";
        
        for (int i = 0; i < scaleValues.length; i++) {
            scaleChecks[i] = MenuItemFactory.createCheckBoxMenuItem(scaleValues[i]);
            viewMenu.add(scaleChecks[i]);
            if (scaleValues[i].equals(defaultZoom)) {
                scaleChecks[i].setSelected(true);
            }                
            scaleChecks[i].addActionListener(this);
            bg.add(scaleChecks[i]);
        }

        gridCheck = new JCheckBoxMenuItem("Display grid", parent.shouldDisplayGrid());
        
        JCheckBoxMenuItem marginCheck = MenuItemFactory.createCheckBoxMenuItem(
                                                    "Display page margin",
                                                    parent.shouldDisplayMargin());
        JCheckBoxMenuItem displayColumnsCheck = MenuItemFactory.createCheckBoxMenuItem(
                                                    "Display referenced keys only", true);
        
        viewMenu.addSeparator();
        viewMenu.add(displayColumnsCheck);
        viewMenu.add(gridCheck);
        viewMenu.add(marginCheck);
        
        displayColumnsCheck.addActionListener(this);
        marginCheck.addActionListener(this);
        gridCheck.addActionListener(this);
        zoomIn.addActionListener(this);
        zoomOut.addActionListener(this);
        reset.addActionListener(this);
        newTable.addActionListener(this);
        newRelation.addActionListener(this);
        fontProperties.addActionListener(this);
        lineProperties.addActionListener(this);
        
        JMenuItem help = MenuItemFactory.createMenuItem(ActionBuilder.get("help-command"));
        help.setIcon(null);
        help.setActionCommand("erd");
        help.setText(Bundles.get("common.help.button"));
        
        add(newMenu);
        addSeparator();
        add(fontProperties);
        add(lineProperties);
        addSeparator();
        add(viewMenu);
        addSeparator();
        add(help);
        
    }
    
    protected void displayViewItemsOnly() {
        
        removeAll();
        Component[] components = viewMenu.getMenuComponents();
        for (Component component : components) {

            add(component);
        }
        
    }
    
    public void setGridDisplayed(boolean display) {
        gridCheck.setSelected(display);
    }
    
    public void setMenuScaleSelection(int index) {
        scaleChecks[index].setSelected(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        //Log.debug(command);
        
        if (command.equals("Font Style")) {
            parent.showFontStyleDialog();
        }
        else if (command.equals("Line Style")) {
            parent.showLineStyleDialog();
        }
        else if (command.equals("Database Table")) {
            new ErdNewTableDialog(parent);
        }
        else if (command.equals("Relationship")) {
            
            if (parent.getAllComponentsVector().size() <= 1) {
                GUIUtilities.displayErrorMessage(
                     "You need at least 2 tables to create a relationship");
                return;
            }

            new ErdNewRelationshipDialog(parent);
            
        }            
        else if (command.endsWith("%")) {
            String scaleString = command.substring(0,command.indexOf("%"));
            double scale = Double.parseDouble(scaleString) / 100;
            parent.setScaledView(scale);
            parent.setScaleComboValue(command);
        }
        else if (command.equals("Zoom In")) {
            parent.zoom(true);
        }
        else if (command.equals("Zoom Out")) {
            parent.zoom(false);
        }
        else if (command.equals("Layout")) {
            parent.reset();
        }
        else if (command.equals("Display grid")) {
            parent.swapCanvasBackground();
        }
        else if (command.equals("Display page margin")) {
            parent.swapPageMargin();
        }
        else if (command.equals("Display referenced keys only")) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
            parent.setDisplayKeysOnly(item.isSelected());
        }

    }
    
    public void removeAll() {
        scaleChecks = null;
        super.removeAll();
    }

}


