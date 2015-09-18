/*
 * StatusBarLayout.java
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

package org.underworldlabs.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Simple horizontal layout where components are effectively set
 * to a specified size and resized optionally to fill the width
 * of a status bar within a frame, panel etc.
 * Effective when some labels within a status bar are a fixed width 
 * and where others should fill the remaining space.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class StatusBarLayout implements LayoutManager2, Serializable {
    
    /** the height of this status bar */
    private int height;
    
    /** default layout height of 20px */
    public static final int DEFAULT_HEIGHT = 20;
    
    /** The constraints/component pairs */
    private Hashtable<StatusBarLayoutConstraints,Component> componentsMap;
    
    /** The tool bar constraints */
    private Vector<StatusBarLayoutConstraints> constraintsList;

    /** the constraints comparator for ordering */
    private ConstraintsComparator comparator;
    
    /** Creates a new instance of StatusBarLayout */
    public StatusBarLayout() {
        this(DEFAULT_HEIGHT);
    }

    /** 
     * Creates a new instance of StatusBarLayout with
     * the specified height.
     * 
     * @param the height of the status bar
     */
    public StatusBarLayout(int height) {
        this.height = height;
        constraintsList = new Vector<StatusBarLayoutConstraints>();
        componentsMap = new Hashtable<StatusBarLayoutConstraints,Component>();
        comparator = new ConstraintsComparator();
    }

    /**
     * Indicates that a child has changed its layout related information,
     * and thus any cached calculations should be flushed.
     * <p>
     * This method is called by AWT when the invalidate method is called
     * on the Container.  Since the invalidate method may be called 
     * asynchronously to the event thread, this method may be called
     * asynchronously.
     *
     * @param target  the affected container
     */
    public synchronized void invalidateLayout(Container target) {}

    /**
     * Not used by this class.
     *
     * @param name the name of the component
     * @param comp the component
     */
    public void addLayoutComponent(String name, Component comp) {}

    /**
     * Not used by this class.
     *
     * @param comp the component
     */
    public void removeLayoutComponent(Component comp) {
        if (componentsMap.containsValue(comp)) {
            for (Enumeration i = componentsMap.keys(); i.hasMoreElements();) {
                Object object = i.nextElement();
                if (componentsMap.get(object) == comp) {
                    componentsMap.remove(object);
                    constraintsList.remove(object);
                    break;
                }
            }
        }
    }

    /**
     * Adds the specified component with the specified constraints
     * to the layout. Constraints must be an instance of 
     * <code>StatusBarLayoutConstraints</code>.
     *
     * @param comp the component
     * @param constraints constraints
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        //StatusBarLayoutConstraints
        if (constraints instanceof StatusBarLayoutConstraints) {
            
            StatusBarLayoutConstraints _constraints = 
                        (StatusBarLayoutConstraints)constraints;
            
            componentsMap.put(_constraints, comp);
            constraintsList.add(_constraints);
            
        }
        else if (constraints != null) {
            throw new IllegalArgumentException(
            "cannot add to layout: constraints must be a StatusBarLayoutConstraints");
        }

    }

    /**
     * Returns the preferred dimensions for this layout, given the components
     * in the specified target container.
     *
     * @param target  the container that needs to be laid out
     * @return the dimension
     */
    public Dimension preferredLayoutSize(Container target) {
        
        // calculate component layout positions
        int _width = 0;
        int _height = height;

        Rectangle[] rects = computePositions(target);
        for (int i = 0; i < rects.length; i++) {
            _width += rects[i].width;
        }

        Insets insets = target.getInsets();
        _width += (insets.left + insets.right);
        _height += (insets.top + insets.bottom);// + 1;
        
        return new Dimension(_width, _height);
    }

    /**
     * Returns the minimum dimensions needed to lay out the components
     * contained in the specified target container.
     *
     * @param target  the container that needs to be laid out 
     * @return the dimension
     */
    public Dimension minimumLayoutSize(Container target) {
        //return preferredLayoutSize(target);
        int _height = height;
        Insets insets = target.getInsets();
        _height += (insets.top + insets.bottom);
        return new Dimension(1, _height);
    }

    /**
     * Returns the maximum dimensions the target container can use
     * to lay out the components it contains.
     *
     * @param target  the container that needs to be laid out 
     * @return the dimenion
     */
    public Dimension maximumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    /**
     * Returns the alignment along the X axis for the container.
     * If the box is horizontal, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the X axis will be returned.
     *
     * @param target  the container
     * @return the alignment >= 0.0f && <= 1.0f
     */
    public synchronized float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the Y axis for the container.
     * If the box is vertical, the default
     * alignment will be returned. Otherwise, the alignment needed
     * to place the children along the Y axis will be returned.
     *
     * @param target  the container
     * @return the alignment >= 0.0f && <= 1.0f
     */
    public synchronized float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }

    /**
     * Called by the AWT <!-- XXX CHECK! --> when the specified container
     * needs to be laid out.
     *
     * @param target  the container to lay out
     *
     * @exception AWTError  if the target isn't the container specified to the
     *                      BoxLayout constructor
     */
    public void layoutContainer(Container target) {
	
        // order the indexes
        Collections.sort(constraintsList, comparator);
        
        // calculate component layout positions
        Rectangle[] rects = computePositions(target);
        
        // flush changes to the container
        for (int i = 0; i < rects.length; i++) {
            Component c = componentsMap.get(constraintsList.get(i));
            c.setBounds(rects[i]);
        }

    }

    private Rectangle[] computePositions(Container target) {
        // parent container size
        Dimension targetDim = target.getSize();

        // parent container insets
        Insets insets = target.getInsets();
        
        int resizeCount = 0;
        int totalComponentWidth = 0;
        Rectangle[] rects = new Rectangle[constraintsList.size()];
        
        for (int i = 0; i < rects.length; i++) {
            StatusBarLayoutConstraints cons = constraintsList.get(i);
            int preferredWidth = cons.getPreferredWidth();
            boolean resizeToFit = cons.isResizeable();
            
            // will need to reset x values after all 
            // widths have been calculated

            if (resizeToFit) {
                resizeCount++;
            }
            rects[i] = new Rectangle(0, insets.top, preferredWidth, height);
            totalComponentWidth += preferredWidth;
        }

        // resize components as required
        int fillWidth = 0;
        // let the resizable components fill the rest
        if (resizeCount > 0 && totalComponentWidth < targetDim.width) {
            fillWidth = ((targetDim.width - insets.left - insets.right
                    - totalComponentWidth)/resizeCount);// - 1;
        }

        int xPosn = insets.left;
        for (int i = 0; i < rects.length; i++) {
            Rectangle rect = rects[i];
            rect.x = xPosn;

            StatusBarLayoutConstraints cons = constraintsList.get(i);
            if (cons.isResizeable()) {
                rect.width = cons.getPreferredWidth() + fillWidth;
            }

            xPosn += rect.width;
        }

        return rects;
    }

    class ConstraintsComparator implements Comparator {
        
        public int compare(Object obj1, Object obj2) {

            StatusBarLayoutConstraints cons1 = (StatusBarLayoutConstraints)obj1;
            StatusBarLayoutConstraints cons2 = (StatusBarLayoutConstraints)obj2;
            return cons1.getIndex() -  cons2.getIndex();
        }
    }

}














