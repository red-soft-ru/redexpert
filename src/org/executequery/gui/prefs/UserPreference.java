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

import org.executequery.Constants;
import org.underworldlabs.util.LabelValuePair;

import java.awt.*;
import java.util.Objects;

/**
 * @author Takis Diakoumis
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
    public static final int DIR_TYPE = 8;
    public static final int DATE_PATTERN_TYPE = 9;

    private boolean saveActual;
    private Object value;

    private final int type;
    private final String key;
    private final int maxLength;
    private final Object[] values;
    private final String savedValue;
    private final String displayedKey;

    public UserPreference(int type, int maxLength, String key, String displayedKey, Object value) {
        this(type, maxLength, key, displayedKey, value, null);
    }

    public UserPreference(int type, String key, String displayedKey, Object value) {
        this(type, -1, key, displayedKey, value, null);
    }

    public UserPreference(int type, String key, String displayedKey, Object value, Object[] values) {
        this(type, -1, key, displayedKey, value, values);
    }

    public UserPreference(int type, int maxLength, String key, String displayedKey, Object value, Object[] values) {
        this.savedValue = extractSavedValue(type, value);
        this.value = extractValue(type, value, values);
        this.displayedKey = displayedKey;
        this.maxLength = maxLength;
        this.values = values;
        this.type = type;
        this.key = key;
    }

    private String extractSavedValue(int type, Object value) {

        if (type == STRING_TYPE || type == DATE_PATTERN_TYPE) {
            if (value.getClass().isEnum())
                return ((Enum<?>) value).name();

            return value.toString();
        }

        return null;
    }

    private Object extractValue(int type, Object value, Object[] values) {

        if (canProcess(type, values)) {
            try {
                int index = Integer.parseInt(savedValue);
                return values[index];

            } catch (NumberFormatException | NullPointerException e) {
                saveActual = true;
                for (Object availableValue : values)
                    if (valueOf(availableValue).equals(value))
                        return availableValue;
            }
        }

        return value;
    }

    // ---

    public void reset(Object value, Class<?> clazz) {
        Object oldValue = getStringValue(this.value, false);

        if (canProcess(type, values)) {

            if (saveActual)
                this.value = savedValue;

            try {
                int index = Integer.parseInt(savedValue);
                this.value = values[index];

            } catch (NumberFormatException e) {
                for (Object availableValue : values) {
                    if (valueOf(availableValue).equals(value)) {
                        this.value = availableValue;
                        break;
                    }
                }
            }

        } else
            this.value = value;

        Object newValue = getStringValue(this.value, false);
        if (!Objects.equals(newValue, oldValue))
            PropertiesPanel.setHasChanges(key, clazz);
    }

    public String getSaveValue() {
        switch (type) {
            case DATE_PATTERN_TYPE:
            case STRING_TYPE:

                if (values != null) {

                    if (saveActual && value != null)
                        return valueOf(value).toString();

                    for (int i = 0; i < values.length; i++)
                        if (Objects.equals(value, values[i]))
                            return Integer.toString(i);
                }

                return getStringValue(value, true);

            case COLOUR_TYPE:
                return Integer.toString(((Color) value).getRGB());

            case ENUM_TYPE:
                return ((Enum<?>) valueOf(value)).name();

            case BOOLEAN_TYPE:
            case INTEGER_TYPE:
            default:
                return value.toString();
        }
    }

    // ---

    public void setValue(Object value, Class<?> clazz) {
        PropertiesPanel.setHasChanges(key, clazz);
        this.value = value;
    }

    private Object valueOf(Object object) {
        if (object instanceof LabelValuePair)
            return ((LabelValuePair) object).getValue();
        return object;
    }

    private String getStringValue(Object value, boolean notNull) {
        if (value != null)
            return value.toString();
        return notNull ? Constants.EMPTY : null;
    }

    private static boolean canProcess(int type, Object[] values) {
        return (type == STRING_TYPE || type == DATE_PATTERN_TYPE) && values != null && values.length > 0;
    }

    // ---

    public Object[] getValues() {
        return values;
    }

    public String getDisplayedKey() {
        return displayedKey;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public Object getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public int getType() {
        return type;
    }

}
