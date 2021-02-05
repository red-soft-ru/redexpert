package org.executequery.databaseobjects.impl;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

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
    private boolean systemFlag;
    private boolean sqlSecurity;
    private String description;

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
        return PACKAGE;
    }

    public String getMetaDataKey() {
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

    public boolean isSystemFlag() {
        return systemFlag;
    }

    public void setSystemFlag(boolean systemFlag) {
        this.systemFlag = systemFlag;
    }

    public boolean isSqlSecurity() {
        return sqlSecurity;
    }

    public void setSqlSecurity(boolean sqlSecurity) {
        this.sqlSecurity = sqlSecurity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return this.description;
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
        if (this.description != null && !this.description.isEmpty()) {
            sb.append("comment on package");
            sb.append(" ");
            sb.append(getName());
            sb.append(" ");
            sb.append("is");
            sb.append("\n");
            sb.append("'");
            sb.append(getDescription());
            sb.append("';");
        }

        return sb.toString();
    }

    protected String queryForInfo() {
        String sql_security = "null";
        if (getHost().getDatabaseProductName().toLowerCase().contains("reddatabase"))
            sql_security = "p.rdb$sql_security\n";
        return "select 0,\n" +
                "p.rdb$package_header_source,\n" +
                "p.rdb$package_body_source,\n" +
                "p.rdb$valid_body_flag,\n" +
                "p.rdb$security_class,\n" +
                "p.rdb$owner_name,\n" +
                "p.rdb$system_flag,\n" +
                "p.rdb$description,\n" +
                sql_security +
                "\n" +
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
            setDescription(rs.getString(8));
            setSqlSecurity(rs.getBoolean(9));
        }
    }

    protected void getObjectInfo() {
        super.getObjectInfo();
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        try {
            String query = queryForInfo();
            ResultSet rs = querySender.getResultSet(query).getResultSet();
            setInfoFromResultSet(rs);
        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog("Error get info about" + getName(), e);
        } finally {
            querySender.releaseResources();
            setMarkedForReload(false);
        }
    }
}
