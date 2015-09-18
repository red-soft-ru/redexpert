/*
 * LinkButton.java
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * Simple button behaving/looking like a hyperlink item.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class LinkButton extends JButton {
    
//    private static final Color LINK_COLOR = new Color(0, 124, 165);// Color.blue;//new Color(-16485995); 
//    private static final Color LINK_COLOR = new Color(-16622374);// Color.blue;//new Color(-16485995);

    private static final Color LINK_COLOR = new Color(-15641140);// Color.blue;//new Color(-16485995); 
    private static final Border LINK_BORDER = BorderFactory.createEmptyBorder(0, 0, 1, 0); 
    private static final Border HOVER_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, LINK_COLOR); 
    
    public LinkButton(String text) {
        super(text);
        init(); 
    }

    public LinkButton(Action action) {
        super(action);
        init();
    }
    
    private void init() {
        setBorder(null); 
        setBorder(LINK_BORDER); 
        setForeground(LINK_COLOR); 
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        setFocusPainted(false); 
        setRequestFocusEnabled(false); 
        setContentAreaFilled(false);
        addMouseListener(new LinkMouseListener());
    }
    
    private class LinkMouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent e){ 
            ((JComponent)e.getComponent()).setBorder(HOVER_BORDER);
        }
        public void mouseReleased(MouseEvent e){
            ((JComponent)e.getComponent()).setBorder(HOVER_BORDER); 
        } 
        public void mouseExited(MouseEvent e){ 
            ((JComponent)e.getComponent()).setBorder(LINK_BORDER); 
        }
    }; 
    
}

