package org.underworldlabs.swing.hexeditor;

import org.underworldlabs.swing.hexeditor.bdoc.BinaryDocument;
import org.underworldlabs.swing.hexeditor.bdoc.BinaryDocumentEvent;
import org.underworldlabs.swing.hexeditor.bdoc.ByteSpan;
import org.underworldlabs.swing.hexeditor.bdoc.Location;

import java.util.EventObject;

public class BinaryEditorEvent extends EventObject {

    // CONSTANTS
    public static final int DOCUMENT_CHANGED = 0;
    public static final int SELECTION_CHANGED = 1;
    public static final int LOCATION_CHANGED = 2;
    public static final int DOCUMENT_EVENT = 3;

    // MEMBERS
    private BinaryDocument document;
    private Location location;
    private ByteSpan selection;
    private BinaryDocumentEvent docEvent;
    private int type;

    // CONSTRUCTOR
    public BinaryEditorEvent(BinaryEditor source, BinaryDocument document,
                             Location location, ByteSpan selection,
                             BinaryDocumentEvent docEvent, int type) {
        super(source);
        this.document = document;
        this.location = location;
        this.selection = selection;
        this.docEvent = docEvent;
        this.type = type;
    }

    // GETTERS
    public BinaryDocument getDocument() {
        return document;
    }

    public Location getCurrentLocation() {
        return location;
    }

    public ByteSpan getSelectionSpan() {
        return selection;
    }

    public BinaryDocumentEvent getDocumentEvent() {
        return docEvent;
    }

    public int getType() {
        return type;
    }
}
