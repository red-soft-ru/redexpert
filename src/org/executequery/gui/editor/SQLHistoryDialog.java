/*
 * SQLHistoryDialog.java
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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SQLTextArea;
import org.executequery.localization.Bundles;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.SqlCommandHistoryRepository;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.FlatSplitPane;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The History Dialog displays the executed SQL statement history
 * from within the Query Editor. The data represented as a
 * <code>Vector</code> object, is displayed within a <code>JLIst</code>.
 * Selection of a stored statement can be achieved by double-clicking the
 * statement, selecting and pressing the ENTER key or by selecting
 * and clicking the SELECT button.<br>
 * The selected statement is displayed within the Query Editor that
 * initiated the frame.<br>
 * Selecting the CANCEL button closes the dialog.
 *
 * @author Takis Diakoumis
 */
public class SQLHistoryDialog extends AbstractBaseDialog {

    private static final String TITLE = bundleString("title");

    // --- GUI components ---

    private SQLTextArea textPane;
    private JTextField searchField;
    private JCheckBox openNewEditorCheck;
    private JList<String> historyList;

    private JButton copyButton;
    private JButton clearButton;
    private JButton cancelButton;
    private JButton selectButton;
    private JButton insertButton;
    private JButton searchButton;

    // ---

    private Vector<String> data;
    private final QueryEditor queryEditor;

    public SQLHistoryDialog(Vector<String> data, QueryEditor queryEditor) {
        super(GUIUtilities.getParentFrame(), TITLE, true);
        this.queryEditor = queryEditor;
        this.data = data;

        init();
        arrange();
    }

    private void init() {

        // --- buttons ---

        selectButton = WidgetFactory.createButton("selectButton", Bundles.get("common.select.button"), e -> selectQuery());
        selectButton.setToolTipText(bundleString("selectButton.toolTip"));

        copyButton = WidgetFactory.createButton("copyButton", Bundles.get("common.copy.button"), e -> copyQuery());
        copyButton.setToolTipText(bundleString("copyButton.toolTip"));

        clearButton = WidgetFactory.createButton("clearButton", Bundles.get("common.clear.button"), e -> clearHistory());
        clearButton.setToolTipText(bundleString("clearButton.toolTip"));

        insertButton = WidgetFactory.createButton("insertButton", bundleString("insertButton"), e -> insertQuery());
        insertButton.setToolTipText(bundleString("insertButton.toolTip"));

        searchButton = WidgetFactory.createButton("searchButton", Bundles.get("common.search.button"), e -> searchForQuery());
        searchButton.setToolTipText(bundleString("searchButton.toolTip"));

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.get("common.cancel.button"), e -> dispose());

        // --- history list ---

        historyList = new JList<>(data);
        historyList.setFixedCellHeight(20);
        historyList.addListSelectionListener(e -> updateTextPanel());

        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2)
                    selectQuery();
            }
        });

        historyList.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    selectQuery();
            }
        });

        // --- others ---

        openNewEditorCheck = WidgetFactory.createCheckBox("openNewEditorCheck", bundleString("openNewEditorCheck"));
        openNewEditorCheck.setToolTipText(bundleString("openNewEditorCheck.toolTip"));

        searchField = WidgetFactory.createTextField("searchField");
        searchField.addActionListener(e -> searchForQuery());

        textPane = new SQLTextArea();
        textPane.setEditable(false);

    }

    private void arrange() {
        GridBagHelper gbh;

        // --- split pane ---

        JSplitPane splitPane = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(historyList));
        splitPane.setRightComponent(new JScrollPane(textPane));
        splitPane.setDividerLocation(0.7);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(4);

        // --- search panel ---

        JPanel searchPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().rightGap(5);
        searchPanel.add(new JLabel(Bundles.get("common.search")), gbh.topGap(3).get());
        searchPanel.add(searchField, gbh.nextCol().topGap(0).setMaxWeightX().get());
        searchPanel.add(searchButton, gbh.nextCol().rightGap(0).setMinWeightX().get());

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        buttonPanel.add(openNewEditorCheck, gbh.get());
        buttonPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().get());
        buttonPanel.add(selectButton, gbh.nextCol().setMinWeightX().rightGap(5).get());
        buttonPanel.add(insertButton, gbh.nextCol().get());
        buttonPanel.add(copyButton, gbh.nextCol().get());
        buttonPanel.add(clearButton, gbh.nextCol().get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillBoth().bottomGap(5);
        mainPanel.add(searchPanel, gbh.setMinWeightY().spanX().get());
        mainPanel.add(splitPane, gbh.nextRow().setMaxWeightY().get());
        mainPanel.add(buttonPanel, gbh.nextRow().bottomGap(0).setMinWeightY().get());

        // --- base ---

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(800, 490));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());

        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));
        setVisible(true);
    }

    private void updateTextPanel() {
        textPane.setText(queryForIndex(historyList.getSelectedIndex()));
    }

    /**
     * Sets the statement history data to the <code>JList</code>.
     *
     * @param data the statement history <code>Vector</code>
     */
    public void setHistoryData(Vector<String> data) {
        this.data = data;
        historyList.setListData(data);
    }

    private void searchForQuery() {

        String text = searchField.getText();
        if (MiscUtils.isNull(text))
            return;

        int start = historyList.getSelectedIndex();
        if (start == -1 || start == data.size() - 1)
            start = 0;
        else
            start++;

        search(text, start);
    }

    private void clearHistory() {
        sqlCommandHistoryRepository().clearSqlCommandHistory(queryEditor.getSelectedConnection().getId());
        setHistoryData(new Vector<>(0));
    }

    private SqlCommandHistoryRepository sqlCommandHistoryRepository() {
        return (SqlCommandHistoryRepository) RepositoryCache.load(SqlCommandHistoryRepository.REPOSITORY_ID);
    }

    private void search(String text, int start) {

        Pattern pattern = Pattern.compile("\\b" + text, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(Constants.EMPTY);

        for (int i = start, k = data.size(); i < k; i++) {
            matcher.reset(data.get(i));

            if (matcher.find()) {
                historyList.setSelectedIndex(i);
                scrollToSelection(i);
                return;
            }
        }

        GUIUtilities.displayInformationMessage(bundleString("stringNotFound"));
    }

    private void scrollToSelection(int i) {
        historyList.ensureIndexIsVisible(i);
    }

    private boolean invalidSelection() {

        if (data.isEmpty())
            return true;

        if (historyList.isSelectionEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("noSelection"));
            return true;
        }

        return false;
    }

    private void copyQuery() {

        if (invalidSelection())
            return;

        String query = queryForIndices(historyList.getSelectedIndices());
        GUIUtilities.copyToClipBoard(query);
        dispose();
    }

    private void insertQuery() {

        if (openNewEditorCheck.isSelected()) {
            selectQuery();

        } else if (queryEditor != null) {
            String query = queryForIndices(historyList.getSelectedIndices());
            queryEditor.insertTextAtCaret(query);
            dispose();
        }
    }


    private void selectQuery() {

        if (invalidSelection())
            return;

        String query = queryForIndices(historyList.getSelectedIndices());
        if (openNewEditorCheck.isSelected()) {
            GUIUtilities.addCentralPane(
                    QueryEditor.TITLE,
                    QueryEditor.FRAME_ICON,
                    new QueryEditor(query),
                    null,
                    true
            );

        } else if (queryEditor != null)
            queryEditor.setEditorText(query);

        dispose();
    }

    private String queryForIndices(int[] indices) {

        if (indices.length < 1)
            return Constants.EMPTY;

        StringBuilder sb = new StringBuilder();
        for (int index : indices)
            sb.append(queryForIndex(index).trim()).append("\n\n");

        return sb.toString().trim();
    }

    private String queryForIndex(int index) {
        return index != -1 ? data.get(index) : Constants.EMPTY;
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(SQLHistoryDialog.class, key, args);
    }

}
