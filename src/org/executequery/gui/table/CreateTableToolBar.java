/*
 * CreateTableToolBar.java
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

package org.executequery.gui.table;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the
 *           release of version 3.0.0beta1 has meant a
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 * @author Takis Diakoumis
 */
public class CreateTableToolBar extends JPanel implements ActionListener {

    /**
     * The parent panel where this toolbar will be attached
     */
    private final TableFunction parent;

    /**
     * Whether the move buttons are available
     */
    private final boolean canMove;

    /**
     * The insert row (column) after button
     */
    private JButton insertAfterButton;

    /**
     * The insert row (column) before button
     */
    private JButton insertBeforeButton;

    /**
     * The delete row (column) button
     */
    private JButton deleteRowButton;

    /**
     * The move row (column) up button
     */
    private JButton moveUpButton;

    /**
     * The move row (column) down button
     */
    private JButton moveDownButton;

    public CreateTableToolBar(TableFunction parent) {
        this(parent, true);
    }

    public CreateTableToolBar(TableFunction parent, boolean canMove) {
        super();
        setLayout(new GridBagLayout());
        this.parent = parent;
        this.canMove = canMove;
        initialiseButtons();
    }

    /**
     * <p>Creates the toolbar buttons and associates
     * these with the relevant listener.
     */
    private void initialiseButtons() {

        insertAfterButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnInsertAfter16.svg"),
                bundleString("InsertAfter"),
                null
        );

        insertBeforeButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnInsertBefore16.svg"),
                bundleString("InsertBefore"),
                null
        );

        deleteRowButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnDelete16.svg"),
                bundleString("DeleteSelection"),
                null
        );

        GridBagHelper gbh = new GridBagHelper();
        gbh.anchorNorth().setInsets(0, 0, 0, 1);

        add(insertAfterButton, gbh.get());
        add(insertBeforeButton, gbh.nextRowFirstCol().get());
        add(deleteRowButton, gbh.nextRowFirstCol().get());

        if (canMove) {

            moveUpButton = ActionUtilities.createToolbarButton(
                    this,
                    "Up16.svg",
                    bundleString("MoveUp"),
                    null
            );

            moveDownButton = ActionUtilities.createToolbarButton(
                    this,
                    "Down16.svg",
                    bundleString("MoveDown"),
                    null
            );

            add(moveUpButton, gbh.nextRowFirstCol().get());
            add(moveDownButton, gbh.nextRowFirstCol().get());
        }

    }

    /**
     * <p>Enables/disables as specified the buttons
     * insert before, move up and move down.
     *
     * @param enable <code>true</code> to enable these button or <code>false</code> to disable
     */
    public void enableButtons(boolean enable) {

        insertBeforeButton.setEnabled(enable);
        if (canMove) {
            moveUpButton.setEnabled(enable);
            moveDownButton.setEnabled(enable);
        }
    }

    /**
     * <p>Determines which button was selected and
     * calls the relevant method to execute that action.
     *
     * @param e the event initiating this action
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        Object button = e.getSource();
        if (button.equals(insertAfterButton)) parent.insertAfter();
        else if (button.equals(insertBeforeButton)) parent.insertBefore();
        else if (button.equals(deleteRowButton)) parent.deleteRow();
        else if (button.equals(moveUpButton)) parent.moveColumnUp();
        else if (button.equals(moveDownButton)) parent.moveColumnDown();

    }

    public static String bundleString(String key) {
        return Bundles.get(CreateTableToolBar.class, key);
    }

}
