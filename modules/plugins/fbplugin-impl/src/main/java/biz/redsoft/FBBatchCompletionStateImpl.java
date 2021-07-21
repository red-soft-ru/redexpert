package biz.redsoft;

import org.firebirdsql.gds.ng.FbBatchCompletionState;

import java.sql.SQLException;

public class FBBatchCompletionStateImpl implements IFBBatchCompletionState{

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
}
