package org.executequery.gui.browser.connection;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.BrowserController;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.TransactionIsolationComboBox;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author Alexey Kozlov
 */
public class AdvancedPropertiesPanel extends JPanel {

    private static final List<String> IGNORED_KEYS = Arrays.asList(
            "password",
            "lc_ctype",
            "useGSSAuth",
            "process_id",
            "process_name",
            "roleName",
            "isc_dpb_trusted_auth",
            "isc_dpb_multi_factor_auth",
            "isc_dpb_certificate_base64",
            "isc_dpb_repository_pin",
            "isc_dpb_verify_server"
    );

    private DatabaseConnection connection;
    private final BrowserController controller;

    // --- gui components ---

    private JButton addButton;
    private JButton removeButton;
    private JButton setLevelButton;
    private JTable propertiesTable;
    private JdbcPropertiesModel propertiesModel;
    private TransactionIsolationComboBox levelsCombo;

    // ---

    public AdvancedPropertiesPanel(DatabaseConnection connection, BrowserController controller) {
        super(new GridBagLayout());
        this.connection = connection;
        this.controller = controller;

        init();
        arrange();
    }

    private void init() {

        levelsCombo = WidgetFactory.createTransactionIsolationComboBox("levelsCombo");
        addButton = WidgetFactory.createButton("addButton", Bundles.get("common.add.button"), e -> add());
        removeButton = WidgetFactory.createButton("removeButton", Bundles.get("common.delete.button"), e -> remove());

        setLevelButton = WidgetFactory.createButton("setLevelButton", Bundles.get("common.apply.button"), e -> setLevel());
        setLevelButton.setEnabled(false);

        propertiesModel = new JdbcPropertiesModel();
        propertiesTable = WidgetFactory.createTable("propertiesTable", propertiesModel);
        propertiesTable.getTableHeader().setReorderingAllowed(false);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- properties panel ---

        JPanel propertiesPanel = WidgetFactory.createPanel("propertiesPanel");
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(bundleString("JDBCProperties")));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        propertiesPanel.add(new JLabel(bundleString("propertiesText")), gbh.topGap(8).spanX().get());
        propertiesPanel.add(new JScrollPane(propertiesTable), gbh.nextRow().topGap(0).setMaxWeightY().fillBoth().get());
        propertiesPanel.add(new JPanel(), gbh.nextRow().setWidth(1).setMaxWeightX().setMinWeightY().get());
        propertiesPanel.add(addButton, gbh.nextCol().leftGap(0).setMinWeightX().get());
        propertiesPanel.add(removeButton, gbh.nextCol().get());

        // --- transaction panel ---

        JPanel transactionPanel = WidgetFactory.createPanel("transactionPanel");
        transactionPanel.setBorder(BorderFactory.createTitledBorder(bundleString("TransactionIsolation")));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        transactionPanel.add(new JLabel(bundleString("transactionText")), gbh.topGap(8).spanX().get());
        transactionPanel.add(new JLabel(bundleString("IsolationLevel")), gbh.nextRow().setMinWeightX().bottomGap(5).setWidth(1).get());
        transactionPanel.add(levelsCombo, gbh.nextCol().leftGap(0).topGap(5).setMaxWeightX().get());
        transactionPanel.add(setLevelButton, gbh.nextCol().setMinWeightX().get());

        // --- main panel ---

        JPanel mainPanel = WidgetFactory.createPanel("mainPanel");

        gbh = new GridBagHelper().anchorNorthWest().fillBoth().spanX();
        mainPanel.add(propertiesPanel, gbh.setMaxWeightY().get());
        mainPanel.add(transactionPanel, gbh.nextRow().topGap(5).setMinWeightY().get());

        // --- base ---

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());
    }

    // --- handlers ---

    private void add() {
        propertiesModel.addRow(Bundles.getCommons(new String[]{"key", "value"}));
    }

    private void remove() {

        int selectedRow = propertiesTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        String key = (String) propertiesTable.getValueAt(selectedRow, 0);
        removeProperty(key);

        updatePropertiesTable();

        int rowsCount = propertiesTable.getModel().getRowCount();
        if (rowsCount > 0) {
            if (selectedRow >= rowsCount)
                selectedRow--;

            propertiesTable.setRowSelectionInterval(selectedRow, selectedRow);
            propertiesTable.requestFocusInWindow();
        }
    }

    private void setLevel() {

        if (!ConnectionManager.isTransactionSupported(connection)) {
            GUIUtilities.displayWarningMessage(bundleString("setLevel.warning"));
            return;
        }

        try {
            applyTransactionLevel(true);
            if (connection.getTransactionIsolation() != -1)
                GUIUtilities.displayInformationMessage(bundleString("setLevel.success", levelsCombo.getSelectedItem()));

        } catch (DataSourceException e) {
            GUIUtilities.displayWarningMessage(bundleString("setLevel.exception", e.getMessage()));
        }
    }

    // --- jdbc properties methods ---

    private void putProperty(String key, String val) {

        if (connection == null)
            return;

        Properties properties = connection.getJdbcProperties();
        properties.put(key, val);
        connection.setJdbcProperties(properties);
    }

    private void removeProperty(String key) {

        if (connection == null || key == null)
            return;

        Properties properties = connection.getJdbcProperties();
        properties.remove(key);
        connection.setJdbcProperties(properties);
    }

    private Object[][] getProperties() {

        if (connection == null)
            return new Object[0][0];

        Properties properties = connection.getJdbcProperties();
        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            String key = (String) prop.getKey();
            if (propertyIgnored(key))
                properties.remove(key);
        }

        int index = 0;
        Object[][] data = new Object[properties.size()][2];
        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            data[index][0] = prop.getKey();
            data[index][1] = prop.getValue();
            index++;
        }

        return data;
    }

    private boolean propertyIgnored(String key) {
        return key != null && IGNORED_KEYS.contains(key);
    }

    // --- transaction isolation methods ---

    /**
     * Applies the transaction level on open connections of the type selected.
     */
    public void applyTransactionLevel(boolean reloadProperties) throws DataSourceException {
        int isolationLevel = getTransactionIsolationLevel();
        ConnectionManager.setTransactionIsolationLevel(connection, isolationLevel);

        if (reloadProperties)
            controller.updateDatabaseProperties();
    }

    /**
     * Sets the values for the transaction level on the connection object
     * based on the value from the combo.
     */
    public int getTransactionIsolationLevel() {

        if (connection == null)
            return -1;

        int value = levelsCombo.getSelectedLevel();
        connection.setTransactionIsolation(value);

        return value;
    }

    /**
     * Sets the values for the tx level on the tx combo
     * based on the value from the connection object.
     */
    public void setTransactionIsolationLevel() {

        if (connection == null)
            return;

        int value = connection.getTransactionIsolation();
        levelsCombo.setSelectedLevel(value);
    }

    // ---

    public void setConnection(DatabaseConnection connection) {
        this.connection = connection;
        update();
    }

    public void update() {
        updatePropertiesTable();
        setTransactionIsolationLevel();
    }

    public void updatePropertiesTable() {
        propertiesModel.update();
    }

    public void setTransactionEnabled(boolean enable) {
        levelsCombo.setEnabled(enable);
        setLevelButton.setEnabled(enable);
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(AdvancedPropertiesPanel.class, key, args);
    }

    // ---

    private class JdbcPropertiesModel extends DefaultTableModel {
        private final String[] columnNames = Bundles.getCommons(new String[]{"key", "value"});

        public JdbcPropertiesModel() {
            update();
        }

        public void update() {
            setDataVector(getProperties(), columnNames);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {

            if (col == 0) {

                String newKey = (String) value;
                if (propertyIgnored(newKey)) {
                    GUIUtilities.displayWarningMessage(bundleString("propertyLocked"));
                    return;
                }

                String oldKey = (String) getValueAt(row, 0);
                removeProperty(oldKey);
            }

            super.setValueAt(value, row, col);

            String key = (String) getValueAt(row, 0);
            String val = (String) getValueAt(row, 1);
            putProperty(
                    key != null ? key : "",
                    val != null ? val : ""
            );
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

    } // JdbcPropertiesModel class

}
