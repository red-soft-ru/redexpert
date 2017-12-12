package org.underworldlabs.swing.hexeditor.textgrid;

import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.text.AttributedString;

public class TextGrid extends JComponent implements TextGridModelListener, Scrollable {

    // CONSTANTS
    public static final String FONT = SystemProperties.getProperty("user", "sqlsyntax.font.name");
    public static final int FONT_SIZE = SystemProperties.getIntProperty("user", "sqlsyntax.font.size");
    public static final Font PLAIN_FONT = new Font(FONT, Font.PLAIN, FONT_SIZE);
    public static final Font BOLD_FONT = new Font(FONT, Font.BOLD, FONT_SIZE);
    public static final Font ITALIC_FONT = new Font(FONT, Font.ITALIC, FONT_SIZE);
    public static final Font BOLD_ITALIC_FONT = new Font(FONT, Font.BOLD | Font.ITALIC, FONT_SIZE);

    public static final int PLAIN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int STRIKETHROUGH = 4;
    public static final int UNDERLINE = 8;
    public static final int UNDERLINE_LOW = 16;
    public static final int UNDERLINE_LOW_DASHED = 32;
    public static final int UNDERLINE_LOW_DOTTED = 64;
    public static final int UNDERLINE_LOW_DOUBLE = 128;

    // MEMBERS
    protected int charHeight;
    protected int charWidth;
    protected int charDescent;
    protected int leftMargin;
    protected int topMargin;

    protected TextGridModel model;
    protected TextGridCursor cursor;

    /**
     * Construct the editor with a document.
     */
    public TextGrid(TextGridModel model) {
        setFont(PLAIN_FONT);
        setModel(model);

        topMargin = 2;
        leftMargin = 2;

        setOpaque(true);
        setFocusable(true);
        setAutoscrolls(true);
    }

    public void setFont(Font font) {
        super.setFont(font);
        charHeight = getFontMetrics(font).getHeight() - 1;  // TODO: Figure out why this works better.
        charWidth = getFontMetrics(font).charWidth('0'); // Assume fixed width!
        charDescent = getFontMetrics(font).getDescent();
    }

    // MODEL STUFF

    public TextGridModel getModel() {
        return model;
    }

    public void setModel(TextGridModel model) {
        if (model != null)
            model.removeTextGridModelListener(this);

        this.model = model;

        if (model != null)
            model.addTextGridModelListener(this);

        revalidate();
        repaint();
    }

    // model can change its column count, so this is now dynamic
    public Dimension getPreferredSize() {
        return new Dimension(leftMargin + model.getColumnCount() * charWidth,
                topMargin + model.getRowCount() * charHeight);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // CURSOR STUFF

    public TextGridCursor getTextGridCursor(TextGridCursor cursor) {
        return this.cursor;
    }

    public void setTextGridCursor(TextGridCursor cursor) {
        this.cursor = cursor;
        cursor.install(this);
    }

    // DIMENSION STUFF

    /**
     * Get the row count.
     */
    public int getRowCount() {
        return model.getRowCount();
    }

    /**
     * Get the column count.
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    // VIEW - MODEL STUFF

    /**
     * Convert a screen point to row and column position.
     */
    public Point viewToModel(Point p) {
        int row = (p.y - topMargin) / charHeight;
        int col = (p.x - leftMargin) / charWidth;
        row = row < 0 ? 0 : row;
        row = row >= getRowCount() ? getRowCount() - 1 : row;

        col = col < 0 ? 0 : col;
        col = col >= getColumnCount() ? getColumnCount() - 1 : col;

        return new Point(col, row);
    }

    /**
     * Convert a row/column to a rectangle on the screen.
     */
    public Rectangle modelToView(int row, int col) {
        return new Rectangle(col * charWidth + leftMargin, row * charHeight + topMargin, charWidth, charHeight);
    }

    /**
     * Get the character at a particular locaiton.
     */
    public char getCharAt(int row, int col) {
        return model.getCharAt(row, col);
    }

    /**
     * Get a character's fg colour.
     */
    public Color getCharColor(int row, int col) {
        return model.getCharColor(row, col);
    }

    /**
     * Get a character's bg colour.
     */
    public Color getCharBackground(int row, int col) {
        return model.getCharBackground(row, col);
    }

    /**
     * Get a characters style.
     */
    public int getCharStyle(int row, int col) {
        return model.getCharStyle(row, col);
    }

    // TEXT GRID MODEL LISTENER INTERFACE
    public void textGridUpdated(TextGridModelEvent e) {
        revalidate();
        repaint();
    }

    // SCROLLABLE INTERFACE
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        int result = 0;
        if (orientation == SwingConstants.VERTICAL) {
            if (direction < 0)
                result = (visibleRect.y - topMargin) % charHeight;
            else
                result = charHeight - (visibleRect.y - topMargin) % charHeight;

            if (result <= 0) result = charHeight;
        } else if (orientation == SwingConstants.HORIZONTAL) {
            if (direction < 0)
                result = (visibleRect.x - leftMargin) % charWidth;
            else
                result = charWidth - (visibleRect.x - leftMargin) % charWidth;

            if (result <= 0) result = charWidth;
        }

        return result;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        int result = 0;

        if (orientation == SwingConstants.VERTICAL) {
            result = visibleRect.height - charHeight;
            result += getScrollableUnitIncrement(new Rectangle(visibleRect.x,
                    result + visibleRect.y,
                    visibleRect.width,
                    visibleRect.height), orientation, direction);
        } else if (orientation == SwingConstants.HORIZONTAL) {
            result = visibleRect.width - charWidth;
            result += getScrollableUnitIncrement(new Rectangle(result + visibleRect.x,
                    visibleRect.y,
                    visibleRect.width,
                    visibleRect.height), orientation, direction);
        }

        return result;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * Paint the component.
     */
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (isOpaque()) {
            g2d.setColor(getBackground());
            g2d.fill(g2d.getClip());
        }

        Rectangle bounds = g2d.getClipBounds();

        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        //                     RenderingHints.VALUE_ANTIALIAS_ON);

        Point minRowCol = viewToModel(new Point(bounds.x, bounds.y));
        Point maxRowCol = viewToModel(new Point(bounds.x + bounds.width, bounds.y + bounds.height));

        String line;

        Rectangle rect;
        Color bgColor;
        Color fgColor;
        int style;
        int i, j;

        // Draw the text layer
        for (i = minRowCol.y; i <= maxRowCol.y; i++) {
            int lastStyle = 0;
            int lastStyleIdx = 0;

            Color lastBg = getBackground();
            int lastBgIdx = 0;

            Color lastFg = getForeground();
            int lastFgIdx = 0;

            rect = modelToView(i, 0);
            int baseLine = rect.y + rect.height - charDescent;

            AttributedString as = new AttributedString(getRowText(i));
            as.addAttribute(TextAttribute.FONT, getFont());

            for (j = minRowCol.x; j <= maxRowCol.x; j++) {
                if (cursor != null && cursor.isSelectionVisible() && cursor.isSelected(i, j)) {
                    bgColor = cursor.getSelectionColor();
                    fgColor = cursor.getSelectedTextColor();
                } else {
                    bgColor = model.getCharBackground(i, j);
                    fgColor = model.getCharColor(i, j);
                }

                if (!lastFg.equals(fgColor)) {
                    if (j > 0)
                        as.addAttribute(TextAttribute.FOREGROUND, lastFg, lastFgIdx, j);
                    lastFg = fgColor;
                    lastFgIdx = j;
                }

                if (!lastBg.equals(bgColor)) {
                    if (j > 0)
                        as.addAttribute(TextAttribute.BACKGROUND, lastBg, lastBgIdx, j);
                    lastBg = bgColor;
                    lastBgIdx = j;
                }

                style = model.getCharStyle(i, j);

                if (lastStyle != style) {
                    if (lastStyle > 0) {
                        if ((lastStyle & BOLD) > 0 && (lastStyle & ITALIC) > 0)
                            as.addAttribute(TextAttribute.FONT, BOLD_ITALIC_FONT, lastStyleIdx, j);
                        else if ((lastStyle & ITALIC) > 0)
                            as.addAttribute(TextAttribute.FONT, ITALIC_FONT, lastStyleIdx, j);
                        else if ((lastStyle & BOLD) > 0)
                            as.addAttribute(TextAttribute.FONT, BOLD_FONT, lastStyleIdx, j);

                        if ((lastStyle & STRIKETHROUGH) > 0)
                            as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, lastStyleIdx, j);

                        if ((lastStyle & UNDERLINE) > 0)
                            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, lastStyleIdx, j);
                        else if ((lastStyle & UNDERLINE_LOW) > 0)
                            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, lastStyleIdx, j);
                        else if ((lastStyle & UNDERLINE_LOW_DASHED) > 0)
                            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DASHED, lastStyleIdx, j);
                        else if ((lastStyle & UNDERLINE_LOW_DOTTED) > 0)
                            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED, lastStyleIdx, j);
                        else if ((lastStyle & UNDERLINE_LOW_DOUBLE) > 0)
                            as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, lastStyleIdx, j);
                    }

                    lastStyle = style;
                    lastStyleIdx = j;
                }
            }

            // Apply remaining color changes
            as.addAttribute(TextAttribute.FOREGROUND, lastFg, lastFgIdx, j);
            as.addAttribute(TextAttribute.BACKGROUND, lastBg, lastBgIdx, j);

            // Apply remaining style change
            if (lastStyle > 0) {
                if ((lastStyle & BOLD) > 0 && (lastStyle & ITALIC) > 0)
                    as.addAttribute(TextAttribute.FONT, BOLD_ITALIC_FONT, lastStyleIdx, j);
                else if ((lastStyle & ITALIC) > 0)
                    as.addAttribute(TextAttribute.FONT, ITALIC_FONT, lastStyleIdx, j);
                else if ((lastStyle & BOLD) > 0)
                    as.addAttribute(TextAttribute.FONT, BOLD_FONT, lastStyleIdx, j);

                if ((lastStyle & STRIKETHROUGH) > 0)
                    as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, lastStyleIdx, j);

                if ((lastStyle & UNDERLINE) > 0)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, lastStyleIdx, j);
                else if ((lastStyle & UNDERLINE_LOW) > 0)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, lastStyleIdx, j);
                else if ((lastStyle & UNDERLINE_LOW_DASHED) > 0)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DASHED, lastStyleIdx, j);
                else if ((lastStyle & UNDERLINE_LOW_DOTTED) > 0)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED, lastStyleIdx, j);
                else if ((lastStyle & UNDERLINE_LOW_DOUBLE) > 0)
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, lastStyleIdx, j);
            }

            g2d.translate(leftMargin, baseLine);
            g2d.drawString(as.getIterator(), 0, 0);
            g2d.translate(-leftMargin, -baseLine);
        }

        // Draw the caret
        if (cursor != null && shouldDrawCursor())
            cursor.paint(g2d);
    }

    protected boolean shouldDrawCursor() {
        return hasFocus() && isEnabled();
    }

    public int getSelectionStartVerticalOffset() {
        int offset = 0;
        if (cursor != null && cursor.isSelectionVisible()) {
            Point p = cursor.getSelectionStart();
            offset = charHeight * p.y;
        }
        return offset;
    }

    public String getSelectedText() {
        String selectedText = null;
        if (cursor != null && cursor.isSelectionVisible()) {
            Point start = cursor.getSelectionStart();
            StringBuilder sb = new StringBuilder();
            int j = start.x;
            for (int i = start.y; i < getRowCount(); i++) {
                for (; j < getColumnCount(); j++) {
                    if (!cursor.isSelected(i, j))
                        break;
                    sb.append(getCharAt(i, j));
                }
                j = 0;
            }
            selectedText = sb.toString();
        }
        return selectedText;
    }

    //////////////////////////
    // PROTECTED METHODS

    private String getRowText(int row) {
        char[] chars = new char[getColumnCount()];
        for (int j = 0; j < chars.length; j++)
            chars[j] = getCharAt(row, j);
        return new String(chars);
    }
}
