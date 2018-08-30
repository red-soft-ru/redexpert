/*
 * HelpCommand.java
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

import org.executequery.actions.AbstractUrlLauncherCommand;
import org.underworldlabs.util.MiscUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Executes the Help command.<br>
 * This will open the system help in a separate window.
 *
 * @author Takis Diakoumis
 */
public class HelpCommand extends AbstractUrlLauncherCommand {

    private static final String URL = "guide/RedExpert_Guide-ru.pdf";
    private static final String URL2 = "../guide/RedExpert_Guide-ru.pdf";

    @Override
    public String url() {
        java.net.URL[] urls = new URL[0];
        File file = new File(URL);
        if (file.exists()) {
            try {
                urls = MiscUtils.loadURLs(URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                urls = MiscUtils.loadURLs(URL2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return urls[0].toString();
    }

}

