package org.underworldlabs.swing.hexeditor;

import org.underworldlabs.swing.hexeditor.bdoc.*;

public interface BinaryEditor {

  // General getters and setters
  public BinaryDocument getDocument();
  public void setDocument(BinaryDocument document);

  public Location getCurrentLocation();
  public void setCurrentLocation(Location loc);

  public ByteSpan getSelectionSpan();
  public void setSelectionSpan(ByteSpan selection);

  // Add/remove listeners
  public void addBinaryEditorListener( BinaryEditorListener l );
  public void removeBinaryEditorListener( BinaryEditorListener l );
}
