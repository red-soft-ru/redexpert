/*
 * PropertiesDefaults.java
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

package org.executequery.gui.prefs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.executequery.gui.prefs.PropertiesPanel;

/**
 * resets defaults on propertie frame
 * <P>
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class PropertiesDefaults extends JPanel
                                implements ActionListener {
    
    public PropertiesDefaults(PropertiesPanel f) {
        super(new GridBagLayout());
        try  {
            init();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes the state of this instance.
     */
    private void init() throws Exception {
        
        JButton restoreButton = new JButton("Restore Defaults");
        restoreButton.setPreferredSize(new Dimension(135,30));
        restoreButton.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.top = 20;
        this.add(new JLabel("Restores all preferences to system default values."), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        this.add(restoreButton, gbc);
    }

    public void actionPerformed(ActionEvent e) {
        //frame.restoreAllDefaults(); 
    }

}










