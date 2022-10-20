package org.underworldlabs.swing;

public class ListSelectionPanelEvent extends java.util.EventObject {
    public final static int SELECT = 0;
    public final static int DESELECT = SELECT + 1;
    public final static int MOVE = DESELECT + 1;
    public final static int ADD = MOVE + 1;
    public final static int REMOVE = ADD + 1;
    public final static int CLEAR = REMOVE + 1;

    private final int type;

    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public ListSelectionPanelEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
