/*
 * ToolBarLayout.java
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

package org.underworldlabs.swing.toolbar;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author   Takis Diakoumis
 */
public class ToolBarLayout implements LayoutManager2 {
    
    /** The default tool bar row height */
    private static final int ROW_HEIGHT = 30;
    
    /** The bottom border offset */
    public static final int BORDER_OFFSET = 2;
    
    /** the applied row height */
    private int rowHeight = -1;
    
    /** The number of rows currently displayed */
    private int toolBarRows;
    
    /** The comparator for tool bar ordering */
    private ToolsPositionComparator comparator;
    
    /** The constraints/component pairs */
    private Hashtable componentsMap;
    
    /** The tool bar constraints */
    private ArrayList constraintsList;
    
    /** <p>Constructs a new layout instance initialised
     *  to the specified number of rows.
     *
     *  @param the initial number of rows
     */
    public ToolBarLayout(int toolBarRows) {
        constraintsList = new ArrayList();
        componentsMap = new Hashtable();
        comparator = new ToolsPositionComparator();
        this.toolBarRows = toolBarRows;
    }
    
    /** <p>Lays out the container in the specified container.
     *
     *  @param the component which needs to be laid out
     */
    public void layoutContainer(Container parent) {
        
        Dimension parentDim = parent.getSize();
        Collections.sort(constraintsList, comparator);
        
        // the x and y position
        int locX = 0, locY = 0;
        // the resize offset x position
        int resizeOffsetX = 0;
        // the last and current row number
        int previousRow = 0, currentRow = 0;
        // the widths to be set
        int width = 0, preferredWidth = 0, minWidth = 0;
        // component count
        int compCount = constraintsList.size();

        
        ToolBarConstraints constraints = null;
        Component component = null;
        
        for (int i = 0; i < compCount; i++) {
            constraints = (ToolBarConstraints)constraintsList.get(i);
            component = (Component)componentsMap.get(constraints);
            if (!component.isVisible()) {
                continue;
            }
            
            if (rowHeight <= 0) {
                rowHeight = component.getPreferredSize().height;
            }
            
            currentRow = constraints.getRow();
            if (currentRow > toolBarRows) {
                toolBarRows = currentRow;
            }
            
            minWidth = constraints.getMinimumWidth();
            preferredWidth = constraints.getPreferredWidth();
            
            // reset the x location if new row
            if (currentRow != previousRow) {
                locX = 0;
                locY = currentRow * getRowHeight();
            }
            
            // check if we stretch this tool bar to fill the width
            if (i != compCount - 1) {
                
                // check the next component
                ToolBarConstraints _constraints = (ToolBarConstraints)
                constraintsList.get(i + 1);
                resizeOffsetX = _constraints.getResizeOffsetX();
                
                // if the next component is on a new row,
                // fill the current row
                if (_constraints.getRow() == currentRow + 1) {
                    width = parentDim.width - locX;
                }
                // if the component is resizing
                else if (resizeOffsetX != -1) {
                    
                    ToolBarConstraints[] tbca = getToolBars(currentRow);
                    
                    int maxWidth = parentDim.width - locX;
                    int _width = resizeOffsetX - locX;
                    int _minWidth = 0;
                    int start = 0;
                    
                    // determine the components position in the row
                    for (int k = 0; k < tbca.length; k++) {
                        
                        if (tbca[k] == constraints) {
                            start = k + 1;
                            break;
                        }
                        
                    }
                    
                    for (int j = start; j < tbca.length; j++) {
                        
                        _minWidth = tbca[j].getMinimumWidth();
                        if (_minWidth == -1) {
                            _minWidth = ((Component)componentsMap.get(constraints)).
                                                getMinimumSize().width;
                        }
                        maxWidth -= _minWidth;
                        
                    }
                    
                    if (_width < minWidth) {
                        width = minWidth;
                    } else if (_width > maxWidth) {
                        width = maxWidth;
                    } else {
                        width = _width;
                    }

                }
                // if the component has a preferred width
                else if (preferredWidth != -1) {
                    width = preferredWidth;
                }
                // otherwise use the minimum
                else {
                    width = minWidth;
                }
                
            }
            
            // otherwise the component should fill its row
            else {
                minWidth = constraints.getMinimumWidth();
                if (minWidth == -1) {
                    minWidth = component.getMinimumSize().width;
                }
                
                width = parentDim.width - locX;                
                if (width < minWidth) {
                    width = minWidth;
                }                
                locX = parentDim.width - width;
            }

            component.setBounds(locX, locY, width, getRowHeight());
            
            // reset the component's position
            constraints.setLocX(locX);
            
            // reset values
            previousRow = currentRow;
            locX += width;
            minWidth = 0;
            resizeOffsetX = -1;
            
        }
        comparator.setFirstLayout(false);
    }
    
    /** <p>Returns all the toolbars on the specified row.
     *
     *  @parm the tool bar row
     */
    private ToolBarConstraints[] getToolBars(int row) {
        int _row = -1;
        ToolBarConstraints tbc = null;
        Vector _toolBars = new Vector();
        
        for (int i = 0, k = constraintsList.size(); i < k; i++) {
            tbc = (ToolBarConstraints)constraintsList.get(i);
            _row = tbc.getRow();
            
            if (_row > row) {
                break;
            }
            
            if (_row == row) {
                _toolBars.add(tbc);
            }
            
        }
        
        return (ToolBarConstraints[])_toolBars.toArray(new ToolBarConstraints[]{});
    }
    
    /** <p>Returns the currently displayed tool bar row count.
     *
     *  @return the current row count
     */
    public int getRowCount() {
        return toolBarRows;
    }
    
    public void setRows(int rows) {
        toolBarRows = rows;
    }
    
    /** <p>Retrieves the specified components constraints.
     *
     *  @param the component
     *  @return the component's constraints
     */
    public ToolBarConstraints getConstraint(Component comp) {
        ToolBarConstraints constraint = null;
        Enumeration consEnum = componentsMap.keys();
        
        while (consEnum.hasMoreElements()) {
            Object element = consEnum.nextElement();
            
            if (componentsMap.get(element) == comp) {
                constraint = (ToolBarConstraints)element;
                break;
            }
            
        }
        
        return constraint;
        
    }
    
    /** <p>Determines the component count for the
     *  specified row.
     *
     *  @param the row
     *  @return the component count for the row
     */
    private int getComponentCount(int row) {
        int count = 0;
        int size = constraintsList.size();
        ToolBarConstraints constraint = null;
        
        for (int i = 0; i < size; i++) {
            constraint = (ToolBarConstraints)constraintsList.get(i);
            
            if (constraint.getRow() == row) {
                Component c = (Component)componentsMap.get(constraint);
                if (c.isVisible()) {
                    count++;
                }
            }
            
        }
        
        return count;
    }
    
    private boolean maybeRemoveRow() {
        int removeRow = -1;
        
        for (int i = 0; i < toolBarRows; i++) {
            
            if (getComponentCount(i) == 0) {
                removeRow = i;
                break;
            }
            
        }
        
        if (removeRow == -1)
            return false;
        
        int currentRow = -1;
        int size = constraintsList.size();
        ToolBarConstraints constraint = null;
        
        for (int i = 0; i < size; i++) {
            constraint = (ToolBarConstraints)constraintsList.get(i);
            currentRow = constraint.getRow();
            if (constraint.getRow() > removeRow) {
                constraint.setRow(currentRow - 1);
            }
        }
        
        toolBarRows--;
        return true;
    }
    
    public int getRowHeight() {
        if (rowHeight <= 0) {
            return ROW_HEIGHT;
        }
        return rowHeight;
    }

    public boolean maybeAddRow(Component comp) {
        ToolBarConstraints constraint = getConstraint(comp);
        int currentRow = constraint.getRow();
        
        if (getComponentCount(currentRow) == 1)
            return false;
        
        int newRow = (int)Math.round((double)comp.getY() / (double)getRowHeight());
        constraint.setRow(newRow);
        
        if (newRow + 1 > toolBarRows)
            toolBarRows++;
        
        return true;
    }
    
    public void componentResized(Component comp, int locX) {
        ToolBarConstraints constraint = getConstraint(comp);
        constraint.setResizeOffsetX(locX);
        constraint.setLocX(locX);
    }
    
    public void componentMoved(Component comp, int locX, int locY) {
        ToolBarConstraints constraint = getConstraint(comp);
        
        // determine the new row (if changed)
        int currentRow = constraint.getRow();
        int newRow = (int)Math.round((double)locY / (double)getRowHeight());
        
        if (newRow != currentRow)
            constraint.setResizeOffsetX(-1);
        
        if (newRow > toolBarRows)
            newRow = toolBarRows - 1;
        
        constraint.setLocX(locX);
        constraint.setRow(newRow);
        maybeRemoveRow();
    }
    
    /** <p>Adds the specified component to the layout, using the specified
     *  constraint object.
     *
     *  @param the component to be added
     *  @param  where/how the component is added to the layout.
     */
    public void addLayoutComponent(Component comp, Object cons) {
        
        if (cons instanceof ToolBarConstraints) {
            
            ToolBarConstraints _cons = (ToolBarConstraints)((ToolBarConstraints)cons).clone();
            
            if (_cons.getMinimumWidth() == -1)
                _cons.setMinimumWidth(comp.getMinimumSize().width);
            
            componentsMap.put(_cons, comp);
            constraintsList.add(_cons);
            
        }
        
        else if (cons != null) {
            throw new IllegalArgumentException(
            "cannot add to layout: constraints must be a ToolBarConstraints");
        }
        
    }
    
    /** <p>Returns the maximum size of this component.
     *
     *  @param the target container
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    /** <p>Calculates the minimum size dimensions for the specified
     *  panel given the components in the specified parent container.
     *
     *  @param parent the component to be laid out
     *  @see #preferredLayoutSize
     */
    public Dimension minimumLayoutSize(Container parent) {
        return getLayoutSize(parent, false);
    }
    
    private Rectangle getComponentBounds(Component comp, boolean isPreferred) {
        Rectangle rectangle = comp.getBounds();
        
        if (rectangle.height == 0) {
            ToolBarConstraints constraints = getConstraint(comp);
            rectangle.height = getRowHeight();
            rectangle.y = getRowHeight() * constraints.getRow();
        }
        
        if(rectangle.width <= 0 || rectangle.height <= 0) {
            Dimension dimension = isPreferred ? comp.getPreferredSize() :
                comp.getMinimumSize();
                
                if(rectangle.width <= 0)
                    rectangle.width = dimension.width;
                if(rectangle.height <= 0)
                    rectangle.height = dimension.height;
                
        }
        
        return rectangle;
    }
    
    protected Dimension getLayoutSize(Container parent, boolean isPreferred) {
        Dimension dimension = new Dimension(0, 0);
        int i = parent.getComponentCount();
        
        for(int j = 0; j < i; j++) {
            Component component = parent.getComponent(j);
            if(component.isVisible()) {
                Rectangle rectangle = getComponentBounds(component, isPreferred);
                dimension.width = 
                        Math.max(dimension.width, rectangle.x + rectangle.width);
            }
            
        }
        
        Insets insets = parent.getInsets();
        dimension.width += insets.left + insets.right;
        dimension.height = (getRowHeight() * toolBarRows) +
                                BORDER_OFFSET + insets.top + insets.bottom;
        return dimension;
    }
    
    /** <p>Calculates the preferred size dimensions for the specified
     *  panel given the components in the specified parent container.
     *
     *  @param parent the component to be laid out
     *  @see #minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
        return getLayoutSize(parent, true);
    }
    
    /** <p>Removes the specified component from the layout.
     *
     *  @param the component to be removed
     */
    public void removeLayoutComponent(Component comp) {}
    
    /** <p>Removes all components from this layout. */
    public void removeComponents() {
        componentsMap.clear();
        constraintsList.clear();
        comparator.setFirstLayout(true);
    }
    
    /** <p>Invalidates the layout, indicating that if the layout manager
     *  has cached information it should be discarded.
     *
     *  @param the target container
     */
    public void invalidateLayout(Container target) {}
    
    /** <p>Returns the alignment along the x axis. This specifies how
     *  the component would like to be aligned relative to other
     *  components. The value should be a number between 0 and 1
     *  where 0 represents alignment along the origin, 1 is aligned
     *  the furthest away from the origin, 0.5 is centered, etc.
     *
     *  @param the target container
     */
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
    
    /** <p>Returns the alignment along the y axis.  This specifies how
     *  the component would like to be aligned relative to other
     *  components.  The value should be a number between 0 and 1
     *  where 0 represents alignment along the origin, 1 is aligned
     *  the furthest away from the origin, 0.5 is centered, etc.
     *
     *  @param the target container
     */
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }
    
    /** <p>Adds the specified component with the specified name to
     *  the layout. This does nothing in ToolBarLayout - constraints
     *  are required.
     */
    public void addLayoutComponent(String name, Component comp) {}
    
    
    // reorders the constraints depending on component placement
    class ToolsPositionComparator implements Comparator {
        
        private boolean firstLayout = true;
        
        /** <p>Compares the two objects. */
        public int compare(Object obj1, Object obj2) {
            ToolBarConstraints cons1 = (ToolBarConstraints)obj1;
            ToolBarConstraints cons2 = (ToolBarConstraints)obj2;
            
            int halfWidth = ((Component)componentsMap.get(cons1)).getWidth() / 2;
            
            if (firstLayout) {
                halfWidth = 0;
            }
            
            int firstX = cons1.getLocX() + halfWidth;
            int secondX = cons2.getLocX();
            
            int firstY = cons1.getRow();
            int secondY = cons2.getRow();
            
            if (firstX < secondX) {
                if (firstY > secondY) {
                    return 1;
                } else {
                    return -1;
                }                
            }
            else if (firstX > secondX) {
                if (firstY < secondY) {
                    return -1;
                } else {
                    return 1;
                }
            }
            else {
                return 0;
            }
        }
        
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
                
        public void setFirstLayout(boolean firstLayout) {
            this.firstLayout = firstLayout;
        }
        
    } // class ToolsPositionComparator
    
    
}















