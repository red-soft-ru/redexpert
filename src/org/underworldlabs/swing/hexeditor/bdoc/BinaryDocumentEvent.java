package org.underworldlabs.swing.hexeditor.bdoc;

public abstract class BinaryDocumentEvent {

  // PRIVATE MEMBERS
  private BinaryDocument bDoc;

  // CONSTRUCTORS
  public BinaryDocumentEvent( BinaryDocument bDoc ) {
    this.bDoc = bDoc;
  }

  // GETTERS
  public BinaryDocument getDocument() {
    return bDoc;
  }
}
