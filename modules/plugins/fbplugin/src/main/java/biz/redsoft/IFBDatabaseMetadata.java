package biz.redsoft;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public interface IFBDatabaseMetadata {

    public String getProcedureSourceCode(DatabaseMetaData metaData, String procedureName) throws SQLException;

}
