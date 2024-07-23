package org.executequery.event;

public interface RecentOpenFileEventListener
        extends ApplicationEventListener {

    @SuppressWarnings("unused")
    void recentFilesUpdated(RecentOpenFileEvent e);
}
