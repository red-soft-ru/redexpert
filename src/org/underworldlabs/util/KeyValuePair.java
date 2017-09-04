/*
 * KeyValuePair.java
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

/**
 * Simple key/value pair POJO object.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class KeyValuePair implements java.io.Serializable {
    
    private Object key;
    private Object value;
    
    public KeyValuePair() {}

    public KeyValuePair(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
    
    public Object getKey() {
        return key;
    }
    
}















