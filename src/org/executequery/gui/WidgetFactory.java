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

import org.executequery.GUIUtilities;
import org.executequery.gui.browser.DefaultInlineFieldButton;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.LinkButton;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.RolloverButton;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * The utility class for simplified creation of named Swing components
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public final class WidgetFactory {

    private static final int DEFAULT_HEIGHT = 26;


    // --------------------------------
    // --- Default Swing Components ---
    // --------------------------------


    /**
     * Create named JButton class instance
     *
     * @param name the component's name
     * @param text the displayed button text
     */
    public static JButton createButton(String name, String text) {

        JButton button = new JButton(text);
        button.setPreferredSize(getPreferredSize(button));
        button.setName(name);

        return button;
    }

    /**
     * Create named JButton class instance
     *
     * @param name    the component's name
     * @param icon    the icon used as the default image
     * @param toolTip the string to display
     */
    public static JButton createButton(String name, Icon icon, String toolTip) {

        JButton button = createButton(name, null);
        button.setIcon(icon);
        button.setToolTipText(toolTip);
        button.setPreferredSize(getPreferredSize(button));

        return button;
    }

    /**
     * Create named JButton class instance
     *
     * @param name    the component's name
     * @param text    the displayed button text
     * @param icon    the icon used as the default image
     * @param toolTip the string to display
     */
    public static JButton createButton(String name, String text, Icon icon, String toolTip) {

        JButton button = createButton(name, icon, toolTip);
        button.setText(text);

        Dimension defaultPrefered = getPreferredSize(button);
        defaultPrefered.width += button.getFontMetrics(button.getFont()).stringWidth(text) + button.getMargin().left + button.getMargin().right;
        button.setPreferredSize(defaultPrefered);

        return button;
    }

    /**
     * Create named JRadioButton class instance
     *
     * @param name the component's name
     * @param text the displayed button text
     */
    public static JRadioButton createRadioButton(String name, String text) {

        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setName(name);

        return radioButton;
    }

    /**
     * Create named JRadioButton class instance
     *
     * @param name     the component's name
     * @param text     the displayed button text
     * @param selected true if the button is selected
     */
    public static JRadioButton createRadioButton(String name, String text, boolean selected) {

        JRadioButton radioButton = createRadioButton(name, text);
        radioButton.setSelected(true);

        return radioButton;
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name the component's name
     */
    public static JComboBox createComboBox(String name) {

        JComboBox comboBox = new JComboBox();
        comboBox.setPreferredSize(getPreferredSize(comboBox));
        comboBox.setName(name);

        return comboBox;
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param model the ComboBoxModel that provides the displayed list of items
     */
    public static JComboBox createComboBox(String name, ComboBoxModel model) {

        JComboBox comboBox = createComboBox(name);
        comboBox.setModel(model);

        return comboBox;
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param items the data vector to insert into the combo box
     */
    public static JComboBox createComboBox(String name, Vector<?> items) {
        return createComboBox(name, new DefaultComboBoxModel<>(items));
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param items an array of objects to insert into the combo box
     */
    public static JComboBox createComboBox(String name, Object[] items) {
        return createComboBox(name, new DefaultComboBoxModel<>(items));
    }

    /**
     * Create named JTextField class instance
     *
     * @param name the component's name
     */
    public static JTextField createTextField(String name) {

        JTextField textField = new JTextField();
        textField.setPreferredSize(getPreferredSize(textField));
        textField.setName(name);

        return textField;
    }

    /**
     * Create named JTextField class instance
     *
     * @param name the component's name
     * @param text the text to be set
     */
    public static JTextField createTextField(String name, String text) {

        JTextField textField = createTextField(name);
        textField.setText(text);

        return textField;
    }

    /**
     * Create named JPasswordField class instance
     *
     * @param name the component's name
     */
    public static JPasswordField createPasswordField(String name) {

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(getPreferredSize(passwordField));
        passwordField.setName(name);

        return passwordField;
    }

    /**
     * Create named JCheckBox class instance
     *
     * @param name the component's name
     * @param text the text of the checkbox
     */
    public static JCheckBox createCheckBox(String name, String text) {

        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setPreferredSize(getPreferredSize(checkBox));
        checkBox.setName(name);

        return checkBox;
    }

    /**
     * Create named JCheckBox class instance
     *
     * @param name the component's name
     */
    public static JSpinner createSpinner(String name) {

        JSpinner spinner = new JSpinner();
        spinner.setPreferredSize(getPreferredSize(spinner));
        spinner.setName(name);

        return spinner;
    }

    /**
     * Create named JSpinner class instance
     *
     * @param name  the component's name
     * @param model the SpinnerModel
     */
    public static JSpinner createSpinner(String name, SpinnerModel model) {

        JSpinner spinner = createSpinner(name);
        spinner.setModel(model);

        return spinner;
    }

    /**
     * Create named JSpinner class instance
     *
     * @param name     the component's name
     * @param value    the current (non-null) number
     * @param minimum  the maximum (non-null) number
     * @param maximum  the maximum (non-null) number
     * @param stepSize the size of the value change
     */
    public static JSpinner createSpinner(String name, int value, int minimum, int maximum, int stepSize) {

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel();
        spinnerModel.setMinimum(minimum);
        spinnerModel.setMaximum(maximum);
        spinnerModel.setStepSize(stepSize);
        spinnerModel.setValue(value);

        return createSpinner(name, spinnerModel);
    }

    /**
     * Create named JTable class instance
     *
     * @param name the component's name
     */
    public static JTable createTable(String name) {

        JTable table = new JTable();
        table.setName(name);

        return table;
    }

    /**
     * Create named JTable class instance
     *
     * @param name  the component's name
     * @param model the data model for the table
     */
    public static JTable createTable(String name, TableModel model) {

        JTable table = createTable(name);
        table.setModel(model);

        return table;
    }

    /**
     * Create named JTable class instance
     *
     * @param name        the component's name
     * @param columnNames the names of the columns
     */
    public static JTable createTable(String name, Object[] columnNames) {
        return createTable(name, new Object[][]{}, columnNames);
    }

    /**
     * Create named JTable class instance
     *
     * @param name        the component's name
     * @param data        the data of the table
     * @param columnNames the names of the columns
     */
    public static JTable createTable(String name, Object[][] data, Object[] columnNames) {
        return createTable(name, new DefaultTableModel(data, columnNames));
    }

    /**
     * Create named JTabbedPane class instance
     *
     * @param name the component's name
     */
    public static JTabbedPane createTabbedPane(String name) {

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName(name);

        return tabbedPane;
    }

    /**
     * Create named JPanel class instance
     *
     * @param name the component's name
     */
    public static JPanel createPanel(String name) {

        JPanel panel = new JPanel();
        panel.setName(name);

        return panel;
    }

    /**
     * Create named JToolBar class instance
     *
     * @param name the component's name
     */
    public static JToolBar createToolBar(String name) {

        JToolBar toolBar = new JToolBar();
        toolBar.setName(name);

        return toolBar;
    }

    /**
     * Create named JProgressBar class instance
     *
     * @param name the component's name
     */
    public static JProgressBar createProgressBar(String name) {

        JProgressBar progressBar = new JProgressBar();
        progressBar.setName(name);
        progressBar.setPreferredSize(getPreferredSize(progressBar));

        return progressBar;
    }


    // -------------------------
    // --- Custom Components ---
    // -------------------------


    /**
     * Create named DefaultInlineFieldButton class instance
     *
     * @param name the component's name
     * @param text the displayed button text
     */
    public static JButton createInlineFieldButton(String name, String text) {

        JButton button = new DefaultInlineFieldButton(text);
        button.setName(name);

        return button;
    }

    /**
     * Create named DefaultInlineFieldButton class instance
     *
     * @param name          the component's name
     * @param text          the displayed button text
     * @param actionCommand the action command for this button
     */
    public static JButton createInlineFieldButton(String name, String text, String actionCommand) {

        JButton button = createInlineFieldButton(name, text);
        button.setActionCommand(actionCommand);

        return button;
    }

    /**
     * Create named DefaultPanelButton class instance
     *
     * @param name           the component's name
     * @param text           the displayed button text
     * @param toolTip        the tool tip text for this component
     * @param actionListener the ActionListener to be added
     */
    public static JButton createPanelButton(String name, String text, String toolTip, ActionListener actionListener) {

        JButton button = new DefaultPanelButton(text);
        button.addActionListener(actionListener);
        button.setToolTipText(toolTip);
        button.setName(name);

        return button;
    }

    /**
     * Create named DefaultPanelButton class instance
     *
     * @param name           the component's name
     * @param text           the displayed button text
     * @param toolTip        the tool tip text for this component
     * @param actionListener the ActionListener to be added
     * @param actionCommand  the action command for this button
     */
    public static JButton createPanelButton(String name, String text, String toolTip, ActionListener actionListener, String actionCommand) {

        JButton button = createPanelButton(name, text, toolTip, actionListener);
        button.setActionCommand(actionCommand);

        return button;
    }

    /**
     * Create named DefaultButton class instance
     *
     * @param name           the component's name
     * @param text           the displayed button text
     * @param actionListener the ActionListener to be added
     */
    public static JButton createButton(String name, ActionListener actionListener, String text) {

        DefaultButton button = new DefaultButton(actionListener, text, null);
        button.setName(name);

        return button;
    }

    /**
     * Create named RolloverButton class instance
     *
     * @param name    the component's name
     * @param toolTip the tool tip text for this component
     * @param icon    the icon file name used as the default image
     */
    public static RolloverButton createRolloverButton(String name, String toolTip, String icon) {

        RolloverButton button = new RolloverButton();
        button.setIcon(GUIUtilities.loadIcon(icon));
        button.setMouseEnteredContentAreaFill(false);
        button.setToolTipText(toolTip);
        button.setName(name);

        return button;
    }

    /**
     * Create named LinkButton class instance
     *
     * @param name the component's name
     * @param text the text for display on this component
     */
    public static LinkButton createLinkButton(String name, String text) {

        LinkButton button = new LinkButton(text);
        button.setName(name);

        return button;
    }

    /**
     * Create named NumberTextField class instance
     *
     * @param name the component's name
     */
    public static NumberTextField createNumberTextField(String name) {

        NumberTextField numberTextField = new NumberTextField();
        numberTextField.setName(name);

        return numberTextField;
    }


    // -----------------------
    // --- Utility Methods ---
    // -----------------------

    private static Dimension getPreferredSize(JComponent component) {
        return new Dimension((int) component.getPreferredSize().getWidth(), DEFAULT_HEIGHT);
    }

}
