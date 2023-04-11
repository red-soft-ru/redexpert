package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by vasiliy on 04.05.17.
 */
public class DefaultDatabasePackage extends DefaultDatabaseExecutable
        implements DatabaseProcedure {

    private String headerSource;
    private String bodySource;
    private boolean validBodyFlag;
    private String securityClass;
    private String ownerName;


    public DefaultDatabasePackage() {
    }

    public DefaultDatabasePackage(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
    }

    public DefaultDatabasePackage(String schema, String name) {
        setName(name);
        setSchemaName(schema);
    }

    public int getType() {
        if (isSystem())
            return SYSTEM_PACKAGE;
        return PACKAGE;
    }

    public String getMetaDataKey() {
        if (isSystem())
            return META_TYPES[SYSTEM_PACKAGE];
        return META_TYPES[PACKAGE];
    }

    public String getHeaderSource() {

        if (isMarkedForReload())
            getObjectInfo();

        StringBuilder sb = new StringBuilder();
        sb.append("create or alter package  ").append(getName());
        sb.append("\nas\n").append(this.headerSource);

        return sb.toString();
    }

    public void setHeaderSource(String headerSource) {
        this.headerSource = headerSource;
    }

    public String getBodySource() {

        StringBuilder sb = new StringBuilder();
        sb.append("recreate package body ").append(getName());
        sb.append("\nas\n").append(this.bodySource);

        return sb.toString();
    }

    public void setBodySource(String bodySource) {
        this.bodySource = bodySource;
    }

    public boolean isValidBodyFlag() {
        return validBodyFlag;
    }

    public void setValidBodyFlag(boolean validBodyFlag) {
        this.validBodyFlag = validBodyFlag;
    }

    public String getSecurityClass() {
        return securityClass;
    }

    public void setSecurityClass(String securityClass) {
        this.securityClass = securityClass;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    @Override
    public String getCreateSQLText() {
        return SQLUtils.generateCreatePackage(getName(), getHeaderSource(), getBodySource(), getDescription());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("PACKAGE", getName());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String comment = Comparer.isCommentsNeed() ? getDescription() : null;
        return SQLUtils.generateCreatePackage(getName(), getHeaderSource(), getBodySource(), comment);
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
    }

    protected String queryForInfo() {

        String sql_security = "null";
        if (getHost().getDatabaseProductName().toLowerCase().contains("reddatabase"))
            sql_security = "IIF(p.rdb$sql_security is null,null,IIF(p.rdb$sql_security,'DEFINER','INVOKER'))";

        String sql = "select 0,\n" +
                "p.rdb$package_header_source,\n" +
                "p.rdb$package_body_source,\n" +
                "p.rdb$valid_body_flag,\n" +
                "p.rdb$security_class,\n" +
                "p.rdb$owner_name,\n" +
                "p.rdb$system_flag,\n" +
                "p.rdb$description as DESCRIPTION,\n" +
                sql_security + " as SQL_SECURITY\n" +
                "from rdb$packages p\n" +
                "where p.rdb$package_name=?";

        return sql;
    }

    protected final static String PACKAGE_HEADER_SOURCE = "PACKAGE_HEADER_SOURCE";
    protected final static String PACKAGE_BODY_SOURCE = "PACKAGE_BODY_SOURCE";
    protected final static String VALID_BODY_FLAG = "VALID_BODY_FLAG";
    protected final static String SECURITY_CLASS = "SECURITY_CLASS";
    protected final static String OWNER_NAME = "OWNER_NAME";
    protected final static String SYSTEM_FLAG = "SYSTEM_FLAG";

    @Override
    protected String getFieldName() {
        return "PACKAGE_NAME";
    }

    @Override
    protected Table getMainTable() {
        return Table.createTable("RDB$PACKAGES", "P");
    }

    @Override
    protected SelectBuilder builderCommonQuery() {
        SelectBuilder sb = new SelectBuilder();
        Table packages = getMainTable();
        sb.appendFields(packages, getFieldName(), PACKAGE_HEADER_SOURCE, PACKAGE_BODY_SOURCE, VALID_BODY_FLAG,
                SECURITY_CLASS, OWNER_NAME, SYSTEM_FLAG, DESCRIPTION);
        sb.appendField(buildSqlSecurityField(packages));
        sb.appendTable(packages);
        sb.setOrdering(getObjectField().getFieldTable());
        return sb;
    }

    @Override
    public Object setInfoFromSingleRowResultSet(ResultSet rs, boolean first) throws SQLException {
        setHeaderSource(getFromResultSet(rs, PACKAGE_HEADER_SOURCE));
        setBodySource(getFromResultSet(rs, PACKAGE_BODY_SOURCE));
        setValidBodyFlag(rs.getBoolean(VALID_BODY_FLAG));
        /*setSecurityClass(getFromResultSet(rs,SECURITY_CLASS));
        setOwnerName(getFromResultSet(rs,OWNER_NAME));
        setSystemFlag(rs.getBoolean(SYSTEM_FLAG))*/
        ;
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
        return null;
    }

    @Override
    public void prepareLoadingInfo() {

    }

    @Override
    public void finishLoadingInfo() {

    }

    @Override
    public boolean isAnyRowsResultSet() {
        return false;
    }
}
