/*
 * FindAction.java
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

package org.executequery.gui.browser;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.plaf.UIUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Vector;

/**
 * Modified from the original by Santhosh Kumar
 * from http://www.jroller.com/santhosh/category/Swing
 *
 * @author Santhosh Kumar, Takis Diakoumis
 */
@SuppressWarnings("unchecked")
public abstract class FindAction<T> extends AbstractAction
        implements DocumentListener,
        KeyListener {

    private JPanel searchPanel;

    private JTextField searchField;

    private JPopupMenu popup;

    protected JList resultsList;

    public FindAction() {

        super("Incremental Search");

        putValue(Action.ACCELERATOR_KEY, INVOKE_KEY_STROKE);
        putValue(Action.SMALL_ICON, GUIUtilities.loadIcon("Zoom16.png"));

        init();
    }

    private void init() {

        searchPanel = new JPanel(new GridBagLayout());
        popup = new JPopupMenu();

        searchField = WidgetFactory.createTextField();
        searchField.setPreferredSize(new Dimension(270, 22));

        resultsList = initSearchResultsList();
        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setPreferredSize(new Dimension(300, 150));

        JLabel label = new JLabel(" Search: ");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.left = 3;
        gbc.insets.top = 3;
        gbc.insets.bottom = 3;
        searchPanel.add(label, gbc);
        gbc.gridx = 1;
        gbc.insets.right = 3;
        gbc.insets.left = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets.left = 3;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.BOTH;
        searchPanel.add(scrollPane, gbc);

        popup.setBorder(BorderFactory.createLineBorder(UIUtils.getDefaultBorderColour()));
        popup.add(searchPanel);

        // when the window containing the "comp" has registered Esc key
        // then on pressing Esc instead of search popup getting closed
        // the event is sent to the window. to overcome this we
        // register an action for Esc.
        searchField.registerKeyboardAction(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   popup.setVisible(false);
                                               }
                                           }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_FOCUSED);
    }

    private JList initSearchResultsList() {

        final JList list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ListCellRenderer listCellRenderer = getListCellRenderer();
        if (listCellRenderer != null) {

            list.setCellRenderer(listCellRenderer);
        }

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    listValueSelected((T) list.getSelectedValue());
                }
            }
        });

        list.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {
                    listValueSelected((T) list.getSelectedValue());
                } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    searchField.requestFocus();
                }
            }
        });

        return list;
    }

    public void listValueSelected(T selection) {

        hidePopup();
        listValueSelected(component, selection);
    }

    protected abstract ListCellRenderer getListCellRenderer();

    protected abstract void listValueSelected(JComponent component, T selection);

    protected void foundValues(List<T> values) {

        Vector<T> listData = new Vector<T>(values.size());
        listData.addAll(values);

        resultsList.setListData(listData);
    }

    protected final void hidePopup() {

        popup.setVisible(false);
        clearResultsListData();
    }

    private void clearResultsListData() {

        resultsList.setListData(new Object[0]);
    }

    private JComponent component = null;

    protected final JComponent getInstalledComponent() {

        return component;
    }

    protected boolean ignoreCase() {

        return ignoreCase;
    }

    private boolean ignoreCase;

    /*-------------------------------------------------[ ActionListener ]---------------------------------------------------*/

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchField)
            popup.setVisible(false);
        else {
//			component = (JComponent) e.getSource();
            ignoreCase = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;

            searchField.removeActionListener(this);
            searchField.removeKeyListener(this);
            searchField.getDocument().removeDocumentListener(this);
            initSearch(e);
            searchField.addActionListener(this);
            searchField.addKeyListener(this);
            searchField.getDocument().addDocumentListener(this);

            clearResultsListData();

            Rectangle rect = component.getVisibleRect();
//            popup.show(comp, rect.x = 5, rect.y - popup.getPreferredSize().height
//                    - 10);
            popup.show(component, rect.x = 80, rect.y);
            searchField.requestFocus();
        }
    }

    // can be overridden by subclasses to change initial search text etc.
    protected void initSearch(ActionEvent ae) {
        searchField.setText(""); // NOI18N
        searchField.setForeground(Color.black);
    }

    private void changed(Position.Bias bias) {
        // note: popup.pack() doesn't work for first character insert
//		popup.setVisible(false);
        popup.setVisible(true);

        String searchText = searchField.getText();

        searchField.requestFocus();
        searchField.setForeground(changed(component, searchText, bias) ? Color.black
                : Color.red);

        if (StringUtils.isBlank(searchText)) {

            clearResultsListData();
        }

    }

    // should search for given text and select item and
    // return true if search is successfull
    protected abstract boolean changed(JComponent comp, String text, Position.Bias bias);

    /*-----------------[ DocumentListener ]---------------------------*/

    public void insertUpdate(DocumentEvent e) {
        changed(null);
    }

    public void removeUpdate(DocumentEvent e) {
        changed(null);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    /*------------------[ KeyListener ]-------------------------------*/

	/*
	protected boolean shiftDown = false;
	protected boolean controlDown = false;
    */

    public void keyPressed(KeyEvent keyEvent) {

        int keyCode = keyEvent.getKeyCode();

        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_TAB) {

            setFirstListItemSelectedAndFocus();
        }
        if (keyCode == KeyEvent.VK_ALT) {
            keyEvent.setKeyCode(KeyEvent.VK_CONTROL);
        }
    }

    private void setFirstListItemSelectedAndFocus() {

        if (resultsList.getModel().getSize() > 0) {

            resultsList.setSelectedIndex(0);
            resultsList.requestFocus();
        }

    }

    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            e.consume();
        }
    }

    /*--------------[ Installation ]--------------*/

    public void install(JComponent component) {

        this.component = component;

        component.registerKeyboardAction(
                this, INVOKE_KEY_STROKE, JComponent.WHEN_IN_FOCUSED_WINDOW);

//		comp.registerKeyboardAction(this, KeyStroke.getKeyStroke('I',
//				KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK),
//				JComponent.WHEN_FOCUSED);
    }

    public static final KeyStroke INVOKE_KEY_STROKE =
            KeyStroke.getKeyStroke('I', KeyEvent.CTRL_MASK);

}







