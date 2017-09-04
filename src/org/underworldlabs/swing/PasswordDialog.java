/*
 * PasswordDialog.java
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

package org.underworldlabs.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class PasswordDialog extends JDialog {
    
    public static final int OK = 1;
    public static final int CANCEL = 0;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private JPasswordField field;
    
    private String message;
    
    private int result;
    
    /** <p>Constructs a new instance with the specified owner,
     *  title and message.
     *
     *  @param the dialog owner
     *  @param the dialog title
     *  @param the dialog message displayed
     */
    public PasswordDialog(Frame owner, String title, String message) {
        super(owner, title, true);
        this.message = message;
        jbInit();
        display();
    }
    
    private void jbInit() {
        try {
            JPanel base = new JPanel(new GridBagLayout());
            
            okButton = new JButton("OK");
            cancelButton = new JButton("Cancel");
            
            Insets btnIns = new Insets(2, 2, 2, 2);
            okButton.setMargin(btnIns);
            cancelButton.setMargin(btnIns);
            
            Dimension btnDim = new Dimension(65, 25);
            okButton.setPreferredSize(btnDim);
            cancelButton.setPreferredSize(btnDim);
            
            ActionListener buttonListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    buttons_actionPerformed(e); }
            };
            
            okButton.addActionListener(buttonListener);
            cancelButton.addActionListener(buttonListener);
            
            field = new JPasswordField();
            field.setPreferredSize(new Dimension(250,20));
            field.addActionListener(buttonListener);
            
            GridBagConstraints gbc = new GridBagConstraints();
            Insets ins = new Insets(10,15,0,15);
            gbc.gridwidth = 2;
            gbc.insets = ins;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            base.add(new JLabel(message), gbc);
            gbc.gridy = 1;
            base.add(field, gbc);
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets.right = 0;
            gbc.insets.left = 70;
            gbc.insets.bottom = 15;
            gbc.weighty = 1.0;
            base.add(okButton, gbc);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets.left = 5;
            gbc.gridx = 1;
            base.add(cancelButton, gbc);
            
            setResizable(false);
            getContentPane().add(base);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getValue() {
        
        if (result == CANCEL) {
            return null;
        }
        
        char[] pwd = field.getPassword();
        
        StringBuffer pwdBuffer = new StringBuffer(10);
        
        for (int i = 0; i < pwd.length; i++) {
            pwdBuffer.append(pwd[i]);
            pwd[i] = 0;
        }
        
        return pwdBuffer.toString();
    }
    
    public int getResult() {
        return result;
    }
    
    private void buttons_actionPerformed(ActionEvent e) {
        result = -1;
        
        if (e.getSource() == cancelButton) {
            result = CANCEL;
        } else {
            result = OK;
        }
        setVisible(false);
    }
    
    private void display() {
        pack();
        setLocation(GUIUtils.getPointToCenter(getOwner(), getSize()));
        setVisible(true);
    }
    
}


















