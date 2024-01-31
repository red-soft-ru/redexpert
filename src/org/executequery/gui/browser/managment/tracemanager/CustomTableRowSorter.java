package org.executequery.gui.browser.managment.tracemanager;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import java.util.List;

public class CustomTableRowSorter extends TableRowSorter<TraceDataModel> {
    public CustomTableRowSorter(final TraceDataModel datamodel) {
        super(datamodel);
    }

    @Override
    public void toggleSortOrder(final int column) {
        if (LogConstants.TSTAMP_COLUMN.equals(getModel().getColumnName(column))) {
            setMaxSortKeys(2);
            final List<SortKey> keys = new ArrayList<>(getSortKeys());
            if (keys.size() <= 1) {
                keys.clear();
                keys.add(new SortKey(column, SortOrder.DESCENDING));
                keys.add(new SortKey(0, SortOrder.DESCENDING));
            } else {
                final SortOrder previousOrder = keys.get(0).getSortOrder();
                final SortOrder newOrder = previousOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING
                        : SortOrder.ASCENDING;
                keys.set(0, new SortKey(column, newOrder));
                keys.set(1, new SortKey(0, newOrder));
            }
            setSortKeys(keys);
        } else {
            setMaxSortKeys(1);
            super.toggleSortOrder(column);
        }

    }

}
