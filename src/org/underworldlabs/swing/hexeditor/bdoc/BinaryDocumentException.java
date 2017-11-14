package org.underworldlabs.swing.hexeditor.bdoc;
import java.io.*;

public class BinaryDocumentException extends RuntimeException {
  private BinaryDocument bDoc;
        
  public BinaryDocumentException(BinaryDocument bDoc) {
    super();
    this.bDoc = bDoc;
  }
  
  public BinaryDocumentException(BinaryDocument bDoc, String s) {
    super(s);
    this.bDoc = bDoc;
  }

  public BinaryDocument getDocument() {
    return bDoc;
  }
}
  
