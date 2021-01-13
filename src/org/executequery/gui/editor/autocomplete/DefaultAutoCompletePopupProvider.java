package org.executequery.gui.editor.autocomplete;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.gui.editor.ConnectionChangeListener;
import org.executequery.gui.editor.QueryWithPosition;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.log.Log;
import org.executequery.repository.spi.KeywordRepositoryImpl;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.QueryTable;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultAutoCompletePopupProvider implements AutoCompletePopupProvider, AutoCompletePopupListener,
        CaretListener, ConnectionChangeListener, FocusListener {


    private static final KeyStroke KEY_STROKE_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

    private static final KeyStroke KEY_STROKE_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);

    private static final KeyStroke KEY_STROKE_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);

    private static final KeyStroke KEY_STROKE_PAGE_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);

    private static final KeyStroke KEY_STROKE_PAGE_UP = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);

    private static final KeyStroke KEY_STROKE_TAB = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);

    private static final String LIST_FOCUS_ACTION_KEY = "focusActionKey";

    private static final String LIST_SCROLL_ACTION_KEY_DOWN = "scrollActionKeyDown";

    private static final String LIST_SCROLL_ACTION_KEY_UP = "scrollActionKeyUp";

    private static final String LIST_SCROLL_ACTION_KEY_PAGE_DOWN = "scrollActionKeyPageDown";

    private static final String LIST_SCROLL_ACTION_KEY_PAGE_UP = "scrollActionKeyPageUp";

    private static final String LIST_SELECTION_ACTION_KEY = "selectionActionKey";

    private AutoCompleteSelectionsFactory selectionsFactory;

    private AutoCompletePopupAction autoCompletePopupAction;

    private QueryEditorAutoCompletePopupPanel autoCompletePopup;

    private DatabaseObjectFactory databaseObjectFactory;

    private DatabaseHost databaseHost;

    private List<AutoCompleteListItem> autoCompleteListItems;

    private boolean autoCompleteKeywords;

    private boolean autoCompleteSchema;

    SQLTextPane sqlTextPane;

    DatabaseConnection connection;

    public DefaultAutoCompletePopupProvider(DatabaseConnection dc, SQLTextPane textPane) {

        super();
        connection = dc;
        sqlTextPane = textPane;

        selectionsFactory = new AutoCompleteSelectionsFactory(this);
        databaseObjectFactory = new DatabaseObjectFactoryImpl();

        setAutoCompleteOptionFlags();
        autoCompleteListItems = new ArrayList<AutoCompleteListItem>();
        queryEditorTextComponent().addFocusListener(this);

        autoCompletePopupAction = new AutoCompletePopupAction(this);
        autoCompleteListItems = new ArrayList<AutoCompleteListItem>();
    }

    public void setAutoCompleteOptionFlags() {

        UserProperties userProperties = UserProperties.getInstance();
        autoCompleteKeywords = userProperties.getBooleanProperty("editor.autocomplete.keywords.on");
        autoCompleteSchema = userProperties.getBooleanProperty("editor.autocomplete.schema.on");
    }

    public void reset() {

        connectionChanged(databaseHost.getDatabaseConnection());
    }

    public Action getPopupAction() {

        return autoCompletePopupAction;
    }

    boolean noProposals = false;

    private QueryEditorAutoCompletePopupPanel popupMenu() {

        if (autoCompletePopup == null) {

            autoCompletePopup = new QueryEditorAutoCompletePopupPanel();
            autoCompletePopup.addAutoCompletePopupListener(this);

            autoCompletePopup.addPopupMenuListener(new PopupMenuListener() {

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                    popupHidden();
                }

                public void popupMenuCanceled(PopupMenuEvent e) {

                    popupHidden();
                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

            });

        }

        return autoCompletePopup;
    }

    private JTextComponent queryEditorTextComponent() {

        return sqlTextPane;
    }

    private void focusAndSelectList() {

        autoCompletePopup.focusAndSelectList();
    }

    private Object existingKeyStrokeDownAction;
    private Object existingKeyStrokeUpAction;
    private Object existingKeyStrokePageDownAction;
    private Object existingKeyStrokePageUpAction;
    private Object existingKeyStrokeTabAction;
    private Object existingKeyStrokeEnterAction;

    private boolean editorActionsSaved;

    private void addFocusActions() {

        JTextComponent textComponent = queryEditorTextComponent();

        ActionMap actionMap = textComponent.getActionMap();
        actionMap.put(LIST_FOCUS_ACTION_KEY, listFocusAction);
        actionMap.put(LIST_SCROLL_ACTION_KEY_DOWN, listScrollActionDown);
        actionMap.put(LIST_SCROLL_ACTION_KEY_UP, listScrollActionUp);
        actionMap.put(LIST_SELECTION_ACTION_KEY, listSelectionAction);
        actionMap.put(LIST_SCROLL_ACTION_KEY_PAGE_DOWN, listScrollActionPageDown);
        actionMap.put(LIST_SCROLL_ACTION_KEY_PAGE_UP, listScrollActionPageUp);

        InputMap inputMap = textComponent.getInputMap();
        saveExistingActions(inputMap);

        inputMap.put(KEY_STROKE_DOWN, LIST_SCROLL_ACTION_KEY_DOWN);
        inputMap.put(KEY_STROKE_UP, LIST_SCROLL_ACTION_KEY_UP);

        inputMap.put(KEY_STROKE_PAGE_DOWN, LIST_SCROLL_ACTION_KEY_PAGE_DOWN);
        inputMap.put(KEY_STROKE_PAGE_UP, LIST_SCROLL_ACTION_KEY_PAGE_UP);

        inputMap.put(KEY_STROKE_TAB, LIST_FOCUS_ACTION_KEY);
        inputMap.put(KEY_STROKE_ENTER, LIST_SELECTION_ACTION_KEY);

        //textComponent.addCaretListener(this);
    }

    private void saveExistingActions(InputMap inputMap) {

        if (!editorActionsSaved) {

            existingKeyStrokeDownAction = inputMap.get(KEY_STROKE_DOWN);
            existingKeyStrokeUpAction = inputMap.get(KEY_STROKE_UP);
            existingKeyStrokePageDownAction = inputMap.get(KEY_STROKE_PAGE_DOWN);
            existingKeyStrokePageUpAction = inputMap.get(KEY_STROKE_PAGE_UP);
            existingKeyStrokeEnterAction = inputMap.get(KEY_STROKE_ENTER);
            existingKeyStrokeTabAction = inputMap.get(KEY_STROKE_TAB);

            editorActionsSaved = true;
        }

    }

    private boolean canExecutePopupActions() {

        if (popupMenu().isVisible()) {

            return true;
        }

        resetEditorActions();
        return false;
    }

    private void resetEditorActions() {

        JTextComponent textComponent = queryEditorTextComponent();

        ActionMap actionMap = textComponent.getActionMap();
        actionMap.remove(LIST_FOCUS_ACTION_KEY);
        actionMap.remove(LIST_SELECTION_ACTION_KEY);
        actionMap.remove(LIST_SCROLL_ACTION_KEY_DOWN);
        actionMap.remove(LIST_SCROLL_ACTION_KEY_UP);

        InputMap inputMap = textComponent.getInputMap();
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

        textComponent.removeCaretListener(this);
    }

    private void popupHidden() {

        resetEditorActions();
        queryEditorTextComponent().requestFocus();
    }

    public void refocus() {

        queryEditorTextComponent().requestFocus();
    }

    private String getWordEndingAt(int position) {

        String text = sqlTextPane.getText();

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

    private QueryWithPosition getQueryAt(int position) {

        String text = sqlTextPane.getText();
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

    public void firePopupTrigger() {


        final JTextComponent textComponent = queryEditorTextComponent();

        Caret caret = textComponent.getCaret();
        final Point caretCoords = caret.getMagicCaretPosition();

        int heightFont = textComponent.getFontMetrics(textComponent.getFont()).getHeight();

        addFocusActions();

        resetCount = 0;
        captureAndResetListValues();
        if (!noProposals && caretCoords != null && caret.getDot() > 0) {
            QueryEditorAutoCompletePopupPanel popupPanel = popupMenu();
            if (caretCoords.x + popupPanel.getWidth() > textComponent.getWidth())
                caretCoords.x = textComponent.getWidth() - popupPanel.getWidth();
            if (caretCoords.x < 0)
                caretCoords.x = 0;
            popupPanel.show(textComponent, caretCoords.x, caretCoords.y + heightFont);
            textComponent.requestFocus();
        } else popupHidden();


    }

    // TODO: determine query being executed and suggest based on that
    // introduce query types (select, insert etc)
    // track columns/tables in statement ????

    private static final int MINIMUM_CHARS_FOR_DATABASE_LOOKUP = 2;

    int oldDot = 0;

    /*private boolean hasTables(List<QueryTable> tables) {

        return (tables != null && !tables.isEmpty());
    }*/

    private List<AutoCompleteListItem> itemsStartingWith(List<QueryTable> tables, String prefix) {

        String editorText = sqlTextPane.getText();
        if (StringUtils.isBlank(prefix)) {
            return new ArrayList<>();
        }


        trace("Building list of items starting with [ " + prefix + " ] from table list with size " + tables.size());

        List<AutoCompleteListItem> searchList = autoCompleteListItems;
        String wordPrefix = prefix.trim().toUpperCase();
        String tableString = "";

        int dotIndex = prefix.indexOf('.');
        boolean hasDotIndex = (dotIndex != -1);
        if (hasDotIndex) {

            tableString = wordPrefix.substring(0, dotIndex);
            tableString = tableString.replace("(", "");
            wordPrefix = wordPrefix.substring(dotIndex + 1);

        } else if (wordPrefix.length() < MINIMUM_CHARS_FOR_DATABASE_LOOKUP /*&& !hasTables*/) {
            return buildItemsStartingWithForList(
                    selectionsFactory.buildKeywords(databaseHost, autoCompleteKeywords), tables, wordPrefix, false);
        }

        // try to get columns for table
        if (hasDotIndex) {
            List<AutoCompleteListItem> itemsForTable =
                    selectionsFactory.buildItemsForTable(databaseHost, tableString.toUpperCase());

            if (!itemsForTable.isEmpty()) {
                itemsForTable =
                        buildItemsStartingWithForList(itemsForTable, tables, wordPrefix, hasDotIndex);
            }
            if (!itemsForTable.isEmpty()) {
                return itemsForTable;
            }

        }

        // maybe alias?
        if (hasDotIndex) {
            KeywordRepositoryImpl kw = new KeywordRepositoryImpl();
            List<String> sql92 = kw.getSQL92();
            Pattern pattern = Pattern.compile("([A-z]*\\$\\w+)\\s("
                            + tableString
                            + "\\b)|(\\w+)\\s("
                            + tableString
                            + "\\b)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(editorText);
            while (matcher.find()) {
                String tableFromAlias = "";
                String tableFromAlias1 = matcher.group(1);
                String tableFromAlias3 = matcher.group(3);

                if (tableFromAlias1 != null)
                    tableFromAlias = tableFromAlias1;
                else if (tableFromAlias3 != null)
                    tableFromAlias = tableFromAlias3;

                if (sql92.contains(tableFromAlias.toUpperCase()))
                    continue;

                List<AutoCompleteListItem> itemsForTable =
                        selectionsFactory.buildItemsForTable(databaseHost, tableFromAlias.toUpperCase());

                if (!itemsForTable.isEmpty()) {
                    itemsForTable =
                            buildItemsStartingWithForList(itemsForTable, tables, wordPrefix, hasDotIndex);
                }
                return itemsForTable;
            }
        }

        List<AutoCompleteListItem> itemsStartingWith =
                buildItemsStartingWithForList(searchList, tables, wordPrefix, hasDotIndex);

        if (itemsStartingWith.isEmpty()) {

            // do it one more time without the tables...
            itemsStartingWith = buildItemsStartingWithForList(searchList, null, wordPrefix, hasDotIndex);

            if (itemsStartingWith.isEmpty()) { // now bail...
                noProposalsAvailable(itemsStartingWith);
            }

            return itemsStartingWith;
        }

        if (rebuildingList) {

            itemsStartingWith.add(0, buildingProposalsListItem());
        }

        return itemsStartingWith;
    }

    private DefaultAutoCompletePopupProvider.AutoCompleteListItemComparator autoCompleteListItemComparator = new DefaultAutoCompletePopupProvider.AutoCompleteListItemComparator();

    static class AutoCompleteListItemComparator implements Comparator<AutoCompleteListItem> {

        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {

            if (o1.isSchemaObject() && o2.isSchemaObject()) {

                return o1.getInsertionValue().compareTo(o2.getInsertionValue());

            } else if (o1.isSchemaObject() && !o2.isSchemaObject()) {

                return -1;

            } else if (o2.isSchemaObject() && !o1.isSchemaObject()) {

                return 1;
            }

            return o1.getUpperCaseValue().compareTo(o2.getUpperCaseValue());
        }

    }

    private void captureAndResetListValues() {
        noProposals = false;
        int dot = sqlTextPane.getCaretPosition();
        String wordAtCursor = getWordEndingAt(dot);
        trace("Capturing and resetting list values for word [ " + wordAtCursor + " ]");
        DerivedQuery derivedQuery = new DerivedQuery(getQueryAt(sqlTextPane.getCaretPosition()).getQuery());
        List<QueryTable> tables = derivedQuery.tableForWord(wordAtCursor);
        List<AutoCompleteListItem> itemsStartingWith = itemsStartingWith(tables, wordAtCursor);
        if (itemsStartingWith.isEmpty()) {
            //noProposals = true;
            noProposalsAvailable(itemsStartingWith);
        }

        if (rebuildingList) {

            popupMenu().scheduleReset(itemsStartingWith);

        } else {

            popupMenu().reset(itemsStartingWith);
        }
        if (noProposals)
            popupMenu().hidePopup();

    }

    private void noProposalsAvailable(List<AutoCompleteListItem> itemsStartingWith) {

        noProposals = true;
        if (rebuildingList) {

            debug("Suggestions list still in progress");
            itemsStartingWith.add(buildingProposalsListItem());

        } else {

            debug("Suggestions list completed - no matches found for input");
            itemsStartingWith.add(noProposalsListItem());
        }

    }

    private AutoCompleteListItem buildingProposalsAutoCompleteListItem;

    private AutoCompleteListItem buildingProposalsListItem() {

        if (buildingProposalsAutoCompleteListItem == null) {

            buildingProposalsAutoCompleteListItem = new AutoCompleteListItem(null,
                    "Please wait. Generating proposals...", null, AutoCompleteListItemType.GENERATING_LIST);
        }

        return buildingProposalsAutoCompleteListItem;
    }

    private AutoCompleteListItem noProposalsAutoCompleteListItem;

    private AutoCompleteListItem noProposalsListItem() {

        if (noProposalsAutoCompleteListItem == null) {

            noProposalsAutoCompleteListItem = new AutoCompleteListItem(null,
                    "No Proposals Available", null, AutoCompleteListItemType.NOTHING_PROPOSED);
        }

        return noProposalsAutoCompleteListItem;
    }

    private final DefaultAutoCompletePopupProvider.ListFocusAction listFocusAction = new DefaultAutoCompletePopupProvider.ListFocusAction();

    class ListFocusAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {

            if (!canExecutePopupActions()) {

                return;
            }

            focusAndSelectList();
        }

    } // ListFocusAction

    private final DefaultAutoCompletePopupProvider.ListSelectionAction listSelectionAction = new DefaultAutoCompletePopupProvider.ListSelectionAction();

    class ListSelectionAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {

            if (!canExecutePopupActions()) {

                return;
            }

            popupSelectionMade();
        }

    } // ListSelectionAction

    enum ListScrollType {

        UP, DOWN, PAGE_DOWN, PAGE_UP;
    }

    private final DefaultAutoCompletePopupProvider.ListScrollAction listScrollActionDown = new DefaultAutoCompletePopupProvider.ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.DOWN);
    private final DefaultAutoCompletePopupProvider.ListScrollAction listScrollActionUp = new DefaultAutoCompletePopupProvider.ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.UP);
    private final DefaultAutoCompletePopupProvider.ListScrollAction listScrollActionPageDown = new DefaultAutoCompletePopupProvider.ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.PAGE_DOWN);
    private final DefaultAutoCompletePopupProvider.ListScrollAction listScrollActionPageUp = new DefaultAutoCompletePopupProvider.ListScrollAction(DefaultAutoCompletePopupProvider.ListScrollType.PAGE_UP);

    class ListScrollAction extends AbstractAction {

        private final org.executequery.gui.editor.autocomplete.DefaultAutoCompletePopupProvider.ListScrollType direction;

        ListScrollAction(org.executequery.gui.editor.autocomplete.DefaultAutoCompletePopupProvider.ListScrollType direction) {

            this.direction = direction;
        }

        public void actionPerformed(ActionEvent e) {

            if (!canExecutePopupActions()) {

                return;
            }

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

    } // ListScrollAction

    public void popupClosed() {

//        popupHidden();
    }

    public void popupSelectionCancelled() {

        queryEditorTextComponent().requestFocus();
    }

    private boolean isAllLowerCase(String text) {

        for (char character : text.toCharArray()) {

            if (Character.isUpperCase(character)) {

                return false;
            }

        }

        return true;
    }

    public void popupSelectionMade() {

        AutoCompleteListItem selectedListItem = (AutoCompleteListItem) autoCompletePopup.getSelectedItem();

        if (selectedListItem == null || selectedListItem.isNothingProposed()) {

            return;
        }

        String selectedValue = selectedListItem.getInsertionValue();

        try {

            JTextComponent textComponent = queryEditorTextComponent();
            Document document = textComponent.getDocument();

            int caretPosition = textComponent.getCaretPosition();
            String wordAtCursor = getWordEndingAt(sqlTextPane.getCaretPosition());

            if (StringUtils.isNotBlank(wordAtCursor)) {

                int wordAtCursorLength = wordAtCursor.length();
                int insertionIndex = caretPosition - wordAtCursorLength;

                if (selectedListItem.isKeyword() && isAllLowerCase(wordAtCursor)) {

                    selectedValue = selectedValue.toLowerCase();
                }

                if (!Character.isLetterOrDigit(wordAtCursor.charAt(0))) {

                    // cases where you might have a.column_name or similar

                    insertionIndex++;
                    wordAtCursorLength--;

                } else if (wordAtCursor.contains(".")) {

                    int index = wordAtCursor.indexOf(".");
                    insertionIndex += index + 1;
                    wordAtCursorLength -= index + 1;
                }

                document.remove(insertionIndex, wordAtCursorLength);
                document.insertString(insertionIndex, selectedValue, null);

            } else {

                document.insertString(caretPosition, selectedValue, null);
            }

        } catch (BadLocationException e) {

            debug("Error on autocomplete insertion", e);

        } finally {

            autoCompletePopup.hidePopup();
        }

    }

    private List<AutoCompleteListItem> buildItemsStartingWithForList(
            List<AutoCompleteListItem> items, List<QueryTable> tables, String prefix,
            boolean prefixHadAlias) {


        String searchPattern = prefix;
        if (prefix.startsWith("(")) {

            searchPattern = prefix.substring(1);
        }

        List<AutoCompleteListItem> itemsStartingWith = new ArrayList<AutoCompleteListItem>();

        if (items != null) {

            for (int i = 0, n = items.size(); i < n; i++) {

                AutoCompleteListItem item = items.get(i);
                if (item.isForPrefix(tables, searchPattern, prefixHadAlias)) {

                    itemsStartingWith.add(item);
                }

            }

        }

        Collections.sort(itemsStartingWith, autoCompleteListItemComparator);
        return itemsStartingWith;
    }

    public void caretUpdate(CaretEvent e) {

    }

    public void connectionChanged(DatabaseConnection databaseConnection) {

        if (worker != null) {

            worker.interrupt();
        }

        if (autoCompleteListItems != null) {

            autoCompleteListItems.clear();
        }

        if (databaseConnection != null) {

            databaseHost = databaseObjectFactory.createDatabaseHost(databaseConnection);
            scheduleListItemLoad();
        }

    }

    public void focusGained(FocusEvent e) {

        if (e.getSource() == queryEditorTextComponent()) {

            scheduleListItemLoad();
        }

    }

    private boolean rebuildListSelectionsItems() {

        DatabaseConnection selectedConnection = connection;
        if (selectedConnection == null) {

            databaseHost = null;

        } else if (databaseHost == null) {

            databaseHost = databaseObjectFactory.createDatabaseHost(selectedConnection);
        }

        selectionsFactory.build(databaseHost, autoCompleteKeywords, autoCompleteSchema, sqlTextPane);

        return true;
    }

    public void addListItems(List<AutoCompleteListItem> items) {

        if (autoCompleteListItems == null) {

            autoCompleteListItems = new ArrayList<AutoCompleteListItem>();
        }

        autoCompleteListItems.addAll(items);
//        Collections.sort(autoCompleteListItems, autoCompleteListItemComparatorByValue);
        reapplyIfVisible();
    }

    private int resetCount;
    private static final int RESET_COUNT_THRESHOLD = 20; // apply every 5 calls

    private void reapplyIfVisible() {

        if (popupMenu().isVisible()) {

            if (++resetCount == RESET_COUNT_THRESHOLD) {

                trace("Reset count reached -- Resetting autocomplete popup list values");
                captureAndResetListValues();
                resetCount = 0;
            }

        }
    }

    private boolean rebuildingList;
    private org.underworldlabs.swing.util.SwingWorker worker;

    private void scheduleListItemLoad() {

        if (rebuildingList || !autoCompleteListItems.isEmpty()) {

            return;
        }

        worker = new SwingWorker() {

            public Object construct() {

                try {

                    debug("Rebuilding suggestions list...");

                    rebuildingList = true;
                    rebuildListSelectionsItems();

                    return "done";

                } finally {

                    rebuildingList = false;
                }
            }

            public void finished() {

                try {

                    rebuildingList = false;
                    debug("Rebuilding suggestions list complete");

                    // force
                    resetCount = RESET_COUNT_THRESHOLD - 1;
                    reapplyIfVisible();

                } finally {

                    popupMenu().done();
                }
            }

        };

        debug("Starting worker thread for suggestions list");
        worker.start();
    }

    public void focusLost(FocusEvent e) {
    }

    static class AutoCompleteListItemComparatorByValue implements Comparator<AutoCompleteListItem> {

        public int compare(AutoCompleteListItem o1, AutoCompleteListItem o2) {

            return o1.getValue().toUpperCase().compareTo(o2.getValue().toUpperCase());
        }

    }

    private void debug(String message) {

        Log.debug(message);
    }

    private void trace(String message) {

        Log.trace(message);
    }

    private void debug(String message, Throwable e) {

        Log.debug(message, e);
    }

}
