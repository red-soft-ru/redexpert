/*
 * TablePrinter.java
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

package org.executequery.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.JTable.PrintMode;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.underworldlabs.swing.table.PrintableTableModel;

/**
 * Simple <code>Printable</code> implementation for
 * <code>JTable</code>s.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class TablePrinter implements Printable {
                                     //Pageable {

   
    protected int headerStatus = ALL_PAGES;
    protected int maxNumPage = 1;


    /** the table to be printed */
    protected JTable table;
    
    /** the page format for this printing */
    protected PageFormat pageFormat;

    /** the page header text */
    private String pageHeaderText;
    
    /** the table data font */
    private Font plainFont;
    
    /** the table header font */
    private Font boldFont;
    
    /** the footer font */
    private Font footerFont;

    /** the table header bg colour */
    private Color headerBackground;
    
    /** Indicates whether the first column in the table should be printed */
    private boolean printFirstColumn;

    /** table row height */
    private int rowHeight;

    /** table data row count */
    private int rowCount;

    // These constants indicate which pages should include column headers
    public final static int ALL_PAGES = 0;
    public final static int FIRST_PAGE_ONLY = 1;
    public final static int NO_PAGES = 2;


    public TablePrinter(JTable table, String pageHeaderText) {
        this(table, pageHeaderText, true);
    }

    public TablePrinter(JTable table, String pageHeaderText, boolean printFirstColumn) {
        this.table = table;
        this.pageHeaderText = pageHeaderText;
        this.printFirstColumn = printFirstColumn;
        
        rowCount = table.getRowCount();
        rowHeight = table.getRowHeight();
        
        // init the fonts
        String fontName = "monospaced";
        plainFont = new Font(fontName, Font.PLAIN, 10);
        boldFont = new Font(fontName, Font.BOLD, 10);
        footerFont = new Font(fontName, Font.PLAIN, 9);
        
        // colours
        headerBackground = new Color(204,204,204);
    }

    /**
     * Resets the state of the printable.<br>
     * This is intended to be called when you want to begin
     * the printing again from the beginning.
     */
    public void reset() {
        _lastRow = 0;
        _lastColumn = 0;
        if (pages != null) {
            pages.clear();
        }
    }
    
    /** 
     * Returns the table set to be printed.
     *
     * @return the table being printed
     */
    public JTable getTable() {
        return table;
    }
    
    /**
     * Sets the table to be printed by this printable.
     *
     * @param table - the table to print
     */
    public void setTable(JTable table) {
        this.table = table;
    }

    // don't like this - it only calls the table's paint method with the
    // printers graphics object - includes renderers, editors etc... yuk!!
    // also has some unreadable scaling for larger tables when FIT_WIDTH is set
    // ...nice page breaking though
    public int print_(Graphics graphics, PageFormat pageFormat, int pageIndex)
        throws PrinterException {

        PrintMode printMode = JTable.PrintMode.FIT_WIDTH;
        if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
            printMode = JTable.PrintMode.NORMAL;
        }
        
        return table.getPrintable(printMode, 
                                  new MessageFormat(pageHeaderText), 
                                  new MessageFormat("Page {0}")).
                print(graphics, pageFormat, pageIndex);
    }

    /** the last row printed in multi-page */
    private int _lastRow = 0;
    
    /** the last column printed in multi-page */
    private int _lastColumn = 0;
    
    private int lastPageIndex;
    
    private Vector<TablePrintSegment> pages;
    
    /**
     * Prints the page at the specified index into the specified 
     * context in the specified format. A <code>PrinterJob</code> calls
     * the <code>Printable</code> interface to request that a page be
     * rendered into the context specified by 
     * <code>graphics</code>. The format of the page to be drawn is
     * specified by <code>pageFormat</code>. The zero based index
     * of the requested page is specified by <code>pageIndex</code>. 
     * If the requested page does not exist then this method returns
     * NO_SUCH_PAGE; otherwise PAGE_EXISTS is returned.
     * The <code>Graphics</code> class or subclass implements the
     * interface to provide additional information.  If the 
     * <code>Printable</code> object aborts the print job then it throws 
     * a <code>PrinterException</code>.
     *
     * @param graphics the context into which the page is drawn 
     * @param pageFormat the size and orientation of the page being drawn
     * @param pageIndex the zero based index of the page to be drawn
     * @return PAGE_EXISTS if the page is rendered successfully
     *         or NO_SUCH_PAGE if <code>pageIndex</code> specifies a
     *	       non-existent page.
     * @exception java.awt.print.PrinterException
     *         thrown when the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
        throws PrinterException {

        int lastRow = 0;
        int lastColumn = 0;
        
        TablePrintSegment segment = getPrintSegment(pageIndex);
        if (segment != null) {
            lastRow = segment.lastRow;
            lastColumn = segment.lastColumn;
        }
        
        /*
        Log.debug("page: "+pageIndex);
        Log.debug("lastRow: "+lastRow);
        Log.debug("lastColumn: "+lastColumn);        
        */

        if (lastRow == -1 && lastColumn == -1) {
            GUIUtilities.getStatusBar().setSecondLabelText("");
            if (pages != null) {
                pages.clear();
            }
            return NO_SUCH_PAGE;
        }
        lastPageIndex = pageIndex;

        GUIUtilities.getStatusBar().setSecondLabelText(
                "Printing page " +(pageIndex + 1));

        Graphics2D g2d = (Graphics2D)graphics;
        pageFormat = getPageFormat();
        
        graphics.translate((int)pageFormat.getImageableX(),
                           (int)pageFormat.getImageableY());

        // determine the actual print width and height
        int printWidth = (int)pageFormat.getImageableWidth();
        int printHeight = (int)pageFormat.getImageableHeight();

        /*
        Log.debug("imageableHeight: " + pageFormat.getImageableHeight());
        Log.debug("imageableWidth: " + pageFormat.getImageableWidth());
        */

        /*
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(0,0,printWidth,printHeight);
        */

        // add the page number footer
        g2d.setFont(footerFont);
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        String footerText = "Page " + (pageIndex + 1);
        int textX = (printWidth - fm.stringWidth(footerText)) / 2;
        int textY = (printHeight - fm.getHeight());
        g2d.drawString(footerText, textX, textY);
        
        // reduce the printHeight to account for page number
        printHeight -= (fm.getHeight() + 5);
        
        //Log.debug("imageable x: "+pageFormat.getImageableX());
        //Log.debug("imageable y: "+pageFormat.getImageableY());

        // scale factor for each column if we are trying to fit
        // all available columns on the one page
        double colScaleFactor = 1;

        // check of we need to scale the column widths
        if (table.getAutoResizeMode() != JTable.AUTO_RESIZE_OFF) {
            int tableWidth = table.getWidth();
            if (tableWidth > printWidth) {
                colScaleFactor = ((double)printWidth/(double)tableWidth);
            }
        }

        g2d.setFont(plainFont);
        g2d.setColor(Color.black);

        fm = graphics.getFontMetrics();
        int y = fm.getAscent();
        
        // draw the title
        if (pageIndex == 0) {
            if (pageHeaderText != null) {
                g2d.setClip(0, 0, printWidth, printHeight);
                g2d.drawString(pageHeaderText, 0, y);
                y += 20; // space between title and table headers
            }
        }

        int firstColumn = 0;
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        if (!printFirstColumn) {
            firstColumn = 1;
        }

        // if we have left-over columns start there
        if (lastColumn > 0) {
            firstColumn = lastColumn;
        }
        
        // define an array to hold the col x positions
        int x[] = new int[columnCount];
        x[0] = 0;
        
        // define an array to hold the col width values
        int colWidths[] = new int[columnCount];
        colWidths[0] = 0;
        
        // table header font
        g2d.setFont(boldFont);
        fm = g2d.getFontMetrics();

        // font ascent offset
        int h = fm.getAscent();

        Rectangle rect = new Rectangle();
        
        if (pageIndex > 0) {
            rect.y = rowHeight + h;
        } else {
            rect.y = y;
        }
        rect.height = rowHeight;
        
        int totalWidth = 0;
        
        // draw the header
        for (int col = firstColumn; col < columnCount; col++) {
            TableColumn tk = columnModel.getColumn(col);
            int width = tk.getWidth();

            rect.x = x[col];
            rect.width = (int)(width * colScaleFactor);// + 1;

            if ((rect.x + rect.width) > printWidth) {
                lastColumn = col;
                // update/add the segment
                TablePrintSegment nextSegment = getPrintSegment(pageIndex + 1);
                if (nextSegment == null) {
                    addPrintSegment(pageIndex + 1, lastRow, lastColumn);
                } else {
                    nextSegment.lastRow = lastRow;
                    nextSegment.lastColumn = lastColumn;
                }
                break;
            }
            lastColumn = -1;

            rect.x--;
            g2d.setClip(rect);
            
            totalWidth += rect.width;
            
            // fill the background
            g2d.setColor(headerBackground);
            g2d.fill(rect);
            g2d.setColor(Color.BLACK);

            // top line
            g2d.setClip(0, 0, printWidth, printHeight);
            g2d.drawLine(0, 
                         rect.y, 
                         totalWidth - 2, 
                         rect.y);

            // reset the clip region
            g2d.setClip(rect);

            // draw the left border
            if (col == firstColumn) {
                g2d.drawLine(rect.x + 1, rect.y, rect.x + 1, rect.y + rect.height);
            }

            // draw the right border
            g2d.drawLine(rect.x + rect.width - 1, 
                         rect.y, 
                         rect.x + rect.width - 1, 
                         rect.y + rect.height);
            
            colWidths[col] = rect.width;

            if (col+1 < columnCount) {
                x[col+1] = x[col] + rect.width;
            }

            y = ((rect.y + rect.height) / 2) + rect.y - h;
            String title = (String)tk.getIdentifier();
            BasicGraphicsUtils.drawStringUnderlineCharAt(g2d, title, -1, rect.x + 2, y);            
        }
        
        // draw the bottom border
        g2d.setClip(0, 0, printWidth, printHeight);
        g2d.drawLine(0, 
                     rect.y + rect.height, 
                     totalWidth - 2, 
                     rect.y + rect.height);

        g2d.setFont(plainFont);
        fm = g2d.getFontMetrics();
        h = fm.getHeight();

        boolean usingPrintableModel = false;
        TableModel model = table.getModel();
        PrintableTableModel printModel = null;
        if (model instanceof PrintableTableModel) {
            usingPrintableModel = true;
            printModel = (PrintableTableModel)model;
        }
        g2d.setColor(Color.BLACK);
        
        /*
        Log.debug("pageIndex: " + pageIndex);
        Log.debug("rowCount: " + table.getRowCount());
        Log.debug("hPage: " + printHeight);
        Log.debug("lastRow: " + lastRow);
        */
        
        for (int row = lastRow; row < rowCount; row++) {
            y += rowHeight;
            rect.y = y - h;
            if (row > lastRow) {
                rect.height = rowHeight;
            } else {
                rect.y -= 2;
                rect.height += 2;
            }
            
            //Log.debug("index: "+nRow+ " rect: " + rect);

            if ((rect.y + rect.height) > printHeight) {
                if (lastColumn == -1) {
                    lastRow = row;
                }
                TablePrintSegment nextSegment = getPrintSegment(pageIndex + 1);
                if (nextSegment == null) {
                    addPrintSegment(pageIndex + 1, lastRow, lastColumn);
                } else {
                    nextSegment.lastRow = lastRow;
                    nextSegment.lastColumn = lastColumn;
                }
                return PAGE_EXISTS;
            }
            
            g2d.setClip(0, rect.y - 2, 
                        totalWidth + 1, rect.y + rect.height + 2);

            // bottom line
            g2d.drawLine(0, rect.y + rect.height - 1, 
                         totalWidth - 2, rect.y + rect.height - 1);

            for (int col = firstColumn; col < columnCount; col++) {
                
                rect.x = x[col];
                rect.width = colWidths[col];

                rect.x--;
                g2d.setClip(rect.x, rect.y - 1, rect.width, rect.height);

                // draw the left border
                if (col == firstColumn) {
                    g2d.drawLine(rect.x + 1, rect.y - 1, rect.x + 1, rect.y + rect.height);
                }

                // draw the right border
                g2d.drawLine(rect.x + rect.width - 1, rect.y - 1, 
                             rect.x + rect.width - 1, rect.y + rect.height);

                //int col = columnModel.getColumn(nCol).getModelIndex();
                
                //Log.debug("col: "+col+" row: " + row);

                String value = null;
                if (usingPrintableModel) {
                    value = printModel.getPrintValueAt(row, col);
                } else {
                    Object obj = model.getValueAt(row, col);
                    value = (obj == null ? Constants.EMPTY : obj.toString());                    
                }

                if (value == null) {
                    value = Constants.EMPTY;
                }
                
                if (col > firstColumn) {
                    g2d.drawString(value, rect.x + 2, y);
                } else {
                    g2d.drawString(value, rect.x + 4, y);
                }
                
            }
            
            // if we don't have to add more columns do the row check
            if (lastColumn == -1) {
                if (row == rowCount - 1) {
                    lastRow = -1;
                    TablePrintSegment nextSegment = getPrintSegment(pageIndex + 1);
                    if (nextSegment == null) {
                        addPrintSegment(pageIndex + 1, lastRow, lastColumn);
                    } else {
                        nextSegment.lastRow = lastRow;
                        nextSegment.lastColumn = lastColumn;
                    }
                }
            }

        }

        if (pageIndex == 0) {
            addPrintSegment(pageIndex, 0, lastColumn);
        }

        return PAGE_EXISTS; 
    }
    
    private PageFormat getPageFormat() {

        if (pageFormat == null) {
        
            PrintingSupport printingSupport = new PrintingSupport();

            pageFormat = printingSupport.getPageFormat();
        }

        return pageFormat;
    }

    private void addPrintSegment(int pageIndex, int lastRow, int lastColumn) {
        if (pages == null) {
            pages = new Vector<TablePrintSegment>();
        }
        TablePrintSegment segment = new TablePrintSegment();
        segment.pageIndex = pageIndex;
        segment.lastRow = lastRow;
        segment.lastColumn = lastColumn;
        pages.add(segment);
    }
    
    private TablePrintSegment getPrintSegment(int pageIndex) {
        if (pages == null || pages.isEmpty()) {
            return null;
        }
        for (int i = 0, n = pages.size(); i < n; i++) {
            TablePrintSegment segment = pages.get(i);
            if (segment.pageIndex == pageIndex) {
                return segment;
            }
        }
        return null;
    }
    
    /**
     * Calculate how much of the table will fit on a page without
     * causing a row or column to be split across two pages
     */
    protected Dimension getPrintSize(int positionX, int positionY) {
        Rectangle rect;
        int printWidth;
        int printHeight;
//        int firstCol = table.columnAtPoint(new Point(positionX, positionY));
//        int firstRow = table.rowAtPoint(new Point(positionX, positionY));
        int maxWidth = (int)(pageFormat.getImageableWidth());
        int maxHeight = (int)(pageFormat.getImageableHeight());
        
        if (displayHeaderOnPage(positionY))
            maxHeight -= table.getTableHeader().getHeight();
        
        int lastCol = table.columnAtPoint(new Point(positionX + maxWidth, positionY));
        
        if (lastCol == -1) {
            printWidth = table.getWidth() - positionX;
        } else {
            rect = table.getCellRect(0, lastCol - 1, true);
            printWidth = rect.x + rect.width - positionX;
        }
        
        int lastRow = table.rowAtPoint(new Point(positionX, positionY + maxHeight));
        
        if (lastRow == -1) {
            printHeight = table.getHeight() - positionY;
        } else {
            rect = table.getCellRect(lastRow - 1, 0, true);
            printHeight = rect.y + rect.height - positionY;
        }
        
        return new Dimension(printWidth, printHeight);
    }
    
    /**
     * Paint / print a portion of the table
     */
    /*
    protected void paintTable(Graphics g,
    int positionX, int positionY,
    Dimension size) {
        
        int offsetX = (int)(pageFormat.getImageableX());
        int offsetY = (int)(pageFormat.getImageableY());
        
        if (displayHeaderOnPage(positionY)) {
            
            JTableHeader header = table.getTableHeader();
            
            if ((header.getWidth() == 0) || (header.getHeight() == 0))
                header.setSize(header.getPreferredSize());
            
            int headerHeight = header.getHeight();
            g.translate(offsetX - positionX, offsetY);
            g.clipRect(positionX, 0, size.width, size.height + headerHeight);
            
            header.paint(g);
            
            g.translate(0, headerHeight - positionY);
            g.clipRect(positionX, positionY, size.width, size.height);
            
        }
        
        else {
            g.translate(offsetX - positionX, offsetY - positionY);
            g.clipRect(positionX, positionY, size.width, size.height);
        }
        
        table.paint(g);
    }
    */
    /**
     * Determine whether or not to paint the headers on the current page
     */
    protected boolean displayHeaderOnPage(int positionY) {
        return ((headerStatus == ALL_PAGES) ||
        ((headerStatus == FIRST_PAGE_ONLY) &&
        positionY == 0));
    }
    
    /**
     * Calculate the number of pages it will take to print the entire table
     */
    public int getNumberOfPages() {
        //Log.debug("getNumberOfPages");
        Dimension size = null;
        int tableWidth = table.getWidth();
        int tableHeight = table.getHeight();
        int positionX = 0;
        int positionY = 0;
        
        int pageIndex = 0;
        
        while (positionY < tableHeight) {
            positionX = 0;
            
            while (positionX < tableWidth) {
                size = getPrintSize(positionX, positionY);
                positionX += size.width;
                pageIndex++;
            }
            
            positionY += size.height;
            
        }
        
        return pageIndex;
    }
    
    public Printable getPrintable(int index) {
        return this;
    }
    
    public PageFormat getPageFormat(int index) {
        return getPageFormat();
    }
    
    class TablePrintSegment {
        int pageIndex;
        int lastRow;
        int lastColumn;
        public TablePrintSegment() {}
    }
    
}











