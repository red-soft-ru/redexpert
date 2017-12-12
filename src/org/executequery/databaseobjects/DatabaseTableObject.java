package org.executequery.databaseobjects;

import org.executequery.gui.resultset.RecordDataItem;

import java.util.List;

public interface DatabaseTableObject extends DatabaseObject {

    String prepareStatement(List<String> columns, List<RecordDataItem> changes);

    String prepareStatementAdding(List<String> columns, List<RecordDataItem> changes);

    String prepareStatementDeleting(List<RecordDataItem> changes);

    void addTableDataChange(TableDataChange tableDataChange);

    void removeTableDataChange(List<RecordDataItem> row);

    boolean hasTableDataChanges();

    void clearDataChanges();

    int applyTableDataChanges();


}

