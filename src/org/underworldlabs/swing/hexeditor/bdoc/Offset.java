package org.underworldlabs.swing.hexeditor.bdoc;

public class Offset extends Location {
    private long offset;

    public Offset(BinaryDocument bDoc, long offset) {
        super(bDoc);
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    public Location addOffset(long offset) {
        return new Offset(getDocument(), this.offset + offset);
    }

    public int compareTo(Object o) {
        return (int) (offset - ((Location) o).getOffset());
    }

    public String toString() {
        return super.toString() + "[offset=" + offset + "]";
    }
}
