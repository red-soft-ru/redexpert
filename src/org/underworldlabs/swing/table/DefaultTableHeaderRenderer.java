/*
 * DefaultTableHeaderRenderer.java
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

package org.underworldlabs.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import org.underworldlabs.Constants;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 * Simple header renderer with tool tip text and configurable height.
 *
 * @author   Takis Diakoumis
 */
public class DefaultTableHeaderRenderer extends DefaultTableCellRenderer { 

    /** the height of the header */
    private int height;

    /** the light gradient colour 1 */
    private Color colour1;

    /** the dark gradient colour 2 */
    private Color colour2;

    /** whether to fill a gradient background */
    private boolean fillGradient;

    /** the default height - 22 */
    protected static final int DEFAULT_HEIGHT = 24;
    
    /** Creates a new instance of DefaultTableHeaderRenderer */
    public DefaultTableHeaderRenderer() {
        this(DEFAULT_HEIGHT);
    }

    /** Creates a new instance of DefaultTableHeaderRenderer */
    public DefaultTableHeaderRenderer(int height) {
        
        this.height = height;

        setHorizontalAlignment(JLabel.CENTER);

        fillGradient = (UIUtils.is3DLookAndFeel() || UIUtils.usingOcean() || UIUtils.isNativeMacLookAndFeel());
        if (fillGradient) {
            colour1 = UIManager.getColor("Label.background");
            colour2 = UIUtils.getDarker(colour1, 0.85);
        }

    }
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        String label = null;
        if (value != null) {
            label = value.toString();
        } else {
            label = Constants.EMPTY;
        }

        setText(label);
        setToolTipText(label);

        if (!UIUtils.isNativeMacLookAndFeel()) {
        	
        	setBorder(UIManager.getBorder("TableHeader.cellBorder"));

        } else {

        	Color color = UIManager.getColor("controlShadow");
			if (column == 0) {
        		
        		setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, color));

        	} else {

        		setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, color));        		
        	}
        	
        }
        
        
        setFont(UIManager.getFont("TableHeader.font"));
        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));

        return this;
    }

    public void paintComponent(Graphics g) {
        if (fillGradient) {
            int width = getWidth();
            int height = getHeight();

            Graphics2D g2 = (Graphics2D)g;
            Paint originalPaint = g2.getPaint();
            GradientPaint fade = new GradientPaint(0, height, colour2, 0, (int)(height * 0.5), colour1);

            g2.setPaint(fade);
            g2.fillRect(0,0, width, height);

            g2.setPaint(originalPaint);
        }
        super.paintComponent(g);
    }

    public boolean isOpaque() {
        return !fillGradient;
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), height);
    }
    
    public int getHeight() {
        return height;
    }

}




