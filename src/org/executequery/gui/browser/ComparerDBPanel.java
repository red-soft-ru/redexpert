package org.executequery.gui.browser;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
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
    private static final String WELCOME_TEXT = "Master database will be modified." + '\n' + "Compare database is an example.";

    private Comparer comparer;
    private List<DatabaseConnection> databaseConnectionList;

    // --- panel components ---

    private JComboBox dbMasterComboBox;
    private JComboBox dbCompareComboBox;
    private JButton compareButton;
    private JButton saveScriptButton;
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

        databaseConnectionList = new ArrayList<DatabaseConnection>();

        // --- buttons defining ---

        compareButton = new JButton();
        compareButton.setText("Compare");
        compareButton.addActionListener(e -> compareDatabase());

        saveScriptButton = new JButton();
        saveScriptButton.setText("Save Script");
        saveScriptButton.addActionListener(e -> saveScript());

        selectAllAttributesButton = new JButton();
        selectAllAttributesButton.setText("Select All");
        selectAllAttributesButton.addActionListener(e -> selectAll("attributes"));

        selectAllPropertiesButton = new JButton();
        selectAllPropertiesButton.setText("Select All");
        selectAllPropertiesButton.addActionListener(e -> selectAll("properties"));

        // --- checkBoxes defining ---

        attributesCheckBoxMap = new HashMap<>();
        for (int i = 0; i < NamedObject.SYSTEM_DOMAIN; i++)
            attributesCheckBoxMap.put(i, new JCheckBox(Bundles.get(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[i])));

        propertiesCheckBoxMap = new HashMap<>();
        propertiesCheckBoxMap.put(0, new JCheckBox("Check for CREATE"));
        propertiesCheckBoxMap.put(1, new JCheckBox("Check for ALTER"));
        propertiesCheckBoxMap.put(2, new JCheckBox("Check for DROP"));
        propertiesCheckBoxMap.put(3, new JCheckBox("Safe type conversion"));
        propertiesCheckBoxMap.put(4, new JCheckBox("Ignore RDB$ elements"));

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

        gridBagHelper.addLabelFieldPair(connectionsPanel, "Master database", dbMasterComboBox, null);
        gridBagHelper.addLabelFieldPair(connectionsPanel, "Compare database", dbCompareComboBox, null);

        // --- attributes panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel attributesPanel = new JPanel(new GridBagLayout());
        JScrollPane attributesPanelWithScrolls = new JScrollPane(attributesPanel);
        attributesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder("Attributes"));

        attributesPanel.add(selectAllAttributesButton, gridBagHelper.nextRowFirstCol().setLabelDefault().anchorNorthWest().get());
        for (JCheckBox checkBox : attributesCheckBoxMap.values())
            attributesPanel.add(checkBox, gridBagHelper.nextRowFirstCol().get());

        // --- properties panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel propertiesPanel = new JPanel(new GridBagLayout());
        JScrollPane propertiesPanelWithScrolls = new JScrollPane(propertiesPanel);
        propertiesPanelWithScrolls.setBorder(BorderFactory.createTitledBorder("Properties"));

        propertiesPanel.add(selectAllPropertiesButton, gridBagHelper.nextRowFirstCol().setLabelDefault().anchorNorthWest().get());
        for (JCheckBox checkBox : propertiesCheckBoxMap.values())
            propertiesPanel.add(checkBox, gridBagHelper.nextRowFirstCol().get());

        // --- tabbed pane ---

        JTabbedPane tabPane = new JTabbedPane();

        tabPane.add("Output", loggingOutputPanel);
        tabPane.add("SQL", sqlTextPanel);

        // --- compare panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        JPanel comparePanel = new JPanel(new GridBagLayout());

        comparePanel.add(connectionsPanel, gridBagHelper.setWidth(2).get());
        comparePanel.add(attributesPanelWithScrolls, gridBagHelper.nextRowFirstCol().setWidth(1).get());
        comparePanel.add(propertiesPanelWithScrolls, gridBagHelper.nextCol().fillHorizontally().get());

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setLabelDefault().setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(comparePanel, gridBagHelper.setMinWeightX().get());
        mainPanel.add(tabPane, gridBagHelper.nextCol().spanX().get());

        add(mainPanel);

    }

    // --- buttons handler ---

    private void compareDatabase() {

        if (databaseConnectionList.size() < 2 || dbCompareComboBox.getSelectedIndex() == dbMasterComboBox.getSelectedIndex()) {
            loggingOutputPanel.append("\nError: Unable to compare");
            return;
        }

        comparer = new Comparer(
                databaseConnectionList.get(dbCompareComboBox.getSelectedIndex()),
                databaseConnectionList.get(dbMasterComboBox.getSelectedIndex()));

        loggingOutputPanel.append("\nStart comparing DBs\n");

        sqlTextPanel.setSQLText("");
        comparer.clearLists();

        String charset = "";
        String dialect = "";

        String query = "select rdb$database.rdb$character_set_name\n"
                + "from rdb$database\n";

        try {
            ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet();

            while (rs.next()) {

                charset = rs.getString(1).trim();
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        query = "select mon$database.mon$sql_dialect\n"
                + "from mon$database\n";

        try {
            ResultSet rs = comparer.getMasterConnection().execute(query, true).getResultSet();

            while (rs.next()) {

                dialect = rs.getString(1).trim();
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

        } finally {
            comparer.getMasterConnection().releaseResources();
        }

        comparer.addToScript("\n/* Setting properties */\n\n");
        comparer.addToScript("set names " + charset + ";\n");
        comparer.addToScript("set sql dialect " + dialect + ";\n");
        comparer.addToScript(
                "connect '" + comparer.getMasterConnection().getDatabaseConnection().getName() +
                " ' user '" + comparer.getMasterConnection().getDatabaseConnection().getUserName() + "' "
                + "password '" + comparer.getMasterConnection().getDatabaseConnection().getUnencryptedPassword() + "';\n");
        comparer.addToScript("set autoddl on;\n\n");

        comparer.setLists("");

        /*
        comparer.dropFKs(jCheckBox12.isSelected());
        logPanel.append("\n============= Foreign keys to drop =============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropPKs(jCheckBox11.isSelected());
        logPanel.append("\n============= Primary keys to drop =============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropUniques(jCheckBox13.isSelected());
        logPanel.append("\n=============== Uniques to drop ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropChecks(jCheckBox14.isSelected());
        logPanel.append("\n=============== Checks to drop  ===============\n\n");
        logPanel.append(comparer.lists);
        */

        for (Integer type : attributesCheckBoxMap.keySet()) {
            comparer.setLists("");
            comparer.createObjects(attributesCheckBoxMap.get(type).isSelected(), type);
            loggingOutputPanel.append(MessageFormat.format("\n============= {0} to create  =============\n\n", Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type])));
            loggingOutputPanel.append(comparer.getLists());
        }

        /*
        comparer.lists = "";
        comparer.alterExceptions(jCheckBox7.isSelected());
        logPanel.append("\n============== Exceptions to alter ==============\n\n");
        logPanel.append(comparer.lists);


        comparer.lists = "";
        comparer.alterGenerators(jCheckBox6.isSelected());
        logPanel.append("\n============== Generators to alter ==============\n\n");
        logPanel.append(comparer.lists);


        comparer.lists = "";
        comparer.alterUDFs(jCheckBox8.isSelected());
        logPanel.append("\n================ UDFs to alter ================\n\n");
        logPanel.append(comparer.lists);


        comparer.lists = "";
        comparer.alterDomains(jCheckBox2.isSelected());
        logPanel.append("\n=============== Domains to alter ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropIndices(jCheckBox10.isSelected());
        logPanel.append("\n================ Indices to drop  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropTriggers(jCheckBox5.isSelected());
        logPanel.append("\n=============== Triggers to drop  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropViews(jCheckBox3.isSelected());
        logPanel.append("\n================ Views to drop ================\n\n");
        logPanel.append(comparer.lists);


        comparer.lists = "";
        comparer.alterTables(jCheckBox1.isSelected());
        logPanel.append("\n================ Tables to alter ================\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropTables(jCheckBox1.isSelected());
        logPanel.append("\n================ Tables to drop ================\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterProcedures(jCheckBox4.isSelected());
        logPanel.append("\n============== Procedures to alter ==============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropProcedures(jCheckBox4.isSelected());
        logPanel.append("\n============== Procedures to drop  ==============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.createViews(jCheckBox3.isSelected());
        comparer.retViews(true);
        logPanel.append("\n================ Views to create  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterViews(jCheckBox3.isSelected());
        logPanel.append("\n================= Views to alter ================\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.createChecks(jCheckBox14.isSelected());
        logPanel.append("\n=============== Checks to create  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterChecks(jCheckBox14.isSelected());
        logPanel.append("\n================ Checks to alter ================\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.createUniques(jCheckBox13.isSelected());
        logPanel.append("\n=============== Uniques to create ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterUniques(jCheckBox13.isSelected());
        logPanel.append("\n================ Uniques to alter  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.createPKs(jCheckBox11.isSelected());
        logPanel.append("\n============= Primary keys to create =============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterPKs(jCheckBox11.isSelected());
        logPanel.append("\n=============  Primary keys to alter ==============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.createFKs(jCheckBox12.isSelected());
        logPanel.append("\n============= Foreign keys to create =============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterFKs(jCheckBox12.isSelected());
        logPanel.append("\n============== Foreign keys to alter  =============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropDomains(jCheckBox2.isSelected());
        logPanel.append("\n=============== Domains to drop ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropUDFs(jCheckBox8.isSelected());
        logPanel.append("\n================ UDFs to drop  ================\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropExceptions(jCheckBox7.isSelected());
        logPanel.append("\n============== Exceptions to drop  ==============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.dropGenerators(jCheckBox6.isSelected());
        logPanel.append("\n============== Generators to drop  ==============\n\n");
        logPanel.append(comparer.lists);

        comparer.lists = "";
        comparer.alterTriggers(jCheckBox5.isSelected());
        logPanel.append("\n=============== Triggers to alter  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.computedField(true);
        comparer.fillViews(true);
        comparer.fillProcedures(true);
        comparer.fillTriggers(true);
        comparer.recreateChecks(true);

        comparer.lists = "";
        comparer.alterIndices(jCheckBox10.isSelected());
        logPanel.append("\n================ Indices to alter  ===============\n\n");
        logPanel.append(comparer.lists);

        comparer.fillIndices(true);

        comparer.lists = "";
        comparer.dropRoles(jCheckBox9.isSelected());
        logPanel.append("\n================ Roles to drop  ================\n\n");
        logPanel.append(comparer.lists);
        */

        for (int i = 0; i < comparer.getScript().size(); i++) {
            //System.out.println(comparer.script.get(i));
            sqlTextPanel.getTextPane().append(comparer.getScript(i));
        }
        /*for (int i = 0; i < comparer.warnings.size(); i++) {
         System.out.println(i + 1 + ": " + comparer.warnings.get(i));
         }*/

    }

    private void saveScript() {

        /*String s = "";

         for (int i = 0; i < comparer.script.size(); i++){
         s = s + comparer.script.get(i);
         }

         try{
         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output_file.txt"), "Cp1251"));
         w.write(s);
         w.flush();
         w.close();}
         catch(IOException ex){}*/

        if (comparer == null) {
            loggingOutputPanel.append("\nNothing to save - script is empty");
            return;
        }
        if (comparer.getScript().isEmpty()) {
            loggingOutputPanel.append("\nNothing to save - script is empty");
            return;
        }

        JFileChooser filesave = new JFileChooser("C:\\");

        //FileNameExtensionFilter filter = new FileNameExtensionFilter("*.*");
        //filesave.setFileFilter(filter);
        FileFilter sqlFilter = new FileTypeFilter(".sql", "SQL files");
        FileFilter txtFilter = new FileTypeFilter(".txt", "Text files");

        filesave.addChoosableFileFilter(sqlFilter);
        filesave.addChoosableFileFilter(txtFilter);
        filesave.setAcceptAllFileFilterUsed(false);

        int ret = filesave.showDialog(null, "Save Script");

        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = filesave.getSelectedFile();

            String text, n = "\n";

            String name = file.getAbsoluteFile().toString();

            int dot = name.lastIndexOf(".");
            dot = dot == -1 ? name.length() : dot;

            String name_ = name.substring(0, dot)
                    + filesave.getFileFilter().getDescription().substring(filesave.getFileFilter().getDescription().indexOf("(*") + 2,
                    filesave.getFileFilter().getDescription().lastIndexOf(")"));
            comparer.addToScript("русский текст");
            try (FileOutputStream path = new FileOutputStream(name_)) {
                for (int i = 0; i < comparer.getScript().size(); i++) {
                    text = comparer.getScript(i);

                    //String str = new String(text.getBytes(), "Cp1251");
                    byte[] buffer = text.getBytes();

                    path.write(buffer, 0, buffer.length);

                    /*byte[] bufferN = n.getBytes();
                     path.write(bufferN, 0, bufferN.length);*/
                }

                loggingOutputPanel.append("\nScript was saved:\n" + "«" + name_ + "»");
            } catch (IOException ex) {

                System.out.println(ex.getMessage());
            }
        }

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

    public class FileTypeFilter extends FileFilter {

        private final String extension;
        private final String description;

        public FileTypeFilter(String extension, String description) {
            this.extension = extension;
            this.description = description;
        }

        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().endsWith(extension);
        }

        public String getDescription() {
            return description + String.format(" (*%s)", extension);
        }
    }

    public String bundleString(String key) {
        return Bundles.get(ComparerDBPanel.class, key);
    }

}
