/*
 * IndeterminateProgressBar.java
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

import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class IndeterminateProgressBar extends JComponent
        implements Runnable, ProgressBar {

    private final int scrollWidth;
    private final boolean paintBorder;
    private final Color scrollbarColour;

    private String label;
    private boolean isStopped;
    private boolean inProgress;
    private int animationOffset;
    private boolean fillWhenStopped;

    private Timer timer;

    public IndeterminateProgressBar(boolean paintBorder) {

        this.isStopped = true;
        this.inProgress = false;
        this.paintBorder = paintBorder;

        this.scrollWidth = 20;
        this.animationOffset = scrollWidth * -1;

        this.scrollbarColour = UIUtils.isNativeMacLookAndFeel() ?
                UIManager.getColor("Focus.color") :
                UIManager.getColor("ProgressBar.foreground");

        createTimer();
    }

    @Override
    public void run() {

        animationOffset++;
        repaint();

        if (animationOffset >= 0)
            animationOffset = scrollWidth * -1;
    }

    @Override
    public void start() {

        if (!hasTimer())
            createTimer();

        inProgress = true;
        isStopped = false;
        timer.start();
        repaint();
    }

    @Override
    public void stop() {

        if (hasTimer())
            timer.stop();

        inProgress = false;
        isStopped = true;
        repaint();
    }

    @Override
    public void cleanup() {
        if (hasTimer()) {
            timer.stop();
            timer = null;
        }
    }

    @Override
    public int getHeight() {
        return (int) Math.max(super.getHeight(), getPreferredSize().getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {

        UIUtils.antialias(g);

        int width = getWidth();
        int height = getHeight();

        int y1 = height - 2;
        int y4 = height - 3;

        if (paintBorder) {
            g.setColor(scrollbarColour);
            g.drawRect(0, 0, width - 2, height - 2);
            width--;
        }

        if (!inProgress) {

            if (isStopped && fillWhenStopped) {
                g.setColor(scrollbarColour);
                g.fillRect(0, 0, width, height);
            }

            drawLabel(g, width - 2, height - 2);
            return;
        }

        // set the polygon points
        int[] xPos = {0, 0, 0, 0};
        int[] yPos = {y1, 1, 1, y1};

        // constrain the clip
        g.setClip(0, 1, width, y4);
        g.setColor(hasLabel() ? scrollbarColour.brighter() : scrollbarColour);

        for (int i = 0, k = width + scrollWidth; i < k; i += scrollWidth) {

            xPos[0] = i + animationOffset;
            xPos[1] = xPos[0] + (scrollWidth / 2);
            xPos[2] = xPos[0] + scrollWidth;
            xPos[3] = xPos[1];

            g.fillPolygon(xPos, yPos, 4);
        }

        drawLabel(g, width - 2, height - 2);
    }

    @Override
    public void fillWhenStopped() {
        this.fillWhenStopped = true;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void resetLabel() {
        this.label = null;
    }

    private void drawLabel(Graphics g, int containerWidth, int containerHeight) {

        if (!hasLabel())
            return;

        Font font = UIManager.getDefaults().getFont("Label.font");
        Color textColor = new JLabel().getForeground();
        FontMetrics fontMetrics = g.getFontMetrics(font);

        int x = (containerWidth - fontMetrics.stringWidth(label)) / 2;
        int y = ((containerHeight - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();

        g.setFont(font);
        g.setColor(textColor);
        g.drawString(label, x, y);
    }

    private void createTimer() {
        timer = new Timer(25, e -> run());
        timer.setInitialDelay(0);
    }

    private boolean hasTimer() {
        return timer != null;
    }

    private boolean hasLabel() {
        return label != null && !label.isEmpty();
    }

}
