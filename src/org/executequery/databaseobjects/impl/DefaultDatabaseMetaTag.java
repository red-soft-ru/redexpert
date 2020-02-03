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
import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.*;
import org.executequery.datasource.PooledConnection;
import org.executequery.datasource.PooledResultSet;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
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
     * the catalog object for this meta tag
     */
    private DatabaseCatalog catalog;

    /**
     * the schema object for this meta tag
     */
    private DatabaseSchema schema;

    /**
     * the host object for this meta tag
     */
    private DatabaseHost host;

    /**
     * the meta data key name of this object
     */
    private String metaDataKey;

    /**
     * the child objects of this meta type
     */
    private List<NamedObject> children;

    /**
     * Creates a new instance of DefaultDatabaseMetaTag
     */

    public DefaultDatabaseMetaTag(DatabaseHost host,
                                  DatabaseCatalog catalog,
                                  DatabaseSchema schema,
                                  String metaDataKey, int typeTree) {
        this.typeTree = typeTree;
        this.host = host;
        setCatalog(catalog);
        setSchema(schema);
        this.metaDataKey = metaDataKey;
    }

    public DefaultDatabaseMetaTag(DatabaseHost host,
                                  DatabaseCatalog catalog,
                                  DatabaseSchema schema,
                                  String metaDataKey) {
        this(host, catalog, schema, metaDataKey, TreePanel.DEFAULT);
    }

    public DefaultDatabaseMetaTag(DatabaseHost host,
                                  DatabaseCatalog catalog,
                                  DatabaseSchema schema,
                                  String metaDataKey,
                                  int typeTree,
                                  DatabaseObject dependedObject) {
        this(host, catalog, schema, metaDataKey, typeTree);
        this.dependedObject = dependedObject;
    }

    /**
     * Returns the db object with the specified name or null if
     * it does not exist.
     *
     * @param name the name of the object
     * @return the NamedObject or null if not found
     */
    public NamedObject getNamedObject(String name) throws DataSourceException {

        List<NamedObject> objects = getObjects();
        if (objects != null) {

            name = name.toUpperCase();

            for (NamedObject object : objects) {

                if (name.equals(object.getName().toUpperCase())) {

                    return object;
                }

            }

        }

        return null;
    }

    /**
     * Retrieves child objects classified as this tag type.
     * These may be database tables, functions, procedures, sequences, views, etc.
     *
     * @return this meta tag's child database objects.
     */
    public List<NamedObject> getObjects() throws DataSourceException {

        if (!isMarkedForReload() && children != null) {

            return children;
        }

        int type = getSubType();
        if (type == SYSTEM_DATABASE_TRIGGER
                || type == SYSTEM_DOMAIN
                || type == SYSTEM_FUNCTION
                || type == SYSTEM_INDEX
                || type == SYSTEM_TABLE
                || type == SYSTEM_VIEW
                || type == SYSTEM_TRIGGER
                || type == GLOBAL_TEMPORARY
        )
            if (typeTree != TreePanel.DEFAULT) {
                return new ArrayList<NamedObject>();
            }
        if (type != SYSTEM_FUNCTION) {


            if (isFunctionOrProcedure()) {

                children = loadFunctionsOrProcedures(type);

            } else if (isIndex()) {

                children = loadIndices();

            } else if (isTrigger()) {

                children = loadTriggers();

            } else if (isSequence()) {
                children = loadSequences();

            } else if (isDomain()) {

                children = loadDomains();

            } else if (isRole()) {
                if (typeTree != TreePanel.DEFAULT)
                    return new ArrayList<>();
                children = loadRoles();
            } else if (isException()) {

                children = loadExceptions();

            } else if (isUDF()) {
                if (typeTree != TreePanel.DEFAULT)
                    return new ArrayList<>();
                children = loadUDFs();

            } else if (isSystemDomain()) {

                children = loadSystemDomains();

            } else if (isSystemIndex()) {

                children = loadSystemIndices();

            } else if (isSystemTrigger()) {

                children = loadSystemTriggers();

            } else if (isSystemDatabaseTrigger()) {

                children = loadSystemDatabaseTriggers();

            } else if (isPackage()) {

                children = loadPackages();

            } else {

                String className = getHost().getDatabaseConnection().getJDBCDriver().getClassName();
                if (className.contains("FBDriver")) {
                    // Red Database
                    children = loadTables(getMetaDataKey());

                } else {
                    // Another database
                    children = getHost().getTables(getCatalogName(),
                            getSchemaName(),
                            getMetaDataKey());
                }

                if (children != null && type == TABLE) {

                    // reset as editable tables for a default
                    // connection and meta type TABLE

                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {
                        DefaultDatabaseTable table = new DefaultDatabaseTable((DatabaseObject) i);
                        _children.add(table);
                    }

                    children = _children;

                } else if (type == VIEW) {

                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {

                        _children.add(new DefaultDatabaseView((DatabaseObject) i));
                    }

                    children = _children;

                } else if (type == GLOBAL_TEMPORARY) {
                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {

                        _children.add(new DefaultTemporaryDatabaseTable((DatabaseObject) i));
                    }

                    children = _children;

                }

            }

        } else {

            // system functions break down further

            children = getSystemFunctionTypes();
        }

        // loop through and add this object as the parent object
        addAsParentToObjects(children);

        return children;
    }

    private void addAsParentToObjects(List<NamedObject> children) {

        if (children != null) {

            for (NamedObject i : children) {

                i.setParent(this);
            }

        }

    }

    private List<NamedObject> loadFunctionsOrProcedures(int type)
            throws DataSourceException {

        try {

            if (StringUtils.equalsIgnoreCase(getMetaDataKey(), procedureTerm())) {

                // check what the term is - proc or function
                if (type == FUNCTION) {

                    return getFunctions();

                } else if (type == PROCEDURE) {

                    return getProcedures();
                }

            } else if (type == FUNCTION) { // Red Database 3.0
                return getFunctions();
            }

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }

        return new ArrayList<NamedObject>(0);
    }

    private List<NamedObject> loadIndices()
            throws DataSourceException {

        return getIndices();

    }

    private List<NamedObject> loadTriggers()
            throws DataSourceException {

        return getTriggers();

    }

    private List<NamedObject> loadSequences()
            throws DataSourceException {

        return getSequences();

    }

    private List<NamedObject> loadDomains()
            throws DataSourceException {

        return getDomains();

    }

    private List<NamedObject> loadRoles()
            throws DataSourceException {

        return getRoles();

    }

    private List<NamedObject> loadExceptions()
            throws DataSourceException {

        return getExceptions();

    }

    private List<NamedObject> loadUDFs()
            throws DataSourceException {

        return getUDFs();

    }

    private List<NamedObject> loadSystemDomains()
            throws DataSourceException {

        return getSystemDomains();

    }

    private List<NamedObject> loadSystemIndices()
            throws DataSourceException {

        return getSystemIndices();

    }

    private List<NamedObject> loadSystemTriggers()
            throws DataSourceException {

        return getSystemTriggers();

    }

    private List<NamedObject> loadSystemDatabaseTriggers()
            throws DataSourceException {

        return getSystemDatabaseTriggers();

    }

    private List<NamedObject> loadPackages()
            throws DataSourceException {

        return getPackages();

    }

    private List<NamedObject> loadTables(String metaDataKey)
            throws DataSourceException {

        return getTables(metaDataKey);

    }

    private List<NamedObject> getTables(String metaDataKey) {
        ResultSet rs = null;
        try {

            List<NamedObject> tables = new ArrayList<NamedObject>();
            String tableName;

            rs = getTablesResultSet(metaDataKey);


            while (rs.next()) {

                tableName = rs.getString(1);
                DefaultDatabaseObject object = new DefaultDatabaseObject(this.getHost(), metaDataKey);
                object.setName(tableName);
                if (typeTree == DEFAULT) {
                    object.setCatalogName("");
                    object.setSchemaName("");
                    object.setRemarks(rs.getString(2));
                    object.setSource(rs.getString(3));
                } else if (typeTree == TreePanel.DEPENDED_ON) {
                    object.setTypeTree(typeTree);
                    object.setDependObject(dependedObject);

                }
                if (metaDataKey.contains("SYSTEM"))
                    object.setSystemFlag(true);
                tables.add(object);

            }
            return tables;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    public boolean hasChildObjects() throws DataSourceException {

        if (!isMarkedForReload() && children != null) {

            return !children.isEmpty();
        }

        try {

            int type = getSubType();
            if (type == SYSTEM_DATABASE_TRIGGER
                    || type == SYSTEM_DOMAIN
                    || type == SYSTEM_FUNCTION
                    || type == SYSTEM_INDEX
                    || type == SYSTEM_TABLE
                    || type == SYSTEM_VIEW
                    || type == SYSTEM_TRIGGER
            )
                if (typeTree != TreePanel.DEFAULT) {
                    return false;
                }
            if (type != SYSTEM_FUNCTION) {

                if (isFunctionOrProcedure()) {

                    if (StringUtils.equalsIgnoreCase(getMetaDataKey(), procedureTerm())) {

                        if (type == FUNCTION) {

                            return hasFunctions();

                        } else if (type == PROCEDURE) {

                            return hasProcedures();
                        }

                    }

                    return false;

                } else if (isIndex()) {

                    if (type == INDEX) {
                        return hasIndices();
                    }

                } else if (isTrigger()) {

                    if (type == TRIGGER) {
                        return hasTriggers();
                    }

                } else if (isSequence()) {

                    if (type == SEQUENCE) {
                        return hasSequences();
                    }

                } else if (isDomain()) {

                    if (type == DOMAIN) {
                        return hasDomains();
                    }

                } else if (isRole()) {

                    if (type == ROLE) {
                        return hasRoles();
                    }

                } else if (isException()) {

                    if (type == EXCEPTION) {
                        return hasException();
                    }

                } else if (isUDF()) {

                    if (type == UDF) {
                        return hasUDF();
                    }

                } else if (isSystemDomain()) {

                    if (type == SYSTEM_DOMAIN) {
                        return hasSystemDomain();
                    }

                } else if (isSystemIndex()) {

                    if (type == SYSTEM_INDEX) {
                        return hasSystemIndex();
                    }

                } else if (isSystemTrigger()) {

                    if (type == SYSTEM_TRIGGER) {
                        return hasSystemTrigger();
                    }

                } else if (isSystemDatabaseTrigger()) {

                    if (type == SYSTEM_DATABASE_TRIGGER) {
                        return hasSystemDatabaseTrigger();
                    }

                } else if (isPackage()) {

                    if (type == PACKAGE) {
                        return hasPackages();
                    }

                } else {

                    return getHost().hasTablesForType(getCatalogName(), getSchemaName(), getMetaDataKey());
                }

            }

        } catch (SQLException e) {

            logThrowable(e);
            return false;
        }

        return true;
    }

    private boolean isFunctionOrProcedure() {

        int type = getSubType();
        return type == FUNCTION || type == PROCEDURE;
    }

    private boolean isIndex() {

        int type = getSubType();
        return type == INDEX;
    }

    private boolean isTrigger() {

        int type = getSubType();
        return type == TRIGGER;
    }

    private boolean isSequence() {

        int type = getSubType();
        return type == SEQUENCE;
    }

    private boolean isDomain() {

        int type = getSubType();
        return type == DOMAIN;
    }

    private boolean isRole() {

        int type = getSubType();
        return type == ROLE;
    }

    private boolean isException() {

        int type = getSubType();
        return type == EXCEPTION;
    }

    private boolean isUDF() {

        int type = getSubType();
        return type == UDF;
    }

    private boolean isSystemDomain() {

        int type = getSubType();
        return type == SYSTEM_DOMAIN;
    }

    private boolean isSystemIndex() {

        int type = getSubType();
        return type == SYSTEM_INDEX;
    }

    private boolean isSystemTrigger() {

        int type = getSubType();
        return type == SYSTEM_TRIGGER;
    }

    private boolean isSystemDatabaseTrigger() {

        int type = getSubType();
        return type == SYSTEM_DATABASE_TRIGGER;
    }

    private boolean isPackage() {

        int type = getSubType();
        return type == PACKAGE;
    }

    private String procedureTerm() throws SQLException {
        return getHost().getDatabaseMetaData().getProcedureTerm();
    }

    private boolean hasFunctions() {

        ResultSet rs = null;
        try {

            rs = getFunctionsResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasProcedures() {

        ResultSet rs = null;
        try {

            rs = getProceduresResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasIndices() {

        ResultSet rs = null;
        try {

            rs = getIndicesResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasTriggers() {

        ResultSet rs = null;
        try {

            rs = getTriggersResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasSequences() {

        ResultSet rs = null;
        try {

            rs = getSequencesResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasDomains() {

        ResultSet rs = null;
        try {

            rs = getDomainsResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasRoles() {

        ResultSet rs = null;
        try {

            rs = getRolesResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasException() {

        ResultSet rs = null;
        try {

            rs = getExceptionResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasUDF() {

        ResultSet rs = null;
        try {

            rs = getUDFResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } catch (Exception e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasSystemDomain() {

        ResultSet rs = null;
        try {

            rs = getSystemDomainResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasSystemIndex() {

        ResultSet rs = null;
        try {

            rs = getSystemIndexResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasSystemTrigger() {

        ResultSet rs = null;
        try {

            rs = getSystemTriggerResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasSystemDatabaseTrigger() {

        ResultSet rs = null;
        try {

            rs = getSystemDatabaseTriggerResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    private boolean hasPackages() {

        ResultSet rs = null;
        try {

            rs = getPackagesResultSet();
            return rs != null && rs.next();

        } catch (SQLException e) {

            logThrowable(e);
            return false;

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database functions.
     */
    private List<NamedObject> getFunctions() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getFunctionsResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            if (rs != null) { // informix returns null rs

                while (rs.next()) {
                    if (typeTree == TreePanel.DEFAULT) {
                        DefaultDatabaseFunction function = new DefaultDatabaseFunction(this, rs.getString(3));
                        function.setRemarks(rs.getString(4));
                        list.add(function);
                    } else {
                        DefaultDatabaseFunction function = new DefaultDatabaseFunction(this, rs.getString(1));
                        list.add(function);
                    }
                }

            }
            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database procedures.
     */
    private List<NamedObject> getProcedures() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getProceduresResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            if (((PooledResultSet) rs).getResultSet().unwrap(ResultSet.class).getClass().getName().contains("FBResultSet")) {
                while (rs.next()) {

                    DefaultDatabaseProcedure procedure = new DefaultDatabaseProcedure(this, rs.getString(1));
                    procedure.setHost(getHost());
                    //procedure.setRemarks(rs.getString(2));
                    list.add(procedure);
                }
            } else {
                while (rs.next()) {

                    DefaultDatabaseProcedure procedure = new DefaultDatabaseProcedure(this, rs.getString(3));
                    procedure.setHost(getHost());
                    procedure.setRemarks(rs.getString(7));
                    list.add(procedure);
                }
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database indices.
     */
    private List<NamedObject> getIndices() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getIndicesResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseIndex index = new DefaultDatabaseIndex(this, rs.getString(1).trim());
                index.setHost(this.getHost());
                index.setActive(rs.getInt(2) != 1);
                list.add(index);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    public DefaultDatabaseIndex getIndexFromName(String name) throws DataSourceException {

        ResultSet rs = null;
        DefaultDatabaseIndex index=null;
        try {

            rs = getIndexFromNameResultSet(name);
            while (rs.next()) {

                index = new DefaultDatabaseIndex(this, rs.getString(1).trim());
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

            releaseResources(rs);
        }
    }

    /**
     * Loads the database triggers.
     */
    private List<NamedObject> getTriggers() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getTriggersResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseTrigger trigger = new DefaultDatabaseTrigger(this,
                        rs.getString(1).trim());
                if (typeTree == TreePanel.DEFAULT)
                    trigger.setTriggerActive(rs.getInt(2) != 1);
                list.add(trigger);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database triggers.
     */
    private List<NamedObject> getSequences() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getSequencesResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseSequence sequence = new DefaultDatabaseSequence(this, rs.getString(1));
                //sequence.setRemarks(rs.getString(3));
                list.add(sequence);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database triggers.
     */
    private List<NamedObject> getDomains() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getDomainsResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseDomain domain = new DefaultDatabaseDomain(this, rs.getString(1));
                list.add(domain);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getRoles() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getRolesResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseRole role = new DefaultDatabaseRole(this, rs.getObject(1).toString());
                list.add(role);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database triggers.
     */
    private List<NamedObject> getExceptions() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getExceptionResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseException exception = new DefaultDatabaseException(this, rs.getString(1));
                exception.setRemarks(rs.getString(2));
                list.add(exception);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    /**
     * Loads the database UDFs.
     */
    private List<NamedObject> getUDFs() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getUDFResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseUDF udf = new DefaultDatabaseUDF(this,
                        rs.getString(1).trim(),
                        this.getHost());
                udf.setRemarks(rs.getString(2));
                String moduleName = rs.getString(3);
                if (!MiscUtils.isNull(moduleName))
                    udf.setModuleName(moduleName.trim());
                String entryPoint = rs.getString(4);
                if (!MiscUtils.isNull(entryPoint))
                    udf.setEntryPoint(entryPoint.trim());
                udf.setReturnArg(rs.getInt(5));
                udf.setDescription(rs.getString("description"));
                list.add(udf);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } catch (Exception e) {
            logThrowable(e);
            return new ArrayList<NamedObject>(0);
        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getSystemDomains() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getSystemDomainResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseDomain domain = new DefaultDatabaseDomain(this, rs.getString(1));
                domain.setSystemFlag(true);
                list.add(domain);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getSystemIndices() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getSystemIndexResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseIndex index = new DefaultDatabaseIndex(this, rs.getString(1).trim());
                index.setHost(this.getHost());
                index.setSystemFlag(true);
                list.add(index);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getSystemTriggers() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getSystemTriggerResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseTrigger trigger = new DefaultDatabaseTrigger(this, rs.getString(1));
                trigger.setSystemFlag(true);
                list.add(trigger);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getSystemDatabaseTriggers() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getSystemDatabaseTriggerResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseTrigger trigger = new DefaultDatabaseTrigger(this,
                        rs.getString(1).trim());
                trigger.setTriggerActive(rs.getInt(2) != 1);
                list.add(trigger);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private List<NamedObject> getPackages() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getPackagesResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabasePackage databasePackage = new DefaultDatabasePackage(this, rs.getString(1).trim());

                list.add(databasePackage);
            }

            return list;

        } catch (SQLException e) {

            logThrowable(e);
            return new ArrayList<NamedObject>(0);

        } finally {

            releaseResources(rs);
        }
    }

    private ResultSet getProceduresResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Connection realConnection = ((PooledConnection) dmd.getConnection()).getRealConnection();
        if (realConnection.unwrap(Connection.class).getClass().getName().contains("FBConnection")) { // Red Database or FB
            Connection fbConn = realConnection.unwrap(Connection.class);
            IFBDatabaseConnection db = null;
            try {
                db = (IFBDatabaseConnection) DynamicLibraryLoader.loadingObjectFromClassLoader(fbConn, "FBDatabaseConnectionImpl");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            db.setConnection(fbConn);
            String condition = "";
            if (db.getMajorVersion() > 2)
                condition = "where RDB$PACKAGE_NAME is null\n";
            String sql = "select rdb$procedure_name as procedure_name\n" +
                    "from rdb$procedures \n" +
                    condition +
                    "order by procedure_name";
            Statement statement = dmd.getConnection().createStatement();
            if (typeTree == TreePanel.DEPENDED_ON)
                sql = getDependOnQuery(5);
            else if (typeTree == TreePanel.DEPENDENT)
                sql = getDependentQuery(5);
            ResultSet rs = statement.executeQuery(sql);
            return rs;
        } else { // Another database
            return dmd.getProcedures(catalogName, schemaName, null);
        }
    }

    private ResultSet getIndicesResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        String query = "select " +
                "I.RDB$INDEX_NAME,\n" +
                "I.RDB$INDEX_INACTIVE\n" +
                "FROM RDB$INDICES AS I LEFT JOIN rdb$relation_constraints as c on i.rdb$index_name=c.rdb$index_name\n" +
                "where I.RDB$SYSTEM_FLAG = 0 \n" +
                "ORDER BY I.RDB$INDEX_NAME";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(10);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(10);
        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private ResultSet getIndexFromNameResultSet(String name) throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "I.RDB$INDEX_NAME, " +
                "I.RDB$RELATION_NAME, " +
                "I.RDB$SYSTEM_FLAG," +
                "I.RDB$INDEX_TYPE," +
                "I.RDB$UNIQUE_FLAG," +
                "I.RDB$INDEX_INACTIVE," +
                "I.RDB$DESCRIPTION," +
                "C.RDB$CONSTRAINT_TYPE\n" +
                "FROM RDB$INDICES AS I LEFT JOIN rdb$relation_constraints as c on i.rdb$index_name=c.rdb$index_name\n" +
                "where I.RDB$SYSTEM_FLAG = 0 \n" +
                "AND I.RDB$INDEX_NAME='"+name+"'");

        return resultSet;
    }

    private ResultSet getTriggersResultSet() throws SQLException {


        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        String query = "select t.rdb$trigger_name,\n" +
                "t.rdb$trigger_inactive\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$system_flag = 0\n" +
                "and t.rdb$trigger_type <= 114 \n" +
                "order by t.rdb$trigger_name";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(2);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(2);
        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }


    private ResultSet getSequencesResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        String query = "select rdb$generator_name from rdb$generators where rdb$system_flag is distinct from 1\n" +
                "     order by  rdb$generator_name";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(14);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(14);

        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private ResultSet getDomainsResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();


        String query = "select " +
                "RDB$FIELD_NAME " +
                "from RDB$FIELDS\n" +
                "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                "and RDB$FIELD_NAME not like 'MON$%'\n" +
                "order by RDB$FIELD_NAME";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(9);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(9);
        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private ResultSet getRolesResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT RDB$ROLE_NAME FROM RDB$ROLES");

        return resultSet;
    }

    private ResultSet getExceptionResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        String query = "select RDB$EXCEPTION_NAME, " +
                "RDB$DESCRIPTION\n" +
                "from RDB$EXCEPTIONS\n" +
                "order by RDB$EXCEPTION_NAME";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(7);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(7);
        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private ResultSet getUDFResultSet() throws Exception {

        ResultSet resultSet = null;

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        PooledConnection connection = (PooledConnection) dmd.getConnection();
        Connection fbConn = connection.unwrap(Connection.class);
        IFBDatabaseConnection db = null;
        try {
            db = (IFBDatabaseConnection) DynamicLibraryLoader.loadingObjectFromClassLoader(fbConn, "FBDatabaseConnectionImpl");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        db.setConnection(fbConn);
        switch (db.getMajorVersion()) {
            case 2:
                resultSet = statement.executeQuery("select RDB$FUNCTION_NAME,\n" +
                        "RDB$DESCRIPTION,\n" +
                        "RDB$MODULE_NAME,\n" +
                        "RDB$ENTRYPOINT,\n" +
                        "RDB$RETURN_ARGUMENT,\n" +
                        "RDB$DESCRIPTION as description\n" +
                        "from RDB$FUNCTIONS\n" +
                        "order by RDB$FUNCTION_NAME");
                break;
            case 3:
            case 4:
                resultSet = statement.executeQuery("select RDB$FUNCTION_NAME,\n" +
                        "RDB$DESCRIPTION,\n" +
                        "RDB$MODULE_NAME,\n" +
                        "RDB$ENTRYPOINT,\n" +
                        "RDB$RETURN_ARGUMENT,\n" +
                        "RDB$DESCRIPTION as description\n" +
                        "from RDB$FUNCTIONS\n" +
                        "where RDB$LEGACY_FLAG = 1 and (RDB$MODULE_NAME is not NULL)\n" +
                        "order by RDB$FUNCTION_NAME");
                break;
        }

        return resultSet;
    }

    private ResultSet getSystemDomainResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$FIELD_NAME " +
                "from RDB$FIELDS\n" +
                "where RDB$FIELD_NAME like 'RDB$%'\n" +
                "or RDB$FIELD_NAME like 'MON$%'\n" +
                "order by RDB$FIELD_NAME");

        return resultSet;
    }

    private ResultSet getSystemIndexResultSet() throws SQLException {


        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$INDEX_NAME\n " +
                "FROM RDB$INDICES \n" +
                "where RDB$SYSTEM_FLAG = 1 \n" +
                "ORDER BY RDB$INDEX_NAME");

        return resultSet;
    }

    private ResultSet getSystemTriggerResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select t.rdb$trigger_name\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$system_flag <> 0" +
                "order by t.rdb$trigger_name");

        return resultSet;
    }

    private ResultSet getSystemDatabaseTriggerResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select t.rdb$trigger_name,\n" +
                "t.rdb$trigger_inactive\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$system_flag = 0" +
                "and t.rdb$trigger_type > 114 \n" +
                "order by t.rdb$trigger_name");

        return resultSet;
    }

    private ResultSet getPackagesResultSet() throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();
        String query = "select p.rdb$package_name \n" +
                "from rdb$packages p\n" +
                "order by p.rdb$package_name";
        if (typeTree == TreePanel.DEPENDED_ON)
            query = getDependOnQuery(19);
        else if (typeTree == TreePanel.DEPENDENT)
            query = getDependentQuery(19);

        ResultSet resultSet = statement.executeQuery(query);

        return resultSet;
    }

    private ResultSet getTablesResultSet(String metaDataKey) throws SQLException {

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = null;
        if (metaDataKey.equals("TABLE")) {
            String query = "select rdb$relation_name, \n" +
                    "rdb$description,\n" +
                    "rdb$view_source\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is null \n" +
                    "and (rdb$system_flag is null or rdb$system_flag = 0) and rdb$relation_type=0 or rdb$relation_type=2\n" +
                    "order by rdb$relation_name";
            if (typeTree == TreePanel.DEPENDED_ON)
                query = getDependOnQuery(0);
            else if (typeTree == TreePanel.DEPENDENT)
                query = getDependentQuery(0);
            resultSet = statement.executeQuery(query);
        } else if (metaDataKey.equals("SYSTEM TABLE")) {
            resultSet = statement.executeQuery("select rdb$relation_name, \n" +
                    "rdb$description,\n" +
                    "rdb$view_source\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is null \n" +
                    "and (rdb$system_flag is not null and rdb$system_flag = 1) \n" +
                    "order by rdb$relation_name");
        } else if (metaDataKey.equals("VIEW")) {
            String query = "select rdb$relation_name, \n" +
                    "rdb$description,\n" +
                    "rdb$view_source\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is not null \n" +
                    "and (rdb$system_flag is null or rdb$system_flag = 0) \n" +
                    "order by rdb$relation_name";
            if (typeTree == TreePanel.DEPENDED_ON)
                query = getDependOnQuery(1);
            else if (typeTree == TreePanel.DEPENDENT)
                query = getDependentQuery(1);
            resultSet = statement.executeQuery(query);
        } else if (metaDataKey.equals("SYSTEM VIEW")) {
            resultSet = statement.executeQuery("select rdb$relation_name, \n" +
                    "rdb$description,\n" +
                    "rdb$view_source\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is not null \n" +
                    "and (rdb$system_flag is not null and rdb$system_flag = 1) \n" +
                    "order by rdb$relation_name");
        } else if (metaDataKey.equals("GLOBAL TEMPORARY")) {
            resultSet = statement.executeQuery("select r.rdb$relation_name, \n" +
                    "r.rdb$description,\n" +
                    "rdb$view_source\n" +
                    "from rdb$relations r\n" +
                    "join rdb$types t on r.rdb$relation_type = t.rdb$type \n" +
                    "where\n" +
                    "(t.rdb$field_name = 'RDB$RELATION_TYPE') \n" +
                    "and (t.rdb$type = 4 or t.rdb$type = 5) \n" +
                    "order by r.rdb$relation_name");
        }

        return resultSet;
    }

    private String schemaNameForQuery() {

        return getHost().getSchemaNameForQueries(getSchemaName());
    }

    private String catalogNameForQuery() {

        return getHost().getCatalogNameForQueries(getCatalogName());
    }

    private ResultSet getFunctionsResultSet() throws SQLException {

        try {
            String catalogName = catalogNameForQuery();
            String schemaName = schemaNameForQuery();

            DatabaseMetaData dmd = getHost().getDatabaseMetaData();
            String query = "select 0,\n" +
                    "0,\n" +
                    "rdb$function_name as function_name,\n" +
                    "rdb$description as remarks\n" +
                    "from rdb$functions\n" +
                    "where (RDB$MODULE_NAME is NULL) and (RDB$PACKAGE_NAME is NULL)\n" +
                    "order by function_name ";

            Connection realConnection = ((PooledConnection) dmd.getConnection()).getRealConnection();
            if (realConnection.unwrap(Connection.class).getClass().getName().contains("FBConnection")) {
                Statement statement = dmd.getConnection().createStatement();
                if (typeTree == TreePanel.DEPENDED_ON)
                    query = getDependOnQuery(15);
                else if (typeTree == TreePanel.DEPENDENT)
                    query = getDependentQuery(15);
                ResultSet rs = statement.executeQuery(query);
                return rs;
            } else {
                return dmd.getFunctions(catalogName, schemaName, null);
            }

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

    /**
     * Loads the system function types.
     */
    private List<NamedObject> getSystemFunctionTypes() {

        List<NamedObject> objects = new ArrayList<NamedObject>(3);

        objects.add(new DefaultSystemFunctionMetaTag(
                this, SYSTEM_STRING_FUNCTIONS, "String Functions"));

        objects.add(new DefaultSystemFunctionMetaTag(
                this, SYSTEM_NUMERIC_FUNCTIONS, "Numeric Functions"));

        objects.add(new DefaultSystemFunctionMetaTag(
                this, SYSTEM_DATE_TIME_FUNCTIONS, "Date/Time Functions"));

        return objects;
    }

    /**
     * Returns the sub-type indicator of this meta tag - the type this
     * meta tag ultimately represents.
     *
     * @return the sub-type, or -1 if not found/available
     */
    public int getSubType() {

        String key = getMetaDataKey();
        for (int i = 0; i < META_TYPES.length; i++) {

            if (META_TYPES[i].equals(key)) {

                return i;
            }

        }

        return -1;
    }

    /**
     * Returns the parent host object.
     *
     * @return the parent object
     */
    public DatabaseHost getHost() {
        return host;
    }

    /**
     * Returns the name of this object.
     *
     * @return the object name
     */
    public String getName() {
        return Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[getSubType()]);
    }

    /**
     * Override to do nothing - name is the meta data key value.
     */
    public void setName(String name) {
    }

    /**
     * Returns the catalog name or null if there is
     * no catalog attached.
     */
    private String getCatalogName() {

        DatabaseCatalog _catalog = getCatalog();
        if (_catalog != null) {

            return _catalog.getName();
        }

        return null;
    }

    /**
     * Returns the parent catalog object.
     *
     * @return the parent catalog object
     */
    public DatabaseCatalog getCatalog() {
        return catalog;
    }

    /**
     * Returns the schema name or null if there is
     * no schema attached.
     */
    private String getSchemaName() {

        DatabaseSchema _schema = getSchema();
        if (_schema != null) {

            return _schema.getName();
        }

        return null;
    }

    /**
     * Returns the parent schema object.
     *
     * @return the parent schema object
     */
    public DatabaseSchema getSchema() {
        return schema;
    }

    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object - catalog or schema
     */
    public NamedObject getParent() {
        return getSchema() == null ? getCatalog() : getSchema();
    }

    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return META_TAG;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return metaDataKey;
    }

    /**
     * Does nothing.
     */
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
        String query = null;
        int version = ((AbstractDatabaseObject) dependedObject).getDatabaseMajorVersion();
        List<Integer> list = getTypeDependFromDatabaseObject(dependedObject);
        String domainsQuery = "select distinct rdb$field_source, cast(null as varchar(64)), cast(9 as integer)\n" +
                "from rdb$relation_fields\n" +
                "where (rdb$relation_name = '" + dependedObject.getName() + "') and (rdb$field_source not starting with 'RDB$')\n" +
                "union all\n";
        String tableQuery = "select distinct\n" +
                "C.RDB$RELATION_NAME as FK_Table,\n" +
                "null, cast(0 as integer)\n" +
                "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
                "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E, RDB$INDICES I\n" +
                "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
                "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
                "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME) and\n" +
                "(A.RDB$INDEX_NAME=I.RDB$INDEX_NAME)\n" +
                "and (A.RDB$RELATION_NAME = '" + dependedObject.getName() + "')\n" +
                "union all\n";
        String condition = "";
        if (version > 2)
            condition = "and (T2.RDB$PACKAGE_NAME IS NULL)\n";
        String packageQuery = "select distinct T2.RDB$PACKAGE_NAME, cast(T2.RDB$FIELD_NAME as varchar(64)), CAST(19 AS INTEGER)\n" +
                "from RDB$DEPENDENCIES T2 where (T2.RDB$DEPENDENT_NAME = 'COUNTRY')\n" +
                "and (T2.RDB$DEPENDENT_TYPE = 0)\n" +
                condition +
                "union all\n";
        condition = "";
        if (version > 2)
            condition = "and (T1.RDB$PACKAGE_NAME IS NULL)\n";
        String comparing = "";
        for (int i = 0; i < list.size(); i++) {
            String union = "or";
            if (i == 0)
                union = "and (";
            comparing += union + " (t1.RDB$DEPENDENT_TYPE = " + list.get(i) + ")\n";
        }
        comparing += ")";
        query = "select distinct t1.RDB$DEPENDED_ON_NAME, null, CAST(T1.RDB$DEPENDED_ON_TYPE AS INTEGER)\n" +
                "from RDB$DEPENDENCIES t1 where (t1.RDB$DEPENDENT_NAME = '" + dependedObject.getName() + "')\n" +
                comparing +
                condition +
                "and (T1.RDB$DEPENDED_ON_TYPE=" + typeObject + ")\n" +
                "union all\n" +
                "select distinct d.rdb$depended_on_name, null, CAST(D.RDB$DEPENDED_ON_TYPE AS INTEGER)\n" +
                "from rdb$dependencies d, rdb$relation_fields f\n" +
                "where (d.rdb$dependent_type = 3) and\n" +
                "(d.rdb$dependent_name = f.rdb$field_source)\n" +
                "and (f.rdb$relation_name = '" + dependedObject.getName() + "')\n" +
                "and (D.RDB$DEPENDED_ON_TYPE='" + typeObject + "')\n" +
                "order by 1,2";
        if (typeObject == 9)
            query = domainsQuery + query;
        if (typeObject == 18 || typeObject == 19)
            query = packageQuery + query;
        if (typeObject == 0)
            query = tableQuery + query;
        return query;
    }

    private String getDependentQuery(int typeObject) {
        String query = null;
        int version = ((AbstractDatabaseObject) dependedObject).getDatabaseMajorVersion();
        List<Integer> list = getTypeDependFromDatabaseObject(dependedObject);
        String tableQuery = "union all\n" +
                "select distinct f2.rdb$relation_name\n" +
                "from rdb$dependencies d2, rdb$relation_fields f2\n" +
                "left join rdb$relations r2 on ((f2.rdb$relation_name = r2.rdb$relation_name) and (not (r2.Rdb$View_Blr is null)))\n" +
                "where (d2.rdb$dependent_type = 3) and\n" +
                "(d2.rdb$dependent_name = f2.rdb$field_source)\n" +
                "and (d2.rdb$depended_on_name = '" + dependedObject.getName() + "')\n" +
                "union all\n" +
                "select distinct A.RDB$RELATION_NAME\n" +
                "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
                "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E\n" +
                "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and\n" +
                "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
                "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME)\n" +
                "and (C.RDB$RELATION_NAME = '" + dependedObject.getName() + "')\n";
        String comparing = "";
        for (int i = 0; i < list.size(); i++) {
            String union = "or";
            if (i == 0)
                union = "and (";
            comparing += union + " (d1.RDB$DEPENDED_ON_TYPE = " + list.get(i) + ")\n";
        }
        comparing += ")";
        query = "select distinct D1.RDB$DEPENDENT_NAME\n" +
                "from RDB$DEPENDENCIES D1\n" +
                "left join rdb$relations r1 on ((D1.RDB$DEPENDENT_NAME = r1.rdb$relation_name) and (not (r1.Rdb$View_Blr is null)))\n" +
                "where (D1.RDB$DEPENDENT_TYPE = " + typeObject + ")\n" +
                "and (D1.RDB$DEPENDENT_TYPE <> 3)\n" +
                "and (D1.RDB$DEPENDED_ON_NAME = '" + dependedObject.getName() + "')\n" +
                comparing;
        if (typeObject == 0) {
            query = query + tableQuery;
        }
        return query;
    }
}


