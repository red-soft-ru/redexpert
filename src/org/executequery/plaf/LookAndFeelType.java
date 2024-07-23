/*
 * LookAndFeelType.java
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

package org.executequery.plaf;

import org.executequery.localization.Bundles;

public enum LookAndFeelType {

    DEFAULT_LIGHT(bundleString("DefaultLight")),
    DEFAULT_DARK(bundleString("DefaultDark")),
    CLASSIC_LIGHT(bundleString("ClassicLight")),
    CLASSIC_DARK(bundleString("ClassicDark")),
    NATIVE(bundleString("System")),
    PLUGIN(bundleString("UserDefined")),
    LACKEY("Lackey theme");

    private final String description;

    LookAndFeelType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDarkTheme() {
        return this == LookAndFeelType.CLASSIC_DARK || this == LookAndFeelType.DEFAULT_DARK;
    }

    public boolean isClassicTheme() {
        return this == CLASSIC_LIGHT || this == CLASSIC_DARK || this == LACKEY;
    }

    public boolean isDefaultTheme() {
        return this == DEFAULT_LIGHT || this == DEFAULT_DARK;
    }

    private static String bundleString(String key) {
        return Bundles.get(LookAndFeelType.class, key);
    }

    @Override
    public String toString() {
        return getDescription();
    }

}
