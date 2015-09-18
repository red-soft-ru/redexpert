/*
 * AbstractToolBarPanel.java
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

package org.underworldlabs.swing.toolbar;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import javax.swing.JPanel;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Base tool bar panel that paints its own gradient
 * background where the look and feel is the EQ default.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public abstract class AbstractToolBarPanel extends JPanel {
    
    /** the light gradient colour 1 */
    private Color colour1;

    /** the dark gradient colour 2 */
    private Color colour2;

    /** whether to fill a gradient background */
    private boolean fillGradient;

    /**
     * Creates a new panel with a double buffer and a flow layout.
     */
    public AbstractToolBarPanel() {
        this(true);
    }
    
    /**
     * Creates a new panel with <code>FlowLayout</code>
     * and the specified buffering strategy.
     * If <code>isDoubleBuffered</code> is true, the <code>JPanel</code>
     * will use a double buffer.
     *
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public AbstractToolBarPanel(boolean isDoubleBuffered) {
        this(new FlowLayout(), isDoubleBuffered);
    }
    
    /**
     * Create a new buffered panel with the specified layout manager
     *
     * @param layout  the LayoutManager to use
     */
    public AbstractToolBarPanel(LayoutManager layout) {
        this(layout, true);
    }
    
    /**
     * Creates a new panel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public AbstractToolBarPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        fillGradient = UIUtils.is3DLookAndFeel() || UIUtils.usingOcean();
        if (fillGradient) {
            colour1 = getBackground();
            colour2 = UIUtils.getDarker(colour1, 0.85);
        }
    }
    
    public boolean isOpaque() {
        return !fillGradient;
    }

    public void paintComponent(Graphics g) {
        if (fillGradient) {
            int width = getWidth();
            int height = getHeight();

            Graphics2D g2 = (Graphics2D)g;
            Paint originalPaint = g2.getPaint();
            GradientPaint fade = new GradientPaint(0, height, colour2,
                    0, (int)(height * 0.5), colour1);

            g2.setPaint(fade);
            g2.fillRect(0,0, width, height);

            g2.setPaint(originalPaint);
        }
        super.paintComponent(g);
    }
    
}

