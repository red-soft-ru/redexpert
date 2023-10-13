package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.datasource.SimpleDataSource;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.browser.comparer.ComparedObject;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.text.DifferenceSqlTextPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.BackgroundProgressDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparerDBPanel extends JPanel implements TabView {

    public static final String TITLE = bundleString("ComparerDB");
    public static final String FRAME_ICON = "ComparerDB_16.png";

    private static final int CHECK_CREATE = 0;
    private static final int CHECK_ALTER = 1;
    private static final int CHECK_DROP = 2;
    private static final int IGNORE_COMMENTS = 3;
    private static final int IGNORE_COMPUTED_FIELDS = 4;
    private static final int IGNORE_FIELDS_POSITIONS = 5;
    private static final int IGNORE_PK = 50;
    private static final int IGNORE_FK = IGNORE_PK + 1;
    private static final int IGNORE_UK = IGNORE_FK + 1;
    private static final int IGNORE_CK = IGNORE_UK + 1;
    private static final int STUBS = -100;

    private static boolean isComparing = false;

    private Comparer comparer;
    private List<DatabaseConnection> databaseConnectionList;
    private List<Integer> scriptGenerationOrder;
    private List<ComparedObject> comparedObjectList;
    private boolean isScriptGeneratorOrderReversed;

    // --- panel components ---

    private JComboBox dbMasterComboBox;
    private JComboBox dbTargetComboBox;
    private JButton compareButton;
    private JButton saveScriptButton;
    private JButton executeScriptButton;
    private JButton selectAllAttributesButton;
    private JButton selectAllPropertiesButton;
    private JButton switchTargetSourceButton;

    private JTabbedPane tabPane;
    private LoggingOutputPanel loggingOutputPanel;
    private SimpleSqlTextPanel sqlTextPanel;
    private DifferenceSqlTextPanel differenceSqlTextPanel;
    private JTree dbComponentsTree;
    private ComparerTreeNode rootTreeNode;

    private JProgressBar progressBar;
    private BackgroundProgressDialog progressDialog;

    private Map<Integer, JCheckBox> attributesCheckBoxMap;
    private Map<Integer, JCheckBox> propertiesCheckBoxMap;

    private StringBuilder settingScriptProps;

    // ---

    public ComparerDBPanel() {

        init();

        List<DatabaseConnection> connections =
                ((DatabaseConnectionRepository) Objects.requireNonNull(
                        RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID))).findAll();

        for (DatabaseConnection dc : connections) {
            databaseConnectionList.add(dc);
            dbTargetComboBox.addItem(dc.getName());
            dbMasterComboBox.addItem(dc.getName());
        }

    }

    private void init() {

        databaseConnectionList = new ArrayList<>();
        comparedObjectList = new ArrayList<>();

        // --- script generation order defining ---

        scriptGenerationOrder = new LinkedList<>();
        isScriptGeneratorOrderReversed = false;

        scriptGenerationOrder.add(NamedObject.COLLATION);
        scriptGenerationOrder.add(NamedObject.DOMAIN);
        scriptGenerationOrder.add(NamedObject.TABLESPACE);
        scriptGenerationOrder.add(NamedObject.TABLE);
        scriptGenerationOrder.add(NamedObject.GLOBAL_TEMPORARY);
        scriptGenerationOrder.add(NamedObject.VIEW);
        scriptGenerationOrder.add(NamedObject.INDEX);
        scriptGenerationOrder.add(NamedObject.SEQUENCE);
        scriptGenerationOrder.add(NamedObject.EXCEPTION);
        scriptGenerationOrder.add(NamedObject.ROLE);
        scriptGenerationOrder.add(NamedObject.USER);
        scriptGenerationOrder.add(STUBS);
        scriptGenerationOrder.add(NamedObject.FUNCTION);
        scriptGenerationOrder.add(NamedObject.PROCEDURE);
        scriptGenerationOrder.add(NamedObject.JOB);
        scriptGenerationOrder.add(NamedObject.UDF);
        scriptGenerationOrder.add(NamedObject.TRIGGER);
        scriptGenerationOrder.add(NamedObject.DDL_TRIGGER);
        scriptGenerationOrder.add(NamedObject.DATABASE_TRIGGER);
        scriptGenerationOrder.add(NamedObject.PACKAGE);

        // --- buttons defining ---

        compareButton = new JButton();
        compareButton.setText(bundleString("CompareButton"));
        compareButton.addActionListener(e -> compareDatabase());

        saveScriptButton = new JButton();
        saveScriptButton.setText(bundleString("SaveScriptButton"));
        saveScriptButton.addActionListener(e -> saveScript());

        executeScriptButton = new JButton();
        executeScriptButton.setText(bundleString("ExecuteScriptButton"));
        executeScriptButton.addActionListener(e -> executeScript());

        selectAllAttributesButton = new JButton();
        selectAllAttributesButton.setText(bundleString("SelectAllButton"));
        selectAllAttributesButton.addActionListener(e -> selectAll("attributes"));

        selectAllPropertiesButton = new JButton();
        selectAllPropertiesButton.setText(bundleString("SelectAllButton"));
        selectAllPropertiesButton.addActionListener(e -> selectAll("properties"));

        switchTargetSourceButton = new JButton();
        switchTargetSourceButton.setText("<html><p style=\"font-size:20pt\">&#x21C5;</p>"); // Unicode Character '⇅' (U+21C5)
        switchTargetSourceButton.addActionListener(e -> switchTargetSource());

        // --- attributes checkBox defining ---

        attributesCheckBoxMap = new HashMap<>();
        for (int objectType = 0; objectType < NamedObject.SYSTEM_DOMAIN; objectType++) {

            String checkBoxText = bundleString(NamedObject.META_TYPES_FOR_BUNDLE[objectType]);
            if (checkBoxText.isEmpty())
                checkBoxText = Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[objectType]);

            attributesCheckBoxMap.put(objectType, new JCheckBox(checkBoxText));
        }

        // --- properties checkBox defining ---

        propertiesCheckBoxMap = new LinkedHashMap<>();
        propertiesCheckBoxMap.put(CHECK_CREATE, new JCheckBox(bundleString("CheckCreate")));
        propertiesCheckBoxMap.put(CHECK_ALTER, new JCheckBox(bundleString("CheckAlter")));
        propertiesCheckBoxMap.put(CHECK_DROP, new JCheckBox(bundleString("CheckDrop")));
        propertiesCheckBoxMap.put(IGNORE_COMMENTS, new JCheckBox(bundleString(("IgnoreComments"))));
        propertiesCheckBoxMap.put(IGNORE_COMPUTED_FIELDS, new JCheckBox(bundleString(("IgnoreComputed"))));
        propertiesCheckBoxMap.put(IGNORE_FIELDS_POSITIONS, new JCheckBox(bundleString(("IgnorePositions"))));
        propertiesCheckBoxMap.put(IGNORE_PK, new JCheckBox(bundleString("IgnorePK")));
        propertiesCheckBoxMap.put(IGNORE_FK, new JCheckBox(bundleString("IgnoreFK")));
        propertiesCheckBoxMap.put(IGNORE_UK, new JCheckBox(bundleString("IgnoreUK")));
        propertiesCheckBoxMap.put(IGNORE_CK, new JCheckBox(bundleString("IgnoreCK")));

        propertiesCheckBoxMap.get(CHECK_CREATE).setSelected(true);
        propertiesCheckBoxMap.get(CHECK_ALTER).setSelected(true);
        propertiesCheckBoxMap.get(CHECK_DROP).setSelected(true);

        // --- comboBoxes defining ---

        dbTargetComboBox = new JComboBox<DatabaseConnection>();
        dbTargetComboBox.removeAllItems();

        dbMasterComboBox = new JComboBox<DatabaseConnection>();
        dbMasterComboBox.removeAllItems();

        // --- db components tree view ---

        rootTreeNode = new ComparerTreeNode(bundleString("DatabaseChanges"));
        dbComponentsTree = new JTree(new DefaultTreeModel(rootTreeNode));
        dbComponentsTree.setCellRenderer(new ComparerTreeCellRenderer());
        dbComponentsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                TreePath selectionPath = dbComponentsTree.getSelectionPath();
                if (selectionPath != null) {

                    ComparerTreeNode node = (ComparerTreeNode) selectionPath.getLastPathComponent();
                    comparedObjectList.stream()
                            .filter(i -> (i.getType() == node.type && i.getName().equals(node.name))).findFirst()
                            .ifPresent(i -> differenceSqlTextPanel.setTexts(i.getSourceObjectScript(), i.getTargetObjectScript()));

                    if (e.getClickCount() > 1)
                        goToScript(node);

                }
            }
        });

        // --- other components ---

        loggingOutputPanel = new LoggingOutputPanel();
        loggingOutputPanel.append(bundleString("WelcomeText"));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);

        sqlTextPanel = new SimpleSqlTextPanel();
        differenceSqlTextPanel = new DifferenceSqlTextPanel(bundleString("SourceLabel"), bundleString("TargetLabel"));

        // ---

        arrangeComponents();
    }

    private void arrangeComponents() {

        GridBagHelper gridBagHelper;

        // --- connections selector panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel connectionsSelectorPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(connectionsSelectorPanel,
                bundleString("CompareDatabaseLabel"), dbTargetComboBox, null);
        gridBagHelper.addLabelFieldPair(connectionsSelectorPanel,
                bundleString("MasterDatabaseLabel"), dbMasterComboBox, null);

        // --- connections panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel connectionsPanel = new JPanel(new GridBagLayout());
        connectionsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ConnectionsLabel")));

        connectionsPanel.add(connectionsSelectorPanel, gridBagHelper.setMaxWeightX().get());
        connectionsPanel.add(switchTargetSourceButton, gridBagHelper.nextCol().setMinWeightX().fillVertical().get());
        connectionsPanel.add(compareButton, gridBagHelper.nextRowFirstCol().setWidth(2).fillHorizontally().get());

        // --- attributes panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel attributesPanel = new JPanel(new GridBagLayout());

        attributesPanel.add(selectAllAttributesButton, gridBagHelper.nextRowFirstCol().setLabelDefault().anchorNorthWest().get());
        for (JCheckBox checkBox : attributesCheckBoxMap.values())
            attributesPanel.add(checkBox, gridBagHelper.nextRowFirstCol().get());
        attributesPanel.add(new JPanel(), gridBagHelper.nextRowFirstCol().setMaxWeightY().spanY().get());

        attributesPanel.add(new JScrollPane());

        JScrollPane attributesPanelWithScrolls = new JScrollPane(attributesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        attributesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder(bundleString("AttributesLabel")));
        attributesPanelWithScrolls.setMinimumSize(new Dimension(220, 150));

        // --- properties panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel propertiesPanel = new JPanel(new GridBagLayout());

        propertiesPanel.add(selectAllPropertiesButton, gridBagHelper.nextRowFirstCol().setLabelDefault().anchorNorthWest().get());
        for (JCheckBox checkBox : propertiesCheckBoxMap.values())
            propertiesPanel.add(checkBox, gridBagHelper.nextRowFirstCol().get());
        propertiesPanel.add(new JPanel(), gridBagHelper.nextRowFirstCol().setMaxWeightY().spanY().get());

        JScrollPane propertiesPanelWithScrolls = new JScrollPane(propertiesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder(bundleString("PropertiesLabel")));
        propertiesPanelWithScrolls.setMinimumSize(new Dimension(220, 150));

        // --- SQL panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        JPanel sqlPanel = new JPanel(new GridBagLayout());

        sqlPanel.add(sqlTextPanel, gridBagHelper.setWidth(3).setMaxWeightY().spanX().get());
        sqlPanel.add(saveScriptButton, gridBagHelper.setLabelDefault().nextRowFirstCol().get());
        sqlPanel.add(executeScriptButton, gridBagHelper.nextCol().get());
        sqlPanel.add(new JPanel(), gridBagHelper.nextCol().get());

        // --- view panel ---

        JScrollPane dbComponentsTreePanel = new JScrollPane(dbComponentsTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JSplitPane viewPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        viewPanel.setResizeWeight(0.25);
        viewPanel.setTopComponent(dbComponentsTreePanel);
        viewPanel.setBottomComponent(differenceSqlTextPanel);

        // --- tabbed pane ---

        tabPane = new JTabbedPane();

        tabPane.add(bundleString("OutputLabel"), loggingOutputPanel);
        tabPane.add(bundleString("TreeView"), viewPanel);
        tabPane.add("SQL", sqlPanel);

        // --- compare panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        JPanel comparePanel = new JPanel(new GridBagLayout());

        comparePanel.add(connectionsPanel, gridBagHelper.setWidth(2).get());
        comparePanel.add(attributesPanelWithScrolls, gridBagHelper.nextRowFirstCol().setWidth(1).get());
        comparePanel.add(propertiesPanelWithScrolls, gridBagHelper.nextCol().spanY().get());

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(comparePanel, gridBagHelper.setMaxWeightY().get());
        mainPanel.add(tabPane, gridBagHelper.nextCol().spanX().get());
        mainPanel.add(progressBar, gridBagHelper.nextRowFirstCol().setMinWeightY().spanX().get());

        // --- layout configure ---

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

    }

    private boolean prepareComparer() {

        DatabaseConnection masterConnection = databaseConnectionList.get(dbMasterComboBox.getSelectedIndex());
        DatabaseConnection targetConnection = databaseConnectionList.get(dbTargetComboBox.getSelectedIndex());

        try {

            if (!masterConnection.isConnected())
                ConnectionManager.createDataSource(masterConnection);
            if (!targetConnection.isConnected())
                ConnectionManager.createDataSource(targetConnection);

        } catch (DataSourceException e) {
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoConnections"));
            return false;
        }

        comparer = new Comparer(
                this, targetConnection, masterConnection,
                new boolean[]{
                        !propertiesCheckBoxMap.get(IGNORE_PK).isSelected(),
                        !propertiesCheckBoxMap.get(IGNORE_FK).isSelected(),
                        !propertiesCheckBoxMap.get(IGNORE_UK).isSelected(),
                        !propertiesCheckBoxMap.get(IGNORE_CK).isSelected()
                },
                !propertiesCheckBoxMap.get(IGNORE_COMMENTS).isSelected(),
                !propertiesCheckBoxMap.get(IGNORE_COMPUTED_FIELDS).isSelected(),
                !propertiesCheckBoxMap.get(IGNORE_FIELDS_POSITIONS).isSelected()
        );

        loggingOutputPanel.clear();
        sqlTextPanel.setSQLText("");
        comparer.clearLists();
        comparedObjectList.clear();

        try {

            DefaultDatabaseHost masterHost = new DefaultDatabaseHost(databaseConnectionList.get(dbMasterComboBox.getSelectedIndex()));
            DefaultDatabaseHost slaveHost = new DefaultDatabaseHost(databaseConnectionList.get(dbTargetComboBox.getSelectedIndex()));

            if (!slaveHost.getDatabaseProductName().toLowerCase().contains("reddatabase") ||
                    !masterHost.getDatabaseProductName().toLowerCase().contains("reddatabase")) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("FDBCompared"));
            }

            if (slaveHost.getDatabaseMajorVersion() < 3 || masterHost.getDatabaseMajorVersion() < 3) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("USER")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("PACKAGE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("FUNCTION")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("DDL_TRIGGER")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("RDBVersionBelow3"));

            } else if (slaveHost.getDatabaseMajorVersion() < 4 || masterHost.getDatabaseMajorVersion() < 4) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("RDBVersionBelow4"));
            }

        } catch (SQLException | NullPointerException e) {
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoConnections"));
            return false;
        }

        settingScriptProps = new StringBuilder();
        settingScriptProps.append("\n/* Setting properties */\n\n");
        settingScriptProps.append("SET NAMES ").append(getMasterDBCharset()).append(";\n");
        settingScriptProps.append("SET SQL DIALECT ").append(getMasterDBDialect()).append(";\n");
        settingScriptProps.append("CONNECT '").append(SimpleDataSource.generateUrl(
                        comparer.getMasterConnection().getDatabaseConnection(),
                        SimpleDataSource.buildAdvancedProperties(comparer.getMasterConnection().getDatabaseConnection()))
                .replace("jdbc:firebirdsql://", "")
        );
        settingScriptProps.append("' USER '").append(comparer.getMasterConnection().getDatabaseConnection().getUserName());
        settingScriptProps.append("' PASSWORD '").append(comparer.getMasterConnection().getDatabaseConnection().getUnencryptedPassword());
        settingScriptProps.append("';\nSET AUTODDL ON;\n");

        comparer.addToScript(settingScriptProps.toString());

        return true;
    }

    private void compare() {

        comparer.dropConstraints(
                attributesCheckBoxMap.get(NamedObject.TABLE).isSelected(),
                attributesCheckBoxMap.get(NamedObject.GLOBAL_TEMPORARY).isSelected(),
                propertiesCheckBoxMap.get(CHECK_DROP).isSelected(),
                propertiesCheckBoxMap.get(CHECK_ALTER).isSelected());

        rootTreeNode.removeAllChildren();

        if (propertiesCheckBoxMap.get(CHECK_CREATE).isSelected() && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.CREATE, bundleString("CreateObjects")));

            if (isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = false;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (isCanceled())
                    break;

                if (type == STUBS) {
                    comparer.createStubs(attributesCheckBoxMap.get(NamedObject.FUNCTION).isSelected(),
                            attributesCheckBoxMap.get(NamedObject.PROCEDURE).isSelected(),
                            attributesCheckBoxMap.get(NamedObject.TRIGGER).isSelected(),
                            attributesCheckBoxMap.get(NamedObject.DDL_TRIGGER).isSelected(),
                            attributesCheckBoxMap.get(NamedObject.DATABASE_TRIGGER).isSelected(),
                            attributesCheckBoxMap.get(NamedObject.JOB).isSelected());

                    continue;
                }

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    ((ComparerTreeNode) rootTreeNode.getChildAt(ComparerTreeNode.CREATE))
                            .add(new ComparerTreeNode(ComparerTreeNode.CREATE, type,
                                    Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]), ComparerTreeNode.TYPE_FOLDER));

                    loggingOutputPanel.append(MessageFormat.format("\n============= {0} to CREATE  =============",
                            Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                    comparer.createObjects(type);
                }
            }
        }

        if (propertiesCheckBoxMap.get(CHECK_ALTER).isSelected() && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.ALTER, bundleString("AlterObjects")));

            if (isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = false;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (isCanceled())
                    break;

                if (type == STUBS)
                    continue;

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    ((ComparerTreeNode) rootTreeNode.getChildAt(ComparerTreeNode.ALTER))
                            .add(new ComparerTreeNode(ComparerTreeNode.ALTER, type,
                                    Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]), ComparerTreeNode.TYPE_FOLDER));

                    loggingOutputPanel.append(MessageFormat.format("\n============= {0} to ALTER  =============",
                            Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                    comparer.alterObjects(type);
                }
            }
        }

        if (propertiesCheckBoxMap.get(CHECK_DROP).isSelected() && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.DROP, bundleString("DropObjects")));

            if (!isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = true;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (isCanceled())
                    break;

                if (type == STUBS)
                    continue;

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    ((ComparerTreeNode) rootTreeNode.getChildAt(ComparerTreeNode.DROP))
                            .add(new ComparerTreeNode(ComparerTreeNode.DROP, type,
                                    Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]), ComparerTreeNode.TYPE_FOLDER));

                    loggingOutputPanel.append(MessageFormat.format("\n============= {0} to DROP  =============",
                            Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                    comparer.dropObjects(type);
                }
            }
        }

        if (!isCanceled()) {
            loggingOutputPanel.append("\n============= CONSTRAINTS defining  =============");
            if (!Objects.equals(comparer.getConstraintsList(), "") && comparer.getConstraintsList() != null)
                loggingOutputPanel.append(comparer.getConstraintsList());
            comparer.createConstraints();
        }

        if (!propertiesCheckBoxMap.get(IGNORE_COMPUTED_FIELDS).isSelected() && !isCanceled()) {
            loggingOutputPanel.append("\n============= COMPUTED FIELDS defining  =============");
            if (!Objects.equals(comparer.getComputedFieldsList(), "") && comparer.getComputedFieldsList() != null)
                loggingOutputPanel.append(comparer.getComputedFieldsList());
            comparer.createComputedFields();
        }

    }

    // --- buttons handlers ---

    private void compareDatabase() {

        if (isComparing) {
            isComparing = false;
            compareButton.setEnabled(false);
            compareButton.setText(bundleString("Canceling"));
            return;
        }

        if (databaseConnectionList.size() < 2 ||
                dbTargetComboBox.getSelectedIndex() == dbMasterComboBox.getSelectedIndex()) {
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareSampleConnections"));
            return;
        }

        for (int i = 0; i < NamedObject.SYSTEM_DOMAIN; i++) {
            if (attributesCheckBoxMap.get(i).isSelected())
                break;
            if (i == NamedObject.SYSTEM_DOMAIN - 1) {
                GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoAttributes"));
                return;
            }
        }

        if (!propertiesCheckBoxMap.get(CHECK_CREATE).isSelected() &&
                !propertiesCheckBoxMap.get(CHECK_ALTER).isSelected() &&
                !propertiesCheckBoxMap.get(CHECK_DROP).isSelected()) {
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoProperties"));
            return;
        }

        progressDialog = new BackgroundProgressDialog(bundleString("Executing"));
        SwingWorker worker = new SwingWorker("DBComparerProcess", this) {

            @Override
            public Object construct() {

                compareButton.setText(Bundles.get("common.cancel.button"));
                isComparing = true;

                long startTime = System.currentTimeMillis();

                if (prepareComparer()) {

                    try {
                        compare();
                    } catch (Throwable e) {
                        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorOccurred"), e);
                        Log.error("Error occurred while comparing DBs", e);
                    }

                    int[] counter = comparer.getCounter();
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    GUIUtilities.displayInformationMessage(
                            String.format(bundleString("ComparingEnds"), counter[0], counter[1], counter[2]));
                    Log.info(String.format("Comparing has been finished. Time elapsed: %d ms", elapsedTime));
                }

                return null;
            }

            @Override
            public void finished() {

                if (progressDialog != null)
                    progressDialog.dispose();

                finishCompare();
            }
        };

        worker.start();
        progressDialog.run();

        if (progressDialog.isCancel()) {
            worker.setCancel(true);
            compareButton.setText(bundleString("Canceling"));
            compareButton.setEnabled(false);
        }
    }

    private void finishCompare() {

        for (int i = 0; comparer != null && i < comparer.getScript().size(); i++)
            sqlTextPanel.getTextPane().append(comparer.getScript(i));

        isComparing = false;
        compareButton.setEnabled(true);
        compareButton.setText(bundleString("CompareButton"));
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setString(bundleString("ProgressBarFinish"));

        for (int i = 0; i < rootTreeNode.getChildCount(); i++)
            sortTreeNodes(rootTreeNode.getChildAt(i));

        dbComponentsTree.setModel(new DefaultTreeModel(rootTreeNode));
    }

    private void saveScript() {

        if (comparer == null || comparer.getScript().isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("NothingToSave"));
            return;
        }

        JFileChooser fileSave = new JFileChooser("C:\\");

        FileFilter sqlFilter = new FileTypeFilter(".sql", "SQL files");
        FileFilter txtFilter = new FileTypeFilter(".txt", "Text files");

        fileSave.addChoosableFileFilter(sqlFilter);
        fileSave.addChoosableFileFilter(txtFilter);
        fileSave.setAcceptAllFileFilterUsed(false);

        int ret = fileSave.showDialog(null, bundleString("SaveScriptButton"));

        if (ret == JFileChooser.APPROVE_OPTION) {

            File file = fileSave.getSelectedFile();
            String name = file.getAbsoluteFile().toString();

            int dot = name.lastIndexOf(".");
            dot = (dot == -1) ? name.length() : dot;

            String fileSavePath = name.substring(0, dot)
                    + fileSave.getFileFilter().getDescription().substring(fileSave.getFileFilter().getDescription().indexOf("(*") + 2,
                    fileSave.getFileFilter().getDescription().lastIndexOf(")"));

            try (FileOutputStream path = new FileOutputStream(fileSavePath)) {

                for (int i = 0; i < comparer.getScript().size(); i++) {
                    String text = comparer.getScript(i);
                    byte[] buffer = text.getBytes();
                    path.write(buffer, 0, buffer.length);
                }

                loggingOutputPanel.appendAction(bundleString("ScriptSaved"));
                loggingOutputPanel.append(bundleString("SavedTo") + fileSavePath);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeScript() {

        if (comparer == null || comparer.getScript().isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("NothingToExecute"));
            return;
        }

        QueryEditor queryEditor = new QueryEditor(sqlTextPanel.getSQLText().replace(settingScriptProps.toString(), ""));
        queryEditor.setSelectedConnection(comparer.getMasterConnection().getDatabaseConnection());
        GUIUtilities.addCentralPane(
                QueryEditor.TITLE, QueryEditor.FRAME_ICON,
                queryEditor, null, true);

    }

    private void selectAll(String selectedBox) {

        boolean curFlag = true;
        Map<Integer, JCheckBox> checkBoxMap;

        if (Objects.equals(selectedBox, "attributes"))
            checkBoxMap = attributesCheckBoxMap;
        else if (Objects.equals(selectedBox, "properties"))
            checkBoxMap = propertiesCheckBoxMap;
        else
            return;

        for (JCheckBox checkBox : checkBoxMap.values()) {
            curFlag = curFlag && checkBox.isSelected();
        }
        for (JCheckBox checkBox : checkBoxMap.values()) {
            checkBox.setSelected(!curFlag);
        }

    }

    private void switchTargetSource() {
        Object sourceConnection = dbMasterComboBox.getSelectedItem();
        dbMasterComboBox.setSelectedItem(dbTargetComboBox.getSelectedItem());
        dbTargetComboBox.setSelectedItem(sourceConnection);
    }

    @Override
    public boolean tabViewClosing() {
        sqlTextPanel.cleanup();
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

    // ---

    private String getMasterDBCharset() {

        String charset = "";
        String query = "select rdb$database.rdb$character_set_name from rdb$database";

        try (ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet()) {
            while (rs.next())
                charset = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        return charset;
    }

    private String getMasterDBDialect() {

        String dialect = "";
        String query = "select mon$database.mon$sql_dialect from mon$database";

        try (ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet()) {
            while (rs.next())
                dialect = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        return dialect;
    }

    public void recreateProgressBar(String label, String metaTag, int maxValue) {
        progressBar.setValue(0);
        progressBar.setMaximum(maxValue);
        progressBar.setString(MiscUtils.isNull(metaTag) ? bundleString(label) : String.format(bundleString(label), Bundles.get(NamedObject.class, metaTag)));
    }

    public void incrementProgressBarValue() {
        progressBar.setValue(progressBar.getValue() + 1);
    }

    public void goToScript(ComparerTreeNode node) {

        if (node.isComponent) {
            tabPane.setSelectedIndex(2);

            String searchText = "/\\* " + node.name.replace("$", "\\$") + " \\*/";
            Pattern pattern = Pattern.compile(searchText);
            Matcher matcher = pattern.matcher(sqlTextPanel.getSQLText());

            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                sqlTextPanel.getTextPane().select(start, end);
            }
        }

    }

    public void addTreeComponent(int action, int type, String name) {

        ComparerTreeNode actionNode = (ComparerTreeNode) rootTreeNode.getChildByAction(action);
        if (actionNode != null) {

            ComparerTreeNode typeNode = (ComparerTreeNode) actionNode.getChildByType(type);
            if (typeNode != null)
                typeNode.add(new ComparerTreeNode(action, type, name, ComparerTreeNode.COMPONENT));
        }

    }

    private void sortTreeNodes(TreeNode node) {

        ComparerTreeNode comparerTreeNode = (ComparerTreeNode) node;
        Map<Integer, ComparerTreeNode> childrenMap = new HashMap<>();

        Enumeration<TreeNode> childrenEnumeration = comparerTreeNode.children();
        while (childrenEnumeration.hasMoreElements()) {
            ComparerTreeNode child = (ComparerTreeNode) childrenEnumeration.nextElement();
            childrenMap.put(child.type, child);
        }
        comparerTreeNode.removeAllChildren();

        for (int type = 0; type < NamedObject.SYSTEM_DOMAIN; type++) {

            ComparerTreeNode child = childrenMap.get(type);
            if (child != null)
                comparerTreeNode.add(child);
        }

    }

    public List<ComparedObject> getComparedObjectList() {
        return comparedObjectList;
    }

    public boolean isCanceled() {
        return progressDialog.isCancel() || !isComparing;
    }

    public void addToLog(String text) {
        loggingOutputPanel.append(text);
    }

    public static String bundleString(String key) {
        return Bundles.get(ComparerDBPanel.class, key);
    }

    private static class FileTypeFilter extends FileFilter {

        private final String extension;
        private final String description;

        public FileTypeFilter(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public boolean accept(File file) {
            if (file.isDirectory())
                return true;

            return file.getName().endsWith(extension);
        }

        public String getDescription() {
            return description + String.format(" (*%s)", extension);
        }

    }

    public static class ComparerTreeNode extends DefaultMutableTreeNode {

        // --- Action constraints ---

        public static final int CREATE = 0;
        public static final int ALTER = CREATE + 1;
        public static final int DROP = ALTER + 1;

        // --- Level constraints ---

        public static final int ROOT = 0;
        public static final int TYPE_FOLDER = ROOT + 1;
        public static final int COMPONENT = TYPE_FOLDER + 1;

        // ---

        private final boolean isComponent;
        private final int level;
        private final int action;
        private final int type;
        private final String name;

        public ComparerTreeNode(String name) {
            this(-1, -1, name, ROOT);
        }

        public ComparerTreeNode(int action, String name) {
            this(action, -1, name, ROOT);
        }

        public ComparerTreeNode(int action, int type, String name, int level) {
            super();
            this.action = action;
            this.type = type;
            this.name = name;
            this.level = level;
            this.isComponent = (level == COMPONENT);
        }

        public TreeNode getChildByAction(int type) {

            for (Object child : children) {
                ComparerTreeNode comparerTreeNode = (ComparerTreeNode) child;
                if (comparerTreeNode.action == type)
                    return comparerTreeNode;
            }

            return null;
        }

        public TreeNode getChildByType(int type) {

            for (Object child : children) {
                ComparerTreeNode comparerTreeNode = (ComparerTreeNode) child;
                if (comparerTreeNode.type == type)
                    return comparerTreeNode;
            }

            return null;
        }

    }

    private static class ComparerTreeCellRenderer extends AbstractTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            ComparerTreeNode treeNode = (ComparerTreeNode) value;
            switch (treeNode.type) {

                case NamedObject.DOMAIN:
                    setIcon(GUIUtilities.loadIcon("domain16.png"));
                    break;

                case NamedObject.TABLE:
                    setIcon(GUIUtilities.loadIcon("PlainTable16.png"));
                    break;

                case NamedObject.GLOBAL_TEMPORARY:
                    setIcon(GUIUtilities.loadIcon("GlobalTable16.png"));
                    break;

                case NamedObject.VIEW:
                    setIcon(GUIUtilities.loadIcon("TableView16.png"));
                    break;

                case NamedObject.PROCEDURE:
                    setIcon(GUIUtilities.loadIcon("Procedure16.png"));
                    break;

                case NamedObject.FUNCTION:
                    setIcon(GUIUtilities.loadIcon("Function16.png"));
                    break;

                case NamedObject.PACKAGE:
                    setIcon(GUIUtilities.loadIcon("package16.png"));
                    break;

                case NamedObject.TRIGGER:
                    setIcon(GUIUtilities.loadIcon("Trigger.png"));
                    break;

                case NamedObject.DDL_TRIGGER:
                    setIcon(GUIUtilities.loadIcon("TriggerDDL.png"));
                    break;

                case NamedObject.DATABASE_TRIGGER:
                    setIcon(GUIUtilities.loadIcon("TriggerDB.png"));
                    break;

                case NamedObject.SEQUENCE:
                    setIcon(GUIUtilities.loadIcon("Sequence16.png"));
                    break;

                case NamedObject.EXCEPTION:
                    setIcon(GUIUtilities.loadIcon("exception16.png"));
                    break;

                case NamedObject.UDF:
                    setIcon(GUIUtilities.loadIcon("udf16.png"));
                    break;

                case NamedObject.USER:
                    setIcon(GUIUtilities.loadIcon("User16.png"));
                    break;

                case NamedObject.ROLE:
                    setIcon(GUIUtilities.loadIcon("user_manager_16.png"));
                    break;

                case NamedObject.INDEX:
                    setIcon(GUIUtilities.loadIcon("TableIndex16.png"));
                    break;

                case NamedObject.TABLESPACE:
                    setIcon(GUIUtilities.loadIcon("tablespace16.png"));
                    break;

                case NamedObject.JOB:
                    setIcon(GUIUtilities.loadIcon("job16.png"));
                    break;

                case NamedObject.COLLATION:
                    setIcon(GUIUtilities.loadIcon("XmlFile16.png"));
                    break;

                default:
                    setIcon(getDefaultOpenIcon());
                    break;

            }

            Font font = getFont();
            if (treeNode.level == ComparerTreeNode.TYPE_FOLDER && treeNode.getChildCount() > 0) {
                setText(treeNode.name + " (" + treeNode.getChildCount() + ")");
                if (font != null)
                    setFont(font.deriveFont(Font.BOLD));

            } else {
                setText(treeNode.name);
                if (font != null)
                    setFont(font.deriveFont(Font.PLAIN));
            }

            return this;
        }

    }

}
