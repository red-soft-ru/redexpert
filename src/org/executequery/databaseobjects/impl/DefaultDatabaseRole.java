package org.executequery.databaseobjects.impl;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseProcedure;
import org.executequery.databaseobjects.NamedObject;
import org.firebirdsql.jdbc.FBDatabaseMetaData;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasiliy on 02.02.17.
 */
public class DefaultDatabaseRole extends DefaultDatabaseExecutable
         {
public String name;
public DefaultDatabaseRole(DatabaseMetaTag metaTagParent, String name)
{
    super (metaTagParent,name);
}

             @Override
             public int getType() {
                 return NamedObject.ROLE;
             }

             @Override
             public String getMetaDataKey() {
                 return META_TYPES[ROLE];
             }
         }