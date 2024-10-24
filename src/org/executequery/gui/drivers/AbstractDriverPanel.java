/*
 * AbstractDriverPanel.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.datasource.DatabaseDefinition;
import org.executequery.gui.SimpleValueSelectionDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseDefinitionCache;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.FileSelector;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

public abstract class AbstractDriverPanel extends JPanel
        implements DriverPanel,
        ItemListener,
        FocusListener {

    /**
     * The currently selected driver
     */
    private DatabaseDriver databaseDriver;

    // --- GUI components ---

    private JButton findButton;
    private JButton addButton;
    private JButton removeButton;

    private JTextField nameField;
    private JTextField descriptionField;

    private JList<?> jarPathList;
    private JComboBox<?> classCombo;
    private JComboBox<?> databaseCombo;
    private JComboBox<?> driverUrlCombo;

    private DefaultListModel<String> jarPathModel;
    private DynamicComboBoxModel urlComboModel;
    private DynamicComboBoxModel classComboModel;

    // ---

    public AbstractDriverPanel() {
        super(new BorderLayout());
        init();
        arrange();
    }

    private void init() {

        addButton = WidgetFactory.createRolloverButton(
                "browseButton",
                bundleString("addLibraryButton"),
                "icon_add",
                e -> browseDrivers()
        );

        removeButton = WidgetFactory.createRolloverButton(
                "removeButton",
                bundleString("addRemoveButton"),
                "icon_delete",
                e -> removeDrivers()
        );

        findButton = WidgetFactory.createButton(
                "findButton",
                bundleString("addFindButton"),
                e -> findDriverClass()
        );

        jarPathModel = new DefaultListModel<>();
        jarPathList = WidgetFactory.createList("jarPathList", jarPathModel);

        classComboModel = new DynamicComboBoxModel();
        classCombo = WidgetFactory.createComboBox("classField", classComboModel);
        classCombo.setEditable(true);

        urlComboModel = new DynamicComboBoxModel();
        driverUrlCombo = WidgetFactory.createComboBox("driverUrlCombo", urlComboModel);
        driverUrlCombo.setToolTipText(bundleString("jdbcUrlToolTip"));
        driverUrlCombo.setEditable(true);

        databaseCombo = WidgetFactory.createComboBox("databaseNameCombo", DatabaseDefinitionCache.getDatabaseDefinitions());
        databaseCombo.addItemListener(this);

        nameField = WidgetFactory.createTextField("nameField");
        nameField.addFocusListener(this);

        descriptionField = WidgetFactory.createTextField("descField");
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- library panel ---

        JPanel libraryPanel = WidgetFactory.createPanel("libraryPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillBoth();
        libraryPanel.add(new JScrollPane(jarPathList), gbh.setMaxWeightX().spanY().get());
        libraryPanel.add(addButton, gbh.nextCol().setHeight(1).setMinWeightX().leftGap(5).anchorCenter().fillNone().get());
        libraryPanel.add(removeButton, gbh.nextRow().topGap(5).get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("driverNameLabel")), gbh.setMinWeightX().topGap(3).get());
        mainPanel.add(nameField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(0).spanX().get());
        mainPanel.add(new JLabel(bundleString("descriptionLabel")), gbh.nextRowFirstCol().setWidth(1).setMinWeightX().leftGap(0).topGap(8).get());
        mainPanel.add(descriptionField, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).spanX().get());
        mainPanel.add(new JLabel(bundleString("databaseLabel")), gbh.setWidth(1).nextRowFirstCol().setMinWeightX().leftGap(0).topGap(8).get());
        mainPanel.add(databaseCombo, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).spanX().get());
        mainPanel.add(new JLabel(bundleString("jdbcUrlLabel")), gbh.setWidth(1).nextRowFirstCol().setMinWeightX().leftGap(0).topGap(8).get());
        mainPanel.add(driverUrlCombo, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).spanX().get());
        mainPanel.add(new JLabel(bundleString("pathLabel")), gbh.setWidth(1).nextRowFirstCol().setMinWeightX().leftGap(0).topGap(8).get());
        mainPanel.add(libraryPanel, gbh.nextCol().setMaxWeightX().setMaxWeightY().leftGap(5).topGap(5).fillBoth().spanX().get());
        mainPanel.add(new JLabel(bundleString("classNameLabel")), gbh.setWidth(1).nextRowFirstCol().setMinWeightX().setMinWeightY().leftGap(0).topGap(8).fillHorizontally().get());
        mainPanel.add(classCombo, gbh.nextCol().setMaxWeightX().leftGap(5).topGap(5).get());
        mainPanel.add(findButton, gbh.nextCol().setMinWeightX().get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();
        add(mainPanel, gbh.spanX().spanY().get());
    }

    private void databaseChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.DESELECTED) {
            urlComboModel.removeAllElements();
            return;
        }

        DatabaseDefinition database = getSelectedDatabase();
        int databaseId = database.getId();

        if (databaseId > 0) {
            resetUrlCombo(database);
            return;
        }

        urlComboModel.removeAllElements();
    }

    private DatabaseDefinition getSelectedDatabase() {
        return (DatabaseDefinition) databaseCombo.getSelectedItem();
    }

    private String jarPathsFormatted() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0, modelSize = jarPathModel.size(); i < modelSize; i++) {
            sb.append(jarPathModel.get(i));
            if (i < (modelSize - 1))
                sb.append(";");
        }

        return sb.toString();
    }

    public void findDriverClass() {

        if (jarPathModel.isEmpty()) {
            GUIUtilities.displayErrorMessage(bundleString("pathListEmptyError"));
            return;
        }

        String[] drivers;
        try {
            GUIUtilities.showWaitCursor();
            drivers = implementingDriverClasses();

        } finally {
            GUIUtilities.showNormalCursor();
        }

        if (drivers.length == 0) {
            GUIUtilities.displayWarningMessage(bundleString("noDriverClassesError"));
            return;
        }

        SimpleValueSelectionDialog dialog = new SimpleValueSelectionDialog(bundleString("selectJdbcDriverLabel"), drivers);
        if (dialog.showDialog() == JOptionPane.OK_OPTION) {

            String value = dialog.getValue();
            if (StringUtils.isNotBlank(value)) {
                classCombo.setSelectedItem(value);
                databaseDriver.setClassName(value);
            }
        }
    }

    private String[] implementingDriverClasses() {
        try {
            return MiscUtils.findImplementingClasses("java.sql.Driver", jarPathsFormatted());

        } catch (MalformedURLException e) {
            Log.debug("Error locating driver class", e);
            GUIUtilities.displayErrorMessage(bundleString("pathListEmptyError"));

        } catch (IOException e) {
            Log.debug("Error locating driver class", e);
            GUIUtilities.displayErrorMessage(bundleString("ioError", e.getMessage()));
        }

        return new String[0];
    }

    public void removeDrivers() {
        int selectedIndex = jarPathList.getSelectedIndex();

        List<?> selections = jarPathList.getSelectedValuesList();
        if (selections != null && !selections.isEmpty()) {
            for (Object path : selections)
                jarPathModel.removeElement(path);
        }

        int newSize = jarPathModel.size();
        if (newSize > 0) {
            if (selectedIndex > newSize - 1)
                selectedIndex = newSize - 1;

            jarPathList.setSelectedIndex(selectedIndex);
        }

        populateDriverClassCombo();
    }

    public void browseDrivers() {
        FileSelector jarFiles = new FileSelector(new String[]{"jar"}, bundleString("javaArchiveFiles"));
        FileSelector zipFiles = new FileSelector(new String[]{"zip"}, bundleString("zipArchiveFiles"));

        final FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setDialogTitle(bundleString("selectJdbcDrivers"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.addChoosableFileFilter(zipFiles);
        fileChooser.addChoosableFileFilter(jarFiles);
        fileChooser.setMultiSelectionEnabled(true);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), bundleString("select"));
        if (result != JFileChooser.CANCEL_OPTION)
            ThreadUtils.startWorker(() -> selectDriver(fileChooser));
    }

    private void selectDriver(FileChooserDialog fileChooser) {
        GUIUtilities.showWaitCursor();

        try {
            for (File file : fileChooser.getSelectedFiles()) {
                String path = file.getAbsolutePath();
                if (!jarPathModel.contains(path))
                    jarPathModel.addElement(path);
            }

            databaseDriver.setPath(jarPathsFormatted());
            populateDriverClassCombo();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    /**
     * Populates the driver object from the field values.
     */
    protected final void populateDriverObject() {

        if (databaseDriver == null)
            return;

        databaseDriver.setName(nameField.getText());
        databaseDriver.setDescription(descriptionField.getText());
        databaseDriver.setClassName(driverClassName());
        databaseDriver.setURL(driverUrlCombo.getEditor().getItem().toString());
        databaseDriver.setPath(jarPathsFormatted());

        DatabaseDefinition database = getSelectedDatabase();
        if (database.getId() > 0)
            databaseDriver.setDatabaseType(database.getId());

        if (databaseDriver.getId() == 0)
            databaseDriver.setId(System.currentTimeMillis());
    }

    private String driverClassName() {

        Object selectedItem = classCombo.getSelectedItem();
        if (selectedItem != null)
            return selectedItem.toString();

        return Constants.EMPTY;
    }

    private void driverPathsToList(String driversPath) {
        if (!MiscUtils.isNull(driversPath))
            for (String path : driversPath.split(";"))
                jarPathModel.addElement(path);
    }

    private void populateDriverClassCombo() {
        String[] driverClasses = implementingDriverClasses();

        String currentSelection = null;
        if (classCombo.getSelectedItem() != null)
            currentSelection = driverClassName();

        classComboModel.setElements(driverClasses);

        if (currentSelection != null) {
            for (String clazz : driverClasses) {
                if (StringUtils.equals(currentSelection, clazz)) {
                    classComboModel.setSelectedItem(clazz);
                    break;
                }
            }
        }
    }

    private DatabaseDefinition loadDatabaseDefinition(int databaseId) {
        return DatabaseDefinitionCache.getDatabaseDefinition(databaseId);
    }

    private String formatDriverDescription(DatabaseDriver databaseDriver) {

        String description = databaseDriver.getDescription();
        if (Objects.equals(description, "Not Available"))
            description = "";

        return description;
    }

    @SuppressWarnings("unchecked")
    private void resetUrlCombo(DatabaseDefinition database) {

        String firstElement = null;
        if (urlComboModel.getElementAt(0) != null)
            firstElement = urlComboModel.getElementAt(0).toString();

        urlComboModel.removeAllElements();

        if (firstElement != null)
            urlComboModel.addElement(firstElement);

        for (int i = 0, n = database.getUrlCount(); i < n; i++)
            urlComboModel.addElement(database.getUrl(i));
    }

    // --- DriverPanel impl ---

    @Override
    @SuppressWarnings("unchecked")
    public void setDriver(DatabaseDriver databaseDriver) {
        databaseCombo.removeItemListener(this);
        this.databaseDriver = databaseDriver;

        try {
            nameField.setText(databaseDriver.getName());
            descriptionField.setText(formatDriverDescription(databaseDriver));

            jarPathModel.clear();
            driverPathsToList(databaseDriver.getPath());

            populateDriverClassCombo();
            if (!classComboModel.contains(databaseDriver.getClassName()))
                classComboModel.addElement(databaseDriver.getClassName());
            classComboModel.setSelectedItem(databaseDriver.getClassName());

            int databaseId = databaseDriver.getType();
            DatabaseDefinition database = loadDatabaseDefinition(databaseId);
            if (database.isValid()) {
                resetUrlCombo(database);
                databaseCombo.setSelectedItem(database);
            } else {
                urlComboModel.removeAllElements();
                databaseCombo.setSelectedIndex(0);
            }

            String url = databaseDriver.getURL();
            if (!MiscUtils.isNull(url))
                urlComboModel.insertElementAt(url, 0);

            if (urlComboModel.getSize() > 0)
                driverUrlCombo.setSelectedIndex(0);

            nameField.requestFocusInWindow();
            nameField.selectAll();

        } finally {
            databaseCombo.addItemListener(this);
        }
    }

    @Override
    public DatabaseDriver getDriver() {
        populateDriverObject();
        return databaseDriver;
    }

    @Override
    public void driverNameChanged() {
    }

    // --- ItemListener impl ---

    @Override
    public void itemStateChanged(ItemEvent e) {
        databaseChanged(e);
    }

    // --- FocusListener impl ---

    @Override
    public void focusLost(FocusEvent e) {
        driverNameChanged();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    // ---

    protected final String bundleString(String key, Object... args) {
        return Bundles.get(AbstractDriverPanel.class, key, args);
    }

}
