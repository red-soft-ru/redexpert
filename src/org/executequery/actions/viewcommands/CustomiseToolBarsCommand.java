/*
 * CustomiseToolBarsCommand.java
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

package org.executequery.actions.viewcommands;

import java.awt.event.ActionEvent;

import javax.swing.SwingUtilities;

import org.executequery.GUIUtilities;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.prefs.PropertiesPanel;
import org.executequery.gui.prefs.PropertyTypes;
import org.underworldlabs.swing.actions.BaseCommand;

/** 
 * 
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class CustomiseToolBarsCommand implements BaseCommand {

    public void execute(ActionEvent e) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            
                try {
                
                    GUIUtilities.showWaitCursor();

                    BaseDialog dialog = new BaseDialog(PropertiesPanel.TITLE, true);

                    PropertiesPanel panel = new PropertiesPanel(
                            dialog, PropertyTypes.TOOLBAR_GENERAL);

                    dialog.addDisplayComponentWithEmptyBorder(panel);
                    dialog.display();
                    
                } finally {
                    
                    GUIUtilities.showNormalCursor();
                }

            }
        });
        
    }
    
}











