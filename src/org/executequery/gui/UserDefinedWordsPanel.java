/*
 * UserDefinedWordsPanel.java
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

package org.executequery.gui;

import org.executequery.ApplicationException;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class UserDefinedWordsPanel extends DefaultActionButtonsPanel
        implements FocusComponentPanel,
        ActionListener {

    public static final String TITLE = "User Defined Keywords";

    private List<String> definedTypes;
    private List<String> sql92Types;

    private JTable userTable;

    private JButton addButton;
    private RolloverButton deleteButton;

    private JTextField newWordField;

    private KeywordModel userModel;

    /**
     * the parent container
     */
    private final ActionContainer parent;

    public UserDefinedWordsPanel(ActionContainer parent) {

        this.parent = parent;

        try {
            // create a separate word list
            KeywordRepository keywordRepository =
                    (KeywordRepository) RepositoryCache.load(KeywordRepository.REPOSITORY_ID);

            List<String> words = keywordRepository.getUserDefinedSQL();

            definedTypes = new ArrayList<String>(words.size());
            for (String word : words) {

                definedTypes.add(word);
            }

            sql92Types = keywordRepository.getServerKeywords(0, 0, "");

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        JPanel tablePanel = new JPanel(new GridBagLayout());

        Dimension dim = new Dimension(350, 350);

        userModel = new KeywordModel(definedTypes, true);
        userTable = new DefaultTable(userModel);
        userTable.getTableHeader().setResizingAllowed(false);
        userTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane js1 = new JScrollPane();
        js1.getViewport().add(userTable);

        JPanel userPanel = new JPanel(new GridBagLayout());
        userPanel.setBorder(BorderFactory.createTitledBorder("User Defined"));
        userPanel.setPreferredSize(dim);

        deleteButton = new RolloverButton(
                GUIUtilities.loadIcon("Delete16.svg"), "Delete selection");
        actionToButton(deleteButton, "deleteWord");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        userPanel.add(js1, gbc);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        userPanel.add(deleteButton, gbc);

        JTable sql92Table = new DefaultTable(new KeywordModel(sql92Types));
        sql92Table.getTableHeader().setResizingAllowed(false);
        sql92Table.getTableHeader().setReorderingAllowed(false);

        JScrollPane js2 = new JScrollPane();
        js2.getViewport().add(sql92Table);

        JPanel sql92Panel = new JPanel(new BorderLayout());
        sql92Panel.setBorder(BorderFactory.createTitledBorder("SQL92"));
        sql92Panel.add(js2, BorderLayout.CENTER);
        sql92Panel.setPreferredSize(dim);

        newWordField = WidgetFactory.createTextField("newWordField");
        actionToField(newWordField, "addWord");

        addButton = WidgetFactory.createButton("addButton", "Add");
        actionToButton(addButton, "addWord");

        JPanel addPanel = new JPanel(new GridBagLayout());

        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.top = 5;
        gbc.insets.bottom = 5;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        tablePanel.add(userPanel, gbc);
        gbc.insets.right = 5;
        gbc.gridx = 2;
        tablePanel.add(sql92Panel, gbc);
        gbc.insets.top = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        addPanel.add(new JLabel("New Keyword:"), gbc);
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        addPanel.add(newWordField, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.0;
        gbc.insets.right = 5;
        gbc.insets.left = 0;
        addPanel.add(addButton, gbc);
        gbc.insets.right = 0;
        gbc.gridwidth = 3;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.insets.left = 5;
        tablePanel.add(addPanel, gbc);

        tablePanel.setBorder(BorderFactory.createEtchedBorder());

        newWordField.addActionListener(this);

        addContentPanel(tablePanel);

        addActionButton(createSaveButton());
        addActionButton(createCancelButton());
        addHelpButton("user-defined-keywords");

        newWordField.requestFocus();
    }

    public Component getDefaultFocusComponent() {
        return newWordField;
    }

    private void actionToButton(JButton button, String actionCommand) {
        button.addActionListener(this);
        button.setActionCommand(actionCommand);
    }

    private void actionToField(JTextField textField, String actionCommand) {
        textField.addActionListener(this);
        textField.setActionCommand(actionCommand);
    }

    public void addWord() {

        String newWord = newWordField.getText();

        if (MiscUtils.isNull(newWord)) {
            return;
        }

        newWord = newWord.trim().toUpperCase();
        if (Collections.binarySearch(sql92Types, newWord) >= 0) {
            GUIUtilities.displayWarningMessage(
                    "The word entered is already part of the SQL92 keyword list.\n");
            newWordField.selectAll();
            newWordField.requestFocus();
            return;
        } else if (Collections.binarySearch(userModel.getWords(), newWord) >= 0) {
            GUIUtilities.displayWarningMessage(
                    "The word entered is already part of the user defined keyword list.\n");
            newWordField.selectAll();
            newWordField.requestFocus();
            return;
        }

        userModel.addNewWord(newWord);
        newWordField.setText("");
        newWordField.requestFocus();

        userTable.revalidate();
    }

    public void deleteWord() {

        int selection = userTable.getSelectedRow();
        if (selection == -1) {
            return;
        }

        userModel.deleteWord(selection);

        userTable.revalidate();
    }

    public void cancel() {
        parent.finished();
    }

    public void save() {

        try {

            keywords().setUserDefinedKeywords(userModel.getWords());
            parent.finished();

        } catch (ApplicationException e) {

            GUIUtilities.displayErrorMessage(
                    "An error occured updating your keywords.\nPlease try again.");
        }

    }

    private KeywordRepository keywords() {

        return (KeywordRepository) RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
    }

    private JButton createCancelButton() {

        JButton button = new DefaultPanelButton(Bundles.get("common.cancel.button"));

        button.setActionCommand("cancel");
        button.addActionListener(this);

        return button;
    }

    private JButton createSaveButton() {

        JButton button = new DefaultPanelButton(Bundles.get("common.save.button"));

        button.setActionCommand("save");
        button.addActionListener(this);

        return button;
    }

    private class KeywordModel extends AbstractTableModel {

        private final List<String> words;
        private final String header = "Keyword";
        private boolean editable = false;

        public KeywordModel(List<String> words) {
            this(words, false);
        }

        public KeywordModel(List<String> words, boolean isEditable) {
            Collections.sort(words);
            this.words = words;
            editable = isEditable;
        }

        public List<String> getWords() {
            return words;
        }

        public void deleteWord(int index) {
            words.remove(index);
            fireTableRowsUpdated(index, definedTypes.size() - 1);
        }

        public void addNewWord(String word) {
            words.add(word);
            int row = definedTypes.size() - 1;
            fireTableRowsUpdated(row, row);
        }

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return words.size();
        }

        public Object getValueAt(int row, int col) {
            return words.get(row);
        }

        public void setValueAt(Object value, int row, int col) {
            words.set(row, (String) value);
            fireTableRowsUpdated(row, row);
        }

        public boolean isCellEditable(int row, int col) {
            return editable;
        }

        public String getColumnName(int col) {
            return header;
        }

        public Class<?> getColumnClass(int col) {
            return String.class;
        }

    } // class KeywordModel

}





