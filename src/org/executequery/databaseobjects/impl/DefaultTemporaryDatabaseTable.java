package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


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
                getTypeTemporary(), getExternalFile(), getAdapter(), getSqlSecurity(), getTablespace(), getRemarks());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("TABLE", getName());
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
                getAdapter(), getSqlSecurity(), getTablespace(), getRemarks());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) {
        DefaultTemporaryDatabaseTable comparingTable = (DefaultTemporaryDatabaseTable) databaseObject;
        return SQLUtils.generateAlterTable(this, comparingTable, true,
                new boolean[]{false, false, false, false}, Comparer.isComputedFieldsNeed());
    }

    @Override
    protected String queryForInfo() {

        SelectBuilder sb = new SelectBuilder();
        sb.setDistinct(true);
        Table rels = Table.createTable("RDB$RELATIONS", "R");
        Table relCons = Table.createTable("RDB$RELATION_CONSTRAINTS", "RC");
        Table checkCons = Table.createTable("RDB$CHECK_CONSTRAINTS", "CC");
        Table triggers = Table.createTable("RDB$TRIGGERS", "T");

        Field conName = Field.createField(relCons, CONSTRAINT_NAME);
        Field conType = Field.createField(relCons, CONSTRAINT_TYPE);
        Function compareCheck = Function.createFunction("IIF")
                .appendArgument(conType.getFieldTable() + " <> 'CHECK'")
                .appendArgument("NULL")
                .appendArgument(conName.getFieldTable());
        sb.appendField(Field.createField().setStatement(compareCheck.getStatement()).setAlias(conName.getAlias()));
        compareCheck.setArgument(2, conType.getFieldTable());
        sb.appendField(Field.createField().setStatement(compareCheck.getStatement()).setAlias(conType.getAlias()));
        sb.appendField(Field.createField(triggers, TRIGGER_SOURCE));
        Field sqlSecurity = Field.createField(rels, SQL_SECURITY);
        sqlSecurity.setStatement(Function.createFunction("IIF")
                .appendArgument(sqlSecurity.getFieldTable() + " IS NULL").appendArgument("NULL").appendArgument(Function.createFunction().setName("IIF")
                        .appendArgument(sqlSecurity.getFieldTable()).appendArgument("'DEFINER'").appendArgument("'INVOKER'").getStatement()).getStatement());
        sqlSecurity.setNull(getDatabaseMajorVersion() < 3);
        sb.appendField(sqlSecurity);
        Field typeTemp = Field.createField(rels, RELATION_TYPE);
        Function subIIF = Function.createFunction("IIF")
                .appendArgument(typeTemp.getFieldTable() + " = 5")
                .appendArgument("'ON COMMIT DELETE ROWS'")
                .appendArgument("NULL");
        Function mainIIF = Function.createFunction("IIF").appendArgument(typeTemp.getFieldTable() + " = 4")
                .appendArgument("'ON COMMIT PRESERVE ROWS'")
                .appendArgument(subIIF.getStatement());
        sb.appendField(typeTemp.setStatement(mainIIF.getStatement()));
        sb.appendField(Field.createField(rels, DESCRIPTION));
        Field relName = Field.createField(rels, "RELATION_NAME");
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(relName, Field.createField(relCons, relName.getAlias())));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(conName, Field.createField(checkCons, conName.getAlias())));
        sb.appendJoin(LeftJoin.createLeftJoin().appendFields(Field.createField(checkCons, "TRIGGER_NAME"),
                Field.createField(triggers, "TRIGGER_NAME")));
        sb.appendCondition(Condition.createCondition(relName, "=", "?"));

        sb.appendCondition(Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(triggers, "TRIGGER_TYPE"), "=", "1"))
                .appendCondition(Condition.createCondition(Field.createField(triggers, "TRIGGER_TYPE"), "IS", "NULL"))
                .setLogicOperator("OR"));

        return sb.getSQLQuery();
    }

    protected void setInfoFromResultSet(ResultSet rs) {
        try {

            boolean first = true;
            checkConstraints = new ArrayList<>();
            List<String> names = new ArrayList<>();
            while (rs.next()) {

                if (first) {
                    setRemarks(getFromResultSet(rs, DESCRIPTION));
                    setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
                    setTypeTemporary(getFromResultSet(rs, RELATION_TYPE));
                }

                first = false;
                String conType = rs.getString(CONSTRAINT_TYPE);
                if (conType != null) {
                    String name = rs.getString(CONSTRAINT_NAME).trim();
                    if (!names.contains(name)) {
                        ColumnConstraint constraint = new TableColumnConstraint(rs.getString(TRIGGER_SOURCE));
                        constraint.setName(name);
                        constraint.setTable(this);
                        checkConstraints.add(constraint);
                        names.add(name);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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