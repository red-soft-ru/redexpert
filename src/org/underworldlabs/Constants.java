/*
 * Constants.java
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

package org.underworldlabs;

import java.awt.Dimension;
import java.awt.Insets;

/**
 * Static constants.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public interface Constants {

    /** An empty string */
    public static final String EMPTY = "";
    
    public static final String NEW_LINE_STRING = "\n";
    public static final String QUOTE_STRING = "'";
    public static final char QUOTE_CHAR = '\'';
    public static final char NEW_LINE_CHAR = '\n';
    public static final char TAB_CHAR = '\t';
    public static final char COMMA_CHAR = ',';

    // tool tip html tags
    public static final String TABLE_TAG_START = 
            "<table border='0' cellspacing='0' cellpadding='2'>";

    public static final String TABLE_TAG_END = 
            "</table>";

    public static final Insets EMPTY_INSETS = new Insets(0,0,0,0);

    public static final Dimension FORM_BUTTON_SIZE = new Dimension(100, 25);
                                             
}















