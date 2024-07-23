/*
 * TabControlIcon.java
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

package org.executequery.base;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public interface TabControlIcon extends Icon {

    static int iconWidth() {
        return GUIUtilities.getLookAndFeel().isDefaultTheme() ? 16 : 7;
    }

    static int iconHeight() {
        return iconWidth();
    }

    static Color iconColor() {
        return GUIUtilities.getLookAndFeel().isDefaultTheme() ?
                SystemProperties.getColourProperty("user", "editor.text.selection.background") :
                UIUtils.getColour("executequery.TabbedPane.icon", UIManager.getColor("controlShadow").darker().darker());
    }

}
