package org.executequery.databaseobjects;

import org.executequery.gui.browser.ColumnData;

import java.sql.DatabaseMetaData;

/**
 * @author vasiliy
 */
public class FunctionArgument extends Parameter {

    public FunctionArgument(String name, int dataType, int size, int precision, int scale,
                            int subType, int position, int typeOf, String relationName, String fieldName) {
        this.name = name;
        if (this.name == null)
            this.type = DatabaseMetaData.procedureColumnReturn;
        else {
            this.type = DatabaseMetaData.procedureColumnIn;
            this.name = name.trim();
        }
        this.dataType = dataType;
        this.scale = scale;
        this.subType = subType;
        this.size = precision == 0 ? size : precision;
        this.position = position;
        this.typeOf = (typeOf == 1);
        if (relationName != null)
            this.relationName = relationName.trim();
        if (fieldName != null)
            this.fieldName = fieldName.trim();
        if (this.relationName != null && this.fieldName != null)
            this.typeOfFrom = ColumnData.TYPE_OF_FROM_COLUMN;
    }

}
