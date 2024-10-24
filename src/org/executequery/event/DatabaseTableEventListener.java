package org.executequery.event;

/// @author Aleksey Kozlov
public interface DatabaseTableEventListener extends ApplicationEventListener {

    /// Invokes when database object was reset
    @SuppressWarnings("unused")
    void processTableReset(DatabaseTableEvent e);
}
