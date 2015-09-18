/*
 * FlatSplitPane.java
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import javax.swing.border.Border;

import org.underworldlabs.swing.plaf.FlatSplitPaneUI;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Simple JSplitPane with line borders.
 * This is achieved simply by 'nulling' all borders, including the divider itself.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class FlatSplitPane extends JSplitPane {
    
    private int storedDividerLocation;
    
    protected static Border lineBorder;
    
    static {
        lineBorder = BorderFactory.createLineBorder(UIManager.getColor("controlShadow"));
    }
    
    public FlatSplitPane() {
        this(JSplitPane.HORIZONTAL_SPLIT, false,
                new JButton(UIManager.getString("SplitPane.leftButtonText")),
                new JButton(UIManager.getString("SplitPane.rightButtonText")));
    }
    
    public FlatSplitPane(int newOrientation) {
        this(newOrientation, false);
    }
    
    public FlatSplitPane(int newOrientation, boolean newContinuousLayout) {
        this(newOrientation, newContinuousLayout, null, null);
    }
    
    public FlatSplitPane(int newOrientation,
                         Component newLeftComponent, Component newRightComponent) {
        this(newOrientation, false, newLeftComponent, newRightComponent);
    }
    
    public FlatSplitPane(int newOrientation, boolean newContinuousLayout,
                         Component newLeftComponent, Component newRightComponent) {
        
        super(newOrientation, false, newLeftComponent, newRightComponent);
    }

    public Border getBorder() {
        return null;
    }

    public void updateUI() {
        setUI(new FlatSplitPaneUI());
    }
    
    public void storeDividerLocation() {
        storedDividerLocation = getDividerLocation();
    }    

    public void storeDividerLocation(int location) {
        storedDividerLocation = location;
    }    

    public void restoreDividerLocation() {
        if (storedDividerLocation > 0) {
            setDividerLocation(storedDividerLocation);
        } else {
            resetToPreferredSizes();
        }
    }
    
    /*
    public void setTopComponent(Component comp) {
        setComponentBorder(comp);
        super.setTopComponent(comp);
    }
    
    public void setLeftComponent(Component comp) {
        setComponentBorder(comp);
        super.setLeftComponent(comp);
    }
    
    public void setRightComponent(Component comp) {
        setComponentBorder(comp);
        super.setRightComponent(comp);
    }
    
    public void setBottomComponent(Component comp) {
        setComponentBorder(comp);
        super.setBottomComponent(comp);
    }
    */
    protected void setComponentBorder(Component comp) {
        
        // TODO: FIX ME!!! ???

        // this is a little untidy
        
        boolean hasScroller = false;
        
        if (comp instanceof JComponent) {
            
            JComponent jComponent = (JComponent)comp;
            Component[] components = jComponent.getComponents();
            
            for (int i = 0; i < components.length; i++) {
                
                if (components[i] instanceof JScrollPane) {
                    hasScroller = true;
                    JScrollPane scroller = (JScrollPane)components[i];
                    scroller.setBorder(lineBorder);
                    break;
                }
                
            }
            
            if (!hasScroller) {
                jComponent.setBorder(lineBorder);
            }
            
        }
        
    }

    public int getStoredDividerLocation() {
        return storedDividerLocation;
    }

}














