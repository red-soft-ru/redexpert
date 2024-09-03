package biz.redsoft;

import org.firebirdsql.gds.ng.FbBatchCompletionState;

import java.sql.SQLException;

public class FBBatchCompletionStateImpl implements IFBBatchCompletionState {

    private final FbBatchCompletionState state;

    public FBBatchCompletionStateImpl(FbBatchCompletionState state) {
        this.state = state;
    }

    @Override
    public int[] getAllStates() throws SQLException {
        return state.getAllStates();
    }

    @Override
    public String printAllStates() throws SQLException {
        return state.printAllStates();
    }

    @Override
    public int getState(int i) throws SQLException {
        return state.getState(i);
    }

    @Override
    public String getError(int i) throws SQLException {
        return state.getError(i);
    }

}
