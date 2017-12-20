package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.impl.DefaultDatabaseException;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

public class CreateExceptionPanel extends JPanel {

    private JLabel connectionLabel;

    private JComboBox connectionsCombo;

    private DynamicComboBoxModel connectionsModel;

    private JLabel nameLabel;

    private JTextField nameText;

    private JTabbedPane tabbedPane;

    private SimpleTextArea textExceptionPanel;

    private SimpleTextArea descriptionPanel;

    private DatabaseConnection connection;

    private ActionContainer parent;

    private boolean editing;

    private DefaultDatabaseException exception;

    private JButton ok;

    private JButton cancel;

    public static final String CREATE_TITLE = "Create Exception";

    public CreateExceptionPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseException exception) {
        parent = dialog;
        connection = dc;
        this.exception = exception;
        editing = exception != null;
        init();
        if (editing)
            init_edited();
    }

    public CreateExceptionPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    void init() {
        nameText = new JTextField();
        descriptionPanel = new SimpleTextArea();
        textExceptionPanel = new SimpleTextArea();
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(500,200));
        nameLabel = new JLabel("Name");
        connectionLabel = new JLabel("Connection");
        ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                generateScript();
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
        gbcLabel.anchor = GridBagConstraints.EAST;
        gbc.anchor = GridBagConstraints.WEST;
        gbcLabel.ipadx = 0;
        gbc.ipadx = 0;
        gbcLabel.ipady = 0;
        gbc.ipady = 0;
        gbcLabel.insets = new Insets(5, 5, 5, 5);
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setLayout(new GridBagLayout());
        this.add(connectionLabel,gbcLabel);
        this.add(connectionsCombo,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        this.add(nameLabel,gbcLabel);
        this.add(nameText,gbc);
        gbc.gridx=0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.gridwidth =2;
        this.add(tabbedPane,gbc);
        tabbedPane.add("Text Exception",textExceptionPanel);
        tabbedPane.add("Description",descriptionPanel);
        gbc.gridy++;
        gbc.gridx++;
        gbc.gridwidth = 1;
        gbc.weightx = 0.2;
        gbc.weighty =0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbcLabel.gridy = gbc.gridy;
        gbcLabel.gridx = 0;
        gbcLabel.weightx = 0.2;
        gbcLabel.fill = GridBagConstraints.HORIZONTAL;
        this.add(ok,gbcLabel);
        this.add(cancel,gbc);

    }

    void init_edited() {
    }

    void generateScript() {
        String query = "CREATE EXCEPTION "+nameText.getText()+" '"+textExceptionPanel.getTextAreaComponent().getText()+"'";
        ExecuteQueryDialog eqd = new ExecuteQueryDialog(CREATE_TITLE, query, connection, true,"^");
        eqd.display();
        if (eqd.getCommit()) {
            parent.finished();
        }
    }


}
