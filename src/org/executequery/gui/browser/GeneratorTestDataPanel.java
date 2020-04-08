package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.browser.generatortestdata.FieldGenerator;
import org.executequery.gui.browser.generatortestdata.FieldsPanel;
import org.executequery.gui.components.OpenConnectionsComboboxPanel;
import org.executequery.localization.Bundles;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GeneratorTestDataPanel extends JPanel implements TabView {

    public final static String TITLE = bundles("TITLE");

    private OpenConnectionsComboboxPanel comboboxPanel;

    private JComboBox tableBox;

    private DynamicComboBoxModel tableBoxModel;

    private DefaultStatementExecutor executor;

    private FieldsPanel fieldsPanel;

    private JButton startButton;

    private JButton stopButton;

    private boolean stop = false;

    private NumberTextField countRecordsField;

    private LoggingOutputPanel logPanel;

    private JProgressBar progressBar;

    private JCheckBox logBox;


    public GeneratorTestDataPanel() {
        init();
    }

    public static String bundles(String key) {
        return Bundles.get(GeneratorTestDataPanel.class, key);
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

    public DatabaseConnection getSelectedConnection() {
        return comboboxPanel.getSelectedConnection();
    }

    private NumberTextField commitAfterField;

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
            if (tables.size() == 0) {
                tables.add("");
                GUIUtilities.displayErrorMessage("there is no table in the database");
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

    private void fillCols() {
        if (tableBox.getSelectedItem() != "") {
            NamedObject object = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getHostNode(getSelectedConnection()).getDatabaseObject();
            DatabaseHost host = (DatabaseHost) object;
            List<DatabaseColumn> cols = host.getColumns(null, null, (String) tableBox.getSelectedItem());
            List<FieldGenerator> fieldGenerators = new ArrayList<>();
            for (int i = 0; i < cols.size(); i++) {
                fieldGenerators.add(new FieldGenerator(cols.get(i), executor));
            }
            if (fieldsPanel == null) {
                fieldsPanel = new FieldsPanel(fieldGenerators);
            } else fieldsPanel.setFieldGenerators(fieldGenerators);
        }
    }

    private void init() {
        executor = new DefaultStatementExecutor();
        progressBar = new JProgressBar();
        logPanel = new LoggingOutputPanel();
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
        tableBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    fillCols();
                }
            }
        });
        stopButton = new JButton(bundles("Stop"));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop = true;
            }
        });
        stopButton.setEnabled(false);

        startButton = new JButton(bundles("Start"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingWorker worker = new SwingWorker() {
                    @Override
                    public Object construct() {
                        stop = false;
                        int count = countRecordsField.getValue();
                        if (count <= 0)
                            GUIUtilities.displayErrorMessage("the number of records to be added must be greater than zero");
                        else {
                            boolean outlog = logBox.isSelected();
                            startButton.setEnabled(false);
                            stopButton.setEnabled(true);
                            long startTime = System.currentTimeMillis();
                            progressBar.setMinimum(0);
                            progressBar.setMaximum(count);
                            int commitAfter = commitAfterField.getValue();
                            int countSuccess = 0;
                            int countError = 0;
                            List<FieldGenerator> fieldGenerators = fieldsPanel.getFieldGenerators();
                            List<FieldGenerator> selectedFields = new ArrayList<>();
                            String sql = "INSERT INTO " + tableBox.getSelectedItem() + " (";
                            String values = "";
                            boolean first = true;
                            for (int g = 0; g < fieldGenerators.size(); g++) {
                                if (fieldGenerators.get(g).isSelectedField()) {
                                    selectedFields.add(fieldGenerators.get(g));
                                    if (!first) {
                                        sql += ",";
                                        values += ",";
                                    } else first = false;
                                    sql += " " + fieldGenerators.get(g).getColumn().getName();
                                    values += "? ";

                                }
                            }
                            sql += ") VALUES (" + values + ");";
                            logPanel.append("execute:\n");
                            logPanel.append(sql);
                            try {
                                executor.setCommitMode(false);
                                executor.setKeepAlive(true);
                                PreparedStatement statement = executor.getPreparedStatement(sql);
                                boolean lastError = false;
                                String lastMessage = "";
                                int i = 0;
                                for (; i < count; i++) {
                                    if (stop)
                                        break;
                                    if (i % commitAfter == 0 && i != 0) {
                                        executor.getConnection().commit();
                                    }
                                    progressBar.setValue(i);
                                    for (int g = 0; g < selectedFields.size(); g++) {
                                        Object param = selectedFields.get(g).getMethodGeneratorPanel().getTestDataObject();
                                        statement.setObject(g + 1, param);
                                    }
                                    SqlStatementResult result = executor.execute(QueryTypes.INSERT, statement);
                                    String message = sql;// + params;
                                    if (result.isException()) {
                                        if (outlog) {
                                            String errorMessage = result.getSqlException().getMessage();
                                            if (!lastError) {
                                                if (!errorMessage.contentEquals(lastMessage)) {
                                                    message += errorMessage;
                                                    lastMessage = message;
                                                    logPanel.appendError(message);
                                                }
                                                logPanel.appendError("failed from " + i);
                                                lastError = true;
                                            }
                                        }
                                        countError++;
                                    } else {
                                        countSuccess++;
                                        if (outlog && lastError) {
                                            logPanel.appendError("to " + (i - 1));
                                            lastError = false;
                                        }
                                    }
                                }
                                if (outlog && lastError) {
                                    logPanel.appendError("to " + (i - 1));
                                }
                                executor.getConnection().commit();
                                logPanel.append("Execution time: " + (System.currentTimeMillis() - startTime) + " ms");
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            } finally {
                                executor.releaseResources();
                            }
                            GUIUtilities.displayInformationMessage(countSuccess + " records added successfully\n" + countError + " queries failed");
                            logPanel.append(countSuccess + " records added successfully\n" + countError + " queries failed");
                            progressBar.setValue(0);
                        }
                        return null;
                    }

                    @Override
                    public void finished() {
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                    }
                };
                worker.start();

            }
        });
        countRecordsField = new NumberTextField();
        countRecordsField.setValue(100);

        commitAfterField = new NumberTextField();
        commitAfterField.setValue(100);

        logBox = new JCheckBox(bundles("OutputLog"));


        tableBoxModel.setElements(fillTables());
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);


        GridBagHelper gbh = new GridBagHelper();

        //ConnectionCombobox
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);

        topPanel.setLayout(new GridBagLayout());


        topPanel.add(comboboxPanel, gbh.defaults().spanX().fillHorizontally().setInsets(0, 0, 0, 0).get());

        JLabel label = new JLabel(bundles("Table"));
        topPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        topPanel.add(tableBox, gbh.defaults().nextCol().spanX().get());

        label = new JLabel(bundles("CountRecords"));
        topPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        topPanel.add(countRecordsField, gbh.defaults().nextCol().spanX().get());

        label = new JLabel(bundles("AfterCommit"));
        topPanel.add(label, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        topPanel.add(commitAfterField, gbh.defaults().nextCol().spanX().get());

        topPanel.add(logBox, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        topPanel.add(progressBar, gbh.defaults().nextRowFirstCol().spanX().get());

        topPanel.add(fieldsPanel, gbh.defaults().nextRowFirstCol().spanX().setMaxWeightY().fillBoth().get());

        topPanel.add(startButton, gbh.defaults().nextRowFirstCol().setLabelDefault().get());

        topPanel.add(stopButton, gbh.defaults().nextCol().setLabelDefault().get());

        topPanel.add(new JPanel(), gbh.defaults().nextCol().spanX().get());

        bottomPanel.setLayout(new GridBagLayout());

        gbh.setXY(0, 0);

        bottomPanel.add(new JScrollPane(logPanel), gbh.defaults().spanX().spanY().fillBoth().get());

        setLayout(new GridBagLayout());

        gbh.setXY(0, 0);

        add(splitPane, gbh.defaults().spanX().spanY().fillBoth().get());

        splitPane.setResizeWeight(0.7);


    }

}
