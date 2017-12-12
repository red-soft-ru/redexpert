package org.executequery.databaseobjects.impl;

import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.sql.SQLFormatter;
import org.executequery.sql.StatementGenerator;
import org.underworldlabs.jdbc.DataSourceException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by vasiliy on 25.01.17.
 */
public class DefaultTemporaryDatabaseTable extends DefaultDatabaseObject {

    private DefaultDatabaseTable defaultDatabaseTable;

    public DefaultTemporaryDatabaseTable(DatabaseObject object) {

        this(object.getHost());

        setCatalogName(object.getCatalogName());
        setSchemaName(object.getSchemaName());
        setName(object.getName());
        setRemarks(object.getRemarks());

        defaultDatabaseTable = new DefaultDatabaseTable(object);
    }

    public DefaultTemporaryDatabaseTable(DatabaseHost host) {

        super(host, "GLOBAL TEMPORARY");
    }

    public String getCreateSQLText() throws DataSourceException {

        StatementGenerator statementGenerator = createStatementGenerator();
        String databaseProductName = databaseProductName();
        Statement statement = null;
        int type = -1;
        try {
            statement = defaultDatabaseTable.getHost().getConnection().createStatement();

            ResultSet resultSet = statement.executeQuery("Select RDB$RELATION_TYPE FROM RDB$RELATIONS R \n" +
                    "WHERE R.RDB$RELATION_NAME = '" + defaultDatabaseTable.getName() + "'");

            resultSet.next();
            type = resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseResources(statement);
        }

        String createStatement =
                statementGenerator.createTableWithConstraints(databaseProductName, defaultDatabaseTable);

        createStatement = formatSqlText(createStatement);

        createStatement = createStatement.replace("CREATE TABLE", "CREATE GLOBAL TEMPORARY TABLE");

        createStatement = createStatement.substring(0, createStatement.length() - 2);

        if (type == 4)
            createStatement += " ON COMMIT PRESERVE ROWS;\n\n";
        else if (type == 5)
            createStatement += " ON COMMIT DELETE ROWS;\n\n";

        StringBuilder sb = new StringBuilder();
        sb.append(createStatement);
        sb.append("\n\n");
        sb.append(statementGenerator.tableConstraintsAsAlter(databaseProductName, defaultDatabaseTable));

        return sb.toString();
    }

    private String formatSqlText(String text) {

        return new SQLFormatter(text).format();
    }

    @Override
    public boolean hasSQLDefinition() {

        return true;
    }
}