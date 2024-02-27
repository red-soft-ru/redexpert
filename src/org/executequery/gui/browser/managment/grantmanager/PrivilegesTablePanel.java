package org.executequery.gui.browser.managment.grantmanager;

import biz.redsoft.IFBUser;
import biz.redsoft.IFBUserManager;
import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTableCellRenderer;
import org.executequery.components.table.RoleTableModel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.GrantManagerPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static org.executequery.gui.browser.GrantManagerPanel.*;

public class PrivilegesTablePanel extends JPanel implements ActionListener {

    public static final int USER_OBJECTS = 0;
    public static final int OBJECT_USERS = 1;

    private static final int REVOKE = 0;
    private static final int GRANT = 1;
    private static final int GRANT_OPTION = 2;
    private static final int GRANT_FIELD = 3;
    JToolBar grantToolBar;
    JToolBar grantFieldsToolbar;
    String grants;// "SUDIXRGA";
    String[] headers;// {bundleString("Object"), "Select", "Update", "Delete", "Insert", "Execute", "References", "Usage"};
    String[] headersFields = {bundleString("Field"), bundleString("Type"), "Update", "References"};
    Icon[] icons;
    Vector<NamedObject> objectVector;
    Map<NamedObject, Vector<Object>> tableMap;
    Vector<DatabaseColumn> columnVector;
    Map<DatabaseColumn, Vector<Object>> tableForColumnsMap;
    DatabaseConnection databaseConnection;
    int act;
    String[] iconNamesForFields = {"no_grant_vertical", "no_grant_gorisont", "grant_vertical", "grant_gorisont", "admin_option_vertical", "admin_option_gorisont"};
    String[] iconNames = {"no_grant_vertical", "no_grant_gorisont", "no_grant_all", "grant_vertical", "grant_gorisont", "grant_all", "admin_option_vertical", "admin_option_gorisont", "admin_option_all"};
    String[] toolTips = bundleStrings(new String[]{"no_grant_vertical", "no_grant_gorisont", "no_grant_all", "grant_vertical", "grant_gorisont", "grant_all", "admin_option_vertical", "admin_option_gorisont", "admin_option_all"});
    int firstGrantColumn = 1;
    int col_execute = 6;
    int col_usage = 7;
    List<TypeObject> objectTypes;
    boolean enableElements;
    boolean inited = false;
    long startTime;
    boolean visibleProgress = false;
    private final int typeTable;
    private NamedObject user;
    private NamedObject relation;
    private RolloverButton[] grantFieldButtons;
    private RolloverButton[] grantButtons;
    private JTable tablePrivileges;
    private JTable privilegesForFieldTable;
    private JComboBox<String> filterBox;
    private JTextField filterField;
    private JCheckBox invertFilterCheckBox;
    private JPanel bottomPanel;
    private JProgressBar progressBar;
    private RolloverButton refreshButton;
    private RolloverButton cancelButton;
    private JCheckBox systemCheck;
    private EQCheckCombox objectTypeBox;
    private DefaultStatementExecutor querySender;
    //private EQCheckCombox userBox;
    private final int buttonSize = 20;
    private GrantManagerPanel grantManagerPanel;

    public PrivilegesTablePanel(int typeTable, GrantManagerPanel grantManagerPanel) {
        this.grantManagerPanel = grantManagerPanel;
        this.typeTable = typeTable;
        icons = new Icon[4];
        icons[GRANT] = GUIUtilities.loadIcon(BrowserConstants.GRANT_IMAGE);
        icons[REVOKE] = GUIUtilities.loadIcon(BrowserConstants.NO_GRANT_IMAGE);
        icons[GRANT_OPTION] = GUIUtilities.loadIcon(BrowserConstants.ADMIN_OPTION_IMAGE);
        icons[GRANT_FIELD] = GUIUtilities.loadIcon(BrowserConstants.FIELD_GRANT_IMAGE);
        objectVector = new Vector<>();
        tableMap = new HashMap<>();
        tableForColumnsMap = new HashMap<>();
    }

    public void setGrantManagerPanel(GrantManagerPanel grantManagerPanel) {
        this.grantManagerPanel = grantManagerPanel;
    }

    boolean isFilledObjectBox = false;

    public static boolean supportType(int metattype, DatabaseConnection databaseConnection) {
        boolean supported = true;
        if (metattype != NamedObject.USER) {
            try {
                supported = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).supportedObject(metattype);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (supported)
                if (databaseConnection.getMajorServerVersion() < 3) {
                    if (metattype == NamedObject.SEQUENCE || metattype == NamedObject.EXCEPTION)
                        return false;
                }
        }
        return supported;
    }

    public void setDatabaseObject(NamedObject databaseObject) {
        if (databaseObject != null) {
            if (typeTable == USER_OBJECTS)
                this.user = databaseObject;
            else
                this.relation = databaseObject;
            databaseConnection = ((AbstractDatabaseObject) databaseObject).getHost().getDatabaseConnection();
            querySender = new DefaultStatementExecutor(databaseConnection, true);
            querySender.setCloseConnectionAfterQuery(false);
            if (!inited)
                initComponents();
            else {
                fillObjectBox();
                act = CREATE_TABLE;
                execute_thread();
            }
        }
    }

    private void initComponents() {
        removeAll();
        if (typeTable == OBJECT_USERS) {
            if (isTable(relation)) {
                headers = new String[]{bundleString("User"), "Select", "Update", "Delete", "Insert", "References"};
                grants = "SUDIRA";
            } else if (isGeneratorOrException(relation)) {
                headers = new String[]{bundleString("User"), "Usage"};
                grants = "GA";
            } else {
                headers = new String[]{bundleString("User"), "Execute"};
                grants = "XA";
            }
        } else {
            headers = new String[]{bundleString("Object"), "Select", "Update", "Delete", "Insert", "References", "Execute", "Usage"};
            grants = "SUDIRXGA";
        }
        col_execute = grants.indexOf("X") + firstGrantColumn;
        if (col_execute < firstGrantColumn)
            col_execute = -1;
        col_usage = grants.indexOf("G") + firstGrantColumn;
        if (col_usage < firstGrantColumn)
            col_usage = -1;
        objectTypeBox = new EQCheckCombox();

        fillObjectBox();

        objectTypeBox.getModel().addListCheckListener(new ListCheckListener() {
            @Override
            public void removeCheck(ListEvent listEvent) {
                if (!isFilledObjectBox) {
                    act = CREATE_TABLE;
                    execute_thread();
                }
            }

            @Override
            public void addCheck(ListEvent listEvent) {
                if (!isFilledObjectBox) {
                    act = CREATE_TABLE;
                    execute_thread();
                }
            }
        });
        grantFieldButtons = new RolloverButton[iconNamesForFields.length];
        for (int i = 0; i < grantFieldButtons.length; i++) {
            grantFieldButtons[i] = new RolloverButton();
            grantFieldButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/" + iconNamesForFields[i] + ".svg", buttonSize));
            grantFieldButtons[i].setMouseEnteredContentAreaFill(false);
            grantFieldButtons[i].setActionCommand("field_" + i);
            grantFieldButtons[i].setToolTipText(bundleString(iconNamesForFields[i]));
            grantFieldButtons[i].addActionListener(this);
        }
        tablePrivileges = new JTable();
        privilegesForFieldTable = new JTable();
        BrowserTableCellRenderer bctr = new BrowserTableCellRenderer();
        tablePrivileges.setDefaultRenderer(Object.class, bctr);
        privilegesForFieldTable.setDefaultRenderer(Object.class, bctr);
        grantToolBar = new JToolBar();
        grantToolBar.setFloatable(false);
        grantButtons = new RolloverButton[iconNames.length];
        for (int i = 0; i < grantButtons.length; i++) {
            grantButtons[i] = new RolloverButton();
            grantButtons[i].setIcon(IconUtilities.loadIcon("/org/executequery/icons/" + iconNames[i] + ".svg", buttonSize));
            grantButtons[i].setActionCommand(iconNames[i]);
            grantButtons[i].setMouseEnteredContentAreaFill(false);
            grantButtons[i].setToolTipText(toolTips[i]);
            grantToolBar.add(grantButtons[i]);
            grantButtons[i].addActionListener(this);
        }
        grantFieldsToolbar = new JToolBar();
        grantFieldsToolbar.setFloatable(false);

        tablePrivileges.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = tablePrivileges.getSelectedRow();
                if (row > 0) {
                    if (typeTable == USER_OBJECTS)
                        relation = objectVector.elementAt(row);
                    else user = objectVector.elementAt(row);
                    if (typeTable == OBJECT_USERS)
                        checkVisibleGrantOptionButtons();
                }

            }
        });

        tablePrivileges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enableElements) {
                    int row = tablePrivileges.getSelectedRow();
                    if (typeTable == USER_OBJECTS)
                        relation = objectVector.elementAt(row);
                    else user = objectVector.elementAt(row);
                    if (e.getClickCount() > 1) {
                        int col = tablePrivileges.getSelectedColumn();
                        if (col > 0) {
                            if (tablePrivileges.getValueAt(row, col).equals(icons[GRANT])) {
                                if (user instanceof DefaultDatabaseUser || user instanceof DefaultDatabaseRole)
                                    grantOnRole(GRANT_OPTION, row, col);
                                else grantOnRole(REVOKE, row, col);
                            } else if (tablePrivileges.getValueAt(row, col).equals(icons[GRANT_OPTION])) {
                                grantOnRole(REVOKE, row, col);
                            } else {
                                grantOnRole(GRANT, row, col);
                            }
                            querySender.releaseResources();
                        }
                    }
                    if (row >= 0) {
                        setVisiblePanelOfTable(isTable(relation));
                    }
                }
            }
        });

        privilegesForFieldTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enableElements) {
                    int row = tablePrivileges.getSelectedRow();
                    if (row >= 0) {
                        if (typeTable == USER_OBJECTS)
                            relation = objectVector.elementAt(row);
                        else user = objectVector.elementAt(row);
                        int row2 = privilegesForFieldTable.getSelectedRow();
                        if (e.getClickCount() > 1) {
                            int col = privilegesForFieldTable.getSelectedColumn();
                            if (col > 1) {
                                if (privilegesForFieldTable.getValueAt(row2, col).equals(icons[GRANT])) {
                                    if (isUserOrRole(user))
                                        grantOnRoleForCol(GRANT_OPTION, row, col, row2);
                                    else grantOnRoleForCol(REVOKE, row, col, row2);
                                } else if (privilegesForFieldTable.getValueAt(row2, col).equals(icons[GRANT_OPTION])) {
                                    grantOnRoleForCol(REVOKE, row, col, row2);
                                } else {
                                    grantOnRoleForCol(GRANT, row, col, row2);
                                }
                            }
                        }
                    }

                }
            }
        });

        refreshButton = new RolloverButton();
        refreshButton.setToolTipText(bundleString("Refresh"));
        refreshButton.setIcon(IconUtilities.loadIcon("/org/executequery/icons/Refresh16.svg", buttonSize));
        refreshButton.setMouseEnteredContentAreaFill(false);
        refreshButton.addActionListener(this);

        cancelButton = new RolloverButton();
        cancelButton.setToolTipText(bundleString("CancelFill"));
        cancelButton.setIcon(IconUtilities.loadIcon("/org/executequery/icons/Stop16.svg", buttonSize));
        cancelButton.setMouseEnteredContentAreaFill(false);
        cancelButton.addActionListener(this);

        filterBox = new JComboBox<>();
        filterField = new JTextField();
        filterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                act = CREATE_TABLE;
                execute_thread();
                filterField.requestFocusInWindow();
            }
        });
        invertFilterCheckBox = new JCheckBox();
        filterBox.setModel(new DefaultComboBoxModel<>(bundleStrings(new String[]{"DisplayAll", "GrantedOnly", "Non-grantedOnly"})));
        filterBox.addActionListener(this);

        filterField.addActionListener(this);

        invertFilterCheckBox.setText(bundleString("InvertFilter"));
        invertFilterCheckBox.addActionListener(this);
        systemCheck = new JCheckBox();
        systemCheck.setText(bundleString("ShowSystemObjects"));
        systemCheck.addActionListener(this);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);


        arrangeComponents();
        setEnableElements(true);
        act = CREATE_TABLE;
        execute_thread();
        inited = true;

    }

    void checkVisibleGrantOptionButtons() {
        grantButtons[GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION].setVisible(isUserOrRole(user));
        grantButtons[ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION].setVisible(isUserOrRole(user));
        grantButtons[ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION].setVisible(isUserOrRole(user));
        grantFieldButtons[GRANT_OPTION * 2].setVisible(isUserOrRole(user));
        grantFieldButtons[GRANT_OPTION * 2 + 1].setVisible(isUserOrRole(user));
    }

    private void arrangeComponents() {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.8);
        JScrollPane scrollMainPane = new JScrollPane(tablePrivileges);
        splitPane.setTopComponent(scrollMainPane);
        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setDefaultsStatic().defaults();
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldsToolbar.add(grantFieldButtons[i]);
        grantToolBar.add(refreshButton);
        grantToolBar.add(cancelButton);

        bottomPanel.add(grantFieldsToolbar, gridBagHelper.fillHorizontally().spanX().get());
        JScrollPane scrollPane = new JScrollPane(privilegesForFieldTable);
        bottomPanel.add(scrollPane, gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().get());

        splitPane.setBottomComponent(bottomPanel);

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        gbh.defaults();
        add(objectTypeBox, gbh.nextRowFirstCol().fillHorizontally().setMinWeightY().setHeight(1).get());

        add(systemCheck, gbh.nextRow().get());

        add(filterBox, gbh.previousRow().nextCol().fillHorizontally().get());

        add(filterField, gbh.nextCol().fillHorizontally().get());

        add(invertFilterCheckBox, gbh.nextRow().get());

        add(progressBar, gbh.nextRowFirstCol().fillHorizontally().spanX().get());

        add(grantToolBar, gbh.nextRowFirstCol().fillHorizontally().spanX().get());

        add(splitPane, gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    void fillObjectBox() {
        isFilledObjectBox = true;
        objectTypeBox.getModel().clear();
        if (typeTable == USER_OBJECTS)
            objectTypes = createObjectTypes(NamedObject.TABLE, NamedObject.GLOBAL_TEMPORARY, NamedObject.VIEW, NamedObject.PROCEDURE, NamedObject.FUNCTION, NamedObject.PACKAGE, NamedObject.SEQUENCE, NamedObject.EXCEPTION);//"Tables", "GlobalTemporaries", "Views", "Procedures", "Functions", "Packages", "Generators", "Exceptions");
        else
            objectTypes = createObjectTypes(NamedObject.USER, NamedObject.ROLE, NamedObject.VIEW, NamedObject.TRIGGER, NamedObject.PROCEDURE, NamedObject.FUNCTION, NamedObject.PACKAGE);//"Users", "Roles", "Views", "Triggers", "Procedures", "Functions", "Packages");
        for (TypeObject obj : objectTypes) {
            objectTypeBox.getModel().addElement(obj);
            objectTypeBox.getModel().addCheck(obj);
        }
        isFilledObjectBox = false;
    }

    void setEnableElements(boolean enable) {
        enableElements = enable;

        for (int i = 0; i < grantButtons.length; i++)
            grantButtons[i].setEnabled(enable);
        for (int i = 0; i < grantFieldButtons.length; i++)
            grantFieldButtons[i].setEnabled(enable);
        objectTypeBox.setEnabled(enable);
        invertFilterCheckBox.setEnabled(enable);
        filterBox.setEnabled(enable);
        filterField.setEnabled(enable);
        refreshButton.setEnabled(enable);
        systemCheck.setEnabled(enable);
        if (enable) {
            progressBar.setValue(0);
            setProgressBarVisible(false);
            checkVisibleGrantOptionButtons();
        }
        if (databaseConnection != null)
            ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).setPauseLoadingTreeForSearch(!enable);
        if (grantManagerPanel != null)
            grantManagerPanel.setElementsEnabled(enable);
    }

    void grantOnRole(int grant, int row, int col) {
        if (row < tablePrivileges.getRowCount()) {
            if (typeTable == USER_OBJECTS)
                relation = objectVector.elementAt(row);
            else user = objectVector.elementAt(row);
            if (isExecutable(relation) && col != col_execute)
                return;
            else if (isTable(relation) && (col == col_execute || col == col_usage))
                return;
            else if (isGeneratorOrException(relation) && col != col_usage)
                return;
            if (grant == GRANT && tablePrivileges.getValueAt(row, col).equals(icons[GRANT_OPTION]))
                grantOnRole(REVOKE, row, col);
            grantQuery(getGrantQuery(user, grant, headers[col], relation), icons[grant], row, col, tablePrivileges);
            querySender.releaseResourcesWithoutCommit();
        }
    }

    void grantOnRoleForCol(int grant, int row, int col, int row2) {
        if (row < tablePrivileges.getRowCount() && row2 < privilegesForFieldTable.getRowCount()) {
            if (typeTable == USER_OBJECTS)
                relation = objectVector.elementAt(row);
            else user = objectVector.elementAt(row);
            if (grant == GRANT && privilegesForFieldTable.getValueAt(row2, col).equals(icons[GRANT_OPTION]))
                grantOnRoleForCol(REVOKE, row, col, row2);
            grantQuery(getGrantQuery(user, grant, headersFields[col], relation, columnVector.elementAt(row2).getName()), icons[grant], row2, col, privilegesForFieldTable);
            querySender.releaseResources();
        }
    }

    void grantCase(int grant, int row, int col) {
        grantOnRole(grant, row, col);
    }

    void grantAllOnRole(int grant, int row) {
        if (row < tablePrivileges.getRowCount()) {
            if (typeTable == USER_OBJECTS)
                relation = objectVector.elementAt(row);
            else user = objectVector.elementAt(row);
            if (grant == 1) {
                boolean containsAdm = false;
                for (int i = firstGrantColumn; i < headers.length; i++)
                    if (tablePrivileges.getValueAt(row, i).equals(icons[GRANT_OPTION])) {
                        containsAdm = true;
                        break;
                    }
                if (containsAdm) grantAllOnRole(REVOKE, row);
            }

            if (isTable(relation))
                grantAllQuery(getGrantQuery(user, grant, "ALL", relation), icons[grant], row, grant);
            else if (isExecutable(relation))
                grantQuery(getGrantQuery(user, grant, "EXECUTE", relation), icons[grant], row, col_execute, tablePrivileges);
            else if (isGeneratorOrException(relation))
                grantQuery(getGrantQuery(user, grant, "USAGE", relation), icons[grant], row, col_usage, tablePrivileges);

        }
    }

    void grantQuery(String query, Icon icon, int row, int col, JTable t) {
        try {
            SqlStatementResult result = querySender.execute(QueryTypes.GRANT, query);
            if (result.isException()) {
                Log.info("ErrorQuery=" + query);
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
            SqlStatementResult result = querySender.execute(QueryTypes.GRANT, query, -1);
            if (result.isException()) {

                Log.info("ErrorQuery=" + query);
                throw result.getSqlException();
            } else {
                for (int i = firstGrantColumn; i < headers.length; i++)
                    if (i != col_execute && i != col_usage)
                        tablePrivileges.setValueAt(icon, row, i);
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            for (int i = firstGrantColumn; i < headers.length; i++) {
                grantCase(grant, row, i);
            }
        } finally {
            querySender.releaseResourcesWithoutCommit();
        }
    }

    private String getGrantQuery(NamedObject grantor, int typeGrant, String grant, NamedObject relation) {
        return getGrantQuery(grantor, typeGrant, grant, relation, null);

    }

    private String getGrantQuery(NamedObject grantor, int typeGrant, String grant, NamedObject relation, String... fields) {
        GrantBuilder gb = new GrantBuilder(querySender.getDatabaseConnection());
        gb.setGrantor(grantor.getName()).setRelation(relation.getName()).setGrantType(grant);
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
        gb.setGrantorType(grantor.getMetaDataKey().replace("SYSTEM ", "").replace("DDL ", "").replace("DATABASE ", ""));
        if (fields != null)
            gb.appendFields(fields);
        return gb.getSQLQuery();

    }

    void execute_thread() {
        if (enableElements) {
            setEnableElements(false);
            SwingWorker sw = new SwingWorker("executeThreadForGrants") {
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
        switch (act) {
            case CREATE_TABLE:
                loadTable();
                break;
            case ALL_GRANTS_TO_ALL_OBJECTS:
                //jProgressBar1.setMaximum(objectVector.size());
                for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                    // jProgressBar1.setValue(row);
                    //isClose();
                    grantAllOnRole(GRANT, row);

                }
                //jProgressBar1.setValue(0);
                setEnableElements(true);
                break;
            case ALL_GRANTS_TO_OBJECT:
                if (tablePrivileges.getSelectedRow() >= 0)
                    grantAllOnRole(GRANT, tablePrivileges.getSelectedRow());
                break;
            case ALL_GRANTS_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                    grantAllOnRole(GRANT_OPTION, row);

                }
                setEnableElements(true);
                break;
            case ALL_GRANTS_TO_OBJECT_WITH_GRANT_OPTION:
                if (tablePrivileges.getSelectedRow() >= 0)
                    grantAllOnRole(GRANT_OPTION, tablePrivileges.getSelectedRow());
                break;
            case NO_ALL_GRANTS_TO_ALL_OBJECTS:
                for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                    grantAllOnRole(REVOKE, row);

                }
                setEnableElements(true);
                break;
            case NO_ALL_GRANTS_TO_OBJECT:
                if (tablePrivileges.getSelectedRow() >= 0)
                    grantAllOnRole(REVOKE, tablePrivileges.getSelectedRow());
                break;
            case GRANT_TO_ALL_OBJECTS:
                col = tablePrivileges.getSelectedColumn();
                if (col > 0)
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        grantOnRole(GRANT, row, col);
                    }
                setEnableElements(true);
                break;
            case GRANT_TO_ALL_OBJECTS_WITH_GRANT_OPTION:
                col = tablePrivileges.getSelectedColumn();
                if (col > 0)
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        grantOnRole(GRANT_OPTION, row, col);
                    }
                setEnableElements(true);
                break;
            case NO_GRANT_TO_ALL_OBJECTS:
                col = tablePrivileges.getSelectedColumn();
                if (col > 0)
                    for (int row = 0; row < objectVector.size() && !enableElements; row++) {
                        grantOnRole(REVOKE, row, col);
                    }
                setEnableElements(true);
                break;
        }
        querySender.releaseResources();
        setEnableElements(true);
    }

    private void grantSomeForCol(RolloverButton button) {
        int row2 = tablePrivileges.getSelectedRow();
        int index = Integer.parseInt(button.getActionCommand().replace("field_", ""));
        int grant = index / 2;
        if (index % 2 == 0) {
            int col = privilegesForFieldTable.getSelectedColumn();
            if (col > 1)
                for (int row = 0; row < columnVector.size(); row++) {
                    grantOnRoleForCol(grant, row2, col, row);
                }
        } else {
            int row = privilegesForFieldTable.getSelectedRow();
            if (row >= 0)
                for (int col = 2; col < 4; col++) {
                    grantOnRoleForCol(grant, row2, col, row);
                }
        }
    }

    String buildQueryForPrivileges(int rdbTypeUser, int rdbTypeObject) {
        SelectBuilder sb = new SelectBuilder(querySender.getDatabaseConnection());
        Table userPrivileges = Table.createTable("RDB$USER_PRIVILEGES", "UP");
        sb.appendTable(userPrivileges);
        sb.appendFields(userPrivileges, "RELATION_NAME", "PRIVILEGE", "GRANT_OPTION", "FIELD_NAME");
        sb.appendField(Field.createField(userPrivileges, "USER").setAlias("USER_NAME"));
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER_TYPE"), "=", String.valueOf(rdbTypeUser)));
        String aliasCondition = "USER";
        if (typeTable == OBJECT_USERS)
            aliasCondition = "RELATION_NAME";
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, aliasCondition), "=", "?"));
        Condition rfbTypeCondition = Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", String.valueOf(rdbTypeObject)));
        if (rdbTypeObject == 1) {
            rfbTypeCondition.setLogicOperator("OR");
            rfbTypeCondition.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", 0 + ""));
        }
        sb.appendCondition(rfbTypeCondition);
        String aliasOrdering = "USER";
        if (typeTable == USER_OBJECTS)
            aliasOrdering = "RELATION_NAME";
        sb.setOrdering(Field.createField(userPrivileges, aliasOrdering).getFieldTable() + "," + Field.createField(userPrivileges, "FIELD_NAME").getFieldTable());
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

    List<TypeObject> createObjectTypes(int... types) {
        List<TypeObject> list = new ArrayList<>();
        for (int type : types) {

            if (supportType(type, databaseConnection))
                list.add(new TypeObject(type));
        }
        return list;
    }

    List<NamedObject> getNamedObjectsFromSomeTypes(int... types) {
        List<NamedObject> resultList = new ArrayList<>();
        for (int type : types) {
            resultList.addAll(getRelationsFromType(type));
        }
        return resultList;
    }

    List<NamedObject> getUsers() {
        List<NamedObject> users = new ArrayList<>();
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
                        users.add(createUser(u.getUserName(), u.getPlugin()));
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    GUIUtilities.displayErrorMessage(e.toString());
                }
            }
        }
        users.add(createUser("PUBLIC", ""));
        return users;
    }

    IFBUserManager getUserManager(IFBUserManager userManager, DatabaseConnection dc) {
        userManager.setDatabase(dc.getSourceName());
        userManager.setHost(dc.getHost());
        userManager.setPort(dc.getPortInt());
        userManager.setUser(dc.getUserName());
        userManager.setPassword(dc.getUnencryptedPassword());
        return userManager;
    }

    DefaultDatabaseUser createUser(String name, String plugin) {
        return new DefaultDatabaseUser(
                new DefaultDatabaseMetaTag(
                        ConnectionsTreePanel
                                .getPanelFromBrowser()
                                .getDefaultDatabaseHostFromConnection(databaseConnection),
                        null,
                        null,
                        NamedObject.META_TYPES[NamedObject.USER]
                ),
                name,
                plugin
        );
    }

    List<NamedObject> getRelationsFromType(int type) {
        if (type == NamedObject.USER) {
            return getUsers();
        } else
            return ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
    }

    void addRelationsToTable(List<NamedObject> list) {
        if (list != null && list.size() > 0)
            try {
                if (typeTable == USER_OBJECTS)
                    relation = list.get(0);
                else user = list.get(0);
                int rdbTypeObject = relation.getRDBType();
                int rdbTypeUser = user.getRDBType();
                String query = buildQueryForPrivileges(rdbTypeUser, rdbTypeObject);
                PreparedStatement statement = querySender.getPreparedStatement(query);
                if (typeTable == USER_OBJECTS)
                    statement.setString(1, user.getName());
                else statement.setString(1, relation.getName());
                ResultSet rs = querySender.execute(QueryTypes.SELECT, statement).getResultSet();
                NamedObject previousObject = null;
                int lastIndex = 0;
                progressBar.setMaximum(list.size());
                progressBar.setString(Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[list.get(0).getType()]));
                while (rs.next() && !enableElements) {
                    Vector<Object> roleData;
                    NamedObject currentObject;
                    if (typeTable == USER_OBJECTS) {
                        relation = getRelationFromName(list, rs.getString("RELATION_NAME"));
                        if (relation == null)
                            continue;
                        roleData = tableMap.get(relation);
                        currentObject = relation;
                    } else {
                        user = getRelationFromName(list, rs.getString("USER_NAME"));
                        if (user == null)
                            continue;
                        roleData = tableMap.get(user);
                        currentObject = user;
                    }
                    if (currentObject != previousObject && previousObject != null) {
                        if (!visibleProgress && System.currentTimeMillis() - startTime > 1000) {
                            setProgressBarVisible(true);
                            visibleProgress = true;
                        }
                        int curIndex = list.indexOf(currentObject);
                        for (int i = lastIndex; i < curIndex && !enableElements; i++) {
                            Vector<Object> roledata = tableMap.get(list.get(i));
                            boolean granted = roledata.contains(icons[GRANT]) || roledata.contains(icons[GRANT_OPTION]);
                            boolean adding = (filterBox.getSelectedIndex() == 0 || (filterBox.getSelectedIndex() == 1) == granted);
                            if (adding)
                                ((RoleTableModel) tablePrivileges.getModel()).addRow(roledata);
                            else {
                                removeRow(list.get(i));
                            }
                            progressBar.setValue(i);
                        }
                        lastIndex = curIndex;
                    }
                    previousObject = currentObject;
                    String grant = rs.getString("PRIVILEGE").trim();
                    int ind = grants.indexOf(grant);
                    Object gr_opt = rs.getObject("GRANT_OPTION");
                    boolean fieldGrant = rs.getString("FIELD_NAME") != null;
                    if (gr_opt == null)
                        gr_opt = 0;
                    if (ind != 7) {
                        if (fieldGrant) {
                            if (roleData.get(ind + firstGrantColumn).equals(icons[REVOKE]))
                                roleData.set(ind + firstGrantColumn, icons[GRANT_FIELD]);
                        } else if (gr_opt.equals(0)) {
                            roleData.set(ind + firstGrantColumn, icons[GRANT]);
                        } else
                            roleData.set(ind + firstGrantColumn, icons[GRANT_OPTION]);
                    } else {
                        for (int i = firstGrantColumn; i < firstGrantColumn + 5; i++) {
                            if (gr_opt.equals(0)) {
                                roleData.set(i, icons[GRANT]);
                            } else
                                roleData.set(i, icons[GRANT_OPTION]);
                        }
                    }
                }
                for (int i = lastIndex; i < list.size() && !enableElements; i++) {
                    Vector<Object> roledata = tableMap.get(list.get(i));
                    boolean granted = roledata.contains(icons[GRANT]) || roledata.contains(icons[GRANT_OPTION]);
                    boolean adding = (filterBox.getSelectedIndex() == 0 || (filterBox.getSelectedIndex() == 1) == granted);
                    if (adding)
                        ((RoleTableModel) tablePrivileges.getModel()).addRow(roledata);
                    else {
                        removeRow(list.get(i));
                    }
                    progressBar.setValue(i);
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
        startTime = System.currentTimeMillis();
        visibleProgress = false;
        for (Object obj : objectTypeBox.getModel().getCheckeds()) {
            TypeObject typeObject = (TypeObject) obj;
            for (int type : typeObject.types)
                addRelations(getRelationsFromType(type));

        }
        setEnableElements(true);
        setVisiblePanelOfTable(false);

    }

    void removeRow(NamedObject relation) {
        objectVector.remove(relation);
    }

    void addRow(NamedObject namedObject) {
        objectVector.add(namedObject);
        Vector<Object> roleData = new Vector<Object>();
        Object[] obj = {namedObject, Color.BLACK};
        if (namedObject.isSystem())
            obj[1] = Color.RED;
        roleData.add(obj);
        if (typeTable == USER_OBJECTS)
            relation = namedObject;
        for (int i = firstGrantColumn; i < headers.length; i++) {
            if (isTable(relation) && (i < col_execute || typeTable == OBJECT_USERS)
                    || isExecutable(relation) && i == col_execute
                    || isGeneratorOrException(relation) && i == col_usage)
                roleData.add(icons[REVOKE]);
            else roleData.add("");
        }

        tableMap.put(namedObject, roleData);
    }

    String buildQueryForPrivilegesByField(int rdbTypeUser, int rdbTypeObject) {
        SelectBuilder sb = new SelectBuilder(querySender.getDatabaseConnection());
        Table userPrivileges = Table.createTable("RDB$USER_PRIVILEGES", "UP");
        sb.appendTable(userPrivileges);
        sb.appendFields(userPrivileges, "RELATION_NAME", "PRIVILEGE", "GRANT_OPTION", "FIELD_NAME");
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER_TYPE"), "=", String.valueOf(rdbTypeUser)));
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "USER"), "=", "?"));
        Condition rfbTypeCondition = Condition.createCondition()
                .appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", String.valueOf(rdbTypeObject)));
        if (rdbTypeObject == 1) {
            rfbTypeCondition.setLogicOperator("OR");
            rfbTypeCondition.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "OBJECT_TYPE"), "=", 0 + ""));
        }
        sb.appendCondition(rfbTypeCondition);
        sb.appendCondition(Condition.createCondition(Field.createField(userPrivileges, "RELATION_NAME"), "=", "?"));
        sb.setOrdering(Field.createField(userPrivileges, "FIELD_NAME").getFieldTable());
        return sb.getSQLQuery();
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

    void loadTableFields(NamedObject namedObject) {
        if (typeTable == USER_OBJECTS)
            relation = namedObject;
        else user = namedObject;
        privilegesForFieldTable.setModel(new RoleTableModel(headersFields, 0));
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
                    roleData.add(icons[REVOKE]);
                tableForColumnsMap.put(col, roleData);
                columnVector.add(col);
            }
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());
        }
        try {
            String query = buildQueryForPrivilegesByField(user.getRDBType(), relation.getRDBType());
            PreparedStatement statement = querySender.getPreparedStatement(query);
            statement.setString(1, user.getName());
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
                        roleData.set(2, icons[GRANT]);
                    } else
                        roleData.set(2, icons[GRANT_OPTION]);
                if (ind == 4)
                    if (gr_opt.equals(0)) {
                        roleData.set(3, icons[GRANT]);
                    } else
                        roleData.set(3, icons[GRANT_OPTION]);
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


    void setVisiblePanelOfTable(boolean flag) {
        bottomPanel.setVisible(flag);
        if (flag) {
            int row = tablePrivileges.getSelectedRow();
            bottomPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf") + "[" + objectVector.elementAt(row) + "]"));
            loadTableFields(objectVector.elementAt(row));
            ((JSplitPane) bottomPanel.getParent()).setDividerLocation(0.7);
        } else {
            bottomPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ColumnsOf")));
            privilegesForFieldTable.setModel(new RoleTableModel(headersFields, 0));
            ((JSplitPane) bottomPanel.getParent()).setDividerLocation(1);
        }
        repaint();
    }

    void setProgressBarVisible(boolean visible) {
        cancelButton.setVisible(visible);
        progressBar.setVisible(visible);
    }

    private boolean isUserOrRole(NamedObject defaultDatabaseObject) {
        return defaultDatabaseObject instanceof DefaultDatabaseUser || defaultDatabaseObject instanceof DefaultDatabaseRole;
    }

    private boolean isTable(NamedObject defaultDatabaseObject) {
        return defaultDatabaseObject instanceof DefaultDatabaseObject;
    }

    private boolean isExecutable(NamedObject defaultDatabaseObject) {
        return defaultDatabaseObject instanceof DefaultDatabaseExecutable;
    }

    private boolean isGeneratorOrException(NamedObject defaultDatabaseObject) {
        return defaultDatabaseObject instanceof DefaultDatabaseSequence || defaultDatabaseObject instanceof DefaultDatabaseException;
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

    public String[] bundleStringsOf(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            if (keys.length > 0)
                keys[i] = bundleString(keys[i]);
        }
        return keys;
    }

    void addRelations(List<NamedObject> objectList) {
        try {

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
            if (!enableElements) addRelationsToTable(resultList);
        } catch (Exception e) {
            GUIUtilities.displayErrorMessage(e.getMessage());

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            setEnableElements(true);

        } else if (e.getSource() == refreshButton) {
            act = CREATE_TABLE;
            execute_thread();

        } else if (e.getActionCommand() != null && e.getActionCommand().startsWith("field_")) {
            grantSomeForCol((RolloverButton) e.getSource());

        } else {

            if (e.getSource() instanceof RolloverButton) {
                for (int i = 0; i < iconNames.length; i++) {
                    if (iconNames[i].equals(e.getActionCommand())) {
                        act = i;
                        break;
                    } else
                        act = CREATE_TABLE;
                }
            }

            execute_thread();
        }

        ((Component) e.getSource()).requestFocusInWindow();
    }

    public void cleanup() {

        if (grantToolBar != null) {
            for (Component comp : grantToolBar.getComponents()) {
                if (comp instanceof RolloverButton) {
                    ((RolloverButton) comp).removeActionListener(this);
                }
            }
        }
        if (grantFieldsToolbar != null)
            for (Component comp : grantFieldsToolbar.getComponents()) {
                if (comp instanceof RolloverButton) {
                    ((RolloverButton) comp).removeActionListener(this);
                }
            }
    }

    class TypeObject {
        int type;
        List<Integer> types;

        TypeObject(int type) {
            this.type = type;
            types = new ArrayList<>();
            types.add(type);
            if (NamedObject.getSystemTypeFromType(type) != -1)
                types.add(NamedObject.getSystemTypeFromType(type));
            if (type == NamedObject.TRIGGER) {
                types.add(NamedObject.DDL_TRIGGER);
                types.add(NamedObject.DATABASE_TRIGGER);
            }
        }


        @Override
        public String toString() {
            return Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]);
        }


    }
}
