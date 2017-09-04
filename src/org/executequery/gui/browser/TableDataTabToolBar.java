/*
 * TableDataTabToolBar.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.executequery.Constants;
import org.executequery.gui.DefaultNumberTextField;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.util.SystemProperties;

public class TableDataTabToolBar extends JPanel implements FocusListener {

    private JCheckBox maxRowCountCheckBox;

    private NumberTextField maxRowCountField;

    public TableDataTabToolBar() {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));

        maxRowCountCheckBox = new JCheckBox();
        maxRowCountCheckBox.setOpaque(true);
        maxRowCountCheckBox.setSelected(true);
        maxRowCountCheckBox.setToolTipText("Enable/disable max records");
        maxRowCountCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                maxRowCountCheckBoxSelected();
            }
        });

        maxRowCountField = new DefaultNumberTextField() {
            @Override
            public int getWidth() {
                return 100;
            }
        };
        maxRowCountField.setToolTipText("Set the maximum rows returned (-1 for all)");
        maxRowCountField.setFocusAccelerator('r');
        maxRowCountField.addFocusListener(this);
        maxRowCountField.setValue(SystemProperties.getIntProperty("user", "browser.max.records"));

        JPanel maxRowCountPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.insets.top = 2;
        gbc.insets.left = 10;
        gbc.anchor = GridBagConstraints.EAST;
        maxRowCountPanel.add(maxRowCountCheckBox, gbc);
        gbc.gridx++;
        gbc.insets.left = 0;
        gbc.insets.right = 7;
        gbc.weightx = 0;
        maxRowCountPanel.add(createLabel("Max Rows:", 'R'), gbc);
        gbc.gridx++;
        gbc.weighty = 1.0;
        gbc.insets.top = 2;
        gbc.insets.bottom = 2;
        gbc.insets.right = 2;
        gbc.fill = GridBagConstraints.BOTH;
        maxRowCountPanel.add(maxRowCountField, gbc);

        add(maxRowCountPanel);
        
    }
    
    private void maxRowCountCheckBoxSelected() {

        maxRowCountField.setEnabled(maxRowCountCheckBox.isSelected());
        maxRowCountField.requestFocus();
    }

    public void focusGained(FocusEvent e) {
        
        maxRowCountField.selectAll();
    }

    public void focusLost(FocusEvent e) {}
    
    private JLabel createLabel(String text, char mnemonic) {

        final JLabel label = new JLabel(text);
        label.setDisplayedMnemonic(mnemonic);
        label.setOpaque(true);

        return label;
    }

    private RolloverButton createButton(String actionId, String toolTipText) {

        RolloverButton button = new RolloverButton(ActionBuilder.get(actionId), toolTipText);
        button.setText(Constants.EMPTY);
        return button;
    }

    @Override
    public int getHeight() {
        return 35;
    }
    
}


