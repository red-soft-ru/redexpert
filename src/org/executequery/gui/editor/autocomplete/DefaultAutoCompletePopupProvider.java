package org.executequery.gui.editor.autocomplete;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.editor.ConnectionChangeListener;
import org.executequery.gui.editor.QueryWithPosition;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.QueryTable;
import org.executequery.util.UserProperties;
import org.fife.ui.rsyntaxtextarea.Token;
import org.underworldlabs.sqlLexer.CustomToken;
import org.underworldlabs.sqlLexer.SqlLexer;
import org.underworldlabs.sqlLexer.SqlLexerTokenMaker;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

public class DefaultAutoCompletePopupProvider
        implements AutoCompletePopupProvider,
        AutoCompletePopupListener,
        ConnectionChangeListener,
        FocusListener {

    private static final int RESET_COUNT_THRESHOLD = 20;
    private static final int MINIMUM_CHARS_FOR_DATABASE_LOOKUP = 2;

    enum ListScrollType {
        UP, DOWN, PAGE_DOWN, PAGE_UP
    }

    private static final KeyStroke KEY_STROKE_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    private static final KeyStroke KEY_STROKE_TAB = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
    private static final KeyStroke KEY_STROKE_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    private static final KeyStroke KEY_STROKE_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final KeyStroke KEY_STROKE_PAGE_UP = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
    private static final KeyStroke KEY_STROKE_PAGE_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);

    private static final String LIST_FOCUS_ACTION_KEY = "focusActionKey";
    private static final String LIST_SCROLL_ACTION_KEY_UP = "scrollActionKeyUp";
    private static final String LIST_SELECTION_ACTION_KEY = "selectionActionKey";
    private static final String LIST_SCROLL_ACTION_KEY_DOWN = "scrollActionKeyDown";
    private static final String LIST_SCROLL_ACTION_KEY_PAGE_UP = "scrollActionKeyPageUp";
    private static final String LIST_SCROLL_ACTION_KEY_PAGE_DOWN = "scrollActionKeyPageDown";

    private final SQLTextArea sqlTextPane;
    private final AutoCompleteSelectionsFactory selectionsFactory;
    private final AutoCompleteListItemComparator autoCompleteListItemComparator;

    private final AbstractAction listFocusAction;
    private final AbstractAction listSelectionAction;
    private final ListScrollAction listScrollActionUp;
    private final ListScrollAction listScrollActionDown;
    private final ListScrollAction listScrollActionPageUp;
    private final ListScrollAction listScrollActionPageDown;
    private final AutoCompletePopupAction autoCompletePopupAction;

    private Object existingKeyStrokeUpAction;
    private Object existingKeyStrokeTabAction;
    private Object existingKeyStrokeDownAction;
    private Object existingKeyStrokeEnterAction;
    private Object existingKeyStrokePageUpAction;
    private Object existingKeyStrokePageDownAction;

    private SwingWorker worker;
    private DatabaseConnection connection;
    private DefaultDatabaseHost databaseHost;
    private List<AutoCompleteListItem> autoCompleteListItems;
    private QueryEditorAutoCompletePopupPanel autoCompletePopup;
    private AutoCompleteListItem noProposalsAutoCompleteListItem;
    private AutoCompleteListItem buildingProposalsAutoCompleteListItem;

    private int resetCount;
    private boolean addingQuote;
    private boolean noProposals;
    private boolean rebuildingList;
    private boolean editorActionsSaved;
    private final boolean autoCompleteSchema;
    private final boolean autoCompleteKeywords;

    public DefaultAutoCompletePopupProvider(DatabaseConnection connection, SQLTextArea sqlTextPane) {
        super();
        this.addingQuote = false;
        this.noProposals = false;
        this.connection = connection;
        this.sqlTextPane = sqlTextPane;

        autoCompleteSchema = UserProperties.getInstance().getBooleanProperty("editor.autocomplete.schema.on");
        autoCompleteKeywords = UserProperties.getInstance().getBooleanProperty("editor.autocomplete.keywords.on");

        listFocusAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPopupActionsBlocked())
                    focusAndSelectList();
            }
        };

        listSelectionAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isPopupActionsBlocked())
                    popupSelectionMade();
            }
        };

        autoCompletePopupAction = new AutoCompletePopupAction(this);
        listScrollActionUp = new ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.UP);
        listScrollActionDown = new ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.DOWN);
        listScrollActionPageUp = new ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.PAGE_UP);
        listScrollActionPageDown = new ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.PAGE_DOWN);

        autoCompleteListItems = new ArrayList<>();
        selectionsFactory = new AutoCompleteSelectionsFactory(this);
        autoCompleteListItemComparator = new AutoCompleteListItemComparator();

        sqlTextPane.addFocusListener(this);
    }

    public void reset() {
        connectionChanged(databaseHost.getDatabaseConnection());
    }

    private QueryEditorAutoCompletePopupPanel popupMenu() {

        if (autoCompletePopup != null)
            return autoCompletePopup;

        autoCompletePopup = new QueryEditorAutoCompletePopupPanel();
        autoCompletePopup.addAutoCompletePopupListener(this);
        autoCompletePopup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                popupHidden();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                popupHidden();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
        });

        return autoCompletePopup;
    }

    public boolean isShow() {
        return autoCompletePopup != null && autoCompletePopup.isVisible();
    }

    private void focusAndSelectList() {
        autoCompletePopup.focusAndSelectList();
    }

    private void addFocusActions() {

        ActionMap actionMap = sqlTextPane.getActionMap();
        actionMap.put(LIST_FOCUS_ACTION_KEY, listFocusAction);
        actionMap.put(LIST_SCROLL_ACTION_KEY_DOWN, listScrollActionDown);
        actionMap.put(LIST_SCROLL_ACTION_KEY_UP, listScrollActionUp);
        actionMap.put(LIST_SELECTION_ACTION_KEY, listSelectionAction);
        actionMap.put(LIST_SCROLL_ACTION_KEY_PAGE_DOWN, listScrollActionPageDown);
        actionMap.put(LIST_SCROLL_ACTION_KEY_PAGE_UP, listScrollActionPageUp);

        InputMap inputMap = sqlTextPane.getInputMap();
        saveExistingActions(inputMap);

        inputMap.put(KEY_STROKE_DOWN, LIST_SCROLL_ACTION_KEY_DOWN);
        inputMap.put(KEY_STROKE_UP, LIST_SCROLL_ACTION_KEY_UP);
        inputMap.put(KEY_STROKE_PAGE_DOWN, LIST_SCROLL_ACTION_KEY_PAGE_DOWN);
        inputMap.put(KEY_STROKE_PAGE_UP, LIST_SCROLL_ACTION_KEY_PAGE_UP);
        inputMap.put(KEY_STROKE_TAB, LIST_FOCUS_ACTION_KEY);
        inputMap.put(KEY_STROKE_ENTER, LIST_SELECTION_ACTION_KEY);
    }

    private void saveExistingActions(InputMap inputMap) {

        if (editorActionsSaved)
            return;

        existingKeyStrokeDownAction = inputMap.get(KEY_STROKE_DOWN);
        existingKeyStrokeUpAction = inputMap.get(KEY_STROKE_UP);
        existingKeyStrokePageDownAction = inputMap.get(KEY_STROKE_PAGE_DOWN);
        existingKeyStrokePageUpAction = inputMap.get(KEY_STROKE_PAGE_UP);
        existingKeyStrokeEnterAction = inputMap.get(KEY_STROKE_ENTER);
        existingKeyStrokeTabAction = inputMap.get(KEY_STROKE_TAB);

        editorActionsSaved = true;
    }

    private boolean isPopupActionsBlocked() {

        if (popupMenu().isVisible())
            return false;

        resetEditorActions();
        return true;
    }

    private void resetEditorActions() {

        ActionMap actionMap = sqlTextPane.getActionMap();
        actionMap.remove(LIST_FOCUS_ACTION_KEY);
        actionMap.remove(LIST_SELECTION_ACTION_KEY);
        actionMap.remove(LIST_SCROLL_ACTION_KEY_DOWN);
        actionMap.remove(LIST_SCROLL_ACTION_KEY_UP);

        InputMap inputMap = sqlTextPane.getInputMap();
        inputMap.remove(KEY_STROKE_DOWN);
        inputMap.remove(KEY_STROKE_UP);
        inputMap.remove(KEY_STROKE_PAGE_DOWN);
        inputMap.remove(KEY_STROKE_PAGE_UP);
        inputMap.remove(KEY_STROKE_ENTER);
        inputMap.remove(KEY_STROKE_TAB);

        inputMap.put(KEY_STROKE_DOWN, existingKeyStrokeDownAction);
        inputMap.put(KEY_STROKE_UP, existingKeyStrokeUpAction);
        inputMap.put(KEY_STROKE_PAGE_DOWN, existingKeyStrokePageDownAction);
        inputMap.put(KEY_STROKE_PAGE_UP, existingKeyStrokePageUpAction);
        inputMap.put(KEY_STROKE_TAB, existingKeyStrokeTabAction);
        inputMap.put(KEY_STROKE_ENTER, existingKeyStrokeEnterAction);
    }

    private void popupHidden() {
        resetEditorActions();
        sqlTextPane.requestFocus();
    }

    private String getWordEndingAt(int position) {
        addingQuote = false;

        String text = sqlTextPane.getText();
        if (MiscUtils.isNull(text))
            return Constants.EMPTY;

        char[] chars = text.toCharArray();

        int start = -1;
        for (int i = position - 1; i >= 0; i--) {
            if (isNotLetterChar(chars[i])) {
                start = i;
                break;
            }
        }

        if (start < 0)
            start = 0;

        if (chars[start] == '"') {
            addingQuote = true;
            start++;

        } else if (start != 0 && !Character.isSpaceChar(chars[start]))
            start++;

        if (start >= position)
            return "";

        return text.substring(start, position).trim();
    }

    private boolean isNotLetterChar(char ch) {
        return !Character.isLetterOrDigit(ch) && ch != '_' && ch != '.' && ch != '$';
    }

    private QueryWithPosition getQueryAt(int position) {

        String text = sqlTextPane.getText();
        if (MiscUtils.isNull(text))
            return new QueryWithPosition(0, 0, 0, Constants.EMPTY);

        char[] chars = text.toCharArray();
        if (position == chars.length)
            position--;

        int end = -1;
        int start = -1;
        boolean wasSpaceChar = false;

        // determine the start point
        for (int i = position; i >= 0; i--) {
            if (chars[i] == Constants.NEW_LINE_CHAR) {

                if (i == 0 || wasSpaceChar)
                    break;

                if (start != -1) {

                    if (chars[i - 1] == Constants.NEW_LINE_CHAR)
                        break;

                    if (Character.isSpaceChar(chars[i - 1])) {
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
                    if (end == -1)
                        end = i;
                    break;
                }

                if (end != -1) {

                    if (chars[i + 1] == Constants.NEW_LINE_CHAR)
                        break;

                    if (Character.isSpaceChar(chars[i + 1])) {
                        wasSpaceChar = true;
                        i++;
                    }
                }

            } else if (!Character.isSpaceChar(chars[i])) {
                end = i;
                wasSpaceChar = false;
            }
        }

        String query = text.substring(start, end + 1);
        if ((MiscUtils.isNull(query) && start != 0))
            return getQueryAt(start);

        return new QueryWithPosition(position, start, end + 1, query);
    }

    private List<AutoCompleteListItem> itemsStartingWith(String prefix) {

        if (StringUtils.isBlank(prefix))
            return new ArrayList<>();

        List<QueryTable> tables = new ArrayList<>();
        List<AutoCompleteListItem> searchList = autoCompleteListItems;

        String tableString = "";
        String wordPrefix = prefix.trim().toUpperCase();
        int dotIndex = prefix.indexOf('.');

        boolean hasDotIndex = (dotIndex != -1);
        if (hasDotIndex) {

            tableString = wordPrefix.substring(0, dotIndex);
            tableString = tableString.replace("(", "");
            wordPrefix = wordPrefix.substring(dotIndex + 1);

            DerivedQuery derivedQuery = new DerivedQuery(getQueryAt(sqlTextPane.getCaretPosition()).getQuery());
            tables = getQueryTables(derivedQuery.getDerivedQuery());

        } else if (wordPrefix.length() < MINIMUM_CHARS_FOR_DATABASE_LOOKUP) {
            return buildItemsStartingWithForList(
                    selectionsFactory.buildKeywords(databaseHost, autoCompleteKeywords),
                    tables,
                    wordPrefix,
                    false
            );
        }

        // try to get columns for table
        if (hasDotIndex) {

            List<AutoCompleteListItem> itemsForTable = selectionsFactory.buildItemsForTable(databaseHost, tableString.toUpperCase());
            if (!itemsForTable.isEmpty())
                itemsForTable = buildItemsStartingWithForList(itemsForTable, tables, wordPrefix, hasDotIndex);

            if (!itemsForTable.isEmpty())
                return itemsForTable;
        }

        // maybe alias?
        if (hasDotIndex) {

            String tableFromAlias = null;
            if ((tableString.equalsIgnoreCase("new") || tableString.equalsIgnoreCase("old")) && sqlTextPane.getTriggerTable() != null) {
                tableFromAlias = sqlTextPane.getTriggerTable();

            } else {
                for (QueryTable queryTable : tables) {
                    if (queryTable.getAlias().equalsIgnoreCase(tableString)) {
                        tableFromAlias = queryTable.getName();
                        break;
                    }
                }
            }

            if (tableFromAlias != null) {
                List<AutoCompleteListItem> itemsForTable = selectionsFactory.buildItemsForTable(
                        databaseHost,
                        tableFromAlias.startsWith("\"") ?
                                tableFromAlias :
                                tableFromAlias.toUpperCase()
                );

                if (!itemsForTable.isEmpty())
                    itemsForTable = buildItemsStartingWithForList(itemsForTable, tables, wordPrefix, hasDotIndex);

                return itemsForTable;
            }
        }

        List<AutoCompleteListItem> itemsStartingWith = buildItemsStartingWithForList(searchList, tables, wordPrefix, hasDotIndex);
        if (itemsStartingWith.isEmpty()) {

            itemsStartingWith = buildItemsStartingWithForList(searchList, null, wordPrefix, hasDotIndex);
            if (itemsStartingWith.isEmpty())
                noProposalsAvailable(itemsStartingWith);

            return itemsStartingWith;
        }

        if (rebuildingList)
            itemsStartingWith.add(0, buildingProposalsListItem());

        return itemsStartingWith;
    }

    private List<QueryTable> getQueryTables(String query) {

        List<QueryTable> queryTables = new ArrayList<>();
        for (CustomToken alias : getAliasesFromQuery(query))
            if (alias.getType() == SqlLexerTokenMaker.ALIAS)
                queryTables.add(new QueryTable(alias.getTableNameForAlias(), alias.getText()));

        return queryTables;
    }

    private List<CustomToken> getAliasesFromQuery(String query) {

        if (databaseHost == null)
            return new ArrayList<>();

        String lastDBObject = null;
        List<CustomToken> aliases = new ArrayList<>();
        List<String> tableNames = databaseHost.getTableNames();
        SqlLexer lexer = new SqlLexer(CharStreams.fromString(query));

        while (true) {

            org.antlr.v4.runtime.Token token = lexer.nextToken();
            if (token.getType() != SqlLexer.SPACES
                    && token.getType() != SqlLexer.SINGLE_LINE_COMMENT
                    && token.getType() != SqlLexer.MULTILINE_COMMENT
                    && token.getType() != SqlLexer.QUOTE_IDENTIFIER
                    && token.getType() != SqlLexer.IDENTIFIER
                    && !token.getText().equalsIgnoreCase("as")
            ) {
                lastDBObject = null;
            }

            if (token.getType() == SqlLexer.IDENTIFIER || token.getType() == SqlLexer.QUOTE_IDENTIFIER) {
                if (tableNames != null) {

                    String tokenText = token.getText();
                    if (!tokenText.isEmpty() && tokenText.charAt(0) >= 'A' && tokenText.charAt(0) <= 'z')
                        tokenText = tokenText.toUpperCase();

                    if (tokenText.startsWith("\"") && tokenText.endsWith("\"") && tokenText.length() > 1)
                        tokenText = tokenText.substring(1, tokenText.length() - 1);

                    if (lastDBObject != null) {
                        CustomToken customToken = new CustomToken(token);
                        customToken.setType(SqlLexerTokenMaker.ALIAS);
                        customToken.setTableNameForAlias(lastDBObject);
                        lastDBObject = null;
                        aliases.add(customToken);

                    } else if (tableNames.contains(tokenText)) {
                        CustomToken customToken = new CustomToken(token);
                        customToken.setType(SqlLexerTokenMaker.DB_OBJECT);
                        lastDBObject = tokenText;
                    }
                }
            }

            if (token.getType() == CommonToken.EOF)
                break;
        }

        return aliases;
    }

    private void captureAndResetListValues() {
        noProposals = false;
        int dot = sqlTextPane.getCaretPosition();

        int tok_dot = dot;
        if (tok_dot > 0)
            tok_dot = tok_dot - 1;

        Token token = sqlTextPane.getTokenForPosition(tok_dot);
        if (token != null && token.getType() == Token.LITERAL_STRING_DOUBLE_QUOTE) {
            noProposals = true;
            popupMenu().hidePopup();
            return;
        }

        String wordAtCursor = getWordEndingAt(dot);
        Log.trace("Capturing and resetting list values for word [ " + wordAtCursor + " ]");
        List<AutoCompleteListItem> itemsStartingWith = itemsStartingWith(wordAtCursor);
        if (itemsStartingWith.isEmpty())
            noProposalsAvailable(itemsStartingWith);

        if (rebuildingList)
            popupMenu().scheduleReset(itemsStartingWith);
        else
            popupMenu().reset(itemsStartingWith);

        noProposals = noProposals || (itemsStartingWith.size() == 1 && wordAtCursor.toLowerCase().contentEquals(itemsStartingWith.get(0).getDisplayValue().toLowerCase()));
        if (noProposals)
            popupMenu().hidePopup();
        else
            itemsStartingWith.sort(Comparator.comparing(AutoCompleteListItem::getDisplayValue));
    }

    private void noProposalsAvailable(List<AutoCompleteListItem> itemsStartingWith) {

        noProposals = true;
        if (rebuildingList) {
            Log.debug("Suggestions list still in progress");
            itemsStartingWith.add(buildingProposalsListItem());

        } else {
            Log.debug("Suggestions list completed - no matches found for input");
            itemsStartingWith.add(noProposalsListItem());
        }
    }

    private AutoCompleteListItem buildingProposalsListItem() {

        if (buildingProposalsAutoCompleteListItem == null) {
            buildingProposalsAutoCompleteListItem = new AutoCompleteListItem(
                    null,
                    "Please wait. Generating proposals...",
                    null,
                    AutoCompleteListItemType.GENERATING_LIST
            );
        }

        return buildingProposalsAutoCompleteListItem;
    }

    private AutoCompleteListItem noProposalsListItem() {

        if (noProposalsAutoCompleteListItem == null) {
            noProposalsAutoCompleteListItem = new AutoCompleteListItem(
                    null,
                    "No Proposals Available",
                    null,
                    AutoCompleteListItemType.NOTHING_PROPOSED
            );
        }

        return noProposalsAutoCompleteListItem;
    }

    private boolean isAllLowerCase(String text) {

        for (char character : text.toCharArray())
            if (Character.isUpperCase(character))
                return false;

        return true;
    }

    private List<AutoCompleteListItem> buildItemsStartingWithForList(
            List<AutoCompleteListItem> items,
            List<QueryTable> tables,
            String prefix,
            boolean prefixHadAlias) {


        String searchPattern = prefix;
        if (prefix.startsWith("("))
            searchPattern = prefix.substring(1);

        List<AutoCompleteListItem> itemsStartingWith = new ArrayList<>();
        if (items != null) {
            for (AutoCompleteListItem item : items) {

                if (item.getInsertionValue().equalsIgnoreCase(searchPattern))
                    return new ArrayList<>();

                if (item.isForPrefix(tables, searchPattern, prefixHadAlias))
                    itemsStartingWith.add(item);
            }
        }

        itemsStartingWith.sort(autoCompleteListItemComparator);
        return itemsStartingWith;
    }

    private void rebuildListSelectionsItems() {

        DatabaseConnection selectedConnection = connection;
        databaseHost = selectedConnection != null ?
                ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(selectedConnection) :
                null;

        autoCompleteListItems = new ArrayList<>();
        selectionsFactory.build(databaseHost, autoCompleteKeywords, autoCompleteSchema, sqlTextPane);
    }

    private void reapplyIfVisible() {

        if (popupMenu().isVisible()) {
            if (++resetCount == RESET_COUNT_THRESHOLD) {
                Log.trace("Reset count reached -- Resetting autocomplete popup list values");
                captureAndResetListValues();
                resetCount = 0;
            }
        }
    }

    public void resetAutoCompleteListItems() {
        if (!rebuildingList)
            autoCompleteListItems = new ArrayList<>();
    }

    public void scheduleListItemLoad() {

        if (rebuildingList || !autoCompleteListItems.isEmpty())
            return;

        worker = new SwingWorker("rebuildAutocomplete") {

            @Override
            public Object construct() {
                try {
                    Log.debug("Rebuilding suggestions list...");
                    rebuildListSelectionsItems();

                    return "done";

                } finally {
                    rebuildingList = false;
                }
            }

            @Override
            public void finished() {

                try {
                    rebuildingList = false;
                    Log.debug("Rebuilding suggestions list complete");

                    resetCount = RESET_COUNT_THRESHOLD - 1;
                    reapplyIfVisible();

                } finally {
                    popupMenu().done();
                }
            }
        };

        Log.debug("Starting worker thread for suggestions list");
        rebuildingList = true;
        worker.start();
    }

    public void setVariables(TreeSet<String> variables) {
        selectionsFactory.setVariables(variables);
        rebuildListSelectionsItems();
    }

    public void setParameters(TreeSet<String> parameters) {
        selectionsFactory.setParameters(parameters);
        rebuildListSelectionsItems();
    }

    // --- AutoCompletePopupProvider impl ---

    @Override
    public void firePopupTrigger() {

        final JTextComponent textComponent = sqlTextPane;
        Caret caret = textComponent.getCaret();
        final Point caretPosition = caret.getMagicCaretPosition();
        int heightFont = textComponent.getFontMetrics(textComponent.getFont()).getHeight();

        addFocusActions();

        resetCount = 0;
        captureAndResetListValues();
        if (!noProposals && caretPosition != null && caret.getDot() > 0) {
            QueryEditorAutoCompletePopupPanel popupPanel = popupMenu();
            Container parent = textComponent.getParent();

            int popupHeight = popupPanel.getHeight();
            if (popupHeight != 0 && caretPosition.y + popupHeight > parent.getHeight())
                caretPosition.y = caretPosition.y - popupHeight;
            else
                caretPosition.y += heightFont;

            if (caretPosition.x + popupPanel.getWidth() > parent.getWidth())
                caretPosition.x = parent.getWidth() - popupPanel.getWidth();

            if (caretPosition.x < 0)
                caretPosition.x = 0;

            popupPanel.focusAndSelectList();
            popupPanel.show(textComponent, caretPosition.x, caretPosition.y);
            textComponent.requestFocus();

        } else
            popupHidden();
    }

    @Override
    public void addListItems(List<AutoCompleteListItem> items) {

        if (autoCompleteListItems == null)
            autoCompleteListItems = new ArrayList<>();

        autoCompleteListItems.addAll(items);
        reapplyIfVisible();
    }

    @Override
    public Action getPopupAction() {
        return autoCompletePopupAction;
    }

    // --- AutoCompletePopupListener impl ---

    @Override
    public void popupSelectionMade() {

        AutoCompleteListItem selectedListItem = (AutoCompleteListItem) autoCompletePopup.getSelectedItem();
        if (selectedListItem == null || selectedListItem.isNothingProposed())
            return;

        String selectedValue = selectedListItem.getInsertionValue();

        try {
            JTextComponent textComponent = sqlTextPane;
            Document document = textComponent.getDocument();

            int caretPosition = textComponent.getCaretPosition();
            String wordAtCursor = getWordEndingAt(sqlTextPane.getCaretPosition());
            if (addingQuote)
                selectedValue += "\"";

            if (StringUtils.isNotBlank(wordAtCursor)) {

                int wordAtCursorLength = wordAtCursor.length();
                int insertionIndex = caretPosition - wordAtCursorLength;

                if (selectedListItem.isKeyword() && isAllLowerCase(wordAtCursor))
                    selectedValue = selectedValue.toLowerCase();

                if (!Character.isLetterOrDigit(wordAtCursor.charAt(0))) {
                    insertionIndex++;
                    wordAtCursorLength--;

                } else if (wordAtCursor.contains(".")) {
                    int index = wordAtCursor.indexOf(".");
                    insertionIndex += index + 1;
                    wordAtCursorLength -= index + 1;
                    selectedValue = MiscUtils.getFormattedObject(selectedValue, connection);
                }

                document.remove(insertionIndex, wordAtCursorLength);
                document.insertString(insertionIndex, selectedValue, null);

            } else
                document.insertString(caretPosition, selectedValue, null);

        } catch (BadLocationException e) {
            Log.debug("Error on autocomplete insertion", e);

        } finally {
            autoCompletePopup.hidePopup();
        }
    }

    @Override
    public void popupSelectionCancelled() {
        sqlTextPane.requestFocus();
    }

    @Override
    public void popupClosed() {
    }

    // --- ConnectionChangeListener impl ---

    @Override
    public void connectionChanged(DatabaseConnection databaseConnection) {
        connection = databaseConnection;

        if (worker != null)
            worker.interrupt();

        if (autoCompleteListItems != null) {
            if (worker != null)
                worker.interrupt();

            rebuildingList = false;
            autoCompleteListItems.clear();
        }

        if (databaseConnection != null) {
            databaseHost = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection);
            scheduleListItemLoad();
        }
    }

    // --- FocusListener impl ---

    @Override
    public void focusGained(FocusEvent e) {
        if (e.getSource() == sqlTextPane)
            scheduleListItemLoad();
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    // ---

    private class ListScrollAction extends AbstractAction {

        private final ListScrollType direction;

        private ListScrollAction(ListScrollType direction) {
            this.direction = direction;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (isPopupActionsBlocked())
                return;

            switch (direction) {

                case DOWN:
                    autoCompletePopup.scrollSelectedIndexDown();
                    break;

                case UP:
                    autoCompletePopup.scrollSelectedIndexUp();
                    break;

                case PAGE_DOWN:
                    autoCompletePopup.scrollSelectedIndexPageDown();
                    break;

                case PAGE_UP:
                    autoCompletePopup.scrollSelectedIndexPageUp();
                    break;
            }
        }

    } // ListScrollAction class

    private static class AutoCompleteListItemComparator implements Comparator<AutoCompleteListItem> {

        @Override
        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {

            if (o1.isSchemaObject() && o2.isSchemaObject()) {
                return o1.getInsertionValue().compareTo(o2.getInsertionValue());

            } else if (o1.isSchemaObject() && !o2.isSchemaObject()) {
                return -1;

            } else if (o2.isSchemaObject() && !o1.isSchemaObject())
                return 1;

            return o1.getUpperCaseValue().compareTo(o2.getUpperCaseValue());
        }

    } // AutoCompleteListItemComparator class

}
