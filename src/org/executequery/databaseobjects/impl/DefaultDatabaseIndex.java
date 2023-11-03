package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.Join;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vasiliy on 15.02.17.
 */
public class DefaultDatabaseIndex extends AbstractDatabaseObject {

    public static class DatabaseIndexColumn {

        private double selectivity;
        private String fieldName;
        private int fieldPosition;

        DatabaseIndexColumn() {

        }

        public double getSelectivity() {
            return selectivity;
        }

        public void setSelectivity(double selectivity) {
            this.selectivity = selectivity;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public int getFieldPosition() {
            return fieldPosition;
        }

        public void setFieldPosition(int fieldPosition) {
            this.fieldPosition = fieldPosition;
        }
    }

    public static class IndexColumnsModel implements TableModel {

        private final Set<TableModelListener> listeners = new HashSet<>();

        private final List<DatabaseIndexColumn> columns;

        public IndexColumnsModel(List<DatabaseIndexColumn> columns) {
            this.columns = columns;
        }

        public void addTableModelListener(TableModelListener listener) {
            listeners.add(listener);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        public int getColumnCount() {
            return 3;
        }

        private String bundleString(String key){

            return Bundles.get(IndexColumnsModel.class,key);
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return bundleString("FieldName");
                case 1:
                    return bundleString("StatisticSelectivity");
                case 2:
                    return bundleString("FieldPosition");
            }
            return "";
        }

        public int getRowCount() {
            return columns.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DatabaseIndexColumn column = columns.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return column.getFieldName();
                case 1:
                    return column.getSelectivity();
                case 2:
                    return column.getFieldPosition();
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

    private int indexType;
    private static final String RELATION_NAME = "RELATION_NAME";

    private String tableName;
    private boolean isActive;
    private boolean isUnique;
    private String expression;
    private String constraint_type;
    private boolean markedReloadActive;
    private String tablespace;
    private static final String INDEX_TYPE = "INDEX_TYPE";

    public DefaultDatabaseIndex(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }
    private static final String UNIQUE_FLAG = "UNIQUE_FLAG";

    public void setIndexColumns(List<DatabaseIndexColumn> columns) {
        this.columns = columns;
    }

    public int getIndexType() {
        if (isMarkedForReload())
            getObjectInfo();
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }

    private static final String INDEX_INACTIVE = "INDEX_INACTIVE";

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isActive() {
        if (isMarkedReloadActive())
            getObjectInfo();
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        setMarkedReloadActive(false);
    }

    public boolean isUnique() {
        if (isMarkedForReload())
            getObjectInfo();
        return isUnique;
    }

    public void setUnique(boolean unique) {
        isUnique = unique;
    }


    public boolean isMarkedReloadActive() {
        return markedReloadActive;
    }

    public void setMarkedReloadActive(boolean markedReloadActive) {
        this.markedReloadActive = markedReloadActive;
    }

    private static final String STATISTICS = "STATISTICS";
    private static final String EXPRESSION_SOURCE = "EXPRESSION_SOURCE";

    public void setConstraint_type(String constraint_type) {
        this.constraint_type = constraint_type;
    }

    public String getConstraint_type() {
        if (isMarkedForReload())
            getObjectInfo();
        return constraint_type;
    }

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreateIndex(
                getName(), getType(), isUnique(), getTableName(), null, getCondition(),
                getIndexColumns(), getTablespace(), isActive(), getRemarks(), getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("INDEX", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        return (!MiscUtils.isNull(this.getConstraint_type())) ?
                "/* Will be created with constraint defining */\n" :
                getCreateSQLText();
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        DefaultDatabaseIndex comparingIndex = (DefaultDatabaseIndex) databaseObject;
        return SQLUtils.generateAlterIndex(this, comparingIndex);
    }

    public String getComparedDropSQL() throws DataSourceException {
        return (!MiscUtils.isNull(this.getConstraint_type())) ?
                "/* Remove with table constraint */\n" : getDropSQL();
    }

    private static final String CONDITION_SOURCE = "CONDITION_SOURCE";
    private static final String TABLESPACE_NAME = "TABLESPACE_NAME";
    private static final String CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String FIELD_NAME = "FIELD_NAME";
    private static final String FIELD_POSITION = "FIELD_POSITION";
    private List<DatabaseIndexColumn> columns;
    private String condition;

    public List<DatabaseIndexColumn> getIndexColumns() {
        checkOnReload(columns);
        return columns;
    }

    public String getTableName() {
        checkOnReload(tableName);
        return tableName;
    }

    public String getExpression() {
        checkOnReload(expression);
        return expression;
    }

    public void setExpression(String expression) {
        if (MiscUtils.isNull(expression))
            return;
        expression = expression.trim().substring(1, expression.trim().length() - 1);
        this.expression = expression;
    }

    @Override
    protected String getFieldName() {
        return "INDEX_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$INDICES", "I");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table indicies = getMainTable();
        Table constraints = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC");
        Table indexSegments = Table.createTable("RDB$INDEX_SEGMENTS", "ISGMT");

        sb.appendField(Field.createField(indicies, getFieldName()));
        sb.appendField(Field.createField(indicies, RELATION_NAME));
        sb.appendField(Field.createField(indicies, INDEX_TYPE));
        sb.appendField(Field.createField(indicies, UNIQUE_FLAG));
        sb.appendField(Field.createField(indicies, INDEX_INACTIVE));
        sb.appendField(Field.createField(indicies, STATISTICS));
        sb.appendField(Field.createField(indicies, EXPRESSION_SOURCE));
        sb.appendField(Field.createField(indicies, DESCRIPTION));
        sb.appendField(Field.createField(indicies, CONDITION_SOURCE).setNull(getDatabaseMajorVersion() < 5));
        sb.appendField(Field.createField(indicies, TABLESPACE_NAME).setNull(!tablespaceCheck()));
        sb.appendField(Field.createField(constraints, CONSTRAINT_TYPE));
        sb.appendField(Field.createField(indexSegments, FIELD_NAME));
        sb.appendField(Field.createField(indexSegments, FIELD_POSITION));
        Field indexName = Field.createField(indicies, "INDEX_NAME");
        sb.appendJoin(Join.createLeftJoin().appendFields(indexName, Field.createField(constraints, indexName.getAlias())));
        sb.appendJoin(Join.createLeftJoin().appendFields(indexName, Field.createField(indexSegments, indexName.getAlias())));
        sb.setOrdering(getObjectField().getFieldTable() + ", " + Field.createField(indexSegments, FIELD_POSITION).getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        if (first) {
            setTableName(getFromResultSet(rs, RELATION_NAME));
            setIndexType(rs.getInt(INDEX_TYPE));
            setActive(rs.getInt(INDEX_INACTIVE) != 1);
            setUnique(rs.getInt(UNIQUE_FLAG) == 1);
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setConstraint_type(getFromResultSet(rs, CONSTRAINT_TYPE));
            setExpression(getFromResultSet(rs, EXPRESSION_SOURCE));
            setTablespace(getFromResultSet(rs, TABLESPACE_NAME));
            setCondition(getFromResultSet(rs, CONDITION_SOURCE));
        }
        DefaultDatabaseIndex.DatabaseIndexColumn column = new DefaultDatabaseIndex.DatabaseIndexColumn();
        String name = rs.getString(FIELD_NAME);
        if (name != null)
            column.setFieldName(name.trim());
        column.setSelectivity(rs.getDouble(STATISTICS));
        column.setFieldPosition(rs.getInt(FIELD_POSITION));
        columns.add(column);
        return null;
    }

    @Override
    public void prepareLoadingInfo() {
        columns = new ArrayList<>();
    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return true;
    }


    @Override
    public int getType() {
        return isSystem() ? SYSTEM_INDEX : INDEX;
    }

    @Override
    public String getMetaDataKey() {
        return META_TYPES[getType()];
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    public String getTablespace() {
        checkOnReload(tablespace);
        return tablespace;
    }

    public void setTablespace(String tablespace) {
        this.tablespace = tablespace;
    }

    public boolean setStatistics() {

        boolean res = true;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = "SET STATISTICS INDEX " + MiscUtils.getFormattedObject(getName(), getHost().getDatabaseConnection());

        try {

            SqlStatementResult result = querySender.execute(QueryTypes.SET_STATISTICS, query);
            if (result.isException()) {
                res = false;
                result.getSqlException().printStackTrace();
            } else
                Log.info("Executing:\"" + query + "\"");

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            querySender.releaseResources();
        }

        return res;
    }

    public void reset() {
        super.reset();
        setMarkedReloadActive(true);
    }

    public String getCondition() {
        checkOnReload(condition);
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
