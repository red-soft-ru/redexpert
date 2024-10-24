/*
 * SimplePreferencesPanel.java
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

package org.executequery.gui.prefs;

import org.apache.commons.io.FileExistsException;
import org.executequery.*;
import org.executequery.components.table.CategoryHeaderCellRenderer;
import org.executequery.components.table.FileSelectionTableCell;
import org.executequery.gui.resultset.ResultSetCellAlign;
import org.executequery.localization.Bundles;
import org.executequery.localization.InterfaceLanguage;
import org.executequery.log.Log;
import org.executequery.plaf.LookAndFeelType;
import org.underworldlabs.swing.celleditor.picker.StringPicker;
import org.underworldlabs.swing.table.*;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.LabelValuePair;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.*;

/**
 * Properties panel base.
 *
 * @author Takis Diakoumis
 */
public class SimplePreferencesPanel extends JPanel
        implements MouseListener {

    /**
     * the table grid colour
     */
    private static Color GRID_COLOR;

    /**
     * the gutter width
     */
    private static int GUTTER_WIDTH;

    /**
     * fixed row height for a preference value
     */
    protected final int VALUE_ROW_HEIGHT = 20;

    /**
     * fixed row height for a preference header
     */
    protected final int CATEGORY_ROW_HEIGHT = 18;

    /**
     * preferences array that this panel displays
     */
    private UserPreference[] preferences;

    /**
     * the table display
     */
    private JTable table;

    /**
     * the table model
     */
    private PreferencesTableModel tableModel;

    private Map<String, DefaultCellEditor> cellEditors;

    private List<PreferenceTableModelListener> listeners;

    static {

        GRID_COLOR = UIManager.getColor("Table.gridColor");// Color.LIGHT_GRAY;
        GUTTER_WIDTH = 10;
    }

    /**
     * Creates a new instance of SimplePreferencesPanel
     */
    public SimplePreferencesPanel(UserPreference[] preferences) {

        super(new BorderLayout());
        this.preferences = preferences;

        listeners = new ArrayList<>();

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        tableModel = new PreferencesTableModel();

        table = new JTable(tableModel);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);
        table.setFont(AbstractPropertiesBasePanel.getDefaultFont());
        table.setTableHeader(null);

        EachRowEditor rowEditor = new EachRowEditor(table);

        // lazily create as required
        FileSelectionTableCell fileRenderer = null;
        FileSelectionTableCell dirRenderer = null;
        ColourTableCellRenderer colourRenderer = null;
        CheckBoxTableCellRenderer checkBoxRenderer = null;
        CategoryHeaderCellRenderer categoryRenderer = null;
        ComboBoxCellRenderer comboRenderer = null;
        PasswordTableCellRenderer passwordRenderer = null;

        EachRowRenderer rowRendererKeys = null;
        EachRowRenderer rowRendererValues = new EachRowRenderer();

        cellEditors = new HashMap<String, DefaultCellEditor>();

        for (int i = 0; i < preferences.length; i++) {
            int type = preferences[i].getType();
            DefaultCellEditor editor = null;

            switch (type) {
                case UserPreference.ENUM_TYPE:
                case UserPreference.STRING_TYPE:
                    Object[] values = preferences[i].getAvailableValues();
                    if (values != null && values.length > 0) {
                        editor = new DefaultCellEditor(new TableComboBox(values));
                        rowEditor.setEditorAt(i, editor);

                        if (comboRenderer == null) {
                            comboRenderer = new ComboBoxCellRenderer();
                        }

                        rowRendererValues.add(i, comboRenderer);
                    } else {
                        rowEditor.setEditorAt(i,
                                new DefaultCellEditor(new StringPicker()));
                        //rowEditor.setEditorAt(i, editor);
                    }
                    break;
                case UserPreference.PASSWORD_TYPE:
                    PasswordCellEditor passwordCellEditor = new PasswordCellEditor();
                    if (passwordRenderer == null) {

                        passwordRenderer = new PasswordTableCellRenderer(passwordCellEditor.getEchoChar());
                    }
                    rowRendererValues.add(i, passwordRenderer);
                    rowEditor.setEditorAt(i, new DefaultCellEditor(passwordCellEditor));
                    break;
                case UserPreference.INTEGER_TYPE:
                    final NumberCellEditor numEditor =
                            new NumberCellEditor(preferences[i].getMaxLength(), true);
                    numEditor.setFont(AbstractPropertiesBasePanel.getDefaultFont());

                    editor = new DefaultCellEditor(numEditor) {
                        public Object getCellEditorValue() {
                            return numEditor.getStringValue();
                        }
                    };

                    rowEditor.setEditorAt(i, editor);
                    break;
                case UserPreference.BOOLEAN_TYPE:

                    if (checkBoxRenderer == null) {
                        checkBoxRenderer = new CheckBoxTableCellRenderer();
                        checkBoxRenderer.setHorizontalAlignment(JLabel.LEFT);
                    }

                    rowRendererValues.add(i, checkBoxRenderer);
                    rowEditor.setEditorAt(i, new DefaultCellEditor(new JCheckBox()));
                    break;
                case UserPreference.COLOUR_TYPE:

                    if (colourRenderer == null) {
                        colourRenderer = new ColourTableCellRenderer();
                        colourRenderer.setFont(AbstractPropertiesBasePanel.getDefaultFont());
                        table.addMouseListener(this);
                    }

                    rowRendererValues.add(i, colourRenderer);
                    break;
                case UserPreference.CATEGORY_TYPE:

                    if (categoryRenderer == null) {
                        categoryRenderer = new CategoryHeaderCellRenderer();
                    }
                    if (rowRendererKeys == null) {
                        rowRendererKeys = new EachRowRenderer();
                    }

                    rowRendererValues.add(i, categoryRenderer);
                    rowRendererKeys.add(i, categoryRenderer);
                    break;
                case UserPreference.FILE_TYPE:

                    if (fileRenderer == null) {
                        fileRenderer = new FileSelectionTableCell(JFileChooser.FILES_ONLY);
                        fileRenderer.setFont(AbstractPropertiesBasePanel.getDefaultFont());
                    }

                    rowRendererValues.add(i, fileRenderer);
                    rowEditor.setEditorAt(i, fileRenderer);
                    break;

                case UserPreference.DIR_TYPE:

                    if (dirRenderer == null) {
                        dirRenderer = new FileSelectionTableCell(JFileChooser.DIRECTORIES_ONLY);
                        dirRenderer.setFont(AbstractPropertiesBasePanel.getDefaultFont());
                    }

                    rowRendererValues.add(i, dirRenderer);
                    rowEditor.setEditorAt(i, dirRenderer);
                    break;

            }

            cellEditors.put(preferences[i].getKey(), editor);

            if (type == UserPreference.CATEGORY_TYPE) {
                table.setRowHeight(i, CATEGORY_ROW_HEIGHT);
            } else {
                table.setRowHeight(i, VALUE_ROW_HEIGHT);
            }

        }

        table.setGridColor(GRID_COLOR);
        table.setRowHeight(AbstractPropertiesBasePanel.TABLE_ROW_HEIGHT);
        TableColumnModel tcm = table.getColumnModel();

        int secondColumnWidth = 200;
        TableColumn column = tcm.getColumn(2);
        column.setCellRenderer(rowRendererValues);
        column.setCellEditor(rowEditor);
        column.setPreferredWidth(secondColumnWidth);
        column.setMaxWidth(secondColumnWidth);
        column.setMinWidth(secondColumnWidth);

        column = tcm.getColumn(1);
        column.setCellRenderer(rowRendererKeys);

        column = tcm.getColumn(0);
        column.setMaxWidth(GUTTER_WIDTH);
        column.setMinWidth(GUTTER_WIDTH);
        column.setPreferredWidth(GUTTER_WIDTH);
        column.setCellRenderer(categoryRenderer);

        DisplayViewport viewport = new DisplayViewport(table);
        JScrollPane scroller = new JScrollPane();
        scroller.setViewport(viewport);
        add(scroller, BorderLayout.CENTER);

        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

                tableValueChangedForRow(e.getFirstRow());
            }
        });

    }

    private void tableValueChangedForRow(int row) {

        firePreferenceTableModelChange(new PreferenceTableModelChangeEvent(preferences[row]));
    }

    private void firePreferenceTableModelChange(PreferenceTableModelChangeEvent e) {

        for (PreferenceTableModelListener listener : listeners) {

            listener.preferenceTableModelChange(e);
        }

    }

    public void addPreferenceTableModelListener(PreferenceTableModelListener listener) {

        listeners.add(listener);
    }

    protected void restoreDefaults() {

        for (UserPreference preference : preferences) {
            switch (preference.getType()) {

                case UserPreference.STRING_TYPE:
                case UserPreference.INTEGER_TYPE:
                case UserPreference.FILE_TYPE:
                case UserPreference.DIR_TYPE:
                    preference.reset(getDefaultStringValue(preference));
                    if (Objects.equals(preference.getKey(), "startup.java.path"))
                        JavaFileProperty.restore();
                    break;

                case UserPreference.ENUM_TYPE:
                    preference.reset(getDefaultEnumValue(preference));
                    break;

                case UserPreference.BOOLEAN_TYPE:
                    preference.reset(getDefaultBooleanValue(preference));
                    break;

                case UserPreference.COLOUR_TYPE:
                    preference.reset(getDefaultColorValue(preference));
                    break;
            }
        }
        fireTableDataChanged();
    }

    private static String getDefaultStringValue(UserPreference preference) {
        return SystemProperties.getProperty("defaults", preference.getKey());
    }

    private static Color getDefaultColorValue(UserPreference preference) {
        return SystemProperties.getColourProperty("defaults", preference.getKey());
    }

    private static Boolean getDefaultBooleanValue(UserPreference preference) {
        return Boolean.valueOf(getDefaultStringValue(preference));
    }

    private static Object getDefaultEnumValue(UserPreference preference) {

        Class<?> clazz = preference.getValue().getClass();
        if (Objects.equals(clazz, LabelValuePair.class))
            clazz = ((LabelValuePair) preference.getValue()).getValue().getClass();

        String value = getDefaultStringValue(preference);
        if (Objects.equals(clazz, LookAndFeelType.class))
            return LookAndFeelType.valueOf(value);
        if (Objects.equals(clazz, InterfaceLanguage.class))
            return InterfaceLanguage.valueOf(value);
        if (Objects.equals(clazz, ResultSetCellAlign.class))
            return ResultSetCellAlign.valueOf(value);

        return value;
    }

    protected void fireTableDataChanged() {

        tableModel.fireTableDataChanged();
    }

    public Component getComponentEditorForKey(String key) {

        return cellEditors.get(key).getComponent();
    }

    public UserPreference[] getPreferences() {

        return preferences;
    }

    public Object getValue(String key) {

        for (UserPreference userPreference : preferences) {

            if (key.equals(userPreference.getKey())) {

                return userPreference.getValue();
            }
        }

        return null;
    }

    protected void savePreferences() {

        String propertiesName = "user";

        // stop table editing
        if (table.isEditing())
            table.editingStopped(null);

        // set the new properties
        for (UserPreference preference : preferences) {
            if (preference.getType() != UserPreference.CATEGORY_TYPE) {

                if (preference.getKey().equals("startup.java.path")) {

                    // TODO remove in the next release
                    // needs to get rid of the old relative path syntax
                    String pathToJava = preference.getSaveValue();
                    if (pathToJava.startsWith("%re%")) {
                        pathToJava = pathToJava.replace("%re%" + FileSystems.getDefault().getSeparator(), "");
                        pathToJava = pathToJava.replace("%re%", "");
                    }

                    if (JavaFileProperty.setValue(pathToJava)) {
                        SystemProperties.setProperty(propertiesName, preference.getKey(), pathToJava);
                        PropertiesPanel.setUpdateEnvNeed(true);
                    }

                } else
                    SystemProperties.setProperty(propertiesName, preference.getKey(), preference.getSaveValue());

                if (preference.getKey().equals("startup.display.language"))
                    System.setProperty("user.language", preference.getSaveValue());
            }
        }

    }

    private void leftButtonAction(int col, int row, int valueColumn) {

        if (col == valueColumn) {

            if (preferences[row].getType() == UserPreference.COLOUR_TYPE) {

                Color oldColor = (Color) preferences[row].getValue();
                Color newColor = JColorChooser.showDialog(
                        GUIUtilities.getInFocusDialogOrWindow(),
                        Bundles.get("LocaleManager.ColorChooser.title"),
                        (Color) tableModel.getValueAt(row, valueColumn));

                if (newColor != null) {
                    tableModel.setValueAt(newColor, row, valueColumn);
                    firePropertyChange(Constants.COLOUR_PREFERENCE, oldColor, newColor);
                }
            }
        }

    }

    private void rightButtonAction(int col, int row, int valueColumn, MouseEvent evt) {

        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editButton = new JMenuItem(Bundles.get("common.edit"));
        editButton.addActionListener(e -> leftButtonAction(col, row, valueColumn));
        popupMenu.add(editButton);

        JMenuItem resetButton = new JMenuItem(Bundles.get("common.default"));
        resetButton.addActionListener(e -> resetColour(tableModel, row, col));
        popupMenu.add(resetButton);

        popupMenu.show(table, evt.getX(), evt.getY());
    }

    private void resetColour(TableModel model, int row, int col) {

        if (model instanceof PreferencesTableModel)
            ((PreferencesTableModel) model).restoreSingleDefault(row, col);

    }

    @Override
    public void mouseClicked(MouseEvent evt) {

        int valueColumn = 2;
        int row = table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());

        if (row == -1)
            return;

        if (evt.getButton() == MouseEvent.BUTTON1)  // left mouse button
            leftButtonAction(col, row, valueColumn);
        else if (evt.getButton() == MouseEvent.BUTTON3 && col == 2) // right mouse button
            rightButtonAction(col, row, valueColumn, evt);

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    class PreferencesTableModel extends AbstractTableModel {

        @Override
        public void setValueAt(Object value, int row, int column) {
            UserPreference preference = preferences[row];
            preference.setValue(value);
            fireTableCellUpdated(row, column);
        }

        @Override
        public int getRowCount() {
            return preferences.length;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int row, int column) {
            UserPreference preference = preferences[row];

            switch (column) {
                case 1:
                    return preference.getDisplayedKey();
                case 2:
                    return preference.getValue();
                default:
                    return Constants.EMPTY;
            }

        }

        @Override
        public boolean isCellEditable(int row, int column) {
            UserPreference preference = preferences[row];
            return (preference.getType() != UserPreference.CATEGORY_TYPE)
                    && (column == 2);
        }

        public void restoreSingleDefault(int row, int col) {

            Properties defaults = defaultsForTheme();
            UserPreference userPreference = preferences[row];
            String property = defaults.getProperty(userPreference.getKey());

            try {
                setValueAt(new Color(Integer.parseInt(property)), row, col);

            } catch (NumberFormatException e) {
                Log.error("Unable to set up default color, loaded property [" + property + "] could not convert to Integer");
            }
        }

        protected Properties defaultsForTheme() {

            LookAndFeelType selectedLaf = GUIUtilities.getLookAndFeel();

            String resourcePath;
            if (selectedLaf.isDefaultTheme()) {
                resourcePath = selectedLaf.isDarkTheme() ?
                        "org/executequery/gui/editor/resource/sql-syntax.default.dark.profile" :
                        "org/executequery/gui/editor/resource/sql-syntax.default.light.profile";
            } else {
                resourcePath = selectedLaf.isDarkTheme() ?
                        "org/executequery/gui/editor/resource/sql-syntax.classic.dark.profile" :
                        "org/executequery/gui/editor/resource/sql-syntax.classic.light.profile";
            }

            try {
                return FileUtils.loadPropertiesResource(resourcePath);

            } catch (IOException e) {
                throw new ApplicationException(e);
            }
        }

    } // class PreferencesTableModel

    class DisplayViewport extends JViewport {

        protected DisplayViewport(JTable _table) {
            setView(_table);
            setBackground(_table.getBackground());
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int viewHeight = getViewSize().height;
            g.setColor(GRID_COLOR);
            g.fillRect(0, viewHeight - 1, GUTTER_WIDTH, getHeight() - viewHeight + 1);
        }

    } // class DisplayViewport

    @SuppressWarnings({"rawtypes", "unchecked"})
    class TableComboBox extends JComboBox {

        public TableComboBox(Object[] values) {

            super(values);
            setFont(AbstractPropertiesBasePanel.getDefaultFont());
        }

    } // class TableComboBox

    private static class JavaFileProperty {

        private static final String CACHE_JAVA_PATH64 = ApplicationContext.getInstance().getUserSettingsHome() + ".cache_java_path64";
        private static final Path CACHE_JAVA_FILE_PATH = new File(CACHE_JAVA_PATH64).toPath();

        static boolean setValue(String path) {
            try {
                rewrite(path);

            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog("Error updating Java path property", e, JavaFileProperty.class);
                return false;
            }

            return true;
        }

        static void restore() {
            try {
                delete();

            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog("Error updating Java path property", e, JavaFileProperty.class);
            }
        }

        private static void rewrite(String pathToJava) throws Exception {

            if (pathToJava.isEmpty()) {
                delete();
                return;
            }

            String appPath = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            String validationPathToJava = appPath + FileSystems.getDefault().getSeparator() + pathToJava;

            if (!new File(pathToJava).exists() && !new File(validationPathToJava).exists())
                throw new FileExistsException("Specified file doesn't exists");

            String value = "jvm=" + pathToJava;

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pathToJava = pathToJava.replace("\\jvm.dll", "");
                value += "\npath=" + pathToJava.substring(0, pathToJava.lastIndexOf(FileSystems.getDefault().getSeparator()));
            }

            delete();
            Files.createFile(CACHE_JAVA_FILE_PATH);
            Files.write(CACHE_JAVA_FILE_PATH, value.getBytes(), StandardOpenOption.WRITE);
        }

        private static void delete() throws Exception {
            Files.deleteIfExists(CACHE_JAVA_FILE_PATH);
        }

    } // class JavaFileProperty

}
