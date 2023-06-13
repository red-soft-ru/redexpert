/*
 * InsertRowBeforeCommand.java
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

package org.executequery.actions.othercommands;

import org.executequery.GUIUtilities;
import org.executequery.gui.table.TableFunction;

import java.awt.event.ActionEvent;

/**
 * Executes insert row before current row in the parent table.
 *
 * @author Takis Diakoumis
 */
public class InsertRowBeforeCommand extends AbstractBaseCommand {

    private static final String ICON = "RowInsertBefore24.svg";

    public static final int CREATE_TABLE = 0;

    private TableFunction panel;

    public InsertRowBeforeCommand(TableFunction panel) {
        super(GUIUtilities.loadIcon(ICON));
        this.panel = panel;
    }

    public void execute(ActionEvent e) {
        panel.insertBefore();
    }

}

