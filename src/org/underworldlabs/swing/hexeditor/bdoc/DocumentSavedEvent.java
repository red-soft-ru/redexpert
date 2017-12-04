package org.underworldlabs.swing.hexeditor.bdoc;

import java.io.File;

public class DocumentSavedEvent extends BinaryDocumentEvent {

    // CONSTANTS
    public static final int SAVE = 0;
    public static final int SAVE_AS = 1;
    public static final int SAVE_ANNOTATIONS = 2;
    public static final int SAVE_ANNOTATIONS_AS = 3;

    // PRIVATE MEMBERS
    private int type;
    private File file;

    // CONSTRUCTOR
    public DocumentSavedEvent(BinaryDocument bDoc, File file, int type) {
        super(bDoc);
        this.file = file;
        this.type = type;
    }

    // GETTERS
    public int getType() {
        return type;
    }

    public File getFile() {
        return file;
    }
}
