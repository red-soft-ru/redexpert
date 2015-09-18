/*
 * NewCommand.java
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

package org.executequery.actions.filecommands;

import org.executequery.actions.toolscommands.EditorCommand;

/** 
 * <p>Command execution for File | New.
 * This will open a new Query Editor frame only.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class NewCommand extends EditorCommand { //OpenFrameCommand
//                        implements BaseCommand {

    /*
    public void execute(ActionEvent e) {
        if (!isFrameOpen(QueryEditor.TITLE)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GUIUtilities.addInternalFrame(QueryEditor.TITLE,
                                                  QueryEditor.FRAME_ICON, 
                                                  new QueryEditor(),
                                                  true, true, true, true, false);
                }
            });
        }
    }
*/
}














