package org.executequery.gui.procedure;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.databaseobjects.AbstractCreateObjectPanel;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.traceparser.ProcedureParserBaseListener;
import org.underworldlabs.traceparser.ProcedureParserLexer;
import org.underworldlabs.traceparser.ProcedureParserParser;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author vasiliy
 */
public abstract class CreateProcedureFunctionPanel extends AbstractCreateObjectPanel
        implements FocusComponentPanel,
        ItemListener,
        TextEditorContainer {

    protected String procedure;

    /**
     * The input parameters for procedure
     */
    protected NewProcedurePanel inputParametersPanel;

    /**
     * The output parameters for procedure
     */
    protected NewProcedurePanel outputParametersPanel;

    /**
     * The output parameters for procedure
     */
    protected NewProcedurePanel variablesPanel;

    /**
     * The body of procedure
     */
    protected SimpleSqlTextPanel sqlBodyText;

    /**
     * The text pane showing SQL generated
     */
    protected SimpleSqlTextPanel outSqlText;

    /**
     * The tabbed pane containing parameters
     */
    protected JTabbedPane parametersTabs;

    /**
     * The buffer off all SQL generated
     */
    private StringBuffer sqlBuffer;

    //    private CreateTableToolBar tools;

    /**
     * Utility to retrieve database meta data
     */
    protected MetaDataValues metaData;

    /**
     * The base panel
     */
    private JPanel containerPanel;

    protected JPanel descriptionPanel;
    protected SimpleTextArea descriptionArea;

    private JPanel ddlPanel;
    protected SimpleSqlTextPanel ddlTextPanel;

    /**
     * The base panel
     */
    protected JPanel mainPanel;

    /**
     * <p> Constructs a new instance.
     */

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedure) {
        this(dc, dialog, procedure, null);
    }

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedure, Object[] params) {
        super(dc, dialog, procedure, params);
    }

    protected void initEditing() {
        nameField.setText(this.procedure);
        nameField.setEnabled(false);
        loadParameters();
        loadVariables();
        loadDescription();
    }

    private boolean containsType(String type, String[] array) {
        for (String arrayType :
                array) {
            if (arrayType.toUpperCase().contains(type.toUpperCase()))
                return true;
        }
        return false;
    }

    private void loadDescription() {
        try {
            ResultSet resultSet = sender.getResultSet(queryGetDescription()).getResultSet();
            if (resultSet.next())
                descriptionArea.getTextAreaComponent().setText(resultSet.getString(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }
    }

    private void loadVariables() {
        // remove first empty row

        String fullProcedureBody = getFullSourceBody();
        if (fullProcedureBody != null && !fullProcedureBody.isEmpty()) {
            ProcedureParserLexer lexer = new ProcedureParserLexer(CharStreams.fromString(fullProcedureBody));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ProcedureParserParser sqlparser = new ProcedureParserParser(tokens);
            List<? extends ANTLRErrorListener> listeners = sqlparser.getErrorListeners();
            for (int i = 0; i < listeners.size(); i++) {
                if (listeners.get(i) instanceof ConsoleErrorListener)
                    sqlparser.removeErrorListener(listeners.get(i));
            }
            ParseTree tree = sqlparser.declare_block_without_params();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new ProcedureParserBaseListener() {
                @Override
                public void enterDeclare_block_without_params(ProcedureParserParser.Declare_block_without_paramsContext ctx) {
                    ProcedureParserParser.Full_bodyContext bodyContext = ctx.full_body();
                    sqlBodyText.setSQLText(bodyContext.getText());
                    List<ProcedureParserParser.Local_variableContext> vars = ctx.local_variable();
                    if (!vars.isEmpty()) {
                        variablesPanel.deleteEmptyRow();
                        for (int i = 0; i < vars.size(); i++) {
                            ProcedureParserParser.Local_variableContext var = vars.get(i);
                            ProcedureParameter variable = new ProcedureParameter("", DatabaseMetaData.procedureColumnUnknown,
                                    0, "", 0, 0);
                            variable.setName(var.variable_name().getText());
                            ProcedureParserParser.DatatypeContext type = var.datatype();
                            if (type != null && !type.isEmpty()) {
                                if (type.domain_name() != null && !type.domain_name().isEmpty()) {
                                    variable.setDomain(type.domain_name().getText());
                                }
                                if (type.datatypeSQL() != null && !type.datatypeSQL().isEmpty()) {
                                    variable.setSqlType(type.datatypeSQL().getText());
                                    if (type.datatypeSQL().type_size() != null && type.datatypeSQL().type_size().isEmpty()) {
                                        variable.setSize(Integer.parseInt(type.datatypeSQL().type_size().getText()));
                                    }
                                    if (type.datatypeSQL().scale() != null && type.datatypeSQL().scale().isEmpty()) {
                                        variable.setScale(Integer.parseInt(type.datatypeSQL().scale().getText()));
                                    }
                                    if (type.datatypeSQL().subtype() != null && type.datatypeSQL().subtype().isEmpty()) {
                                        if (type.datatypeSQL().subtype().any_name() != null && !type.datatypeSQL().subtype().any_name().isEmpty()) {
                                            variable.setSubType(1);
                                        }
                                        if (type.datatypeSQL().subtype().int_number() != null && !type.datatypeSQL().subtype().int_number().isEmpty()) {
                                            variable.setSubType(Integer.parseInt(type.datatypeSQL().subtype().int_number().getText()));
                                        }
                                    }
                                    if (type.datatypeSQL().charset_name() != null && type.datatypeSQL().charset_name().isEmpty()) {
                                        variable.setEncoding(type.datatypeSQL().charset_name().getText());
                                    }
                                }
                                if (type.type_of() != null && !type.type_of().isEmpty()) {
                                    if (type.type_of().domain_name() != null && !type.type_of().domain_name().isEmpty()) {
                                        variable.setDomain(type.type_of().domain_name().getText());
                                        variable.setTypeOfFrom(ColumnData.TYPE_OF_FROM_DOMAIN);
                                    }
                                    if (type.type_of().column_name() != null && !type.type_of().column_name().isEmpty()) {
                                        variable.setRelationName(type.type_of().table_name().getText());
                                        variable.setFieldName(type.type_of().column_name().getText());
                                        variable.setTypeOfFrom(ColumnData.TYPE_OF_FROM_COLUMN);
                                    }
                                }
                            }
                            if (var.notnull() != null && !var.notnull().isEmpty()) {
                                variable.setNullable(0);
                            } else variable.setNullable(1);
                            variablesPanel.addRow(variable);
                        }
                    }
                }
            }, tree);

        }
    }

    protected abstract String queryGetDescription();

    protected abstract String getFullSourceBody();

    protected abstract void loadParameters();

    protected void init() {

        //initialise the schema label
        metaData = new MetaDataValues(true);

        parametersTabs = new JTabbedPane();
        // create the column definition panel
        // and add this to the tabbed pane
        inputParametersPanel = new NewProcedurePanel(ColumnData.INPUT_PARAMETER);
        parametersTabs.add("Input parameters", inputParametersPanel);

        outputParametersPanel = new NewProcedurePanel(ColumnData.OUTPUT_PARAMETER);
        parametersTabs.add("Output parameters", outputParametersPanel);

        variablesPanel = new NewProcedurePanel(ColumnData.VARIABLE);
        parametersTabs.add("Variables", variablesPanel);

        sqlBodyText = new SimpleSqlTextPanel();
        sqlBodyText.appendSQLText(getEmptySqlBody());
        sqlBodyText.setBorder(BorderFactory.createTitledBorder(getTypeObject() + " body"));

        outSqlText = new SimpleSqlTextPanel();

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        containerPanel = new JPanel(new GridBagLayout());

        descriptionPanel = new JPanel(new GridBagLayout());

        ddlPanel = new JPanel(new GridBagLayout());

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints(0, 0,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0);
        topPanel.add(parametersTabs, gbcTop);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcBottom = new GridBagConstraints(0, 0,
                1, GridBagConstraints.REMAINDER, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0);
        bottomPanel.add(sqlBodyText, gbcBottom);

        JSplitPane3 splitPane = new JSplitPane3();//new SplitPaneFactory().create(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(bottomPanel);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(0.6);
        splitPane.setDividerSize(5);

        containerPanel.add(splitPane,
                new GridBagConstraints(0, 0,
                        1, 1, 1, 1,
                        GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                        0, 0));

        descriptionArea = new SimpleTextArea();

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1;
        gbc1.weighty = 1;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.insets.right = 5;
        gbc1.insets.left = 5;
        gbc1.insets.top = 5;
        gbc1.insets.bottom = 5;

        descriptionPanel.add(descriptionArea, gbc1);

        tabbedPane.insertTab("Edit", null, containerPanel, null, 0);

        tabbedPane.insertTab("Description", null, descriptionPanel, null, 1);

        ddlTextPanel = new SimpleSqlTextPanel();

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 1;
        gbc2.weighty = 1;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.insets.right = 5;
        gbc2.insets.left = 5;
        gbc2.insets.top = 5;
        gbc2.insets.bottom = 5;

        ddlPanel.add(ddlTextPanel, gbc2);
        tabbedPane.insertTab("DDL", null, ddlPanel, null, 2);

        tabbedPane.addChangeListener(changeEvent -> generateScript());

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);

        // check initial values for possible value inits
        metaData.setDatabaseConnection(connection);
        inputParametersPanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
        inputParametersPanel.setDomains(getDomains());
        inputParametersPanel.setDatabaseConnection(connection);

        outputParametersPanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
        outputParametersPanel.setDomains(getDomains());
        outputParametersPanel.setDatabaseConnection(connection);

        variablesPanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
        variablesPanel.setDomains(getDomains());
        variablesPanel.setDatabaseConnection(connection);
        //metaData


    }

    protected abstract String getEmptySqlBody();

    private String formattedParameter(ColumnData cd) {
        StringBuilder sb = new StringBuilder();
        sb.append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getColumnName()).
                append(" ");
        if (MiscUtils.isNull(cd.getComputedBy())) {
            if (MiscUtils.isNull(cd.getDomain())) {
                if (cd.getColumnType() != null || cd.isTypeOf()) {
                    sb.append(cd.getFormattedDataType());
                }
            } else {
                if (cd.isTypeOf())
                    sb.append(cd.getFormattedDataType());
                else
                    sb.append(cd.getDomainInQuotes());
            }
            sb.append(cd.isRequired() ? " NOT NULL" : CreateTableSQLSyntax.EMPTY);
            if (cd.getTypeParameter() != ColumnData.OUTPUT_PARAMETER && !MiscUtils.isNull(cd.getDefaultValue())) {
                String value = "";
                boolean str = false;
                int sqlType = cd.getSQLType();
                switch (sqlType) {

                    case Types.LONGVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.CHAR:
                    case Types.NCHAR:
                    case Types.VARCHAR:
                    case Types.VARBINARY:
                    case Types.BINARY:
                    case Types.NVARCHAR:
                    case Types.CLOB:
                    case Types.DATE:
                    case Types.TIME:
                    case Types.TIMESTAMP:
                        value = "'";
                        str = true;
                        break;
                    default:
                        break;
                }
                value += cd.getDefaultValue();
                if (str) {
                    value += "'";
                }
                sb.append(" DEFAULT ").append(value);
            }
            if (!MiscUtils.isNull(cd.getCheck())) {
                sb.append(" CHECK ( ").append(cd.getCheck()).append(")");
            }
        } else {
            sb.append("COMPUTED BY ( ").append(cd.getComputedBy()).append(")");
        }
        return sb.toString();
    }

    protected String formattedParameters(Vector<ColumnData> tableVector, boolean variable) {
        StringBuilder sqlText = new StringBuilder();
        sqlText.append("\n");
        for (int i = 0, k = tableVector.size(); i < k; i++) {
            ColumnData cd = tableVector.elementAt(i);
            if (!MiscUtils.isNull(cd.getColumnName())) {
                if (variable)
                    sqlText.append("DECLARE VARIABLE ");
                sqlText.append(formattedParameter(cd));
                if (variable) {
                    sqlText.append(";");
                    if (cd.getDescription() != null && !cd.getDescription().isEmpty()) {
                        sqlText.append(" /*");
                        sqlText.append(cd.getDescription());
                        sqlText.append("*/");
                    }
                } else if (i != k - 1) {
                    sqlText.append(",");
                }
                sqlText.append("\n");
            }
        }
        return sqlText.toString();
    }

    protected abstract void generateScript();

    String[] getDomains() {
        java.util.List<String> domains = new ArrayList<>();
        try {
            String query = "select " +
                    "RDB$FIELD_NAME FROM RDB$FIELDS " +
                    "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                    "and RDB$FIELD_NAME not like 'MON$%'\n" +
                    "order by RDB$FIELD_NAME";
            ResultSet rs = sender.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            sender.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading domains:" + e.getMessage());
            return null;
        }
    }

    String[] getGenerators() {
        List<String> domains = new ArrayList<>();
        try {
            String query = "select " +
                    "RDB$GENERATOR_NAME FROM RDB$GENERATORS " +
                    "where RDB$SYSTEM_FLAG = 0 " +
                    "order by 1";
            ResultSet rs = sender.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            sender.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading generators:" + e.getMessage());
            return null;
        }
    }
    /**
     * Returns the procedure name field.
     */
    public Component getDefaultFocusComponent() {
        return nameField;
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    public void itemStateChanged(ItemEvent event) {
        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED) {
            return;
        }

        final Object source = event.getSource();
        GUIUtils.startWorker(() -> {
            try {
                setInProcess(true);
                if (source == connectionsCombo) {
                    connectionChanged();
                }
            } finally {
                setInProcess(false);
            }
        });
    }

    private void columnChangeConnection(DatabaseConnection dc) {
        Vector<ColumnData> cd = inputParametersPanel.getTableColumnDataVector();
        for (ColumnData c : cd) {
            c.setDatabaseConnection(dc);
        }

        cd = outputParametersPanel.getTableColumnDataVector();
        for (ColumnData c : cd) {
            c.setDatabaseConnection(dc);
        }

        cd = variablesPanel.getTableColumnDataVector();
        for (ColumnData c : cd) {
            c.setDatabaseConnection(dc);
        }
    }

    private void connectionChanged() {
        // retrieve connection selection
        DatabaseConnection connection =
                (DatabaseConnection) connectionsCombo.getSelectedItem();

        // reset meta data
        metaData.setDatabaseConnection(connection);
        inputParametersPanel.setDatabaseConnection(connection);
        outputParametersPanel.setDatabaseConnection(connection);
        variablesPanel.setDatabaseConnection(connection);
        columnChangeConnection(connection);

        // reset data types
        try {
            populateDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the data types for the " +
                            "selected connection.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            populateDataTypes(new String[0], new int[0]);
        }

    }

    private void populateDataTypes(final String[] dataTypes, final int[] intDataTypes) {
        GUIUtils.invokeAndWait(() -> {
            inputParametersPanel.setDataTypes(dataTypes, intDataTypes);
            inputParametersPanel.setDomains(getDomains());

            outputParametersPanel.setDataTypes(dataTypes, intDataTypes);
            outputParametersPanel.setDomains(getDomains());

            variablesPanel.setDataTypes(dataTypes, intDataTypes);
            variablesPanel.setDomains(getDomains());
        });
    }

    protected void setFocusComponent() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    public void setSQLTextCaretPosition(int position) {
        outSqlText.setCaretPosition(position);
    }

    protected void addButtonsPanel(JPanel buttonsPanel) {
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void fireEditingStopped() {
        inputParametersPanel.fireEditingStopped();
        outputParametersPanel.fireEditingStopped();
        variablesPanel.fireEditingStopped();
    }

    public void setColumnDataArray(ColumnData[] cda) {
        inputParametersPanel.setColumnDataArray(cda, null);
        outputParametersPanel.setColumnDataArray(cda, null);
        variablesPanel.setColumnDataArray(cda, null);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
    }

    // -----------------------------------------------
    // --- TableConstraintFunction implementations ---
    // -----------------------------------------------

    public abstract Vector<String> getColumnNamesVector(String tableName, String schemaName);

    public void resetSQLText() {
        inputParametersPanel.resetSQLText();
        outputParametersPanel.resetSQLText();
        variablesPanel.resetSQLText();
    }

    public String getSQLText() {
        if (tabbedPane.getSelectedComponent() != ddlPanel)
            generateScript();
        return ddlTextPanel.getSQLText();
    }

    public String getTableName() {
        return nameField.getText();
    }

    // -----------------------------------------------


    // constraints panel only
    public void updateCellEditor(int col, int row, String value) {
    }

    public void columnValuesChanging(int col, int row, String value) {
    }

    public void stateChanged(ChangeEvent e) {

    }

    /*
    private void tableTabs_changed() {

        if (parametersTabs.getSelectedIndex() == 1) {
            tools.enableButtons(false);

            //          if (table.isEditing())
            //            table.removeEditor();

        }
        else {
            tools.enableButtons(true);
        }

    }
    */

    public ColumnData[] getTableColumnDataAndConstraints() {

        ColumnData[] cda = inputParametersPanel.getTableColumnData();

        for (ColumnData aCda : cda) {

            // reset the keys
            aCda.setPrimaryKey(false);
            aCda.setForeignKey(false);
            aCda.resetConstraints();
        }

        return cda;

    }

    public void columnValuesChanging() {
    }

//    public ColumnData[] getTableColumnData() {
//        return inputParametersPanel.getTableColumnData();
//    }


    // -----------------------------------------------

    public String getDisplayName() {
        return "";
    }

    // ------------------------------------------------
    // ----- TextEditorContainer implementations ------
    // ------------------------------------------------

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    public TextEditor getTextEditor() {
        return outSqlText;
    }

    public class JSplitPane3 extends JSplitPane {
        private boolean hasProportionalLocation = false;
        private double proportionalLocation = 0.5;
        private boolean isPainted = false;

        public void setDividerLocation(double proportionalLocation) {
            if (!isPainted) {
                hasProportionalLocation = true;
                this.proportionalLocation = proportionalLocation;
            } else {
                super.setDividerLocation(proportionalLocation);
            }
        }

        public void paint(Graphics g) {
            super.paint(g);
            if (!isPainted) {
                if (hasProportionalLocation) {
                    super.setDividerLocation(proportionalLocation);
                }
                isPainted = true;
            }
        }

    }

    protected void releaseResources(ResultSet rs) {
        try {
            if (rs == null)
                return;
            Statement st = rs.getStatement();
            if (st != null) {
                if (!st.isClosed())
                    st.close();
            }
        } catch (SQLException sqlExc) {
        }
    }

}