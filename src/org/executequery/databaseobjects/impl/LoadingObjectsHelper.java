package org.executequery.databaseobjects.impl;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.datasource.PooledStatement;

public class LoadingObjectsHelper {

    public static final int FULL_LOAD_CONSTANT = 25;
    DefaultStatementExecutor querySender = null;
    PooledStatement statement = null;

    boolean isFirst = true;

    DefaultStatementExecutor querySenderForCols = null;
    PooledStatement statementForCols = null;

    boolean isFirstForCols = true;
    boolean fullLoadObject = false;
    DatabaseHost databaseHost;
    private int listSize;

    public LoadingObjectsHelper(int listSize) {
        this.listSize = listSize;
    }

    public void preparingLoadForObject(AbstractDatabaseObject abstractDatabaseObject) {
        if (isFirst) {
            fullLoadObject = abstractDatabaseObject.getMetaTagParent().getObjects().size() / listSize < FULL_LOAD_CONSTANT;
            databaseHost = abstractDatabaseObject.getHost();
            databaseHost.setPauseLoadingTreeForSearch(true);
        }
        abstractDatabaseObject.setFullLoad(fullLoadObject);
        if (!isFirst) {
            abstractDatabaseObject.setStatementForLoadInfo(statement);
            abstractDatabaseObject.setQuerySender(querySender);
        }
        abstractDatabaseObject.setSomeExecute(true);
    }

    public void preparingLoadForObjectCols(AbstractDatabaseObject abstractDatabaseObject) {
        if (!isFirstForCols) {
            abstractDatabaseObject.setStatementForLoadInfoForColumns(statementForCols);
            abstractDatabaseObject.setQuerySenderForColumns(querySenderForCols);
        }
        abstractDatabaseObject.setSomeExecuteForColumns(true);
    }

    public void preparingLoadForObjectAndCols(AbstractDatabaseObject abstractDatabaseObject) {
        preparingLoadForObject(abstractDatabaseObject);
        preparingLoadForObjectCols(abstractDatabaseObject);
    }

    public void postProcessingLoadForObject(AbstractDatabaseObject abstractDatabaseObject) {
        abstractDatabaseObject.getHost().setPauseLoadingTreeForSearch(false);
        querySender = abstractDatabaseObject.getQuerySender();
        statement = abstractDatabaseObject.getStatementForLoadInfo();
        isFirst = false;
    }

    public void postProcessingLoadForObjectForCols(AbstractDatabaseObject abstractDatabaseObject) {
        querySenderForCols = abstractDatabaseObject.getQuerySenderForColumns();
        statementForCols = abstractDatabaseObject.getStatementForLoadInfoForColumns();
        isFirstForCols = false;
    }

    public void postProcessingLoadForObjectAndCols(AbstractDatabaseObject abstractDatabaseObject) {
        postProcessingLoadForObject(abstractDatabaseObject);
        postProcessingLoadForObjectForCols(abstractDatabaseObject);
    }

    public void releaseResources() {
        if (databaseHost != null)
            databaseHost.setPauseLoadingTreeForSearch(false);
        databaseHost = null;
        if (querySender != null)
            querySender.releaseResources();
        querySender = null;
        if (querySenderForCols != null)
            querySenderForCols.releaseResources();
        querySenderForCols = null;
        statement = null;
    }
}
