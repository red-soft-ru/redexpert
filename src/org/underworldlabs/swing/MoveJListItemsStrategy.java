/*
 * MoveJListItemsStrategy.java
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

package org.underworldlabs.swing;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class MoveJListItemsStrategy {

    private final JList list;
    
    private static final int MOVE_DOWN = 1;
    
    private static final int MOVE_UP = -1;
    
    public MoveJListItemsStrategy(JList list) {

        if (!(list.getModel() instanceof DefaultListModel)) {

            throw new IllegalArgumentException(
                    "Model in specified list must be an instance of DefaultListModel");
        }

        this.list = list;
    }

    public void moveSelectionDown() {

        if (noSelection() || lastElementSelected()) {

            return;
        }

        moveSelection(MOVE_DOWN);
        
    }

    public void moveSelectionUp() {

        if (noSelection() || firstElementSelected()) {

            return;
        }

        moveSelection(MOVE_UP);
    }

    private void moveSelection(int increment) {

        int index = list.getSelectedIndex();
        Object element = list.getSelectedValue();

        DefaultListModel model = modelFromList();
        
        model.remove(index);
        model.add(index + increment, element);

        list.setSelectedIndex(index + increment);
    }

    private DefaultListModel modelFromList() {
        return (DefaultListModel)list.getModel();
    }
    
    private boolean firstElementSelected() {

        return (list.getSelectedIndex() == 0);
    }

    private boolean lastElementSelected() {
        
        return (modelFromList().lastElement() == list.getSelectedValue());
    }

    private boolean noSelection() {

        return list.isSelectionEmpty();
    }
    
}










