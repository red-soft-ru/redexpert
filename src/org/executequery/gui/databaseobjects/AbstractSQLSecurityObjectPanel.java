package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;

import javax.swing.*;


public abstract class AbstractSQLSecurityObjectPanel extends AbstractCreateObjectPanel {

    protected JLabel sqlSecurityLabel;
    protected JComboBox<?> authidCombo;
    protected JComboBox<?> securityCombo;

    public AbstractSQLSecurityObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    protected void initSQLSecurity(boolean spanX) {

        sqlSecurityLabel = new JLabel();
        authidCombo = WidgetFactory.createComboBox("authidCombo", new String[]{"", "OWNER", "CALLER"});
        securityCombo = WidgetFactory.createComboBox("securityCombo", new String[]{"", "DEFINER", "INVOKER"});

        if (getDatabaseVersion() <= 2 && this instanceof CreateProcedurePanel) {
            sqlSecurityLabel.setText("AUTHID");
            topGbh.addLabelFieldPair(topPanel, sqlSecurityLabel, authidCombo, null);

        } else if (getDatabaseVersion() >= 3) {
            sqlSecurityLabel.setText("SQL SECURITY");
            topGbh.addLabelFieldPair(topPanel, sqlSecurityLabel, securityCombo, null, true, spanX);
        }
    }

}
