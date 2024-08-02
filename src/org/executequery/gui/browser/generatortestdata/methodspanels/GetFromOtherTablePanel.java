package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class GetFromOtherTablePanel extends AbstractMethodPanel {

    private JComboBox<?> tablesCombo;
    private JComboBox<?> columnsCombo;
    private NumberTextField recordsCountField;
    private JCheckBox useFirstNRecordsCheck;
    private DynamicComboBoxModel columnsModel;

    private final Random random;
    private final List<Object> objList;
    private final DefaultStatementExecutor executor;

    public GetFromOtherTablePanel(DatabaseColumn col, DefaultStatementExecutor executor) {
        super(col);
        this.random = new Random();
        this.objList = new ArrayList<>();
        this.executor = new DefaultStatementExecutor(executor.getDatabaseConnection());

        init();
    }

    private void init() {

        JLabel recordsCountLabel = new JLabel(bundleString("CountRecords"));
        recordsCountLabel.setEnabled(false);

        columnsModel = new DynamicComboBoxModel();
        columnsCombo = WidgetFactory.createComboBox("columnsCombo", columnsModel);
        tablesCombo = WidgetFactory.createComboBox("tablesCombo", getTablesVector());

        useFirstNRecordsCheck = WidgetFactory.createCheckBox("useFirstNRecordsCheck", bundleString("useFirstNRecordsCheck"));
        useFirstNRecordsCheck.addActionListener(e -> {
            boolean enabled = useFirstNRecordsCheck.isSelected();
            recordsCountLabel.setEnabled(enabled);
            recordsCountField.setEnabled(enabled);
        });

        recordsCountField = WidgetFactory.createNumberTextField("recordsCountField", "10");
        recordsCountField.setEnableNegativeNumbers(false);
        recordsCountField.setEnabled(false);

        // --- arrange ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        mainPanel.add(new JLabel(bundleString("TableView")), gbh.leftGap(3).topGap(3).get());
        mainPanel.add(tablesCombo, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        mainPanel.add(new JLabel(bundleString("Column")), gbh.nextRowFirstCol().leftGap(3).topGap(8).setMinWeightX().get());
        mainPanel.add(columnsCombo, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        mainPanel.add(useFirstNRecordsCheck, gbh.nextRowFirstCol().leftGap(0).spanX().get());
        mainPanel.add(recordsCountLabel, gbh.nextRowFirstCol().setWidth(1).leftGap(3).topGap(8).setMinWeightX().get());
        mainPanel.add(recordsCountField, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        mainPanel.add(new JPanel(), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());

        setLayout(new GridBagLayout());
        gbh = new GridBagHelper().setInsets(0, 5, 0, 0).fillBoth().spanX().spanY();
        add(mainPanel, gbh.get());

        // ---

        tablesCombo.addItemListener(this::tablesComboTriggered);
        columnsModel.setElements(getColumnsList());
    }

    private Vector<String> getTablesVector() {
        String query = "SELECT RDB$RELATION_NAME\n" +
                "FROM RDB$RELATIONS\n" +
                "WHERE (RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0)\n" +
                "AND RDB$RELATION_TYPE = 0 OR RDB$RELATION_TYPE = 1 OR RDB$RELATION_TYPE = 2\n" +
                "ORDER BY RDB$RELATION_NAME";

        Vector<String> tables = new Vector<>();
        try {
            SqlStatementResult result = executor.getResultSet(query);
            ResultSet rs = result.getResultSet();
            while (rs.next())
                tables.add(rs.getString(1).trim());

        } catch (SQLException | NullPointerException e) {
            Log.error(e.getMessage(), e);

        } finally {
            executor.releaseResources();
        }

        return tables;
    }

    private List<DatabaseColumn> getColumnsList() {

        JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabComponent instanceof ConnectionsTreePanel) {
            DatabaseObjectNode objectNode = ((ConnectionsTreePanel) tabComponent).getHostNode(executor.getDatabaseConnection());
            if (objectNode != null) {
                DatabaseHost host = (DatabaseHost) objectNode.getDatabaseObject();
                return host.getColumns((String) tablesCombo.getSelectedItem());
            }
        }

        return new ArrayList<>();
    }

    private String getQuery() {
        Object selectedColumn = columnsCombo.getSelectedItem();

        String tableName = (String) tablesCombo.getSelectedItem();
        String columnName = selectedColumn instanceof DatabaseColumn ?
                ((DatabaseColumn) selectedColumn).getName() :
                (String) columnsCombo.getModel().getElementAt(columnsCombo.getSelectedIndex());

        return useFirstNRecordsCheck.isSelected() ?
                String.format("SELECT FIRST %d %s FROM %s", recordsCountField.getValue(), columnName, tableName) :
                String.format("SELECT %s FROM %s", columnName, tableName);
    }

    private void tablesComboTriggered(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED)
            columnsModel.setElements(getColumnsList());
    }

    // --- AbstractMethodPanel impl ---

    @Override
    public Object getTestDataObject() {

        if (first) {
            try {
                ResultSet rs = executor.getResultSet(getQuery()).getResultSet();
                while (rs.next())
                    objList.add(rs.getObject(1));

            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
                return null;

            } finally {
                executor.releaseResources();
            }

            first = false;
        }

        return objList.get(random.nextInt(objList.size()));
    }

}
