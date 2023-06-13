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

package org.executequery.gui.procedure;

import org.executequery.GUIUtilities;
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
 *
 * @author   Takis Diakoumis
 */
public class CreateProcedureToolBar extends JPanel
                                implements ActionListener {

    /**
     * The parent panel where this tool bar will be attached
     */
    private final DefinitionPanel parent;

    /** The insert row (column) after button */
    private JButton insertAfterButton;

    /** The insert row (column) before button */
    private JButton insertBeforeButton;

    /** The delete row (column) button */
    private JButton deleteRowButton;

    /** The move row (column) up button */
    private JButton moveUpButton;

    /** The move row (column) down button */
    private JButton moveDownButton;

    /**
     * Whether the move buttons are available
     */
    private final boolean canMove;

    public CreateProcedureToolBar(DefinitionPanel parent) {
        this(parent, true);
    }

    public CreateProcedureToolBar(DefinitionPanel parent, boolean canMove) {
        super();
        setLayout(new GridBagLayout());
        this.parent = parent;
        this.canMove = canMove;
        initialiseButtons();
    }
    
    /** <p>Creates the tool bar buttons and associates
     *  these with the relevant listener. */
    private void initialiseButtons() {

        insertAfterButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnInsertAfter16.svg"),
                "Insert a value after the current selection", 
                null);

        insertBeforeButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnInsertBefore16.svg"),
                "Insert a value before the current selection",
                null);

        deleteRowButton = ActionUtilities.createToolbarButton(
                this,
                GUIUtilities.getAbsoluteIconPath("ColumnDelete16.svg"),
                "Delete the selected value",
                null);

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(GridBagHelper.DEFAULT_CONSTRAINTS);
        gbh.defaults();
        gbh.anchorNorth();
        gbh.setInsets(0, 2, 0, 0);
        add(insertAfterButton, gbh.get());
        add(insertBeforeButton, gbh.nextRow().get());
        add(deleteRowButton, gbh.nextRow().get());

        if (canMove) {
            moveUpButton = ActionUtilities.createToolbarButton(
                    this,
                    "Up16.svg",
                    "Move the selection up",
                    null);

            moveDownButton = ActionUtilities.createToolbarButton(
                    this,
                    "Down16.svg",
                    "Move the selection down",
                    null);
            add(moveUpButton, gbh.nextRow().get());
            add(moveDownButton, gbh.nextRow().get());
            add(new JPanel(), gbh.nextRow().spanY().get());
        }
        
    }
    
    /** <p>Enables/disables as specified the buttons
     *  insert before, move up and move down.
     *
     *  @param enable <code>true</code> to enable these buttons
     *         <code>false</code> to disable these buttons
     */
    public void enableButtons(boolean enable) {
        insertBeforeButton.setEnabled(enable);
        
        if (canMove) {
            moveUpButton.setEnabled(enable);
            moveDownButton.setEnabled(enable);
        }
    }
    
    /** <p>Determines which button was selected and
     *  calls the relevant method to execute that action.
     *
     *  @param e the event initiating this action
     */
    public void actionPerformed(ActionEvent e) {
        Object button = e.getSource();
        
        if (button == insertAfterButton)
            parent.insertAfter();
        
        else if (button == insertBeforeButton)
            parent.insertBefore();
        
        else if (button == deleteRowButton)
            parent.deleteRow();
        
        else if (button == moveUpButton)
            parent.moveColumnUp();
        
        else if (button == moveDownButton)
            parent.moveColumnDown();
        
    }
    
}















