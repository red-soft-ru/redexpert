package biz.redsoft;

import java.sql.SQLException;

/**
 * Interface for batch operations.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public interface IFBBatch {

    void setObject(int index, Object o) throws SQLException;

    IFBBlob addBlob(int index, byte[] inBuffer) throws SQLException;

    void addBatch() throws SQLException;

    IFBBatchCompletionState execute() throws SQLException;

    void startTransaction() throws SQLException;

    void commit() throws SQLException;

    void cancel() throws SQLException;
}
