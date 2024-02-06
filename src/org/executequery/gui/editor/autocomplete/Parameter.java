package org.executequery.gui.editor.autocomplete;

import org.executequery.GUIUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Parameter {

    int type;
    Object value;
    String name;
    String typeName;
    boolean needUpdateValue;

    public Parameter(String name) {
        this.name = name;
        this.needUpdateValue = true;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Object getPreparedValue() {
        if (value instanceof File)
            try {
                return new FileInputStream((File) value);
            } catch (FileNotFoundException e) {
                GUIUtilities.displayExceptionErrorDialog("Invalid file", e);
            }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isNeedUpdateValue() {
        return needUpdateValue;
    }

    public void setNeedUpdateValue(boolean updateValue) {
        this.needUpdateValue = updateValue;
    }

    public boolean isNull()
    {
        return value == null;
    }
}
