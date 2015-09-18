/*
 * ExecuteProcedurePanel.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.executequery.Constants;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.DefaultTabViewActionPanel;
import org.executequery.components.ItemSelectionListener;
import org.executequery.components.SplitPaneFactory;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseMetaTag;
import org.executequery.databaseobjects.DatabaseSource;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.editor.QueryEditorResultsPanel;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.actions.ActionUtilities;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ExecuteProcedurePanel extends DefaultTabViewActionPanel
                                   implements NamedView,
                                              ItemListener,
                                              ItemSelectionListener,
                                              ConnectionListener {
    
    public static final String TITLE = "Execute Stored Objects ";
    public static final String FRAME_ICON = "Procedure16.png";
    
    /** the active connections combo */
    private JComboBox connectionsCombo;

    /** lists available schemas */
    private JComboBox schemaCombo;
    
    /** the object type combo */
    private JComboBox objectTypeCombo;
    
    /** lists available procedures */
    private JComboBox procedureCombo;
    
    /** the active connections combo box model */
    private DynamicComboBoxModel proceduresModel;

    /** the parameters table */
    private JTable table;
    
    /** proc parameters table model */
    private ParameterTableModel tableModel;
    
    /** the results panel */
    private QueryEditorResultsPanel resultsPanel;
    
    /** execution utility */
    private StatementExecutor statementExecutor;
    
    private TableSelectionCombosGroup combosGroup;
    
    /** the instance count */
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
        
        connectionsCombo = WidgetFactory.createComboBox();
        schemaCombo = WidgetFactory.createComboBox();

        combosGroup = new TableSelectionCombosGroup(connectionsCombo, schemaCombo, null);
        combosGroup.addItemSelectionListener(this);
        
        objectTypeCombo = WidgetFactory.createComboBox(createAvailableObjectTypes());
        objectTypeCombo.setToolTipText("Select the database object type");
        objectTypeCombo.addItemListener(this);
        
        proceduresModel = new DynamicComboBoxModel();
        procedureCombo = WidgetFactory.createComboBox(proceduresModel);
        procedureCombo.setActionCommand("procedureSelectionChanged");
        procedureCombo.setToolTipText("Select the database object name");
        procedureCombo.addActionListener(this);

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,7,5,8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy++;
        base.add(new JLabel("Connection:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;        
        base.add(connectionsCombo, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy++;
        gbc.insets.left = 7;
        gbc.insets.top = 0;
        base.add(new JLabel("Schema:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        base.add(schemaCombo, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy++;
        gbc.insets.left = 7;
        gbc.insets.top = 0;
        base.add(new JLabel("Object Type:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.left = 0;
        base.add(objectTypeCombo, gbc);
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridy++;
        gbc.insets.left = 7;
        base.add(new JLabel("Object Name:"), gbc);
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
        
        JButton executeButton = ActionUtilities.createButton(this, "Execute", "execute");

        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.top = 0;
        gbc.insets.bottom = 20;
        base.add(executeButton, gbc);
        
        base.setBorder(BorderFactory.createEtchedBorder());
        setBorder(BorderFactory.createEmptyBorder(5,5,7,5));
        
        add(base, BorderLayout.CENTER);
        
        EventMediator.registerListener(this);
        
        connectionSelectionMade();
    }

    private Vector<ExecutableObjectType> createAvailableObjectTypes() {
        
        Vector<ExecutableObjectType> types = new Vector<ExecutableObjectType>();

        String type = NamedObject.META_TYPES[NamedObject.FUNCTION];
        types.add(new ExecutableObjectType(type));
        
        type = NamedObject.META_TYPES[NamedObject.PROCEDURE];
        types.add(new ExecutableObjectType(type));
        
        return types;
    }

    private void enableCombos(boolean enable) {

        schemaCombo.setEnabled(enable);
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
            
        } else if (source == schemaCombo) {
            
            schemaSelectionMade();
        }
        
        if (source == objectTypeCombo) {
            
            objectTypeSelectionMade();
        }
        
    }

    private void objectTypeSelectionMade() {

        DatabaseSource databaseSource = combosGroup.getSelectedSource();
        
        ExecutableObjectType objectType = (ExecutableObjectType) objectTypeCombo.getSelectedItem();
        DatabaseMetaTag databaseMetaTag = databaseSource.getDatabaseMetaTag(objectType.name);

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

        schemaSelectionMade();
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
                    
                    DatabaseHost selectedHost = combosGroup.getSelectedHost();
                    
                    if (selectedHost == null) {

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
                    String text = type == 0 ? " function " : " procedure ";
                    setActionMessage("Executing" + text + databaseExecutable.getName() + "...");

                    DatabaseConnection databaseConnection = selectedHost.getDatabaseConnection();
                    
                    if (statementExecutor == null) {

                        statementExecutor = new DefaultStatementExecutor(databaseConnection);

                    } else {

                        statementExecutor.setDatabaseConnection(databaseConnection);
                    }

                    SqlStatementResult result = statementExecutor.execute(databaseExecutable);
                    Map results = (Map)result.getOtherResult();

                    if (results == null) {

                        setErrorMessage(result.getErrorMessage());

                    } else {

                        setPlainMessage("Statement executed successfully.");
                        int updateCount = result.getUpdateCount();

                        if (updateCount > 0) {

                            setPlainMessage(updateCount + 
                                    updateCount > 1 ? " rows affected." : " row affected.");
                        }

                        String SPACE = " = ";

                        for (Iterator<?> i = results.keySet().iterator(); i.hasNext();) {

                            String key = i.next().toString();

                            setPlainMessage(key + SPACE + results.get(key));                            
                        }

                        if (result.isResultSet()) {
                            
                            resultsPanel.setResultSet(result.getResultSet(), false, -1);
                        }
                        
                    }

                } catch(Exception e) {
                  
                    e.printStackTrace();

                } finally {
                  
                    setInProcess(false);
                }
                
            }
        });
    
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
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {

        enableCombos(true);
        combosGroup.connectionOpened(connectionEvent.getDatabaseConnection());
    }

    /**
     * Indicates a connection has been closed.
     * 
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {
        combosGroup.connectionClosed(connectionEvent.getDatabaseConnection());
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

            } catch (SQLException e) {}
        
        }

        combosGroup.close();

        return true;
    }
    
    public void itemStateChanging(ItemEvent e) {}
    
    
    class ParameterTableModel extends AbstractTableModel {
        
        private String UNKNOWN = "UNKNOWN";
        private String RETURN = "RETURN";
        private String RESULT = "RESULT";
        private String IN = "IN";
        private String INOUT = "INOUT";
        private String OUT = "OUT";
        
        private String[] columns = {"Parameter", "Data Type", "Mode", "Value"};
        private ProcedureParameter[] values;
        
        public ParameterTableModel() {}
        
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
            return 4;
        }
        
        public void clear() {
            values = null;
        }
        
        public void setValues(ProcedureParameter[] _procParams) {
            values = _procParams;
        }
        
        public Object getValueAt(int row, int col) {
            if (values == null) {
                return "";
            }

            ProcedureParameter param = values[row];
            
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
            ProcedureParameter param = values[row];
            
            switch (col) {
                
                case 0:
                    param.setName((String)value);
                    break;
                    
                case 1:
                    param.setSqlType((String)value);
                    break;
                    
                case 2:
                    if (value == IN) {
                        param.setType(DatabaseMetaData.procedureColumnIn);
                    }
                    else if (value == OUT) {
                        param.setType(DatabaseMetaData.procedureColumnOut);
                    }
                    else if (value == INOUT) {
                        param.setType(DatabaseMetaData.procedureColumnInOut);
                    }
                    else if (value == UNKNOWN) {
                        param.setType(DatabaseMetaData.procedureColumnUnknown);
                    }
                    else if (value == RESULT) {
                        param.setType(DatabaseMetaData.procedureColumnResult);
                    }
                    else if (value == RETURN) {
                        param.setType(DatabaseMetaData.procedureColumnReturn);
                    }
                    break;
                case 3:
                    param.setValue((String)value);
                    break;
                    
            }
            
            fireTableCellUpdated(row, col);
            
        }
        
        public String getColumnName(int col) {
            return columns[col];
        }
        
        public boolean isCellEditable(int row, int col) {
            
            if (col != 3) {
                return false;
            }
            
            ProcedureParameter param = values[row];
            int mode = param.getType();
            switch (mode) {
                
                case DatabaseMetaData.procedureColumnIn:
                case DatabaseMetaData.procedureColumnInOut:
                    return true;
                    
                case DatabaseMetaData.procedureColumnOut:
                case DatabaseMetaData.procedureColumnUnknown:
                case DatabaseMetaData.procedureColumnResult:
                case DatabaseMetaData.procedureColumnReturn:
                    return false;
                    
                default:
                    return true;
                    
            }
            
        }
        
    } // class ParameterTableModel


    class ExecutableObjectType {
        
        String name;
        
        ExecutableObjectType(String name) {

            this.name = name;
        }
        
        public String toString() {

            return StringUtils.capitalize(name.toLowerCase());
        }
        
    } // ExecutableObjectType
    
    
}





