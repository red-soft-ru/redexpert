package biz.redsoft;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by Vasiliy on 4/6/2017.
 */
public interface IDatabaseMetadata {

    public String getProcedureSourceCode(DatabaseMetaData netaData, String procedureName) throws SQLException;

}
