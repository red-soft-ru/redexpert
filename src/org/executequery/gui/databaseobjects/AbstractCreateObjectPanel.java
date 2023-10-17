package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.*;
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
import org.executequery.gui.table.InsertColumnPanel;
import org.executequery.gui.text.SimpleCommentPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.UpperFilter;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.Printable;
import java.util.Vector;

public abstract class AbstractCreateObjectPanel extends AbstractFormObjectViewPanel {
    protected JPanel topPanel;
    protected JPanel centralPanel;
    protected JTabbedPane tabbedPane;
    protected DatabaseConnection connection;
    protected JComboBox connectionsCombo;
    private DynamicComboBoxModel connectionsModel;
    protected boolean editing;
    protected ActionContainer parent;
    protected JTextField nameField;
    protected DefaultStatementExecutor sender;
    private ConnectionsTreePanel treePanel;
    private TreePath currentPath;
    private boolean commit;
    protected boolean edited = false;
    protected String firstQuery;

    protected SimpleCommentPanel simpleCommentPanel;

    protected GridBagHelper topGbh;

    public static AbstractCreateObjectPanel getEditPanelFromType(int type, DatabaseConnection dc, Object databaseObject, Object[] params) {
        switch (type) {
            case NamedObject.DOMAIN:
            case NamedObject.SYSTEM_DOMAIN:
                return new CreateDomainPanel(dc, null, ((DatabaseObject) databaseObject).getName());
            case NamedObject.PROCEDURE:
                return new CreateProcedurePanel(dc, null, ((DatabaseObject) databaseObject).getName());
            case NamedObject.FUNCTION:
            case NamedObject.SYSTEM_FUNCTION:
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
            case NamedObject.ROLE:
            case NamedObject.SYSTEM_ROLE:
                return new CreateRolePanel(dc, null, databaseObject);
            case NamedObject.TABLESPACE:
                return new CreateTablespacePanel(dc, null, databaseObject);
            case NamedObject.JOB:
                return new CreateJobPanel(dc, null, databaseObject);
            case NamedObject.TABLE_COLUMN:
                return new InsertColumnPanel((DatabaseTable) ((DatabaseColumn) databaseObject).getParent(), null, (DatabaseColumn) databaseObject);
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
        if (connection != null) {
            nameField.setText(SQLUtils.generateNameForDBObject(getTypeObject(), connection));
            if (connection.isNamesToUpperCase() && !editing) {
                PlainDocument doc = (PlainDocument) nameField.getDocument();
                doc.setDocumentFilter(new UpperFilter());
            }
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
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        this.setLayout(new BorderLayout());
        topPanel = new JPanel(new GridBagLayout());
        topGbh = new GridBagHelper();
        topGbh.setDefaultsStatic().defaults();
        topGbh.addLabelFieldPair(topPanel, Bundles.getCommon("connection"), connectionsCombo, null, true, false);
        topGbh.addLabelFieldPair(topPanel, Bundles.getCommon("name"), nameField, null, false);
        centralPanel = new JPanel();

        BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(parent != null && parent.isDialog());
        bottomButtonPanel.setOkButtonAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connection.isNamesToUpperCase() && !editing)
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
        if (parent != null)
            panel.setBorder(BorderFactory.createEtchedBorder());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic();
        gbh.fullDefaults();
        panel.add(topPanel, gbh.setMaxWeightX().fillHorizontally().get());
        panel.add(centralPanel, gbh.nextRowFirstCol().get());
        panel.add(tabbedPane, gbh.nextRowFirstCol().fillBoth().spanY().get());

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
        return MiscUtils.getFormattedObject(nameField.getText(), getDatabaseConnection());
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
                else firstQuery = generateQuery();
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
        } catch (Exception e) {
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
        createSqlPanel.setSQLText(databaseObject.getCreateSQLText());
        tabbedPane.add(bundleStaticString("createSQL"), createSqlPanel);
    }

    protected void addCommentTab(DatabaseObject databaseObject) {
        simpleCommentPanel = new SimpleCommentPanel(databaseObject);
        tabbedPane.add(Bundles.getCommon("comment-field-label"), simpleCommentPanel.getCommentPanel());
    }

    @Override
    public void cleanup() {
        super.cleanup();
        currentPath = null;
        cleanupForSqlTextArea(this);
    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    @Override
    public String getLayoutName() {
        return getEditTitle();
    }

    protected void addLabelFieldPairToToolBar(JToolBar toolBar, String label, Component component) {
        toolBar.add(new JLabel(label));
        toolBar.addSeparator();
        toolBar.add(component);
    }


}
