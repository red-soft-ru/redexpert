/*
 * DefaultDatabaseExecutable.java
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

import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author takisd
 */
public abstract class DefaultDatabaseExecutable extends AbstractDatabaseObject
        implements DatabaseExecutable {


    /**
     * proc parameters
     */
    protected ArrayList<ProcedureParameter> parameters;

    /**
     * the proc type
     */
    private short executableType;

    protected String sourceCode;

    protected List<ProcedureParameter> procedureInputParameters;
    protected List<ProcedureParameter> procedureOutputParameters;

    protected String entryPoint;
    protected String engine;
    protected String sqlSecurity;
    protected String authid;


    public DefaultDatabaseExecutable() {
        super((DatabaseMetaTag) null);
    }

    public DefaultDatabaseExecutable(DatabaseMetaTag metaTagParent, String name) {
        super(metaTagParent, name);
        if (metaTagParent.getCatalog() != null) {
            setCatalogName(metaTagParent.getCatalog().getName());
        }

        if (metaTagParent.getSchema() != null) {
            setSchemaName(metaTagParent.getSchema().getName());
        }


    }

    /**
     * Indicates whether this executable object has any parameters.
     *
     * @return true | false
     */
    public boolean hasParameters() {
        List<ProcedureParameter> _parameters = getParameters();
        return _parameters != null && !_parameters.isEmpty();
    }

    /**
     * Adds the specified values as a single parameter to this object.
     */
    public ProcedureParameter addParameter(String name, int type, int dataType,
                                           String sqlType, int size, int nullable) {
        if (parameters == null) {

            parameters = new ArrayList<ProcedureParameter>();
        }

        ProcedureParameter parameter = new ProcedureParameter(name, type, dataType, sqlType, size, nullable);
        parameters.add(parameter);

        return parameter;
    }

    /**
     * Returns this object's parameters as an array.
     */
    public ProcedureParameter[] getParametersArray() throws DataSourceException {
        if (parameters == null) {
            getParameters();
        }
        return parameters.toArray(new
                ProcedureParameter[parameters.size()]);
    }

    public boolean supportCatalogOrSchemaInFunctionOrProcedureCalls() throws DataSourceException {

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls() || dmd.supportsSchemasInProcedureCalls();

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean supportCatalogInFunctionOrProcedureCalls() throws DataSourceException {

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsCatalogsInProcedureCalls();

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    public boolean supportSchemaInFunctionOrProcedureCalls() throws DataSourceException {

        try {

            DatabaseMetaData dmd = getMetaTagParent().getHost().getDatabaseMetaData();
            return dmd.supportsSchemasInProcedureCalls();

        } catch (SQLException e) {

            throw new DataSourceException(e);
        }
    }

    /**
     * Returns this object's parameters.
     */
    public List<ProcedureParameter> getParameters() throws DataSourceException {

        checkOnReload(parameters);
        return parameters;

    }



    /**
     * Returns the procedure parameters.
     *
     * @return the result set
     */


    /**
     * Returns the database object type.
     *
     * @return the object type
     */
    public int getType() {
        return PROCEDURE;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return META_TYPES[PROCEDURE];
    }


    /**
     * Returns the parent named object of this object.
     *
     * @return the parent object - the meta tag
     */
    public NamedObject getParent() {
        return super.getParent();
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    /**
     * The executable (procedure) type:<br>
     * <ul>
     * <li> procedureResultUnknown - May return a result
     * <li> procedureNoResult - Does not return a result
     * <li> procedureReturnsResult - Returns a result
     * </ul>
     *
     * @return the proc type
     */
    public short getExecutableType() {
        return executableType;
    }

    public void setExecutableType(short executableType) {
        this.executableType = executableType;
    }

    public String getSourceCode() {
        checkOnReload(sourceCode);
        return sourceCode;

    }


    public List<ProcedureParameter> getProcedureInputParameters() {
        return procedureInputParameters;
    }

    public List<ProcedureParameter> getProcedureOutputParameters() {
        return procedureOutputParameters;
    }

    public String getEntryPoint() {
        checkOnReload(entryPoint);
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getEngine() {
        checkOnReload(engine);
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getSqlSecurity() {
        checkOnReload(sqlSecurity);
        return sqlSecurity;
    }

    public void setSqlSecurity(String sqlSecurity) {
        this.sqlSecurity = sqlSecurity;
    }

    public String getAuthid() {
        checkOnReload(authid);
        return authid;
    }

    public void setAuthid(String authid) {
        this.authid = authid;
    }
}






