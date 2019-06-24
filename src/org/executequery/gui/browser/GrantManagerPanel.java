/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.executequery.gui.browser;

import biz.redsoft.IFBUser;
import biz.redsoft.IFBUserManager;
import org.executequery.GUIUtilities;
import org.executequery.components.SplitPaneFactory;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.managment.ThreadOfGrantManager;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.DefaultButton;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author mikhan808
 */
public class GrantManagerPanel extends JPanel {

    public static final String TITLE = Bundles.get(GrantManagerPanel.class, "GrantManager");
    public static final String FRAME_ICON = "grant_manager_16.png";
    public DatabaseConnection dbc;
    boolean connected;
    List<DatabaseConnection> listConnections;
    Connection con;
    DefaultListModel userlistModel;
    boolean enabled_dBox;
    boolean enableElements;
    Vector<String> relName;
    Vector<String> relType;
    Vector<Boolean> relSystem;
    Vector<Boolean> relGranted;
    String grants = "SUDIXRGA";
    String[] headers = {bundleString("Object"), "Select", "Update", "Delete", "Insert", "Execute", "References", "Usage"};
    String[] headers2 = {bundleString("Field"), bundleString("Type"), "Update", "References"};
    Icon gr, no, adm;
    Action act;
    Vector<String> fieldName;
    Vector<String> fieldType;
    int obj_index;
    StatementExecutor querySender;
    int col_execute = 5;
    int col_usage = 7;
    private JButton cancelButton;
    private JPanel interruptPanel;
    private JComboBox<String> databaseBox;
    private JPanel downPanel;
    private JComboBox<String> filterBox;
    private JTextField filterField;
    private JButton grant_all;
    private JButton grant_g;
    private JButton grant_g1;
    private JButton grant_option_all;
    private JButton grant_option_g;
    private JButton grant_option_g1;
    private JButton grant_option_v;
    private JButton grant_option_v1;
    private JButton grant_v;
    private JButton grant_v1;
    private JCheckBox jCheckBox1;
    private JLabel labelPrivileges;
    private JLabel jLabel2;
    private JProgressBar jProgressBar1;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JTable jTable2;
    private JLabel labelTable;
    private JPanel leftPanel;
    private JComboBox<String> objectBox;
    private JButton refreshButton;
    private JButton revoke_all;
    private JButton revoke_g;
    private JButton revoke_g1;
    private JButton revoke_v;
    private JButton revoke_v1;
    private JPanel rightPanel;
    private JCheckBox systemCheck;
    private JTable tablePrivileges;
    private JPanel upPanel;
    private JComboBox<String> usersBox;
    private JList<String> userList;

    /**
     * Creates new form GrantManagerPanel
     */
    public GrantManagerPanel() {
        gr = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        no = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adm = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        enableElements = true;
        enabled_dBox = false;
        initComponents();
        relName = new Vector<>();
        relType = new Vector<>();
        relSystem = new Vector<Boolean>();
        relGranted = new Vector<Boolean>();
        fieldName = new Vector<>();
        fieldType = new Vector<>();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        tablePrivileges.setDefaultRenderer(Object.class, bctr);
        jTable2.setDefaultRenderer(Object.class, bctr);
        enabled_dBox = true;
        setEnableElements(true);
        load_connections();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        upPanel = new JPanel();
        interruptPanel = new JPanel();
        databaseBox = new JComboBox<>();
        refreshButton = new DefaultButton("Refresh");
        leftPanel = new JPanel();
        labelPrivileges = new JLabel();
        usersBox = new JComboBox<>();
        jScrollPane1 = new JScrollPane();
        userList = new JList<>();
        rightPanel = new JPanel();
        jLabel2 = new JLabel();
        revoke_v = new DefaultButton();
        revoke_g = new DefaultButton();
        revoke_all = new DefaultButton();
        grant_v = new DefaultButton();
        grant_g = new DefaultButton();
        grant_all = new DefaultButton();
        grant_option_v = new DefaultButton();
        grant_option_g = new DefaultButton();
        grant_option_all = new DefaultButton();
        objectBox = new JComboBox<>();
        filterBox = new JComboBox<>();
        filterField = new JTextField();
        jCheckBox1 = new JCheckBox();
        systemCheck = new JCheckBox();
        jScrollPane2 = new JScrollPane();
        tablePrivileges = new JTable();
        downPanel = new JPanel();
        labelTable = new JLabel();
        revoke_v1 = new DefaultButton();
        revoke_g1 = new DefaultButton();
        grant_v1 = new DefaultButton();
        grant_g1 = new DefaultButton();
        grant_option_v1 = new DefaultButton();
        grant_option_g1 = new DefaultButton();
        jScrollPane3 = new JScrollPane();
        jTable2 = new JTable();
        jProgressBar1 = new JProgressBar();
        cancelButton = new DefaultButton();

        databaseBox.setModel(new DefaultComboBoxModel<>(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        databaseBox.addActionListener(evt -> databaseBoxActionPerformed(evt));

        refreshButton.setIcon(GUIUtilities.loadIcon("Refresh16.png", true));
        refreshButton.addActionListener(evt -> refreshButtonActionPerformed(evt));

        upPanel.setLayout(new GridBagLayout());
        upPanel.add(databaseBox, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
        upPanel.add(refreshButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.LINE_END, GridBagConstraints.LINE_END, new Insets(5, 0, 5, 5), 0, 0));

        labelPrivileges.setText(bundleString("PrivelegesFor"));
        labelPrivileges.setOpaque(true);

        usersBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"Users", "Roles", "Views", "Triggers", "Procedures"})));
        usersBox.addActionListener(evt -> userBoxActionPerformed(evt));

        userList.addListSelectionListener(evt -> userListValueChanged());
        jScrollPane1.setViewportView(userList);

        leftPanel.setLayout(new GridBagLayout());
        leftPanel.add(labelPrivileges, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
        leftPanel.add(usersBox, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
        leftPanel.add(jScrollPane1, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        JSplitPane splitPane = new SplitPaneFactory().createHorizontal();
        splitPane.setDividerSize(6);

        jLabel2.setText(bundleString("GrantsOn"));
        jLabel2.setOpaque(true);

        revoke_v.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_vertical.png")));
        revoke_v.addActionListener(evt -> revoke_vActionPerformed(evt));

        revoke_g.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_gorisont.png")));
        revoke_g.addActionListener(evt -> revoke_gActionPerformed(evt));

        revoke_all.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_all.png")));
        revoke_all.addActionListener(evt -> revoke_allActionPerformed(evt));

        grant_v.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_vertical.png")));
        grant_v.addActionListener(evt -> grant_vActionPerformed(evt));

        grant_g.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_gorisont.png")));
        grant_g.addActionListener(evt -> grant_gActionPerformed(evt));

        grant_all.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_all.png")));
        grant_all.addActionListener(evt -> grant_allActionPerformed(evt));

        grant_option_v.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_vertical.png")));
        grant_option_v.addActionListener(evt -> grant_option_vActionPerformed(evt));

        grant_option_g.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_gorisont.png")));
        grant_option_g.addActionListener(evt -> grant_option_gActionPerformed(evt));

        grant_option_all.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_all.png")));
        grant_option_all.addActionListener(evt -> grant_option_allActionPerformed(evt));

        objectBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"AllObjects", "Tables", "Views", "Procedures"})));
        objectBox.addActionListener(evt -> objectBoxActionPerformed(evt));

        filterBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"DisplayAll", "GrantedOnly", "Non-grantedOnly"})));
        filterBox.addActionListener(evt -> filterBoxActionPerformed(evt));

        filterField.addActionListener(evt -> filterFieldActionPerformed(evt));

        jCheckBox1.setText(bundleString("InvertFilter"));
        jCheckBox1.addActionListener(evt -> jCheckBox1ActionPerformed(evt));

        systemCheck.setText(bundleString("ShowSystemTables"));
        systemCheck.addActionListener(evt -> systemCheckActionPerformed(evt));

        tablePrivileges.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{

                },
                new String[]{

                }
        ));
        tablePrivileges.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablePrivilegesMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tablePrivileges);

        labelTable.setText(bundleString("ColumnsOf"));
        labelTable.setOpaque(true);

        revoke_v1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_vertical.png"))); // NOI18N
        revoke_v1.addActionListener(evt -> revoke_v1ActionPerformed(evt));

        revoke_g1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_gorisont.png"))); // NOI18N
        revoke_g1.addActionListener(evt -> revoke_g1ActionPerformed(evt));

        grant_v1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_vertical.png"))); // NOI18N
        grant_v1.addActionListener(evt -> grant_v1ActionPerformed(evt));

        grant_g1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_gorisont.png"))); // NOI18N
        grant_g1.addActionListener(evt -> grant_g1ActionPerformed(evt));

        grant_option_v1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_vertical.png"))); // NOI18N
        grant_option_v1.addActionListener(evt -> grant_option_v1ActionPerformed(evt));

        grant_option_g1.setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_gorisont.png"))); // NOI18N
        grant_option_g1.addActionListener(evt -> grant_option_g1ActionPerformed(evt));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{}
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable2);
        GroupLayout downPanelLayout = new GroupLayout(downPanel);
        downPanel.setLayout(downPanelLayout);
        downPanelLayout.setHorizontalGroup(
                downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(labelTable, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(downPanelLayout.createSequentialGroup()
                                .addComponent(revoke_v1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(revoke_g1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(grant_v1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(grant_g1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(grant_option_v1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(grant_option_g1)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(jScrollPane3, GroupLayout.Alignment.TRAILING)
        );
        downPanelLayout.setVerticalGroup(
                downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(downPanelLayout.createSequentialGroup()
                                .addComponent(labelTable, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(revoke_v1)
                                                .addComponent(revoke_g1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(grant_v1, GroupLayout.Alignment.TRAILING)
                                                .addComponent(grant_g1, GroupLayout.Alignment.TRAILING))
                                        .addComponent(grant_option_v1, GroupLayout.Alignment.TRAILING)
                                        .addComponent(grant_option_g1, GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE))
        );

        cancelButton.setText(bundleString("CancelFill"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        interruptPanel.setLayout(new GridBagLayout());
        interruptPanel.add(jProgressBar1, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
        interruptPanel.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));

        GroupLayout rightPanelLayout = new GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
                rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2)
                        .addComponent(downPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(rightPanelLayout.createSequentialGroup()
                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(objectBox, GroupLayout.Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                                .addComponent(revoke_v)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(revoke_g)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(revoke_all)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                                .addComponent(grant_v)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(grant_g)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(grant_all)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(grant_option_v))
                                                        .addComponent(filterBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                                .addGap(6, 6, 6)
                                                                .addComponent(grant_option_g)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(grant_option_all)
                                                                .addGap(0, 0, Short.MAX_VALUE))
                                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                                .addGap(9, 9, 9)
                                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                                                .addComponent(jCheckBox1)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                        .addComponent(filterField)))))
                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(systemCheck))
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        rightPanelLayout.setVerticalGroup(
                rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(rightPanelLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(revoke_v)
                                                .addComponent(revoke_g, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(revoke_all)
                                                .addComponent(grant_v)
                                                .addComponent(grant_g))
                                        .addComponent(grant_all)
                                        .addComponent(grant_option_v)
                                        .addComponent(grant_option_g)
                                        .addComponent(grant_option_all))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(objectBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(filterField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(rightPanelLayout.createSequentialGroup()
                                                .addGroup(rightPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(jCheckBox1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(systemCheck))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 247, GroupLayout.DEFAULT_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(downPanel, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE))
        );

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        this.setLayout(new GridBagLayout());
        this.add(upPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        this.add(interruptPanel, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        this.add(splitPane, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    private void databaseBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if (enabled_dBox) {
            querySender = new DefaultStatementExecutor(listConnections.get(databaseBox.getSelectedIndex()), true);
            querySender.setCommitMode(false);
            dbc = listConnections.get(databaseBox.getSelectedIndex());
            con = ConnectionManager.getConnection(listConnections.get(databaseBox.getSelectedIndex()));
            load_userList();
        }
    }

    private void userBoxActionPerformed(java.awt.event.ActionEvent evt) {
        load_userList();
    }

    private void userListValueChanged() {
        if (userlistModel.size() > 0)
            if (userList.getSelectedValue() != null) {
                act = Action.CREATE_TABLE;
                execute_thread();
            }
    }

    private void objectBoxActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.CREATE_TABLE;
        execute_thread();
    }

    private void filterBoxActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.CREATE_TABLE;
        execute_thread();
    }

    private void filterFieldActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.CREATE_TABLE;
        execute_thread();
    }

    private void systemCheckActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.CREATE_TABLE;
        execute_thread();
    }

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.CREATE_TABLE;
        execute_thread();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setEnableElements(true);
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
        load_connections();
    }

    private void revoke_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.NO_GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void revoke_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(0, row, col);
            }
        }
    }

    private void revoke_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.NO_ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(1, row, col);
            }
        }
    }

    private void grant_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_option_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    private void grant_option_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(2, row, col);
            }
        }
    }

    private void grant_option_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = Action.ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    private void tablePrivilegesMouseClicked(java.awt.event.MouseEvent evt) {
        if (enableElements) {
            int row = tablePrivileges.getSelectedRow();
            if (evt.getClickCount() > 1) {
                int col = tablePrivileges.getSelectedColumn();
                if (col > 0) {
                    if (tablePrivileges.getValueAt(row, col).equals(gr)) {
                        grant_on_role(2, row, col);
                    } else if (tablePrivileges.getValueAt(row, col).equals(adm)) {
                        grant_on_role(0, row, col);
                    } else {
                        grant_on_role(1, row, col);
                    }
                }
            }
            if (row >= 0) {
                setVisiblePanelOfTable(!relType.elementAt(row).equals(objectBox.getItemAt(3)));
            }
        }
    }

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {
        if (enableElements) {
            int row = tablePrivileges.getSelectedRow();
            int row2 = jTable2.getSelectedRow();
            if (evt.getClickCount() > 1) {
                int col = jTable2.getSelectedColumn();
                if (col > 1) {
                    if (jTable2.getValueAt(row2, col).equals(gr)) {
                        grant_on_role(2, row, col, row2);
                    } else if (jTable2.getValueAt(row2, col).equals(adm)) {
                        grant_on_role(0, row, col, row2);
                    } else {
                        grant_on_role(1, row, col, row2);
                    }
                }
            }

        }
    }

    private void revoke_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = jTable2.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(0, row2, col, row);
            }
    }

    private void revoke_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = jTable2.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grant_on_role(0, row2, col, row);
            }
    }

    private void grant_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = jTable2.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(1, row2, col, row);
            }
    }

    private void grant_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = jTable2.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grant_on_role(1, row2, col, row);
            }
    }

    private void grant_option_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = jTable2.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(2, row2, col, row);
            }
    }

    private void grant_option_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = jTable2.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grant_on_role(2, row2, col, row);
            }
    }

    public void load_connections() {
        boolean selected = databaseBox.getSelectedIndex() >= 0;
        enabled_dBox = false;
        setEnableElements(true);
        String item = "";
        if (selected)
            item = databaseBox.getItemAt(databaseBox.getSelectedIndex());
        selected = selected && item != "Item 1";
        databaseBox.removeAllItems();
        List<DatabaseConnection> cons;
        listConnections = new ArrayList<DatabaseConnection>();
        cons = ((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll();
        connected = false;
        for (int i = 0; i < cons.size(); i++) {
            if (cons.get(i).isConnected()) {
                connected = true;
                databaseBox.addItem(cons.get(i).getName());
                listConnections.add(cons.get(i));
                enabled_dBox = true;
                if (!selected) {
                    item = cons.get(i).getName();
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

    void load_userList() {
        if (connected) {
            userlistModel = new DefaultListModel();
            userList.setModel(userlistModel);
            int ind_userBox = usersBox.getSelectedIndex();
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
                default:
                    break;
            }
            if (userlistModel.size() > 0)
                userList.setSelectedIndex(0);
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

    void get_users() {
        Connection connection = null;
        try {
            connection = con.unwrap(Connection.class);
        } catch (SQLException e) {
            Log.error("error get connection for getting users in grant manager:", e);
        }

        URL[] urls = new URL[0];
        Class clazzdb = null;
        Object odb = null;
        try {
            urls = MiscUtils.loadURLs("./lib/fbplugin-impl.jar;../lib/fbplugin-impl.jar");
            ClassLoader cl = new URLClassLoader(urls, connection.getClass().getClassLoader());
            clazzdb = cl.loadClass("biz.redsoft.FBUserManagerImpl");
            odb = clazzdb.newInstance();
        } catch (ClassNotFoundException e) {
            Log.error("Error get users in Grant Manager:", e);
        } catch (IllegalAccessException e) {
            Log.error("Error get users in Grant Manager:", e);
        } catch (InstantiationException e) {
            Log.error("Error get users in Grant Manager:", e);
        } catch (MalformedURLException e) {
            Log.error("Error get users in Grant Manager:", e);
        }
        IFBUserManager userManager = (IFBUserManager) odb;
        userManager = getUserManager(userManager, listConnections.get(databaseBox.getSelectedIndex()));
        Map<String, IFBUser> users;
        try {
            users = userManager.getUsers();
            for (IFBUser u : users.values()) {
                userlistModel.addElement(u.getUserName());
            }
            userlistModel.addElement("PUBLIC");
        } catch (Exception e) {
            System.out.println(e.toString());
            GUIUtilities.displayErrorMessage(e.toString());
        }
    }

    void get_roles() {
        String query = "SELECT RDB$ROLE_NAME FROM RDB$ROLES order by 1";
        get_user_list(query);
    }

    void get_views_for_userlist() {
        String query = "Select RDB$RELATION_NAME from RDB$RELATIONS" +
                " WHERE RDB$RELATION_TYPE = 1 order by 1";
        get_user_list(query);
    }

    void get_user_list(String query) {
        try {
            ResultSet result = querySender.execute(QueryTypes.SELECT, query, -1).getResultSet();
            while (result.next()) {
                String role = result.getString(1);
                userlistModel.addElement(role);
            }
            result.close();
            querySender.releaseResources();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void get_triggers_for_userlist() {
        String query = "Select RDB$TRIGGER_NAME from RDB$TRIGGERS order by 1";
        get_user_list(query);
    }

    void get_procedures_for_userlist() {
        String query = "Select RDB$PROCEDURE_NAME from RDB$PROCEDURES order by 1";
        get_user_list(query);
    }

    void load_table() {
        tablePrivileges.setModel(new RoleTableModel(headers, 0));
        relName.removeAllElements();
        relType.removeAllElements();
        relSystem.removeAllElements();
        relGranted.removeAllElements();
        if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 1)
            getTables();
        if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 2)
            getViews();
        if (objectBox.getSelectedIndex() == 0 || objectBox.getSelectedIndex() == 3)
            getProcedures();
        jProgressBar1.setMaximum(relName.size());
        for (int i = 0, g = 0; i < relName.size() && !enableElements; g++, i++) {
            jProgressBar1.setValue(g);
            try {
                String s = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION,RDB$FIELD_NAME from RDB$USER_PRIVILEGES\n" +
                        "where (rdb$Relation_name='" + relName.elementAt(i) + "') and (rdb$user='"
                        + userList.getSelectedValue().trim() + "')";
                ResultSet rs1 = querySender.execute(QueryTypes.SELECT, s, -1).getResultSet();
                Vector<Object> roleData = new Vector<Object>();
                Object[] obj = {relName.elementAt(i), Color.BLACK};
                if (relSystem.elementAt(i))
                    obj[1] = Color.RED;
                roleData.add(obj);
                for (int k = 0; k < 7; k++)
                    roleData.add(no);
                boolean adding = true;
                while (rs1.next()) {
                    relGranted.set(i, true);
                    if (filterBox.getSelectedIndex() == 0 || (filterBox.getSelectedIndex() == 1) == relGranted.elementAt(i)) {
                        try {
                            if (rs1.getString(3) == null) {
                                String grant = rs1.getString(1).trim();
                                int ind = grants.indexOf(grant);
                                if (ind != 7) {
                                    Object gr_opt = rs1.getObject(2);
                                    if (gr_opt == null)
                                        gr_opt = 0;
                                    if (gr_opt.equals(0)) {
                                        roleData.set(ind + 1, gr);
                                    } else
                                        roleData.set(ind + 1, adm);
                                }
                            }
                        } catch (Exception e) {
                            Log.error(e.getMessage());
                        }
                    } else {
                        adding = false;
                        break;
                    }
                }
                rs1.close();
                if (adding)
                    adding = (filterBox.getSelectedIndex() == 0 || (filterBox.getSelectedIndex() == 1) == relGranted.elementAt(i));
                if (adding)
                    ((RoleTableModel) tablePrivileges.getModel()).addRow(roleData);
                else {
                    removeRow(i);
                    i--;
                }
                querySender.releaseResources();
            } catch (NullPointerException e) {
                Log.error(bundleString("connection.close"));
            } catch (SQLException e) {
                Log.error(e.getMessage());
            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            }

            if (GUIUtilities.getCentralPane(TITLE) == null)
                setEnableElements(true);
        }
        setEnableElements(true);
        setVisiblePanelOfTable(false);

    }

    void load_table2(String rname) {
        jTable2.setModel(new RoleTableModel(headers2, 0));
        fieldName = new Vector<>();
        fieldType = new Vector<>();
        try {
            String query = "Select RF.RDB$FIELD_NAME,F.RDB$FIELD_TYPE\n" +
                    "from RDB$RELATION_FIELDS AS RF left join RDB$FIELDS AS F\n" +
                    "ON F.RDB$FIELD_NAME=RF.RDB$FIELD_SOURCE\n" +
                    "WHERE RF.RDB$RELATION_NAME='" + rname + "'";
            ResultSet rs = querySender.execute(QueryTypes.SELECT, query, -1).getResultSet();
            while (rs.next()) {
                String name = rs.getString(1).trim();
                String type = getTypeField(rs.getInt(2));
                fieldName.addElement(name);
                fieldType.addElement(type);
            }
            rs.close();
            querySender.releaseResources();
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        for (int i = 0; i < fieldName.size(); i++)
            try {
                String s = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION from RDB$USER_PRIVILEGES\n" +
                        "where (rdb$Relation_name='" + rname + "') and (rdb$user='" + userList.getSelectedValue().trim() + "') and\n" +
                        "(RDB$FIELD_NAME='" + fieldName.elementAt(i) + "')";
                ResultSet rs1 = querySender.execute(QueryTypes.SELECT, s, -1).getResultSet();
                Vector<Object> roleData = new Vector<Object>();
                roleData.add(fieldName.elementAt(i));
                roleData.add(fieldType.elementAt(i));
                for (int k = 0; k < 2; k++)
                    roleData.add(no);
                ((RoleTableModel) jTable2.getModel()).addRow(roleData);
                while (rs1.next()) {
                    String grant = rs1.getString(1).trim();
                    int ind = grants.indexOf(grant);
                    if (ind == 1)
                        if (rs1.getObject(2).equals(0)) {
                            jTable2.setValueAt(gr, i, 2);
                        } else
                            jTable2.getModel().setValueAt(adm, i, 2);
                    if (ind == 5)
                        if (rs1.getObject(2).equals(0)) {
                            jTable2.setValueAt(gr, i, 3);
                        } else
                            jTable2.getModel().setValueAt(adm, i, 3);

                }
                rs1.close();
                querySender.releaseResources();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
    }

    void getTables() {
        String query = "Select RDB$RELATION_NAME,RDB$SYSTEM_FLAG from RDB$RELATIONS" +
                " WHERE RDB$RELATION_TYPE != 1 order by 1";
        String type = objectBox.getItemAt(1);
        add_relations(query, type);
    }

    void getViews() {
        String query = "Select RDB$RELATION_NAME,RDB$SYSTEM_FLAG from RDB$RELATIONS" +
                " WHERE RDB$RELATION_TYPE = 1 order by 1";
        String type = objectBox.getItemAt(2);
        add_relations(query, type);
    }

    void getProcedures() {
        String query = "Select RDB$PROCEDURE_NAME,RDB$SYSTEM_FLAG from RDB$PROCEDURES order by 1";
        String type = objectBox.getItemAt(3);
        add_relations(query, type);
    }

    void removeRow(int i) {
        relName.remove(i);
        relType.remove(i);
        relSystem.remove(i);
        relGranted.remove(i);
    }

    void addRow(String name, String type, boolean system_flag, boolean grant_flag) {
        relName.add(name);
        relType.add(type);
        relSystem.add(system_flag);
        relGranted.add(grant_flag);
    }

    void add_relations(String query, String type) {
        Vector<String> rname = new Vector<>();
        Vector<Boolean> sflag = new Vector<>();
        try {
            ResultSet rs = querySender.execute(QueryTypes.SELECT, query, -1).getResultSet();
            while (rs.next()) {
                String name = rs.getString(1).trim();
                boolean system_flag = rs.getInt(2) == 1;
                rname.addElement(name);
                sflag.addElement(system_flag);
            }
            rs.close();
            querySender.releaseResources();
            for (int i = 0; i < rname.size(); i++) {
                String name = rname.elementAt(i);
                boolean system_flag = sflag.elementAt(i);
                boolean granted_flag = false;
                if (!system_flag || system_flag == systemCheck.isSelected()) {
                    if (!jCheckBox1.isSelected() == name.contains(filterField.getText()))
                        addRow(name, type, system_flag, granted_flag);
                }
            }
        } catch (SQLException e) {
            GUIUtilities.displayErrorMessage(e.getMessage());

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    void setEnableElements(boolean enable) {
        enableComponents(GUIUtilities.getParentFrame(), enable);
        enableElements = enable;
        databaseBox.setEnabled(enable);
        objectBox.setEnabled(enable);
        jCheckBox1.setEnabled(enable);
        filterBox.setEnabled(enable);
        filterField.setEnabled(enable);
        grant_all.setEnabled(enable);
        grant_g.setEnabled(enable);
        grant_g1.setEnabled(enable);
        grant_option_all.setEnabled(enable);
        grant_option_g.setEnabled(enable);
        grant_option_g1.setEnabled(enable);
        grant_option_v.setEnabled(enable);
        grant_option_v1.setEnabled(enable);
        grant_v.setEnabled(enable);
        grant_v1.setEnabled(enable);
        refreshButton.setEnabled(enable);
        revoke_all.setEnabled(enable);
        revoke_g.setEnabled(enable);
        revoke_g1.setEnabled(enable);
        revoke_v.setEnabled(enable);
        revoke_v1.setEnabled(enable);
        systemCheck.setEnabled(enable);
        usersBox.setEnabled(enable);
        userList.setEnabled(enable);
        cancelButton.setVisible(!enable);
        cancelButton.setEnabled(!enable);
        jProgressBar1.setVisible(!enable);
        if (enable)
            jProgressBar1.setValue(0);
    }

    void isClose() {
        if (GUIUtilities.getCentralPane(TITLE) == null)
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

    public void run() {
        int col;
        switch (act) {
            case CREATE_TABLE:
                load_table();
                break;
            case ALL_GRANTS_TO_ALL_OBJECTS: {
                jProgressBar1.setMaximum(relName.size());
                for (int row = 0; row < relName.size() && !enableElements; row++) {
                    jProgressBar1.setValue(row);
                    isClose();
                    grant_all_on_role(1, row);

                }
                jProgressBar1.setValue(0);
                setEnableElements(true);
            }
            break;
            case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                jProgressBar1.setMaximum(relName.size());
                for (int row = 0; row < relName.size() && !enableElements; row++) {
                    jProgressBar1.setValue(row);
                    isClose();
                    grant_all_on_role(2, row);

                }
                jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
            case NO_ALL_GRANTS_TO_ALL_OBJECTS:
                jProgressBar1.setMaximum(relName.size());
                for (int row = 0; row < relName.size() && !enableElements; row++) {
                    jProgressBar1.setValue(row);
                    isClose();
                    grant_all_on_role(0, row);

                }
                jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
            case GRANT_TO_ALL_OBJECTS:
                col = tablePrivileges.getSelectedColumn();
                jProgressBar1.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grant_on_role(1, row, col);
                    }
                jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
            case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                col = tablePrivileges.getSelectedColumn();
                jProgressBar1.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grant_on_role(2, row, col);
                    }
                jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
            case NO_GRANT_TO_ALL_OBJECTS:
                col = tablePrivileges.getSelectedColumn();
                jProgressBar1.setMaximum(relName.size());
                if (col > 0)
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grant_on_role(0, row, col);
                    }
                jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
        }
    }

    void grant_query(String query, Icon icon, int row, int col, JTable t) {
        try {
            querySender.execute(QueryTypes.GRANT, query);
            querySender.execute(QueryTypes.COMMIT, (String) null);
            t.setValueAt(icon, row, col);
            querySender.releaseResources();
        } catch (NullPointerException e) {
            Log.error(bundleString("connection.close"));
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    void revoke_execute(int row) {
        String query = "REVOKE EXECUTE ON PROCEDURE \"" + relName.elementAt(row) + "\" FROM \""
                + userList.getSelectedValue() + "\";";
        grant_query(query, no, row, col_execute, tablePrivileges);
    }

    void grant_execute(int row) {
        String query = "GRANT EXECUTE ON PROCEDURE \"" +
                relName.elementAt(row) + "\" TO \"" + userList.getSelectedValue() + "\";";
        grant_query(query, gr, row, col_execute, tablePrivileges);
    }

    void grant_execute_admin(int row) {
        String query = "GRANT EXECUTE ON PROCEDURE \"" + relName.elementAt(row) +
                "\" TO \"" + userList.getSelectedValue() + "\" WITH GRANT OPTION;";
        grant_query(query, adm, row, col_execute, tablePrivileges);
    }

    void grant_all_query(String query, Icon icon, int row, int grantt) {
        try {
            querySender.execute(QueryTypes.GRANT, query, -1);
            for (int i = 1; i < headers.length; i++)
                if (i != col_execute && i != col_usage)
                    tablePrivileges.setValueAt(icon, row, i);
            querySender.execute(QueryTypes.COMMIT, (String) null);
            querySender.releaseResources();
        } catch (Exception e) {
            Log.error(e.getMessage());
            for (int i = 1; i < headers.length; i++) {
                grant_case(grantt, row, i);
            }
        }
    }

    void grant_case(int grantt, int row, int col) {
        switch (grantt) {
            case 0:
                revoke(row, col);
                break;
            case 1:
                grant(row, col);
                break;
            case 2:
                grant_admin(row, col);
                break;
        }
    }

    void revoke_all(int row) {
        String query = "REVOKE ALL ON \"" + relName.elementAt(row) + "\" FROM \"" + userList.getSelectedValue() + "\";";
        grant_all_query(query, no, row, 0);

    }

    void grant_all(int row) {
        String query = "GRANT ALL ON \"" + relName.elementAt(row)
                + "\" TO \"" + userList.getSelectedValue() + "\";";
        grant_all_query(query, gr, row, 1);
    }

    void grant_all_admin(int row) {
        String query = "GRANT ALL ON \"" + relName.elementAt(row)
                + "\" TO \"" + userList.getSelectedValue() + "\" WITH GRANT OPTION;";
        grant_all_query(query, adm, row, 2);
    }

    void revoke(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            String query = "REVOKE " + headers[col] + " ON \"" + relName.elementAt(row)
                    + "\" FROM \"" + userList.getSelectedValue() + "\";";
            grant_query(query, no, row, col, tablePrivileges);
        }
    }

    void grant(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            String query = "GRANT " + headers[col] + " ON \"" + relName.elementAt(row)
                    + "\" TO \"" + userList.getSelectedValue() + "\";";
            grant_query(query, gr, row, col, tablePrivileges);
        }
    }

    void grant_admin(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            String query = "GRANT " + headers[col] + " ON \"" + relName.elementAt(row)
                    + "\" TO \"" + userList.getSelectedValue() + "\" WITH GRANT OPTION;";
            grant_query(query, adm, row, col, tablePrivileges);
        }
    }

    void grant_all_on_role(int grantt, int row) {
        if (row < tablePrivileges.getRowCount()) {
            switch (grantt) {
                case 0:
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        revoke_all(row);
                    } else {
                        revoke_execute(row);
                    }
                    break;
                case 1:
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        revoke_all(row);
                        grant_all(row);
                    } else {
                        revoke_execute(row);
                        grant_execute(row);
                    }

                    break;
                case 2:
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        grant_all_admin(row);
                    } else {
                        grant_execute_admin(row);
                    }
                    break;
            }
        }
    }

    void grant_on_role(int grantt, int row, int col) {
        if (row < tablePrivileges.getRowCount()) {
            switch (grantt) {
                case 0:
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        revoke(row, col);
                    } else if (headers[col].equals("Execute")) {
                        revoke_execute(row);
                    }
                    break;
                case 1:
                    if (tablePrivileges.getValueAt(row, col).equals(adm)) {
                        if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                            revoke(row, col);
                        } else if (headers[col].equals("Execute")) {
                            revoke_execute(row);
                        }
                    }
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        grant(row, col);
                    } else if (headers[col].equals("Execute")) {
                        grant_execute(row);
                    }
                    break;
                case 2:
                    if (!relType.elementAt(row).equals(objectBox.getItemAt(3))) {
                        grant_admin(row, col);
                    } else if (headers[col].equals("Execute")) {
                        grant_execute_admin(row);
                    }
                    break;
            }
        }
    }

    void grant_on_role(int grantt, int row, int col, int row2) {
        String query;
        if (row < tablePrivileges.getRowCount()) {
            switch (grantt) {
                case 0:
                    query = "REVOKE " + headers2[col] + " (" + fieldName.elementAt(row2) + ")"
                            + " ON \"" + relName.elementAt(row) + "\" FROM \"" + userList.getSelectedValue() + "\";";
                    grant_query(query, no, row2, col, jTable2);
                    break;
                case 1:
                    if (jTable2.getValueAt(row2, col).equals(adm)) {
                        query = "REVOKE " + headers2[col] + " (" + fieldName.elementAt(row2) + ")"
                                + " ON \"" + relName.elementAt(row) + "\" FROM \"" + userList.getSelectedValue() + "\";";
                        grant_query(query, no, row2, col, jTable2);
                    }
                    query = "GRANT " + headers2[col] + " (" + fieldName.elementAt(row2) + ")"
                            + " ON \"" + relName.elementAt(row) + "\" TO \"" + userList.getSelectedValue() + "\";";
                    grant_query(query, gr, row2, col, jTable2);

                    break;
                case 2:
                    query = "GRANT " + headers2[col] + " (" + fieldName.elementAt(row2) + ")"
                            + " ON \"" + relName.elementAt(row) + "\" TO \"" + userList.getSelectedValue() + "\" WITH GRANT OPTION;";
                    grant_query(query, adm, row2, col, jTable2);
                    break;
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

    void execute_thread() {
        if (enableElements) {
            setEnableElements(false);
            Runnable r = new ThreadOfGrantManager(this);
            Thread t = new Thread(r);
            t.start();
        }
    }

    void setVisiblePanelOfTable(boolean flag) {
        if (flag) {
            int row = tablePrivileges.getSelectedRow();
            labelTable.setText(bundleString("ColumnsOf") + "[" + relName.elementAt(row) + "]");
            load_table2(relName.elementAt(row));
            obj_index = row;
        } else {
            labelTable.setText(bundleString("ColumnsOf"));
            jTable2.setModel(new RoleTableModel(headers2, 0));
        }
    }

    String getTypeField(int type) {
        switch (type) {
            case 7:
                return "SMALLINT";
            case 8:
                return "INTEGER";
            case 10:
                return "FLOAT";
            case 12:
                return "DATE";
            case 13:
                return "TIME";
            case 14:
                return "CHAR";
            case 16:
                return "BIGINT";
            case 27:
                return "DOUBLE PRECISION";
            case 35:
                return "TIMESTAMP";
            case 37:
                return "VARCHAR";
            case 261:
                return "BLOB";
            default:
                return "UNKNOWN";
        }
    }

    public String bundleString(String key) {
        return Bundles.get(GrantManagerPanel.class, key);
    }

    public String[] bundleStrings(String[] key) {
        for (int i = 0; i < key.length; i++) {
            if (key.length > 0)
                key[i] = bundleString(key[i]);
        }
        return key;
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
}
