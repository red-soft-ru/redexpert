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

    public static final String TITLE = Bundles.get(GrantManagerPanel.class, "GrantManager");
    public static final String FRAME_ICON = "grant_manager_16.svg";
    public DatabaseConnection dbc;
    boolean connected;
    DefaultListModel userlistModel;
    boolean enabled_dBox;
    boolean enableElements;
    private JScrollPane recipientsOfPrivilegesScroll;
    private JList<NamedObject> userList;
    public static final int NO_GRANT_TO_ALL_OBJECTS = 0;
    public static final int NO_ALL_GRANTS_TO_OBJECT = 1;
    public static final int NO_ALL_GRANTS_TO_ALL_OBJECTS = 2;
    public static final int GRANT_TO_ALL_OBJECTS = 3;
    public static final int ALL_GRANTS_TO_OBJECT = 4;
    public static final int ALL_GRANTS_TO_ALL_OBJECTS = 5;
    public static final int GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION = 6;
    public static final int ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION = 7;
    public static final int ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION = 8;
    public static final int CREATE_TABLE = 9;
    private JComboBox<DatabaseConnection> databaseBox;
    private JComboBox<String> userTypeBox;
    private boolean isClose = false;

    private PrivilegesTablePanel tablePanel;
    @SuppressWarnings("unchecked")

    private final int buttonSize = 20;

    /**
     * Creates new form GrantManagerPanel
     */
    public GrantManagerPanel() {

        enableElements = true;
        enabled_dBox = false;
        initComponents();
        enabled_dBox = true;
        setEnableElements(true);
        load_connections();
    }

    private void initComponents() {

        databaseBox = new JComboBox<>();
        userTypeBox = new JComboBox<>();
        recipientsOfPrivilegesScroll = new JScrollPane();
        userList = new JList<>();

        databaseBox.addActionListener(evt -> databaseBoxActionPerformed());

        fillUserBox();
        userTypeBox.addActionListener(evt -> userBoxActionPerformed());

        userList.addListSelectionListener(evt -> userListValueChanged());
        recipientsOfPrivilegesScroll.setViewportView(userList);
        tablePanel = new PrivilegesTablePanel(PrivilegesTablePanel.USER_OBJECTS, this);



        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        setLayout(new GridBagLayout());
        gbh.defaults();


        //gbh.nextCol().fillHorizontally().setMaxWeightX().insertEmptyGap(buttonPanel);


        gbh.defaults();

        gbh.addLabelFieldPair(this, Bundles.getCommon("connection"), databaseBox, null);

        gbh.addLabelFieldPair(this, bundleString("PrivelegesFor"), userTypeBox, null, true, false);

        add(tablePanel, gbh.nextCol().fillBoth().spanX().setMaxWeightY().setHeight(2).get());

        add(recipientsOfPrivilegesScroll, gbh.nextRowFirstCol().setWidth(2).setHeight(1).fillBoth().setMaxWeightY().setMaxWeightX().get());

    }

    private void fillUserBox() {
        List<String> recipients = new ArrayList<>();
        recipients.add("Users");
        recipients.add("Roles");
        recipients.add("Views");
        recipients.add("Triggers");
        recipients.add("Procedures");
        if (databaseBox.getSelectedItem() != null)
            if (((DatabaseConnection) (databaseBox.getSelectedItem())).getMajorServerVersion() >= 3) {
                recipients.add("Functions");
                recipients.add("Packages");
            }

        userTypeBox.setModel(new DefaultComboBoxModel<>(bundleStrings(recipients)));
    }

    public void setEnableElements(boolean enable) {
        enableElements = enable;
        databaseBox.setEnabled(enable);

        userTypeBox.setEnabled(enable);
        userList.setEnabled(enable);
    }

    List<NamedObject> getUsers() {
        List<NamedObject> users = new ArrayList<>();
        DatabaseConnection databaseConnection = (DatabaseConnection) databaseBox.getSelectedItem();
        if (databaseConnection.getMajorServerVersion() >= 3)
            users.addAll(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.USER]));
        else {
            Connection connection = null;
            try {
                connection = ConnectionManager.getTemporaryConnection(databaseConnection).unwrap(Connection.class);
            } catch (SQLException e) {
                Log.error("error get connection for getting users in grant manager:", e);
            }

            IFBUserManager userManager = null;
            try {
                userManager = (IFBUserManager) DynamicLibraryLoader.loadingObjectFromClassLoader((databaseConnection).getDriverMajorVersion(), connection, "FBUserManagerImpl");
            } catch (ClassNotFoundException e) {
                Log.error("Error get users in Grant Manager:", e);
            }
            if (userManager != null) {
                userManager = getUserManager(userManager, databaseConnection);
                Map<String, IFBUser> userMap;
                try {
                    userMap = userManager.getUsers();
                    for (IFBUser u : userMap.values()) {
                        users.add(createUser(u.getUserName()));
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    GUIUtilities.displayErrorMessage(e.toString());
                }
            }
        }
        users.add(createUser("PUBLIC"));
        return users;
    }

    DefaultDatabaseUser createUser(String name) {
        return new DefaultDatabaseUser(new DefaultDatabaseMetaTag(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dbc), null, null, NamedObject.META_TYPES[NamedObject.USER]), name);
    }

    List<NamedObject> getRelationsFromType(int type) {
        if (type == NamedObject.USER) {
            return getUsers();
        } else
            return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dbc).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
    }

    private void userBoxActionPerformed() {
        load_userList();
    }

    private void cancelButtonActionPerformed() {
        setEnableElements(true);
    }


    private void userListValueChanged() {
        tablePanel.setDatabaseObject(userList.getSelectedValue());
        repaint();
        /*
        if (userlistModel.size() > 0)
            if (userList.getSelectedValue() != null) {
                act = CREATE_TABLE;
                execute_thread();
            }*/
    }



    public void load_connections() {
        boolean selected = databaseBox.getSelectedIndex() >= 0;
        enabled_dBox = false;
        setEnableElements(true);
        DatabaseConnection item = null;
        if (selected)
            item = databaseBox.getItemAt(databaseBox.getSelectedIndex());
        databaseBox.removeAllItems();
        List<DatabaseConnection> cons;
        cons = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        connected = false;
        for (int i = 0; i < cons.size(); i++) {
            if (cons.get(i).isConnected()) {
                connected = true;
                databaseBox.addItem(cons.get(i));
                enabled_dBox = true;
                if (!selected) {
                    item = cons.get(i);
                    selected = true;
                }
            }
        }
        if (!connected) {
            GUIUtilities.displayErrorMessage(bundleString("message.notConnected"));
            GUIUtilities.closeTab(TITLE);
        } else {
            databaseBox.setSelectedItem(item);
        }
    }

    IFBUserManager getUserManager(IFBUserManager userManager, DatabaseConnection dc) {
        userManager.setDatabase(dc.getSourceName());
        userManager.setHost(dc.getHost());
        userManager.setPort(dc.getPortInt());
        userManager.setUser(dc.getUserName());
        userManager.setPassword(dc.getUnencryptedPassword());
        return userManager;
    }


    void get_roles() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.ROLE]);
        metatags.add(NamedObject.META_TYPES[NamedObject.SYSTEM_ROLE]);
        get_user_list(metatags);
    }

    void get_views_for_userlist() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.VIEW]);
        get_user_list(metatags);
    }

    void get_user_list(List<String> metatags) {
        for (String metatag : metatags) {
            List<NamedObject> list = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dbc).getDatabaseObjectsForMetaTag(metatag);
            for (NamedObject object : list) {
                userlistModel.addElement(object);
            }
        }
    }

    void get_triggers_for_userlist() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.TRIGGER]);
        metatags.add(NamedObject.META_TYPES[NamedObject.DATABASE_TRIGGER]);
        metatags.add(NamedObject.META_TYPES[NamedObject.DDL_TRIGGER]);
        get_user_list(metatags);
    }

    void get_procedures_for_userlist() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.PROCEDURE]);
        get_user_list(metatags);
    }

    void get_functions_for_userlist() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.FUNCTION]);
        get_user_list(metatags);
    }

    void get_packages_for_userlist() {
        List<String> metatags = new ArrayList<>();
        metatags.add(NamedObject.META_TYPES[NamedObject.PACKAGE]);
        get_user_list(metatags);
    }

    int getTypeFromUserBoxIndex(int indUserBox) {
        switch (indUserBox) {
            case 0:
                return NamedObject.USER;
            case 1:
                return NamedObject.ROLE;
            case 2:
                return NamedObject.VIEW;
            case 3:
                return NamedObject.TRIGGER;
            case 4:
                return NamedObject.PROCEDURE;
            case 5:
                return NamedObject.FUNCTION;
            case 6:
                return NamedObject.PACKAGE;
            default:
                return -1;
        }
    }

    void load_userList() {
        if (connected) {
            userlistModel = new DefaultListModel();
            userList.setModel(userlistModel);
            int ind_userBox = userTypeBox.getSelectedIndex();
            switch (ind_userBox) {
                case 0:
                    get_users();
                    break;
                case 1:
                    get_roles();
                    break;
                case 2:
                    get_views_for_userlist();
                    break;
                case 3:
                    get_triggers_for_userlist();
                    break;
                case 4:
                    get_procedures_for_userlist();
                    break;
                case 5:
                    get_functions_for_userlist();
                    break;
                case 6:
                    get_packages_for_userlist();
                    break;
                default:
                    break;
            }
            if (userlistModel.size() > 0)
                userList.setSelectedIndex(0);
        }
    }



    private void databaseBoxActionPerformed() {
        if (enabled_dBox) {
            dbc = (DatabaseConnection) databaseBox.getSelectedItem();
            fillUserBox();
            load_userList();
        }
    }



    void isClose() {
        if (isClose)
            if (GUIUtilities.displayConfirmDialog(bundleString("message.terminate-grant")) == 0) {
                setEnableElements(true);

            } else {
                GUIUtilities.addCentralPane(GrantManagerPanel.TITLE,
                        GrantManagerPanel.FRAME_ICON,
                        this,
                        null,
                        true);
            }
    }


    public String bundleString(String key) {
        return Bundles.get(GrantManagerPanel.class, key);
    }

    public String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++) {
            key[i] = bundleString(key[i]);
        }
        return key;
    }

    public String[] bundleStrings(List<String> keys) {
        String[] result = new String[keys.size()];
        for (int i = 0; i < keys.size(); i++) {
            result[i] = bundleString(keys.get(i));
        }
        return result;
    }


    @Override
    public boolean tabViewClosing() {
        isClose = true;
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



    public String[] bundleStringsOf(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys.length > 0)
                keys[i] = bundleString(keys[i]);
        }
        return keys;
    }

    void get_users() {
        List<NamedObject> users = getUsers();
        for (NamedObject user : users)
            userlistModel.addElement(user);
    }



}
