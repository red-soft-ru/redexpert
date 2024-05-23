/*
 * ReleaseNotesCommand.java
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

package org.executequery.actions.helpcommands;

import org.executequery.actions.OpenFrameCommand;
import org.executequery.gui.ChangelogDialog;
import org.executequery.gui.InformationDialog;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.BaseCommand;

import java.awt.event.ActionEvent;

/**
 * Release Notes command execution.
 *
 * @author Takis Diakoumis
 */
public class ChangelogCommand extends OpenFrameCommand
        implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        new ChangelogDialog(
                Bundles.get("action.changelog-command"),
                Bundles.get("action.changelog-command.path"),
                InformationDialog.RESOURCE_PATH_VALUE,
                "UTF8"
        ).display();
    }

}
