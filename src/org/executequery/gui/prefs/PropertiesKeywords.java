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

package org.executequery.gui.prefs;

import org.executequery.ApplicationException;
import org.executequery.GUIUtilities;
import org.executequery.gui.*;
import org.executequery.localization.Bundles;
import org.executequery.repository.KeywordRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class PropertiesKeywords extends AbstractPropertiesBasePanel {

    private JTable userTable;
    private JTable sql92Table;
    private JButton addButton;
    private JButton deleteButton;
    private JTextField newWordField;

    private KeywordModel userModel;
    private List<String> sql92Types;
    private List<String> userDefinedTypes;

    public PropertiesKeywords() {
        init();
        arrange();
    }

    private void init() {
        userDefinedTypes = new ArrayList<>();
        sql92Types = new ArrayList<>();

        Repository repo = RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
        if (repo instanceof KeywordRepository) {
            KeywordRepository keywordRepo = (KeywordRepository) repo;
            userDefinedTypes.addAll(keywordRepo.getUserDefinedSQL());
            sql92Types.addAll(keywordRepo.getServerKeywords(0, 0, ""));
        }

        userModel = new KeywordModel(userDefinedTypes, true);
        userTable = new DefaultTable(userModel);
        userTable.getTableHeader().setResizingAllowed(false);
        userTable.getTableHeader().setReorderingAllowed(false);

        sql92Table = new DefaultTable(new KeywordModel(sql92Types));
        sql92Table.getTableHeader().setResizingAllowed(false);
        sql92Table.getTableHeader().setReorderingAllowed(false);

        newWordField = WidgetFactory.createTextField("newWordField");
        newWordField.addActionListener(e -> addWord());

        addButton = WidgetFactory.createButton(
                "addButton",
                bundledString("Add"),
                e -> addWord()
        );

        deleteButton = WidgetFactory.createButton(
                "deleteButton",
                bundledString("Delete"),
                e -> deleteWord()
        );
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- user defined panel ---

        JPanel userDefinedPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper();
        userDefinedPanel.add(new JScrollPane(userTable), gbh.setMaxWeightY().fillBoth().spanX().get());
        userDefinedPanel.add(deleteButton, gbh.nextRow().setMinWeightY().topGap(5).get());

        // --- table panel ---

        JPanel tablePanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth().spanY();
        tablePanel.add(userDefinedPanel, gbh.setWeightX(0.5).get());
        tablePanel.add(new JScrollPane(sql92Table), gbh.nextCol().leftGap(5).spanX().get());

        // --- new word panel ---

        JPanel newWordPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillHorizontally().rightGap(5);
        newWordPanel.add(new JLabel(bundledString("NewKeyword")), gbh.setMinWeightX().get());
        newWordPanel.add(newWordField, gbh.nextCol().setMaxWeightX().get());
        newWordPanel.add(addButton, gbh.nextCol().setMinWeightX().rightGap(0).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth();
        mainPanel.add(newWordPanel, gbh.setMinWeightY().spanX().get());
        mainPanel.add(tablePanel, gbh.nextRow().setMaxWeightY().topGap(5).get());

        // --- base ---

        addContent(mainPanel);
    }

    public void addWord() {

        String newWord = newWordField.getText();
        if (MiscUtils.isNull(newWord))
            return;

        newWord = newWord.trim().toUpperCase();
        if (Collections.binarySearch(sql92Types, newWord) >= 0) {
            GUIUtilities.displayWarningMessage(bundledString("AlreadyInSQL92"));
            newWordField.selectAll();
            newWordField.requestFocus();
            return;

        } else if (Collections.binarySearch(userModel.getWords(), newWord) >= 0) {
            GUIUtilities.displayWarningMessage(bundledString("AlreadyInUserDefined"));
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
        if (selection == -1)
            return;

        userModel.deleteWord(selection);
        userTable.revalidate();
    }

    private class KeywordModel extends AbstractTableModel {

        private final boolean userDefined;
        private final List<String> words;

        public KeywordModel(List<String> words) {
            this(words, false);
        }

        public KeywordModel(List<String> words, boolean userDefined) {
            Collections.sort(words);
            this.words = words;
            this.userDefined = userDefined;
        }

        public List<String> getWords() {
            return words;
        }

        public void deleteWord(int index) {

            if (!userDefined)
                return;

            words.remove(index);
            fireTableRowsUpdated(index, userDefinedTypes.size() - 1);
        }

        public void addNewWord(String word) {

            if (!userDefined)
                return;

            words.add(word);
            int row = userDefinedTypes.size() - 1;
            fireTableRowsUpdated(row, row);
        }

        public void clearWordList() {

            if (!userDefined)
                return;

            int rowsCount = words.size();
            words.clear();
            fireTableRowsUpdated(0, rowsCount - 1);
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return words.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return words.get(row);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            words.set(row, (String) value);
            fireTableRowsUpdated(row, row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return userDefined;
        }

        @Override
        public String getColumnName(int col) {
            return Bundles.get(userDefined ? "PropertiesKeywords.UserDefinedHeader" : "PropertiesKeywords.SqlHeader");
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

    } // KeywordModel class

    // --- UserPreferenceFunction impl ---

    @Override
    public void save() {
        try {
            Repository repo = RepositoryCache.load(KeywordRepository.REPOSITORY_ID);
            if (repo instanceof KeywordRepository)
                ((KeywordRepository) repo).setUserDefinedKeywords(userModel.getWords());

        } catch (ApplicationException e) {
            GUIUtilities.displayExceptionErrorDialog(bundledString("ErrorUpdating"), e);
        }
    }

    @Override
    public void restoreDefaults() {
        userModel.clearWordList();
        userTable.revalidate();
    }

}
