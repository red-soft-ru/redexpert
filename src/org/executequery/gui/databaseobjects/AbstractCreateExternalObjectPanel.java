package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class AbstractCreateExternalObjectPanel extends AbstractSQLSecurityObjectPanel {

    protected JCheckBox useExternalBox;
    protected JTextField externalField;

    protected JTextField engineField;

    public AbstractCreateExternalObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public AbstractCreateExternalObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    protected void initExternal() {
        initSQLSecurity(false);
        useExternalBox = new JCheckBox(bundleStaticString("useExternal"));
        useExternalBox.addItemListener(e -> checkExternal());
        externalField = new JTextField(30);
        engineField = new JTextField(30);

        topPanel.add(useExternalBox, topGbh.nextCol().setLabelDefault().setWidth(2).get());
        topGbh.addLabelFieldPair(topPanel, bundleStaticString("EntryPoint"), externalField, null, true, false);
        topGbh.addLabelFieldPair(topPanel, bundleStaticString("Engine"), engineField, null, false);
    }

    protected void initEditedExternal() {
        useExternalBox.setEnabled(false);
    }


    protected void checkExternal() {

        boolean selected = useExternalBox.isSelected();
        int ind = topPanel.getComponentZOrder(externalField) - 1;

        externalField.setVisible(selected);
        topPanel.getComponent(ind).setVisible(selected);
        ind = topPanel.getComponentZOrder(engineField) - 1;
        engineField.setVisible(selected);
        topPanel.getComponent(ind).setVisible(selected);

        if (!selected) {
            externalField.setText(null);
            engineField.setText(null);
        }
    }
}
