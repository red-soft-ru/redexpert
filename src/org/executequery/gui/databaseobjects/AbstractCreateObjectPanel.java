package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.Vector;

public abstract class AbstractCreateObjectPanel extends JPanel {
    protected JPanel first_panel;
    protected JPanel main_panel;
    protected JTabbedPane tabbedPane;
    protected JButton okButton;
    protected JButton cancelButton;
    protected DatabaseConnection connection;
    protected JComboBox connectionsCombo;
    protected DynamicComboBoxModel connectionsModel;
    protected boolean editing;
    protected ActionContainer parent;
    protected JTextField nameField;
    protected DefaultStatementExecutor sender;
    protected JPanel okCancelPanel;

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject)
    {
        parent = dialog;
        connection = dc;
        initComponents();
        setDatabaseObject(databaseObject);
        init();
        editing = databaseObject!=null;
        if(editing)
            try {
                init_edited();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private void initComponents()
    {
        nameField = new JTextField();
        nameField.setText("NEW_"+getTypeObject());
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(700, 400));
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                create_object();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
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
                sender.setDatabaseConnection(connection);
            }
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        this.setLayout(new GridBagLayout());
        sender = new DefaultStatementExecutor(connection, true);
        first_panel = new JPanel(new GridBagLayout());
        JLabel connLabel = new JLabel("Connection");
        first_panel.add(connLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        first_panel.add(connectionsCombo, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel nameLabel = new JLabel("Name");
        first_panel.add(nameLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        first_panel.add(nameField, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        main_panel=new JPanel();
        okCancelPanel = new JPanel(new GridBagLayout());
        okCancelPanel.add(okButton, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        okCancelPanel.add(cancelButton, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        this.add(first_panel,new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                0, 0));
        this.add(main_panel,new GridBagConstraints(0, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                0, 0));
        this.add(tabbedPane,new GridBagConstraints(0, 2,
            1, 1, 1, 1,
            GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
            0, 0));
        this.add(okCancelPanel,new GridBagConstraints(0, 3,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                0, 0));

    }
    protected abstract void init();
    protected abstract void init_edited();
    public abstract void create_object();
    public abstract String getCreateTitle();
    public abstract String getEditTitle();
    public abstract String getTypeObject();
    public abstract void setDatabaseObject(Object databaseObject);
    public String bundleString(String key)
    {
        return Bundles.get(getClass(), key);
    }
    public String bundlesString(String key)
    {
        return Bundles.get(AbstractCreateObjectPanel.class, key);
    }
    protected void displayExecuteQueryDialog(String query,String delimiter)
    {
        String titleDialog;
        if(editing)
            titleDialog=getEditTitle();
        else titleDialog = getCreateTitle();
        ExecuteQueryDialog eqd = new ExecuteQueryDialog(titleDialog, query, connection, true,delimiter);
        eqd.display();
        if (eqd.getCommit())
            parent.finished();
    }

    protected int getDatabaseVersion() {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        try {
            return host.getDatabaseMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
