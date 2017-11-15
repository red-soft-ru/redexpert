/*
 * SmoothGradientComboBoxButton.java
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.CellRendererPane;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

/**
 *
 * @author   Takis Diakoumis
 */
final class SmoothGradientComboBoxButton extends JButton {

    private static final int LEFT_INSET  = 2;
    private static final int RIGHT_INSET = 3;

    private final JList listBox;
    private final CellRendererPane rendererPane;

    private   JComboBox  comboBox;
    private   Icon       comboIcon;
    protected boolean   iconOnly = false;
    private   boolean   borderPaintsFocus;

    /**
     * Constructs a <code>PlasticComboBoxButton</code>.
     */
    SmoothGradientComboBoxButton(
        JComboBox comboBox,
        Icon comboIcon,
        boolean iconOnly,
        CellRendererPane rendererPane,
        JList listBox) {
        super("");
        setModel(new DefaultButtonModel() {
            public void setArmed(boolean armed) {
                super.setArmed(isPressed() || armed);
            }
        });
        this.comboBox  = comboBox;
        this.comboIcon = comboIcon;
        this.iconOnly  = iconOnly;
        this.rendererPane = rendererPane;
        this.listBox = listBox;
        setEnabled(comboBox.isEnabled());
        setRequestFocusEnabled(comboBox.isEnabled());
        setBorder(UIManager.getBorder("ComboBox.arrowButtonBorder"));
        setMargin(new Insets(0, LEFT_INSET, 0, RIGHT_INSET));
        borderPaintsFocus =
            Boolean.TRUE.equals(UIManager.get("ComboBox.borderPaintsFocus"));
    }

    public JComboBox getComboBox() {
        return comboBox;
    }
    
    public void setComboBox(JComboBox cb) {
        comboBox = cb;
    }

    public Icon getComboIcon() {
        return comboIcon;
    }
    
    public void setComboIcon(Icon i) {
        comboIcon = i;
    }

    public boolean isIconOnly() {
        return iconOnly;
    }
    
    public void setIconOnly(boolean b) {
        iconOnly = b;
    }

    public boolean isFocusTraversable() {
        return SmoothGradientLookUtils.IS_BEFORE_14 && 
            (!comboBox.isEditable()) && comboBox.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        // Set the background and foreground to the combobox colors.
        if (enabled) {
            setBackground(comboBox.getBackground());
            setForeground(comboBox.getForeground());
        } else {
            setBackground(UIManager.getColor("ComboBox.disabledBackground"));
            setForeground(UIManager.getColor("ComboBox.disabledForeground"));
        }
    }

    /**
     * Checks and answers if we should paint a pseudo 3D effect.
     */
    private boolean is3D() {
        if (SmoothGradientUtils.force3D(comboBox))
            return true;
        if (SmoothGradientUtils.forceFlat(comboBox))
            return false;
        return SmoothGradientUtils.is3D("ComboBox.");
    }

    /**
     * Paints the component; honors the 3D settings and 
     * tries to switch the renderer component to transparent.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        boolean leftToRight = SmoothGradientUtils.isLeftToRight(comboBox);

        Insets insets = getInsets();

        int width  = getWidth()  - (insets.left + insets.right);
        int height = getHeight() - (insets.top  + insets.bottom);

        if (height <= 0 || width <= 0) {
            return;
        }

        int left   = insets.left;
        int top    = insets.top;
        int right  = left + (width - 1);

        int iconWidth = 0;
        int iconLeft = (leftToRight) ? right : left;

        // Paint the icon
        if (comboIcon != null) {
            iconWidth = comboIcon.getIconWidth();
            int iconHeight = comboIcon.getIconHeight();
            int iconTop;

            if (iconOnly) {
                iconLeft = (getWidth()  - iconWidth)  / 2;
                iconTop  = (getHeight() - iconHeight) / 2;
            } else {
                if (leftToRight) {
                    iconLeft = (left + (width - 1)) - iconWidth;
                } else {
                    iconLeft = left;
                }
                iconTop = (getHeight() - iconHeight) / 2;
            }

            comboIcon.paintIcon(this, g, iconLeft, iconTop);

        }

        // Let the renderer paint
        if (!iconOnly && comboBox != null) {
            ListCellRenderer renderer = comboBox.getRenderer();
            boolean renderPressed = getModel().isPressed();
            Component c =
                renderer.getListCellRendererComponent(
                    listBox,
                    comboBox.getSelectedItem(),
                    -1,
                    renderPressed,
                    false);
            c.setFont(rendererPane.getFont());

            if (model.isArmed() && model.isPressed()) {
                if (isOpaque()) {
                    c.setBackground(UIManager.getColor("Button.select"));
                }
                c.setForeground(comboBox.getForeground());
            } else if (!comboBox.isEnabled()) {
                if (isOpaque()) {
                    c.setBackground(
                        UIManager.getColor("ComboBox.disabledBackground"));
                }
                c.setForeground(
                    UIManager.getColor("ComboBox.disabledForeground"));
            } else {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            }

            int cWidth = width - (insets.right + iconWidth);

            // Fix for 4238829: should lay out the JPanel.
            boolean shouldValidate = c instanceof JPanel;
            int x = leftToRight ? left : left + iconWidth;
            int myHeight = getHeight() - LEFT_INSET - RIGHT_INSET - 
                    (SmoothGradientLookUtils.IS_BEFORE_14 ? 2 : 1);

            if (!is3D()) {
                rendererPane.paintComponent(
                    g,
                    c,
                    this,
                    x,
                    top + 1,
                    cWidth,
                    myHeight,
                    shouldValidate);
            } else if (!(c instanceof JComponent)) {
                rendererPane.paintComponent(
                    g,
                    c,
                    this,
                    x,
                    top + 1,
                    cWidth,
                    myHeight,
                    shouldValidate);
                //LookUtils.log("Custom renderer detected: " + c);				
                //LookUtils.log("Custom renderer superclass: " + c.getClass().getSuperclass().getName());				
            } else {
                // In case, we are in 3D mode _and_ have a JComponent renderer,
                // store the opaque state, set it to transparent, paint, then restore.
                JComponent component = (JComponent) c;
                boolean hasBeenOpaque = component.isOpaque();
                component.setOpaque(false);
                rendererPane.paintComponent(
                    g,
                    c,
                    this,
                    x,
                    top + 1,
                    cWidth,
                    myHeight,
                    shouldValidate);
                component.setOpaque(hasBeenOpaque);
            }
        }
        
        if (comboIcon != null) {
            // Paint the focus
            boolean hasFocus = SmoothGradientLookUtils.IS_BEFORE_14
                                        ? hasFocus()
                                        : comboBox.hasFocus();
            if (!borderPaintsFocus && hasFocus) {
                g.setColor(SmoothGradientLookAndFeel.getFocusColor());
                int x = LEFT_INSET;
                int y = LEFT_INSET;
                int w = getWidth()  - LEFT_INSET - RIGHT_INSET;
                int h = getHeight() - LEFT_INSET - RIGHT_INSET;
                g.drawRect(x, y, w - 1, h - 1);
            }
        }

    }

}















