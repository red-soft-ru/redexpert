/*
 * QueryEditorTextPanel.java
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
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.gui.text.TextUtilities;
import org.executequery.log.Log;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This object is the primary mediator between the parent
 * <code>QueryEditor</code> object and the actual Query Editor's
 * text pane - the <code>QueryEditorTextPane</code>. All text commands for the
 * text pane are propagated through here.
 *
 * @author Takis Diakoumis
 */
public class QueryEditorTextPanel extends JPanel {

    @SuppressWarnings("RegExpRedundantEscape")
    private static final String[] REGEX_CHARS = {
            "\\*", "\\^", "\\.", "\\[", "\\]", "\\(", "\\)",
            "\\?", "\\&", "\\{", "\\}", "\\+"
    };
    private static final String[] REGEX_SUBS = {
            "\\\\*", "\\\\^", "\\\\.", "\\\\[", "\\\\]", "\\\\(", "\\\\)",
            "\\\\?", "\\\\&", "\\\\{", "\\\\}", "\\\\+"
    };
    private static final String SQL_COMMENT_REGEX = "^\\s*--";
    private static final String SQL_COMMENT = "--";

    private static Insets borderInsets;
    private static Color borderColour;

    /**
     * The SQL text pane
     */
    private QueryEditorTextPane queryPane;

    /**
     * The SQL text pane scroll pane
     */
    private RTextScrollPane queryScroll;

    /**
     * The editor's controller
     */
    private QueryEditor queryEditor;

    /**
     * The pattern matcher to check for comments to be removed
     */
    private Matcher sqlCommentMatcher;

    public QueryEditorTextPanel(QueryEditor queryEditor) {
        super(new BorderLayout());

        this.queryEditor = queryEditor;

        try {
            init();

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    private void init() {

        queryPane = new QueryEditorTextPane(this);
        queryPane.setSize(new Dimension(400, 300));

        queryScroll = new RTextScrollPane(queryPane);
        queryScroll.setLineNumbersEnabled(true);
        queryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        queryScroll.setBorder(new EditorScrollerBorder());

        add(queryScroll, BorderLayout.CENTER);
    }


    public void addEditorPaneMouseListener(MouseListener listener) {
        queryPane.addMouseListener(listener);
    }

    public void setTextPaneBackground(Color c) {
        queryPane.setBackground(c);
    }

    public void setSQLKeywords(boolean reset) {
        queryPane.setSQLKeywords(reset);
    }

    protected ActionMap getTextPaneActionMap() {
        return queryPane.getActionMap();
    }

    protected InputMap getTextPaneInputMap() {
        return queryPane.getInputMap();
    }

    /**
     * Indicates that the editor is closing and performs some cleanup.
     */
    protected void closingEditor() {
        queryPane.cleanup();
        queryEditor = null;
    }

    public void showLineNumbers(boolean show, Font font) {
        queryScroll.setLineNumbersEnabled(show);
        if (show) {
            queryScroll.getGutter().setLineNumberFont(font);
            queryScroll.getGutter().setBackground(SystemProperties.getColourProperty("user", "editor.linenumber.background"));
            queryScroll.getGutter().setLineNumberColor(SystemProperties.getColourProperty("user", "editor.linenumber.foreground"));
            queryScroll.getGutter().setCurrentLineNumberColor(SystemProperties.getColourProperty("user", "editor.text.selection.background"));
        }
    }

    protected void setTextFocus() {
        GUIUtils.requestFocusInWindow(queryPane);
    }

    /**
     * Enters the specified text at the editor's current
     * insertion point.
     */
    public void insertTextAtCaret(String text) {
        queryPane.replaceSelection(text);
        setTextFocus();
    }

    public SQLTextArea getQueryArea() {
        return queryPane;
    }

    public void focusLost() {
        if (queryEditor != null)
            queryEditor.focusLost();
    }

    public void focusGained() {
        if (queryEditor != null)
            queryEditor.focusGained();
    }

    /**
     * Sets the editor's text content that specified.
     *
     * @param s - the text to be set
     */
    public void setQueryAreaText(String s) {

        try {
            queryPane.uninstallListeners();
            queryPane.setText(s);

        } finally {
            queryPane.reinstallListeners();
        }
    }

    public void insertTextAfter(int after, String text) {
        queryPane.insertTextAfter(after, text);
    }

    /**
     * Loads the specified text into a blank 'offscreen' document
     * before switching to the SQL document.
     */
    public void loadText(String text) {
        queryPane.loadText(text);
    }

    public QueryEditorStatusBar getStatusBar() {
        return queryEditor.getStatusBar();
    }

    public void disableUpdates(boolean disable) {
        queryPane.disableUpdates(disable);
    }

    public void preferencesChanged() {
        queryPane.resetAttributeSets();
    }

    /**
     * Moves the caret to the beginning of the specified query.
     *
     * @param query the query to move the cursor to
     */
    public void caretToQuery(String query) {

        // replace any regex control chars
        for (int i = 0; i < REGEX_CHARS.length; i++)
            query = query.replaceAll(REGEX_CHARS[i], REGEX_SUBS[i]);

        Matcher matcher = Pattern.compile(query, Pattern.DOTALL).matcher(queryPane.getText());
        if (matcher.find()) {

            int index = matcher.start();
            if (index != -1)
                queryPane.setCaretPosition(index);
        }

        GUIUtils.requestFocusInWindow(queryPane);
    }

    /**
     * Returns the currently selected text, or null if not text
     * is currently selected.
     */
    public String getSelectedText() {

        String selection = queryPane.getSelectedText();
        if (StringUtils.isNotBlank(selection))
            return selection;

        return null;
    }

    public void replaceRegion(int start, int end, String replacement) {
        queryPane.select(start, end);
        queryPane.replaceSelection(replacement);
    }

    public int getSelectionStart() {
        return queryPane.getSelectionStart();
    }

    public int getSelectionEnd() {
        return queryPane.getSelectionEnd();
    }

    public void goToRow(int row) {
        queryPane.goToRow(row);
    }

    /**
     * Sets the table results to the specified
     * <code>ResultSet</code> object for display.
     *
     * @param resultSet the table results to display
     * @param query     the executed query of the result set
     */
    public void setResultSet(ResultSet resultSet, String query, DatabaseConnection dc) throws SQLException {
        queryEditor.setResultSet(resultSet, query, dc);
    }

    public String getQueryAreaText() {
        return queryPane.getText();
    }

    private void addCommentToRows(int startRow, int endRow) {

        Matcher matcher = sqlCommentMatcher();
        for (int i = startRow; i <= endRow; i++) {

            matcher.reset(queryPane.getTextAtRow(i));
            if (!matcher.find())
                queryPane.insertTextAtOffset(queryPane.getRowStartOffset(i), SQL_COMMENT);
        }
    }

    private void removeCommentFromRows(int startRow, int endRow) throws BadLocationException {

        Document document = queryPane.getDocument();
        Matcher matcher = sqlCommentMatcher();

        for (int i = startRow; i <= endRow; i++) {

            String text = queryPane.getTextAtRow(i);
            matcher.reset(text);

            if (matcher.find()) {

                int index = text.indexOf(SQL_COMMENT);
                int startOffset = queryPane.getRowPosition(i);

                document.remove(startOffset + index, 2);
            }
        }
    }

    /**
     * Adds a comment tag to the beginning of the current line
     * or selected lines.
     */
    public void commentLines() {

        int selectionStart = queryPane.getSelectionStart();
        int selectionEnd = queryPane.getSelectionEnd();

        boolean singleRow = (selectionStart == selectionEnd);

        int startRow = queryPane.getRowAt(selectionStart);
        int endRow = queryPane.getRowAt(selectionEnd);

        int endRowStartIndex = queryPane.getRowStartOffset(endRow);
        if (!singleRow && selectionEnd == endRowStartIndex)
            endRow--;

        try {

            if (rowsHaveComments(startRow, endRow, true)) {
                removeCommentFromRows(startRow, endRow);

            } else if (rowsHaveComments(startRow, endRow, false)) {

                if (singleRow)
                    removeCommentFromRows(startRow, endRow);
                else
                    addCommentToRows(startRow, endRow);

            } else
                addCommentToRows(startRow, endRow);

        } catch (BadLocationException e) {
            Log.error(e.getMessage(), e);
        }

        if (!singleRow) {
            queryPane.setSelectionStart(queryPane.getRowStartOffset(startRow));
            queryPane.setSelectionEnd(queryPane.getRowEndOffset(endRow));
        }
    }

    private Matcher sqlCommentMatcher() {

        if (sqlCommentMatcher == null)
            sqlCommentMatcher = Pattern.compile(SQL_COMMENT_REGEX).matcher("");

        return sqlCommentMatcher;
    }

    private boolean rowsHaveComments(int startRow, int endRow, boolean allRows) {

        Matcher matcher = sqlCommentMatcher();
        for (int i = startRow; i <= endRow; i++) {

            matcher.reset(queryPane.getTextAtRow(i));
            if (matcher.find()) {
                if (!allRows)
                    return true;

            } else if (allRows)
                return false;
        }

        return allRows;
    }

    /**
     * Shifts the text on the current line or the currently
     * selected text to the right one TAB.
     */
    public void shiftTextRight() {

        if (getSelectedText() == null) {

            int start = queryPane.getCurrentRowStart();
            queryPane.shiftTextRight(start);

        } else {
            try {

                // simulate a tab key for selected text
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);

            } catch (AWTException e) {
                Log.debug("Error simulating tab key events", e);
            }
        }
    }

    public void moveSelectionUp() {
        queryPane.moveSelectionUp();
    }

    public void moveSelectionDown() {
        queryPane.moveSelectionDown();
    }

    public void duplicateRowUp() {
        queryPane.duplicateTextUp();
    }

    public void duplicateRowDown() {
        queryPane.duplicateTextDown();
    }

    /**
     * Shifts the text on the current line or the currently
     * selected text to the left one TAB.
     */
    public void shiftTextLeft() {

        if (getSelectedText() == null) {

            int start = queryPane.getCurrentRowStart();
            int end = queryPane.getCurrentRowEnd();
            queryPane.shiftTextLeft(start, end);

        } else {
            try {

                // simulate a tab key for selected text
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_SHIFT);

            } catch (AWTException e) {
                Log.debug("Error simulating tab key events", e);
            }
        }
    }

    // -----------------------------------
    // --- TextFunction implementation ---
    // -----------------------------------

    public void paste() {
        queryPane.paste();
    }

    public void copy() {
        queryPane.copy();
    }

    public void cut() {
        queryPane.cut();
    }

    public void selectAll() {
        TextUtilities.selectAll(queryPane);
    }

    public void selectNone() {
        TextUtilities.selectNone(queryPane);
        queryEditor.focusGained();
    }

    public void changeSelectionToCamelCase() {
        TextUtilities.changeSelectionToCamelCase(queryPane);
    }

    public void changeSelectionToUnderscore() {
        TextUtilities.changeSelectionToUnderscore(queryPane);
    }

    public void changeSelectionCase(boolean upper) {
        TextUtilities.changeSelectionCase(queryPane, upper);
    }

    public void deleteLine() {
        TextUtilities.deleteLine(queryPane);
    }

    public void deleteWord() {
        TextUtilities.deleteWord(queryPane);
    }

    public void deleteSelection() {
        TextUtilities.deleteSelection(queryPane);
    }

    public void insertFromFile() {
        TextUtilities.insertFromFile(queryPane);
    }

    public void insertLineAfter() {
        TextUtilities.insertLineAfter(queryPane);
    }

    public void insertLineBefore() {
        TextUtilities.insertLineBefore(queryPane);
    }

    // -----------------------------------

    /**
     * Propagates the call to the parent QueryEditor object
     * that the text content has been altered from the original
     * or previously saved state.
     *
     * @param contentChanged true | false
     */
    public void setContentChanged(boolean contentChanged) {
        queryEditor.setContentChanged(contentChanged);
    }

    public void editorShortcutsUpdated() {
        queryPane.editorShortcutsUpdated();
    }

    private static class EditorScrollerBorder implements Border {

        protected EditorScrollerBorder() {

            if (borderInsets == null)
                borderInsets = new Insets(0, 0, 0, 0);

            if (borderColour == null)
                borderColour = GUIUtilities.getDefaultBorderColour();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return borderInsets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(borderColour);
            g.drawLine(x, height - 1, width, height - 1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

    } // EditorScrollerBorder class

}
