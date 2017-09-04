/*
 * SmoothGradientButtonUI.java
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

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class SmoothGradientButtonUI extends MetalButtonUI {

    private static final SmoothGradientButtonUI INSTANCE = new SmoothGradientButtonUI();

    private boolean borderPaintsFocus;

    public static ComponentUI createUI(JComponent b) {
        return INSTANCE;
    }

    /**
     * Installs defaults and honors the client property <code>isNarrow</code>.
     */
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
//        PolishedLookUtils.installNarrowMargin(b, getPropertyPrefix());
        borderPaintsFocus =
            Boolean.TRUE.equals(UIManager.get("Button.borderPaintsFocus"));
    }

    /**
     * Installs an extra listener for a change of the isNarrow property.
     */ /*
    public void installListeners(AbstractButton b) {
        super.installListeners(b);
        PropertyChangeListener listener =
            new ButtonMarginListener(getPropertyPrefix());
        b.putClientProperty(ButtonMarginListener.CLIENT_KEY, listener);
        b.addPropertyChangeListener(Options.IS_NARROW_KEY, listener);
    }
*/
    /**
     * Uninstalls the extra listener for a change of the isNarrow property.
     */ /*
    public void uninstallListeners(AbstractButton b) {
        super.uninstallListeners(b);
        PropertyChangeListener listener =
            (PropertyChangeListener) b.getClientProperty(
                ButtonMarginListener.CLIENT_KEY);
        b.removePropertyChangeListener(listener);
    }
*/
    // Painting ***************************************************************

    public void update(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        if (c.isOpaque()) {
            if (isToolBarButton(b)) {
                c.setOpaque(false);
            } else if (b.isContentAreaFilled()) {
                g.setColor(c.getBackground());
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                if (is3D(b)) {
                    Rectangle r =
                        new Rectangle(
                            1,
                            1,
                            c.getWidth() - 2,
                            c.getHeight() - 1);
                    SmoothGradientUtils.add3DEffekt(g, r);
                }
            }
        }
        paint(g, c);
    }

    /**
     * Paints the focus with close to the button's border.
     */
    protected void paintFocus(
        Graphics g,
        AbstractButton b,
        Rectangle viewRect,
        Rectangle textRect,
        Rectangle iconRect) {

        if (borderPaintsFocus) {
            return;
        }

        boolean isDefault =
            b instanceof JButton && ((JButton) b).isDefaultButton();
        int topLeftInset = isDefault ? 3 : 2;
        int width = b.getWidth() - 1 - topLeftInset * 2;
        int height = b.getHeight() - 1 - topLeftInset * 2;

        g.setColor(getFocusColor());
        g.drawRect(topLeftInset, topLeftInset, width - 1, height - 1);
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

    /**
     * Checks and answers if this button shall use a pseudo 3D effect
     * 
     * @param b  the button to check
     * @return true indicates a 3D effect, false flat
     */
    protected boolean is3D(AbstractButton b) {
        if (SmoothGradientUtils.force3D(b))
            return true;
        if (SmoothGradientUtils.forceFlat(b))
            return false;
        ButtonModel model = b.getModel();
        return SmoothGradientUtils.is3D("Button.")
            && b.isBorderPainted()
            && model.isEnabled()
            && !(model.isPressed() && model.isArmed())
            && !(b.getBorder() instanceof EmptyBorder);

        /*
         * Implementation note regarding the last line: instead of checking 
         * for the EmptyBorder in NetBeans, I'd prefer to just check the
         * 'borderPainted' property. I'd recommend to the NetBeans developers,
         * to switch this property on and off, instead of changing the border.
         */
    } 

}
















