/*
 * ErdBackgroundPanel.java
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

package org.executequery.gui.erd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.print.PageFormat;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.executequery.print.PrintingSupport;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1498 $
 * @date     $Date: 2015-09-18 10:16:35 +1000 (Fri, 18 Sep 2015) $
 */
public class ErdBackgroundPanel extends JComponent {
    
    /** Whether to display the grid */
    private boolean displayGrid;

    /** Whether to display the page margins */
    private boolean displayMargin;

    /** The grid image to tile */
    private Image gridImage;

    /** The background colour */
    private Color backgroundColour;

    /** The dashed stroke */
    private static BasicStroke dashedStroke;
    
    /** the grid colour */
    private Color gridColour;
    
    /** <p>Constructs a new instance with the specified
     *  <code>ErdViewerPanel</code> as the parent or controller
     *  object.
     *
     *  @param the parent controller object
     */
    public ErdBackgroundPanel(boolean displayGrid) {
        setDoubleBuffered(true);
        setDisplayGrid(displayGrid);
        
        //setToDisplayGrid(false);
        
        displayMargin = false;
        backgroundColour = UIUtils.getColour("executequery.Erd.background", Color.WHITE);
        
        float dash2[] = {10f, 3.0f};
        dashedStroke = new BasicStroke(1.0f, 0, 0, 10f, dash2, 0.0f);
        
    }
    
    /** <p>Overrides this class's <code>paintComponent</code>
     *  method to draw the grid background if this is a
     *  selected option.
     *
     *  @param the <code>Graphics</code> object
     */
    protected void paintComponent(Graphics _g) {
        
        Graphics2D g = (Graphics2D)_g;
        
        g.setColor(backgroundColour);
        g.fillRect(0, 0, getWidth(), getHeight());

        // draw the background grid
        if (displayGrid) {
            int width = getWidth();
            int height = getHeight();
            
            int xy = 0;
            int gridSize = 25;

            if (gridColour == null) {

                gridColour = UIUtils.getColour("executequery.Erd.grid", new Color(-3158040));
            }
            g.setColor(gridColour);

            // draw the horizontal grid lines
            for (int i = 0, k = height / gridSize; i < k; i++) {
                xy += gridSize;
                g.drawLine(0, xy, width, xy);
            }

            xy = 0;
            // draw the vertical grid lines
            for (int i = 0, k = width / gridSize; i < k; i++) {
                xy += gridSize;
                g.drawLine(xy, 0, xy, height);
            }

            /*
            int width = getWidth();
            int height = getHeight();
            
            int imageWidth = gridImage.getWidth(this);
            int imageHeight = gridImage.getHeight(this);
           
            for (int x = 0; x < width; x += imageWidth) {
                
                for (int y = 0; y < height; y += imageHeight) {
                    g.drawImage(gridImage, x, y, this);
                }

            }
             */

        }        
        else {
            g.setColor(backgroundColour);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        
        if (displayMargin) {
            
            PrintingSupport printingSupport = new PrintingSupport();
            
            PageFormat pageFormat = printingSupport.getPageFormat();
//            Paper paper = pageFormat.getPaper();
            
            boolean isPortrait = pageFormat.getOrientation() == PageFormat.PORTRAIT;
            
            int imageWidth = 0;
            int imageHeight = 0;
            
            if (isPortrait) {
                imageWidth = (int)(pageFormat.getImageableWidth() / ErdPrintable.PRINT_SCALE);
                imageHeight = (int)(pageFormat.getImageableHeight() / ErdPrintable.PRINT_SCALE);
            }
            else {
                imageWidth = (int)(pageFormat.getImageableHeight() / ErdPrintable.PRINT_SCALE);
                imageHeight = (int)(pageFormat.getImageableWidth() / ErdPrintable.PRINT_SCALE);
            }
            
            g.setColor(Color.GRAY);
            g.setStroke(dashedStroke);
            
            g.drawLine(imageWidth, 0, imageWidth, imageHeight);
            g.drawLine(0, imageHeight, imageWidth, imageHeight);
            
        }
        
    }
    
    public void setBackground(Color c) {
        
        if (c != null) {
            backgroundColour = c;
        } else {
            UIUtils.getColour("executequery.Erd.background", Color.WHITE);
        }
        displayGrid = false;
        gridImage = null;
    }
    
    public Color getBackground() {
        return backgroundColour;
    }
    
    public boolean shouldDisplayGrid() {
        return displayGrid;
    }
    
    public void swapBackground() {
        setDisplayGrid(!displayGrid);
    }
    
    protected void setDisplayGrid(boolean displayGrid) {
        this.displayGrid = displayGrid;
        
        if (displayGrid) {
            ImageIcon icon = new ImageIcon(ErdBackgroundPanel.class.getResource(
            "/org/executequery/images/ErdGrid.gif"));
            gridImage = icon.getImage();
        }
        
    }
    
    public void setDisplayMargin(boolean displayMargin) {
        this.displayMargin = displayMargin;
    }
    
    public boolean shouldDisplayMargin() {
        return displayMargin;
    }
    
    public void swapPageMargin() {
        displayMargin = !displayMargin;
    }
    
    public int getHeight() {
        return getPreferredSize().height;
    }
    
    public int getWidth() {
        return getPreferredSize().width;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(0, 0, getWidth(), getHeight());
    }
    
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getSize() {
        return getPreferredSize();
    }
    
    public void clean() {
        gridImage = null;
    }
    
}











