package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class GetFromOtherTablePanel extends AbstractMethodPanel {
    NumberTextField countRowsField;
    DefaultStatementExecutor executor;
    private JComboBox tableBox;
    private JComboBox colBox;
    private DynamicComboBoxModel tableBoxModel;
    private DynamicComboBoxModel colBoxModel;
    Random random;
    private List<Object> objList;

    private void init() {
        setLayout(new GridBagLayout());
        tableBoxModel = new DynamicComboBoxModel();
        tableBoxModel.setElements(fillTables());
        colBoxModel = new DynamicComboBoxModel();
        tableBox = new JComboBox(tableBoxModel);
        tableBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    colBoxModel.setElements(fillCols());
                }
            }
        });
        colBox = new JComboBox(colBoxModel);
        countRowsField = new NumberTextField(false);
        countRowsField.setValue(1);
        GridBagHelper gbh = new GridBagHelper();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);

        JLabel label = new JLabel(bundles("TableView"));

        add(label, gbh.defaults().setLabelDefault().get());

        add(tableBox, gbh.defaults().nextCol().spanX().get());

        label = new JLabel(bundles("Column"));

        add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        add(colBox, gbh.defaults().nextCol().spanX().get());

        label = new JLabel(bundles("CountRecords"));

        add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        add(countRowsField, gbh.defaults().nextCol().spanX().spanY().get());

        colBoxModel.setElements(fillCols());

    }

    public GetFromOtherTablePanel(DatabaseColumn col, DefaultStatementExecutor executor) {
        super(col);
        this.executor = new DefaultStatementExecutor(executor.getDatabaseConnection());
        init();
    }

    @Override
    public Object getTestDataObject() {
        if (first) {
            first = false;
            objList = new ArrayList<>();
            String query = "Select first " + countRowsField.getStringValue() + " \n" + ((DatabaseColumn) colBox.getSelectedItem()).getName() + " from " + tableBox.getSelectedItem();
            try {
                ResultSet rs = executor.getResultSet(query).getResultSet();
                random = new Random();
                int count = countRowsField.getValue();
                for (int i = 0; i < count && rs.next(); i++) {
                    objList.add(rs.getObject(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                executor.releaseResources();
            }
        }
        int rand = random.nextInt(objList.size());
        return objList.get(rand);
    }

    private Vector<String> fillTables() {
        Vector<String> tables = new Vector<>();
        SqlStatementResult result = null;
        try {
            String query = "select rdb$relation_name\n" +
                    "from rdb$relations\n" +
                    "where \n" +
                    "(rdb$system_flag is null or rdb$system_flag = 0) and rdb$relation_type=0 or rdb$relation_type=1 or rdb$relation_type=2\n" +
                    "order by rdb$relation_name";
            result = executor.getResultSet(query);
            ResultSet rs = result.getResultSet();
            while (rs.next()) {
                tables.add(rs.getString(1).trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            executor.releaseResources();
        }
        return tables;
    }

    private List<DatabaseColumn> fillCols() {
        NamedObject object = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(executor.getDatabaseConnection()).getDatabaseObject();
        DatabaseHost host = (DatabaseHost) object;
        return host.getColumns(null, null, (String) tableBox.getSelectedItem());
    }
}
