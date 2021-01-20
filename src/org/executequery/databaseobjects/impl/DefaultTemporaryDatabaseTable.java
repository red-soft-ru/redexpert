package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ColumnData;
import org.executequery.sql.SQLFormatter;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by vasiliy on 25.01.17.
 */
public class DefaultTemporaryDatabaseTable extends DefaultDatabaseTable {


    public DefaultTemporaryDatabaseTable(DatabaseObject object) {

        super(object, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);

    }

    public DefaultTemporaryDatabaseTable(DatabaseHost host) {

        super(host, NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]);
    }

    public String getCreateSQLText() throws DataSourceException {


        DefaultStatementExecutor querySender = new DefaultStatementExecutor();
        querySender.setDatabaseConnection(getHost().getDatabaseConnection());
        int type = -1;
        try {
            SqlStatementResult result=querySender.getResultSet("Select RDB$RELATION_TYPE FROM RDB$RELATIONS R \n" +
                    "WHERE R.RDB$RELATION_NAME = '" + getName() + "'");
            if(result.isException())
                throw result.getSqlException();
            ResultSet resultSet=result.getResultSet();

            resultSet.next();
            type = resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
        String typeTemporary = "";
        if (type == 4)
            typeTemporary += " ON COMMIT PRESERVE ROWS;\n\n";
        else if (type == 5)
            typeTemporary += " ON COMMIT DELETE ROWS;\n\n";
        List<ColumnData> listCD=new ArrayList<>();
        for(int i=0;i<getColumnCount();i++)
        {
            listCD.add(new ColumnData(getHost().getDatabaseConnection(),getColumns().get(i)));
        }
        List<org.executequery.gui.browser.ColumnConstraint> listCC=new ArrayList<>();
        for(int i=0;i<getConstraints().size();i++)
        {
            listCC.add(new org.executequery.gui.browser.ColumnConstraint(false,getConstraints().get(i)));
        }
        return formatSqlText(SQLUtils.generateCreateTable(getName(), listCD, listCC, true, true, typeTemporary, getExternalFile(), getAdapter()));
    }

    private String formatSqlText(String text) {

        return new SQLFormatter(text).format();
    }

    public int getType() {
        return GLOBAL_TEMPORARY;
    }

    /**
     * Returns the meta data key name of this object.
     *
     * @return the meta data key name.
     */
    public String getMetaDataKey() {
        return NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY];
    }

    @Override
    public boolean hasSQLDefinition() {

        return true;
    }
}