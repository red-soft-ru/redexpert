package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.FunctionArgument;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseFunction;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.procedure.CreateProcedureFunctionPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class CreateFunctionPanel extends CreateProcedureFunctionPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.FUNCTION);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.FUNCTION);

    // --- GUI components ---

    private JComboBox<?> domainCombo;
    private SelectTypePanel typePanel;
    private JCheckBox deterministicCheck;
    private JCheckBox useDomainTypeCheck;

    // ---

    private ColumnData returnType;
    private DefaultDatabaseFunction function;

    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null, null);
    }

    public CreateFunctionPanel(DatabaseConnection dc, ActionContainer dialog, String procedure, DefaultDatabaseFunction databaseFunction) {
        super(dc, dialog, procedure, new Object[]{databaseFunction});
    }

    @Override
    protected void init() {
        super.init();

        Vector<NamedObject> domains = new Vector<>(getDomains());
        domains.add(0, null);

        // --- return type components ---

        deterministicCheck = WidgetFactory.createCheckBox("deterministicCheck", bundleStaticString("deterministic"));
        deterministicCheck.addActionListener(e -> generateDdlScript(false));

        useDomainTypeCheck = WidgetFactory.createCheckBox("useDomainTypeCheck", bundledString("useDomainTypeCheck"));
        useDomainTypeCheck.addActionListener(e -> domainCheckTriggered());

        domainCombo = WidgetFactory.createComboBox("domainCombo", domains);
        domainCombo.addActionListener(e -> domainChanged());

        typePanel = new SelectTypePanel(
                connection.getDataTypesArray(),
                connection.getIntDataTypesArray(),
                returnType,
                true,
                changeActionListener,
                changeKeyListener
        );

        if (function != null && function.getReturnArgument() != null) {

            if (function.getReturnArgument().getDomain() != null) {
                returnType.setDomain(function.getReturnArgument().getDomain());
                useDomainTypeCheck.setSelected(true);

                for (int i = 1; i < domainCombo.getItemCount(); i++) {
                    NamedObject item = (NamedObject) domainCombo.getItemAt(i);
                    if (item != null && Objects.equals(item.getName(), returnType.getDomain())) {
                        domainCombo.setSelectedIndex(i);
                        break;
                    }
                }

            } else
                returnType.setDomain(function.getReturnArgument().getSystemDomain());
        }
        typePanel.refresh();

        // --- return type panel ---

        JPanel returnTypePanel = new JPanel(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper().setInsets(0, 10, 5, 5).anchorNorthWest().fillBoth();

        returnTypePanel.add(deterministicCheck, gbh.setMinWeightX().setMinWeightY().get());
        returnTypePanel.add(useDomainTypeCheck, gbh.nextCol().get());
        returnTypePanel.add(domainCombo, gbh.nextCol().setMaxWeightX().leftGap(5).get());
        returnTypePanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbh.nextRowFirstCol().topGap(5).spanX().get());
        returnTypePanel.add(typePanel, gbh.nextRowFirstCol().setMaxWeightY().setInsets(0, 0, 5, 0).spanY().get());

        // --- tabbed pane ---

        tabbedPane.remove(outputParamsPanel);
        tabbedPane.setTitleAt(tabbedPane.indexOfComponent(inputParamsPanel), bundledString("Arguments"));
        tabbedPane.insertTab(bundledString("ReturnsType"), null, returnTypePanel, null, 1);

        // ---

        domainCheckTriggered();
        firstQuery = generateQuery();
    }

    @Override
    protected void initEdited() {
        super.initEditing();

        if (function != null)
            deterministicCheck.setSelected(function.isDeterministic());

        ddlTextPanel.setSQLText(generateQuery());
    }

    @Override
    protected String getFullSourceBody() {

        String query = "SELECT RDB$FUNCTION_SOURCE\n" +
                "FROM RDB$FUNCTIONS\n" +
                "WHERE RDB$FUNCTION_NAME = '" + procedureName + "'";

        String result = "";
        try {
            ResultSet rs = sender.getResultSet(query).getResultSet();
            if (rs.next())
                result = rs.getString(1);

        } catch (SQLException e) {
            Log.error(e.getMessage(), e);

        } finally {
            sender.releaseResources();
        }

        return result;
    }

    @Override
    protected String getUpperScript() {
        Vector<ColumnData> variables = null;
        if (isParseVariables()) {
            variables = new Vector<>();
            variables.addAll(variablesPanel.getProcedureParameterModel().getTableVector());
            variables.addAll(cursorsPanel.getCursorsVector());
            variables.addAll(subProgramPanel.getSubProgsVector());
        }
        return SQLUtils.generateUpperCreateFunctionScript(nameField.getText(),
                externalField.getText(),
                engineField.getText(),
                inputParamsPanel.getProcedureParameterModel().getTableVector(),
                variables,
                (String) securityCombo.getSelectedItem(),
                returnType,
                deterministicCheck != null && deterministicCheck.isSelected(),
                connection);
    }

    @Override
    protected String getDownScript() {
        return SQLUtils.generateDownCreateFunctionScript(nameField.getText(),
                simpleCommentPanel.getComment(),
                inputParamsPanel.getProcedureParameterModel().getTableVector(),
                true,
                connection);
    }

    @Override
    protected void loadParameters() {
        try {
            inputParamsPanel.clearRows();

            List<FunctionArgument> parameters = function.getFunctionArguments();
            if (parameters.size() > 1)
                inputParamsPanel.deleteEmptyRow();

            for (FunctionArgument pp : parameters)
                if (pp.getType() == DatabaseMetaData.functionColumnIn)
                    inputParamsPanel.addRow(pp);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    @Override
    protected String getEmptySqlBody() {
        return "BEGIN\n\t/* Function impl */\nEND";
    }

    @Override
    protected String generateQuery() {

        if (isParseVariables()) {
            Vector<ColumnData> variables = new Vector<>();
            variables.addAll(variablesPanel.getProcedureParameterModel().getTableVector());
            variables.addAll(cursorsPanel.getCursorsVector());
            variables.addAll(subProgramPanel.getSubProgsVector());
            variables.sort(new Comparator<ColumnData>() {
                @Override
                public int compare(ColumnData o1, ColumnData o2) {
                    return Integer.compare(o1.getColumnPosition(), o2.getColumnPosition());
                }
            });

            return SQLUtils.generateCreateFunction(
                    nameField.getText(),
                    inputParamsPanel.getProcedureParameterModel().getTableVector(),
                    variables,
                    returnType,
                    procedureBody,
                    externalField.getText(),
                    engineField.getText(),
                    (String) securityCombo.getSelectedItem(),
                    simpleCommentPanel.getComment(),
                    false,
                    true,
                    deterministicCheck != null && deterministicCheck.isSelected(),
                    connection
            );
        }

        return SQLUtils.generateCreateFunction(
                nameField.getText(),
                inputParamsPanel.getProcedureParameterModel().getTableVector(),
                returnType,
                procedureBody,
                externalField.getText(),
                engineField.getText(),
                (String) securityCombo.getSelectedItem(),
                simpleCommentPanel.getComment(),
                false,
                true,
                deterministicCheck != null && deterministicCheck.isSelected(),
                connection
        );
    }

    @Override
    public void createObject() {
        try {
            displayExecuteQueryDialog(getSQLText(), "^");

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc, this.getClass());
        }
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.FUNCTION];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        procedureName = (String) databaseObject;
        returnType = new ColumnData(connection);
    }

    @Override
    public void setParameters(Object[] params) {
        function = (DefaultDatabaseFunction) params[0];
        if (function != null && function.getReturnArgument() != null)
            returnType = SQLUtils.columnDataFromProcedureParameter(function.getReturnArgument(), connection, true);
    }

    private void domainCheckTriggered() {

        if (!useDomainTypeCheck.isSelected()) {
            domainCombo.setSelectedIndex(0);
            returnType.setDomain(null);
            typePanel.refresh();
        }

        domainCombo.setEnabled(useDomainTypeCheck.isSelected());
        typePanel.setEnabled(!useDomainTypeCheck.isSelected());
    }

    private void domainChanged() {

        if (!useDomainTypeCheck.isSelected())
            return;

        NamedObject selectedDomain = (NamedObject) domainCombo.getSelectedItem();
        if (selectedDomain != null)
            returnType.setDomain(selectedDomain.getName());

        typePanel.refresh(false);
        generateDdlScript(false);
    }

    private List<NamedObject> getDomains() {
        return ConnectionsTreePanel
                .getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.DOMAIN]);
    }

    public String bundledString(String key) {
        return Bundles.get(CreateFunctionPanel.class, key);
    }

}
