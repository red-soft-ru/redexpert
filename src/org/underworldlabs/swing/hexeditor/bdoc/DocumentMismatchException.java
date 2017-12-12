package org.underworldlabs.swing.hexeditor.bdoc;

public class DocumentMismatchException extends BinaryDocumentException {
    public DocumentMismatchException(BinaryDocument bDoc) {
        super(bDoc);
    }

    public DocumentMismatchException(BinaryDocument bDoc, String s) {
        super(bDoc, s);
    }
}
  
