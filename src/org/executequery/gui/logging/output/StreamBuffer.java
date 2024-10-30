package org.executequery.gui.logging.output;

/**
 * A mutable sequence of characters whose size does not exceed a given threshold value.<p>
 * The buffer truncates the beginning if the total size exceeds
 * the specified size when a new sequence of characters is added.
 *
 * @author Aleksey Kozlov
 */
public final class StreamBuffer {

    private final StringBuilder buffer;
    private final boolean trimByLine;
    private final int bufferSize;

    public StreamBuffer(int bufferSize, boolean trimByLine) {
        this.buffer = new StringBuilder();
        this.bufferSize = bufferSize;
        this.trimByLine = trimByLine;
    }

    public void append(String str) {
        buffer.append(str);
        truncate();
    }

    public void append(char c) {
        buffer.append(c);
        truncate();
    }

    public boolean isEmpty() {
        return buffer.toString().isEmpty();
    }

    // --- helpers ---

    private void truncate() {
        if (buffer.length() > bufferSize) {
            buffer.delete(0, getExcess());
            buffer.insert(0, "...");
        }
    }

    private int getExcess() {

        int excess = buffer.length() - bufferSize;
        if (trimByLine)
            excess = buffer.indexOf("\n", excess);

        return excess;
    }

    // --- Object impl ---

    @Override
    public String toString() {
        return buffer.toString();
    }

}
