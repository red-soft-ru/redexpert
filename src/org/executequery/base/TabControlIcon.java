/*
 * TabControlIcon.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.UIManager;

import org.underworldlabs.swing.plaf.UIUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1498 $
 * @date     $Date: 2015-09-18 10:16:35 +1000 (Fri, 18 Sep 2015) $
 */
public interface TabControlIcon extends Icon {
    
    /** the icon width */
    public static final int ICON_WIDTH = 7;
    
    /** the icon height */
    public static final int ICON_HEIGHT = 7;

    /** The icon image colour */
    public static final Color ICON_COLOR = 
            UIUtils.getColour("executequery.TabbedPane.icon", UIManager.getColor("controlShadow").darker().darker());
    
}
