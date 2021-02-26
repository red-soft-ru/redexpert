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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Executes the Help command.<br>
 * This will open the system help in a separate window.
 *
 * @author Takis Diakoumis
 */
public class HelpCommand extends AbstractUrlLauncherCommand {

    private static final String URL = "guide/RedXpert_Guide-ru.pdf";

    @Override
    public String url() {
        java.net.URL[] urls = new URL[0];

            try {
                urls = MiscUtils.loadURLs(URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        String url = urls[0].toString();
        url = url.replace("bin/guide/RedXpert_Guide-ru.pdf", "guide/RedXpert_Guide-ru.pdf");
        return url;
    }

}

