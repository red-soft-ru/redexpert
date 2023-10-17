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
import org.executequery.gui.table.CreateTableSQLSyntax;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    protected CursorsPanel cursorsPanel;

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
     * The base panel
     */
    private JPanel containerPanel;

    private JPanel ddlPanel;
    protected SimpleSqlTextPanel ddlTextPanel;

    /**
     * The base panel
     */
    protected JPanel mainPanel;

    protected JLabel sqlSecurityLabel;
    protected JCheckBox parseVariablesBox;





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
        initEditedExternal();
        JButton executeButton = new JButton(Bundles.getCommon("execute"));
        executeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BaseDialog dialog = new BaseDialog(Bundles.getCommon("execute"), true, new ExecuteProcedurePanel(getTypeObject().contentEquals(NamedObject.META_TYPES[NamedObject.FUNCTION]) ? 1 : 0, procedure));
                dialog.display();
            }
        });
        DefaultDatabaseExecutable executable = (DefaultDatabaseExecutable) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedure);
        if (!MiscUtils.isNull(executable.getEntryPoint())) {
            useExternalBox.setSelected(true);
            engineField.setText(executable.getEngine());
            externalField.setText(executable.getEntryPoint());

        }
        if (!MiscUtils.isNull(executable.getAuthid())) {
            authidCombo.setSelectedItem(executable.getAuthid());
        }
        if (!MiscUtils.isNull(executable.getSqlSecurity())) {
            sqlSecurityCombo.setSelectedItem(executable.getSqlSecurity());
        }
        topPanel.add(executeButton, topGbh.setLabelDefault().get());
        //TODO for system functions and procedures
        addPrivilegesTab(tabbedPane, (AbstractDatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedure));
        addDependenciesTab((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedure));
        simpleCommentPanel.setDatabaseObject((DatabaseObject) ConnectionsTreePanel.getNamedObjectFromHost(connection, getTypeObject(), procedure));
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
                        boolean first_var = true, first_cursor = true;
                        for (int i = 0; i < vars.size(); i++) {
                            ProcedureParserParser.Local_variableContext var = vars.get(i);
                            if (var.cursor() == null) {
                                ProcedureParameter variable = new ProcedureParameter("", DatabaseMetaData.procedureColumnUnknown,
                                        0, "", 0, 0);
                                variable.setName(var.variable_name().getText());
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
                                        if (type.datatypeSQL().type_size() != null && !type.datatypeSQL().type_size().isEmpty()) {
                                            variable.setSize(Integer.parseInt(type.datatypeSQL().type_size().getText().trim()));
                                        }
                                        if (type.datatypeSQL().scale() != null && !type.datatypeSQL().scale().isEmpty()) {
                                            variable.setScale(Integer.parseInt(type.datatypeSQL().scale().getText().trim()));
                                        }
                                        if (type.datatypeSQL().subtype() != null && !type.datatypeSQL().subtype().isEmpty()) {
                                            if (type.datatypeSQL().subtype().any_name() != null && !type.datatypeSQL().subtype().any_name().isEmpty()) {
                                                variable.setSubType(1);
                                            }
                                            if (type.datatypeSQL().subtype().int_number() != null && !type.datatypeSQL().subtype().int_number().isEmpty()) {
                                                variable.setSubType(Integer.parseInt(type.datatypeSQL().subtype().int_number().getText().trim()));
                                            }
                                        }
                                        if (type.datatypeSQL().charset_name() != null && !type.datatypeSQL().charset_name().isEmpty()) {
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
                                if (first_var)
                                    variablesPanel.deleteEmptyRow();
                                first_var = false;
                                variablesPanel.addRow(variable);
                            } else {
                                ColumnData cursor = new ColumnData(connection);
                                cursor.setCursor(true);
                                if (var.variable_name() != null) {
                                    cursor.setColumnName(var.variable_name().getText());
                                }
                                if (var.cursor().scroll() != null)
                                    cursor.setScroll(var.cursor().scroll().getText().contentEquals("SCROLL"));
                                else cursor.setScroll(false);
                                if (var.cursor().operator_select() != null) {
                                    cursor.setSelectOperator(var.cursor().operator_select().operator_select_in().getText());
                                }
                                if (var.comment() != null) {
                                    String description = var.comment().getText();
                                    if (description.startsWith("--")) {
                                        description = description.substring(2);
                                        cursor.setDescriptionAsSingleComment(true);
                                    } else if (description.startsWith("/*"))
                                        description = description.substring(2, description.length() - 2);
                                    cursor.setDescription(description);
                                }
                                if (first_cursor)
                                    cursorsPanel.deleteEmptyRow();
                                first_cursor = false;
                                cursorsPanel.addRow(cursor);

                            }
                        }
                    }
                }
            }, tree);

        }
    }

    protected abstract String getFullSourceBody();

    protected abstract void loadParameters();

    GridBagHelper centralGbh;

    protected void init() {
        initExternal();
        parseVariablesBox = new JCheckBox(bundleString("parseVariables"));
        parseVariablesBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                fillSqlBody();
            }
        });
        parametersTabs = new JTabbedPane();
        // create the column definition panel
        // and add this to the tabbed pane
        inputParametersPanel = new NewProcedurePanel(ColumnData.INPUT_PARAMETER);
        parametersTabs.add(bundleString("InputParameters"), inputParametersPanel);

        outputParametersPanel = new NewProcedurePanel(ColumnData.OUTPUT_PARAMETER);
        parametersTabs.add(bundleString("OutputParameters"), outputParametersPanel);

        variablesPanel = new NewProcedurePanel(ColumnData.VARIABLE);
        parametersTabs.add(bundleString("Variables"), variablesPanel);

        cursorsPanel = new CursorsPanel();
        parametersTabs.add(bundleString("Cursors"), cursorsPanel);

        sqlBodyText = new SimpleSqlTextPanel();
        sqlBodyText.appendSQLText(getEmptySqlBody());
        //sqlBodyText.setBorder(BorderFactory.createTitledBorder());
        sqlBodyText.getTextPane().setDatabaseConnection(connection);
        parametersTabs.insertTab(bundleString("Body", bundleString(getTypeObject())),null,sqlBodyText,null,0);
        parametersTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                fillCustomKeyWords();
            }
        });


        outSqlText = new SimpleSqlTextPanel();
        outSqlText.getTextPane().setDatabaseConnection(connection);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        containerPanel = new JPanel(new GridBagLayout());
        ddlPanel = new JPanel(new GridBagLayout());


        centralPanel.setLayout(new GridBagLayout());
        centralGbh = new GridBagHelper();
        centralGbh.setDefaultsStatic();
        centralGbh.defaults();

        topPanel.add(ddlPanel, topGbh.nextRowFirstCol().setLabelDefault().get());

        //centralGbh.previousRow().previousRow().addLabelFieldPair(topPanel, sqlSecurityLabel, authidCombo, null);

        containerPanel.add(parseVariablesBox,
                new GridBagConstraints(0, 0,
                        1, 1, 0, 0,
                        GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                        0, 0));
        containerPanel.add(parametersTabs,
                new GridBagConstraints(0, 1,
                        1, 1, 1, 1,
                        GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                        0, 0));





        tabbedPane.insertTab(bundleString("Edit"), null, containerPanel, null, 0);

        addCommentTab(null);

        ddlTextPanel = new SimpleSqlTextPanel();
        ddlTextPanel.getTextPane().setDatabaseConnection(connection);

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
        tabbedPane.insertTab(bundleString("DDL"), null, ddlPanel, null, 2);

        tabbedPane.addChangeListener(changeEvent -> generateScript());

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);

        // check initial values for possible value inits
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
    }

    protected void fillCustomKeyWords()
    {
        TreeSet<String> vars = new TreeSet<>();
        vars = fillTreeSetFromTableVector(vars,variablesPanel.tableVector);
        vars = fillTreeSetFromTableVector(vars,cursorsPanel.tableVector);
        sqlBodyText.getTextPane().setVariables(vars);
        ddlTextPanel.getTextPane().setVariables(vars);
        TreeSet<String> pars = new TreeSet<>();
        pars = fillTreeSetFromTableVector(pars,inputParametersPanel.tableVector);
        pars = fillTreeSetFromTableVector(pars,outputParametersPanel.tableVector);
        sqlBodyText.getTextPane().setParameters(pars);
        ddlTextPanel.getTextPane().setParameters(pars);
    }

    TreeSet<String> fillTreeSetFromTableVector(TreeSet<String> treeSet,List<ColumnData> tableVector)
    {
        for(ColumnData cd:tableVector)
        {
            if(!MiscUtils.isNull(cd.getColumnName()))
                treeSet.add(cd.getColumnName().toUpperCase());
        }
        return treeSet;
    }


    protected void checkExternal() {
        super.checkExternal();
        boolean selected = useExternalBox.isSelected();
        if (selected) {
            parametersTabs.remove(sqlBodyText);
            parseVariablesBox.setVisible(false);
            int index = parametersTabs.indexOfComponent(variablesPanel);
            if (index > 0) {
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

    protected abstract String getEmptySqlBody();


    protected abstract void generateScript();

    String[] getDomains() {
        java.util.List<String> domains = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection).getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.DOMAIN]);
        return domains.toArray(new String[domains.size()]);
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
        inputParametersPanel.setDatabaseConnection(connection);
        outputParametersPanel.setDatabaseConnection(connection);
        variablesPanel.setDatabaseConnection(connection);
        columnChangeConnection(connection);

        // reset data types
        try {
            populateDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
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

    protected void addButtonsPanel(JPanel buttonsPanel) {
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    public void fireEditingStopped() {
        inputParametersPanel.fireEditingStopped();
        outputParametersPanel.fireEditingStopped();
        variablesPanel.fireEditingStopped();
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


    public String getSQLText() {
        if (tabbedPane.getSelectedComponent() != ddlPanel)
            generateScript();
        return ddlTextPanel.getSQLText();
    }

    public String getTableName() {
        return nameField.getText();
    }


    public void stateChanged(ChangeEvent e) {

    }






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

    public String bundleString(String key) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key);
    }

    public String bundleString(String key, Object... args) {
        return Bundles.get(CreateProcedureFunctionPanel.class, key, args);
    }

    protected void reset() {
        nameField.setText(this.procedure);
        nameField.setEditable(false);
        loadParameters();
        fillSqlBody();
        simpleCommentPanel.resetComment();
    }

    protected void fillSqlBody() {
        if (parseVariablesBox.isSelected()) {
            if (parametersTabs.indexOfComponent(variablesPanel) < 0) {
                parametersTabs.add(bundleString("Variables"), variablesPanel);
                parametersTabs.add(bundleString("Cursors"), cursorsPanel);
            }
            loadVariables();
        } else {
            int index = parametersTabs.indexOfComponent(variablesPanel);
            if (index > 0) {
                parametersTabs.remove(variablesPanel);
                parametersTabs.remove(cursorsPanel);
            }
            if (procedure != null)
                sqlBodyText.setSQLText(getFullSourceBody());
        }
    }
}