package org.executequery.event;

import org.underworldlabs.swing.table.TableSorter;

import javax.swing.table.TableModel;

public class DefaultSortingEvent extends AbstractApplicationEvent implements SortingEvent {

    private TableSorter sorter;

    public DefaultSortingEvent(TableSorter sorter, String method) {
        super(sorter, method);
        this.sorter = sorter;
    }

    @Override
    public TableSorter getSorter() {
        return sorter;
    }

    @Override
    public TableModel getOriginModel() {
        return sorter.getTableModel();
    }
}
