package org.executequery.gui.procedure;

import biz.redsoft.IFBDatabaseMetadata;
import org.executequery.GUIUtilities;
import org.executequery.components.SplitPaneFactory;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.DatabaseObjectFactoryImpl;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.PooledDatabaseMetaData;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.*;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vasiliy
 */
public abstract class CreateProcedureFunctionPanel extends JPanel
        implements FocusComponentPanel,
        ItemListener,
        TextEditorContainer {

    private String procedure;
    /**
     * The procedure name field
     */
    protected JTextField nameField;

    /**
     * The connection combo selection
     */
    protected JComboBox connectionsCombo;

    /**
     * the schema combo box model
     */
    protected DynamicComboBoxModel connectionsModel;

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
    private JTabbedPane procedureTabs;

    /**
     * The tabbed pane containing parameters
     */
    private JTabbedPane parametersTabs;

    /**
     * The buffer off all SQL generated
     */
    protected StringBuffer sqlBuffer;

    /**
     * The tool bar
     */
//    private CreateTableToolBar tools;

    /**
     * Utility to retrieve database meta data
     */
    protected MetaDataValues metaData;

    /**
     * The base panel
     */
    protected JPanel containerPanel;

    protected JPanel descriptionPanel;
    protected SimpleTextArea descriptionArea;

    protected JPanel ddlPanel;
    protected SimpleSqlTextPanel ddlTextPanel;

    /**
     * The base panel
     */
    protected JPanel mainPanel;

    /**
     * <p> Constructs a new instance.
     */
    public CreateProcedureFunctionPanel() {
        super(new BorderLayout());

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public CreateProcedureFunctionPanel(String procedure) {
        this();
        this.procedure = procedure;
        try {
            initEditing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEditing() throws Exception {
        nameField.setText(this.procedure);
        nameField.setEnabled(false);
        loadParameters();
        loadVariables();
    }

    boolean containsType (String type, String[] array) {
        for (String arrayType :
                array) {
            if (arrayType.toUpperCase().contains(type.toUpperCase()))
                return true;
        }
        return false;
    }

    private void loadVariables() {
        variablesPanel.deleteEmptyRow(); // remove first empty row

        String fullProcedureBody = null;
        DatabaseHost host = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            DatabaseConnection connection =
                    (DatabaseConnection) connectionsCombo.getSelectedItem();
            host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
            DatabaseMetaData dmd = host.getDatabaseMetaData();
            List<ProcedureParameter> parameters = new ArrayList<ProcedureParameter>();


            PooledDatabaseMetaData poolMetaData = (PooledDatabaseMetaData) dmd;
            DatabaseMetaData dMetaData = poolMetaData.getInner();
            URL[] urls = new URL[0];
            Class clazzdb = null;
            Object odb = null;
            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, dMetaData.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBDatabaseMetadataImpl");
            odb = clazzdb.newInstance();
            IFBDatabaseMetadata db = (IFBDatabaseMetadata) odb;

            fullProcedureBody = db.getProcedureSourceCode(dMetaData, this.procedure);
            try {
                statement = dMetaData.getConnection().createStatement();
                resultSet = statement.executeQuery("select\n" +
                        "p.rdb$description \n" +
                        "from rdb$procedures p\n" +
                        "where p.rdb$procedure_name = '" +
                        this.procedure +
                        "'");
                if (resultSet.next())
                    descriptionArea.getTextAreaComponent().setText(resultSet.getString(1));
            } finally {
                if (resultSet != null && !resultSet.isClosed())
                    resultSet.close();
                if (statement != null && !statement.isClosed())
                    statement.close();
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (host != null)
                host.close();
        }

        if (fullProcedureBody != null && !fullProcedureBody.isEmpty()) {
            fullProcedureBody = fullProcedureBody.toUpperCase();
            sqlBodyText.setSQLText(fullProcedureBody.substring(fullProcedureBody.indexOf("BEGIN")));

            if (fullProcedureBody.indexOf("DECLARE") == -1) // no variables
                return;
            String declaredVariables = fullProcedureBody.substring(fullProcedureBody.indexOf("DECLARE"), fullProcedureBody.indexOf("BEGIN"));
            if (declaredVariables != null && !declaredVariables.isEmpty()) {
                String[] split = declaredVariables.split("\r\n");
                for (String varString :
                        split) {

                    varString = varString.replace("DECLARE VARIABLE", "");
                    ProcedureParameter variable = new ProcedureParameter("", DatabaseMetaData.procedureColumnUnknown,
                            0, "", 0, 0);

                    String pattern = "([A-Z])\\w+"; // to find variable name and domain

                    // Create a Pattern object
                    Pattern r = Pattern.compile(pattern);

                    // Now create matcher object.
                    Matcher m = r.matcher(varString);
                    int matchesCount = 0;
                    while (m.find( )) {
                        if (matchesCount == 0) { // find name
                            variable.setName(m.group(0));
                            varString = varString.replace(m.group(0), "");
                        } else if (matchesCount == 1) { // find domain
                            String domain = m.group(0);
                            // check for type
                            if (!containsType(domain, metaData.getDataTypesArray())) {
                                variable.setDomain(domain);
                                varString = varString.replace(domain, "");
                            }
                        }
                        matchesCount++;
                        if (matchesCount == 2)
                            break;
                    }

                    // if the variable is a domain type, do not need to parse further
                    if (variable.getDomain() == null || variable.getDomain().isEmpty()) {
                        pattern = "([A-Z])\\w+"; // to find variable type

                        // Create a Pattern object
                        r = Pattern.compile(pattern);

                        // Now create matcher object.
                        m = r.matcher(varString);

                        String type = "";
                        if (m.find()) {
                            type = m.group(0);
                        }
                        // need to find blob subtype
                        if (type.equals("BLOB")) {
                            pattern = "(-?[0-9]\\d*(\\.\\d+)?)";
                            r = Pattern.compile(pattern);
                            m = r.matcher(varString);
                            matchesCount = 0;
                            while (m.find( )) {
                                if (matchesCount == 0) { // subtype
                                    variable.setSubtype(Integer.valueOf(m.group(0)));
                                    varString = varString.replace(m.group(0), "");
                                } else if (matchesCount == 1) { // segment size
                                    variable.setSize(Integer.valueOf(m.group(0)));
                                    varString = varString.replace(m.group(0), "");
                                }
                                matchesCount++;
                            }

                            if (varString.contains("BINARY")) {
                                variable.setSize(variable.getSubtype());
                                variable.setSubtype(0);
                            } else if (varString.contains("TEXT")) {
                                variable.setSize(variable.getSubtype());
                                variable.setSubtype(1);
                            }

                            if (variable.getSubtype() < 0)
                                type = "BLOB SUB_TYPE <0";
                            else if (variable.getSubtype() == 0)
                                type = "BLOB SUB_TYPE 0";
                            else
                                type = "BLOB SUB_TYPE 1";


                        } else {
                            pattern = "(?<=\\()\\d+(?:\\.\\d+)?(?=\\))"; // pattern for size of varchar and etc.
                            r = Pattern.compile(pattern);
                            m = r.matcher(varString);
                            if (m.find())
                                variable.setSize(Integer.valueOf(m.group(0)));

                            pattern = "(?<=\\()\\d+(?:\\.\\d+)?(?=\\,)"; // pattern for size of decimal and etc.
                            r = Pattern.compile(pattern);
                            m = r.matcher(varString);
                            if (m.find())
                                variable.setSize(Integer.valueOf(m.group(0)));

                            pattern = "(?<=\\,)\\d+(?:\\.\\d+)?(?=\\))"; // pattern for scale of decimal and etc.
                            r = Pattern.compile(pattern);
                            m = r.matcher(varString);
                            if (m.find())
                                variable.setScale(Integer.valueOf(m.group(0)));
                        }

                        pattern = "\\/\\*(.*?)\\*\\/"; // pattern for comment
                        r = Pattern.compile(pattern);
                        m = r.matcher(varString);
                        if (m.find()) {
                            String description = m.group(0);
                            description = description.replace("/*", "");
                            description = description.replace("*/", "");
                            variable.setDescription(description);
                        }
                        variable.setSqlType(type);

                    }

                    if (varString.contains("NOT NULL"))
                        variable.setNullable(0);
                    else
                        variable.setNullable(1);

                    variablesPanel.addRow(variable);
                }
            }
        }
    }

    private void loadParameters() {
        inputParametersPanel.deleteEmptyRow(); // remove first empty row
        outputParametersPanel.deleteEmptyRow(); // remove first empty row
        DatabaseHost host = null;
        try {
            DatabaseConnection connection =
                    (DatabaseConnection) connectionsCombo.getSelectedItem();
            host = new DatabaseObjectFactoryImpl().createDatabaseHost(connection);
            DatabaseMetaData dmd = host.getDatabaseMetaData();
            List<ProcedureParameter> parameters = new ArrayList<ProcedureParameter>();

            ResultSet rs = dmd.getProcedureColumns(null, null, this.procedure, null);

            while (rs.next()) {
                ProcedureParameter procedureParameter = new ProcedureParameter(rs.getString(4),
                        rs.getInt(5),
                        rs.getInt(6),
                        rs.getString(7),
                        rs.getInt(8),
                        0/*rs.getInt(12)*/);
                procedureParameter.setScale(rs.getInt(10));
                parameters.add(procedureParameter);
            }

            if (rs != null)
                rs.close();

            for (ProcedureParameter pp :
                    parameters) {
                Statement statement = dmd.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery("select\n" +
                        "f.rdb$field_sub_type as field_subtype,\n" +
                        "f.rdb$segment_length as segment_length,\n" +
                        "pp.rdb$field_source as field_source,\n" +
                        "pp.rdb$null_flag as null_flag,\n" +
                        "cs.rdb$character_set_name as character_set,\n" +
                        "pp.rdb$description as description\n" +
                        "from rdb$procedure_parameters pp,\n" +
                        "rdb$fields f\n" +
                        "left join rdb$character_sets cs on cs.rdb$character_set_id = f.rdb$character_set_id\n" +
                        "where pp.rdb$parameter_name = '" + pp.getName() + "'\n" +
                        "and pp.rdb$procedure_name = '" + this.procedure + "'\n" +
                        "and  pp.rdb$field_source = f.rdb$field_name");
                try {
                    if (resultSet.next()) {
                        pp.setSubtype(resultSet.getInt(1));
                        int size = resultSet.getInt(2);
                        if (size != 0)
                            pp.setSize(size);
                        pp.setNullable(resultSet.getInt(4) == 1 ? 0 : 1);
                        String domain = resultSet.getString(3);
                        if (!domain.contains("RDB$"))
                            pp.setDomain(domain.trim());
                        String characterSet = resultSet.getString(5);
                        if (characterSet != null && !characterSet.isEmpty() && !characterSet.contains("NONE"))
                            pp.setEncoding(characterSet.trim());
                        pp.setDescription(resultSet.getString(6));
                    }
                } finally {
                    resultSet.close();
                    statement.close();
                }
                if (pp.getType() == DatabaseMetaData.procedureColumnIn)
                    inputParametersPanel.addRow(pp);
                else if (pp.getType() == DatabaseMetaData.procedureColumnOut)
                    outputParametersPanel.addRow(pp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (host != null)
                host.close();
        }
    }

    private void init() throws Exception {

        nameField = WidgetFactory.createTextField();
        //initialise the schema label
        metaData = new MetaDataValues(true);

        // combo boxes
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(this);

        // create tab pane
        procedureTabs = new JTabbedPane();

        // create tab pane
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
        sqlBodyText.appendSQLText("begin\n" +
                "  /* Procedure Text */\n" +
                "  suspend;\n" +
                "end");
        sqlBodyText.setBorder(BorderFactory.createTitledBorder("Procedure body"));

        outSqlText = new SimpleSqlTextPanel();

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        containerPanel = new JPanel(new GridBagLayout());

        descriptionPanel = new JPanel(new GridBagLayout());

        ddlPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;

        WidgetFactory.addLabelFieldPair(mainPanel, "Connection:", connectionsCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Procedure Name:", nameField, gbc);

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
        splitPane.setDividerLocation(0.3);
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

        procedureTabs.insertTab("Edit", null, containerPanel, null, 0);

        procedureTabs.insertTab("Description", null, descriptionPanel, null, 1);

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
        procedureTabs.insertTab("DDL", null, ddlPanel, null, 2);

        mainPanel.add(procedureTabs, new GridBagConstraints(0, 2,
                2, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));

        procedureTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                generateScript();
            }
        });

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(mainPanel, BorderLayout.CENTER);

        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);

        // check initial values for possible value inits
        if (connections == null || connections.isEmpty()) {
            connectionsCombo.setEnabled(false);
        } else {
            DatabaseConnection connection =
                    (DatabaseConnection) connections.elementAt(0);
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

    }

    String formattedParameter(ColumnData cd) {
        StringBuffer sb = new StringBuffer();
        sb.append(cd.getColumnName() == null ? CreateTableSQLSyntax.EMPTY : cd.getColumnName()).
                append(" ");
        if (MiscUtils.isNull(cd.getComputedBy())) {
            if (MiscUtils.isNull(cd.getDomain())) {
                if (cd.getColumnType() != null) {
                    sb.append(cd.getFormattedDataType());
                }
            } else {
                sb.append(cd.getDomain());
            }
            sb.append(cd.isRequired() ? " NOT NULL" : CreateTableSQLSyntax.EMPTY);
            if (cd.getTypeParameter()!=ColumnData.OUTPUT_PARAMETER&&!MiscUtils.isNull(cd.getDefaultValue())) {
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
                sb.append(" DEFAULT " + value);
            }
            if (!MiscUtils.isNull(cd.getCheck())) {
                sb.append(" CHECK ( " + cd.getCheck() + ")");
            }
        } else {
            sb.append("COMPUTED BY ( " + cd.getComputedBy() + ")");
        }
        return sb.toString();
    }

    public String formattedParameters(Vector<ColumnData> tableVector, boolean variable) {
        StringBuffer sqlText = new StringBuffer();
        sqlText.append("\n");
        for (int i = 0, k = tableVector.size(); i < k; i++) {
            ColumnData cd = tableVector.elementAt(i);
            if(!MiscUtils.isNull(cd.getColumnName())) {
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

    void generateScript() {
        StringBuffer sb = new StringBuffer();
        sb.append("create or alter procedure ");
        sb.append(nameField.getText());
        sb.append(" (");
        sb.append(formattedParameters(inputParametersPanel._model.getTableVector(), false));
        sb.append(")\n");
        String output = formattedParameters(outputParametersPanel._model.getTableVector(), false);
        if(!MiscUtils.isNull(output.trim())) {
            sb.append("returns (");
            sb.append(output);
            sb.append(")\n");
        }
        sb.append("as");
        sb.append(formattedParameters(variablesPanel._model.getTableVector(), true));
        sb.append(sqlBodyText.getSQLText());
        sb.append("^\n");

        sb.append("\n");

        // add procedure description
        String text = descriptionArea.getTextAreaComponent().getText();
        if (text != null && !text.isEmpty()) {
            sb.append("\n");
            sb.append("COMMENT ON PROCEDURE ");
            sb.append(nameField.getText());
            sb.append(" IS '");
            sb.append(text);
            sb.append("'");
            sb.append("^\n");
        }

        for (ColumnData cd :
                inputParametersPanel._model.getTableVector()) {
            String cdText = cd.getDescription();
            if (cdText != null && !cdText.isEmpty()) {
                sb.append("\n");
                sb.append("COMMENT ON PARAMETER ");
                sb.append(nameField.getText()).append(".");
                sb.append(cd.getColumnName());
                sb.append(" IS '");
                sb.append(cdText);
                sb.append("'\n");
                sb.append("^\n");
            }
        }

        for (ColumnData cd :
                outputParametersPanel._model.getTableVector()) {
            String cdText = cd.getDescription();
            if (cdText != null && !cdText.isEmpty()) {
                sb.append("\n");
                sb.append("COMMENT ON PARAMETER ");
                sb.append(nameField.getText()).append(".");
                sb.append(cd.getColumnName());
                sb.append(" IS '");
                sb.append(cdText);
                sb.append("'\n");
                sb.append("^\n");
            }
        }

        ddlTextPanel.setSQLText(sb.toString());
    }

    String[] getDomains() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(getSelectedConnection(), true);
        java.util.List<String> domains = new ArrayList<>();
        try {
            String query = "select " +
                    "RDB$FIELD_NAME FROM RDB$FIELDS " +
                    "where RDB$FIELD_NAME not like 'RDB$%'\n" +
                    "and RDB$FIELD_NAME not like 'MON$%'\n" +
                    "order by RDB$FIELD_NAME";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading domains:" + e.getMessage());
            return null;
        }
    }

    String[] getGenerators() {
        DefaultStatementExecutor executor = new DefaultStatementExecutor(getSelectedConnection(), true);
        List<String> domains = new ArrayList<>();
        try {
            String query = "select " +
                    "RDB$GENERATOR_NAME FROM RDB$GENERATORS " +
                    "where RDB$SYSTEM_FLAG = 0 " +
                    "order by 1";
            ResultSet rs = executor.execute(QueryTypes.SELECT, query).getResultSet();
            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }
            executor.releaseResources();
            return domains.toArray(new String[domains.size()]);
        } catch (Exception e) {
            Log.error("Error loading generators:" + e.getMessage());
            return null;
        }
    }

    /**
     * Returns the selected connection from the panel's
     * connections combo selection box.
     *
     * @return the selected connection properties object
     */
    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) connectionsCombo.getSelectedItem();
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
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {
                    setInProcess(true);
                    if (source == connectionsCombo) {
                        connectionChanged();
                    }
                } finally {
                    setInProcess(false);
                }
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
        GUIUtils.invokeAndWait(new Runnable() {
            public void run() {
                inputParametersPanel.setDataTypes(dataTypes, intDataTypes);
                inputParametersPanel.setDomains(getDomains());

                outputParametersPanel.setDataTypes(dataTypes, intDataTypes);
                outputParametersPanel.setDomains(getDomains());

                variablesPanel.setDataTypes(dataTypes, intDataTypes);
                variablesPanel.setDomains(getDomains());
            }
        });
    }

    public void setFocusComponent() {
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
        if (procedureTabs.getSelectedComponent() != ddlPanel)
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

        for (int i = 0; i < cda.length; i++) {

            // reset the keys
            cda[i].setPrimaryKey(false);
            cda[i].setForeignKey(false);
            cda[i].resetConstraints();
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

}