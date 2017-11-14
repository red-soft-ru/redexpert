

package org.underworldlabs.swing.hexeditor.bdoc;
import  java.util.*;

public class AnnotatedByteSpan extends ByteSpan {
     
  // PRIVATE MEMBERS 
  private Object value;

  // CONSTRUCTORS
  public AnnotatedByteSpan( Location startLocation, Location endLocation, Object value ) {
    super( startLocation, endLocation );
    this.value = value;
  }

  // GETTERS AND SETTERS
  public Object getValue() {
    return value;
  }

  public void setValue( Object value ) {
    this.value = value;
  }

  public AnnotatedBinaryDocument getAnnotatedBinaryDocument() {
    return (AnnotatedBinaryDocument) getDocument();
  }
}

