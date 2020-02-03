package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
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
 * @author vasiliy
 */
public class BrowserFunctionPanel extends AbstractFormObjectViewPanel {

    public static final String NAME = "BrowserFunctionPanel";

    private DependenciesPanel dependenciesPanel;

    private DisabledField funcNameField;
    //private DisabledField schemaNameField;

    private JLabel objectNameLabel;

    private JTable table;
    private BrowserFunctionPanel.FunctionTableModel model;

    private Map cache;

    JTextPane sourceTextPane;
    JTextPane createSqlPane;

    /**
     * the browser's control object
     */
    private BrowserController controller;

    public BrowserFunctionPanel(BrowserController controller) {
        super();
        this.controller = controller;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void init() {
        model = new BrowserFunctionPanel.FunctionTableModel();
        dependenciesPanel = new DependenciesPanel();
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
        sourcePanel.setBorder(BorderFactory.createTitledBorder(bundleString("Source")));
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
        funcNameField = new DisabledField();
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
        base.add(funcNameField, gbc);
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
        return new TablePrinter(table, funcNameField.getText());
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

    public void setValues(DefaultDatabaseFunction defaultDatabaseFunction) {
        dependenciesPanel.setDatabaseObject(defaultDatabaseFunction);
        objectNameLabel.setText(bundleString("function-name"));
        setHeaderText(bundleString("function"));
        setHeaderIcon(GUIUtilities.loadIcon("Function24.png", true));

        try {
            funcNameField.setText(defaultDatabaseFunction.getName());
            model.setValues(defaultDatabaseFunction.getFunctionArgumentsArray());
            sourceTextPane.setText(defaultDatabaseFunction.getFunctionSourceCode());
            createSqlPane.setText(defaultDatabaseFunction.getCreateSQLText());

        } catch (DataSourceException e) {
            controller.handleException(e);
        }

    }

    public void setValues(BaseDatabaseObject metaObject) {
        DefaultDatabaseFunction inF = (DefaultDatabaseFunction) cache.get(metaObject);
        setValues(metaObject, inF);
    }

    public void setValues(BaseDatabaseObject metaObject, DefaultDatabaseFunction function) {
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

        if (function != null) {
            funcNameField.setText(function.getName());
        } else {
            funcNameField.setText(metaObject.getName());
        }

        //schemaNameField.setText(metaObject.getSchemaName());
    }

    private void setHeaderIcon(String icon) {

//        setHeaderIcon(GUIUtilities.loadIcon(icon, true));
    }

    class FunctionTableModel extends AbstractTableModel {

        private String UNKNOWN = "UNKNOWN";
        private String RETURN = "RETURN";
        private String RESULT = "RESULT";
        private String IN = "IN";
        private String INOUT = "INOUT";
        private String OUT = "OUT";

        private String[] columns = Bundles.getCommons(new String[]{"parameter", "data-type", "mode"});
        private FunctionArgument[] funcParams;

        public FunctionTableModel() {
        }

        public FunctionTableModel(FunctionArgument[] _funcParams) {
            funcParams = _funcParams;
        }

        public int getRowCount() {

            if (funcParams == null)
                return 0;

            return funcParams.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public void setValues(FunctionArgument[] _funcParams) {

            if (_funcParams == funcParams)
                return;

            funcParams = _funcParams;
            fireTableDataChanged();

        }

        public Object getValueAt(int row, int col) {
            FunctionArgument param = funcParams[row];

            switch (col) {

                case 0:
                    return param.getName();

                case 1:

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
            FunctionArgument param = funcParams[row];

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
