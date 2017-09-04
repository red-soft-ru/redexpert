/*
 * ErdScrollPane.java
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

package org.executequery.gui.erd;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.executequery.print.PrintingSupport;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ErdScrollPane extends JScrollPane {
    
    /** The controller for the ERD viewer */
    private ErdViewerPanel parent;
    /** The magnification to display with */
    private double scale = 1.0;
    
    
    /** <p>Constructs a new instance with the specified
     *  <code>ErdViewerPanel</code> as the parent or controller
     *  object.
     *
     *  @param the <code>ErdViewerPanel</code> controller object
     */
    public ErdScrollPane(ErdViewerPanel parent) {
        this.parent = parent;
        
        getViewport().setOpaque(false);
        
        // set some defaults
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // set scrollbars to scroll by 5 pixels each...
        getHorizontalScrollBar().setUnitIncrement(5);
        getVerticalScrollBar().setUnitIncrement(5);
        
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public Rectangle getDesktopRectangle() {
        return getViewport().getViewRect();
    }
    
    /** <p>Centers the viewport of the virtual desktop around the
     *  provided the table
     *
     *  @param f the internal frame to center the viewport about
     */
    public void centerView(ErdTable f) {
        
        // set the view centered around this item
        Rectangle viewP = getViewport().getViewRect();
        int xCoords = f.getX() + f.getWidth() / 2 - viewP.width / 2;
        int yCoords = f.getY() + f.getHeight() / 2 - viewP.height / 2;
        
        Dimension canvasSize = parent.getCanvasSize();
        
        if ((xCoords + viewP.width) > canvasSize.width)
            xCoords = canvasSize.width - viewP.width;
        else if (xCoords < 0)
            xCoords = 0;
        
        if ((yCoords + viewP.height) > canvasSize.height)
            yCoords=  canvasSize.height - viewP.height;
        else if (yCoords < 0)
            yCoords = 0;
        
        getViewport().setViewPosition(new Point(xCoords, yCoords));
        
    }
    
    /* <p>Resizes the virtual desktop based upon the locations of its
     *  internal frames. This updates the desktop scrollbars in real-time.
     *  Executes as an "invoked later" thread for a slight perceived
     *  performance boost.
     */

    public void resizeCanvas() {
        
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run(){
                
                // has to go through all the internal frames now and make sure none
                // off screen, and if so, add those scroll bars!
                
                Rectangle viewP = getViewport().getViewRect();
                
                double maxX = (viewP.width + viewP.x);
                double maxY = (viewP.height + viewP.y);
                double minX = viewP.x;
                double minY = viewP.y;
                
                // determine the min/max extents of all components
                ErdMoveableComponent component = null;
                ErdTable[] tables = parent.getAllComponentsArray();
                
                int x = 0, y = 0;
                int c_width = 0, c_height = 0;
                
                for (int i = 0; i < tables.length; i++) {
                    
                    component = tables[i];
                    x = component.getX();
                    y = component.getY();
                    
                    c_width = component.getWidth();
                    c_height = component.getHeight();
                    
                    if (x * scale < minX) // get minimum X
                        minX = x * scale;
                    
                    if ((x + c_width) * scale > maxX)
                        maxX = (x + c_width) * scale;
                    
                    if (y * scale < minY) // get minimum Y
                        minY = y * scale;
                    
                    if ((y + c_height) * scale > maxY)
                        maxY = (y + c_height) * scale;
                    
                }
                
                // check the title panel
                component = parent.getTitlePanel();
                
                if (component != null) {
                    
                    x = component.getX();
                    y = component.getY();
                    
                    c_width = component.getWidth();
                    c_height = component.getHeight();
                    
                    if (x * scale < minX)
                        minX = x * scale;
                    
                    if ((x + c_width) * scale > maxX)
                        maxX = (x + c_width) * scale;
                    
                    if (y * scale < minY)
                        minY = y * scale;
                    
                    if ((y + c_height) * scale > maxY)
                        maxY = (y + c_height) * scale;
                    
                }
                
                // TODO: evil hack to quick fix scroll issue 
                
                if (minY < 0) {
                    moveComponentsByOffsetY(minY - 100);
                    maxY += Math.abs(minY);
                    minY = 0;
                }

                if (minX < 0) {
                    moveComponentsByOffsetX(minX - 100);
                    maxX += Math.abs(minX);
                    minX = 0;
                }

                // -----------------------------------------
                
                // don't update the viewport while resizing/relocating
                setVisible(false);
                //          if (minX != 0 || minY != 0) {
                
/*
          for (int i=0; i < tables.length; i++) {
            t = tables[i];
            t.setLocation((int)(t.getX() - minX), (int)(t.getY() - minY));
          }
 */
                JViewport view = getViewport();
                
                //        Dimension viewDim = new Dimension((int)(maxX - minX),(int)(maxY - minY));
                
                if (parent.shouldDisplayMargin()) {
                    
                    PageFormat pageFormat = getPageFormat();
                    Paper paper = pageFormat.getPaper();
                    
                    boolean isPortrait = pageFormat.getOrientation() == PageFormat.PORTRAIT;
                    
                    int imageWidth = 0;
                    int imageHeight = 0;
                    
                    if (isPortrait) {
                        imageWidth = (int)(paper.getImageableWidth() / ErdPrintable.PRINT_SCALE);
                        imageHeight = (int)(paper.getImageableHeight() / ErdPrintable.PRINT_SCALE);
                    }
                    else {
                        imageWidth = (int)(paper.getImageableHeight() / ErdPrintable.PRINT_SCALE);
                        imageHeight = (int)(paper.getImageableWidth() / ErdPrintable.PRINT_SCALE);
                    }
                    
                    if (maxX < imageWidth)
                        maxX = imageWidth + 20;

                    if (maxY < imageHeight)
                        maxY = imageHeight + 20;

                }
                
                Dimension viewDim = new Dimension((int)maxX, (int)(maxY));
                view.setViewSize(viewDim);

//                view.setViewPosition(new Point((int)(viewP.x - minX),
//                                                     (int)(viewP.y - minY)));

                setViewport(view);
                
                // Dimension viewDim = new Dimension((int)(maxX - minX),(int)(maxY - minY));
                parent.setCanvasSize(viewDim);
                setVisible(true); // update the viewport again
                
            } // run

            private PageFormat getPageFormat() {

                PrintingSupport printingSupport = new PrintingSupport();
                
                return printingSupport.getPageFormat();
            }
        });
        
    }

    private void moveComponentsByOffsetX(double offset) {

        ErdTable[] tables = parent.getAllComponentsArray();
        
        for (int i = 0; i < tables.length; i++) {

            tables[i].setLocation(
                    (int) (tables[i].getX() - offset), tables[i].getY());
        }

    }

    private void moveComponentsByOffsetY(double offset) {
        
        ErdTable[] tables = parent.getAllComponentsArray();
        
        for (int i = 0; i < tables.length; i++) {

            tables[i].setLocation(
                    tables[i].getX(), (int) (tables[i].getY() - offset));
        }

    }
    
    
/*
  public void resizeCanvas() {
 
    SwingUtilities.invokeLater(new Runnable() {
 
      public void run(){
 
        // has to go through all the internal frames now and make sure none
        // off screen, and if so, add those scroll bars!
 
        Rectangle viewP = getViewport().getViewRect();
 
        int maxX = viewP.width + viewP.x, maxY = viewP.height + viewP.y;
        int minX = viewP.x, minY = viewP.y;
 
        // determine the min/max extents of all internal frames
 
        ErdTable f = null;
        ErdTable[] frames = parent.getAllComponentsArray();
 
          for (int i = 0; i < frames.length; i++) {
            f = frames[i];
 
            if (f.getX() < minX) // get minimum X
              minX = f.getX();
 
            if ((f.getX() + f.getWidth()) > maxX)
              maxX = f.getX() + f.getWidth();
 
            if (f.getY() < minY) // get minimum Y
                  minY = f.getY();
 
            if ((f.getY() + f.getHeight()) > maxY)
              maxY = f.getY() + f.getHeight();
 
          }
 
        setVisible(false); // don't update the viewport
                     we move everything (otherwise desktop looks 'bouncy')
 
//          if (minX != 0 || minY != 0) {
          // have to scroll it to the right or up the amount that it's off screen...
          // before scroll, move every component to the right / down by that amount
 
 
              for (int i=0; i < frames.length; i++) {
                    f = frames[i];
                    f.setLocation(f.getX() - minX, f.getY() - minY);
              }
 
            // have to scroll (set the viewport) to the right or up the amount
            // that it's off screen...
            JViewport view = getViewport();
 
            Dimension viewDim = new Dimension((int)((maxX - minX)*scale),
                                              (int)((maxY - minY)*scale));
//            view.setViewSize(viewDim);
 
            view.setViewSize(new Dimension((maxX - minX),(maxY - minY)));
            view.setViewPosition(new Point((viewP.x - minX),(viewP.y - minY)));
            setViewport(view);
 
//          }
 
        // resize the desktop
        parent.setCanvasSize(new Dimension(maxX - minX, maxY - minY));
        setVisible(true); // update the viewport again
 
      } // run
    });
 
  }
 */
    
}







