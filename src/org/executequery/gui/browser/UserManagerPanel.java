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
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.managment.FrameLogin;
import org.executequery.gui.browser.managment.ThreadOfUserManager;
import org.executequery.gui.browser.managment.WindowAddRole;
import org.executequery.gui.browser.managment.WindowAddUser;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
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

    public static final String TITLE = Bundles.get(UserManagerPanel.class, "UserManager");
    public static final String FRAME_ICON = "user_manager_16.png";
    public IFBUserManager userManager;
    public BrowserController controller;
    public IFBUser userAdd;
    public DatabaseConnection dbc;
    boolean execute_w;
    boolean enableElements;
    Icon gr, no, adm;
    Connection con;
    List<DatabaseConnection> listConnections;
    Map<String, IFBUser> users;
    Vector<String> user_names;
    Vector<String> role_names;
    ResultSet result;
    int version;
    Action act;
    private JButton addUserButton;
    private JButton addRoleButton;
    private JButton adminButton;
    private JComboBox<String> databaseBox;
    private JLabel databaseLabel;
    private JButton deleteUserButton;
    private JButton deleteRoleButton;
    private JButton editUserButton;
    private JButton grantButton;
    private JPanel jPanel1;
    private JPanel interruptPanel;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JTabbedPane jTabbedPane1;
    private JPanel membershipPanel;
    private JTable membershipTable;
    private JButton no_grantButton;
    private JButton refreshUsersButton;
    private JPanel rolesPanel;
    private JComboBox<String> serverBox;
    private JLabel serverLabel;
    private JPanel usersPanel;
    private JTable usersTable;
    private JTable rolesTable;
    private JProgressBar jProgressBar1;
    private JButton cancelButton;

    /**
     * Creates new form UserManagerPanel
     */
    public UserManagerPanel() {
        gr = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        no = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adm = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        execute_w = false;
        user_names = new Vector<String>();
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
            databaseBox.addItem(dc.getName());
            if (dc.isConnected() && !selected) {
                execute_w = true;
                databaseBox.setSelectedItem(dc.getName());
                selected = true;
            }
        }
        if (!execute_w) {
            if (databaseBox.getItemCount() == 0)
                databaseBox.addItem("");
            execute_w = true;
            databaseBox.setSelectedIndex(0);


        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jPanel1 = new JPanel();
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
        addUserButton = new JButton();
        addRoleButton = new JButton();
        editUserButton = new JButton();
        deleteUserButton = new JButton();
        deleteRoleButton = new JButton();
        refreshUsersButton = new JButton();
        rolesPanel = new JPanel();
        membershipPanel = new JPanel();
        membershipTable = new JTable();
        grantButton = new JButton();
        adminButton = new JButton();
        no_grantButton = new JButton();
        jProgressBar1 = new JProgressBar();
        cancelButton = new JButton();

        cancelButton.setText(bundleString("cancelButton"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel1.setName("upPanel"); // NOI18N

        databaseLabel.setText(bundleString("database"));

        serverLabel.setText(bundleString("server"));

        databaseBox.setEditable(true);
        databaseBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                databaseBoxActionPerformed(evt);
            }
        });

        serverBox.setEditable(true);

        interruptPanel.setLayout(new GridBagLayout());
        interruptPanel.add(jProgressBar1, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        interruptPanel.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(databaseLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(serverLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(serverBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(databaseBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(databaseLabel)
                                        .addComponent(databaseBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(serverLabel)
                                        .addComponent(serverBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

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
        addUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addUserButtonActionPerformed(evt);
            }
        });
        addRoleButton.setText(bundleString("Add"));
        addRoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRoleButtonActionPerformed(evt);
            }
        });

        editUserButton.setText(bundleString("Edit"));
        editUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editUserButtonActionPerformed(evt);
            }
        });

        deleteUserButton.setText(bundleString("Delete"));
        deleteUserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUserButtonActionPerformed(evt);
            }
        });

        deleteRoleButton.setText(bundleString("Delete"));
        deleteRoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRoleButtonActionPerformed(evt);
            }
        });

        refreshUsersButton.setText(bundleString("Refresh"));
        refreshUsersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
        membershipTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                membershipMouseClicked(evt);
            }
        });

        grantButton.setIcon(gr);
        grantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grantButtonActionPerformed(evt);
            }
        });
        grantButton.setToolTipText("GRANT ROLE");

        adminButton.setIcon(adm);
        adminButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adminButtonActionPerformed(evt);
            }
        });
        adminButton.setToolTipText("GRANT ROLE WITH ADMIN OPTION");

        no_grantButton.setIcon(no);
        no_grantButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                no_grantButtonActionPerformed(evt);
            }
        });
        no_grantButton.setToolTipText("REVOKE ROLE");

        GroupLayout membershipPanelLayout = new GroupLayout(membershipPanel);
        membershipPanel.setLayout(membershipPanelLayout);
        membershipPanelLayout.setHorizontalGroup(
                membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, membershipPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(membershipPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(grantButton)
                                        .addComponent(adminButton)
                                        .addComponent(no_grantButton))
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
                                                .addComponent(grantButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(adminButton)
                                                .addGap(18, 18, 18)
                                                .addComponent(no_grantButton))
                                        .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))
                                .addContainerGap())
        );

        jTabbedPane1.addTab(bundleString("Membership"), membershipPanel);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(interruptPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()
                                .addComponent(interruptPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap()
                                .addComponent(jTabbedPane1, GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE))
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName(bundleString("Users"));
    }

    void initUserManager() throws Exception {
        if (con == null) {
            version = 2;
            DatabaseDriver dd = null;
            List<DatabaseDriver> dds = driverRepository().findAll();
            for (DatabaseDriver d : dds) {
                if (d.getClassName().contains("FBDriver"))
                    dd = d;
                break;
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

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, o.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserImpl");
            odb = clazzdb.newInstance();

            userAdd = (IFBUser) odb;

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
            cl = new URLClassLoader(urls, o.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserManagerImpl");
            odb = clazzdb.newInstance();

            this.userManager = (IFBUserManager) odb;
        } else {
            Connection connection = con.unwrap(Connection.class);

            URL[] urls = new URL[0];
            Class clazzdb = null;
            Object odb = null;
            DatabaseHost host = new DefaultDatabaseHost(dbc);
            version = host.getDatabaseMetaData().getDatabaseMajorVersion();

            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserImpl");
            odb = clazzdb.newInstance();

            userAdd = (IFBUser) odb;

            String loadedClass;
            if (version >= 3)
                loadedClass = "biz.redsoft.FB3UserManagerImpl";
            else
                loadedClass = "biz.redsoft.FBUserManagerImpl";
            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar");
            cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
            clazzdb = cl.loadClass(loadedClass);
            if (version >= 3)
                odb = clazzdb.getConstructor(Connection.class).newInstance(con);
            else
                odb = clazzdb.newInstance();
            this.userManager = (IFBUserManager) odb;
        }
    }

    private void databaseBoxActionPerformed(java.awt.event.ActionEvent evt) {

        if (execute_w) {
            if (listConnections.size() > 0) {
                dbc = listConnections.get(databaseBox.getSelectedIndex());
                if (listConnections.get(databaseBox.getSelectedIndex()).isConnected()) {
                    act = Action.REFRESH;
                    execute_thread();

                } else {
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
                    JFrame frame_pass = new FrameLogin(this, listConnections.get(databaseBox.getSelectedIndex()).getUserName(),
                            listConnections.get(databaseBox.getSelectedIndex()).getUnencryptedPassword());
                    frame_pass.setVisible(true);
                    int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                    int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                    frame_pass.setLocation(width / 2 - frame_pass.getWidth() / 2, height / 2 - frame_pass.getHeight() / 2);
                }
            } else {
                usersTable.setModel(new RoleTableModel(
                        new Object[][]{

                        },
                        bundleStrings(new String[]{
                                "UserName", "FirstName", "MiddleName", "LastName"
                        })
                ));
                JFrame frame_pass = new FrameLogin(this, "",
                        "");
                frame_pass.setVisible(true);
                int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                frame_pass.setLocation(width / 2 - frame_pass.getWidth() / 2, height / 2 - frame_pass.getHeight() / 2);
            }
        }
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setEnableElements(true);
    }

    void addUserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        GUIUtilities.addCentralPane(bundleString("AddUser"),
                UserManagerPanel.FRAME_ICON,
                new WindowAddUser(this, version),
                null,
                true);
    }

    void editUserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int ind = usersTable.getSelectedRow();
        if (ind >= 0) {
            GUIUtilities.addCentralPane(bundleString("EditUser"),
                    UserManagerPanel.FRAME_ICON,
                    new WindowAddUser(this, ((IFBUser) (users.values().toArray()[ind])), version),
                    null,
                    true);
        }
    }

    void addRoleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            GUIUtilities.showWaitCursor();
            BaseDialog dialog =
                    new BaseDialog(WindowAddRole.TITLE, true);
            WindowAddRole panel = new WindowAddRole(dialog, dbc);
            dialog.addDisplayComponentWithEmptyBorder(panel);
            dialog.display();
            act = Action.REFRESH;
            execute_thread();
        } finally {
            GUIUtilities.showNormalCursor();
        }
    }

    void refreshUserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.REFRESH;
        execute_thread();
    }

    void deleteUserButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int ind = usersTable.getSelectedRow();
        if (ind >= 0) {
            if (GUIUtilities.displayConfirmDialog(bundleString("message.confirm-delete-user")) == 0) {
                try {
                    userManager.delete(((IFBUser) (users.values().toArray()[ind])));
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                act = Action.REFRESH;
                execute_thread();
            }
        }
    }

    void deleteRoleButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int ind = rolesTable.getSelectedRow();
        if (ind >= 0) {
            String role = (String) rolesTable.getModel().getValueAt(ind, 0);
            if (GUIUtilities.displayConfirmDialog(bundleString("message.confirm-delete-role") + role + "?") == 0)
                try {
                    Statement state = con.createStatement();
                    state.execute("DROP ROLE " + role);
                    state.close();
                    act = Action.REFRESH;
                    execute_thread();
                } catch (Exception e) {
                    GUIUtilities.displayErrorMessage(e.getMessage());
                    System.out.println(e.toString());
                }
        }
    }

    private void grantButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (enableElements) if (col >= 0) {
            if (membershipTable.getValueAt(row, col).equals(adm)) {
                revokeGrant(row, col);
            }
            grantTo(row, col);
        }
    }

    private void adminButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (enableElements) if (col >= 0) {

            grantWithAdmin(row, col);
        }
    }

    private void no_grantButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int row = membershipTable.getSelectedRow();
        int col = membershipTable.getSelectedColumn();
        if (col >= 0)
            if (enableElements) {
                revokeGrant(row, col);
            }
    }

    private void membershipMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            int row = membershipTable.getSelectedRow();
            int col = membershipTable.getSelectedColumn();
            if (enableElements) if (col >= 0) {
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
        cancelButton.setVisible(!enable);
        cancelButton.setEnabled(!enable);
        jProgressBar1.setVisible(!enable);
        if (enable)
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

    void grantWithAdmin(int row, int col) {
        if (col >= 0) {
            try {
                Statement st = con.createStatement();
                st.execute("GRANT \"" + role_names.elementAt(col) + "\" TO \"" + user_names.elementAt(row) + "\" WITH ADMIN OPTION;");
                st.close();
                act = Action.GET_MEMBERSHIP;
                execute_thread();
            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            }
        }
    }

    void grantTo(int row, int col) {
        if (col >= 0) {
            try {
                Statement st = con.createStatement();
                String query = "GRANT \"" + role_names.elementAt(col) + "\" TO \"" + user_names.elementAt(row) + "\";";
                st.execute(query);
                st.close();
                act = Action.GET_MEMBERSHIP;
                execute_thread();
            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            }
        }
    }

    void revokeGrant(int row, int col) {
        if (col >= 0) {
            try {
                Statement st = con.createStatement();
                st.execute("REVOKE \"" + role_names.elementAt(col) + "\" FROM \"" + user_names.elementAt(row) + "\";");
                st.close();
            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            }
            act = Action.GET_MEMBERSHIP;
            execute_thread();
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
                user_names.add(u.getUserName().trim());
                Object[] rowData = new Object[]{u.getUserName(), u.getFirstName(), u.getMiddleName(), u.getLastName()};
                ((RoleTableModel) usersTable.getModel()).addRow(rowData);
                //update();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            GUIUtilities.displayErrorMessage(e.toString());
        }
    }

    public void refresh() throws Exception {
        if (databaseBox.getItemAt(databaseBox.getSelectedIndex()) != "") {
            serverBox.removeAllItems();
            serverBox.addItem(listConnections.get(databaseBox.getSelectedIndex()).getHost());
        }
        if (listConnections.size() > 0) {
            userManager.setDatabase(listConnections.get(databaseBox.getSelectedIndex()).getSourceName());
            userManager.setHost(listConnections.get(databaseBox.getSelectedIndex()).getHost());
            userManager.setPort(listConnections.get(databaseBox.getSelectedIndex()).getPortInt());

            if (listConnections.get(databaseBox.getSelectedIndex()).isConnected()) {
                if (jTabbedPane1.getTabCount() < 2) {
                    jTabbedPane1.addTab(bundleString("Roles"), rolesPanel);
                    jTabbedPane1.addTab(bundleString("Membership"), membershipPanel);
                }
                con = ConnectionManager.getConnection(listConnections.get(databaseBox.getSelectedIndex()));
                initUserManager();
                userManager.setDatabase(listConnections.get(databaseBox.getSelectedIndex()).getSourceName());
                userManager.setHost(listConnections.get(databaseBox.getSelectedIndex()).getHost());
                userManager.setPort(listConnections.get(databaseBox.getSelectedIndex()).getPortInt());
                userManager.setUser(listConnections.get(databaseBox.getSelectedIndex()).getUserName());
                userManager.setPassword(listConnections.get(databaseBox.getSelectedIndex()).getUnencryptedPassword());
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

        setEnableElements(true);
    }

    void getRoles() {
        try {
            Statement state = con.createStatement();
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
                user_names.add(rol);
                Object[] roleData = new Object[]{rol, result.getObject(2)};
                ((RoleTableModel) rolesTable.getModel()).addRow(roleData);
            }
            state.close();
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.toString());
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
            try {
                Statement st = con.createStatement();
                ResultSet rs1 = st.executeQuery("select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION,rdb$Relation_name from RDB$USER_PRIVILEGES\n" +
                        "where (RDB$USER='" + user_names.elementAt(i) + "') and (rdb$object_type=8 or rdb$object_type=13)");
                Vector<Object> roleData = new Vector<Object>();
                for (String u : role_names) {
                    roleData.add(no);
                }
                while (rs1.next()) {
                    String u = rs1.getString(3);
                    u = u.trim();
                    int ind = role_names.indexOf(u);
                    if (rs1.getObject(2).equals(0))
                        roleData.set(ind, gr);
                    else
                        roleData.set(ind, adm);

                }
                st.close();
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
                    rowHeader.setCellRenderer(new RowHeaderRenderer(membershipTable));
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
            } catch (NullPointerException e) {

            } catch (SQLException e) {
                Log.error(e.getMessage());
            } catch (Exception e) {
                Log.error(e.getMessage());
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
        execute_thread();

    }

    public void editUser() throws SQLException, IOException {

        userManager.update(userAdd);
        act = Action.REFRESH;
        execute_thread();

    }

    void execute_thread() {
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

    enum Action {
        REFRESH,
        GET_USERS,
        GET_ROLES,
        GET_MEMBERSHIP
    }
}


