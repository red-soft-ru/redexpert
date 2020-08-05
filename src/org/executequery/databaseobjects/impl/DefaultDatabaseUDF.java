package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.underworldlabs.util.MiscUtils;

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
    public static final String[] mechanisms = {"BY VALUE", "BY REFERENCE", "BY DESCRIPTOR", "BY BLOB DESCRIPTOR"};
    public static final int BY_VALUE = 0;
    public static final int BY_REFERENCE = 1;
    public static final int BY_DESCRIPTOR = 2;
    public static final int BY_BLOB_DESCRIPTOR = 3;
    public static final int BY_SCALAR_ARRAY_DESCRIPTOR = 4;
    public static final int BY_REFERENCE_WITH_NULL = 5;

    // TODO why so many methods??? WHY???
    public static String getStringMechanismFromInt(int mechanism) {
        if (mechanism >= 0 && mechanism < mechanisms.length)
            return mechanisms[mechanism];
        else if (mechanism == -1) {
            return mechanisms[1];
        }
        else
            return "";
    }

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
                    return "Free it";
                case 7:
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
                    return udf.getFreeIt();
                case 7:
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

    public class UDFParameter {
        private int argPosition;
        private int mechanism;
        private int fieldType;
        private int fieldScale;
        private int fieldLenght;
        private int fieldSubType;
        private int fieldPrecision;
        private String encoding;
        private String fieldStringType;
        private String stringMechanism;
        private boolean notNull;
        private boolean isCString;

        UDFParameter(int argPosition, int mechanism,
                     int fieldType, int fieldScale,
                     int fieldLength, int fieldSubType,
                     int fieldPrecision) {
            this.argPosition = argPosition;
            this.mechanism = mechanism;
            if (this.mechanism == BY_REFERENCE_WITH_NULL)
                this.notNull = false;
            this.fieldType = fieldType;
            this.fieldScale = fieldScale;
            this.fieldLenght = fieldLength;
            this.fieldSubType = fieldSubType;
            this.fieldStringType = DatabaseTypeConverter.getTypeWithSize(fieldType, fieldSubType, fieldLength, fieldScale);
            if (this.fieldStringType.contains("BLOB"))
                this.fieldStringType = "BLOB";
            this.fieldPrecision = fieldPrecision;
            this.stringMechanism = getStringMechanismFromInt(this.mechanism);
            if (this.fieldType == 40)
                isCString = true;
        }

        public String getFieldStringType() {
            return fieldStringType;
        }

        public String getStringMechanism() {
            return stringMechanism;
        }

        public int getMechanism() {
            return this.mechanism;
        }

        public int getArgPosition() {
            return this.argPosition;
        }

        public int getFieldType() {
            return this.fieldType;
        }

        public int getFieldLenght() {
            return this.fieldLenght;
        }

        public int getFieldSubType() {
            return this.fieldSubType;
        }

        public int getFieldScale() {
            return this.fieldScale;
        }

        public int getFieldPrecision() {
            return this.fieldPrecision;
        }

        public boolean isNotNull() {
            return notNull;
        }

        public void setNotNull(boolean notNull) {
            this.notNull = notNull;
        }

        public boolean isCString() {
            return this.isCString;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getEncoding() {
            return encoding;
        }
    }

    private String moduleName;
    private String entryPoint;
    private int returnArg;

    private String returnMechanism = "";
    private String returns = "";
    private String inputParameters = "";
    private Boolean freeIt = false;
    private String description;

    List<UDFParameter> parameters = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF() {
    }

    /**
     * Creates a new instance.
     */
    public DefaultDatabaseUDF(DatabaseMetaTag metaTagParent, String name, DatabaseHost host) {
        super(metaTagParent, name);
        setHost(host);
    }

    public void loadParameters() throws SQLException {
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
                "c.rdb$character_set_name as character_set_name,\n" +
                "fa.rdb$field_precision\n";
                if (getHost().getDatabaseMetaData().getDatabaseMajorVersion() >= 3)
                    sqlQuery += ",fa.rdb$null_flag as null_flag\n";
                sqlQuery += "from rdb$functions f\n" +
                "left join rdb$function_arguments fa on f.rdb$function_name = fa.rdb$function_name\n" +
                "left join rdb$character_sets c on fa.rdb$character_set_id = c.rdb$character_set_id\n" +
                "where (f.rdb$function_name = '" + getName() + "')\n" +
                "order by fa.rdb$argument_position";

        Statement statement = null;
        ResultSet rs = null;

        parameters.clear();
        inputParameters = "";

        try {
            statement = this.getHost().getConnection().createStatement();

            rs = statement.executeQuery(sqlQuery);

            while (rs.next()) {
                UDFParameter udfParameter = new UDFParameter(rs.getInt(6),
                        rs.getInt(7), rs.getInt(8), rs.getInt(9),
                        rs.getInt(10), rs.getInt(11), rs.getInt(14));
                int nullFlag = 0;
                if (getHost().getDatabaseMetaData().getDatabaseMajorVersion() >= 3)
                    nullFlag = rs.getInt("null_flag");
                if (rs.getInt(7) != BY_REFERENCE_WITH_NULL) // already setup
                    udfParameter.setNotNull(nullFlag == 0 ? false : true);
                udfParameter.setEncoding(rs.getString("character_set_name"));
                parameters.add(udfParameter);
            }

            releaseResources(rs, this.getHost().getConnection());

            if (returnArg != 0) {
                returnMechanism = parameters.get(returnArg - 1).getStringMechanism();
                if (parameters.get(returnArg - 1).getMechanism() == -1)
                    this.freeIt = true;
            } else {
                returnMechanism = parameters.get(0).getStringMechanism();
                if (parameters.get(0).getMechanism() == -1)
                    this.freeIt = true;
            }

            for (int i = 0; i < parameters.size(); i++) {
                if (returnArg == 0 && i == 0)
                    continue;
                inputParameters += parameters.get(i).getFieldStringType();
                if (parameters.get(i).getMechanism() != BY_REFERENCE &&
                        parameters.get(i).getMechanism() != BY_VALUE) {
                    inputParameters += " " + parameters.get(i).getStringMechanism();
                }
                inputParameters += ", ";
            }
            if (!inputParameters.isEmpty())
                inputParameters = inputParameters.substring(0, inputParameters.length() - 2);

            if (returnArg != 0)
                returns = "Parameter " + returnArg;
            else {
                returns = parameters.get(0).getFieldStringType();
                if (parameters.get(0).getMechanism() != BY_REFERENCE &&
                        parameters.get(0).getMechanism() != -1) {
                    returns += " ";
                    returns += parameters.get(0).getStringMechanism();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResources(rs, this.getHost().getConnection());
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

    public List<UDFParameter> getUDFParameters() {
        return this.parameters;
    }

    public int getType() {
        return UDF;
    }

    public Boolean getFreeIt() {
        return this.freeIt;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateSQLText() {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE EXTERNAL FUNCTION ");
        sb.append(getName());
        sb.append("\n");
        String args = "";
        for (int i = 0; i < parameters.size(); i++) {
            if (returnArg == 0 && i == 0)
                continue;
            args += "\t" + parameters.get(i).getFieldStringType();
            if (parameters.get(i).getMechanism() != BY_VALUE &&
                    parameters.get(i).getMechanism() != BY_REFERENCE
                    ) {
                if (parameters.get(i).isNotNull() || parameters.get(i).getMechanism() == BY_DESCRIPTOR)
                        args += " " + parameters.get(i).getStringMechanism();
            }
            if (!parameters.get(i).isNotNull() && parameters.get(i).getMechanism() != BY_DESCRIPTOR &&
                    parameters.get(i).getMechanism() != BY_REFERENCE && returnArg - 1 != i)
                args += " " + "NULL";
            args += ",\n";
        }
        if (!args.isEmpty())
            args = args.substring(0, args.length() - 2);
        sb.append(args);
        sb.append("\n");
        sb.append("RETURNS\n");
        if (returnArg == 0) {
            sb.append(parameters.get(0).getFieldStringType());
            if (parameters.get(0).getMechanism() != BY_REFERENCE &&
                    parameters.get(0).getMechanism() != -1) {
                sb.append(" ");
                sb.append(parameters.get(0).getStringMechanism());
            }
        }
        else
            sb.append("PARAMETER " + returnArg);
        if (this.freeIt)
            sb.append(" FREE_IT ");
        sb.append("\n");
        sb.append("ENTRY_POINT '");
        if (!MiscUtils.isNull(getEntryPoint()))
            sb.append(getEntryPoint());
        sb.append("' MODULE_NAME '");
        if (!MiscUtils.isNull(getModuleName()))
            sb.append(getModuleName());
        sb.append("';");
        return sb.toString();
    }
}