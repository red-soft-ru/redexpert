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
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.ActionUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    public static final String FRAME_ICON = "Procedure16.svg";

    /**
     * the active connections combo
     */
    private OpenConnectionsComboboxPanel connectionsCombo;

    /**
     * the object type combo
     */
    private JComboBox<?> objectTypeCombo;

    /**
     * lists available procedures
     */
    private JComboBox<?> procedureCombo;

    /**
     * the active connections combo box model
     */
    private DynamicComboBoxModel proceduresModel;

    /**
     * the parameters table
     */
    private JTable table;

    /**
     * procedure parameters table model
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

    private final int type;
    private final String nameStoredObject;

    public ExecuteProcedurePanel() {
        this(0, null);
    }

    public ExecuteProcedurePanel(int type, String nameStoredObject) {
        super(new BorderLayout());

        this.type = type;
        this.nameStoredObject = nameStoredObject;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        tableModel = new ParameterTableModel();
        table = new DefaultTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        connectionsCombo = new OpenConnectionsComboboxPanel();
        connectionsCombo.connectionsCombo.addItemListener(e -> connectionSelectionMade());
        connectionsCombo.setEnabled(nameStoredObject == null);

        objectTypeCombo = WidgetFactory.createComboBox("objectTypeCombo", createAvailableObjectTypes());
        objectTypeCombo.setToolTipText(bundleString("ObjectType.tool-tip"));
        objectTypeCombo.addItemListener(this);

        proceduresModel = new DynamicComboBoxModel();
        procedureCombo = WidgetFactory.createComboBox("procedureCombo", proceduresModel);
        procedureCombo.setActionCommand("procedureSelectionChanged");
        procedureCombo.setToolTipText(bundleString("ObjectName.tool-tip"));
        procedureCombo.addActionListener(this);

        resultsPanel = new QueryEditorResultsPanel();

        arrangeComponents();

        if (nameStoredObject != null) {

            objectTypeCombo.setSelectedIndex(type);
            for (int i = 0; i < proceduresModel.getSize(); i++) {

                if (((NamedObject) proceduresModel.getElementAt(i)).getName().contentEquals(nameStoredObject)) {
                    procedureCombo.setSelectedIndex(i);
                    procedureCombo.setEnabled(false);
                    enableCombos(false);
                    break;
                }
            }
        }

    }

    private void arrangeComponents() {

        // --- resultPanel ---

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(resultsPanel, BorderLayout.CENTER);
        resultPanel.setMinimumSize(new Dimension(0, 250));

        // --- splitPane ---

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(new JScrollPane(table));
        splitPane.setBottomComponent(resultPanel);

        // --- basePanel ---

        GridBagHelper ghb = new GridBagHelper();
        ghb.anchorNorthWest().setInsets(5, 5, 5, 5).fillBoth();

        JPanel basePanel = new JPanel(new GridBagLayout());
        basePanel.setBorder(BorderFactory.createEtchedBorder());

        // comboBoxes
        basePanel.add(connectionsCombo, ghb.setWidth(2).setMaxWeightX().get());
        ghb.setWidth(1).setMinWeightX().addLabelFieldPair(basePanel, bundleString("ObjectType"), objectTypeCombo, null, true);
        ghb.addLabelFieldPair(basePanel, bundleString("ObjectName"), procedureCombo, null, true);

        // result splitPane
        basePanel.add(splitPane, ghb.nextRowFirstCol().spanX().get());

        // execute button
        basePanel.add(ActionUtilities.createButton(this, Bundles.getCommon("execute"), "execute"),
                ghb.nextRowFirstCol().anchorEast().fillNone().get());

        // --- main frame ---

        setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        add(basePanel, BorderLayout.CENTER);

        // ---

        EventMediator.registerListener(this);
        connectionSelectionMade();
    }

    private Vector<ExecutableObjectType> createAvailableObjectTypes() {

        Vector<ExecutableObjectType> types = new Vector<>();
        types.add(new ExecutableObjectType(NamedObject.META_TYPES[NamedObject.PROCEDURE]));
        types.add(new ExecutableObjectType(NamedObject.META_TYPES[NamedObject.FUNCTION]));

        return types;
    }

    private void enableCombos(boolean enable) {
        connectionsCombo.setEnabled(enable);
        objectTypeCombo.setEnabled(objectTypeCombo.isEnabled() ? enable : objectTypeCombo.isEnabled());
        procedureCombo.setEnabled(enable);
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    @Override
    public void itemStateChanged(ItemEvent e) {

        // interested in selections only
        if (e.getStateChange() != ItemEvent.DESELECTED)
            reloadProcedureList(e.getSource());
    }

    private void reloadProcedureList(Object source) {

        if (source == connectionsCombo)
            connectionSelectionMade();
        if (source == objectTypeCombo)
            objectTypeSelectionMade();
    }

    private void objectTypeSelectionMade() {

        ExecutableObjectType objectType = (ExecutableObjectType) objectTypeCombo.getSelectedItem();
        ConnectionsTreePanel treePanel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);

        if (objectType != null && treePanel != null) {

            DatabaseObjectNode node = treePanel.getHostNode(connectionsCombo.getSelectedConnection());
            DatabaseHost databaseHost = (DatabaseHost) node.getDatabaseObject();
            DatabaseMetaTag databaseMetaTag = getDatabaseMetaTag(objectType.name, databaseHost.getMetaObjects());

            if (databaseMetaTag != null) {
                populateProcedureValues(databaseMetaTag.getObjects());

            } else {
                proceduresModel.removeAllElements();
                procedureCombo.setEnabled(false);
            }
        }
    }

    private void connectionSelectionMade() {

        if (objectTypeCombo.getSelectedIndex() != 0)
            objectTypeCombo.setSelectedIndex(0);
        else
            objectTypeSelectionMade();

    }

    private void populateProcedureValues(final List<NamedObject> pros) {

        if (pros != null && !pros.isEmpty()) {
            proceduresModel.setElements(pros);
            procedureCombo.setSelectedIndex(0);
            procedureCombo.setEnabled(true);

        } else {
            proceduresModel.removeAllElements();
            procedureCombo.setEnabled(false);
        }

    }

    /**
     * Invoked on selection of a procedure from the combo.
     */
    @SuppressWarnings("unused")
    public void procedureSelectionChanged() {

        int index = procedureCombo.getSelectedIndex();
        DatabaseExecutable databaseExecutable = (DatabaseExecutable) proceduresModel.getElementAt(index);

        if (databaseExecutable != null) {

            if (databaseExecutable instanceof DefaultDatabaseFunction) {
                FunctionArgument[] args = new FunctionArgument[((DefaultDatabaseFunction) databaseExecutable).getFunctionArguments().size()];
                ((DefaultDatabaseFunction) databaseExecutable).getFunctionArguments().toArray(args);
                tableModel.setValues(args);

            } else
                tableModel.setValues(databaseExecutable.getParametersArray());

        } else
            tableModel.clear();

        tableModel.fireTableDataChanged();
    }

    /**
     * Executes the selected procedure.
     */
    public void execute() {

        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();

        if (selectedRow != -1 && selectedColumn != -1 && table.isEditing())
            table.getCellEditor(selectedRow, selectedColumn).stopCellEditing();

        GUIUtils.startWorker(() -> {

            try {
                setInProcess(true);

                DatabaseConnection databaseConnection = connectionsCombo.getSelectedConnection();
                if (databaseConnection == null) {
                    GUIUtilities.displayErrorMessage("No database connection is available.");
                    return;
                }

                Object object = procedureCombo.getSelectedItem();
                if (object == null)
                    return;

                DatabaseExecutable databaseExecutable = (DatabaseExecutable) object;
                int type = objectTypeCombo.getSelectedIndex();
                String text = type == 1 ? " function " : " procedure ";
                setActionMessage("Executing" + text + databaseExecutable.getName() + "...");

                if (statementExecutor == null)
                    statementExecutor = new DefaultStatementExecutor(databaseConnection);
                else
                    statementExecutor.setDatabaseConnection(databaseConnection);

                int queryType;
                StringBuilder sql;

                if (type == 1) {
                    queryType = QueryTypes.SELECT;

                    DefaultDatabaseFunction function = (DefaultDatabaseFunction) databaseExecutable;
                    sql = new StringBuilder("SELECT " + function.getNameForQuery() + " (");

                    boolean first = true;
                    for (int i = 0; i < function.getFunctionArguments().size(); i++) {
                        if (!function.getFunctionArguments().get(i).getName().contentEquals("< Return Value >")) {
                            sql.append(!first ? "," : "").append(":").append(function.getFunctionArguments().get(i).getName());
                            first = false;
                        }
                    }
                    sql.append(") AS \"< Return Value >\" FROM rdb$database");

                } else {
                    queryType = QueryTypes.EXECUTE;

                    DefaultDatabaseProcedure procedure = (DefaultDatabaseProcedure) databaseExecutable;
                    sql = new StringBuilder("EXECUTE PROCEDURE " + procedure.getNameForQuery());

                    if (procedure.getProcedureInputParameters().size() > 0) {
                        sql.append("(");

                        boolean first = true;
                        for (int i = 0; i < procedure.getProcedureInputParameters().size(); i++) {
                            sql.append(!first ? "," : "").append(":").append(procedure.getProcedureInputParameters().get(i).getName());
                            first = false;
                        }

                        sql.append(")");
                    }
                }

                setActionMessage("Executing: " + sql);
                PreparedStatement statement = prepareStatementWithParameters(sql.toString(), "");
                SqlStatementResult result = statementExecutor.execute(queryType, statement);
                Map results = (Map) result.getOtherResult();

                if (!result.isException()) {

                    setPlainMessage("Statement executed successfully.");
                    int updateCount = result.getUpdateCount();

                    if (updateCount > 0)
                        setPlainMessage(updateCount + updateCount > 1 ? " rows affected." : " row affected.");

                    if (results != null)
                        for (Object key : results.keySet())
                            setPlainMessage(key.toString() + " = " + results.get(key.toString()));

                    if (result.isResultSet())
                        resultsPanel.setResultSet(result.getResultSet(), false, -1);

                } else
                    setErrorMessage(result.getErrorMessage());


            } catch (Exception e) {
                if (!e.getMessage().contentEquals("Canceled"))
                    e.printStackTrace();

            } finally {
                statementExecutor.releaseResources();
                setInProcess(false);
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

            List<org.executequery.gui.editor.autocomplete.Parameter> oldParams =
                    QueryEditorHistory.getHistoryParameters().get(statementExecutor.getDatabaseConnection());

            for (Parameter dp : displayParams) {
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
            if (spd.isCanceled())
                throw new DataSourceException("Canceled");
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
        GUIUtils.invokeAndWait(() -> resultsPanel.setActionMessage(message));
    }

    private void setPlainMessage(final String message) {
        GUIUtils.invokeAndWait(() -> resultsPanel.setPlainMessage(message));
    }

    private void setErrorMessage(final String message) {
        GUIUtils.invokeAndWait(() -> resultsPanel.setErrorMessage(message));
    }

    // ---------------------------------------------
    // ConnectionListener implementation
    // ---------------------------------------------

    /**
     * Indicates a connection has been established.
     *
     * @param connectionEvent encapsulating event
     */
    @Override
    public void connected(ConnectionEvent connectionEvent) {
        enableCombos(true);
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param connectionEvent encapsulating event
     */
    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent);
    }

    /**
     * Returns the display name for this view.
     *
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return TITLE + (count++);
    }

    /**
     * Indicates the panel is being removed from the pane
     */
    @Override
    public boolean tabViewClosing() {

        EventMediator.deregisterListener(this);

        if (statementExecutor != null) {
            try {
                statementExecutor.destroyConnection();

            } catch (SQLException ignored) {
            }
        }

        return true;
    }

    @Override
    public void itemStateChanging(ItemEvent e) {
    }

    public DatabaseMetaTag getDatabaseMetaTag(String name, List<DatabaseMetaTag> metaObjects) {

        name = name.toUpperCase();
        for (DatabaseMetaTag object : metaObjects)
            if (name.equals(object.getMetaDataKey().toUpperCase()))
                return object;

        return null;
    }

    static class ExecutableObjectType {

        String name;

        ExecutableObjectType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return StringUtils.capitalize(name.toLowerCase());
        }

    } // class ExecutableObjectType

    static class ParameterTableModel extends AbstractTableModel {

        private final String UNKNOWN = "UNKNOWN";
        private final String RETURN = "RETURN";
        private final String RESULT = "RESULT";
        private final String IN = "IN";
        private final String INOUT = "INOUT";
        private final String OUT = "OUT";

        private final String[] columns = Bundles.get(ExecuteProcedurePanel.class,
                new String[]{"Parameter", "DataType", "Mode"});
        private org.executequery.databaseobjects.Parameter[] values;

        @Override
        public int getRowCount() {
            return (values != null) ? values.length : 0;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        public void clear() {
            values = null;
        }

        public void setValues(ProcedureParameter[] procedureParameters) {
            values = procedureParameters;
        }

        public void setValues(FunctionArgument[] functionArguments) {
            values = functionArguments;
        }

        @Override
        public Object getValueAt(int row, int col) {

            if (values == null)
                return "";

            org.executequery.databaseobjects.Parameter param = values[row];
            switch (col) {

                case 0:
                    return param.getName();

                case 1:
                    return (param.getSize() > 0) ?
                            param.getSqlType() + "(" + param.getSize() + ")" :
                            param.getSqlType();

                case 2:
                    switch (param.getType()) {

                        case DatabaseMetaData.procedureColumnIn:
                            return IN;
                        case DatabaseMetaData.procedureColumnOut:
                            return OUT;
                        case DatabaseMetaData.procedureColumnInOut:
                            return INOUT;
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

        @Override
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
                    if (value == IN)
                        param.setType(DatabaseMetaData.procedureColumnIn);
                    else if (value == OUT)
                        param.setType(DatabaseMetaData.procedureColumnOut);
                    else if (value == INOUT)
                        param.setType(DatabaseMetaData.procedureColumnInOut);
                    else if (value == UNKNOWN)
                        param.setType(DatabaseMetaData.procedureColumnUnknown);
                    else if (value == RESULT)
                        param.setType(DatabaseMetaData.procedureColumnResult);
                    else if (value == RETURN)
                        param.setType(DatabaseMetaData.procedureColumnReturn);
                    break;

                case 3:
                    param.setValue((String) value);
                    break;
            }

            fireTableCellUpdated(row, col);
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

    } // class ParameterTableModel

}
