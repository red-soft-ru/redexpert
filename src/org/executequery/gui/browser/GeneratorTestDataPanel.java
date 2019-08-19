package org.executequery.gui.browser;

import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.gui.components.OpenConnectionsComboboxPanel;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class GeneratorTestDataPanel extends JPanel implements TabView {

    public final static String TITLE = "Generator Test Data";

    private OpenConnectionsComboboxPanel comboboxPanel;

    private JComboBox tableBox;

    private DynamicComboBoxModel tableBoxModel;

    private DefaultStatementExecutor executor;

    public GeneratorTestDataPanel() {
        init();
    }

    private void init() {
        executor = new DefaultStatementExecutor();
        comboboxPanel = new OpenConnectionsComboboxPanel();
        comboboxPanel.connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    executor.setDatabaseConnection(getSelectedConnection());
                    tableBoxModel.setElements(fillTables());
                }
            }
        });
        executor.setDatabaseConnection(getSelectedConnection());
        tableBoxModel = new DynamicComboBoxModel();
        tableBox = new JComboBox(tableBoxModel);
        tableBoxModel.setElements(fillTables());

        setLayout(new GridBagLayout());

        //ConnectionCombobox
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 2, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        add(comboboxPanel, gbc);

        //Label Table
        JLabel label = new JLabel("Table");
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.weightx = 0;
        add(label, gbc);

        //Table Combobox
        gbc.gridx++;
        gbc.weightx = 1;
        add(tableBox, gbc);

    }

    @Override
    public boolean tabViewClosing() {
        return false;
    }

    @Override
    public boolean tabViewSelected() {
        return false;
    }

    @Override
    public boolean tabViewDeselected() {
        return false;
    }

    public DatabaseConnection getSelectedConnection() {
        return comboboxPanel.getSelectedConnection();
    }

    private Vector<String> fillTables() {
        Vector<String> tables = new Vector<>();
        SqlStatementResult result = null;
        try {
            String query = "select rdb$relation_name\n" +
                    "from rdb$relations\n" +
                    "where rdb$view_blr is null \n" +
                    "and (rdb$system_flag is null or rdb$system_flag = 0) and rdb$relation_type=0 or rdb$relation_type=2\n" +
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
}
