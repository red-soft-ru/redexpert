package org.underworldlabs.swing.hexeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;

import org.underworldlabs.swing.hexeditor.bdoc.*;
import org.underworldlabs.swing.hexeditor.textgrid.*;
import org.underworldlabs.util.SystemProperties;

public class ByteEditor extends TextGrid implements BinaryEditor {
        
  // CONSTANTS
  public static final int DEFAULT_BYTES_PER_ROW = 16;
  
  // MEMBERS
  protected BinaryDocument document;
  protected Location location;
  protected ByteSpan selection;
  protected LinkedList listeners;
  
  protected int bytesPerRow = DEFAULT_BYTES_PER_ROW;
  protected int radix;
  protected int byteWidth;

  protected LocalTextGridModel localTextGridModel;
  protected LocalTextGridCursor localTextGridCursor;
  protected LocalDocumentObserver localDocumentObserver;

  /**
   * Construct the editor with a document.
   */
  public ByteEditor(BinaryDocument document) {
    super(null);
    listeners = new LinkedList();
    
    localTextGridModel = new LocalTextGridModel();
    localTextGridCursor = new LocalTextGridCursor();
    localDocumentObserver = new LocalDocumentObserver();

    setDocument(document);
    setModel(localTextGridModel);
    setTextGridCursor(localTextGridCursor);
   
    setSelectionSpan(null);
    setCurrentLocation(document.createOffset(0));
    
    radix = 16;
    byteWidth = Integer.toString(0xFF,radix).length(); 
    
    setBackground(SystemProperties.getColourProperty("user","editor.text.background.colour"));
    setForeground(SystemProperties.getColourProperty("user","editor.text.foreground.colour"));
  }

  public int getBytesPerRow() {
    return bytesPerRow;
  }  
  
  public Dimension getPreferredSize() {
    int charsPerRow = bytesPerRow*(byteWidth+1)-1;
    Dimension dim = super.getPreferredSize();
    dim.width = leftMargin + charsPerRow*charWidth;
    return dim;
  }

  public Dimension getMinimumSize() {
    int charsPerRow = bytesPerRow*(byteWidth+1)-1;
    Dimension dim = super.getMinimumSize();
    dim.width = leftMargin + charsPerRow*charWidth;
    return dim;
  }

  public BinaryDocument getDocument() {
    return document;
  }

  public void setDocument(BinaryDocument document) {
    if (this.document != null)
      document.deleteObserver(localDocumentObserver);
    
    this.document = document;
    
    if (this.document != null)
      document.addObserver(localDocumentObserver);
  }

  public Location getCurrentLocation() {
    return localTextGridModel.gridToLocation(localTextGridCursor.getCurrentRow(),
                                             localTextGridCursor.getCurrentColumn());
  }
  
  public void setCurrentLocation(Location location) {
    localTextGridCursor.moveTo(location);
  }

  public ByteSpan getSelectionSpan() {
    return selection;
  }
  
  public void setSelectionSpan(ByteSpan selection) {
    this.selection = selection;
    fireBinaryEditorEvent( new BinaryEditorEvent(this, document, getCurrentLocation(), selection, null,
                                                    BinaryEditorEvent.SELECTION_CHANGED) );
    repaint();
  }

  public void addBinaryEditorListener(BinaryEditorListener l) {
    listeners.add(l);
  }

  public void removeBinaryEditorListener(BinaryEditorListener l) {
    listeners.remove(l);
  }

  public void fireBinaryEditorEvent(BinaryEditorEvent e) {
    Iterator i = listeners.iterator();
    while(i.hasNext()) {
      BinaryEditorListener l = (BinaryEditorListener) i.next();
      l.editorUpdated(e);
    }
  }

  protected boolean shouldDrawCursor() {
    return super.shouldDrawCursor() && (selection == null || selection.length()==0);
  }

  public void copy() {
    String selectedText = getSelectedText().replace(" ", "");
    if (selectedText != null && selectedText.length() > 0) {
      StringSelection ss = new StringSelection(selectedText);
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
    }
  }

  public void cut() {
    ByteSpan selection = getSelectionSpan();
    if (selection != null && selection.length() > 0) {
      copy();
      localTextGridCursor.deleteSelection(selection);
    }
  }

  public void paste() {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    try {
      if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        String text = (String)t.getTransferData(DataFlavor.stringFlavor);
        for (int i = 0; i < text.length(); i++) {
          localTextGridCursor.typeKeyChar(text.charAt(i));
        }
      }
    } catch (Exception e) { }
  }

  ////////////////////////////////
  // GRID MODEL
  private class LocalTextGridModel implements TextGridModel {
    private int lastRowIndex = 0;
    private String lastRowText = null;
    private LinkedList listeners;
    private Color whiteColor = SystemProperties.getColourProperty("user","editor.text.background.colour");
    private Color alternateColor = SystemProperties.getColourProperty("user","editor.text.background.alternate.color");
    
    public LocalTextGridModel() {
      listeners = new LinkedList();
    }
    
    public int getColumnCount() {
      return bytesPerRow*(byteWidth+1)-1;
    }
    
    public int getRowCount() {
      return (int) document.length()/bytesPerRow + 1;
    }
  
    public char getCharAt(int row, int col) {
      if (lastRowText == null || lastRowIndex != row ) {
        lastRowIndex = row;
        lastRowText  = getRowText(row);
      }
      return lastRowText.charAt(col);
    }
    
    public Color getCharColor(int row, int col) {
      return (isEnabled() ? SystemProperties.getColourProperty("user","editor.text.foreground.colour"):
      SystemProperties.getColourProperty("user","editor.text.selection.foreground"));
    }
    
    public Color getCharBackground(int row, int col) {
      return (row % 2 == 0 ? whiteColor : alternateColor);
    }
    
    public int getCharStyle(int row, int col) {
      return 0;
    }

    public void addTextGridModelListener(TextGridModelListener l) {
      listeners.add(l);
    }
    
    public void removeTextGridModelListener(TextGridModelListener l) {
      listeners.remove(l);
    }

    public void fireTextGridModelEvent(TextGridModelEvent e) {
      Iterator i = listeners.iterator();
      while(i.hasNext()) {
        TextGridModelListener l = (TextGridModelListener) i.next();
        l.textGridUpdated(e);
        lastRowIndex = 0;
        lastRowText = null;
      }
    }

    public String getRowText(int row) {
      StringBuilder result = new StringBuilder();
      int bytesRead = 0;
      byte[] b = new byte[bytesPerRow];

      try {
        bytesRead = document.read(document.createOffset(row*bytesPerRow), b);
      } catch (Exception ignore) {}

      for (int i = 0; i < bytesRead; i++) {
        if (i > 0) result.append(' ');
        String tmp = Integer.toString(0xFF & b[i], radix);
        int len = tmp.length();
        while (len < byteWidth) {
          String pad = Integer.toString(0, radix);
          len += pad.length();
          result.append(pad);
        }
        result.append(tmp);
      }

      int desiredLength = getColumnCount();
      while (result.length() < desiredLength) {
        result.append(' ');
      }

      return result.toString();
    }

    public Location gridToLocation(int row, int col) {
      return document.createOffset((row*bytesPerRow)+(col/(byteWidth+1)));
    }

    public Point locationToGrid(Location loc) {
      long offset = loc.getOffset();
      Point p = new Point();
      p.y = (int) (offset/bytesPerRow);
      p.x = (int) (offset%bytesPerRow)*(byteWidth+1)-1;
      return p;
    }
  }

  ////////////////////////////////
  // GRID CURSOR
  private class LocalTextGridCursor extends TextGridCursor {
    private boolean isInserting = false;
    private boolean insertingAtLineStart = true;
    private Color insertColor = SystemProperties.getColourProperty("user","editor.text.foreground.colour");
    private Color replaceColor =SystemProperties.getColourProperty("user","editor.text.foreground.colour");
    private Color greySelectionColor =  SystemProperties.getColourProperty("user","editor.text.selection.background.alternative");
  
    public void left() {
      if (getCurrentColumn() == 0 && !insertingAtLineStart) {
        isInserting = true;
        insertingAtLineStart = true;
      } else {
        super.left();
      }
    }
    
    public void right() {
      if (getCurrentColumn() == getColumnCount() - 1) {
        super.right();
        isInserting = insertingAtLineStart = true;
      } else if (insertingAtLineStart) {
        int b = document.read(document.createOffset(getCurrentRow()*bytesPerRow));
        if (b != -1) {
          isInserting = insertingAtLineStart = false;
        }
      } else {
        super.right();
      }
    }
    
    public void up() {
      if (insertingAtLineStart) {
        super.up();
        isInserting = insertingAtLineStart = true;
      } else {
        super.up();
      }
    }
    
    public void down() {
      if (insertingAtLineStart) {
        super.down();
        isInserting = insertingAtLineStart = true;
      } else {
        super.down();
      }
    }

    public void moveTo(int row, int column) {
      if (insertingAtLineStart && column == 0 && row == getCurrentRow()) {
        return;
      }

      boolean insertingAtLineStart = false;
      try {
        int charsPerRow = bytesPerRow*(byteWidth+1)-1;
        int realColumn = column;
        int realRow = row;
        if (realColumn >= charsPerRow) {
          realColumn = realColumn%charsPerRow;
          realRow += column/charsPerRow;
        } else while (realColumn < 0) {
          realColumn += charsPerRow;
          realRow--;
        }
        byte [] b = new byte[bytesPerRow];
        int bytesRead = document.read(document.createOffset(realRow*bytesPerRow), b);
        if (bytesRead == -1) {
          column = 0;
          row = realRow;
          insertingAtLineStart = true;
        } else if (bytesRead*(byteWidth+1)-1 <= realColumn) {
          column = bytesRead*(byteWidth+1)-1;
          row = realRow;
        }
      } catch (Exception ignore) {}

      super.moveTo(row,column);
      this.isInserting = this.insertingAtLineStart = insertingAtLineStart;

      Location cLoc = localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn());
      
      if (isMarkSet()) {
        Location mLoc = localTextGridModel.gridToLocation(getMarkedRow(),getMarkedColumn());
        if (mLoc.compareTo(cLoc) <= 0)
          setSelectionSpan(new ByteSpan(mLoc,cLoc));
        else
          setSelectionSpan(new ByteSpan(cLoc,mLoc));
      }
      else {
        setSelectionSpan(null);
      }

      fireBinaryEditorEvent( new BinaryEditorEvent(ByteEditor.this, document, cLoc, getSelectionSpan(), null,
                                                   BinaryEditorEvent.LOCATION_CHANGED) );        
    }

    public void moveTo(Location loc) {
      Location cLoc = localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn());
      if (cLoc.compareTo(loc) != 0) {
        Point p = localTextGridModel.locationToGrid(loc);
        moveTo(p.y,p.x+1);
      }
    }

    public Color getSelectionColor() {
      if (ByteEditor.this.hasFocus())
        return SystemProperties.getColourProperty("user","editor.text.selection.background");
      else
        return greySelectionColor;
    }

    public Color getSelectedTextColor() {
      Color color = null;
      if (ByteEditor.this.hasFocus()) {
        color =  SystemProperties.getColourProperty("user","editor.text.selection.foreground");
      }
      return (color != null ? color : super.getSelectedTextColor());
    }

    public Point getSelectionStart() {
      Point selectionStart = null;
      ByteSpan span = getSelectionSpan();
      if (span != null && span.length() > 0) {
        Point p = localTextGridModel.locationToGrid(span.getStartLocation());
        selectionStart = new Point(p.x + 1, p.y);
      }
      return selectionStart;
    }

    public boolean isSelected(int row, int column) {
      ByteSpan span = getSelectionSpan();
      if (span != null) {
        Point p = localTextGridModel.locationToGrid(span.getStartLocation());
        if (p.x == column && p.y == row)
          return false;
        else
          return span.contains(localTextGridModel.gridToLocation(row,column));
      }
      return false;
    }

    public boolean isPositionedForInsert() {
      return getCurrentColumn()%(byteWidth+1) == 2 || insertingAtLineStart ||
             localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn()).getOffset() >= document.length();  
    }

    public char [] getByteChars() {
      char [] byteChars = new char[byteWidth];
      int row = getCurrentRow();
      int col = getCurrentColumn() - getCurrentColumn() % (byteWidth+1);
      TextGridModel model = getTextGrid().getModel();

      for (int i=0; i<byteChars.length; i++)
        byteChars[i] = model.getCharAt(row, col+i);        

      return byteChars;
    }

    public void paint(Graphics g) {
      if (draw) {
        Rectangle rect = getCaretRect();
        if (isPositionedForInsert()) {
          g.setColor(insertColor);
          if (insertingAtLineStart)
            g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
          else
            g.drawLine(rect.x + rect.width - 1, rect.y, rect.x + rect.width - 1, rect.y + rect.height - 1);
        } else {
          char c = getCharAt(getCurrentRow(), getCurrentColumn());
          g.setColor(replaceColor);
          g.fillRect(rect.x, rect.y, rect.width, rect.height);
          g.setColor(SystemProperties.getColourProperty("user","editor.text.selection.foreground"));
          g.drawChars(new char[] {c}, 0, 1, rect.x, 1 + rect.y + rect.height - charDescent);
        }
      }
    }

    protected void processComponentMouseEvent(MouseEvent e) {
      super.processComponentMouseEvent(e);
      if (e.getID() == MouseEvent.MOUSE_PRESSED) {
        if (e.getPoint().x <= leftMargin) {
          insertingAtLineStart = true;
        }
      }
    }

    public void typeKeyChar(char keyChar) {
      try {        
        // There is a selection
        if (selection != null && selection.length() > 0) {
          char [] byteChars = new char[byteWidth];
          byteChars[0] = keyChar;
          for (int i=1; i<byteWidth; i++)
             byteChars[i] = Integer.toString(0, radix).charAt(0);
          int byteValue = Integer.parseInt(new String(byteChars), radix);
          if (byteValue >=0 && byteValue <= 0xFF) {
            int selectionLength = (int) selection.length();
            moveTo(selection.getEndLocation().addOffset(-selectionLength + 1));
            getDocument().delete(selection.getStartLocation(), selectionLength);
            getDocument().insert(selection.getStartLocation(), byteValue);
            right();
            clearMark();
            setSelectionSpan(null);
          }
        }
        // No selection -- Typeover
        else if (!isPositionedForInsert()) {
          int offset = getCurrentColumn() % (byteWidth+1);
          int byteValue = 0;
          char [] byteChars = getByteChars();
          byteChars[offset] = keyChar;
          byteValue = Integer.parseInt(new String(byteChars), radix);
          if (byteValue >=0 && byteValue <= 0xFF) {
            getDocument().write(localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn()),byteValue);
            if (isInserting) {
              right();
              isInserting = true;
              if (getCurrentColumn() == 0)
                insertingAtLineStart = true;
            }
            else {
              right();
              if (isPositionedForInsert()) {
                if (getCurrentColumn() == 0 && getCurrentRow() == getRowCount() - 1) {
                  byte [] b = new byte[bytesPerRow];
                  int bytesRead = document.read(document.createOffset(getCurrentRow()*bytesPerRow), b);
                  if (bytesRead > 0)
                    right();
                } else {
                  right();
                }
              }
            }
          }
        }
        // No selection - Insert
        else {
          char [] byteChars = new char[byteWidth];
          byteChars[0] = keyChar;
          for (int i=1; i<byteWidth; i++)
            byteChars[i] = Integer.toString(0, radix).charAt(0);
          int byteValue = Integer.parseInt(new String(byteChars), radix);
          if (byteValue >=0 && byteValue <= 0xFF) {
            int col = (insertingAtLineStart ? getCurrentColumn() : getCurrentColumn()+(byteWidth+1));
            Location loc = localTextGridModel.gridToLocation(getCurrentRow(), col);
            getDocument().insert(loc, byteValue);
            right();
            right();
            isInserting = true;
          }
        }
      }
      catch(NumberFormatException exception) {
      }
    }

    public void deleteSelection(ByteSpan selection) {
      boolean move = true;
      Location newLoc = selection.getEndLocation().addOffset(-selection.length());
      if (newLoc.getOffset() == -1) {
        newLoc = newLoc.addOffset(1);
        move = false;
      }
      moveTo(newLoc);
      getDocument().delete(selection.getStartLocation(), (int) selection.length());
      clearMark();
      setSelectionSpan(null);
      if (move) {
        right();
        right();
      }
      isInserting = true;
      if (getCurrentColumn() == 0)
        insertingAtLineStart = true;
    }

    protected void processComponentKeyEvent(KeyEvent e) {
      super.processComponentKeyEvent(e);
      
      ByteSpan selection = getSelectionSpan();

      if (e.getID() == KeyEvent.KEY_PRESSED) {
        switch(e.getKeyCode()) {
          case KeyEvent.VK_END:
            e.consume();
            return;

          case KeyEvent.VK_BACK_SPACE:
            if (selection != null && selection.length() > 0) {
              deleteSelection(selection);
            } else if (isPositionedForInsert()) {
              if (getCurrentColumn() > 0) {
                Location loc = localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn()-1);
                getDocument().delete(loc, 1);
                left();
                // if we were at the end of the document,
                // a single left() may have already put
                // us into the correct position
                if (!isPositionedForInsert()) {
                  left();
                  if (getCurrentColumn() == 0)
                    insertingAtLineStart = true;
                  else
                    left();
                  isInserting = true;
                }
              } else if (getCurrentRow() > 0) {
                Location loc = localTextGridModel.gridToLocation(getCurrentRow()-1,getColumnCount()-1);
                getDocument().delete(loc, 1);
                left();
                // if we were at the end of the document,
                // a single left() may have already put
                // us into the correct position
                if (!isPositionedForInsert()) {
                  left();
                  left();
                }
              }
            }
            break;

          case KeyEvent.VK_DELETE:
            if (selection != null && selection.length() > 0) {
              boolean move = true;
              Location newLoc = selection.getEndLocation().addOffset(-selection.length());
              if (newLoc.getOffset() == -1) {
                newLoc = newLoc.addOffset(1);
                move = false;
              }
              moveTo(newLoc);
              getDocument().delete(selection.getStartLocation(), (int) selection.length());
              clearMark();
              setSelectionSpan(null);
              if (move) {
                right();
                right();
              }
              isInserting = true;
              if (getCurrentColumn() == 0)
                insertingAtLineStart = true;
            } else if (isPositionedForInsert()) {
                Location loc = localTextGridModel.gridToLocation(getCurrentRow(),getCurrentColumn()+1);
                getDocument().delete(loc, 1);
            }
            break;
        }
      } else if (e.getID() == KeyEvent.KEY_TYPED && (e.getModifiers() | KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK) {
        // User types a character
        typeKeyChar(e.getKeyChar());
        e.consume();
      }
    }
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    Rectangle clip = g2d.getClipBounds();
    g2d.setColor(Color.LIGHT_GRAY);
    for (int i = 0; i < 3; i++) { 
      Rectangle r1 = modelToView(0, (i+1)*4*(byteWidth+1));
      Rectangle r2 = modelToView(0, (i+1)*4*(byteWidth+1)-1);
      int x = (r1.x+r2.x)/2;
      g2d.drawLine(x, clip.y, x, clip.y + clip.height);
    }
  }

  ////////////////////////////////
  // DOCUMENT OBSERVER
  private class LocalDocumentObserver implements Observer {
    public void update(Observable o, Object arg) {

      // The document has changed
      if (arg instanceof ContentChangedEvent) {
        ContentChangedEvent e = (ContentChangedEvent) arg;
        localTextGridModel.fireTextGridModelEvent(
          new TextGridModelEvent(localTextGridModel,
                                 TextGridModelEvent.FIRST_ROW,
                                 TextGridModelEvent.FIRST_COLUMN,
                                 TextGridModelEvent.LAST_ROW,
                                 TextGridModelEvent.LAST_COLUMN,
                                 TextGridModelEvent.UPDATE));
      }
    }
  }
}
