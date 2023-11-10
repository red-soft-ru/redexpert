/*
 * DriversPanel.java
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

package org.executequery.gui.drivers;

import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.components.TextFieldPanel;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.datasource.DatabaseDefinition;
import org.executequery.gui.DefaultPanelButton;
import org.executequery.gui.SimpleValueSelectionDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseDefinitionCache;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 * @deprecated
 */
public class DriversPanel extends AbstractFormObjectViewPanel
        implements ItemListener, DriverPanel {

    public static final String TITLE = Bundles.getCommon("drivers");

    public static final String FRAME_ICON = "DatabaseDrivers16.svg";

    private JTextField nameField;
    private JTextField descField;
    private JList jarPathList;
    private JTextField classField;

    private DefaultListModel jarPathListModel;

    private JComboBox driverUrlCombo;
    private JComboBox databaseNameCombo;

    private DynamicComboBoxModel urlComboModel;

    /**
     * the currently selected driver
     */
    private DatabaseDriver databaseDriver;

    /**
     * the parent panel containing the selection tree
     */
    private DriverViewPanel parent;

    /**
     * Creates a new instance of DriversPanel
     */
    public DriversPanel(DriverViewPanel parent) {
        super();
        this.parent = parent;
        init();
    }

    private void init() {

        ReflectiveAction action = new ReflectiveAction(this);

        JButton browseButton = new DefaultPanelButton(
                action, "Add Library", "browseDrivers");
        JButton findButton = new DefaultPanelButton(
                action, "Find", "findDriverClass");
        JButton removeButton = new DefaultPanelButton(
                action, "Remove", "removeDrivers");

        jarPathListModel = new DefaultListModel();

        nameField = WidgetFactory.createTextField("nameField");
        descField = WidgetFactory.createTextField("descField");
        jarPathList = new JList(jarPathListModel);
        classField = WidgetFactory.createTextField("classField");

        nameField.addFocusListener(new DriverNameFieldListener(this));

        // retrieve the db name list
        List<DatabaseDefinition> databases = DatabaseDefinitionCache.getDatabaseDefinitions();
        int count = databases.size() + 1;

        Vector<DatabaseDefinition> _databases = new Vector<DatabaseDefinition>(count);

        // create a new list with a dummy value
        for (int i = 1; i < count; i++) {

            _databases.add(databases.get(i - 1));
        }

        // add the dummy
        _databases.insertElementAt(new DatabaseDefinition(
                DatabaseDefinition.INVALID_DATABASE_ID, "Select..."), 0);

        databaseNameCombo = WidgetFactory.createComboBox("databaseNameCombo", _databases);
        databaseNameCombo.addItemListener(this);

        urlComboModel = new DynamicComboBoxModel();
        driverUrlCombo = WidgetFactory.createComboBox("driverUrlCombo", urlComboModel);
        driverUrlCombo.setEditable(true);

        JPanel base = new TextFieldPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.insets = new Insets(10, 10, 5, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        base.add(new JLabel("Driver Name:"), gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        base.add(new JLabel("Description:"), gbc);
        gbc.gridy++;
        base.add(new JLabel("Database:"), gbc);
        gbc.gridy++;
        base.add(new JLabel("JDBC URL:"), gbc);
        gbc.gridy++;
        base.add(new JLabel("Path:"), gbc);
        gbc.gridy += 4;
        base.add(new JLabel("Class Name:"), gbc);
        gbc.gridy = 0;
        gbc.gridx = 1;
        gbc.insets.right = 10;
        gbc.insets.top = 10;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(nameField, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        base.add(descField, gbc);
        gbc.gridy++;
        base.add(databaseNameCombo, gbc);
        gbc.gridy++;
        base.add(driverUrlCombo, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.right = 0;
        gbc.gridheight = 2;
        base.add(new JScrollPane(jarPathList), gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.right = 10;
        gbc.gridheight = 1;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.NONE;
        base.add(browseButton, gbc);
        gbc.gridy++;
        base.add(removeButton, gbc);

        gbc.gridy += 3;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.right = 0;
        gbc.insets.left = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        base.add(classField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.weighty = 1.0;
        gbc.insets.right = 10;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.NONE;
        base.add(findButton, gbc);

        setHeaderText("Database Driver");
        setHeaderIcon(GUIUtilities.loadIcon("DatabaseDriver24.svg"));
        setContentPanel(base);
    }

    public void driverNameChanged() {
        if (panelSelected) {
            populateAndSave();
        }
    }

    public void itemStateChanged(ItemEvent e) {
        // interested in selections only
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }

        DatabaseDefinition database = getSelectedDatabase();
        int id = database.getId();
        if (id > 0) {
            // reload the urls for the combo selection
            resetUrlCombo(database);
        } else {
            // otherwise clear all
            urlComboModel.removeAllElements();
        }
    }

    private DatabaseDefinition getSelectedDatabase() {

        return (DatabaseDefinition) databaseNameCombo.getSelectedItem();
    }

    public boolean saveDrivers() {

        try {

            ((DatabaseDriverRepository) RepositoryCache.load(
                    DatabaseDriverRepository.REPOSITORY_ID)).save();

            return true;

        } catch (RepositoryException e) {

            GUIUtilities.displayErrorMessage(e.getMessage());

            return false;
        }

    }

    private boolean populateAndSave() {
        populateDriverObject();
        return saveDrivers();
    }

    // --------------------------------------------
    // DockedTabView implementation
    // --------------------------------------------

    private boolean panelSelected = true;

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        panelSelected = false;
        //nameField.removeFocusListener(this);
        return populateAndSave();
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        panelSelected = true;
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewDeselected() {
        return tabViewClosing();
    }

    // --------------------------------------------

    private String jarPathsFormatted() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = jarPathListModel.size(); i < n; i++) {

            sb.append(jarPathListModel.get(i));
            if (i < (n - 1)) {
                sb.append(";");
            }

        }

        return sb.toString();
    }

    public void findDriverClass(ActionEvent e) {

        if (jarPathListModel.isEmpty()) {

            GUIUtilities.displayErrorMessage(
                    "A valid path to the JDBC library is required");

            return;
        }

        String[] drivers = null;
        try {
            GUIUtilities.showWaitCursor();
            drivers = MiscUtils.findImplementingClasses(
                    "java.sql.Driver", jarPathsFormatted());
        } catch (MalformedURLException urlExc) {
            GUIUtilities.displayErrorMessage(
                    "A valid path to the JDBC library is required");
        } catch (IOException ioExc) {
            GUIUtilities.displayErrorMessage(
                    "An error occured accessing the specified file:\n" +
                            ioExc.getMessage());
        } finally {
            GUIUtilities.showNormalCursor();
        }

        if (drivers == null || drivers.length == 0) {
            GUIUtilities.displayWarningMessage(
                    "No valid classes implementing java.sql.Driver\n" +
                            "were found in the specified resource");
            return;
        }

        int result = -1;
        String value = null;
        while (true) {
            SimpleValueSelectionDialog dialog =
                    new SimpleValueSelectionDialog("Select JDBC Driver", drivers);
            result = dialog.showDialog();

            if (result == JOptionPane.OK_OPTION) {
                value = dialog.getValue();

                if (value == null) {
                    GUIUtilities.displayErrorMessage(
                            "You must select a driver from the list");
                } else {
                    classField.setText(value);
                    databaseDriver.setClassName(value);
                    break;
                }

            } else {
                break;
            }

        }
    }

    public void removeDrivers(ActionEvent e) {

        int selectedIndex = jarPathList.getSelectedIndex();

        Object[] selections = jarPathList.getSelectedValues();
        if (selections != null && selections.length > 0) {
            for (Object path : selections) {
                jarPathListModel.removeElement(path);
            }
        }

        int newSize = jarPathListModel.size();
        if (newSize > 0) {
            if (selectedIndex > newSize - 1) {
                selectedIndex = newSize - 1;
            }
            jarPathList.setSelectedIndex(selectedIndex);
        }

    }

    public void browseDrivers(ActionEvent e) {

        FileSelector jarFiles = new FileSelector(
                new String[]{"jar"}, "Java Archive files");

        FileSelector zipFiles = new FileSelector(
                new String[]{"zip"}, "ZIP Archive files");

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(zipFiles);
        fileChooser.addChoosableFileFilter(jarFiles);

        fileChooser.setDialogTitle("Select JDBC Drivers...");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");

        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File[] files = fileChooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {

            jarPathListModel.addElement(files[i].getAbsolutePath());
        }

        databaseDriver.setPath(jarPathsFormatted());
    }

    /**
     * Checks the current selection for a name change
     * to be propagated back to the tree view.
     */
    private void checkNameUpdate() {
        String oldName = databaseDriver.getName();
        String newName = nameField.getText().trim();
        if (!oldName.equals(newName)) {
            databaseDriver.setName(newName);
            parent.nodeNameValueChanged(databaseDriver);
        }
    }

    /**
     * Populates the driver object from the field values.
     */
    private void populateDriverObject() {

        databaseDriver.setDescription(descField.getText());
        databaseDriver.setClassName(classField.getText());
        databaseDriver.setURL(driverUrlCombo.getEditor().getItem().toString());
        databaseDriver.setPath(jarPathsFormatted());

        DatabaseDefinition database = getSelectedDatabase();
        if (database.getId() > 0) {
            databaseDriver.setDatabaseType(database.getId());
        }

        checkNameUpdate();

        if (databaseDriver.getId() == 0) {
            databaseDriver.setId(System.currentTimeMillis());
            // need to check exisitng conns with this driver's name
        }

    }

    private void driverPathsToList(String driversPath) {
        if (!MiscUtils.isNull(driversPath)) {
            String[] paths = driversPath.split(";");
            for (String path : paths) {
                jarPathListModel.addElement(path);
            }
        }
    }

    public void setDriver(DatabaseDriver databaseDriver) {

        // store the field values of the current selection
        if (this.databaseDriver != null) {

            populateDriverObject();
        }

        try {

            databaseNameCombo.removeItemListener(this);

            this.databaseDriver = databaseDriver;

            nameField.setText(databaseDriver.getName());
            descField.setText(formatDriverDescription(databaseDriver));
            classField.setText(databaseDriver.getClassName());

            jarPathListModel.clear();
            driverPathsToList(databaseDriver.getPath());

            int databaseId = databaseDriver.getType();
            DatabaseDefinition database = DatabaseDefinitionCache.
                    getDatabaseDefinition(databaseId);

            if (database != null && database.isValid()) {

                resetUrlCombo(database);
                databaseNameCombo.setSelectedItem(database);

            } else {

                urlComboModel.removeAllElements();
                databaseNameCombo.setSelectedIndex(0);
            }

            String url = databaseDriver.getURL();
            if (!MiscUtils.isNull(url)) {

                urlComboModel.insertElementAt(url, 0);
            }

            if (urlComboModel.getSize() > 0) {

                driverUrlCombo.setSelectedIndex(0);
            }

            nameField.requestFocusInWindow();
            nameField.selectAll();

        } finally {

            databaseNameCombo.addItemListener(this);
        }

    }

    private String formatDriverDescription(DatabaseDriver databaseDriver) {
        return databaseDriver.getDescription().equals("Not Available") ?
                "" : databaseDriver.getDescription();
    }

    private void resetUrlCombo(DatabaseDefinition database) {

        String firstElement = null;
        if (urlComboModel.getElementAt(0) != null) {
            firstElement = urlComboModel.getElementAt(0).toString();
        }

        urlComboModel.removeAllElements();

        if (firstElement != null) {
            urlComboModel.addElement(firstElement);
        }

        for (int i = 0, n = database.getUrlCount(); i < n; i++) {
            urlComboModel.addElement(database.getUrl(i));
        }
    }

    public DatabaseDriver getDriver() {
        return databaseDriver;
    }

    /**
     * Performs some cleanup and releases resources before being closed.
     */
    public void cleanup() {

    }

    /**
     * Refreshes the data and clears the cache
     */
    public void refresh() {
    }

    /**
     * Returns the print object - if any
     */
    public Printable getPrintable() {
        return null;
    }

    /**
     * Returns the name of this panel
     */
    public String getLayoutName() {
        return TITLE;
    }

}







