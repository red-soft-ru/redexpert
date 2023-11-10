/*
 * PropertiesRootPanel.java
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

package org.executequery.gui.prefs;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class PropertiesRootPanel extends JPanel
        implements UserPreferenceFunction {

    private Color darkColour;
    private Color lightColour;
    private Image preferencesImage;
    private Image textImage;

    public PropertiesRootPanel() {

        darkColour = new Color(107, 165, 237);
        lightColour = new Color(187, 209, 236);

        ImageIcon icon = new ImageIcon(getClass().getResource(
                "/org/executequery/images/AboutText.svg"));
        preferencesImage = icon.getImage();

        icon = new ImageIcon(getClass().getResource(
                "/org/executequery/images/PreferencesText.svg"));
        textImage = icon.getImage();
    }

    public void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        g2d.setPaint(new GradientPaint(0, 0, darkColour, width, height, lightColour));
        g2d.fillRect(0, 0, width, height);

        int xOffset = -10;
        int yOffset = height - preferencesImage.getHeight(this) - 15;
        g2d.drawImage(preferencesImage, xOffset, yOffset, this);

        xOffset = width - textImage.getWidth(this) - 15;
        yOffset = height - textImage.getHeight(this) - 20;
        g2d.drawImage(textImage, xOffset, yOffset, this);
    }

    public void save() {
    }

    public void restoreDefaults() {
    }

    public void preferenceChange(PreferenceChangeEvent e) {
        PropertiesPanel.checkAndSetRestartNeed(e.getKey());
    }

    public void addPreferenceChangeListener(PreferenceChangeListener preferenceChangeListener) {
    }

}


