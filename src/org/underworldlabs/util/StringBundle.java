/*
 * StringBundle.java
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

package org.underworldlabs.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Resource bundle wrapper.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class StringBundle {
    
    /** the wrapped resource */
    private ResourceBundle bundle;
    
    /** the package name this resource belongs to */
    private String packageName;
    
    /** Creates a new instance of StringBundle */
    public StringBundle(ResourceBundle bundle, String packageName) {
        this.bundle = bundle;
        this.packageName = packageName;
    }

    /**
     * Returns the string from the bundle referenced by the
     * specified key value.
     *
     * @param key the string value's key
     * @return the string value
     */
    public String getString(String key) {
        return bundle.getString(key);
    }

    /**
     * Returns the string from the bundle referenced by the
     * specified key value with the specified argument substituted
     * at position 1.
     *
     * @param key the string value's key
     * @param arg the param argument for position 1
     * @return the string value
     */    
    public String getString(String key, Object arg) {
        Object[] args;
        if (arg == null) {
            args = new Object[0];
        } else {
            args = new Object[] {arg};
        }
        return getString(key, args);
    }

    /**
     * Returns the string from the bundle referenced by the
     * specified key value with the specified arguments substituted
     * at respective positions.
     *
     * @param key the string value's key
     * @param arg the param arguments
     * @return the string value
     */    
    public String getString(String key, Object[] args) {
        if (args == null) {
            args = new Object[0];
        }

        String value = getString(key);
        try {
            return MessageFormat.format(value, args);
        }
        catch (IllegalArgumentException ex) {
            String msg = "Error formatting i18n string for key '" + key + "'";
            Log.error(msg, ex);
            return msg + ": " + ex.toString();
        }
    }
    
    /**
     * Returns the resource bundle wrapped by this object.
     */
    public ResourceBundle getBundle() {
        return bundle;
    }

    /**
     * Returns the package name this bundle represents.
     */
    public String getPackageName() {
        return packageName;
    }
 
    /** resource bundle cache */
    private static Map<String,StringBundle> bundles = 
                            new HashMap<String,StringBundle>();

    /**
     * Loads the resource bundle for the specified class.
     * The bundle will be retrieved from a derived path from
     * the specified class's package name and by appending
     * /resource to the end.
     *
     * @param clazz the clazz to load the bundle for
     * @return the wrapped resource bundle
     */
    public static StringBundle loadBundle(Class clazz) {
        String clazzName = clazz.getName();
        String packageName = clazzName.substring(0, clazzName.lastIndexOf("."));

        String key = packageName.replaceAll("\\.", "/");
        if (!bundles.containsKey(key)) {
            String path = key + "/resource/resources";
            ResourceBundle bundle = 
                    ResourceBundle.getBundle(path, Locale.getDefault());
            bundles.put(key, new StringBundle(bundle, key));
        }

        return bundles.get(key);
    }

}












