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
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.actions.ReflectiveAction;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * <p>Find replace for text components.
 *
 * @author Takis Diakoumis
 */
public class FindReplaceDialog extends DefaultActionButtonsPanel
        implements ActionListener,
        ActiveComponent {

    public static final String TITLE = Bundles.get("action.FindAndReplace");

    public static final int FIND = 0;
    public static final int REPLACE = 1;

    private JButton findNextButton;
    private JButton closeButton;
    private JButton replaceButton;
    private JButton replaceAllButton;

    private JCheckBox wholeWordsCheck;
    private JCheckBox regexCheck;
    private JCheckBox matchCaseCheck;
    private JCheckBox replaceCheck;
    private JCheckBox wrapCheck;

    private JRadioButton searchUpRadio;
    private JRadioButton searchDownRadio;

    private JComboBox findField;
    private JComboBox replaceField;
    private final TextEditor textEditor;

    private final ActionContainer parent;

    public FindReplaceDialog(ActionContainer parent, int type, TextEditor textEditor) {
        this.textEditor = textEditor;
        this.parent = parent;
        init();
        setFindReplace(type == REPLACE);
    }

    private void init() {

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        Dimension optionsDim = new Dimension(600, 140);
        optionsPanel.setPreferredSize(optionsDim);


        JTextComponent textComponent = textEditor.getEditorTextComponent();
        String selectedText = textComponent.getSelectedText();

        if (selectedText != null && selectedText.length() > 0) {
            addFind(selectedText);
        }

        findField = WidgetFactory.createComboBox(TextAreaSearch.getPrevFindValues());
        replaceField = WidgetFactory.createComboBox(TextAreaSearch.getPrevReplaceValues());
        findField.setEditable(true);
        replaceField.setEditable(true);

        KeyAdapter keyListener = createKeyListener();
        findFieldTextEditor().addKeyListener(keyListener);
        replaceFieldTextEditor().addKeyListener(keyListener);

        wholeWordsCheck = new JCheckBox(Bundles.get("action.WholeWordsCheck"));
        matchCaseCheck = new JCheckBox(Bundles.get("action.MatchCaseCheck"));
        wrapCheck = new JCheckBox(Bundles.get("action.WrapCheck"), true);

        replaceCheck = ActionUtilities.createCheckBox(Bundles.get("action.Replace") + ":", "setToReplace");
        replaceCheck.setEnabled(textEditor.getEditorTextComponent().isEditable());
        regexCheck = ActionUtilities.createCheckBox(Bundles.get("action.RegularExpressions"), "setToRegex");

        searchUpRadio = new JRadioButton(Bundles.get("action.SearchUp"));
        searchDownRadio = new JRadioButton(Bundles.get("action.SearchDown"), true);

        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(searchUpRadio);
        btnGroup.add(searchDownRadio);

        findNextButton = new DefaultButton(Bundles.get("action.find-next-command"));
        replaceButton = new DefaultButton(Bundles.get("action.Replace"));
        replaceAllButton = new DefaultButton(Bundles.get("action.ReplaceAll"));
        closeButton = ActionUtilities.createButton(Bundles.get("common.close.button"), "close");

        setExpandButtonsToFill(true);

        addActionButton(findNextButton);
        addActionButton(replaceButton);
        addActionButton(replaceAllButton);
        addActionButton(closeButton);

        findNextButton.setMnemonic('F');
        replaceButton.setMnemonic('R');
        replaceAllButton.setMnemonic('A');
        closeButton.setMnemonic('C');

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        Insets ins = new Insets(15, 10, 0, 5);
        gbc.insets = ins;
        gbc.anchor = GridBagConstraints.WEST;
        optionsPanel.add(matchCaseCheck, gbc);
        gbc.gridy = 1;
        gbc.insets.top = 0;
        optionsPanel.add(wholeWordsCheck, gbc);
        gbc.gridy = 2;
        gbc.insets.bottom = 10;
        optionsPanel.add(regexCheck, gbc);
        gbc.insets.bottom = 10;
        gbc.insets.left = 20;
        gbc.insets.right = 5;
        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        optionsPanel.add(wrapCheck, gbc);
        gbc.insets.bottom = 0;
        gbc.gridy = 1;
        optionsPanel.add(searchDownRadio, gbc);
        gbc.gridy = 0;
        gbc.insets.top = 15;
        optionsPanel.add(searchUpRadio, gbc);
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.insets.left = 10;
        gbc.insets.top = 13;
        gbc.insets.right = 5;
        panel.add(new JLabel(Bundles.get("action.FindText")), gbc);
        gbc.insets.top = 10;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.gridy = 1;
        panel.add(replaceCheck, gbc);
        gbc.insets.top = 10;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        panel.add(findField, gbc);
        gbc.gridy = 1;
        gbc.insets.top = 5;
        panel.add(replaceField, gbc);
        gbc.insets.left = 5;
        gbc.gridwidth = 4;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets.bottom = 10;
        panel.add(optionsPanel, gbc);

        addContentPanel(panel);

        ReflectiveAction action = new ReflectiveAction(this);
        replaceCheck.addActionListener(action);
        regexCheck.addActionListener(action);
        closeButton.addActionListener(action);

        findNextButton.addActionListener(this);
        replaceButton.addActionListener(this);
        replaceAllButton.addActionListener(this);

    }

    private JTextField replaceFieldTextEditor() {
        return editorFromComboBox(replaceField);
    }

    private JTextField findFieldTextEditor() {
        return editorFromComboBox(findField);
    }

    private JTextField editorFromComboBox(JComboBox comboBox) {
        return (JTextField) comboBox.getEditor().getEditorComponent();
    }

    private KeyAdapter createKeyListener() {
        KeyAdapter keyListener = new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                keyPressedInFields(e);
            }
        };
        return keyListener;
    }

    private void keyPressedInFields(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ENTER) {
            JTextField comboField = findFieldTextEditor();

            if (comboField == e.getSource()) {
                startFindReplace(findNextButton);
            } else {
                startFindReplace(replaceButton);
            }

        }

    }

    public Component getDefaultFocusComponent() {
        return findField;
    }

    public void cleanup() {
        TextAreaSearch.setTextComponent(null);
    }

    private void startFindReplace(Object button) {

        try {

            GUIUtilities.showWaitCursor();

            String find = getFindFieldText();
            String replacement = getReplaceFieldText();

            if (!isValidForReplace(find, replacement)) {

                return;
            }

            addFind(find);


            TextAreaSearch.setTextComponent(textEditor.getEditorTextComponent());
            TextAreaSearch.setFindText(find);
            TextAreaSearch.setSearchDirection(searchUpRadio.isSelected() ?
                    TextAreaSearch.SEARCH_UP :
                    TextAreaSearch.SEARCH_DOWN);

            boolean useRegex = regexCheck.isSelected();
            TextAreaSearch.setUseRegex(useRegex);

            if (useRegex) {

                TextAreaSearch.setWholeWords(false);

            } else {

                TextAreaSearch.setWholeWords(wholeWordsCheck.isSelected());
            }

            TextAreaSearch.setMatchCase(matchCaseCheck.isSelected());
            TextAreaSearch.setWrapSearch(wrapCheck.isSelected());

            if (button == findNextButton) {

                TextAreaSearch.findNext(false, true);

            } else if (button == replaceButton) {

                if (!replaceCheck.isSelected()) {

                    return;
                }

                addReplace(replacement);
                TextAreaSearch.setReplacementText(replacement);
                TextAreaSearch.findNext(true, true);

            } else if (button == replaceAllButton) {

                if (!replaceCheck.isSelected()) {

                    return;
                }

                addReplace(replacement);
                TextAreaSearch.setReplacementText(replacement);
                TextAreaSearch.replaceAll();
            }

            findField.requestFocusInWindow();
            GUIUtils.scheduleGC();

        } finally {

            GUIUtilities.showNormalCursor();
        }

    }

    private boolean isValidForReplace(String find, String replacement) {

        if (replaceCheck.isSelected() && find.compareTo(replacement) == 0) {

            GUIUtilities.displayErrorMessage(
                    Bundles.get("message.FindReplaceDialog"));

            return false;
        }

        return true;
    }

    private String getReplaceFieldText() {
        return (String) (replaceField.getEditor().getItem());
    }

    private String getFindFieldText() {
        return (String) (findField.getEditor().getItem());
    }

    public void setToReplace(ActionEvent e) {
        setFindReplace(replaceCheck.isSelected());
    }

    public void setToRegex(ActionEvent e) {
        wholeWordsCheck.setEnabled(!regexCheck.isSelected());
    }

    public void close(ActionEvent e) {
        parent.finished();
    }

    public void actionPerformed(ActionEvent e) {
        startFindReplace(e.getSource());
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
        {
            replaceCheck.setSelected(replace);
            replaceField.setEditable(replace);
            replaceField.setEnabled(replace);
            replaceField.setOpaque(replace);
        }
    }

}


