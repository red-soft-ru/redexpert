package org.executequery.gui.browser;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseRole;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.log.Log;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.Vector;

/**
 * Created by mikhan808 on 02.04.2017.
 */
public class BrowserRolePanel extends AbstractFormObjectViewPanel implements ConnectionListener {

    public BrowserRolePanel(BrowserController browserController) {
        controller = browserController;
        grantIcon = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        noGrantIcon = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adminIcon = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        enableGrant = true;
        EventMediator.registerListener(this);
        initComponents();
    }

    @Override
    public void connected(ConnectionEvent connectionEvent) {

    }

    @Override
    public void disconnected(ConnectionEvent connectionEvent) {
        cleanup();
    }

    enum Action {
        NO_ALL_GRANTS_TO_OBJECT,
        ALL_GRANTS_TO_OBJECT,
        ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION,
        NO_GRANT_TO_ALL_OBJECTS,
        GRANT_TO_ALL_OBJECTS,
        GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION,
        NO_ALL_GRANTS_TO_ALL_OBJECTS,
        ALL_GRANTS_TO_ALL_OBJECTS,
        ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION,
        CREATE_TABLE
    }

    public static final String NAME = "BrowserRolePanel";
    public BrowserController controller;
    public JProgressBar progressBar;

    private Action action;
    private StatementExecutor querySender;
    private boolean enableGrant;
    private Vector<String> roles;
    private String grants = "SUDIXR";
    private String[] headers = {"Object", "Select", "Update", "Delete", "Insert", "Execute", "References"};
    private Vector<String> relationNames;
    private Vector<String> relationTypes;
    private Icon grantIcon, noGrantIcon, adminIcon;
    private JButton cancelWait;
    private JButton noAllGrantsButton;
    private JComboBox<String> objectBox;
    private JButton allAdminOptionButton;
    private JButton allGrantsButton;
    private JButton allRolesNoGrantButton;
    private JButton allUsersAdminOptionButton;
    private JButton allUsersGrantButton;
    private JButton grantAllToAllButton;
    private JButton grantAllToAllWithGrantButton;
    private JButton revokeAllFromAllButton;
    private JTable rolesTable;
    private JComboBox<String> rolesListCombo;
    private JCheckBox showSysTablesCheckBox;
    private JScrollPane jScrollPane1;

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return false;
    }

    void setValues(DefaultDatabaseRole ddr, BrowserController browserController) {
        controller = browserController;
        try {
            querySender = new DefaultStatementExecutor(ddr.getMetaTagParent().getHost().getDatabaseConnection(), true);
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        createRolesList(ddr.getName());
    }

    void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        rolesTable = new javax.swing.JTable();
        rolesListCombo = new javax.swing.JComboBox<>();
        showSysTablesCheckBox = new javax.swing.JCheckBox();
        grantAllToAllButton = new javax.swing.JButton();
        grantAllToAllWithGrantButton = new javax.swing.JButton();
        revokeAllFromAllButton = new javax.swing.JButton();
        objectBox = new javax.swing.JComboBox<>();
        allUsersGrantButton = new javax.swing.JButton();
        allUsersAdminOptionButton = new javax.swing.JButton();
        allRolesNoGrantButton = new javax.swing.JButton();
        allGrantsButton = new javax.swing.JButton();
        allAdminOptionButton = new javax.swing.JButton();
        noAllGrantsButton = new javax.swing.JButton();
        cancelWait = new DefaultButton();
        progressBar = new JProgressBar();
        BrowserTableCellRenderer cellRenderer = new BrowserTableCellRenderer();
        rolesTable.setDefaultRenderer(Object.class, cellRenderer);
        jScrollPane1.setViewportView(rolesTable);
        rolesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                rolesTableMouseClicked(e);
        }
        });

        rolesListCombo.addActionListener(e -> rolesListAction());
        rolesListCombo.setToolTipText("List of roles");
        progressBar.setMinimum(0);
        showSysTablesCheckBox.setText("Show system tables");
        showSysTablesCheckBox.addActionListener(e -> showSysTablesAction());

        grantAllToAllButton.setIcon(GUIUtilities.loadIcon("grant_all.png"));
        grantAllToAllButton.addActionListener(e -> grantAllToAll());
        grantAllToAllButton.setToolTipText("GRANT ALL TO ALL");

        grantAllToAllWithGrantButton.setIcon(GUIUtilities.loadIcon("admin_option_all.png"));
        grantAllToAllWithGrantButton.addActionListener(e -> grantAllToAllWithGrant());
        grantAllToAllWithGrantButton.setToolTipText("GRANT ALL TO ALL WITH GRANT_OPTION");

        revokeAllFromAllButton.setIcon(GUIUtilities.loadIcon("no_grant_all.png"));
        revokeAllFromAllButton.addActionListener(e -> revokeAllFromAll());
        revokeAllFromAllButton.setToolTipText("REVOKE ALL FROM ALL");

        objectBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All objects", "Tables", "Procedures", "Views"}));
        objectBox.addActionListener(e -> objectBoxAction());
        objectBox.setToolTipText("Select type of objects");

        allUsersGrantButton.setIcon(GUIUtilities.loadIcon("grant_vertical.png"));
        allUsersGrantButton.addActionListener(e -> allUsersGrantButtonAction());
        allUsersGrantButton.setToolTipText("GRANT TO ALL OBJECTS");

        allUsersAdminOptionButton.setIcon(GUIUtilities.loadIcon("admin_option_vertical.png"));
        allUsersAdminOptionButton.addActionListener(e -> allUsersAdminOptionAction());
        allAdminOptionButton.setToolTipText("GRANT TO ALL OBJECTS WITH GRANT OPTION");

        allRolesNoGrantButton.setIcon(GUIUtilities.loadIcon("no_grant_vertical.png"));
        allRolesNoGrantButton.addActionListener(e -> allRolesNoGrantAction());
        allRolesNoGrantButton.setToolTipText("REVOKE FROM ALL OBJECTS");

        allGrantsButton.setIcon(GUIUtilities.loadIcon("grant_gorisont.png"));
        allGrantsButton.addActionListener(e -> allGrantsButtonAction());
        allGrantsButton.setToolTipText("ALL GRANTS TO OBJECT");

        allAdminOptionButton.setIcon(GUIUtilities.loadIcon("admin_option_gorisont.png"));
        allAdminOptionButton.addActionListener(e -> allAdminOptionButtonAction());
        allAdminOptionButton.setToolTipText("ALL GRANTS TO OBJECT WITH GRANT OPTION");

        noAllGrantsButton.setIcon(GUIUtilities.loadIcon("no_grant_gorisont.png"));
        noAllGrantsButton.addActionListener(e -> noAllGrantsButtonAction());
        noAllGrantsButton.setToolTipText("REVOKE ALL FROM OBJECT");

        cancelWait.setText("Cancel waiting fill");
        cancelWait.addActionListener(e -> cancelWaitAction());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(showSysTablesCheckBox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(progressBar))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(rolesListCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(objectBox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allGrantsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allAdminOptionButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(noAllGrantsButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(grantAllToAllButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(grantAllToAllWithGrantButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(revokeAllFromAllButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allUsersGrantButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allUsersAdminOptionButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(allRolesNoGrantButton)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelWait)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(rolesListCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(objectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(allGrantsButton)
                                        .addComponent(allAdminOptionButton)
                                        .addComponent(noAllGrantsButton)
                                        .addComponent(grantAllToAllButton)
                                        .addComponent(grantAllToAllWithGrantButton)
                                        .addComponent(revokeAllFromAllButton)
                                        .addComponent(allUsersGrantButton)
                                        .addComponent(allUsersAdminOptionButton)
                                        .addComponent(allRolesNoGrantButton)
                                        .addComponent(cancelWait))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(showSysTablesCheckBox)
                                        .addComponent(progressBar))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE))
        );
    }

    void createRolesList() {
        try {
            String query = "SELECT RDB$ROLE_NAME FROM RDB$ROLES";
            ResultSet result = querySender.getResultSet(query).getResultSet();
            roles = new Vector<>();
            while (result.next()) {
                String role = result.getString(1);
                roles.add(role);
            }
            querySender.releaseResources();
            rolesListCombo.setModel(new javax.swing.DefaultComboBoxModel<>(roles));
            rolesListCombo.setSelectedIndex(0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
    }

    void createRolesList(String selectedRole) {
        enableGrant = false;
        try {
            //Statement st = con.createStatement();
            String query = "SELECT RDB$ROLE_NAME FROM RDB$ROLES ORDER BY 1";
            ResultSet result = querySender.getResultSet(query).getResultSet();
            roles = new Vector<>();
            while (result.next()) {
                String role = result.getString(1);
                roles.add(role.trim());
            }
            querySender.releaseResources();
            rolesListCombo.setModel(new javax.swing.DefaultComboBoxModel<>(roles));
            rolesListCombo.setSelectedItem(selectedRole.trim());
            rolesListCombo.setEnabled(false);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
        enableGrant = true;
    }

    void createTable() {
        setEnableGrant(false);
        relationNames = new Vector<>();
        relationTypes = new Vector<>();
        rolesTable.setModel(new RoleTableModel(headers, 0));
        try {
            if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 1) {
                String query = "Select RDB$RELATION_NAME from RDB$RELATIONS WHERE RDB$RELATION_TYPE != 1 ORDER BY 1";
                ResultSet rs = querySender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (showSysTablesCheckBox.isSelected()) {
                        relationNames.add(name);
                        relationTypes.add(objectBox.getItemAt(1));
                    } else {
                        if (!name.contains("$")) {
                            relationNames.add(name);
                            relationTypes.add(objectBox.getItemAt(1));
                        }
                    }
                }
                querySender.releaseResources();
            }
            if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 3) {
                String query = "Select DISTINCT RDB$VIEW_NAME from RDB$VIEW_RELATIONS ORDER BY 1";
                ResultSet rs = querySender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (showSysTablesCheckBox.isSelected()) {
                        relationNames.add(name);
                        relationTypes.add(objectBox.getItemAt(3));
                    } else {
                        if (!name.contains("$")) {
                            relationNames.add(name);
                            relationTypes.add(objectBox.getItemAt(3));
                        }
                    }
                }
                querySender.releaseResources();
            }
            if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 2) {
                String query = "Select RDB$PROCEDURE_NAME from RDB$PROCEDURES ORDER BY 1";
                ResultSet rs = querySender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (showSysTablesCheckBox.isSelected()) {
                        relationNames.add(name);
                        relationTypes.add(objectBox.getItemAt(2));
                    } else {
                        if (!name.contains("$")) {
                            relationNames.add(name);
                            relationTypes.add(objectBox.getItemAt(2));
                        }
                    }
                }
                querySender.releaseResources();
            }
            progressBar.setMaximum(relationNames.size());
            for (int i = 0; i < relationNames.size() && !enableGrant; i++) {
                progressBar.setValue(i);
                String s = "";
                try {
                    s = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION from RDB$USER_PRIVILEGES\n" +
                            "where (rdb$grant_option is not null) and (rdb$Relation_name='" + relationNames.elementAt(i) + "') and (rdb$user='" + rolesListCombo.getSelectedItem() + "')";
                    ResultSet resultSet = querySender.getResultSet(s).getResultSet();
                    Vector<Object> roleData = new Vector<Object>();

                    roleData.add(relationNames.elementAt(i));
                    for (int k = 0; k < 6; k++)
                        roleData.add(noGrantIcon);
                    ((RoleTableModel) rolesTable.getModel()).addRow(roleData);

                    while (resultSet.next()) {
                        String grant = resultSet.getString(1);
                        grant = grant.substring(0, 1);
                        int ind = grants.indexOf(grant);
                        if (resultSet.getObject(2).equals(0)) {
                            rolesTable.setValueAt(grantIcon, i, ind + 1);
                        } else
                            rolesTable.getModel().setValueAt(adminIcon, i, ind + 1);
                    }
                    querySender.releaseResources();
                } catch (Exception e) {
                    if (querySender.getDatabaseConnection().isConnected()) {
                        GUIUtils.startWorker(new Runnable() {
                            @Override
                            public void run() {
                                GUIUtilities.displayInformationMessage(LocalTime.now());
                            }
                        });
                        Log.error("SQL:" + s);
                        e.printStackTrace();
                    }
                } finally {
                    querySender.releaseResources();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
        setEnableGrant(true);
        progressBar.setValue(0);
    }

    private void rolesListAction() {
        action = Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void showSysTablesAction() {
        action = Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    void grantOnRole(int grant, int row, int col) {
        if (row < rolesTable.getRowCount()) {
            switch (grant) {
                case 0:
                    try {
                        if (!relationTypes.elementAt(row).equals(objectBox.getItemAt(2))) {
                            if (!headers[col].equals("Execute")) {
                                String query = "REVOKE " + headers[col] + " ON \"" + relationNames.elementAt(row) + "\" FROM \"" + rolesListCombo.getSelectedItem() + "\";";
                                querySender.execute(QueryTypes.REVOKE, query);
                                querySender.execute(QueryTypes.COMMIT, (String) null);
                                rolesTable.setValueAt(noGrantIcon, row, col);
                                querySender.releaseResources();
                            }
                        } else if (headers[col].equals("Execute")) {
                            String query = "REVOKE " + headers[col] + " ON PROCEDURE \"" + relationNames.elementAt(row) + "\" FROM \"" + rolesListCombo.getSelectedItem() + "\";";
                            querySender.execute(QueryTypes.REVOKE, query);
                            querySender.execute(QueryTypes.COMMIT, (String) null);
                            rolesTable.setValueAt(noGrantIcon, row, col);
                            querySender.releaseResources();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        querySender.releaseResources();
                    }
                    break;
                case 1:
                    if (rolesTable.getValueAt(row, col).equals(adminIcon)) {
                        try {
                            if (!relationTypes.elementAt(row).equals(objectBox.getItemAt(2))) {
                                if (!headers[col].equals("Execute")) {
                                    String query = "REVOKE " + headers[col] + " ON \"" + relationNames.elementAt(row) + "\" FROM \"" + rolesListCombo.getSelectedItem() + "\";";
                                    querySender.execute(QueryTypes.REVOKE, query);
                                    querySender.execute(QueryTypes.COMMIT, (String) null);
                                    rolesTable.setValueAt(noGrantIcon, row, col);
                                    querySender.releaseResources();
                                }
                            } else if (headers[col].equals("Execute")) {
                                String query = "REVOKE " + headers[col] + " ON PROCEDURE \"" + relationNames.elementAt(row) + "\" FROM \"" + rolesListCombo.getSelectedItem() + "\";";
                                querySender.execute(QueryTypes.REVOKE, query);
                                querySender.execute(QueryTypes.COMMIT, (String) null);
                                rolesTable.setValueAt(noGrantIcon, row, col);
                                querySender.releaseResources();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            querySender.releaseResources();
                        }

                        try {
                            if (!relationTypes.elementAt(row).equals(objectBox.getItemAt(2))) {
                                if (!headers[col].equals("Execute")) {
                                    String query = "GRANT " + headers[col] + " ON \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\";";
                                    querySender.execute(QueryTypes.GRANT, query);
                                    querySender.execute(QueryTypes.COMMIT, (String) null);
                                    querySender.releaseResources();
                                    rolesTable.setValueAt(grantIcon, row, col);
                                }
                            } else if (headers[col].equals("Execute")) {
                                String query = "GRANT " + headers[col] + " ON PROCEDURE \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\";";
                                querySender.execute(QueryTypes.GRANT, query);
                                querySender.execute(QueryTypes.COMMIT, (String) null);
                                querySender.releaseResources();
                                rolesTable.setValueAt(grantIcon, row, col);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            querySender.releaseResources();
                        }

                    } else
                        try {
                            if (!relationTypes.elementAt(row).equals(objectBox.getItemAt(2))) {
                                if (!headers[col].equals("Execute")) {
                                    String query = "GRANT " + headers[col] + " ON \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\";";
                                    querySender.execute(QueryTypes.GRANT, query);
                                    querySender.execute(QueryTypes.COMMIT, (String) null);
                                    querySender.releaseResources();
                                    rolesTable.setValueAt(grantIcon, row, col);
                                }
                            } else if (headers[col].equals("Execute")) {
                                String query = "GRANT " + headers[col] + " ON PROCEDURE \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\";";
                                querySender.execute(QueryTypes.GRANT, query);
                                querySender.execute(QueryTypes.COMMIT, (String) null);
                                querySender.releaseResources();
                                rolesTable.setValueAt(grantIcon, row, col);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            querySender.releaseResources();
                        }
                    break;
                case 2:
                    try {
                        if (!relationTypes.elementAt(row).equals(objectBox.getItemAt(2))) {
                            if (!headers[col].equals("Execute")) {
                                String query = "GRANT " + headers[col] + " ON \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\" WITH GRANT OPTION;";
                                querySender.execute(QueryTypes.GRANT, query);
                                querySender.execute(QueryTypes.COMMIT, (String) null);
                                querySender.releaseResources();
                                rolesTable.setValueAt(adminIcon, row, col);
                            }
                        } else if (headers[col].equals("Execute")) {
                            String query = "GRANT " + headers[col] + " ON PROCEDURE \"" + relationNames.elementAt(row) + "\" TO \"" + rolesListCombo.getSelectedItem() + "\" WITH GRANT OPTION;";
                            querySender.execute(QueryTypes.GRANT, query);
                            querySender.execute(QueryTypes.COMMIT, (String) null);
                            querySender.releaseResources();
                            rolesTable.setValueAt(adminIcon, row, col);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        querySender.releaseResources();
                    }
                    break;
            }
        }
    }

    private void grantAllToAll() {
        if (enableGrant) {
            action = Action.ALL_GRANTS_TO_ALL_OBJECTS;
            setEnableGrant(false);
            Runnable r = new ThreadOfRole(this);
            Thread t = new Thread(r);
            t.start();
        } else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void grantAllToAllWithGrant() {
        action = Action.ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void revokeAllFromAll() {
        action = Action.NO_ALL_GRANTS_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void allGrantsButtonAction() {
        if (enableGrant) {
            int row = rolesTable.getSelectedRow();
            if (row >= 0) {
                for (int col = 1; col < headers.length; col++) {
                    grantOnRole(1, row, col);
                }
            }
        } else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void allAdminOptionButtonAction() {
        if (enableGrant) {
            int row = rolesTable.getSelectedRow();
            if (row >= 0) {
                for (int col = 1; col < headers.length; col++) {
                    grantOnRole(2, row, col);
                }
            }
        } else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void noAllGrantsButtonAction() {
        if (enableGrant) {
            int row = rolesTable.getSelectedRow();
            if (row >= 0) {
                for (int col = 1; col < headers.length; col++) {
                    grantOnRole(0, row, col);
                }
            }
        } else {
            GUIUtilities.displayInformationMessage("Please wait");
        }
    }

    private void allUsersGrantButtonAction() {
        action = Action.GRANT_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void allUsersAdminOptionAction() {
        action = Action.GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void allRolesNoGrantAction() {
        action = Action.NO_GRANT_TO_ALL_OBJECTS;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void objectBoxAction() {
        action = Action.CREATE_TABLE;
        setEnableGrant(false);
        Runnable r = new ThreadOfRole(this);
        Thread t = new Thread(r);
        t.start();
    }

    private void rolesTableMouseClicked(MouseEvent e) {
        if (enableGrant) {
            if (e.getClickCount() > 1) {
                int row = rolesTable.getSelectedRow();
                int col = rolesTable.getSelectedColumn();
                if (col > 0) {
                    if (rolesTable.getValueAt(row, col).equals(grantIcon))
                        grantOnRole(2, row, col);
                    else if (rolesTable.getValueAt(row, col).equals(adminIcon))
                        grantOnRole(0, row, col);
                    else
                        grantOnRole(1, row, col);
                }
            }
        }
    }

    void setEnableGrant(boolean enable) {
        enableGrant = enable;
        //rolesListCombo.setEnabled(enable);
        objectBox.setEnabled(enable);
        showSysTablesCheckBox.setEnabled(enable);
        grantAllToAllButton.setEnabled(enable);
        grantAllToAllWithGrantButton.setEnabled(enable);
        revokeAllFromAllButton.setEnabled(enable);
        noAllGrantsButton.setEnabled(enable);
        allAdminOptionButton.setEnabled(enable);
        allGrantsButton.setEnabled(enable);
        allRolesNoGrantButton.setEnabled(enable);
        allUsersAdminOptionButton.setEnabled(enable);
        allUsersGrantButton.setEnabled(enable);
        cancelWait.setVisible(!enable);
        progressBar.setVisible(!enable);
        if (enable)
            progressBar.setValue(0);
    }

    public void run() {
        int column;
        switch (action) {
            case CREATE_TABLE:
                createTable();
                break;
            case ALL_GRANTS_TO_ALL_OBJECTS: {
                progressBar.setMaximum(relationNames.size());
                for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                    progressBar.setValue(row);
                    for (column = 1; column < headers.length; column++)
                        grantOnRole(1, row, column);
                }
                progressBar.setValue(0);
                setEnableGrant(true);
            }
            break;
            case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                progressBar.setMaximum(relationNames.size());
                for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                    progressBar.setValue(row);
                    for (column = 1; column < headers.length; column++)
                        grantOnRole(2, row, column);
                }
                progressBar.setValue(0);
                setEnableGrant(true);
                break;
            case NO_ALL_GRANTS_TO_ALL_OBJECTS:
                progressBar.setMaximum(relationNames.size());
                for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                    progressBar.setValue(row);
                    for (column = 1; column < headers.length; column++)
                        grantOnRole(0, row, column);
                }
                progressBar.setValue(0);
                setEnableGrant(true);
                break;
            case GRANT_TO_ALL_OBJECTS:
                column = rolesTable.getSelectedColumn();
                progressBar.setMaximum(relationNames.size());
                if (column > 0)
                    for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                        progressBar.setValue(row);
                        grantOnRole(1, row, column);
                    }
                progressBar.setValue(0);
                setEnableGrant(true);
                break;
            case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                column = rolesTable.getSelectedColumn();
                progressBar.setMaximum(relationNames.size());
                if (column > 0)
                    for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                        progressBar.setValue(row);
                        grantOnRole(2, row, column);
                    }
                progressBar.setValue(0);
                setEnableGrant(true);
                break;
            case NO_GRANT_TO_ALL_OBJECTS:
                column = rolesTable.getSelectedColumn();
                progressBar.setMaximum(relationNames.size());
                if (column > 0)
                    for (int row = 0; row < relationNames.size() && !enableGrant; row++) {
                        progressBar.setValue(row);
                        grantOnRole(0, row, column);
                    }
                progressBar.setValue(0);
                setEnableGrant(true);
                break;
        }
    }

    void cancelWaitAction() {
        setEnableGrant(true);
    }

    @Override
    public void cleanup() {
        setEnableGrant(true);
        EventMediator.deregisterListener(this);

    }

    @Override
    public Printable getPrintable() {
        return null;
    }

    @Override
    public String getLayoutName() {
        return NAME;
    }
}

