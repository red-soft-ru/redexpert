/*
 * QueryEditorAutoCompletePopupPanel.java
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

package org.executequery.gui.editor.autocomplete;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import org.executequery.Constants;
import org.executequery.gui.editor.TypeAheadList;
import org.executequery.gui.editor.TypeAheadListProvider;
import org.underworldlabs.swing.plaf.UIUtils;

public class QueryEditorAutoCompletePopupPanel extends JPopupMenu implements TypeAheadListProvider {

    private static final Dimension PREFERRED_SIZE = new Dimension(460, 150);

    private static final int TIMER_DELAY = 1000;

    private TypeAheadList list;

    private List<AutoCompletePopupListener> listeners;

    private List<AutoCompleteListItem> values;

    private Timer timer;
    
    public QueryEditorAutoCompletePopupPanel() {

        init();
    }

    private void init() {

        setLayout(new BorderLayout());
        setVisible(false);

        list = new TypeAheadList(this);
        list.setCellRenderer(new AutoCompleteListItemCellRenderer());

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.getDefaultBorderColour()));

        setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
        setPreferredSize(PREFERRED_SIZE);
        
        timer = new Timer(0, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        
        listeners = new ArrayList<AutoCompletePopupListener>();
    }

    public String getSelectedValue() {

        Object selectedValue = list.getSelectedValue();
        if (selectedValue != null) {

            return selectedValue.toString();
        }

        return Constants.EMPTY;
    }

    public Object getSelectedItem() {

        return list.getSelectedValue();
    }

    public void addAutoCompletePopupListener(AutoCompletePopupListener autoCompletePopupListener) {

        listeners.add(autoCompletePopupListener);
    }

    protected void done() {
        
        timer.stop();
    }
    
    protected void reset(List<AutoCompleteListItem> values) {
        
        this.values = values;
        reset();
    }
    
    protected void scheduleReset(List<AutoCompleteListItem> values) {

        this.values = values;
        if (!timer.isRunning()) {
 
            timer.start();
        
        } else if (timer.getDelay() != TIMER_DELAY) {

            timer.stop();
            timer.setDelay(TIMER_DELAY);
            timer.restart();
        }
        
    }
    
    private void reset() {

        if (values == null || values.isEmpty()) {
            
            return;
        }
        
        Object selectedValue = list.getSelectedValue();
        
        list.resetValues(values);
        if (values != null && !values.isEmpty()) {

            if (selectedValue != null) {
                
                list.setSelectedValue(selectedValue, true);

            } else {
                
                selectListIndex(0);
            }
            
        }
            
    }
    
    protected void scrollSelectedIndexUp() {

        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex > 0) {

            selectListIndex(selectedIndex - 1);
        
        } else if (selectedIndex == 0) {
            
            selectListIndex(list.getModel().getSize() - 1);
        }

    }

    protected void scrollSelectedIndexDown() {

        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex < list.getModel().getSize() - 1) {

            selectListIndex(selectedIndex + 1);
        
        } else {
            
            selectListIndex(0);
        }

    }

    private static final int PAGE_SCROLL_SIZE = 5;

    protected void scrollSelectedIndexPageUp() {

        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex > 0) {

            int newIndex = selectedIndex - PAGE_SCROLL_SIZE;
            if (newIndex > 0) {

                selectListIndex(newIndex);

            } else {

                selectListIndex(0);
            }

        }

    }

    protected void scrollSelectedIndexPageDown() {

        int selectedIndex = list.getSelectedIndex();
        int modelSize = list.getModel().getSize();
        if (selectedIndex < modelSize - 1) {

            int newIndex = selectedIndex + PAGE_SCROLL_SIZE;
            if (newIndex < modelSize) {

                selectListIndex(newIndex);

            } else {

                selectListIndex(modelSize - 1);
            }

        }

    }

    private void selectListIndex(int index) {

        list.setSelectedIndex(index);
        list.ensureIndexIsVisible(index);
    }

    public void focusAndSelectList() {

        list.setListItemSelectedAndFocus(0);
    }

    public void listValueSelected(Object selectedValue) {

        firePopupSelectionMade();
        hidePopup();
    }

    public void refocus() {}

    protected void hidePopup() {

        if (isVisible()) {

            setVisible(false);
        }

        firePopupClosed();
    }

    private void firePopupClosed() {

        for (AutoCompletePopupListener listener : listeners) {

            listener.popupClosed();
        }

    }

    private void firePopupSelectionMade() {

        for (AutoCompletePopupListener listener : listeners) {

            listener.popupSelectionMade();
        }

    }

    private void firePopupSelectionCancelled() {

        for (AutoCompletePopupListener listener : listeners) {

            listener.popupSelectionCancelled();
        }

    }

    class ListSelectionCancelledAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {

            hidePopup();
            firePopupSelectionCancelled();
        }

    } // ListSelectionCancelledAction


    class PopupCancelledAction extends AbstractAction {

        public void actionPerformed(ActionEvent e) {

            hidePopup();
            firePopupSelectionCancelled();
        }

    } // PopupCancelledAction

}






