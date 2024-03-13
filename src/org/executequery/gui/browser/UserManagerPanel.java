/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.executequery.gui.browser;

import biz.redsoft.IFBUser;
import biz.redsoft.IFBUserManager;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.components.table.RowHeaderRenderer;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultConnectionBuilder;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseRole;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.databaseobjects.AbstractCreateUserPanel;
import org.executequery.gui.databaseobjects.CreateDatabaseUserPanel;
import org.executequery.gui.databaseobjects.CreateFbUserPanel;
import org.executequery.gui.databaseobjects.CreateRolePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.Repository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * @author mikhan808
 */
public class UserManagerPanel extends JPanel implements Runnable {

    public static final String TITLE = Bundles.get(UserManagerPanel.class, "UserManager");
    public static final String FRAME_ICON = "user_manager_16.png";

    private static final Icon GRANT_ROLE_ICON = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
    private static final Icon REVOKE_ROLE_ICON = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
    private static final Icon GRANT_ADMIN_ROLE_ICON = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);

    private enum Action {
        REFRESH,
        GET_USERS,
        GET_ROLES,
        GET_MEMBERSHIP
    }

    // --- GUI components ---

    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;

    private JButton addRoleButton;
    private JButton deleteRoleButton;

    private JButton grantRoleButton;
    private JButton revokeRoleButton;
    private JButton grandAdminRoleButton;

    private JButton refreshButton;
    private JButton cancelButton;

    private JTable usersTable;
    private JTable rolesTable;
    private JTable membershipTable;

    private JProgressBar progressBar;
    private JCheckBox roleToRoleCheck;
    private JComboBox<DatabaseConnection> databasesCombo;

    private JPanel usersPanel;
    private JPanel rolesPanel;
    private JPanel membershipPanel;
    private JTabbedPane tabbedPane;
    private JScrollPane membershipScrollPane;

    // ---

    private boolean execute;
    private boolean enableElements;

    private Action currentAction;
    private Connection connection;
    private ItemListener databaseComboListener;
    private List<DatabaseConnection> databaseConnectionList;

    private int version;
    private IFBUser fbUser;
    private IFBUserManager userManager;

    private List<Object> userList;
    private final Vector<String> roleNamesVector;
    private final Vector<UserRole> userNamesVector;

    public UserManagerPanel() {

        execute = false;
        userNamesVector = new Vector<>();
        roleNamesVector = new Vector<>();

        init();
        arrange();
        loadConnections();
    }

    private void init() {

        // --- comboBoxes ---

        //noinspection unchecked
        databasesCombo = WidgetFactory.createComboBox("databasesCombo");
        databaseComboListener = e -> databaseChanged();

        // --- tables ---

        membershipTable = WidgetFactory.createTable("membershipTable", new String[]{"Title 1", "Title 2", "Title 3", "Title 4"});
        membershipTable.setDefaultRenderer(Object.class, new BrowserTableCellRenderer());
        membershipTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() > 1)
                    changeMemberships();
            }
        });

        rolesTable = WidgetFactory.createTable("rolesTable", new String[]{"", ""});
        usersTable = WidgetFactory.createTable("usersTable", new String[]{"", "", "", ""});

        // --- buttons ---

        addUserButton = WidgetFactory.createButton("addUserButton", bundleString("Add"));
        addUserButton.addActionListener(e -> showCreateUserDialog());

        editUserButton = WidgetFactory.createButton("editUserButton", bundleString("Edit"));
        editUserButton.addActionListener(e -> showEditUserDialog());

        deleteUserButton = WidgetFactory.createButton("deleteUserButton", bundleString("Delete"));
        deleteUserButton.addActionListener(e -> showDropUserDialog());

        addRoleButton = WidgetFactory.createButton("addRoleButton", bundleString("Add"));
        addRoleButton.addActionListener(e -> showCreateRoleDialog());

        deleteRoleButton = WidgetFactory.createButton("deleteRoleButton", bundleString("Delete"));
        deleteRoleButton.addActionListener(e -> showDropRoleDialog());

        grantRoleButton = WidgetFactory.createButton(
                "grantRoleButton",
                "GRANT",
                GRANT_ROLE_ICON,
                "GRANT ROLE"
        );
        grantRoleButton.addActionListener(e -> grantRole());

        grandAdminRoleButton = WidgetFactory.createButton(
                "grandAdminRoleButton",
                "ADMIN",
                GRANT_ADMIN_ROLE_ICON,
                "GRANT ROLE WITH ADMIN OPTION"
        );
        grandAdminRoleButton.addActionListener(e -> grandAdminRole());

        revokeRoleButton = WidgetFactory.createButton(
                "revokeRoleButton",
                "REVOKE",
                REVOKE_ROLE_ICON,
                "REVOKE ROLE"
        );
        revokeRoleButton.addActionListener(e -> revokeRole());

        refreshButton = WidgetFactory.createButton("refreshButton", bundleString("Refresh"));
        refreshButton.addActionListener(e -> setRefreshAction());

        cancelButton = WidgetFactory.createButton("cancelButton", bundleString("cancelButton"));
        cancelButton.addActionListener(e -> setEnableElements(true));

        // --- panels ---

        usersPanel = new JPanel(new GridBagLayout());
        rolesPanel = new JPanel(new GridBagLayout());
        membershipPanel = new JPanel(new GridBagLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(bundleString("Users"), usersPanel);
        tabbedPane.addTab(bundleString("Roles"), rolesPanel);
        tabbedPane.addTab(bundleString("Membership"), membershipPanel);
        tabbedPane.getAccessibleContext().setAccessibleName(bundleString("Users"));

        membershipScrollPane = new JScrollPane(
                membershipTable,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        );

        // --- others ---

        roleToRoleCheck = WidgetFactory.createCheckBox("roleToRoleCheck", bundleString("RoleRole"));
        roleToRoleCheck.addItemListener(e -> setRefreshAction());
        roleToRoleCheck.setSelected(false);

        progressBar = WidgetFactory.createProgressBar("progressBar");
    }

    private void arrange() {

        GridBagHelper gbh;

        // --- scroll panes ---

        JScrollPane usersTableScrollPane = new JScrollPane();
        usersTableScrollPane.setViewportView(usersTable);

        JScrollPane rolesTableScrollPane = new JScrollPane();
        rolesTableScrollPane.setViewportView(rolesTable);

        // --- connection panel ---

        JPanel connectionPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillNone();
        connectionPanel.add(new JLabel(bundleString("database")), gbh.setMinWeightX().get());
        connectionPanel.add(databasesCombo, gbh.nextCol().setMaxWeightX().fillHorizontally().get());
        connectionPanel.add(refreshButton, gbh.nextCol().setMinWeightX().get());

        // --- progress panel ---

        JPanel progressPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        progressPanel.add(progressBar, gbh.setMaxWeightX().get());
        progressPanel.add(cancelButton, gbh.nextCol().setMinWeightX().fillNone().get());

        // --- users panel ---

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        usersPanel.add(usersTableScrollPane, gbh.setMaxWeightX().spanY().get());
        usersPanel.add(addUserButton, gbh.nextCol().setHeight(1).fillHorizontally().setMinWeightY().setMinWeightX().get());
        usersPanel.add(editUserButton, gbh.nextRow().get());
        usersPanel.add(deleteUserButton, gbh.nextRow().get());
        usersPanel.add(new JPanel(), gbh.setMaxWeightY().fillBoth().spanY().get());

        // --- roles panel ---

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        rolesPanel.add(rolesTableScrollPane, gbh.setMaxWeightX().spanY().get());
        rolesPanel.add(addRoleButton, gbh.nextCol().setHeight(1).fillHorizontally().setMinWeightY().setMinWeightX().get());
        rolesPanel.add(deleteRoleButton, gbh.nextRow().get());
        rolesPanel.add(new JPanel(), gbh.setMaxWeightY().fillBoth().spanY().get());

        // --- membership panel ---

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        membershipPanel.add(membershipScrollPane, gbh.setMaxWeightX().spanY().get());
        membershipPanel.add(grantRoleButton, gbh.nextCol().setHeight(1).fillHorizontally().setMinWeightY().setMinWeightX().get());
        membershipPanel.add(grandAdminRoleButton, gbh.nextRow().get());
        membershipPanel.add(revokeRoleButton, gbh.nextRow().get());
        membershipPanel.add(roleToRoleCheck, gbh.nextRow().get());
        membershipPanel.add(new JPanel(), gbh.setMaxWeightY().fillBoth().spanY().get());

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();
        mainPanel.add(connectionPanel, gbh.setMinWeightY().spanX().get());
        mainPanel.add(tabbedPane, gbh.nextRowFirstCol().setMaxWeightY().fillBoth().get());
        mainPanel.add(progressPanel, gbh.nextRowFirstCol().setMinWeightY().spanX().get());

        // --- base ---

        setLayout(new GridBagLayout());
        add(mainPanel, new GridBagHelper().fillBoth().spanX().spanY().get());
        setVisible(true);
        setEnableElements(true);
    }

    private void setEnableElements(boolean enable) {
        enableElements = enable;
        addUserButton.setEnabled(enable);
        editUserButton.setEnabled(enable);
        deleteUserButton.setEnabled(enable);
        refreshButton.setEnabled(enable);
        addRoleButton.setEnabled(enable);
        deleteRoleButton.setEnabled(enable);
        grantRoleButton.setEnabled(enable);
        grandAdminRoleButton.setEnabled(enable);
        revokeRoleButton.setEnabled(enable);
        cancelButton.setEnabled(!enable);
        progressBar.setEnabled(!enable);
        progressBar.setValue(0);
    }

    // --- handlers ---

    private void databaseChanged() {

        if (databaseConnectionList.isEmpty()) {
            refreshNoConnection();
            return;
        }

        int selectedIndex = databasesCombo.getSelectedIndex();
        if (selectedIndex < 0)
            return;

        if (!getSelectedConnection().isConnected())
            ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(getSelectedConnection()).connect();

        try {
            DatabaseMetaData metadata = new DefaultDatabaseHost(getSelectedConnection()).getDatabaseMetaData();
            if (metadata != null)
                version = metadata.getDatabaseMajorVersion();

        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }

        refreshUserManager(version >= 3);
        setRefreshAction();
    }

    private void showCreateUserDialog() {

        BaseDialog dialog = new BaseDialog(CreateDatabaseUserPanel.CREATE_TITLE, false);
        AbstractCreateUserPanel panel = version >= 3 ?
                new CreateDatabaseUserPanel(getSelectedConnection(), dialog) :
                new CreateFbUserPanel(getSelectedConnection(), dialog, fbUser, this, false);

        showDialog(dialog, panel);
    }

    private void showEditUserDialog() {

        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        BaseDialog dialog = new BaseDialog(CreateDatabaseUserPanel.EDIT_TITLE, false);
        AbstractCreateUserPanel panel = version >= 3 ?
                new CreateDatabaseUserPanel(getSelectedConnection(), dialog, (DefaultDatabaseUser) userList.get(selectedRow)) :
                new CreateFbUserPanel(getSelectedConnection(), dialog, (IFBUser) userList.get(selectedRow), this, true);

        showDialog(dialog, panel);
    }

    private void showDropUserDialog() {

        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        if (version >= 3) {
            ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog(
                    "Dropping object",
                    ((DefaultDatabaseUser) userList.get(selectedRow)).getDropSQL(),
                    getSelectedConnection(),
                    true
            );
            executeQueryDialog.display();

        } else {
            try {
                userManager.delete((IFBUser) userList.get(selectedRow));
            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }

        setRefreshAction();
    }

    private void showCreateRoleDialog() {
        BaseDialog dialog = new BaseDialog(CreateRolePanel.TITLE, true);
        CreateRolePanel panel = new CreateRolePanel(getSelectedConnection(), dialog, null);
        showDialog(dialog, panel);
    }

    private void showDropRoleDialog() {

        int selectedRow = rolesTable.getSelectedRow();
        if (selectedRow < 0)
            return;

        String roleName = (String) rolesTable.getModel().getValueAt(selectedRow, 0);
        String query = SQLUtils.generateDefaultDropQuery("ROLE", roleName, getSelectedConnection());
        ExecuteQueryDialog executeQueryDialog = new ExecuteQueryDialog("Dropping object", query, getSelectedConnection(), true);

        executeQueryDialog.display();
        setRefreshAction();
    }

    private void grantRole() {

        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();

        if (enableElements && col >= 0) {
            if (membershipTable.getValueAt(row, col).equals(GRANT_ADMIN_ROLE_ICON))
                revoke(row, col);
            grant(row, col);
        }
    }

    private void grandAdminRole() {

        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();

        if (enableElements && col >= 0)
            grantWithAdminOption(row, col);
    }

    private void revokeRole() {

        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();

        if (enableElements && col >= 0)
            revoke(row, col);
    }

    private void changeMemberships() {

        if (!enableElements)
            return;

        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (col < 0)
            return;

        if (membershipTable.getValueAt(row, col).equals(GRANT_ROLE_ICON))
            grantWithAdminOption(row, col);
        else if (membershipTable.getValueAt(row, col).equals(GRANT_ADMIN_ROLE_ICON))
            revoke(row, col);
        else
            grant(row, col);
    }

    // --- helpers ---

    private void refreshUserManager(boolean dropOnly) {

        fbUser = null;
        userManager = null;

        if (dropOnly || connection == null)
            return;

        try {
            DatabaseConnection databaseConnection = getSelectedConnection();
            int driverVersion = databaseConnection.getDriverMajorVersion();

            fbUser = (IFBUser) DynamicLibraryLoader.loadingObjectFromClassLoader(driverVersion, connection.unwrap(Connection.class), "FBUserImpl");
            userManager = (IFBUserManager) DynamicLibraryLoader.loadingObjectFromClassLoader(driverVersion, connection.unwrap(Connection.class), "FBUserManagerImpl");
            userManager.setDatabase(databaseConnection.getSourceName());
            userManager.setHost(databaseConnection.getHost());
            userManager.setPort(databaseConnection.getPortInt());
            userManager.setUser(databaseConnection.getUserName());
            userManager.setPassword(databaseConnection.getUnencryptedPassword());

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    private void grantWithAdminOption(int row, int col) {

        if (col < 0)
            return;

        String roleName = roleNamesVector.elementAt(col);
        String userName = userNamesVector.elementAt(row).getName();

        String type = "";
        if (userNamesVector.elementAt(row).isUser())
            type = "USER ";
        else if (version >= 3)
            type = "ROLE ";

        String query = "GRANT \"" + roleName + "\" " +
                "TO " + type + "\"" + userName + "\"" +
                "WITH ADMIN OPTION;";

        executeStatement(query);
        setGetMembershipAction();
    }

    private void grant(int row, int col) {

        if (col < 0)
            return;

        String roleName = roleNamesVector.elementAt(col);
        String userName = userNamesVector.elementAt(row).getName();

        String type = "";
        if (userNamesVector.elementAt(row).isUser())
            type = "USER ";
        else if (version >= 3)
            type = "ROLE ";

        String query = "GRANT \"" + roleName + "\" " +
                "TO " + type + "\"" + userName + "\";";

        executeStatement(query);
        setGetMembershipAction();
    }

    private void revoke(int row, int col) {

        if (col < 0)
            return;

        String roleName = roleNamesVector.elementAt(col);
        String userName = userNamesVector.elementAt(row).getName();

        String type = "";
        if (userNamesVector.elementAt(row).isUser())
            type = "USER ";
        else if (version >= 3)
            type = "ROLE ";

        String query = "REVOKE \"" + roleName + "\" " +
                "FROM " + type + "\"" + userName + "\";";

        executeStatement(query);
        setGetMembershipAction();
    }

    private void loadConnections() {

        execute = false;
        connection = null;
        enableElements = true;
        databasesCombo.removeAllItems();
        databasesCombo.removeItemListener(databaseComboListener);

        Repository repo = RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
        if (repo == null)
            return;

        databaseConnectionList = ((DatabaseConnectionRepository) repo).findAll();

        boolean selected = false;
        for (DatabaseConnection dc : databaseConnectionList) {
            databasesCombo.addItem(dc);
            if (dc.isConnected() && !selected) {
                execute = true;
                databasesCombo.setSelectedItem(dc);
                selected = true;
            }
        }

        if (!execute) {
            if (databasesCombo.getItemCount() == 0)
                databasesCombo.addItem(null);

            databasesCombo.setSelectedIndex(0);
            execute = true;
        }

        databasesCombo.addItemListener(databaseComboListener);
        if (connection == null)
            databaseChanged();
    }

    private void showDialog(BaseDialog dialog, JPanel panel) {

        try {
            GUIUtilities.showWaitCursor();

            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    setRefreshAction();
                    super.windowClosed(e);
                }
            });
            dialog.display();

        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    private void refreshUsers() {

        userNamesVector.clear();
        usersTable.setModel(new RoleTableModel(
                new Object[][]{},
                bundleStrings(new String[]{
                        "UserName", "FirstName", "MiddleName", "LastName"
                })
        ));

        for (Object userObject : getRefreshUserList()) {

            String shortName;
            String firstName;
            String middleName;
            String lastName;

            if (version >= 3) {
                DefaultDatabaseUser user = (DefaultDatabaseUser) userObject;
                user.loadData();

                shortName = user.getShortName().trim();
                firstName = user.getFirstName();
                middleName = user.getMiddleName();
                lastName = user.getLastName();

            } else {
                IFBUser user = (IFBUser) userObject;

                shortName = user.getUserName().trim();
                firstName = user.getFirstName();
                middleName = user.getMiddleName();
                lastName = user.getLastName();
            }

            addUser(shortName, firstName, middleName, lastName);
        }
    }

    private void refreshRoles() {

        List<NamedObject> customRoles = ConnectionsTreePanel.getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(getSelectedConnection())
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.ROLE]);

        List<NamedObject> systemRoles = ConnectionsTreePanel.getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(getSelectedConnection())
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.SYSTEM_ROLE]);

        List<DefaultDatabaseRole> roleList = new ArrayList<>();
        if (customRoles != null) {
            roleList.addAll(customRoles.stream()
                    .map(obj -> (DefaultDatabaseRole) obj)
                    .collect(Collectors.toList())
            );
        }
        if (systemRoles != null) {
            roleList.addAll(systemRoles.stream()
                    .map(obj -> (DefaultDatabaseRole) obj)
                    .collect(Collectors.toList())
            );
        }

        rolesTable.setModel(new RoleTableModel(
                new Object[][]{},
                bundleStrings(new String[]{
                        "RoleName", "Owner"
                })
        ));

        roleNamesVector.clear();
        for (DefaultDatabaseRole role : roleList) {

            roleNamesVector.add(role.getName());
            if (roleToRoleCheck.isSelected())
                userNamesVector.add(new UserRole(role.getName(), false));

            ((RoleTableModel) rolesTable.getModel()).addRow(new Object[]{
                    role.getName(),
                    role.getOwner()
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshMembership() {

        String query = "SELECT DISTINCT\n" +
                "RDB$PRIVILEGE,\n" +
                "RDB$GRANT_OPTION,\n" +
                "RDB$RELATION_NAME\n" +
                "FROM RDB$USER_PRIVILEGES\n" +
                "WHERE (RDB$USER='%s')\n" +
                "AND (RDB$OBJECT_TYPE=8 OR RDB$OBJECT_TYPE=13)\n" +
                "AND RDB$USER_TYPE=%s;";

        membershipTable.setModel(new RoleTableModel(
                new Object[][]{},
                roleNamesVector.toArray()
        ));

        refreshTabs();
        progressBar.setMaximum(userNamesVector.size());

        boolean first = true;
        for (int i = 0; i < userNamesVector.size() && !enableElements; i++) {
            progressBar.setValue(i);

            String userName = userNamesVector.elementAt(i).getName();
            String type = userNamesVector.elementAt(i).isUser() ? "8" : "13";

            try {

                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(query, userName, type));

                Vector<Object> roleData = new Vector<>();
                roleNamesVector.forEach(role -> roleData.add(REVOKE_ROLE_ICON));

                while (resultSet.next()) {

                    int roleIndex = roleNamesVector.indexOf(resultSet.getString(3).trim());
                    if (roleIndex < 0)
                        continue;

                    if (resultSet.getInt(2) == 0)
                        roleData.set(roleIndex, GRANT_ROLE_ICON);
                    else
                        roleData.set(roleIndex, GRANT_ADMIN_ROLE_ICON);
                }
                if (!statement.isClosed())
                    statement.close();

                ((RoleTableModel) membershipTable.getModel()).addRow(roleData);
                if (GUIUtilities.getCentralPane(TITLE) == null)
                    setEnableElements(true);

                if (first) {

                    int totalSize = 0;
                    for (int j = 0; j < roleNamesVector.size(); j++) {
                        int size = roleNamesVector.elementAt(j).length() * 8;
                        membershipTable.getColumn(roleNamesVector.elementAt(j)).setMinWidth(size);
                        totalSize += size;
                    }

                    JList<UserRole> rowHeader = new JList<>(userNamesVector);
                    rowHeader.setFixedCellWidth(150);
                    rowHeader.setFixedCellHeight(membershipTable.getRowHeight());
                    rowHeader.setCellRenderer(new MembershipListCellRenderer(membershipTable));

                    membershipScrollPane.setRowHeaderView(rowHeader);

                    int paneWidth = membershipScrollPane.getPreferredSize().width;
                    membershipTable.setAutoResizeMode(totalSize > paneWidth ?
                            JTable.AUTO_RESIZE_OFF :
                            JTable.AUTO_RESIZE_ALL_COLUMNS
                    );

                    first = false;
                }

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    public void refresh() {

        if (!databaseConnectionList.isEmpty()) {

            if (!getSelectedConnection().isConnected())
                new DefaultConnectionBuilder(getSelectedConnection()).connect();

            ConnectionsTreePanel.getPanelFromBrowser().getHostNode(getSelectedConnection()).getDatabaseObject().reset();
            connection = ConnectionManager.getTemporaryConnection(getSelectedConnection());

            if (tabbedPane.getTabCount() < 2) {
                tabbedPane.addTab(bundleString("Roles"), rolesPanel);
                tabbedPane.addTab(bundleString("Membership"), membershipPanel);
            }

            refreshUsers();
            refreshRoles();
            refreshMembership();
            refreshTabs();

        } else {
            refreshUsers();
            if (tabbedPane.getTabCount() > 1) {
                tabbedPane.remove(rolesPanel);
                tabbedPane.remove(membershipPanel);
            }
        }

        setEnableElements(true);
    }

    private List<Object> getRefreshUserList() {

        userList = new ArrayList<>();
        try {

            if (version >= 3) {
                userList.addAll(
                        ConnectionsTreePanel.getPanelFromBrowser()
                                .getDefaultDatabaseHostFromConnection(getSelectedConnection())
                                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.USER])
                );

            } else {
                if (userManager == null)
                    refreshUserManager(false);
                userList.addAll(userManager.getUsers().values());
            }

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }

        return userList;
    }

    private void addUser(String shortName, String firstName, String middleName, String lastName) {
        userNamesVector.add(new UserRole(shortName, true));
        ((RoleTableModel) usersTable.getModel()).addRow(new Object[]{
                shortName,
                firstName,
                middleName,
                lastName
        });
    }

    public void refreshNoConnection() {

        if (tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(rolesPanel);
            tabbedPane.remove(membershipPanel);
        }

        usersTable.setModel(new RoleTableModel(
                new Object[][]{},
                bundleStrings(new String[]{
                        "UserName", "FirstName", "MiddleName", "LastName"
                })
        ));
    }

    private void refreshTabs() {

        int selectedTabIndex = tabbedPane.getSelectedIndex();

        tabbedPane.setSelectedIndex(1);
        tabbedPane.setSelectedIndex(2);
        tabbedPane.setSelectedIndex(0);
        tabbedPane.setSelectedIndex(selectedTabIndex);
    }

    private void executeStatement(String query) {

        try {
            Statement statement = connection.createStatement();
            statement.execute(query);
            if (!statement.isClosed())
                statement.close();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
        }
    }

    public void editFbUser(IFBUser fbUser) {
        try {
            userManager.update(fbUser);
        } catch (SQLException | IOException e) {
            Log.error(e.getMessage(), e);
        }

        setRefreshAction();
    }

    public void addFbUser(IFBUser fbUser) {
        try {
            userManager.add(fbUser);
        } catch (SQLException | IOException e) {
            Log.error(e.getMessage(), e);
        }

        setRefreshAction();
    }

    private void executeThread() {
        if (enableElements) {
            setEnableElements(false);
            new Thread(this).start();
        }
    }

    private void setRefreshAction() {
        currentAction = Action.REFRESH;
        executeThread();
    }

    private void setGetMembershipAction() {
        currentAction = Action.GET_MEMBERSHIP;
        executeThread();
    }

    public DatabaseConnection getSelectedConnection() {
        return (DatabaseConnection) databasesCombo.getSelectedItem();
    }

    // --- runnable ---

    @Override
    public void run() {

        if (enableElements)
            return;

        if (connection == null)
            databaseChanged();

        switch (currentAction) {
            case REFRESH:
                refresh();
                break;
            case GET_MEMBERSHIP:
                refreshMembership();
                setEnableElements(true);
                break;
        }
    }

    // ---

    public String bundleString(String key) {
        return Bundles.get(UserManagerPanel.class, key);
    }

    private String[] bundleStrings(String[] keys) {
        for (int i = 0; i < keys.length; i++)
            keys[i] = bundleString(keys[i]);
        return keys;
    }

    public static class UserRole {

        private final String name;
        private final boolean isUser;

        public UserRole(String name, boolean isUser) {
            this.name = name;
            this.isUser = isUser;
        }

        public String getName() {
            return name;
        }

        public boolean isUser() {
            return isUser;
        }

    } // UserRole class

    private static class MembershipListCellRenderer extends RowHeaderRenderer {

        private final ImageIcon roleIcon = GUIUtilities.loadIcon("user_manager_16.png");
        private final ImageIcon userIcon = GUIUtilities.loadIcon("User16.png");

        public MembershipListCellRenderer(JTable table) {
            super(table);
        }

        @Override
        protected void setValue(Object value) {

            if (value.getClass().equals(String.class)) {
                setIcon(null);
                setText((String) value);
            }

            if (value.getClass().equals(UserManagerPanel.UserRole.class)) {
                UserManagerPanel.UserRole userRole = (UserManagerPanel.UserRole) value;
                setText(userRole.getName());
                setIcon(userRole.isUser() ? userIcon : roleIcon);
            }
        }

    } // MembershipListCellRenderer class

}
