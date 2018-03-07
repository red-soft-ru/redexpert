package org.executequery.gui.editor.autocomplete;

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

    /*public void setFormattedParameter(PreparedStatement statement,int number)
    {
        switch (type)
        {
            case Types.BIGINT:
                statement.setInt();
        }
    }*/
}
