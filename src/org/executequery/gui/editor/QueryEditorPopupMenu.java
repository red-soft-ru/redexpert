/*
 * QueryEditorPopupMenu.java
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

package org.executequery.gui.editor;

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryEditorPopupMenu extends JPopupMenu
        implements MouseListener {

    private final List<JMenuItem> executingButtons;
    private final List<JMenuItem> executableButtons;
    private final List<JMenuItem> transactionButtons;

    public QueryEditorPopupMenu() {
        executingButtons = new ArrayList<>();
        executableButtons = new ArrayList<>();
        transactionButtons = new ArrayList<>();

        init();
        statementFinished();
    }

    private void init() {

        JMenu findTextMenu = MenuItemFactory.createMenu(Bundles.get("menu.search"));
        findTextMenu.add(createMenuItem("find-command", null));
        findTextMenu.add(createMenuItem("find-next-command", null));
        findTextMenu.add(createMenuItem("find-previous-command", null));

        JMenu replaceTextMenu = MenuItemFactory.createMenu(Bundles.get("common.replace"));
        replaceTextMenu.add(createMenuItem("replace-command", null));
        replaceTextMenu.add(createMenuItem("replace-next-command", null));

        JMenu changeCaseMenu = MenuItemFactory.createMenu(Bundles.get("menu.edit.change-case"));
        changeCaseMenu.add(createMenuItem("to-upper-case-command", null));
        changeCaseMenu.add(createMenuItem("to-lower-case-command", null));
        changeCaseMenu.add(createMenuItem("to-camel-case-command", null));
        changeCaseMenu.add(createMenuItem("to-underscore-command", null));

        JMenu duplicateTextMenu = MenuItemFactory.createMenu(Bundles.get("common.duplicate"));
        duplicateTextMenu.add(createMenuItem("duplicate-row-up-command", null, executableButtons));
        duplicateTextMenu.add(createMenuItem("duplicate-row-down-command", null, executableButtons));

        JMenu moveTextMenu = MenuItemFactory.createMenu(Bundles.get("common.move"));
        moveTextMenu.add(createMenuItem("move-row-up-command", null, executableButtons));
        moveTextMenu.add(createMenuItem("move-row-down-command", null, executableButtons));
        moveTextMenu.add(createMenuItem("shift-text-left-command", null, executableButtons));
        moveTextMenu.add(createMenuItem("shift-text-right-command", null, executableButtons));

        // ---

        add(createMenuItem("copy-command", Bundles.get("common.copy.button")));
        add(createMenuItem("cut-command", Bundles.get("common.cut")));
        add(createMenuItem("paste-command", Bundles.get("common.paste")));

        addSeparator();
        add(createMenuItem("execute-script-command", null, executableButtons));
        add(createMenuItem("execute-statement-command", null, executableButtons));
        add(createMenuItem("execute-in-profiler-command", null, executableButtons));
        add(createMenuItem("stop-execution-command", null, executingButtons));

        addSeparator();
        add(createMenuItem("commit-command", null, executableButtons, transactionButtons));
        add(createMenuItem("rollback-command", null, executableButtons, transactionButtons));

        addSeparator();
        add(findTextMenu);
        add(replaceTextMenu);
        add(createMenuItem("goto-command", null));

        addSeparator();
        add(createMenuItem("editor-format-sql-command", null, executableButtons));
        add(changeCaseMenu);
        add(duplicateTextMenu);
        add(moveTextMenu);

        addSeparator();
        add(createMenuItem("customise-query-editor-command", Bundles.get("preferences.Preferences"), executableButtons));
        add(createMenuItem("help-command", Bundles.get("common.help.button"), executableButtons));
    }

    @SafeVarargs
    private final JMenuItem createMenuItem(String command, String text, List<JMenuItem>... lists) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(ActionBuilder.get(command));
        menuItem.setIcon(null);
        if (!MiscUtils.isNull(text))
            menuItem.setText(text);

        Arrays.stream(lists).forEach(list -> list.add(menuItem));

        return menuItem;
    }

    public void statementExecuting() {
        setExecutableButtonsEnabled(false);
        setExecutingButtonsEnabled(true);
    }

    public void statementFinished() {
        setExecutableButtonsEnabled(true);
        setExecutingButtonsEnabled(false);
    }

    public void setCommitMode(boolean autoCommit) {
        setTransactionButtonsEnabled(!autoCommit);
    }

    private void setTransactionButtonsEnabled(boolean enable) {
        transactionButtons.forEach(menuItem -> menuItem.setEnabled(enable));
    }

    private void setExecutingButtonsEnabled(boolean enable) {
        executingButtons.forEach(menuItem -> menuItem.setEnabled(enable));
    }

    private void setExecutableButtonsEnabled(boolean enable) {
        executableButtons.forEach(menuItem -> menuItem.setEnabled(enable));
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger())
            show(e.getComponent(), e.getX(), e.getY());
    }

    // --- MouseListener impl ---

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // ---

    @Override
    public void removeAll() {
        executingButtons.clear();
        executableButtons.clear();
        transactionButtons.clear();

        super.removeAll();
    }

}
