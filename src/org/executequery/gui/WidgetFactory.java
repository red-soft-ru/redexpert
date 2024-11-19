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

import org.executequery.Constants;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.underworldlabs.swing.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
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
        button.setFocusPainted(false);
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
     * @param name     the component's name
     * @param icon     the icon used as the default image
     * @param listener the ActionListener to be added
     */
    public static JButton createButton(String name, Icon icon, ActionListener listener) {

        JButton button = createButton(name, icon, Constants.EMPTY);
        button.setPreferredSize(getPreferredSize(button));
        button.addActionListener(listener);

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

        Dimension preferredSize = getPreferredSize(button);
        preferredSize.width += button.getFontMetrics(button.getFont()).stringWidth(text) + icon.getIconWidth();
        button.setPreferredSize(preferredSize);

        return button;
    }

    /**
     * Create named JButton class instance
     *
     * @param name     the component's name
     * @param text     the displayed button text
     * @param listener the ActionListener to be added
     */
    public static JButton createButton(String name, String text, ActionListener listener) {

        JButton button = createButton(name, text);
        button.addActionListener(listener);

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
    public static <E> JComboBox<E> createComboBox(String name) {

        JComboBox<E> comboBox = new JComboBox<>();
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
    public static <E> JComboBox<E> createComboBox(String name, ComboBoxModel<E> model) {

        JComboBox comboBox = createComboBox(name);
        comboBox.setModel(model);

        return comboBox;
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name     the component's name
     * @param model    the ComboBoxModel that provides the displayed list of items
     * @param listener the ItemListener that is to be notified
     */
    public static <E> JComboBox<E> createComboBox(String name, ComboBoxModel<E> model, ItemListener listener) {

        JComboBox comboBox = createComboBox(name, model);
        comboBox.addItemListener(listener);

        return comboBox;
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param items the data vector to insert into the combo box
     */
    public static <E> JComboBox<E> createComboBox(String name, Vector<E> items) {
        return createComboBox(name, new DefaultComboBoxModel<>(items));
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param items the data list to insert into the combo box
     */
    public static <E> JComboBox<E> createComboBox(String name, List<E> items) {
        return createComboBox(name, new Vector<>(items));
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name     the component's name
     * @param items    the data vector to insert into the combo box
     * @param listener the ItemListener that is to be notified
     */
    public static <E> JComboBox<E> createComboBox(String name, Vector<E> items, ItemListener listener) {
        return createComboBox(name, new DefaultComboBoxModel<>(items), listener);
    }

    /**
     * Create named JComboBox class instance
     *
     * @param name  the component's name
     * @param items an array of objects to insert into the combo box
     */
    public static <E> JComboBox<E> createComboBox(String name, E[] items) {
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
     * @param name     the component's name
     * @param editable the boolean to be set
     */
    public static JTextField createTextField(String name, boolean editable) {

        JTextField textField = createTextField(name);
        textField.setEditable(editable);

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
     * @param name     the component's name
     * @param text     the text of the checkbox
     * @param selected true if check should be selected
     */
    public static JCheckBox createCheckBox(String name, String text, boolean selected) {

        JCheckBox checkBox = createCheckBox(name, text);
        checkBox.setSelected(selected);

        return checkBox;
    }

    /**
     * Create named JCheckBox class instance
     *
     * @param name     the component's name
     * @param text     the text of the checkbox
     * @param listener the <code>ActionListener</code> to be added
     */
    public static JCheckBox createCheckBox(String name, String text, ActionListener listener) {

        JCheckBox checkBox = createCheckBox(name, text);
        checkBox.addActionListener(listener);

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

        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mousePressed(final MouseEvent e) {
                        SwingUtilities.invokeLater(() -> {
                            JTextField tf = (JTextField) e.getSource();
                            int offset = tf.viewToModel(e.getPoint());
                            tf.setCaretPosition(offset);
                        });
                    }
                }
        );

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
     * Create named JSpinner class instance
     *
     * @param name      the component's name
     * @param maximum   the maximum (non-null) number
     * @param alignment the value alignment
     */
    public static JSpinner createSpinner(String name, int maximum, int alignment) {

        JSpinner spinner = createSpinner(name, 1, 1, maximum, 1);

        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        editor.getTextField().setHorizontalAlignment(alignment);

        return spinner;
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

        JPanel panel = new JPanel(new GridBagLayout());
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

    /**
     * Create named JProgressBar class instance
     *
     * @param name  the component's name
     * @param model the <code>ListModel</code> that provides the list of items for display
     */
    public static <E> JList<E> createList(String name, ListModel<E> model) {

        JList list = new JList();
        list.setModel(model);
        list.setName(name);

        return list;
    }

    /**
     * Create JLabel class instance
     *
     * @param text     text to be displayed by the label
     * @param fontSize the point size of the <code>Font</code>
     */
    public static JLabel createLabel(String text, int fontSize) {

        JLabel label = new JLabel(text);
        label.setFont(new Font(label.getFont().getFontName(), Font.PLAIN, fontSize));

        return label;
    }

    /**
     * Create JLabel class instance
     *
     * @param text text to be displayed by the label
     */
    public static JLabel createLabel(String text) {

        JLabel label = new JLabel(text);
        label.setPreferredSize(getPreferredSize(label));

        return label;
    }

    /**
     * Create JLabel class instance
     *
     * @param text text to be displayed by the label
     * @param icon the default icon this component will display
     */
    public static JLabel createLabel(String text, Icon icon) {

        JLabel label = new JLabel(text);
        label.setIcon(icon);
        label.setPreferredSize(getPreferredSize(label));

        return label;
    }


    // -------------------------
    // --- Custom Components ---
    // -------------------------

    /**
     * Create named <code>ConnectionsComboBox</code> class instance,
     * that extended from <code>JComboBox</code> with the <code>DatabaseConnection</code> items
     * with the automatically updated active connections list
     *
     * @param name           the component's name
     * @param showOnlyActive whether comboBox will contain only active connections
     */
    public static ConnectionsComboBox createConnectionComboBox(String name, boolean showOnlyActive) {
        return createConnectionComboBox(name, showOnlyActive, false, false);
    }

    /**
     * Create named <code>ConnectionsComboBox</code> class instance,
     * that extended from <code>JComboBox</code> with the <code>DatabaseConnection</code> items
     * with the automatically updated active connections list
     *
     * @param name             the component's name
     * @param showOnlyActive   whether comboBox will contain only active connections
     * @param allowAutoConnect whether comboBox will automatically connect to the selected database
     * @param embeddedFilter   whether comboBox will prevent to select embedded connections
     */
    public static ConnectionsComboBox createConnectionComboBox(String name, boolean showOnlyActive, boolean allowAutoConnect, boolean embeddedFilter) {

        ConnectionsComboBox connectionsCombo = new ConnectionsComboBox(showOnlyActive, allowAutoConnect, embeddedFilter);
        connectionsCombo.setPreferredSize(getPreferredSize(connectionsCombo));
        connectionsCombo.setName(name);

        return connectionsCombo;
    }

    /**
     * Create named <code>TransactionIsolationComboBox</code> class instance,
     * that extended from <code>JComboBox</code> with the <code>String</code> items
     *
     * @param name the component's name
     */
    public static TransactionIsolationComboBox createTransactionIsolationComboBox(String name) {

        TransactionIsolationComboBox isolationsCombo = new TransactionIsolationComboBox();
        isolationsCombo.setPreferredSize(getPreferredSize(isolationsCombo));
        isolationsCombo.setName(name);

        return isolationsCombo;
    }

    /**
     * Create named <code>ViewablePasswordField</code> class instance,
     * that enable password visibility changing.
     *
     * @param name the component's name
     */
    public static ViewablePasswordField createViewablePasswordField(String name) {

        ViewablePasswordField passwordField = new ViewablePasswordField();
        passwordField.setPreferredSize(getPreferredSize(passwordField));
        passwordField.setName(name);

        return passwordField;
    }

    /**
     * Create named <code>SimpleSqlTextPanel</code> class instance.
     *
     * @param name the component's name
     */
    public static SimpleSqlTextPanel createSimpleSqlTextPanel(String name) {

        SimpleSqlTextPanel sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.setName(name);

        return sqlTextPanel;
    }

    /**
     * Create named <code>ListSelectionPanel</code> class instance.
     *
     * @param name   the component's name
     * @param values the available values list
     */
    public static ListSelectionPanel createListSelectionPanel(String name, List<?> values) {

        ListSelectionPanel listSelectionPanel = new ListSelectionPanel();
        listSelectionPanel.createAvailableList(values);
        listSelectionPanel.setName(name);

        return listSelectionPanel;
    }

    /**
     * Create named <code>ListSelectionPanel</code> class instance.
     *
     * @param name   the component's name
     * @param values the available values list
     * @param border the border to be rendered for this component
     */
    public static ListSelectionPanel createListSelectionPanel(String name, List<?> values, Border border) {

        ListSelectionPanel listSelectionPanel = createListSelectionPanel(name, values);
        listSelectionPanel.setBorder(border);

        return listSelectionPanel;
    }

    /**
     * Create named RolloverButton class instance
     *
     * @param name the component's name
     */
    public static RolloverButton createRolloverButton(String name) {

        RolloverButton button = new RolloverButton();
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

        RolloverButton button = createRolloverButton(name);
        button.setIcon(IconManager.getIcon(icon));
        button.setToolTipText(toolTip);
        button.setName(name);

        return button;
    }

    /**
     * Create named RolloverButton class instance
     *
     * @param name     the component's name
     * @param toolTip  the tool tip text for this component
     * @param icon     the icon file name used as the default image
     * @param listener <code>ActionListener</code> to be added
     */
    public static RolloverButton createRolloverButton(String name, String toolTip, String icon, ActionListener listener) {

        RolloverButton button = createRolloverButton(name, toolTip, icon);
        button.addActionListener(listener);

        return button;
    }

    /**
     * Create named RolloverButton class instance
     *
     * @param name    the component's name
     * @param action  action to be added
     * @param toolTip the tool tip text for this component
     */
    public static RolloverButton createRolloverButton(String name, Action action, String toolTip) {

        RolloverButton button = new RolloverButton(action, toolTip);
        button.setText(Constants.EMPTY);
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
     * Create named LinkButton class instance
     *
     * @param name     the component's name
     * @param text     the text for display on this component
     * @param listener <code>ActionListener</code> to be added
     */
    public static LinkButton createLinkButton(String name, String text, ActionListener listener) {

        LinkButton button = createLinkButton(name, text);
        button.addActionListener(listener);

        return button;
    }

    /**
     * Create named NumberTextField class instance
     *
     * @param name the component's name
     */
    public static NumberTextField createNumberTextField(String name) {

        NumberTextField numberTextField = new NumberTextField();
        numberTextField.setPreferredSize(getPreferredSize(numberTextField));
        numberTextField.setName(name);

        return numberTextField;
    }

    /**
     * Create named NumberTextField class instance
     *
     * @param name the component's name
     * @param text the text to be set
     */
    public static NumberTextField createNumberTextField(String name, String text) {

        NumberTextField numberTextField = createNumberTextField(name);
        numberTextField.setText(text);

        return numberTextField;
    }

    /**
     * Create named NumberTextField class instance
     *
     * @param name    the component's name
     * @param text    the text to be set
     * @param columns the number of columns
     */
    public static NumberTextField createNumberTextField(String name, String text, int columns) {

        NumberTextField numberTextField = createNumberTextField(name, text);
        numberTextField.setColumns(columns);

        return numberTextField;
    }

    /**
     * Create named DefaultCheckComboBox class instance
     *
     * @param name the component's name
     */
    public static DefaultCheckComboBox createCheckComboBox(String name) {

        DefaultCheckComboBox checkComboBox = new DefaultCheckComboBox();
        checkComboBox.setPreferredSize(getPreferredSize(checkComboBox));
        checkComboBox.setName(name);

        return checkComboBox;
    }

    /**
     * Create named DefaultCheckComboBox class instance
     *
     * @param name  the component's name
     * @param items the data array to insert into the combo box
     */
    public static DefaultCheckComboBox createCheckComboBox(String name, Object[] items) {

        DefaultCheckComboBox checkComboBox = createCheckComboBox(name);
        Arrays.stream(items).forEach(e -> checkComboBox.getModel().addElement(e));

        return checkComboBox;
    }

    /**
     * Create named LinkLabel class instance
     *
     * @param name the component's name
     * @param text the text to be displayed by the label
     * @param link the string of the link to browse
     */
    public static LinkLabel createLinkLabel(String name, String text, String link) {
        link = link != null ? link : "";

        LinkLabel linkLabel = new LinkLabel(text, link);
        linkLabel.setName(name);

        return linkLabel;
    }

    /**
     * Create named LinkLabel class instance
     *
     * @param name     the component's name
     * @param text     the text to be displayed by the label
     * @param link     the string of the link to browse
     * @param fontSize the point size of the <code>Font</code>
     */
    public static LinkLabel createLinkLabel(String name, String text, String link, int fontSize) {

        LinkLabel linkLabel = createLinkLabel(name, text, link);
        linkLabel.setFont(new Font(linkLabel.getFont().getFontName(), Font.PLAIN, fontSize));

        return linkLabel;
    }

    // -----------------------
    // --- Utility Methods ---
    // -----------------------

    private static Dimension getPreferredSize(JComponent component) {
        return new Dimension(
                (int) component.getPreferredSize().getWidth(),
                (int) Math.max(DEFAULT_HEIGHT, component.getPreferredSize().getHeight())
        );
    }

    public static int defaultHeight() {
        return DEFAULT_HEIGHT;
    }
}
