/*
 * ErdSQLViewer.java
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

package org.executequery.gui.erd;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;

import org.executequery.localization.Bundles;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ErdSQLViewer extends ErdPrintableDialog {
    
    /** The controller for the ERD viewer */
    private ErdViewerPanel parent;
    
    public ErdSQLViewer(ErdViewerPanel parent) {
        super("SQL Text");
        this.parent = parent;
        
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        display();
        
    }
    
    private void jbInit() throws Exception {
        sqlText.setSQLText(parent.getAllSQLText());
        sqlText.setPreferredSize(new Dimension(530,175));
        sqlText.setBorder(BorderFactory.createEtchedBorder());
        
        JButton cancelButton = new JButton(Bundles.get("common.close.button"));
        JButton okButton = new JButton("Execute");
        
        Dimension btnDim = new Dimension(80, 30);
        cancelButton.setPreferredSize(btnDim);
        okButton.setPreferredSize(btnDim);
        
        ActionListener btnListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buttons_actionPerformed(e); }
        };
        
        cancelButton.addActionListener(btnListener);
        okButton.addActionListener(btnListener);
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        c.add(sqlText, gbc);
        gbc.insets.top = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridy = 1;
        c.add(okButton, gbc);
        gbc.insets.left = 0;
        gbc.gridx = 1;
        gbc.weightx = 0;
        c.add(cancelButton, gbc);
        
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
    }
    
    private void execute() {
        setVisible(false);
        new ErdExecuteSQL(parent);
        //    new ErdExecuteSQL(parent, sqlText.getSQLText());
        dispose();
    }
    
    private void buttons_actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (command.equals("Close"))
            dispose();
        
        else if (command.equals("Execute"))
            execute();
        
    }
    
    
}

















