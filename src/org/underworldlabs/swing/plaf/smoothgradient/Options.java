/*
 * Options.java
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

package org.underworldlabs.swing.plaf.smoothgradient;

import javax.swing.*;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the
 *           release of version 3.0.0beta1 has meant a
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * Provides access to several optional properties for the
 * JGoodies L&amp;Fs, either by a key to the <code>UIDefaults</code> table
 * or via a method or both.
 *
 * @author Karsten Lentzsch
 * @author Takis Diakoumis
 */
public final class Options {

    public static final String IS_NARROW_KEY = "jgoodies.isNarrow";
    public static final String FONT_SIZE_HINTS_KEY = "jgoodies.fontSizeHints";

    /**
     * Answers the global <code>FontSizeHints</code>, can be overridden
     * by look specific setting.
     */
    public static FontSizeHints getGlobalFontSizeHints() {
        Object value = UIManager.get(FONT_SIZE_HINTS_KEY);
        if (value != null)
            return (FontSizeHints) value;

        String name = SmoothGradientLookUtils.getSystemProperty(FONT_SIZE_HINTS_KEY, "");
        try {
            return FontSizeHints.valueOf(name);
        } catch (IllegalArgumentException e) {
            return FontSizeHints.DEFAULT;
        }
    }

    /**
     * Sets the global <code>FontSizeHints</code>.
     */
    public static void setGlobalFontSizeHints(FontSizeHints hints) {
        UIManager.put(FONT_SIZE_HINTS_KEY, hints);
    }

}
