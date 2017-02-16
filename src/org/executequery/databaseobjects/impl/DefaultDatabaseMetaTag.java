/*
 * DefaultDatabaseMetaTag.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.executequery.databaseobjects.*;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;

/**
 * Default meta tag object implementation.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultDatabaseMetaTag extends AbstractNamedObject 
                                    implements DatabaseMetaTag {
    
    /** the catalog object for this meta tag */
    private DatabaseCatalog catalog;

    /** the schema object for this meta tag */
    private DatabaseSchema schema;
    
    /** the host object for this meta tag */
    private DatabaseHost host;

    /** the meta data key name of this object */
    private String metaDataKey;
    
    /** the child objects of this meta type */
    private List<NamedObject> children;
    
    /** Creates a new instance of DefaultDatabaseMetaTag */
    public DefaultDatabaseMetaTag(DatabaseHost host,
                                  DatabaseCatalog catalog, 
                                  DatabaseSchema schema,
                                  String metaDataKey) {
        this.host = host;
        setCatalog(catalog);
        setSchema(schema);
        this.metaDataKey = metaDataKey;
    }

    /**
     * Returns the db object with the specified name or null if 
     * it does not exist.
     *
     * @param name  the name of the object
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

            } else if (isException()) {

                children = loadExceptions();

            } else if (isUDF()) {

                children = loadUDFs();

            } else if (isSystemDomain()) {

                children = loadSystemDomains();

            } else if (isSystemIndex()) {

                children = loadSystemIndices();

            } else if (isSystemTrigger()) {

                children = loadSystemTriggers();

            } else {

                children = getHost().getTables(getCatalogName(), 
                                               getSchemaName(), 
                                               getMetaDataKey());

                if (children != null && type == TABLE) {

                    // reset as editable tables for a default
                    // connection and meta type TABLE
                    
                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {
    
                        _children.add(new DefaultDatabaseTable((DatabaseObject)i));
                    }
                    
                    children = _children;
                
                } else if (type == VIEW) {
                    
                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {
    
                        _children.add(new DefaultDatabaseView((DatabaseObject)i));
                    }
                    
                    children = _children;
                    
                } else if (type == GLOBAL_TEMPORARY) {
                    List<NamedObject> _children = new ArrayList<NamedObject>(children.size());
                    for (NamedObject i : children) {

                        _children.add(new DefaultTemporaryDatabaseTable((DatabaseObject)i));
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

                ((DatabaseObject)i).setParent(this);
            }

        }

    }

    private List<NamedObject> loadFunctionsOrProcedures(int type)
        throws DataSourceException {

        try {

            if (StringUtils.equalsIgnoreCase(getName(), procedureTerm())) {

                // check what the term is - proc or function
                if (type == FUNCTION) {

                    return getFunctions();

                } else if (type == PROCEDURE) {

                    return getProcedures();
                }

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

    public boolean hasChildObjects() throws DataSourceException {
        
        if (!isMarkedForReload() && children != null) {
            
            return !children.isEmpty();
        }

        try {
        
            int type = getSubType();
            if (type != SYSTEM_FUNCTION) {

                if (isFunctionOrProcedure()) {

                    if (StringUtils.equalsIgnoreCase(getName(), procedureTerm())) {

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

                }  else if (isTrigger()) {

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
            
                    DefaultDatabaseFunction function = new DefaultDatabaseFunction(this, rs.getString(3));
                    function.setRemarks(rs.getString(4));
                    list.add(function);
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
            while (rs.next()) {

                DefaultDatabaseProcedure procedure = new DefaultDatabaseProcedure(this, rs.getString(3));
                procedure.setRemarks(rs.getString(7));
                list.add(procedure);
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

                DefaultDatabaseIndex index = new DefaultDatabaseIndex(rs.getString(1).trim());
                index.setTableName(rs.getString(2));
                index.setIndexType(rs.getInt(4));
                index.setActive(rs.getInt(6) != 1);
                index.setUnique(rs.getInt(5) == 1);
                index.setRemarks(rs.getString(7));
                index.setHost(this.getHost());
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

    /**
     * Loads the database triggers.
     */
    private List<NamedObject> getTriggers() throws DataSourceException {

        ResultSet rs = null;
        try {

            rs = getTriggersResultSet();
            List<NamedObject> list = new ArrayList<NamedObject>();
            while (rs.next()) {

                DefaultDatabaseTrigger trigger = new DefaultDatabaseTrigger(this, rs.getString(1));
                trigger.setTableName(rs.getString(3));
                trigger.setTriggerSequence(rs.getInt(4));
                trigger.setTriggerActive(rs.getInt(6) != 1);
                trigger.setTriggerType(rs.getInt(5));
                trigger.setTriggerDescription(rs.getString(7));
                trigger.setTriggerSourceCode(rs.getString(2));
                trigger.setRemarks(rs.getString(7));
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
                exception.setRemarks(rs.getString(4));
                exception.setExceptionID(rs.getString(3));
                exception.setExceptionText(rs.getString(2));
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

                DefaultDatabaseUDF udf = new DefaultDatabaseUDF(this, rs.getString(1), this.getHost());
                udf.setRemarks(rs.getString(2));
                udf.setModuleName(rs.getString(3));
                udf.setEntryPoint(rs.getString(4));
                udf.setReturnArg(rs.getInt(5));
                list.add(udf);
            }

            return list;

        } catch (SQLException e) {

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

                DefaultDatabaseIndex index = new DefaultDatabaseIndex(rs.getString(1).trim());
                index.setTableName(rs.getString(2));
                index.setIndexType(rs.getInt(4));
                index.setActive(rs.getInt(6) != 1);
                index.setUnique(rs.getInt(5) == 1);
                index.setRemarks(rs.getString(7));
                index.setHost(this.getHost());
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
                trigger.setTableName(rs.getString(3));
                trigger.setTriggerSequence(rs.getInt(4));
                trigger.setTriggerActive(rs.getInt(6) != 1);
                trigger.setTriggerType(rs.getInt(5));
                trigger.setTriggerDescription(rs.getString(7));
                trigger.setTriggerSourceCode(rs.getString(2));
                trigger.setRemarks(rs.getString(7));
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

    private ResultSet getProceduresResultSet() throws SQLException {
        
        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();
        
        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        return dmd.getProcedures(catalogName, schemaName, null);
    }

    private ResultSet getIndicesResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$INDEX_NAME, " +
                "RDB$RELATION_NAME, " +
                "RDB$SYSTEM_FLAG," +
                "RDB$INDEX_TYPE," +
                "RDB$UNIQUE_FLAG," +
                "RDB$INDEX_INACTIVE," +
                "RDB$DESCRIPTION\n" +
                "FROM RDB$INDICES " +
                "where RDB$SYSTEM_FLAG = 0 \n" +
                "ORDER BY RDB$INDEX_NAME");

        return resultSet;
    }

    private ResultSet getTriggersResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select t.rdb$trigger_name,\n" +
                "t.rdb$trigger_source,\n" +
                "t.rdb$relation_name,\n" +
                "t.rdb$trigger_sequence,\n" +
                "t.rdb$trigger_type,\n" +
                "t.rdb$trigger_inactive,\n" +
                "t.rdb$description\n" +
                "\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$system_flag = 0" +
                "order by t.rdb$trigger_name");

        return resultSet;
    }

    private ResultSet getSequencesResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("execute block\n" +
                "returns (\n" +
                "    out_name char(31),\n" +
                "    out_value bigint,\n" +
                "    out_desc blob sub_type 1)\n" +
                "as\n" +
                "begin\n" +
                "    for select rdb$generator_name, rdb$description from rdb$generators where rdb$system_flag is distinct from 1\n" +
                "     order by  rdb$generator_name\n" +
                "     into out_name, out_desc do\n" +
                "    begin\n" +
                "        execute statement 'select gen_id(' || out_name || ', 0) from rdb$database' into out_value;\n" +
                "        suspend;\n" +
                "    end\n" +
                "end");

        return resultSet;
    }

    private ResultSet getDomainsResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$FIELD_NAME, " +
                "RDB$SYSTEM_FLAG, " +
                "RDB$DESCRIPTION\n" +
                "from RDB$FIELDS\n" +
                "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                "and RDB$FIELD_NAME not like 'MON$%'\n" +
                "order by RDB$FIELD_NAME");

        return resultSet;
    }

    private ResultSet getExceptionResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select RDB$EXCEPTION_NAME, " +
                "RDB$MESSAGE, " +
                "RDB$EXCEPTION_NUMBER, " +
                "RDB$DESCRIPTION\n" +
                "from RDB$EXCEPTIONS\n" +
                "order by RDB$EXCEPTION_NAME");

        return resultSet;
    }

    private ResultSet getUDFResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select RDB$FUNCTION_NAME,\n" +
                "RDB$DESCRIPTION,\n" +
                "RDB$MODULE_NAME,\n" +
                "RDB$ENTRYPOINT,\n" +
                "RDB$RETURN_ARGUMENT\n" +
                "from RDB$FUNCTIONS\n" +
                "order by RDB$FUNCTION_NAME");

        return resultSet;
    }

    private ResultSet getSystemDomainResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$FIELD_NAME, " +
                "RDB$SYSTEM_FLAG, " +
                "RDB$DESCRIPTION\n" +
                "from RDB$FIELDS\n" +
                "where RDB$FIELD_NAME like 'RDB$%'\n" +
                "or RDB$FIELD_NAME like 'MON$%'\n" +
                "order by RDB$FIELD_NAME");

        return resultSet;
    }

    private ResultSet getSystemIndexResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select " +
                "RDB$INDEX_NAME, " +
                "RDB$RELATION_NAME, " +
                "RDB$SYSTEM_FLAG," +
                "RDB$INDEX_TYPE," +
                "RDB$UNIQUE_FLAG," +
                "RDB$INDEX_INACTIVE," +
                "RDB$DESCRIPTION\n" +
                "FROM RDB$INDICES \n" +
                "where RDB$SYSTEM_FLAG = 1 \n" +
                "ORDER BY RDB$INDEX_NAME");

        return resultSet;
    }

    private ResultSet getSystemTriggerResultSet() throws SQLException {

        String catalogName = catalogNameForQuery();
        String schemaName = schemaNameForQuery();

        DatabaseMetaData dmd = getHost().getDatabaseMetaData();
        Statement statement = dmd.getConnection().createStatement();

        ResultSet resultSet = statement.executeQuery("select t.rdb$trigger_name,\n" +
                "t.rdb$trigger_source,\n" +
                "t.rdb$relation_name,\n" +
                "t.rdb$trigger_sequence,\n" +
                "t.rdb$trigger_type,\n" +
                "t.rdb$trigger_inactive,\n" +
                "t.rdb$description\n" +
                "\n" +
                "from rdb$triggers t\n" +
                "where t.rdb$system_flag <> 0" +
                "order by t.rdb$trigger_name");

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
        return getMetaDataKey();
    }

    /**
     * Override to do nothing - name is the meta data key value.
     */
    public void setName(String name) {}

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

    public void setCatalog(DatabaseCatalog catalog) {
        this.catalog = catalog;
    }

    public void setSchema(DatabaseSchema schema) {
        this.schema = schema;
    }
    
}

