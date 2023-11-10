/*
 * CompareDataTypesPanel.java
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

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObjectFactory;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
@SuppressWarnings("unchecked")
public class CompareDataTypesPanel extends DefaultTabViewActionPanel
        implements NamedView,
        ListSelectionListener,
        ConnectionListener {

    public static final String TITLE = "Compare Data Types ";
    public static final String FRAME_ICON = "CompareDataTypes16.svg";

    /**
     * the active connections combo box model
     */
    private DynamicComboBoxModel connectionsModel_1;

    /**
     * the mapped connections combo box model
     */
    private DynamicComboBoxModel connectionsModel_2;

    /**
     * the master list
     */
    private JList masterList;

    /**
     * the mapped list
     */
    private JList mappedList;

    /**
     * the active connections combo
     */
    private JComboBox connectionsCombo;

    /**
     * the active connections combo
     */
    private JComboBox connectionsCombo2;

    /**
     * the data type table view
     */
    private JTable table;

    /**
     * the table model
     */
    private DataTypesTableModel tableModel;

    /**
     * the instance count
     */
    private static int count = 1;

    /**
     * the column names
     */
    private String[] columns;

    /**
     * the master list model
     */
    private DataTypeListModel masterListModel;

    /**
     * the master data type meta data list
     */
    private List<List> masterTypes;

    /**
     * the mapped to data type meta data list
     */
    private List<List> mappedToTypes;

    /**
     * the table data
     */
    private List<List> tableData;

    /**
     * the selected data type
     */
    private List<List> selectedDataType;

    /**
     * the first database name filed
     */
    private DisabledField databaseField_1;

    /**
     * the second database name filed
     */
    private DisabledField databaseField_2;

    /**
     * the data type sorter
     */
    private DataTypeRowSorter sorter;

    /**
     * the result set column number for the type name
     */
    private static final int NAME_COLUMN = 1;

    /**
     * the result set column number for the type value
     */
    private static final int TYPE_COLUMN = 2;

    private DatabaseObjectFactory databaseObjectFactory;

    /**
     * Creates a new instance of CompareDataTypesPanel
     */
    public CompareDataTypesPanel() {
        super(new BorderLayout());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        databaseObjectFactory = new DatabaseObjectFactoryImpl();

        // combo boxes
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel_1 = new DynamicComboBoxModel(connections);

        connectionsCombo = WidgetFactory.createComboBox("connectionsCombo", connectionsModel_1);
        connectionsCombo.addActionListener(this);
        connectionsCombo.setActionCommand("firstConnectionChanged");

        connectionsModel_2 = new DynamicComboBoxModel(
                (Vector<DatabaseConnection>) connections);

        connectionsCombo2 = WidgetFactory.createComboBox("connectionsCombo2", connectionsModel_2);
        connectionsCombo2.addActionListener(this);
        connectionsCombo2.setActionCommand("secondConnectionChanged");

        masterListModel = new DataTypeListModel();
        masterList = new DefaultList(masterListModel);
        masterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        masterList.addListSelectionListener(this);

        mappedList = new DefaultList();
        mappedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        GridBagConstraints gbc = new GridBagConstraints();

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("First Connection"));

        databaseField_1 = new DisabledField();

        gbc.insets = new Insets(3, 2, 2, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridy++;
        leftPanel.add(new JLabel("Connection:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 5;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.left = 2;
        gbc.insets.top = 2;
        gbc.fill = GridBagConstraints.NONE;
        leftPanel.add(new JLabel("Database:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 5;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(databaseField_1, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 3;
        gbc.insets.left = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        leftPanel.add(new JScrollPane(masterList), gbc);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Second Connection"));

        databaseField_2 = new DisabledField();

        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.insets.left = 2;
        gbc.weighty = 0;
        gbc.weightx = 0;
        rightPanel.add(new JLabel("Connection:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(connectionsCombo2, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.left = 2;
        gbc.insets.top = 2;
        gbc.fill = GridBagConstraints.NONE;
        rightPanel.add(new JLabel("Database:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 5;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(databaseField_2, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.insets.top = 3;
        gbc.insets.left = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(new JScrollPane(mappedList), gbc);

        // setup the top panel
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 3, 3));
        topPanel.add(leftPanel);
        topPanel.add(rightPanel);

        // create the table
        tableModel = new DataTypesTableModel();
        table = new DefaultTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        // setup the table panel
        JPanel tablePanel = new JPanel(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets.left = 5;
        gbc.insets.right = 5;
        gbc.insets.top = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        tablePanel.add(new JLabel("Data Type Meta Data Comparison:"), gbc);
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets.bottom = 5;
        gbc.fill = GridBagConstraints.BOTH;
        tablePanel.add(new JScrollPane(table), gbc);

        tablePanel.setBorder(BorderFactory.createLineBorder(
                GUIUtilities.getDefaultBorderColour()));

        // setup the split panes
        JSplitPane verticalSplit = new SplitPaneFactory().createVertical();
        verticalSplit.setTopComponent(topPanel);
        verticalSplit.setBottomComponent(tablePanel);
        verticalSplit.setResizeWeight(0.7);
        verticalSplit.setDividerSize(5);

        setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(verticalSplit, BorderLayout.CENTER);

        sorter = new DataTypeRowSorter();

        // startup load
        buildFirstConnectionValues();
        buildSecondConnectionValues();

        verticalSplit.setDividerLocation(0.7);

        EventMediator.registerListener(this);
    }

    private String[] getColumnsArray(ResultSetMetaData rsmd) throws SQLException {
        String[] columnNames = new String[rsmd.getColumnCount()];
        for (int i = 1; i <= columnNames.length; i++) {
            columnNames[i - 1] = rsmd.getColumnName(i);
        }
        return columnNames;
    }

    /**
     * Indicates whether the cell renderer has been applied
     */
    private boolean rendererApplied;

    private void applyCellRenderer() {
        // init the cell renderer component
        TableColumnModel tcm = table.getColumnModel();
        for (int i = 0, n = tcm.getColumnCount(); i < n; i++) {
            tcm.getColumn(i).setCellRenderer(new DataTypeCellRenderer());
        }
        rendererApplied = true;
    }

    private List<List> buildDataTypeList(
            List<List> dataTypes, ResultSet rs, boolean reloadColumns) {

        try {

            if (reloadColumns) {
                // rebuild the columns based on the selection
                columns = getColumnsArray(rs.getMetaData());
            }

            boolean addRow = true;
            List<String> row = null;

            if (dataTypes != null) {
                dataTypes.clear();
            } else {
                dataTypes = new ArrayList<List>();
            }

            // load the column names for this rs
            String[] columnNames = null;
            if (!reloadColumns) {
                // if we're not reloading for a master, load the
                // compared to values to check column names match
                columnNames = getColumnsArray(rs.getMetaData());
            } else {
                columnNames = columns;
            }

            String underscore = "_";
            while (rs.next()) {
                addRow = true;
                row = new ArrayList<String>(columns.length);
                for (int i = 0; i < columns.length; i++) {
                    if (i > 0) {

                        if (containsColumn(columnNames, columns[i])) {
                            row.add(rs.getString(columns[i]));
                        }

                    }
                    // evaluate the column name for invalids
                    else {
                        String name = rs.getString(NAME_COLUMN);
                        if (name.startsWith(underscore)) {
                            addRow = false;
                            break;
                        }
                        row.add(name);
                    }
                }

                if (addRow) {
                    dataTypes.add(row);
                }
            }

        } catch (SQLException e) {

            handleError(e);

        }

        return dataTypes;
    }

    private boolean containsColumn(String[] columnNames, String column) {
        for (int i = 0; i < columnNames.length; i++) {
            if (column.equalsIgnoreCase(columnNames[i])) {
                return true;
            }
        }
        return false;
    }

    private void handleError(Throwable e) {
        GUIUtilities.displayExceptionErrorDialog(
                "Error retrieving data types.\nThe system returned:\n\n" +
                        e.getMessage(), e);
    }

    public void firstConnectionChanged() {
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    setInProcess(true);
                    buildFirstConnectionValues();
                } finally {
                    setInProcess(false);
                }
            }
        });
    }

    public void buildFirstConnectionValues() {

        // retrieve connection selection
        DatabaseConnection databaseConnection =
                (DatabaseConnection) connectionsCombo.getSelectedItem();
        DatabaseHost host = hostForConnection(databaseConnection);

        // set the database name
        try {

            setDatabaseFieldText(1, host.getDatabaseProductName());

        } catch (DataSourceException e) {

            setDatabaseFieldText(1, "Not Available");
        }

        ResultSet rs = null;
        try {

            rs = host.getDataTypeInfo();
            masterTypes = buildDataTypeList(masterTypes, rs, true);

        } catch (DataSourceException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving data types for selected " +
                            "connection:.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return;

        } finally {

            releaseResources(rs);
            host.close();
        }

        // sort the rows in alpha
        Collections.sort(masterTypes, sorter);

        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                masterListModel.fireContentsChanged();

                if (masterTypes.size() > 0) {
                    masterList.setSelectedIndex(0);
                    masterList.ensureIndexIsVisible(0);
                }

                if (mappedToTypes != null && mappedToTypes.size() > 0) {
                    generateMappedList();
                }
            }
        });

    }

    private DatabaseHost hostForConnection(DatabaseConnection databaseConnection) {
        DatabaseHost host = databaseObjectFactory.createDatabaseHost(databaseConnection);
        return host;
    }

    private void setDatabaseFieldText(final int connection, final String name) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                if (connection == 1) {
                    databaseField_1.setText(name);
                } else if (connection == 2) {
                    databaseField_2.setText(name);
                }
            }
        });
    }

    public void secondConnectionChanged() {
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    setInProcess(true);
                    buildSecondConnectionValues();
                } finally {
                    setInProcess(false);
                }
            }
        });
    }

    public void buildSecondConnectionValues() {

        // retrieve connection selection
        DatabaseConnection databaseConnection =
                (DatabaseConnection) connectionsCombo2.getSelectedItem();
        DatabaseHost host = hostForConnection(databaseConnection);

        try {

            setDatabaseFieldText(2, host.getDatabaseProductName());

        } catch (DataSourceException e) {

            setDatabaseFieldText(2, "Not Available");
        }

        ResultSet rs = null;
        try {

            rs = host.getDataTypeInfo();
            mappedToTypes = buildDataTypeList(mappedToTypes, rs, false);

        } catch (DataSourceException e) {

            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving data types for the selected " +
                            "connection.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return;

        } finally {

            releaseResources(rs);
            host.close();
        }


        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {

                if (mappedToTypes != null && !mappedToTypes.isEmpty()) {

                    generateMappedList();
                }
            }
        });

    }

    /**
     * dummy reset value
     */
    private Object[] dummyListData = new Object[0];

    private void generateMappedList() {

        int index = masterList.getSelectedIndex();
        if (index == -1) {
            return;
        }

        if (tableData == null) {
            tableData = new ArrayList<List>();
        } else {
            tableData.clear();
        }

        if (masterTypes.size() == 0) {
            return;
        }

        // retrieve the selected data type
        List row = masterTypes.get(index);
        String typeString = row.get(TYPE_COLUMN - 1).toString();
        int typeValue = Integer.parseInt(typeString);

        // add as the first row to the table display
        tableData.add(row);
        selectedDataType = row;

        // clear the current list
        mappedList.setListData(dummyListData);

        if (mappedToTypes == null) {
            return;
        }

        // loop through the second conns types and pick out the mapped ones
        Vector<String> mappedListV = new Vector<String>();
        for (int i = 0, n = mappedToTypes.size(); i < n; i++) {
            row = mappedToTypes.get(i);
            typeString = row.get(TYPE_COLUMN - 1).toString();

            int _typeValue = Integer.parseInt(typeString);
            if (_typeValue == typeValue) {
                mappedListV.add(row.get(NAME_COLUMN - 1).toString());
                // add the row to the table display
                tableData.add(row);
            }

        }
        mappedList.setListData(mappedListV);

        // apply the cell renderer if it hasn't been
        if (rendererApplied) {
            tableModel.fireTableDataChanged();
        } else {
            tableModel.fireTableStructureChanged();
            applyCellRenderer();
        }
    }

    private void releaseResources(ResultSet rs) {
        try {
            if (rs != null) {
                Statement st = rs.getStatement();
                releaseResources(st);
            }
        } catch (SQLException sqlExc) {
        }
    }

    private void releaseResources(Statement st) {
        try {
            if (st != null) {
                if (!st.isClosed())
                    st.close();
            }
        } catch (SQLException sqlExc) {
        }
    }

    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
        generateMappedList();
    }

    public void cleanup() {

        EventMediator.deregisterListener(this);
    }

    private void enableCombos(boolean enable) {
        connectionsCombo.setEnabled(enable);
        connectionsCombo2.setEnabled(enable);
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Indicates a connection has been established.
     *
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {
        enableCombos(true);
        connectionsModel_1.addElement(connectionEvent.getSource());
        connectionsModel_2.contentsChanged();
        //connectionsModel_2.addElement(connectionEvent.getSource());
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {
        connectionsModel_1.removeElement(connectionEvent.getSource());
        connectionsModel_2.contentsChanged();
        //connectionsModel_2.removeElement(connectionEvent.getSource());
        if (connectionsModel_1.getSize() == 0) {
            enableCombos(false);
        }
        // TODO: NEED TO CHECK OPEN CONN

    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }

    /**
     * Returns the display name for this view.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return TITLE + (count++);
    }

    // --------------------------------------------
    // TabView implementation
    // --------------------------------------------

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {
        cleanup();
        return true;
    }

    /**
     * Indicates the panel is being selected in the pane
     */
    public boolean tabViewSelected() {
        return true;
    }

    /**
     * Indicates the panel is being de-selected in the pane
     */
    public boolean tabViewDeselected() {
        return true;
    }

    // --------------------------------------------


    class DataTypeListModel extends DefaultListModel {

        public DataTypeListModel() {
        }

        public void fireContentsChanged() {
            fireContentsChanged(this, -1, -1);
        }

        public Object getElementAt(int index) {
            if (masterTypes == null) {
                return null;
            }
            return masterTypes.get(index).get(NAME_COLUMN - 1);
        }

        public int getSize() {
            if (masterTypes == null) {
                return 0;
            }
            return masterTypes.size();
        }

        public boolean isEmpty() {
            return getSize() == 0;
        }

    }


    class DataTypesTableModel extends AbstractTableModel {

        public DataTypesTableModel() {
        }

        public String getColumnName(int column) {
            if (columns == null) {
                return "";
            }
            return columns[column];
        }

        public int getColumnCount() {
            if (columns == null) {
                return 0;
            }
            return columns.length;
        }

        public int getRowCount() {
            if (tableData == null) {
                return 0;
            }
            return tableData.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= tableData.size()) {
                return null;
            }
            List row = tableData.get(rowIndex);
            return row.get(columnIndex);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

    }

    // diff cell backgrounds
    private Color masterTypeBg = new Color(153, 204, 153);
    private Color diffTypeBg = new Color(255, 255, 102);

    class DataTypeCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {

                if (row == 0) {
                    setBackground(masterTypeBg);
                } else {
                    setBackground(table.getBackground());
                    if (column > 0) {
                        Object masterValue = selectedDataType.get(column);
                        if (masterValue != null) {
                            if (!masterValue.equals(value)) {
                                setBackground(diffTypeBg);
                            }
                        }
                    }
                }
                setForeground(table.getForeground());
            }

            setValue(value);
            return this;
        }

    }


    class DataTypeRowSorter implements Comparator {

        public int compare(Object obj1, Object obj2) {
            List<String> row1 = (List<String>) obj1;
            List<String> row2 = (List<String>) obj2;
            return row1.get(NAME_COLUMN - 1).compareTo(row2.get(NAME_COLUMN - 1));
        }

    }

}












