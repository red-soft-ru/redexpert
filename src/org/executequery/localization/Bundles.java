/*
 * Bundles.java
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

package org.executequery.localization;

import java.util.Locale;
import java.util.ResourceBundle;

import org.executequery.util.StringBundle;
import org.underworldlabs.util.MiscUtils;

public final class Bundles {

    private static StringBundle stringBundle;

    public static String get(String key) {

        return bundle().getString(key);
    }

    public static String getCommon(String key) {

        return bundle().getString("common." + key);
    }

    public static String[] getCommons(String[] keys) {

        String[] commonKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {

            if (!MiscUtils.isNull(keys[i])) {
             
                commonKeys[i] = getCommon(keys[i]);
            }

        }
        return commonKeys;
    }

    public static String[] get(Class<?> clazz, String[] keys) {

        for (int i = 0; i < keys.length; i++) {

            if (!MiscUtils.isNull(keys[i])) {
             
                keys[i] = get(clazz, keys[i]);
            }

        }
        return keys;
    }

    public static String get(String key, Object... args) {

        return bundle().getString(key, args);
    }

    public static String get(Class<?> clazz, String key) {

        return bundle().getString(keyForClazz(clazz, key));
    }

    public static String get(Class<?> clazz, String key, Object... args) {

        return bundle().getString(keyForClazz(clazz, key), args);
    }

    private static StringBundle bundle() {

        if (stringBundle == null) {

            String packageName = Bundles.class.getPackage().getName();
            String path = packageName.replaceAll("\\.", "/") + "/resources";

            ResourceBundle bundle = ResourceBundle.getBundle(path,
                    Locale.getDefault());
            stringBundle = new StringBundle(bundle);
        }

        return stringBundle;
    }

    private static String keyForClazz(Class<?> clazz, String key) {

        return clazz.getSimpleName() + "." + key;
    }

}
