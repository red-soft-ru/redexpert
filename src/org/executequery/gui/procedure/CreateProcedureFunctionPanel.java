package org.executequery.gui.procedure;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.ProcedureParameter;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseExecutable;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteProcedurePanel;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.AbstractCreateExternalObjectPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.localization.Bundles;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.procedureParser.ProcedureParserBaseListener;
import org.underworldlabs.procedureParser.ProcedureParserLexer;
import org.underworldlabs.procedureParser.ProcedureParserParser;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

/**
 * @author vasiliy
 */
public abstract class CreateProcedureFunctionPanel extends AbstractCreateExternalObjectPanel
        implements FocusComponentPanel,
        ItemListener,
        TextEditorContainer {

    protected String procedureName;
    private Object oldSelectedTab = null;

    protected JTabbedPane parametersTabs;
    protected NewProcedurePanel inputParametersPanel;
    protected NewProcedurePanel outputParametersPanel;
    protected NewProcedurePanel variablesPanel;

    protected SimpleSqlTextPanel sqlBodyText;
    protected SimpleSqlTextPanel outSqlText;
    protected SimpleSqlTextPanel ddlTextPanel;

    private JPanel ddlPanel;
    protected CursorsPanel cursorsPanel;
    protected JCheckBox parseVariablesBox;

    protected abstract String getEmptySqlBody();

    protected abstract String getFullSourceBody();

    protected abstract void loadParameters();

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedureName) {
        this(dc, dialog, procedureName, null);
    }

    public CreateProcedureFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedureName, Object[] params) {
        super(dc, dialog, procedureName, params);
    }

    @Override
    protected void init() {

        initExternal();

        parseVariablesBox = new JCheckBox(bundleString("parseVariables"));
        parseVariablesBox.addItemListener(e -> fillSqlBody());

        inputParametersPanel = new NewProcedurePanel(ColumnData.INPUT_PARAMETER);
        outputParametersPanel = new NewProcedurePanel(ColumnData.OUTPUT_PARAMETER);
        variablesPanel = new NewProcedurePanel(ColumnData.VARIABLE);
        cursorsPanel = new CursorsPanel();
        ddlPanel = new JPanel(new GridBagLayout());

        sqlBodyText = new SimpleSqlTextPanel();
        sqlBodyText.appendSQLText(getEmptySqlBody());
        sqlBodyText.getTextPane().setDatabaseConnection(connection);

        parametersTabs = new JTabbedPane();
        parametersTabs.add(bundleString("InputParameters"), inputParametersPanel);
        parametersTabs.add(bundleString("OutputParameters"), outputParametersPanel);
        parametersTabs.add(bundleString("Variables"), variablesPanel);
        parametersTabs.add(bundleString("Cursors"), cursorsPanel);
        parametersTabs.insertTab(bundleString("Body", bundleString(getTypeObject())), null, sqlBodyText, null, 0);
        parametersTabs.addChangeListener(e -> fillCustomKeyWords());

        outSqlText = new SimpleSqlTextPanel();
        outSqlText.getTextPane().setDatabaseConnection(connection);

        ddlTextPanel = new SimpleSqlTextPanel();
        ddlTextPanel.getTextPane().setDatabaseConnection(connection);

        topPanel.add(ddlPanel, topGbh.nextRowFirstCol().setLabelDefault().get());

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest().fillHorizontally().setInsets(5, 5, 5, 5);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(parseVariablesBox, gridBagHelper.get());
        mainPanel.add(parametersTabs, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());
        tabbedPane.insertTab(bundleString("Edit"), null, mainPanel, null, 0);

        addCommentTab(null);

        ddlPanel.add(ddlTextPanel, gridBagHelper.get());
        tabbedPane.insertTab(bundleString("DDL"), null, ddlPanel, null, 2);
        tabbedPane.addChangeListener(e -> generateScript());

        // check initial values for possible value init
        inputParametersPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        inputParametersPanel.setDomains(getDomains());
        inputParametersPanel.setDatabaseConnection(connection);

        outputParametersPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        outputParametersPanel.setDomains(getDomains());
        outputParametersPanel.setDatabaseConnection(connection);

        variablesPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        variablesPanel.setDomains(getDomains());
        variablesPanel.setDatabaseConnection(connection);

        //metaData
        topGbh.nextRowFirstCol();
        checkExternal();
        fillSqlBody();

        centralPanel.setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }

    protected void initEditing() {

        initEditedExternal();

        JButton executeButton = new JButton(Bundles.getCommon("execute"));
        executeButton.addActionListener(e -> displayExecuteDialog());

        DefaultDatabaseExecutable executable = (DefaultDatabaseExecutable) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedureName);

        if (!MiscUtils.isNull(executable.getEntryPoint())) {
            useExternalBox.setSelected(true);
            engineField.setText(executable.getEngine());
            externalField.setText(executable.getEntryPoint());
        }

        if (!MiscUtils.isNull(executable.getAuthid()))
            authidCombo.setSelectedItem(executable.getAuthid());

        if (!MiscUtils.isNull(executable.getSqlSecurity()))
            sqlSecurityCombo.setSelectedItem(executable.getSqlSecurity());

        topPanel.add(executeButton, topGbh.setLabelDefault().get());
        addPrivilegesTab(tabbedPane, (AbstractDatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedureName));
        addDependenciesTab((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedureName));

        simpleCommentPanel.setDatabaseObject((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedureName));
        generateScript();
        reset();
        fillCustomKeyWords();
    }

    private void loadVariables() {

        // remove first empty row
        variablesPanel.clearRows();

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
                    sqlBodyText.setSQLText(bodyContext.getText());

                    List<ProcedureParserParser.Local_variableContext> vars = ctx.local_variable();
                    if (!vars.isEmpty()) {

                        boolean firstVar = true;
                        boolean firstCursor = true;

                        for (ProcedureParserParser.Local_variableContext var : vars) {
                            if (var.cursor() == null) {

                                ProcedureParameter variable = new ProcedureParameter(
                                        var.variable_name().getText(), DatabaseMetaData.procedureColumnUnknown,
                                        0, "", 0, 0);

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
                                        cursor.setDescriptionAsSingleComment(true);
                                    } else if (description.startsWith("/*"))
                                        description = description.substring(2, description.length() - 2);

                                    cursor.setDescription(description);
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

    protected void fillCustomKeyWords() {

        TreeSet<String> vars = new TreeSet<>();
        vars = fillTreeSetFromTableVector(vars, variablesPanel.tableVector);
        vars = fillTreeSetFromTableVector(vars, cursorsPanel.tableVector);

        sqlBodyText.getTextPane().setVariables(vars);
        ddlTextPanel.getTextPane().setVariables(vars);

        TreeSet<String> pars = new TreeSet<>();
        pars = fillTreeSetFromTableVector(pars, inputParametersPanel.tableVector);
        pars = fillTreeSetFromTableVector(pars, outputParametersPanel.tableVector);

        sqlBodyText.getTextPane().setParameters(pars);
        ddlTextPanel.getTextPane().setParameters(pars);
    }

    private TreeSet<String> fillTreeSetFromTableVector(TreeSet<String> treeSet, List<ColumnData> tableVector) {

        for (ColumnData cd : tableVector)
            if (!MiscUtils.isNull(cd.getColumnName()))
                treeSet.add(cd.getColumnName().toUpperCase());

        return treeSet;
    }

    protected void generateScript() {

        if (tabbedPane.getSelectedComponent() != ddlPanel && oldSelectedTab == ddlPanel) {

            String ddlText = ddlTextPanel.getSQLText();
            if (GUIUtilities.displayConfirmDialog(bundleString("confirmTabChange")) != JOptionPane.YES_OPTION) {
                tabbedPane.setSelectedComponent(ddlPanel);
                ddlTextPanel.setSQLText(ddlText);
            }

        } else
            ddlTextPanel.setSQLText(generateQuery());

        oldSelectedTab = tabbedPane.getSelectedComponent();
    }

    @Override
    protected void checkExternal() {

        super.checkExternal();

        if (useExternalBox.isSelected()) {
            parametersTabs.remove(sqlBodyText);
            parseVariablesBox.setVisible(false);
            if (parametersTabs.indexOfComponent(variablesPanel) > 0) {
                parametersTabs.remove(variablesPanel);
                parametersTabs.remove(cursorsPanel);
            }

        } else {
            parametersTabs.insertTab(bundleString("Body", bundleString(getTypeObject())), null, sqlBodyText, null, 0);
            parametersTabs.setSelectedComponent(sqlBodyText);
            parseVariablesBox.setVisible(true);
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

        ExecuteProcedurePanel procedurePanel = new ExecuteProcedurePanel(
                getTypeObject().contentEquals(NamedObject.META_TYPES[NamedObject.FUNCTION]) ? 1 : 0,
                procedureName
        );

        BaseDialog dialog = new BaseDialog(Bundles.getCommon("execute"), true, procedurePanel);
        dialog.display();
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

        // interested in selections only
        if (event.getStateChange() == ItemEvent.DESELECTED)
            return;

        final Object source = event.getSource();
        GUIUtils.startWorker(() -> {
            try {
                setInProcess(true);
                if (source == connectionsCombo)
                    connectionChanged();
            } finally {
                setInProcess(false);
            }
        });
    }

    private void columnChangeConnection(DatabaseConnection dc) {

        Vector<ColumnData> columnDataVector = inputParametersPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setDatabaseConnection(dc));

        columnDataVector = outputParametersPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setDatabaseConnection(dc));

        columnDataVector = variablesPanel.getTableColumnDataVector();
        columnDataVector.forEach(c -> c.setDatabaseConnection(dc));

    }

    private void connectionChanged() {

        DatabaseConnection connection = (DatabaseConnection) connectionsCombo.getSelectedItem();

        // reset meta data
        inputParametersPanel.setDatabaseConnection(connection);
        outputParametersPanel.setDatabaseConnection(connection);
        variablesPanel.setDatabaseConnection(connection);
        columnChangeConnection(connection);

        // reset data types
        try {

            if (connection != null)
                populateDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
            else
                populateDataTypes(new String[0], new int[0]);

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorRetrievingDataTypes") + e.getExtendedMessage(), e);
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

    protected void addButtonsPanel(JPanel buttonsPanel) {
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void fireEditingStopped() {
        inputParametersPanel.fireEditingStopped();
        outputParametersPanel.fireEditingStopped();
        variablesPanel.fireEditingStopped();
    }

    public String getSQLText() {
        if (tabbedPane.getSelectedComponent() != ddlPanel)
            generateScript();
        return ddlTextPanel.getSQLText();
    }

    public String getTableName() {
        return nameField.getText();
    }

    public String getDisplayName() {
        return "";
    }

    protected void releaseResources(ResultSet rs) {

        try {
            if (rs != null) {
                Statement statement = rs.getStatement();
                if (statement != null && !statement.isClosed())
                    statement.close();
            }

        } catch (SQLException ignored) {
        }
    }

    protected void fillSqlBody() {

        if (parseVariablesBox.isSelected()) {

            if (parametersTabs.indexOfComponent(variablesPanel) < 0) {
                parametersTabs.add(bundleString("Variables"), variablesPanel);
                parametersTabs.add(bundleString("Cursors"), cursorsPanel);
            }
            loadVariables();

        } else {

            if (parametersTabs.indexOfComponent(variablesPanel) > 0) {
                parametersTabs.remove(variablesPanel);
                parametersTabs.remove(cursorsPanel);
            }
            if (procedureName != null)
                sqlBodyText.setSQLText(getFullSourceBody());
        }
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

    /**
     * Returns the SQL text pane as the TextEditor component
     * that this container holds.
     */
    @Override
    public TextEditor getTextEditor() {
        return outSqlText;
    }

    @Override
    protected void reset() {
        nameField.setText(this.procedureName);
        nameField.setEditable(false);
        loadParameters();
        fillSqlBody();
        simpleCommentPanel.resetComment();
    }

    @Override
    public String bundleString(String key) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key);
    }

    public String bundleString(String key, Object... args) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key, args);
    }

}
