package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private boolean markedReloadProcedures = true;
    private boolean markedReloadFunctions = true;

    private List<AbstractDatabaseObject> procedures;
    private List<DefaultDatabaseFunction> functions;

    private List<NamedObject> childs;

    private DefaultDatabaseMetaTag procedureMetatag;
    private DefaultDatabaseMetaTag functionMetatag;

    public DefaultDatabasePackage(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
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

        return "CREATE OR ALTER PACKAGE  " + getName() +
                "\nAS\n" + this.headerSource;
    }

    public void setHeaderSource(String headerSource) {
        this.headerSource = headerSource;
    }

    public String getBodySource() {

        return "RECREATE PACKAGE BODY " + getName() +
                "\nAS\n" + this.bodySource;
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
        return SQLUtils.generateCreatePackage(getName(), getHeaderSource(), getBodySource(), getRemarks(), getHost().getDatabaseConnection());
    }

    @Override
    public String getDropSQL() throws DataSourceException {
        return SQLUtils.generateDefaultDropQuery("PACKAGE", getName(), getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareCreateSQL() throws DataSourceException {
        String comment = Comparer.isCommentsNeed() ? getRemarks() : null;
        return SQLUtils.generateCreatePackage(getName(), getHeaderSource(), getBodySource(), comment, getHost().getDatabaseConnection());
    }

    @Override
    public String getCompareAlterSQL(AbstractDatabaseObject databaseObject) throws DataSourceException {
        return (!this.getCompareCreateSQL().equals(databaseObject.getCompareCreateSQL())) ?
                databaseObject.getCompareCreateSQL() : "/* there are no changes */";
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
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
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
        setRemarks(getFromResultSet(rs, DESCRIPTION));
        setSqlSecurity(getFromResultSet(rs, SQL_SECURITY));
        return null;
    }

    public void prepareLoadChildren(String metatag) {
        if (childs == null)
            childs = new ArrayList<>();
        if (metatag.contentEquals(NamedObject.META_TYPES[NamedObject.PROCEDURE])) {
            procedures = new ArrayList<>();
            procedureMetatag = new DefaultDatabaseMetaTag(getHost(), null, null, metatag);
        } else if (metatag.contentEquals(NamedObject.META_TYPES[NamedObject.FUNCTION])) {
            functions = new ArrayList<>();
            functionMetatag = new DefaultDatabaseMetaTag(getHost(), null, null, metatag);
        }
    }

    public void addChildFromResultSet(ResultSet rs, String metatag) throws SQLException {
        if (metatag.contentEquals(NamedObject.META_TYPES[NamedObject.PROCEDURE])) {
            DefaultDatabaseProcedure procedure = new DefaultDatabaseProcedure(procedureMetatag, rs.getString(1));
            procedure.setParent(this);
            procedure.setSystemFlag(isSystem());
            procedures.add(procedure);
            childs.add(procedure);
        } else if (metatag.contentEquals(NamedObject.META_TYPES[NamedObject.FUNCTION])) {
            DefaultDatabaseFunction function = new DefaultDatabaseFunction(functionMetatag, rs.getString(1));
            function.setParent(this);
            function.setSystemFlag(isSystem());
            functions.add(function);
            childs.add(function);
        }
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

    public boolean isMarkedReloadProcedures() {
        return markedReloadProcedures;
    }

    public void setMarkedReloadProcedures(boolean markedReloadProcedures) {
        this.markedReloadProcedures = markedReloadProcedures;
    }

    public boolean isMarkedReloadChildren(String metatag) {
        if (metatag.contentEquals(NamedObject.META_TYPES[PROCEDURE]))
            return markedReloadProcedures;
        else return markedReloadFunctions;
    }

    public void setMarkedReloadChildren(boolean markedReloadChildren, String metatag) {
        if (metatag.contentEquals(NamedObject.META_TYPES[PROCEDURE]))
            this.markedReloadProcedures = markedReloadChildren;
        else this.markedReloadFunctions = markedReloadChildren;
    }


    public boolean allowsChildren() {
        return true;
    }

    @Override
    protected String prefixLabel() {
        return null;
    }

    @Override
    protected String mechanismLabel() {
        return null;
    }

    @Override
    protected String positionLabel() {
        return null;
    }

    public List<NamedObject> getObjects() throws DataSourceException {
        return childs;
    }

    public void reset() {
        super.reset();
        markedReloadProcedures = true;
        markedReloadFunctions = true;
        childs = null;
        procedures = null;
        functions = null;
    }
}
