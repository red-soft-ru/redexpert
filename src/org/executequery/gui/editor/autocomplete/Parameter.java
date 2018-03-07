package org.executequery.gui.editor.autocomplete;

import org.underworldlabs.util.MiscUtils;

import java.sql.Types;

public class Parameter {
    int type;
    Object value;
    String name;
    String typeName;

    public Parameter(String name) {
        this.name = name;
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

    public boolean isNull()
    {
        switch (type)
        {
            case Types.BINARY:
            case Types.BLOB:
            case Types.LONGVARBINARY:
                return value == null;
            default:
                return MiscUtils.isNull((String) value);
        }
    }
}
