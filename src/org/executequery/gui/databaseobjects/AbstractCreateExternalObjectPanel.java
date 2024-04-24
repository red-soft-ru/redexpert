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
        initSQLSecurity();

        useExternalCheck = WidgetFactory.createCheckBox("useExternalCheck", bundleStaticString("useExternal"));
        useExternalCheck.addItemListener(e -> checkExternal());

        externalField = WidgetFactory.createTextField("externalField");
        externalField.setMinimumSize(externalField.getPreferredSize());

        engineField = WidgetFactory.createTextField("engineField");
        engineField.setMinimumSize(engineField.getPreferredSize());

        topPanel.add(useExternalCheck, topGbh.nextCol().leftGap(0).fillBoth().spanX().get());
        topPanel.add(new JLabel(bundleStaticString("EntryPoint")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().leftGap(5).topGap(1).rightGap(0).get());
        topPanel.add(externalField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(bundleStaticString("Engine")), topGbh.nextCol().setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(engineField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
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
