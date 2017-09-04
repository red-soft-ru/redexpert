/*
 * MinimumWidthActionButton.java
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

package org.executequery.components;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import org.underworldlabs.swing.DefaultButton;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
*/
public class MinimumWidthActionButton extends DefaultButton {

    private final int minimumWidth;
    
    public MinimumWidthActionButton(int minimumWidth,
                                  ActionListener actionListener, 
                                  String name, 
                                  String command) {

        super(name);
        this.minimumWidth = minimumWidth;
        setActionCommand(command);
        addActionListener(actionListener);
    }
    
    @Override
    public Dimension getPreferredSize() {

        Dimension dimension = super.getPreferredSize();
        dimension.width = Math.max(minimumWidth, dimension.width);

        return dimension;
    }

}






