/*
 * ButtonSelectionPane.java
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.event.ChangeListener;

import javax.swing.plaf.metal.MetalLookAndFeel;

import org.underworldlabs.swing.table.ArrowIcon;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ButtonSelectionPane extends JPanel
                                 implements ActionListener {
    
    /** The default button background */
    private static Color defaultColour;
    /** The button background on mouse hover */
    private static Color hoverColour;
    /** The icon for a selection */
    private static ArrowIcon selectedIcon;
    /** The default icon */
    private static ArrowIcon defaultIcon;
    
    /** Default selection model */
    private SingleSelectionModel model;
    /** The components displayed */
    private Vector componentPanels;
    /** The selection buttons */
    private Vector componentButtons;
    /** Whether the look and feel is an instance of Java look and feel */
    private boolean isJavaLookAndFeel;
    /** The background colour */
    private static Color background;
    /** The foreground colour */
    private static Color foreground;
    
    public ButtonSelectionPane(Vector componentPanels, Vector buttonLabels) {
        this();
        this.componentPanels = componentPanels;
        
        int numButtons = buttonLabels.size();
        componentButtons = new Vector(numButtons);
        
        for (int i = 0; i < numButtons; i++) {
            SelectionPaneButton button = new SelectionPaneButton(
                                                buttonLabels.elementAt(i).toString());
            componentButtons.add(button);
            button.addActionListener(this);
        }
       
    }
    
    public ButtonSelectionPane() {
        super(new GridBagLayout());
        
        // initialise the model
        model = new ButtonSelectionPaneModel();
        model.setSelectedIndex(0);
        
        // initialise the component cache
        componentButtons = new Vector();
        componentPanels = new Vector();
        
        hoverColour = UIManager.getColor("activeCaption");
        defaultColour = getBackground();
        
        defaultIcon = new ArrowIcon(Color.BLACK, ArrowIcon.RIGHT);
        selectedIcon = new ArrowIcon(Color.BLACK, ArrowIcon.DOWN);
        
        isJavaLookAndFeel = UIManager.getLookAndFeel() 
                                        instanceof MetalLookAndFeel;
        
        background = UIManager.getDefaults().getColor(
                                    "InternalFrame.borderDarkShadow");
        foreground = Color.WHITE;        
    }

    public void actionPerformed(ActionEvent e) {
        requestFocus();
        final SelectionPaneButton button = (SelectionPaneButton)e.getSource();

        if (button.isSelected()) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                button.setSelected(true);
                model.setSelectedIndex(getComponentIndex(button));
                layoutComponents();
            }
        });

    }

    public int getComponentIndex(JComponent component) {
        int index = 0;
        Vector cache = null;
        
        if (component instanceof SelectionPaneButton)
            cache = componentButtons;
        else
            cache = componentPanels;
        
        int size = cache.size();
        
        for (int i = 0; i < size; i++) {
            
            if (cache.elementAt(i) == component) {
                index = i;
                break;
            }
            
        }
        
        return index;
        
    }
    
    public void layoutComponents() {
        removeAll();
        invalidate();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        
        int total = componentPanels.size();
        int selectedIndex = model.getSelectedIndex();
        
        for (int i = 0; i < total; i++) {
            SelectionPaneButton button = (SelectionPaneButton)componentButtons.elementAt(i);
            this.add(button, gbc);
            button.setSelected(false);
            gbc.gridy++;
            
            if (i == selectedIndex) {
                button.setSelected(true);
                JComponent selectedPanel = (JComponent)componentPanels.elementAt(i);
                gbc.weighty = 1.0;
                gbc.fill = GridBagConstraints.BOTH;
                this.add(selectedPanel, gbc);
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridy++;
                gbc.weighty = 0;
            }
            
        }
        
        validate();
        repaint();
        
    }
    
    public void setSelectedIndex(int index) {
        model.setSelectedIndex(index);
    }
    
    public int getSelectedIndex() {
        return model.getSelectedIndex();
    }
    
    public void addSelectionPanel(String label, JComponent panel) {
        addSelectionPanel(label, panel, null);
    }
    
    public void addSelectionPanel(String label, JComponent panel, String toolTip) {
        SelectionPaneButton button = new SelectionPaneButton(label, toolTip);
        button.addActionListener(this);
        componentButtons.add(button);
        componentPanels.add(panel);
    }
    
    
    class SelectionPaneButton extends JButton
                              implements MouseListener {
        
        private boolean selected;
        
        public SelectionPaneButton(String label, String toolTip) {
            super(label);
            setToolTipText(toolTip);
            jbInit();
        }
        
        public SelectionPaneButton(String label) {
            super(label);
            jbInit();
        }
        
        /** <p>Initialises the state of the button. */
        private void jbInit() {
            addMouseListener(this);
            selected = false;
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        
        public Icon getIcon() {
            
            if (selected)
                return selectedIcon;
            else
                return defaultIcon;
            
        }
        
        public boolean isSelected() {
            return selected;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            super.setSelected(selected);
        }
        
        public void paintComponent(Graphics g) {
            
            super.paintComponent(g);
            
            if (isJavaLookAndFeel) {
                
                int height = getHeight();
                Shape clip = g.getClip();
                
                g.setColor(foreground);
                g.setClip(2, 2, 8, height - 6);
                
                for (int x = 3; x <= height; x += 4) {
                    
                    for (int y = 3; y <= height; y += 4) {
                        g.drawLine(x, y, x, y);
                        g.drawLine(x + 2, y + 2, x + 2, y + 2);
                    }
                    
                }
                
                g.setColor(background);
                
                for (int x = 3; x <= height; x += 4) {
                    
                    for (int y = 3; y <= height; y += 4) {
                        g.drawLine(x + 1, y + 1, x + 1, y + 1);
                        g.drawLine(x + 3, y + 3, x + 3, y + 3);
                    }
                    
                }
                
                g.setClip(clip);
                
            }
            
        }
        
        /**
         * Paints the button's borders as the mouse pointer enters.
         *
         * @param e the MouseEvent that created this event
         */
        public void mouseEntered(MouseEvent e) {
            
            if(!selected) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBackground(hoverColour);
            }
            
        }
        
        /** Override the <code>isFocusable()</code>
         *  method of <code>Component</code> (JDK1.4) to
         *  return false so the button never maintains
         *  the focus.
         *
         *  @return false
         */
        public boolean isFocusable() {
            return false;
        }
        
        /**
         * Sets the button's borders unpainted as the mouse
         * pointer exits.
         *
         * @param e the MouseEvent that created this event
         */
        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
            setBackground(defaultColour);
        }
        
        public void mouseClicked(MouseEvent e) {
            selected = true;
        }
        
        public void mouseReleased(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {}
        
    } // class SelectionPaneButton
    
    class ButtonSelectionPaneModel implements SingleSelectionModel {
        
        private int selectedIndex;
        
        public void clearSelection() {
            selectedIndex = -1;
        }
        
        public int getSelectedIndex() {
            return selectedIndex;
        }
        
        public void setSelectedIndex(int index) {
            selectedIndex = index;
        }
        
        public boolean isSelected() {
            return selectedIndex != -1;
        }
        
        public void removeChangeListener(ChangeListener listener) {}
        public void addChangeListener(ChangeListener listener) {}
        
    } // class ButtonSelectionPaneModel
    
}














