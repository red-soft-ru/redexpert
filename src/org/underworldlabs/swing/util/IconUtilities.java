/*
 * IconUtilities.java
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

package org.underworldlabs.swing.util;

import org.executequery.gui.browser.BrowserConstants;
import org.executequery.log.Log;
import org.underworldlabs.swing.plaf.SVGImage;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Icon and image loader and cache.<br>
 * This aims to load images from jar file resources not
 * local file system paths.
 *
 * @author Takis Diakoumis
 */
public class IconUtilities {

    private static final Map<String, ImageIcon> icons = new HashMap<>();

    public static ImageIcon loadImage(String name) {
        URL url = IconUtilities.class.getResource(name);
        return url != null ? new ImageIcon(url) : null;
    }

    public static ImageIcon loadIcon(String name) {
        return loadIcon(name, false);
    }

    public static ImageIcon loadIcon(String name, int iconSize) {
        return loadIcon(name, false, iconSize, iconSize);
    }

    public static ImageIcon loadIcon(String name, boolean store) {
        return loadIcon(name, store, -1, -1);
    }

    public static ImageIcon loadIcon(String name, boolean store, int iconSize) {
        return loadIcon(name, store, iconSize, iconSize);
    }

    public static ImageIcon loadIcon(String path, boolean store, int width, int height) {

        if (icons.containsKey(path))
            return icons.get(path);

        URL url = IconUtilities.class.getResource(path);
        if (url == null) {

            String message = "icon with path [" + path + "] not found";
            if (path.contains(BrowserConstants.LIGHT_SUFFIX)) {
                Log.info(message + ", trying to load non light icon");
                return loadIcon(path.replaceFirst(BrowserConstants.LIGHT_SUFFIX, ""), store, width, height);
            }

            Log.info(message);
            return null;
        }

        ImageIcon icon = null;
        if (url.getPath().endsWith(".svg")) {
            try {
                BufferedImage image = SVGImage.fromSvg(url, width, height);
                icon = new ImageIcon(image);

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }

        } else
            icon = new ImageIcon(url);

        if (store && icon != null)
            icons.put(path, icon);

        return icon;
    }

}
