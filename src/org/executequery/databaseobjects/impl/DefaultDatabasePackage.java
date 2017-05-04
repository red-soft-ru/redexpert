package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;

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

    public DefaultDatabasePackage() {}

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
        return headerSource;
    }

    public void setHeaderSource(String headerSource) {
        this.headerSource = headerSource;
    }

    public String getBodySource() {
        return bodySource;
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
}
