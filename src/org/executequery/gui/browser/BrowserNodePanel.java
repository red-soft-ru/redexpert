/*
 * BrowserNodePanel.java
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

package org.executequery.gui.browser;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.underworldlabs.swing.GradientLabel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class BrowserNodePanel extends JPanel {
    
    protected static Border emptyBorder;
    protected GradientLabel gradientLabel;
    
    public BrowserNodePanel() {
        
        super(new BorderLayout());

        gradientLabel = new GradientLabel();
        add(gradientLabel, BorderLayout.NORTH);
    }
    
    static {
        emptyBorder = BorderFactory.createEmptyBorder(5,5,5,5);
    }
    
    protected void setContentPanel(JComponent panel) {
        add(panel, BorderLayout.CENTER);
    }
    
    public void setHeader(String text, ImageIcon icon) {
        gradientLabel.setText(text);
        gradientLabel.setIcon(icon);
    }
    
    public void setHeaderText(String text) {
        gradientLabel.setText(text);
    }
    
    public void setHeaderIcon(ImageIcon icon) {
        gradientLabel.setIcon(icon);
    }
    
}







