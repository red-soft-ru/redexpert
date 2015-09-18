/*
 * ErdPrintable.java
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import org.executequery.GUIUtilities;
import org.executequery.print.PrintingSupport;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ErdPrintable implements Printable {
    
    /** The print scale transform */
    public static final double PRINT_SCALE = 0.7;
    
    /** The ERD parent panel */
    private ErdViewerPanel parent;

    /** The maximum number of pages */
    private int maxNumPages;

    /** The generated image */
    //private BufferedImage image;
    //private Image image;
    
    private ErdTable[] tablesArray;
    
    /** Constructs a new instance with the specified <code>
     *  ErdViewerPanel</code> as the parent. */
    public ErdPrintable(ErdViewerPanel parent) {
        this.parent = parent;
        maxNumPages = 1;
        
        tablesArray = parent.getAllComponentsArray();

/*
        ErdTable[] _tablesArray = parent.getAllComponentsArray();
        tablesArray = new ErdTable[_tablesArray.length];
        System.arraycopy(_tablesArray, 0, tablesArray, 0, _tablesArray.length);
*/
        //generate();
    }

//    double scale = 300.0/72.0;
/*
    private void generate() {
        
        Dimension extents = parent.getMaxImageExtents();

        int width = (int)(extents.getWidth()*scale);
        int height = (int)(extents.getHeight()*scale);


//        int width = (int)(extents.getWidth()*PRINT_SCALE);
//        int height = (int)(extents.getHeight()*PRINT_SCALE);

//        int width = (int)(extents.getWidth());
//        int height = (int)(extents.getHeight());

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Log.debug("scale: " + scale);
        Log.debug("width: " + width + " height: " + height);
        
        Graphics2D g = image.createGraphics();
        
        setRenderingHints(g);
        //g.scale(scale, scale);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        //AffineTransform af = new AffineTransform();
        //af.scale(PRINT_SCALE, PRINT_SCALE);
        //g.transform(af);

        parent.resetAllTableJoins();
        parent.getDependenciesPanel().drawDependencies(g);

        ErdTable[] _tablesArray = parent.getAllComponentsArray();
        tablesArray = _tablesArray;//new ErdTable[_tablesArray.length];
        //System.arraycopy(_tablesArray, 0, tablesArray, 0, _tablesArray.length);

        for (int i = 0; i < tablesArray.length; i++) {
            tablesArray[i].setSelected(false);
            tablesArray[i].drawTable(g, tablesArray[i].getX(), tablesArray[i].getY());
        }
        
        ErdTitlePanel title = parent.getTitlePanel();

        if (title != null) {
            title.setSelected(false);
            title.drawTitlePanel(g, title.getX(), title.getY());
        }
        
        //g.scale(scale, scale);
        //af.scale(scale, scale);
        //g.transform(af);

        
        Log.debug("_width: " + image.getWidth()+ " _height: " + image.getHeight());
        
        System.gc();
        //g.scale(PRINT_SCALE, PRINT_SCALE);
        //GUIUtilities.scheduleGC();
    }
*/
        
    public int print(Graphics _g, PageFormat format, int pageIndex)
        throws PrinterException {
        
        if (pageIndex >= maxNumPages) {// || image == null) {

            return NO_SUCH_PAGE;
        }

        //Log.debug("page index: " + pageIndex);
        
        PrintingSupport printingSupport = new PrintingSupport();        
        format = printingSupport.getPageFormat();

        Graphics2D g = (Graphics2D)_g;
        
        //setRenderingHints(g);
        g.scale(PRINT_SCALE, PRINT_SCALE);
        
        g.translate(format.getImageableX(), format.getImageableY());

/*
        Dimension extents = parent.getMaxImageExtents();
        int w = (int)(extents.getWidth());
        int h = (int)(extents.getHeight());
*/
/*
        Dimension extents = parent.getMaxImageExtents();
        Image _image = image.getScaledInstance(
                                        (int)extents.getWidth(), 
                                        (int)extents.getHeight(),
                                        Image.SCALE_SMOOTH);
*/      
//        int w = (int)(image.getWidth(parent) * (1/PRINT_SCALE));
//        int h = (int)(image.getHeight(parent) * (1/PRINT_SCALE));

        // using the actual image size from the pane
        Dimension extents = parent.getMaxImageExtents();
        int w = (int)(extents.getWidth() * PRINT_SCALE);
        int h = (int)(extents.getHeight() * PRINT_SCALE);

        //Log.debug("w: " + w + " h: " + h);

        if (w == 0 || h == 0) {
            return NO_SUCH_PAGE;
        }

        //int wPage = (int)format.getImageableWidth();
        //int hPage = (int)format.getImageableHeight();

        int wPage = (int)(format.getImageableWidth());// / PRINT_SCALE);
        int hPage = (int)(format.getImageableHeight());// / PRINT_SCALE);

//        int nCol = (int)(Math.max((int)Math.ceil((double)w/wPage), 1) / PRINT_SCALE);
//        int nRow = (int)(Math.max((int)Math.ceil((double)h/hPage), 1) / PRINT_SCALE);
        int nCol = Math.max((int)Math.ceil((double)w/wPage), 1);
        int nRow = Math.max((int)Math.ceil((double)h/hPage), 1);

        //Log.debug("nCol: " + nCol + " nRow: " + nRow);
        
        maxNumPages = nCol * nRow;
        
        //Log.debug("max pages: " + maxNumPages);
        
        int iCol = pageIndex % nCol;
        int iRow = pageIndex / nCol;
        
        int x = iCol * wPage;
        int y = iRow * hPage;
        
//        int wImage = (int)(Math.min(wPage, w-x));
//        int hImage = (int)(Math.min(hPage, h-y));

        // ---- works with no scale
        //Rectangle realClip = new Rectangle(x, y, wImage, hImage);
        //Rectangle fakeClip = new Rectangle(0, 0, wImage, hImage);
        // ------------------------------
        
        int clipWidth = (int)(wPage/PRINT_SCALE);
        int clipHeight = (int)(hPage/PRINT_SCALE);
        
        //g.setColor(Color.BLUE);
        //g.drawRect(1, 1, clipWidth, clipHeight);

        x = (int)(x / PRINT_SCALE);
        y = (int)(y / PRINT_SCALE);

        //Rectangle realClip = new Rectangle(x, y, wImage, hImage);
        //Rectangle fakeClip = new Rectangle(0, 0, wImage, hImage);

        Rectangle realClip = new Rectangle(x, y, clipWidth, clipHeight);
        Rectangle fakeClip = new Rectangle(0, 0, clipWidth, clipHeight);
        g.setClip(fakeClip);
        
        /*
        Log.debug("x: " + x + " y: " + y);
        Log.debug("fake clip: " + fakeClip);
        Log.debug("real clip: " + realClip);
        */

        parent.resetAllTableJoins();
        parent.getDependenciesPanel().drawDependencies(g, -x, -y);

        for (int i = 0; i < tablesArray.length; i++) {
            
            if (tablesArray[i].getBounds().intersects(realClip)) {
                /*
                Log.debug("table: " + tablesArray[i].getTableName()
                                        + " Y: " + tablesArray[i].getY());
                */
                tablesArray[i].setSelected(false);
                tablesArray[i].drawTable(g,
                                         tablesArray[i].getX() - x,
                                         tablesArray[i].getY() - y);
            }

        }

        
        //Log.debug("x: " + x + " y: " + y);

        
        /*
        AffineTransform af = new AffineTransform();
        af.scale(_scale, _scale);
        g.transform(af);
*/
        //g.scale(_scale, _scale);
        
//        AffineTransform af = new AffineTransform();
        
//        double scale = 300.0/72.0;
//        af.scale(PRINT_SCALE, PRINT_SCALE);
//        g.transform(af);
/*
//        parent.resetAllTableJoins();
//        Rectangle clip = new Rectangle(x, y, (int)(wPage / PRINT_SCALE), (int)(hPage / PRINT_SCALE));

//        g.setClip(x, (int)(y / PRINT_SCALE), (int)(wPage / PRINT_SCALE), (int)(hPage / PRINT_SCALE));
//        g.setClip(x, y, (int)(wImage / PRINT_SCALE), (int)(hImage / PRINT_SCALE));

/*
        g.setClip(x, y, wImage, hImage);

        AffineTransform af = new AffineTransform();
        af.scale(PRINT_SCALE, PRINT_SCALE);
        g.transform(af);
        
        parent.getDependenciesPanel().drawDependencies(g, -x, -y);

        for (int i = 0; i < tablesArray.length; i++) {

//            if (tablesArray[i].getBounds().intersects(clip)) {
                tablesArray[i].setSelected(false);
                Log.debug(tablesArray[i].getTableName() + " x: " +
                        tablesArray[i].getX() + " y: " + (tablesArray[i].getY()-y) );
                tablesArray[i].drawTable(g,
                                         tablesArray[i].getX() - x,
                                         tablesArray[i].getY() - y);
//            }

        }
*/
  
        /* working at scale 
        g.scale(1/scale, 1/scale);
        g.scale(PRINT_SCALE, PRINT_SCALE);
        g.drawImage(image, 0, 0, wImage, hImage, x, y, x+wImage, y+hImage, parent);
         */
        
        GUIUtilities.scheduleGC();
        return PAGE_EXISTS;
        
    }
    
    protected void setRenderingHints(Graphics2D g) {
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
         
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
         
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

//        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        /*
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
            RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING,
            RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_NORMALIZE);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
         */
    }
    
}















