package org.underworldlabs.swing.hexeditor.bdoc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;


public class BinaryDocument extends Observable {

    // PRIVATE MEMBERS
    private File file;
    private boolean readOnly;
    private boolean modified;

    private byte[] data;
    private int occupied;

    // Used for O(1) access to positions.
    private HashMap anchor2Offset;

    // CONSTRUCTORS

    /**
     * Construct an empty binary document.
     * Documents created in this way are not readOnly, but are considered
     * new, (as defined by the isNew() method). New documents must be saved
     * using the saveAs() method.
     */
    public BinaryDocument() {
        file = null;
        readOnly = false;
        modified = false;

        data = new byte[256];
        occupied = 0;

        anchor2Offset = new HashMap();
    }

    /**
     * Construct an empty binary document with the specified capacity.
     * Documents created in this way are not readOnly, but are considered
     * new, (as defined by the isNew() method). New documents must be saved
     * using the saveAs() method.
     *
     * @param capacity The initial allocated capacity for the document.
     */
    public BinaryDocument(int capacity) {
        file = null;
        readOnly = false;
        modified = false;

        data = new byte[capacity];
        occupied = 0;

        anchor2Offset = new HashMap();
    }

    /**
     * Construct a binary document from a file.
     * The document is opened in read/write mode.
     *
     * @param file The file to open.
     * @throws IOException if an exception occurs while reading the file.
     */
    public BinaryDocument(File file) throws IOException {
        this(file, false);
    }

    /**
     * Construct a binary document from a file.
     * The document is opened read-only mode if readOnly is true.
     * Otherwise the document is opened in in read/write mode.
     *
     * @param file     The file to open.
     * @param readOnly True if the document should be opened in read-only mode.
     * @throws IOException if an exception occurs while reading the file.
     */
    public BinaryDocument(File file, boolean readOnly) throws IOException {
        this.file = file;
        this.readOnly = readOnly;
        this.modified = false;

        anchor2Offset = new HashMap();

        RandomAccessFile ioFile = new RandomAccessFile(file, "r");
        occupied = (int) ioFile.length();
        data = new byte[occupied + 256];
        ioFile.read(data);
        ioFile.close();
    }

    public BinaryDocument(byte[] data, boolean readOnly) {
        this.file = null;
        this.readOnly = readOnly;
        this.modified = false;

        anchor2Offset = new HashMap();

        occupied = data.length;
        this.data = data.clone();
    }

    public byte[] getData() {
        return Arrays.copyOfRange(data, 0, occupied);
    }

    public void setData(byte[] data) {


        anchor2Offset = new HashMap();

        occupied = data.length;
        this.data = data.clone();
    }

    // SAVE / CLOSE

    /**
     * Save the document back to the source file.
     * This method saves the document back to the file from which it was
     * created. This method can not be called if the document is new or
     * read-only.
     *
     * @throws IOException           if an exception occured while writing the file.
     * @throws DocumentSaveException if the document is read-only or if the document is new.
     */
    public void save() throws IOException {
        if (isReadOnly())
            throw new DocumentSaveException(this,
                    "Cannot call save() on a read-only document. Try saveAs(File).");

        if (isNew())
            throw new DocumentSaveException(this,
                    "Cannot call save() on a new document. Try saveAs(File).");

        RandomAccessFile ioFile = new RandomAccessFile(file, "rw");
        ioFile.write(data, 0, (int) length());
        ioFile.close();

        modified = false;
    }

    /**
     * Save the document back to a new file.
     * This method saves the document to a new file. This new file becomes the source
     * of the document, and subsequent calls to save() will save to this newly specified
     * file.
     *
     * @throws IOException if an exception occured while writing the file.
     */
    public void saveAs(File file) throws IOException {
        this.file = file;

        RandomAccessFile ioFile = new RandomAccessFile(file, "rw");
        ioFile.write(data, 0, (int) length());
        ioFile.close();

        modified = false;
    }

    /**
     * Close a document, releasing all resources.
     * Once a document is closed, it can not be re-opened and this instance
     * becomes invalid. Create a new BinaryDocument to re-open the file.
     *
     * @throws IOException if an exception occured while closing the source file.
     */
    public void close() throws IOException {
        data = null;
        occupied = 0;
        modified = false;
    }

    // GETTERS

    /**
     * Returns the length of document.
     */
    public long length() {
        return occupied;
    }

    /**
     * Returns true if the document is read-only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns true if the document is new, and has not yet been saved.
     */
    public boolean isNew() {
        return (file == null);
    }

    /**
     * Returns true if the document has been modified since it was last
     * opened, or last saved.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Returns the source file of the document, or null if the document is new.
     */
    public File getFile() {
        return file;
    }

    // POSITIONS, OFFSETS, and CURSORS -- Oh my!

    /**
     * Create a Position at the specified offset.
     * Positions track changes as the document is modified.
     * NOTE: Positions are bound to this document instance.
     *
     * @return a new postion that begins at the specified location.
     */
    public Position createPosition(long offset) {
        Long _offset = new Long(offset);
        PositionAnchor anchor = null;

        Set entries = anchor2Offset.entrySet();
        Iterator i = entries.iterator();

        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            if (entry.getValue().equals(_offset)) {
                anchor = (PositionAnchor) entry.getKey();
                break;
            }
        }

        if (anchor == null) {
            anchor = new PositionAnchor(this);
            anchor2Offset.put(anchor, _offset);
        }

        return new Position(this, anchor);
    }

    /**
     * Create an Offset instance representing the specified offset.
     * NOTE: Offset instances are bound to this document instance.
     *
     * @return a new Offset instance.
     */
    public Offset createOffset(long offset) {
        return new Offset(this, offset);
    }

    /**
     * Create a cursor that can be used to sequentially (or randomly)
     * access this document. Cursors are position based, and thus "float"
     * as the document is modified. (directly or from other cursors).
     *
     * @return a new cursor who's position begins at the specified location.
     */
    public Cursor createCursor(Location loc) {
        return new Cursor(createPosition(loc.getOffset()));
    }

    // READ OPERATIONS

    public int read(Location loc) {
        byte[] b = new byte[1];
        int ret = read(loc, b, 0, b.length);
        if (ret == -1)
            return -1;
        else
            return 0xFF & (int) b[0];
    }

    public int read(Location loc, byte[] b) {
        return read(loc, b, 0, b.length);
    }

    public int read(Location loc, byte[] b, int off, int len) {
        long offset = loc.getOffset();
        int bytesRemaining = (int) (length() - offset);

        if (len > bytesRemaining)
            len = bytesRemaining;

        if (len < 1)
            return -1;

        for (int i = 0; i < len; i++)
            b[off + i] = data[(int) (offset + i)];

        return len;
    }

    // WRITE OPERATIONS
    public void write(Location loc, int b) {
        byte[] bt = new byte[1];
        bt[0] = (byte) b;
        write(loc, bt, 0, bt.length);
    }

    public void write(Location loc, byte[] b) {
        write(loc, b, 0, b.length);
    }

    public void write(Location loc, byte[] b, int off, int len) {
        modified = true;

        long offset = loc.getOffset();
        int bytesRemaining = (int) (length() - offset);

        if (len > bytesRemaining) {
            if ((int) offset + len >= data.length)
                expandBuffer(((int) offset + len) - data.length);
            occupied += len - bytesRemaining;
        }

        byte[] oldContent = new byte[len];

        for (int i = 0; i < len; i++)
            oldContent[i] = data[(int) (offset + i)];

        for (int i = 0; i < len; i++)
            data[(int) offset + i] = b[off + i];

        setChanged();
        notifyObservers(new ContentChangedEvent(this, new ByteSpan(loc, loc.addOffset(len - 1)),
                ContentChangedEvent.WRITTEN, oldContent));
        clearChanged();
    }

    // INSERT OPERATIONS
    public void insert(Location loc, int b) {
        byte[] bt = new byte[1];
        bt[0] = (byte) b;
        insert(loc, bt, 0, bt.length);
    }

    public void insert(Location loc, byte[] b) {
        insert(loc, b, 0, b.length);
    }

    public void insert(Location loc, byte[] b, int off, int len) {
        modified = true;

        long offset = loc.getOffset();
        int spaceRemaining = (int) (data.length - length());

        if (spaceRemaining < len)
            expandBuffer(len - spaceRemaining);

        for (int i = (int) length() - 1; i >= (int) offset; i--)
            data[i + len] = data[i];
        occupied += len;

        for (int i = 0; i < len; i++)
            data[(int) offset + i] = b[off + i];

        Vector anchors = new Vector(anchor2Offset.keySet());
        HashMap anchor2Offset = new HashMap(2 * this.anchor2Offset.size() + 1);

        for (int i = 0; i < anchors.size(); i++) {
            PositionAnchor anchor = (PositionAnchor) anchors.get(i);
            Long _offset = new Long(anchor.getOffset());
            if (offset < _offset.longValue())
                _offset = new Long(_offset.longValue() + len);
            anchor2Offset.put(anchor, _offset);
        }

        this.anchor2Offset = anchor2Offset;

        setChanged();
        notifyObservers(new ContentChangedEvent(this, new ByteSpan(loc, loc.addOffset(len - 1)),
                ContentChangedEvent.INSERTED, null));
        clearChanged();
    }

    // DELETE
    public int delete(Location loc, int len) {
        modified = true;

        long offset = loc.getOffset();
        int bytesRemaining = (int) (length() - offset);

        if (len > bytesRemaining)
            len = bytesRemaining;

        byte[] oldContent = new byte[len];

        for (int i = 0; i < len; i++)
            oldContent[i] = data[(int) (offset + i)];

        occupied -= len;

        for (int i = (int) offset; i < (int) length(); i++)
            data[i] = data[i + len];

        Vector anchors = new Vector(anchor2Offset.keySet());
        HashMap anchor2Offset = new HashMap(2 * this.anchor2Offset.size() + 1);

        for (int i = 0; i < anchors.size(); i++) {
            PositionAnchor anchor = (PositionAnchor) anchors.get(i);
            Long _offset = new Long(anchor.getOffset());
            if (offset < _offset.longValue()) {
                if (len < _offset.longValue() - offset)
                    _offset = new Long(_offset.longValue() - len);
                else
                    _offset = new Long(offset);
            }
            anchor2Offset.put(anchor, _offset);
        }

        this.anchor2Offset = anchor2Offset;

        setChanged();
        notifyObservers(new ContentChangedEvent(this, new ByteSpan(loc, loc.addOffset(len - 1)),
                ContentChangedEvent.DELETED, oldContent));
        clearChanged();

        return len;
    }

    ////// PACKAGE PROTECTED
    void removeAnchor(PositionAnchor anchor) {
        Long offset = (Long) anchor2Offset.get(anchor);
        anchor2Offset.remove(anchor);
    }

    long getAnchorOffset(PositionAnchor p) {
        Long offset = (Long) anchor2Offset.get(p);
        if (offset == null) return -1;
        return offset.longValue();
    }

    void expandBuffer(int minimum) {
        int expandBy = (512 < minimum ? minimum : 512);
        byte[] data = new byte[this.data.length + expandBy];
        for (int i = 0; i < this.data.length; i++)
            data[i] = this.data[i];
        this.data = data;
    }

    void rawPrint() {
        System.out.println(new String(data));
    }
}
