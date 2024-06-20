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

    protected void initSQLSecurity() {

        sqlSecurityLabel = new JLabel();
        authidCombo = WidgetFactory.createComboBox("authidCombo", new String[]{"", "OWNER", "CALLER"});
        securityCombo = WidgetFactory.createComboBox("securityCombo", new String[]{"", "DEFINER", "INVOKER"});
        if (connection != null) {
            if (getDatabaseVersion() <= 2 && this instanceof CreateProcedurePanel) {
                sqlSecurityLabel.setText("AUTHID");
                topPanel.add(sqlSecurityLabel, topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(3).rightGap(0).get());
                topPanel.add(authidCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());

            } else if (getDatabaseVersion() >= 3) {
                sqlSecurityLabel.setText("SQL SECURITY");
                topPanel.add(sqlSecurityLabel, topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(3).rightGap(0).get());
                topPanel.add(securityCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
            }
        }
    }

}
