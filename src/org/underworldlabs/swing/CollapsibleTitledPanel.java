/*
 * CollapsibleTitledPanel.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.underworldlabs.swing.table.ArrowIcon;

/** 
 * Panel container with a titled border that may be 'collapsed'
 * and 'expanded' to show/hide the panels contents.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class CollapsibleTitledPanel extends JPanel 
                                    implements ActionListener  {
    
    /** the panel border title */
    private String title;
    
    /** selection button */
    private JButton button;
    
    /** indicates the collapsed state */
    private boolean collapsed;
    
    /** the collapsed border */
    private CollapsedTitleBorder border;
    
    /** the content panel */
    private JPanel panel;

    /** the normal expanded icon */
    private Icon normalIcon;
    
    /** collapsed icon */
    private Icon collapsedIcon;
    
    /** Creates a new instance of CollapsibleTitledPanel */
    public CollapsibleTitledPanel(String title) {
        super(new BorderLayout());

        button = new BlankButton(title);
        border = new CollapsedTitleBorder(button);
        setBorder(border);
        panel = new JPanel();

        add(button);
        add(panel);

        this.title = title;
        button.setIcon(getNormalIcon());
        button.addActionListener(this);
    }

    public void setTitle(String title) {
        this.title = title;
        button.setText(title);
    }

    public String getTitle() {
        return title;
    }

    public JPanel getContentPane() {
        return panel;
    }
    
    public void doLayout() {
        Insets insets = getInsets();
        Rectangle rect = getBounds();
        
        rect.x = 0;
        rect.y = 0;

        Rectangle compR = border.getComponentRect(rect,insets);
        button.setBounds(compR);

        rect.x += insets.left;
        rect.y += insets.top;
        rect.width  -= insets.left + insets.right;
        rect.height -= insets.top  + insets.bottom;
        panel.setBounds(rect);
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        stateChanged();
    }

    protected void stateChanged() {
        if (isCollapsed()) {
            button.setIcon(getCollapsedIcon());
            getContentPane().setVisible(false);
        } else {
            button.setIcon(getNormalIcon());
            getContentPane().setVisible(true);
        }
    }

    public void actionPerformed(ActionEvent e) {
        setCollapsed(!isCollapsed());
    }
    
    protected Icon getNormalIcon() {
        if (normalIcon == null) {
            normalIcon = new ArrowIcon(getForeground(), ArrowIcon.DOWN, 12);
        }
        return normalIcon;
    }

    protected Icon getCollapsedIcon() {
        if (collapsedIcon == null) {
            collapsedIcon = new ArrowIcon(getForeground(), ArrowIcon.RIGHT, 12);
        }
        return collapsedIcon;
    }

    // border drawing just the top line when collapsed
    class CollapsedTitleBorder extends ComponentTitledBorder {
        
        public CollapsedTitleBorder(JComponent component) {
            super(component);
        }
        
        public void paintBorder(Component c, Graphics g,
                                int x, int y, int width, int height) {

            if (!isCollapsed()) {
                super.paintBorder(c, g, x, y, width, height);
                return;
            }
            
            Rectangle borderR = new Rectangle(x +  EDGE_SPACING,
                                              y +  EDGE_SPACING,
                                              width - (EDGE_SPACING * 2),
                                              height - (EDGE_SPACING * 2));
            Insets borderInsets;
            if (border != null) {
                borderInsets = border.getBorderInsets(c);
            } else {
                borderInsets = new Insets(0, 0, 0, 0);
            }

            Rectangle rect = new Rectangle(x, y, width, height);
            Insets insets = getBorderInsets(c);
            Rectangle compR = getComponentRect(rect, insets);

            int diff = insets.top/2 - borderInsets.top - EDGE_SPACING;
            borderR.y += diff;
            borderR.height -= diff;

            border.paintBorder(c, g, borderR.x, borderR.y,
                               borderR.width, 1);

            Color col = g.getColor();
            g.setColor(c.getBackground());
            g.fillRect(compR.x - 2, compR.y, compR.width + 4, compR.height);
            g.setColor(col);

            if (component != null) {
                component.repaint();
            }
        }

        
    } // class CollapsedTitleBorder

    
    // Simple borderless blank button
    class BlankButton extends JButton {
        public BlankButton(String text) {
            super(text);
            setMargin(new Insets(0,0,0,0));
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(true);
            try {
                setUI(new javax.swing.plaf.basic.BasicButtonUI());
            } catch (NullPointerException nullExc) {}
        }
    } // class BlankButton

    
}











