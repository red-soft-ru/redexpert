package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseJob;
import org.executequery.gui.resultset.ResultSetTable;
import org.executequery.gui.resultset.ResultSetTableModel;
import org.executequery.localization.Bundles;
import org.executequery.sql.sqlbuilder.Condition;
import org.executequery.sql.sqlbuilder.Field;
import org.executequery.sql.sqlbuilder.SelectBuilder;
import org.executequery.sql.sqlbuilder.Table;
import org.underworldlabs.swing.EQDateTimePicker;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JobsLogPanel extends JPanel implements TabView {
    List<JCheckBox> typesEvents;
    //private JComboBox connectionCombo;
    //private JComboBox jobCombo;
    private ResultSetTable resultSetTable;
    private ResultSetTableModel tableModel;
    private EQDateTimePicker startDatePicker;
    private EQDateTimePicker endDatePicker;
    private JButton refreshButton;
    private DefaultStatementExecutor querySender;
    private final DatabaseConnection connection;
    private final DefaultDatabaseJob job;

    public JobsLogPanel(DefaultDatabaseJob job) {
        this.job = job;
        connection = job.getHost().getDatabaseConnection();
        init();
    }

    private void init() {
        querySender = new DefaultStatementExecutor(connection);
        try {
            tableModel = new ResultSetTableModel(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        resultSetTable = new ResultSetTable(tableModel);
        typesEvents = new ArrayList<>();
        typesEvents.add(new JCheckBox("RUN_START"));
        typesEvents.add(new JCheckBox("RUN_FINISH"));
        typesEvents.add(new JCheckBox("RUN_ERROR"));
        startDatePicker = new EQDateTimePicker();
        endDatePicker = new EQDateTimePicker();
        refreshButton = new JButton();
        refreshButton.setIcon(GUIUtilities.loadIcon("Refresh16.svg"));
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTable();
            }
        });

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();
        gbh.addLabelFieldPair(this, bundleString("startDate"), startDatePicker, null, true, false, typesEvents.size());
        gbh.addLabelFieldPair(this, bundleString("endDate"), endDatePicker, null, false, true);
        add(refreshButton, gbh.nextRowFirstCol().setLabelDefault().get());
        for (JCheckBox checkBox : typesEvents) {
            checkBox.setSelected(true);
            add(checkBox, gbh.nextCol().setLabelDefault().get());
        }
        add(new JScrollPane(resultSetTable), gbh.nextRowFirstCol().fillBoth().spanX().spanY().get());
        refreshTable();


    }

    private boolean allTypesSelected() {
        boolean result = true;
        for (JCheckBox checkBox : typesEvents)
            result = result && checkBox.isSelected();
        return result;
    }

    private void refreshTable() {
        SelectBuilder sb = new SelectBuilder(connection);
        Table jl = Table.createTable("RDB$JOBS_LOG", "JL");
        sb.appendTable(jl);
        sb.appendField(Field.createField(jl, "TIMESTAMP").setAlias("EVENT_TIMESTAMP"));
        sb.appendFields(jl, "EVENT", "MESSAGE");
        sb.appendCondition(Condition.createCondition(Field.createField(jl, "JOB_ID"), "=", job.getId()));
        if (!allTypesSelected()) {
            Condition c = Condition.createCondition().setLogicOperator("OR");
            for (JCheckBox checkBox : typesEvents) {
                if (checkBox.isSelected())
                    c.appendCondition(Condition.createCondition(Field.createField(jl, "EVENT"), "=", "'" + checkBox.getText() + "'"));
            }
            if (c.getConditions() != null)
                sb.appendCondition(c);
        }
        if (!startDatePicker.isNull())
            sb.appendCondition(Condition.createCondition(Field.createField(jl, "TIMESTAMP"), ">=", "'" + startDatePicker.getStringValue() + "'"));
        if (!endDatePicker.isNull())
            sb.appendCondition(Condition.createCondition(Field.createField(jl, "TIMESTAMP"), "<=", "'" + endDatePicker.getStringValue() + "'"));
        sb.setOrdering("1");
        String query = sb.getSQLQuery();
        try {
            ResultSet rs = querySender.getResultSet(query).getResultSet();
            tableModel.createTable(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            querySender.releaseResources();
        }
    }


    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    private String bundleString(String key,Object... args)
    {
        return Bundles.get(JobsLogPanel.class,key,args);
    }
}
