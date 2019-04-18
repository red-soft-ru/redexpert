package org.executequery.gui.table;

import org.executequery.databaseobjects.DatabaseTable;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.TableColumnConstraint;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.databaseobjects.AbstractCreateObjectPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.CheckBoxPanel;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;


public class EditConstraintPanel extends AbstractCreateObjectPanel implements KeyListener {
    public static final String CREATE_TITLE = "Create Constraint";
    public static final String EDIT_TITLE = "Edit Constraint";
    boolean generate_name;
    private JScrollPane primaryPanel;
    private JPanel foreignPanel;
    private SimpleSqlTextPanel checkPanel;
    private JComponent typePanel;
    private ColumnConstraint constraint;
    private JLabel tableLabel;
    private JTextField tableNameField;
    private JLabel typeLabel;
    private JComboBox typeBox;
    private DatabaseTable table;
    private JTextField primaryIndexField;
    private JComboBox primarySortingBox;
    private JTextField foreignIndexField;
    private JComboBox foreignSortingBox;
    private CheckBoxPanel onFieldPrimaryPanel;
    private JComboBox referenceTable;
    private CheckBoxPanel referenceColumn;
    private CheckBoxPanel fieldConstraint;

    public EditConstraintPanel(DatabaseTable table, ActionContainer dialog) {
        super(table.getHost().getDatabaseConnection(), dialog, null, new Object[]{table});
    }

    public EditConstraintPanel(DatabaseTable table, ActionContainer dialog, ColumnConstraint columnConstraint) {
        super(table.getHost().getDatabaseConnection(), dialog, columnConstraint, new Object[]{table});
    }

    protected void init() {
        generate_name = true;
        tableLabel = new JLabel("Table:");
        tableNameField = new JTextField(table.getName());
        tableNameField.setEnabled(false);
        nameField.addKeyListener(this);
        typeLabel = new JLabel("Type:");
        primaryIndexField = new JTextField();
        foreignIndexField = new JTextField();
        String[] sorting = new String[]{Bundles.get("CreateIndexPanel.ascending"), Bundles.get("CreateIndexPanel.descending")};
        primarySortingBox = new JComboBox(sorting);
        foreignSortingBox = new JComboBox(sorting);
        typeBox = new JComboBox(new String[]{ColumnConstraint.PRIMARY, ColumnConstraint.FOREIGN, ColumnConstraint.UNIQUE, ColumnConstraint.CHECK});
        typeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    loadPanel();
                }
            }
        });
        /*
        <Primary Panel>
        */
        JPanel panel = new JPanel();
        primaryPanel = new JScrollPane(panel);
        panel.setLayout(new GridBagLayout());
        JLabel label = new JLabel("Index");
        panel.add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        panel.add(primaryIndexField, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        label = new JLabel("Sorting");
        panel.add(label, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        panel.add(primarySortingBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        onFieldPrimaryPanel = new CheckBoxPanel(table.getColumns().toArray(), 6, false);
        onFieldPrimaryPanel.setBorder(BorderFactory.createTitledBorder("On Field"));
        panel.add(onFieldPrimaryPanel, new GridBagConstraints(0, 2,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        panel.add(new JPanel(), new GridBagConstraints(0, 10,
                1, 1, 0, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        /*
        </Primary Panel>
        */

        /*
        <Foreign Panel>
        */

        foreignPanel = new JPanel();
        foreignPanel.setLayout(new GridBagLayout());
        label = new JLabel("Index");
        foreignPanel.add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        foreignPanel.add(foreignIndexField, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        label = new JLabel("Sorting");
        foreignPanel.add(label, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        foreignPanel.add(foreignSortingBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        foreignPanel.add(new JPanel(), new GridBagConstraints(1, 12,
                1, 1, 1, 1,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        fieldConstraint = new CheckBoxPanel(table.getColumns().toArray(), 6, false);
        String[] tables = metaData.getTables(null, null, "TABLE");
        referenceTable = new JComboBox(tables);
        referenceColumn = new CheckBoxPanel(metaData.getColumnNames((String) referenceTable.getSelectedItem(), null), 6, false);
        referenceTable.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    referenceColumn.setNamesBox(metaData.getColumnNames((String) referenceTable.getSelectedItem(), null));

                }
            }
        });
        fieldConstraint.setBorder(BorderFactory.createTitledBorder("On Field"));
        foreignPanel.add(fieldConstraint, new GridBagConstraints(0, 2,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        label = new JLabel("Reference Table");
        foreignPanel.add(label, new GridBagConstraints(0, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        foreignPanel.add(referenceTable, new GridBagConstraints(1, 3,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        referenceColumn.setBorder(BorderFactory.createTitledBorder("Reference Column"));
        foreignPanel.add(referenceColumn, new GridBagConstraints(0, 4,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        /*
        </Foreign Panel>
        */

        /*
        <Check Panel>
        */

        checkPanel = new SimpleSqlTextPanel();


        /*
        </Primary Panel>
        */

        centralPanel.setLayout(new GridBagLayout());
        centralPanel.add(tableLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(tableNameField, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(typeLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(typeBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        loadPanel();
    }

    private void loadPanel() {
        if (typePanel != null) {
            tabbedPane.remove(typePanel);
        }
        switch ((String) typeBox.getSelectedItem()) {
            case ColumnConstraint.PRIMARY:
                typePanel = primaryPanel;
                break;
            case ColumnConstraint.FOREIGN:
                typePanel = foreignPanel;
                break;
            case ColumnConstraint.UNIQUE:
                typePanel = primaryPanel;
                break;
            case ColumnConstraint.CHECK:
                typePanel = checkPanel;
                break;
            default:
                typePanel = new JPanel();
                break;
        }
        tabbedPane.add(typePanel, 0);
        tabbedPane.setTitleAt(0, "Constraint");
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
                String query = "select i.rdb$field_name,\n" +
                        "rc.rdb$index_name, idx.RDB$INDEX_TYPE\n" +
                        "from rdb$relation_constraints rc, rdb$index_segments i, rdb$indices idx\n" +
                        "where rc.RDB$CONSTRAINT_NAME = '" + nameField.getText() + "' AND\n" +
                        "(i.rdb$index_name = rc.rdb$index_name) AND\n" +
                        "(idx.rdb$index_name = rc.rdb$index_name) and\n" +
                        "(rc.RDB$RELATION_NAME = '" + table.getName() + "')\n" +
                        "order by rc.rdb$relation_name, i.rdb$field_position";
                ResultSet rs = sender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    onFieldPrimaryPanel.getCheckBoxMap().get(rs.getString("RDB$FIELD_NAME").trim()).setSelected(true);
                    primaryIndexField.setText(rs.getString("RDB$INDEX_NAME").trim());
                    primarySortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sender.releaseResources();
            }
        }
        if (typeBox.getSelectedItem() == TableColumnConstraint.FOREIGN) {
            referenceTable.setSelectedItem(constraint.getReferencedTable());
            try {
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
                        "I.RDB$INDEX_TYPE\n" +
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
                    fieldConstraint.getCheckBoxMap().get(fieldName).setSelected(true);
                    referenceColumn.getCheckBoxMap().get(refCol).setSelected(true);
                    foreignSortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));

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
            //check = check.replaceAll("  ","");
            checkPanel.setSQLText(check);
        }
        if (typeBox.getSelectedItem() == TableColumnConstraint.UNIQUE) {
            try {
                String query = "select i.rdb$field_name,\n" +
                        "rc.rdb$index_name, idx.RDB$INDEX_TYPE\n" +
                        "from rdb$relation_constraints rc, rdb$index_segments i, rdb$indices idx\n" +
                        "where rc.RDB$CONSTRAINT_NAME = '" + nameField.getText() + "' AND\n" +
                        "(i.rdb$index_name = rc.rdb$index_name) AND\n" +
                        "(idx.rdb$index_name = rc.rdb$index_name) and\n" +
                        "(rc.RDB$RELATION_NAME = '" + table.getName() + "')\n" +
                        "order by rc.rdb$relation_name, i.rdb$field_position";
                ResultSet rs = sender.getResultSet(query).getResultSet();
                while (rs.next()) {
                    onFieldPrimaryPanel.getCheckBoxMap().get(rs.getString("RDB$FIELD_NAME").trim()).setSelected(true);
                    primaryIndexField.setText(rs.getString("RDB$INDEX_NAME").trim());
                    primarySortingBox.setSelectedIndex(rs.getInt("RDB$INDEX_TYPE"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                sender.releaseResources();
            }
        }
    }

    @Override
    public void createObject() {
        StringBuilder sb = new StringBuilder();
        if (editing)
            sb.append("ALTER TABLE ").append(MiscUtils.wordInQuotes(table.getName().trim())).append(" DROP CONSTRAINT ").append(getNameInQuotes()).append("^");
        sb.append("ALTER TABLE ").append(MiscUtils.wordInQuotes(table.getName().trim())).append("\n");
        String name_constraint = getNameInQuotes();
        if (generate_name) {
            name_constraint = generateName();
        }
        sb.append("ADD CONSTRAINT " + name_constraint + " ");

        switch ((String) typeBox.getSelectedItem()) {
            case ColumnConstraint.PRIMARY:
                sb.append("PRIMARY KEY(");
                boolean first = true;
                for (String key : onFieldPrimaryPanel.getCheckBoxMap().keySet()) {
                    if (onFieldPrimaryPanel.getCheckBoxMap().get(key).isSelected()) {
                        if (!first)
                            sb.append(", ");
                        first = false;
                        sb.append(MiscUtils.wordInQuotes(key));
                    }
                }
                sb.append(")");
                if (!primaryIndexField.getText().isEmpty()) {
                    String sorting = "";
                    if (primarySortingBox.getSelectedIndex() == 0) {
                        sorting = "ASCENDING";
                    } else {
                        sorting = "DESCENDING";
                    }
                    sb.append("USING ").append(sorting).append(" INDEX ").append(primaryIndexField.getText());
                }
                break;
            case ColumnConstraint.FOREIGN:
                sb.append("FOREIGN KEY(");
                first = true;
                for (String key : fieldConstraint.getCheckBoxMap().keySet()) {
                    if (fieldConstraint.getCheckBoxMap().get(key).isSelected()) {
                        if (!first)
                            sb.append(", ");
                        first = false;
                        sb.append(MiscUtils.wordInQuotes(key));
                    }
                }
                sb.append(")\n");
                sb.append("REFERENCES ").append(MiscUtils.wordInQuotes((String) referenceTable.getSelectedItem())).append("(");
                first = true;
                for (String key : referenceColumn.getCheckBoxMap().keySet()) {
                    if (referenceColumn.getCheckBoxMap().get(key).isSelected()) {
                        if (!first)
                            sb.append(", ");
                        first = false;
                        sb.append(MiscUtils.wordInQuotes(key));
                    }
                }
                sb.append(")\n");
                if (!foreignIndexField.getText().isEmpty()) {
                    String sorting = "";
                    if (foreignSortingBox.getSelectedIndex() == 0) {
                        sorting = "ASCENDING";
                    } else {
                        sorting = "DESCENDING";
                    }
                    sb.append("USING ").append(sorting).append(" INDEX ").append(MiscUtils.wordInQuotes(foreignIndexField.getText()));
                }
                break;
            case ColumnConstraint.UNIQUE:
                sb.append("UNIQUE(");
                first = true;
                for (String key : onFieldPrimaryPanel.getCheckBoxMap().keySet()) {
                    if (onFieldPrimaryPanel.getCheckBoxMap().get(key).isSelected()) {
                        if (!first)
                            sb.append(", ");
                        first = false;
                        sb.append(MiscUtils.wordInQuotes(key));
                    }
                }
                sb.append(")");
                if (!primaryIndexField.getText().isEmpty()) {
                    String sorting = "";
                    if (primarySortingBox.getSelectedIndex() == 0) {
                        sorting = "ASCENDING";
                    } else {
                        sorting = "DESCENDING";
                    }
                    sb.append("USING ").append(sorting).append(" INDEX ").append(MiscUtils.wordInQuotes(primaryIndexField.getText()));
                }
                break;
            case ColumnConstraint.CHECK:
                sb.append("CHECK(");
                sb.append(checkPanel.getSQLText()).append(")");
                break;

        }
        displayExecuteQueryDialog(sb.toString(), "^");
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
        return "CONSTRAINT";
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

    private String generateName() {
        String name = "_" + table.getName().trim();
        switch ((String) typeBox.getSelectedItem()) {
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
                number = rs.getString("rdb$constraint_name").trim().replace(name, "").trim();
            }
            number = "" + (Integer.parseInt(number) + 1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            sender.releaseResources();
        }
        return name + number;
    }

}
