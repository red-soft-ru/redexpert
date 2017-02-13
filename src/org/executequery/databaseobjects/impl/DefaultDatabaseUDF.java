package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vasiliy on 13.02.17.
 */

public class DefaultDatabaseUDF extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private class DatabaseUDFParameter {
        private int argPosition;
        private int mechanism;
        private int fieldType;
        private int fieldScale;
        private int fieldLenght;
        private int fieldSubType;
        private int fieldPrecision;
    }

    private String moduleName;
    private String entryPoint;
    private int returnArg;

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);

        loadParameters();
    }

    private void loadParameters() {
        String sqlQuery = "select f.rdb$function_name,\n" +
                "f.rdb$module_name,\n" +
                "f.rdb$entrypoint,\n" +
                "f.rdb$return_argument,\n" +
                "f.rdb$description,\n" +
                "fa.rdb$argument_position,\n" +
                "fa.rdb$mechanism,\n" +
                "fa.rdb$field_type,\n" +
                "fa.rdb$field_scale,\n" +
                "fa.rdb$field_length,\n" +
                "fa.rdb$field_sub_type,\n" +
                "c.rdb$bytes_per_character,\n" +
                "c.rdb$character_set_name,\n" +
                "fa.rdb$field_precision\n" +
                "from rdb$functions f\n" +
                "left join rdb$function_arguments fa on f.rdb$function_name = fa.rdb$function_name\n" +
                "left join rdb$character_sets c on fa.rdb$character_set_id = c.rdb$character_set_id\n" +
                "where (f.rdb$function_name = '" + getName() + "')\n" +
                "order by fa.rdb$argument_position";

        Statement statement = null;

        try {
            statement = this.getHost().getConnection().createStatement();

            ResultSet rs = statement.executeQuery(sqlQuery);

            while (rs.next()) {

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!statement.isClosed())
                    statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Creates a new instance with
     * the specified values.
     */
    public DefaultDatabaseUDF(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }


    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public int getReturnArg() {
        return returnArg;
    }

    public void setReturnArg(int returnArg) {
        this.returnArg = returnArg;
    }

    public int getType() {
        return UDF;
    }

    public String getCreateSQLText() {
        return "";
    }
}