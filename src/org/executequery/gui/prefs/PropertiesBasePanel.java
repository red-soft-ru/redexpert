/*
 * PropertiesBasePanel.java
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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.actions.othercommands.RestoreDefaultsCommand;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.util.SystemProperties;

/**
 * User preferences base panel.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
abstract class PropertiesBasePanel extends JPanel
                                     implements UserPreferenceFunction {

    /** common font used across props panels */
    protected static Font panelFont;
    
    /** common layout constraints acroos props panels */
    protected static GridBagConstraints contentPanelConstraints;

    static {
        panelFont = new Font("dialog", Font.PLAIN, 12);
        contentPanelConstraints = new GridBagConstraints(
                                            1, 1, 1, 1, 1.0, 1.0,
                                            GridBagConstraints.NORTHWEST, 
                                            GridBagConstraints.BOTH,
                                            new Insets(5, 5, 0, 5), 0, 0);
    }
    
    public PropertiesBasePanel() {

        super(new GridBagLayout());        
        setBorder(BorderFactory.createLineBorder(GUIUtilities.getDefaultBorderColour()));
        
        try {

            init();

        } catch (Exception e) {
        
            e.printStackTrace();
        }
        
    }

    protected void addContent(JPanel panel) {
        add(panel, contentPanelConstraints);
    }
    
    private void init() throws Exception {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(new DefaultButton(new RestoreDefaultsCommand(this)));
        add(panel, new GridBagConstraints(
                            1, 2, 1, 1, 0, 0,
                            GridBagConstraints.SOUTHEAST, 
                            GridBagConstraints.NONE,
                            new Insets(0, 0, 5, 0), 0, 0));
        
    }

    protected String stringUserProperty(String key) {
    
        return SystemProperties.getProperty(Constants.USER_PROPERTIES_KEY, key);
    }

}

