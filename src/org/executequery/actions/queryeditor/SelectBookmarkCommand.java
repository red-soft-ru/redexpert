/*
 * SelectBookmarkCommand.java
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

import java.awt.event.ActionEvent;

import org.executequery.repository.QueryBookmarks;

/** 
 * <p>The Query Editor's select bookmark command.
 *
 * @author   Takis Diakoumis
 */
public class SelectBookmarkCommand extends AbstractQueryEditorCommand {

    public void execute(ActionEvent e) {

        if (isQueryEditorTheCentralPanel()) {

            String name = e.getActionCommand();
            queryEditor().setEditorText(loadQuery(name));
        }

    }

    private String loadQuery(String name) {
        QueryBookmarks bookmarks = QueryBookmarks.getInstance();
        return bookmarks.findBookmarkByName(name).getQuery().trim();
    }

}











