/*
 * PopupMenuButton.java
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

import javax.swing.*;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class PopupMenuButton extends RolloverButton {

    private final JPopupMenu menu;

    public PopupMenuButton(Icon icon, String toolTipText) {
        super();

        menu = createMenu();
        menu.addMenuKeyListener(new PopupMenuKeyListener());

        Action action = new PopupMenuAction();
        if (icon != null)
            action.putValue(Action.SMALL_ICON, icon);

        setAction(action);
        setToolTipText(toolTipText);
        setText(null);
    }

    public void addSeparator() {
        menu.addSeparator();
    }

    public void addMenuItem(JMenuItem menuItem) {
        menu.add(menuItem);
    }

    public void removeMenuItems() {
        menu.removeAll();
    }

    private JPopupMenu createMenu() {
        return new JPopupMenu();
    }

    private void showMenu() {
        if (menu != null) {
            menu.show(this, calculatePopupMenuX(), calculatePopupMenuY());
            menu.requestFocus();
        }
    }

    private int calculatePopupMenuY() {
        return getY() + getHeight() - 2;
    }

    private int calculatePopupMenuX() {
        return 0;
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        if (getAction() != null)
            getAction().putValue(Action.ACCELERATOR_KEY, keyStroke);
    }

    class PopupMenuKeyListener implements MenuKeyListener {

        @Override
        public void menuKeyReleased(MenuKeyEvent e) {

            if (e.getKeyCode() != KeyEvent.VK_ENTER)
                return;

            MenuElement[] menuElements = e.getMenuSelectionManager().getSelectedPath();
            if (menuElements.length == 2 && (menuElements[1] instanceof JMenuItem)) {

                JMenuItem menuItem = (JMenuItem) menuElements[1];
                ActionEvent action = new ActionEvent(menuItem, ActionEvent.ACTION_FIRST, menuItem.getActionCommand());
                menuItem.getAction().actionPerformed(action);

                menu.setVisible(false);
            }
        }

        @Override
        public void menuKeyPressed(MenuKeyEvent e) {
        }

        @Override
        public void menuKeyTyped(MenuKeyEvent e) {
        }

    } // PopupMenuKeyListener class

    private class PopupMenuAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            showMenu();
        }

    } // PopupMenuAction class

}
