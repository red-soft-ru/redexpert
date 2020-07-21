/*
 * SplashPanel.java
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

import java.awt.*;

/**
 * This class creates a splash panel to the size of the image to be
 * displayed. The panel is displayed for as long as is required to
 * load required classes and build the application frame and associated
 * components.
 *
 * @author Takis Diakoumis
 */
public class SplashPanel extends Canvas {

    /**
     * This object's font metrics
     */
    private FontMetrics fontMetrics;

    /**
     * The window displayed
     */
    private Window window;

    /**
     * The splash image
     */
    private Image image;

    /**
     * The off-screen image
     */
    private Image offscreenImg;

    /**
     * The off-screen graphics
     */
    private Graphics offscreenGfx;

    /**
     * The startup progress posiiton
     */
    private int progress;

    /**
     * The version info string
     */
    private String version;

    /**
     * The progress bar's colour
     */
    private final Color progressColour;

    /**
     * the light gradient colour
     */
    private final Color gradientColour;

    /**
     * the x-coord of the version string
     */
    private int versionLabelX;

    /**
     * the y-coord of the version string
     */
    private int versionLabelY;

    /**
     * The progress bar height
     */
    private static final int PROGRESS_HEIGHT = 15;

    private final Color versionTextColour;

    /**
     * Creates a new instance of the splash panel.
     */
    public SplashPanel(Color progressBarColour,
                       String imageResourcePath,
                       String versionNumber) {

        this(progressBarColour, imageResourcePath, versionNumber, -1, -1);
    }

    public SplashPanel(Color progressBarColour,
                       String imageResourcePath,
                       String versionNumber,
                       int versionLabelX,
                       int versionLabelY) {

        this(progressBarColour, imageResourcePath, versionNumber,
                Color.WHITE, versionLabelX, versionLabelY);
    }

    public SplashPanel(Color progressBarColour,
                       String imageResourcePath,
                       String versionNumber,
                       Color versionTextColour,
                       int versionLabelX,
                       int versionLabelY) {

        this.versionTextColour = versionTextColour;
        this.versionLabelX = versionLabelX;
        this.versionLabelY = versionLabelY;

        progressColour = progressBarColour;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setBackground(Color.white);

        gradientColour = UIUtils.getBrighter(progressBarColour, 0.75);

        //Font font = new Font("Dialog", Font.BOLD, 15);
        Font font = new Font("Dialog", Font.PLAIN, 14);
        setFont(font);
        fontMetrics = getFontMetrics(font);

        image = getToolkit().getImage(getClass().getResource(imageResourcePath));

        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(image, 0);

        if (versionNumber != null) {

            //version = "Version " + versionNumber;
            version = versionNumber;
        }

        try {

            tracker.waitForAll();

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        window = new Window(new Frame());

        Dimension size = new Dimension(image.getWidth(this), image.getHeight(this));
        window.setSize(size);

        window.setLayout(new BorderLayout());
        window.add(BorderLayout.CENTER, this);

        window.setLocation(GUIUtils.getPointToCenter(null, size));

        window.validate();
        window.setVisible(true);
    }

    public synchronized void advance() {

        progress++;
        repaint();

        // wait for it to be painted to ensure
        // progress is updated continuously

        try {

            wait();

        } catch (InterruptedException ie) {
        }

    }

    @Override
    public synchronized void paint(Graphics g) {

        Dimension size = getSize();

        if (offscreenImg == null) {
            offscreenImg = createImage(size.width, size.height);
            offscreenGfx = offscreenImg.getGraphics();
            offscreenGfx.setFont(getFont());
        }

        offscreenGfx.drawImage(image, 0, 0, this);

        offscreenGfx.setColor(progressColour);
        /*
        offscreenGfx.fillRect(0, 
                              image.getHeight(this) - PROGRESS_HEIGHT,
                              (window.getWidth() * progress) / 9, 
                              PROGRESS_HEIGHT);
        */

        Graphics2D offscreenGfx2d = (Graphics2D) offscreenGfx;

        offscreenGfx2d.setPaint(new GradientPaint(0,
                image.getHeight(this) - PROGRESS_HEIGHT,
                gradientColour,//new Color(95,95,190),
                0,
                image.getHeight(this), progressColour));

        offscreenGfx.fillRect(0,
                image.getHeight(this) - PROGRESS_HEIGHT,
                (window.getWidth() * progress) / 9,
                PROGRESS_HEIGHT);

        if (version != null) {

            if (versionLabelX == -1) {

                versionLabelX = (getWidth() - fontMetrics.stringWidth(version)) / 2;
            }

            if (versionLabelY == -1) {

                // if no y value - set just above progress bar
                versionLabelY = image.getHeight(this) - PROGRESS_HEIGHT - fontMetrics.getHeight();
            }

            offscreenGfx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            offscreenGfx2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            offscreenGfx2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            offscreenGfx2d.setColor(versionTextColour);
            offscreenGfx2d.drawString(version,
                    versionLabelX,
                    versionLabelY);
        }

        g.drawImage(offscreenImg, 0, 0, this);

        notify();
    }

    public void dispose() {

        // wait a moment
        try {

            Thread.sleep(700);
//            Thread.sleep(90000);

        } catch (InterruptedException e) {
        }

        window.dispose();
    }

    @Override
    public void update(Graphics g) {

        paint(g);
    }

}






