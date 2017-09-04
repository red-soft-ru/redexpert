/*
 * ErdSelectionDialog.java
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

package org.executequery.gui.erd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.executequery.GUIUtilities;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.AbstractBaseDialog;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ErdSelectionDialog extends AbstractBaseDialog {
    
    /** The ERD parent panel */
    private ErdViewerPanel parent;
    /** The table selection panel */
    private ErdSelectionPanel selectionPanel;
    
    public ErdSelectionDialog(ErdViewerPanel parent) {
        super(GUIUtilities.getParentFrame(), "Add Table", true);
        this.parent = parent;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        pack();
        setLocation(GUIUtilities.getLocationForDialog(getSize()));        
        setVisible(true);
    }
    
    private void jbInit() throws Exception {
        Container c = this.getContentPane();
        c.setLayout(new GridBagLayout());
        
        selectionPanel = new ErdSelectionPanel();
        
        JButton addButton = new DefaultPanelButton(Bundles.get("common.add.button"));
        JButton cancelButton = new DefaultPanelButton(Bundles.get("common.cancel.button"));
        
        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e); }
        };
        
        addButton.addActionListener(btnListener);
        cancelButton.addActionListener(btnListener);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        c.add(selectionPanel, gbc);
        gbc.insets.top = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        c.add(addButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        c.add(cancelButton, gbc);
        
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
    }
    
    /** <p>Performs the respective action upon selection
     *  of a button within this dialog.
     *
     *  @param the <code>ActionEvent</code>
     */
    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Cancel")) {
            dispose();
        }
        else if (command.equals("Add")) {
            
            if (!selectionPanel.hasSelections()) {
                GUIUtilities.displayErrorMessage("You must select at least one table.");
                return;
            }
            
            setVisible(false);
            
            parent.setDatabaseConnection(selectionPanel.getDatabaseConnection());
            new ErdGenerateProgressDialog(selectionPanel.getSelectedValues(),
                                          parent,
                                          selectionPanel.getSchema());
            dispose();
        }
        
    }
    
}





