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

import org.executequery.util.UserProperties;
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

    /**
     * default icon resource path in this package tree
     */
    private static final String ICON_PATH = "/org/underworldlabs/swing/icons/";

    /**
     * Icons repository
     */
    private static Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

    public static ImageIcon loadImage(String name) {

        return new ImageIcon(IconUtilities.class.getResource(name));
    }

    public static ImageIcon loadIcon(String name) {
        return loadIcon(name, false);
    }

    public static ImageIcon loadIcon(String name, int iconSize) {
        return loadIcon(name, false, iconSize, iconSize);
    }

    public static ImageIcon loadIcon(String name, boolean store) {
        return loadIcon(name, store, UserProperties.getInstance().getIntProperty("icons.size"),UserProperties.getInstance().getIntProperty("icons.size"));
    }

    public static ImageIcon loadIcon(String name, boolean store, int width, int height) {
        ImageIcon icon = null;

        if (icons.containsKey(name)) {

            icon = icons.get(name);

        } else {

            URL url = IconUtilities.class.getResource(name);
            if (url != null) {
                if (url.getPath().endsWith(".svg")) {
                    try {
                        BufferedImage image = SVGImage.fromSvg(url, width, height);
                        icon = new ImageIcon(image);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                } else
                    icon = new ImageIcon(url);

            }
            // try the default resource path
            else {
                icon = loadDefaultIconResource(name, false);
            }

            if (store) {
                icons.put(name, icon);
            }
        }
        return icon;
    }

    public static ImageIcon loadDefaultIconResource(String name, boolean store) {

        ImageIcon icon = null;
        name = ICON_PATH + name;
        if (icons.containsKey(name)) {

            icon = icons.get(name);

        } else {

            URL url = IconUtilities.class.getResource(name);
            if (url == null) {

                throw new RuntimeException("Icon at resource path not found: " +
                        name);
            }
            icon = new ImageIcon(url);

            if (store) {
                icons.put(name, icon);
            }
        }
        return icon;
    }

    // http://codehustler.org/blog/java-to-create-grayscale-images-icons/
    
    /*
    private Icon getGray(ImageIcon icon) {
        
        BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics g = bufferedImage.createGraphics();
        g.drawImage(icon.getImage(), 0, 0, null);
        g.dispose();
        
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null); 
        BufferedImage image = op.filter(bufferedImage, null);
        
        return new ImageIcon(image);
    }
    */

    private IconUtilities() {
    }

}



