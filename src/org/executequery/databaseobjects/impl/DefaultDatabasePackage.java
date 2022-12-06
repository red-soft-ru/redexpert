package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.underworldlabs.util.MiscUtils;

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
        if (isMarkedForReload()) {
            getObjectInfo();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("create or alter package");
        sb.append(" ");
        sb.append(getName());
        sb.append("\n");
        sb.append("as");
        sb.append("\n");
        sb.append(this.headerSource);

        return sb.toString();
    }

    public void setHeaderSource(String headerSource) {
        this.headerSource = headerSource;
    }

    public String getBodySource() {
        StringBuilder sb = new StringBuilder();
        sb.append("recreate package body");
        sb.append(" ");
        sb.append(getName());
        sb.append("\n");
        sb.append("as");
        sb.append("\n");
        sb.append(this.bodySource);

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
        StringBuilder sb = new StringBuilder();
        sb.append("set term ^ ;");
        sb.append("\n");
        sb.append("\n");
        sb.append(getHeaderSource());
        sb.append("^");
        sb.append("\n");
        sb.append("\n");
        sb.append(getBodySource());
        sb.append("^");
        sb.append("\n");
        sb.append("\n");
        sb.append("set term ; ^");
        sb.append("\n");
        sb.append("\n");
        if (!MiscUtils.isNull(getRemarks())) {
            sb.append("comment on package");
            sb.append(" ");
            sb.append(getName());
            sb.append(" ");
            sb.append("is");
            sb.append("\n");
            sb.append("'");
            sb.append(getRemarks());
            sb.append("';");
        }

        return sb.toString();
    }

    protected String queryForInfo() {
        String sql_security = "null";
        if (getHost().getDatabaseProductName().toLowerCase().contains("reddatabase"))
            sql_security = "IIF(p.rdb$sql_security is null,null,IIF(p.rdb$sql_security,'DEFINER','INVOKER'))";
        return "select 0,\n" +
                "p.rdb$package_header_source,\n" +
                "p.rdb$package_body_source,\n" +
                "p.rdb$valid_body_flag,\n" +
                "p.rdb$security_class,\n" +
                "p.rdb$owner_name,\n" +
                "p.rdb$system_flag,\n" +
                "p.rdb$description as DESCRIPTION,\n" +
                sql_security + " as SQL_SECURITY,\n" +
                "from rdb$packages p\n" +
                "where p.rdb$package_name='" + getName().trim() + "'";
    }

    @Override
    protected void setInfoFromResultSet(ResultSet rs) throws SQLException {
        if (rs.next()) {
            setHeaderSource(rs.getString(2));
            setBodySource(rs.getString(3));
            setValidBodyFlag(rs.getBoolean(4));
            setSecurityClass(rs.getString(5));
            setOwnerName(rs.getString(6));
            setSystemFlag(rs.getBoolean(7));
            setRemarks(getFromResultSet(rs, "DESCRIPTION"));
            setSqlSecurity(getFromResultSet(rs, "SQL_SECURITY"));
        }
    }
}
