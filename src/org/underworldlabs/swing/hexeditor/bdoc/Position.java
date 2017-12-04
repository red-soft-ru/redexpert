package org.underworldlabs.swing.hexeditor.bdoc;


public class Position extends Location {

    private PositionAnchor anchor;

    Position(BinaryDocument bDoc, PositionAnchor anchor) {
        super(bDoc);
        this.anchor = anchor;
        anchor.referenceAdded();
    }

    protected void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    public long getOffset() {
        return anchor.getOffset();
    }

    public Location addOffset(long offset) {
        return anchor.addOffset(offset);
    }

    public int compareTo(Object o) {
        return anchor.compareTo(o);
    }

    public void dispose() {
        if (anchor != null) {
            anchor.referenceLost();
            anchor = null;
        }
    }
}
