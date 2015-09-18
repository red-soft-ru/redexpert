/*
 * AbstractQueryEditorCommand.java
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

package org.executequery.actions.queryeditor;

import javax.swing.JPanel;

import org.executequery.GUIUtilities;
import org.executequery.actions.othercommands.AbstractBaseCommand;
import org.executequery.gui.editor.QueryEditor;

abstract class AbstractQueryEditorCommand extends AbstractBaseCommand {

    protected final boolean isQueryEditorTheCentralPanel() {
        
        JPanel panel = GUIUtilities.getSelectedCentralPane();
        
        return (panel instanceof QueryEditor);
    }

    protected final QueryEditor queryEditor() {
        
        return (QueryEditor) GUIUtilities.getSelectedCentralPane();
    }
    
}










