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
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.sql.SqlStatementResult;
import org.executequery.sql.sqlbuilder.*;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.underworldlabs.swing.EQCheckCombox;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.IconUtilities;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

/**
 * @author mikhan808
 */
public class GrantManagerPanel extends JPanel implements TabView {

    public static final String TITLE = Bundles.get(GrantManagerPanel.class, "GrantManager");
    public static final String FRAME_ICON = "grant_manager_16.png";
    public DatabaseConnection dbc;
    boolean connected;
    DefaultListModel userlistModel;
    boolean enabled_dBox;
    boolean enableElements;
    Vector<NamedObject> objectVector;
    Map<NamedObject, Vector<Object>> tableMap;
    Vector<DatabaseColumn> columnVector;
    Map<DatabaseColumn, Vector<Object>> tableForColumnsMap;
    String grants = "SUDIRXGA";
    String[] headers = {bundleString("Object"), "Select", "Update", "Delete", "Insert", "References", "Execute", "Usage"};
    int firstGrantColumn = 1;
    String[] headers2 = {bundleString("Field"), bundleString("Type"), "Update", "References"};
    Icon gr, no, adm, fieldGr;
    Icon[] icons;
    int obj_index;
    DefaultStatementExecutor querySender;
    int col_execute = 6;
    int col_usage = 7;
    private JButton cancelButton;
    String[] objectTypes;
    private JPanel downPanel;
    private JComboBox<String> filterBox;
    private JTextField filterField;
    private JCheckBox invertFilterCheckBox;
    private JProgressBar jProgressBar1;
    private JScrollPane recipientsOfPrivilegesScroll;
    private JScrollPane privilegesScroll;
    private JScrollPane privilegesForFieldScroll;
    private JTable privilegesForFieldTable;
    JToolBar grantFieldsToolbar;
    private JButton refreshButton;
    private JPanel rightPanel;
    private JCheckBox systemCheck;
    private JTable tablePrivileges;
    int act;
    private JList<String> userList;
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
    JToolBar grantToolbar;
    // private JPanel interruptPanel;
    private JComboBox<DatabaseConnection> databaseBox;
    private EQCheckCombox objectBox;
    private JComboBox<String> userTypeBox;
    private boolean isClose = false;
    private RolloverButton[] grantFieldButtons;
    private RolloverButton[] grantButtons;
    @SuppressWarnings("unchecked")

    private int buttonSize = 20;

    /**
     * Creates new form GrantManagerPanel
     */
    public GrantManagerPanel() {
        gr = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        no = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        adm = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        fieldGr = GUIUtilities.loadIcon(BrowserConstants.FIELD_GRANT_IMAGE);
        icons = new Icon[]{no, gr, adm, fieldGr};
        enableElements = true;
        enabled_dBox = false;
        initComponents();
        objectVector = new Vector<>();
        tableMap = new HashMap<>();
        tableForColumnsMap = new HashMap<>();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        tablePrivileges.setDefaultRenderer(Object.class, bctr);
        privilegesForFieldTable.setDefaultRenderer(Object.class, bctr);
        enabled_dBox = true;
        setEnableElements(true);
        load_connections();
    }

    private void initComponents() {

        databaseBox = new JComboBox<>();
        refreshButton = new JButton(bundleString("Refresh"));
        userTypeBox = new JComboBox<>();
        recipientsOfPrivilegesScroll = new JScrollPane();
        userList = new JList<>();
        rightPanel = new JPanel();
        grantFieldButtons = new RolloverButton[6];
        for (int i = 0; i < grantFieldButtons.length; i++) {
            grantFieldButtons[i] = new RolloverButton();
            grantFieldButtons[i].setMouseEnteredContentAreaFill(false);
            switch (i) {
                case 0:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/no_grant_vertical.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> revoke_v1ActionPerformed());
                    break;
                case 1:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/no_grant_gorisont.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> revoke_g1ActionPerformed());

                    break;
                case 2:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/grant_vertical.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_v1ActionPerformed());

                    break;
                case 3:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/grant_gorisont.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_g1ActionPerformed());

                    break;
                case 4:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/admin_option_vertical.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_option_v1ActionPerformed());

                    break;
                case 5:
                    grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/admin_option_gorisont.svg", buttonSize)); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_option_g1ActionPerformed());
                    break;
            }
        }

        objectBox = new EQCheckCombox();
        filterBox = new JComboBox<>();
        filterField = new JTextField();
        invertFilterCheckBox = new JCheckBox();
        systemCheck = new JCheckBox();
        privilegesScroll = new JScrollPane();
        tablePrivileges = new JTable();
        downPanel = new JPanel();
        grantButtons = new RolloverButton[9];
        for (int i = 0; i < grantButtons.length; i++) {
            grantButtons[i] = new RolloverButton();
            grantButtons[i].setMouseEnteredContentAreaFill(false);
            switch (i) {
                case 0:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/no_grant_vertical.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> revokeVertical());
                    break;
                case 1:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/no_grant_gorisont.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> revoke_gActionPerformed());
                    break;
                case 2:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/no_grant_all.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> revokeAll());
                    break;
                case 3:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/grant_vertical.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grantVertical());
                    break;
                case 4:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/grant_gorisont.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grant_gActionPerformed());
                    break;
                case 5:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/grant_all.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grantAll());
                    break;
                case 6:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/admin_option_vertical.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grantVerticalWithGrantOption());
                    break;
                case 7:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/admin_option_gorisont.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grant_option_gActionPerformed());
                    break;
                case 8:
                    grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/admin_option_all.svg", buttonSize));
                    grantButtons[i].addActionListener(evt -> grantAllWithGrantOption());
                    break;
            }
        }
        privilegesForFieldScroll = new JScrollPane();
        privilegesForFieldTable = new JTable();
        jProgressBar1 = new JProgressBar();
        cancelButton = new JButton();
        grantToolbar = new JToolBar();
        grantToolbar.setFloatable(false);

        grantFieldsToolbar = new JToolBar();
        grantFieldsToolbar.setFloatable(false);
        databaseBox.addActionListener(evt -> databaseBoxActionPerformed());

        refreshButton.setIcon(GUIUtilities.loadIcon("Refresh16.png", true));
        refreshButton.addActionListener(evt -> refreshButtonActionPerformed());

        String[] recipients = new String[]{"Users", "Roles", "Views", "Triggers", "Procedures", "Functions", "Packages"};
        userTypeBox.setModel(new DefaultComboBoxModel<>(bundleStrings(recipients)));
        userTypeBox.addActionListener(evt -> userBoxActionPerformed());

        userList.addListSelectionListener(evt -> userListValueChanged());
        recipientsOfPrivilegesScroll.setViewportView(userList);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(6);


        objectTypes = bundleStringsOf("Tables", "GlobalTemporaries", "Views", "Procedures", "Functions", "Packages", "Generators", "Exceptions");
        for (String obj : objectTypes) {
            objectBox.getModel().addElement(obj);
            objectBox.getModel().addCheck(obj);
        }
        objectBox.getModel().addListCheckListener(new ListCheckListener() {
            @Override
            public void removeCheck(ListEvent listEvent) {
                objectBoxActionPerformed();
            }

            @Override
            public void addCheck(ListEvent listEvent) {
                objectBoxActionPerformed();
            }
        });

        filterBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"DisplayAll", "GrantedOnly", "Non-grantedOnly"})));
        filterBox.addActionListener(evt -> filterBoxActionPerformed(evt));

        filterField.addActionListener(evt -> filterFieldActionPerformed());

        invertFilterCheckBox.setText(bundleString("InvertFilter"));
        invertFilterCheckBox.addActionListener(evt -> jCheckBox1ActionPerformed(evt));

        systemCheck.setText(bundleString("ShowSystemObjects"));
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
        privilegesScroll.setViewportView(tablePrivileges);


        privilegesForFieldTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{}
        ));
        privilegesForFieldTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        privilegesForFieldScroll.setViewportView(privilegesForFieldTable);

        cancelButton.setText(bundleString("CancelFill"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        //leftPanel.setBorder(BorderFactory.createTitledBorder(bundleString("PrivelegesFor")));
        rightPanel.setBorder(BorderFactory.createTitledBorder(bundleString("GrantsOn")));
        downPanel.setBorder(BorderFactory.createTitledBorder("ColumnsOf"));

        /*JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());*/

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        setLayout(new GridBagLayout());
        gbh.defaults();
        for (int i = 0; i < grantButtons.length; i++)
            grantToolbar.add(grantButtons[i]);


        //gbh.nextCol().fillHorizontally().setMaxWeightX().insertEmptyGap(buttonPanel);


        gbh.defaults();

        gbh.addLabelFieldPair(this, Bundles.getCommon("connection"), databaseBox, null);

        gbh.addLabelFieldPair(this, bundleString("PrivelegesFor"), userTypeBox, null, true, false);

        add(splitPane, gbh.nextCol().fillBoth().spanX().setMaxWeightY().setHeight(2).get());

        add(recipientsOfPrivilegesScroll, gbh.nextRowFirstCol().setWidth(2).setHeight(1).fillBoth().setMaxWeightY().setMaxWeightX().get());
        add(cancelButton, gbh.nextRowFirstCol().setLabelDefault().get());
        add(jProgressBar1, gbh.nextColWidth().fillHorizontally().spanX().get());
        gbh.defaults();
        rightPanel.setLayout(new GridBagLayout());

        rightPanel.add(objectBox, gbh.nextRowFirstCol().fillHorizontally().setMinWeightY().setHeight(1).get());

        rightPanel.add(systemCheck, gbh.nextRow().get());

        rightPanel.add(filterBox, gbh.previousRow().nextCol().fillHorizontally().get());

        rightPanel.add(filterField, gbh.nextCol().fillHorizontally().get());

        rightPanel.add(invertFilterCheckBox, gbh.nextRow().get());

        rightPanel.add(refreshButton, gbh.previousRow().nextCol().setLabelDefault().get());

        gbh.nextRow();

        rightPanel.add(grantToolbar, gbh.nextRowFirstCol().fillHorizontally().spanX().get());

        rightPanel.add(privilegesScroll, gbh.nextRowFirstCol().fillBoth().setMaxWeightX().setMaxWeightY().setWidth(6).get());

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setDefaultsStatic().defaults();
        downPanel.setLayout(new GridBagLayout());
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldsToolbar.add(grantFieldButtons[i]);
        downPanel.add(grantFieldsToolbar, gridBagHelper.fillHorizontally().spanX().get());
        downPanel.add(privilegesForFieldScroll, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        splitPane.setTopComponent(rightPanel);
        splitPane.setBottomComponent(downPanel);
        splitPane.setResizeWeight(0.8);
    }

    void setEnableElements(boolean enable) {
        //enableComponents(GUIUtilities.getParentFrame(), enable);
        enableElements = enable;
        databaseBox.setEnabled(enable);
        objectBox.setEnabled(enable);
        invertFilterCheckBox.setEnabled(enable);
        filterBox.setEnabled(enable);
        filterField.setEnabled(enable);
        for (int i = 0; i < grantButtons.length; i++)
            grantButtons[i].setEnabled(enable);
        refreshButton.setEnabled(enable);
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldButtons[i].setEnabled(enable);
        systemCheck.setEnabled(enable);
        userTypeBox.setEnabled(enable);
        userList.setEnabled(enable);
        //cancelButton.setVisible(!enable);
        cancelButton.setEnabled(!enable);
        jProgressBar1.setEnabled(!enable);
        if (enable)
            jProgressBar1.setValue(0);
    }

    private void userBoxActionPerformed() {
        load_userList();
    }

    private void cancelButtonActionPerformed() {
        setEnableElements(true);
    }

    private void refreshButtonActionPerformed() {
        if (userlistModel.size() > 0)
            if (userList.getSelectedValue() != null) {
                act = CREATE_TABLE;
                execute_thread();
            }
    }

    private void userListValueChanged() {
        if (userlistModel.size() > 0)
            if (userList.getSelectedValue() != null) {
                act = CREATE_TABLE;
                execute_thread();
            }
    }

    private void revoke_gActionPerformed() {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = firstGrantColumn; col < headers.length; col++) {
                grantOnRole(0, row, col);
            }
        }
    }

    private void grant_gActionPerformed() {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = firstGrantColumn; col < headers.length; col++) {
                grantOnRole(1, row, col);
            }
        }
        querySender.releaseResources();
    }

    private void grant_option_gActionPerformed() {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = firstGrantColumn; col < headers.length; col++) {
                grantOnRole(2, row, col);
            }
            querySender.releaseResources();
        }
    }

    private void objectBoxActionPerformed() {
        act = CREATE_TABLE;
        execute_thread();
    }

    private void tablePrivilegesMouseClicked(java.awt.event.MouseEvent evt) {
        if (enableElements) {
            int row = tablePrivileges.getSelectedRow();
            if (evt.getClickCount() > 1) {
                int col = tablePrivileges.getSelectedColumn();
                if (col > 0) {
                    if (tablePrivileges.getValueAt(row, col).equals(gr)) {
                        grantOnRole(2, row, col);
                    } else if (tablePrivileges.getValueAt(row, col).equals(adm)) {
                        grantOnRole(0, row, col);
                    } else {
                        grantOnRole(1, row, col);
                    }
                }
                querySender.releaseResources();
            }
            if (row >= 0) {
                setVisiblePanelOfTable(objectVector.elementAt(row).getType() >= NamedObject.TABLE && objectVector.elementAt(row).getType() <= NamedObject.VIEW
                        || objectVector.elementAt(row).getType() >= NamedObject.SYSTEM_TABLE && objectVector.elementAt(row).getType() <= NamedObject.SYSTEM_VIEW);
            }
        }
    }

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {
        if (enableElements) {
            int row = tablePrivileges.getSelectedRow();
            int row2 = privilegesForFieldTable.getSelectedRow();
            if (evt.getClickCount() > 1) {
                int col = privilegesForFieldTable.getSelectedColumn();
                if (col > 1) {
                    if (privilegesForFieldTable.getValueAt(row2, col).equals(gr)) {
                        grantOnRoleForCol(2, row, col, row2);
                    } else if (privilegesForFieldTable.getValueAt(row2, col).equals(adm)) {
                        grantOnRoleForCol(0, row, col, row2);
                    } else {
                        grantOnRoleForCol(1, row, col, row2);
                    }
                }
            }

        }
    }

    private void revoke_v1ActionPerformed() {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < columnVector.size(); row++) {
                grantOnRoleForCol(0, row2, col, row);
            }
    }

    private void revoke_g1ActionPerformed() {
        int row = privilegesForFieldTable.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grantOnRoleForCol(0, row2, col, row);
            }
    }

    private void grant_v1ActionPerformed() {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < columnVector.size(); row++) {
                grantOnRoleForCol(1, row2, col, row);
            }
    }

    private void grant_g1ActionPerformed() {
        int row = privilegesForFieldTable.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grantOnRoleForCol(1, row2, col, row);
            }
    }

    private void grant_option_v1ActionPerformed() {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < columnVector.size(); row++) {
                grantOnRoleForCol(2, row2, col, row);
            }
    }

    private void grant_option_g1ActionPerformed() {
        int row = privilegesForFieldTable.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grantOnRoleForCol(2, row2, col, row);
            }
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

    private void filterBoxActionPerformed(java.awt.event.ActionEvent evt) {
        act = CREATE_TABLE;
        execute_thread();
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
            List<String> names = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dbc).getDatabaseObjectNamesForMetaTag(metatag);
            for (String name : names) {
                userlistModel.addElement(name);
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

    void loadTable2(NamedObject relation) {
        privilegesForFieldTable.setModel(new RoleTableModel(headers2, 0));
        columnVector = new Vector<>();
        tableForColumnsMap.clear();
        List<DatabaseColumn> cols = null;
        try {
            if (!(relation instanceof DefaultDatabaseObject))
                return;
            cols = ((DefaultDatabaseObject) relation).getColumns();
            for (DatabaseColumn col : cols) {
                Vector<Object> roleData = new Vector<Object>();
                Object[] obj = {col, Color.BLACK};
                if (relation.isSystem())
                    obj[1] = Color.RED;
                roleData.add(obj);
                roleData.add(col.getFormattedDataType());
                for (int k = 0; k < 2; k++)
                    roleData.add(no);
                tableForColumnsMap.put(col, roleData);
                columnVector.add(col);
            }
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        try {
            String query = buildQueryForPrivilegesByField(AbstractDatabaseObject.getRDBTypeFromType(getTypeFromUserBoxIndex(userTypeBox.getSelectedIndex())), relation.getRDBType());
            PreparedStatement statement = querySender.getPreparedStatement(query);
            statement.setString(1, userList.getSelectedValue().trim());
            statement.setString(2, relation.getName());
            ResultSet rs = querySender.execute(QueryTypes.SELECT, statement).getResultSet();
            while (rs.next()) {
                DatabaseColumn currentCol = getColumnFromName(cols, rs.getString("FIELD_NAME"));
                if (currentCol == null)
                    continue;
                Vector<Object> roleData = tableForColumnsMap.get(currentCol);
                String grant = rs.getString("PRIVILEGE").trim();
                int ind = grants.indexOf(grant);
                Object gr_opt = rs.getObject("GRANT_OPTION");
                if (gr_opt == null)
                    gr_opt = 0;
                if (ind == 1)
                    if (gr_opt.equals(0)) {
                        roleData.set(2, gr);
                    } else
                        roleData.set(2, adm);
                if (ind == 4)
                    if (gr_opt.equals(0)) {
                        roleData.set(3, gr);
                    } else
                        roleData.set(3, adm);
            }

            for (int i = 0; i < cols.size(); i++) {
                Vector<Object> roleData = tableForColumnsMap.get(cols.get(i));
                ((RoleTableModel) privilegesForFieldTable.getModel()).addRow(roleData);
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            querySender.releaseResources();
        }
    }

    void getTables() {
        addRelations(NamedObject.TABLE);
        if (systemCheck.isSelected())
            addRelations(NamedObject.SYSTEM_TABLE);
    }

    void getGlobalTemporaries() {
        addRelations(NamedObject.GLOBAL_TEMPORARY);
    }

    void getViews() {
        addRelations(NamedObject.VIEW);
        if (systemCheck.isSelected())
            addRelations(NamedObject.SYSTEM_VIEW);
    }

    void getProcedures() {
        addRelations(NamedObject.PROCEDURE);
    }

    void getFunctions() {
        addRelations(NamedObject.FUNCTION);
        if (systemCheck.isSelected())
            addRelations(NamedObject.SYSTEM_FUNCTION);
    }

    void getPackages() {
        addRelations(NamedObject.PACKAGE);
        if (systemCheck.isSelected())
            addRelations(NamedObject.SYSTEM_PACKAGE);
    }

    void getExceptions() {
        addRelations(NamedObject.EXCEPTION);
    }

    void getGenerators() {
        addRelations(NamedObject.SEQUENCE);
        if (systemCheck.isSelected())
            addRelations(NamedObject.SYSTEM_SEQUENCE);
    }

    void removeRow(NamedObject relation) {
        objectVector.remove(relation);
    }

    void addRow(NamedObject relation) {
        objectVector.add(relation);
        Vector<Object> roleData = new Vector<Object>();
        Object[] obj = {relation, Color.BLACK};
        if (relation.isSystem())
            obj[1] = Color.RED;
        roleData.add(obj);
        for (int k = 0; k < 7; k++) {
            if (relation instanceof DefaultDatabaseObject && k < col_execute - firstGrantColumn
                    || relation instanceof DefaultDatabaseExecutable && k == col_execute - firstGrantColumn
                    || (relation instanceof DefaultDatabaseException || relation instanceof DefaultDatabaseSequence) && k == col_usage - firstGrantColumn)
                roleData.add(no);
            else roleData.add("");
        }

        tableMap.put(relation, roleData);
    }

    void addRelations(int type) {
        try {
            List<NamedObject> objectList = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection((DatabaseConnection) databaseBox.getSelectedItem()).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
            List<NamedObject> resultList = new ArrayList<>();
            if (objectList != null)
                for (NamedObject relation : objectList) {
                    if (!relation.isSystem() || relation.isSystem() == systemCheck.isSelected()) {
                        if (!invertFilterCheckBox.isSelected() == relation.getName().contains(filterField.getText())) {
                            addRow(relation);
                            resultList.add(relation);
                        }
                    }
                }
            addRelationsToTable(resultList);
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());

        }
    }

    private void filterFieldActionPerformed() {
        act = CREATE_TABLE;
        execute_thread();
    }

    private void databaseBoxActionPerformed() {
        if (enabled_dBox) {
            querySender = new DefaultStatementExecutor((DatabaseConnection) databaseBox.getSelectedItem(), true);
            querySender.setCloseConnectionAfterQuery(false);
            querySender.setCommitMode(false);
            querySender.setAutoddl(false);
            dbc = (DatabaseConnection) databaseBox.getSelectedItem();
            load_userList();
        }
    }

    String buildQueryForPrivileges(int rdbTypeUser, int rdbTypeObject) {
        SelectBuilder sb = new SelectBuilder();
        Table userPrivileges = Table.createTable("RDB$USER_PRIVILEGES", "UP");
        sb.appendTable(userPrivileges);
        sb.appendFields(userPrivileges, "RELATION_NAME", "PRIVILEGE", "GRANT_OPTION", "FIELD_NAME");
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER_TYPE"), "=", rdbTypeUser + ""));
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER"), "=", "?"));
        Condition rfbTypeCondition = Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", rdbTypeObject + ""));
        if (rdbTypeObject == 1) {
            rfbTypeCondition.setLogicOperator("OR");
            rfbTypeCondition.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", 0 + ""));
        }
        sb.appendCondition(rfbTypeCondition);
        sb.setOrdering(Field.createField(userPrivileges, "RELATION_NAME").getFieldTable() + "," + Field.createField(userPrivileges, "FIELD_NAME").getFieldTable());
        return sb.getSQLQuery();
    }

    String buildQueryForPrivilegesByField(int rdbTypeUser, int rdbTypeObject) {
        SelectBuilder sb = new SelectBuilder();
        Table userPrivileges = Table.createTable("RDB$USER_PRIVILEGES", "UP");
        sb.appendTable(userPrivileges);
        sb.appendFields(userPrivileges, "RELATION_NAME", "PRIVILEGE", "GRANT_OPTION", "FIELD_NAME");
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER_TYPE"), "=", rdbTypeUser + ""));
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER"), "=", "?"));
        Condition rfbTypeCondition = Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", rdbTypeObject + ""));
        if (rdbTypeObject == 1) {
            rfbTypeCondition.setLogicOperator("OR");
            rfbTypeCondition.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", 0 + ""));
        }
        sb.appendCondition(rfbTypeCondition);
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "RELATION_NAME"), "=", "?"));
        sb.setOrdering(Field.createField(userPrivileges, "FIELD_NAME").getFieldTable());
        return sb.getSQLQuery();
    }

    NamedObject getRelationFromName(List<NamedObject> list, String name) {
        if (name == null)
            return null;
        name = name.trim();
        for (NamedObject namedObject : list)
            if (namedObject.getName().contentEquals(name))
                return namedObject;
        return null;
    }

    DatabaseColumn getColumnFromName(List<DatabaseColumn> list, String name) {
        if (name == null)
            return null;
        name = name.trim();
        for (DatabaseColumn col : list)
            if (col.getName().contentEquals(name))
                return col;
        return null;
    }

    private void systemCheckActionPerformed(java.awt.event.ActionEvent evt) {
        act = CREATE_TABLE;
        execute_thread();
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

    void addRelationsToTable(List<NamedObject> list) {
        if (list != null && list.size() > 0)
            try {
                int rdbTypeObject = list.get(0).getRDBType();
                int rdbTypeUser = AbstractDatabaseObject.getRDBTypeFromType(getTypeFromUserBoxIndex(userTypeBox.getSelectedIndex()));
                String query = buildQueryForPrivileges(rdbTypeUser, rdbTypeObject);
                PreparedStatement statement = querySender.getPreparedStatement(query);
                statement.setString(1, userList.getSelectedValue().trim());
                ResultSet rs = querySender.execute(QueryTypes.SELECT, statement).getResultSet();
                while (rs.next()) {
                    NamedObject relation = getRelationFromName(list, rs.getString("RELATION_NAME"));
                    if (relation == null)
                        continue;
                    Vector<Object> roleData = tableMap.get(relation);
                    String grant = rs.getString("PRIVILEGE").trim();
                    int ind = grants.indexOf(grant);
                    Object gr_opt = rs.getObject("GRANT_OPTION");
                    boolean fieldGrant = rs.getString("FIELD_NAME") != null;
                    if (gr_opt == null)
                        gr_opt = 0;
                    if (ind != 7) {
                        if (fieldGrant) {
                            if (roleData.get(ind + firstGrantColumn).equals(no))
                                roleData.set(ind + firstGrantColumn, fieldGr);
                        } else if (gr_opt.equals(0)) {
                            roleData.set(ind + firstGrantColumn, gr);
                        } else
                            roleData.set(ind + firstGrantColumn, adm);
                    } else {
                        for (int i = firstGrantColumn; i < firstGrantColumn + 5; i++) {
                            if (gr_opt.equals(0)) {
                                roleData.set(i, gr);
                            } else
                                roleData.set(i, adm);
                        }
                    }
                }

                for (int i = 0; i < list.size() && !enableElements; i++) {
                    Vector<Object> roleData = tableMap.get(list.get(i));
                    boolean granted = roleData.contains(gr) || roleData.contains(adm);
                    boolean adding = (filterBox.getSelectedIndex() == 0 || (filterBox.getSelectedIndex() == 1) == granted);
                    if (adding)
                        ((RoleTableModel) tablePrivileges.getModel()).addRow(roleData);
                    else {
                        removeRow(list.get(i));
                    }
                }

            } catch (NullPointerException e) {
                Log.error(bundleString("connection.close"));
            } catch (SQLException e) {
                Log.error(e.getMessage());
            } catch (Exception e) {
                GUIUtilities.displayErrorMessage(e.getMessage());
            } finally {
                querySender.releaseResources();
            }
    }

    void loadTable() {
        tablePrivileges.setModel(new RoleTableModel(headers, 0));
        objectVector.removeAllElements();
        tableMap.clear();
        jProgressBar1.setMaximum(objectTypes.length);
        int i = 0;
        if (objectBox.getModel().isChecked(objectTypes[0]))
            getTables();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[1]))
            getGlobalTemporaries();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[2]))
            getViews();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[3]))
            getProcedures();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[4]))
            getFunctions();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[5]))
            getPackages();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[6]))
            getGenerators();
        jProgressBar1.setValue(i++);
        if (objectBox.getModel().isChecked(objectTypes[7]))
            getExceptions();
        jProgressBar1.setValue(objectTypes.length);
        setEnableElements(true);
        setVisiblePanelOfTable(false);

    }

    private String getGrantQuery(String grantor, int typeGrant, String grant, NamedObject relation) {
        return getGrantQuery(grantor, typeGrant, grant, relation, null);

    }

    private String getGrantQuery(String grantor, int typeGrant, String grant, NamedObject relation, String... fields) {
        GrantBuilder gb = new GrantBuilder();
        gb.setGrantor(grantor).setRelation(relation.getName()).setGrantType(grant);
        switch (typeGrant) {
            case 0:
                gb.setGrant(false);
                break;
            case 1:
                gb.setGrant(true);
                break;
            case 2:
                gb.setGrant(true);
                gb.setGrantOption(true);
        }
        if (relation instanceof DefaultDatabaseObject)
            gb.setRelationType("TABLE");
        else
            gb.setRelationType(relation.getMetaDataKey().replace("SYSTEM ", ""));
        int ind_userBox = userTypeBox.getSelectedIndex();
        switch (ind_userBox) {
            case 0:
                gb.setGrantorType("USER");
                break;
            case 1:
                gb.setGrantorType("ROLE");
                break;
            case 2:
                gb.setGrantorType("VIEW");
                break;
            case 3:
                gb.setGrantorType("TRIGGER");
                break;
            case 4:
                gb.setGrantorType("PROCEDURE");
                break;
            case 5:
                gb.setGrantorType("FUNCTION");
                break;
            case 6:
                gb.setGrantorType("PACKAGE");
                break;
            default:
                break;
        }
        if (fields != null)
            gb.appendFields(fields);
        return gb.getSQLQuery();

    }

    public void runToThread() {
        int col;
            switch (act) {
                case CREATE_TABLE:
                    loadTable();
                    break;
                case ALL_GRANTS_TO_ALL_OBJECTS:
                    jProgressBar1.setMaximum(objectVector.size());
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grantAllOnRole(1, row);

                    }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
                case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                    jProgressBar1.setMaximum(objectVector.size());
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grantAllOnRole(2, row);

                    }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
                case NO_ALL_GRANTS_TO_ALL_OBJECTS:
                    jProgressBar1.setMaximum(objectVector.size());
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grantAllOnRole(0, row);

                    }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
                case GRANT_TO_ALL_OBJECTS:
                    col = tablePrivileges.getSelectedColumn();
                    jProgressBar1.setMaximum(objectVector.size());
                    if (col > 0)
                        for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                            jProgressBar1.setValue(row);
                            isClose();
                            grantOnRole(1, row, col);
                        }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
                case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                    col = tablePrivileges.getSelectedColumn();
                    jProgressBar1.setMaximum(objectVector.size());
                    if (col > 0)
                        for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                            jProgressBar1.setValue(row);
                            isClose();
                            grantOnRole(2, row, col);
                        }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
                case NO_GRANT_TO_ALL_OBJECTS:
                    col = tablePrivileges.getSelectedColumn();
                    jProgressBar1.setMaximum(objectVector.size());
                    if (col > 0)
                        for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                            jProgressBar1.setValue(row);
                            isClose();
                            grantOnRole(0, row, col);
                        }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
                    break;
            }
            querySender.releaseResources();
    }

    void grantQuery(String query, Icon icon, int row, int col, JTable t) {
        try {
            SqlStatementResult result = querySender.execute(QueryTypes.GRANT, query);
            if (result.isException()) {
                Log.info("query=" + query);
                throw result.getSqlException();
            }
            t.setValueAt(icon, row, col);
        } catch (NullPointerException e) {
            Log.error(bundleString("connection.close"));
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            querySender.releaseResourcesWithoutCommit();
        }
    }

    void grantAllQuery(String query, Icon icon, int row, int grant) {
        try {
            querySender.execute(QueryTypes.GRANT, query, -1);
            for (int i = firstGrantColumn; i < headers.length; i++)
                if (i != col_execute && i != col_usage)
                    tablePrivileges.setValueAt(icon, row, i);
        } catch (Exception e) {
            Log.error(e.getMessage());
            for (int i = firstGrantColumn; i < headers.length; i++) {
                grantCase(grant, row, i);
            }
        } finally {
            querySender.releaseResourcesWithoutCommit();
        }
    }

    void grantCase(int grant, int row, int col) {
        grantOnRole(grant, row, col);
    }

    void grantAllOnRole(int grant, int row) {
        if (row < tablePrivileges.getRowCount()) {
            if (grant == 1) {
                boolean containsAdm = false;
                for (int i = firstGrantColumn; i < headers.length; i++)
                    if (tablePrivileges.getValueAt(row, i).equals(adm)) {
                        containsAdm = true;
                        break;
                    }
                if (containsAdm) grantAllOnRole(0, row);
            }

            if (objectVector.elementAt(row) instanceof DefaultDatabaseObject)
                grantAllQuery(getGrantQuery(userList.getSelectedValue(), grant, "ALL", objectVector.elementAt(row)), icons[grant], row, grant);
            else if (objectVector.elementAt(row) instanceof DefaultDatabaseExecutable)
                grantQuery(getGrantQuery(userList.getSelectedValue(), grant, "EXECUTE", objectVector.elementAt(row)), icons[grant], row, col_execute, tablePrivileges);
            else
                grantQuery(getGrantQuery(userList.getSelectedValue(), grant, "USAGE", objectVector.elementAt(row)), icons[grant], row, col_usage, tablePrivileges);

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
            SwingWorker sw = new SwingWorker("GrantManagerExecuteThread") {
                @Override
                public Object construct() {
                    runToThread();
                    return "DONE";
                }
            };
            sw.start();
        }
    }

    void grantOnRole(int grant, int row, int col) {
        if (row < tablePrivileges.getRowCount()) {
            if (objectVector.elementAt(row) instanceof DefaultDatabaseExecutable && col != col_execute)
                return;
            else if (objectVector.elementAt(row) instanceof DefaultDatabaseObject && (col == col_execute || col == col_usage))
                return;
            else if ((objectVector.elementAt(row) instanceof DefaultDatabaseException
                    || objectVector.elementAt(row) instanceof DefaultDatabaseSequence) && col != col_usage)
                return;
            if (grant == 1 && tablePrivileges.getValueAt(row, col).equals(adm))
                grantOnRole(0, row, col);
            grantQuery(getGrantQuery(userList.getSelectedValue(), grant, headers[col], objectVector.elementAt(row)), icons[grant], row, col, tablePrivileges);
            querySender.releaseResourcesWithoutCommit();
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

    void grantOnRoleForCol(int grant, int row, int col, int row2) {
        if (row < tablePrivileges.getRowCount() && row2 < privilegesForFieldTable.getRowCount()) {
            if (grant == 1 && privilegesForFieldTable.getValueAt(row2, col).equals(adm))
                grantOnRoleForCol(0, row, col, row2);
            grantQuery(getGrantQuery(userList.getSelectedValue(), grant, headers2[col], objectVector.elementAt(row), columnVector.elementAt(row2).getName()), icons[grant], row2, col, privilegesForFieldTable);
            querySender.releaseResources();
        }
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

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        act = CREATE_TABLE;
        execute_thread();
    }

    void setVisiblePanelOfTable(boolean flag) {
        if (flag) {
            int row = tablePrivileges.getSelectedRow();
            downPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf") + "[" + objectVector.elementAt(row) + "]"));
            loadTable2(objectVector.elementAt(row));
            obj_index = row;
        } else {
            downPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf")));
            privilegesForFieldTable.setModel(new RoleTableModel(headers2, 0));
        }
    }

    public String[] bundleStringsOf(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys.length > 0)
                keys[i] = bundleString(keys[i]);
        }
        return keys;
    }

    private void revokeVertical() {
        act = NO_GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void revokeAll() {
        act = NO_ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grantVertical() {
        act = GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grantAll() {
        act = ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grantVerticalWithGrantOption() {
        act = GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    private void grantAllWithGrantOption() {
        act = ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    void get_users() {
        Connection connection = null;
        try {
            connection = ConnectionManager.getTemporaryConnection(dbc).unwrap(Connection.class);
        } catch (SQLException e) {
            Log.error("error get connection for getting users in grant manager:", e);
        }

        IFBUserManager userManager = null;
        try {
            userManager = (IFBUserManager) DynamicLibraryLoader.loadingObjectFromClassLoader(((DatabaseConnection) databaseBox.getSelectedItem()).getDriverMajorVersion(), connection, "FBUserManagerImpl");
        } catch (ClassNotFoundException e) {
            Log.error("Error get users in Grant Manager:", e);
        }
        if (userManager != null) {
            userManager = getUserManager(userManager, (DatabaseConnection) databaseBox.getSelectedItem());
            Map<String, IFBUser> users;
            try {
                users = userManager.getUsers();
                for (IFBUser u : users.values()) {
                    userlistModel.addElement(u.getUserName());
                }
                userlistModel.addElement("PUBLIC");
            } catch (Exception e) {
                System.out.println(e);
                GUIUtilities.displayErrorMessage(e.toString());
            }
        }
    }



}
