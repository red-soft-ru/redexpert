/*
 * GradientLabel.java
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIManager;
import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 */
public class GradientLabel extends JComponent {
    
    /** The text to be displayed */
    private String text;
    
    /** The font */
    private Font font;
    
    /** the image icon displayed */
    private ImageIcon icon;
    
    /** The component's left-hand gradient colour */
    private Color leftGradientColor;

    /** The component's right-hand gradient colour */
    private Color rightGradientColor;
    
    /** The text label foreground colour */
    private Color foregroundColor;
    
    /** indicates that a shadow should be dropped on the text */
    private boolean shadowDropped;
    
    /** The component's insets */
    private static Insets insets;
    
    /** the default font size - 15pts */
    public static final float DEFAULT_FONT_SIZE = 15f;
    
    /** Creates a new instance with default component settings. */
    public GradientLabel() {
        this("", null, UIManager.getFont("Label.font").deriveFont(Font.BOLD, DEFAULT_FONT_SIZE));
    }

    /**
     * Creates a new instance with the specified text, icon and font.
     *
     * @param text - the label text
     * @param icon - the label image icon
     * @param font - the label font
     */
    public GradientLabel(String text, ImageIcon icon, Font font) {
        this(text, icon, font, null, null);
    }
    
    public GradientLabel(String text, ImageIcon icon, Font font, 
                         Color leftGradientColor, Color rightGradientColor) {
        this(text, icon, font, leftGradientColor, rightGradientColor, null, false);
    }

    public GradientLabel(String text, ImageIcon icon, Font font, 
                         Color leftGradientColor, Color rightGradientColor,
                         Color foregroundColor, boolean shadowDropped) {

        this.text = text;
        this.icon = icon;
        this.font = font;
        this.setLeftGradientColor(leftGradientColor);
        this.setRightGradientColor(rightGradientColor);
        this.shadowDropped = shadowDropped;
        
        if (foregroundColor == null) {

            foregroundColor = determineForegroundColour();
        }

        setForeground(foregroundColor);
    }
    
    private Color determineForegroundColour() {

        if (UIUtils.isNativeMacLookAndFeel()) {
    	
            return UIManager.getColor("text");
    	}    	
    	return UIUtils.getColour("executequery.GradientLabel.foreground", "controlText");
    }

    /** 
     * Overides to return <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean isOpaque() {
        return true;
    }

    /**
     * Returns the component's inset margin.
     *
     * @return the insets
     */
    public Insets getInsets() {
        if (insets == null) {
            insets = new Insets(3, 2, 3, 2);
        }
        return insets;
    }
    
    /**
     * Returns the label text foreground colour.
     *
     * @return the label foreground colour
     */
    public Color getForeground() {
        return foregroundColor;
    }
    
    /**
     * Returns the label text font.
     *
     * @return the font
     */
    public Font getFont() {
        if (font == null) {
            return super.getFont();
        }
        return font;
    }
    
    /** 
     * Performs the painting for this component.
     *
     * @param the <code>Graphics</code> object to
     *         perform the painting
     */
    protected void paintComponent(Graphics g) {
        
    	Graphics2D g2 = (Graphics2D)g;
    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	
    	super.paintComponent(g);
        
        int width = getWidth();
        int height = getHeight();

        // draw the gradient background
        Color color1 = getLeftGradientColor();
        Color color2 = getRightGradientColor();

        if (color1 != null && color2 != null) {
            Paint paint = g2.getPaint();
            GradientPaint fade = new GradientPaint(0, 0, color1, 
                                    (int)(width * 0.9), 0, color2);
            g2.setPaint(fade);
            g2.fillRect(0,0, width, height);
            g2.setPaint(paint);
        }
        
        Color lineColour = getSeparatorColour();
        if (lineColour != null) {
            g2.setColor(lineColour);
            g2.drawLine(0, 0,  width, 0);
        }

        // draw the icon and text
        Insets _insets = getInsets();
        int x = _insets.left + 5;
        int y = 0;
        if (icon != null) {
            y = (getHeight() - icon.getIconHeight()) / 2;
            icon.paintIcon(this, g2, x, y);
            x += icon.getIconWidth() + 10;
        }

        if (text != null) {
            Font _font = getFont();
            g2.setFont(_font);

            FontMetrics metrics = getFontMetrics(_font);
            int fontHeight = metrics.getHeight();
            y = ((height - fontHeight) / 2) + fontHeight - 2;
            
            if (isShadowDropped()) {
                Composite composite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                g2.setColor(Color.BLACK);
                g2.drawString(text, x+2, y+2);
                g2.setComposite(composite);
            }

            g2.setColor(getForeground());
            g2.drawString(text, x, y);            
        }
    }
    
    /** 
     * Returns the colour of the top border line.
     * This is the only border line painted by the component.
     * The default value returned is the value retrieved from
     * <code>UIManager.getColor("controlShadow")</code>.
     * <br>Override to return null and not have the line painted.
     *
     * @return the top-line separator colour
     */
    public Color getSeparatorColour() {
        return UIUtils.getDefaultBorderColour();
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
    /** the label height */
    private int height;
    
    /** 
     * Calculates the height based on the font and icon size specified.
     *
     * @return the component's height
     */
    public int getHeight() {
        if (height == 0) {
            Insets _insets = getInsets();
            height = _insets.top + _insets.bottom;

            // check icon height
            int iconHeight = 0;
            if (icon != null) {
                iconHeight = icon.getIconHeight();
            }
            
            // get font height
            FontMetrics metrics = getFontMetrics(getFont());
            int fontHeight = metrics.getHeight();

            height += Math.max(iconHeight, fontHeight) + 10;
        }
        return height;
    }
    
    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }
    
    /**
     * Returns the right hand gradient colour.
     * If this is null, the default colour used is retrieved using
     * <code>UIUtils.getDefaultInactiveBackgroundColour()</code> and
     * is usually the unmodified component brackground.
     *
     * @return the right-hand gradient colour
     */
    public Color getRightGradientColor() {
        if (rightGradientColor == null) {
            setRightGradientColor(UIUtils.getDefaultInactiveBackgroundColour());
        }
        return rightGradientColor;
    }

    /**
     * Returns the right hand gradient colour.
     * If this is null, the default colour used is retrieved using
     * <code>UIUtils.getDefaultActiveBackgroundColour()</code>.
     *
     * @return the left-hand gradient colour
     */
    public Color getLeftGradientColor() {
        if (leftGradientColor == null) {
            
            if (!UIUtils.isNativeMacLookAndFeel()) {
                
                setLeftGradientColor(UIUtils.getDefaultActiveBackgroundColour());
                
            } else {

                setLeftGradientColor(leftGradientColourForMac());
            }
            
        }
        return leftGradientColor;
    }

    private Color leftGradientColourForMac() {        
        return UIManager.getColor("Focus.color");
    }
    
    public void setText(String _text) {
        String oldValue = text;
        
        if (_text == null) {
            text = "";
        } else {
            text = _text;
        }

        firePropertyChange("text", oldValue, text);        
        if (oldValue == null || !text.equals(oldValue)) {
            revalidate();
            repaint();
        }
        
    }
    
    /** 
     * Returns the text string that this component displays.
     *
     * @return the text displayed
     */
    public String getText() {
        return text;
    }

    public boolean isShadowDropped() {
        return shadowDropped;
    }

    public void setShadowDropped(boolean shadowDropped) {
        this.shadowDropped = shadowDropped;
    }

    public void setLeftGradientColor(Color leftGradientColor) {
        this.leftGradientColor = leftGradientColor;
    }

    public void setRightGradientColor(Color rightGradientColor) {
        this.rightGradientColor = rightGradientColor;
    }

    public void setForeground(Color foregroundColor) {
        super.setForeground(foregroundColor);
        this.foregroundColor = foregroundColor;
    }
    
}

