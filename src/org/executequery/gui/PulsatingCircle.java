/*
 * PulsatingCircle.java
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

package org.executequery.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Takis Diakoumis
 */
public class PulsatingCircle extends ImageIcon {

    private final Component owner;

    private final int radius;

    private final Timer timer;

    private float alpha = 0f;

    private boolean goingUp;

    public PulsatingCircle(Component owner, int radius) {

        this.owner = owner;
        this.radius = radius;

        final Runnable fader = new Runnable() {
            public void run() {

                if (!goingUp) {

                    alpha -= 0.010f;
                    if (alpha < 0) {

                        alpha = 0.0f;
                        goingUp = true;
                    }

                } else {

                    alpha += 0.025f;
                    if (alpha > 1.0f) {

                        alpha = 1.0f;
                        goingUp = false;
                    }
                }

                PulsatingCircle.this.owner.repaint();
            }

        };

        TimerTask paintImage = new TimerTask() {
            public void run() {

                EventQueue.invokeLater(fader);
            }
        };

        timer = new Timer();
        timer.schedule(paintImage, 400, 25);
    }

    @Override
    public int getIconWidth() {

        return (radius * 2) + 6;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {

        Graphics2D g2d = (Graphics2D) g;

        Composite composite = g2d.getComposite();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int componentHeight = c.getHeight();

        int width = radius * 2;
        int height = width;

        g2d.setColor(Color.YELLOW);
        g2d.fillOval(8, (componentHeight - height) / 2, width, height);

        g2d.setComposite(composite);
    }

}

