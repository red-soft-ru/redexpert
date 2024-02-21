/*
 * FindReplaceDialog.java
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

import org.executequery.ActiveComponent;
import org.executequery.GUIUtilities;
import org.executequery.gui.text.TextEditor;
import org.executequery.localization.Bundles;
import org.executequery.search.TextAreaSearch;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Find replace for text components.
 *
 * @author Takis Diakoumis
 */
public class FindReplaceDialog extends DefaultActionButtonsPanel
        implements ActiveComponent {

    public static final String TITLE = Bundles.get("FindReplaceDialog.FindAndReplace");

    public static final int FIND = 0;
    public static final int REPLACE = 1;

    // --- GUI components ---

    private final ActionContainer parent;
    private final TextEditor textEditor;

    private JButton findNextButton;
    @SuppressWarnings("FieldCanBeLocal")
    private JButton closeButton;
    private JButton replaceButton;
    private JButton replaceAllButton;

    private JCheckBox isWholeWordsCheck;
    private JCheckBox isUseRegexCheck;
    private JCheckBox isMatchCaseCheck;
    private JCheckBox isReplaceCheck;
    private JCheckBox isWrapCheck;

    private JRadioButton searchUpRadio;
    private JRadioButton searchDownRadio;

    private JComboBox<?> findStringCombo;
    private JComboBox<?> replaceStringCombo;

    // ---

    public FindReplaceDialog(ActionContainer parent, int type, TextEditor textEditor) {

        this.textEditor = textEditor;
        this.parent = parent;

        init();
        arrange();
        setFindReplace(type == REPLACE);
    }

    private void init() {

        String selectedText = textEditor.getEditorTextComponent().getSelectedText();
        if (selectedText != null && !selectedText.isEmpty())
            addFind(selectedText);

        // --- combo boxes ---

        KeyAdapter keyListener = getCreateKeyListener();

        findStringCombo = WidgetFactory.createComboBox("findField", TextAreaSearch.getPrevFindValues());
        findStringCombo.getEditor().getEditorComponent().addKeyListener(keyListener);
        findStringCombo.setEditable(true);

        replaceStringCombo = WidgetFactory.createComboBox("replaceField", TextAreaSearch.getPrevReplaceValues());
        replaceStringCombo.getEditor().getEditorComponent().addKeyListener(keyListener);
        replaceStringCombo.setEditable(true);

        // --- check boxes ---

        isReplaceCheck = WidgetFactory.createCheckBox("isReplaceCheck", bundleString("Replace") + ":");
        isReplaceCheck.setEnabled(textEditor.getEditorTextComponent().isEditable());
        isReplaceCheck.addActionListener(e -> setToReplace());

        isUseRegexCheck = WidgetFactory.createCheckBox("isUseRegexCheck", bundleString("RegularExpressions"));
        isUseRegexCheck.addActionListener(e -> setToRegex());

        isWrapCheck = WidgetFactory.createCheckBox("isWrapCheck", bundleString("WrapCheck"));
        isWrapCheck.setSelected(true);

        isWholeWordsCheck = WidgetFactory.createCheckBox("isWholeWordsCheck", bundleString("WholeWordsCheck"));
        isMatchCaseCheck = WidgetFactory.createCheckBox("isMatchCaseCheck", bundleString("MatchCaseCheck"));

        // --- radio buttons ---

        searchUpRadio = WidgetFactory.createRadioButton("searchUpRadio", bundleString("SearchUp"));
        searchDownRadio = WidgetFactory.createRadioButton("searchDownRadio", bundleString("SearchDown"), true);

        ButtonGroup radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(searchUpRadio);
        radioButtonGroup.add(searchDownRadio);

        // --- buttons ---

        setExpandButtonsToFill(true);

        findNextButton = WidgetFactory.createButton("findNextButton", Bundles.get("action.find-next-command"));
        findNextButton.addActionListener(e -> startFindReplace(e.getSource()));
        findNextButton.setMnemonic('F');
        addActionButton(findNextButton);

        replaceButton = WidgetFactory.createButton("replaceButton", bundleString("Replace"));
        replaceButton.addActionListener(e -> startFindReplace(e.getSource()));
        replaceButton.setMnemonic('R');
        addActionButton(replaceButton);

        replaceAllButton = WidgetFactory.createButton("replaceAllButton", bundleString("ReplaceAll"));
        replaceAllButton.addActionListener(e -> startFindReplace(e.getSource()));
        replaceAllButton.setMnemonic('A');
        addActionButton(replaceAllButton);

        closeButton = WidgetFactory.createButton("closeButton", Bundles.get("common.close.button"));
        closeButton.addActionListener(e -> parent.finished());
        closeButton.setMnemonic('C');
        addActionButton(closeButton);

    }

    private void arrange() {

        GridBagHelper gbh;

        // --- options panel ---

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("Options")));
        optionsPanel.setPreferredSize(new Dimension(600, 140));

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().setInsets(5, 5, 5, 5);
        optionsPanel.add(isMatchCaseCheck, gbh.setMinWeightX().get());
        optionsPanel.add(searchUpRadio, gbh.setMaxWeightX().nextCol().spanX().get());
        optionsPanel.add(isWholeWordsCheck, gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        optionsPanel.add(searchDownRadio, gbh.setMaxWeightX().nextCol().spanX().get());
        optionsPanel.add(isUseRegexCheck, gbh.setMinWeightX().nextRowFirstCol().setWidth(1).get());
        optionsPanel.add(isWrapCheck, gbh.setMaxWeightX().nextCol().spanX().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillHorizontally();
        mainPanel.add(new JLabel(bundleString("FindText")), gbh.get());
        mainPanel.add(findStringCombo, gbh.nextCol().setMaxWeightX().spanX().get());
        mainPanel.add(isReplaceCheck, gbh.nextRowFirstCol().setMinWeightX().setWidth(1).get());
        mainPanel.add(replaceStringCombo, gbh.nextCol().setMaxWeightX().spanX().get());
        mainPanel.add(optionsPanel, gbh.nextRowFirstCol().setMaxWeightY().fillBoth().spanX().spanY().get());

        // --- base ---

        addContentPanel(mainPanel);
    }

    private KeyAdapter getCreateKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startFindReplace(
                            e.getSource() == findStringCombo.getEditor().getEditorComponent() ?
                                    findNextButton :
                                    replaceButton
                    );
                }
            }
        };
    }

    // --- handlers ---

    private void startFindReplace(Object button) {

        try {
            GUIUtilities.showWaitCursor();

            String find = getFindFieldText();
            String replacement = getReplaceFieldText();

            if (!isValidForReplace(find, replacement))
                return;

            addFind(find);

            TextAreaSearch.setTextComponent(textEditor.getEditorTextComponent());
            TextAreaSearch.setFindText(find);
            TextAreaSearch.setSearchDirection(searchUpRadio.isSelected() ?
                    TextAreaSearch.SEARCH_UP :
                    TextAreaSearch.SEARCH_DOWN
            );

            boolean useRegex = isUseRegexCheck.isSelected();
            TextAreaSearch.setUseRegex(useRegex);
            TextAreaSearch.setWholeWords(!useRegex && isWholeWordsCheck.isSelected());

            TextAreaSearch.setMatchCase(isMatchCaseCheck.isSelected());
            TextAreaSearch.setWrapSearch(isWrapCheck.isSelected());

            if (button == findNextButton) {
                TextAreaSearch.findNext(false, true);

            } else if (button == replaceButton) {

                if (!isReplaceCheck.isSelected())
                    return;

                addReplace(replacement);
                TextAreaSearch.setReplacementText(replacement);
                TextAreaSearch.findNext(true, true);

            } else if (button == replaceAllButton) {

                if (!isReplaceCheck.isSelected())
                    return;

                addReplace(replacement);
                TextAreaSearch.setReplacementText(replacement);
                TextAreaSearch.replaceAll();
            }

            findStringCombo.requestFocusInWindow();
            GUIUtils.scheduleGC();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private boolean isValidForReplace(String find, String replacement) {

        if (isReplaceCheck.isSelected() && find.compareTo(replacement) == 0) {
            GUIUtilities.displayErrorMessage(bundleString("message"));
            return false;
        }

        return true;
    }

    private String getReplaceFieldText() {
        return (String) (replaceStringCombo.getEditor().getItem());
    }

    private String getFindFieldText() {
        return (String) (findStringCombo.getEditor().getItem());
    }

    public void setToReplace() {
        setFindReplace(isReplaceCheck.isSelected());
    }

    public void setToRegex() {
        isWholeWordsCheck.setEnabled(!isUseRegexCheck.isSelected());
    }

    private void addFind(String s) {
        TextAreaSearch.addPrevFindValue(s);
    }

    private void addReplace(String s) {
        TextAreaSearch.addPrevReplaceValue(s);
    }

    private void setFindReplace(boolean replace) {

        if (!textEditor.getEditorTextComponent().isEditable())
            replace = false;

        isReplaceCheck.setSelected(replace);
        replaceStringCombo.setEditable(replace);
        replaceStringCombo.setEnabled(replace);
        replaceStringCombo.setOpaque(replace);
    }

    public Component getDefaultFocusComponent() {
        return findStringCombo;
    }

    @Override
    public void cleanup() {
        TextAreaSearch.setTextComponent(null);
    }

}
