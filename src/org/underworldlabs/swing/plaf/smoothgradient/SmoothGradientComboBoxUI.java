/*
 * SmoothGradientComboBoxUI.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class SmoothGradientComboBoxUI extends MetalComboBoxUI {

    public static ComponentUI createUI(JComponent b) {
        return new SmoothGradientComboBoxUI();
    }

    /**
     * Creates the editor that is to be used in editable combo boxes. 
     * This method only gets called if a custom editor has not already 
     * been installed in the JComboBox.
     */
    protected ComboBoxEditor createEditor() {
        return new SmoothGradientComboBoxEditor.UIResource();
    }

    protected ComboPopup createPopup() {
        return new PolishedComboPopup(comboBox);
    }

    /**
     * Overriden to correct the combobox height.
     */
    public Dimension getMinimumSize(JComponent c) {
        if (!isMinimumSizeDirty) {
            return new Dimension(cachedMinimumSize);
        }

        Dimension size = null;
        
        if (!comboBox.isEditable()
            && arrowButton != null
            && arrowButton instanceof SmoothGradientComboBoxButton) {

            SmoothGradientComboBoxButton button =
                (SmoothGradientComboBoxButton) arrowButton;
            Insets buttonInsets = button.getInsets();
            Insets insets = comboBox.getInsets();

            size = getDisplaySize();

            /*
             * The next line will lead to good results if used with standard renderers;
             * In case, a custom renderer is used, it may use a different height, 
             * and we can't help much.
             */
            size.height += 2;

            size.width  += insets.left + insets.right;
            size.width  += buttonInsets.left  + buttonInsets.right;
            size.width  += buttonInsets.right + button.getComboIcon().getIconWidth();
            size.height += insets.top + insets.bottom;
            size.height += buttonInsets.top + buttonInsets.bottom;

        } else if (
            comboBox.isEditable() && arrowButton != null && editor != null) {

            // Includes the text editor border and inner margin
            size = getDisplaySize();

            // Since the button is positioned besides the editor,
            // do not add the buttons margin to the height.

            Insets insets = comboBox.getInsets();
            size.height += insets.top + insets.bottom;
        } else {
            size = super.getMinimumSize(c);
        }

        cachedMinimumSize.setSize(size.width, size.height);
        isMinimumSizeDirty = false;

        return new Dimension(cachedMinimumSize);
    }

    /**
     * Creates and answers the arrow button that is to be used in the combo box.<p>  
     * 
     * Overridden to use a button that can have a pseudo 3D effect.
     */
    protected JButton createArrowButton() {
        return new SmoothGradientComboBoxButton(
            comboBox,
            SmoothGradientIconFactory.getComboBoxButtonIcon(),
            comboBox.isEditable(),
            currentValuePane,
            listBox);
    }

    /**
     * Creates a layout manager for managing the components which 
     * make up the combo box.<p>
     * 
     * Overriden to use a layout that has a fixed width arrow button.
     * 
     * @return an instance of a layout manager
     */
    protected LayoutManager createLayoutManager() {
        return new PolishedComboBoxLayoutManager();
    }

    /**
     * This layout manager handles the 'standard' layout of combo boxes.  
     * It puts the arrow button to the right and the editor to the left.
     * If there is no editor it still keeps the arrow button to the right.
     * 
     * Overriden to use a fixed arrow button width. 
     */
    private class PolishedComboBoxLayoutManager
        extends MetalComboBoxUI.MetalComboBoxLayoutManager {

        public void layoutContainer(Container parent) {
            JComboBox cb = (JComboBox) parent;

            // Use superclass behavior if the combobox is not editable.
            if (!cb.isEditable()) {
                super.layoutContainer(parent);
                return;
            }

            int width  = cb.getWidth();
            int height = cb.getHeight();

            Insets insets = getInsets();
            int buttonWidth = UIManager.getInt("ScrollBar.width");
            int buttonHeight = height - (insets.top + insets.bottom);

            if (arrowButton != null) {
                if (cb.getComponentOrientation().isLeftToRight()) {
                    arrowButton.setBounds(
                        width - (insets.right + buttonWidth),
                        insets.top,
                        buttonWidth,
                        buttonHeight);
                } else {
                    arrowButton.setBounds(
                        insets.left,
                        insets.top,
                        buttonWidth,
                        buttonHeight);
                }
            }
            if (editor != null) {
                editor.setBounds(rectangleForCurrentValue());
            }
        }
    }

    // Required if we have a combobox button that does not extend MetalComboBoxButton
    public PropertyChangeListener createPropertyChangeListener() {
        return new PolishedPropertyChangeListener();
    }

    // Overriden to use PlasticComboBoxButton instead of a MetalComboBoxButton.
    // Required if we have a combobox button that does not extend MetalComboBoxButton
    private class PolishedPropertyChangeListener
        extends BasicComboBoxUI.PropertyChangeHandler {

        public void propertyChange(PropertyChangeEvent e) {
            super.propertyChange(e);
            String propertyName = e.getPropertyName();

            if (propertyName.equals("editable")) {
                SmoothGradientComboBoxButton button =
                    (SmoothGradientComboBoxButton) arrowButton;
                button.setIconOnly(comboBox.isEditable());
                comboBox.repaint();
            } else if (propertyName.equals("background")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setBackground(color);
                listBox.setBackground(color);

            } else if (propertyName.equals("foreground")) {
                Color color = (Color) e.getNewValue();
                arrowButton.setForeground(color);
                listBox.setForeground(color);
            }
        }
    }

    // Differs from the MetalComboPopup in that it uses the standard popmenu border.
    private class PolishedComboPopup extends MetalComboPopup {

        private PolishedComboPopup(JComboBox combo) {
            super(combo);
        }

        /**
         * Configures the list created by #createList().
         */
        protected void configureList() {
            super.configureList();
            list.setForeground(UIManager.getColor("MenuItem.foreground"));
            list.setBackground(UIManager.getColor("MenuItem.background"));
        }

        /**
         * Configures the JScrollPane created by #createScroller().
         */
        protected void configureScroller() {
            super.configureScroller();
            scroller.getVerticalScrollBar().putClientProperty(
                MetalScrollBarUI.FREE_STANDING_PROP,
                Boolean.FALSE);
        }

    }

}















