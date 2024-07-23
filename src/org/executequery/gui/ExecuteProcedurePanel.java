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

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.*;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.editor.InputParametersDialog;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.editor.QueryEditorResultsPanel;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.sqlParser.SqlParser;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Takis Diakoumis
 */
public class ExecuteProcedurePanel extends JPanel {

    private JTable table;
    private JButton executeButton;
    private ParameterTableModel tableModel;
    private QueryEditorResultsPanel resultsPanel;

    private StatementExecutor executor;
    private DatabaseExecutable executableObject;

    private final String objectType;
    private final String objectName;
    private final DatabaseConnection connection;

    public ExecuteProcedurePanel(String objectType, String objectName, DatabaseConnection connection) {
        super(new BorderLayout());

        this.objectType = objectType;
        this.objectName = objectName;
        this.connection = connection;

        init();
        arrange();
    }

    private void init() {

        tableModel = new ParameterTableModel();
        table = new DefaultTable(tableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        resultsPanel = new QueryEditorResultsPanel();
        resultsPanel.setVisible(false);

        executeButton = WidgetFactory.createButton(
                "executeButton",
                Bundles.getCommon("execute"),
                e -> execute()
        );

        if (objectName != null)
            prepareExecute();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- basePanel ---

        JPanel basePanel = new JPanel(new GridBagLayout());
        basePanel.setBorder(BorderFactory.createEtchedBorder());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillBoth();
        basePanel.add(new JScrollPane(table), gbh.setMaxWeightY().spanX().get());
        basePanel.add(resultsPanel, gbh.nextRowFirstCol().get());
        basePanel.add(executeButton, gbh.nextRowFirstCol().setMinWeightY().anchorEast().bottomGap(0).fillNone().get());

        // --- main frame ---

        setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));
        add(basePanel, BorderLayout.CENTER);
    }

    private void prepareExecute() {

        JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabComponent instanceof ConnectionsTreePanel) {

            ConnectionsTreePanel treePanel = (ConnectionsTreePanel) tabComponent;
            DatabaseObjectNode node = treePanel.getHostNode(connection);
            DatabaseHost databaseHost = (DatabaseHost) node.getDatabaseObject();
            DatabaseMetaTag databaseMetaTag = getDatabaseMetaTag(objectType, databaseHost.getMetaObjects());

            for (NamedObject namedObject : databaseMetaTag.getObjects()) {
                if (Objects.equals(namedObject.getName(), objectName)) {

                    executableObject = (DatabaseExecutable) namedObject;
                    if (executableObject instanceof DefaultDatabaseFunction) {
                        DefaultDatabaseFunction function = (DefaultDatabaseFunction) executableObject;
                        tableModel.setValues(function.getFunctionArguments().toArray(new FunctionArgument[0]));

                    } else
                        tableModel.setValues(executableObject.getParametersArray());

                    tableModel.fireTableDataChanged();
                    break;
                }
            }
        }
    }

    private void execute() {

        if (connection == null) {
            GUIUtilities.displayErrorMessage("No database connection is available.");
            return;
        }

        int selectedRow = table.getSelectedRow();
        int selectedColumn = table.getSelectedColumn();

        if (selectedRow != -1 && selectedColumn != -1 && table.isEditing())
            table.getCellEditor(selectedRow, selectedColumn).stopCellEditing();

        GUIUtils.startWorker(() -> {
            setActionMessage("Executing " + objectType.toLowerCase() + " " + executableObject.getName() + "...");

            if (executor == null)
                executor = new DefaultStatementExecutor(connection);
            else
                executor.setDatabaseConnection(connection);

            int queryType;
            StringBuilder sql = new StringBuilder();

            if (Objects.equals(objectType, NamedObject.META_TYPES[NamedObject.FUNCTION])) {
                queryType = QueryTypes.SELECT;

                DefaultDatabaseFunction function = (DefaultDatabaseFunction) executableObject;
                sql.append("SELECT ").append(function.getNameForQuery()).append(" (");

                boolean first = true;
                for (int i = 0; i < function.getFunctionArguments().size(); i++) {
                    if (!function.getFunctionArguments().get(i).getName().contentEquals("< Return Value >")) {
                        sql.append(!first ? "," : "").append(":").append(function.getFunctionArguments().get(i).getName());
                        first = false;
                    }
                }
                sql.append(") AS \"< Return Value >\" FROM rdb$database");

            } else {
                DefaultDatabaseProcedure procedure = (DefaultDatabaseProcedure) executableObject;
                if (procedure.getProcedureType() == DefaultDatabaseProcedure.SELECTABLE) {
                    queryType = QueryTypes.SELECT;
                    sql.append("SELECT * FROM ").append(procedure.getNameForQuery());

                } else {
                    queryType = QueryTypes.EXECUTE;
                    sql.append("EXECUTE PROCEDURE ").append(procedure.getNameForQuery());
                }

                if (!procedure.getProcedureInputParameters().isEmpty()) {
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
            try {

                SqlStatementResult result = executor.execute(queryType, prepareStatement(sql.toString()));
                if (result.isException()) {
                    setErrorMessage(result.getErrorMessage());
                    return;
                }

                setPlainMessage("Statement executed successfully.");
                int updateCount = result.getUpdateCount();
                if (updateCount > 0)
                    setPlainMessage(updateCount + updateCount > 1 ? " rows affected." : " row affected.");

                if (result.getOtherResult() instanceof Map<?, ?>) {
                    Map<?, ?> results = (Map<?, ?>) result.getOtherResult();
                    for (Object key : results.keySet())
                        setPlainMessage(key.toString() + " = " + results.get(key.toString()));
                }

                if (result.isResultSet())
                    resultsPanel.setResultSet(result.getResultSet(), false, -1, null);

            } catch (Exception e) {
                if (!e.getMessage().contentEquals("Canceled"))
                    Log.error(e.getMessage(), e);

            } finally {
                executor.releaseResources();
                resultsPanel.setVisible(true);
            }
        });
    }

    private PreparedStatement prepareStatement(String sql) throws SQLException {

        SqlParser parser = new SqlParser(sql, "");
        PreparedStatement statement = executor.getPreparedStatement(parser.getProcessedSql());
        statement.setEscapeProcessing(true);

        List<Parameter> params = parser.getParameters();
        List<Parameter> displayParams = parser.getDisplayParameters();

        for (int i = 0; i < params.size(); i++) {
            params.get(i).setType(statement.getParameterMetaData().getParameterType(i + 1));
            params.get(i).setTypeName(statement.getParameterMetaData().getParameterTypeName(i + 1));
        }

        if (QueryEditorHistory.getHistoryParameters().containsKey(executor.getDatabaseConnection())) {
            List<Parameter> oldParams = QueryEditorHistory.getHistoryParameters().get(executor.getDatabaseConnection());

            for (Parameter parameter : displayParams) {
                for (int i = 0; i < oldParams.size(); i++) {
                    Parameter oldParameter = oldParams.get(i);

                    if (oldParameter.getType() == parameter.getType() && oldParameter.getName().contentEquals(parameter.getName())) {
                        parameter.setValue(oldParameter.getValue());
                        oldParams.remove(oldParameter);
                        break;
                    }
                }
            }
        }

        if (!displayParams.isEmpty()) {
            InputParametersDialog inputParametersDialog = new InputParametersDialog(displayParams);
            inputParametersDialog.display();
            if (inputParametersDialog.isCanceled())
                throw new DataSourceException("Canceled");
        }

        for (int i = 0; i < params.size(); i++) {
            if (params.get(i).isNull())
                statement.setNull(i + 1, params.get(i).getType());
            else
                statement.setObject(i + 1, params.get(i).getPreparedValue());
        }

        QueryEditorHistory.getHistoryParameters().put(executor.getDatabaseConnection(), displayParams);
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

    public DatabaseMetaTag getDatabaseMetaTag(String name, List<DatabaseMetaTag> metaObjects) {

        for (DatabaseMetaTag object : metaObjects)
            if (Objects.equals(name.toUpperCase(), object.getMetaDataKey().toUpperCase()))
                return object;

        return null;
    }

    static class ParameterTableModel extends AbstractTableModel {

        private final String UNKNOWN = "UNKNOWN";
        private final String RETURN = "RETURN";
        private final String RESULT = "RESULT";
        private final String INOUT = "INOUT";
        private final String OUT = "OUT";
        private final String IN = "IN";

        private org.executequery.databaseobjects.Parameter[] values;
        private final String[] columns = Bundles.get(ExecuteProcedurePanel.class, new String[]{
                "Parameter",
                "DataType",
                "Mode"
        });

        public void setValues(ProcedureParameter[] procedureParameters) {
            values = procedureParameters;
        }

        public void setValues(FunctionArgument[] functionArguments) {
            values = functionArguments;
        }

        public void clear() {
            values = null;
        }

        @Override
        public int getRowCount() {
            return (values != null) ? values.length : 0;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
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
                    return param.getSize() > 0 ?
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
