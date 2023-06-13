/*
 * ActionUtilities.java
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

package org.underworldlabs.swing.actions;

import org.executequery.util.UserProperties;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.DefaultToolbarButton;
import org.underworldlabs.swing.util.IconUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class ActionUtilities {

    private ActionUtilities() {
    }

    public static JButton createButton(ActionListener actionListener,
                                       Icon icon,
                                       String name,
                                       String command) {
        JButton item = new DefaultButton(name);
        item.setActionCommand(command);

        if (icon != null) {
            item.setIcon(icon);
        }

        if (actionListener != null) {
            item.addActionListener(actionListener);
        }

        return item;
    }

    public static JButton createButton(ActionListener actionListener,
                                       String command,
                                       Icon icon,
                                       String toolTipText) {
        JButton item = new DefaultButton(icon);
        item.setMargin(new Insets(1, 1, 1, 1));

        item.setToolTipText(toolTipText);
        item.setActionCommand(command);

        if (actionListener != null) {
            item.addActionListener(actionListener);
        }

        return item;
    }

    public static JButton createToolbarButton(ActionListener actionListener,
                                              String command,
                                              Icon icon,
                                              String toolTipText) {

        JButton item = new DefaultToolbarButton(icon);
        item.setMargin(new Insets(1, 1, 1, 1));

        item.setToolTipText(toolTipText);
        item.setActionCommand(command);

        if (actionListener != null) {
            item.addActionListener(actionListener);
        }

        return item;
    }

    public static JButton createToolbarButton(ActionListener actionListener,
                                              String icon,
                                              String toolTipText,
                                              String command) {

        JButton item = new DefaultToolbarButton();
        return init(actionListener, icon, toolTipText, command, item);
    }

    public static JButton createButton(ActionListener actionListener,
                                       String icon,
                                       String toolTipText,
                                       String command) {

        JButton button = new DefaultButton();
        return init(actionListener, icon, toolTipText, command, button);
    }

    private static JButton init(ActionListener actionListener, String icon, String toolTipText, String command,
                                JButton item) {

        if (icon != null) {
            item.setIcon(IconUtilities.loadIcon(icon, UserProperties.getInstance().getIntProperty("icons.size")));
            item.setMargin(new Insets(1, 1, 1, 1));
        }

        item.setToolTipText(toolTipText);
        item.setActionCommand(command);

        if (actionListener != null) {
            item.addActionListener(actionListener);
        }

        return item;
    }

    public static JButton createButton(String name,
                                       String icon,
                                       String command,
                                       boolean iconOnly) {
        JButton item = new DefaultButton(name);
        item.setToolTipText(name);
        item.setActionCommand(command);

        if (icon != null) {
            item.setIcon(IconUtilities.loadIcon(icon,UserProperties.getInstance().getIntProperty("icons.size")));
        }

        if (iconOnly) {
            item.setMargin(new Insets(1, 1, 1, 1));
            item.setText(null);
        }
        return item;
    }

    public static JButton createButton(ActionListener actionListener,
                                       String name,
                                       String command) {
        JButton item = new DefaultButton(name);
        item.setActionCommand(command);
        item.addActionListener(actionListener);
        return item;
    }

    public static JButton createButton(String name, String command) {
        JButton item = new DefaultButton(name);
        item.setActionCommand(command);
        return item;
    }

    public static JCheckBox createCheckBox(ActionListener actionListener,
                                           String name,
                                           String command,
                                           boolean selected) {
        JCheckBox item = new JCheckBox(name, selected);
        item.setActionCommand(command);

        if (actionListener != null) {
            item.addActionListener(actionListener);
        }

        return item;
    }

    public static JCheckBox createCheckBox(ActionListener actionListener,
                                           String name,
                                           String command) {
        return createCheckBox(actionListener, name, command, false);
    }

    public static JCheckBox createCheckBox(String name,
                                           String command,
                                           boolean selected) {
        return createCheckBox(null, name, command, selected);
    }

    public static JCheckBox createCheckBox(String name,
                                           String command) {
        return createCheckBox(null, name, command, false);
    }

    public static JComboBox createComboBox(ActionListener actionListener,
                                           String[] values,
                                           String command) {
        JComboBox combo = new JComboBox(values);
        combo.setActionCommand(command);

        if (actionListener != null) {
            combo.addActionListener(actionListener);
        }

        return combo;
    }

    public static JComboBox createComboBox(String[] values, String command) {
        return createComboBox(null, values, command);
    }

    public static JComboBox createComboBox(ActionListener actionListener,
                                           Vector<?> values,
                                           String command) {
        JComboBox combo = new JComboBox(values);
        combo.setActionCommand(command);

        if (actionListener != null) {
            combo.addActionListener(actionListener);
        }

        return combo;
    }

    public static JComboBox createComboBox(Vector<?> values, String command) {
        return createComboBox(null, values, command);
    }

}






