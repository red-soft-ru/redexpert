package org.executequery.event;

import org.underworldlabs.swing.table.TableSorter;

import javax.swing.table.TableModel;

public interface SortingEvent extends ApplicationEvent {
    String PRESORTING = "presorting";
    String POSTSORTING = "postsorting";

    TableSorter getSorter();

    TableModel getOriginModel();
}
