/*
 * QueryEditorAutoCompletePopupProvider.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui.editor.autocomplete;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.gui.editor.ConnectionChangeListener;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.log.Log;
import org.executequery.sql.DerivedQuery;
import org.executequery.sql.QueryTable;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.util.SwingWorker;

public class QueryEditorAutoCompletePopupProvider implements AutoCompletePopupProvider, AutoCompletePopupListener,
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

    private QueryEditor queryEditor;

    private AutoCompleteSelectionsFactory selectionsFactory;

    private AutoCompletePopupAction autoCompletePopupAction;

    private QueryEditorAutoCompletePopupPanel autoCompletePopup;

    private DatabaseObjectFactory databaseObjectFactory;

    private DatabaseHost databaseHost;

    private List<AutoCompleteListItem> autoCompleteListItems;

    private boolean autoCompleteKeywords;

    private boolean autoCompleteSchema;

    public QueryEditorAutoCompletePopupProvider(QueryEditor queryEditor) {

        super();
        this.queryEditor = queryEditor;

        selectionsFactory = new AutoCompleteSelectionsFactory(this);
        databaseObjectFactory = new DatabaseObjectFactoryImpl();

        setAutoCompleteOptionFlags();
        autoCompleteListItems = new ArrayList<AutoCompleteListItem>();

        queryEditor.addConnectionChangeListener(this);
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

    public void firePopupTrigger() {

        try {

            final JTextComponent textComponent = queryEditorTextComponent();

            Caret caret = textComponent.getCaret();
            final Rectangle caretCoords = textComponent.modelToView(caret.getDot());

            addFocusActions();
            
            resetCount = 0;
            captureAndResetListValues();

            popupMenu().show(textComponent, caretCoords.x, caretCoords.y + caretCoords.height);
            textComponent.requestFocus();

        } catch (BadLocationException e) {

            debug("Error on caret coordinates", e);
        }

    }

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
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

            });

        }

        return autoCompletePopup;
    }

    private JTextComponent queryEditorTextComponent() {

        return queryEditor.getEditorTextComponent();
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

        textComponent.addCaretListener(this);
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

    private void captureAndResetListValues() {

        String wordAtCursor = queryEditor.getWordToCursor();
        trace("Capturing and resetting list values for word [ " + wordAtCursor + " ]");
        
        DerivedQuery derivedQuery = new DerivedQuery(queryEditor.getQueryAtCursor().getQuery());
        List<QueryTable> tables = derivedQuery.tableForWord(wordAtCursor);

        List<AutoCompleteListItem> itemsStartingWith = itemsStartingWith(tables, wordAtCursor);
        if (itemsStartingWith.isEmpty()) {

            noProposalsAvailable(itemsStartingWith);
        }

        if (rebuildingList) {
        
            popupMenu().scheduleReset(itemsStartingWith);
        
        } else {
            
            popupMenu().reset(itemsStartingWith);            
        }

    }

    // TODO: determine query being executed and suggest based on that
    // introduce query types (select, insert etc)
    // track columns/tables in statement ????

    private static final int MINIMUM_CHARS_FOR_DATABASE_LOOKUP = 2;

    private List<AutoCompleteListItem> itemsStartingWith(List<QueryTable> tables, String prefix) {

        boolean hasTables = hasTables(tables);
        if (StringUtils.isBlank(prefix) && !hasTables) {

            return selectionsFactory.buildKeywords(databaseHost, autoCompleteKeywords);
        }

        trace("Building list of items starting with [ " + prefix + " ] from table list with size " + tables.size());

        List<AutoCompleteListItem> searchList = autoCompleteListItems;
        String wordPrefix = prefix.trim().toUpperCase();

        int dotIndex = prefix.indexOf('.');
        boolean hasDotIndex = (dotIndex != -1);
        if (hasDotIndex) {

            wordPrefix = wordPrefix.substring(dotIndex + 1);

        } else if (wordPrefix.length() < MINIMUM_CHARS_FOR_DATABASE_LOOKUP && !hasTables) {

            return buildItemsStartingWithForList(
                    selectionsFactory.buildKeywords(databaseHost, autoCompleteKeywords), tables, wordPrefix, false);
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
        /* ----- might be a little sluggish right now ...
        else { // add other entities starting with at the end of the list (??)

            itemsStartingWith.addAll(buildItemsStartingWithForList(
                    autoCompleteListItems, null, wordPrefix, hasDotIndex));
        }
        */

        if (rebuildingList) {
            
            itemsStartingWith.add(0, buildingProposalsListItem());
        }
        
        return itemsStartingWith;
    }

    private boolean hasTables(List<QueryTable> tables) {

        return (tables != null && !tables.isEmpty());
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

    private AutoCompleteListItemComparator autoCompleteListItemComparator = new AutoCompleteListItemComparator();
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

    private void noProposalsAvailable(List<AutoCompleteListItem> itemsStartingWith) {
        
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

    private final ListFocusAction listFocusAction = new ListFocusAction();

    class ListFocusAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {

            if (!canExecutePopupActions()) {

                return;
            }

            focusAndSelectList();
        }

    } // ListFocusAction

    private final ListSelectionAction listSelectionAction = new ListSelectionAction();

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

    private final ListScrollAction listScrollActionDown = new ListScrollAction(ListScrollType.DOWN);
    private final ListScrollAction listScrollActionUp = new ListScrollAction(ListScrollType.UP);
    private final ListScrollAction listScrollActionPageDown = new ListScrollAction(ListScrollType.PAGE_DOWN);
    private final ListScrollAction listScrollActionPageUp = new ListScrollAction(ListScrollType.PAGE_UP);

    class ListScrollAction extends AbstractAction {

        private final ListScrollType direction;

        ListScrollAction(ListScrollType direction) {

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
            String wordAtCursor = queryEditor.getWordToCursor();

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
                    wordAtCursorLength -=  index + 1;
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

    public void caretUpdate(CaretEvent e) {

        captureAndResetListValues();
    }

    public void connectionChanged(DatabaseConnection databaseConnection) {

        if (worker != null) {
            
            worker.interrupt();
        }
        
    	if (autoCompleteListItems != null) {
    		
    		autoCompleteListItems.clear();
    	}
    	
        if (databaseHost != null) {

            databaseHost.close();
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

        DatabaseConnection selectedConnection = queryEditor.getSelectedConnection();
        if (selectedConnection == null) {

            databaseHost = null;

        } else if (databaseHost == null) {

            databaseHost = databaseObjectFactory.createDatabaseHost(selectedConnection);
        }

    	selectionsFactory.build(databaseHost, autoCompleteKeywords, autoCompleteSchema);

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
    private SwingWorker worker;

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

    public void focusLost(FocusEvent e) {}

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

