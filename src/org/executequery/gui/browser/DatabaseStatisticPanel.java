package org.executequery.gui.browser;

import biz.redsoft.IFBStatisticManager;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.BottomButtonPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractTableObject;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.gui.browser.managment.dbstatistic.CompareStatPanel;
import org.executequery.gui.browser.managment.dbstatistic.DbStatPanel;
import org.executequery.localization.Bundles;
import org.executequery.util.UserProperties;
import org.underworldlabs.statParser.*;
import org.underworldlabs.swing.*;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseStatisticPanel extends AbstractServiceManagerPanel implements TabView {
    public static final String TITLE = Bundles.get(DatabaseStatisticPanel.class, "title");
    private IFBStatisticManager statisticManager;
    protected RolloverButton fileStatButton;
    protected RolloverButton compareButton;
    protected JToolBar toolBar;

    private JButton getStatButton;

    protected JCheckBox defaultStatCheckBox;
    protected JCheckBox tableStatBox;
    protected JCheckBox indexStatBox;
    protected JCheckBox systemTableStatBox;
    protected JCheckBox recordVersionsStatBox;
    protected JCheckBox headerPageStatBox;
    protected JCheckBox onlySelectTablesBox;
    protected JTextField tablesField;
    protected IndeterminateProgressBar progressBar;
    ListSelectionPanel tablesStatPanel;
    ItemListener itemListener;
    ActionListener okListener;
    protected List<StatDatabase> statDatabaseList;

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

    @Override
    protected void initOtherComponents() {
        initStatManager(null);
        statDatabaseList = new ArrayList<>();
        toolBar = WidgetFactory.createToolBar("toolBar");
        toolBar.setFloatable(false);
        fileStatButton = WidgetFactory.createRolloverButton("fileStatButton", bundleString("OpenFileLog"), "Open16.png");
        toolBar.add(fileStatButton);
        compareButton = WidgetFactory.createRolloverButton("compareButton", bundleString("compare"), "ComparerDB_16.png");
        toolBar.add(compareButton);
        fileStatButton.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromFile(fileChooser, fileStatButton);
            }
        });
        compareButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BaseDialog dialog = new BaseDialog(bundleString("selectStatsDialogTitle"), true);
                JComboBox comboBox1 = WidgetFactory.createComboBox("combobox1");
                DynamicComboBoxModel model1 = new DynamicComboBoxModel();
                model1.setElements(statDatabaseList);
                comboBox1.setModel(model1);
                JComboBox comboBox2 = WidgetFactory.createComboBox("combobox2");
                DynamicComboBoxModel model2 = new DynamicComboBoxModel();
                model2.setElements(statDatabaseList);
                comboBox2.setModel(model2);
                ActionListener actionListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        GUIUtilities.closeSelectedDialog();
                        SwingWorker sw = new SwingWorker("compareStatistics") {
                            @Override
                            public Object construct() {
                                StatDatabase compareDB = new StatDatabase();
                                StatDatabase db1 = (StatDatabase) comboBox1.getSelectedItem();
                                StatDatabase db2 = (StatDatabase) comboBox2.getSelectedItem();
                                compareDB = compareDBS(db1, db2);
                                CompareStatPanel compareStatPanel = new CompareStatPanel(compareDB, db1, db2);
                                tabPane.addTab(null, null, compareStatPanel, db1 + " vs " + db2);
                                tabPane.setTabComponentAt(tabPane.indexOfComponent(compareStatPanel), new ClosableTabTitle(bundleString("compare"), null, compareStatPanel));
                                tabPane.setSelectedComponent(compareStatPanel);
                                return null;
                            }

                            @Override
                            public void finished() {
                                GUIUtilities.showNormalCursor();
                            }
                        };
                        sw.start();
                        GUIUtilities.showWaitCursor();
                    }
                };
                BottomButtonPanel bottomButtonPanel = new BottomButtonPanel(actionListener, "OK", null, true);
                bottomButtonPanel.setHelpButtonVisible(false);
                AbstractPanel panel = new AbstractPanel() {
                    @Override
                    protected void initComponents() {

                    }

                    @Override
                    protected void arrangeComponents() {
                        add(comboBox1, gbh.setInsets(10,10,10,0).fillHorizontally().spanX().get());
                        add(comboBox2, gbh.nextRowFirstCol().topGap(5).fillHorizontally().spanX().get());
                        add(bottomButtonPanel, gbh.nextRowFirstCol().bottomGap(10).rightGap(5).spanX().get());
                    }

                    @Override
                    protected void postInitActions() {

                    }
                };
                dialog.setContentPane(panel);
                dialog.setPreferredSize(new Dimension(300, 120));
                dialog.setResizable(false);
                dialog.display();

            }
        });
        progressBar = new IndeterminateProgressBar(true);
        toolBar.add(progressBar);
        getStatButton = WidgetFactory.createButton("getStatButton", bundleString("Start"));
        getStatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (logToFileBox.isSelected()) {
                    if (logToFileBox.isSelected()) {
                        if (!fileLogField.getText().isEmpty()) {
                            File file = new File(fileLogField.getText());
                            try {
                                fileLog = new FileOutputStream(file, true);
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if (fileLog != null) {
                        outputStream = new ServiceOutputStream();
                    } else {
                        GUIUtilities.displayErrorMessage("File is empty");
                        return;
                    }
                } else
                    outputStream = new PipedOutputStream();
                try {
                    inputStream = new PipedInputStream(outputStream);
                    String charset = null;
                    if (charsetCombo.getSelectedIndex() > 0)
                        charset = MiscUtils.getJavaCharsetFromSqlCharset((String) charsetCombo.getSelectedItem());
                    else charset = "UTF8";
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                statisticManager.setUser(userField.getText());
                statisticManager.setPassword(new String(passwordField.getPassword()));
                statisticManager.setLogger(outputStream);
                if (charsetCombo.getSelectedIndex() == 0)
                    statisticManager.setCharSet("UTF8");
                else
                    statisticManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset((String) charsetCombo.getSelectedItem()));
                statisticManager.setDatabase(fileDatabaseField.getText());
                statisticManager.setHost(hostField.getText());
                statisticManager.setPort(portField.getValue());
                try {
                    clearAll();
                    SwingWorker getStat = new SwingWorker("getDBStat") {
                        @Override
                        public Object construct() {
                            try {
                                progressBar.start();
                                if (defaultStatCheckBox.isSelected())
                                    statisticManager.getDatabaseStatistics();
                                else if (headerPageStatBox.isSelected())
                                    statisticManager.getHeaderPage();
                                else if (onlySelectTablesBox.isSelected()) {
                                    String[] tables = new String[tablesStatPanel.getSelectedValues().size()];
                                    for (int i = 0; i < tables.length; i++) {
                                        tables[i] = ((NamedObject) tablesStatPanel.getSelectedValues().get(i)).getName();
                                    }
                                    statisticManager.getTableStatistics(tables);
                                } else {
                                    int options = 0;
                                    if (tableStatBox.isSelected())
                                        options = options | IFBStatisticManager.DATA_TABLE_STATISTICS;
                                    if (indexStatBox.isSelected())
                                        options = options | IFBStatisticManager.INDEX_STATISTICS;
                                    if (systemTableStatBox.isSelected())
                                        options = options | IFBStatisticManager.SYSTEM_TABLE_STATISTICS;
                                    if (recordVersionsStatBox.isSelected())
                                        options = options | IFBStatisticManager.RECORD_VERSION_STATISTICS;
                                    statisticManager.getDatabaseStatistics(options);
                                }
                            } catch (SQLException ex) {
                                GUIUtilities.displayExceptionErrorDialog(ex.getMessage(), ex);
                            } finally {
                                try {
                                    statisticManager.getLogger().close();
                                    progressBar.stop();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                    getStat.start();
                    SwingWorker sw = new SwingWorker("readDBStat") {
                        @Override
                        public Object construct() {
                            try {
                                String dcName = "stat";
                                if (databaseBox.getSelectedItem() != null)
                                    dcName = ((DatabaseConnection) databaseBox.getSelectedItem()).getName();
                                readFromBufferedReader(bufferedReader, dcName + " " + LocalDateTime.now(), null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                    sw.start();
                    EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this,
                            ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) databaseBox.getSelectedItem()
                    ));
                } catch (Exception e1) {
                    GUIUtilities.displayExceptionErrorDialog("Error get database statistics", e1);
                }
            }
        });
        itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getSource() == defaultStatCheckBox) {
                        tableStatBox.setSelected(false);
                        indexStatBox.setSelected(false);
                        recordVersionsStatBox.setSelected(false);
                        systemTableStatBox.setSelected(false);
                        headerPageStatBox.setSelected(false);
                        onlySelectTablesBox.setSelected(false);
                        tablesField.setEnabled(false);
                    } else if (e.getSource() == headerPageStatBox) {
                        tableStatBox.setSelected(false);
                        indexStatBox.setSelected(false);
                        recordVersionsStatBox.setSelected(false);
                        systemTableStatBox.setSelected(false);
                        defaultStatCheckBox.setSelected(false);
                        onlySelectTablesBox.setSelected(false);
                        tablesField.setEnabled(false);
                    } else if (e.getSource() == tableStatBox || e.getSource() == indexStatBox || e.getSource() == recordVersionsStatBox || e.getSource() == systemTableStatBox) {
                        headerPageStatBox.setSelected(false);
                        onlySelectTablesBox.setSelected(false);
                        defaultStatCheckBox.setSelected(false);
                        tablesField.setEnabled(false);
                    } else if (e.getSource() == onlySelectTablesBox) {
                        tableStatBox.setSelected(false);
                        indexStatBox.setSelected(false);
                        recordVersionsStatBox.setSelected(false);
                        systemTableStatBox.setSelected(false);
                        defaultStatCheckBox.setSelected(false);
                        headerPageStatBox.setSelected(false);
                        tablesField.setEnabled(true);
                        SwingWorker sw = new SwingWorker("displayDialog") {
                            @Override
                            public Object construct() {
                                BaseDialog dialog = new BaseDialog(bundleString("selectTableDialogTitle"), true);
                                dialog.addDisplayComponent(new DialogPanel());
                                dialog.setResizable(false);
                                dialog.display();
                                return null;
                            }
                        };
                        sw.start();
                    }
                }
            }
        };
        defaultStatCheckBox = WidgetFactory.createCheckBox("defaultStatCheckBox", bundleString("default"));
        defaultStatCheckBox.setSelected(true);
        defaultStatCheckBox.addItemListener(itemListener);
        tableStatBox = WidgetFactory.createCheckBox("tableStatBox", bundleString("tables"));
        tableStatBox.addItemListener(itemListener);
        indexStatBox = WidgetFactory.createCheckBox("indexStatBox", bundleString("indices"));
        indexStatBox.addItemListener(itemListener);
        systemTableStatBox = WidgetFactory.createCheckBox("systemTableStatBox", bundleString("systemTables"));
        systemTableStatBox.addItemListener(itemListener);
        recordVersionsStatBox = WidgetFactory.createCheckBox("recordVersionsStatBox", bundleString("recordVersions"));
        recordVersionsStatBox.addItemListener(itemListener);
        headerPageStatBox = WidgetFactory.createCheckBox("headerPageStatBox", bundleString("headerPage"));
        headerPageStatBox.addItemListener(itemListener);
        onlySelectTablesBox = WidgetFactory.createCheckBox("onlySelectTablesBox", bundleString("onlySelectedTables"));
        onlySelectTablesBox.addItemListener(itemListener);
        onlySelectTablesBox.setEnabled(false);
        tablesStatPanel = new ListSelectionPanel();
        tablesStatPanel.setLabelText(bundleString("availableTablesLabel"), bundleString("selectedTablesLabel"));
        tablesField = WidgetFactory.createTextField("tablesField");
        tablesField.setEditable(false);
        okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIUtilities.closeSelectedDialog();
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Object object : tablesStatPanel.getSelectedValues()) {
                    AbstractTableObject tableObject = (AbstractTableObject) object;
                    if (!first)
                        sb.append(",");
                    first = false;
                    sb.append(tableObject.getName());
                }
                tablesField.setText(sb.toString());
            }
        };

    }

    protected void loadFromFile(FileChooserDialog fileChooser, JButton fileStatButton) {
        int returnVal = fileChooser.showOpenDialog(fileStatButton);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            SwingWorker sw = new SwingWorker("loadStatisticFromFile") {
                @Override
                public Object construct() {
                    String fullPath = fileChooser.getSelectedFile().getAbsolutePath();
                    clearAll();
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(
                                new InputStreamReader(
                                        Files.newInputStream(Paths.get(fullPath)), UserProperties.getInstance().getStringProperty("system.file.encoding")));
                        readFromBufferedReader(reader, fileChooser.getSelectedFile().getName(), fullPath);
                    } catch (Exception e1) {
                        GUIUtilities.displayExceptionErrorDialog("file opening error", e1);
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e1) {
                            }
                        }
                    }
                    return null;
                }

                @Override
                public void finished() {
                    GUIUtilities.showNormalCursor();
                    tabPane.setEnabled(true);

                }
            };
            GUIUtilities.showWaitCursor();
            tabPane.setEnabled(false);
            sw.start();

        }
    }

    @Override
    protected void setEnableElements() {

    }

    @Override
    protected void changeDatabaseConnection() {
        if (databaseBox.getSelectedItem() != null) {
            DatabaseConnection dc = (DatabaseConnection) databaseBox.getSelectedItem();
            fileDatabaseField.setText(dc.getSourceName());
            userField.setText(dc.getUserName());
            passwordField.setText(dc.getUnencryptedPassword());
            hostField.setText(dc.getHost());
            portField.setText(dc.getPort());
            charsetCombo.setSelectedItem(dc.getCharset());
            if (dc.isConnected()) {
                tablesStatPanel.createAvailableList(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(dc).getTables());
                tablesField.setText("");
                onlySelectTablesBox.setEnabled(true);
            } else {
                tablesField.setText("");
                onlySelectTablesBox.setSelected(false);
                onlySelectTablesBox.setEnabled(false);
            }
            initStatManager(dc);
        }
    }

    @Override
    protected void arrangeComponents() {
        gbh.insertEmptyRow(this, 5);
        gbh.nextRowFirstCol();
        add(toolBar, gbh.fillHorizontally().spanX().get());
        add(tabPane, gbh.nextRowFirstCol().fillBoth().spanX().setMaxWeightY().get());
        gbh.fullDefaults();
        tabPane.add(bundleString("Connection"), connectionPanel);
        connectionPanel.setLayout(new GridBagLayout());
        gbh.fullDefaults();
        gbh.addLabelFieldPair(connectionPanel, bundleString("Connections"), databaseBox, null, true, true);
        gbh.nextRowFirstCol();
        JLabel label = new JLabel(bundleString("Database"));
        connectionPanel.add(label, gbh.setLabelDefault().get());
        gbh.addLabelFieldPair(connectionPanel, fileDatabaseButton, fileDatabaseField, null, false, false);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Host"), hostField, null, false, false);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Port"), portField, null, false, true);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Username"), userField, null, true, false, 2);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Password"), passwordField, null, false, true);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Charset"), charsetCombo, null, true, true);
        gbh.nextRowFirstCol();
        connectionPanel.add(logToFileBox, gbh.setLabelDefault().get());
        gbh.addLabelFieldPair(connectionPanel, fileLogButton, fileLogField, null, false, true);
        connectionPanel.add(new CheckBoxPanel(), gbh.nextRowFirstCol().fillHorizontally().spanX().get());
        connectionPanel.add(getStatButton, gbh.nextRowFirstCol().setLabelDefault().get());
        connectionPanel.add(new JPanel(), gbh.anchorSouth().nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    @Override
    protected void postInitActions() {

    }

    void initStatManager(DatabaseConnection dc) {
        try {
            Driver driver = loadDriver(dc);
            statisticManager = (IFBStatisticManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(), driver, "FBStatisticManagerImpl");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFromBufferedReader(BufferedReader reader, String nameDb, String fullPath) {
        String line = null;
        StatParser.ParserParameters parserParameters = new StatParser.ParserParameters();
        parserParameters.db = new StatDatabase();
        parserParameters.db.name = nameDb;
        parserParameters.db.fullPath = fullPath;
        while (true) {
            try {
                if ((line = reader.readLine()) == null)
                    break;
                else {
                    parserParameters.db.sb.append(line).append("\n");
                    parserParameters = StatParser.parse(parserParameters, line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (parserParameters.db.indices != null) {
            for (StatIndex index : parserParameters.db.indices) {
                index.calculateValues();
            }
        }
        if (parserParameters.db.tables != null) {
            for (StatTable table : parserParameters.db.tables) {
                table.calculateValues();
            }
        }
        if (parserParameters.db.tablespaces != null) {
            for (StatTablespace ts : parserParameters.db.tablespaces) {
                ts.calculateValues();
            }
        }
        statDatabaseList.add(parserParameters.db);
        DbStatPanel dbStatPanel = new DbStatPanel(parserParameters.db);
        tabPane.addTab(null, null, dbStatPanel, parserParameters.db.fullPath);
        tabPane.setTabComponentAt(tabPane.indexOfComponent(dbStatPanel), new ClosableTabTitle(parserParameters.db.toString(), parserParameters.db, dbStatPanel));
        tabPane.setSelectedComponent(dbStatPanel);
    }

    protected StatDatabase compareDBS(StatDatabase mainDb, StatDatabase compareDb) {
        StatDatabase db = new StatDatabase();
        db = (StatDatabase) compareObjects(mainDb, compareDb, db);
        List<StatTable> firstList = new ArrayList<>();
        firstList.addAll(mainDb.tables);
        List<StatTable> secondList = new ArrayList<>();
        secondList.addAll(compareDb.tables);
        for (StatTable table : firstList) {
            boolean finded = false;
            for (StatTable table2 : secondList) {
                if (table.getName().contentEquals(table2.getName())) {
                    finded = true;
                    StatTable res = new StatTable();
                    res.name = table.name;
                    res = (StatTable) compareObjects(table, table2, res);
                    res.setCompared(TableModelObject.NOT_CHANGED);
                    res.indices = new ArrayList<>();
                    if (table2.indices != null)
                        res.indices.addAll(table2.indices);
                    db.tables.add(res);
                    secondList.remove(table2);
                    break;
                }


            }
            if (!finded) {
                table.setCompared(TableModelObject.DELETED);
                db.tables.add(table);
            }

        }
        for (StatTable table : secondList) {

            table.setCompared(TableModelObject.ADDED);
            db.tables.add(table);
        }
        List<StatIndex> firstListInd = new ArrayList<>();
        firstListInd.addAll(mainDb.indices);
        List<StatIndex> secondListInd = new ArrayList<>();
        secondListInd.addAll(compareDb.indices);
        for (StatIndex index : firstListInd) {
            boolean finded = false;
            for (StatIndex index1 : secondListInd) {
                if (index.getName().contentEquals(index1.getName())) {
                    finded = true;
                    StatIndex res = new StatIndex(index1.table);
                    res.name = index.name;
                    res.table_name = index1.table_name;
                    res = (StatIndex) compareObjects(index, index1, res);
                    res.setCompared(TableModelObject.NOT_CHANGED);
                    db.indices.add(res);
                    secondListInd.remove(index1);
                    break;
                }


            }
            if (!finded) {
                index.setCompared(TableModelObject.DELETED);
                db.indices.add(index);
            }

        }
        for (StatIndex index : secondListInd) {

            index.setCompared(TableModelObject.ADDED);
            db.indices.add(index);
        }

        List<StatTablespace> firstListTS = new ArrayList<>();
        firstListTS.addAll(mainDb.tablespaces);
        List<StatTablespace> secondListTS = new ArrayList<>();
        secondListTS.addAll(compareDb.tablespaces);
        for (StatTablespace tablespace : firstListTS) {
            boolean finded = false;
            for (StatTablespace tablespace1 : secondListTS) {
                if (tablespace.getName().contentEquals(tablespace1.getName())) {
                    finded = true;
                    StatTablespace res = new StatTablespace();
                    res.name = tablespace.name;
                    res = (StatTablespace) compareObjects(tablespace, tablespace1, res);
                    res.setCompared(TableModelObject.NOT_CHANGED);
                    db.tablespaces.add(res);
                    secondListTS.remove(tablespace1);
                    break;
                }
            }
            if (!finded) {
                tablespace.setCompared(TableModelObject.DELETED);
                db.tablespaces.add(tablespace);
            }

        }
        for (StatTablespace tablespace : secondListTS) {

            tablespace.setCompared(TableModelObject.ADDED);
            db.tablespaces.add(tablespace);
        }
        return db;
    }

    protected TableModelObject compareObjects(TableModelObject first, TableModelObject second, TableModelObject result) {
        Class clazz = result.getClass();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            if ((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                continue;
            String typeName = field.getGenericType().getTypeName();
            if (typeName.contentEquals("int")) {
                try {
                    field.setInt(result, field.getInt(second) - field.getInt(first));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (typeName.contentEquals("long")) {
                try {
                    field.setLong(result, field.getLong(second) - field.getLong(first));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (typeName.contentEquals("float")) {
                try {
                    field.setFloat(result, field.getFloat(second) - field.getFloat(first));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (typeName.contentEquals("double")) {
                try {
                    field.setDouble(result, field.getDouble(second) - field.getDouble(first));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (result instanceof StatTableIndex) {
            ((StatTableIndex) result).setDistribution(new FillDistribution());
            ((StatTableIndex) result).getDistribution().range_0_19 = ((StatTableIndex) second).getDistribution().range_0_19 - ((StatTableIndex) first).getDistribution().range_0_19;
            ((StatTableIndex) result).getDistribution().range_20_39 = ((StatTableIndex) second).getDistribution().range_20_39 - ((StatTableIndex) first).getDistribution().range_20_39;
            ((StatTableIndex) result).getDistribution().range_40_59 = ((StatTableIndex) second).getDistribution().range_40_59 - ((StatTableIndex) first).getDistribution().range_40_59;
            ((StatTableIndex) result).getDistribution().range_60_79 = ((StatTableIndex) second).getDistribution().range_60_79 - ((StatTableIndex) first).getDistribution().range_60_79;
            ((StatTableIndex) result).getDistribution().range_80_99 = ((StatTableIndex) second).getDistribution().range_80_99 - ((StatTableIndex) first).getDistribution().range_80_99;
        }
        return result;
    }

    void clearAll() {

    }

    class DialogPanel extends AbstractPanel {

        BottomButtonPanel bottomButtonPanel;

        @Override
        protected void initComponents() {
            bottomButtonPanel = new BottomButtonPanel(okListener, "OK", "help", true);
            bottomButtonPanel.setHelpButtonVisible(false);
        }

        @Override
        protected void arrangeComponents() {
            add(tablesStatPanel, gbh.fillBoth().topGap(10).spanX().setMaxWeightY().get());
            add(bottomButtonPanel, gbh.fillHorizontally().spanX().nextRowFirstCol().get());
        }

        @Override
        protected void postInitActions() {

        }
    }

    class CheckBoxPanel extends AbstractPanel {

        @Override
        protected void initComponents() {

        }

        @Override
        protected void arrangeComponents() {
            add(defaultStatCheckBox, gbh.setLabelDefault().nextRowFirstCol().get());
            add(tableStatBox, gbh.nextCol().get());
            add(indexStatBox, gbh.nextCol().get());
            add(recordVersionsStatBox, gbh.nextCol().get());
            add(systemTableStatBox, gbh.nextCol().get());
            add(headerPageStatBox, gbh.nextCol().get());
            add(onlySelectTablesBox, gbh.nextCol().get());
            add(tablesField, gbh.nextCol().fillHorizontally().spanX().get());
        }

        @Override
        protected void postInitActions() {

        }
    }

    class ClosableTabTitle extends JPanel {

        public ClosableTabTitle(final String title, StatDatabase db, JPanel panel) {
            super(new BorderLayout(5, 5));
            setOpaque(false);
            JLabel lbl = new JLabel(title);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2)
                        closeStat(db, panel);
                }
            });
            //lbl.setToolTipText(tooltip);
            JLabel icon = new JLabel(GUIUtilities.loadIcon("CloseDockable.png"));

            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    closeStat(db, panel);
                }
            });
            add(lbl, BorderLayout.CENTER);
            add(icon, BorderLayout.EAST);
        }

        public void closeStat(StatDatabase db, JPanel panel) {
            if (db != null)
                statDatabaseList.remove(db);
            tabPane.remove(panel);
        }
    }

}
