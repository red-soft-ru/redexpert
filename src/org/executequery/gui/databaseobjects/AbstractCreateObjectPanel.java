package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Vector;

public abstract class AbstractCreateObjectPanel extends JPanel {
    private JPanel topPanel;
    protected JPanel centralPanel;
    protected JTabbedPane tabbedPane;
    protected DatabaseConnection connection;
    protected JComboBox connectionsCombo;
    private DynamicComboBoxModel connectionsModel;
    protected boolean editing;
    protected ActionContainer parent;
    protected JTextField nameField;
    protected DefaultStatementExecutor sender;
    protected MetaDataValues metaData;
    private ConnectionsTreePanel treePanel;
    private TreePath currentPath;
    private boolean commit;

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        this(dc, dialog, databaseObject, null);
    }

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        parent = dialog;
        connection = dc;
        commit = false;
        initComponents();
        setDatabaseObject(databaseObject);
        if (params != null)
            setParameters(params);
        editing = databaseObject != null;
        init();
        if (editing) {
            try {
                initEdited();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUIUtilities.displayConfirmDialog(Bundles.getCommon("confirmation-request")) == JOptionPane.YES_OPTION) {
                    dialog.finished();
                }

            }
        };

        this.registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initComponents() {
        nameField = new JTextField();
        nameField.setText("NEW_" + getTypeObject());
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(700, 400));
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
            sender.setDatabaseConnection(connection);
            metaData.setDatabaseConnection(connection);
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        this.setLayout(new BorderLayout());
        sender = new DefaultStatementExecutor(connection, true);
        metaData = new MetaDataValues(connection, true);
        topPanel = new JPanel(new GridBagLayout());
        JLabel connLabel = new JLabel(Bundles.getCommon("connection"));
        topPanel.add(connLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        topPanel.add(connectionsCombo, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel nameLabel = new JLabel(Bundles.getCommon("name"));
        topPanel.add(nameLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        topPanel.add(nameField, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel = new JPanel();

        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(parent.isDialog());
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connection.isNamesToUpperCase())
                    nameField.setText(nameField.getText().toUpperCase());
                createObject();
            }
        });
        bottomButtonPanel.setOkButtonText("OK");
        bottomButtonPanel.setCancelButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUIUtilities.displayConfirmDialog(Bundles.getCommon("confirmation-request")) == JOptionPane.YES_OPTION) {
                    parent.finished();
                }
            }
        });
        bottomButtonPanel.setCancelButtonText(Bundles.getCommon("cancel.button"));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(topPanel, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                0, 0));
        panel.add(centralPanel, new GridBagConstraints(0, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0),
                0, 0));
        panel.add(tabbedPane, new GridBagConstraints(0, 2,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));

        this.add(panel, BorderLayout.CENTER);
        this.add(bottomButtonPanel, BorderLayout.SOUTH);

    }


    protected abstract void init();

    protected abstract void initEdited();

    public abstract void createObject();

    public abstract String getCreateTitle();

    public abstract String getEditTitle();

    public abstract String getTypeObject();

    public abstract void setDatabaseObject(Object databaseObject);

    public abstract void setParameters(Object[] params);

    public String getNameInQuotes() {
        return MiscUtils.wordInQuotes(nameField.getText());
    }

    public String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    public String bundlesString(String key) {
        return Bundles.get(AbstractCreateObjectPanel.class, key);
    }

    protected void displayExecuteQueryDialog(String query, String delimiter) {
        String titleDialog;
        if (editing)
            titleDialog = getEditTitle();
        else titleDialog = getCreateTitle();
        ExecuteQueryDialog eqd = new ExecuteQueryDialog(titleDialog, query, connection, true, delimiter);
        eqd.display();
        if (eqd.getCommit()) {
            commit = true;
            if (treePanel != null && currentPath != null) {
                DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
                if (node.getDatabaseObject() instanceof DefaultDatabaseMetaTag)
                    treePanel.reloadPath(currentPath);
                else
                    treePanel.reloadPath(currentPath.getParentPath());
            }
            parent.finished();
        }
    }

    public boolean isCommit() {
        return commit;
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

    public ConnectionsTreePanel getTreePanel() {
        return treePanel;
    }

    public void setTreePanel(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
    }

    public TreePath getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(TreePath currentPath) {
        this.currentPath = currentPath;
    }
}
