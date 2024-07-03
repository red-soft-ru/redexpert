/*
 * ErdLayeredPane.java
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

import org.executequery.GUIUtilities;
import org.executequery.components.OutlineDragPanel;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings({"rawtypes"})
public class ErdLayeredPane extends JLayeredPane
        implements MouseListener,
        MouseMotionListener,
        MouseWheelListener {

    /**
     * The controller for the ERD viewer
     */
    private final ErdViewerPanel parent;

    /**
     * The popup menu
     */
    private final ErdPopupMenu popup;

    /**
     * The currently selected component
     */
    private static ErdMoveableComponent selectedComponent;
    private static ErdMoveableComponent changedSizeComponent;
    private final OutlineDragPanel selectionDrawing;
    private static int changedSizeCursor;

    /** The title panel */
    //  private ErdTitle titlePanel;

    /**
     * The display scale factor
     */
    private double scale = 1.0;

    public ErdLayeredPane(ErdViewerPanel parent) {
        this.parent = parent;
        popup = new ErdPopupMenu(parent);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        selectionDrawing = new OutlineDragPanel(new Rectangle(0, 0, 1, 1), BorderFactory.createLineBorder(Color.BLUE, 2));
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

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

    /**
     * <p>Sets the specified component as the in-focus component
     * and applies a focus border on the table. If the CTRL key
     * is specified as held down, any tables that currently have
     * the focus keep their focus. This allows for mutliple table
     * selection/deselection as required.
     *
     * @param the               table to set in-focus
     * @param <code>true</code> if the CTRL key is down -
     *                          <code>false</code> otherwise
     */
    protected void setFocusComponent(ErdMoveableComponent component, boolean ctrlDown) {

        if (ctrlDown) {
            boolean currentFocus = component.isSelected();
            component.setSelected(!currentFocus);
        } else {
            removeFocusBorders();
            component.setSelected(true);
        }

        selectedComponent = component;

    }

    /**
     * <p>Removes the focus border from all tables
     * if they are currently in focus.
     */
    protected void removeFocusBorders() {
        // check the tables
        Vector tables = parent.getAllComponentsVector();
        ErdMoveableComponent component = null;

        for (int i = 0, k = tables.size(); i < k; i++) {
            component = (ErdMoveableComponent) tables.elementAt(i);

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
        return UIUtils.getColour("executequery.Erd.background", Color.WHITE);
    }

    boolean dragged = false;

    private ErdMoveableComponent getClickedComponent(MouseEvent e) {
        Vector<ErdMoveableComponent> vector = parent.getAllComponentsVector();
        ErdMoveableComponent component = null;
        ErdMoveableComponent selectedTable = null;

        boolean intersects = false;

        int index = -1;
        int lastIndex = Integer.MAX_VALUE;
        int mouseX = (int) (e.getX() / scale);
        int mouseY = (int) (e.getY() / scale);

        for (int i = 0, k = vector.size(); i < k; i++) {
            component = vector.elementAt(i);

            intersects = component.getBounds().contains(mouseX, mouseY);

            index = getIndexOf(component);

            if (intersects && index < lastIndex) {
                lastIndex = index;
                selectedTable = component;
            }

        }
        return selectedTable;
    }

    public void mouseDragged(MouseEvent e) {
        /*if (selectedComponent != null) {
            selectedComponent.dragging(e);
        }*/
        if (changedSizeComponent != null) {
            changedSizeComponent.changeSize(e, changedSizeCursor);
            dragged = true;
        } else if (!e.isControlDown() && selectionDraw) {
            int x = selectionX;
            int y = selectionY;
            int mouseX = (int) (e.getX() / scale);
            int mouseY = (int) (e.getY() / scale);
            selectionDrawing.setBounds(Math.min(x, mouseX), Math.min(y, mouseY), Math.abs(mouseX - x), Math.abs(mouseY - y));
            parent.repaintLayeredPane();
        } else if (!e.isControlDown()) {
            ErdMoveableComponent[] selectComponents = parent.getSelectedComponentsArray();
            for (ErdMoveableComponent selectComponent : selectComponents) {
                selectComponent.dragging(e);
            }
            dragged = true;
        }
    }

    // -------------------------------------------
    // ------ MouseListener implementations ------
    // -------------------------------------------

    boolean selectionDraw = false;
    int selectionX = 0;
    int selectionY = 0;

    void showSelectionArea(MouseEvent e) {
        int mouseX = (int) (e.getX() / scale);
        int mouseY = (int) (e.getY() / scale);
        selectionX = mouseX;
        selectionY = mouseY;
        selectionDrawing.setBounds(mouseX, mouseY, 1, 1);
        parent.addOutlinePanel(selectionDrawing);
        selectionDraw = true;
    }

    public void mousePressed(MouseEvent e) {
        ErdMoveableComponent clickedComponent = getClickedComponent(e);
        if (changedSizeComponent == null) {
            if (clickedComponent == null || !clickedComponent.isSelected() || e.isControlDown()) {
                determineSelectedTable(e);
                if (clickedComponent == null && !e.isControlDown()) {
                    showSelectionArea(e);
                }
                if (selectedComponent != null) {
                    selectedComponent.selected(e);
                }
            }
        }
        ErdMoveableComponent[] comps = parent.getSelectedComponentsArray();
        for (ErdMoveableComponent comp : comps)
            comp.calculateDragging(e);
    }

    private void determineChangeSizeCursor(MouseEvent e) {
        Vector<ErdMoveableComponent> vector = (Vector<ErdMoveableComponent>) parent.getSelectedComponents();
        ErdMoveableComponent component = null;
        changedSizeComponent = null;

        int mouseX = (int) (e.getX() / scale);
        int mouseY = (int) (e.getY() / scale);

        for (int i = 0, k = vector.size(); i < k; i++) {
            component = vector.elementAt(i);

            int cursor = component.checkChangeSizeCoords(mouseX, mouseY);
            if (cursor < 0)
                continue;
            GUIUtilities.showChangeSizeCursor(cursor);
            changedSizeComponent = component;
            changedSizeCursor = cursor;

        }
        if (changedSizeComponent == null)
            GUIUtilities.showNormalCursor();

    }

    private void determineSelectedTable(MouseEvent e) {
        Vector<ErdMoveableComponent> vector = parent.getAllComponentsVector();
        ErdMoveableComponent component = null;
        ErdMoveableComponent selectedTable = null;

        boolean intersects = false;
        boolean selectTable = false;

        int index = -1;
        int lastIndex = Integer.MAX_VALUE;
        int mouseX = (int) (e.getX() / scale);
        int mouseY = (int) (e.getY() / scale);

        for (int i = 0, k = vector.size(); i < k; i++) {
            component = vector.elementAt(i);

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
        } else {
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
        parent.requestFocusInWindow();

    }

    void hideSelectionDraw() {
        Vector<ErdMoveableComponent> vector = parent.getAllComponentsVector();
        for (ErdMoveableComponent emc : vector) {
            if (selectionDrawing.getBounds().intersects(emc.getBounds()))
                emc.setSelected(true);
        }
        parent.removeOutlinePanel(selectionDrawing);
        parent.repaintLayeredPane();
    }

    public void mouseReleased(MouseEvent e) {
        ErdMoveableComponent[] comps = parent.getSelectedComponentsArray();
        if (dragged) {
            parent.fireDragging();
        }
        for (ErdMoveableComponent comp : comps)
            comp.finishedDragging();
        dragged = false;
        if (selectionDraw)
            hideSelectionDraw();
        selectionDraw = false;
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
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        determineChangeSizeCursor(e);
    }
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

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isControlDown()) {
            if (e.getWheelRotation() < 0)
                parent.zoom(true);
            else if (e.getWheelRotation() > 0)
                parent.zoom(false);
        }
        int units = Math.abs(e.getUnitsToScroll()) * 3;
        Rectangle viewRect = parent.getScroll().getViewport().getViewRect();
        int max = parent.getScroll().getVerticalScrollBar().getVisibleAmount();
        if (e.isShiftDown() || max == parent.getScroll().getVerticalScrollBar().getMaximum()) {
            if (e.getWheelRotation() < 0) {
                viewRect.x -= units;
                if (viewRect.x <= parent.getScroll().getHorizontalScrollBar().getMinimum()) {
                    viewRect.x = parent.getScroll().getHorizontalScrollBar().getMinimum();
                }
            } else { // (direction > 0
                viewRect.x += units;
                if (viewRect.x >= parent.getScroll().getHorizontalScrollBar().getMaximum()) {
                    viewRect.x = parent.getScroll().getHorizontalScrollBar().getMaximum();
                }
            }
            parent.getScroll().getHorizontalScrollBar().setValue(viewRect.x);
        } else {
            if (e.getWheelRotation() < 0) {
                viewRect.y -= units;
                if (viewRect.y <= parent.getScroll().getVerticalScrollBar().getMinimum()) {
                    viewRect.y = parent.getScroll().getVerticalScrollBar().getMinimum();
                }
            } else { // (direction > 0
                viewRect.y += units;
                if (viewRect.y >= parent.getScroll().getVerticalScrollBar().getMaximum()) {
                    viewRect.y = parent.getScroll().getVerticalScrollBar().getMaximum();
                }
            }
            parent.getScroll().getVerticalScrollBar().setValue(viewRect.y);
        }
    }

}


