package org.underworldlabs.swing.hexeditor.bdoc;

public class PropertyChangedEvent extends BinaryDocumentEvent {

  // PRIVATE MEMBERS
  private Object key;
  private Object oldValue;
  private Object newValue;
  
  // CONSTRUCTOR
  public PropertyChangedEvent( BinaryDocument bDoc, Object key, Object oldValue, Object newValue ) {
    super(bDoc);
    this.key = key;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  // GETTERS
  public Object getKey() {
    return key;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public Object getNewValue() {
    return newValue;
  }
  
  public AnnotatedBinaryDocument getAnnotatedBinaryDocument() {
    return (AnnotatedBinaryDocument) getDocument();
  }
}
