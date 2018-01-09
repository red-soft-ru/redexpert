package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.List;

public class CreateIndexPanel extends JPanel {

    private ActionContainer parent;

    JComboBox tableName;

    JTextField nameText;

    JButton ok;

    JButton cancel;

    DatabaseConnection connection;

    private JComboBox connectionsCombo;

    private DynamicComboBoxModel connectionsModel;

    JList<CheckListItem> fields;

    JTabbedPane tabbedPane;

    JPanel fieldsPanel;

    JPanel descriptionPanel;

    SimpleTextArea description;

    DefaultStatementExecutor sender;

    JScrollPane scrollList;

    JComboBox sortingBox;

    JCheckBox uniqueBox;

    boolean editing;

    public static final String CREATE_TITLE = "Create Index";

    public static final String ALTER_TITLE = "Alter Index";

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateIndexPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseIndex index) {
        parent = dialog;
        connection = dc;
        init();
    }


    void init() {
        fieldsPanel = new JPanel();
        descriptionPanel = new JPanel();
        nameText = new JTextField();
        tableName = new JComboBox(new Vector());
        sortingBox = new JComboBox(new String[]{"DESCENDING", "ASCENDING"});
        uniqueBox = new JCheckBox("Unique");
        fields = new JList<>();
        fields.setModel(new DefaultListModel<>());
        scrollList = new JScrollPane(fields);
        this.description = new SimpleTextArea();
        tabbedPane = new JTabbedPane();

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
                item.setSelected(!item.isSelected()); // Toggle selected state
                list.repaint(list.getCellBounds(index, index));// Repaint cell
            }
        });

        ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createIndex();
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
                sender.setDatabaseConnection(connection);
            }
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        sender = new DefaultStatementExecutor(connection, true);
        tableName.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }

                updateListFields();

            }
        });
        updateListTables();

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0);
        JLabel connLabel = new JLabel("Connections");
        this.add(connLabel, gbc);
        gbc.gridx++;
        this.add(connectionsCombo, gbc);
        JLabel tableLabel = new JLabel("Table");
        gbc.gridx = 0;
        gbc.gridy++;
        this.add(tableLabel, gbc);
        gbc.gridx++;
        this.add(tableName, gbc);
        JLabel nameLabel = new JLabel("Name");
        gbc.gridx = 0;
        gbc.gridy++;
        this.add(nameLabel, gbc);
        gbc.gridx++;
        this.add(nameText, gbc);
        JLabel sortLabel = new JLabel("Sorting");
        gbc.gridx = 0;
        gbc.gridy++;
        this.add(sortLabel, gbc);
        gbc.gridx++;
        this.add(sortingBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        this.add(uniqueBox, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(tabbedPane, gbc);
        tabbedPane.add("Fields", fieldsPanel);
        fieldsPanel.setLayout(new BorderLayout());
        fieldsPanel.add(scrollList);
        tabbedPane.add("Description", descriptionPanel);
        descriptionPanel.setLayout(new BorderLayout());
        descriptionPanel.add(description);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(ok, gbc);
        gbc.gridx++;
        this.add(cancel, gbc);

    }

    boolean free_sender = true;

    void updateListTables() {
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

    void updateListFields() {
        if (tableName.getSelectedItem() != null && free_sender)
            try {
                String query = "select  RRF.RDB$FIELD_NAME from rdb$relation_fields RRF\n" +
                        "where\n" +
                        "    RRF.rdb$relation_name = '" + tableName.getSelectedItem() + "'\n order by 1";
                ResultSet rs = sender.getResultSet(query).getResultSet();
                ((DefaultListModel<CheckListItem>) fields.getModel()).clear();
                while (rs.next()) {
                    ((DefaultListModel<CheckListItem>) fields.getModel()).addElement(new CheckListItem(rs.getString(1).trim()));
                }
            } catch (Exception e) {
                Log.error("Error getting fields in CreateIndexPanel");
            } finally {
                sender.releaseResources();
            }

    }

    void createIndex() {
        String query = "CREATE ";
        if (uniqueBox.isSelected())
            query += "UNIQUE ";
        if (sortingBox.getSelectedIndex() == 0)
            query += "DESCENDING ";
        query += "INDEX " + nameText.getText() +
                " ON "+tableName.getSelectedItem()+" (";
        DefaultListModel model = ((DefaultListModel)fields.getModel());
        String fieldss="";
        boolean first=true;
        for(int i = 0;i<model.getSize();i++)
        {
            CheckListItem item = (CheckListItem) model.elementAt(i);
            if(item.isSelected)
            {
                if(!first)
                    fieldss+=",";
                first=false;
                fieldss+=item.label;
            }
        }
        query+=fieldss+");";
        if(!MiscUtils.isNull(description.getTextAreaComponent().getText()))
            query+="COMMENT ON INDEX "+nameText.getText()+" IS '"+description.getTextAreaComponent().getText()+"'";
        ExecuteQueryDialog eqd = new ExecuteQueryDialog(CREATE_TITLE,query,connection,true);
        eqd.display();
        if(eqd.getCommit())
            parent.finished();
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
