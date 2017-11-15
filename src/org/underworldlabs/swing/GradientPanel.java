/*
 * GradientPanel.java
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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import javax.swing.JPanel;
import javax.swing.UIManager;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Simple panel with a left-right gradient background.
 *
 * @author   Takis Diakoumis
 */
public class GradientPanel extends JPanel {
    
    /** The component's gradient colour */
    private Color gradientColor;

    /** The component's fill colour */
    private Color fillColor;

    /** Creates a new instance of GradientPanel */
    public GradientPanel() {
        super();
    }
    
    public GradientPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    public GradientPanel(LayoutManager layout) {
        super(layout);
    }
    
    public GradientPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public void setColours(Color colour1, Color colour2) {
        gradientColor = colour1;
        fillColor = colour2;
    }
    
    public boolean isOpaque() {
        return true;
    }
    
    /** 
     * Performs the painting for this component.
     *
     * @param the <code>Graphics</code> object to
     *         perform the painting
     */
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        // if we have no colours - use the InternalFrame colours

        if (gradientColor == null) {
            gradientColor = UIManager.getColor("InternalFrame.activeTitleBackground");
        }
        
        if (fillColor == null) {
            fillColor = UIManager.getColor("InternalFrame.inactiveTitleBackground");
        }
        
        int width = getWidth();
        int height = getHeight();
        
        Graphics2D g2 = (Graphics2D)g;

        Paint originalPaint = g2.getPaint();
        if (gradientColor != null && fillColor != null) {
            GradientPaint fade = new GradientPaint(0, 0, gradientColor,
                    (int)(width * 0.9), 0, fillColor);

            g2.setPaint(fade);
            g2.fillRect(0,0, width, height);
        }
        
        g2.setPaint(originalPaint);
    }

}















