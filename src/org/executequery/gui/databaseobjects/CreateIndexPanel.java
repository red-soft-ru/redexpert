package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseMetaTag;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CreateIndexPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = "Create Index";
    public static final String ALTER_TITLE = "Alter Index";
    private JComboBox tableName;
    private JList<CheckListItem> fields;
    private JPanel fieldsPanel;
    private JPanel descriptionPanel;
    private SimpleTextArea description;
    private SimpleSqlTextPanel computedPanel;
    private JScrollPane scrollList;
    private JComboBox sortingBox;
    private JCheckBox uniqueBox;
    private JCheckBox computedBox;
    private JCheckBox activeBox;
    private DefaultDatabaseIndex databaseIndex;
    private String table_name;
    private boolean edited;
    private boolean free_sender = true;

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, (DefaultDatabaseIndex) null);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, String tableName) {
        this(dc, dialog, null, tableName);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseIndex index) {
        super(dc, dialog, index);
        edited = false;
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseIndex index, String tableName) {
        super(dc, dialog, index, new Object[]{tableName});
        edited = false;
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
                for (int g = 0; g < fields.getModel().getSize(); g++) {
                    CheckListItem item = ((DefaultListModel<CheckListItem>) fields.getModel()).elementAt(g);
                    if (column.getFieldName().trim().equals(item.label))
                        item.setSelected(true);
                }
            }
            fields.repaint();
        } else {
            computedBox.setSelected(true);
            computedPanel.setSQLText(databaseIndex.getExpression());
            tabbedPane.remove(0);
            tabbedPane.insertTab(bundlesString("computed"), null, computedPanel, null, 0);
            tabbedPane.setSelectedIndex(0);
        }
        uniqueBox.setSelected(databaseIndex.isUnique());
        activeBox.setSelected(databaseIndex.isActive());
        sortingBox.setSelectedIndex(databaseIndex.getIndexType());

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

    protected void init() {
        fieldsPanel = new JPanel();
        descriptionPanel = new JPanel();
        tableName = new JComboBox(new Vector());
        sortingBox = new JComboBox(new String[]{bundleString("ascending"), bundleString("descending")});
        uniqueBox = new JCheckBox(bundleString("unique"));
        computedBox = new JCheckBox(bundlesString("computed"));
        activeBox = new JCheckBox(bundlesString("active"));
        fields = new JList<>();
        fields.setModel(new DefaultListModel<>());
        scrollList = new JScrollPane(fields);
        this.description = new SimpleTextArea();
        computedPanel = new SimpleSqlTextPanel();

        computedBox.addActionListener(actionEvent -> {
            if (computedBox.isSelected()) {
                tabbedPane.remove(0);
                tabbedPane.insertTab(bundlesString("computed"), null, computedPanel, null, 0);
            } else {
                tabbedPane.remove(0);
                tabbedPane.insertTab(bundleString("fields"), null, fieldsPanel, null, 0);
            }
            tabbedPane.setSelectedIndex(0);
            edited = true;

        });

        fields.setCellRenderer(new CheckListRenderer());
        fields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fields.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();
                int index = list.locationToIndex(event.getPoint());// Get index of item
                // clicked
                CheckListItem item = (CheckListItem) list.getModel()
                        .getElementAt(index);
                item.setSelected(!item.isSelected());// Toggle selected state
                list.repaint(list.getCellBounds(index, index));// Repaint cell
                edited = true;
            }
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
            edited = true;
        });

        centralPanel.setLayout(new GridBagLayout());

        JLabel tableLabel = new JLabel(bundlesString("table"));
        centralPanel.add(tableLabel, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(tableName, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));
        JLabel sortLabel = new JLabel(bundleString("sorting"));
        centralPanel.add(sortLabel, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        centralPanel.add(sortingBox, new GridBagConstraints(1, 1,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

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

        centralPanel.add(checksPanel, new GridBagConstraints(0, 2,
                2, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        tabbedPane.add(bundleString("fields"), fieldsPanel);
        fieldsPanel.setLayout(new BorderLayout());
        fieldsPanel.add(scrollList);
        tabbedPane.add(bundlesString("description"), descriptionPanel);
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
                ((DefaultListModel<CheckListItem>) fields.getModel()).clear();
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
                        ((DefaultListModel<CheckListItem>) fields.getModel()).addElement(new CheckListItem(col.getColumnName()));
                    }
                }
            } catch (Exception e) {
                Log.error("Error getting fields in CreateIndexPanel");
            } finally {
                sender.releaseResources();
            }

    }

    private void createIndex() {
        String query = "";
        if (editing && !edited) {
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
                query += "COMPUTED BY (" + computedPanel.getSQLText() + ");";
            } else {
                query += "(";
                DefaultListModel model = ((DefaultListModel) fields.getModel());
                StringBuilder fieldss = new StringBuilder();
                boolean first = true;
                for (int i = 0; i < model.getSize(); i++) {
                    CheckListItem item = (CheckListItem) model.elementAt(i);
                    if (item.isSelected) {
                        if (!first)
                            fieldss.append(",");
                        first = false;
                        fieldss.append(MiscUtils.getFormattedObject(item.label));
                    }
                }
                query += fieldss + ");";
            }
            if (!activeBox.isSelected())
                query += "ALTER INDEX " + getFormattedName() + " INACTIVE;";
        }
        if (!MiscUtils.isNull(description.getTextAreaComponent().getText()))
            query += new StringBuilder().append("COMMENT ON INDEX ").append(getFormattedName()).append(" IS '").append(description.getTextAreaComponent().getText()).append("'").toString();
        displayExecuteQueryDialog(query, ";");

    }

    class CheckListItem {

        private String label;
        private boolean isSelected = false;

        public CheckListItem(String label) {
            this.label = label;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    class CheckListRenderer extends JCheckBox implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean hasFocus) {
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

}
