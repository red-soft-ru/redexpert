/*
 * ExecuteProcedurePanel.java
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

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.ItemSelectionListener;
import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.components.OpenConnectionsComboboxPanel;
import org.executequery.gui.editor.InputParametersDialog;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.editor.QueryEditorResultsPanel;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.ActionUtilities;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Takis Diakoumis
 */
public class ExecuteProcedurePanel extends DefaultTabViewActionPanel
        implements NamedView,
        ItemListener,
        ItemSelectionListener,
        ConnectionListener {

    public static final String TITLE = "Execute Stored Objects ";
    public static final String FRAME_ICON = "Procedure16.png";

    /**
     * the active connections combo
     */
    private OpenConnectionsComboboxPanel connectionsCombo;

    /**
     * lists available schemas
     */
    //private JComboBox schemaCombo;

    /**
     * the object type combo
     */
    private JComboBox objectTypeCombo;

    /**
     * lists available procedures
     */
    private JComboBox procedureCombo;

    /**
     * the active connections combo box model
     */
    private DynamicComboBoxModel proceduresModel;

    /**
     * the parameters table
     */
    private JTable table;

    /**
     * proc parameters table model
     */
    private ParameterTableModel tableModel;

    /**
     * the results panel
     */
    private QueryEditorResultsPanel resultsPanel;

    /**
     * execution utility
     */
    private StatementExecutor statementExecutor;



    /**
     * the instance count
     */
    private static int count = 1;

    public ExecuteProcedurePanel() {

        super(new BorderLayout());

        try {

            init();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void init() throws Exception {

        tableModel = new ParameterTableModel();
        table = new DefaultTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        connectionsCombo = new OpenConnectionsComboboxPanel();


        connectionsCombo.connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                connectionSelectionMade();
            }
        });

        objectTypeCombo = WidgetFactory.createComboBox(createAvailableObjectTypes());
        objectTypeCombo.setToolTipText(bundleString("ObjectType.tool-tip"));
        objectTypeCombo.addItemListener(this);

        proceduresModel = new DynamicComboBoxModel();
        procedureCombo = WidgetFactory.createComboBox(proceduresModel);
        procedureCombo.setActionCommand("procedureSelectionChanged");
        procedureCombo.setToolTipText(bundleString("ObjectName.tool-tip"));
        procedureCombo.addActionListener(this);

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        base.add(connectionsCombo, gbc);
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.left = 7;
        gbc.insets.top = 0;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy++;
        gbc.insets.left = 7;
        gbc.insets.top = 0;
        base.add(new JLabel(bundleString("ObjectType")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        base.add(objectTypeCombo, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy++;
        gbc.insets.left = 7;
        base.add(new JLabel(bundleString("ObjectName")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        base.add(procedureCombo, gbc);

        resultsPanel = new QueryEditorResultsPanel();
        JPanel resultsBase = new JPanel(new BorderLayout());
        resultsBase.add(resultsPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new SplitPaneFactory().create(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), resultsBase);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.75);
        splitPane.setDividerSize(5);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weighty = 1.0;
        gbc.insets.left = 7;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        base.add(splitPane, gbc);

        JButton executeButton = ActionUtilities.createButton(this, bundleString("Execute"), "execute");

        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.top = 0;
        gbc.insets.bottom = 20;
        base.add(executeButton, gbc);

        base.setBorder(BorderFactory.createEtchedBorder());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));

        add(base, BorderLayout.CENTER);

        EventMediator.registerListener(this);

        connectionSelectionMade();
    }

    private Vector<ExecutableObjectType> createAvailableObjectTypes() {

        Vector<ExecutableObjectType> types = new Vector<ExecutableObjectType>();

        String type = NamedObject.META_TYPES[NamedObject.PROCEDURE];
        types.add(new ExecutableObjectType(type));

        type = NamedObject.META_TYPES[NamedObject.FUNCTION];
        types.add(new ExecutableObjectType(type));

        return types;
    }

    private void enableCombos(boolean enable) {

        connectionsCombo.setEnabled(enable);

        if (objectTypeCombo.isEnabled()) {
            objectTypeCombo.setEnabled(enable);
        }

        procedureCombo.setEnabled(enable);
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent e) {

        // interested in selections only
        if (e.getStateChange() == ItemEvent.DESELECTED) {

            return;
        }

        final Object source = e.getSource();

        GUIUtils.startWorker(new Runnable() {
            public void run() {

                try {

                    setInProcess(true);
                    reloadProcedureList(source);

                } finally {

                    setInProcess(false);
                }

            }

        });

    }

    private void reloadProcedureList(Object source) {

        if (source == connectionsCombo) {

            connectionSelectionMade();

        }

        if (source == objectTypeCombo) {

            objectTypeSelectionMade();
        }

    }

    private void objectTypeSelectionMade() {



        ExecutableObjectType objectType = (ExecutableObjectType) objectTypeCombo.getSelectedItem();
        ConnectionsTreePanel treePanel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        DatabaseObjectNode node = treePanel.getHostNode(connectionsCombo.getSelectedConnection());
        DatabaseHost databaseHost = (DatabaseHost) node.getDatabaseObject();
        DatabaseMetaTag databaseMetaTag = getDatabaseMetaTag(objectType.name, databaseHost.getMetaObjects());

        if (databaseMetaTag != null) {

            populateProcedureValues(databaseMetaTag.getObjects());

        } else {

            GUIUtils.invokeAndWait(new Runnable() {
                public void run() {
                    proceduresModel.removeAllElements();
                    procedureCombo.setEnabled(false);
                }
            });

        }

    }

    private void schemaSelectionMade() {

        int index = objectTypeCombo.getSelectedIndex();

        if (index != 0) {

            objectTypeCombo.setSelectedIndex(0);

        } else {

            objectTypeSelectionMade();
        }
    }

    private void connectionSelectionMade() {

        int index = objectTypeCombo.getSelectedIndex();

        if (index != 0) {

            objectTypeCombo.setSelectedIndex(0);

        } else {

            objectTypeSelectionMade();
        }
    }

    private void populateProcedureValues(final List<NamedObject> procs) {

        GUIUtils.invokeAndWait(new Runnable() {

            public void run() {

                if (procs != null && !procs.isEmpty()) {

                    proceduresModel.setElements(procs);
                    procedureCombo.setSelectedIndex(0);
                    procedureCombo.setEnabled(true);

                } else {

                    proceduresModel.removeAllElements();
                    procedureCombo.setEnabled(false);
                }

            }

        });

    }

    /**
     * Invoked on selection of a procedure from the combo.
     */
    public void procedureSelectionChanged() {

        int index = procedureCombo.getSelectedIndex();

        DatabaseExecutable databaseExecutable =
                (DatabaseExecutable) proceduresModel.getElementAt(index);

        if (databaseExecutable != null) {
            if (databaseExecutable instanceof DefaultDatabaseFunction) {
                FunctionArgument[] args = new FunctionArgument[((DefaultDatabaseFunction) databaseExecutable).getFunctionArguments().size()];
                ((DefaultDatabaseFunction) databaseExecutable).getFunctionArguments().toArray(args);
                tableModel.setValues(args);
            } else
            tableModel.setValues(databaseExecutable.getParametersArray());

        } else {

            tableModel.clear();
        }

        tableModel.fireTableDataChanged();
    }

    /**
     * Executes the selected procedure.
     */
    public void execute() {
        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();

        if (selectedRow != -1 && selectedColumn != -1) {
            if (table.isEditing()) {
                table.getCellEditor(
                        selectedRow, selectedColumn).stopCellEditing();
            }
        }

        GUIUtils.startWorker(new Runnable() {
            @SuppressWarnings("unchecked")
            public void run() {
                try {
                    setInProcess(true);

                    DatabaseConnection databaseConnection = connectionsCombo.getSelectedConnection();

                    if (databaseConnection == null) {

                        GUIUtilities.displayErrorMessage(
                                "No database connection is available.");
                        return;
                    }

                    Object object = procedureCombo.getSelectedItem();
                    if (object == null) {

                        return;
                    }

                    DatabaseExecutable databaseExecutable = (DatabaseExecutable) object;

                    int type = objectTypeCombo.getSelectedIndex();
                    String text = type == 1 ? " function " : " procedure ";
                    setActionMessage("Executing" + text + databaseExecutable.getName() + "...");


                    if (statementExecutor == null) {

                        statementExecutor = new DefaultStatementExecutor(databaseConnection);

                    } else {

                        statementExecutor.setDatabaseConnection(databaseConnection);
                    }
                    String sql;
                    int queryType = 0;
                    if (type == 1) {
                        queryType = QueryTypes.SELECT;
                        DefaultDatabaseFunction function = (DefaultDatabaseFunction) databaseExecutable;
                        sql = "select " + function.getNameForQuery() + " (";
                        boolean first = true;
                        for (int i = 0; i < function.getFunctionArguments().size(); i++) {
                            if (!function.getFunctionArguments().get(i).getName().contentEquals("< Return Value >")) {
                                if (first) {
                                    first = false;
                                } else sql += ",";
                                sql += ":" + function.getFunctionArguments().get(i).getName();
                            }
                        }
                        sql = sql + ") as \"< Return Value >\" from rdb$database";
                    } else {
                        queryType = QueryTypes.EXECUTE;
                        DefaultDatabaseProcedure procedure = (DefaultDatabaseProcedure) databaseExecutable;
                        sql = "execute procedure " + procedure.getNameForQuery() + "(";
                        boolean first = true;
                        for (int i = 0; i < procedure.getProcedureInputParameters().size(); i++) {
                            if (first) {
                                first = false;
                            } else sql += ",";
                            sql += ":" + procedure.getProcedureInputParameters().get(i).getName();
                        }
                        sql = sql + ")";
                    }
                    setActionMessage("Executing: " + sql);
                    PreparedStatement statement = prepareStatementWithParameters(sql, "");
                    SqlStatementResult result = statementExecutor.execute(queryType, statement);
                    Map results = (Map) result.getOtherResult();

                    if (result.isException()) {

                        setErrorMessage(result.getErrorMessage());

                    } else {

                        setPlainMessage("Statement executed successfully.");
                        int updateCount = result.getUpdateCount();

                        if (updateCount > 0) {

                            setPlainMessage(updateCount +
                                    updateCount > 1 ? " rows affected." : " row affected.");
                        }

                        String SPACE = " = ";
                        if (results != null)
                        for (Iterator<?> i = results.keySet().iterator(); i.hasNext(); ) {

                            String key = i.next().toString();

                            setPlainMessage(key + SPACE + results.get(key));
                        }

                        if (result.isResultSet()) {

                            resultsPanel.setResultSet(result.getResultSet(), false, -1);
                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace();

                } finally {
                    statementExecutor.releaseResources();
                    setInProcess(false);
                }

            }
        });

    }

    PreparedStatement prepareStatementWithParameters(String sql, String variables) throws SQLException {
        SqlParser parser = new SqlParser(sql, variables);
        String queryToExecute = parser.getProcessedSql();
        PreparedStatement statement = statementExecutor.getPreparedStatement(queryToExecute);
        statement.setEscapeProcessing(true);
        ParameterMetaData pmd = statement.getParameterMetaData();
        List<org.executequery.gui.editor.autocomplete.Parameter> params = parser.getParameters();
        List<org.executequery.gui.editor.autocomplete.Parameter> displayParams = parser.getDisplayParameters();
        for (int i = 0; i < params.size(); i++) {
            params.get(i).setType(pmd.getParameterType(i + 1));
            params.get(i).setTypeName(pmd.getParameterTypeName(i + 1));
        }
        if (QueryEditorHistory.getHistoryParameters().containsKey(statementExecutor.getDatabaseConnection())) {
            List<org.executequery.gui.editor.autocomplete.Parameter> oldParams = QueryEditorHistory.getHistoryParameters().get(statementExecutor.getDatabaseConnection());
            for (int i = 0; i < displayParams.size(); i++) {
                org.executequery.gui.editor.autocomplete.Parameter dp = displayParams.get(i);
                for (int g = 0; g < oldParams.size(); g++) {
                    Parameter p = oldParams.get(g);
                    if (p.getType() == dp.getType() && p.getName().contentEquals(dp.getName())) {
                        dp.setValue(p.getValue());
                        oldParams.remove(p);
                        break;
                    }
                }
            }
        }
        if (!displayParams.isEmpty()) {
            InputParametersDialog spd = new InputParametersDialog(displayParams);
            spd.display();
        }
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).isNull())
                statement.setNull(i + 1, params.get(i).getType());
            else
                statement.setObject(i + 1, params.get(i).getPreparedValue());
        }
        QueryEditorHistory.getHistoryParameters().put(statementExecutor.getDatabaseConnection(), displayParams);
        return statement;
    }

    private void setActionMessage(final String message) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                resultsPanel.setActionMessage(message);
            }
        });
    }

    private void setPlainMessage(final String message) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                resultsPanel.setPlainMessage(message);
            }
        });
    }

    private void setErrorMessage(final String message) {
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                resultsPanel.setErrorMessage(message);
            }
        });
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        enableCombos(true);

    }

    /**
     * Indicates a connection has been closed.
     *
     * @param connectionEvent encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {

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

    /**
     * Indicates the panel is being removed from the pane
     */
    public boolean tabViewClosing() {

        EventMediator.deregisterListener(this);

        if (statementExecutor != null) {

            try {

                statementExecutor.destroyConnection();

            } catch (SQLException e) {
            }

        }

        return true;
    }

    public void itemStateChanging(ItemEvent e) {
    }

    public DatabaseMetaTag getDatabaseMetaTag(String name, List<DatabaseMetaTag> metaObjects) {

        name = name.toUpperCase();
        for (DatabaseMetaTag object : metaObjects) {

            if (name.equals(object.getMetaDataKey().toUpperCase())) {

                return object;
            }

        }

        return null;
    }

    private String[] bundleStrings(String[] keys) {
        return Bundles.get(this.getClass(), keys);
    }


    class ExecutableObjectType {

        String name;

        ExecutableObjectType(String name) {

            this.name = name;
        }

        public String toString() {

            return StringUtils.capitalize(name.toLowerCase());
        }

    } // ExecutableObjectType

    class ParameterTableModel extends AbstractTableModel {

        private String UNKNOWN = "UNKNOWN";
        private String RETURN = "RETURN";
        private String RESULT = "RESULT";
        private String IN = "IN";
        private String INOUT = "INOUT";
        private String OUT = "OUT";

        private String[] columns = bundleStrings(new String[]{"Parameter", "DataType", "Mode"});
        private org.executequery.databaseobjects.Parameter[] values;

        public ParameterTableModel() {
        }

        public ParameterTableModel(ProcedureParameter[] _procParams) {
            values = _procParams;
        }

        public int getRowCount() {
            if (values == null) {
                return 0;
            }
            return values.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public void clear() {
            values = null;
        }

        public void setValues(ProcedureParameter[] _procParams) {
            values = _procParams;
        }

        public void setValues(FunctionArgument[] _funcArgs) {
            values = _funcArgs;
        }

        public Object getValueAt(int row, int col) {
            if (values == null) {
                return "";
            }

            org.executequery.databaseobjects.Parameter param = values[row];

            switch (col) {

                case 0:
                    return param.getName();

                case 1:

                    if (param.getSize() > 0)
                        return param.getSqlType() + "(" + param.getSize() + ")";
                    else
                        return param.getSqlType();

                case 2:
                    int mode = param.getType();

                    switch (mode) {
                        case DatabaseMetaData.procedureColumnIn:
                            return IN;
                        case DatabaseMetaData.procedureColumnOut:
                            return OUT;
                        case DatabaseMetaData.procedureColumnInOut:
                            return INOUT;
                        case DatabaseMetaData.procedureColumnUnknown:
                            return UNKNOWN;
                        case DatabaseMetaData.procedureColumnResult:
                            return RESULT;
                        case DatabaseMetaData.procedureColumnReturn:
                            return RETURN;
                        default:
                            return UNKNOWN;
                    }

                case 3:
                    String value = param.getValue();
                    return value == null ? Constants.EMPTY : value;

                default:
                    return UNKNOWN;

            }

        }

        public void setValueAt(Object value, int row, int col) {
            org.executequery.databaseobjects.Parameter param = values[row];

            switch (col) {

                case 0:
                    param.setName((String) value);
                    break;

                case 1:
                    param.setSqlType((String) value);
                    break;

                case 2:
                    if (value == IN) {
                        param.setType(DatabaseMetaData.procedureColumnIn);
                    } else if (value == OUT) {
                        param.setType(DatabaseMetaData.procedureColumnOut);
                    } else if (value == INOUT) {
                        param.setType(DatabaseMetaData.procedureColumnInOut);
                    } else if (value == UNKNOWN) {
                        param.setType(DatabaseMetaData.procedureColumnUnknown);
                    } else if (value == RESULT) {
                        param.setType(DatabaseMetaData.procedureColumnResult);
                    } else if (value == RETURN) {
                        param.setType(DatabaseMetaData.procedureColumnReturn);
                    }
                    break;
                case 3:
                    param.setValue((String) value);
                    break;

            }

            fireTableCellUpdated(row, col);

        }

        public String getColumnName(int col) {
            return columns[col];
        }

        public boolean isCellEditable(int row, int col) {


            return false;



        }

    } // class ParameterTableModel

}






