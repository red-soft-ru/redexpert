/*
 * PreferenceChangeEvent.java
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

package org.executequery.gui.prefs;

/**
 *
 * @author   Takis Diakoumis
 */
public class PreferenceChangeEvent {

    private String key;
    
    private Object value;
    
    private Object source;

    public PreferenceChangeEvent(Object source, String key, Object value) {

        this.source = source;
        this.key = key;
        this.value = value;
    }
    
    public Object getValue() {
     
        return value;
    }
    
    public String getKey() {
     
        return key;
    }
    
    public Object getSource() {
     
        return source;
    }
    
}

