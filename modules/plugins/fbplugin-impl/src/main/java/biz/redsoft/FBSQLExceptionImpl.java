package biz.redsoft;

import org.firebirdsql.jdbc.FBSQLException;

import java.sql.SQLException;

public class FBSQLExceptionImpl implements IFBSQLException {
    private FBSQLException e;

    public FBSQLExceptionImpl(SQLException e) {
        if (e instanceof FBSQLException)
            this.e = (FBSQLException) e;
        else this.e = null;
    }

    @Override
    public int getVendorCode() {
        return e.getErrorCode();
    }

    @Override
    public boolean isFBSQLException() {
        return e != null;

    }
}
