package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.MetaDataValues;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.BrowserTreePopupMenu;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.DependenciesPanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.Printable;
import java.sql.SQLException;
import java.util.Vector;

public abstract class AbstractCreateObjectPanel extends AbstractFormObjectViewPanel {
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
    protected boolean edited = false;
    protected String firstQuery;

    public static AbstractCreateObjectPanel getEditPanelFromType(int type, DatabaseConnection dc, Object databaseObject, Object[] params) {
        switch (type) {
            case NamedObject.DOMAIN:
                return new CreateDomainPanel(dc, null, ((DefaultDatabaseDomain) databaseObject));
            case NamedObject.PROCEDURE:
                return new CreateProcedurePanel(dc, null, ((DatabaseObject) databaseObject).getName());
            case NamedObject.FUNCTION:
                DefaultDatabaseFunction function = (DefaultDatabaseFunction) databaseObject;
                return new CreateFunctionPanel(dc, null, function.getName(), function);
            case NamedObject.TRIGGER:
            case NamedObject.DDL_TRIGGER:
            case NamedObject.DATABASE_TRIGGER:
                DefaultDatabaseTrigger trigger = (DefaultDatabaseTrigger) databaseObject;
                return new CreateTriggerPanel(dc, null, trigger, trigger.getIntTriggerType());
            case NamedObject.SEQUENCE:
                return new CreateGeneratorPanel(dc, null, (DefaultDatabaseSequence) databaseObject);
            case NamedObject.PACKAGE:
                return new CreatePackagePanel(dc, null, (DefaultDatabasePackage) databaseObject);
            case NamedObject.EXCEPTION:
                return new CreateExceptionPanel(dc, null, (DefaultDatabaseException) databaseObject);
            case NamedObject.UDF:
                return new CreateUDFPanel(dc, null, databaseObject);
            case NamedObject.USER:
                return new CreateUserPanel(dc, null, (DefaultDatabaseUser) databaseObject);
            case NamedObject.TABLESPACE:
                return new CreateTablespacePanel(dc, null, databaseObject);
            default:
                return null;
        }
    }

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        this(dc, dialog, databaseObject, null);
    }

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        parent = dialog;
        connection = dc;
        commit = false;
        editing = databaseObject != null;
        initComponents();
        setDatabaseObject(databaseObject);
        if (params != null)
            setParameters(params);
        init();
        if (editing) {
            try {
                initEdited();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        treePanel = ConnectionsTreePanel.getPanelFromBrowser();
        DatabaseObjectNode hostNode = ConnectionsTreePanel.getPanelFromBrowser().getHostNode(connection);

        for (DatabaseObjectNode metaTagNode : hostNode.getChildObjects()) {
            if (metaTagNode.getMetaDataKey().equals(getTypeObject())) {
                if (editing) {
                    for (DatabaseObjectNode node : metaTagNode.getChildObjects()) {
                        if (node.getDatabaseObject() == databaseObject) {
                            currentPath = node.getTreePath();
                            break;
                        }
                    }
                } else {
                    currentPath = metaTagNode.getTreePath();
                }
                break;
            }
        }

        ActionListener escListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeDialog();

            }
        };

        this.registerKeyboardAction(escListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        firstQuery = generateQuery();
    }

    private void initComponents() {
        nameField = new JFormattedTextField();
        nameField.setText(SQLUtils.generateNameForDBObject(getTypeObject(), connection));
        if (connection.isNamesToUpperCase() && !editing) {
            PlainDocument doc = (PlainDocument) nameField.getDocument();
            doc.setDocumentFilter(new UpperFilter());
        }
        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(700, 400));
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        sender = new DefaultStatementExecutor(connection, true);
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

        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(parent != null && parent.isDialog());
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
                cancelClick();
            }
        });
        bottomButtonPanel.setCancelButtonText(Bundles.getCommon("cancel.button"));
        bottomButtonPanel.setHelpButtonVisible(false);

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
        if (parent != null) {
            ((BaseDialog) parent).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ((BaseDialog) parent).addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeDialog();
                }
            });
        } else connectionsCombo.setEnabled(false);

    }

    protected void checkChanges() {
        String query = generateQuery();
        edited = !firstQuery.contentEquals(query);
    }

    public void cancelClick() {
        if (parent != null)
            closeDialog();
        else {
            reset();
        }
    }

    protected abstract void reset();

    public void closeDialog() {
        checkChanges();
        if (edited && GUIUtilities.displayConfirmDialog(Bundles.getCommon("confirmation-request")) == JOptionPane.YES_OPTION) {
            parent.finished();
        } else if (!edited)
            parent.finished();
    }


    protected abstract void init();

    protected abstract void initEdited();

    public abstract void createObject();

    public abstract String getCreateTitle();

    public abstract String getEditTitle();

    public abstract String getTypeObject();

    public abstract void setDatabaseObject(Object databaseObject);

    public abstract void setParameters(Object[] params);

    protected abstract String generateQuery();

    public String getFormattedName() {
        return MiscUtils.getFormattedObject(nameField.getText());
    }

    public String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    public String bundleStaticString(String key) {
        return Bundles.get(AbstractCreateObjectPanel.class, key);
    }

    protected void displayExecuteQueryDialog(String query, String delimiter) {
        if (query != null && !query.isEmpty() && (!editing || !query.contentEquals(firstQuery))) {
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
                    else if (editing) {
                        treePanel.reloadPath(currentPath);
                    } else treePanel.reloadPath(currentPath.getParentPath());
                }
                if (parent != null)
                    parent.finished();
            }
        } else if (parent != null) parent.finished();
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

    protected static String getCreateTitle(int type) {
        return Bundles.get(BrowserTreePopupMenu.class, "create", Bundles.get(BrowserTreePopupMenu.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
    }

    protected static String getEditTitle(int type) {
        return Bundles.get(BrowserTreePopupMenu.class, "edit", Bundles.get(BrowserTreePopupMenu.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
    }

    protected void addDependenciesTab(DatabaseObject databaseObject) {
        DependenciesPanel dependenciesPanel = new DependenciesPanel();
        dependenciesPanel.setDatabaseObject(databaseObject);
        tabbedPane.addTab(Bundles.getCommon("dependencies"), dependenciesPanel);
    }

    protected void addCreateSqlTab(DatabaseObject databaseObject) {
        SimpleSqlTextPanel createSqlPanel = new SimpleSqlTextPanel();
        createSqlPanel.getTextPane().setDatabaseConnection(connection);
        createSqlPanel.getTextPane().setEditable(false);
        createSqlPanel.setSQLText(databaseObject.getCreateFullSQLText());
        tabbedPane.add(bundleStaticString("createSQL"), createSqlPanel);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    @Override
    public String getLayoutName() {
        return getEditTitle();
    }

    class UpperFilter extends DocumentFilter {
        public void insertString(FilterBypass fb, int offset, String string,
                                 AttributeSet attr) throws BadLocationException {
            super.insertString(fb, offset, string.toUpperCase(), attr);
        }

        public void replace(FilterBypass fb, int offset, int length, String text,
                            AttributeSet attrs) throws BadLocationException {
            super.replace(fb, offset, length, text.toUpperCase(), attrs);
        }
    }

}
