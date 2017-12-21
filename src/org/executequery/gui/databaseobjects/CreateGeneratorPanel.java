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
import java.sql.SQLException;
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
        try {
            if (getVersion() < 3) {
                labelIncrement.setVisible(false);
                incrementText.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        editing = generator != null;
        if (editing) {
            try {
                init_edited();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void init_edited() throws SQLException {
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
                try {
                    createGenerator();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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

        this.setLayout(new BorderLayout());

        JPanel elementsPanel = new JPanel(new GridBagLayout());

        JLabel connLabel = new JLabel("Connections");
        elementsPanel.add(connLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        elementsPanel.add(connectionsCombo, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel nameLabel = new JLabel("Name");
        elementsPanel.add(nameLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        elementsPanel.add(nameText, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel startLabel = new JLabel("Start Value");
        elementsPanel.add(startLabel, new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        elementsPanel.add(startValueText, new GridBagConstraints(1, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        labelIncrement = new JLabel("Increment");
        elementsPanel.add(labelIncrement, new GridBagConstraints(0, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        elementsPanel.add(incrementText, new GridBagConstraints(1, 3,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel descLabel = new JLabel("Description");
        elementsPanel.add(descLabel, new GridBagConstraints(0, 4,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        elementsPanel.add(this.description, new GridBagConstraints(1, 4,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        this.add(elementsPanel, BorderLayout.PAGE_START);

        JPanel okCancelPanel = new JPanel(new GridBagLayout());
        okCancelPanel.add(ok, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        okCancelPanel.add(cancel, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        this.add(okCancelPanel, BorderLayout.PAGE_END);

    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    void createGenerator() throws SQLException {
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
