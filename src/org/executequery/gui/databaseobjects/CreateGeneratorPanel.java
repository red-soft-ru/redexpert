package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class CreateGeneratorPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "Create Sequence";
    public static final String ALTER_TITLE = "Alter Sequence";
    NumberTextField startValueText;
    NumberTextField incrementText;
    SimpleTextArea description;
    JLabel labelIncrement;
    DefaultDatabaseSequence generator;

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseSequence generator) {
        super(dc, dialog, generator);
    }

    protected void init_edited() {
        nameField.setText(generator.getName().trim());
        nameField.setEnabled(false);
        startValueText.setLongValue(generator.getSequenceValue());
        if (getDatabaseVersion() >= 3)
            incrementText.setValue(generator.getIncrement());
        description.getTextAreaComponent().setText(generator.getDescription());
    }

    @Override
    public void create_object() {
        try {
            createGenerator();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        return NamedObject.META_TYPES[NamedObject.SEQUENCE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        generator = (DefaultDatabaseSequence) databaseObject;
    }

    protected void init() {
        startValueText = new NumberTextField();
        startValueText.setValue(0);
        incrementText = new NumberTextField();
        incrementText.setValue(1);
        this.description = new SimpleTextArea();

        main_panel.setLayout(new GridBagLayout());
        JLabel startLabel = new JLabel(bundleString("start-value"));
        main_panel.add(startLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        main_panel.add(startValueText, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        labelIncrement = new JLabel(bundleString("increment"));
        main_panel.add(labelIncrement, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        main_panel.add(incrementText, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        tabbedPane.add(bundlesString("description"), description);
        if (getDatabaseVersion() < 3) {
            labelIncrement.setVisible(false);
            incrementText.setVisible(false);
        }

    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    void createGenerator() throws SQLException {
        if (!MiscUtils.isNull(nameField.getText().trim())) {
            String query;
            if (getVersion() == 3) {
                query = "CREATE OR ALTER SEQUENCE " + nameField.getText() + " START WITH " + startValueText.getStringValue()
                        + " INCREMENT BY " + incrementText.getStringValue() + ";";
            } else {
                if (!editing)
                    query = "CREATE SEQUENCE " + nameField.getText() + ";";
                else query = "";
                query += "\nALTER SEQUENCE " + nameField.getText() + " RESTART WITH " + startValueText.getStringValue() + ";";
            }
            if (!MiscUtils.isNull(description.getTextAreaComponent().getText().trim()))
                query += "\nCOMMENT ON SEQUENCE " + nameField.getText() + " IS '" + description.getTextAreaComponent().getText() + "'";
            ExecuteQueryDialog eqd = new ExecuteQueryDialog(CREATE_TITLE, query, connection, true);
            eqd.display();
            if (eqd.getCommit()) {
                parent.finished();
            }
        } else
            GUIUtilities.displayErrorMessage("Name can not be empty");
    }
}
