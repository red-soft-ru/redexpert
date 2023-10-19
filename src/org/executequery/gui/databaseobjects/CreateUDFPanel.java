package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.UDFParameter;
import org.executequery.databaseobjects.impl.DefaultDatabaseUDF;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.procedure.UDFDefinitionPanel;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.sql.Types;
import java.util.Vector;

import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.BY_DESCRIPTOR;
import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.BY_VALUE;
import static org.underworldlabs.util.SQLUtils.columnDataFromProcedureParameter;

public class CreateUDFPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.UDF);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.UDF);
    JTextField nameModuleField;
    JTextField entryPointField;
    NumberTextField parameterNumberField;
    NumberTextField cstringLengthField;
    JCheckBox parameterBox;
    JCheckBox cstringBox;
    JComboBox mechanismBox;
    JCheckBox freeItBox;
    DynamicComboBoxModel mechanismModel;
    UDFDefinitionPanel parametersPanel;
    SelectTypePanel selectTypePanel;
    ColumnData returnsType;
    DefaultDatabaseUDF editedUDF;

    public CreateUDFPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateUDFPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    @Override
    protected void init() {
        nameModuleField = new JTextField();
        entryPointField = new JTextField();
        mechanismModel = new DynamicComboBoxModel();
        mechanismBox = new JComboBox(mechanismModel);
        mechanismModel.setElements(new String[]{"BY REFERENCE", "BY VALUE", "BY DESCRIPTOR",});
        freeItBox = new JCheckBox("FREE IT");
        parameterBox = new JCheckBox(bundleString("Parameter"));
        cstringBox = new JCheckBox("CSTRING");
        parameterNumberField = new NumberTextField();
        parameterNumberField.setValue(0);
        cstringLengthField = new NumberTextField();
        cstringLengthField.setValue(0);
        parametersPanel = new UDFDefinitionPanel();
        parametersPanel.setDataTypes(connection.getDataTypesArray(), connection.getIntDataTypesArray());
        returnsType = new ColumnData(connection);
        selectTypePanel = new SelectTypePanel(connection.getDataTypesArray(), connection.getIntDataTypesArray(), returnsType, false);

        parameterBox.addActionListener(actionEvent -> {
            parameterBoxChanged();
        });

        cstringBox.addActionListener(actionEvent -> {
            cstringBoxChanged();
        });

        tabbedPane.insertTab(bundleString("InputParameters"), null, parametersPanel, null, 0);
        addCommentTab(null);
        centralPanel.setVisible(false);
        topGbh.addLabelFieldPair(topPanel, bundleString("ModuleName"), nameModuleField, null, true, false);
        topGbh.addLabelFieldPair(topPanel, bundleString("EntryPoint"), entryPointField, null, false, true);
        topGbh.addLabelFieldPair(topPanel, bundleString("Mechanism"), mechanismBox, null, true, false);
        topPanel.add(parameterBox, topGbh.nextCol().setLabelDefault().get());
        topGbh.addLabelFieldPair(topPanel, bundleString("Position"), parameterNumberField, null, false, true);
        topPanel.add(freeItBox, topGbh.nextRowFirstCol().setLabelDefault().setWidth(2).get());
        topPanel.add(cstringBox, topGbh.nextCol().setLabelDefault().get());
        topGbh.addLabelFieldPair(topPanel, bundleString("MaxCountCharacters"), cstringLengthField, null, false, true);
        parameterBoxChanged();
        cstringBoxChanged();

    }

    @Override
    protected void initEdited() {
        reset();
        addCreateSqlTab(editedUDF);
    }

    protected String generateQuery() {
        StringBuilder sb = new StringBuilder();
        if (editing)
            sb.append("DROP EXTERNAL FUNCTION ").append(getFormattedName()).append(";\n");
        sb.append("DECLARE EXTERNAL FUNCTION ").append(getFormattedName()).append("\n");
        Vector<ColumnData> params = parametersPanel.getTableColumnDataVector();
        for (int i = 0; i < params.size(); i++) {
            ColumnData param = params.elementAt(i);
            if (param.isCString())
                sb.append("CSTRING (").append(param.getColumnSize()).append(") ");
            else {
                if (param.getSQLType() == Types.BLOB ||
                        param.getSQLType() == Types.LONGVARBINARY ||
                        param.getSQLType() == Types.LONGVARCHAR)
                    sb.append("BLOB").append(" ");
                else
                    sb.append(param.getFormattedDataType()).append(" ");
                if (!MiscUtils.isNull(param.getMechanism()) &&
                        !param.isNotNull() &&
                        !param.getMechanism().contains("BY VALUE") &&
                        !param.getMechanism().contains("BY REFERENCE") &&
                        !param.getMechanism().contains("BY BLOB DESCRIPTOR"))
                    sb.append(param.getMechanism()).append(" ");
            }
            if (param.isRequired() && param.getMechanism() != null && !param.getMechanism().contains("BY BLOB DESCRIPTOR"))
                sb.append(" NULL ");
            if (i < params.size() - 1)
                sb.append(",");
            sb.append("\n");
        }
        sb.append("RETURNS\n");
        if (cstringBox.isSelected()) {
            sb.append("CSTRING (").append(cstringLengthField.getValue()).append(")\n");
        } else if (parameterBox.isSelected()) {
            sb.append("PARAMETER ").append(parameterNumberField.getValue()).append("\n");
        } else {
            if (returnsType.getSQLType() == Types.BLOB ||
                    returnsType.getSQLType() == Types.LONGVARBINARY ||
                    returnsType.getSQLType() == Types.LONGVARCHAR)
                sb.append("BLOB").append(" ").append(mechanismBox.getSelectedItem()).append("\n");
            else {
                sb.append(returnsType.getFormattedDataType());
                if (mechanismBox.getSelectedIndex() != 0)
                    sb.append(" ").append(mechanismBox.getSelectedItem());
                sb.append("\n");
            }
        }
        if (freeItBox.isSelected())
            sb.append("FREE_IT\n");
        sb.append("ENTRY_POINT '").append(entryPointField.getText()).append("'\n");
        sb.append("MODULE_NAME '").append(nameModuleField.getText()).append("';\n");
        String text = simpleCommentPanel.getComment();
        if (!MiscUtils.isNull(text) && !text.trim().isEmpty()) {
            sb.append("COMMENT ON EXTERNAL FUNCTION ").append(getFormattedName()).append(" IS '");
            text = text.replace("'", "''");
            sb.append(text).append("'");
        }
        return sb.toString();
    }

    @Override
    public void createObject() {

        displayExecuteQueryDialog(generateQuery(), ";");
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
        return "UDF";
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        editedUDF = (DefaultDatabaseUDF) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    void parameterBoxChanged() {

        mechanismBox.setEnabled(!cstringBox.isSelected() && !parameterBox.isSelected());
        parameterNumberField.setEnabled(parameterBox.isSelected());
        if (parameterBox.isSelected()) {
            if (tabbedPane.getComponentAt(1) == selectTypePanel)
                tabbedPane.remove(selectTypePanel);
            cstringBox.setSelected(false);
            cstringBoxChanged();
        } else if (!cstringBox.isSelected()) {
            tabbedPane.insertTab(bundleString("ReturnsType"), null, selectTypePanel, null, 1);
            tabbedPane.setSelectedIndex(0);
        }
    }

    void cstringBoxChanged() {

        mechanismBox.setEnabled(!cstringBox.isSelected() && !parameterBox.isSelected());
        cstringLengthField.setEnabled(cstringBox.isSelected());
        if (cstringBox.isSelected()) {
            if (tabbedPane.getComponentAt(1) == selectTypePanel)
                tabbedPane.remove(selectTypePanel);
            parameterBox.setSelected(false);
            parameterBoxChanged();
        } else if (!parameterBox.isSelected()) {
            tabbedPane.insertTab(bundleString("ReturnsType"), null, selectTypePanel, null, 1);
            tabbedPane.setSelectedIndex(0);
        }
    }

    protected void reset() {
        if (editedUDF == null)
            return;
        editedUDF.getEntryPoint();
        nameField.setText(editedUDF.getName());
        nameField.setEnabled(false);
        freeItBox.setSelected(editedUDF.getFreeIt());
        nameModuleField.setText(editedUDF.getModuleName());
        entryPointField.setText(editedUDF.getEntryPoint());
        simpleCommentPanel.setDatabaseObject(editedUDF);

        int returnArg = editedUDF.getReturnArg();

        if (returnArg != 0) {
            parameterBox.setSelected(true);
            parameterNumberField.setText(String.valueOf(returnArg));
            parameterBoxChanged();
        } else {
            UDFParameter udfParameter = editedUDF.getUDFParameters().get(0);
            if (udfParameter.getSqlType().contains("CSTRING")) {// check for cstring type
                cstringBox.setSelected(true);
                cstringLengthField.setText(String.valueOf(udfParameter.getSize()));
                cstringBoxChanged();
            } else {
                returnsType = columnDataFromProcedureParameter(udfParameter, connection, false);

                selectTypePanel.setColumnData(returnsType);
                selectTypePanel.refresh();

                if (udfParameter.getMechanism() == BY_DESCRIPTOR)
                    mechanismBox.setSelectedIndex(2);
                else if (udfParameter.getMechanism() == BY_VALUE)
                    mechanismBox.setSelectedIndex(1);
                else mechanismBox.setSelectedIndex(0);
            }
        }

        // remove first empty row
        parametersPanel.removeRow(0);
        for (UDFParameter parameter :
                editedUDF.getUDFParameters()) {
            if (parameter.getPosition() == 0)
                continue;
            ColumnData cd = columnDataFromProcedureParameter(parameter, connection, false);
            cd.setMechanism(parameter.getStringMechanism());
            cd.setCString(parameter.isCString());
            cd.setNotNull(parameter.isNotNull());
            parametersPanel.addRow(cd);
        }
    }
}
