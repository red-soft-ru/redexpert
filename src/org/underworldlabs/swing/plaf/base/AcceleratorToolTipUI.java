/*
 * AcceleratorToolTipUI.java
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

package org.underworldlabs.swing.plaf.base;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicToolTipUI;

import org.apache.commons.lang.StringUtils;
import org.underworldlabs.Constants;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class AcceleratorToolTipUI extends BasicToolTipUI {

    private static final String DELIMITER = "+";

    public void paint(Graphics g, JComponent c) {

        UIUtils.antialias(g);
        
        Font font = c.getFont();
        FontMetrics metrics = c.getFontMetrics(font);

        Dimension size = c.getSize();
        if (c.isOpaque()) {
            g.setColor(c.getBackground());
            g.fillRect(0, 0, size.width + 20, size.height);
        }

        JToolTip toolTip = (JToolTip) c;
        String tipText = getTipText(toolTip);

        if (!MiscUtils.isNull(tipText)) {
            
            Insets insets = c.getInsets();
            Rectangle paintTextR = new Rectangle(
                insets.left,
                insets.top,
                size.width - (insets.left + insets.right),
                size.height - (insets.top + insets.bottom));

            Color foreground = c.getForeground();
            g.setColor(foreground);
            g.setFont(font);

            g.drawString(tipText, 
                    paintTextR.x + 3,
                    paintTextR.y + metrics.getAscent());

            String acceleratorString = getAcceleratorStringForRender(toolTip);
            if (StringUtils.isNotBlank(acceleratorString)) {
                
                Font acceleratorFont = font.deriveFont(font.getSize() - 1f);                
                g.setFont(acceleratorFont);
                g.setColor(GUIUtils.getSlightlyBrighter(foreground, 2.0f));
                
                g.drawString(acceleratorString, 
                        paintTextR.x + 6 + metrics.stringWidth(tipText),
                        paintTextR.y + metrics.getAscent());
            }
            
        }
        
    }

    public Dimension getPreferredSize(JComponent c) {

        Dimension d = super.getPreferredSize(c);

        JToolTip tip = (JToolTip) c;
        String tipText = getTipText(tip) + getAcceleratorStringForRender(tip);

        if (!MiscUtils.isNull(tipText)) {

            Font font = c.getFont();
            FontMetrics fm = c.getFontMetrics(font);	
            d.width = fm.stringWidth(tipText) + 15;
        
        } else {
            
            d.width += 10;
        }

        return d;
    }

    private String getTipText(JToolTip tip) { 
        
        String text = tip.getTipText();
        if (text == null) {
        
            text = Constants.EMPTY; 
        }

        return text;
    }
    
    private String getAcceleratorStringForRender(JToolTip tip) {
        
        String acceleratorString = getAcceleratorString(tip);
        if (StringUtils.isNotBlank(acceleratorString)) {
            
            return " (" + acceleratorString + ")";
        }

        return Constants.EMPTY;
    }
    
    private String getAcceleratorString(JToolTip tip) {
        
        String acceleratorString = null;
        Action action = ((AbstractButton)tip.getComponent()).getAction();

        if (action != null) {

            KeyStroke keyStroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
            if (keyStroke != null) {
                
                int mod = keyStroke.getModifiers();
                acceleratorString = KeyEvent.getKeyModifiersText(mod);

                if (!MiscUtils.isNull(acceleratorString)) {

                    acceleratorString += DELIMITER;
                }

                String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
                if (!MiscUtils.isNull(keyText)) {

                    acceleratorString += keyText;
                }

            }

        }

        return acceleratorString;
    }
    
}





