/*
 * CustomKeyboardFocusManager.java
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
import java.awt.DefaultKeyboardFocusManager;
import java.awt.FocusTraversalPolicy;

import javax.swing.JComponent;
import javax.swing.JTextField;


/** <p>Custom KeyboardFocusManager to mainly control the focus of
 *  <code>JTextField<code> objects within a container. This
 *  provides the functionaliaty to select (highlight) any
 *  text already contained within the field when focus is
 *  gained through keyboard traversal (ie. TAB key).
 *
 *  @author   Takis Diakoumis
 *  @version  $Revision: 1487 $
 *  @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class CustomKeyboardFocusManager extends DefaultKeyboardFocusManager {
    
    /** <p>Focuses the Component before aComponent, typically based on a
     *  FocusTraversalPolicy.
     *
     *  @param aComponent the Component that is the basis for the focus
     *         traversal operation
     */
    public void focusPreviousComponent(Component aComponent) {

        if (aComponent == null)
            return;
       
        if (aComponent.getParent() instanceof TextFieldFocusController) {
            
            Container rootAncestor = aComponent.getFocusCycleRootAncestor();
            Component comp = aComponent;
            
            if (comp instanceof JTextField) {
                ((JTextField)comp).select(0, 0);
            }

            while (rootAncestor != null && !(rootAncestor.isShowing() &&
                    rootAncestor.isFocusable() &&
                    rootAncestor.isEnabled()) ) {
                
                comp = rootAncestor;
                rootAncestor = comp.getFocusCycleRootAncestor();
                
            }

            if (rootAncestor != null) {
                
                FocusTraversalPolicy policy =
                        rootAncestor.getFocusTraversalPolicy();
                Component _component = policy.getComponentBefore(rootAncestor, comp);
                
                if (_component == null)
                    _component = policy.getDefaultComponent(rootAncestor);
                
                if (_component != null) {
                    
                    if (_component instanceof JTextField) {
                        ((JTextField)_component).selectAll();
                    }
                    
                    ((JComponent)_component).grabFocus();
                    
                } else {
                    aComponent.transferFocusBackward();
                }
                
            }
            
        } else {
            aComponent.transferFocusBackward();
        }

    }
    
    /** <p>Focuses the Component after aComponent, typically based on a
     *  FocusTraversalPolicy.
     *
     *  @param the Component that is the basis for the focus
     *         traversal operation
     */
    public void focusNextComponent(Component aComponent) {
        
        if (aComponent == null)
            return;

        if (aComponent.getParent() instanceof TextFieldFocusController) {

            Container rootAncestor = aComponent.getFocusCycleRootAncestor();
            Component comp = aComponent;
            
            if (comp instanceof JTextField) {
                ((JTextField)comp).select(0, 0);
            }
            
            while (rootAncestor != null && !(rootAncestor.isShowing() &&
                    rootAncestor.isFocusable() &&
                    rootAncestor.isEnabled()) ) {
                comp = rootAncestor;
                rootAncestor = comp.getFocusCycleRootAncestor();
            }
            
            if (rootAncestor != null) {
                
                FocusTraversalPolicy policy =
                        rootAncestor.getFocusTraversalPolicy();
                Component _component = policy.getComponentAfter(rootAncestor, comp);
                
                if (_component == null)
                    _component = policy.getDefaultComponent(rootAncestor);
                
                if (_component != null) {
                    
                    if (_component instanceof JTextField) {
                        ((JTextField)_component).selectAll();
                    }

                    ((JComponent)_component).grabFocus();
                    
                } else {
                    aComponent.transferFocus();
                }
                
            }
            
        } else {
            aComponent.transferFocus();
        }
        
    }
    
    // --- some fiddling with the focus bits ---
    
/*
    public CustomKeyboardFocusManager() {
        super();
//        addVetoableChangeListener(new FocusVetoableChangeListener());
        addPropertyChangeListener(new FocusChangeListener());
    }

    class FocusChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            
            Component oldComp = null;
            Component newComp = null;

            Object oldObject = evt.getOldValue();
            Object newObject = evt.getNewValue();
            
            if (oldObject != null && oldObject instanceof Component) {
                oldComp = (Component)oldObject;
            }
            
            if (newObject != null && newObject instanceof Component) {
                newComp = (Component)evt.getNewValue();
            }
    
            if ("focusOwner".equals(evt.getPropertyName())) {
                if (oldComp != null) {
                System.out.println("oldComp: " + oldComp.getClass().getName());
                }
                else {
                    System.out.println("oldComp is null");
                }
                if (newComp != null) {
                System.out.println("newComp: " + newComp.getClass().getName());
                }
                else {
                    System.out.println("newComp is null");
                }

            }
        
        }

    }
    
    class FocusVetoableChangeListener implements VetoableChangeListener {
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            Component oldComp = (Component)evt.getOldValue();
            Component newComp = (Component)evt.getNewValue();

            boolean vetoFocusChange = false;
            if ("focusOwner".equals(evt.getPropertyName())) {
                if (newComp == null) {
                    vetoFocusChange = true;
                    // the newComp component will gain the focus
                } 
            }

            if (vetoFocusChange) {
                throw new PropertyVetoException("message", evt);
            }
        }
    }
    */
}



