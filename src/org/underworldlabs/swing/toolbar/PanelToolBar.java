/*
 * PanelToolBar.java
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

package org.underworldlabs.swing.toolbar;

import org.executequery.util.UserProperties;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.util.IconUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Tab component tool bar panel.
 *
 * @author Takis Diakoumis
 */
public class PanelToolBar extends AbstractToolBarPanel {

    public PanelToolBar() {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 1));
    }

    public JButton addButton(ActionListener action, String actionCommand,
                             String icon, String toolTip, boolean enabled) {

        JButton button = new RolloverButton();

        if (icon != null) {

            button.setIcon(IconUtilities.loadIcon(icon, UserProperties.getInstance().getIntProperty("icons.size")));
        }

        button.setText(null);
        button.setToolTipText(toolTip);
        button.setActionCommand(actionCommand);
        button.addActionListener(action);
        button.setEnabled(enabled);
        add(button);

        return button;
    }

    public void removeButton(JButton button) {
        remove(button);
    }

    public JButton addButton(ActionListener action, String actionCommand,
                             String icon, String toolTip) {
        return addButton(action, actionCommand, icon, toolTip, true);
    }

    public JButton addButton(Action action) {

        JButton button = new RolloverButton();

        button.setAction(action);
        button.setText(null);
        add(button);

        return button;
    }

    public void addSeparator() {
        add(new PanelToolBarSeparator());
    }

    public void addLabel(String text) {
        add(new JLabel(text));
    }

    public void addTextField(JTextField textField) {
        add(textField);
    }

    public void addComboBox(JComboBox comboBox) {
        add(comboBox);
    }

    public void addButton(JButton button) {
        add(button);
    }

    private class PanelToolBarSeparator extends JLabel {

        private static final int DEFAULT_WIDTH = 4;

        private int preferredWidth;

        public PanelToolBarSeparator() {
            this(DEFAULT_WIDTH);
        }

        public PanelToolBarSeparator(int preferredWidth) {
            this.preferredWidth = preferredWidth;
        }

        public boolean isOpaque() {
            return !GUIUtils.isDefaultLookAndFeel();
        }

        public Dimension getPreferredSize() {
            return new Dimension(preferredWidth, 1);
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

    }

}







