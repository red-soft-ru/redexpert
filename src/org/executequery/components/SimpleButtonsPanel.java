/*
 * SimpleButtonsPanel.java
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

package org.executequery.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.executequery.gui.DefaultPanelButton;
import org.executequery.util.StringBundle;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.MiscUtils;

/** 
 * <p>Simple button panel with ok, cancel buttons.
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class SimpleButtonsPanel extends JPanel {

    private final ActionListener actionListener;

    private final String okButtonText;

    private final String okButtonCommand;

    private final String cancelButtonText;

    private final String cancelButtonCommand;

    public SimpleButtonsPanel(ActionListener actionListener, 
            String okButtonCommand, String cancelButtonCommand) {

        this(actionListener, null, okButtonCommand, null, cancelButtonCommand);
    }

    public SimpleButtonsPanel(ActionListener actionListener, 
            String okButtonText, String okButtonCommand, 
            String cancelButtonText, String cancelButtonCommand) {
        
        super(new GridBagLayout());

        this.actionListener = actionListener;
        this.okButtonText = okButtonText;
        this.okButtonCommand = okButtonCommand;
        this.cancelButtonText = cancelButtonText;
        this.cancelButtonCommand = cancelButtonCommand;

        init();
    }
    
    private void init() {

        JButton okButton = createOkButton();        
        JButton cancelButton = createCancelButton();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets.top = 5;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        add(okButton, gbc);
        gbc.insets.left = 5;
        gbc.gridx = 1;
        gbc.weightx = 0;
        add(cancelButton, gbc);
    }

    private JButton createCancelButton() {
        JButton button = new DefaultPanelButton(defaultCancelText());
        if (!MiscUtils.isNull(cancelButtonText)) {

            button.setText(cancelButtonText);
        }
        
        initialiseButton(button);
        button.setActionCommand(cancelButtonCommand);
        
        return button;
    }

    private JButton createOkButton() {
        JButton button = new DefaultPanelButton(defaultOkText());
        if (!MiscUtils.isNull(okButtonText)) {

            button.setText(okButtonText);
        }
        
        initialiseButton(button);
        button.setActionCommand(okButtonCommand);

        return button;
    }

    private void initialiseButton(JButton button) {
        button.addActionListener(actionListener);
    }

    private String defaultCancelText() {
        return loadBundle().getString("SimpleButtonsPanel.cancel");
    }

    private String defaultOkText() {
        return loadBundle().getString("SimpleButtonsPanel.ok");
    }
    
    private StringBundle loadBundle() {
        return SystemResources.loadBundle(SimpleButtonsPanel.class);
    }

}










