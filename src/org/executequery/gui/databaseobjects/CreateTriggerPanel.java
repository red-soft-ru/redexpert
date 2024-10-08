package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ExtractMethodRecommender")
public class CreateTriggerPanel extends AbstractCreateExternalObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.TRIGGER);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.TRIGGER);

    private static final String TABLE_TRIGGER = Bundles.get(CreateTriggerPanel.class, "table-trigger");
    private static final String DB_TRIGGER = Bundles.get(CreateTriggerPanel.class, "database-trigger");
    private static final String DDL_TRIGGER = Bundles.get(CreateTriggerPanel.class, "DDL-trigger");

    private static final String[] META_TYPES = {
            "FUNCTION", "INDEX", "PROCEDURE", "SEQUENCE", "TABLE", "TRIGGER", "VIEW", "DOMAIN", "EXCEPTION",
            "PACKAGE", "USER", "COLLATION", "CHARACTER SET", "MAPPING", "ROLE", "PACKAGE BODY"
    };

    // --- GUI components ---

    private JComboBox<?> tableCombo;
    private JComboBox<?> actionCombo;
    private JComboBox<?> beforeAfterCombo;
    private JComboBox<?> triggerTypeCombo;

    private JCheckBox activeCheck;
    private JCheckBox insertCheck;
    private JCheckBox deleteCheck;
    private JCheckBox updateCheck;

    private JCheckBox dropAllCheck;
    private JCheckBox alterAllCheck;
    private JCheckBox createAllCheck;
    private JCheckBox anyStatementCheck;
    private List<JCheckBox> ddlCheckBoxes;

    private JSpinner positionField;
    private JScrollPane ddlCheckScroll;
    private SimpleSqlTextPanel sqlTextPanel;

    // ---

    private int triggerType;
    private DefaultDatabaseTrigger trigger;

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, int triggerType) {
        this(dc, parent, null, triggerType);
    }

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, int triggerType, String table) {
        this(dc, parent, null, triggerType);
        tableCombo.setSelectedItem(table);
        tableCombo.setEnabled(false);
    }

    public CreateTriggerPanel(DatabaseConnection dc, ActionContainer parent, DefaultDatabaseTrigger trigger, int triggerType) {
        super(dc, parent, trigger, new Object[]{triggerType});
    }

    @Override
    protected void init() {
        initExternal();

        String[] tableTypes = new String[]{
                "BEFORE", "AFTER"
        };
        String[] actions = new String[]{
                "CONNECT", "DISCONNECT", "TRANSACTION START", "TRANSACTION COMMIT", "TRANSACTION ROLLBACK"
        };
        String[] ignoredStatements = new String[]{
                "ALTER COLLATION", "ALTER PACKAGE BODY", "CREATE CHARACTER SET", "DROP CHARACTER SET"
        };

        // --- combo boxes ---

        actionCombo = WidgetFactory.createComboBox("actionCombo", actions);

        beforeAfterCombo = WidgetFactory.createComboBox("beforeAfterCombo", tableTypes);
        beforeAfterCombo.setPreferredSize(new Dimension(beforeAfterCombo.getPreferredSize().width + 50, beforeAfterCombo.getPreferredSize().height));

        triggerTypeCombo = WidgetFactory.createComboBox("triggerTypeCombo", getTypeComboItems());
        triggerTypeCombo.addActionListener(e -> triggerTypeChanged());

        tableCombo = WidgetFactory.createComboBox("tableCombo", getTables());
        tableCombo.addItemListener(e -> sqlTextPanel.getTextPane().setTriggerTable((String) tableCombo.getSelectedItem()));

        // --- check boxes ---

        anyStatementCheck = WidgetFactory.createCheckBox("anyStatementCheck", "ANY DDL STATEMENT", this::ddlCheckChanged);
        createAllCheck = WidgetFactory.createCheckBox("createAllCheck", "CREATE ALL", this::ddlCheckChanged);
        alterAllCheck = WidgetFactory.createCheckBox("alterAllCheck", "ALTER ALL", this::ddlCheckChanged);
        dropAllCheck = WidgetFactory.createCheckBox("dropAllCheck", "DROP ALL", this::ddlCheckChanged);

        activeCheck = WidgetFactory.createCheckBox("activeCheck", bundleStaticString("active"), true);
        insertCheck = WidgetFactory.createCheckBox("insertCheck", "INSERT");
        updateCheck = WidgetFactory.createCheckBox("updateCheck", "UPDATE");
        deleteCheck = WidgetFactory.createCheckBox("deleteCheck", "DELETE");

        ddlCheckBoxes = new LinkedList<>();
        for (String metaType : META_TYPES) {
            for (String operator : new String[]{"CREATE", "ALTER", "DROP"}) {
                String text = (operator + " " + metaType).toUpperCase();
                if (Arrays.stream(ignoredStatements).noneMatch(o -> Objects.equals(o, text)))
                    ddlCheckBoxes.add(WidgetFactory.createCheckBox(text + " check", text));
            }
        }

        // --- sql text panel ---

        sqlTextPanel = new SimpleSqlTextPanel();
        sqlTextPanel.getTextPane().setDatabaseConnection(connection);
        sqlTextPanel.getTextPane().setTriggerTable((String) tableCombo.getSelectedItem());
        sqlTextPanel.setSQLText("AS BEGIN\n\t/* Trigger impl */\nEND");

        // --- others

        positionField = WidgetFactory.createSpinner("positionField", 0, 0, Short.MAX_VALUE, 1);

        tabbedPane.add(bundleStaticString("SQL"), sqlTextPanel);
        addCommentTab(null);

        if (getDatabaseVersion() < 3) {
            sqlSecurityLabel.setEnabled(false);
            securityCombo.setEnabled(false);
        }

        externalField.setMinimumSize(externalField.getPreferredSize());
        engineField.setMinimumSize(engineField.getPreferredSize());

        // ---

        arrange();
        triggerTypeChanged();
        checkExternal();
    }

    @Override
    protected void initEdited() {
        reset();

        addDependenciesTab(trigger);
        if (parent == null)
            addPrivilegesTab(tabbedPane, trigger);
        addCreateSqlTab(trigger);
    }

    private void arrange() {
        centralPanel.setVisible(false);

        // --- ddl check panel ---

        JPanel ddlCheckPanel = new JPanel(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().leftGap(5).anchorNorthWest().fillHorizontally();
        ddlCheckPanel.add(createAllCheck, gbh.get());
        ddlCheckPanel.add(alterAllCheck, gbh.nextCol().get());
        ddlCheckPanel.add(dropAllCheck, gbh.nextCol().spanX().get());
        ddlCheckPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbh.nextRowFirstCol().rightGap(5).get());

        gbh.rightGap(0);
        for (JCheckBox checkBox : ddlCheckBoxes) {

            String text = checkBox.getText();
            if (text.startsWith("CREATE"))
                gbh.nextRowFirstCol().setWidth(1);
            else if (text.startsWith("ALTER"))
                gbh.setX(1).setWidth(1);
            else
                gbh.setX(2).spanX();

            ddlCheckPanel.add(checkBox, gbh.get());
        }

        // --- skrolled ddl check panel ---

        ddlCheckScroll = new JScrollPane(ddlCheckPanel);
        ddlCheckScroll.setMinimumSize(new Dimension(100, 150));

        // --- base ---

        topGbh = new GridBagHelper().setInsets(5, 0, 5, 5).anchorNorthWest().fillBoth();

        topPanel.removeAll();
        topPanel.add(new JLabel(Bundles.getCommon("connection")), topGbh.setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(connectionsCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(Bundles.getCommon("name")), topGbh.nextCol().setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(nameField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());

        topPanel.add(activeCheck, topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().leftGap(0).rightGap(0).get());
        topPanel.add(triggerTypeCombo, topGbh.nextCol().setMaxWeightX().leftGap(5).rightGap(5).get());
        topPanel.add(new JLabel(bundleString("position")), topGbh.nextCol().setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(positionField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());

        topPanel.add(sqlSecurityLabel, topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().rightGap(0).get());
        topPanel.add(securityCombo, topGbh.nextCol().setMaxWeightX().rightGap(5).get());
        topPanel.add(useExternalCheck, topGbh.nextCol().leftGap(0).spanX().get());

        topPanel.add(new JLabel(bundleStaticString("EntryPoint_alternative")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().setInsets(5, 2, 0, 5).get());
        topPanel.add(externalField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(bundleStaticString("Engine")), topGbh.nextCol().setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(engineField, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).spanX().get());

        topPanel.add(new JLabel(bundleString("event")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(actionCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());
        topPanel.add(new JLabel(bundleStaticString("table")), topGbh.nextRowFirstCol().setWidth(1).setMinWeightX().topGap(1).rightGap(0).get());
        topPanel.add(tableCombo, topGbh.nextCol().setMaxWeightX().topGap(0).rightGap(5).get());

        topGbh.setMinWeightX();
        topPanel.add(beforeAfterCombo, triggerType == NamedObject.DDL_TRIGGER ? topGbh.nextRowFirstCol().get() : topGbh.nextCol().get());
        topPanel.add(anyStatementCheck, topGbh.nextCol().leftGap(0).get());
        topPanel.add(insertCheck, topGbh.nextCol().get());
        topPanel.add(updateCheck, topGbh.nextCol().get());
        topPanel.add(deleteCheck, topGbh.nextCol().get());

        topPanel.add(ddlCheckScroll, topGbh.nextRowFirstCol().leftGap(5).topGap(5).bottomGap(0).spanX().spanY().get());

        // ---

        if (parent != null) {
            ((BaseDialog) parent).setPreferredSize(new Dimension(700, 700));
            ((BaseDialog) parent).setResizable(false);
        }
    }

    private void triggerTypeChanged() {

        boolean isDDL = triggerTypeCombo.getSelectedItem() == DDL_TRIGGER;
        boolean isTable = triggerTypeCombo.getSelectedItem() == TABLE_TRIGGER;
        boolean isDatabase = triggerTypeCombo.getSelectedItem() == DB_TRIGGER;

        actionCombo.setVisible(isDatabase);
        topPanel.getComponent(topPanel.getComponentZOrder(actionCombo) - 1).setVisible(isDatabase);

        ddlCheckScroll.setVisible(isDDL);
        anyStatementCheck.setVisible(isDDL);
        beforeAfterCombo.setVisible(isTable || isDDL);

        tableCombo.setVisible(isTable);
        insertCheck.setVisible(isTable);
        updateCheck.setVisible(isTable);
        deleteCheck.setVisible(isTable);
        topPanel.getComponent(topPanel.getComponentZOrder(tableCombo) - 1).setVisible(isTable);
    }

    private void ddlCheckChanged(ActionEvent e) {
        ddlCheckChanged((JCheckBox) e.getSource());
    }

    private void ddlCheckChanged(JCheckBox source) {

        Stream<JCheckBox> checkBoxStream = ddlCheckBoxes.stream();
        if (Objects.equals(source, createAllCheck))
            checkBoxStream = checkBoxStream.filter(checkBox -> checkBox.getText().startsWith("CREATE"));
        else if (Objects.equals(source, alterAllCheck))
            checkBoxStream = checkBoxStream.filter(checkBox -> checkBox.getText().startsWith("ALTER"));
        else if (Objects.equals(source, dropAllCheck))
            checkBoxStream = checkBoxStream.filter(checkBox -> checkBox.getText().startsWith("DROP"));

        if (Objects.equals(source, anyStatementCheck)) {
            boolean enable = anyStatementCheck.isSelected();

            createAllCheck.setSelected(enable);
            alterAllCheck.setSelected(enable);
            dropAllCheck.setSelected(enable);
            createAllCheck.setEnabled(!enable);
            alterAllCheck.setEnabled(!enable);
            dropAllCheck.setEnabled(!enable);
        }

        checkBoxStream.forEach(checkBox -> {
            checkBox.setSelected(source.isSelected());
            checkBox.setEnabled(!source.isSelected());
        });
    }

    private Object[] getTables() {

        DefaultDatabaseHost host = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection);
        if (host == null)
            return new Object[0];

        List<String> tables = new ArrayList<>();
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.TABLE]));
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY]));
        tables.addAll(host.getDatabaseObjectNamesForMetaTag(NamedObject.META_TYPES[NamedObject.VIEW]));

        return tables.toArray();
    }

    private String[] getTypeComboItems() {

        String[] items;
        if (triggerType == NamedObject.TRIGGER)
            items = new String[]{TABLE_TRIGGER};
        else if (triggerType == NamedObject.DATABASE_TRIGGER)
            items = new String[]{DB_TRIGGER};
        else if (triggerType == NamedObject.DDL_TRIGGER && getDatabaseVersion() > 2)
            items = new String[]{DDL_TRIGGER};
        else if (getDatabaseVersion() > 2)
            items = new String[]{TABLE_TRIGGER, DB_TRIGGER, DDL_TRIGGER};
        else
            items = new String[]{TABLE_TRIGGER, DB_TRIGGER};

        return items;
    }

    @Override
    protected void checkExternal() {

        boolean selected = useExternalCheck.isSelected();
        sqlTextPanel.setVisible(!selected);

        if (!selected) {
            if (tabbedPane.getComponentZOrder(sqlTextPanel) < 0) {
                tabbedPane.insertTab(bundleStaticString("SQL"), null, sqlTextPanel, null, 0);
                tabbedPane.setSelectedComponent(sqlTextPanel);
            }

        } else if (tabbedPane.getComponentZOrder(sqlTextPanel) >= 0)
            tabbedPane.remove(sqlTextPanel);

        super.checkExternal();
    }

    @Override
    protected void reset() {
        if (!editing)
            return;

        nameField.setEditable(false);
        triggerTypeCombo.setEnabled(false);

        nameField.setText(trigger.getName());
        simpleCommentPanel.setDatabaseObject(trigger);
        activeCheck.setSelected(trigger.isTriggerActive());
        positionField.setValue(trigger.getTriggerSequence());
        sqlTextPanel.setSQLText(trigger.getTriggerSourceCode());

        if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.DATABASE_TRIGGER) {
            int type = ((int) trigger.getLongTriggerType()) - 8192;
            actionCombo.setSelectedIndex(type);

        } else {
            beforeAfterCombo.setSelectedIndex(trigger.getStringTriggerType().startsWith("BEFORE") ? 0 : 1);

            if (trigger.getIntTriggerType() == DefaultDatabaseTrigger.TABLE_TRIGGER) {
                insertCheck.setSelected(trigger.getStringTriggerType().contains("INSERT"));
                updateCheck.setSelected(trigger.getStringTriggerType().contains("UPDATE"));
                deleteCheck.setSelected(trigger.getStringTriggerType().contains("DELETE"));

                for (int i = 0; i < tableCombo.getItemCount(); i++) {
                    String tableName = ((String) tableCombo.getModel().getElementAt(i)).trim();
                    if (tableName.equalsIgnoreCase(trigger.getTriggerTableName().trim())) {
                        tableCombo.setSelectedIndex(i);
                        break;
                    }
                }

            } else if (trigger.getStringTriggerType().trim().contains(anyStatementCheck.getText())) {
                anyStatementCheck.setSelected(true);
                ddlCheckChanged(anyStatementCheck);

            } else {
                for (JCheckBox checkBox : ddlCheckBoxes)
                    checkBox.setSelected(trigger.getStringTriggerType().contains(checkBox.getText()));
            }
        }

        if (!MiscUtils.isNull(trigger.getEntryPoint())) {
            useExternalCheck.setSelected(true);
            engineField.setText(trigger.getEngine());
            externalField.setText(trigger.getEntryPoint());
        }

        if (!MiscUtils.isNull(trigger.getSqlSecurity()))
            securityCombo.setSelectedItem(trigger.getSqlSecurity());
    }

    @Override
    protected String generateQuery() {

        String table = null;
        String selectedItem = (String) triggerTypeCombo.getSelectedItem();

        if (Objects.equals(selectedItem, TABLE_TRIGGER)) {
            table = (String) tableCombo.getSelectedItem();
            if (table != null)
                table = table.trim();
        }

        StringBuilder triggerType = new StringBuilder();
        if (Objects.equals(selectedItem, TABLE_TRIGGER)) {
            triggerType.append(beforeAfterCombo.getSelectedItem()).append(" ");

            boolean first = true;
            if (insertCheck.isSelected()) {
                first = false;
                triggerType.append("INSERT ");
            }

            if (updateCheck.isSelected()) {
                if (!first)
                    triggerType.append("OR ");
                first = false;
                triggerType.append("UPDATE ");
            }

            if (deleteCheck.isSelected()) {
                if (!first)
                    triggerType.append("OR ");
                triggerType.append("DELETE ");
            }

        } else if (Objects.equals(selectedItem, DB_TRIGGER)) {
            triggerType.append("ON ").append(actionCombo.getSelectedItem()).append(" ");

        } else {
            triggerType.append(beforeAfterCombo.getSelectedItem()).append(" ");
            if (anyStatementCheck.isSelected()) {
                triggerType.append(anyStatementCheck.getText()).append(" ");

            } else {
                boolean first = true;
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

        return SQLUtils.generateCreateTriggerStatement(
                nameField.getText(),
                table,
                activeCheck.isSelected(),
                triggerType.toString(),
                (int) positionField.getValue(),
                sqlTextPanel.getSQLText(),
                useExternalCheck.isSelected() ? engineField.getText() : null,
                useExternalCheck.isSelected() ? externalField.getText() : null,
                (String) securityCombo.getSelectedItem(),
                simpleCommentPanel.getComment(),
                false,
                getDatabaseConnection()
        );
    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(generateQuery(), "^");
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

}
