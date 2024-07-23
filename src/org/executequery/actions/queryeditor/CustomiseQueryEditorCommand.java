/*
 * CustomiseQueryEditorCommand.java
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

package org.executequery.actions.queryeditor;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.prefs.PropertiesPanel;
import org.executequery.gui.prefs.PropertyTypes;
import org.underworldlabs.swing.actions.BaseCommand;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takis Diakoumis
 */
public class CustomiseQueryEditorCommand implements BaseCommand {

    @Override
    public void execute(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {

            try {
                GUIUtilities.showWaitCursor();

                BaseDialog dialog = new BaseDialog(PropertiesPanel.TITLE, true);
                PropertiesPanel panel = new PropertiesPanel(dialog, PropertyTypes.EDITOR);

                dialog.addDisplayComponentWithEmptyBorder(panel);
                dialog.display();

            } finally {
                GUIUtilities.showNormalCursor();
            }
        });
    }
}
