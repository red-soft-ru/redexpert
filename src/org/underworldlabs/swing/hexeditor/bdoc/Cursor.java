package org.underworldlabs.swing.hexeditor.bdoc;

import java.io.IOException;
import java.util.Observable;

public class Cursor extends Observable {

  // PRIVATE MEMBERS
  private BinaryDocument bDoc;
  private Position pos;
  
  // CONSTRUCTORS
  Cursor( Position pos ) {
    this.pos = pos;
  }

  // GETTERS
  public BinaryDocument getDocument() {
    return pos.getDocument();
  }
  
  public Position getPosition() {
    return pos;
  }

  public Offset getOffset() {
    return pos.getDocument().createOffset(pos.getOffset());
  }

  // CURSOR METHODS
  public void seek( Location loc ) {
    pos = pos.getDocument().createPosition(loc.getOffset());
    setChanged();
    notifyObservers(getOffset());
  }

  public int read() throws IOException {
    int b = getDocument().read( pos );
    if (b > 0)
      moveCursor(1); 
    return b;
  }

  public int read( byte [] b ) throws IOException {
    int bytesRead = getDocument().read( pos, b );
    if (bytesRead > 0) 
      moveCursor( bytesRead );
    return bytesRead;
  }

  public int read( byte [] b, int off, int len ) {
    int bytesRead = getDocument().read( pos, b, off, len );
    if (bytesRead > 0) 
      moveCursor( bytesRead );
    return bytesRead;
  }

  public void write( int b ) {
    getDocument().write( pos, b );
    moveCursor(1);
  }

  public void write( byte [] b ) {
    getDocument().write( pos, b );
    moveCursor(b.length);
  }

  public void write( byte [] b, int off, int len ) {
    getDocument().write( pos, b, off, len );
    moveCursor(len);
  }

  public void insert( int b ) {
    getDocument().insert( pos, b );
    moveCursor(1);
  }

  public void insert( byte [] b ) {
    getDocument().insert( pos, b );
    moveCursor(b.length);
  }

  public void insert( byte [] b, int off, int len ) {
    getDocument().insert( pos, b, off, len );
    moveCursor(len);
  }
  
  public int delete( int n ) {
    return getDocument().delete( pos, n );
  }
  
  public int skip( int n ) {
    if (pos.getOffset() + n >= getDocument().length())
      n = (int) (getDocument().length() - pos.getOffset() - n - 1);
    if (n>0) 
      moveCursor(n);
    
    return n;    
  }

  ////
  private void moveCursor(int n) {
    Position oldPos = pos;      
    pos = (Position) pos.addOffset(n);
    if (pos != oldPos)
      oldPos.dispose();

    setChanged();
    notifyObservers(getOffset());
  }
}
