package org.underworldlabs.swing.hexeditor.bdoc;
import  java.io.*;
import  java.util.*;

public class AnnotatedBinaryDocument extends BinaryDocument {
   
  // PRIVATE MEMBERS
  protected TreeMap indexMap;
  protected HashMap keyMap;   
  protected HashMap properties;
        
  // CONSTRUCTORS
  public AnnotatedBinaryDocument() {
    super();
    indexMap = new TreeMap();
    keyMap = new HashMap();
    properties = new HashMap();
  }

  public AnnotatedBinaryDocument(File file) throws IOException {
    super(file);
    indexMap = new TreeMap();
    keyMap = new HashMap();
    properties = new HashMap();
  }

  public AnnotatedBinaryDocument(byte[] data,boolean readOnly)  {
    super(data,readOnly);
    indexMap = new TreeMap();
    keyMap = new HashMap();
    properties = new HashMap();
  }
  
  public AnnotatedBinaryDocument(File file, boolean readOnly) throws IOException {
    super(file, readOnly);
    indexMap = new TreeMap();
    keyMap = new HashMap();
    properties = new HashMap();
  }
  
  public AnnotatedBinaryDocument(File file, boolean readOnly, boolean loadAnnotations) throws IOException {
    super();
    // TODO: Set the file and length. Clear isNew
  }
  
  // SIMPLE PROPERTY STUFF
  public void putProperty(Object key, Object value) {
    Object oldValue = properties.get(key);
    
    if (value == null)
      properties.remove(key);
    else
      properties.put(key,value);

    firePropertyChanged(new PropertyChangedEvent(this, key, oldValue, value));
  }

  public Object getProperty(Object key) {
    return properties.get(key);
  }
  
  protected void firePropertyChanged(PropertyChangedEvent e) {
    setChanged();
    notifyObservers(e);
  }

  public Object [] getProperties() {
    return properties.keySet().toArray();
  }
 
 /* 
  // ANNOTATED BINARY DOCUMENT STUFF
  public void addAnnotation(Object key, ByteSpan span, Object value) {
    setChanged();
    notifyObservers( new AnnotationChangedEvent(this, annotation, AnnotationChangedEvent.ADDED) );
  }
  
  public void removeAnnotation(Object value) {
    setChanged();
    notifyObservers( new AnnotationChangedEvent(this, annotation, AnnotationChangedEvent.DELETED) );
  }

  public int countAnnotations() {
    return hashMap.size();
  }
 
  public AnnotatedByteSpan [] getAnnotations(ByteSpan span, boolean matchPartials) {
    return null;
  }

  public AnnotatedByteSpan [] getAnnotations(ByteSpan span) {
    return getAnnotations(span, true);
  }  
  */
}

