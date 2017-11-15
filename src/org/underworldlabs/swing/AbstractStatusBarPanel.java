/*
 * AbstractStatusBarPanel.java
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

package org.underworldlabs.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 *
 * @author   Takis Diakoumis
 */
public abstract class AbstractStatusBarPanel extends JPanel {
    
    /** default border colour */
    private static Color BORDER_COLOUR;
    
    /** default for for non-JLabel components */
    private static Border COMPONENT_BORDER;
    
    /** components within this status bar */
    private List<JComponent> components;
    
    protected AbstractStatusBarPanel(int height) {

        super(new StatusBarLayout(height));
    }
    
    protected void addLabel(int index, int width, boolean resizable) {

        if (components == null) {

            components = newComponentsList();
        }

        StatusBarLabel label = new StatusBarLabel(false, true, false, false);
        add(label, new StatusBarLayoutConstraints(index, width, resizable));

        components.add(index, label);
    }

    private List<JComponent> newComponentsList() {

        return new ArrayList<JComponent>();
    }

    protected void addComponent(JComponent c, int index, int width, boolean resizable) {
        
        if (components == null) {
            components = newComponentsList();
        }
        
        if (COMPONENT_BORDER == null) {
            COMPONENT_BORDER = new StatusBarComponentBorder();
        }

        c.setBorder(COMPONENT_BORDER);
        add(c, new StatusBarLayoutConstraints(index, width, resizable));
        
        components.add(index, c);
    }

    protected JLabel getLabel(int index) {

        Object object = components.get(index);

        if (object instanceof JLabel) {

            return (JLabel)object;
        }

        return null;
    }
    
    protected void setLabelText(int index, final String text) {
        
        Object object = components.get(index);
        
        if (object instanceof JLabel) {
        
            final JLabel label = (JLabel)object;
            label.setText(formatText(text));

            scheduleLabelRepaint(label);
        }

    }

    private void scheduleLabelRepaint(final JLabel label) {

        Runnable update = new Runnable() {
            public void run() {
                Dimension dim = label.getSize();
                label.paintImmediately(0, 0, dim.width, dim.height);
            }
        };
        SwingUtilities.invokeLater(update);
    }
    
    private String formatText(String text) {
        if (text != null && text.length() > 0) {
            char firstChar = text.charAt(0);
            if (!Character.isWhitespace(firstChar)) {
                return " " + text;
            }
        }
        return text;
    }

    /**
     * Returns the status bar label component border colour.
     */
    public Color getBorderColor() {
        if (BORDER_COLOUR == null) {
            BORDER_COLOUR = GUIUtils.getDefaultBorderColour();
        }
        return BORDER_COLOUR;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        int height = getHeight();
        int width = getWidth();

        Insets insets = getInsets();

        g.setColor(getBorderColor());
        g.drawRect(insets.left, 
                   insets.top,
                   width - insets.left - insets.right,
                   height - insets.top - insets.bottom - 1);
        
    }
    
}





