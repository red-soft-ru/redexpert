package org.executequery.event;

/// @author Aleksey Kozlov
public class DatabaseTableEvent extends AbstractApplicationEvent
        implements ApplicationEvent {

    public static final String PROCESS_TABLE_RESET = "processTableReset";

    public DatabaseTableEvent(Object source, String method) {
        super(source, method);
    }

    @Override
    public boolean isDatabaseTableEvent() {
        return true;
    }

}
