/*
 * DefaultDatabaseMetaTag.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.databaseobjects.impl;

import biz.redsoft.IFBDatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.PooledConnection;
import org.executequery.datasource.PooledResultSet;
import org.executequery.gui.browser.ComparerDBPanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.sqlbuilder.Condition;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.InterruptibleThread;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.executequery.gui.browser.tree.TreePanel.DEFAULT;

/**
 * Default meta tag object implementation.
 *
 * @author Takis Diakoumis
 */
public class DefaultDatabaseMetaTag extends AbstractNamedObject
        implements DatabaseMetaTag {

    DatabaseObject dependedObject;
    int typeTree;

    /**
     * Catalog object for this meta tag
     */
    private DatabaseCatalog catalog;

    /**
     * Schema object for this meta tag
     */
    private DatabaseSchema schema;

    /**
     * Host object for this meta tag
     */
    private final DatabaseHost host;

    /**
     * Metadata key name of this object
     */
    private final String metaDataKey;

    /**
     * Child objects of this meta type
     */
    private List<NamedObject> children;

    public DefaultDatabaseMetaTag(
            DatabaseHost host, DatabaseCatalog catalog, DatabaseSchema schema, String metaDataKey, int typeTree) {

        this.typeTree = typeTree;
        this.host = host;
        setCatalog(catalog);
        setSchema(schema);
        this.metaDataKey = metaDataKey;
    }

    public DefaultDatabaseMetaTag(
            DatabaseHost host, DatabaseCatalog catalog, DatabaseSchema schema, String metaDataKey) {

        this(host, catalog, schema, metaDataKey, TreePanel.DEFAULT);
    }

    public DefaultDatabaseMetaTag(
            DatabaseHost host, DatabaseCatalog catalog, DatabaseSchema schema, String metaDataKey,
            int typeTree, DatabaseObject dependedObject) {

        this(host, catalog, schema, metaDataKey, typeTree);
        this.dependedObject = dependedObject;
    }


    /**
     * Returns the db object with the specified name
     * or null if it does not exist
     *
     * @param name the name of the object
     * @return NamedObject or null if not found
     */
    @Override
    public NamedObject getNamedObject(String name) throws DataSourceException {

        List<NamedObject> objects = getObjects();
        if (objects == null)
            return null;

        for (NamedObject object : objects)
            if (name.equalsIgnoreCase(object.getName()))
                return object;

        return null;
    }

    /**
     * Retrieves child objects classified as this tag type.
     * These may be database tables, functions, procedures, sequences, views, etc.
     *
     * @return this meta tag's child database objects.
     */
    @Override
    public List<NamedObject> getObjects() throws DataSourceException {

        if (!isMarkedForReload() && children != null)
            return children;

        int type = getSubType();

        if (type >= SYSTEM_DOMAIN)
            setSystemFlag(true);

        if (type == DATABASE_TRIGGER
                || type == DDL_TRIGGER
                || type == SYSTEM_DOMAIN
                || type == SYSTEM_FUNCTION
                || type == SYSTEM_INDEX
                || type == SYSTEM_TABLE
                || type == SYSTEM_VIEW
                || type == SYSTEM_TRIGGER
                || type == SYSTEM_ROLE
                || type == GLOBAL_TEMPORARY
                || type == SYSTEM_PACKAGE
                || type == ROLE
                || type == TABLESPACE
                || type == JOB
        ) {
            if (typeTree == TreePanel.DEPENDENT || typeTree == TreePanel.DEPENDED_ON)
                return new ArrayList<>();
        }

        children = (type != SYSTEM_FUNCTION) ? loadObjects(type) : getSystemFunctionTypes();


        // loop through and add this object as the parent object
        addAsParentToObjects(children);
        setMarkedForReload(false);
        if (typeTree == DEFAULT && (type == PACKAGE || type == SYSTEM_PACKAGE)) {
            loadChildrenForAllPackages(META_TYPES[PROCEDURE]);
            loadChildrenForAllPackages(META_TYPES[FUNCTION]);
        }
        return children;
    }

    @Override
    public void loadFullInfoForObjects() {

        getHost().setPauseLoadingTreeForSearch(true);

        List<NamedObject> objects = getObjects();
        if (objects.isEmpty())
            return;

        boolean first = true;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = ((AbstractDatabaseObject) objects.get(0)).queryForInfoAllObjects();

        try {

            InterruptibleThread thread = null;
            if (Thread.currentThread() instanceof InterruptibleThread)
                thread = (InterruptibleThread) Thread.currentThread();

            ResultSet rs = querySender.getResultSet(query).getResultSet();

            ComparerDBPanel comparerDBPanel = getComparerDBPanel(
                    thread, "LoadFullInfoForObjects", objects.size());

            int i = 0;
            while (rs != null && rs.next()) {

                if (thread != null && thread.isCanceled()) {
                    querySender.releaseResources();
                    return;
                }

                while (!objects.get(i).getName().contentEquals(MiscUtils.trimEnd(rs.getString(1)))) {
                    i++;
                    if (i >= objects.size())
                        throw new DataSourceException("Error load info for" + metaDataKey);
                    first = true;
                }

                if (first) {
                    ((AbstractDatabaseObject) objects.get(i)).prepareLoadingInfo();
                    if (comparerDBPanel != null)
                        comparerDBPanel.incrementProgressBarValue();
                }

                ((AbstractDatabaseObject) objects.get(i)).setInfoFromSingleRowResultSet(rs, first);
                first = false;
            }

            for (NamedObject namedObject : objects) {
                ((AbstractDatabaseObject) namedObject).finishLoadingInfo();
                ((AbstractDatabaseObject) namedObject).setMarkedForReload(false);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            querySender.releaseResources();
            getHost().setPauseLoadingTreeForSearch(false);
        }

    }

    @Override
    public void loadColumnsForAllTables() {

        List<NamedObject> objects = getObjects();
        if (objects.isEmpty())
            return;

        boolean first = true;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = ((AbstractDatabaseObject) objects.get(0)).getBuilderLoadColsForAllTables().getSQLQuery();

        try {

            InterruptibleThread thread = null;
            if (Thread.currentThread() instanceof InterruptibleThread)
                thread = (InterruptibleThread) Thread.currentThread();

            ResultSet rs = querySender.getResultSet(query).getResultSet();

            ComparerDBPanel comparerDBPanel = getComparerDBPanel(
                    thread, "LoadColumnsForAllTables", objects.size());

            int i = 0;
            AbstractDatabaseObject previousObject = null;
            while (rs != null && rs.next()) {

                if (thread != null && thread.isCanceled()) {
                    querySender.releaseResources();
                    return;
                }

                while (getHost().isPauseLoadingTreeForSearch() && Thread.currentThread().getName().contentEquals("loadingTreeForSearch")) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                while (!objects.get(i).getName().contentEquals(MiscUtils.trimEnd(rs.getString(1)))) {
                    i++;
                    if (i >= objects.size())
                        throw new DataSourceException("Error load columns for " + metaDataKey);
                    first = true;
                }

                AbstractDatabaseObject abstractDatabaseObject = (AbstractDatabaseObject) objects.get(i);
                if (abstractDatabaseObject.isMarkedForReloadCols()) {

                    if (first) {
                        abstractDatabaseObject.prepareLoadColumns();
                        if (previousObject != null) {
                            previousObject.finishLoadColumns();
                            previousObject.setMarkedForReloadCols(false);
                        }
                        if (comparerDBPanel != null)
                            comparerDBPanel.incrementProgressBarValue();
                    }

                    abstractDatabaseObject.addColumnFromResultSet(rs);
                    first = false;
                    previousObject = abstractDatabaseObject;
                }
            }

            if (previousObject != null) {
                previousObject.finishLoadColumns();
                previousObject.setMarkedForReloadCols(false);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            querySender.releaseResources();
            getHost().setPauseLoadingTreeForSearch(false);
        }

    }

    protected Condition checkSystemCondition(boolean isSystem, Table mainTable) {
        if (!isSystem) {
            return Condition.createCondition()
                    .appendCondition(Condition.createCondition(Field.createField(mainTable, "SYSTEM_FLAG"), "IS", "NULL"))
                    .appendCondition(Condition.createCondition(Field.createField(mainTable, "SYSTEM_FLAG"), "=", "0"))
                    .setLogicOperator("OR");
        } else return Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(mainTable, "SYSTEM_FLAG"), "IS", "NOT NULL"))
                .appendCondition(Condition.createCondition(Field.createField(mainTable, "SYSTEM_FLAG"), "<>", "0"))
                .setLogicOperator("AND");
    }

    SelectBuilder getBuilderForPackageChildren(String metatag) {
        SelectBuilder sb = new SelectBuilder(getHost().getDatabaseConnection());
        Table mainTable = Table.createTable("RDB$" + metatag + "S", metatag + "S");
        sb.appendTable(mainTable);
        sb.appendField(Field.createField(mainTable, metatag + "_NAME"));
        sb.appendField(Field.createField(mainTable, "PACKAGE_NAME"));
        sb.appendCondition(Condition.createCondition(Field.createField(mainTable, "PACKAGE_NAME"), "IS", "NOT NULL"));
        sb.appendCondition(checkSystemCondition(getSubType() == NamedObject.SYSTEM_PACKAGE, mainTable));
        sb.setOrdering("RDB$PACKAGE_NAME, RDB$" + metatag + "_NAME");
        return sb;
    }

    public void loadChildrenForAllPackages(String metatag) {

        List<NamedObject> objects = getObjects();
        if (objects.isEmpty())
            return;

        boolean first = true;
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        String query = getBuilderForPackageChildren(metatag).getSQLQuery();

        try {


            ResultSet rs = querySender.getResultSet(query).getResultSet();


            int i = 0;
            DefaultDatabasePackage previousObject = null;
            while (rs != null && rs.next()) {
                String packageName = rs.getString("PACKAGE_NAME");
                while (!objects.get(i).getName().contentEquals(MiscUtils.trimEnd(packageName))) {
                    i++;
                    if (i >= objects.size())
                        new DataSourceException("Error load all children for " + metaDataKey).printStackTrace();
                    first = true;
                }

                DefaultDatabasePackage defaultDatabasePackage = (DefaultDatabasePackage) objects.get(i);
                if (defaultDatabasePackage.isMarkedReloadChildren(metatag)) {

                    if (first) {
                        defaultDatabasePackage.prepareLoadChildren(metatag);
                        if (previousObject != null) {
                            //previousObject.finishLoadColumns();
                            previousObject.setMarkedReloadChildren(false, metatag);
                        }
                    }

                    defaultDatabasePackage.addChildFromResultSet(rs, metatag);
                    first = false;
                    previousObject = defaultDatabasePackage;
                }
            }

            if (previousObject != null) {
                previousObject.setMarkedReloadChildren(false, metatag);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);

        } finally {
            querySender.releaseResources();
        }

    }


    private ComparerDBPanel getComparerDBPanel(InterruptibleThread thread, String labelKey, int objectsSize) {

        ComparerDBPanel comparerDBPanel = null;
        if (thread != null) {

            Object threadUserObject = thread.getUserObject();
            if (threadUserObject instanceof ComparerDBPanel) {
                comparerDBPanel = ((ComparerDBPanel) threadUserObject);
                comparerDBPanel.recreateProgressBar(labelKey, NamedObject.META_TYPES_FOR_BUNDLE[getSubType()], objectsSize);
            }
        }

        return comparerDBPanel;
    }

    private void addAsParentToObjects(List<NamedObject> children) {
        if (children != null)
            for (NamedObject child : children)
                child.setParent(this);
    }

    private ResultSet getObjectsResultSet(int type) throws SQLException {
        switch (type) {
            case DOMAIN:
                return getDomainsResultSet();
            case PROCEDURE:
                return getProceduresResultSet();
            case FUNCTION:
                return getFunctionsResultSet();
            case PACKAGE:
                return getPackagesResultSet();
            case TRIGGER:
                return getTriggersResultSet();
            case DDL_TRIGGER:
                return getDDLTriggerResultSet();
            case DATABASE_TRIGGER:
                return getDatabaseTriggerResultSet();
            case SEQUENCE:
                return getSequencesResultSet();
            case EXCEPTION:
                return getExceptionResultSet();
            case UDF:
                return getUDFResultSet();
            case USER:
                return getUsersResultSet();
            case ROLE:
                return getRolesResultSet();
            case INDEX:
                return getIndicesResultSet();
            case TABLESPACE:
                return getTablespacesResultSet();
            case JOB:
                return getJobsResultSet();
            case COLLATION:
                return getCollationsResultSet();
            case SYSTEM_DOMAIN:
                return getSystemDomainResultSet();
            case SYSTEM_TRIGGER:
                return getSystemTriggerResultSet();
            case SYSTEM_SEQUENCE:
                return getSystemSequencesResultSet();
            case SYSTEM_ROLE:
                return getSystemRolesResultSet();
            case SYSTEM_INDEX:
                return getSystemIndexResultSet();
            case SYSTEM_PACKAGE:
                return getSystemPackagesResultSet();
            default:
                ResultSet rs = getTablesResultSet(getMetaDataKey(), false);
                return (rs != null) ? rs : getTablesResultSet(getMetaDataKey(), true);
        }
    }

    private List<NamedObject> loadObjects(int type) throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getObjectsResultSet(type);
            if (rs == null)
                return new ArrayList<>();

            List<NamedObject> list = new ArrayList<>();
            while (rs.next()) {

                if (!getHost().getDatabaseConnection().isConnected())
                    return new ArrayList<>();

                AbstractDatabaseObject namedObject = null;
                switch (type) {

                    case DOMAIN:
                    case SYSTEM_DOMAIN:
                        namedObject = getDomain(rs);
                        break;

                    case TABLE:
                    case GLOBAL_TEMPORARY:
                    case VIEW:
                    case SYSTEM_TABLE:
                    case SYSTEM_VIEW:
                        namedObject = getTable(rs, getMetaDataKey(), type);
                        break;

                    case PROCEDURE:
                        namedObject = getProcedure(rs);
                        break;

                    case FUNCTION:
                        namedObject = getFunction(rs);
                        break;

                    case PACKAGE:
                    case SYSTEM_PACKAGE:
                        namedObject = getPackage(rs);
                        break;

                    case TRIGGER:
                    case DDL_TRIGGER:
                    case DATABASE_TRIGGER:
                    case SYSTEM_TRIGGER:
                        namedObject = getTrigger(rs);
                        break;

                    case SEQUENCE:
                    case SYSTEM_SEQUENCE:
                        namedObject = getSequence(rs);
                        break;

                    case EXCEPTION:
                        namedObject = getException(rs);
                        break;

                    case UDF:
                        namedObject = getUDF(rs);
                        break;

                    case USER:
                        namedObject = getUser(rs);
                        break;

                    case ROLE:
                    case SYSTEM_ROLE:
                        namedObject = getRole(rs);
                        break;

                    case INDEX:
                    case SYSTEM_INDEX:
                        namedObject = getIndex(rs);
                        break;

                    case TABLESPACE:
                        namedObject = getTablespace(rs);
                        break;

                    case JOB:
                        namedObject = getJob(rs);
                        break;

                    case COLLATION:
                        namedObject = getCollation(rs);
                        break;
                }

                if (namedObject != null) {
                    namedObject.setSystemFlag(type >= SYSTEM_DOMAIN);
                    namedObject.setHost(getHost());
                    list.add(namedObject);
                }
            }
            return list;

        } catch (SQLException e) {
           e.printStackTrace();
            return new ArrayList<>();

        } finally {
            try {
                releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
            } catch (SQLException e) {
                releaseResources(rs, null);
            }
        }
    }

    /**
     * Loads the database tables
     */
    private AbstractDatabaseObject getTable(ResultSet rs, String metaDataKey, int type) throws SQLException {

        String tableName = rs.getString(1);
        DefaultDatabaseObject object = new DefaultDatabaseObject(this, metaDataKey);
        object.setName(tableName);

        if (typeTree != DEFAULT) {
            object.setTypeTree(typeTree);
            object.setDependObject(dependedObject);
        }
        if (metaDataKey.contains("SYSTEM"))
            object.setSystemFlag(true);

        switch (type) {
            case TABLE:
                return new DefaultDatabaseTable(object);
            case VIEW:
                return new DefaultDatabaseView(object);
            case GLOBAL_TEMPORARY:
                return new DefaultTemporaryDatabaseTable(object);
            default:
                return object;
        }
    }

    /**
     * Loads the database functions
     */
    private AbstractDatabaseObject getFunction(ResultSet rs) throws SQLException {

        if (typeTree == TreePanel.DEFAULT) {
            DefaultDatabaseFunction function = new DefaultDatabaseFunction(this, rs.getString(3));
            function.setRemarks(rs.getString(4));
            return function;

        } else
            return new DefaultDatabaseFunction(this, rs.getString(1));
    }

    /**
     * Loads the database procedures
     */
    private AbstractDatabaseObject getProcedure(ResultSet rs) throws SQLException {

        if (((PooledResultSet) rs).getResultSet().unwrap(ResultSet.class).getClass().getName().contains("FBResultSet")) {
            return new DefaultDatabaseProcedure(this, rs.getString(1));

        } else {
            DefaultDatabaseProcedure procedure = new DefaultDatabaseProcedure(this, rs.getString(3));
            procedure.setRemarks(rs.getString(7));
            return procedure;
        }
    }

    /**
     * Loads the database indices
     */
    private AbstractDatabaseObject getIndex(ResultSet rs) throws SQLException {

        DefaultDatabaseIndex index = new DefaultDatabaseIndex(this, MiscUtils.trimEnd(rs.getString(1)));
        index.setHost(this.getHost());
        index.setActive(rs.getInt(2) != 1);

        return index;
    }

    /**
     * Loads the database indices by the name
     */
    public DefaultDatabaseIndex getIndexFromName(String name) throws DataSourceException {

        ResultSet rs = null;
        DefaultDatabaseIndex index = null;
        try {

            rs = getIndexFromNameResultSet(name);
            while (rs.next()) {

                index = new DefaultDatabaseIndex(this, MiscUtils.trimEnd(rs.getString(1)));
                index.setTableName(rs.getString(2));
                index.setIndexType(rs.getInt(4));
                index.setActive(rs.getInt(6) != 1);
                index.setUnique(rs.getInt(5) == 1);
                index.setRemarks(rs.getString(7));
                index.setConstraint_type(rs.getString(8));
                index.setHost(this.getHost());
            }
            return index;

        } catch (SQLException e) {
            logThrowable(e);
            return null;

        } finally {
            try {
                releaseResources(rs, getHost().getDatabaseMetaData().getConnection());
            } catch (SQLException e) {
                releaseResources(rs, null);
            }
        }
    }

    /**
     * Loads the database triggers
     */
    private AbstractDatabaseObject getTrigger(ResultSet rs) throws SQLException {

        DefaultDatabaseTrigger trigger = new DefaultDatabaseTrigger(
                this, MiscUtils.trimEnd(rs.getString(1)));

        if (typeTree == TreePanel.DEFAULT)
            trigger.setTriggerActive(rs.getInt(2) != 1);
        else
            trigger.getObjectInfo();

        return trigger;
    }

    /**
     * Loads the database sequences
     */
    private AbstractDatabaseObject getSequence(ResultSet rs) throws SQLException {
        return new DefaultDatabaseSequence(this, rs.getString(1));
    }

    /**
     * Loads the database domains
     */
    private AbstractDatabaseObject getDomain(ResultSet rs) throws SQLException {
        return new DefaultDatabaseDomain(this, rs.getString(1));
    }

    /**
     * Loads the database users
     */
    private AbstractDatabaseObject getUser(ResultSet rs) throws SQLException {
        return new DefaultDatabaseUser(this, rs.getObject(1).toString());
    }

    /**
     * Loads the database indices
     */
    private AbstractDatabaseObject getTablespace(ResultSet rs) throws SQLException {
        return new DefaultDatabaseTablespace(this, rs.getObject(1).toString());
    }

    /**
     * Loads the database jobs
     */
    private AbstractDatabaseObject getJob(ResultSet rs) throws SQLException {
        return new DefaultDatabaseJob(this, rs.getObject(1).toString());
    }

    /**
     * Loads the database collations
     */
    private AbstractDatabaseObject getCollation(ResultSet rs) throws SQLException {
        return new DefaultDatabaseCollation(this, rs.getObject(1).toString());
    }

    /**
     * Loads the database roles
     */
    private AbstractDatabaseObject getRole(ResultSet rs) throws SQLException {
        return new DefaultDatabaseRole(this, rs.getObject(1).toString());
    }

    /**
     * Loads the database exceptions
     */
    private AbstractDatabaseObject getException(ResultSet rs) throws SQLException {
        DefaultDatabaseException exception = new DefaultDatabaseException(this, rs.getString(1));
        exception.setRemarks(rs.getString(2));
        return exception;
    }

    /**
     * Loads the database UDFs
     */
    private AbstractDatabaseObject getUDF(ResultSet rs) throws SQLException {

        DefaultDatabaseUDF udf = new DefaultDatabaseUDF(
                this, MiscUtils.trimEnd(rs.getString(1)), this.getHost());

        String moduleName = rs.getString(3);
        if (!MiscUtils.isNull(moduleName))
            udf.setModuleName(moduleName.trim());

        String entryPoint = rs.getString(4);
        if (!MiscUtils.isNull(entryPoint))
            udf.setEntryPoint(entryPoint.trim());

        udf.setReturnArg(rs.getInt(5));
        udf.setRemarks(rs.getString("description"));

        return udf;
    }

    /**
     * Loads the database packages
     */
    private AbstractDatabaseObject getPackage(ResultSet rs) throws SQLException {
        return new DefaultDatabasePackage(this, MiscUtils.trimEnd(rs.getString(1)));
    }

    private ResultSet getResultSetFromQuery(String query) throws SQLException {
        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        return querySender.getResultSet(query).getResultSet();
    }

    private ResultSet getProceduresResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Connection realConnection = ((PooledConnection) dmd.getConnection()).getRealConnection();

        if (realConnection.unwrap(Connection.class).getClass().getName().contains("FBConnection")) { // Red Database or FB

            int majorVersion = 0;
            try {
                Connection fbConn = realConnection.unwrap(Connection.class);
                IFBDatabaseConnection db = (IFBDatabaseConnection) DynamicLibraryLoader.loadingObjectFromClassLoader(
                        getHost().getDatabaseConnection().getDriverMajorVersion(), fbConn, "FBDatabaseConnectionImpl");
                db.setConnection(fbConn);
                majorVersion = db.getMajorVersion();

            } catch (ClassNotFoundException e) {
                e.printStackTrace(System.out);
            }

            String sql = "SELECT CAST (RDB$PROCEDURE_NAME as VARCHAR(1024)) AS PROCEDURE_NAME\n" +
                    "FROM RDB$PROCEDURES\n" +
                    ((majorVersion > 2) ? "WHERE RDB$PACKAGE_NAME IS NULL\n" : "") +
                    "ORDER BY PROCEDURE_NAME";

            if (typeTree == TreePanel.DEPENDED_ON)
                sql = getDependOnQuery(5);
            else if (typeTree == TreePanel.DEPENDENT)
                sql = getDependentQuery(5);

            return getResultSetFromQuery(sql);

        } else // Another database
            return dmd.getProcedures(catalogName, schemaName, null);
    }

    private ResultSet getIndicesResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (I.RDB$INDEX_NAME as VARCHAR(1024))," +
                "I.RDB$INDEX_INACTIVE\n" +
                "FROM RDB$INDICES AS I\n" +
                "WHERE I.RDB$SYSTEM_FLAG = 0\n" +
                "ORDER BY I.RDB$INDEX_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(10);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(10);
        else if (typeTree == TreePanel.TABLESPACE)
            query = ((DefaultDatabaseTablespace) dependedObject).getIndexesQuery();

        return getResultSetFromQuery(query);
    }

    private ResultSet getIndexFromNameResultSet(String name) throws SQLException {

        String query = "SELECT\n" +
                "I.RDB$INDEX_NAME, " +
                "I.RDB$RELATION_NAME, " +
                "I.RDB$SYSTEM_FLAG," +
                "I.RDB$INDEX_TYPE," +
                "I.RDB$UNIQUE_FLAG," +
                "I.RDB$INDEX_INACTIVE," +
                "I.RDB$DESCRIPTION," +
                "C.RDB$CONSTRAINT_TYPE\n" +
                "FROM RDB$INDICES AS I\n" +
                "LEFT JOIN RDB$RELATION_CONSTRAINTS AS C ON I.RDB$INDEX_NAME = C.RDB$INDEX_NAME\n" +
                "WHERE I.RDB$SYSTEM_FLAG = 0 AND I.RDB$INDEX_NAME = ?";

        DefaultStatementExecutor querySender = new DefaultStatementExecutor(getHost().getDatabaseConnection());
        PreparedStatement st = querySender.getPreparedStatement(query);
        st.setString(1, name);

        return querySender.getResultSet(-1, st).getResultSet();
    }

    private ResultSet getTriggersResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (T.RDB$TRIGGER_NAME as VARCHAR(1024)),\n" +
                "T.RDB$TRIGGER_INACTIVE\n" +
                "FROM RDB$TRIGGERS T\n" +
                "WHERE T.RDB$SYSTEM_FLAG = 0\n" +
                "AND T.RDB$TRIGGER_TYPE <= 114\n" +
                "ORDER BY T.RDB$TRIGGER_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(2);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(2);

        return getResultSetFromQuery(query);
    }


    private ResultSet getSequencesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$GENERATOR_NAME as VARCHAR(1024))\n" +
                "FROM RDB$GENERATORS\n" +
                "WHERE ((RDB$SYSTEM_FLAG IS NULL) OR (RDB$SYSTEM_FLAG = 0))\n" +
                "ORDER BY RDB$GENERATOR_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(14);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(14);

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemSequencesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$GENERATOR_NAME as VARCHAR(1024))\n" +
                "FROM RDB$GENERATORS\n" +
                "WHERE ((RDB$SYSTEM_FLAG IS NOT NULL) AND (RDB$SYSTEM_FLAG != 0))\n" +
                "ORDER BY  RDB$GENERATOR_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(14);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(14);

        return getResultSetFromQuery(query);
    }

    private ResultSet getDomainsResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$FIELD_NAME as VARCHAR(1024))\n" +
                "FROM RDB$FIELDS\n" +
                "WHERE (NOT (RDB$FIELD_NAME STARTING WITH 'RDB$')) AND (RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL)\n" +
                "ORDER BY RDB$FIELD_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(9);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(9);

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemDomainResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$FIELD_NAME as VARCHAR(1024))\n" +
                "FROM RDB$FIELDS\n" +
                "WHERE (RDB$FIELD_NAME STARTING WITH 'RDB$') OR (RDB$SYSTEM_FLAG <> 0 AND RDB$SYSTEM_FLAG IS NOT NULL)\n" +
                "ORDER BY RDB$FIELD_NAME";

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemRolesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$ROLE_NAME as VARCHAR(1024))\n" +
                "FROM RDB$ROLES\n" +
                "WHERE RDB$SYSTEM_FLAG != 0 AND RDB$SYSTEM_FLAG IS NOT NULL\n" +
                "ORDER BY 1";

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemPackagesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$PACKAGE_NAME as VARCHAR(1024))\n" +
                "FROM RDB$PACKAGES\n" +
                "WHERE RDB$SYSTEM_FLAG != 0 AND RDB$SYSTEM_FLAG IS NOT NULL\n" +
                "ORDER BY 1";

        return getResultSetFromQuery(query);
    }

    private ResultSet getUsersResultSet() throws SQLException {

        String query = "SELECT CAST (SEC$USER_NAME as VARCHAR(1024)) FROM SEC$USERS ORDER BY 1";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(8);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(8);

        return getResultSetFromQuery(query);
    }

    private ResultSet getRolesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$ROLE_NAME as VARCHAR(1024))\n" +
                "FROM RDB$ROLES\n" +
                "WHERE RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL\n" +
                "ORDER BY 1";

        return getResultSetFromQuery(query);
    }

    private ResultSet getTablespacesResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$TABLESPACE_NAME as VARCHAR(1024)) FROM RDB$TABLESPACES ORDER BY 1";
        return getResultSetFromQuery(query);
    }

    private ResultSet getJobsResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$JOB_NAME as VARCHAR(1024)) FROM RDB$JOBS ORDER BY 1";
        return getResultSetFromQuery(query);
    }

    private ResultSet getCollationsResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$COLLATION_NAME as VARCHAR(1024))\n" +
                "FROM RDB$COLLATIONS\n" +
                "WHERE RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL\n" +
                "ORDER BY 1";

        return getResultSetFromQuery(query);
    }

    private ResultSet getExceptionResultSet() throws SQLException {

        String query = "SELECT CAST (RDB$EXCEPTION_NAME as VARCHAR(1024)), RDB$DESCRIPTION\n" +
                "FROM RDB$EXCEPTIONS\n" +
                "ORDER BY RDB$EXCEPTION_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(7);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(7);

        return getResultSetFromQuery(query);
    }

    private ResultSet getUDFResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (RDB$FUNCTION_NAME as VARCHAR(1024))," +
                "RDB$DESCRIPTION," +
                "RDB$MODULE_NAME," +
                "RDB$ENTRYPOINT," +
                "RDB$RETURN_ARGUMENT," +
                "RDB$DESCRIPTION AS DESCRIPTION\n" +
                "FROM RDB$FUNCTIONS\n" +
                ((getHost().getDatabaseMetaData().getDatabaseMajorVersion() == 2) ?
                        "WHERE RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL\n" :
                        "WHERE RDB$LEGACY_FLAG = 1 AND (RDB$MODULE_NAME IS NOT NULL) AND (RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL)\n"
                ) +
                "ORDER BY RDB$FUNCTION_NAME";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(15);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(15);

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemIndexResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (I.RDB$INDEX_NAME as VARCHAR(1024))," +
                "I.RDB$INDEX_INACTIVE\n" +
                "FROM RDB$INDICES AS I\n" +
                "LEFT JOIN RDB$RELATION_CONSTRAINTS AS C ON I.RDB$INDEX_NAME = C.RDB$INDEX_NAME\n" +
                "WHERE I.RDB$SYSTEM_FLAG = 1\n" +
                "ORDER BY I.RDB$INDEX_NAME";

        return getResultSetFromQuery(query);
    }

    private ResultSet getSystemTriggerResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (T.RDB$TRIGGER_NAME as VARCHAR(1024))," +
                "T.RDB$TRIGGER_INACTIVE\n" +
                "FROM RDB$TRIGGERS T\n" +
                "WHERE T.RDB$SYSTEM_FLAG <> 0\n" +
                "ORDER BY T.RDB$TRIGGER_NAME";

        return getResultSetFromQuery(query);
    }

    private ResultSet getDDLTriggerResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (T.RDB$TRIGGER_NAME as VARCHAR(1024))," +
                "T.RDB$TRIGGER_INACTIVE\n" +
                "FROM RDB$TRIGGERS T\n" +
                "WHERE T.RDB$SYSTEM_FLAG = 0\n" +
                "AND BIN_AND(T.RDB$TRIGGER_TYPE," + DefaultDatabaseTrigger.RDB_TRIGGER_TYPE_MASK + ") = " + DefaultDatabaseTrigger.TRIGGER_TYPE_DDL + "\n" +
                "ORDER BY T.RDB$TRIGGER_NAME";

        return getResultSetFromQuery(query);
    }

    private ResultSet getDatabaseTriggerResultSet() throws SQLException {

        String query = "SELECT\n" +
                "CAST (T.RDB$TRIGGER_NAME as VARCHAR(1024))," +
                "T.RDB$TRIGGER_INACTIVE\n" +
                "FROM RDB$TRIGGERS T\n" +
                "WHERE T.RDB$SYSTEM_FLAG = 0\n" +
                "AND BIN_AND(T.RDB$TRIGGER_TYPE," + DefaultDatabaseTrigger.RDB_TRIGGER_TYPE_MASK + ") = " + DefaultDatabaseTrigger.TRIGGER_TYPE_DB + "\n" +
                "ORDER BY T.RDB$TRIGGER_NAME";

        return getResultSetFromQuery(query);
    }

    private ResultSet getPackagesResultSet() throws SQLException {

        String query = "SELECT CAST (P.RDB$PACKAGE_NAME as VARCHAR(1024))\n" +
                "FROM RDB$PACKAGES P\n" +
                "WHERE RDB$SYSTEM_FLAG = 0 OR RDB$SYSTEM_FLAG IS NULL ORDER BY 1";

        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(19);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(19);

        return getResultSetFromQuery(query);
    }

    private ResultSet getTablesResultSet(String metaDataKey, boolean repeat) throws SQLException {

        String query = null;
        if (metaDataKey.equals(NamedObject.META_TYPES[TABLE])) {

            query = "SELECT CAST (RDB$RELATION_NAME as VARCHAR(1024))\n" +
                    "FROM RDB$RELATIONS\n" +
                    "WHERE RDB$VIEW_BLR IS NULL\n" +
                    "AND (RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0)\n" +
                    ((getHost().getDatabaseMetaData().getDatabaseMajorVersion() < 2 || repeat) ? "" :
                            "AND (RDB$RELATION_TYPE = 0 OR RDB$RELATION_TYPE = 2 OR RDB$RELATION_TYPE IS NULL)\n") +
                    "ORDER BY RDB$RELATION_NAME";

            if (typeTree == TreePanel.DEPENDED_ON)
                query = getDependOnQuery(0);
            else if (typeTree == TreePanel.DEPENDENT)
                query = getDependentQuery(0);
            else if (typeTree == TreePanel.TABLESPACE)
                query = ((DefaultDatabaseTablespace) dependedObject).getTablesQuery();

        } else if (metaDataKey.equals(NamedObject.META_TYPES[SYSTEM_TABLE])) {

            query = "SELECT CAST (RDB$RELATION_NAME as VARCHAR(1024))\n" +
                    "FROM RDB$RELATIONS\n" +
                    "WHERE RDB$VIEW_BLR IS NULL\n" +
                    "AND (RDB$SYSTEM_FLAG IS NOT NULL AND RDB$SYSTEM_FLAG = 1)\n" +
                    "ORDER BY RDB$RELATION_NAME";

        } else if (metaDataKey.equals(NamedObject.META_TYPES[VIEW])) {

            query = "SELECT CAST (RDB$RELATION_NAME as VARCHAR(1024))\n" +
                    "FROM RDB$RELATIONS\n" +
                    "WHERE RDB$VIEW_BLR IS NOT NULL\n" +
                    "AND (RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0)\n" +
                    "ORDER BY RDB$RELATION_NAME";

            if (typeTree == TreePanel.DEPENDED_ON)
                query = getDependOnQuery(1);
            else if (typeTree == TreePanel.DEPENDENT)
                query = getDependentQuery(1);

        } else if (metaDataKey.equals(NamedObject.META_TYPES[SYSTEM_VIEW])) {

            query = "SELECT CAST (RDB$RELATION_NAME as VARCHAR(1024))\n" +
                    "FROM RDB$RELATIONS\n" +
                    "WHERE RDB$VIEW_BLR IS NOT NULL\n" +
                    "AND (RDB$SYSTEM_FLAG IS NOT NULL AND RDB$SYSTEM_FLAG = 1)\n" +
                    "ORDER BY RDB$RELATION_NAME";

        } else if (metaDataKey.equals(NamedObject.META_TYPES[GLOBAL_TEMPORARY])) {

            query = "SELECT CAST (R.RDB$RELATION_NAME as VARCHAR(1024))\n" +
                    "FROM RDB$RELATIONS R\n" +
                    "JOIN RDB$TYPES T ON R.RDB$RELATION_TYPE = T.RDB$TYPE\n" +
                    "WHERE (T.RDB$FIELD_NAME = 'RDB$RELATION_TYPE')\n" +
                    "AND (T.RDB$TYPE = 4 OR T.RDB$TYPE = 5)\n" +
                    "ORDER BY R.RDB$RELATION_NAME";
        }

        return query != null ? getResultSetFromQuery(query) : null;
    }

    private ResultSet getFunctionsResultSet() throws SQLException {

        try {

            String catalogName = catalogNameForQuery();
            String schemaName = schemaNameForQuery();

            String query = "SELECT 0, 0,\n" +
                    "CAST (RDB$FUNCTION_NAME as VARCHAR(1024)) AS FUNCTION_NAME,\n" +
                    "RDB$DESCRIPTION AS REMARKS\n" +
                    "FROM RDB$FUNCTIONS\n" +
                    "WHERE (RDB$MODULE_NAME IS NULL) AND (RDB$PACKAGE_NAME IS NULL)\n" +
                    "ORDER BY FUNCTION_NAME ";

            DatabaseMetaData dmd = getHost().getDatabaseMetaData();
            Connection realConnection = ((PooledConnection) dmd.getConnection()).getRealConnection();
            if (realConnection.unwrap(Connection.class).getClass().getName().contains("FBConnection")) {

                if (typeTree == TreePanel.DEPENDED_ON)
                    query = getDependOnQuery(15);
                else if (typeTree == TreePanel.DEPENDENT)
                    query = getDependentQuery(15);

                return getResultSetFromQuery(query);

            } else
                return dmd.getFunctions(catalogName, schemaName, null);

        } catch (Throwable e) {

            // possible SQLFeatureNotSupportedException
            Log.warning("Error retrieving database functions - " + e.getMessage());
            Log.warning("Reverting to old function retrieval implementation");

            return getFunctionResultSetOldImpl();
        }
    }

    private ResultSet getFunctionResultSetOldImpl() throws SQLException {
        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        return dmd.getProcedures(getCatalogName(), getSchemaName(), null);
    }

    private String schemaNameForQuery() {
        return getHost().getSchemaNameForQueries(getSchemaName());
    }

    private String catalogNameForQuery() {
        return getHost().getCatalogNameForQueries(getCatalogName());
    }

    /**
     * Loads the system function types.
     */
    private List<NamedObject> getSystemFunctionTypes() {

        List<NamedObject> objects = new ArrayList<>(3);
        objects.add(new DefaultSystemFunctionMetaTag(this, SYSTEM_STRING_FUNCTIONS, "String Functions"));
        objects.add(new DefaultSystemFunctionMetaTag(this, SYSTEM_NUMERIC_FUNCTIONS, "Numeric Functions"));
        objects.add(new DefaultSystemFunctionMetaTag(this, SYSTEM_DATE_TIME_FUNCTIONS, "Date/Time Functions"));

        return objects;
    }

    /**
     * Returns the catalog name or null if there is
     * no catalog attached.
     */
    private String getCatalogName() {
        DatabaseCatalog catalog = getCatalog();
        return (catalog != null) ? catalog.getName() : null;
    }

    /**
     * Returns the schema name or null if there is
     * no schema attached.
     */
    private String getSchemaName() {
        DatabaseSchema schema = getSchema();
        return (schema != null) ? schema.getName() : null;
    }

    /**
     * Returns the subtype indicator of this meta tag - the type this
     * meta tag ultimately represents.
     *
     * @return subtype, or -1 if not found/available
     */
    @Override
    public int getSubType() {

        String key = getMetaDataKey();
        for (int i = 0; i < META_TYPES.length; i++)
            if (META_TYPES[i].equals(key))
                return i;

        return -1;
    }

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    @Override
    public DatabaseHost getHost() {
        return host;
    }

    /**
     * Returns the name of this object.
     *
     * @return the object name
     */
    @Override
    public String getName() {
        return Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[getSubType()]);
    }

    /**
     * Override to do nothing - name is the metadata key value.
     */
    @Override
    public void setName(String name) {
    }

    /**
     * Returns the parent catalog object.
     *
     * @return the parent catalog object
     */
    @Override
    public DatabaseCatalog getCatalog() {
        return catalog;
    }

    /**
     * Returns the parent schema object.
     *
     * @return the parent schema object
     */
    @Override
    public DatabaseSchema getSchema() {
        return schema;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object - catalog or schema
     */
    @Override
    public NamedObject getParent() {
        return getSchema() == null ? getCatalog() : getSchema();
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    @Override
    public int getType() {
        return META_TAG;
    }

    /**
     * Returns the metadata key name of this object.
     *
     * @return the metadata key name.
     */
    @Override
    public String getMetaDataKey() {
        return metaDataKey;
    }

    /**
     * Does nothing.
     */
    @Override
    public int drop() throws DataSourceException {
        return 0;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    public void setCatalog(DatabaseCatalog catalog) {
        this.catalog = catalog;
    }

    public void setSchema(DatabaseSchema schema) {
        this.schema = schema;
    }

    public int getTypeTree() {
        return typeTree;
    }

    public void setTypeTree(int typeTree) {
        this.typeTree = typeTree;
    }

    private List<Integer> getTypeDependFromDatabaseObject(DatabaseObject databaseObject) {

        ArrayList<Integer> list = new ArrayList<>();
        if (databaseObject instanceof DefaultDatabaseTable)
            list.add(0);
        if (databaseObject instanceof DefaultDatabaseView)
            list.add(1);
        if (databaseObject instanceof DefaultDatabaseTrigger)
            list.add(2);
        if (databaseObject instanceof DefaultDatabaseProcedure)
            list.add(5);
        if (databaseObject instanceof DefaultDatabaseIndex)
            list.add(6);
        if (databaseObject instanceof DefaultDatabaseException)
            list.add(7);
        if (databaseObject instanceof DefaultDatabaseDomain)
            list.add(9);
        if (databaseObject instanceof DefaultDatabaseIndex)
            list.add(10);
        if (databaseObject instanceof DefaultDatabaseSequence)
            list.add(14);
        if (databaseObject instanceof DefaultDatabaseFunction)
            list.add(15);
        if (databaseObject instanceof DefaultDatabasePackage) {
            list.add(18);
            list.add(19);
        }

        return list;
    }

    private String getDependOnQuery(int typeObject) {

        String query;
        int version = ((AbstractDatabaseObject) dependedObject).getDatabaseMajorVersion();
        List<Integer> list = getTypeDependFromDatabaseObject(dependedObject);

        String domainsQuery = "SELECT DISTINCT\n" +
                "RDB$FIELD_SOURCE," +
                "CAST(NULL AS VARCHAR(64))," +
                "CAST(9 AS INTEGER)\n" +
                "FROM RDB$RELATION_FIELDS\n" +
                "WHERE (RDB$RELATION_NAME = '" + dependedObject.getName() + "') AND (RDB$FIELD_SOURCE NOT STARTING WITH 'RDB$')\n" +
                "UNION ALL\n";

        String tableQuery = "SELECT DISTINCT\n" +
                "C.RDB$RELATION_NAME AS FK_TABLE," +
                "NULL," +
                "CAST(0 AS INTEGER)\n" +
                "FROM RDB$REF_CONSTRAINTS B," +
                "RDB$RELATION_CONSTRAINTS A," +
                "RDB$RELATION_CONSTRAINTS C," +
                "RDB$INDEX_SEGMENTS D," +
                "RDB$INDEX_SEGMENTS E," +
                "RDB$INDICES I\n" +
                "WHERE (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY')\n" +
                "AND (A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME)\n" +
                "AND (B.RDB$CONST_NAME_UQ = C.RDB$CONSTRAINT_NAME)\n" +
                "AND (C.RDB$INDEX_NAME = D.RDB$INDEX_NAME)\n" +
                "AND (A.RDB$INDEX_NAME = E.RDB$INDEX_NAME)\n" +
                "AND (A.RDB$INDEX_NAME = I.RDB$INDEX_NAME)\n" +
                "AND (A.RDB$RELATION_NAME = '" + dependedObject.getName() + "')\n" +
                "UNION ALL\n";

        String packageQuery = "SELECT DISTINCT\n" +
                "T2.RDB$PACKAGE_NAME," +
                "CAST(T2.RDB$FIELD_NAME AS VARCHAR(64))," +
                "CAST(19 AS INTEGER)\n" +
                "FROM RDB$DEPENDENCIES T2\n" +
                "WHERE (T2.RDB$DEPENDENT_NAME = 'COUNTRY') AND (T2.RDB$DEPENDENT_TYPE = 0)\n" +
                ((version > 2) ? "AND (T2.RDB$PACKAGE_NAME IS NULL)\n" : "") +
                "UNION ALL\n";

        String comparingCondition = "AND (";
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) comparingCondition += " OR ";
            comparingCondition += "(T1.RDB$DEPENDENT_TYPE = " + list.get(i) + ")";
        }
        comparingCondition += ")";

        query = "SELECT DISTINCT\n" +
                "T1.RDB$DEPENDED_ON_NAME," +
                "NULL," +
                "CAST(T1.RDB$DEPENDED_ON_TYPE AS INTEGER)\n" +
                "FROM RDB$DEPENDENCIES T1\n" +
                "WHERE (T1.RDB$DEPENDENT_NAME = '" + dependedObject.getName() + "')\n" +
                ((version > 2) ? "AND (T1.RDB$PACKAGE_NAME IS NULL)\n" : "") +
                comparingCondition +
                "AND (T1.RDB$DEPENDED_ON_TYPE=" + typeObject + ")\n" +
                "UNION ALL\n" +
                "SELECT DISTINCT\n" +
                "D.RDB$DEPENDED_ON_NAME," +
                "NULL," +
                "CAST(D.RDB$DEPENDED_ON_TYPE AS INTEGER)\n" +
                "FROM RDB$DEPENDENCIES D, RDB$RELATION_FIELDS F\n" +
                "WHERE (D.RDB$DEPENDENT_TYPE = 3)\n" +
                "AND (D.RDB$DEPENDENT_NAME = F.RDB$FIELD_SOURCE)\n" +
                "AND (F.RDB$RELATION_NAME = '" + dependedObject.getName() + "')\n" +
                "AND (D.RDB$DEPENDED_ON_TYPE ='" + typeObject + "')\n" +
                "ORDER BY 1,2\n";

        if (typeObject == 9)
            query = domainsQuery + query;
        if (typeObject == 18 || typeObject == 19)
            query = packageQuery + query;
        if (typeObject == 0)
            query = tableQuery + query;

        return query;
    }

    private String getDependentQuery(int typeObject) {

        String query;
        ((AbstractDatabaseObject) dependedObject).getDatabaseMajorVersion();
        List<Integer> list = getTypeDependFromDatabaseObject(dependedObject);

        String tableQuery = "UNION ALL\n" +
                "SELECT DISTINCT F2.RDB$RELATION_NAME\n" +
                "FROM RDB$DEPENDENCIES D2, RDB$RELATION_FIELDS F2\n" +
                "LEFT JOIN RDB$RELATIONS R2 ON ((F2.RDB$RELATION_NAME = R2.RDB$RELATION_NAME) AND (NOT (R2.RDB$VIEW_BLR IS NULL)))\n" +
                "WHERE (D2.RDB$DEPENDENT_TYPE = 3)\n" +
                "AND (D2.RDB$DEPENDENT_NAME = F2.RDB$FIELD_SOURCE)\n" +
                "AND (D2.RDB$DEPENDED_ON_NAME = '" + dependedObject.getName() + "')\n" +
                "UNION ALL\n" +
                "SELECT DISTINCT A.RDB$RELATION_NAME\n" +
                "FROM RDB$REF_CONSTRAINTS B," +
                "RDB$RELATION_CONSTRAINTS A," +
                "RDB$RELATION_CONSTRAINTS C,\n" +
                "RDB$INDEX_SEGMENTS D," +
                "RDB$INDEX_SEGMENTS E\n" +
                "WHERE (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY')\n" +
                "AND (A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME)\n" +
                "AND (B.RDB$CONST_NAME_UQ = C.RDB$CONSTRAINT_NAME)\n" +
                "AND (C.RDB$INDEX_NAME = D.RDB$INDEX_NAME)\n" +
                "AND (A.RDB$INDEX_NAME = E.RDB$INDEX_NAME)\n" +
                "AND (C.RDB$RELATION_NAME = '" + dependedObject.getName() + "')\n";

        String comparingCondition = "AND (";
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) comparingCondition += " OR ";
            comparingCondition += "(D1.RDB$DEPENDED_ON_TYPE = " + list.get(i) + ")\n";
        }
        comparingCondition += ")";

        query = "SELECT DISTINCT D1.RDB$DEPENDENT_NAME\n" +
                "FROM RDB$DEPENDENCIES D1\n" +
                "LEFT JOIN RDB$RELATIONS R1 ON ((D1.RDB$DEPENDENT_NAME = R1.RDB$RELATION_NAME) AND (NOT (R1.RDB$VIEW_BLR IS NULL)))\n" +
                "WHERE (D1.RDB$DEPENDENT_TYPE = " + typeObject + ")\n" +
                "AND (D1.RDB$DEPENDENT_TYPE <> 3)\n" +
                "AND (D1.RDB$DEPENDED_ON_NAME = '" + dependedObject.getName() + "')\n" +
                comparingCondition + "\n";

        if (list.contains(9)) {

            tableQuery = "UNION ALL\n" +
                    "SELECT DISTINCT F.RDB$RELATION_NAME\n" +
                    "FROM RDB$RELATION_FIELDS F, RDB$RELATIONS R\n" +
                    "WHERE (R.RDB$VIEW_BLR IS NULL)\n" +
                    "AND (F.RDB$RELATION_NAME = R.RDB$RELATION_NAME)\n" +
                    "AND (F.RDB$FIELD_SOURCE = '" + dependedObject.getName() + "')";

            if (typeObject == 1)
                query += "UNION ALL\n" +
                        "SELECT DISTINCT F1.RDB$RELATION_NAME\n" +
                        "FROM RDB$RELATION_FIELDS F1, RDB$RELATIONS R1\n" +
                        "WHERE (NOT (R1.RDB$VIEW_BLR IS NULL))\n" +
                        "AND (F1.RDB$RELATION_NAME = R1.RDB$RELATION_NAME)\n" +
                        "AND (F1.RDB$FIELD_SOURCE = '" + dependedObject.getName() + "')\n" +
                        "UNION ALL\n" +
                        "SELECT RF.RDB$RELATION_NAME\n" +
                        "FROM RDB$DEPENDENCIES D1\n" +
                        "LEFT JOIN RDB$RELATION_FIELDS RF ON (RF.RDB$FIELD_SOURCE = D1.RDB$DEPENDENT_NAME)\n" +
                        "WHERE (D1.RDB$DEPENDED_ON_NAME =  '" + dependedObject.getName() + "')\n" +
                        "AND (D1.RDB$DEPENDENT_TYPE = 3)\n" +
                        "AND (RF.RDB$VIEW_CONTEXT IS NOT NULL)\n";

            if (typeObject == 5)
                query += "UNION ALL\n" +
                        "SELECT P.RDB$PROCEDURE_NAME\n" +
                        "FROM RDB$PROCEDURE_PARAMETERS P\n" +
                        "WHERE (P.RDB$FIELD_SOURCE = '" + dependedObject.getName() + "')\n";
        }

        if (typeObject == 0)
            query += tableQuery;

        return query;
    }
}
