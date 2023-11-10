/*
 * LineNumber.java
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

package org.executequery.components;

import org.executequery.GUIUtilities;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class LineNumber extends JComponent {

    private static final int HEIGHT = Integer.MAX_VALUE - 1000000;
    private static final int MARGIN = 5;

    private FontMetrics fontMetrics;
    private int lineHeight;
    private int currentRowWidth;
    private int executingLine;
    private int totalRows;

    private final JComponent component;
    private int componentFontHeight;
    private int componentFontAscent;

    private final Image executingIcon;
    private final int iconHeight;
    private final int iconWidth;

    private List<String> borderLabels;

    /**
     * Convenience constructor for Text Components.
     */
    public LineNumber(JComponent component) {

        setForeground(foregroundColour());
        setBackground(backgroundColour());

        this.component = (component != null) ? component : this;
        this.borderLabels = new ArrayList<>();

        Font font = this.component.getFont();
        setFont(this.component.getFont());

        if (font != null) {
            componentFontHeight = this.component.getFontMetrics(this.component.getFont()).getHeight();
            componentFontAscent = this.component.getFontMetrics(this.component.getFont()).getAscent();
        }

        ImageIcon icon = GUIUtilities.loadIcon("ExecutingPointer.svg", true);
        iconWidth = icon.getIconWidth();
        iconHeight = icon.getIconHeight();
        executingIcon = icon.getImage();

        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, GUIUtilities.getDefaultBorderColour().darker()));
        setPreferredWidth(9999);
        totalRows = 1;
    }

    /**
     * Using FontMetrics, calculate the width of the given integer and then
     * set the preferred size of the component.
     */
    public void setPreferredWidth(int row) {

        if (fontMetrics == null)
            return;

        int width = fontMetrics.stringWidth(String.valueOf(row));
        if (currentRowWidth < width) {
            currentRowWidth = width;
            setPreferredSize(new Dimension(2 * MARGIN + width, HEIGHT));
        }
    }

    public void updatePreferences(Font font) {
        setFont(font);
        setForeground(foregroundColour());
        setBackground(backgroundColour());
    }

    /**
     * Reset variables that are dependent on the font.
     */
    @Override
    public void setFont(Font font) {

        super.setFont(font);

        fontMetrics = getFontMetrics(getFont());
        if (fontMetrics != null) {
            componentFontHeight = fontMetrics.getHeight();
            componentFontAscent = fontMetrics.getAscent();
        }
    }

    /**
     * The line height defaults to the line height of the font for this component.
     */
    public int getLineHeight() {
        return (lineHeight == 0) ? componentFontHeight : lineHeight;
    }

    /**
     * Override the default line height with a positive value.
     * For example, when you want line numbers for a JTable you could
     * use the JTable row height.
     */
    @SuppressWarnings("unused")
    public void setLineHeight(int lineHeight) {
        if (lineHeight > 0) {
            this.lineHeight = lineHeight;
        }
    }

    /**
     * Sets the total row count on the border and
     * calls a repaint if required.
     */
    public void setRowCount(int rows) {

        if (totalRows != rows) {
            totalRows = rows;
            repaint();
        }
    }

    public int getStartOffset() {
        return component.getInsets().top + componentFontAscent;
    }

    @Override
    public void paintComponent(Graphics g) {

        UIUtils.antialias(g);

        int lineHeight = getLineHeight();
        int startOffset = getStartOffset();
        Rectangle drawHere = g.getClipBounds();

        // Paint the background
        g.setColor(getBackground());
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
        g.setColor(getForeground());

        // Determine the number of lines to draw in the foreground.
        int startLineNumber = (drawHere.y / lineHeight) + 1;
        int tempEndLineNumber = startLineNumber + (drawHere.height / lineHeight);
        int endLineNumber = Math.min(totalRows, tempEndLineNumber);

        int start = (drawHere.y / lineHeight) * lineHeight + startOffset;

        for (int i = startLineNumber; i <= endLineNumber; i++) {

            String lineLabel = String.valueOf(i);
            if (!borderLabels.isEmpty())
                lineLabel = (borderLabels.size() > i - 1) ? borderLabels.get(i - 1) : "";

            int width = fontMetrics.stringWidth(lineLabel);

            if (executingLine == i) {
                g.drawImage(executingIcon(),
                        MARGIN + currentRowWidth - width - 2,
                        start - iconHeight + 2,
                        iconWidth,
                        iconHeight,
                        this
                );
            } else
                g.drawString(lineLabel, MARGIN + currentRowWidth - width, start);

            start += lineHeight;
        }

        setPreferredWidth(endLineNumber);
    }

    public void resetExecutingLine() {

        resetBorderLabels();
        if (executingLine != -1) {
            executingLine = -1;
            repaint();
        }
    }

    public void setBorderLabels(List<String> borderLabels) {
        this.borderLabels = borderLabels;
    }

    public void resetBorderLabels() {
        this.borderLabels = new ArrayList<>();
    }

    public void setExecutingLine(int lineNumber) {
        resetBorderLabels();
        executingLine = lineNumber + 1;
    }

    private Color backgroundColour() {
        return UserProperties.getInstance().getColourProperty("editor.linenumber.background");
    }

    private Color foregroundColour() {
        return UserProperties.getInstance().getColourProperty("editor.linenumber.foreground");
    }

    private Image executingIcon() {
        return executingIcon;
    }

}
