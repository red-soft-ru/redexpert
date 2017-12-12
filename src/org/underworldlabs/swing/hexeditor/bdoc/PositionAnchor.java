package org.underworldlabs.swing.hexeditor.bdoc;

public class PositionAnchor extends Location {
    private int referenceCount;

    public PositionAnchor(BinaryDocument bDoc) {
        super(bDoc);
        referenceCount = 0;
    }

    protected void finalize() throws Throwable {
        try {
            //System.out.println("DESTROY(anchor)");
        } finally {
            super.finalize();
        }
    }

    public long getOffset() {
        return getDocument().getAnchorOffset(this);
    }

    public Location addOffset(long offset) {
        return getDocument().createPosition(getOffset() + offset);
    }

    public int compareTo(Object o) {
        return (int) (getOffset() - ((Location) o).getOffset());
    }

    public void referenceAdded() {
        referenceCount++;
        //System.out.println("REFERENCES("+referenceCount+")");
    }

    public void referenceLost() {
        referenceCount--;
        //System.out.println("REFERENCES("+referenceCount+")");
        if (referenceCount == 0)
            destroy();
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    protected void destroy() {
        getDocument().removeAnchor(this);
    }
}
