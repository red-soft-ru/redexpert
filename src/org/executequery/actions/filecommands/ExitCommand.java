/*
 * ExitCommand.java
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

package org.executequery.actions.filecommands;

import org.executequery.Application;
import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Command execution for File | Exit.
 *
 * @author Takis Diakoumis
 */
public class ExitCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        if (GUIUtilities.displayConfirmDialog(Bundles.getCommon("exit-confirmation")) != JOptionPane.YES_OPTION)
            return;
        Application.getInstance().exitProgram();
    }

}











