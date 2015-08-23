package org.executequery.databaseobjects;

import java.util.List;
import org.executequery.gui.resultset.RecordDataItem;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class TableDataChange {

    private final List<RecordDataItem> rowDataForRow;

    public TableDataChange(List<RecordDataItem> rowDataForRow) {
        this.rowDataForRow = rowDataForRow;
    }

    public List<RecordDataItem> getRowDataForRow() {
        return rowDataForRow;
    }
}
