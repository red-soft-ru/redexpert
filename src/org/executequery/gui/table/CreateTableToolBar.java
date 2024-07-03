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

import org.executequery.gui.WidgetFactory;
import org.executequery.gui.procedure.DefinitionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class CreateTableToolBar extends JPanel {

    private final boolean canMove;
    private final DefinitionPanel parent;

    private JButton moveUpButton;
    private JButton moveDownButton;
    private JButton deleteRowButton;
    private JButton addRowButton;

    public CreateTableToolBar(DefinitionPanel parent) {
        this(parent, true);
    }

    public CreateTableToolBar(DefinitionPanel parent, boolean canMove) {
        super();
        this.parent = parent;
        this.canMove = canMove;

        init();
        arrange();
    }

    private void init() {

        addRowButton = WidgetFactory.createRolloverButton(
                "addRowButton",
                bundleString("AddRow"),
                "icon_add",
                e -> parent.addRow()
        );

        deleteRowButton = WidgetFactory.createRolloverButton(
                "deleteRowButton",
                bundleString("DeleteSelection"),
                "icon_delete",
                e -> parent.deleteRow()
        );

        moveUpButton = WidgetFactory.createRolloverButton(
                "moveUpButton",
                bundleString("MoveUp"),
                "icon_move_up",
                e -> parent.moveRowUp()
        );

        moveDownButton = WidgetFactory.createRolloverButton(
                "moveDownButton",
                bundleString("MoveDown"),
                "icon_move_down",
                e -> parent.moveRowDown()
        );

    }

    private void arrange() {
        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorNorth().setInsets(5, 5, 5, 5);
        add(addRowButton, gbh.get());
        add(deleteRowButton, gbh.nextRow().topGap(0).get());
        if (canMove) {
            add(moveUpButton, gbh.nextRow().get());
            add(moveDownButton, gbh.nextRow().get());
        }

        add(new JPanel(), gbh.nextRow().setMaxWeightY().fillVertical().spanY().get());
    }

    public void enableButtons(boolean enable) {
        moveUpButton.setEnabled(enable);
        moveDownButton.setEnabled(enable);
        deleteRowButton.setEnabled(enable);
        addRowButton.setEnabled(enable);
    }

    public static String bundleString(String key) {
        return Bundles.get(CreateTableToolBar.class, key);
    }

}
