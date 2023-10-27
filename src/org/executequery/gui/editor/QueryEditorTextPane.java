/*
 * QueryEditorTextPane.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.editor;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.UndoableComponent;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.repository.EditorSQLShortcut;
import org.executequery.repository.EditorSQLShortcuts;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.sql.SqlMessages;
import org.executequery.util.UserProperties;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The SQL text area for the Query Editor.
 *
 * @author Takis Diakoumis
 */
public class QueryEditorTextPane extends SQLTextArea
        implements UndoableComponent,
        CaretListener,
        FocusListener{

    private static final int DEFAULT_CARET_BLINK_RATE = 500;

    private static final Insets INSETS = new Insets(2, 2, 2, 2);

    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_DOWN = 2;

    /**
     * The editor panel containing this text component
     */
    private final QueryEditorTextPanel editorPanel;
    private Map<String, EditorSQLShortcut> editorShortcuts;

    public QueryEditorTextPane(QueryEditorTextPanel editorPanel) {
        super(false);
        this.editorPanel = editorPanel;

        try {

            /*
            Action actions[] = getActions();
            Comparator<Action> comparator = new Comparator<Action>() {
              public int compare(Action a1, Action a2) {
                String firstName = (String) a1.getValue(Action.NAME);
                String secondName = (String) a2.getValue(Action.NAME);
                return firstName.compareTo(secondName);
              }
            };
            Arrays.sort(actions, comparator);

            int count = actions.length;
            System.out.println("Count: " + count);
            for (int i = 0; i < count; i++) {

                System.out.printf("%28s : %s\n",
                        actions[i].getValue(Action.NAME),
                        actions[i].getClass().getName());
            }
            */

            /*
            ActionMap actionMap = getActionMap();
            actionMap.put(DefaultEditorKit.selectWordAction,
                    new QueryEditorSelectWordAction(DefaultEditorKit.selectWordAction, false));

            // **** ctrl-right
            actionMap.put(DefaultEditorKit.nextWordAction,
                    new QueryEditorBeginWordAction(DefaultEditorKit.nextWordAction,
                            actionMap.get(DefaultEditorKit.nextWordAction)));

            // **** ctrl-left
            actionMap.put(DefaultEditorKit.previousWordAction,
                    new QueryEditorPreviousWordAction(DefaultEditorKit.previousWordAction,
                            actionMap.get(DefaultEditorKit.previousWordAction)));
            */

            initializeComponents();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ctrl-left
    static class QueryEditorPreviousWordAction extends TextAction {

        private final Action originalAction;

        QueryEditorPreviousWordAction(String nm, Action originalAction) {

            super(nm);
            this.originalAction = originalAction;
        }

        public void actionPerformed(ActionEvent e) {

            JTextComponent target = getTextComponent(e);

            if (target != null) {

                try {

                    int offset = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offset);
                    int endOffs = Utilities.getWordEnd(target, offset);

                    int caretPositionInWord = offset - begOffs;
                    Document document = target.getDocument();
                    String wordAtCursor = document.getText(begOffs, (endOffs - begOffs));

                    System.out.println("A - " + wordAtCursor);

                    if (StringUtils.isBlank(wordAtCursor)) {

                        originalAction.actionPerformed(e);
                        return;
                    }

                    if (caretPositionInWord == 0) {

                        if (offset > 0 && !Character.isWhitespace(document.getText(offset - 1, 1).charAt(0))) {

                            System.out.println("B - " + wordAtCursor);
                        }

//                        originalAction.actionPerformed(e);
//                        return;
                    }

                    String[] strings = StringUtils.splitByCharacterTypeCamelCase(wordAtCursor);
                    if (strings.length == 1) {

                        originalAction.actionPerformed(e);
                        return;
                    }

                    int movingOffset = 0;
                    for (String string : strings) {

                        int length = string.length();
                        movingOffset += length;

                        if (movingOffset >= caretPositionInWord) {

                            begOffs += (movingOffset - length);
                            break;
                        }

                    }

                    target.setCaretPosition(begOffs);

                } catch (BadLocationException bl) {

                    originalAction.actionPerformed(e);
                }

            }

        }

    }


    // ctrl-right
    static class QueryEditorBeginWordAction extends TextAction {

        private final Action originalAction;

        public QueryEditorBeginWordAction(String nm, Action originalAction) {
            super(nm);
            this.originalAction = originalAction;
        }

        public void actionPerformed(ActionEvent e) {

            JTextComponent target = getTextComponent(e);
            if (target != null) {

                try {

                    int offset = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offset);
                    int endOffs = Utilities.getWordEnd(target, offset);

                    int caretPositionInWord = offset - begOffs;
                    String wordAtCursor = target.getDocument().getText(begOffs, (endOffs - begOffs));

                    if (caretPositionInWord == wordAtCursor.length()) {

                        originalAction.actionPerformed(e);
                        return;
                    }

                    String[] strings = StringUtils.splitByCharacterTypeCamelCase(wordAtCursor);
                    if (strings.length == 1) {

                        originalAction.actionPerformed(e);
                        return;
                    }

                    int movingOffset = 0;
                    for (String string : strings) {

                        int length = string.length();
                        movingOffset += length;

                        if (movingOffset > caretPositionInWord) {

                            if (begOffs > 0) {

                                begOffs += movingOffset;

                            } else {

                                begOffs = movingOffset;
                            }
                            break;
                        }

                    }

                    target.setCaretPosition(begOffs);

                } catch (BadLocationException bl) {

                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }

            }

        }

    }

    private static final String separatorChars = ".(){}{},:;_-+/<>*&$";

    // word selection start
    static class QueryEditorBeginWordSelectionAction extends TextAction {

        public QueryEditorBeginWordSelectionAction(String nm) {

            super(nm);
        }

        public void actionPerformed(ActionEvent e) {

            JTextComponent target = getTextComponent(e);
            if (target != null) {

                try {

                    int offset = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offset);
                    int endOffs = Utilities.getWordEnd(target, offset);

                    int wordOffset = offset - begOffs;

                    String wordAtCursor = target.getDocument().getText(begOffs, (endOffs - begOffs));

                    int movingOffset = 0;
                    String[] strings = StringUtils.splitPreserveAllTokens(wordAtCursor, separatorChars, -1);

                    /*
                    if (strings.length == 1) {

                        // use original action
                    }
                    */

                    int count = 0;
                    for (String string : strings) {

                        int length = string.length();
                        movingOffset += length;
                        if (movingOffset > wordOffset) {

                            begOffs += (movingOffset - length);
                            break;
                        }
                        count++;

                    }

                    begOffs += count;
                    target.setCaretPosition(begOffs);

                } catch (BadLocationException bl) {

                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }

            }

        }

    }

    // word selection end
    static class QueryEditorEndWordSelectionAction extends TextAction {

        public QueryEditorEndWordSelectionAction(String name) {

            super(name);
        }

        public void actionPerformed(ActionEvent e) {

            JTextComponent target = getTextComponent(e);
            if (target != null) {
                try {
                    int offset = target.getCaretPosition();
                    int begOffs = Utilities.getWordStart(target, offset);
                    int endOffs = Utilities.getWordEnd(target, offset);

                    int wordOffset = offset - begOffs;

                    String wordAtCursor = target.getDocument().getText(begOffs, (endOffs - begOffs));

                    int count = 0;
                    int movingOffset = 0;
                    String[] strings = StringUtils.splitPreserveAllTokens(wordAtCursor, separatorChars, -1);
                    for (String string : strings) {

                        int length = string.length();
                        movingOffset += length;
                        if (movingOffset > wordOffset) {

                            endOffs = begOffs + movingOffset;
                            break;
                        }
                        count++;

                    }

                    endOffs += count;
                    target.moveCaretPosition(endOffs);

                } catch (BadLocationException bl) {

                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                }

            }

        }

    }

    static class QueryEditorSelectWordAction extends TextAction {

        private final Action start;
        private final Action end;

        public QueryEditorSelectWordAction(String nm, boolean select) {
            super(DefaultEditorKit.selectWordAction);
            start = new QueryEditorBeginWordSelectionAction("beginWord");
            end = new QueryEditorEndWordSelectionAction("endWord");
        }

        public void actionPerformed(ActionEvent e) {
            start.actionPerformed(e);
            end.actionPerformed(e);
        }

    }

    protected void initializeComponents() {

        setMargin(INSETS);

        if (editorPanel == null) {

            setEditorPreferences();
        }
        addCaretListener(this);

        // undo functionality
        undoManager.setLimit(userProperties().getIntProperty("editor.undo.count"));


        addFocusListener(this);

        setDragEnabled(true);
        setRequestFocusEnabled(true);
        setFocusable(true);

        // set the caret

        // set to insert mode
        document.setInsertMode(SqlMessages.INSERT_MODE);

        editorShortcuts = new HashMap<String, EditorSQLShortcut>();
        loadEditorShortcuts();
    }

    private UserProperties userProperties() {
        return UserProperties.getInstance();
    }



    public void disableUpdates(boolean disable) {

        String text = getText();
        if (disable) {

            setDocument(new DefaultStyledDocument());
            setText(text);
            disableCaretUpdate(true);

        } else {

            setDocument(document);
            setText(text);
            disableCaretUpdate(false);
        }

    }

    public void disableCaretUpdate(boolean disable) {

        if (disable) {

            removeCaretListener(this);

        } else {

            boolean hasListener = false;
            CaretListener[] caretListners = getCaretListeners();

            for (int i = 0; i < caretListners.length; i++) {

                if (caretListners[i] == this) {
                    hasListener = true;
                    break;
                }

            }

            if (!hasListener) {
                addCaretListener(this);
                caretUpdate(null);
            }

        }

    }

    /**
     * Clears the undo managers stored edits
     */
    protected void clearEdits() {
        undoManager.discardAllEdits();
    }

    /**
     * Removes (designed to be temporary) listeners attached
     * to this text pane including the caret updates and
     * undo/redo listener objects.
     */
    protected void uninstallListeners() {
        removeCaretListener(this);
        document.removeDocumentListener(this);
    }

    /**
     * Reinstates listeners attached to this text pane
     * including the caret updates and undo/redo listener objects.
     */
    protected void reinstallListeners() {
        addCaretListener(this);
        document.addDocumentListener(this);
    }

    private void loadDummyDocument() {
        //setDocument(new DefaultStyledDocument());
    }

    /**
     * Inserts the specified text at the offset.
     *
     * @param offset - the insertion point
     * @param text   - the text
     */
    public void insertTextAtOffset(int offset, String text) {
        try {
            fireTextUpdateStarting();

            try {
                // clear the contents of we have any
                int length = document.getLength();

                if (offset > length || offset < 0) {
                    offset = 0;
                }

                document.insertString(offset, text, null);

            } catch (BadLocationException e) {
            }

        } finally {

            fireTextUpdateFinished();
            setCaretPosition(offset);
        }
    }

    /**
     * Loads the specified text into a blank 'offscreen' document
     * before switching to the SQL document. This is most effective
     * for very large chunks of text loaded from file or similar.
     */
    public void loadText(String text) {

        try {

//            undoManager.reset();
//            undoManager.suspend();

            fireTextUpdateStarting();

            // clear the current held edits
            clearEdits();

            // create a dummy document to load the text into
            // loadDummyDocument();

            try {
                // clear the contents of we have any
                int length = document.getLength();
                if (length > 0) {

                    // replace the existing text
                    document.replace(0, length, text, null);

                } else {

                    // set the new text
                    document.insertString(0, text, null);
                }

            } catch (BadLocationException e) {
            }

            // reset the SQL document
            setDocument(document);

        } finally {

            fireTextUpdateFinished();
//            undoManager.reinstate();
            setCaretPosition(0);
        }

    }

    private void fireTextUpdateStarting() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        uninstallListeners();
    }

    private void fireTextUpdateFinished() {
        //updateLineBorder();
        reinstallListeners();
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Override to update the line border.
     */
    public void setText(String text) {
        super.setText(text);
        //updateLineBorder();
    }

    /**
     * Override to return false.
     */
    public boolean isOpaque() {
        return false;
    }

    /**
     * Paints the current line highlight and right-hand margin
     * before a call to the super class.
     *
     * @param g the <code>Graphics</code> object to protect
     */
    public void paintComponent(Graphics g) {

        int height = getHeight();
        int width = getWidth();

        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        // paint the current line highlight
        /*if (QueryEditorSettings.isDisplayLineHighlight()) {
            int currentRow = getCurrentCursorRow();
            g.setColor(QueryEditorSettings.getLineHighlightColour());
            g.fillRect(1, (currentRow * fontHeight) + 2, width - 1, fontHeight);
        }

        // paint the right-hand margin
        if (QueryEditorSettings.isDisplayRightMargin()) {
            int xPosn = fontWidth * QueryEditorSettings.getRightMarginSize();
            g.setColor(QueryEditorSettings.getRightMarginColour());
            g.drawLine(xPosn, 0, xPosn, height);
        }*/

        try {
            super.paintComponent(g);
        } catch (Exception e) {
        }

    }

    public void setQueryAreaText(String s) {
        setText(s);
    }

    /**
     * Shifts the text at position start to position end left.
     *
     * @param start - the start offset
     * @param end   - the end offset
     */
    public void shiftTextLeft(int start, int end) {

        getSQLSyntaxDocument().shiftTabEvent(start, end, false);
    }

    /**
     * Shifts the text at position start to the right.
     * This will usually be the start position of any
     * particular row.
     *
     * @param start - the start offset
     */
    public void shiftTextRight(int offset) {

        insertTextAtOffset(offset, "\t");
    }

    public void insertTextAfter(int after, String text) {
        if (getDocument().getLength() > 0) {
            text = "\n" + text;
        }
        insertTextAtOffset(after, text);
    }

    public SQLTextArea getQueryArea() {
        return this;
    }

    public void goToRow(int row) {
        int goToRow = getRowPosition(row - 1);
        if (goToRow < 0) {
            GUIUtilities.displayErrorMessage("The line number enterinsertTextAftered is invalid.");
            return;
        }
        setCaretPosition(goToRow);
    }

    protected void setEditorPreferences() {
        // call to super class
        super.setEditorPreferences();
        // set the pane background
        setBackground(QueryEditorSettings.getEditorBackground());
    }


    private KeywordRepository keywords() {

        return (KeywordRepository) RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
    }

    public void resetAttributeSets() {
        String text = getText();
        setEditorPreferences();
        //document.resetAttributeSets();
        lineBorder.updatePreferences(QueryEditorSettings.getEditorFont());
        lineBorder.repaint();
        setText(text);
    }

    /**
     * Returns the query around the specified (cursor) position.
     *
     * @param the position
     * @return the query around the specified position
     */
    private QueryWithPosition getQueryAt(int position) {

        String text = getText();
        if (MiscUtils.isNull(text)) {

            return new QueryWithPosition(0, 0, 0, Constants.EMPTY);
        }

        char[] chars = text.toCharArray();

        if (position == chars.length) {
            position--;
        }

        int start = -1;
        int end = -1;
        boolean wasSpaceChar = false;

        // determine the start point
        for (int i = position; i >= 0; i--) {

            if (chars[i] == Constants.NEW_LINE_CHAR) {

                if (i == 0 || wasSpaceChar) {

                    break;

                } else if (start != -1) {

                    if (chars[i - 1] == Constants.NEW_LINE_CHAR) {

                        break;

                    } else if (Character.isSpaceChar(chars[i - 1])) {

                        wasSpaceChar = true;
                        i--;
                    }

                }

            } else if (!Character.isSpaceChar(chars[i])) {

                wasSpaceChar = false;
                start = i;
            }

        }

        if (start < 0) { // text not found
            for (int j = 0; j < chars.length; j++) {
                if (!Character.isWhitespace(chars[j])) {
                    start = j;
                    break;
                }
            }
        }

        // determine the end point
        for (int i = start; i < chars.length; i++) {

            if (chars[i] == Constants.NEW_LINE_CHAR) {

                if (i == chars.length - 1 || wasSpaceChar) {
                    if (end == -1) {
                        end = i;
                    }
                    break;
                } else if (end != -1) {
                    if (chars[i + 1] == Constants.NEW_LINE_CHAR) {
                        break;
                    } else if (Character.isSpaceChar(chars[i + 1])) {
                        wasSpaceChar = true;
                        i++;
                    }
                }

            } else if (!Character.isSpaceChar(chars[i])) {
                end = i;
                wasSpaceChar = false;
            }
        }

        //Log.debug("start: " + start + " end: " + end);

        String query = text.substring(start, end + 1);
        //Log.debug(query);

        if ((MiscUtils.isNull(query) && start != 0)) { // || start == end) {

            return getQueryAt(start);
        }

        return new QueryWithPosition(position, start, end + 1, query);
    }

    // ----------------------------------------
    // DocumentListener implementation
    // ----------------------------------------

    /**
     * Does nothing.
     */


    /**
     * Notifies the parent QueryPanel that the text content
     * has changed and resets the line number border panel.
     *
     * @param the event object
     */
    public void insertUpdate(DocumentEvent e) {
        editorPanel.setContentChanged(true);
        super.insertUpdate(e);
    }


    public QueryWithPosition getQueryAtCursor() {

        return getQueryAt(getCaretPosition());
    }

    public String getWordEndingAtCursor() {

        return getWordEndingAt(getCaretPosition());
    }

    public String getCompleteWordEndingAtCursor() {

        String text = getText();
        if (MiscUtils.isNull(text)) {

            return Constants.EMPTY;
        }

        int start = indexOfWordStartFromIndex(getCaretPosition());
        int end = indexOfWordEndFromIndex();

        if (start < 0) {

            start = 0;
        }

        if (end < 0) {

            end = getText().length();
        }

        return text.substring(start, end).trim();
    }

    private int indexOfWordStartFromIndex(int index) {

        int start = -1;
        int end = index;

        char[] chars = getText().toCharArray();
        for (int i = end - 1; i >= 0; i--) {

            if (!Character.isLetterOrDigit(chars[i])
                    && chars[i] != '_' && chars[i] != '.') {

                start = i;
                break;
            }

        }

        return start;
    }

    private int indexOfWordEndFromIndex() {

        int start = getCaretPosition();
        char[] chars = getText().toCharArray();

        for (int i = start; i < chars.length; i++) {

            if (Character.isWhitespace(chars[i])) {

                return i;
            }

        }

        return -1;
    }

    private String getWordEndingAt(int position) {

        String text = getText();

        if (MiscUtils.isNull(text)) {

            return Constants.EMPTY;
        }

        char[] chars = text.toCharArray();

        int start = -1;
        int end = position;

        for (int i = end - 1; i >= 0; i--) {

            if (!Character.isLetterOrDigit(chars[i])
                    && chars[i] != '_' && chars[i] != '.' && chars[i] != '$') {

                start = i;
                break;
            }

        }

        if (start < 0) {

            start = 0;
        }

        return text.substring(start, end).trim();
    }

    protected void setExecutingQuery(String query) {

        int index = getText().indexOf(query);
        if (query.charAt(0) == Constants.NEW_LINE_CHAR) {

            index++;
        }

        lineBorder.setExecutingLine(getRowAt(index));
        lineBorder.repaint();
    }

    public String getTextAtRow(int rowNumber) {

        Element line = getElementMap().getElement(rowNumber);

        int startOffset = line.getStartOffset();
        int endOffset = line.getEndOffset();
        try {

            return getText(startOffset, (endOffset - startOffset));

        } catch (BadLocationException e) {

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Overrides <code>processKeyEvent</code> to additional process events.
     */
    protected void processKeyEvent(KeyEvent e) {

        if (e.getID() == KeyEvent.KEY_PRESSED) {

            int keyCode = e.getKeyCode();

            // add the processing for SHIFT-TAB
            if (e.isShiftDown() && keyCode == KeyEvent.VK_TAB) {

//                int currentPosition = getCurrentPosition();
                int selectionStart = getSelectionStart();
                int selectionEnd = getSelectionEnd();

                if (selectionStart == selectionEnd) {

                    int start = getCurrentRowStart();
                    int end = getCurrentRowEnd();

                    shiftTextLeft(start, end);

                    /*
                    int newPosition = currentPosition - QueryEditorSettings.getTabSize();
                    int currentRowPosition = getCurrentRowStart();

                    if (!isAtStartOfRow()) {

                        if (newPosition < 0) {

                            setCaretPosition(0);

                        } else if (newPosition < currentRowPosition) {

                            setCaretPosition(currentRowPosition);

                        } else {

                            setCaretPosition(newPosition);
                        }

                    }
                    */

                } else {

                    document.shiftTabEvent(selectionStart, selectionEnd);
                }

            } else if (keyCode == KeyEvent.VK_INSERT && e.getModifiers() == 0) {

                // toggle insert mode on the document

                int insertMode = document.getInsertMode();
                if (insertMode == SqlMessages.INSERT_MODE) {

                    document.setInsertMode(SqlMessages.OVERWRITE_MODE);
                    editorPanel.getStatusBar().setInsertionMode("OVR");

                } else {

                    document.setInsertMode(SqlMessages.INSERT_MODE);
                    editorPanel.getStatusBar().setInsertionMode("INS");
                }

            } else if (keyCode == KeyEvent.VK_SPACE) {
                checkForShortcutText();
            }

        }

        super.processKeyEvent(e);
    }

    private void checkForShortcutText() {

        int index = getCaretPosition();
        String word = getWordEndingAt(index).toUpperCase();

        word = removeBracesAtStart(word);
        if (editorShortcuts.containsKey(word)) {

            String text = editorShortcuts.get(word).getQuery();
            try {
                document.replace(index - word.length(), word.length(), text, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

    }

    private String removeBracesAtStart(String word) {

        int index = 0;
        String braces = "{}[]()";
        char[] chars = word.toCharArray();

        for (char c : chars) {

            if (braces.indexOf(c) != -1) {

                index++;

            } else {

                break;
            }

        }

        return word.substring(index);
    }

    public void editorShortcutsUpdated() {

        editorShortcuts.clear();
        loadEditorShortcuts();
    }

    private void loadEditorShortcuts() {

        List<EditorSQLShortcut> shortcuts = EditorSQLShortcuts.getInstance().getEditorShortcuts();
        for (EditorSQLShortcut editorSQLShortcut : shortcuts) {

            editorShortcuts.put(editorSQLShortcut.getShortcut(), editorSQLShortcut);
        }
    }


    /**
     * the last element count for line border updates
     */


    /**
     * Updates the line border values.
     */



    /**
     * Executes the undo action.
     */
    public void undo() {
        undoManager.undo();
        //updateLineBorder();
    }

    /**
     * Executes the redo action.
     */
    public void redo() {
        undoManager.redo();
        //updateLineBorder();
    }

    // ----------------------------------------
    // FocusListener implementation
    // ----------------------------------------

    /**
     * Updates the state of undo/redo ona focus gain.
     */
    public void focusGained(FocusEvent e) {
        if (e.getSource() != this && editorPanel != null) {
            editorPanel.focusGained();
        }
    }

    /**
     * Updates the state of undo/redo on a focus lost.
     */
    public void focusLost(FocusEvent e) {
        if (editorPanel != null) {
            editorPanel.focusLost();
        }
    }

    // ----------------------------------------

    private int currentRow = 0;
    private int currentPosition = 0;

    /**
     * Returns the row number at the specified position.
     *
     * @param position - the position
     */
    protected int getRowAt(int position) {
        Element map = getElementMap();
        return map.getElementIndex(position);
    }

    /**
     * Called when the caret position is updated.
     *
     * @param e the caret event
     */
    public void caretUpdate(CaretEvent ce) {

        currentPosition = getCaretPosition();

        Element map = getElementMap();
        int row = map.getElementIndex(currentPosition);

        if (currentRow != row) {
            currentRow = row;
        }

        Element lineElem = map.getElement(row);
        int col = currentPosition - lineElem.getStartOffset();
        editorPanel.getStatusBar().setCaretPosition(row + 1, col + 1);

        repaint();
    }

    /*
    private String getSelectedTextOrCurrentRow() {

        try {

            String text = getSelectedText();
            if (StringUtils.isBlank(text) ||
                    ((getSelectionStart() != getCurrentRowStart()
                            || getSelectionEnd() != getCurrentRowEnd()) && !text.contains("\n") ) ) {

                text = getCurrentRowText();
            }

            return StringUtils.trim(text);

        } catch (BadLocationException e) {}

        return "";
    }
    */

    public void moveSelectionUp() {

        try {

            int start = getStartOffsetAtSelectionOrCursor();
            if (start == 0) {

                return;
            }
            int end = getEndOffsetAtSelectionOrCursor();

            int previousRow = getRowAt(start) - 1;
            String previousRowText = getTextAtRow(previousRow);
            if (previousRowText == null) {

                return;
            }

            String textToMove = getText(start, (end - start));
            String textToInsert = textToMove + previousRowText;

            if (getRowAt(end) == getRowAt(end - 1)) {

                textToInsert = StringUtils.removeEnd(textToInsert, "\n");

            }

            int insertLength = textToInsert.length();
            int insertAt = getRowStartOffset(previousRow);
            if (insertAt + insertLength > getDocument().getLength()) {

                insertLength--;
            }

            getDocument().remove(insertAt, insertLength);
            insertTextAtOffset(insertAt, textToInsert);

            setSelectionStart(insertAt);
            setSelectionEnd(insertAt + textToMove.length());

        } catch (BadLocationException e) {

            return;
        }
    }

    public void moveSelectionDown() {

        try {

            int start = getStartOffsetAtSelectionOrCursor();
            int end = getEndOffsetAtSelectionOrCursor();

            int nextRow = getRowAt(end);
            String nextRowText = getTextAtRow(nextRow);
            if (nextRowText == null) {

                return;
            }

            String textToMove = StringUtils.removeEnd(getText(start, (end - start)), "\n");
            String textToInsert = nextRowText + textToMove;

            int insertLength = textToInsert.length();
            if (start + insertLength > getDocument().getLength()) {

                insertLength--;
            }

            getDocument().remove(start, insertLength);
            insertTextAtOffset(start, textToInsert);

            int selectionStart = start + nextRowText.length();
            setSelectionStart(selectionStart);
            setSelectionEnd(selectionStart + textToMove.length() + 1);

        } catch (BadLocationException e) {

            return;
        }
    }

    public void duplicateTextUp() {

        duplicateSelectionOrRowToOffset(DIRECTION_UP);
    }

    public void duplicateTextDown() {

        duplicateSelectionOrRowToOffset(DIRECTION_DOWN);
    }

    private int getStartOffsetAtSelectionOrCursor() {

        int row = currentRow;
        if (hasTextSelected()) {

            row = getRowAt(getSelectionStart());
        }
        return getRowStartOffset(row);
    }

    private int getEndOffsetAtSelectionOrCursor() {

        int row = getRowAt(getSelectionStart());
        if (hasTextSelected()) {

            row = getRowAt(getSelectionEnd() - 1);

        }
        return getRowEndOffset(row);
    }

    private int getLastRow() {

        return getRowAt(getElementMap().getEndOffset());
    }

    private void duplicateSelectionOrRowToOffset(int direction) {

        try {

            int start = getStartOffsetAtSelectionOrCursor();
            int end = getEndOffsetAtSelectionOrCursor();

            int offset = 0;
            int selectionOffset = 0;

            String insertText = getText(start, (end - start));
            insertText = StringUtils.removeEnd(insertText, "\n");

            if (direction == DIRECTION_UP) {

                insertText += "\n";
                offset = start;
                selectionOffset = offset;

            } else {

                insertText = "\n" + insertText;
                offset = end - 1;
                selectionOffset = offset + 1;

                if (StringUtils.isWhitespace(insertText) && getLastRow() == getRowAt(end)) {

                    return;
                }

            }

            insertTextAtOffset(offset, insertText);
            setCaretPosition(offset + insertText.length());
            moveCaretPosition(selectionOffset);

        } catch (Exception e) {
        }

    }

    private boolean hasTextSelected() {

        return getSelectionStart() != getSelectionEnd();
    }

    /*
    private String getCurrentRowText() throws BadLocationException {

        int start = getCurrentRowStart();
        int end = getCurrentRowEnd();

        return document.getText(start, end - start);
    }
    */

    protected boolean isAtStartOfRow() {
        return currentPosition == getRowPosition(currentRow);
    }

    /**
     * Returns the start offset of the current row.
     *
     * @return the current row start offset
     */
    protected int getCurrentRowStart() {
        return getElementMap().getElement(currentRow).getStartOffset();
    }

    /**
     * Returns the end offset of the current row.
     *
     * @return the current row end offset
     */
    protected int getCurrentRowEnd() {
        return getElementMap().getElement(currentRow).getEndOffset();
    }

    /**
     * Returns the start offset of the specified row.
     *
     * @param row - the row
     * @return the start offset of row
     */
    protected int getRowStartOffset(int row) {
        try {
            return getElementMap().getElement(row).getStartOffset();
        } catch (Exception e) { // where row passed is dumb value
            return -1;
        }
    }

    /**
     * Returns the end offset of the specified row.
     *
     * @param row - the row
     * @return the end offset of row
     */
    protected int getRowEndOffset(int row) {
        try {
            return getElementMap().getElement(row).getEndOffset();
        } catch (Exception e) { // where row passed is dumb value
            return -1;
        }
    }

    /**
     * Returns the start offset of the specified row.
     *
     * @param row - a row in the editor
     * @return the start offset of row
     */
    protected int getRowPosition(int row) {
        try {
            return getElementMap().getElement(row).getStartOffset();
        } catch (NullPointerException nullExc) { // TODO: WTF????
            return -1;
        }
    }

    /**
     * Returns the document's root element
     */
    protected Element getElementMap() {
        return getDocument().getDefaultRootElement();
    }

    /**
     * Returns the current caret position.
     */
    protected int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Returns the row number of the current cursor position.
     *
     * @return the current caret row number
     */
    protected int getCurrentCursorRow() {
        return currentRow;
    }




}


