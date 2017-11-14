package org.underworldlabs.swing.hexeditor.bdoc;

public class ContentChangedEvent extends BinaryDocumentEvent {

  // CONSTANTS
  public static final int WRITTEN         = 0;
  public static final int INSERTED        = 1;
  public static final int DELETED         = 2;
  
  // PRIVATE MEMBERS
  private int type;
  private ByteSpan span;
  private byte[] oldContent; // on WRITTEN and DELETED only

  // CONSTRUCTOR
  public ContentChangedEvent( BinaryDocument bDoc, ByteSpan span, int type, byte[] oldContent ) {
    super(bDoc);
    this.span = span;
    this.type = type;
    this.oldContent = oldContent;
  }

  // GETTERS
  public int getType() {
    return type;
  }

  public ByteSpan getSpan() {
    return span;
  }
  
  public byte[] getOldContent() {
    return oldContent;
  }
}
