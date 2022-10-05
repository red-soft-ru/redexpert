package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.LoggingOutputPanel;
import org.executequery.gui.browser.comparer.Comparer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.layouts.GridBagHelper;

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

    public static final String TITLE = Bundles.get(ComparerDBPanel.class, "ComparerDB");
    public static final String FRAME_ICON = "ComparerDB_16.png";
    private static final String WELCOME_TEXT =
            "Master database will be modified." +
            "\nCompare database is an example.";
    private static final String TABLESPACE_NOT_INCLUDED =
            "\nAt least one of the connection use RDB version below 4.0" +
            "\nTablespace will not be included in the comparison";

    private Comparer comparer;
    private List<DatabaseConnection> databaseConnectionList;

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

    private Map<Integer, JCheckBox> attributesCheckBoxMap;
    private Map<Integer, JCheckBox> propertiesCheckBoxMap;

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

        // --- buttons defining ---

        compareButton = new JButton();
        compareButton.setText("Compare");
        compareButton.addActionListener(e -> compareDatabase());

        saveScriptButton = new JButton();
        saveScriptButton.setText("Save Script");
        saveScriptButton.addActionListener(e -> saveScript());

        executeScriptButton = new JButton();
        executeScriptButton.setText("Execute Script");
        executeScriptButton.addActionListener(e -> executeScript());

        selectAllAttributesButton = new JButton();
        selectAllAttributesButton.setText("Select All");
        selectAllAttributesButton.addActionListener(e -> selectAll("attributes"));

        selectAllPropertiesButton = new JButton();
        selectAllPropertiesButton.setText("Select All");
        selectAllPropertiesButton.addActionListener(e -> selectAll("properties"));

        // --- attributes checkBox defining ---

        attributesCheckBoxMap = new HashMap<>();
        for (int i = 0; i < NamedObject.SYSTEM_DOMAIN; i++)
            attributesCheckBoxMap.put(i, new JCheckBox(Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[i])));

        // --- properties checkBox defining ---

        propertiesCheckBoxMap = new HashMap<>();
        propertiesCheckBoxMap.put(0, new JCheckBox("Check for CREATE"));
        propertiesCheckBoxMap.put(1, new JCheckBox("Check for ALTER"));
        propertiesCheckBoxMap.put(2, new JCheckBox("Check for DROP"));
//        propertiesCheckBoxMap.put(3, new JCheckBox("Safe type conversion"));
//        propertiesCheckBoxMap.put(4, new JCheckBox("Create tables without constraints"));

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
        connectionsPanel.setBorder(BorderFactory.createTitledBorder("Connections"));

        gridBagHelper.addLabelFieldPair(connectionsPanel,
                "Master database:", dbMasterComboBox, null);
        gridBagHelper.addLabelFieldPair(connectionsPanel,
                "Compare database:", dbCompareComboBox, null);
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
        attributesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder("Attributes"));
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
        propertiesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder("Properties"));
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

        tabPane.add("Output", loggingOutputPanel);
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

    // --- buttons handlers ---

    private void compareDatabase() {

        if (databaseConnectionList.size() < 2 ||
                dbCompareComboBox.getSelectedIndex() == dbMasterComboBox.getSelectedIndex()) {
            loggingOutputPanel.appendError("Error: Unable to compare");
            return;
        }
        for (int i = 0; i < NamedObject.SYSTEM_DOMAIN; i++) {
            if (attributesCheckBoxMap.get(i).isSelected())
                break;
            if (i == NamedObject.SYSTEM_DOMAIN - 1) {
                loggingOutputPanel.appendError("Error: No attributes for comparing selected");
                return;
            }
        }
        if (!propertiesCheckBoxMap.get(0).isSelected() &&
                !propertiesCheckBoxMap.get(1).isSelected() &&
                !propertiesCheckBoxMap.get(2).isSelected()) {
            loggingOutputPanel.appendError("Error: No properties for comparing selected");
            return;
        }

        comparer = new Comparer(
                databaseConnectionList.get(dbCompareComboBox.getSelectedIndex()),
                databaseConnectionList.get(dbMasterComboBox.getSelectedIndex()));

        loggingOutputPanel.clear();
        loggingOutputPanel.append(WELCOME_TEXT);
        loggingOutputPanel.appendAction("Start comparing DBs");
        sqlTextPanel.setSQLText("");
        comparer.clearLists();

        try {
            if (new DefaultDatabaseHost(databaseConnectionList.get(dbCompareComboBox.getSelectedIndex())).getDatabaseMajorVersion() < 4 ||
                    new DefaultDatabaseHost(databaseConnectionList.get(dbMasterComboBox.getSelectedIndex())).getDatabaseMajorVersion() < 4) {
                attributesCheckBoxMap.get(Arrays.asList(NamedObject.META_TYPES_FOR_BUNDLE).indexOf("TABLESPACE")).setSelected(false);
                loggingOutputPanel.append(TABLESPACE_NOT_INCLUDED);
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

        comparer.addToScript("\n/* Setting properties */\n\n");
        comparer.addToScript("SET NAMES " + charset + ";\n");
        comparer.addToScript("SET SQL DIALECT " + dialect + ";\n");
        comparer.addToScript(
                "CONNECT '" + comparer.getMasterConnection().getDatabaseConnection().getName() +
                        " ' USER '" + comparer.getMasterConnection().getDatabaseConnection().getUserName() + "' "
                        + "PASSWORD '" + comparer.getMasterConnection().getDatabaseConnection().getUnencryptedPassword() + "';\n");
        comparer.addToScript("SET AUTO DDL ON;\n");

        if (propertiesCheckBoxMap.get(0).isSelected()) {
            for (Integer type : attributesCheckBoxMap.keySet()) {
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

        if (propertiesCheckBoxMap.get(1).isSelected()) {
            for (Integer type : attributesCheckBoxMap.keySet()) {
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

        if (propertiesCheckBoxMap.get(2).isSelected()) {
            for (Integer type : attributesCheckBoxMap.keySet()) {
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

        for (int i = 0; i < comparer.getScript().size(); i++)
            sqlTextPanel.getTextPane().append(comparer.getScript(i));

    }

    private void saveScript() {

        if (comparer == null || comparer.getScript().isEmpty()) {
            loggingOutputPanel.appendError("Error: nothing to save (script is empty)");
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

                loggingOutputPanel.appendAction("Script was saved successfully");
                loggingOutputPanel.append("Saved to: " + fileSavePath);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void executeScript() {

        if (comparer == null || comparer.getScript().isEmpty()) {
            loggingOutputPanel.appendError("Error: nothing to execute (script is empty)");
            return;
        }

        loggingOutputPanel.appendError("This function not implemented yet");
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
