package org.executequery.gui.procedure;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.FocusComponentPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.table.*;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.TextEditor;
import org.executequery.gui.text.TextEditorContainer;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author vasiliy
 */
public abstract class CreateProcedureFunctionPanel extends JPanel
        implements FocusComponentPanel,
        ItemListener,
        ChangeListener,
        TableFunction,
        TextEditorContainer {

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
     * The procedure column definition panel
     */
    protected NewProcedurePanel procedurePanel;

    /**
     * The body of procedure
     */
    protected SimpleSqlTextPanel sqlBodyText;

    /**
     * The text pane showing SQL generated
     */
    protected SimpleSqlTextPanel outSqlText;

    /**
     * The tabbed pane containing definition and constraints
     */
    private JTabbedPane tableTabs;

    /**
     * The buffer off all SQL generated
     */
    protected StringBuffer sqlBuffer;

    /**
     * The tool bar
     */
    private CreateTableToolBar tools;

    /**
     * Utility to retrieve database meta data
     */
    protected MetaDataValues metaData;

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
        tableTabs = new JTabbedPane();
        // create the column definition panel
        // and add this to the tabbed pane
        procedurePanel = new NewProcedurePanel(this);
        tableTabs.add("Input parameters", procedurePanel);

        sqlBodyText = new SimpleSqlTextPanel();
        sqlBodyText.setBorder(BorderFactory.createTitledBorder("LOL"));

        outSqlText = new SimpleSqlTextPanel();
        tools = new CreateTableToolBar(this);

        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;

        WidgetFactory.addLabelFieldPair(mainPanel, "Connection:", connectionsCombo, gbc);
        WidgetFactory.addLabelFieldPair(mainPanel, "Procedure Name:", nameField, gbc);

        JPanel definitionPanel = new JPanel(new GridBagLayout());
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.insets.top = 20;
        gbc.fill = GridBagConstraints.VERTICAL;
        definitionPanel.add(tools, gbc);
        gbc.insets.left = 0;
        gbc.insets.right = 5;
        gbc.insets.top = 0;
        gbc.gridx = 1;
        gbc.weighty = 0.4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        definitionPanel.add(tableTabs, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets.top = 10;
        mainPanel.add(definitionPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 0.6;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.insets.top = 5;
        mainPanel.add(sqlBodyText, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weighty = 0.6;
        gbc.insets.left = 5;
        gbc.insets.bottom = 5;
        gbc.insets.top = 5;
        mainPanel.add(outSqlText, gbc);

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(mainPanel, BorderLayout.CENTER);

        tableTabs.addChangeListener(this);
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                setSQLText();
            }
        });

        sqlBuffer = new StringBuffer(CreateTableSQLSyntax.CREATE_TABLE);

        // check initial values for possible value inits
        if (connections == null || connections.isEmpty()) {
            connectionsCombo.setEnabled(false);
        } else {
            DatabaseConnection connection =
                    (DatabaseConnection) connections.elementAt(0);
            metaData.setDatabaseConnection(connection);
            procedurePanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
            procedurePanel.setDomains(getDomains());
            procedurePanel.setDatabaseConnection(connection);
            //metaData
        }

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
        Vector<ColumnData> cd = getTableColumnDataVector();
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
        procedurePanel.setDatabaseConnection(connection);
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
                procedurePanel.setDataTypes(dataTypes, intDataTypes);
                procedurePanel.setDomains(getDomains());
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
        procedurePanel.fireEditingStopped();
    }

    public void setColumnDataArray(ColumnData[] cda) {
        procedurePanel.setColumnDataArray(cda, null);
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
        procedurePanel.resetSQLText();
    }

    public void setSQLText() {
        sqlBuffer.setLength(0);
        sqlBuffer.append(CreateTableSQLSyntax.CREATE_TABLE);

        sqlBuffer.append(nameField.getText()).
                append(CreateTableSQLSyntax.SPACE).
                append(CreateTableSQLSyntax.B_OPEN).
                append(procedurePanel.getSQLText());

        sqlBuffer.append(CreateTableSQLSyntax.B_CLOSE).
                append(CreateTableSQLSyntax.SEMI_COLON);

        setSQLText(sqlBuffer.toString());
    }

    public void setSQLText(String values, int type) {
        sqlBuffer.setLength(0);
        sqlBuffer.append(CreateTableSQLSyntax.CREATE_TABLE);
        StringBuffer primary = new StringBuffer(50);
        primary.setLength(0);
        primary.append(",\nCONSTRAINT PK_");
        primary.append(nameField.getText());
        primary.append(" PRIMARY KEY (");
        primary.append(procedurePanel.getPrimaryText());
        primary.append(")");
        StringBuffer description = new StringBuffer(50);
        description.setLength(0);
        for (String d : procedurePanel.descriptions) {
            description.append("COMMENT ON COLUMN ");
            description.append(nameField.getText());
            description.append("." + d);
            description.append("^");

        }

        sqlBuffer.append(nameField.getText()).
                append(CreateTableSQLSyntax.SPACE).
                append(CreateTableSQLSyntax.B_OPEN);

//        if (type == TableModifier.COLUMN_VALUES) {
//            sqlBuffer.append(values);
//            if (procedurePanel.primary)
//                sqlBuffer.append(primary);
//            sqlBuffer.append(consPanel.getSQLText());
//        } else if (type == TableModifier.CONSTRAINT_VALUES) {
//            sqlBuffer.append(procedurePanel.getSQLText());
//            if (procedurePanel.primary)
//                sqlBuffer.append(primary);
//            sqlBuffer.append(values);
//        }

        sqlBuffer.append(CreateTableSQLSyntax.B_CLOSE).
                append(CreateTableSQLSyntax.SEMI_COLON);
        sqlBuffer.append("\n").append(description);
        setSQLText(sqlBuffer.toString());
    }

    private void setSQLText(final String text) {
        GUIUtils.invokeLater(new Runnable() {
            public void run() {
                outSqlText.setSQLText(text);
            }
        });
    }

    public String getSQLText() {
        return outSqlText.getSQLText();
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

    public Vector getTableColumnDataVector() {
        return procedurePanel.getTableColumnDataVector();
    }

    public void stateChanged(ChangeEvent e) {
        if (tableTabs.getSelectedIndex() == 1) {
            tools.enableButtons(false);

            //          if (table.isEditing())
            //            table.removeEditor();

        } else {
            tools.enableButtons(true);
        }
    }

    /*
    private void tableTabs_changed() {

        if (tableTabs.getSelectedIndex() == 1) {
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

        ColumnData[] cda = procedurePanel.getTableColumnData();

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

    public ColumnData[] getTableColumnData() {
        return procedurePanel.getTableColumnData();
    }

    // -----------------------------------------------
    // -------- TableFunction implementations --------
    // -----------------------------------------------

    public void moveColumnUp() {
        int index = tableTabs.getSelectedIndex();
        if (index == 0) {
            procedurePanel.moveColumnUp();
        }
    }

    public void moveColumnDown() {
        int index = tableTabs.getSelectedIndex();
        if (index == 0) {
            procedurePanel.moveColumnDown();
        }
    }

    public void deleteRow() {
        if (tableTabs.getSelectedIndex() == 0) {
            procedurePanel.deleteRow();
        }
    }

    public void insertBefore() {
        procedurePanel.insertBefore();
    }

    public void insertAfter() {
        if (tableTabs.getSelectedIndex() == 0) {
            procedurePanel.insertAfter();
        }
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

}