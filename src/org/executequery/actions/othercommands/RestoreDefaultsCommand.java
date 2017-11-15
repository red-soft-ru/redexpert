/*
 * RestoreDefaultsCommand.java
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

import java.awt.event.ActionEvent;

import org.executequery.gui.prefs.UserPreferenceFunction;
import org.executequery.localization.Bundles;

/** <p>Restore system defaults command for respective preferences panels.
 *
 *  @author   Takis Diakoumis
 */
public class RestoreDefaultsCommand extends AbstractBaseCommand {
    
    private UserPreferenceFunction frame;
    
    public RestoreDefaultsCommand(UserPreferenceFunction frame) {
        super(Bundles.get(RestoreDefaultsCommand.class, "restoreDefaults"));
        this.frame = frame;
    }
    
    public void execute(ActionEvent e) {
        frame.restoreDefaults();
    }
    
}

