/*
 * ErdTitlePanel.java
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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ErdTitlePanel extends ErdMoveableComponent implements Serializable {
    
    private static final String DATE = "Date:";
    private static final String DESCRIPTION = "Description:";
    private static final String DATABASE = "Database:";
    private static final String AUTHOR = "Author:";
    private static final String FILE_NAME = "File Name:";
    private static final String REVISION = "Rev:";
    
    private static final int TEXT_MARGIN = 4;
    
    private static int width;
    private static int height;
    
    /** The label font */
    private Font labelFont;
    
    /** The text font */
    private Font font;
    
    /** The ERD name */
    private String erdName;
    
    /** The date stamp */
    private String erdDate;
    
    /** The description */
    private String erdDescription;
    
    /** The database type */
    private String erdDatabase;
    
    /** The author */
    private String erdAuthor;
    
    /** The file name */
    private String erdFileName;
    
    /** The revision number */
    private String erdRevision;
    
    /** The description broken into lines */
    private String[] descriptionLines;
    
    /** The title panel image */
    private transient Image img;
    
    public ErdTitlePanel(ErdViewerPanel parent, String title, String date,
                         String description, String database, String author,
                         String revision, String fileName) {
  
        super(parent);
  
        String fontName = "Dialog";
        font = new Font(fontName, Font.PLAIN, 11);
        labelFont = new Font(fontName, Font.PLAIN, 8);
        
        String EMPTY = "";
        
        erdName = title == null ? EMPTY : title;
        erdDate = date == null ? EMPTY : date;
        erdDatabase = database == null ? EMPTY : database;
        erdDescription = description == null ? EMPTY : description;
        erdRevision = revision == null ? EMPTY : revision;
        erdAuthor = author == null ? EMPTY : author;
        erdFileName = fileName == null ? EMPTY : fileName;
        
        width = 300;
        calculateHeight();
    }
    
    public void resetValues(String title, String date, String description,
                            String database, String author,
                            String revision, String fileName) {
        img = null;
        setVisible(false);
        String EMPTY = "";
        erdName = title == null ? EMPTY : title;
        erdDate = date == null ? EMPTY : date;
        erdDatabase = database == null ? EMPTY : database;
        erdDescription = description == null ? EMPTY : description;
        erdRevision = revision == null ? EMPTY : revision;
        erdAuthor = author == null ? EMPTY : author;
        erdFileName = fileName == null ? EMPTY : fileName;
        
        calculateHeight();
        createImage();
        
        Rectangle bounds = getBounds();
        setBounds(bounds.x, bounds.y, width, height);
        setVisible(true);
    }

    private void createImage() {

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        drawImage((Graphics2D)img.getGraphics(), 0, 0);
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    private void calculateHeight() {
        // build the description lines
        partitionDescription(null, (width - (TEXT_MARGIN * 2)) - 2);
        
        // determine the height of the panel
        FontMetrics fm = getFontMetrics(font);
        int lineHeight = fm.getHeight() + TEXT_MARGIN + 2;
        int descSize = descriptionLines.length;
        
        FontMetrics labelFm = getFontMetrics(labelFont);
        
        height = (lineHeight * 3) +
        ((descSize == 0 ? 1 : descSize) * (fm.getHeight() - fm.getDescent())) +
        (TEXT_MARGIN * 2) + labelFm.getHeight();
        
        fm = null;
        labelFm = null;
    }
    
    private void drawImage(Graphics2D g, int offsetX, int offsetY) {
        
        // set the quality to high for the image
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        g.setColor(Color.WHITE);
        g.fillRect(1 + offsetX, 1 + offsetY, width + offsetX, height + offsetY);
        
        int row = 0;
        int x1 = 1, y1 = 1;
        int x2 = width - 2, y2 = height - 2;
        
        FontMetrics fm = g.getFontMetrics(font);
        FontMetrics labelFm = g.getFontMetrics(labelFont);
        
        int fontHeight = fm.getHeight();
        int lineHeight = fontHeight + TEXT_MARGIN + 2;
        int stringWidth = fm.stringWidth(erdName);
        
        int textXPosn = TEXT_MARGIN;
        int textYPosn = lineHeight - (fontHeight / 4) - (TEXT_MARGIN / 2) - 1;
        int labelTextYPosn = (TEXT_MARGIN * 2) + 3;
        
        row++;
        y1 = lineHeight * row;
        y2 = y1;
        
        g.setColor(Color.BLACK);
        g.setFont(font);
        
        // draw the ERD title
        g.drawString(erdName, textXPosn + offsetX, textYPosn + offsetY);
        // draw the first horizontal line below the title
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        textXPosn += stringWidth + (TEXT_MARGIN * 2);
        
        if (textXPosn < 200)
            textXPosn = 200;
        
        x1 = textXPosn;
        y1 = 1;
        x2 = x1;
        
        // draw the vertical line between the title and date
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        textXPosn += TEXT_MARGIN;
        
        // draw the date
        g.setFont(labelFont);
        g.drawString(DATE, textXPosn + offsetX, labelTextYPosn + offsetY);
        
        textXPosn += labelFm.stringWidth(DATE) + (TEXT_MARGIN * 2);
        g.setFont(font);
        g.drawString(erdDate, textXPosn + offsetX, textYPosn + offsetY);
        
        textXPosn = TEXT_MARGIN;
        
        // draw the description
        row++;
        g.setFont(labelFont);
        labelTextYPosn = y2 + (TEXT_MARGIN * 2) + 2;
        g.drawString(DESCRIPTION, textXPosn + offsetX, labelTextYPosn + offsetY);
        
        textYPosn = labelTextYPosn + labelFm.getHeight() + TEXT_MARGIN;
        g.setFont(font);
        
        if (descriptionLines == null)
            partitionDescription(g, (width - (TEXT_MARGIN * 2)) - 2);
        
        int descSize = descriptionLines.length;
        
        if (descSize == 0)
            row++;
        else {
            
            int _lineHeight = fontHeight - fm.getDescent();
            
            for (int i = 0; i < descSize; i++) {
                
                if (i > 0)
                    textYPosn += _lineHeight;
                
                row++;
                g.drawString(descriptionLines[i], textXPosn + offsetX, textYPosn + offsetY);
                
            }
            
        }
        
        x1 = 1;
        y1 = ((descSize == 0 ? 1 : descSize) * (fontHeight - fm.getDescent())) +
        (TEXT_MARGIN * 2) + labelFm.getHeight() + y2;
        x2 = width - 2;
        y2 = y1;
        
        // draw the second horizontal line across
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        // draw the database details
        g.setFont(labelFont);
        labelTextYPosn = y1 + (TEXT_MARGIN * 2) + 2;
        g.drawString(DATABASE, textXPosn + offsetX, labelTextYPosn + offsetY);
        
        stringWidth = labelFm.stringWidth(DATABASE);
        
        textXPosn +=  stringWidth + (TEXT_MARGIN * 2);
        textYPosn = labelTextYPosn + 3;
        
        g.setFont(font);
        g.drawString(erdDatabase, textXPosn + offsetX, textYPosn + offsetY);
        
        stringWidth = fm.stringWidth(erdDatabase);
        x1 = textXPosn + stringWidth + (TEXT_MARGIN * 2);
        x2 = x1;
        y2 = y1 + lineHeight;
        
        // draw the vertical line between db and revision
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        textXPosn = x1 + TEXT_MARGIN;
        
        // draw the revision details
        g.setFont(labelFont);
        g.drawString(REVISION, textXPosn + offsetX, labelTextYPosn + offsetY);
        
        stringWidth = labelFm.stringWidth(REVISION);
        textXPosn += stringWidth + TEXT_MARGIN;
        
        g.setFont(font);
        g.drawString(erdRevision, textXPosn + offsetX, textYPosn + offsetY);
        
        stringWidth = fm.stringWidth(erdRevision);
        x1 = textXPosn + stringWidth + (TEXT_MARGIN * 2);
        x2 = x1;
        
        // draw the vertical line between revision and author
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        textXPosn = x1 + TEXT_MARGIN;
        
        // draw the author details
        g.setFont(labelFont);
        g.drawString(AUTHOR, textXPosn + offsetX, labelTextYPosn + offsetY);
        
        stringWidth = labelFm.stringWidth(AUTHOR);
        textXPosn += stringWidth + (TEXT_MARGIN * 2);
        
        g.setFont(font);
        g.drawString(erdAuthor, textXPosn + offsetX, textYPosn + offsetY);
        
        y1 = y2;
        x1 = 1;
        x2 = width - 2;
        
        // draw the third horizontal line across
        g.drawLine(x1 + offsetX, y1 + offsetY, x2 + offsetX, y2 + offsetY);
        
        row++;
        g.setFont(labelFont);
        textXPosn = TEXT_MARGIN;
        labelTextYPosn = y2 + (TEXT_MARGIN * 2) + 2;
        
        g.drawString(FILE_NAME, textXPosn + offsetX, labelTextYPosn + offsetY);
        stringWidth = labelFm.stringWidth(FILE_NAME);
        
        textXPosn += stringWidth + (TEXT_MARGIN * 2);
        textYPosn = labelTextYPosn + 3;
        
        g.setFont(font);
        g.drawString(erdFileName, textXPosn + offsetX, textYPosn + offsetY);
    }
    
    public void printTitlePanel(Graphics2D g, int offsetX, int offsetY) {
        setSelected(false);
        drawImage(g, offsetX, offsetY);
        drawBorder(g, offsetX, offsetY);
    }
    
    public void drawTitlePanel(Graphics2D g, int offsetX, int offsetY) {
        
        if (img == null) {
            
            createImage();
        }
        
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
        RenderingHints.VALUE_RENDER_QUALITY);
        
        g.drawImage(img, offsetX, offsetY, this);
        drawBorder(g, offsetX, offsetY);
    }
    
    private void drawBorder(Graphics2D g, int offsetX, int offsetY) {
        
        double scale = g.getTransform().getScaleX();
        
        if (selected && scale != ErdPrintable.PRINT_SCALE) {
            
            g.setStroke(focusBorderStroke);
            g.setColor(Color.BLUE);
            
        } else {
         
            g.setColor(Color.BLACK);
        }

        g.drawRect(offsetX, offsetY, getWidth() - 2, getHeight() - 2);
    }
    
    public boolean isOpaque() {
        return true;
    }
    
    public void paintComponent(Graphics g) {
        drawTitlePanel((Graphics2D)g, 0, 0);
    }
    
    public void doubleClicked(MouseEvent e) {
        new ErdTitlePanelDialog(parent, erdName, erdDate, erdDescription, erdDatabase,
                erdAuthor, erdRevision, erdFileName);
    }
    
    private void partitionDescription(Graphics g, int textWidth) {

        FontMetrics fm = null;
        if (g == null) {

            fm = getFontMetrics(font);

        } else {

            fm = g.getFontMetrics(font);
        }
        
        List<String> description = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(erdDescription, " ", true);
        StringBuilder sb = new StringBuilder();
        
        int length = 0;
        int currentLength = 0;
        
        while (st.hasMoreTokens()) {

            String _text = st.nextToken();
            length = fm.stringWidth(_text);
            
            if (currentLength + length <= textWidth) {

                sb.append(_text);
                currentLength += length;

            } else {
                
                description.add(sb.toString().trim());
                
                sb.setLength(0);
                
                currentLength = length;
                sb.append(_text);
            }
            
            _text = null;
            
        }
        
        if (sb.length() > 0) {

            description.add(sb.toString().trim());
        }
        
        descriptionLines = (String[])description.toArray(new String[description.size()]);
        
    }
    
    public String getErdFileName() {
        return erdFileName;
    }
    
    public String getErdRevision() {
        return erdRevision;
    }
    
    public String getErdAuthor() {
        return erdAuthor;
    }
    
    public String getErdDatabase() {
        return erdDatabase;
    }
    
    public String getErdDescription() {
        return erdDescription;
    }
    
    public String getErdDate() {
        return erdDate;
    }
    
    public String getErdName() {
        return erdName;
    }
    
}










