package org.underworldlabs.swing.hexeditor.bdoc;

public class AnnotationChangedEvent extends BinaryDocumentEvent {

  // CONSTANTS
  public static final int ADDED         = 0;
  public static final int DELETED       = 1;
  public static final int MODIFIED      = 2;
  
  // PRIVATE MEMBERS
  private int type;
  private AnnotatedByteSpan annotation;

  // CONSTRUCTOR
  public AnnotationChangedEvent( BinaryDocument bDoc, AnnotatedByteSpan annotation, int type ) {
    super(bDoc);
    this.annotation = annotation;
    this.type = type;
  }

  // GETTERS
  public int getType() {
    return type;
  }

  public AnnotatedByteSpan getAnnotation() {
    return annotation;
  }

  public AnnotatedBinaryDocument getAnnotatedBinaryDocument() {
    return (AnnotatedBinaryDocument) getDocument();
  }
}
