package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
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
import java.util.Vector;

public class CreateUDFPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "CREATE UDF";
    public static final String ALTER_TITLE = "ALTER UDF";
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
        mechanismModel.setElements(new String[]{"BY VALUE", "BY DESCRIPTOR"});
        freeItBox = new JCheckBox("FREE IT");
        parameterBox = new JCheckBox("Parameter");
        cstringBox = new JCheckBox("CSTRING");
        parameterNumberField = new NumberTextField();
        parameterNumberField.setValue(0);
        cstringLengthField = new NumberTextField();
        cstringLengthField.setValue(0);
        parametersPanel = new UDFDefinitionPanel();
        parametersPanel.setDataTypes(metaData.getDataTypesArray(), metaData.getIntDataTypesArray());
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

    }

    @Override
    public void createObject() {
        StringBuilder sb = new StringBuilder();
        sb.append("DECLARE EXTERNAL FUNCTION ").append(nameField.getText()).append("\n");
        Vector<ColumnData> params = parametersPanel.getTableColumnDataVector();
        for (int i = 0; i < params.size(); i++) {
            ColumnData param = params.elementAt(i);
            if (param.isCstring())
                sb.append("CSTRING (").append(param.getColumnSize()).append(") ");
            else {
                sb.append(param.getFormattedDataType()).append(" ");
                if (!MiscUtils.isNull(param.getMechanism()))
                    sb.append(param.getMechanism()).append(" ");
            }
            if (param.isRequired())
                sb.append("NULL ");
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
            sb.append(returnsType.getFormattedDataType()).append(" ").append(mechanismBox.getSelectedItem()).append("\n");
        }
        if (freeItBox.isSelected())
            sb.append("FREE_IT\n");
        sb.append("ENTRY_POINT '").append(entryPointField.getText()).append("'\n");
        sb.append("MODULE_NAME '").append(nameModuleField.getText()).append("';\n");
        sb.append("COMMENT ON EXTERNAL FUNCTION ").append(nameField.getText()).append(" IS '")
                .append(descriptionPanel.getTextAreaComponent().getText()).append("'");
        displayExecuteQueryDialog(sb.toString(), ";");
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return ALTER_TITLE;
    }

    @Override
    public String getTypeObject() {
        return "UDF";
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        returnsType = new ColumnData(connection);
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
