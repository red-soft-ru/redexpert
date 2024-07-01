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

import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * List selection panel base.
 *
 * @author Takis Diakoumis
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListSelectionPanel extends ActionPanel
        implements ListSelection {

    private static final int DEFAULT_ROW_HEIGHT = 20;
    private List<ListSelectionPanelListener> listeners;

    // --- GUI components ---

    private JLabel availableLabel;
    private JLabel selectedLabel;
    private JList availableList;
    private JList selectedList;

    private Vector available;
    private Vector selections;

    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton selectOneButton;
    private JButton selectAllButton;
    private JButton removeOneButton;
    private JButton removeAllButton;
    private JButton movePageUpButton;
    private JButton movePageDownButton;

    // ---

    public ListSelectionPanel() {
        this(null);
    }

    public ListSelectionPanel(Vector availableVector) {
        this(
                Bundles.get(ListSelectionPanel.class, "AvailableColumns"),
                Bundles.get(ListSelectionPanel.class, "SelectedColumns"),
                availableVector
        );
    }

    public ListSelectionPanel(String availableText, String selectedText) {
        this(availableText, selectedText, null);
    }

    public ListSelectionPanel(String availLabel, String selectLabel, Vector availableVector) {
        super(new GridBagLayout());

        init();
        arrange();
        createAvailableList(availableVector);
        setLabelText(availLabel, selectLabel);
    }

    private void init() {
        selections = new Vector();
        listeners = new ArrayList<>();

        // --- labels ---

        availableLabel = new JLabel();
        selectedLabel = new JLabel();

        // --- lists ---

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() < 2)
                    return;

                Object source = e.getSource();
                if (source == availableList) {
                    selectOneAction();

                } else if (source == selectedList)
                    removeOneAction();
            }
        };

        availableList = new JList();
        availableList.addMouseListener(mouseListener);
        availableList.setFixedCellHeight(DEFAULT_ROW_HEIGHT);

        selectedList = new JList();
        selectedList.addMouseListener(mouseListener);
        selectedList.setFixedCellHeight(DEFAULT_ROW_HEIGHT);

        //  --- buttons ---

        selectOneButton = WidgetFactory.createRolloverButton(
                "selectOneButton",
                bundleString("selectOneAction"),
                "icon_move_next",
                e -> selectOneAction()
        );

        selectAllButton = WidgetFactory.createRolloverButton(
                "selectAllButton",
                bundleString("selectAllAction"),
                "icon_move_next_all",
                e -> selectAllAction()
        );

        removeOneButton = WidgetFactory.createRolloverButton(
                "removeOneButton",
                bundleString("removeOneAction"),
                "icon_move_previous",
                e -> removeOneAction()
        );

        removeAllButton = WidgetFactory.createRolloverButton(
                "removeAllButton",
                bundleString("removeAllAction"),
                "icon_move_previous_all",
                e -> removeAllAction()
        );

        moveUpButton = WidgetFactory.createRolloverButton(
                "moveUpButton",
                bundleString("moveSelectionUp"),
                "icon_move_up",
                e -> moveSelectionUp()
        );

        movePageUpButton = WidgetFactory.createRolloverButton(
                "movePageUpButton",
                bundleString("moveSelectionPageUp"),
                "icon_move_up_all",
                e -> moveSelectionPageUp()
        );

        moveDownButton = WidgetFactory.createRolloverButton(
                "moveDownButton",
                bundleString("moveSelectionDown"),
                "icon_move_down",
                e -> moveSelectionDown()
        );

        movePageDownButton = WidgetFactory.createRolloverButton(
                "movePageDownButton",
                bundleString("moveSelectionPageDown"),
                "icon_move_down_all",
                e -> moveSelectionPageDown()
        );
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- scroll panes ---

        Dimension listDim = new Dimension(180, 185);

        JScrollPane availableScrollPane = new JScrollPane(availableList);
        availableScrollPane.setPreferredSize(listDim);

        JScrollPane selectedScrollPane = new JScrollPane(selectedList);
        selectedScrollPane.setPreferredSize(listDim);

        // --- selection buttons panel ---

        JPanel selectionButtonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorCenter();
        selectionButtonPanel.add(selectOneButton, gbh.topGap(15).bottomGap(5).get());
        selectionButtonPanel.add(selectAllButton, gbh.nextRow().topGap(0).get());
        selectionButtonPanel.add(removeOneButton, gbh.nextRow().get());
        selectionButtonPanel.add(removeAllButton, gbh.nextRow().bottomGap(15).get());

        // --- movable buttons panel ---

        JPanel movableButtonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorCenter();
        movableButtonPanel.add(movePageUpButton, gbh.topGap(15).bottomGap(5).get());
        movableButtonPanel.add(moveUpButton, gbh.nextRow().topGap(0).get());
        movableButtonPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbh.nextRow().get());
        movableButtonPanel.add(moveDownButton, gbh.nextRow().get());
        movableButtonPanel.add(movePageDownButton, gbh.nextRow().bottomGap(15).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 0, 5).anchorNorthWest().fillHorizontally();
        mainPanel.add(availableLabel, gbh.get());
        mainPanel.add(selectedLabel, gbh.nextCol().nextCol().rightGap(5).get());
        mainPanel.add(availableScrollPane, gbh.nextRowFirstCol().topGap(0).rightGap(0).fillBoth().setMaxWeightX().spanY().get());
        mainPanel.add(selectionButtonPanel, gbh.nextCol().fillVertical().setMinWeightX().get());
        mainPanel.add(selectedScrollPane, gbh.nextCol().fillBoth().setMaxWeightX().get());
        mainPanel.add(movableButtonPanel, gbh.nextCol().rightGap(5).fillVertical().setMinWeightX().get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());
    }

    public void setLabelText(String availableText, String selectedText) {
        availableLabel.setText(availableText);
        selectedLabel.setText(selectedText);
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

        fireChange(ListSelectionPanelEvent.CLEAR);
    }

    public void createAvailableList(List values) {
        createAvailableList(values.toArray(new Object[0]));
    }

    public void createAvailableList(Object[] values) {

        available = new Vector();
        Collections.addAll(available, values);

        selections.clear();

        availableList.setListData(available);
        selectedList.setListData(selections);
        fireChange(ListSelectionPanelEvent.ADD);
    }

    public void createAvailableList(Vector vector) {

        if (vector == null)
            return;

        available = vector;
        selections.clear();

        availableList.setListData(available);
        selectedList.setListData(selections);
        fireChange(ListSelectionPanelEvent.ADD);
    }

    public void addAvailableItem(Object obj) {

        if (available == null)
            available = new Vector();

        available.add(obj);
        selections.clear();

        availableList.setListData(available);
        selectedList.setListData(selections);
        fireChange(ListSelectionPanelEvent.ADD);
    }

    @Override
    public void removeAllAction() {

        if (selections == null || selections.isEmpty())
            return;

        available.addAll(selections);
        selections.clear();

        availableList.setListData(available);
        selectedList.setListData(selections);
        fireChange(ListSelectionPanelEvent.DESELECT);
    }

    @Override
    public void removeOneAction() {

        if (selectedList.isSelectionEmpty())
            return;

        int selectedIndex = selectedList.getSelectedIndex();
        for (Object selection : selectedList.getSelectedValuesList()) {
            available.add(selection);
            selections.remove(selection);
        }

        selectedList.setListData(selections);
        availableList.setListData(available);
        selectedList.setSelectedIndex(selectedIndex);
        fireChange(ListSelectionPanelEvent.DESELECT);
    }

    @Override
    public void selectAllAction() {

        if (available == null)
            return;

        selections.addAll(available);
        available.clear();

        selectedList.setListData(selections);
        availableList.setListData(available);
        fireChange(ListSelectionPanelEvent.SELECT);
    }

    @Override
    public void selectOneAction() {

        if (availableList.isSelectionEmpty())
            return;

        int selectedIndex = availableList.getSelectedIndex();
        for (Object selection : availableList.getSelectedValuesList()) {
            selections.add(selection);
            available.remove(selection);
        }

        availableList.setListData(available);
        selectedList.setListData(selections);
        availableList.setSelectedIndex(selectedIndex);
        fireChange(ListSelectionPanelEvent.SELECT);
    }

    public void selectOneAction(int indexAvailable) {

        Object selection = available.get(indexAvailable);
        selections.add(selection);
        available.remove(selection);

        availableList.setListData(available);
        selectedList.setListData(selections);
        fireChange(ListSelectionPanelEvent.SELECT);
    }

    public void selectOneStringAction(String object) {

        if (available.isEmpty() || !(available.get(0) instanceof String))
            return;

        for (int i = 0; i < getAvailableValues().size(); i++) {
            if (getAvailableValues().get(i).toString().contentEquals(object)) {
                selectOneAction(i);
                break;
            }
        }
    }

    public Vector getSelectedValues() {
        return selections;
    }

    public Vector getAvailableValues() {
        return available;
    }

    public boolean hasSelections() {
        return !selections.isEmpty();
    }

    public void moveSelectionDown() {

        int selectedIndex = selectedList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex == selections.size() - 1)
            return;

        Object selectedValue = selectedList.getSelectedValue();
        selections.removeElementAt(selectedIndex);
        selections.add(selectedIndex + 1, selectedValue);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(selectedIndex + 1);

        fireChange(ListSelectionPanelEvent.MOVE);
    }

    public void moveSelectionPageDown() {

        int selectedIndex = selectedList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex == selections.size() - 1)
            return;

        Object selectedValue = selectedList.getSelectedValue();
        selections.removeElementAt(selectedIndex);
        selections.add(selectedValue);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(selections.size() - 1);

        fireChange(ListSelectionPanelEvent.MOVE);
    }

    public void moveSelectionUp() {

        int selectedIndex = selectedList.getSelectedIndex();
        if (selectedIndex < 1)
            return;

        Object selectedValue = selectedList.getSelectedValue();
        selections.removeElementAt(selectedIndex);
        selections.add(selectedIndex - 1, selectedValue);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(selectedIndex - 1);

        fireChange(ListSelectionPanelEvent.MOVE);
    }

    public void moveSelectionPageUp() {

        int selectedIndex = selectedList.getSelectedIndex();
        if (selectedIndex < 1)
            return;

        Object selectedValue = selectedList.getSelectedValue();
        selections.removeElementAt(selectedIndex);
        selections.add(0, selectedValue);
        selectedList.setListData(selections);
        selectedList.setSelectedIndex(0);

        fireChange(ListSelectionPanelEvent.MOVE);
    }

    public void addListSelectionPanelListener(ListSelectionPanelListener listener) {
        listeners.add(listener);
    }

    private void fireChange(int type) {
        ListSelectionPanelEvent event = new ListSelectionPanelEvent(this, type);
        for (ListSelectionPanelListener listener : listeners)
            listener.changed(event);
    }

}
