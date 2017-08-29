package biz.redsoft;

import org.firebirdsql.jdbc.FBDatabaseMetaData;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public class DatabaseMetadata implements IDatabaseMetadata {
    @Override
    public String getProcedureSourceCode(DatabaseMetaData databaseMetaData, String s) throws SQLException {
        String procedureSourceCode = "";
        FBDatabaseMetaData fbMetaData = (FBDatabaseMetaData)databaseMetaData;
        procedureSourceCode = fbMetaData.getProcedureSourceCode(s);

        return procedureSourceCode;
    }
}
