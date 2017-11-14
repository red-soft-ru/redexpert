package org.underworldlabs.swing.hexeditor;

import org.underworldlabs.swing.hexeditor.bdoc.*;

public interface BinaryDocumentEditor {

  // Constants
  public static final Object CURSOR_KEY = new Object();
  public static final Object SELECTION_KEY = new Object();
  public static final Object ANNOTATION_KEY = new Object();
  public static final Object ANNOTATION_STYLE_MAP_KEY = new Object();

  // Keys used to "synchronize" common objects between editors. 
  public Object getCursorKey();
  public void setCursorKey(Object key);
  
  public Object getSelectionKey();
  public void setSelectionKey(Object key);
 
  public Object getAnnotationKey();
  public void setAnnotationKey(Object key);
  
  public Object getAnnotationStyleMapKey();
  public void setAnnotationStyleMapKey(Object key);
  
  // General getters and setters
  public AnnotatedBinaryDocument getDocument();
  public void setDocument(AnnotatedBinaryDocument document);

  public Cursor getDocumentCursor();
  public void setDocumentCursor(Cursor cursor);

  public ByteSpan getSelectionSpan();
  public void setSelectionSpan(ByteSpan selection);
  public void selectAll();

  public Object getFocusedAnnotation();
  public void setFocusedAnnotation(Object annotation);

  // Oprtaions on selections
  public void cut();
  public void copy();
  public void paste();
  public void replaceSelection(byte [] b);
}
