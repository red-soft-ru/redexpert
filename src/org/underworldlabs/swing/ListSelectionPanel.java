/*
 * ListSelectionPanel.java
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

package org.underworldlabs.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.util.IconUtilities;

/**
 * List selection panel base.
 *
 * @author   Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListSelectionPanel extends ActionPanel
                                implements ListSelection {

    /** the available object list */
    private JList availableList;
    
    /** the selected object list */
    private JList selectedList;
    
    /** the selections made collection */
    private Vector selections;
    
    /** the available objects collection */
    private Vector available;
    
    /** label above the available object list */
    private JLabel availableLabel;
    
    /** label above the selected object list */
    private JLabel selectedLabel;

    private static final int DEFAULT_ROW_HEIGHT = 20;
    
    public ListSelectionPanel() {
        this(null);
    }

    public ListSelectionPanel(Vector v) {
        this("Available Columns:", "Selected Columns:", v);
    }
    
    public ListSelectionPanel(String availLabel, String selectLabel) {
        this(availLabel, selectLabel, null);
    }
    
    public ListSelectionPanel(String availLabel, String selectLabel, Vector v) {
        super(new GridBagLayout());        
        try  {
            init();
            selections = new Vector();
            createAvailableList(v);
            setLabelText(availLabel, selectLabel);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        // create the labels
        availableLabel = new JLabel();
        selectedLabel = new JLabel();

        // initialise the buttons
        JButton selectOneButton = ActionUtilities.createButton(
                    this, 
                    "selectOneAction",
//                    IconUtilities.loadDefaultIconResource("SelectOne16.png", true),
                    IconUtilities.loadDefaultIconResource("Forward16.png", true),
                    "Select one");

        JButton selectAllButton = ActionUtilities.createButton(
                    this, 
                    "selectAllAction",
                    IconUtilities.loadDefaultIconResource("SelectAll16.png", true),
                    "Select all");

        JButton removeOneButton = ActionUtilities.createButton(
                    this, 
                    "removeOneAction",
//                    IconUtilities.loadDefaultIconResource("RemoveOne16.png", true),
                    IconUtilities.loadDefaultIconResource("Previous16.png", true),
                    "Remove one");

        JButton removeAllButton = ActionUtilities.createButton(
                    this, 
                    "removeAllAction", 
                    IconUtilities.loadDefaultIconResource("RemoveAll16.png", true),
                    "Remove all");

        // reset the button insets
        Insets buttonInsets = UIManager.getInsets("Button.margin");
        if (buttonInsets != null) {
            selectOneButton.setMargin(buttonInsets);
            selectAllButton.setMargin(buttonInsets);
            removeOneButton.setMargin(buttonInsets);
            removeAllButton.setMargin(buttonInsets);
        }

        JButton moveUpButton = ActionUtilities.createButton(
                                                    this, 
                                                    "Up16.png", 
                                                    "Move selection up", 
                                                    "moveSelectionUp");

        JButton moveDownButton = ActionUtilities.createButton(
                                                    this, 
                                                    "Down16.png", 
                                                    "Move selection down", 
                                                    "moveSelectionDown");

        // initialise the lists
        availableList = new JList();
        selectedList = new JList();

        availableList.setFixedCellHeight(DEFAULT_ROW_HEIGHT);
        selectedList.setFixedCellHeight(DEFAULT_ROW_HEIGHT);
        
        // create the list scroll panes
        JScrollPane availableScrollPane = new JScrollPane(availableList);
        JScrollPane selectedScrollPane = new JScrollPane(selectedList);
        
        Dimension listDim = new Dimension(180, 185);
        availableScrollPane.setPreferredSize(listDim);
        selectedScrollPane.setPreferredSize(listDim);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        // first column - available list
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(availableLabel, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy++;
        gbc.insets.top = 2;
        add(availableScrollPane, gbc);
        
        // second column - selection buttons
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets.top = 10;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(selectOneButton, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        buttonPanel.add(selectAllButton, gbc);
        gbc.gridy++;
        buttonPanel.add(removeOneButton, gbc);
        gbc.gridy++;
        buttonPanel.add(removeAllButton, gbc);

        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(buttonPanel, gbc);
        
        // third column - selected list
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(selectedLabel, gbc);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridy++;
        gbc.insets.top = 2;
        gbc.insets.bottom = 0;
        add(selectedScrollPane, gbc);

        // fourth column - move buttons
        JPanel buttonMovePanel = new JPanel(new GridBagLayout());
        gbc.insets.top = 10;
        gbc.insets.left = 0;
        gbc.insets.bottom = 5;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        buttonMovePanel.add(moveUpButton, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        buttonMovePanel.add(new JLabel("Move"), gbc);
        gbc.gridy++;
        buttonMovePanel.add(moveDownButton, gbc);

        gbc.gridy = 0;
        gbc.gridx = 3;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        add(buttonMovePanel, gbc);

        ListMouseSelectionListener mouseSelectionListener = new ListMouseSelectionListener();
        availableList.addMouseListener(mouseSelectionListener);
        selectedList.addMouseListener(mouseSelectionListener);
    }
    
    public void setLabelText(String avail, String select) {
        availableLabel.setText(avail);
        selectedLabel.setText(select);
    }
    
    public void clear() {
        if (available != null) {
            available.clear();
            availableList.setListData(available);
        }
        if (selections != null) {
            selections.clear();
            selectedList.setListData(selections);
        }
    }

    public void createAvailableList(List values) {
        createAvailableList(values.toArray(new Object[values.size()]));
    }
    
    public void createAvailableList(Object[] values) {
        available = new Vector(values.length);
        for (int i = 0; i < values.length; i++) {
            available.add(values[i]);
        }
        
        availableList.setListData(available);
        selections.clear();
        selectedList.setListData(selections);
    }
    
    public void createAvailableList(Vector v) {
        if (v == null) {
            return;
        }
        
        available = v;
        availableList.setListData(available);
        selections.clear();
        selectedList.setListData(selections);
    }
    
    public void removeAllAction() {
        if (selections == null || selections.size() == 0) {
            return;
        }
        for (int i = 0, n = selections.size(); i < n; i++) {
            available.add(selections.elementAt(i));
        }
        
        availableList.setListData(available);
        selections.clear();
        selectedList.setListData(selections);
    }
    
    public void removeOneAction() {
        if (selectedList.isSelectionEmpty()) {
            return;
        }

        int index = selectedList.getSelectedIndex();
        List selectedObjects = selectedList.getSelectedValuesList();
        for (Object selection : selectedObjects) {
            available.add(selection);
            selections.remove(selection);
        }

        selectedList.setListData(selections);
        availableList.setListData(available);
        selectedList.setSelectedIndex(index);
    }
    
    public void selectAllAction() {
        if (available == null) {
            return;
        }
        for (int i = 0, n = available.size(); i < n; i++) {
            selections.add(available.elementAt(i));
        }
        selectedList.setListData(selections);
        available.clear();
        availableList.setListData(available);
    }
    
    public void selectOneAction() {
        if (availableList.isSelectionEmpty()) {
            return;
        }
        
        int index = availableList.getSelectedIndex();
        List selectedObjects = availableList.getSelectedValuesList();
        for (Object selection : selectedObjects) {
            selections.add(selection);
            available.remove(selection);
        }
        
        availableList.setListData(available);
        selectedList.setListData(selections);
        availableList.setSelectedIndex(index);
    }
    
    public Vector getSelectedValues() {
        return selections;
    }
    
    public boolean hasSelections() {
        return selections.size() > 0;
    }
    
    public void moveSelectionDown() {
        if (selectedList.isSelectionEmpty() ||
            selectedList.getSelectedIndex() == selections.size() - 1) {
            return;
        }

        int index = selectedList.getSelectedIndex();
        Object move = selectedList.getSelectedValue();
        selections.removeElementAt(index);
        selections.add(index + 1, move);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(index + 1);
    }
    
    public void moveSelectionUp() {
        if (selectedList.isSelectionEmpty() ||
                    selectedList.getSelectedIndex() == 0) {
            return;
        }

        int index = selectedList.getSelectedIndex();
        Object move = selectedList.getSelectedValue();
        selections.removeElementAt(index);
        selections.add(index - 1, move);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(index - 1);
    }

    
    class ListMouseSelectionListener extends MouseAdapter {
        
        public void mouseClicked(MouseEvent e) {
            
            if (e.getClickCount() == 2) {
                
                Object source = e.getSource();
                if (source == availableList) {
                    
                    selectOneAction();
                
                } else if (source == selectedList) {
                    
                    removeOneAction();
                }
                
            }

        }
        
    }
    
}

