/*
 * ExecuteQueryFrame.java
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

import org.executequery.Application;
import org.executequery.GUIUtilities;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.GlassPanePanel;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application frame.
 *
 * @author Takis Diakoumis
 */
public class ExecuteQueryFrame extends JFrame
        implements ComponentListener {

    public static final String TITLE = "RedXpert";

    private static final String APPLICATION_ICON = "ApplicationIcon48.png";

    private static final String WINDOW_POSITION_KEY = "window.position";

    private static final long serialVersionUID = 1L;

    private int lastX;
    private int lastY;
    private int lastWidth;
    private int lastHeight;

    public ExecuteQueryFrame() {

        super(TITLE);
/*
        String[] icons = {
                "ApplicationIcon256.png",
                "ApplicationIcon128.png",
                "ApplicationIcon48.png",
                "ApplicationIcon32.png",
                "ApplicationIcon24.png",
                "ApplicationIcon16.png"
        };
            
        List<Image> images = new ArrayList<Image>();
        for (String icon : icons) {
            
            images.add(GUIUtilities.loadIcon(icon).getImage());
        }
        setIconImages(images);
*/

        ImageIcon frameIcon = GUIUtilities.loadIcon(APPLICATION_ICON);
        setIconImage(frameIcon.getImage());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {

                Application.getInstance().exitProgram();
            }

        });

        addComponentListener(this);

        setLayout(new BorderLayout());
        getRootPane().setGlassPane(new GlassPanePanel());
    }

    public void setSizeAndPosition(int x, int y, int width, int height) {

        lastX = x;
        lastY = y;
        lastWidth = width;
        lastHeight = height;
        setSize(width, height);
        setLocation(x, y);
    }

    public void setSizeAndPosition() {

        String value = getSizeAndPositionPropertyValue();

        String[] values = MiscUtils.splitSeparatedValues(value, ",");

        int x = 0;
        int y = 0;

        int width = 0;
        int height = 0;

        int dimValue = 0;

        for (int i = 0; i < values.length; i++) {

            dimValue = Integer.parseInt(values[i]);

            switch (i) {
                case 0:
                    x = dimValue;
                    break;
                case 1:
                    y = dimValue;
                    break;
                case 2:
                    width = dimValue;
                    break;
                case 3:
                    height = dimValue;
                    break;
            }

        }

        setSizeAndPosition(x, y, width, height);
    }

    public void position() {

        Dimension screenSize = GUIUtils.getDefaultDeviceScreenSize();
        Dimension frameDim = new Dimension(screenSize.width - 200,
                screenSize.height - 150);

        if (userProperties().getBooleanProperty("startup.window.maximized")) {

            setSize(frameDim);
            setExtendedState(JFrame.MAXIMIZED_BOTH);

        } else if (userProperties().containsKey("window.position")) {

            setSizeAndPosition();

        } else { // center the frame

            setSize(frameDim);

            if (frameDim.height > screenSize.height) {

                frameDim.height = screenSize.height;
            }

            if (frameDim.width > screenSize.width) {

                frameDim.width = screenSize.width;
            }

            setLocation((screenSize.width - frameDim.width) / 2,
                    (screenSize.height - frameDim.height) / 2);
        }

    }

    private void savePosition() {

        int x = getX();
        int y = getY();

        int width = getWidth();
        int height = getHeight();

        if (x == lastX && y == lastY &&
                width == lastWidth && height == lastHeight) {

            return;
        }

        lastX = x;
        lastY = y;
        lastWidth = width;
        lastHeight = height;

        String value = createSizeAndPositonPropertyString(x, y, width, height);

        setSizeAndPositionPropertyValue(value);

    }

    private String createSizeAndPositonPropertyString(int x, int y,
                                                      int width, int height) {

        StringBuilder sb = new StringBuilder();

        sb.append(x).
                append(',').
                append(y).
                append(',').
                append(width).
                append(',').
                append(height);

        return sb.toString();
    }

    private void setSizeAndPositionPropertyValue(String value) {

        userProperties().setStringProperty(WINDOW_POSITION_KEY, value);
    }

    private String getSizeAndPositionPropertyValue() {

        return userProperties().getStringProperty(WINDOW_POSITION_KEY);
    }

    private UserProperties userProperties() {

        return UserProperties.getInstance();
    }

    public void componentMoved(ComponentEvent e) {

        savePosition();
    }

    public void componentResized(ComponentEvent e) {

        savePosition();
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

}





