package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;

import javax.swing.*;

public abstract class AbstractCreateExternalObjectPanel extends AbstractSQLSecurityObjectPanel {

    protected JCheckBox useExternalCheck;
    protected JTextField externalField;
    protected JTextField engineField;

    public AbstractCreateExternalObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    protected void initExternal() {
        initSQLSecurity(false);

        useExternalCheck = WidgetFactory.createCheckBox("useExternalCheck", bundleStaticString("useExternal"));
        useExternalCheck.addItemListener(e -> checkExternal());

        externalField = WidgetFactory.createTextField("externalField");
        engineField = WidgetFactory.createTextField("engineField");

        topPanel.add(useExternalCheck, topGbh.nextCol().setLabelDefault().setWidth(2).get());
        topGbh.addLabelFieldPair(topPanel, bundleStaticString("EntryPoint"), externalField, null, true, false);
        topGbh.addLabelFieldPair(topPanel, bundleStaticString("Engine"), engineField, null, false);
    }

    protected void initEditedExternal() {
        useExternalCheck.setEnabled(false);
    }

    protected void checkExternal() {

        boolean selected = useExternalCheck.isSelected();
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
