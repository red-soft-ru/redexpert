package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vasiliy on 13.02.17.
 */

public class DefaultDatabaseUDF extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    public static class UDFTableModel implements TableModel {

        private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

        private List<DefaultDatabaseUDF> udfs;

        public UDFTableModel(List<DefaultDatabaseUDF> udf) {
            this.udfs = udf;
        }

        public void addTableModelListener(TableModelListener listener) {
            listeners.add(listener);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getColumnCount() {
            return 7;
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Name";
                case 1:
                    return "Library Name";
                case 2:
                    return "Entry Point";
                case 3:
                    return "Input Parameters";
                case 4:
                    return "Returns";
                case 5:
                    return "Return Mechanism";
                case 6:
                    return "Description";
            }
            return "";
        }

        public int getRowCount() {
            return udfs.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DefaultDatabaseUDF udf = udfs.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return udf.getName();
                case 1:
                    return udf.getModuleName();
                case 2:
                    return udf.getEntryPoint();
                case 3:
                    return udf.getInputParameters();
                case 4:
                    return udf.getReturns();
                case 5:
                    return udf.getReturnMechanism();
                case 6:
                    return udf.getRemarks();
            }
            return "";
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public void removeTableModelListener(TableModelListener listener) {
            listeners.remove(listener);
        }

        public void setValueAt(Object value, int rowIndex, int columnIndex) {

        }

    }

    private class UDFParameter {
        private int argPosition;
        private int mechanism;
        private int fieldType;
        private int fieldScale;
        private int fieldLenght;
        private int fieldSubType;
        private int fieldPrecision;

        private String fieldStringType;
        private String stringMechanism;

        UDFParameter (int argPosition, int mechanism,
                              int fieldType, int fieldScale,
                              int fieldLength, int fieldSubType,
                              int fieldPrecision) {
            this.argPosition = argPosition;
            this.mechanism = mechanism;
            this.fieldStringType = getTypeWithSize(fieldType, fieldSubType, fieldLength, fieldScale);
            this.fieldPrecision = fieldPrecision;
            this.stringMechanism = getMechanism(this.mechanism);
        }

        private String getMechanism(int mechanism) {
            switch (mechanism){
                case 0:
                    return "BY VALUE";
                case 1:
                    return "BY REFERENCE";
                case 2:
                    return "BY DESCRIPTOR";
                case 3:
                    return "by BLOB descriptor";
                default:
                    return "";
            }
        }

        public String getFieldStringType() {
            return fieldStringType;
        }

        public String getStringMechanism() {
            return stringMechanism;
        }
    }

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private static final int smallint_type = 7;
    private static final int integer_type = 8;
    private static final int quad_type = 9;
    private static final int float_type = 10;
    private static final int d_float_type = 11;
    private static final int date_type = 12;
    private static final int time_type = 13;
    private static final int char_type = 14;
    private static final int int64_type = 16;
    private static final int double_type = 27;
    private static final int timestamp_type = 35;
    private static final int varchar_type = 37;
    //  private static final int cstring_type = 40;
    private static final int blob_type = 261;
    private static final short boolean_type = 23;

    private String moduleName;
    private String entryPoint;
    private int returnArg;

    private String returnMechanism = "";
    private String returns = "";
    private String inputParameters = "";


    List<UDFParameter> parameters = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF() {}

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name, DatabaseHost host) {
        super(metaTagParent, name);
        setHost(host);
    }

    public void loadParameters() {
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
                UDFParameter udfParameter = new UDFParameter(rs.getInt(6),
                        rs.getInt(7), rs.getInt(8), rs.getInt(9),
                        rs.getInt(10), rs.getInt(11), rs.getInt(14));
                parameters.add(udfParameter);
            }

            if (returnArg != 0)
                returnMechanism = parameters.get(returnArg - 1).getStringMechanism();
            else
                returnMechanism = parameters.get(0).getStringMechanism();

            for (int i = 0; i < parameters.size(); i++) {
                if (returnArg == 0 && i == 0)
                    continue;
                inputParameters += parameters.get(i).getFieldStringType() + " "
                        + parameters.get(i).getStringMechanism() + ", ";
            }
            inputParameters = inputParameters.substring(0, inputParameters.length() - 2);

            if (returnArg != 0)
                returns = "Parameter " + returnArg;
            else
                returns = parameters.get(0).getFieldStringType() + " " +
                        parameters.get(0).getStringMechanism();

        } catch (SQLException e) {
            e.printStackTrace();
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

    public String getReturnMechanism() {
        return returnMechanism;
    }

    public String getReturns() {
        return returns;
    }

    public String getInputParameters() {
        return inputParameters;
    }

    public int getType() {
        return UDF;
    }

    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE EXTERNAL FUNCTION ");
        sb.append(getName());
        sb.append("\n");
        String args = "";
        for (int i = 0; i < parameters.size(); i++) {
            if (returnArg == 0 && i ==0)
                continue;
            args += "\t" + parameters.get(i).getFieldStringType() + " " +
                    parameters.get(i).getStringMechanism() + ",\n";
        }
        args = args.substring(0, args.length()-2);
        sb.append(args);
        sb.append("\n");
        sb.append("RETURNS\n");
        if(returnArg == 0)
            sb.append(parameters.get(0).getFieldStringType() + " " + parameters.get(0).getStringMechanism());
        else
            sb.append("PARAMETER " + returnArg);
        sb.append("\n");
        sb.append("ENTRY POINT '");
        sb.append(getEntryPoint().trim());
        sb.append("' MODULE_NAME '");
        sb.append(getModuleName().trim());
        sb.append("';");
        return sb.toString();
    }

    private String getTypeWithSize(int sqltype, int sqlsubtype, int sqlsize, int sqlscale) {
        switch (sqltype) {
            case smallint_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC(" + sqlsize + ",0)";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL("+ sqlsize + "," + sqlscale + ")";
                else
                    return "SMALLINT";
            case integer_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC(" + sqlsize + ",0)";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL("+ sqlsize + "," + sqlscale + ")";
                else
                    return "INTEGER";
            case double_type:
            case d_float_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC(" + sqlsize + ",0)";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL("+ sqlsize + "," + sqlscale + ")";
                else
                    return "DOUBLE PRECISION";
            case float_type:
                return "FLOAT";
            case char_type:
                return "CHAR(" + sqlsize + ")";
            case varchar_type:
                return "VARCHAR(" + sqlsize + ")";
            case timestamp_type:
                return "TIMESTAMP";
            case time_type:
                return "TIME";
            case date_type:
                return "DATE";
            case int64_type:
                if (sqlsubtype == SUBTYPE_NUMERIC || (sqlsubtype == 0 && sqlscale < 0))
                    return "NUMERIC(" + sqlsize + ",0)";
                else if (sqlsubtype == SUBTYPE_DECIMAL)
                    return "DECIMAL("+ sqlsize + "," + sqlscale + ")";
                else
                    return "BIGINT";
            case blob_type:
                if (sqlsubtype < 0)
                    return "BLOB SUB_TYPE <0";
                else if (sqlsubtype == 0)
                    return "BLOB SUB_TYPE 0";
                else if (sqlsubtype == 1)
                    return "BLOB SUB_TYPE 1";
                else
                    return "BLOB SUB_TYPE " + sqlsubtype;
            case quad_type:
                return "ARRAY";
            case boolean_type:
                return "BOOLEAN";
            default:
                return "NULL";
        }
    }
}