package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.log.Log;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CreateIndexPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.INDEX);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.INDEX);
    private JComboBox tableName;
    private ListSelectionPanel fieldsPanel;
    private JPanel descriptionPanel;
    private SimpleTextArea description;
    private SimpleSqlTextPanel computedPanel;
    private JComboBox sortingBox;
    private JComboBox tablespaceBox;
    private JCheckBox uniqueBox;
    private JCheckBox computedBox;
    private JCheckBox activeBox;
    private DefaultDatabaseIndex databaseIndex;
    private String table_name;
    private boolean changed = false;
    private boolean free_sender = true;

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, (DefaultDatabaseIndex) null);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, String tableName) {
        this(dc, dialog, null, tableName);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseIndex index) {
        super(dc, dialog, index);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseIndex index, String tableName) {
        super(dc, dialog, index, new Object[]{tableName});
    }

    protected void initEdited() {
        nameField.setText(databaseIndex.getName().trim());
        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseIndex.getHost(), null, null, NamedObject.META_TYPES[NamedObject.INDEX]);
        databaseIndex = metaTag.getIndexFromName(databaseIndex.getName());
        databaseIndex.loadColumns();
        nameField.setEnabled(false);
        description.getTextAreaComponent().setText(databaseIndex.getRemarks());
        for (int i = 0; i < tableName.getItemCount(); i++) {
            if (databaseIndex.getTableName().trim().equals(tableName.getItemAt(i))) {
                tableName.setSelectedIndex(i);
                updateListFields();
                break;
            }

        }
        if (databaseIndex.getExpression() == null) {
            for (int i = 0; i < databaseIndex.getIndexColumns().size(); i++) {
                DefaultDatabaseIndex.DatabaseIndexColumn column = databaseIndex.getIndexColumns().get(i);
                for (int g = 0; g < fieldsPanel.getAvailableValues().size(); g++) {
                    String item = (String) fieldsPanel.getAvailableValues().get(g);
                    if (column.getFieldName().trim().contentEquals(item)) {
                        fieldsPanel.selectOneAction(g);
                        g--;
                    }
                }
            }
        } else {
            computedBox.setSelected(true);
            computedPanel.setSQLText(databaseIndex.getExpression());
            tabbedPane.remove(0);
            tabbedPane.insertTab(bundleStaticString("computed"), null, computedPanel, null, 0);
            tabbedPane.setSelectedIndex(0);
        }
        uniqueBox.setSelected(databaseIndex.isUnique());
        activeBox.setSelected(databaseIndex.isActive());
        sortingBox.setSelectedIndex(databaseIndex.getIndexType());
        if (databaseIndex.getTablespace() != null)
            for (NamedObject ts : tss)
                if (ts.getName().equalsIgnoreCase(databaseIndex.getTablespace().trim()))
                    tablespaceBox.setSelectedItem(ts);
        changed = false;

    }

    @Override
    public void createObject() {
        createIndex();
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return ALTER_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.INDEX];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        databaseIndex = (DefaultDatabaseIndex) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
        if (params != null) {
            table_name = (String) params[0];
        }
    }

    private List<NamedObject> tss;

    protected void init() {
        fieldsPanel = new ListSelectionPanel();
        descriptionPanel = new JPanel();
        tableName = new JComboBox(new Vector());
        sortingBox = new JComboBox(new String[]{bundleString("ascending"), bundleString("descending")});
        tablespaceBox = new JComboBox();
        tablespaceBox.addItem(null);
        tss = ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLESPACE]);
        if (tss != null) {
            for (int i = 0; i < tss.size(); i++)
                tablespaceBox.addItem(tss.get(i));
        }
        tablespaceBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                changed = true;
            }
        });
        uniqueBox = new JCheckBox(bundleString("unique"));
        computedBox = new JCheckBox(bundleStaticString("computed"));
        activeBox = new JCheckBox(bundleStaticString("active"));
        activeBox.setSelected(true);
        this.description = new SimpleTextArea();
        computedPanel = new SimpleSqlTextPanel();

        computedBox.addActionListener(actionEvent -> {
            if (computedBox.isSelected()) {
                tabbedPane.remove(0);
                tabbedPane.insertTab(bundleStaticString("computed"), null, computedPanel, null, 0);
            } else {
                tabbedPane.remove(0);
                tabbedPane.insertTab(bundleString("fields"), null, fieldsPanel, null, 0);
            }
            tabbedPane.setSelectedIndex(0);
            changed = true;

        });
        tableName.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }

            updateListFields();

        });
        updateListTables();
        sortingBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED) {
                return;
            }
            changed = true;
        });

        centralPanel.setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        gbh.defaults();
        gbh.addLabelFieldPair(centralPanel, bundleStaticString("table"), tableName, null);
        gbh.addLabelFieldPair(centralPanel, bundleString("sorting"), sortingBox, null);
        if (tss != null)
            gbh.addLabelFieldPair(centralPanel, bundleString("tablespace"), tablespaceBox, null);


        JPanel checksPanel = new JPanel(new GridBagLayout());

        checksPanel.add(uniqueBox, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        checksPanel.add(computedBox, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        checksPanel.add(activeBox, new GridBagConstraints(2, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        centralPanel.add(checksPanel, gbh.nextRowFirstCol().spanX().anchorNorthWest().get());
        tabbedPane.add(bundleString("fields"), fieldsPanel);
        tabbedPane.add(bundleStaticString("description"), descriptionPanel);
        descriptionPanel.setLayout(new BorderLayout());
        descriptionPanel.add(description);
        if (table_name != null) {
            for (int i = 0; i < tableName.getItemCount(); i++) {
                if (table_name.trim().equals(tableName.getItemAt(i))) {
                    tableName.setSelectedIndex(i);
                    updateListFields();
                    break;
                }

            }
            tableName.setEnabled(false);
        }
        changed = false;
    }


    private void updateListTables() {
        try {
            String query = "select rdb$relation_name\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is null \n" +
                    "order by rdb$relation_name";
            free_sender = false;
            ResultSet rs = sender.getResultSet(query).getResultSet();
            tableName.removeAllItems();
            while (rs.next()) {
                tableName.addItem(rs.getString(1).trim());
            }
        } catch (Exception e) {
            Log.error("Error getting tables in CreateIndexPanel");
        } finally {
            free_sender = true;
            sender.releaseResources();
            updateListFields();
        }

    }

    private void updateListFields() {
        if (tableName.getSelectedItem() != null && free_sender)
            try {
                String query = "select  RRF.RDB$FIELD_NAME, RRF.RDB$FIELD_SOURCE,RRF.RDB$FIELD_POSITION from rdb$relation_fields RRF\n" +
                        "where\n" +
                        "    RRF.rdb$relation_name = '" + tableName.getSelectedItem() + "'\n order by 3";
                ResultSet rs = sender.getResultSet(query).getResultSet();
                fieldsPanel.clear();
                List<ColumnData> cols = new ArrayList<>();
                while (rs.next()) {
                    ColumnData col = new ColumnData(rs.getString(1).trim(), connection);
                    col.setDescription(rs.getString(2).trim());
                    cols.add(col);
                }
                sender.releaseResources();
                for (int i = 0; i < cols.size(); i++) {
                    ColumnData col = cols.get(i);
                    col.setDomain(col.getDescription());
                    if (!col.isLOB() && col.getSQLType() != Types.ARRAY && col.getDomainComputedBy() == null) {
                        fieldsPanel.addAvailableItem(col.getColumnName().trim());
                    }
                }
            } catch (Exception e) {
                Log.error("Error getting fields in CreateIndexPanel");
            } finally {
                sender.releaseResources();
            }

    }

    protected String generateQuery() {
        if (databaseIndex != null) {
            if (fieldsPanel.getSelectedValues().size() != databaseIndex.getIndexColumns().size())
                changed = true;
            else {
                for (int i = 0; i < databaseIndex.getIndexColumns().size(); i++) {
                    if (!databaseIndex.getIndexColumns().get(i).getFieldName().trim().contentEquals(fieldsPanel.getSelectedValues().get(i).toString()))
                        changed = true;
                }
            }
        }
        String query = "";
        if (editing && !changed) {
            if (activeBox.isSelected() != databaseIndex.isActive()) {
                String act;
                if (activeBox.isSelected())
                    act = "ACTIVE";
                else act = "INACTIVE";
                query = "ALTER INDEX " + getFormattedName() + " " + act + ";";
            }
        } else {
            if (editing)
                query = "DROP INDEX " + getFormattedName() + ";";
            query += "CREATE ";
            if (uniqueBox.isSelected())
                query += "UNIQUE ";
            if (sortingBox.getSelectedIndex() == 1)
                query += "DESCENDING ";
            query += "INDEX " + getFormattedName() +
                    " ON " + MiscUtils.getFormattedObject(((String) tableName.getSelectedItem()).trim()) + " ";
            if (computedBox.isSelected()) {
                query += "COMPUTED BY (" + computedPanel.getSQLText() + ")";
            } else {
                query += "(";
                StringBuilder fieldss = new StringBuilder();
                boolean first = true;
                for (int i = 0; i < fieldsPanel.getSelectedValues().size(); i++) {
                    if (!first)
                        fieldss.append(",");
                    first = false;
                    fieldss.append(MiscUtils.getFormattedObject((String) fieldsPanel.getSelectedValues().get(i)));
                }
                query += fieldss + ")";
            }
            if (tablespaceBox.getSelectedItem() != null)
                query += "\nTABLESPACE " + MiscUtils.getFormattedObject(((DefaultDatabaseTablespace) tablespaceBox.getSelectedItem()).getName());
            query += ";";
            if (!activeBox.isSelected())
                query += "ALTER INDEX " + getFormattedName() + " INACTIVE;";
        }
        if (!MiscUtils.isNull(description.getTextAreaComponent().getText()))
            query += "COMMENT ON INDEX " + getFormattedName() + " IS '" + description.getTextAreaComponent().getText() + "'";
        return query;
    }

    private void createIndex() {
        displayExecuteQueryDialog(generateQuery(), ";");

    }


}
