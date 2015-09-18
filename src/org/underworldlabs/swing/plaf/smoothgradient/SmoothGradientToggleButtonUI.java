/*
 * SmoothGradientToggleButtonUI.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.text.View;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SmoothGradientToggleButtonUI extends MetalToggleButtonUI {

    private static final SmoothGradientToggleButtonUI INSTANCE =
        new SmoothGradientToggleButtonUI();
        
    /* 
     * Implementation note: The protected visibility prevents
     * the String value from being encrypted by the obfuscator.
     * An encrypted String key would break the client property lookup
     * in the #paint method below.
     */    
    protected static final String HTML_KEY = BasicHTML.propertyKey;

    private boolean borderPaintsFocus;

    public static ComponentUI createUI(JComponent b) {
        return INSTANCE;
    }

    /**
     * Installs defaults and honors the client property <code>isNarrow</code>.
     */
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        borderPaintsFocus =
            Boolean.TRUE.equals(
                UIManager.get("ToggleButton.borderPaintsFocus"));
    }


    public void update(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        if (c.isOpaque()) {
            if (isToolBarButton(b)) {
                c.setOpaque(false);
            } else if (b.isContentAreaFilled()) {
                g.setColor(c.getBackground());
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                    Rectangle r =
                        new Rectangle(
                            1,
                            1,
                            c.getWidth() - 2,
                            c.getHeight() - 1);
                    SmoothGradientUtils.add3DEffekt(g, r);

            }
        }
        paint(g, c);
    }

    /**
     * Paints the focus close to the button's border.
     */
    protected void paintFocus(
        Graphics g,
        AbstractButton b,
        Rectangle viewRect,
        Rectangle textRect,
        Rectangle iconRect) {

        if (borderPaintsFocus)
            return;

        boolean isDefault = false;
        int topLeftInset = isDefault ? 3 : 2;
        int width = b.getWidth() - 1 - topLeftInset * 2;
        int height = b.getHeight() - 1 - topLeftInset * 2;

        g.setColor(getFocusColor());
        g.drawRect(topLeftInset, topLeftInset, width - 1, height - 1);
    }

    /**
     * Unlike the BasicToggleButtonUI.paint, we don't fill the content area;
     * this has been done by the update method before.
     */
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();

        Dimension size = b.getSize();
        FontMetrics fm = g.getFontMetrics();

        Insets i = c.getInsets();

        Rectangle viewRect = new Rectangle(size);

        viewRect.x += i.left;
        viewRect.y += i.top;
        viewRect.width  -= (i.right  + viewRect.x);
        viewRect.height -= (i.bottom + viewRect.y);

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();

        Font f = c.getFont();
        g.setFont(f);

        // layout the text and icon
        String text =
            SwingUtilities.layoutCompoundLabel(
                c,
                fm,
                b.getText(),
                b.getIcon(),
                b.getVerticalAlignment(),
                b.getHorizontalAlignment(),
                b.getVerticalTextPosition(),
                b.getHorizontalTextPosition(),
                viewRect,
                iconRect,
                textRect,
                b.getText() == null ? 0 : getDefaultTextIconGap(b));
        // [Pending 1.4]: b.getIconTextGap());

        g.setColor(b.getBackground());

        if (model.isArmed() && model.isPressed() || model.isSelected())
          paintButtonPressed(g, b);

        // Paint the Icon
        if (b.getIcon() != null)
          paintIcon(g, b, iconRect);


        // Draw the Text
        if (text != null && !text.equals("")) {
            View v = (View) c.getClientProperty(HTML_KEY);
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, c, textRect, text);
            }
        }

        // draw the dashed focus line.
        if (b.isFocusPainted() && b.hasFocus()) {
            paintFocus(g, b, viewRect, textRect, iconRect);
        }
    }

    // Private Helper Code **************************************************************

    /**
     * Checks and answers if this is button is in a tool bar.
     * 
     * @param b   the button to check
     * @return true if in tool bar, false otherwise
     */
    protected boolean isToolBarButton(AbstractButton b) {
        Container parent = b.getParent();
        return parent != null
            && (parent instanceof JToolBar
                || parent.getParent() instanceof JToolBar);
    }

}















