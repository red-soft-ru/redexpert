/*
 * BrowserProcedurePanel.java
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

package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.DatabaseExecutable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DefaultDatabaseProcedure;
import org.executequery.databaseobjects.impl.SystemDatabaseFunction;
import org.executequery.gui.DefaultTable;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SQLTextPane;
import org.executequery.localization.Bundles;
import org.executequery.print.TablePrinter;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DisabledField;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.print.Printable;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Takis Diakoumis
 */
public class BrowserProcedurePanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserProcedurePanel";

    private DependenciesPanel dependenciesPanel;

    private DisabledField procNameField;
    //private DisabledField schemaNameField;

    private JLabel objectNameLabel;

    private JTable table;
    private ProcedureTableModel model;

    private Map cache;

    JTextPane sourceTextPane;
    JTextPane createSqlPane;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserProcedurePanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        dependenciesPanel = new DependenciesPanel();
        model = new ProcedureTableModel();
        table = new DefaultTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setCellSelectionEnabled(true);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(false);

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.8);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        splitPane.setContinuousLayout(true);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);


        JPanel paramPanel = new JPanel(new BorderLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder(Bundles.getCommon("parameters")));
        paramPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.setBorder(BorderFactory.createTitledBorder(bundleString("source")));
        sourceTextPane = new SQLTextPane();
        sourceTextPane.setEditable(false);
        sourcePanel.add(new JScrollPane(sourceTextPane), BorderLayout.CENTER);
//        sourcePanel.add(new JScrollPane(sourceTextPane), BorderLayout.CENTER);
        splitPane.setTopComponent(paramPanel);
        splitPane.setBottomComponent(sourcePanel);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add(Bundles.getCommon("description"), splitPane);

        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.setBorder(BorderFactory.createEtchedBorder());

        createSqlPane = new SQLTextPane();

        sqlPanel.add(new JScrollPane(createSqlPane), BorderLayout.CENTER);

        tabs.add("Sql", sqlPanel);
        tabs.add(Bundles.getCommon("dependencies"), dependenciesPanel);


//        tabs.add("Source", sourcePanel);

        objectNameLabel = new JLabel();
        procNameField = new DisabledField();
        //schemaNameField = new DisabledField();

        JPanel base = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(10, 10, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx++;
        gbc.insets = insets;
        gbc.gridy++;
        base.add(objectNameLabel, gbc);
        gbc.gridy++;
        gbc.insets.top = 0;
        gbc.insets.right = 5;
        //base.add(new JLabel("Schema:"), gbc);
        gbc.insets.right = 10;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 10;
        gbc.fill = GridBagConstraints.BOTH;
        base.add(tabs, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = 5;
        gbc.insets.top = 10;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.gridy = 0;
        gbc.gridx = 1;
        base.add(procNameField, gbc);
        ++gbc.gridy;
        gbc.insets.top = 0;
        //base.add(schemaNameField, gbc);

        setHeaderText(bundleString("procedure"));
        setHeaderIcon(GUIUtilities.loadIcon("Procedure24.png", true));
        setContentPanel(base);
        cache = new HashMap();

    }

    public String getLayoutName() {
        return NAME;
    }

    public Printable getPrintable() {
        return new TablePrinter(table, procNameField.getText());
    }

    public void refresh() {
        cache.clear();
    }

    public void cleanup() {
    }

    public JTable getTable() {
        return table;
    }

    public void removeObject(Object object) {
        cache.remove(object);
    }

    public boolean hasObject(Object object) {
        return cache.containsKey(object);
    }

    public void setValues(DatabaseExecutable executeable) {
        dependenciesPanel.setDatabaseObject(executeable);
        int type = executeable.getType();
        if (executeable instanceof SystemDatabaseFunction) {
            type = ((SystemDatabaseFunction) executeable).getRealType();
        }

        switch (type) {
            case NamedObject.FUNCTION:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("function"));
                setHeaderIcon(GUIUtilities.loadIcon("Function24.png", true));
                break;

            case NamedObject.PROCEDURE:
                objectNameLabel.setText(bundleString("procedure-name"));
                setHeaderText(bundleString("procedure"));
                setHeaderIcon(GUIUtilities.loadIcon("Procedure24.png", true));
                break;

            case NamedObject.SYSTEM_STRING_FUNCTIONS:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-string-function"));
                setHeaderIcon(GUIUtilities.loadIcon("SystemFunction24.png", true));
                break;

            case NamedObject.SYSTEM_NUMERIC_FUNCTIONS:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-numeric-function"));
                setHeaderIcon(GUIUtilities.loadIcon("SystemFunction24.png", true));
                break;

            case NamedObject.SYSTEM_DATE_TIME_FUNCTIONS:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-date-function"));
                setHeaderIcon(GUIUtilities.loadIcon("SystemFunction24.png", true));
                break;
        }

        try {
            procNameField.setText(executeable.getName());
            model.setValues(executeable.getParametersArray());
            sourceTextPane.setText(executeable.getProcedureSourceCode());

            if (executeable instanceof DefaultDatabaseProcedure) {
                DefaultDatabaseProcedure p = (DefaultDatabaseProcedure) executeable;
                p.setHost(((DefaultDatabaseProcedure) executeable).getMetaTagParent().getHost());
                createSqlPane.setText(p.getCreateSQLText());
            }

        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseProcedure procedure = (DefaultDatabaseProcedure) cache.get(metaObject);
        setValues(metaObject, procedure);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseProcedure procedure) {
        int type = metaObject.getType();
        switch (type) {
            case BrowserConstants.FUNCTIONS_NODE:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("function"));
                setHeaderIcon("Function24.png");
                break;

            case BrowserConstants.PROCEDURE_NODE:
                objectNameLabel.setText(bundleString("procedure-name"));
                setHeaderText(bundleString("procedure"));
                setHeaderIcon("Procedure24.png");
                break;

            case BrowserConstants.SYSTEM_STRING_FUNCTIONS_NODE:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-string-function"));
                setHeaderIcon("SystemFunction24.png");
                break;

            case BrowserConstants.SYSTEM_NUMERIC_FUNCTIONS_NODE:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-numeric-function"));
                setHeaderIcon("SystemFunction24.png");
                break;

            case BrowserConstants.SYSTEM_DATE_TIME_FUNCTIONS_NODE:
                objectNameLabel.setText(bundleString("function-name"));
                setHeaderText(bundleString("system-date-function"));
                setHeaderIcon("SystemFunction24.png");
                break;
        }

        if (procedure != null) {
            procNameField.setText(procedure.getName());
            //model.setValues(procedure.getParametersArray());
        } else {
            procNameField.setText(metaObject.getName());
        }

        //schemaNameField.setText(metaObject.getSchemaName());
    }

    private void setHeaderIcon(String icon) {

//        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }

    class ProcedureTableModel extends AbstractTableModel {

        private String UNKNOWN = "UNKNOWN";
        private String RETURN = "RETURN";
        private String RESULT = "RESULT";
        private String IN = "IN";
        private String INOUT = "INOUT";
        private String OUT = "OUT";

        private String[] columns = Bundles.getCommons(new String[]{"parameter", "data-type", "mode"});
        private ProcedureParameter[] procParams;

        public ProcedureTableModel() {
        }

        public ProcedureTableModel(ProcedureParameter[] _procParams) {
            procParams = _procParams;
        }

        public int getRowCount() {

            if (procParams == null)
                return 0;

            return procParams.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public void setValues(ProcedureParameter[] _procParams) {

            if (_procParams == procParams)
                return;

            procParams = _procParams;
            fireTableDataChanged();

        }

        public Object getValueAt(int row, int col) {
            ProcedureParameter param = procParams[row];

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

                default:
                    return UNKNOWN;

            }

        }

        public void setValueAt(Object value, int row, int col) {
            ProcedureParameter param = procParams[row];

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















