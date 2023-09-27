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
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class CreateIndexPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.INDEX);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.INDEX);

    private JComboBox<String> tableName;
    private ListSelectionPanel fieldsPanel;
    private SimpleSqlTextPanel computedPanel;
    private JComboBox<String> sortingBox;
    private JComboBox<NamedObject> tablespaceBox;
    private JCheckBox uniqueBox;
    private JCheckBox computedBox;
    private JCheckBox activeBox;
    private DefaultDatabaseIndex databaseIndex;

    private List<NamedObject> tss;
    private String table_name;
    private boolean changed = false;
    private boolean commentChanged = false;
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

    @Override
    protected void init() {

        fieldsPanel = new ListSelectionPanel();
        tableName = new JComboBox<>(new Vector<>());
        sortingBox = new JComboBox<>(new String[]{bundleString("ascending"), bundleString("descending")});

        tablespaceBox = new JComboBox<>();
        tablespaceBox.addItem(null);

        tss = ConnectionsTreePanel.getPanelFromBrowser()
                .getDefaultDatabaseHostFromConnection(connection)
                .getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[NamedObject.TABLESPACE]);

        if (tss != null)
            for (NamedObject namedObject : tss)
                tablespaceBox.addItem(namedObject);

        uniqueBox = new JCheckBox(bundleString("unique"));
        computedBox = new JCheckBox(bundleStaticString("computed"));
        activeBox = new JCheckBox(bundleStaticString("active"));
        activeBox.setSelected(true);
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
            if (event.getStateChange() == ItemEvent.DESELECTED)
                return;
            updateListFields();
        });

        updateListTables();

        sortingBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.DESELECTED)
                return;
            changed = true;
        });

        centralPanel.setVisible(false);
        topGbh.addLabelFieldPair(topPanel, bundleStaticString("table"), tableName, null, true, false);
        topGbh.addLabelFieldPair(topPanel, bundleString("sorting"), sortingBox, null, false, true);
        if (tss != null)
            topGbh.addLabelFieldPair(topPanel, bundleString("tablespace"), tablespaceBox, null);
        topPanel.add(uniqueBox, topGbh.nextRowFirstCol().setLabelDefault().get());
        topPanel.add(computedBox, topGbh.nextCol().setLabelDefault().get());
        topPanel.add(activeBox, topGbh.nextCol().setLabelDefault().get());
        tabbedPane.add(bundleString("fields"), fieldsPanel);
        addCommentTab(null);

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

    @Override
    protected void initEdited() {

        nameField.setText(databaseIndex.getName().trim());
        DefaultDatabaseMetaTag metaTag = new DefaultDatabaseMetaTag(databaseIndex.getHost(), null, null, NamedObject.META_TYPES[NamedObject.INDEX]);
        databaseIndex = metaTag.getIndexFromName(databaseIndex.getName());
        nameField.setEditable(false);

        simpleCommentPanel.setDatabaseObject(databaseIndex);
        simpleCommentPanel.getCommentField().getTextAreaComponent().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                commentChanged = !databaseIndex.getRemarks().equals(simpleCommentPanel.getComment());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                commentChanged = !databaseIndex.getRemarks().equals(simpleCommentPanel.getComment());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                commentChanged = !databaseIndex.getRemarks().equals(simpleCommentPanel.getComment());
            }
        });

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

        if (!MiscUtils.isNull(databaseIndex.getTablespace()))
            for (NamedObject ts : tss)
                if (ts.getName().equalsIgnoreCase(databaseIndex.getTablespace().trim()))
                    tablespaceBox.setSelectedItem(ts);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        TableModel model = new DefaultDatabaseIndex.IndexColumnsModel(databaseIndex.getIndexColumns());
        JTable table = new JTable(model);
        GridBagHelper gbh = new GridBagHelper();

        gbh.setDefaultsStatic().defaults();
        fieldsPanel.add(new JScrollPane(table), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        tabbedPane.addTab(Bundles.get(DefaultDatabaseIndex.IndexColumnsModel.class, "StatisticSelectivity"), fieldsPanel);
        changed = false;
        addCreateSqlTab(databaseIndex);

    }

    @Override
    protected void reset() {
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
        if (params != null)
            table_name = (String) params[0];
    }

    private void updateListTables() {
        try {

            String query = "select rdb$relation_name" +
                    "\nfrom rdb$relations" +
                    "\nwhere rdb$view_blr is null" +
                    "\norder by rdb$relation_name";

            free_sender = false;
            ResultSet rs = sender.getResultSet(query).getResultSet();
            tableName.removeAllItems();
            while (rs.next())
                tableName.addItem(rs.getString(1).trim());

        } catch (Exception e) {
            Log.error("Error getting tables in CreateIndexPanel");

        } finally {
            free_sender = true;
            sender.releaseResources();
            updateListFields();
        }
    }

    private void updateListFields() {

        if (tableName.getSelectedItem() != null && free_sender) {
            try {

                String query = "select RRF.RDB$FIELD_NAME, RRF.RDB$FIELD_SOURCE,RRF.RDB$FIELD_POSITION" +
                        "\nfrom rdb$relation_fields RRF" +
                        "\nwhere RRF.rdb$relation_name = '" + tableName.getSelectedItem() + "'" +
                        "\n order by 3";

                ResultSet rs = sender.getResultSet(query).getResultSet();
                fieldsPanel.clear();
                List<ColumnData> cols = new ArrayList<>();
                while (rs.next()) {
                    ColumnData col = new ColumnData(rs.getString(1).trim(), connection);
                    col.setDescription(rs.getString(2).trim());
                    cols.add(col);
                }
                sender.releaseResources();

                for (ColumnData col : cols) {
                    col.setDomain(col.getDescription());
                    if (!col.isLOB() && col.getSQLType() != Types.ARRAY && col.getDomainComputedBy() == null)
                        fieldsPanel.addAvailableItem(col.getColumnName().trim());
                }

            } catch (Exception e) {
                Log.error("Error getting fields in CreateIndexPanel");

            } finally {
                sender.releaseResources();
            }
        }
    }

    @Override
    protected String generateQuery() {

        if (databaseIndex != null) {

            if (fieldsPanel.getSelectedValues().size() != databaseIndex.getIndexColumns().size()) {
                changed = true;

            } else {

                for (int i = 0; i < databaseIndex.getIndexColumns().size(); i++)
                    if (!databaseIndex.getIndexColumns().get(i).getFieldName().trim().contentEquals(fieldsPanel.getSelectedValues().get(i).toString()))
                        changed = true;
            }
        }

        String query = "";
        DefaultDatabaseTablespace tablespace = (DefaultDatabaseTablespace) tablespaceBox.getSelectedItem();

        if (editing && !changed) {

            Boolean isActive = (activeBox.isSelected() != databaseIndex.isActive()) ? activeBox.isSelected() : null;
            String comment = commentChanged ? simpleCommentPanel.getComment() : null;

            String stringTablespace = null;
            if (tablespace != null) {
                if (!Objects.equals(databaseIndex.getTablespace(), tablespace.getName()))
                    stringTablespace = tablespace.getName();

            } else if (Objects.equals(databaseIndex.getTablespace(), "PRIMARY") && !databaseIndex.getTablespace().isEmpty())
                stringTablespace = "PRIMARY";

            query = SQLUtils.generateAlterIndex(nameField.getText(), isActive, stringTablespace, comment);

        } else {

            if (editing)
                query = SQLUtils.generateDefaultDropQuery("INDEX", nameField.getText());

            query += SQLUtils.generateCreateIndex(nameField.getText(), sortingBox.getSelectedIndex(), uniqueBox.isSelected(),
                    tableName.getSelectedItem() != null ? tableName.getSelectedItem().toString() : "",
                    computedPanel.getSQLText(), null, fieldsPanel.getSelectedValues(),
                    tablespace != null ? tablespace.getName() : null, activeBox.isSelected(), simpleCommentPanel.getComment());
        }

        return query;
    }

    private void createIndex() {
        displayExecuteQueryDialog(generateQuery(), ";");
    }

}
