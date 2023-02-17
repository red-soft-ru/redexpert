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
import org.executequery.databaseobjects.NamedObject;
import org.executequery.datasource.ConnectionManager;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author mikhan808
 */
public class GrantManagerPanel extends JPanel implements TabView {

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
    Vector<String> fieldName;
    Vector<String> fieldType;
    int obj_index;
    DefaultStatementExecutor querySender;
    int col_execute = 5;
    int col_usage = 7;
    private JButton cancelButton;
    // private JPanel interruptPanel;
    private JComboBox<String> databaseBox;
    private JPanel downPanel;
    private JComboBox<String> filterBox;
    private JTextField filterField;
    private JCheckBox invertFilterCheckBox;
    private JProgressBar jProgressBar1;
    private JScrollPane recipientsOfPrivilegesScroll;
    private JScrollPane privilegesScroll;
    private JScrollPane privilegesForFieldScroll;
    private JTable privilegesForFieldTable;
    private JPanel leftPanel;
    private JComboBox<String> objectBox;
    private JButton refreshButton;
    private JPanel rightPanel;
    private JCheckBox systemCheck;
    private JTable tablePrivileges;
    private JComboBox<String> recipientsOfPrivilegesBox;
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
        privilegesForFieldTable.setDefaultRenderer(Object.class, bctr);
        enabled_dBox = true;
        setEnableElements(true);
        load_connections();
    }

    private boolean isClose = false;
    private String grantor;

    private void userBoxActionPerformed(java.awt.event.ActionEvent evt) {
        load_userList();
    }


    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setEnableElements(true);
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {
        load_connections();
    }

    JToolBar grantFieldsToolbar;

    private void revoke_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(0, row, col);
            }
        }
    }

    int act;
    private JButton[] grantFieldButtons;

    private void grant_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(1, row, col);
            }
        }
        querySender.releaseResources();
    }

    private JButton[] grantButtons;

    private void userListValueChanged() {
        if (userlistModel.size() > 0)
            if (userList.getSelectedValue() != null) {
                act = CREATE_TABLE;
                execute_thread();
            }
    }

    private void grant_option_gActionPerformed(java.awt.event.ActionEvent evt) {
        int row = tablePrivileges.getSelectedRow();
        if (row >= 0) {
            for (int col = 1; col < headers.length; col++) {
                grant_on_role(2, row, col);
            }
            querySender.releaseResources();
        }
    }

    private void objectBoxActionPerformed(java.awt.event.ActionEvent evt) {
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
                        grant_on_role(2, row, col);
                    } else if (tablePrivileges.getValueAt(row, col).equals(adm)) {
                        grant_on_role(0, row, col);
                    } else {
                        grant_on_role(1, row, col);
                    }
                }
                querySender.releaseResources();
            }
            if (row >= 0) {
                setVisiblePanelOfTable(!relType.elementAt(row).equals(objectBox.getItemAt(3)));
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
                        grant_on_role(2, row, col, row2);
                    } else if (privilegesForFieldTable.getValueAt(row2, col).equals(adm)) {
                        grant_on_role(0, row, col, row2);
                    } else {
                        grant_on_role(1, row, col, row2);
                    }
                }
            }

        }
    }

    private void revoke_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(0, row2, col, row);
            }
    }

    private void revoke_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = privilegesForFieldTable.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grant_on_role(0, row2, col, row);
            }
    }

    private void grant_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(1, row2, col, row);
            }
    }

    private void grant_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = privilegesForFieldTable.getSelectedRow();
        int row2 = obj_index;
        if (row >= 0)
            for (int col = 2; col < 4; col++) {
                grant_on_role(1, row2, col, row);
            }
    }

    private void grant_option_v1ActionPerformed(java.awt.event.ActionEvent evt) {
        int col = privilegesForFieldTable.getSelectedColumn();
        int row2 = obj_index;
        if (col > 1)
            for (int row = 0; row < fieldName.size(); row++) {
                grant_on_role(2, row2, col, row);
            }
    }

    private void grant_option_g1ActionPerformed(java.awt.event.ActionEvent evt) {
        int row = privilegesForFieldTable.getSelectedRow();
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
            int ind_userBox = recipientsOfPrivilegesBox.getSelectedIndex();
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

    private String grant;

    void load_table2(String rname) {
        privilegesForFieldTable.setModel(new RoleTableModel(headers2, 0));
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
                ((RoleTableModel) privilegesForFieldTable.getModel()).addRow(roleData);
                while (rs1.next()) {
                    String grant = rs1.getString(1).trim();
                    int ind = grants.indexOf(grant);
                    if (ind == 1)
                        if (rs1.getObject(2).equals(0)) {
                            privilegesForFieldTable.setValueAt(gr, i, 2);
                        } else
                            privilegesForFieldTable.getModel().setValueAt(adm, i, 2);
                    if (ind == 5)
                        if (rs1.getObject(2).equals(0)) {
                            privilegesForFieldTable.setValueAt(gr, i, 3);
                        } else
                            privilegesForFieldTable.getModel().setValueAt(adm, i, 3);

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
                    if (!invertFilterCheckBox.isSelected() == name.contains(filterField.getText()))
                        addRow(name, type, system_flag, granted_flag);
                }
            }
        } catch (SQLException e) {
            GUIUtilities.displayErrorMessage(e.getMessage());

        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
    }

    private void filterFieldActionPerformed(java.awt.event.ActionEvent evt) {
        act = CREATE_TABLE;
        execute_thread();
    }

    private void databaseBoxActionPerformed(java.awt.event.ActionEvent evt) {
        if (enabled_dBox) {
            querySender = new DefaultStatementExecutor(listConnections.get(databaseBox.getSelectedIndex()), true);
            querySender.setCloseConnectionAfterQuery(false);
            querySender.setCommitMode(false);
            querySender.setAutoddl(false);
            dbc = listConnections.get(databaseBox.getSelectedIndex());
            con = ConnectionManager.getTemporaryConnection(listConnections.get(databaseBox.getSelectedIndex()));
            load_userList();
        }
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
        long start = System.currentTimeMillis();
        try {
            String query = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION,RDB$FIELD_NAME from RDB$USER_PRIVILEGES\n" +
                    "where (rdb$Relation_name=?) and (rdb$user='"
                    + userList.getSelectedValue().trim() + "')";
            PreparedStatement statement = querySender.getPreparedStatement(query);
            for (int i = 0, g = 0; i < relName.size() && !enableElements; g++, i++) {
                jProgressBar1.setValue(g);
                statement.setString(1, relName.elementAt(i));
                ResultSet rs1 = querySender.execute(QueryTypes.SELECT, statement).getResultSet();
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


            }
            querySender.releaseResources();
        } catch (NullPointerException e) {
            Log.error(bundleString("connection.close"));
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        setEnableElements(true);
        setVisiblePanelOfTable(false);

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

    private String getGrantQuery(String user, int typeGrant, int typeObject, String grantX, String relName) throws SQLException {
        grant = grantX;
        grantor = user;
        String query = "";
        switch (typeGrant) {
            case 0:
                if (typeObject == 0) {
                    query = "REVOKE " + grant + " ON \"" + relName + "\" FROM \"" + grantor + "\";";
                } else {
                    query = "REVOKE EXECUTE ON PROCEDURE \"" + relName + "\" FROM \""
                            + grantor + "\";";
                }
                break;
            case 1:
                if (typeObject == 0) {
                    query = "GRANT " + grant + " ON \"" + relName + "\" TO \"" + grantor + "\";";
                } else {
                    query = "GRANT EXECUTE ON PROCEDURE \"" + relName + "\" TO \""
                            + grantor + "\";";
                }
                break;
            case 2:
                if (typeObject == 0) {
                    query = "GRANT " + grant + " ON \"" + relName + "\" TO \"" + grantor + "\" WITH GRANT OPTION;";
                } else {
                    query = "GRANT EXECUTE ON PROCEDURE \"" + relName + "\" TO \""
                            + grantor + "\" WITH GRANT OPTION;";
                }
        }
        return query;

    }


    public void runToThread() {
        int col;
            switch (act) {
                case CREATE_TABLE:
                    load_table();
                    break;
                case ALL_GRANTS_TO_ALL_OBJECTS:
                    jProgressBar1.setMaximum(relName.size());
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        jProgressBar1.setValue(row);
                        isClose();
                        grant_all_on_role(1, row);

                    }
                    jProgressBar1.setValue(0);
                    setEnableElements(true);
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
            querySender.releaseResources();
    }

    void grant_query(String query, Icon icon, int row, int col, JTable t) {
        try {
            querySender.execute(QueryTypes.GRANT, query);
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

    void revoke_execute(int row) {
        try {
            grant_query(getGrantQuery(userList.getSelectedValue(), 0, 1, "EXECUTE", relName.elementAt(row)), no, row, col_execute, tablePrivileges);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void grant_execute(int row) {
        try {
            grant_query(getGrantQuery(userList.getSelectedValue(), 1, 1, "EXECUTE", relName.elementAt(row)), gr, row, col_execute, tablePrivileges);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void grant_execute_admin(int row) {
        try {
            grant_query(getGrantQuery(userList.getSelectedValue(), 1, 1, "EXECUTE", relName.elementAt(row)), adm, row, col_execute, tablePrivileges);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void grant_all_query(String query, Icon icon, int row, int grantt) {
        try {
            querySender.execute(QueryTypes.GRANT, query, -1);
            for (int i = 1; i < headers.length; i++)
                if (i != col_execute && i != col_usage)
                    tablePrivileges.setValueAt(icon, row, i);
        } catch (Exception e) {
            Log.error(e.getMessage());
            for (int i = 1; i < headers.length; i++) {
                grant_case(grantt, row, i);
            }
        } finally {
            querySender.releaseResourcesWithoutCommit();
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
        try {
            grant_all_query(getGrantQuery(userList.getSelectedValue(), 0, 0, "ALL", relName.elementAt(row)), no, row, 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    void grant_all(int row) {
        try {
            grant_all_query(getGrantQuery(userList.getSelectedValue(), 1, 0, "ALL", relName.elementAt(row)), gr, row, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void grant_all_admin(int row) {
        try {
            grant_all_query(getGrantQuery(userList.getSelectedValue(), 2, 0, "ALL", relName.elementAt(row)), adm, row, 2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void revoke(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            try {
                grant_query(getGrantQuery(userList.getSelectedValue(), 0, 0, headers[col], relName.elementAt(row)), no, row, col, tablePrivileges);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void grant(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            try {
                grant_query(getGrantQuery(userList.getSelectedValue(), 1, 0, headers[col], relName.elementAt(row)), gr, row, col, tablePrivileges);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void grant_admin(int row, int col) {
        if (col > 0 && col < headers.length && col != col_execute && col != col_usage) {
            try {
                grant_query(getGrantQuery(userList.getSelectedValue(), 2, 0, headers[col], relName.elementAt(row)), adm, row, col, tablePrivileges);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
                        boolean containsAdm = false;
                        for (int i = 0; i < headers.length; i++)
                            if (tablePrivileges.getValueAt(row, i).equals(adm)) {
                                containsAdm = true;
                                break;
                            }
                        if (containsAdm)
                            revoke_all(row);
                        grant_all(row);
                    } else {
                        if (tablePrivileges.getValueAt(row, col_execute).equals(adm))
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
        querySender.releaseResourcesWithoutCommit();
    }

    void grant_on_role(int grantt, int row, int col, int row2) {
        if (row < tablePrivileges.getRowCount()) {
            try {
                switch (grantt) {
                    case 0:
                        grant_query(getGrantQuery(userList.getSelectedValue(), 0, 0, headers[col], relName.elementAt(row)), no, row2, col, privilegesForFieldTable);
                        break;
                    case 1:
                        if (privilegesForFieldTable.getValueAt(row2, col).equals(adm)) {
                            grant_query(getGrantQuery(userList.getSelectedValue(), 0, 0, headers[col], relName.elementAt(row)), no, row2, col, privilegesForFieldTable);
                        }
                        grant_query(getGrantQuery(userList.getSelectedValue(), 1, 0, headers[col], relName.elementAt(row)), gr, row2, col, privilegesForFieldTable);

                        break;
                    case 2:
                        grant_query(getGrantQuery(userList.getSelectedValue(), 2, 0, headers[col], relName.elementAt(row)), adm, row2, col, privilegesForFieldTable);
                        break;
                }
                querySender.execute(QueryTypes.COMMIT, (String) null);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                querySender.releaseResources();
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

    void setVisiblePanelOfTable(boolean flag) {
        if (flag) {
            int row = tablePrivileges.getSelectedRow();
            downPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf") + "[" + relName.elementAt(row) + "]"));
            load_table2(relName.elementAt(row));
            obj_index = row;
        } else {
            downPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf")));
            privilegesForFieldTable.setModel(new RoleTableModel(headers2, 0));
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

    private void revoke_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = NO_GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void revoke_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = NO_ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = GRANT_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = ALL_GRANTS_TO_ALL_OBJECTS;
        execute_thread();
    }

    private void grant_option_vActionPerformed(java.awt.event.ActionEvent evt) {
        act = GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    private void grant_option_allActionPerformed(java.awt.event.ActionEvent evt) {
        act = ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION;
        execute_thread();
    }

    void get_users() {
        Connection connection = null;
        try {
            connection = con.unwrap(Connection.class);
        } catch (SQLException e) {
            Log.error("error get connection for getting users in grant manager:", e);
        }

        IFBUserManager userManager = null;
        try {
            userManager = (IFBUserManager) DynamicLibraryLoader.loadingObjectFromClassLoader(connection, "FBUserManagerImpl");
        } catch (ClassNotFoundException e) {
            Log.error("Error get users in Grant Manager:", e);
        }
        if (userManager != null) {
            userManager = getUserManager(userManager, listConnections.get(databaseBox.getSelectedIndex()));
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

    @SuppressWarnings("unchecked")
    private void initComponents() {

        //upPanel = new JPanel();
        //interruptPanel = new JPanel();
        databaseBox = new JComboBox<>();
        refreshButton = new JButton(bundleString("Refresh"));
        leftPanel = new JPanel();
        recipientsOfPrivilegesBox = new JComboBox<>();
        recipientsOfPrivilegesScroll = new JScrollPane();
        userList = new JList<>();
        rightPanel = new JPanel();
        /*revoke_v = new JButton();
        revoke_h = new JButton();
        revoke_all = new JButton();*/
        grantFieldButtons = new JButton[6];
        for (int i = 0; i < grantFieldButtons.length; i++) {
            grantFieldButtons[i] = new JButton();
            switch (i) {
                case 0:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_vertical.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> revoke_v1ActionPerformed(evt));
                    break;
                case 1:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_gorisont.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> revoke_g1ActionPerformed(evt));

                    break;
                case 2:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_vertical.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_v1ActionPerformed(evt));

                    break;
                case 3:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_gorisont.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_g1ActionPerformed(evt));

                    break;
                case 4:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_vertical.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_option_v1ActionPerformed(evt));

                    break;
                case 5:
                    grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_gorisont.png"))); // NOI18N
                    grantFieldButtons[i].addActionListener(evt -> grant_option_g1ActionPerformed(evt));
                    break;
            }
        }

        objectBox = new JComboBox<>();
        filterBox = new JComboBox<>();
        filterField = new JTextField();
        invertFilterCheckBox = new JCheckBox();
        systemCheck = new JCheckBox();
        privilegesScroll = new JScrollPane();
        tablePrivileges = new JTable();
        downPanel = new JPanel();
        grantButtons = new JButton[9];
        for (int i = 0; i < grantButtons.length; i++) {
            grantButtons[i] = new JButton();
            switch (i) {
                case 0:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_vertical.png")));
                    grantButtons[i].addActionListener(evt -> revoke_vActionPerformed(evt));
                    break;
                case 1:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_gorisont.png")));
                    grantButtons[i].addActionListener(evt -> revoke_gActionPerformed(evt));
                    break;
                case 2:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/no_grant_all.png")));
                    grantButtons[i].addActionListener(evt -> revoke_allActionPerformed(evt));
                    break;
                case 3:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_vertical.png")));
                    grantButtons[i].addActionListener(evt -> grant_vActionPerformed(evt));
                    break;
                case 4:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_gorisont.png")));
                    grantButtons[i].addActionListener(evt -> grant_gActionPerformed(evt));
                    break;
                case 5:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/grant_all.png")));
                    grantButtons[i].addActionListener(evt -> grant_allActionPerformed(evt));
                    break;
                case 6:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_vertical.png")));
                    grantButtons[i].addActionListener(evt -> grant_option_vActionPerformed(evt));
                    break;
                case 7:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_gorisont.png")));
                    grantButtons[i].addActionListener(evt -> grant_option_gActionPerformed(evt));
                    break;
                case 8:
                    grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/admin_option_all.png")));
                    grantButtons[i].addActionListener(evt -> grant_option_allActionPerformed(evt));
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

        databaseBox.setModel(new DefaultComboBoxModel<>(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        databaseBox.addActionListener(evt -> databaseBoxActionPerformed(evt));

        refreshButton.setIcon(GUIUtilities.loadIcon("Refresh16.png", true));
        refreshButton.addActionListener(evt -> refreshButtonActionPerformed(evt));

        String[] recipients = new String[]{"Users", "Roles", "Views", "Triggers", "Procedures", "Functions", "Packages"};
        recipientsOfPrivilegesBox.setModel(new DefaultComboBoxModel<>(bundleStrings(recipients)));
        recipientsOfPrivilegesBox.addActionListener(evt -> userBoxActionPerformed(evt));

        userList.addListSelectionListener(evt -> userListValueChanged());
        recipientsOfPrivilegesScroll.setViewportView(userList);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(6);


        objectBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"AllObjects", "Tables", "Views", "Procedures"})));
        objectBox.addActionListener(evt -> objectBoxActionPerformed(evt));

        filterBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"DisplayAll", "GrantedOnly", "Non-grantedOnly"})));
        filterBox.addActionListener(evt -> filterBoxActionPerformed(evt));

        filterField.addActionListener(evt -> filterFieldActionPerformed(evt));

        invertFilterCheckBox.setText(bundleString("InvertFilter"));
        invertFilterCheckBox.addActionListener(evt -> jCheckBox1ActionPerformed(evt));

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
                cancelButtonActionPerformed(evt);
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

        gbh.addLabelFieldPair(this, bundleString("PrivelegesFor"), recipientsOfPrivilegesBox, null, true, false);

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

        GroupLayout downPanelLayout = new GroupLayout(downPanel);
        downPanel.setLayout(downPanelLayout);
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldsToolbar.add(grantFieldButtons[i]);

        downPanelLayout.setHorizontalGroup(
                downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(grantFieldsToolbar)
                        .addComponent(privilegesForFieldScroll, GroupLayout.Alignment.TRAILING)
        );
        downPanelLayout.setVerticalGroup(
                downPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(downPanelLayout.createSequentialGroup()
                                .addComponent(grantFieldsToolbar)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(privilegesForFieldScroll, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE))
        );
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
        recipientsOfPrivilegesBox.setEnabled(enable);
        userList.setEnabled(enable);
        //cancelButton.setVisible(!enable);
        cancelButton.setEnabled(!enable);
        jProgressBar1.setEnabled(!enable);
        if (enable)
            jProgressBar1.setValue(0);
    }

}
