package biz.redsoft;

import java.sql.SQLException;

public interface IFBBatchCompletionState {

    int[] getAllStates() throws SQLException;

    String printAllStates() throws SQLException;

    int getState(int i) throws SQLException;

    String getError(int i) throws SQLException;

}
