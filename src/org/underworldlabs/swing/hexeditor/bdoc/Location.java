package org.underworldlabs.swing.hexeditor.bdoc;

public abstract class Location implements Comparable {

    // PRIVATE MEMBERS
    private BinaryDocument bDoc;

    // CONSTRUCTOR
    public Location(BinaryDocument bDoc) {
        this.bDoc = bDoc;
    }

    // GETTERS
    public BinaryDocument getDocument() {
        return bDoc;
    }

    public abstract long getOffset();

    public abstract Location addOffset(long offset);
}
