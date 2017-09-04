/*
 * UserPreference.java
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

import java.awt.Color;

import org.executequery.Constants;
import org.underworldlabs.util.LabelValuePair;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class UserPreference {

    public static final int STRING_TYPE = 0;
    public static final int BOOLEAN_TYPE = 1;
    public static final int COLOUR_TYPE = 2;
    public static final int INTEGER_TYPE = 3;
    public static final int CATEGORY_TYPE = 4;
    public static final int FILE_TYPE = 5;
    public static final int PASSWORD_TYPE = 6;
    public static final int ENUM_TYPE = 7;

    private boolean collapsed;
    private boolean saveActual;
    private String savedValue;

    private int type;
    private int maxLength;
    private String key;
    private Object value;
    private String displayedKey;
    private Object[] availableValues;

    public UserPreference() {}

    public UserPreference(int type, int maxLength, String key, String displayedKey, Object value) {

        this(type, maxLength, key, displayedKey, value, null);
    }

    public UserPreference(int type, String key, String displayedKey, Object value) {

        this(type, -1, key, displayedKey, value, null);
    }

    public UserPreference(int type, String key, String displayedKey, Object value, Object[] availableValues) {
        
        this(type, -1, key, displayedKey, value, availableValues);
    }

    public UserPreference(int type, int maxLength, String key, String displayedKey, Object value, Object[] availableValues) {

        this.type = type;
        this.key = key;
        this.maxLength = maxLength;
        
        if (type == STRING_TYPE) {

            if (value.getClass().isEnum()) {
                
                savedValue = ((Enum) value).name();
                
            } else {                
                
                savedValue = value.toString();
            }

            if (availableValues != null && availableValues.length > 0) {

                try {
                  
                    int index = Integer.parseInt(savedValue);
                    this.value = availableValues[index];
                
                } catch (NumberFormatException e) {

                    saveActual = true;
                    // try the value
                    for (int i = 0; i < availableValues.length; i++) {
                        
                        if (valueOf(availableValues[i]).equals(value)) {
                    
                            this.value = availableValues[i];
                            break;
                        }
                        
                    }
                    
                }
            
            } else { 
              
                this.value = value; 
            }
            
        } else {
          
            this.value = value;
        }

        this.displayedKey = displayedKey;
        this.availableValues = availableValues;        
    }

    private Object valueOf(Object object) {

        if (object instanceof LabelValuePair) {
            
            return ((LabelValuePair) object).getValue();
        }
        return object;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void reset(Object value) {
        if (type == STRING_TYPE) {

            if (availableValues != null && availableValues.length > 0) {
                
                if (saveActual) {
                    this.value = savedValue;
                }
                
                int index = Integer.parseInt(savedValue);
                this.value = availableValues[index];
            }

        } else {
            this.value = value;
        }
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSaveValue() {
        switch (type) {        
            case STRING_TYPE:
                
                if (availableValues != null) {

                    if (saveActual && value != null) {
                        return value.toString();
                    }
                    
                    for (int i = 0; i < availableValues.length; i++) {
                        if (value == availableValues[i]) {
                            return Integer.toString(i);
                        }
                    }

                }
                
                if (value == null) {
                
                    return Constants.EMPTY;
                }

                return value.toString();

            case COLOUR_TYPE:
                return Integer.toString(((Color)value).getRGB());

            case ENUM_TYPE:
                return ((Enum) valueOf(value)).name();
                
            case BOOLEAN_TYPE:
            case INTEGER_TYPE:
            default:
                return value.toString();
        }
    }
    
    public String getDisplayedKey() {
        return displayedKey;
    }

    public void setDisplayedKey(String displayedKey) {
        this.displayedKey = displayedKey;
    }

    public Object[] getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(Object[] availableValues) {
        this.availableValues = availableValues;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

}


