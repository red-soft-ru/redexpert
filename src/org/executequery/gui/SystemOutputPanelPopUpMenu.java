/*
 * SystemOutputPanelPopUpMenu.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

package org.executequery.gui;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.executequery.GUIUtilities;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SystemOutputPanelPopUpMenu extends ReadOnlyTextPanePopUpMenu {

    public SystemOutputPanelPopUpMenu(ReadOnlyTextPane readOnlyTextPane) {

        super(readOnlyTextPane);
        add(createMenuItem("Reset", "reset", "Clear the system output pane and reset the output log file"));
    }

    public void reset(ActionEvent e) {

        String message = "Are you sure you want to reset the system activity log?";
        if (GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION) {

            LogRepository logRepository = (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
            logRepository.reset(LogRepository.ACTIVITY);
            clear(e);
        }

    }

}




