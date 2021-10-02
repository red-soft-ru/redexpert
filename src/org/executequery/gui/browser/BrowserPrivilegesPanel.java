package org.executequery.gui.browser;

import biz.redsoft.IFBUser;
import biz.redsoft.IFBUserManager;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Vector;

import static org.executequery.gui.browser.GrantManagerPanel.*;

public class BrowserPrivilegesPanel extends JPanel implements ActionListener {
    private static final int REVOKE = 0;
    private static final int GRANT = 1;
    private static final int GRANT_OPTION = 2;
    JComboBox userTypeBox;
    JToolBar grantToolBar;
    JToolBar grantFieldsToolbar;
    JButton[] grantButtons;
    JButton[] grantFieldButtons;
    String grants;// "SUDIXRGA";
    String[] headers;// {bundleString("Object"), "Select", "Update", "Delete", "Insert", "Execute", "References", "Usage"};
    String[] headersFields = {"Field", "Type", "Update", "References"};
    Icon[] icons;
    Vector<String> fieldName;
    Vector<String> fieldType;
    int obj_index;
    DefaultStatementExecutor querySender;
    int col_execute = 1;
    int col_usage = 1;
    Vector<String> usersVector;
    DatabaseConnection databaseConnection;
    Vector<String> relName;
    Vector<Boolean> relGranted;
    String[] iconNamesForFields = {"no_grant_vertical", "no_grant_gorisont", "grant_vertical", "grant_gorisont", "admin_option_vertical", "admin_option_gorisont"};
    String[] iconNames = {"no_grant_vertical", "no_grant_gorisont", "no_grant_all", "grant_vertical", "grant_gorisont", "grant_all", "admin_option_vertical", "admin_option_gorisont", "admin_option_all"};
    int act;
    String typeObject;
    boolean enableElements;
    private JTable tablePrivileges;
    private AbstractDatabaseObject defaultDatabaseObject;
    private JTable privilegesForFieldTable;
    private JComboBox<String> filterBox;
    private JTextField filterField;
    private JCheckBox invertFilterCheckBox;

    public BrowserPrivilegesPanel() {
        icons = new Icon[3];
        icons[GRANT] = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        icons[REVOKE] = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        icons[GRANT_OPTION] = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        relGranted = new Vector<>();
        relName = new Vector<>();

    }

    private void initComponents() {
        removeAll();
        if (isTable()) {
            headers = new String[]{bundleString("User"), "Select", "Update", "Delete", "Insert", "References"};
            grants = "SUDIRA";
        } else if (isGeneratorOrException()) {
            headers = new String[]{bundleString("User"), "Usage"};
            grants = "GA";
        } else {
            headers = new String[]{bundleString("User"), "Execute"};
            grants = "XA";
        }
        userTypeBox = new JComboBox();
        userTypeBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"Users", "Roles", "Views", "Triggers", "Procedures"})));
        userTypeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                load_userList();
                act = CREATE_TABLE;
                load_table();
            }
        });
        grantFieldButtons = new JButton[iconNamesForFields.length];
        for (int i = 0; i < grantFieldButtons.length; i++) {
            grantFieldButtons[i] = new JButton();
            grantFieldButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/" + iconNamesForFields[i] + ".png")));
        }
        tablePrivileges = new JTable();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        tablePrivileges.setDefaultRenderer(Object.class, bctr);
        grantToolBar = new JToolBar();
        grantToolBar.setFloatable(false);
        grantButtons = new JButton[iconNames.length];
        for (int i = 0; i < grantButtons.length; i++) {
            grantButtons[i] = new JButton();
            grantButtons[i].setIcon(new ImageIcon(getClass().getResource("/org/executequery/icons/" + iconNames[i] + ".png")));
            grantButtons[i].setActionCommand(iconNames[i]);
            grantToolBar.add(grantButtons[i]);
            grantButtons[i].addActionListener(this);
        }
        privilegesForFieldTable = new JTable();
        grantFieldsToolbar = new JToolBar();
        grantFieldsToolbar.setFloatable(false);
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerSize(6);

        tablePrivileges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enableElements) {
                    int row = tablePrivileges.getSelectedRow();
                    if (e.getClickCount() > 1) {
                        int col = tablePrivileges.getSelectedColumn();
                        if (col > 0) {
                            if (tablePrivileges.getValueAt(row, col).equals(icons[GRANT])) {
                                grant_on_role(GRANT_OPTION, row, col);
                            } else if (tablePrivileges.getValueAt(row, col).equals(icons[GRANT_OPTION])) {
                                grant_on_role(REVOKE, row, col);
                            } else {
                                grant_on_role(GRANT, row, col);
                            }
                        }
                    }
                }
            }
        });
        filterBox = new JComboBox<>();
        filterField = new JTextField();
        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                act = CREATE_TABLE;
                execute_thread();
            }
        });
        invertFilterCheckBox = new JCheckBox();
        filterBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"DisplayAll", "GrantedOnly", "Non-grantedOnly"})));
        filterBox.addActionListener(this);

        filterField.addActionListener(this);

        invertFilterCheckBox.setText(bundleString("InvertFilter"));
        invertFilterCheckBox.addActionListener(this);


        JScrollPane scrollMainPane = new JScrollPane(tablePrivileges);
        splitPane.setTopComponent(scrollMainPane);
        JScrollPane scrollPane = new JScrollPane(privilegesForFieldTable);
        splitPane.setBottomComponent(scrollPane);

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));

        add(userTypeBox, gbh.nextRowFirstCol().fillHorizontally().setMinWeightY().setHeight(1).get());
        add(filterBox, gbh.nextCol().fillHorizontally().get());

        add(filterField, gbh.nextCol().fillHorizontally().get());

        add(invertFilterCheckBox, gbh.nextRow().get());
        //gbh.addLabelFieldPair(this,"User type",userTypeBox,null,true,false,1);
        add(grantToolBar, gbh.nextRowFirstCol().spanX().get());
        add(scrollMainPane, gbh.nextRowFirstCol().spanY().fillBoth().get());
        load_userList();
        setEnableElements(true);
        act = CREATE_TABLE;
        execute_thread();

    }

    private boolean isTable() {
        return defaultDatabaseObject instanceof DatabaseTable || defaultDatabaseObject instanceof DefaultDatabaseView;
    }

    private boolean isProcedure() {
        return defaultDatabaseObject instanceof DefaultDatabaseProcedure;
    }

    private boolean isGeneratorOrException() {
        return defaultDatabaseObject instanceof DefaultDatabaseSequence || defaultDatabaseObject instanceof DefaultDatabaseException;
    }

    public void setValues(AbstractDatabaseObject ddo) {
        defaultDatabaseObject = ddo;
        fillTypeObject();
        databaseConnection = ddo.getHost().getDatabaseConnection();
        querySender = new DefaultStatementExecutor(databaseConnection, true);
        querySender.setCloseConnectionAfterQuery(false);
        initComponents();
    }

    void fillTypeObject() {
        typeObject = defaultDatabaseObject.getMetaDataKey().replace("SYSTEM_", "");

        if (typeObject.equals(NamedObject.META_TYPES[NamedObject.VIEW]))
            typeObject = NamedObject.META_TYPES[NamedObject.TABLE];
    }

    void load_userList() {

        usersVector = new Vector<>();
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
            default:
                break;
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
            connection = ConnectionManager.getConnection(databaseConnection).unwrap(Connection.class);
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
        userManager = getUserManager(userManager, databaseConnection);
        Map<String, IFBUser> users;
        try {
            users = userManager.getUsers();
            for (IFBUser u : users.values()) {
                usersVector.addElement(u.getUserName());
            }
            usersVector.addElement("PUBLIC");
        } catch (Exception e) {
            System.out.println(e);
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
                usersVector.addElement(role);
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

    void load_fields_table() {
        privilegesForFieldTable.setModel(new RoleTableModel(headersFields, 0));
        fieldName = new Vector<>();
        fieldType = new Vector<>();
        try {
            String query = "Select RF.RDB$FIELD_NAME,F.RDB$FIELD_TYPE\n" +
                    "from RDB$RELATION_FIELDS AS RF left join RDB$FIELDS AS F\n" +
                    "ON F.RDB$FIELD_NAME=RF.RDB$FIELD_SOURCE\n" +
                    "WHERE RF.RDB$RELATION_NAME='" + defaultDatabaseObject.getName() + "'";
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
                        "where (rdb$Relation_name='" + defaultDatabaseObject.getName() + "') and (rdb$user='" + /*userList.getSelectedValue().trim()*/"" + "') and\n" +
                        "(RDB$FIELD_NAME='" + fieldName.elementAt(i) + "')";
                ResultSet rs1 = querySender.execute(QueryTypes.SELECT, s, -1).getResultSet();
                Vector<Object> roleData = new Vector<Object>();
                roleData.add(fieldName.elementAt(i));
                roleData.add(fieldType.elementAt(i));
                for (int k = 0; k < 2; k++)
                    roleData.add(icons[REVOKE]);
                ((RoleTableModel) privilegesForFieldTable.getModel()).addRow(roleData);
                while (rs1.next()) {
                    String grant = rs1.getString(1).trim();
                    int ind = grants.indexOf(grant);
                    if (ind == 1)
                        if (rs1.getObject(2).equals(0)) {
                            privilegesForFieldTable.setValueAt(icons[GRANT], i, 2);
                        } else
                            privilegesForFieldTable.getModel().setValueAt(icons[GRANT_OPTION], i, 2);
                    if (ind == 5)
                        if (rs1.getObject(2).equals(0)) {
                            privilegesForFieldTable.setValueAt(icons[GRANT], i, 3);
                        } else
                            privilegesForFieldTable.getModel().setValueAt(icons[GRANT_OPTION], i, 3);

                }
                rs1.close();
                querySender.releaseResources();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
    }

    void load_table() {
        tablePrivileges.setModel(new RoleTableModel(headers, 0));
        relName.removeAllElements();
        relGranted.removeAllElements();
        try {
            String query = "select distinct RDB$PRIVILEGE,RDB$GRANT_OPTION,RDB$FIELD_NAME from RDB$USER_PRIVILEGES\n" +
                    "where (rdb$Relation_name= '" + defaultDatabaseObject.getName() + "') and (rdb$user=?)";
            PreparedStatement statement = querySender.getPreparedStatement(query);
            String filter = filterField.getText().toUpperCase();
            boolean invert = invertFilterCheckBox.isSelected();
            for (int i = 0, g = 0; g < usersVector.size(); g++, i++) {
                String username = usersVector.elementAt(g).toUpperCase();
                boolean contains = username.contains(filter);
                boolean adding = !invert == contains;
                addRow(username, "", false, false);
                Vector<Object> roleData = new Vector<Object>();
                if (adding) {
                    statement.setString(1, relName.elementAt(i));
                    ResultSet rs1 = querySender.execute(QueryTypes.SELECT, statement).getResultSet();

                    Object[] obj = {relName.elementAt(i), Color.BLACK};
                    roleData.add(obj);
                    for (int k = 0; k < headers.length; k++)
                        roleData.add(icons[REVOKE]);
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
                                            roleData.set(ind + 1, icons[GRANT]);
                                        } else
                                            roleData.set(ind + 1, icons[GRANT_OPTION]);
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
                }
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
            Log.error("connection.close");
        } catch (SQLException e) {
            Log.error(e.getMessage());
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }

    }

    void removeRow(int i) {
        relName.remove(i);
        relGranted.remove(i);
    }

    void addRow(String name, String type, boolean system_flag, boolean grant_flag) {
        relName.add(name);
        relGranted.add(grant_flag);
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

    void setEnableElements(boolean enable) {
        //enableComponents(GUIUtilities.getParentFrame(), enable);
        enableElements = enable;
        for (int i = 0; i < grantButtons.length; i++)
            grantButtons[i].setEnabled(enable);
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldButtons[i].setEnabled(enable);
    }

    void execute_thread() {
        if (enableElements) {
            setEnableElements(false);
            org.underworldlabs.swing.util.SwingWorker sw = new SwingWorker() {
                @Override
                public Object construct() {
                    runToThread();
                    return "DONE";
                }
            };
            sw.start();
        }
    }

    public void runToThread() {
        int col;
        try {
            switch (act) {
                case CREATE_TABLE:
                    load_table();
                    break;
                case ALL_GRANTS_TO_ALL_OBJECTS:
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        grant_all_on_role(GRANT, row);

                    }
                    setEnableElements(true);
                    break;
                case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        grant_all_on_role(GRANT_OPTION, row);

                    }
                    setEnableElements(true);
                    break;
                case NO_ALL_GRANTS_TO_ALL_OBJECTS:
                    for (int row = 0; row < relName.size() && !enableElements; row++) {
                        grant_all_on_role(REVOKE, row);

                    }
                    setEnableElements(true);
                    break;
                case GRANT_TO_ALL_OBJECTS:
                    col = tablePrivileges.getSelectedColumn();
                    if (col > 0)
                        for (int row = 0; row < relName.size() && !enableElements; row++) {
                            grant_on_role(GRANT, row, col);
                        }
                    setEnableElements(true);
                    break;
                case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                    col = tablePrivileges.getSelectedColumn();
                    if (col > 0)
                        for (int row = 0; row < relName.size() && !enableElements; row++) {
                            grant_on_role(GRANT_OPTION, row, col);
                        }
                    setEnableElements(true);
                    break;
                case NO_GRANT_TO_ALL_OBJECTS:
                    col = tablePrivileges.getSelectedColumn();
                    if (col > 0)
                        for (int row = 0; row < relName.size() && !enableElements; row++) {
                            grant_on_role(REVOKE, row, col);
                        }
                    setEnableElements(true);
                    break;
            }
            querySender.releaseResources();
            querySender.execute(QueryTypes.COMMIT, (String) null);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
            setEnableElements(true);
        }
    }

    private String getGrantQuery(String user, int typeGrant, String grantX) throws SQLException {
        String query = "";
        switch (typeGrant) {
            case REVOKE:
                query = "REVOKE " + grantX + " ON " + typeObject + " \"" + defaultDatabaseObject.getName() + "\" FROM \""
                        + user + "\";";
                break;
            case GRANT:
                query = "GRANT " + grantX + " ON " + typeObject + " \"" + defaultDatabaseObject.getName() + "\" TO \""
                        + user + "\";";
                break;
            case GRANT_OPTION:
                query = "GRANT " + grantX + " ON " + typeObject + " \"" + defaultDatabaseObject.getName() + "\" TO \""
                        + user + "\" WITH GRANT OPTION;";
        }
        return query;

    }

    void grant_all_on_role(int grantt, int row) {
        if (row < tablePrivileges.getRowCount()) {
            try {
                String grantX = "ALL";
                if (headers.length <= 2)
                    grantX = headers[1].toUpperCase();
                grant_all_query(getGrantQuery(relName.get(row), grantt, grantX), icons[grantt], row, grantt);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void grant_on_role(int grantt, int row, int col) {
        if (row < tablePrivileges.getRowCount()) {
            try {
                grant_query(getGrantQuery(relName.get(row), grantt, headers[col].toUpperCase()), icons[grantt], row, col, tablePrivileges);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                querySender.releaseResources();
            }
        }
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
            querySender.releaseResources();
        }
    }

    void grant_all_query(String query, Icon icon, int row, int grantt) {
        try {
            querySender.execute(QueryTypes.GRANT, query, -1);
            for (int i = 1; i < headers.length; i++)
                tablePrivileges.setValueAt(icon, row, i);
        } catch (Exception e) {
            Log.error(e.getMessage());
            for (int i = 1; i < headers.length; i++) {
                grant_on_role(grantt, row, i);
                //grant_case(grantt, row, i);
            }
        } finally {
            querySender.releaseResources();
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
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JButton)
            for (int i = 0; i < iconNames.length; i++)
                if (iconNames[i].equals(e.getActionCommand())) {
                    act = i;
                    break;
                } else
                    act = CREATE_TABLE;
        execute_thread();
    }
}
