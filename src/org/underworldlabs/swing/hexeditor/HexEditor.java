package org.underworldlabs.swing.hexeditor;

import org.underworldlabs.swing.hexeditor.bdoc.*;
import org.underworldlabs.swing.hexeditor.textgrid.TextGrid;
import org.underworldlabs.swing.hexeditor.textgrid.TextGridModel;
import org.underworldlabs.swing.hexeditor.textgrid.TextGridModelEvent;
import org.underworldlabs.swing.hexeditor.textgrid.TextGridModelListener;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

public class HexEditor extends JPanel implements BinaryEditor, Scrollable {

    // CONSTANTS
    public static final int SPACER_WIDTH = 2;

    private static Color addressBackground = SystemProperties.getColourProperty("user", "editor.text.background.colour");

    // MEMBERS
    protected TextGrid addressComponent;
    protected ByteEditor hexEditor;
    protected CharEditor asciiEditor;

    private HexEditorListener hexEditorListener;
    private ASCIIEditorListener asciiEditorListener;
    private BinaryDocument binaryDocument;

    /**
     * Construct the editor with a document.
     */
    public HexEditor(BinaryDocument document, String charset) {
        Color bg = SystemProperties.getColourProperty("user", "editor.text.background.colour");
        setBackground(bg);
        setForeground(SystemProperties.getColourProperty("user", "editor.text.foreground.colour"));

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gridbag);
        binaryDocument = document;

        hexEditor = new ByteEditor(document);
        asciiEditor = new CharEditor(document);
        setCharset(charset);
        addressComponent = new TextGrid(new AddressTextGridModel());

        hexEditorListener = new HexEditorListener();
        asciiEditorListener = new ASCIIEditorListener();

        hexEditor.addBinaryEditorListener(hexEditorListener);
        asciiEditor.addBinaryEditorListener(asciiEditorListener);

        JPanel spacer1 = new JPanel();
        JPanel spacer2 = new JPanel();

        addressComponent.setBackground(bg);
        hexEditor.setBackground(bg);
        asciiEditor.setBackground(bg);

        addressComponent.setFocusable(false);

        Dimension dim = new Dimension(SPACER_WIDTH, 1);
        spacer1.setBackground(bg);
        spacer1.setPreferredSize(dim);
        spacer1.setMaximumSize(dim);
        spacer1.setMinimumSize(dim);
        dim = new Dimension(2 * SPACER_WIDTH, 1);
        spacer2.setBackground(bg);
        spacer2.setPreferredSize(dim);
        spacer2.setMaximumSize(dim);
        spacer2.setMinimumSize(dim);

        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(addressComponent, gbc);
        add(addressComponent);

        gridbag.setConstraints(spacer1, gbc);
        add(spacer1);

        gridbag.setConstraints(hexEditor, gbc);
        add(hexEditor);

        gridbag.setConstraints(spacer2, gbc);
        add(spacer2);

        gbc.insets.right = SPACER_WIDTH;
        gridbag.setConstraints(asciiEditor, gbc);
        add(asciiEditor);
    }


    public void setCharset(String charset) {
        this.asciiEditor.setCharset(charset);
    }

    public void setData(byte[] data) {
        binaryDocument.setData(data);
        TextGridModelEvent gme = new TextGridModelEvent(addressComponent.getModel(),
                TextGridModelEvent.FIRST_ROW,
                TextGridModelEvent.FIRST_COLUMN,
                TextGridModelEvent.LAST_ROW,
                TextGridModelEvent.LAST_COLUMN,
                TextGridModelEvent.UPDATE);
        ((AddressTextGridModel) addressComponent.getModel()).fireTextGridModelEvent(gme);

    }

    public Font getFont() {
        return (hexEditor == null ? super.getFont() : hexEditor.getFont());
    }

    public void focusHexView() {
        hexEditor.requestFocus();
    }

    public void focusAsciiView() {
        asciiEditor.requestFocus();
    }

    public int getSelectionStartVerticalOffset() {
        return hexEditor.getSelectionStartVerticalOffset();
    }

    public void setEnabled(boolean enabled) {
        hexEditor.setEnabled(enabled);
        asciiEditor.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public BinaryDocument getDocument() {
        return hexEditor.getDocument();
    }

    public void setDocument(BinaryDocument document) {
        binaryDocument = document;
        hexEditor.setDocument(document);
        asciiEditor.setDocument(document);
        TextGridModelEvent gme = new TextGridModelEvent(addressComponent.getModel(),
                TextGridModelEvent.FIRST_ROW,
                TextGridModelEvent.FIRST_COLUMN,
                TextGridModelEvent.LAST_ROW,
                TextGridModelEvent.LAST_COLUMN,
                TextGridModelEvent.UPDATE);
        ((AddressTextGridModel) addressComponent.getModel()).fireTextGridModelEvent(gme);
    }

    public Location getCurrentLocation() {
        return hexEditor.getCurrentLocation();
    }

    public void setCurrentLocation(Location location) {
        hexEditor.setCurrentLocation(location);
        asciiEditor.setCurrentLocation(location);
    }

    public ByteSpan getSelectionSpan() {
        return hexEditor.getSelectionSpan();
    }

    public void setSelectionSpan(ByteSpan selection) {
        hexEditor.setSelectionSpan(selection);
        asciiEditor.setSelectionSpan(selection);
    }

    public void addBinaryEditorListener(BinaryEditorListener l) {
    }

    public void removeBinaryEditorListener(BinaryEditorListener l) {
    }

    public String toString() {
        BinaryDocument doc = getDocument();
        if (doc == null)
            return "No Document.";
        else if (doc.isNew())
            return "New Document";
        else
            return doc.getFile().getName();
    }

    /////////////////////////
    // SCROLLABLE INTERFACE

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return hexEditor.getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return hexEditor.getScrollableBlockIncrement(visibleRect, orientation, direction);
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    ////////////////////////////////
    // ADDRESS COMPONENT GRID MODEL
    private class AddressTextGridModel implements TextGridModel {
        private int lastRowIndex = 0;
        private String lastRowText = null;
        private LinkedList listeners;

        public AddressTextGridModel() {
            listeners = new LinkedList();
        }

        public int getColumnCount() {
            return getRowText(0).length();
        }

        public int getRowCount() {
            return (int) hexEditor.getModel().getRowCount();
        }

        public char getCharAt(int row, int col) {
            if (lastRowText == null || lastRowIndex != row) {
                lastRowIndex = row;
                lastRowText = getRowText(row);
            }
            return lastRowText.charAt(col);
        }

        public Color getCharColor(int row, int col) {
            return SystemProperties.getColourProperty("user", "editor.text.foreground.colour");
        }

        public Color getCharBackground(int row, int col) {
            return addressBackground;
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
            String maxAddress = Integer.toString(Integer.MAX_VALUE, 16);
            String result = Integer.toString(row * hexEditor.getBytesPerRow(), 16);
            while (result.length() < maxAddress.length())
                result = Integer.toString(0, 16) + result;
            return result + ":";
        }
    }

    //////////////////////////
    // Hex Editor Listener
    private class HexEditorListener implements BinaryEditorListener {
        private boolean enabled = true;

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }

        public void editorUpdated(BinaryEditorEvent e) {
            // Forward the event

            // Expand the addresses component if needed
            BinaryDocumentEvent bDocEvent = e.getDocumentEvent();
            if (bDocEvent != null && bDocEvent instanceof ContentChangedEvent) {
                ContentChangedEvent ccEvent = (ContentChangedEvent) bDocEvent;
                TextGridModelEvent gme = new TextGridModelEvent(addressComponent.getModel(),
                        TextGridModelEvent.FIRST_ROW,
                        TextGridModelEvent.FIRST_COLUMN,
                        TextGridModelEvent.LAST_ROW,
                        TextGridModelEvent.LAST_COLUMN,
                        TextGridModelEvent.UPDATE);
                ((AddressTextGridModel) addressComponent.getModel()).fireTextGridModelEvent(gme);
            }

            // Synchronize the ascii editor
            if (!enabled) return;
            asciiEditorListener.disable();
            asciiEditor.setCurrentLocation(e.getCurrentLocation());
            asciiEditor.setSelectionSpan(e.getSelectionSpan());
            asciiEditorListener.enable();
        }
    }

    //////////////////////////
    // ASCII Editor Listener
    private class ASCIIEditorListener implements BinaryEditorListener {
        private boolean enabled = true;

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }

        public void editorUpdated(BinaryEditorEvent e) {
            if (!enabled) return;
            hexEditorListener.disable();
            hexEditor.setCurrentLocation(e.getCurrentLocation());
            hexEditor.setSelectionSpan(e.getSelectionSpan());
            hexEditorListener.enable();
        }
    }
}
