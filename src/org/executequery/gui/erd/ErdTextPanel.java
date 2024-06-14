package org.executequery.gui.erd;


import java.awt.*;
import java.awt.event.MouseEvent;

public class ErdTextPanel extends ErdMoveableComponent {
    private static final int TEXT_MARGIN = 4;

    /**
     * This components calculated width
     */
    private int width = 100;
    /**
     * This components calculated height
     */
    private int height = -1;

    /**
     * The description
     */
    private String erdDescription;

    private String[] descriptionLines;

    public ErdTextPanel(ErdViewerPanel parent,
                        String description) {

        super(parent);

        tableBackground = Color.WHITE;

        String EMPTY = "";


        erdDescription = description == null ? EMPTY : description;

        calculateBounds();
    }

    private void calculateBounds() {
        // build the description lines
        partitionDescription();

        // determine the height of the panel
        FontMetrics fm = getFontMetrics(parent.getTextBlockFont());
        int lineHeight = fm.getHeight() + TEXT_MARGIN + 2;
        int descSize = descriptionLines.length;
        int maxWidth = 100;
        for (int i = 0; i < descriptionLines.length; i++) {
            int strWidth = fm.stringWidth(descriptionLines[i]);
            if (strWidth > maxWidth)
                maxWidth = strWidth;
        }
        width = maxWidth + 20;
        height = ((descSize == 0 ? 1 : descSize) * (fm.getHeight() - fm.getDescent())) +
                (TEXT_MARGIN * 2) + lineHeight;

        fm = null;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private void partitionDescription() {
        descriptionLines = erdDescription.split("\n");
    }

    public void resetValues(String description) {
        setVisible(false);
        String EMPTY = "";
        erdDescription = description == null ? EMPTY : description;
        calculateBounds();
        drawImage((Graphics2D) getGraphics(), 0, 0);

        Rectangle bounds = getBounds();
        setBounds(bounds.x, bounds.y, width, height);
        setVisible(true);
    }

    private void drawImage(Graphics2D g, int offsetX, int offsetY) {

        // set the quality to high for the image
        /*g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);*/

        g.setColor(tableBackground);
        g.fillRect(1 + offsetX, 1 + offsetY, width - 1, height - 1);

        FontMetrics fm = g.getFontMetrics(parent.getTextBlockFont());

        int fontHeight = fm.getHeight();
        int lineHeight = fontHeight + TEXT_MARGIN + 2;

        int textXPosn = TEXT_MARGIN;
        int textYPosn = lineHeight - (fontHeight / 4) - (TEXT_MARGIN / 2) - 1;

        g.setColor(Color.BLACK);
        partitionDescription();
        int descSize = descriptionLines.length;

        if (descSize != 0) {

            int _lineHeight = fontHeight - fm.getDescent();
            g.setFont(parent.getTextBlockFont());

            for (int i = 0; i < descSize; i++) {

                if (i > 0)
                    textYPosn += _lineHeight;
                textXPosn = (width / 2) - (fm.stringWidth(descriptionLines[i]) / 2) + offsetX;
                g.drawString(descriptionLines[i], textXPosn, textYPosn + offsetY);

            }

        }


    }

    public void drawTextPanel(Graphics2D g, int offsetX, int offsetY) {
        drawImage(g, offsetX, offsetY);
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
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        drawTextPanel((Graphics2D) g, 0, 0);
    }

    @Override
    public void doubleClicked(MouseEvent e) {
        new ErdTextBlockDialog(parent, this);
    }

    public String getErdDescription() {
        return erdDescription;
    }

    public void setErdDescription(String erdDescription) {
        this.erdDescription = erdDescription;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), width, height);
    }

    public void clean() {
        parent = null;
    }
}
