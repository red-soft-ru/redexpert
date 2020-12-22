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
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.DefaultNumberTextField;
import org.executequery.gui.browser.managment.*;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author mikhan808
 */
public class UserManagerPanel extends JPanel {

    enum Action {
        REFRESH,
        GET_USERS,
        GET_ROLES,
        GET_MEMBERSHIP
    }

    public static final String TITLE = Bundles.get(UserManagerPanel.class, "UserManager");
    public static final String FRAME_ICON = "user_manager_16.png";
    public IFBUserManager userManager;
    public BrowserController controller;
    public IFBUser userAdd;
    boolean execute_w;
    boolean enableElements;
    Icon gr, no, adm;
    Connection con;
    List<DatabaseConnection> listConnections;
    Map<String, IFBUser> users;
    Vector<UserRole> user_names;
    Vector<String> role_names;
    ResultSet result;
    int version;
    Action act;
    private JButton addUserButton;
    private JButton addRoleButton;
    private JButton adminButton;
    private JLabel adminLabel;
    private JComboBox<DatabaseConnection> databaseBox;
    private JLabel databaseLabel;
    private JButton deleteUserButton;
    private JButton deleteRoleButton;
    private JButton editUserButton;
    private JButton grantButton;
    private JLabel grantLabel;
    private JPanel connectPanel;
    private JPanel interruptPanel;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JTabbedPane jTabbedPane1;
    private JPanel membershipPanel;
    private JTable membershipTable;
    private JButton no_grantButton;
    private JLabel no_grantLabel;
    private JButton refreshUsersButton;
    private JPanel rolesPanel;
    private JComboBox<String> serverBox;
    private JLabel serverLabel;
    private JPanel usersPanel;
    private JTable usersTable;
    private JTable rolesTable;
    private JProgressBar jProgressBar1;
    private JButton cancelButton;
    private JButton connectButton;
    private boolean useCustomServer;
    private JTextField portField;
    private JCheckBox roleRoleBox;

    /**
     * Creates new form UserManagerPanel
     */
    public UserManagerPanel() {
        gr = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        no = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adm = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        execute_w = false;
        user_names = new Vector<UserRole>();
        role_names = new Vector<String>();
        initComponents();
        setEnableElements(true);
        try {
            loadConnections();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadConnections() throws Exception {
        setEnableElements(true);
        con = null;
        initUserManager();
        execute_w = false;
        databaseBox.removeAllItems();
        listConnections = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        enableElements = true;
        boolean selected = false;
        for (DatabaseConnection dc : listConnections) {
            databaseBox.addItem(dc);
            if (dc.isConnected() && !selected) {
                execute_w = true;
                databaseBox.setSelectedItem(dc);
                selected = true;
            }
        }
        if (!execute_w) {
            if (databaseBox.getItemCount() == 0)
                databaseBox.addItem(null);
            databaseBox.setSelectedIndex(0);
            execute_w = true;

        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        connectPanel = new JPanel(new GridBagLayout());
        interruptPanel = new JPanel();
        databaseLabel = new JLabel();
        serverLabel = new JLabel();
        databaseBox = new JComboBox<>();
        serverBox = new JComboBox<>();
        jTabbedPane1 = new JTabbedPane();
        usersPanel = new JPanel();
        jScrollPane1 = new JScrollPane();
        jScrollPane2 = new JScrollPane();
        membershipTable = new JTable();
        jScrollPane3 = new JScrollPane(membershipTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        usersTable = new JTable();
        rolesTable = new JTable();
        addUserButton = new DefaultButton();
        addRoleButton = new DefaultButton();
        editUserButton = new DefaultButton();
        deleteUserButton = new DefaultButton();
        deleteRoleButton = new DefaultButton();
        refreshUsersButton = new DefaultButton();
        rolesPanel = new JPanel();
        membershipPanel = new JPanel();
        //membershipTable = new JTable();
        grantButton = new JButton();
        adminButton = new JButton();
        no_grantButton = new JButton();
        grantLabel = new JLabel();
        adminLabel = new JLabel();
        no_grantLabel = new JLabel();
        jProgressBar1 = new JProgressBar();
        cancelButton = new DefaultButton();
        connectButton = new DefaultButton();
        portField = new DefaultNumberTextField();
        roleRoleBox = new JCheckBox(bundleString("RoleRole"));
        portField.setText("3050");

        connectButton.setText(bundleString("connectButton"));
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrameLogin frameLogin;
                if (getSelectedDatabaseConnection() != null) {
                    frameLogin = new FrameLogin(UserManagerPanel.this,
                            getSelectedDatabaseConnection().getUserName(),
                            getSelectedDatabaseConnection().getUnencryptedPassword());
                } else {
                    frameLogin = new FrameLogin(UserManagerPanel.this,
                            "",
                            "");
                    frameLogin.setUseCustomServer(true);
                }
                frameLogin.setVisible(true);
                int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                frameLogin.setLocation(width / 2 - frameLogin.getWidth() / 2, height / 2 - frameLogin.getHeight() / 2);
            }
        });

        cancelButton.setText(bundleString("cancelButton"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        connectPanel.setName("upPanel"); // NOI18N

        databaseLabel.setText(bundleString("database"));

        serverLabel.setText(bundleString("server"));

        databaseBox.setEditable(true);
        databaseBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED)
                    databaseBoxActionPerformed(evt);
            }
        });

        serverBox.setEditable(true);

        interruptPanel.setLayout(new GridBagLayout());
        interruptPanel.add(jProgressBar1, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        interruptPanel.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        connectPanel.setBorder(BorderFactory.createEtchedBorder());
        connectPanel.add(databaseLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectPanel.add(databaseBox, new GridBagConstraints(1, 0,
                3, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        connectPanel.add(serverLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectPanel.add(serverBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        connectPanel.add(portField, new GridBagConstraints(2, 1,
                1, 1, 0.15, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        connectPanel.add(connectButton, new GridBagConstraints(3, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        jTabbedPane1.setToolTipText("");

        usersTable.setModel(new RoleTableModel(
                new Object[][]{},
                new String[]{
                        "User name", "First name", "Middle name", "Last name", "Active"
                }
        ));
        jScrollPane1.setViewportView(usersTable);
        rolesTable.setModel(new RoleTableModel(
                new Object[][]{},
                new String[]{
                        "Role name", "Owner"
                }
        ));
        jScrollPane2.setViewportView(rolesTable);

        addUserButton.setText(bundleString("Add"));
        addUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addUserButtonActionPerformed(evt);
            }
        });
        addRoleButton.setText(bundleString("Add"));
        addRoleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRoleButtonActionPerformed(evt);
            }
        });

        editUserButton.setText(bundleString("Edit"));
        editUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editUserButtonActionPerformed(evt);
            }
        });

        deleteUserButton.setText(bundleString("Delete"));
        deleteUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteUserButtonActionPerformed(evt);
            }
        });

        deleteRoleButton.setText(bundleString("Delete"));
        deleteRoleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    deleteRoleButtonActionPerformed(evt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        refreshUsersButton.setText(bundleString("Refresh"));
        refreshUsersButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refreshUserButtonActionPerformed(evt);
            }
        });


        GroupLayout usersPanelLayout = new GroupLayout(usersPanel);
        usersPanel.setLayout(usersPanelLayout);
        usersPanelLayout.setHorizontalGroup(
                usersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(usersPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(usersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(addUserButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(editUserButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(deleteUserButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(refreshUsersButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
        usersPanelLayout.setVerticalGroup(
                usersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(usersPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(addUserButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(editUserButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteUserButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(refreshUsersButton)
                                .addGap(18, 18, 18)
                                .addContainerGap(47, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundleString("Users"), usersPanel);

        GroupLayout rolesPanelLayout = new GroupLayout(rolesPanel);
        rolesPanel.setLayout(rolesPanelLayout);
        rolesPanelLayout.setHorizontalGroup(
                rolesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(rolesPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(rolesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(addRoleButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(deleteRoleButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        rolesPanelLayout.setVerticalGroup(
                rolesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(rolesPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(addRoleButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteRoleButton)
                                .addGap(18, 18, 18)
                                .addContainerGap(47, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundleString("Roles"), rolesPanel);

        membershipTable.setModel(new RoleTableModel(
                new Object[][]{},
                new String[]{
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }
        ));
        membershipTable.setDefaultRenderer(Object.class, new BrowserTableCellRenderer());
        jScrollPane3.setViewportView(membershipTable);
        membershipTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                try {
                    membershipMouseClicked(evt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        grantButton.setIcon(gr);
        grantButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    grantButtonActionPerformed(evt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        grantButton.setToolTipText("GRANT ROLE");
        grantLabel.setText("GRANT ROLE");

        adminButton.setIcon(adm);
        adminButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    adminButtonActionPerformed(evt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        adminButton.setToolTipText("GRANT ROLE WITH ADMIN OPTION");
        adminLabel.setText("GRANT ROLE WITH ADMIN OPTION");

        no_grantButton.setIcon(no);
        no_grantButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    no_grantButtonActionPerformed(evt);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        no_grantButton.setToolTipText("REVOKE ROLE");
        no_grantLabel.setText("REVOKE ROLE");

        roleRoleBox.setSelected(false);
        roleRoleBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    act = Action.REFRESH;
                    executeThread();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        GroupLayout membershipPanelLayout = new GroupLayout(membershipPanel);
        membershipPanel.setLayout(membershipPanelLayout);
        membershipPanelLayout.setHorizontalGroup(
                membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, membershipPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(membershipPanelLayout.createSequentialGroup()
                                                .addComponent(grantButton)
                                                .addGap(5)
                                                .addComponent(grantLabel))
                                        .addGroup(membershipPanelLayout.createSequentialGroup()
                                                .addComponent(adminButton)
                                                .addGap(5)
                                                .addComponent(adminLabel))
                                        .addGroup(membershipPanelLayout.createSequentialGroup()
                                                .addComponent(no_grantButton)
                                                .addGap(5)
                                                .addComponent(no_grantLabel))
                                        .addComponent(roleRoleBox))
                                .addGap(32, 32, 32)
                                .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                                .addGap(20, 20, 20))
        );
        membershipPanelLayout.setVerticalGroup(
                membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, membershipPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(membershipPanelLayout.createSequentialGroup()
                                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(grantButton)
                                                        .addComponent(grantLabel))
                                                .addGap(18, 18, 18)
                                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(adminButton)
                                                        .addComponent(adminLabel))
                                                .addGap(18, 18, 18)
                                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(no_grantButton)
                                                        .addComponent(no_grantLabel))
                                                .addGap(18, 18, 18)
                                                .addComponent(roleRoleBox))
                                        .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                                .addContainerGap())
        );

        jTabbedPane1.addTab(bundleString("Membership"), membershipPanel);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(connectPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTabbedPane1)
                        .addComponent(interruptPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(connectPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()
                                .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                                .addContainerGap()
                                .addComponent(interruptPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        )
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(bundleString("Users"));
    }

    void initUserManager() throws Exception {
        if (con == null) {
            version = 2;
            DatabaseDriver dd = null;
            List<DatabaseDriver> dds = driverRepository().findAll();
            for (DatabaseDriver d : dds) {
                if (d.getClassName().contains("FBDriver")) {
                    dd = d;
                    break;
                }
            }
            if (dd == null) {
                throw new SQLException("There are no drivers to initialize the user manager.");
            }
            URL[] urlDriver = new URL[0];
            Class clazzDriver = null;
            URL[] urls = new URL[0];
            Class clazzdb = null;
            Object o = null;
            Object odb = null;

            urlDriver = MiscUtils.loadURLs(dd.getPath());
            ClassLoader clD = new URLClassLoader(urlDriver);
            clazzDriver = clD.loadClass(dd.getClassName());
            o = clazzDriver.newInstance();

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, o.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserImpl");
            odb = clazzdb.newInstance();

            userAdd = (IFBUser) odb;

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            cl = new URLClassLoader(urls, o.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserManagerImpl");
            odb = clazzdb.newInstance();

            this.userManager = (IFBUserManager) odb;
        } else {
            Connection connection = con.unwrap(Connection.class);

            URL[] urls = new URL[0];
            Class clazzdb = null;
            Object odb = null;
            DatabaseHost host = new DefaultDatabaseHost(getSelectedDatabaseConnection());
            version = host.getDatabaseMetaData().getDatabaseMajorVersion();

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserImpl");
            odb = clazzdb.newInstance();

            userAdd = (IFBUser) odb;

            String loadedClass;
            if (version >= 3)
                loadedClass = "biz.redsoft.FB3UserManagerImpl";
            else
                loadedClass = "biz.redsoft.FBUserManagerImpl";
            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
            clazzdb = cl.loadClass(loadedClass);
            if (version >= 3)
                odb = clazzdb.getConstructor(Connection.class).newInstance(con);
            else
                odb = clazzdb.newInstance();
            this.userManager = (IFBUserManager) odb;
        }
    }

    private void databaseBoxActionPerformed(ItemEvent evt) {


            if (listConnections.size() > 0) {
                int selectedIndex = databaseBox.getSelectedIndex();
                if (selectedIndex == -1)
                    return;
                if (getSelectedDatabaseConnection().isConnected()) {
                    act = Action.REFRESH;
                    executeThread();

                } else {
                    initNotConnected();
                    if (execute_w) {
                        JFrame frameLogin = new FrameLogin(this, getSelectedDatabaseConnection().getUserName(),
                                getSelectedDatabaseConnection().getUnencryptedPassword());
                        frameLogin.setVisible(true);
                        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                        frameLogin.setLocation(width / 2 - frameLogin.getWidth() / 2, height / 2 - frameLogin.getHeight() / 2);
                    }
                }
            } else {
                initNotConnected();
            }
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setEnableElements(true);
    }

    void addUserButtonActionPerformed(ActionEvent evt) {
        GUIUtilities.addCentralPane(bundleString("AddUser"),
                UserManagerPanel.FRAME_ICON,
                new WindowAddUser(this, version),
                null,
                true);
    }

    void editUserButtonActionPerformed(ActionEvent evt) {
        int ind = usersTable.getSelectedRow();
        if (ind >= 0) {
            GUIUtilities.addCentralPane(bundleString("EditUser"),
                    UserManagerPanel.FRAME_ICON,
                    new WindowAddUser(this, ((IFBUser) (users.values().toArray()[ind])), version),
                    null,
                    true);
        }
    }

    void addRoleButtonActionPerformed(ActionEvent evt) {
        try {
            GUIUtilities.showWaitCursor();
            BaseDialog dialog =
                    new BaseDialog(WindowAddRole.TITLE, true);
            WindowAddRole panel = new WindowAddRole(dialog, getSelectedDatabaseConnection());
            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.display();
            act = Action.REFRESH;
            executeThread();
        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    void refreshUserButtonActionPerformed(ActionEvent evt) {
        act = Action.REFRESH;
        executeThread();
    }

    void deleteUserButtonActionPerformed(ActionEvent evt) {
        int ind = usersTable.getSelectedRow();
        if (ind >= 0) {
            if (GUIUtilities.displayConfirmDialog(bundleString("message.confirm-delete-user")) == 0) {
                try {
                    userManager.delete(((IFBUser) (users.values().toArray()[ind])));
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                act = Action.REFRESH;
                executeThread();
            }
        }
    }

    void deleteRoleButtonActionPerformed(ActionEvent evt) throws SQLException {
        int ind = rolesTable.getSelectedRow();
        if (ind >= 0) {
            String role = (String) rolesTable.getModel().getValueAt(ind, 0);
            if (GUIUtilities.displayConfirmDialog(bundleString("message.confirm-delete-role") + role + "?") == 0) {
                Statement state = null;
                try {
                    state = con.createStatement();
                    state.execute("DROP ROLE " + role);
                    act = Action.REFRESH;
                    executeThread();
                } catch (Exception e) {
                    GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
                    System.out.println(e.toString());
                } finally {
                    if(!state.isClosed())
                        state.close();
                }
            }
        }
    }

    private void grantButtonActionPerformed(ActionEvent evt) throws SQLException {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (enableElements) if (col >= 0) {
            if (membershipTable.getValueAt(row, col).equals(adm)) {
                revokeGrant(row, col);
            }
            grantTo(row, col);
        }
    }

    private void adminButtonActionPerformed(ActionEvent evt) throws SQLException {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (enableElements) {
            if (col >= 0) {
                grantWithAdmin(row, col);
            }
        }
    }

    private void no_grantButtonActionPerformed(ActionEvent evt) throws SQLException {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (col >= 0) {
            if (enableElements) {
                revokeGrant(row, col);
            }
        }
    }

    private void membershipMouseClicked(MouseEvent evt) throws SQLException {
        if (evt.getClickCount() > 1) {
            int row = membershipTable.getSelectedRow();
            int col = membershipTable.getSelectedColumn();
            if (enableElements) {
                if (col >= 0) {
                    if (membershipTable.getValueAt(row, col).equals(gr)) {
                        grantWithAdmin(row, col);
                    } else if (membershipTable.getValueAt(row, col).equals(adm)) {
                        revokeGrant(row, col);
                    } else {
                        grantTo(row, col);
                    }
                }
            }
        }
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container) component, enable);
            }
        }
    }

    void setEnableElements(boolean enable) {
        enableComponents(GUIUtilities.getParentFrame(), enable);
        enableElements = enable;
        addUserButton.setEnabled(enable);
        editUserButton.setEnabled(enable);
        deleteUserButton.setEnabled(enable);
        refreshUsersButton.setEnabled(enable);
        addRoleButton.setEnabled(enable);
        deleteRoleButton.setEnabled(enable);
        grantButton.setEnabled(enable);
        adminButton.setEnabled(enable);
        no_grantButton.setEnabled(enable);
        //cancelButton.setVisible(!enable);
        cancelButton.setEnabled(!enable);
        jProgressBar1.setEnabled(!enable);
        jProgressBar1.setValue(0);
    }

    public void run() {
        switch (act) {
            case REFRESH:
                if (!enableElements) {
                    try {
                        refresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case GET_MEMBERSHIP:
                if (!enableElements) {
                    createMembership();
                    setEnableElements(true);
                }
            default:
                break;
        }
    }

    void grantWithAdmin(int row, int col) throws SQLException {
        if (col >= 0) {
            Statement st = null;
            try {
                st = con.createStatement();
                String type = "";
                if (user_names.elementAt(row).isUser())
                    type = "USER";
                else if (version >= 3)
                    type = "ROLE";
                String query = "GRANT \"" + role_names.elementAt(col) + "\" TO " + type + " \"" +
                        user_names.elementAt(row).getName() + "\" WITH ADMIN OPTION;";
                Log.info("Execution:" + query);
                st.execute(query);
                act = Action.GET_MEMBERSHIP;
                executeThread();
            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            } finally {
                if(!st.isClosed())
                    st.close();
            }
        }
    }

    void grantTo(int row, int col) throws SQLException {
        if (col >= 0) {
            Statement st = null;
            try {
                st = con.createStatement();
                String type = "";
                if (user_names.elementAt(row).isUser())
                    type = "USER";
                else if (version >= 3)
                    type = "ROLE";
                String query = "GRANT \"" + role_names.elementAt(col) + "\" TO " + type + " \"" +
                        user_names.elementAt(row).getName() + "\";";
                Log.info("Execution:" + query);
                st.execute(query);
                act = Action.GET_MEMBERSHIP;
                executeThread();
            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            } finally {
                if (!st.isClosed())
                    st.close();
            }
        }
    }

    void revokeGrant(int row, int col) throws SQLException {
        if (col >= 0) {
            Statement st = null;
            try {
                st = con.createStatement();
                String type = "";
                if (user_names.elementAt(row).isUser())
                    type = "USER";
                else if (version >= 3)
                    type = "ROLE";
                String query = "REVOKE \"" + role_names.elementAt(col) + "\" FROM " + type + " \"" +
                        user_names.elementAt(row).getName() + "\";";
                Log.info("Execution:" + query);
                st.execute(query);
            } catch (Exception e) {
                GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            } finally {
                if (!st.isClosed())
                    st.close();
            }
            act = Action.GET_MEMBERSHIP;
            executeThread();
        }
    }

    void getUsersPanel() {
        try {
            users = userManager.getUsers();
            usersTable.setModel(new RoleTableModel(
                    new Object[][]{},
                    bundleStrings(new String[]{
                            "UserName", "FirstName", "MiddleName", "LastName"
                    })
            ));
            user_names.clear();
            for (IFBUser u : users.values()) {
                    user_names.add(new UserRole(u.getUserName().trim(), true));
                Object[] rowData = new Object[]{u.getUserName(), u.getFirstName(), u.getMiddleName(), u.getLastName()};
                ((RoleTableModel) usersTable.getModel()).addRow(rowData);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            GUIUtilities.displayErrorMessage(e.toString());
        }
    }

    public void refresh() throws Exception {
        if (getUseCustomServer()) {
            userManager.setHost(serverBox.getSelectedItem().toString());
            userManager.setPort(Integer.valueOf(portField.getText()));
            getUsersPanel();
            if (jTabbedPane1.getTabCount() > 1) {
                jTabbedPane1.remove(rolesPanel);
                jTabbedPane1.remove(membershipPanel);
            }
        } else {
            if (getSelectedDatabaseConnection() != null) {
                serverBox.removeAllItems();
                serverBox.addItem(getSelectedDatabaseConnection().getHost());
                portField.setText(getSelectedDatabaseConnection().getPort());
            }
            if (listConnections.size() > 0) {
                userManager.setDatabase(getSelectedDatabaseConnection().getSourceName());
                userManager.setHost(getSelectedDatabaseConnection().getHost());
                userManager.setPort(getSelectedDatabaseConnection().getPortInt());

                if (getSelectedDatabaseConnection().isConnected()) {
                    if (jTabbedPane1.getTabCount() < 2) {
                        jTabbedPane1.addTab(bundleString("Roles"), rolesPanel);
                        jTabbedPane1.addTab(bundleString("Membership"), membershipPanel);
                    }
                    con = ConnectionManager.getConnection(getSelectedDatabaseConnection());
                    initUserManager();
                    userManager.setDatabase(getSelectedDatabaseConnection().getSourceName());
                    userManager.setHost(getSelectedDatabaseConnection().getHost());
                    userManager.setPort(getSelectedDatabaseConnection().getPortInt());
                    userManager.setUser(getSelectedDatabaseConnection().getUserName());
                    userManager.setPassword(getSelectedDatabaseConnection().getUnencryptedPassword());
                    getUsersPanel();
                    getRoles();
                    createMembership();
                    update();
                } else {
                    getUsersPanel();
                    if (jTabbedPane1.getTabCount() > 1) {
                        jTabbedPane1.remove(rolesPanel);
                        jTabbedPane1.remove(membershipPanel);
                    }
                }
            } else {
                getUsersPanel();
                if (jTabbedPane1.getTabCount() > 1) {
                    jTabbedPane1.remove(rolesPanel);
                    jTabbedPane1.remove(membershipPanel);
                }
            }
        }

        setEnableElements(true);
    }

    void getRoles() throws SQLException {
        Statement state = null;
        try {
            state = con.createStatement();
            result = state.executeQuery("SELECT RDB$ROLE_NAME,RDB$OWNER_NAME FROM RDB$ROLES ORDER BY" +
                    " RDB$ROLE_NAME");
            rolesTable.setModel(new RoleTableModel(
                    new Object[][]{},
                    bundleStrings(new String[]{
                            "RoleName", "Owner"
                    })
            ));
            role_names.clear();
            while (result.next()) {
                String rol = result.getString(1).trim();
                role_names.add(rol);
                if (roleRoleBox.isSelected())
                    user_names.add(new UserRole(rol, false));
                Object[] roleData = new Object[]{rol, result.getObject(2)};
                ((RoleTableModel) rolesTable.getModel()).addRow(roleData);
            }
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.toString());
        } finally {
            if (!state.isClosed())
                state.close();
        }
    }

    void update() {
        int ind = jTabbedPane1.getSelectedIndex();
        jTabbedPane1.setSelectedIndex(1);
        jTabbedPane1.setSelectedIndex(2);
        jTabbedPane1.setSelectedIndex(0);
        jTabbedPane1.setSelectedIndex(ind);
    }

    void createMembership() {
        membershipTable.setModel(new RoleTableModel(
                new Object[][]{},
                role_names.toArray()
        ));

        update();

        boolean first = true;

        jProgressBar1.setMaximum(user_names.size());
        for (int i = 0; i < user_names.size() && !enableElements; i++) {
            jProgressBar1.setValue(i);
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                statement = con.createStatement();
                String type = "13";
                if (user_names.elementAt(i).isUser())
                    type = "8";
                resultSet = statement.executeQuery("select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION,rdb$Relation_name from RDB$USER_PRIVILEGES\n" +
                        "where (RDB$USER='" + user_names.elementAt(i).getName() + "') and (rdb$object_type=8 or rdb$object_type=13) and rdb$user_type=" + type);
                Vector<Object> roleData = new Vector<Object>();
                for (String u : role_names) {
                    roleData.add(no);
                }
                while (resultSet.next()) {
                    String u = resultSet.getString(3);
                    u = u.trim();
                    int ind = role_names.indexOf(u);
                    if (resultSet.getObject(2).equals(0))
                        roleData.set(ind, gr);
                    else
                        roleData.set(ind, adm);

                }
                ((RoleTableModel) membershipTable.getModel()).addRow(roleData);
                isClose();

                // need update UI
                if (first) {
                    int sizer = 0;
                    for (int j = 0; j < role_names.size(); j++) {
                        int temper = role_names.elementAt(j).length() * 8;
                        String s = role_names.elementAt(j);
                        membershipTable.getColumn(s).setMinWidth(temper);
                        sizer += temper;
                    }

                    JList rowHeader = new JList(user_names);
                    rowHeader.setFixedCellWidth(150);
                    rowHeader.setFixedCellHeight(membershipTable.getRowHeight());
                    rowHeader.setCellRenderer(new MembershipListCellRenderer(membershipTable));
                    jScrollPane3.setRowHeaderView(rowHeader);
                    int wid = jScrollPane3.getPreferredSize().width;
                    if (sizer > wid)
                        membershipTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                    else
                        membershipTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    first = false;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.error("Error index  out of bounds");
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (!statement.isClosed())
                        statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private DatabaseDriverRepository driverRepository() {
        return (DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID);
    }

    public void addUser() throws SQLException, IOException {
        userManager.add(userAdd);
        act = Action.REFRESH;
        executeThread();
    }

    public void editUser() throws SQLException, IOException {
        userManager.update(userAdd);
        act = Action.REFRESH;
        executeThread();
    }

    void executeThread() {
        if (enableElements) {
            setEnableElements(false);
            Runnable r = new ThreadOfUserManager(this);
            Thread t = new Thread(r);
            t.start();
        }
    }

    public String bundleString(String key) {
        return Bundles.get(UserManagerPanel.class, key);
    }

    private String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++)
            key[i] = bundleString(key[i]);
        return key;
    }

    void isClose() {
        if (GUIUtilities.getCentralPane(TITLE) == null)
            setEnableElements(true);
    }

    public void initNotConnected() {
        if (jTabbedPane1.getTabCount() > 1) {
            jTabbedPane1.remove(rolesPanel);
            jTabbedPane1.remove(membershipPanel);
        }
        usersTable.setModel(new RoleTableModel(
                new Object[][]{

                },
                bundleStrings(new String[]{
                        "UserName", "FirstName", "MiddleName", "LastName"
                })
        ));
    }

    public void setUseCustomServer(boolean useCustomServer) {
        this.useCustomServer = useCustomServer;
    }

    public boolean getUseCustomServer() {
        return useCustomServer;
    }

    public DatabaseConnection getSelectedDatabaseConnection() {
        return (DatabaseConnection) databaseBox.getSelectedItem();
    }

    public class UserRole {
        private String name;
        private boolean isUser;

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
    }

}


