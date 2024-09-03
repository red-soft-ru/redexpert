package org.executequery.gui.browser;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.datasource.SimpleDataSource;
import org.executequery.gui.IconManager;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.comparer.ComparedObject;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.erd.ErdTable;
import org.executequery.gui.text.DifferenceSqlTextPanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.BackgroundProgressDialog;
import org.underworldlabs.swing.ConnectionsComboBox;
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
import java.util.stream.Collectors;

@SuppressWarnings("ExtractMethodRecommender")
public class ComparerDBPanel extends JPanel implements TabView {

    public static final String TITLE = bundleString("title");
    public static final String TITLE_EXPORT = bundleString("title-export");
    public static final String COMPARE_ICON = "icon_compare_db";
    public static final String EXTRACT_ICON = "icon_create_script";

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

    private Comparer comparer;
    private List<Integer> scriptGenerationOrder;
    private List<ComparedObject> comparedObjectList;
    private static final List<DatabaseConnection> busyConnectionList = new ArrayList<>();

    private boolean isComparing;
    private boolean isReverseOrder;
    private final boolean isExtractMetadata;
    private final boolean isErd;

    // --- panel components ---

    private ConnectionsComboBox dbMasterComboBox;
    private ConnectionsComboBox dbTargetComboBox;

    private JButton compareButton;
    private JButton saveScriptButton;
    private JButton executeScriptButton;
    private JButton selectAllAttributesButton;
    private JButton selectAllPropertiesButton;
    private JButton switchTargetSourceButton;

    private JTabbedPane tabPane;
    private SimpleSqlTextPanel sqlTextPanel;
    private LoggingOutputPanel loggingOutputPanel;
    private DifferenceSqlTextPanel differenceSqlTextPanel;

    private JTree dbComponentsTree;
    private ComparerTreeNode rootTreeNode;

    private JProgressBar progressBar;
    private BackgroundProgressDialog progressDialog;

    private List<DefaultDatabaseTable> erdTables;
    private Map<Integer, JCheckBox> attributesCheckBoxMap;
    private Map<Integer, JCheckBox> propertiesCheckBoxMap;

    private StringBuilder settingScriptProps;

    // ---

    public ComparerDBPanel() {
        this.isExtractMetadata = false;
        this.isErd = false;

        init();
        arrange();
    }

    public ComparerDBPanel(List<ErdTable> tables, DatabaseConnection databaseConnection) {
        this.isExtractMetadata = databaseConnection == null;
        this.isErd = true;

        this.erdTables = new ArrayList<>();
        for (ErdTable erd : tables)
            erdTables.add(new DefaultDatabaseTable(erd));

        init();
        arrange();

        if (databaseConnection != null) {
            dbTargetComboBox.setSelectedItem(databaseConnection);
            dbMasterComboBox.setSelectedItem(databaseConnection);
        }

        if (databaseConnection != null)
            attributesCheckBoxMap.values().forEach(checkBox -> checkBox.setSelected(true));
    }

    public ComparerDBPanel(DatabaseConnection databaseConnection) {
        this.isExtractMetadata = true;
        this.isErd = false;

        init();
        arrange();

        if (databaseConnection != null) {
            dbTargetComboBox.setSelectedItem(databaseConnection);
            dbMasterComboBox.setSelectedItem(databaseConnection);
        }

        attributesCheckBoxMap.values().forEach(checkBox -> checkBox.setSelected(true));
    }

    private void init() {

        isComparing = false;
        isReverseOrder = false;
        comparedObjectList = new ArrayList<>();

        // --- script generation order defining ---

        scriptGenerationOrder = Arrays.stream(NamedObject.META_TYPES_FOR_COMPARE).collect(Collectors.toList());
        scriptGenerationOrder.add(scriptGenerationOrder.indexOf(NamedObject.FUNCTION), STUBS);

        // --- buttons defining ---

        compareButton = WidgetFactory.createButton(
                "compareButton",
                bundleString(isExtractMetadata ? "CompareExportButton" : "CompareButton"),
                e -> compareDatabase()
        );

        saveScriptButton = WidgetFactory.createButton(
                "saveScriptButton",
                bundleString("SaveScriptButton"),
                e -> saveScript()
        );

        executeScriptButton = WidgetFactory.createButton(
                "executeScriptButton",
                bundleString("ExecuteScriptButton"),
                e -> executeScript()
        );

        selectAllAttributesButton = WidgetFactory.createButton(
                "selectAllAttributesButton",
                bundleString("SelectAllButton"),
                e -> selectAll("attributes")
        );

        selectAllPropertiesButton = WidgetFactory.createButton(
                "selectAllPropertiesButton",
                bundleString("SelectAllButton"),
                e -> selectAll("properties")
        );

        switchTargetSourceButton = WidgetFactory.createButton(
                "switchTargetSourceButton",
                "<html><p style=\"font-size:20pt\">&#x21C5;</p>",
                e -> switchTargetSource());
        switchTargetSourceButton.setVisible(!isExtractMetadata);
        switchTargetSourceButton.setHorizontalTextPosition(SwingConstants.LEFT);

        // --- attributes checkBox defining ---

        attributesCheckBoxMap = new HashMap<>();
        for (int objectType = 0; objectType < NamedObject.SYSTEM_DOMAIN; objectType++) {

            if (objectType == NamedObject.USER)
                continue;

            String checkBoxText = bundleString(NamedObject.META_TYPES_FOR_BUNDLE[objectType]);
            if (checkBoxText.isEmpty())
                checkBoxText = Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[objectType]);

            JCheckBox checkBox = WidgetFactory.createCheckBox(NamedObject.META_TYPES_FOR_BUNDLE[objectType] + "_CHECK", checkBoxText);
            attributesCheckBoxMap.put(objectType, checkBox);
        }

        // --- properties checkBox defining ---

        propertiesCheckBoxMap = new LinkedHashMap<>();
        propertiesCheckBoxMap.put(CHECK_CREATE, WidgetFactory.createCheckBox("checkCreate", bundleString("CheckCreate")));
        propertiesCheckBoxMap.put(CHECK_ALTER, WidgetFactory.createCheckBox("checkAlter", bundleString("CheckAlter")));
        propertiesCheckBoxMap.put(CHECK_DROP, WidgetFactory.createCheckBox("checkDrop", bundleString("CheckDrop")));
        propertiesCheckBoxMap.put(IGNORE_COMMENTS, WidgetFactory.createCheckBox("ignoreComments", bundleString(("IgnoreComments"))));
        propertiesCheckBoxMap.put(IGNORE_COMPUTED_FIELDS, WidgetFactory.createCheckBox("ignoreComputed", bundleString(("IgnoreComputed"))));
        propertiesCheckBoxMap.put(IGNORE_FIELDS_POSITIONS, WidgetFactory.createCheckBox("ignorePositions", bundleString(("IgnorePositions"))));
        propertiesCheckBoxMap.put(IGNORE_PK, WidgetFactory.createCheckBox("ignorePK", bundleString("IgnorePK")));
        propertiesCheckBoxMap.put(IGNORE_FK, WidgetFactory.createCheckBox("ignoreFK", bundleString("IgnoreFK")));
        propertiesCheckBoxMap.put(IGNORE_UK, WidgetFactory.createCheckBox("ignoreUK", bundleString("IgnoreUK")));
        propertiesCheckBoxMap.put(IGNORE_CK, WidgetFactory.createCheckBox("ignoreCK", bundleString("IgnoreCK")));

        if (isExtractMetadata) {
            propertiesCheckBoxMap.remove(CHECK_CREATE);
            propertiesCheckBoxMap.remove(CHECK_ALTER);
            propertiesCheckBoxMap.remove(CHECK_DROP);
            propertiesCheckBoxMap.remove(IGNORE_FIELDS_POSITIONS);

        } else {
            propertiesCheckBoxMap.get(CHECK_CREATE).setSelected(true);
            propertiesCheckBoxMap.get(CHECK_ALTER).setSelected(true);
            propertiesCheckBoxMap.get(CHECK_DROP).setSelected(true);
        }

        // --- comboBoxes defining ---

        dbTargetComboBox = WidgetFactory.createConnectionComboBox("dbTargetComboBox", false);
        dbMasterComboBox = WidgetFactory.createConnectionComboBox("dbMasterComboBox", false);
        dbMasterComboBox.setVisible(!isExtractMetadata);

        // --- db components tree view ---

        rootTreeNode = new ComparerTreeNode(bundleString("DatabaseChanges"));
        dbComponentsTree = new JTree(new DefaultTreeModel(rootTreeNode));
        dbComponentsTree.setName("dbComponentsTree");
        dbComponentsTree.setCellRenderer(new ComparerTreeCellRenderer());
        dbComponentsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                TreePath selectionPath = dbComponentsTree.getSelectionPath();
                if (selectionPath != null) {

                    ComparerTreeNode node = (ComparerTreeNode) selectionPath.getLastPathComponent();
                    comparedObjectList.stream()
                            .filter(i -> i.getType() == node.type)
                            .filter(i -> Objects.equals(i.getName(), node.name))
                            .filter(i -> Objects.equals(i.getPlugin(), node.plugin))
                            .findFirst()
                            .ifPresent(i -> differenceSqlTextPanel.setTexts(i.getSourceObjectScript(), i.getTargetObjectScript()));

                    if (e.getClickCount() > 1)
                        goToScript(node);
                }
            }
        });

        // --- other components ---

        loggingOutputPanel = new LoggingOutputPanel();
        loggingOutputPanel.append(bundleString("WelcomeText"));
        loggingOutputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);

        sqlTextPanel = new SimpleSqlTextPanel();
        differenceSqlTextPanel = new DifferenceSqlTextPanel(bundleString("SourceLabel"), bundleString("TargetLabel"), !isExtractMetadata);
    }

    private void arrange() {
        GridBagHelper gbh;

        // --- connections selector panel ---

        JPanel connectionsSelectorPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally();
        connectionsSelectorPanel.add(new JLabel(bundleString("CompareDatabaseLabel")), gbh.topGap(3).setMinWeightX().get());
        connectionsSelectorPanel.add(dbTargetComboBox, gbh.nextCol().topGap(0).leftGap(5).setMaxWeightX().get());
        if (!isExtractMetadata) {
            connectionsSelectorPanel.add(new JLabel(bundleString("MasterDatabaseLabel")), gbh.nextRowFirstCol().topGap(8).leftGap(0).setMinWeightX().get());
            connectionsSelectorPanel.add(dbMasterComboBox, gbh.nextCol().topGap(5).leftGap(5).setMaxWeightX().get());
        }

        // --- connections panel ---

        JPanel connectionsPanel = new JPanel(new GridBagLayout());
        connectionsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ConnectionsLabel")));

        gbh = new GridBagHelper().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();
        connectionsPanel.add(connectionsSelectorPanel, gbh.setMaxWeightX().get());
        connectionsPanel.add(switchTargetSourceButton, gbh.nextCol().leftGap(0).setMinWeightX().fillVertical().get());
        connectionsPanel.add(compareButton, gbh.nextRowFirstCol().leftGap(5).topGap(0).fillHorizontally().spanX().get());

        // --- attributes panel ---

        JPanel attributesPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        attributesPanel.add(selectAllAttributesButton, gbh.get());
        for (JCheckBox checkBox : attributesCheckBoxMap.values())
            attributesPanel.add(checkBox, gbh.nextRowFirstCol().get());
        attributesPanel.add(new JPanel(), gbh.nextRowFirstCol().bottomGap(5).setMaxWeightY().spanY().get());

        JScrollPane attributesPanelWithScrolls = new JScrollPane(attributesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        attributesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder(bundleString("AttributesLabel")));
        attributesPanelWithScrolls.setMinimumSize(new Dimension(220, 150));

        // --- properties panel ---

        JPanel propertiesPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        propertiesPanel.add(selectAllPropertiesButton, gbh.get());
        for (JCheckBox checkBox : propertiesCheckBoxMap.values())
            propertiesPanel.add(checkBox, gbh.nextRowFirstCol().get());
        propertiesPanel.add(new JPanel(), gbh.nextRowFirstCol().bottomGap(5).setMaxWeightY().spanY().get());

        JScrollPane propertiesPanelWithScrolls = new JScrollPane(propertiesPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder(bundleString("PropertiesLabel")));
        propertiesPanelWithScrolls.setMinimumSize(new Dimension(220, 150));

        // --- SQL panel ---

        JPanel sqlPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(0, 5, 0, 5).anchorNorthWest().fillBoth();
        sqlPanel.add(sqlTextPanel, gbh.setMaxWeightY().spanX().get());
        sqlPanel.add(saveScriptButton, gbh.nextRowFirstCol().setLabelDefault().topGap(0).bottomGap(6).get());
        sqlPanel.add(executeScriptButton, gbh.nextCol().leftGap(5).get());
        sqlPanel.add(new JPanel(), gbh.nextCol().setMaxWeightX().get());

        // --- view panel ---

        JScrollPane dbComponentsTreePanel = new JScrollPane(dbComponentsTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dbComponentsTreePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 5));

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

        JPanel comparePanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().setInsets(5, 5, 5, 0).anchorNorthWest().fillBoth();
        if (!isErd) {
            comparePanel.add(connectionsPanel, gbh.spanX().get());
            comparePanel.add(attributesPanelWithScrolls, gbh.nextRowFirstCol().setMaxWeightY().setWidth(1).spanY().get());
            gbh.nextCol().leftGap(5);

        } else {
            comparePanel.add(compareButton, gbh.spanX().get());
            gbh.nextRow().setMaxWeightY().spanY();
        }
        comparePanel.add(propertiesPanelWithScrolls, gbh.get());

        // --- main panel ---

        gbh = new GridBagHelper();
        gbh.setLabelDefault().setInsets(0, 5, 5, 5).anchorNorthWest().fillBoth();

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(comparePanel, gbh.setMaxWeightY().get());
        mainPanel.add(tabPane, gbh.nextCol().bottomGap(1).spanX().get());
        mainPanel.add(progressBar, gbh.nextRowFirstCol().topGap(0).leftGap(5).bottomGap(5).setMinWeightY().spanX().get());

        // --- layout configure ---

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

    }

    private boolean prepareComparer() {
        if (isErd && isExtractMetadata) {
            comparer = new Comparer(this, null, new boolean[]{
                    !isPropertySelected(IGNORE_PK),
                    !isPropertySelected(IGNORE_FK),
                    !isPropertySelected(IGNORE_UK),
                    !isPropertySelected(IGNORE_CK)
            },
                    !isPropertySelected(IGNORE_COMMENTS),
                    !isPropertySelected(IGNORE_COMPUTED_FIELDS),
                    !isPropertySelected(IGNORE_FIELDS_POSITIONS));
            return true;
        }

        DatabaseConnection masterConnection = dbMasterComboBox.getSelectedConnection();
        DatabaseConnection targetConnection = dbTargetComboBox.getSelectedConnection();

        if (busyConnectionList.contains(masterConnection) || busyConnectionList.contains(targetConnection)) {
            GUIUtilities.displayWarningMessage(isExtractMetadata ?
                    bundleString("UnableExtractBusyConnections") :
                    bundleString("UnableCompareBusyConnections")
            );
            return false;
        }

        try {

            if (!isExtractMetadata && !masterConnection.isConnected())
                ConnectionMediator.getInstance().connect(masterConnection, true);
            if (!targetConnection.isConnected())
                ConnectionMediator.getInstance().connect(targetConnection, true);

        } catch (DataSourceException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoConnections"));
            return false;
        }

        comparer = !isExtractMetadata ?
                new Comparer(
                        this, targetConnection, masterConnection,
                        new boolean[]{
                                !isPropertySelected(IGNORE_PK),
                                !isPropertySelected(IGNORE_FK),
                                !isPropertySelected(IGNORE_UK),
                                !isPropertySelected(IGNORE_CK)
                        },
                        !isPropertySelected(IGNORE_COMMENTS),
                        !isPropertySelected(IGNORE_COMPUTED_FIELDS),
                        !isPropertySelected(IGNORE_FIELDS_POSITIONS)
                ) :
                new Comparer(this, targetConnection,
                        new boolean[]{
                                !isPropertySelected(IGNORE_PK),
                                !isPropertySelected(IGNORE_FK),
                                !isPropertySelected(IGNORE_UK),
                                !isPropertySelected(IGNORE_CK)
                        },
                        !isPropertySelected(IGNORE_COMMENTS),
                        !isPropertySelected(IGNORE_COMPUTED_FIELDS),
                        !isPropertySelected(IGNORE_FIELDS_POSITIONS)
                );

        if (!isExtractMetadata)
            busyConnectionList.add(masterConnection);
        busyConnectionList.add(targetConnection);

        loggingOutputPanel.clear();
        sqlTextPanel.setSQLText("");
        comparer.clearLists();
        comparedObjectList.clear();

        try {

            DefaultDatabaseHost masterHost = new DefaultDatabaseHost(dbMasterComboBox.getSelectedConnection());
            DefaultDatabaseHost slaveHost = new DefaultDatabaseHost(dbTargetComboBox.getSelectedConnection());

            if (!slaveHost.getDatabaseProductName().toLowerCase().contains("reddatabase") ||
                    (!isExtractMetadata && !masterHost.getDatabaseProductName().toLowerCase().contains("reddatabase"))) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("FDBCompared"));
            }

            if (slaveHost.getDatabaseMajorVersion() < 3 || (!isExtractMetadata && masterHost.getDatabaseMajorVersion() < 3)) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("PACKAGE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("FUNCTION")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("DDL_TRIGGER")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("RDBVersionBelow3"));

            } else if (slaveHost.getDatabaseMajorVersion() < 4 || (!isExtractMetadata && masterHost.getDatabaseMajorVersion() < 4)) {

                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("JOB")).setSelected(false);
                loggingOutputPanel.append(bundleString("RDBVersionBelow4"));
            }

        } catch (SQLException | NullPointerException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoConnections"));
            return false;
        }

        settingScriptProps = new StringBuilder();
        settingScriptProps.append("\n/* Setting properties */\n\n");
        settingScriptProps.append("SET NAMES ").append(getMasterDBCharset()).append(";\n");
        settingScriptProps.append("SET SQL DIALECT ").append(getMasterDBDialect()).append(";\n");
        settingScriptProps.append("CONNECT '").append(SimpleDataSource.generateFormatedUrl(
                comparer.getMasterConnection(),
                SimpleDataSource.buildAdvancedProperties(comparer.getMasterConnection())));
        settingScriptProps.append("' USER '").append(comparer.getMasterConnection().getUserName());
        settingScriptProps.append("' PASSWORD '").append(comparer.getMasterConnection().getUnencryptedPassword());
        settingScriptProps.append("';\nSET AUTODDL ON;\n");

        comparer.addToScript(settingScriptProps.toString());

        return true;
    }

    private void compare() {

        comparer.dropConstraints(
                attributesCheckBoxMap.get(NamedObject.TABLE).isSelected(),
                attributesCheckBoxMap.get(NamedObject.GLOBAL_TEMPORARY).isSelected(),
                isPropertySelected(CHECK_DROP),
                isPropertySelected(CHECK_ALTER)
        );

        rootTreeNode.removeAllChildren();

        if ((isPropertySelected(CHECK_CREATE) || isExtractMetadata) && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.CREATE, bundleString("CreateObjects")));

            if (isReverseOrder) {
                isReverseOrder = false;
                Collections.reverse(scriptGenerationOrder);
            }

            if (isErd && isExtractMetadata) {
                updateOutputPanels(ComparerTreeNode.CREATE, NamedObject.TABLE);
                comparer.createErds(erdTables);

                if (!isPropertySelected(IGNORE_COMPUTED_FIELDS) && !isCanceled()) {
                    loggingOutputPanel.append("\n============= COMPUTED FIELDS defining  =============");
                    if (!Objects.equals(comparer.getComputedFieldsList(), "") && comparer.getComputedFieldsList() != null)
                        loggingOutputPanel.append(comparer.getComputedFieldsList());
                    comparer.createComputedFields();
                }

            } else {

                for (Integer type : scriptGenerationOrder) {

                    if (isCanceled())
                        break;

                    if (type == STUBS) {
                        comparer.setStubsNeed(
                                true,
                                attributesCheckBoxMap.get(NamedObject.FUNCTION).isSelected(),
                                attributesCheckBoxMap.get(NamedObject.PROCEDURE).isSelected(),
                                attributesCheckBoxMap.get(NamedObject.TRIGGER).isSelected(),
                                attributesCheckBoxMap.get(NamedObject.DDL_TRIGGER).isSelected(),
                                attributesCheckBoxMap.get(NamedObject.DATABASE_TRIGGER).isSelected()
                        );

                        if (!isPropertySelected(IGNORE_COMPUTED_FIELDS) && !isCanceled()) {
                            loggingOutputPanel.append("\n============= COMPUTED FIELDS defining  =============");
                            if (!Objects.equals(comparer.getComputedFieldsList(), "") && comparer.getComputedFieldsList() != null)
                                loggingOutputPanel.append(comparer.getComputedFieldsList());
                            comparer.createComputedFields();
                        }

                        continue;
                    }

                    if (attributesCheckBoxMap.get(type).isSelected()) {
                        updateOutputPanels(ComparerTreeNode.CREATE, type);
                        comparer.createObjects(type);
                    }
                }
            }
        }

        if (isPropertySelected(CHECK_ALTER) && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.ALTER, bundleString("AlterObjects")));

            if (isReverseOrder) {
                isReverseOrder = false;
                Collections.reverse(scriptGenerationOrder);
            }

            comparer.setStubsNeed(
                    false,
                    attributesCheckBoxMap.get(NamedObject.FUNCTION).isSelected(),
                    attributesCheckBoxMap.get(NamedObject.PROCEDURE).isSelected(),
                    attributesCheckBoxMap.get(NamedObject.TRIGGER).isSelected(),
                    attributesCheckBoxMap.get(NamedObject.DDL_TRIGGER).isSelected(),
                    attributesCheckBoxMap.get(NamedObject.DATABASE_TRIGGER).isSelected()
            );

            for (Integer type : scriptGenerationOrder) {

                if (isCanceled())
                    break;

                if (type == STUBS)
                    continue;

                if (attributesCheckBoxMap.get(type).isSelected()) {
                    updateOutputPanels(ComparerTreeNode.ALTER, type);

                    if (isErd)
                        comparer.alterErds(erdTables);
                    else
                        comparer.alterObjects(type);
                }
            }
        }

        if (isPropertySelected(CHECK_DROP) && !isCanceled()) {

            rootTreeNode.add(new ComparerTreeNode(ComparerTreeNode.DROP, bundleString("DropObjects")));

            if (!isReverseOrder) {
                isReverseOrder = true;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (isCanceled())
                    break;

                if (type == STUBS)
                    continue;

                if (attributesCheckBoxMap.get(type).isSelected()) {
                    updateOutputPanels(ComparerTreeNode.DROP, type);

                    if (isErd)
                        comparer.dropErds(erdTables);
                    else
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

    }

    private boolean isPropertySelected(int key) {
        return propertiesCheckBoxMap.containsKey(key) && propertiesCheckBoxMap.get(key).isSelected();
    }

    // --- buttons handlers ---

    private void compareDatabase() {

        if (isComparing) {
            isComparing = false;
            compareButton.setEnabled(false);
            compareButton.setText(bundleString("Canceling"));
            return;
        }

        if (!isExtractMetadata) {

            if (Objects.equals(dbTargetComboBox.getSelectedConnection(), dbMasterComboBox.getSelectedConnection())) {
                GUIUtilities.displayWarningMessage(bundleString("UnableCompareSampleConnections"));
                return;
            }

            if (!isPropertySelected(CHECK_CREATE) && !isPropertySelected(CHECK_ALTER) && !isPropertySelected(CHECK_DROP)) {
                GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoProperties"));
                return;
            }
        }
        if (!isErd) {
            for (int objectType = 0; objectType < NamedObject.SYSTEM_DOMAIN; objectType++) {

                if (objectType == NamedObject.USER)
                    continue;

                if (attributesCheckBoxMap.get(objectType).isSelected())
                    break;

                if (objectType == NamedObject.SYSTEM_DOMAIN - 1) {
                    GUIUtilities.displayWarningMessage(bundleString(isExtractMetadata ?
                            "UnableExtractNoAttributes" :
                            "UnableCompareNoAttributes")
                    );
                    return;
                }
            }
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
                        GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorOccurred"), e, this.getClass());
                        Log.error("Error occurred while comparing DBs", e);
                    }

                    int[] counter = comparer.getCounter();
                    long elapsedTime = System.currentTimeMillis() - startTime;

                    GUIUtilities.displayInformationMessage(isExtractMetadata ?
                            String.format(bundleString("ExtractingFinishMessage"), counter[0]) :
                            String.format(bundleString("ComparingFinishMessage"), counter[0], counter[1], counter[2]));
                    Log.info(String.format("Comparing has been finished. Time elapsed: %d ms", elapsedTime));

                    if (comparer != null) {
                        busyConnectionList.remove(comparer.getMasterConnection());
                        busyConnectionList.remove(comparer.getCompareConnection());
                    }
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
        compareButton.setText(bundleString(isExtractMetadata ? "CompareExportButton" : "CompareButton"));
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setString(isExtractMetadata ? bundleString("ExtractingFinish") : bundleString("ComparingFinish"));

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

                String[] sqlTextLines = sqlTextPanel.getSQLText().split("\n");
                for (String sqlLine : sqlTextLines) {
                    byte[] buffer = (sqlLine + "\n").getBytes();
                    path.write(buffer, 0, buffer.length);
                }

                loggingOutputPanel.appendAction(bundleString("ScriptSaved"));
                loggingOutputPanel.append(bundleString("SavedTo") + fileSavePath);

            } catch (IOException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    private void executeScript() {

        if (comparer == null || comparer.getScript().isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("NothingToExecute"));
            return;
        }

        QueryEditor queryEditor = new QueryEditor(isErd() ? sqlTextPanel.getSQLText() : sqlTextPanel.getSQLText().replace(settingScriptProps.toString(), ""));
        queryEditor.setSelectedConnection(comparer.getMasterConnection());
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

        for (JCheckBox checkBox : checkBoxMap.values())
            curFlag = curFlag && checkBox.isSelected();

        for (JCheckBox checkBox : checkBoxMap.values())
            checkBox.setSelected(!curFlag);
    }

    private void switchTargetSource() {
        DatabaseConnection sourceConnection = dbMasterComboBox.getSelectedConnection();
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

        try (ResultSet rs = comparer.getMasterExecutor().execute(query, true).getResultSet()) {
            while (rs.next())
                charset = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace(System.out);

        } finally {
            comparer.getMasterExecutor().releaseResources();
        }

        return charset;
    }

    private String getMasterDBDialect() {

        String dialect = "";
        String query = "select mon$database.mon$sql_dialect from mon$database";

        try (ResultSet rs = comparer.getMasterExecutor().execute(query, true).getResultSet()) {
            while (rs.next())
                dialect = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace(System.out);

        } finally {
            comparer.getMasterExecutor().releaseResources();
        }

        return dialect;
    }

    private void updateOutputPanels(int treeNodeType, Integer objectType) {

        ComparerTreeNode childNode = (ComparerTreeNode) rootTreeNode.getChildAt(rootTreeNode.getChildCount() - 1);
        childNode.add(new ComparerTreeNode(
                treeNodeType,
                objectType,
                Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[objectType]),
                ComparerTreeNode.TYPE_FOLDER
        ));

        String pattern = Constants.EMPTY;
        if (treeNodeType == ComparerTreeNode.CREATE)
            pattern = "\n============= {0} to CREATE  =============";
        else if (treeNodeType == ComparerTreeNode.ALTER)
            pattern = "\n============= {0} to ALTER  =============";
        else if (treeNodeType == ComparerTreeNode.DROP)
            pattern = "\n============= {0} to DROP  =============";

        loggingOutputPanel.append(MessageFormat.format(
                pattern,
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[objectType])
        ));
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

    public void addTreeComponent(int action, int type, NamedObject object) {

        ComparerTreeNode actionNode = (ComparerTreeNode) rootTreeNode.getChildByAction(action);
        if (actionNode != null) {

            ComparerTreeNode typeNode = (ComparerTreeNode) actionNode.getChildByType(type);
            if (typeNode != null) {
                String plugin = object instanceof DefaultDatabaseUser ? ((DefaultDatabaseUser) object).getPlugin() : null;
                typeNode.add(new ComparerTreeNode(action, type, object.getName(), plugin, ComparerTreeNode.COMPONENT));
            }
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

    public void addComparedObject(ComparedObject object) {
        comparedObjectList.add(object);
    }

    public boolean isCanceled() {
        return progressDialog.isCancel() || !isComparing;
    }

    public boolean isExtractMetadata() {
        return isExtractMetadata;
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

        @Override
        public boolean accept(File file) {
            if (file.isDirectory())
                return true;

            return file.getName().endsWith(extension);
        }

        @Override
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
        private final String plugin;

        public ComparerTreeNode(String name) {
            this(-1, -1, name, null, ROOT);
        }

        public ComparerTreeNode(int action, String name) {
            this(action, -1, name, null, ROOT);
        }

        public ComparerTreeNode(int action, int type, String name, int level) {
            this(action, type, name, null, level);
        }

        public ComparerTreeNode(int action, int type, String name, String plugin, int level) {
            super();
            this.action = action;
            this.type = type;
            this.name = name;
            this.plugin = plugin;
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

        private final Color textForeground;
        private final Color selectedTextForeground;

        ComparerTreeCellRenderer() {
            textForeground = UIManager.getColor("Tree.textForeground");
            selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            this.selected = selected;
            this.hasFocus = hasFocus;

            ComparerTreeNode treeNode = (ComparerTreeNode) value;
            selected &= hasFocus;
            switch (treeNode.type) {
                case NamedObject.DOMAIN:
                    setIcon(IconManager.getIcon("icon_db_domain", selected));
                    break;
                case NamedObject.TABLE:
                    setIcon(IconManager.getIcon("icon_db_table", selected));
                    break;
                case NamedObject.GLOBAL_TEMPORARY:
                    setIcon(IconManager.getIcon("icon_db_table_global", selected));
                    break;
                case NamedObject.VIEW:
                    setIcon(IconManager.getIcon("icon_db_view", selected));
                    break;
                case NamedObject.PROCEDURE:
                    setIcon(IconManager.getIcon("icon_db_procedure", selected));
                    break;
                case NamedObject.FUNCTION:
                    setIcon(IconManager.getIcon("icon_db_function", selected));
                    break;
                case NamedObject.PACKAGE:
                    setIcon(IconManager.getIcon("icon_db_package", selected));
                    break;
                case NamedObject.TRIGGER:
                    setIcon(IconManager.getIcon("icon_db_trigger_table", selected));
                    break;
                case NamedObject.DDL_TRIGGER:
                    setIcon(IconManager.getIcon("icon_db_trigger_ddl", selected));
                    break;
                case NamedObject.DATABASE_TRIGGER:
                    setIcon(IconManager.getIcon("icon_db_trigger_db", selected));
                    break;
                case NamedObject.SEQUENCE:
                    setIcon(IconManager.getIcon("icon_db_generator", selected));
                    break;
                case NamedObject.EXCEPTION:
                    setIcon(IconManager.getIcon("icon_db_exception", selected));
                    break;
                case NamedObject.UDF:
                    setIcon(IconManager.getIcon("icon_db_udf", selected));
                    break;
                case NamedObject.USER:
                    setIcon(IconManager.getIcon("icon_db_user", selected));
                    break;
                case NamedObject.ROLE:
                    setIcon(IconManager.getIcon("icon_db_role", selected));
                    break;
                case NamedObject.INDEX:
                    setIcon(IconManager.getIcon("icon_db_index", selected));
                    break;
                case NamedObject.TABLESPACE:
                    setIcon(IconManager.getIcon("icon_db_tablespace", selected));
                    break;
                case NamedObject.JOB:
                    setIcon(IconManager.getIcon("icon_db_job", selected));
                    break;
                case NamedObject.COLLATION:
                    setIcon(IconManager.getIcon("icon_db_collation", selected));
                    break;
                default:
                    setIcon(IconManager.getIcon("icon_folder", selected));
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

            setForeground(selected ? selectedTextForeground : textForeground);
            return this;
        }

    }

    public boolean isErd() {
        return isErd;
    }
}
