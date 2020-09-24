package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseTypeConverter;
import org.executequery.databaseobjects.impl.DefaultDatabaseUDF;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.datatype.SelectTypePanel;
import org.executequery.gui.procedure.UDFDefinitionPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;

import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.BY_DESCRIPTOR;
import static org.executequery.databaseobjects.impl.DefaultDatabaseUDF.BY_VALUE;

public class CreateUDFPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "Create UDF";
    public static final String EDIT_TITLE = "Edit UDF";
    SimpleTextArea descriptionPanel;
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
        descriptionPanel = new SimpleTextArea();
        nameModuleField = new JTextField();
        entryPointField = new JTextField();
        mechanismModel = new DynamicComboBoxModel();
        mechanismBox = new JComboBox(mechanismModel);
        mechanismModel.setElements(new String[]{"BY REFERENCE", "BY VALUE", "BY DESCRIPTOR",});
        freeItBox = new JCheckBox("FREE IT");
        parameterBox = new JCheckBox("Parameter");
        cstringBox = new JCheckBox("CSTRING");
        parameterNumberField = new NumberTextField();
        parameterNumberField.setValue(0);
        cstringLengthField = new NumberTextField();
        cstringLengthField.setValue(0);
        parametersPanel = new UDFDefinitionPanel();
        parametersPanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
        returnsType = new ColumnData(connection);
        selectTypePanel = new SelectTypePanel(metaData.getDataTypesArray(), metaData.getIntDataTypesArray(), returnsType, false);

        parameterBox.addActionListener(actionEvent -> {
            parameterBoxChanged();
        });

        cstringBox.addActionListener(actionEvent -> {
            cstringBoxChanged();
        });

        tabbedPane.insertTab("Input Parameters", null, parametersPanel, null, 0);
        tabbedPane.add("Description", descriptionPanel);
        centralPanel.setLayout(new GridBagLayout());
        centralPanel.add(new JLabel("Module Name"), new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(nameModuleField, new GridBagConstraints(1, 0,
                5, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(new JLabel("Entry Point"), new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(entryPointField, new GridBagConstraints(1, 1,
                5, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(new JLabel("Mechanism"), new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(mechanismBox, new GridBagConstraints(1, 2,
                5, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(parameterBox, new GridBagConstraints(0, 3,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(new JLabel("Position"), new GridBagConstraints(1, 3,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(parameterNumberField, new GridBagConstraints(2, 3,
                1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(cstringBox, new GridBagConstraints(3, 3,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(new JLabel("Max Count Characters"), new GridBagConstraints(4, 3,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(cstringLengthField, new GridBagConstraints(5, 3,
                1, 1, 1, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(freeItBox, new GridBagConstraints(0, 4,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        parameterBoxChanged();
        cstringBoxChanged();

    }

    @Override
    protected void initEdited() {
        if (editedUDF == null)
            return;

        try {
            editedUDF.loadParameters();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        nameField.setText(editedUDF.getName());
        nameField.setEnabled(false);
        freeItBox.setSelected(editedUDF.getFreeIt());
        nameModuleField.setText(editedUDF.getModuleName());
        entryPointField.setText(editedUDF.getEntryPoint());
        descriptionPanel.getTextAreaComponent().setText(editedUDF.getDescription());

        int returnArg = editedUDF.getReturnArg();

        if (returnArg != 0) {
            parameterBox.setSelected(true);
            parameterNumberField.setText(String.valueOf(returnArg));
            parameterBoxChanged();
        } else {
            DefaultDatabaseUDF.UDFParameter udfParameter = editedUDF.getUDFParameters().get(0);
            if (udfParameter.getFieldType() == 40) {// check for cstring type
                cstringBox.setSelected(true);
                cstringLengthField.setText(String.valueOf(udfParameter.getFieldLenght()));
                cstringBoxChanged();
            } else {
                returnsType.setColumnSize(udfParameter.getFieldLenght());
                returnsType.setSQLType(DatabaseTypeConverter.getSqlTypeFromRDBType(udfParameter.getFieldType(),
                        udfParameter.getFieldSubType()));
                returnsType.setColumnSubtype(udfParameter.getFieldSubType());
                returnsType.setColumnScale(udfParameter.getFieldScale());
                returnsType.setColumnType(udfParameter.getFieldStringType());

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
        for (DefaultDatabaseUDF.UDFParameter parameter :
                editedUDF.getUDFParameters()){
            if (parameter.getArgPosition() == 0)
                continue;
            ColumnData cd = new ColumnData(connection);
            cd.setSQLType(DatabaseTypeConverter.getSqlTypeFromRDBType(parameter.getFieldType(),
                    parameter.getFieldSubType()));
            cd.setColumnSubtype(parameter.getFieldSubType());
            cd.setColumnSize(parameter.getFieldLenght());
            cd.setColumnType(DatabaseTypeConverter.getDataTypeName(parameter.getFieldType(),
                    parameter.getFieldSubType(), parameter.getFieldScale()));
            cd.setMechanism(parameter.getStringMechanism());
            cd.setCstring(parameter.isCString());
            cd.setColumnRequired(parameter.isNotNull() ? 0 : 1);
            parametersPanel.addRow(cd);
        }
    }

    @Override
    public void createObject() {
        StringBuilder sb = new StringBuilder();
        if (editing)
            sb.append("DROP EXTERNAL FUNCTION ").append(getFormattedName()).append(";\n");
        sb.append("DECLARE EXTERNAL FUNCTION ").append(getFormattedName()).append("\n");
        Vector<ColumnData> params = parametersPanel.getTableColumnDataVector();
        for (int i = 0; i < params.size(); i++) {
            ColumnData param = params.elementAt(i);
            if (param.isCstring())
                sb.append("CSTRING (").append(param.getColumnSize()).append(") ");
            else {
                if (param.getSQLType() == Types.BLOB ||
                        param.getSQLType() == Types.LONGVARBINARY ||
                        param.getSQLType() == Types.LONGVARCHAR)
                    sb.append("BLOB").append(" ");
                else
                    sb.append(param.getFormattedDataType()).append(" ");
                if (!MiscUtils.isNull(param.getMechanism()) &&
                        param.getColumnRequired() == 1 &&
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
        String text = descriptionPanel.getTextAreaComponent().getText();
        if (!MiscUtils.isNull(text) && !text.trim().isEmpty()) {
            sb.append("COMMENT ON EXTERNAL FUNCTION ").append(getFormattedName()).append(" IS '");
            text = text.replace("'", "''");
            sb.append(text).append("'");
        }
        displayExecuteQueryDialog(sb.toString(), ";");
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
            tabbedPane.insertTab("Returns Type", null, selectTypePanel, null, 1);
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
            tabbedPane.insertTab("Returns Type", null, selectTypePanel, null, 1);
            tabbedPane.setSelectedIndex(0);
        }
    }
}
