package org.executequery.gui.table;

import org.apache.commons.lang.math.NumberUtils;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractTableObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.databaseobjects.impl.TableColumnConstraint;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.databaseobjects.AbstractCreateObjectPanel;
import org.executequery.gui.databaseobjects.CreateIndexPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import static org.executequery.gui.browser.ColumnConstraint.RULES;

public class EditConstraintPanel extends AbstractCreateObjectPanel
        implements KeyListener {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.CONSTRAINT);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.CONSTRAINT);

    boolean generate_name;
    private JComboBox tablespaceBox;
    private JScrollPane primaryPanel;
    private JPanel foreignPanel;
    private SimpleSqlTextPanel checkPanel;
    private JComponent typePanel;
    private ColumnConstraint constraint;
    private JTextField tableNameField;
    private JComboBox typeBox;
    private DatabaseTable table;
    private JTextField primaryIndexField;
    private JComboBox primarySortingBox;
    private JTextField foreignIndexField;
    private JComboBox foreignSortingBox;
    private ListSelectionPanel onFieldPrimaryPanel;
    private JComboBox referenceTable;
    private JComboBox updateRuleBox;
    private JComboBox deleteRuleBox;
    private ListSelectionPanel referenceColumn;
    private ListSelectionPanel fieldConstraint;
    private List<NamedObject> tss;

    public EditConstraintPanel(DatabaseTable table, ActionContainer dialog) {
        super(table.getHost().getDatabaseConnection(), dialog, null, new Object[]{table});
    }

    public EditConstraintPanel(DatabaseTable table, ActionContainer dialog, ColumnConstraint columnConstraint) {
        super(table.getHost().getDatabaseConnection(), dialog, columnConstraint, new Object[]{table});
    }

    protected void init() {

        generate_name = true;
        tableNameField = new JTextField(table.getName());
        tableNameField.setEnabled(false);
        nameField.addKeyListener(this);
        primaryIndexField = new JTextField();
        foreignIndexField = new JTextField();

        String[] sorting = new String[]{
                Bundles.get("CreateIndexPanel.ascending"), Bundles.get("CreateIndexPanel.descending")};
        primarySortingBox = new JComboBox(sorting);
        foreignSortingBox = new JComboBox(sorting);

        typeBox = new JComboBox(new String[]{
                ColumnConstraint.PRIMARY, ColumnConstraint.FOREIGN, ColumnConstraint.UNIQUE, ColumnConstraint.CHECK});

        typeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                loadPanel();
        });

        updateRuleBox = new JComboBox(RULES);
        deleteRuleBox = new JComboBox(RULES);
        tablespaceBox = new JComboBox();
        tablespaceBox.addItem(null);

        tss = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLESPACE]);

        if (tss != null)
            for (NamedObject namedObject : tss)
                tablespaceBox.addItem(namedObject);

        // ----- <Primary Panel> -----

        JPanel panel = new JPanel();
        primaryPanel = new JScrollPane(panel);
        panel.setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        gbh.defaults().setXY(0, 0);

        panel.add(new JLabel(bundleString("Index")), gbh.get());
        panel.add(primaryIndexField, gbh.nextCol().spanX().get());
        panel.add(new JLabel(bundleString("Sorting")), gbh.nextRowFirstCol().setLabelDefault().get());
        panel.add(primarySortingBox, gbh.nextCol().fillHorizontally().spanX().get());

        onFieldPrimaryPanel = new ListSelectionPanel();
        onFieldPrimaryPanel.createAvailableList(getColumnNamesFromColumns(table.getColumns()));
        onFieldPrimaryPanel.setBorder(BorderFactory.createTitledBorder(bundleString("OnField")));
        panel.add(new JScrollPane(onFieldPrimaryPanel), gbh.nextRowFirstCol().spanX().get());

        //TODO CHECK MOVE TO END
        panel.add(new JPanel(), new GridBagConstraints(0, 10, 1, 1, 0, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));

        // ----- <Foreign Panel> -----

        foreignPanel = new JPanel();
        foreignPanel.setLayout(new GridBagLayout());
        gbh.defaults().setXY(0, 0);

        foreignPanel.add(new JLabel(bundleString("Index")), gbh.setLabelDefault().get());
        foreignPanel.add(foreignIndexField, gbh.nextCol().fillHorizontally().spanX().get());
        foreignPanel.add(new JLabel(bundleString("Sorting")), gbh.nextRowFirstCol().setLabelDefault().get());
        foreignPanel.add(foreignSortingBox, gbh.nextCol().fillHorizontally().spanX().get());
        foreignPanel.add(new JLabel(bundleString("UpdateRule")), gbh.setLabelDefault().nextRowFirstCol().get());
        foreignPanel.add(updateRuleBox, gbh.nextCol().fillHorizontally().spanX().get());
        foreignPanel.add(new JLabel(bundleString("DeleteRule")), gbh.setLabelDefault().nextRowFirstCol().get());
        foreignPanel.add(deleteRuleBox, gbh.nextCol().fillHorizontally().spanX().get());

        //TODO CHECK MOVE TO END
//        foreignPanel.add(new JPanel(), new GridBagConstraints(1, 12,
//                1, 1, 1, 1,
//                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
//                0, 0));

        JTabbedPane tabbedPane = new JTabbedPane();

        fieldConstraint = new ListSelectionPanel();
        fieldConstraint.createAvailableList(getColumnNamesFromColumns(table.getColumns()));

        List<NamedObject> tables = ConnectionsTreePanel.getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLE]);
        DynamicComboBoxModel referenceTableModel = new DynamicComboBoxModel();
        referenceTableModel.setElements(tables);

        referenceTable = new JComboBox(referenceTableModel);
        referenceColumn = new ListSelectionPanel();
        referenceColumn.createAvailableList(getColumnNamesFromColumns(((AbstractTableObject) referenceTable.getSelectedItem()).getColumns()));

        referenceTable.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                referenceColumn.createAvailableList(getColumnNamesFromColumns(
                        ((AbstractTableObject) referenceTable.getSelectedItem()).getColumns()));
        });

        tabbedPane.addTab(bundleString("OnField"), fieldConstraint);
        tabbedPane.addTab(bundleString("ReferenceColumn"), referenceColumn);
        foreignPanel.add(new JLabel(bundleString("ReferenceTable")), gbh.setLabelDefault().nextRowFirstCol().get());
        foreignPanel.add(referenceTable, gbh.nextCol().fillBoth().spanX().get());
        foreignPanel.add(tabbedPane, gbh.nextRowFirstCol().spanX().spanY().get());

        // ----- <Check Panel> -----

        checkPanel = new SimpleSqlTextPanel();

        // ----- </> -----

        centralPanel.setLayout(new GridBagLayout());
        gbh.defaults().setXY(0, 0);
        gbh.addLabelFieldPair(centralPanel, new JLabel(bundleString("Table")), tableNameField, null);
        gbh.nextRowFirstCol();
        gbh.addLabelFieldPair(centralPanel, new JLabel(bundleString("Type")), typeBox, null);
        gbh.nextRowFirstCol();
        if (tss != null)
            gbh.addLabelFieldPair(centralPanel,
                    Bundles.get(CreateIndexPanel.class, "tablespace"), tablespaceBox, null);

        loadPanel();
    }

    private void loadPanel() {

        if (typePanel != null)
            tabbedPane.remove(typePanel);

        boolean tsEnable = !ColumnConstraint.CHECK.contentEquals(
                (String) Objects.requireNonNull(typeBox.getSelectedItem()))
                && tss != null;

        if (tsEnable != tablespaceBox.isEnabled())
            tablespaceBox.setSelectedIndex(0);
        tablespaceBox.setEnabled(tsEnable);

        switch ((String) typeBox.getSelectedItem()) {
            case ColumnConstraint.PRIMARY:
            case ColumnConstraint.UNIQUE:
                typePanel = primaryPanel;
                break;

            case ColumnConstraint.FOREIGN:
                typePanel = foreignPanel;
                break;

            case ColumnConstraint.CHECK:
                typePanel = checkPanel;
                break;

            default:
                typePanel = new JPanel();
                break;
        }

        tabbedPane.add(typePanel, 0);
        tabbedPane.setTitleAt(0, bundleString("Constraint"));
        if (generate_name)
            nameField.setText(generateName());

        updateUI();
    }

    @Override
    protected void initEdited() {

        generate_name = false;
        nameField.setText(constraint.getName().trim());
        nameField.setEnabled(false);
        typeBox.setSelectedItem(constraint.getTypeName());
        typeBox.setEnabled(false);

        if (typeBox.getSelectedItem() == TableColumnConstraint.PRIMARY) {
            try {

                String tablespace_query = "";
                if (tss != null)
                    tablespace_query = ", idx.rdb$tablespace_name";

                String query = "select i.rdb$field_name,\n" +
                        "rc.rdb$index_name, idx.RDB$INDEX_TYPE" + tablespace_query + "\n" +
                        "from rdb$relation_constraints rc, rdb$index_segments i, rdb$indices idx\n" +
                        "where rc.RDB$CONSTRAINT_NAME = '" + nameField.getText() + "' AND\n" +
                        "(i.rdb$index_name = rc.rdb$index_name) AND\n" +
                        "(idx.rdb$index_name = rc.rdb$index_name) and\n" +
                        "(rc.RDB$RELATION_NAME = '" + table.getName() + "')\n" +
                        "order by rc.rdb$relation_name, i.rdb$field_position";

                ResultSet rs = sender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    onFieldPrimaryPanel.selectOneStringAction(rs.getString("RDB$FIELD_NAME").trim());
                    primaryIndexField.setText(rs.getString("RDB$INDEX_NAME").trim());
                    primarySortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));
                    if (tss != null) {
                        String tablespace = rs.getString("RDB$TABLESPACE_NAME");
                        if (tablespace != null)
                            for (NamedObject ts : tss)
                                if (ts.getName().equalsIgnoreCase(tablespace.trim()))
                                    tablespaceBox.setSelectedItem(ts);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sender.releaseResources();
            }
        }

        if (typeBox.getSelectedItem() == TableColumnConstraint.FOREIGN) {

            NamedObject foreignTable = null;
            for (int typeTable = NamedObject.TABLE; typeTable <= NamedObject.VIEW && foreignTable == null; typeTable++)
                foreignTable = ConnectionsTreePanel.getNamedObjectFromHost(table.getHost().getDatabaseConnection(), typeTable, constraint.getReferencedTable().trim());
            if (foreignTable == null)
                foreignTable = ConnectionsTreePanel.getNamedObjectFromHost(table.getHost().getDatabaseConnection(), NamedObject.SYSTEM_TABLE, constraint.getReferencedTable().trim());

            referenceTable.setSelectedItem(foreignTable);

            try {
                String tablespace_query = "";
                if (tss != null)
                    tablespace_query = ", I.RDB$TABLESPACE_NAME";

                String query = "select A.RDB$RELATION_NAME,\n" +
                        "A.RDB$CONSTRAINT_NAME,\n" +
                        "A.RDB$CONSTRAINT_TYPE,\n" +
                        "B.RDB$CONST_NAME_UQ,\n" +
                        "B.RDB$UPDATE_RULE,\n" +
                        "B.RDB$DELETE_RULE,\n" +
                        "C.RDB$RELATION_NAME as FK_Table,\n" +
                        "A.RDB$INDEX_NAME,\n" +
                        "D.RDB$FIELD_NAME as FK_Field,\n" +
                        "E.RDB$FIELD_NAME as OnField,\n" +
                        "I.RDB$INDEX_TYPE" + tablespace_query + "\n" +
                        "from RDB$REF_CONSTRAINTS B, RDB$RELATION_CONSTRAINTS A, RDB$RELATION_CONSTRAINTS C,\n" +
                        "RDB$INDEX_SEGMENTS D, RDB$INDEX_SEGMENTS E, RDB$INDICES I\n" +
                        "where (A.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY') and (A.RDB$CONSTRAINT_NAME = '" + constraint.getName() + "') and\n" +
                        "(A.RDB$CONSTRAINT_NAME = B.RDB$CONSTRAINT_NAME) and\n" +
                        "(B.RDB$CONST_NAME_UQ=C.RDB$CONSTRAINT_NAME) and (C.RDB$INDEX_NAME=D.RDB$INDEX_NAME) and\n" +
                        "(A.RDB$INDEX_NAME=E.RDB$INDEX_NAME) and\n" +
                        "(A.RDB$INDEX_NAME=I.RDB$INDEX_NAME)\n" +
                        "and (A.RDB$RELATION_NAME = '" + table.getName() + "')\n" +
                        "order by A.RDB$RELATION_NAME, A.RDB$CONSTRAINT_NAME, D.RDB$FIELD_POSITION, E.RDB$FIELD_POSITION";

                ResultSet rs = sender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    foreignIndexField.setText(rs.getString("RDB$INDEX_NAME").trim());
                    String fieldName = rs.getString("OnField").trim();
                    String refCol = rs.getString("FK_Field").trim();
                    fieldConstraint.selectOneStringAction(fieldName);
                    referenceColumn.selectOneStringAction(refCol);
                    foreignSortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));
                    updateRuleBox.setSelectedItem(constraint.getUpdateRule());
                    deleteRuleBox.setSelectedItem(constraint.getDeleteRule());
                    if (tss != null) {
                        String tablespace = rs.getString("RDB$TABLESPACE_NAME");
                        if (tablespace != null)
                            for (NamedObject ts : tss)
                                if (ts.getName().equalsIgnoreCase(tablespace.trim()))
                                    tablespaceBox.setSelectedItem(ts);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sender.releaseResources();
            }
        }

        if (typeBox.getSelectedItem() == TableColumnConstraint.CHECK) {
            String check = constraint.getCheck().toUpperCase().trim().replace("CHECK(", "");
            check = check.replace("CHECK (", "");
            check = check.substring(0, check.length() - 1);
            checkPanel.setSQLText(check);
        }

        if (typeBox.getSelectedItem() == TableColumnConstraint.UNIQUE) {
            try {

                String tablespace_query = "";
                if (tss != null)
                    tablespace_query = ", idx.rdb$tablespace_name";

                String query = "select i.rdb$field_name,\n" +
                        "rc.rdb$index_name, idx.RDB$INDEX_TYPE" + tablespace_query + "\n" +
                        "from rdb$relation_constraints rc, rdb$index_segments i, rdb$indices idx\n" +
                        "where rc.RDB$CONSTRAINT_NAME = '" + nameField.getText() + "' AND\n" +
                        "(i.rdb$index_name = rc.rdb$index_name) AND\n" +
                        "(idx.rdb$index_name = rc.rdb$index_name) and\n" +
                        "(rc.RDB$RELATION_NAME = '" + table.getName() + "')\n" +
                        "order by rc.rdb$relation_name, i.rdb$field_position";

                ResultSet rs = sender.getResultSet(query).getResultSet();
                while (rs.next()) {

                    for (int i = 0; i < onFieldPrimaryPanel.getAvailableValues().size(); i++) {
                        if (onFieldPrimaryPanel.getAvailableValues().get(i).toString().trim().contentEquals(rs.getString("RDB$FIELD_NAME").trim())) {
                            onFieldPrimaryPanel.selectOneAction(i);
                            break;
                        }
                    }

                    primaryIndexField.setText(rs.getString("RDB$INDEX_NAME").trim());
                    primarySortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));
                    if (tss != null) {
                        String tablespace = rs.getString("RDB$TABLESPACE_NAME");
                        if (tablespace != null)
                            for (NamedObject ts : tss)
                                if (ts.getName().equalsIgnoreCase(tablespace.trim()))
                                    tablespaceBox.setSelectedItem(ts);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sender.releaseResources();
            }
        }

        SimpleSqlTextPanel ddlPanel = new SimpleSqlTextPanel();
        ddlPanel.setSQLText(generateQuery().replaceAll("\n\tDROP CONSTRAINT .+,", ""));
        tabbedPane.add(ddlPanel, bundleStaticString("createSQL"));
    }

    @Override
    protected String generateQuery() {

        org.executequery.gui.browser.ColumnConstraint cc = new org.executequery.gui.browser.ColumnConstraint();
        cc.setName(nameField.getText());
        if (tablespaceBox.getSelectedItem() != null)
            cc.setTablespace(((DefaultDatabaseTablespace) tablespaceBox.getSelectedItem()).getName());

        switch ((String) Objects.requireNonNull(typeBox.getSelectedItem())) {

            case ColumnConstraint.PRIMARY:
            case ColumnConstraint.UNIQUE:
                if (typeBox.getSelectedItem() == ColumnConstraint.PRIMARY)
                    cc.setType(NamedObject.PRIMARY_KEY);
                else
                    cc.setType(NamedObject.UNIQUE_KEY);
                cc.setColumn(getColumnsFromVector(onFieldPrimaryPanel.getSelectedValues()));
                cc.setCountCols(onFieldPrimaryPanel.getSelectedValues().size());
                if (!primaryIndexField.getText().isEmpty()) {
                    String sorting = (primarySortingBox.getSelectedIndex() == 0) ? "ASCENDING" : "DESCENDING";
                    cc.setSorting(sorting);
                }
                break;

            case ColumnConstraint.FOREIGN:
                cc.setType(NamedObject.FOREIGN_KEY);
                cc.setColumn(getColumnsFromVector(fieldConstraint.getSelectedValues()));
                cc.setCountCols(fieldConstraint.getSelectedValues().size());
                String refTable = (referenceTable.getSelectedItem() instanceof AbstractTableObject) ?
                        ((AbstractTableObject) referenceTable.getSelectedItem()).getName() :
                        (String) referenceTable.getSelectedItem();
                cc.setRefTable(refTable);
                cc.setRefColumn(getColumnsFromVector(referenceColumn.getSelectedValues()));
                if (!foreignIndexField.getText().isEmpty()) {
                    String sorting = (primarySortingBox.getSelectedIndex() == 0) ? "ASCENDING" : "DESCENDING";
                    cc.setSorting(sorting);
                }
                cc.setUpdateRule((String) updateRuleBox.getSelectedItem());
                cc.setDeleteRule((String) deleteRuleBox.getSelectedItem());
                break;

            case ColumnConstraint.CHECK:
                cc.setType(NamedObject.CHECK_KEY);
                cc.setCheck(checkPanel.getSQLText());
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(MiscUtils.getFormattedObject(table.getName().trim(), getDatabaseConnection()));
        if (editing)
            sb.append("\n\tDROP CONSTRAINT ").append(getFormattedName()).append(",");
        sb.append("\n\tADD ");
        sb.append(SQLUtils.generateDefinitionColumnConstraint(cc, false, true, getDatabaseConnection(), true).trim().substring(1).trim()).append(";");

        return sb.toString();
    }

    private List<String> getColumnNamesFromColumns(List<DatabaseColumn> columns) {
        List<String> list = new ArrayList<>();
        for (DatabaseColumn col : columns)
            list.add(col.getName());
        return list;
    }

    @Override
    public void createObject() {
        displayExecuteQueryDialog(generateQuery(), "^");
    }

    private String getColumnsFromVector(Vector<String> columns) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String key : columns) {
            if (!first)
                sb.append(", ");
            first = false;
            sb.append(key);
        }

        return sb.toString();
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
        return NamedObject.META_TYPES[NamedObject.CONSTRAINT];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        constraint = (ColumnConstraint) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
        table = (DatabaseTable) params[0];
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        generate_name = false;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    @Override
    protected void reset() {
    }

    private String generateName() {

        String name = "_" + table.getName().trim();
        switch ((String) Objects.requireNonNull(typeBox.getSelectedItem())) {
            case ColumnConstraint.PRIMARY:
                name = "PK" + name;
                break;

            case ColumnConstraint.FOREIGN:
                name = "FK" + name;
                break;

            case ColumnConstraint.CHECK:
                name = "CHECK" + name;
                break;

            case ColumnConstraint.UNIQUE:
                name = "UQ" + name;
                break;
        }

        name = name + "_";
        String number = "0";
        try {
            String query = "Select rdb$constraint_name from rdb$relation_constraints where rdb$constraint_name STARTING WITH '" + name + "' order by 1";
            ResultSet rs = sender.getResultSet(query).getResultSet();
            while (rs.next()) {
                String tempNumber = rs.getString("rdb$constraint_name").trim().replace(name, "").trim();
                number = NumberUtils.isNumber(tempNumber) ? tempNumber : number;
            }
            number = String.valueOf(Integer.parseInt(number) + 1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }

        return name + number;
    }

}
