package org.underworldlabs.swing.hexeditor;

import org.underworldlabs.swing.hexeditor.bdoc.*;

public class BinaryDocumentEditorAdapter implements BinaryDocumentEditor {

  private AnnotatedBinaryDocument document;      
  private Object cursorKey = BinaryDocumentEditor.CURSOR_KEY;
  private Object selectionKey = BinaryDocumentEditor.SELECTION_KEY;
  private Object annotationKey = BinaryDocumentEditor.ANNOTATION_KEY;
  private Object annotationStyleMapKey = BinaryDocumentEditor.ANNOTATION_STYLE_MAP_KEY;  
        
  /**
   * Default constructor.
   */
  public BinaryDocumentEditorAdapter() {
  }
  
  // DOCUMENT STUFF
   
  public AnnotatedBinaryDocument getDocument() {
    return document;
  }

  public void setDocument( AnnotatedBinaryDocument document ) {
    this.document = document;
    setSelectionSpan(null);
  }
  
  // "SYNCHRONIZATION" KEY STUFF
  
  public Object getCursorKey() {
    return cursorKey;
  }

  public void setCursorKey(Object key) {
    cursorKey = key; 
  }
  
  public Object getSelectionKey() {
    return selectionKey;
  }

  public void setSelectionKey(Object key) {
    selectionKey = key; 
  }
  
  public Object getAnnotationKey() {
    return annotationKey;
  }

  public void setAnnotationKey(Object key) {
    annotationKey = key; 
  }
  
  public Object getAnnotationStyleMapKey() {
    return annotationStyleMapKey;
  }

  public void setAnnotationStyleMapKey(Object key) {
    annotationStyleMapKey = key; 
  }
  
  // CURSOR STUFF

  public Cursor getDocumentCursor() {
    return (Cursor) document.getProperty(cursorKey);
  }

  public void setDocumentCursor(Cursor cursor) {
    document.putProperty(cursorKey, cursor);
  }
  
  // SELECTION STUFF  
  
  public ByteSpan getSelectionSpan() {
    return (ByteSpan) document.getProperty(selectionKey);
  }

  public void setSelectionSpan( ByteSpan selection ) {
    document.putProperty(selectionKey, selection);
  }

  public void selectAll() {
    setSelectionSpan( new ByteSpan(document.createOffset(0), 
                      document.createOffset(document.length())) );
  }

  // ANNOTAION STUFF
  
  public Object getFocusedAnnotation() {
    return document.getProperty(annotationKey);
  }

  public void setFocusedAnnotation( Object annotation ) {
    document.putProperty(annotationKey, annotation);
  } 
  
  // ANNOTAION STYLE MAP STUFF
  
  public Object getAnnotationStyleMap() {
    return document.getProperty(annotationStyleMapKey);
  }

  public void setAnnotationStyleMap( Object annotationStyleMap ) {
    document.putProperty(annotationStyleMapKey, annotationStyleMap);
  }

  // OPERATIONS ON SELECTIONS
  
  public void cut() {
  }

  public void copy() {
  }

  public void paste() {
  }

  public void replaceSelection(byte [] b) {
  }
}
