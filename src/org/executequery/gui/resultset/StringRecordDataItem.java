/*
 * StringRecordDataItem.java
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

package org.executequery.gui.resultset;

public class StringRecordDataItem extends SimpleRecordDataItem {

    private static final int DATA_TYPE_INT = -1;

    private static final String DATA_TYPE_NAME = "Simple String Record Data Item";
    
    public StringRecordDataItem(String value) {

        super(DATA_TYPE_NAME, DATA_TYPE_INT, DATA_TYPE_NAME);
        setValue(value);
    }
    
}






