/*
 * TextPrinter.java
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

package org.executequery.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import java.util.StringTokenizer;
import java.util.Vector;

import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class TextPrinter implements Printable {
    
    /** The text to be printed */
    private String text;
    
    /** The font to print with */
    private Font printFont;

    /** Holds the lines of text */
    private Vector lines;
    
    /** The tab sizes in spaces */
    private int tabSize;
    
    /** <p>Constructs a new text printer with the specified text.
     *
     *  @param the text to be printed
     */
    public TextPrinter(String text) {
        this.text = text;
        printFont = new Font("monospaced", Font.PLAIN, 10);
        tabSize = SystemProperties.getIntProperty("user", "editor.tab.spaces");
    }
    
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        
        g.translate((int)pageFormat.getImageableX(),
                    (int)pageFormat.getImageableY());
        
        int printWidth = (int)pageFormat.getImageableWidth();
        int printHeight = (int)pageFormat.getImageableHeight();
        g.setClip(0, 0, printWidth, printHeight);
        
        g.setColor(Color.BLACK);
        g.setFont(printFont);
        
        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight();

        if (lines == null) {
            lines = getLines(fm, printWidth);
        }

        // print the page number
        String footerText = "Page " + (pageIndex + 1);
        int textX = (printWidth - fm.stringWidth(footerText)) / 2;
        int textY = (printHeight - fontHeight);
        g.drawString(footerText, textX, textY);
        // reduce the printHeight to account for page number
        printHeight -= (fontHeight + 10);
        
        int numLines = lines.size();
        int linesPerPage = Math.max(printHeight / fontHeight, 1);
        int numPages = (int)Math.ceil((double)numLines / (double)linesPerPage);
        
        if (numPages == 0) {
            numPages = 1;
        }

        if (pageIndex >= numPages) {
            lines = null;
            return NO_SUCH_PAGE;
        }
        
        int x = 0;
        int y = fm.getAscent();
        int lineIndex = linesPerPage * pageIndex;
        
        while (lineIndex < numLines && y < printHeight) {
            String str = (String)lines.get(lineIndex);
            g.drawString(str, x, y);
            y += fontHeight;
            lineIndex++;
        }
        
        return PAGE_EXISTS;
    }
    
    protected Vector getLines(FontMetrics fm, int wPage) {
        
        Vector v = new Vector();
        
        String prevToken = "";
        StringTokenizer st = new StringTokenizer(text, "\n\r", true);
        
        String empty = "";
        String carriageReturn = "\r";
        String newLine = "\n";
        String tab = "\t";
        String spaceTab = " \t";
        String space = " ";
        
        while (st.hasMoreTokens()) {
            
            String line = st.nextToken();
            
            if (line.equals(carriageReturn)) {
                continue;
            }
            
            // StringTokenizer will ignore empty lines,
            // so it's a bit tricky to get them...
            if (line.equals(newLine) && prevToken.equals(newLine)) {
                v.add(empty);
            }
            
            prevToken = line;
            
            if (line.equals(newLine)) {
                continue;
            }
            
            StringTokenizer st2 = new StringTokenizer(line, spaceTab, true);
            String line2 = empty;
            
            while (st2.hasMoreTokens()) {
                String token = st2.nextToken();
                
                if (token.equals(tab)) {
                    int numSpaces = tabSize - line2.length() % tabSize;
                    token = empty;
                    
                    for (int k = 0; k < numSpaces; k++) {
                        token += space;
                    }
                    
                }
                
                int lineLength = fm.stringWidth(line2 + token);
                
                if (lineLength > wPage && line2.length() > 0) {
                    v.add(line2);
                    line2 = token.trim();
                    continue;
                }
                
                line2 += token;
                
            }
            
            v.add(line2);
            
        }
        
        return v;
    }
    
}














