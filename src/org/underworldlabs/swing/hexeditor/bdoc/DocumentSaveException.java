package org.underworldlabs.swing.hexeditor.bdoc;

public class DocumentSaveException extends BinaryDocumentException {
    public DocumentSaveException(BinaryDocument bDoc) {
        super(bDoc);
    }

    public DocumentSaveException(BinaryDocument bDoc, String s) {
        super(bDoc, s);
    }
}
  
