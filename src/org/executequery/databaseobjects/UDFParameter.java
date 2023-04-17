package org.executequery.databaseobjects;

import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.BY_REFERENCE_WITH_NULL;
import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.getStringMechanismFromInt;

public class UDFParameter extends FunctionArgument {
    int mechanism;
    String stringMechanism;
    boolean isCString;

    String fieldStringType;

    public UDFParameter(int position, int mechanism, int dataType, int scale, int size, int subType, int precision) {
        super(null, dataType, size, precision, scale, subType, position, 0, null, null);
        this.mechanism = mechanism;
        if (this.mechanism == BY_REFERENCE_WITH_NULL)
            this.nullable = 1;
        this.stringMechanism = getStringMechanismFromInt(this.mechanism);
        if (dataType == 40)
            isCString = true;
        this.fieldStringType = DatabaseTypeConverter.getTypeWithSize(dataType, subType, size, scale);

    }

    public boolean isNotNull() {
        return nullable == 0;
    }

    public void setNotNull(boolean flag) {
        if (flag)
            nullable = 0;
        else nullable = 1;
    }

    public int getMechanism() {
        return mechanism;
    }

    public void setMechanism(int mechanism) {
        this.mechanism = mechanism;
    }

    public String getStringMechanism() {
        return stringMechanism;
    }

    public void setStringMechanism(String stringMechanism) {
        this.stringMechanism = stringMechanism;
    }

    public boolean isCString() {
        return isCString;
    }

    public void setCString(boolean CString) {
        isCString = CString;
    }

    public String getFieldStringType() {
        return fieldStringType;
    }

    public void setFieldStringType(String fieldStringType) {
        this.fieldStringType = fieldStringType;
    }
}
