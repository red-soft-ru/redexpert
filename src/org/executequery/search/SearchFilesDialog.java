/*
 * SearchFilesDialog.java
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

package org.executequery.search;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;
import org.underworldlabs.swing.DisabledField;

/**
 *
 * @author   Takis Diakoumis
 */
public class SearchFilesDialog extends AbstractBaseDialog implements FileSearchView {
    
    /** The search text area */
    private JTextArea findTextArea;
    /** The replace text area */
    private JTextArea replaceTextArea;
    /** The file types combo box */
    private JComboBox fileTypesCombo;
    /** The search paths combo box */
    private JComboBox pathCombo;
    /** The match case check box */
    private JCheckBox matchCaseCheck;
    /** The replace check box */
    private JCheckBox replaceCheck;
    /** The whole words check box */
    private JCheckBox wholeWordsCheck;
    /** The search subdirs check box */
    private JCheckBox searchSubdirsCheck;
    /** The use regex check box */
    private JCheckBox regexCheck;
    
    /** The results area scroller */
    private JScrollPane resultsScroll;

    /** The results list */
    private JList resultsList;
    /** The results summary bar */
    private DisabledField resultsSummary;
    /** The search utility performing the work */
    private FileSearch fileSearch;
    
    public SearchFilesDialog() {
        
        super(GUIUtilities.getParentFrame(), "Search Files", false);
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        pack();
        setResizable(true);
        this.setLocation(GUIUtilities.getLocationForDialog(this.getSize()));
        setVisible(true);
        requestFocus();
        findTextArea.requestFocus();
        findTextArea.setCaretPosition(0);
    }
    
    private void jbInit() throws Exception {
        wholeWordsCheck = new JCheckBox("Whole words only", true);
        matchCaseCheck = new JCheckBox("Match case");
        searchSubdirsCheck = new JCheckBox("Search Subdirectories", true);
        replaceCheck = new JCheckBox("Replace:");
        regexCheck = new JCheckBox("Regular expressions");
        
        findTextArea = new JTextArea();
        findTextArea.setLineWrap(true);
        findTextArea.setWrapStyleWord(true);
        JScrollPane findScroll = new JScrollPane(findTextArea);
        
        replaceTextArea = new JTextArea();
        replaceTextArea.setLineWrap(true);
        replaceTextArea.setWrapStyleWord(true);
        JScrollPane replaceScroll = new JScrollPane(replaceTextArea);
        
        Insets textAreaMargin = new Insets(2,2,2,2);
        findTextArea.setMargin(textAreaMargin);
        replaceTextArea.setMargin(textAreaMargin);
        
        Dimension textAreaDim = new Dimension(200, 45);
        findScroll.setPreferredSize(textAreaDim);
        replaceScroll.setPreferredSize(textAreaDim);
        
        replaceCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableReplaceTextArea(replaceCheck.isSelected());
            }
        });
        
        enableReplaceTextArea(false);
        
        fileTypesCombo = WidgetFactory.createComboBox(FileSearch.getTypesValues());
        pathCombo = WidgetFactory.createComboBox(FileSearch.getPathValues());
        fileTypesCombo.setEditable(true);
        pathCombo.setEditable(true);
        
        Dimension comboDim = new Dimension(fileTypesCombo.getWidth(), 22);
        fileTypesCombo.setPreferredSize(comboDim);
        pathCombo.setPreferredSize(comboDim);
        
        JButton browseButton = new JButton("Browse");
        browseButton.setMargin(new Insets(0,0,0,0));
        browseButton.setPreferredSize(new Dimension(85, 22));
        
        JButton findButton = new DefaultPanelButton("Find");
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.close.button"));
        
        resultsList = new JList();
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsScroll = new JScrollPane(resultsList);
        resultsScroll.setPreferredSize(textAreaDim);
        
        resultsSummary = new DisabledField();
        resultsSummary.setPreferredSize(new Dimension(100, 19));
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Find:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.left = 0;
        panel.add(findScroll, gbc);
        gbc.gridy = 1;
        panel.add(replaceScroll, gbc);
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 2;
        panel.add(fileTypesCombo, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.left = 2;
        panel.add(replaceCheck, gbc);
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.left = 5;
        panel.add(new JLabel("File Types:"), gbc);
        gbc.gridy = 3;
        panel.add(new JLabel("Search Path:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 0;
        panel.add(pathCombo, gbc);
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(browseButton, gbc);
        
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        gbc.insets.top = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        optionsPanel.add(matchCaseCheck, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 20;
        optionsPanel.add(searchSubdirsCheck, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets.left = 0;
        optionsPanel.add(wholeWordsCheck, gbc);
        gbc.gridx = 1;
        gbc.insets.left = 20;
        optionsPanel.add(regexCheck, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(optionsPanel, gbc);
        
        gbc.gridy = 5;
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        panel.add(findButton, gbc);
        gbc.gridx = 2;
        gbc.insets.left = 0;
        gbc.weightx = 0;
        panel.add(cancelButton, gbc);
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.left = 5;
        panel.add(resultsScroll, gbc);
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        panel.add(resultsSummary, gbc);
        
        regexCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wholeWordsCheck.setEnabled(!regexCheck.isSelected()); }
        });
        
        ActionListener buttonListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e); }
        };
        
        browseButton.addActionListener(buttonListener);
        findButton.addActionListener(buttonListener);
        cancelButton.addActionListener(buttonListener);
        
        fileSearch = new FileSearch(this);
        
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setPreferredSize(new Dimension(550, 500));
        
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(panel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
                            GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
                            new Insets(5, 5, 5, 5), 0, 0));
        
        this.setResizable(false);
        
    }
    
    public void enableReplaceTextArea(boolean enable) {
        replaceTextArea.setEnabled(enable);
        replaceTextArea.setOpaque(enable);
    }
    
    public void finished() {
        
        if (replaceCheck.isSelected()) {
            //      resultsScroll.getViewport().setViewPosition(new Point (0, resultsList.getHeight()));
            //    JScrollBar verticalScrollBar = resultsScroll.getVerticalScrollBar();
            //    verticalScrollBar.setValue(verticalScrollBar.getVisibleAmount());
            
            int last = fileSearch.getSearchResults().size() - 1;
            
            //      Rectangle bounds = resultsList.getBounds();
            //      bounds.x = 0;
            //      bounds.y = 0;
            //      resultsList.paintImmediately(bounds);
            
            //      resultsList.setSelectedIndex(last);
            resultsList.ensureIndexIsVisible(last);
            resultsList.ensureIndexIsVisible(last);
        }
        
    }
    
    public void setListData(Vector listData) {
        resultsList.setListData(listData);
        resultsList.ensureIndexIsVisible(listData.size()-1);
    }
    
    public void setResultsSummary(String text) {
        resultsSummary.setText(text);
        resultsSummary.repaint();
    }
    
    public void appendResults(String results) {
        /*
        resultsTextArea.append(results);
        resultsTextArea.setCaretPosition(resultsTextArea.getText().length());
         */
    }
    
    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Browse")) {
            
            FileChooserDialog fileChooser = null;
            String searchPath = (String)pathCombo.getSelectedItem();
            
            if (searchPath != null && searchPath.length() > 0) {
                File currentDir = new File(searchPath);
                
                if (currentDir.exists())
                    fileChooser = new FileChooserDialog(currentDir.getAbsolutePath());
                else
                    fileChooser = new FileChooserDialog();
                
            }
            else {
                fileChooser = new FileChooserDialog();
            }

            fileChooser.setDialogTitle("Search Path");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            int result = fileChooser.showDialog(this, "Select");
            
            if (result == JFileChooser.CANCEL_OPTION) {
                return;
            }
            
            File file = fileChooser.getSelectedFile();
            
            fileSearch.setSearchPath(file.getAbsolutePath());
            pathCombo.setModel(new DefaultComboBoxModel(fileSearch.getPathValues()));
            
        }
        else if (command.equals("Close")) {
            dispose();
        }
        else if (command.equals("Find")) {
            String searchText = findTextArea.getText();            
            if (searchText == null || searchText.length() == 0) {
                GUIUtilities.displayErrorMessage("You must enter search text");
                return;
            }

            String replaceText = null;
            boolean replacingText = replaceCheck.isSelected();

            if (replacingText) {
                replaceText = replaceTextArea.getText();
                if (replaceText == null || replaceText.length() == 0) {
                    GUIUtilities.displayErrorMessage("You must enter replacement text");
                    return;
                }

                //resultsTextArea.setText("");
                //resultsScroll.getViewport().add(resultsTextArea);

            }            
            else {
                resultsScroll.getViewport().add(resultsList);
            }

            String searchExtension = (String)fileTypesCombo.getSelectedItem();            
            if (searchExtension == null || searchExtension.length() == 0) {
                searchExtension = FileSearch.WILDCARD;
            }

            fileSearch.setSearchExtension(searchExtension);
            fileTypesCombo.setModel(new DefaultComboBoxModel(FileSearch.getTypesValues()));
            fileTypesCombo.setSelectedItem(searchExtension);

            String searchPath = (String)pathCombo.getSelectedItem();
            if (searchPath == null || searchPath.length() == 0) {
                GUIUtilities.displayErrorMessage("You must select a search path");
                return;
            }

            fileSearch.setSearchPath(searchPath);
            pathCombo.setModel(new DefaultComboBoxModel(FileSearch.getPathValues()));
            pathCombo.setSelectedItem(searchPath);

            File file = new File(searchPath);
            if (!file.exists()) {
                GUIUtilities.displayErrorMessage("The path or file entered does not exist");
                return;
            }

            fileSearch.setSearchText(searchText);

            if (replaceText != null)
                fileSearch.setReplaceText(replaceText);

            boolean useRegex = regexCheck.isSelected();
            fileSearch.setUsingRegex(useRegex);

            if (useRegex) {
                fileSearch.setFindWholeWords(false);
            } else {
                fileSearch.setFindWholeWords(wholeWordsCheck.isSelected());
            }

            fileSearch.setReplacingText(replacingText);
            fileSearch.setSearchingSubdirs(searchSubdirsCheck.isSelected());
            fileSearch.setMatchingCase(matchCaseCheck.isSelected());

            fileSearch.doSearch();
            
        }
        
    }
    
    class ReplacementOffsets {
        private int start;
        private int end;
        
        public ReplacementOffsets(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        public void setEnd(int end) {
            this.end = end;
        }
        
        public int getEnd() {
            return end;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getStart() {
            return start;
        }
        
    } // class ReplacementOffsets
    
}

















