/*
 * ErdMoveableComponent.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.border.Border;

import org.executequery.components.OutlineDragPanel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class ErdMoveableComponent extends JComponent {
    
    /** The controller for the ERD viewer */
    protected ErdViewerPanel parent;
    
    // ----------------------------
    // --- For mouse selections ---
    // ----------------------------
    
    /** The initial x position */
    protected double xDifference;
    /** The initial x position */
    protected double yDifference;
    /** Whether a drag is in progress */
    protected boolean dragging;
    /** The outline dragging panel */
    protected OutlineDragPanel outlinePanel;
    /** Whether this table has focus */
    protected boolean selected;
    
    // ----------------------------
    
    /** The current magnification */
    protected static double scale;
    /** The table's in-focus border */
    protected static Border focusBorder;
    /** The table's focus border stroke */
    protected static BasicStroke focusBorderStroke;
    
    public ErdMoveableComponent(ErdViewerPanel parent) {
        this.parent = parent;
        scale = parent.getScaleIndex();
        focusBorder = BorderFactory.createLineBorder(Color.BLUE, 2);
        focusBorderStroke = new BasicStroke(2.0f);
    }
    
    /** <p>Sets this component as selected and having
     *  the current focus.
     *
     *  @param <code>true</code> to select |
     *         <code>false</code> otherwise
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /** <p>Returns whether this table is currently selected.
     *
     *  @return whether this table is selected
     */
    public boolean isSelected() {
        return selected;
    }
    
    /** <p>Sets the current view scale to the specified value.
     *
     *  @param the current view scale
     */
    public void setScale(double scale) {
        ErdMoveableComponent.scale = scale;
    }
    
    /** <p>Sends this component to the front over all others. */
    public void toFront() {
        if (getParent() instanceof JLayeredPane)
            ((JLayeredPane)getParent()).moveToFront(this);
    }
    
    /** <p>Indicates that this component has been deselected.
     *
     *  @param the event causing the deselection
     */
    public void deselected(MouseEvent e) {
        dragging = false;
        
        if (outlinePanel != null) {
            setBounds(outlinePanel.getBounds());
            parent.removeOutlinePanel(outlinePanel);
            parent.resizeCanvas();
            outlinePanel = null;
        }
        
    }
    
    /** <p>Indicates that this component is being dragged.
     *
     *  @param the event causing the drag
     */
    public void dragging(MouseEvent e) {
        
        if (e.isControlDown())
            return;
        
        if (dragging) {
            outlinePanel.setLocation((int)((e.getX()/scale) - xDifference + getX()),
                                     (int)((e.getY()/scale) - yDifference + getY()));
            parent.repaintLayeredPane();
        }
        
    }
    
    /** <p>Indicates that this component has been selected.
     *
     *  @param the event causing the selection
     */
    public void selected(MouseEvent e) {
        
        if (!e.isControlDown()) {
            toFront();
            outlinePanel = new OutlineDragPanel(getBounds(), focusBorder);
            parent.addOutlinePanel(outlinePanel);
        }
        
        xDifference = e.getX() / scale;
        yDifference = e.getY() / scale;
        dragging = true;
    }
    
    /** <p>Indicates that this component has been double-clicked.
     *
     *  @param the event causing the double-click
     */
    public abstract void doubleClicked(MouseEvent e);
    
}














