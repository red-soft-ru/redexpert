package org.executequery.event;

public interface SortingListener extends ApplicationEventListener {

    void presorting(SortingEvent e);

    void postsorting(SortingEvent e);

}
