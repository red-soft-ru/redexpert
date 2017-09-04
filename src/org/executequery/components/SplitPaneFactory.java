/*
 * SplitPaneFactory.java
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

import java.awt.Component;

import javax.swing.JSplitPane;

import org.executequery.GUIUtilities;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.swing.FlatSplitPane;

public class SplitPaneFactory {

    public JSplitPane createHorizontal() {
        
        return create(JSplitPane.HORIZONTAL_SPLIT);
    }
    
    public JSplitPane createVertical() {
        
        return create(JSplitPane.VERTICAL_SPLIT);
    }
    
    public JSplitPane create(int orientation) {

        if (usesCustomSplitPane()) {
        
            return new FlatSplitPane(orientation);

        } else {
            
            return new JSplitPane(orientation);
        }

    }
    
    public JSplitPane create(int orientation, Component leftComponent, Component rightComponent) {
        
        if (usesCustomSplitPane()) {
            
            return new FlatSplitPane(orientation, leftComponent, rightComponent);
            
        } else {
            
            return new JSplitPane(orientation, leftComponent, rightComponent);
        }
        
    }
    
    public boolean usesCustomSplitPane() {
        
        return !(usesJavaSplitPane());
    }
    
    public boolean usesJavaSplitPane() {
        
        LookAndFeelType lookAndFeelType = GUIUtilities.getLookAndFeel();
        return lookAndFeelType == LookAndFeelType.PLUGIN || lookAndFeelType == LookAndFeelType.NATIVE 
                || lookAndFeelType == LookAndFeelType.GTK;
    }
    
}


