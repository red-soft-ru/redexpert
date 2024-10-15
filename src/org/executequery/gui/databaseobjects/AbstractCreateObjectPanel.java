package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.BrowserTreePopupMenu;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.DependenciesPanel;
import org.executequery.gui.browser.nodes.DatabaseHostNode;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.nodes.tableNode.TableFolderNode;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.gui.table.InsertColumnPanel;
import org.executequery.gui.text.SimpleCommentPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.ConnectionsComboBox;
import org.underworldlabs.swing.UpperFilter;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.Printable;

public abstract class AbstractCreateObjectPanel extends AbstractFormObjectViewPanel {

    private boolean commit;
    private TreePath currentPath;
    private ConnectionsTreePanel treePanel;

    protected GridBagHelper topGbh;
    protected ActionContainer parent;

    protected JPanel topPanel;
    protected JPanel centralPanel;
    protected JTabbedPane tabbedPane;

    protected JButton actionButton;
    protected JButton submitButton;
    protected JButton cancelButton;
    protected JTextField nameField;
    protected ConnectionsComboBox connectionsCombo;
    protected SimpleCommentPanel simpleCommentPanel;

    protected boolean edited;
    protected boolean editing;
    protected String firstQuery;
    protected DatabaseConnection connection;
    protected DefaultStatementExecutor sender;

    protected abstract void init();

    protected abstract void initEdited();

    protected abstract String generateQuery();

    protected abstract void reset();

    public abstract void createObject();

    public abstract String getCreateTitle();

    public abstract String getEditTitle();

    public abstract String getTypeObject();

    public abstract int getType();

    public abstract void setParameters(Object[] params);

    public abstract void setDatabaseObject(Object databaseObject);

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        this(dc, dialog, databaseObject, null);
    }

    public AbstractCreateObjectPanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {

        edited = false;
        commit = false;
        parent = dialog;
        connection = dc;
        editing = databaseObject != null;

        initBase();
        arrangeBase();
        setDatabaseObject(databaseObject);
        if (params != null)
            setParameters(params);

        init();
        if (editing)
            initEdited();

        treePanel = ConnectionsTreePanel.getPanelFromBrowser();
        this.registerKeyboardAction(
                e -> closeDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        firstQuery = generateQuery();
    }

    private void initBase() {

        sender = new DefaultStatementExecutor(connection, true);

        // --- connections comboBox ---

        connectionsCombo = WidgetFactory.createConnectionComboBox("connectionsCombo", true);
        connectionsCombo.addItemListener(this::connectionChanged);
        connectionsCombo.setEnabled(parent != null);

        if (connection == null)
            connection = getSelectedConnection();
        else
            connectionsCombo.setSelectedItem(connection);

        // --- name textField ---

        nameField = WidgetFactory.createTextField("nameField");
        nameField.setMinimumSize(nameField.getPreferredSize());

        if (connection != null) {
            nameField.setText(SQLUtils.generateNameForDBObject(getTypeObject(), connection));
            if (connection.isNamesToUpperCase() && !editing) {
                PlainDocument doc = (PlainDocument) nameField.getDocument();
                doc.setDocumentFilter(new UpperFilter());
            }
        }

        // --- panels ---

        tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(700, 400));

        centralPanel = new JPanel();
        topPanel = new JPanel(new GridBagLayout());

        // --- buttons ---

        actionButton = WidgetFactory.createButton("actionButton", "action");
        actionButton.setVisible(false);

        submitButton = WidgetFactory.createButton("submitButton", Bundles.getCommon("apply.button"));
        submitButton.addActionListener(e -> submitClick());

        cancelButton = WidgetFactory.createButton("cancelButton", Bundles.getCommon("cancel.button"));
        cancelButton.addActionListener(e -> cancelClick());

        // --- parent ---

        if (parent != null) {
            ((BaseDialog) parent).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ((BaseDialog) parent).addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeDialog();
                }
            });
        }
    }

    private void arrangeBase() {
        GridBagHelper gbh;

        // --- top panel ---

        topGbh = new GridBagHelper().setInsets(5, 5, 5, 5).fillHorizontally().anchorNorthWest();

        topPanel.add(new JLabel(Bundles.getCommon("connection")), topGbh.setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(connectionsCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(Bundles.getCommon("name")), topGbh.nextCol().setMinWeightX().topGap(4).rightGap(0).get());
        topPanel.add(nameField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());

        // --- button panel ---

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 0, 0, 5).fillHorizontally().anchorNorthEast();
        buttonPanel.add(new JPanel(), gbh.setMaxWeightX().get());
        buttonPanel.add(actionButton, gbh.nextCol().setMinWeightX().fillNone().get());
        buttonPanel.add(submitButton, gbh.nextCol().get());
        buttonPanel.add(cancelButton, gbh.nextCol().rightGap(5).get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setDefaultsStatic().fullDefaults();
        mainPanel.add(topPanel, gbh.setMaxWeightX().fillHorizontally().get());
        mainPanel.add(centralPanel, gbh.nextRowFirstCol().get());
        mainPanel.add(tabbedPane, gbh.nextRowFirstCol().fillBoth().spanY().get());

        // --- base ---

        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
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

    public static AbstractCreateObjectPanel getEditPanelFromType(int type, DatabaseConnection dc, Object databaseObject) {
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
                return new CreatePackagePanel(dc, null, (DefaultDatabasePackage) databaseObject, false);

            case NamedObject.SYSTEM_PACKAGE:
                return new CreatePackagePanel(dc, null, (DefaultDatabasePackage) databaseObject, true);

            case NamedObject.EXCEPTION:
                return new CreateExceptionPanel(dc, null, (DefaultDatabaseException) databaseObject);

            case NamedObject.UDF:
                return new CreateUDFPanel(dc, null, databaseObject);

            case NamedObject.USER:
                return new CreateDatabaseUserPanel(dc, null, (DefaultDatabaseUser) databaseObject);

            case NamedObject.ROLE:
            case NamedObject.SYSTEM_ROLE:
                return new CreateRolePanel(dc, null, databaseObject);

            case NamedObject.TABLESPACE:
                return new CreateTablespacePanel(dc, null, databaseObject);

            case NamedObject.JOB:
                return new CreateJobPanel(dc, null, databaseObject);

            case NamedObject.TABLE_COLUMN:
                return new InsertColumnPanel((DatabaseTable) ((DatabaseColumn) databaseObject).getParent(), null, (DatabaseColumn) databaseObject);
        }

        return null;
    }

    private void connectionChanged(ItemEvent e) {

        if (e.getStateChange() == ItemEvent.DESELECTED)
            return;

        connection = getSelectedConnection();
        sender.setDatabaseConnection(connection);
    }

    private void submitClick() {
        if (connection.isNamesToUpperCase() && !editing)
            nameField.setText(nameField.getText().toUpperCase());
        createObject();
    }

    private void cancelClick() {
        if (parent != null)
            closeDialog();
        else
            reset();
    }

    protected boolean checkChanges() {
        edited = !firstQuery.contentEquals(generateQuery());
        return edited;
    }

    public void closeDialog() {

        if (checkChanges()) {
            int confirmChanges = GUIUtilities.displayConfirmDialog(Bundles.getCommon("confirmation-request"));
            if (confirmChanges == JOptionPane.YES_OPTION)
                parent.finished();

        } else
            parent.finished();
    }

    public String getFormattedName() {
        return MiscUtils.getFormattedObject(nameField.getText(), getDatabaseConnection());
    }

    protected void displayExecuteQueryDialog(String query, String delimiter) {

        if (query != null && !query.isEmpty() && (!editing || !query.contentEquals(firstQuery))) {

            String titleDialog = editing ? getEditTitle() : getCreateTitle();
            ExecuteQueryDialog eqd = new ExecuteQueryDialog(titleDialog, query, connection, true, delimiter);
            eqd.display();

            if (eqd.getCommit()) {
                commit = true;

                if (treePanel != null && currentPath != null) {

                    DatabaseObjectNode node = (DatabaseObjectNode) currentPath.getLastPathComponent();
                    if (editing || node.getDatabaseObject() instanceof DefaultDatabaseMetaTag)
                        treePanel.reloadPath(currentPath);
                    else
                        treePanel.reloadPath(currentPath.getParentPath());

                    if (!(node instanceof TableFolderNode)) {
                        if (node.getMetaDataKey().contains(NamedObject.META_TYPES[NamedObject.TABLE])) {

                            ((DatabaseHostNode) node.getParent()).getChildObjects().stream()
                                    .filter(child -> child.getMetaDataKey().contains(NamedObject.META_TYPES[NamedObject.INDEX]))
                                    .findFirst()
                                    .ifPresent(child -> ConnectionsTreePanel.getPanelFromBrowser().reloadPath(child.getTreePath()));
                        }
                    }
                }

                if (parent != null)
                    parent.finished();
                else
                    firstQuery = generateQuery();
                reset();
            }

        } else if (parent != null)
            parent.finished();
    }

    protected int getDatabaseVersion() {
        try {
            return connection.getMajorServerVersion();

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
            return 0;
        }
    }

    public SimpleCommentPanel getSimpleCommentPanel() {
        return simpleCommentPanel;
    }

    public void setSimpleCommentPanel(SimpleCommentPanel simpleCommentPanel) {
        this.simpleCommentPanel = simpleCommentPanel;
    }

    public boolean isCommit() {
        return commit;
    }

    public ConnectionsTreePanel getTreePanel() {
        return treePanel;
    }

    public void setTreePanel(ConnectionsTreePanel treePanel) {
        this.treePanel = treePanel;
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

    protected void hideButtons() {
        editButton.setVisible(false);
        actionButton.setVisible(false);
        submitButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    protected DatabaseConnection getSelectedConnection() {
        return connectionsCombo.getSelectedConnection();
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

    @Override
    public String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    public String bundleStaticString(String key) {
        return Bundles.get(AbstractCreateObjectPanel.class, key);
    }

}
