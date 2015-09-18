/*
 * ErdLayeredPane.java
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

package org.executequery.gui.erd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import javax.swing.JLayeredPane;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
@SuppressWarnings({"rawtypes"})
public class ErdLayeredPane extends JLayeredPane
                            implements MouseListener,
                                       MouseMotionListener {
    
    /** The controller for the ERD viewer */
    private ErdViewerPanel parent;
    
    /** The popup menu */
    private ErdPopupMenu popup;
    
    /** The currently selected component */
    private static ErdMoveableComponent selectedComponent;

    /** The title panel */
    //  private ErdTitle titlePanel;
    
    /** The display scale factor */
    private double scale = 1.0;
    
    public ErdLayeredPane(ErdViewerPanel parent) {
        this.parent = parent;
        popup = new ErdPopupMenu(parent);
        addMouseListener(this);
        addMouseMotionListener(this);        
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        
        if (scale != 1.0) {
/*
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
 */
            AffineTransform af = new AffineTransform();
            af.scale(scale, scale);
            g2d.transform(af);
        }
        
        super.paintComponent(g);
    }
    
    /** <p>Sets the specified component as the in-focus component
     *  and applies a focus border on the table. If the CTRL key
     *  is specified as held down, any tables that currently have
     *  the focus keep their focus. This allows for mutliple table
     *  selection/deselection as required.
     *
     *  @param the table to set in-focus
     *  @param <code>true</code> if the CTRL key is down -
     *         <code>false</code> otherwise
     */
    protected void setFocusComponent(ErdMoveableComponent component, boolean ctrlDown) {
        
        if (ctrlDown) {
            boolean currentFocus = component.isSelected();
            component.setSelected(!currentFocus);
        }
        
        else {
            removeFocusBorders();
            component.setSelected(true);
        }
        
        selectedComponent = component;
        
    }
    
    /** <p>Removes the focus border from all tables
     *  if they are currently in focus.
     */
    protected void removeFocusBorders() {
        // check the tables
        Vector tables = parent.getAllComponentsVector();
        ErdMoveableComponent component = null;
        
        for (int i = 0, k = tables.size(); i < k; i++) {
            component = (ErdTable)tables.elementAt(i);
            
            if (component.isSelected()) {
                component.setSelected(false);
                component.deselected(null);
            }
            
        }
        
        // check for a title panel
        component = parent.getTitlePanel();
        
        if (component != null) {
            
            if (component.isSelected()) {
                component.setSelected(false);
                component.deselected(null);
            }
            
        }
        repaint();
    }
    
    public double getScale() {
        return scale;
    }
    
    public boolean isOpaque() {
        return true;
    }
    
    public Color getBackground() {
        return Color.WHITE;
    }
    
    public void mouseDragged(MouseEvent e) {
        if (selectedComponent != null) {
            selectedComponent.dragging(e);
        }
    }
    
    private void determineSelectedTable(MouseEvent e) {
        Vector tables = parent.getAllComponentsVector();
        ErdMoveableComponent component = null;
        ErdMoveableComponent selectedTable = null;
        
        boolean intersects = false;
        boolean selectTable = false;
        
        int index = -1;
        int lastIndex = Integer.MAX_VALUE;
        int mouseX = (int)(e.getX() / scale);
        int mouseY = (int)(e.getY() / scale);
        
        for (int i = 0, k = tables.size(); i < k; i++) {
            component = (ErdMoveableComponent)tables.elementAt(i);
            
            intersects = component.getBounds().contains(mouseX, mouseY);
            
            index = getIndexOf(component);
            
            if (intersects && index < lastIndex) {
                lastIndex = index;
                selectedTable = component;
                selectTable = true;
            }
            
        }
        
        if (selectTable) {
            setFocusComponent(selectedTable, e.isControlDown());
        }
        else {
            intersects = false;
            removeFocusBorders();

            // check the title panel
            component = parent.getTitlePanel();
            if (component != null) {
                if (component.getBounds().contains(mouseX, mouseY)) {
                    intersects = true;
                    setFocusComponent(component, false);
                    //setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }
            }
            
            if (!intersects) {
                selectedComponent = null;
            }

        }
        
    }
    
    // -------------------------------------------
    // ------ MouseListener implementations ------
    // -------------------------------------------
    
    public void mousePressed(MouseEvent e) {
        determineSelectedTable(e);        
        if (selectedComponent != null) {
            selectedComponent.selected(e);
        }
        maybeShowPopup(e);
    }
    
    public void mouseReleased(MouseEvent e) {
        if (selectedComponent != null) {
            selectedComponent.deselected(e);
        }
        maybeShowPopup(e);
    }
    
    private void maybeShowPopup(MouseEvent e) {
//        if (!parent.isEditable()) {
//            return;
//        }
        
        // check for popup menu
        if (e.isPopupTrigger()) {
            popup.show(this, e.getX(), e.getY());
        }
    }
    
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() < 2) {
            return;
        }

        determineSelectedTable(e);
        if (selectedComponent != null) {
            selectedComponent.doubleClicked(e);
        }
    }
    
    // --------------------------------------------
    // --- Unimplemented mouse listener methods ---
    // --------------------------------------------
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {}
    // --------------------------------------------
    
    public void setMenuScaleSelection(int index) {
        popup.setMenuScaleSelection(index);
    }
    
    public void setGridDisplayed(boolean display) {
        popup.setGridDisplayed(display);
    }
    
    protected void clean() {
        popup.removeAll();
    }

    public void displayPopupMenuViewItemsOnly() {
        popup.displayViewItemsOnly();
    }

    /**
     * ERD panel popup menu.
     */
    /*
    class PopMenu extends JPopupMenu 
                  implements ActionListener {
        
        private JCheckBoxMenuItem[] scaleChecks;
        private JCheckBoxMenuItem gridCheck;
        
        public PopMenu() {
            
            MenuBuilder builder = new MenuBuilder();
            
            JMenu newMenu = MenuItemFactory.createMenu("New");
            JMenuItem newTable = builder.createMenuItem(newMenu, "Database Table",
                    MenuBuilder.ITEM_PLAIN, "Create a new database table");
            JMenuItem newRelation = builder.createMenuItem(newMenu, "Relationship",
                    MenuBuilder.ITEM_PLAIN, "Create a new table relationship");
            
            JMenuItem fontProperties = MenuItemFactory.createMenuItem("Font Style");
            JMenuItem lineProperties = MenuItemFactory.createMenuItem("Line Style");
            
            JMenu viewMenu = MenuItemFactory.createMenu("View");
            
            JMenuItem zoomIn = builder.createMenuItem(viewMenu, "Zoom In",
                    MenuBuilder.ITEM_PLAIN, null);
            JMenuItem zoomOut = builder.createMenuItem(viewMenu, "Zoom Out",
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
            newTable.addActionListener(this);
            newRelation.addActionListener(this);
            fontProperties.addActionListener(this);
            lineProperties.addActionListener(this);
            
            JMenuItem help = MenuItemFactory.createMenuItem(ActionBuilder.get("help-command"));
            help.setIcon(null);
            help.setActionCommand("erd");
            help.setText("Help");
            
            add(newMenu);
            addSeparator();
            add(fontProperties);
            add(lineProperties);
            addSeparator();
            add(viewMenu);
            addSeparator();
            add(help);
            
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
        
    } // class PopMenu
    */
    
}

