package org.executequery.gui.procedure;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseExecutable;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.event.UserPreferenceListener;
import org.executequery.gui.*;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.AbstractCreateExternalObjectPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.procedureParser.ProcedureParserBaseListener;
import org.underworldlabs.procedureParser.ProcedureParserLexer;
import org.underworldlabs.procedureParser.ProcedureParserParser;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author vasiliy
 */
public abstract class CreateProcedureFunctionPanel extends AbstractCreateExternalObjectPanel
        implements FocusComponentPanel,
        UserPreferenceListener,
        ItemListener,
        TextEditorContainer {

    protected String procedureName;
    protected String procedureBody;

    protected KeyListener changeKeyListener;
    protected ActionListener changeActionListener;

    // --- GUI components ---

    protected ProcedureDefinitionPanel outputParamsPanel;
    protected ProcedureDefinitionPanel inputParamsPanel;
    protected ProcedureDefinitionPanel variablesPanel;
    protected SimpleSqlTextPanel ddlTextPanel;
    protected CursorsPanel cursorsPanel;

    // ---

    protected abstract String getEmptySqlBody();

    protected abstract String getFullSourceBody();

    protected abstract void loadParameters();

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedureName) {
        this(dc, dialog, procedureName, null);
    }

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedureName, Object[] params) {
        super(dc, dialog, procedureName, params);
        EventMediator.registerListener(this);
    }

    @Override
    protected void init() {

        changeActionListener = e -> generateDdlScript();
        changeKeyListener = new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                generateDdlScript();
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        };

        // --- external fields ---

        initExternal();

        // ---

        cursorsPanel = new CursorsPanel();

        inputParamsPanel = new ProcedureDefinitionPanel(ColumnData.INPUT_PARAMETER);
        inputParamsPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        inputParamsPanel.setDomains(getDomains());
        inputParamsPanel.setDatabaseConnection(connection);

        outputParamsPanel = new ProcedureDefinitionPanel(ColumnData.OUTPUT_PARAMETER);
        outputParamsPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        outputParamsPanel.setDomains(getDomains());
        outputParamsPanel.setDatabaseConnection(connection);

        variablesPanel = new ProcedureDefinitionPanel(ColumnData.VARIABLE);
        variablesPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        variablesPanel.setDomains(getDomains());
        variablesPanel.setDatabaseConnection(connection);

        ddlTextPanel = new SimpleSqlTextPanel(false, true, "DDL");
        ddlTextPanel.setMinimumSize(new Dimension(500, ddlTextPanel.getPreferredSize().height));
        ddlTextPanel.getTextPane().setDatabaseConnection(connection);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.add(bundleString("InputParameters"), inputParamsPanel);
        tabbedPane.add(bundleString("OutputParameters"), outputParamsPanel);
        tabbedPane.add(bundleString("Variables"), variablesPanel);
        tabbedPane.add(bundleString("Cursors"), cursorsPanel);
        addCommentTab(null);

        arrange();
        checkExternal();
        fillSqlBody();

        try {
            generateDdlScript();
        } catch (Exception ignored) {
        }

        if (!editing)
            addListeners();
    }

    protected void initEditing() {
        initEditedExternal();

        actionButton.setVisible(true);
        actionButton.setPreferredSize(null);
        actionButton.setText(Bundles.getCommon("execute"));
        actionButton.addActionListener(e -> displayExecuteDialog());
        actionButton.setPreferredSize(new Dimension(actionButton.getPreferredSize().width, submitButton.getPreferredSize().height));

        NamedObject namedObject = ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedureName);
        DefaultDatabaseExecutable executable = (DefaultDatabaseExecutable) namedObject;

        if (!MiscUtils.isNull(executable.getEntryPoint())) {
            useExternalCheck.setSelected(true);
            engineField.setText(executable.getEngine());
            externalField.setText(executable.getEntryPoint());
        }

        if (!MiscUtils.isNull(executable.getAuthid()))
            authidCombo.setSelectedItem(executable.getAuthid());

        if (!MiscUtils.isNull(executable.getSqlSecurity()))
            securityCombo.setSelectedItem(executable.getSqlSecurity());

        simpleCommentPanel.setDatabaseObject((DatabaseObject) namedObject);
        addPrivilegesTab(tabbedPane, (AbstractDatabaseObject) namedObject);
        addDependenciesTab((DatabaseObject) namedObject);

        reset();
        addListeners();
        generateDdlScript();
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- split pane ---

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(tabbedPane);
        splitPane.setRightComponent(ddlTextPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1.0);

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().leftGap(5).topGap(5).fillHorizontally().anchorNorthEast();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(actionButton, gbh.nextCol().setMinWeightX().fillNone().get());
        buttonPanel.add(submitButton, gbh.nextCol().get());
        buttonPanel.add(cancelButton, gbh.nextCol().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillBoth();
        mainPanel.add(topPanel, gbh.setMinWeightY().spanX().get());
        mainPanel.add(splitPane, gbh.nextRow().setMaxWeightY().setMaxWeightY().get());
        mainPanel.add(buttonPanel, gbh.nextRow().fillNone().anchorNorthEast().setMinWeightY().bottomGap(5).get());

        // --- base ---

        removeAll();
        setLayout(new GridBagLayout());
        centralPanel.setVisible(false);

        add(mainPanel, gbh.fillBoth().spanX().spanY().get());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setPreferredSize(new Dimension(1200, 600));
    }

    private void addListeners() {
        nameField.addKeyListener(changeKeyListener);
        engineField.addKeyListener(changeKeyListener);
        externalField.addKeyListener(changeKeyListener);
        authidCombo.addActionListener(changeActionListener);
        cursorsPanel.addChangesListener(changeActionListener);
        securityCombo.addActionListener(changeActionListener);
        variablesPanel.addChangesListener(changeActionListener);
        useExternalCheck.addActionListener(changeActionListener);
        inputParamsPanel.addChangesListener(changeActionListener);
        outputParamsPanel.addChangesListener(changeActionListener);
        simpleCommentPanel.getCommentField().getTextAreaComponent().addKeyListener(changeKeyListener);
    }

    @SuppressWarnings("DataFlowIssue")
    protected void generateDdlScript() {

        TreeSet<String> variables = new TreeSet<>();
        variables = fillTreeSetFromTableVector(variables, variablesPanel.tableVector);
        variables = fillTreeSetFromTableVector(variables, cursorsPanel.getCursorsVector());

        TreeSet<String> parameters = new TreeSet<>();
        parameters = fillTreeSetFromTableVector(parameters, inputParamsPanel.tableVector);
        parameters = fillTreeSetFromTableVector(parameters, outputParamsPanel.tableVector);

        ddlTextPanel.getTextPane().setVariables(variables);
        ddlTextPanel.getTextPane().setParameters(parameters);
        procedureBody = extractProcedureBody(ddlTextPanel.getSQLText());
        ddlTextPanel.setSQLText(generateQuery());
    }

    private String extractProcedureBody(String sqlText) {

        if (MiscUtils.isNull(sqlText))
            return procedureBody;

        int beginIndex = -1;
        int endIndex = -1;
        sqlText = sqlText.toUpperCase().trim();

        Pattern pattern = Pattern.compile("\\bBEGIN\\b");
        Matcher matcher = pattern.matcher(sqlText);
        if (matcher.find())
            beginIndex = matcher.start();

        pattern = Pattern.compile("\\bEND\\b");
        matcher = pattern.matcher(sqlText);
        while (matcher.find())
            endIndex = matcher.end();

        procedureBody = sqlText.substring(beginIndex, endIndex);
        return procedureBody;
    }

    private void loadVariables() {

        String fullProcedureBody = getFullSourceBody();
        if (fullProcedureBody != null && !fullProcedureBody.isEmpty()) {

            fullProcedureBody = fullProcedureBody.trim();
            ProcedureParserLexer lexer = new ProcedureParserLexer(CharStreams.fromString(fullProcedureBody));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ProcedureParserParser sqlParser = new ProcedureParserParser(tokens);

            List<? extends ANTLRErrorListener> listeners = sqlParser.getErrorListeners();
            for (ANTLRErrorListener listener : listeners) {
                if (listener instanceof ConsoleErrorListener)
                    sqlParser.removeErrorListener(listener);
            }

            ParseTree tree = sqlParser.declare_block_without_params();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new ProcedureParserBaseListener() {
                @Override
                public void enterDeclare_block_without_params(ProcedureParserParser.Declare_block_without_paramsContext ctx) {

                    ProcedureParserParser.Full_bodyContext bodyContext = ctx.full_body();
                    procedureBody = bodyContext.getText();

                    List<ProcedureParserParser.Local_variableContext> vars = ctx.local_variable();
                    if (!vars.isEmpty()) {

                        boolean firstVar = true;
                        boolean firstCursor = true;

                        for (ProcedureParserParser.Local_variableContext var : vars) {
                            if (var.cursor() == null) {

                                ProcedureParameter variable = new ProcedureParameter(
                                        var.variable_name().getText(),
                                        DatabaseMetaData.procedureColumnUnknown,
                                        0,
                                        "",
                                        0,
                                        0
                                );

                                ProcedureParserParser.DatatypeContext type = var.datatype();
                                if (type != null && !type.isEmpty()) {

                                    if (type.domain_name() != null && !type.domain_name().isEmpty()) {

                                        String domain = type.domain_name().getText();
                                        if (!domain.startsWith("\""))
                                            domain = domain.toUpperCase();

                                        variable.setDomain(domain);
                                    }

                                    if (type.datatypeSQL() != null && !type.datatypeSQL().isEmpty()) {

                                        List<ParseTree> children = type.datatypeSQL().children;
                                        variable.setSqlType(children.get(0).getText());

                                        if (type.datatypeSQL().type_size() != null && !type.datatypeSQL().type_size().isEmpty())
                                            variable.setSize(Integer.parseInt(type.datatypeSQL().type_size().getText().trim()));

                                        if (type.datatypeSQL().scale() != null && !type.datatypeSQL().scale().isEmpty())
                                            variable.setScale(Integer.parseInt(type.datatypeSQL().scale().getText().trim()));

                                        if (type.datatypeSQL().subtype() != null && !type.datatypeSQL().subtype().isEmpty()) {

                                            if (type.datatypeSQL().subtype().any_name() != null && !type.datatypeSQL().subtype().any_name().isEmpty())
                                                variable.setSubType(1);

                                            if (type.datatypeSQL().subtype().int_number() != null && !type.datatypeSQL().subtype().int_number().isEmpty())
                                                variable.setSubType(Integer.parseInt(type.datatypeSQL().subtype().int_number().getText().trim()));
                                        }

                                        if (type.datatypeSQL().charset_name() != null && !type.datatypeSQL().charset_name().isEmpty())
                                            variable.setEncoding(type.datatypeSQL().charset_name().getText());
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

                                if (var.notnull() != null && !var.notnull().isEmpty())
                                    variable.setNullable(0);
                                else
                                    variable.setNullable(1);

                                if (var.default_statement() != null)
                                    variable.setDefaultValue(var.default_statement().getText());

                                if (var.comment() != null) {

                                    String description = var.comment().getText();
                                    if (description.startsWith("--")) {
                                        description = description.substring(2);
                                        variable.setDescriptionAsSingleComment(true);

                                    } else if (description.startsWith("/*"))
                                        description = description.substring(2, description.length() - 2);

                                    variable.setDescription(description);
                                }

                                if (firstVar)
                                    variablesPanel.deleteEmptyRow();

                                firstVar = false;
                                variablesPanel.addRow(variable);

                            } else {

                                ColumnData cursor = new ColumnData(connection);
                                cursor.setCursor(true);

                                if (var.variable_name() != null)
                                    cursor.setColumnName(var.variable_name().getText());

                                if (var.cursor().scroll() != null)
                                    cursor.setScroll(var.cursor().scroll().getText().contentEquals("SCROLL"));
                                else
                                    cursor.setScroll(false);

                                if (var.cursor().operator_select() != null)
                                    cursor.setSelectOperator(var.cursor().operator_select().operator_select_in().getText());

                                if (var.comment() != null) {

                                    String description = var.comment().getText();
                                    if (description.startsWith("--")) {
                                        description = description.substring(2);
                                        cursor.setRemarkAsSingleComment(true);

                                    } else if (description.startsWith("/*"))
                                        description = description.substring(2, description.length() - 2);

                                    cursor.setRemarks(description);
                                }

                                if (firstCursor)
                                    cursorsPanel.deleteEmptyRow();

                                firstCursor = false;
                                cursorsPanel.addRow(cursor);
                            }
                        }
                    }

                }
            }, tree);
        }
    }

    private TreeSet<String> fillTreeSetFromTableVector(TreeSet<String> treeSet, List<ColumnData> tableVector) {

        for (ColumnData cd : tableVector)
            if (!MiscUtils.isNull(cd.getColumnName()))
                treeSet.add(cd.getColumnName().toUpperCase());

        return treeSet;
    }

    @Override
    protected void checkExternal() {
        super.checkExternal();

        if (useExternalCheck.isSelected()) {

            if (tabbedPane.indexOfComponent(variablesPanel) > 0) {
                tabbedPane.remove(variablesPanel);
                tabbedPane.remove(cursorsPanel);
            }

        } else {
            procedureBody = getEmptySqlBody();
            tabbedPane.setSelectedIndex(0);
            fillSqlBody();
        }
    }

    private String[] getDomains() {

        List<String> domains = ConnectionsTreePanel.getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.DOMAIN]);

        return domains.toArray(new String[0]);
    }

    private void displayExecuteDialog() {
        new BaseDialog(
                bundleString(Objects.equals(getTypeObject(), NamedObject.META_TYPES[NamedObject.FUNCTION]) ?
                        "executeFunction" :
                        "executeProcedure"
                ),
                true,
                new ExecuteProcedurePanel(
                        getTypeObject(),
                        procedureName,
                        connection
                )
        ).display();
    }

    /**
     * Returns the procedure name field.
     */
    @Override
    public Component getDefaultFocusComponent() {
        return nameField;
    }

    /**
     * Invoked when an item has been selected or deselected by the user.
     * The code written for this method performs the operations
     * that need to occur when an item is selected (or deselected).
     */
    @Override
    public void itemStateChanged(ItemEvent event) {

        if (event.getStateChange() == ItemEvent.DESELECTED)
            return;

        GUIUtils.startWorker(() -> {
            try {
                setInProcess(true);
                if (event.getSource() == connectionsCombo)
                    connectionChanged();

            } finally {
                setInProcess(false);
            }
        });
    }

    private void columnChangeConnection(DatabaseConnection dc) {

        Vector<ColumnData> columnDataVector = inputParamsPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setConnection(dc));

        columnDataVector = outputParamsPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setConnection(dc));

        columnDataVector = variablesPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setConnection(dc));
    }

    private void connectionChanged() {
        DatabaseConnection connection = (DatabaseConnection) connectionsCombo.getSelectedItem();

        // reset meta data
        inputParamsPanel.setDatabaseConnection(connection);
        outputParamsPanel.setDatabaseConnection(connection);
        variablesPanel.setDatabaseConnection(connection);
        columnChangeConnection(connection);

        // reset data types
        try {
            if (connection != null)
                populateDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
            else
                populateDataTypes(new String[0], new int[0]);

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorRetrievingDataTypes") + e.getExtendedMessage(), e, this.getClass());
            populateDataTypes(new String[0], new int[0]);
        }
    }

    private void populateDataTypes(final String[] dataTypes, final int[] intDataTypes) {
        GUIUtils.invokeAndWait(() -> {

            inputParamsPanel.setDataTypes(dataTypes, intDataTypes);
            inputParamsPanel.setDomains(getDomains());

            outputParamsPanel.setDataTypes(dataTypes, intDataTypes);
            outputParamsPanel.setDomains(getDomains());

            variablesPanel.setDataTypes(dataTypes, intDataTypes);
            variablesPanel.setDomains(getDomains());
        });
    }

    protected void setFocusComponent() {
        nameField.requestFocusInWindow();
        nameField.selectAll();
    }

    public void fireEditingStopped() {
        inputParamsPanel.fireEditingStopped();
        outputParamsPanel.fireEditingStopped();
        variablesPanel.fireEditingStopped();
    }

    public String getSQLText() {
        return ddlTextPanel.getSQLText();
    }

    public String getTableName() {
        return nameField.getText();
    }

    public String getDisplayName() {
        return "";
    }

    protected void releaseResources(ResultSet rs) {

        if (rs == null)
            return;

        try {
            Statement statement = rs.getStatement();
            if (statement != null && !statement.isClosed())
                statement.close();

        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
    }

    protected void fillSqlBody() {

        if (isParseVariables()) {

            if (tabbedPane.indexOfComponent(variablesPanel) < 0) {
                tabbedPane.insertTab(bundleString("Variables"), null, variablesPanel, null, 3);
                tabbedPane.insertTab(bundleString("Cursors"), null, cursorsPanel, null, 4);
            }
            loadVariables();

        } else {

            if (tabbedPane.indexOfComponent(variablesPanel) > 0) {
                tabbedPane.remove(variablesPanel);
                tabbedPane.remove(cursorsPanel);
            }

            if (procedureName != null)
                procedureBody = getFullSourceBody();
        }
    }

    protected static boolean isParseVariables() {
        return SystemProperties.getBooleanProperty("user", "general.parse.variables");
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess <code>true|false</code>
     */
    public void setInProcess(boolean inProcess) {
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof UserPreferenceEvent;
    }

    @Override
    public void preferencesChanged(UserPreferenceEvent event) {
        fillSqlBody();
    }

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    @Override
    public TextEditor getTextEditor() {
        return ddlTextPanel;
    }

    @Override
    protected void reset() {

        if (!editing)
            return;

        simpleCommentPanel.resetComment();
        nameField.setText(procedureName);
        nameField.setEditable(false);

        loadParameters();
        fillSqlBody();
    }

    @Override
    public String bundleString(String key) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key);
    }

    public String bundleString(String key, Object... args) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key, args);
    }

}
