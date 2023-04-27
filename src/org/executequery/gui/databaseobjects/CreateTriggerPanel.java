package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class CreateTriggerPanel extends AbstractCreateExternalObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.TRIGGER);

    //common components for table and database trigger
    public static final String EDIT_TITLE = getEditTitle(NamedObject.TRIGGER);

    private static final String TRIGGER = Bundles.get(CreateTriggerPanel.class, "table-trigger");
    private static final String DB_TRIGGER = Bundles.get(CreateTriggerPanel.class, "database-trigger");
    private static final String DDL_TRIGGER = Bundles.get(CreateTriggerPanel.class, "DDL-trigger");

    static String[] meta_types = {"FUNCTION",
            "INDEX",
            "PROCEDURE",
            "SEQUENCE",
            "TABLE",
            "TRIGGER",
            "VIEW",
            "DOMAIN",
            "EXCEPTION",
            "PACKAGE",
            "USER",
            "COLLATION",
            "CHARACTER SET",
            "MAPPING",
            "ROLE",
            "PACKAGE BODY",
    };
    private int triggerType;
    private DefaultDatabaseTrigger trigger;
    private JComboBox typeTriggerCombo;
    private JSpinner positionField;

    //components for database trigger
    private JCheckBox activeBox;
    private JLabel positionLabel;
    private SimpleSqlTextPanel sqlBodyText;

    //components for table trigger
    private List<Component> databaseTriggerComponents;
    private JComboBox actionCombo;
    private JLabel actionLabel;
    private JLabel labelTable;
    private List<Component> tableTriggerComponents;
    private JLabel beforeAfterLabel;

    //components for ddl trigger
    private JComboBox typeTableTriggerCombo;
    private JCheckBox insertBox;
    private JCheckBox deleteBox;
    private JCheckBox updateBox;
    private JPanel ddlTriggerPanel;
    private List<Component> ddlTableTriggerComponents;
    private List<JCheckBox> ddlCheckBoxes;
    private JScrollPane scrollDDL;
    private JCheckBox anyDdlBox;
    /**
     * The table combo selection
     */
    private JComboBox tablesCombo;

    /**
     * The constructor
     */

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, int triggerType) {
        this(dc, parent, null, triggerType);
    }

    String table;

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, int triggerType, String table) {
        this(dc, parent, null, triggerType);
        this.table = table;
        tablesCombo.setSelectedItem(table);
    }

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, DefaultDatabaseTrigger trigger,
                              int triggerType) {
        super(dc, parent, trigger, new Object[]{triggerType});
    }

    protected void init() {
        initExternal();
        if (getDatabaseVersion() > 2) {
            if (this.triggerType == NamedObject.TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER});
            else if (this.triggerType == NamedObject.DATABASE_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DB_TRIGGER});
            else if (this.triggerType == NamedObject.DDL_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DDL_TRIGGER});
            else
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER, DB_TRIGGER, DDL_TRIGGER});
        } else {
            if (this.triggerType == NamedObject.TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER});
            else if (this.triggerType == NamedObject.DATABASE_TRIGGER)
                typeTriggerCombo = new JComboBox(new String[]{DB_TRIGGER});
            else
                typeTriggerCombo = new JComboBox(new String[]{TRIGGER, DB_TRIGGER});
        }
        positionLabel = new JLabel(bundleString("position"));
        SpinnerModel model = new SpinnerNumberModel(0, 0, Short.MAX_VALUE, 1);
        positionField = new JSpinner(model);
        positionField.setValue(0);
        activeBox = new JCheckBox(bundleStaticString("active"));
        activeBox.setSelected(true);
        databaseTriggerComponents = new ArrayList<>();
        actionCombo = new JComboBox(new String[]{"CONNECT", "DISCONNECT", "TRANSACTION START", "TRANSACTION COMMIT", "TRANSACTION ROLLBACK"});
        actionLabel = new JLabel(bundleString("event"));
        tableTriggerComponents = new ArrayList<>();
        typeTableTriggerCombo = new JComboBox(new String[]{"BEFORE", "AFTER"});
        insertBox = new JCheckBox("INSERT");
        updateBox = new JCheckBox("UPDATE");
        deleteBox = new JCheckBox("DELETE");
        labelTable = new JLabel(bundleStaticString("table"));
        beforeAfterLabel = new JLabel(bundleString("before-after"));
        tablesCombo = new JComboBox(getTables());
        sqlBodyText = new SimpleSqlTextPanel();
        ddlTriggerPanel = new JPanel(new GridBagLayout());
        scrollDDL = new JScrollPane(ddlTriggerPanel);
        scrollDDL.setMinimumSize(new Dimension(100, 200));
        setPreferredSize(new Dimension(800, 800));
        ddlTableTriggerComponents = new ArrayList<>();
        ddlCheckBoxes = new ArrayList<>();
        anyDdlBox = new JCheckBox("ANY DDL STATEMENT");
        sqlBodyText.setSQLText("AS\n" +
                "BEGIN\n" +
                "  /* Trigger text */\n" +
                "END");


        typeTriggerCombo.addActionListener(actionEvent -> changeTypeTrigger());

        anyDdlBox.addActionListener(actionEvent -> changeAnyDdlBox());

        centralPanel.setVisible(false);
        topPanel.add(activeBox, topGbh.nextRowFirstCol().setLabelDefault().get());
        topPanel.add(typeTriggerCombo, topGbh.nextCol().fillHorizontally().setMaxWeightX().get());
        topGbh.addLabelFieldPair(topPanel, positionLabel, positionField, null, false, true);

        tabbedPane.add(bundleStaticString("SQL"), sqlBodyText);
        addCommentTab(null);

        GridBagHelper gbh = new GridBagHelper();
        ddlTriggerPanel.setLayout(new GridBagLayout());
        databaseTriggerComponents.add(actionLabel);
        databaseTriggerComponents.add(actionCombo);
        topGbh.addLabelFieldPair(topPanel, actionLabel, actionCombo, null, true, false);
        tableTriggerComponents.add(labelTable);
        tableTriggerComponents.add(tablesCombo);
        topGbh.addLabelFieldPair(topPanel, labelTable, tablesCombo, null, true, false);
        ddlTableTriggerComponents.add(beforeAfterLabel);
        ddlTableTriggerComponents.add(typeTableTriggerCombo);
        topGbh.addLabelFieldPair(topPanel, beforeAfterLabel, typeTableTriggerCombo, null, triggerType == NamedObject.DDL_TRIGGER, false);

        tableTriggerComponents.add(insertBox);
        tableTriggerComponents.add(updateBox);
        tableTriggerComponents.add(deleteBox);
        topPanel.add(insertBox, topGbh.setLabelDefault().nextCol().get());
        topPanel.add(updateBox, topGbh.setLabelDefault().nextCol().get());
        topPanel.add(deleteBox, topGbh.setLabelDefault().nextCol().get());

        ddlTableTriggerComponents.addAll(tableTriggerComponents);
        ddlTableTriggerComponents.add(scrollDDL);
        topPanel.add(scrollDDL, topGbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        gbh.defaults();
        ddlTriggerPanel.add(anyDdlBox, gbh.setLabelDefault().get());
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        ddlTriggerPanel.add(separator, gbh.nextRowFirstCol().fillHorizontally().spanX().get());
        gbh.setWidth(1);
        int[] addingY = {2, 2, 2};
        for (int i = 0; i < meta_types.length; i++) {
            for (int g = 0; g < 3; g++) {
                String operator = "";
                switch (g) {
                    case 0:
                        operator = "CREATE";
                        break;
                    case 1:
                        operator = "ALTER";
                        break;
                    case 2:
                        operator = "DROP";
                        break;
                }
                JCheckBox checkBox = new JCheckBox(operator + " " + meta_types[i]);
                gbh.setX(g);
                gbh.setY(i + addingY[g]);
                if (!checkBox.getText().toUpperCase().contains("ALTER COLLATION")
                        && !checkBox.getText().contains("CREATE CHARACTER SET")
                        && !checkBox.getText().contains("DROP CHARACTER SET")
                        && !checkBox.getText().contains("ALTER PACKAGE BODY")
                ) {
                    ddlTriggerPanel.add(checkBox, gbh.get());
                    ddlCheckBoxes.add(checkBox);
                } else {
                    addingY[g]--;
                }
            }
        }

        changeTypeTrigger();
        checkExternal();

    }

    private void changeAnyDdlBox() {
        boolean selected = anyDdlBox.isSelected();
        for (JCheckBox checkBox : ddlCheckBoxes) {
            checkBox.setSelected(selected);
            checkBox.setEnabled(!selected);
        }
    }

    protected void initEdited() {
        reset();
        addDependenciesTab(trigger);
        if (parent == null)
            addPrivilegesTab(tabbedPane);
        addCreateSqlTab(trigger);
    }

    protected void checkExternal() {
        boolean selected = useExternalBox.isSelected();
        sqlBodyText.setVisible(!selected);
        if (!selected) {
            if (tabbedPane.getComponentZOrder(sqlBodyText) < 0) {
                tabbedPane.insertTab(bundleStaticString("SQL"), null, sqlBodyText, null, 0);
                tabbedPane.setSelectedComponent(sqlBodyText);
            }
        } else {
            if (tabbedPane.getComponentZOrder(sqlBodyText) >= 0)
                tabbedPane.remove(sqlBodyText);
        }
        super.checkExternal();
    }

    protected void reset() {
        typeTriggerCombo.setEnabled(false);
        nameField.setText(trigger.getName());
        nameField.setEnabled(false);
        simpleCommentPanel.setDatabaseObject(trigger);
        activeBox.setSelected(trigger.isTriggerActive());
        positionField.setValue(trigger.getTriggerSequence());
        sqlBodyText.setSQLText(trigger.getTriggerSourceCode());
        if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.DATABASE_TRIGGER) {
            int type = ((int) trigger.getLongTriggerType()) - 8192;
            actionCombo.setSelectedIndex(type);
            //actionCombo.setEnabled(false);
        } else {
            if (trigger.getStringTriggerType().startsWith("BEFORE"))
                typeTableTriggerCombo.setSelectedIndex(0);
            else typeTableTriggerCombo.setSelectedIndex(1);
            //typeTableTriggerCombo
            if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.TABLE_TRIGGER) {
                insertBox.setSelected(trigger.getStringTriggerType().contains("INSERT"));
                updateBox.setSelected(trigger.getStringTriggerType().contains("UPDATE"));
                deleteBox.setSelected(trigger.getStringTriggerType().contains("DELETE"));
                for (int i = 0; i < tablesCombo.getItemCount(); i++) {
                    if (((String) tablesCombo.getModel().getElementAt(i)).trim().equalsIgnoreCase(trigger.getTriggerTableName().trim())) {
                        tablesCombo.setSelectedIndex(i);
                        //tablesCombo.setEnabled(false);
                        break;
                    }
                }
            } else {
                //typeTableTriggerCombo.setEnabled(false);
                //anyDdlBox.setEnabled(false);
                if (trigger.getStringTriggerType().trim().contains(anyDdlBox.getText())) {
                    anyDdlBox.setSelected(true);
                    changeAnyDdlBox();
                } else {
                    for (JCheckBox checkBox : ddlCheckBoxes) {
                        checkBox.setSelected(trigger.getStringTriggerType().contains(checkBox.getText()));
                        //checkBox.setEnabled(false);
                    }
                }
            }
        }
        if (!MiscUtils.isNull(trigger.getEntryPoint())) {
            useExternalBox.setSelected(true);
            engineField.setText(trigger.getEngine());
            externalField.setText(trigger.getEntryPoint());

        }
        if (!MiscUtils.isNull(trigger.getSqlSecurity())) {
            sqlSecurityCombo.setSelectedItem(trigger.getSqlSecurity());
        }
        //useExternalBox.setVisible(false);
    }

    @Override
    public void createObject() {
        generateScript();
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.TRIGGER];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        trigger = (DefaultDatabaseTrigger) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
        this.triggerType = (int) params[0];
    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    private void changeTypeTrigger() {
        visibleComponents(databaseTriggerComponents, typeTriggerCombo.getSelectedItem() == DB_TRIGGER);
        visibleComponents(ddlTableTriggerComponents, typeTriggerCombo.getSelectedItem() == DDL_TRIGGER || typeTriggerCombo.getSelectedItem() == TRIGGER);
        visibleComponents(tableTriggerComponents, typeTriggerCombo.getSelectedItem() == TRIGGER);
        scrollDDL.setVisible(typeTriggerCombo.getSelectedItem() == DDL_TRIGGER);
    }

    private void visibleComponents(List<Component> components, boolean flag) {
        for (Component component : components)
            component.setVisible(flag);
    }

    Object[] getTables() {
        List<String> tables = new Vector<>();
        DefaultDatabaseHost host = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection);
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.TABLE]));
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]));
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.VIEW]));
        return tables.toArray();
    }

    protected String generateQuery() {
        String selectedItem = (String) typeTriggerCombo.getSelectedItem();
        String table = null;
        if (Objects.equals(selectedItem, TRIGGER)) {
            table = (String) tablesCombo.getSelectedItem();
            if (table != null)
                table = table.trim();
        }
        StringBuilder triggerType = new StringBuilder();
        if (Objects.equals(selectedItem, TRIGGER)) {
            triggerType.append(typeTableTriggerCombo.getSelectedItem()).append(" ");
            boolean first = true;
            if (insertBox.isSelected()) {
                first = false;
                triggerType.append("INSERT ");
            }
            if (updateBox.isSelected()) {
                if (!first)
                    triggerType.append("OR ");
                first = false;
                triggerType.append("UPDATE ");
            }
            if (deleteBox.isSelected()) {
                if (!first)
                    triggerType.append("OR ");
                triggerType.append("DELETE ");
            }

        } else {
            if (Objects.equals(selectedItem, DB_TRIGGER))
                triggerType.append("ON ").append(actionCombo.getSelectedItem()).append(" ");
            else {
                triggerType.append(typeTableTriggerCombo.getSelectedItem()).append(" ");
                boolean first = true;
                if (anyDdlBox.isSelected()) {
                    triggerType.append(anyDdlBox.getText()).append(" ");
                } else {
                    for (JCheckBox checkBox : ddlCheckBoxes) {
                        if (checkBox.isSelected()) {
                            if (!first)
                                triggerType.append("OR ");
                            triggerType.append(checkBox.getText()).append(" ");
                            first = false;
                        }
                    }
                }
            }
        }
        String engine = engineField.getText();
        String external = externalField.getText();
        if (!useExternalBox.isSelected()) {
            engine = null;
            external = null;
        }

        return SQLUtils.generateCreateTriggerStatement(nameField.getText(), table, activeBox.isSelected(),
                triggerType.toString(), (int) positionField.getValue(), sqlBodyText.getSQLText(), engine, external,
                (String) sqlSecurityCombo.getSelectedItem(), simpleCommentPanel.getComment(), false);
    }

    private void generateScript() {

        displayExecuteQueryDialog(generateQuery(), "^");
    }

}
