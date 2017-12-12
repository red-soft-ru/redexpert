package org.underworldlabs.swing.hexeditor;

import org.executequery.gui.table.CreateTableSQLSyntax;
import org.underworldlabs.swing.hexeditor.bdoc.BinaryDocument;
import org.underworldlabs.swing.hexeditor.bdoc.ByteSpan;
import org.underworldlabs.swing.hexeditor.bdoc.ContentChangedEvent;
import org.underworldlabs.swing.hexeditor.bdoc.Location;
import org.underworldlabs.swing.hexeditor.textgrid.*;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

public class CharEditor extends TextGrid implements BinaryEditor {

    // CONSTANTS
    public static final int DEFAULT_BYTES_PER_ROW = 16;

    // MEMBERS
    protected BinaryDocument document;
    protected Location location;
    protected ByteSpan selection;
    protected LinkedList listeners;

    protected int bytesPerRow = DEFAULT_BYTES_PER_ROW;

    protected LocalTextGridModel localTextGridModel;
    protected LocalTextGridCursor localTextGridCursor;
    protected LocalDocumentObserver localDocumentObserver;

    private Color charColor;
    private Color selectedColor;

    /**
     * Construct the editor with a document.
     */
    public CharEditor(BinaryDocument document) {
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

        setBackground(SystemProperties.getColourProperty("user", "editor.text.background.colour"));
        setForeground(SystemProperties.getColourProperty("user", "editor.text.foreground.colour"));
        charColor = SystemProperties.getColourProperty("user", "editor.text.foreground.colour");
        selectedColor = SystemProperties.getColourProperty("user", "editor.text.selection.foreground");

    }

    String charset;

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        dim.width = leftMargin + bytesPerRow * charWidth;
        return dim;
    }

    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        dim.width = leftMargin + bytesPerRow * charWidth;
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
        fireBinaryEditorEvent(new BinaryEditorEvent(this, document, getCurrentLocation(), selection, null,
                BinaryEditorEvent.SELECTION_CHANGED));
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
        while (i.hasNext()) {
            BinaryEditorListener l = (BinaryEditorListener) i.next();
            l.editorUpdated(e);
        }
    }

    protected boolean shouldDrawCursor() {
        return super.shouldDrawCursor() && (selection == null || selection.length() == 0);
    }

    public void copy() {
        String selectedText = getSelectedText();
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
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                for (int i = 0; i < text.length(); i++) {
                    localTextGridCursor.typeKeyChar(text.charAt(i));
                }
            }
        } catch (Exception e) {
        }
    }

    ////////////////////////////////
    // GRID MODEL
    private class LocalTextGridModel implements TextGridModel {
        private int lastRowIndex = 0;
        private String lastRowText = null;
        private LinkedList listeners;
        private Color whiteColor = SystemProperties.getColourProperty("user", "editor.text.background.colour");
        private Color alternateColor = SystemProperties.getColourProperty("user", "editor.text.background.alternate.color");

        public LocalTextGridModel() {
            listeners = new LinkedList();
        }

        public int getColumnCount() {
            return bytesPerRow;
        }

        public int getRowCount() {
            return (int) document.length() / bytesPerRow + 1;
        }

        public char getCharAt(int row, int col) {
            if (lastRowText == null || lastRowIndex != row) {
                lastRowIndex = row;
                lastRowText = getRowText(row);
            }
            return lastRowText.charAt(col);
        }

        public Color getCharColor(int row, int col) {
            return (isEnabled() ? charColor : selectedColor);
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
            while (i.hasNext()) {
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
                bytesRead = document.read(document.createOffset(row * bytesPerRow), b);
            } catch (Exception ignore) {
            }
            if (charset == null || charset.equals(CreateTableSQLSyntax.NONE)) {
                result.append(new String(b));
            } else try {
                result.append(new String(b, charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                result.append(new String(b));
            }
            int desiredLength = getColumnCount();
            while (result.length() < desiredLength) {
                result.append(' ');
            }
            return MiscUtils.replaceUnsupportedSimbolsToDot((result.toString()));

        }


        public Location gridToLocation(int row, int col) {
            return document.createOffset((row * bytesPerRow) + col);
        }

        public Point locationToGrid(Location loc) {
            long offset = loc.getOffset();
            Point p = new Point();
            p.y = (int) (offset / bytesPerRow);
            p.x = (int) (offset % bytesPerRow);
            return p;
        }
    }

    ////////////////////////////////
    // GRID CURSOR
    private class LocalTextGridCursor extends TextGridCursor {
        private Color insertColor = Color.BLACK;
        private Color greySelectionColor = new Color(225, 225, 225);

        public void moveTo(int row, int column) {
            try {
                // this block restricts the cursor to
                // parts where there are actual bytes
                int realColumn = column;
                int realRow = row;
                if (realColumn >= bytesPerRow) {
                    realColumn = realColumn % bytesPerRow;
                    realRow += column / bytesPerRow;
                } else while (realColumn < 0) {
                    realColumn += bytesPerRow;
                    realRow--;
                }
                if (realRow == getRowCount())
                    return;
                byte[] b = new byte[bytesPerRow];
                int bytesRead = document.read(document.createOffset(realRow * bytesPerRow), b);
                if (bytesRead == -1) {
                    column = 0;
                    row = realRow;
                } else if (bytesRead <= realColumn) {
                    column = bytesRead;
                    row = realRow;
                }
            } catch (Exception ignore) {
            }

            super.moveTo(row, column);

            Location cLoc = localTextGridModel.gridToLocation(getCurrentRow(), getCurrentColumn());

            ByteSpan span = null;
            if (isMarkSet()) {
                Location mLoc = localTextGridModel.gridToLocation(getMarkedRow(), getMarkedColumn());
                Location start, end;
                if (mLoc.compareTo(cLoc) <= 0) {
                    start = mLoc;
                    end = cLoc;
                } else {
                    start = cLoc;
                    end = mLoc;
                }
                byte[] b = new byte[1];
                while (end.getOffset() > 0 && document.read(end, b) < 0)
                    end = end.addOffset(-1);
                span = new ByteSpan(start, end);
            }
            setSelectionSpan(span);

            fireBinaryEditorEvent(new BinaryEditorEvent(CharEditor.this, document, cLoc, getSelectionSpan(), null,
                    BinaryEditorEvent.LOCATION_CHANGED));
        }

        public void moveTo(Location loc) {
            Location cLoc = localTextGridModel.gridToLocation(getCurrentRow(), getCurrentColumn());
            if (cLoc.compareTo(loc) != 0) {
                Point p = localTextGridModel.locationToGrid(loc);
                moveTo(p.y, p.x + 1);
            }
        }

        public Color getSelectionColor() {
            if (CharEditor.this.hasFocus())
                return SystemProperties.getColourProperty("user", "editor.text.selection.background");
            else
                return SystemProperties.getColourProperty("user", "editor.text.selection.background.alternative");
        }

        public Color getSelectedTextColor() {
            Color color = null;
            if (CharEditor.this.hasFocus()) {
                color = (Color) UIManager.get("TextArea.selectionForeground");
            }
            return (color != null ? color : super.getSelectedTextColor());
        }

        public Point getSelectionStart() {
            Point selectionStart = null;
            ByteSpan span = getSelectionSpan();
            if (span != null && span.length() > 0) {
                Point p = localTextGridModel.locationToGrid(span.getStartLocation());
                selectionStart = new Point(p.x, p.y);
            }
            return selectionStart;
        }

        public boolean isSelected(int row, int column) {
            ByteSpan span = getSelectionSpan();
            if (span != null) {
                Point p = localTextGridModel.locationToGrid(span.getStartLocation());
                return span.contains(localTextGridModel.gridToLocation(row, column));
            }
            return false;
        }

        public void paint(Graphics g) {
            if (draw) {
                Rectangle rect = getCaretRect();
                g.setColor(insertColor);
                g.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
            }
        }

        public void typeKeyChar(char keyChar) {
            if (keyChar != KeyEvent.CHAR_UNDEFINED &&
                    keyChar != KeyEvent.VK_ESCAPE &&
                    keyChar != KeyEvent.VK_ENTER &&
                    keyChar != KeyEvent.VK_DELETE &&
                    keyChar != KeyEvent.VK_BACK_SPACE) {
                int byteValue;
                if (charset == null || charset.equals(CreateTableSQLSyntax.NONE))
                    byteValue = Character.toString(keyChar).getBytes()[0];
                else try {
                    byte[] mas = Character.toString(keyChar).getBytes(charset);
                    byteValue = mas[0] & 0xFF;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    byteValue = Character.toString(keyChar).getBytes()[0];
                }
                if (byteValue >= 0 && byteValue <= 0xFF) {
                    // There is a selection ... delete it first
                    if (selection != null && selection.length() > 0) {
                        int selectionLength = (int) selection.length();
                        moveTo(selection.getEndLocation().addOffset(-selectionLength));
                        getDocument().delete(selection.getStartLocation(), selectionLength);
                        clearMark();
                    }
                    getDocument().insert(localTextGridModel.gridToLocation(getCurrentRow(), getCurrentColumn()), byteValue);
                    right();
                    setSelectionSpan(null);
                }
            }
        }

        public void deleteSelection(ByteSpan selection) {
            moveTo(selection.getEndLocation().addOffset(-selection.length()));
            getDocument().delete(selection.getStartLocation(), (int) selection.length());
            clearMark();
            setSelectionSpan(null);
        }

        protected void processComponentKeyEvent(KeyEvent e) {
            super.processComponentKeyEvent(e);

            ByteSpan selection = getSelectionSpan();

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_END:
                        e.consume();
                        return;

                    case KeyEvent.VK_BACK_SPACE:
                        if (selection != null && selection.length() > 0) {
                            deleteSelection(selection);
                        } else if (getCurrentColumn() > 0) {
                            Location loc = localTextGridModel.gridToLocation(getCurrentRow(), getCurrentColumn() - 1);
                            getDocument().delete(loc, 1);
                            left();
                        } else if (getCurrentRow() > 0) {
                            Location loc = localTextGridModel.gridToLocation(getCurrentRow() - 1, getColumnCount() - 1);
                            getDocument().delete(loc, 1);
                            left();
                        }
                        break;

                    case KeyEvent.VK_DELETE:
                        if (selection != null && selection.length() > 0) {
                            moveTo(selection.getEndLocation().addOffset(-selection.length()));
                            getDocument().delete(selection.getStartLocation(), (int) selection.length());
                            clearMark();
                            setSelectionSpan(null);
                        } else {
                            Location loc = localTextGridModel.gridToLocation(getCurrentRow(), getCurrentColumn());
                            getDocument().delete(loc, 1);
                        }
                        break;
                }
            } else if (e.getID() == KeyEvent.KEY_TYPED && (e.getModifiers() | KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK) {
                typeKeyChar(e.getKeyChar());
                e.consume();
            }
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
