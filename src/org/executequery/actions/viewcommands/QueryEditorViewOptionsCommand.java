/*
 * QueryEditorViewOptionsCommand.java
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

package org.executequery.actions.viewcommands;

import java.awt.event.ActionEvent;

import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.underworldlabs.util.SystemProperties;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class QueryEditorViewOptionsCommand extends AbstractViewOptionsCommand {
    
    private static final String EDITOR_DISPLAY_STATUSBAR = "editor.display.statusbar";
    
    private static final String EDITOR_DISPLAY_LINENUMS = "editor.display.linenums";

    public void viewEditorStatusBar(ActionEvent e) {

        setBooleanProperty(EDITOR_DISPLAY_STATUSBAR, selectionFromEvent(e));
        
        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_STATUSBAR);
    }

    public void viewEditorLineNumbers(ActionEvent e) {

        setBooleanProperty(EDITOR_DISPLAY_LINENUMS, selectionFromEvent(e));

        fireEditorPreferencesChangedEvent(EDITOR_DISPLAY_LINENUMS);
    }

    private void fireEditorPreferencesChangedEvent(String key) {

        EventMediator.fireEvent(
                new DefaultUserPreferenceEvent(this, key, 
                        UserPreferenceEvent.QUERY_EDITOR));
    }
    
    private void setBooleanProperty(String key, boolean value) {
        
        SystemProperties.setBooleanProperty(Constants.USER_PROPERTIES_KEY, key, value);
    }
    
}










