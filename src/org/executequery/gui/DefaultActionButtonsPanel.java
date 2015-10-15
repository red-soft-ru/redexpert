/*
 * DefaultActionButtonsPanel.java
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

package org.executequery.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.executequery.components.BaseActionPanel;
import org.underworldlabs.swing.actions.ActionBuilder;

public abstract class DefaultActionButtonsPanel extends BaseActionPanel {

    private JPanel buttonPanel;
    
    private JButton helpButton;
    
    private List<JButton> buttons;
    
    private boolean expandButtonsToFill;

    public DefaultActionButtonsPanel() {

        super(new GridBagLayout());
        init();
    }

    public boolean isExpandButtonsToFill() {
        return expandButtonsToFill;
    }

    public void setExpandButtonsToFill(boolean expandButtonsToFill) {
        this.expandButtonsToFill = expandButtonsToFill;
    }

    protected final void addActionButton(JButton button) {

        buttons.add(button);
        resetButtonPanel();
    }

    protected final void addHelpButton(JButton helpButton) {
        
        this.helpButton = helpButton;
        resetButtonPanel();
    }

    protected final void addHelpButton(String command) {
        
        JButton helpButton = new DefaultPanelButton();

        helpButton.setAction(ActionBuilder.get("help-command"));
        helpButton.setText("Help");
        helpButton.setActionCommand(command);
        helpButton.setIcon(null);

        addHelpButton(helpButton);
    }
    
    protected final void addContentPanel(JPanel contentPanel) {
        
        contentPanel.setBorder(createContentBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        add(contentPanel, gbc);        
    }

    private void resetButtonPanel() {

        buttonPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;

        if (expandButtonsToFill) {

            gbc.fill = GridBagConstraints.HORIZONTAL;
        }
        
        for (int i = 0, n = buttons.size(); i < n; i++) {

            gbc.gridx++;
            gbc.weightx = 0;

            gbc.insets.top = 5;
            gbc.insets.bottom = 5;
            gbc.insets.left = 5;
            
            gbc.anchor = GridBagConstraints.EAST;

            if (i == 0 || expandButtonsToFill) {

                gbc.weightx = 1.0;
            }

            buttons.get(i).getPreferredSize();
            buttonPanel.add(buttons.get(i), gbc);
        }

        if (helpButton != null) {

            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.insets.left = 0;
            gbc.insets.right = 5;
            gbc.anchor = GridBagConstraints.WEST;            

            buttonPanel.add(helpButton, gbc);
        }

        buttonPanel.revalidate();
    }
    
    private Border createContentBorder() {

        return BorderFactory.createEtchedBorder();
    }

    private void addActionButtonsPanel(JPanel actionButtonsPanel) {
        
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        add(actionButtonsPanel, gbc);
    }

    private void init() {

        expandButtonsToFill = false;
        
        buttons = new ArrayList<JButton>();
        
        buttonPanel = new JPanel(new GridBagLayout());
        addActionButtonsPanel(buttonPanel);
        
        setBorder(BorderFactory.createEmptyBorder(
                EMPTY_BORDER_WIDTH, EMPTY_BORDER_WIDTH, 
                EMPTY_BORDER_WIDTH, EMPTY_BORDER_WIDTH));
    }

    private static final int EMPTY_BORDER_WIDTH = 4;
    
}










