package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

public class CreateGeneratorPanel extends JPanel {

    private ActionContainer parent;

    JTextField nameText;

    NumberTextField valueText;

    NumberTextField startValueText;

    NumberTextField incrementText;

    JTextField description;

    JButton ok;

    JButton cancel;

    DatabaseConnection connection;

    private JComboBox connectionsCombo;

    JLabel labelIncrement;

    private DynamicComboBoxModel connectionsModel;

    DefaultDatabaseSequence generator;

    boolean editing;

    public static final String CREATE_TITLE = "Create Sequence";

    public static final String ALTER_TITLE = "Alter Sequence";

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseSequence generator) {
        this(dc, dialog, 0, 1, generator);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, int initial_value, int increment, DefaultDatabaseSequence generator) {
        this(dc, dialog, "NEW_SEQUENCE", initial_value, increment, generator);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, String name, int initial_value, int increment, DefaultDatabaseSequence generator) {
        this(dc, dialog, name, initial_value, increment, null, generator);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, String name, int initial_value, int increment, String description, DefaultDatabaseSequence generator_edited) {
        parent = dialog;
        connection = dc;
        generator = generator_edited;
        init(name, initial_value, increment, description);
        if (getVersion() < 3) {
            labelIncrement.setVisible(false);
            incrementText.setVisible(false);
        }
        editing = generator != null;
        if (editing)
            init_edited();
    }

    void init_edited() {
        nameText.setText(generator.getName().trim());
        nameText.setEnabled(false);
        startValueText.setLongValue(generator.getSequenceValue());
        if (getVersion() >= 3)
            incrementText.setValue(generator.getIncrement());
        description.setText(generator.getDescription());
        //valueText.setVisible(false);
    }

    void init(String name, int initial_value, int increment, String description) {
        nameText = new JTextField();
        if (name != null)
            nameText.setText(name);
        startValueText = new NumberTextField();
        startValueText.setValue(initial_value);
        incrementText = new NumberTextField();
        incrementText.setValue(increment);
        this.description = new JTextField(15);
        if (description != null)
            this.description.setText(description);
        ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createGenerator();
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
            }
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbc.gridx = 1;
        gbcLabel.gridheight = 1;
        gbc.gridheight = 1;
        gbcLabel.gridwidth = 1;
        gbc.gridwidth = 1;
        gbcLabel.weightx = 0;
        gbc.weightx = 1.0;
        gbcLabel.weighty = 0;
        gbc.weighty = 0;
        gbcLabel.gridy = 0;
        gbc.gridy = 0;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbc.anchor = GridBagConstraints.WEST;
        gbcLabel.ipadx = 0;
        gbc.ipadx = 0;
        gbcLabel.ipady = 0;
        gbc.ipady = 0;
        gbcLabel.insets = new Insets(5, 5, 5, 5);
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setLayout(new GridBagLayout());
        JLabel label = new JLabel("Connections");
        add(label, gbcLabel);
        add(connectionsCombo, gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Name");
        add(label, gbcLabel);
        add(nameText, gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Start Value");
        add(label, gbcLabel);
        add(startValueText, gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        labelIncrement = new JLabel("Increment");
        add(labelIncrement, gbcLabel);
        add(incrementText, gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Description");
        add(label, gbcLabel);
        add(this.description, gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        add(cancel, gbcLabel);
        add(ok, gbc);

    }

    int getVersion() {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        String vers = host.getDatabaseProductVersion();
        int version = 2;
        if (vers != null) {
            int number = 0;
            for (int i = 0; i < vers.length(); i++) {
                if (Character.isDigit(vers.charAt(i))) {
                    number = Character.getNumericValue(vers.charAt(i));
                    break;
                }
            }
            if (number >= 3)
                version = 3;

        }
        return version;
    }

    void createGenerator() {
        if (!MiscUtils.isNull(nameText.getText().trim())) {
            String query;
            if (getVersion() == 3) {
                query = "CREATE OR ALTER SEQUENCE " + nameText.getText() + " START WITH " + startValueText.getStringValue()
                        + " INCREMENT BY " + incrementText.getStringValue() + ";";
            } else {
                if (!editing)
                    query = "CREATE SEQUENCE " + nameText.getText() + ";";
                else query = "";
                query += "\nALTER SEQUENCE " + nameText.getText() + " RESTART WITH " + startValueText.getStringValue() + ";";
            }
            if (!MiscUtils.isNull(description.getText().trim()))
                query += "\nCOMMENT ON SEQUENCE " + nameText.getText() + " IS '" + description.getText() + "'";
            ExecuteQueryDialog eqd = new ExecuteQueryDialog(CREATE_TITLE, query, connection, true);
            eqd.display();
            if (eqd.getCommit()) {
                parent.finished();
            }
        } else
            GUIUtilities.displayErrorMessage("Name can not be empty");
    }
}
