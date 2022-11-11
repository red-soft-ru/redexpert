package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.DefaultProgressDialog;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class ComparerDBPanel extends JPanel {

    public static final String TITLE = bundleString("ComparerDB");
    public static final String FRAME_ICON = "ComparerDB_16.png";
    private static final String WELCOME_TEXT = bundleString("WelcomeText");

    private Comparer comparer;
    private List<DatabaseConnection> databaseConnectionList;
    private List<Integer> scriptGenerationOrder;
    private boolean isScriptGeneratorOrderReversed;

    // --- panel components ---

    private JComboBox dbMasterComboBox;
    private JComboBox dbCompareComboBox;
    private JButton compareButton;
    private JButton saveScriptButton;
    private JButton executeScriptButton;
    private JButton selectAllAttributesButton;
    private JButton selectAllPropertiesButton;
    private LoggingOutputPanel loggingOutputPanel;
    private SimpleSqlTextPanel sqlTextPanel;
    private DefaultProgressDialog progressDialog;

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
            if (dc.isConnected()) {
                databaseConnectionList.add(dc);
                dbCompareComboBox.addItem(dc.getName());
                dbMasterComboBox.addItem(dc.getName());
            }
        }

    }

    private void init() {

        databaseConnectionList = new ArrayList<>();

        // --- script generation order defining ---

        scriptGenerationOrder = new ArrayList<>();
        isScriptGeneratorOrderReversed = false;

        scriptGenerationOrder.add(NamedObject.DOMAIN);
        scriptGenerationOrder.add(NamedObject.TABLE);
        scriptGenerationOrder.add(NamedObject.GLOBAL_TEMPORARY);
        scriptGenerationOrder.add(NamedObject.TABLESPACE);
        scriptGenerationOrder.add(NamedObject.VIEW);
        scriptGenerationOrder.add(NamedObject.INDEX);
        scriptGenerationOrder.add(NamedObject.SEQUENCE);
        scriptGenerationOrder.add(NamedObject.EXCEPTION);
        scriptGenerationOrder.add(NamedObject.PACKAGE);
        scriptGenerationOrder.add(NamedObject.ROLE);
        scriptGenerationOrder.add(NamedObject.USER);
        scriptGenerationOrder.add(NamedObject.PROCEDURE);
        scriptGenerationOrder.add(NamedObject.FUNCTION);
        scriptGenerationOrder.add(NamedObject.UDF);
        scriptGenerationOrder.add(NamedObject.TRIGGER);
        scriptGenerationOrder.add(NamedObject.DDL_TRIGGER);
        scriptGenerationOrder.add(NamedObject.DATABASE_TRIGGER);

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

        // --- attributes checkBox defining ---

        attributesCheckBoxMap = new HashMap<>();
        for (int i = 0; i < NamedObject.SYSTEM_DOMAIN; i++)
            attributesCheckBoxMap.put(i, new JCheckBox(Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[i])));

        // --- properties checkBox defining ---

        propertiesCheckBoxMap = new HashMap<>();
        propertiesCheckBoxMap.put(0, new JCheckBox(bundleString("CheckCreate")));
        propertiesCheckBoxMap.put(1, new JCheckBox(bundleString("CheckAlter")));
        propertiesCheckBoxMap.put(2, new JCheckBox(bundleString("CheckDrop")));
//        propertiesCheckBoxMap.put(3, new JCheckBox(bundleString("SafeTypeConversion")));
        propertiesCheckBoxMap.put(4, new JCheckBox(bundleString("IgnoreTablesConstraints")));
        propertiesCheckBoxMap.put(5, new JCheckBox(bundleString(("IgnoreComments"))));

        // --- comboBoxes defining ---

        dbCompareComboBox = new JComboBox();
        dbCompareComboBox.removeAllItems();

        dbMasterComboBox = new JComboBox();
        dbMasterComboBox.removeAllItems();

        // --- other components ---

        loggingOutputPanel = new LoggingOutputPanel();
        loggingOutputPanel.append(WELCOME_TEXT);

        sqlTextPanel = new SimpleSqlTextPanel();

        // ---

        arrangeComponents();
    }

    private void arrangeComponents() {

        GridBagHelper gridBagHelper;

        // --- connections panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel connectionsPanel = new JPanel(new GridBagLayout());
        connectionsPanel.setBorder(BorderFactory.createTitledBorder(bundleString("ConnectionsLabel")));

        gridBagHelper.addLabelFieldPair(connectionsPanel,
                bundleString("MasterDatabaseLabel"), dbMasterComboBox, null);
        gridBagHelper.addLabelFieldPair(connectionsPanel,
                bundleString("CompareDatabaseLabel"), dbCompareComboBox, null);
        connectionsPanel.add(compareButton, gridBagHelper.nextRowFirstCol().get());

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

        // --- tabbed pane ---

        JTabbedPane tabPane = new JTabbedPane();

        tabPane.add(bundleString("OutputLabel"), loggingOutputPanel);
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

        mainPanel.add(comparePanel, gridBagHelper.get());
        mainPanel.add(tabPane, gridBagHelper.nextCol().spanY().spanX().get());

        // --- layout configure ---

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

    }

    private void startComparing() {

        comparer = new Comparer(
                databaseConnectionList.get(dbCompareComboBox.getSelectedIndex()),
                databaseConnectionList.get(dbMasterComboBox.getSelectedIndex()));
        Comparer.TABLE_CONSTRAINTS_NEED = !propertiesCheckBoxMap.get(4).isSelected();
        Comparer.COMMENTS_NEED = !propertiesCheckBoxMap.get(5).isSelected();

        loggingOutputPanel.clear();
        loggingOutputPanel.append(WELCOME_TEXT);
        sqlTextPanel.setSQLText("");
        comparer.clearLists();

        try {

            if (new DefaultDatabaseHost(databaseConnectionList.get(dbCompareComboBox.getSelectedIndex())).getDatabaseMajorVersion() < 4 ||
                    new DefaultDatabaseHost(databaseConnectionList.get(dbMasterComboBox.getSelectedIndex())).getDatabaseMajorVersion() < 4) {
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                loggingOutputPanel.append(bundleString("RDBVersionBelow4"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        String charset = "";
        String dialect = "";

        String query = "select rdb$database.rdb$character_set_name\n"
                + "from rdb$database\n";

        try (ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet()) {
            while (rs.next())
                charset = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        query = "select mon$database.mon$sql_dialect\n"
                + "from mon$database\n";

        try (ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet()) {
            while (rs.next())
                dialect = rs.getString(1).trim();

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        settingScriptProps = new StringBuilder();
        settingScriptProps.append("\n/* Setting properties */\n\n");
        settingScriptProps.append("SET NAMES ").append(charset).append(";\n");
        settingScriptProps.append("SET SQL DIALECT ").append(dialect).append(";\n");
        settingScriptProps.append("CONNECT '").append(comparer.getMasterConnection().getDatabaseConnection().getName());
        settingScriptProps.append("' USER '").append(comparer.getMasterConnection().getDatabaseConnection().getUserName());
        settingScriptProps.append("' PASSWORD '").append(comparer.getMasterConnection().getDatabaseConnection().getUnencryptedPassword());
        settingScriptProps.append("';\nSET AUTO DDL ON;\n");

        comparer.addToScript(settingScriptProps.toString());

        // ----- comparing -----

        if (propertiesCheckBoxMap.get(0).isSelected() && !progressDialog.isCancel()) {

            if (isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = false;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (progressDialog.isCancel())
                    break;

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    comparer.setLists("");
                    comparer.createObjects(type);

                    if (!Objects.equals(comparer.getLists(), "")) {
                        loggingOutputPanel.append(MessageFormat.format("============= {0} to CREATE  =============",
                                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                        loggingOutputPanel.append(comparer.getLists());
                    }

                }
            }
        }

        if (propertiesCheckBoxMap.get(1).isSelected() && !progressDialog.isCancel()) {

            if (isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = false;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (progressDialog.isCancel())
                    break;

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    comparer.setLists("");
                    comparer.alterObjects(type);

                    if (!Objects.equals(comparer.getLists(), "")) {
                        loggingOutputPanel.append(MessageFormat.format("============= {0} to ALTER  =============",
                                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                        loggingOutputPanel.append(comparer.getLists());
                    }

                }
            }
        }

        if (propertiesCheckBoxMap.get(2).isSelected() && !progressDialog.isCancel()) {

            if (!isScriptGeneratorOrderReversed) {
                isScriptGeneratorOrderReversed = true;
                Collections.reverse(scriptGenerationOrder);
            }

            for (Integer type : scriptGenerationOrder) {

                if (progressDialog.isCancel())
                    break;

                if (attributesCheckBoxMap.get(type).isSelected()) {

                    comparer.setLists("");
                    comparer.dropObjects(type);

                    if (!Objects.equals(comparer.getLists(), "")) {
                        loggingOutputPanel.append(MessageFormat.format("============= {0} to DROP  =============",
                                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
                        loggingOutputPanel.append(comparer.getLists());
                    }

                }
            }
        }

        if (!propertiesCheckBoxMap.get(4).isSelected()) {
            comparer.createConstraints();
            if (!Objects.equals(comparer.getConstraintsList(), "") && comparer.getConstraintsList() != null) {
                loggingOutputPanel.append("============= CONSTRAINTS defining  =============");
                loggingOutputPanel.append(comparer.getConstraintsList());
            }
        }

        for (int i = 0; i < comparer.getScript().size(); i++)
            sqlTextPanel.getTextPane().append(comparer.getScript(i));

    }

    // --- buttons handlers ---

    private void compareDatabase() {

        if (databaseConnectionList.size() < 2 ||
                dbCompareComboBox.getSelectedIndex() == dbMasterComboBox.getSelectedIndex()) {
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
        if (!propertiesCheckBoxMap.get(0).isSelected() &&
                !propertiesCheckBoxMap.get(1).isSelected() &&
                !propertiesCheckBoxMap.get(2).isSelected()) {
            GUIUtilities.displayWarningMessage(bundleString("UnableCompareNoProperties"));
            return;
        }

        progressDialog = new DefaultProgressDialog(bundleString("Executing"));
        SwingWorker worker = new SwingWorker() {
            @Override
            public Object construct() {
                startComparing();
                return null;
            }

            @Override
            public void finished() {
                if (progressDialog != null)
                    progressDialog.dispose();
            }
        };

        worker.start();
        progressDialog.run();

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

        int ret = fileSave.showDialog(null, "Save Script");

        if (ret == JFileChooser.APPROVE_OPTION) {

            File file = fileSave.getSelectedFile();
            String name = file.getAbsoluteFile().toString();

            int dot = name.lastIndexOf(".");
            dot = dot == -1 ? name.length() : dot;

            String fileSavePath = name.substring(0, dot)
                    + fileSave.getFileFilter().getDescription().substring(fileSave.getFileFilter().getDescription().indexOf("(*") + 2,
                    fileSave.getFileFilter().getDescription().lastIndexOf(")"));

            comparer.addToScript("русский текст");

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

    // ---

    public static class FileTypeFilter extends FileFilter {

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

    public static String bundleString(String key) {
        return Bundles.get(ComparerDBPanel.class, key);
    }

}
