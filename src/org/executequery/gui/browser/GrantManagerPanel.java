/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.executequery.gui.browser;

import biz.redsoft.IFBUser;
import biz.redsoft.IFBUserManager;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.managment.grantmanager.PrivilegesTablePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mikhan808
 */
public class GrantManagerPanel extends JPanel implements TabView {

    // --- constants ---

    public static final String FRAME_ICON = "grant_manager_16.png";
    public static final String TITLE = Bundles.get(GrantManagerPanel.class, "GrantManager");

    public static final int NO_GRANT_TO_ALL_OBJECTS = 0;
    public static final int NO_ALL_GRANTS_TO_OBJECT = NO_GRANT_TO_ALL_OBJECTS + 1;
    public static final int NO_ALL_GRANTS_TO_ALL_OBJECTS = NO_ALL_GRANTS_TO_OBJECT + 1;
    public static final int GRANT_TO_ALL_OBJECTS = NO_ALL_GRANTS_TO_ALL_OBJECTS + 1;
    public static final int ALL_GRANTS_TO_OBJECT = GRANT_TO_ALL_OBJECTS + 1;
    public static final int ALL_GRANTS_TO_ALL_OBJECTS = ALL_GRANTS_TO_OBJECT + 1;
    public static final int GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION = ALL_GRANTS_TO_ALL_OBJECTS + 1;
    public static final int ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION = GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION + 1;
    public static final int ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION = ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION + 1;
    public static final int CREATE_TABLE = ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION + 1;

    // --- GUI components ---

    private PrivilegesTablePanel tablePanel;
    private JComboBox<String> userTypeBox;
    private JComboBox<DatabaseConnection> databaseBox;

    // ---

    public DatabaseConnection connection;
    private JList<NamedObject> userList;
    private DefaultListModel<NamedObject> userListModel;

    boolean connected;
    boolean databaseBoxEnabled;
    boolean elementsEnabled;

    // ---

    public GrantManagerPanel() {

        elementsEnabled = true;
        databaseBoxEnabled = true;

        init();
        setElementsEnabled(true);
        loadConnections();
    }

    private void init() {

        // --- init ---

        databaseBox = new JComboBox<>();
        databaseBox.addActionListener(evt -> databaseBoxActionPerformed());

        userTypeBox = new JComboBox<>();
        userTypeBox.addActionListener(evt -> loadUserList());
        fillUserBox();

        userList = new JList<>();
        userList.addListSelectionListener(evt -> userListValueChanged());

        JScrollPane recipientsOfPrivilegesScroll = new JScrollPane();
        recipientsOfPrivilegesScroll.setViewportView(userList);
        tablePanel = new PrivilegesTablePanel(PrivilegesTablePanel.USER_OBJECTS, this);

        // --- arrange ---

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper()
                .setInsets(5, 5, 5, 5)
                .fillHorizontally()
                .anchorNorthWest();

        add(new JLabel(Bundles.getCommon("connection")), gbh.get());
        add(databaseBox, gbh.nextCol().get());
        add(new JLabel(bundleString("PrivilegesFor")), gbh.nextRowFirstCol().get());
        add(userTypeBox, gbh.nextCol().get());
        add(recipientsOfPrivilegesScroll, gbh.nextRowFirstCol().fillBoth().setWidth(2).spanY().get());
        add(tablePanel, gbh.setInsets(0, 0, 0, 0).setX(3).setY(0).spanX().spanY().get());
    }

    private void fillUserBox() {

        List<String> recipients = new ArrayList<>();
        recipients.add("Users");
        recipients.add("Roles");
        recipients.add("Views");
        recipients.add("Triggers");
        recipients.add("Procedures");

        if (databaseBox.getSelectedItem() != null) {
            if (((DatabaseConnection) (databaseBox.getSelectedItem())).getMajorServerVersion() >= 3) {
                recipients.add("Functions");
                recipients.add("Packages");
            }
        }

        userTypeBox.setModel(new DefaultComboBoxModel<>(bundleString(recipients)));
    }

    public void setElementsEnabled(boolean enable) {
        elementsEnabled = enable;
        databaseBox.setEnabled(enable);
        userTypeBox.setEnabled(enable);
        userList.setEnabled(enable);
    }

    private List<NamedObject> getUsersList() {

        List<NamedObject> users = new ArrayList<>();

        DatabaseConnection databaseConnection = (DatabaseConnection) databaseBox.getSelectedItem();
        if (databaseConnection == null)
            return users;

        if (databaseConnection.getMajorServerVersion() >= 3) {
            users.addAll(ConnectionsTreePanel
                    .getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(databaseConnection)
                    .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.USER])
            );

        } else {

            IFBUserManager userManager = null;
            try {
                userManager = (IFBUserManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                        (databaseConnection).getDriverMajorVersion(),
                        ConnectionManager.getTemporaryConnection(databaseConnection).unwrap(Connection.class),
                        "FBUserManagerImpl"
                );

            } catch (SQLException e) {
                Log.error("Error get connection for getting users in grant manager: ", e);
            } catch (ClassNotFoundException e) {
                Log.error("Error get users in Grant Manager: ", e);
            }

            if (userManager == null)
                return users;

            try {
                Map<String, IFBUser> userMap = getUserManager(userManager, databaseConnection).getUsers();
                for (IFBUser user : userMap.values())
                    users.add(getCreateUser(user.getUserName()));

            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.toString());
            }
        }

        return users;
    }

    private DefaultDatabaseUser getCreateUser(String name) {
        return new DefaultDatabaseUser(
                new DefaultDatabaseMetaTag(
                        ConnectionsTreePanel
                                .getPanelFromBrowser()
                                .getDefaultDatabaseHostFromConnection(connection),
                        null,
                        null,
                        NamedObject.META_TYPES[NamedObject.USER]
                ),
                name
        );
    }

    private void userListValueChanged() {
        tablePanel.setDatabaseObject(userList.getSelectedValue());
        repaint();
    }

    @SuppressWarnings("DataFlowIssue")
    public void loadConnections() {

        boolean selected = databaseBox.getSelectedIndex() >= 0;
        databaseBoxEnabled = false;
        connected = false;

        setElementsEnabled(true);
        databaseBox.removeAllItems();

        DatabaseConnection item = selected ? databaseBox.getItemAt(databaseBox.getSelectedIndex()) : null;
        List<DatabaseConnection> connections = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();

        for (DatabaseConnection connection : connections) {
            if (connection.isConnected()) {

                connected = true;
                databaseBoxEnabled = true;
                databaseBox.addItem(connection);

                if (!selected) {
                    item = connection;
                    selected = true;
                }
            }
        }

        if (!connected) {
            GUIUtilities.displayErrorMessage(bundleString("message.notConnected"));
            GUIUtilities.closeTab(TITLE);
        } else
            databaseBox.setSelectedItem(item);
    }

    private IFBUserManager getUserManager(IFBUserManager userManager, DatabaseConnection dc) {

        userManager.setDatabase(dc.getSourceName());
        userManager.setHost(dc.getHost());
        userManager.setPort(dc.getPortInt());
        userManager.setUser(dc.getUserName());
        userManager.setPassword(dc.getUnencryptedPassword());

        return userManager;
    }

    private void getUsers() {
        for (NamedObject user : getUsersList())
            userListModel.addElement(user);
    }

    private void getRoles() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.ROLE]);
        tags.add(NamedObject.META_TYPES[NamedObject.SYSTEM_ROLE]);

        getUserList(tags);
    }

    private void getViews() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.VIEW]);

        getUserList(tags);
    }

    private void getTriggers() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.TRIGGER]);
        tags.add(NamedObject.META_TYPES[NamedObject.DATABASE_TRIGGER]);
        tags.add(NamedObject.META_TYPES[NamedObject.DDL_TRIGGER]);

        getUserList(tags);
    }

    private void getProcedures() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.PROCEDURE]);

        getUserList(tags);
    }

    private void getFunctions() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.FUNCTION]);

        getUserList(tags);
    }

    private void getPackages() {

        List<String> tags = new ArrayList<>();
        tags.add(NamedObject.META_TYPES[NamedObject.PACKAGE]);

        getUserList(tags);
    }

    private void getUserList(List<String> tags) {

        for (String tag : tags) {

            List<NamedObject> list = ConnectionsTreePanel
                    .getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(connection)
                    .getDatabaseObjectsForMetaTag(tag);

            for (NamedObject object : list)
                userListModel.addElement(object);
        }

        userListModel.addElement(getCreateUser("PUBLIC"));
    }

    private void loadUserList() {

        if (!connected)
            return;

        userListModel = new DefaultListModel<>();
        userList.setModel(userListModel);

        switch (userTypeBox.getSelectedIndex()) {
            case 0:
                getUsers();
                break;
            case 1:
                getRoles();
                break;
            case 2:
                getViews();
                break;
            case 3:
                getTriggers();
                break;
            case 4:
                getProcedures();
                break;
            case 5:
                getFunctions();
                break;
            case 6:
                getPackages();
                break;
            default:
                break;
        }

        if (!userListModel.isEmpty())
            userList.setSelectedIndex(0);
    }

    private void databaseBoxActionPerformed() {
        if (databaseBoxEnabled) {
            connection = (DatabaseConnection) databaseBox.getSelectedItem();
            fillUserBox();
            loadUserList();
        }
    }


    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    public String bundleString(String key) {
        return Bundles.get(GrantManagerPanel.class, key);
    }

    public String[] bundleString(List<String> keys) {
        return keys.stream().map(this::bundleString).toArray(String[]::new);
    }

}
