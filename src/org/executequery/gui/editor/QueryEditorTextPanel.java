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
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.TreeFindAction;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.gui.editor.autocomplete.AutoCompletePopupProvider;
import org.executequery.gui.text.TextUtilities;
import org.executequery.log.Log;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
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

    private static final String SQL_COMMENT_REGEX = "^\\s*--";

    private static final String SQL_COMMENT = "--";

    /**
     * The SQL text pane
     */
    private QueryEditorTextPane queryPane;

    /**
     * The editor's controller
     */
    private QueryEditor queryEditor;

    private static final String AUTO_COMPLETE_POPUP_ACTION_KEY = "autoCompletePopupActionKey";

    private AutoCompletePopupProvider autoCompletePopup;

    /**
     * Constructs a new instance.
     */
    public QueryEditorTextPanel(QueryEditor queryEditor) {

        super(new BorderLayout());

        this.queryEditor = queryEditor;

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    /**
     * Initializes the state of this instance.
     */
    private void init() {

        // setup the query text panel and associated scroller
        queryPane = new QueryEditorTextPane(this);
        queryPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1 || e.isControlDown()) {
                    int cursor = queryPane.getCurrentPosition();
                    String s = queryPane.getSQLSyntaxDocument().getNameDBObjectFromPosition(cursor, queryPane.getText());
                    if (s != null) {
                        s = s.replace("$", "\\$");
                        TreeFindAction action = new TreeFindAction();
                        SchemaTree tree = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getTree();
                        action.install(tree);
                        action.findString(tree, s, ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(queryEditor.getSelectedConnection()));
                        BaseDialog dialog = new BaseDialog("find", false);
                        JPanel panel = new JPanel();
                        JList jList = action.getResultsList();
                        if (jList.getModel().getSize() == 1) {
                            jList.setSelectedIndex(0);
                            action.listValueSelected((TreePath) jList.getSelectedValue());
                        } else {
                            jList.addPropertyChangeListener(new PropertyChangeListener() {
                                @Override
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if (jList.getModel().getSize() == 0)
                                        dialog.finished();
                                }
                            });
                            JScrollPane scrollPane = new JScrollPane(jList);
                            panel.add(scrollPane);
                            dialog.addDisplayComponent(panel);
                            dialog.display();
                        }
                    }
                }
            }
        });

        JScrollPane queryScroller = new JScrollPane();
        queryScroller.getViewport().add(queryPane, BorderLayout.CENTER);
        queryScroller.setRowHeaderView(queryPane.getLineBorder());
        queryScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        queryScroller.setBorder(new EditorScrollerBorder());

        add(queryScroller, BorderLayout.CENTER);
    }

    public void deregisterAutoCompletePopup() {

        if (autoCompletePopup != null) {

            Action autoCompletePopupAction = autoCompletePopup.getPopupAction();

            queryPane.getActionMap().remove(AUTO_COMPLETE_POPUP_ACTION_KEY);
            queryPane.getInputMap().remove((KeyStroke)
                    autoCompletePopupAction.getValue(Action.ACCELERATOR_KEY));

            autoCompletePopup = null;
        }

    }

    boolean changed = false;


    public void registerAutoCompletePopup(AutoCompletePopupProvider autoCompletePopup) {

        this.autoCompletePopup = autoCompletePopup;

        Action autoCompletePopupAction = autoCompletePopup.getPopupAction();

        queryPane.getActionMap().put(AUTO_COMPLETE_POPUP_ACTION_KEY, autoCompletePopupAction);
        queryPane.getInputMap().put((KeyStroke)
                        autoCompletePopupAction.getValue(Action.ACCELERATOR_KEY),
                AUTO_COMPLETE_POPUP_ACTION_KEY);
        queryPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {

            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed = true;
            }
        });
        queryPane.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (changed)
                    autoCompletePopupAction.actionPerformed(null);
                changed = false;
            }
        });

    }

    public void addEditorPaneMouseListener(MouseListener listener) {

        queryPane.addMouseListener(listener);
    }

    public boolean isLogEnabled() {

        return OutputLogger.isLogEnabled();
    }

    public void log(String message) {

        if (isLogEnabled()) {

            OutputLogger.info(message);
        }

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

        queryEditor = null;
    }

    public void showLineNumbers(boolean show) {

        queryPane.showLineNumbers(show);
    }

    protected void setTextFocus() {

        GUIUtils.requestFocusInWindow(queryPane);
    }

    /**
     * Resets the executing line within the line
     * number border panel.
     */
    public void resetExecutingLine() {

        queryPane.resetExecutingLine();
    }

    /**
     * Resets the text pane's caret position to zero.
     */
    public void resetCaretPosition() {

        queryPane.setCaretPosition(0);
    }

    /**
     * Enters the specified text at the editor's current
     * insertion point.
     *
     * @param text - the text to insert
     */
    public void insertTextAtCaret(String text) {

        queryPane.replaceSelection(text);
        setTextFocus();
    }

    public JTextPane getQueryArea() {

        return queryPane;
    }

    public void selectAll() {

        TextUtilities.selectAll(queryPane);
    }

    public void selectNone() {

        TextUtilities.selectNone(queryPane);

        queryEditor.focusGained();
    }

    public void focusLost() {

        if (queryEditor != null) {

            queryEditor.focusLost();
        }
    }

    public void focusGained() {

        if (queryEditor != null) {

            queryEditor.focusGained();
        }
    }

    public void commitModeChanged(boolean autoCommit) {

        queryEditor.commitModeChanged(autoCommit);
    }

    /**
     * Sets the editor's text content that specified.
     *
     * @param s - the text to be set
     */
    public void setQueryAreaText(String s) {

        try {

            // uninstall listeners on the text pane
            queryPane.uninstallListeners();

            // clear the current held edits
//            queryPane.clearEdits();

            // set the text
            queryPane.setText(s);

        } finally {

            // reinstall listeners on the text pane
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

    public void disableCaretUpdate(boolean disable) {

        queryPane.disableCaretUpdate(disable);
    }

    public void preferencesChanged() {

        queryPane.resetAttributeSets();
    }

    // -----------------------------------
    // regex replacement arrays
    // -----------------------------------

    private static final String[] REGEX_CHARS = {
            "\\*", "\\^", "\\.", "\\[", "\\]", "\\(", "\\)",
            "\\?", "\\&", "\\{", "\\}", "\\+"};

    private static final String[] REGEX_SUBS = {
            "\\\\*", "\\\\^", "\\\\.", "\\\\[", "\\\\]", "\\\\(", "\\\\)",
            "\\\\?", "\\\\&", "\\\\{", "\\\\}", "\\\\+"};

    /**
     * Moves the caret to the beginning of the specified query.
     *
     * @param query - the query to move the cursor to
     */
    public void caretToQuery(String query) {

        // replace any regex control chars
        for (int i = 0; i < REGEX_CHARS.length; i++) {

            query = query.replaceAll(REGEX_CHARS[i], REGEX_SUBS[i]);
        }

        Matcher matcher = Pattern.compile(query, Pattern.DOTALL).
                matcher(queryPane.getText());

        if (matcher.find()) {

            int index = matcher.start();

            if (index != -1) {

                queryPane.setCaretPosition(index);
            }
        }

        matcher = null;

        GUIUtils.requestFocusInWindow(queryPane);
    }

    /**
     * Returns the currently selected text, or null if not text
     * is currently selected.
     *
     * @return selected text
     */
    public String getSelectedText() {

        String selection = queryPane.getSelectedText();
        if (StringUtils.isNotBlank(selection)) {

            return selection;
        }

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

    public String getCompleteWordEndingAtCursor() {

        return queryPane.getCompleteWordEndingAtCursor();
    }

    protected String getWordToCursor() {

        return queryPane.getWordEndingAtCursor();
    }

    protected QueryWithPosition getQueryAtCursor() {

        return queryPane.getQueryAtCursor();
    }

    protected void setExecutingQuery(String query) {

        queryPane.setExecutingQuery(query);
    }

    public void goToRow(int row) {

        queryPane.goToRow(row);
    }

    public void destroyTable() {

        queryEditor.destroyTable();
    }

    /**
     * Sets the table results to the specified
     * <code>ResultSet</code> object for display.
     *
     * @param rset  the table results to display
     * @param query the executed query of the result set
     */
    public void setResultSet(ResultSet rset, String query) {

        queryEditor.setResultSet(rset, query);
    }

    public void setResult(int updateCount, int type) {

        queryEditor.setResultText(updateCount, type);
    }

    public String getQueryAreaText() {

        return queryPane.getText();
    }

    private void addCommentToRows(int startRow, int endRow) {

        Matcher matcher = sqlCommentMatcher();
        for (int i = startRow; i <= endRow; i++) {

            String text = queryPane.getTextAtRow(i);
            matcher.reset(text);

            if (!matcher.find()) {

                int index = queryPane.getRowStartOffset(i);
                queryPane.insertTextAtOffset(index, SQL_COMMENT);
            }

        }

    }

    private void removeCommentFromRows(int startRow, int endRow) throws BadLocationException {

        Document document = queryPane.getDocument();

        Matcher matcher = sqlCommentMatcher();

        for (int i = startRow; i <= endRow; i++) {

            String text = queryPane.getTextAtRow(i);

            matcher.reset(text);

            if (matcher.find()) {

                // retrieve the exact index of '--' since
                // matcher will return first whitespace

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
        if (!singleRow && selectionEnd == endRowStartIndex) {

            endRow--;
        }

        try {

            if (rowsHaveComments(startRow, endRow, true)) {

                removeCommentFromRows(startRow, endRow);

            } else if (rowsHaveComments(startRow, endRow, false)) {

                if (singleRow) {

                    removeCommentFromRows(startRow, endRow);

                } else {

                    // if any one row of a multi-row selection has
                    // a comment, comment the rest also

                    addCommentToRows(startRow, endRow);
                }

            } else {

                addCommentToRows(startRow, endRow);
            }

        } catch (BadLocationException e) {

            // nothing we can do here

            e.printStackTrace();
        }

        if (!singleRow) {

            queryPane.setSelectionStart(queryPane.getRowStartOffset(startRow));
            queryPane.setSelectionEnd(queryPane.getRowEndOffset(endRow));
        }

    }

    /**
     * pattern matcher to check for comments to be removed
     */
    private Matcher sqlCommentMatcher;

    private Matcher sqlCommentMatcher() {

        if (sqlCommentMatcher == null) {

            sqlCommentMatcher = Pattern.compile(SQL_COMMENT_REGEX).matcher("");
        }

        return sqlCommentMatcher;
    }

    private boolean rowsHaveComments(int startRow, int endRow, boolean allRows) {

        Matcher matcher = sqlCommentMatcher();

        for (int i = startRow; i <= endRow; i++) {

            String text = queryPane.getTextAtRow(i);

            matcher.reset(text);

            if (matcher.find()) {

                if (!allRows) {

                    return true;

                }

            } else if (allRows) {

                return false;
            }

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

        } else { // simulate a tab key for selected text

            try {

                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);

            } catch (AWTException e) {

                e.printStackTrace();
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

        } else { // simulate a tab key for selected text

            try {

                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_SHIFT);

            } catch (AWTException e) {

                if (Log.isDebugEnabled()) {

                    Log.error("Error simulating tab key events", e);
                }

            }

        }
    }

    // ---------------------------------------------
    // TextFunction implementation
    // ---------------------------------------------

    public void paste() {
        queryPane.paste();
    }

    public void copy() {
        queryPane.copy();
    }

    public void cut() {
        queryPane.cut();
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

    // ---------------------------------------------

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

    private static Insets borderInsets;
    private static Color borderColour;

    private class EditorScrollerBorder implements Border {

        protected EditorScrollerBorder() {
            if (borderInsets == null) {
                borderInsets = new Insets(0, 0, 0, 0);
            }
            if (borderColour == null) {
                borderColour = GUIUtilities.getDefaultBorderColour();
            }
        }

        public Insets getBorderInsets(Component c) {
            return borderInsets;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(borderColour);
            g.drawLine(x, height - 1, width, height - 1);
        }

        public boolean isBorderOpaque() {
            return false;
        }

    }

    public void editorShortcutsUpdated() {

        queryPane.editorShortcutsUpdated();
    }

}



