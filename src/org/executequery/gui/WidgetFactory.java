/*
 * WidgetFactory.java
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

package org.executequery.gui;

import org.executequery.gui.browser.DefaultInlineFieldButton;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.DefaultFieldLabel;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

public final class WidgetFactory {

    public static JButton createInlineFieldButton(String text) {

        return new DefaultInlineFieldButton(text);
    }

    public static JButton createPanelButton(String text, String toolTip, ActionListener actionListener) {

        return createPanelButton(text, toolTip, actionListener, null);
    }

    public static JButton createPanelButton(String text, String toolTip, ActionListener actionListener, String actionCommand) {

        JButton button = new DefaultPanelButton(text);
        button.addActionListener(actionListener);
        button.setToolTipText(toolTip);
        button.setActionCommand(actionCommand);

        return button;
    }

    public static JButton createInlineFieldButton(String text, String actionCommand) {

        JButton button = new DefaultInlineFieldButton(text);
        button.setActionCommand(actionCommand);

        return button;
    }

    public static JButton createButton(String text) {

        return new JButton(text);
    }

    public static JButton createButton(ActionListener actionListener, String text) {

        return new DefaultButton(actionListener, text, null);
    }

    public static JComboBox createComboBox(Vector<?> items) {

        return new JComboBox(items);
    }

    public static JComboBox createComboBox(ComboBoxModel model) {

        return new JComboBox(model);
    }

    public static JComboBox createComboBox(Object[] items) {

        return new JComboBox(items);
    }

    public static JComboBox createComboBox() {

        return new JComboBox();
    }

    public static NumberTextField createNumberTextField() {

        return new NumberTextField();
    }

    public static JTextField createTextField() {

        return new JTextField();
    }

    public static JTextField createTextField(String text) {

        return new JTextField(text);
    }

    public static JPasswordField createPasswordField() {

        return new JPasswordField();
    }

    public static void addLabelFieldPair(JPanel panel, String label,
                                         JComponent field, GridBagConstraints gbc) {

        addLabelFieldPair(panel, label, field, null, gbc);
    }

    public static void addLabelFieldPair(JPanel panel, String label,
                                         JComponent field, String toolTip, GridBagConstraints gbc) {

        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;

        if (panel.getComponentCount() > 0) {

            gbc.insets.top = 0;
        }

        gbc.weightx = 0;
        panel.add(new DefaultFieldLabel(label), gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridx = 1;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        panel.add(field, gbc);

        if (toolTip != null) {

            field.setToolTipText(toolTip);
        }

    }

}


