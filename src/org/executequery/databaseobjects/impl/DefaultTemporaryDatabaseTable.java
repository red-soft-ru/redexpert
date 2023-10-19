package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.Condition;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.Function;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by vasiliy on 25.01.17.
 */
public class DefaultTemporaryDatabaseTable extends DefaultDatabaseTable {

    private String typeTemporary;

    public DefaultTemporaryDatabaseTable(DatabaseObject object) {
        super(object, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
    }

    public DefaultTemporaryDatabaseTable(DatabaseHost host) {
        super(host, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
    }

    protected static final String RELATION_TYPE = "RELATION_TYPE";

    @Override
    public String getCreateSQLText() throws DataSourceException {

        updateListCD();
        updateListCC();

        return SQLUtils.generateCreateTable(
                getName(), listCD, listCC, true, true, true, true, true,
                getTypeTemporary(), getExternalFile(), getAdapter(), getSqlSecurity(), getTablespace(), getRemarks(), ";");
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TABLE", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {

        updateListCD();
        updateListCC();

        if (Comparer.isComputedFieldsNeed())
            for (ColumnData cd : listCD)
                if (!MiscUtils.isNull(cd.getComputedBy()))
                    cd.setComputedBy(null);

        return SQLUtils.generateCreateTable(
                getName(), listCD, listCC, true, true, false, false,
                Comparer.isCommentsNeed(), getTypeTemporary(), getExternalFile(),
                getAdapter(), getSqlSecurity(), getTablespace(), getRemarks(), ";");
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) {
        DefaultTemporaryDatabaseTable comparingTable = (DefaultTemporaryDatabaseTable) databaseObject;
        return SQLUtils.generateAlterTable(this, comparingTable, true,
                new boolean[]{false, false, false, false}, Comparer.isComputedFieldsNeed(), Comparer.isFieldsPositionsNeed());
    }

    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = super.builderCommonQuery();
        Field typeTemp = Field.createField(getMainTable(), RELATION_TYPE);
        Function subIIF = Function.createFunction("IIF")
                .appendArgument(typeTemp.getFieldTable() + " = 5")
                .appendArgument("'ON COMMIT DELETE ROWS'")
                .appendArgument("NULL");
        Function mainIIF = Function.createFunction("IIF").appendArgument(typeTemp.getFieldTable() + " = 4")
                .appendArgument("'ON COMMIT PRESERVE ROWS'")
                .appendArgument(subIIF.getStatement());
        sb.appendField(typeTemp.setStatement(mainIIF.getStatement()));
        return sb;
    }

    @Override
    protected SelectBuilder builderForInfoAllObjects(SelectBuilder commonBuilder) {
        SelectBuilder sb = super.builderForInfoAllObjects(commonBuilder);
        sb.appendCondition(Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "RELATION_TYPE"), "=", "4"))
                .appendCondition(Condition.createCondition(Field.createField(getMainTable(), "RELATION_TYPE"), "=", "5"))
                .setLogicOperator("OR"));
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        if (first) {
            setRemarks(getFromResultSet(rs, DESCRIPTION));
            setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
            setTypeTemporary(getFromResultSet(rs, RELATION_TYPE));
        }
        addingConstraint(rs);
        return null;
    }


    private String getTypeTemporary() {
        checkOnReload(typeTemporary);
        return typeTemporary;
    }

    public void setTypeTemporary(String typeTemporary) {
        this.typeTemporary = typeTemporary;
    }

    @Override
    public String getTablespace() {
        return null;
    }

    @Override
    public String getExternalFile() {
        return null;
    }

    @Override
    public String getAdapter() {
        return null;
    }

    @Override
    public int getType() {
        return GLOBAL_TEMPORARY;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY];
    }

    @Override
    public boolean hasSQLDefinition() {
        return true;
    }

}