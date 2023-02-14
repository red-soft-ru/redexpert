package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;

import javax.swing.*;


public abstract class AbstractSQLSecurityObjectPanel extends AbstractCreateObjectPanel {
    public AbstractSQLSecurityObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public AbstractSQLSecurityObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    protected JComboBox sqlSecurityCombo;
    protected JComboBox authidCombo;

    protected JLabel sqlSecurityLabel;

    protected void initSQLSecurity(boolean spanX) {
        sqlSecurityLabel = new JLabel();
        authidCombo = new JComboBox(new String[]{"", "OWNER", "CALLER"});
        sqlSecurityCombo = new JComboBox(new String[]{"", "DEFINER", "INVOKER"});
        if (getDatabaseVersion() <= 2 && this instanceof CreateProcedurePanel) {
            sqlSecurityLabel.setText("AUTHID");
            topGbh.addLabelFieldPair(topPanel, sqlSecurityLabel, authidCombo, null);
        } else if (getDatabaseVersion() >= 3) {
            sqlSecurityLabel.setText("SQL SECURITY");
            topGbh.addLabelFieldPair(topPanel, sqlSecurityLabel, sqlSecurityCombo, null, true, spanX);
        }
    }
}
