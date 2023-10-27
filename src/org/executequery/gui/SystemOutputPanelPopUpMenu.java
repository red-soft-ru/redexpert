/*
 * SystemOutputPanelPopUpMenu.java
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

package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.localization.Bundles;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Takis Diakoumis
 */
public class SystemOutputPanelPopUpMenu extends ReadOnlyTextPanePopUpMenu {

    public SystemOutputPanelPopUpMenu(ReadOnlyTextPane readOnlyTextPane) {

        super(readOnlyTextPane);
        add(createMenuItem(bundleString("reset"), "reset", bundleString("reset.tooltip")));
    }

    public void reset(ActionEvent e) {

        String message = bundleString("reset.confirm");
        if (GUIUtilities.displayConfirmDialog(message) == JOptionPane.YES_OPTION) {

            LogRepository logRepository = (LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID);
            logRepository.reset(LogRepository.ACTIVITY);
            clear(e);
        }

    }

    private static String bundleString(String key) {
        return Bundles.get(SystemOutputPanelPopUpMenu.class, key);
    }

}





