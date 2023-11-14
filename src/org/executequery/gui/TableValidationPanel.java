package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.executequery.actions.databasecommands.TableValidationCommand;
import org.executequery.base.TabView;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.BackgroundProgressDialog;
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexey Kozlov
 */
public class TableValidationPanel extends JPanel implements TabView {

    public static final String TITLE = bundledString("Title");
    public static final String FRAME_ICON = "JDBCDriver16.png";

    private List<DatabaseConnection> databaseConnections;
    private DatabaseConnection selectedConnection;

    private String originalOutputText;
    private String formattedOutputText;

    // --- GUI components ---

    private JComboBox<String> connectionsComboBox;
    private JButton startValidationButton;
    private ListSelectionPanel tableSelectionPanel;
    private ListSelectionPanel indexSelectionPanel;
    private LoggingOutputPanel loggingOutputPanel;
    private JCheckBox hideTimestampsCheckBox;

    // ---

    public TableValidationPanel() {
        init();
        refreshTables();
    }

    public TableValidationPanel(DatabaseConnection selectedConnection, String preparedParameter) {
        init();
        prepareAndValidate(selectedConnection, preparedParameter);
    }

    private void init() {

        originalOutputText = "";
        formattedOutputText = "";

        databaseConnections = ((DatabaseConnectionRepository)
                Objects.requireNonNull(RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)))
                .findAll().stream()
                .sorted((o1, o2) -> Boolean.compare(o1.isConnected(), o2.isConnected()) * -1)
                .collect(Collectors.toList());

        connectionsComboBox = new JComboBox<>();
        databaseConnections.forEach(item -> connectionsComboBox.addItem(item.getName()));
        connectionsComboBox.addActionListener(e -> refreshTables());

        startValidationButton = new JButton();
        startValidationButton.setText(bundledString("StartValidationButton"));
        startValidationButton.addActionListener(e -> prepareAndValidate());

        hideTimestampsCheckBox = new JCheckBox(bundledString("HideTimestampsCheckBox"));
        hideTimestampsCheckBox.addActionListener(e -> setOutputText());

        tableSelectionPanel = new ListSelectionPanel(bundledString("AvailableTablesLabel"), bundledString("SelectedTablesLabel"));

        indexSelectionPanel = new ListSelectionPanel(bundledString("AvailableIndexLabel"), bundledString("SelectedIndexLabel"));
        loggingOutputPanel = new LoggingOutputPanel();

        arrangeComponents();
    }

    private void arrangeComponents() {

        GridBagHelper gridBagHelper;

        // --- tools panel ---

        JPanel toolsPanel = new JPanel(new GridBagLayout());
        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillHorizontally();

        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundledString("Connection"), connectionsComboBox, null, false, false);
        toolsPanel.add(startValidationButton, gridBagHelper.nextCol().setMinWeightX().get());

        // --- tabbed pane ---

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(bundledString("Tables"), tableSelectionPanel);
        tabbedPane.add(bundledString("Indexes"), indexSelectionPanel);
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1)
                refreshIndexes();
        });

        // --- output panel ---

        JPanel outputPanel = new JPanel(new GridBagLayout());
        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        outputPanel.add(loggingOutputPanel, gridBagHelper.setMaxWeightY().spanX().get());
        outputPanel.add(hideTimestampsCheckBox, gridBagHelper.nextRowFirstCol().setMinWeightY().fillNone().get());

        // --- split panel ---

        JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPanel.setTopComponent(tabbedPane);
        splitPanel.setBottomComponent(outputPanel);
        splitPanel.setDividerLocation(0.5);

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());
        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        mainPanel.add(toolsPanel, gridBagHelper.spanX().get());
        mainPanel.add(splitPanel, gridBagHelper.nextRowFirstCol().spanY().get());

        // ---

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void prepareAndValidate() {

        Vector<NamedObject> tableInclVector = tableSelectionPanel.getSelectedValues();
        Vector<NamedObject> indexInclVector = indexSelectionPanel.getSelectedValues();
        Vector<NamedObject> tableExclVector = tableSelectionPanel.getAvailableValues();
        Vector<NamedObject> indexExclVector = indexSelectionPanel.getAvailableValues();

        tableExclVector.removeAll(tableInclVector);
        if (indexInclVector != null && indexExclVector != null)
            indexExclVector.removeAll(indexInclVector);

        StringBuilder tableIncl = new StringBuilder();
        StringBuilder indexIncl = new StringBuilder();
        StringBuilder tableExcl = new StringBuilder();
        StringBuilder indexExcl = new StringBuilder();

        if (tableInclVector.isEmpty()) {
            GUIUtilities.displayWarningMessage(bundledString("NoTableSelected"));
            return;
        }

        if (tableExclVector.isEmpty()) {
            tableIncl.append("%");
        } else if (tableInclVector.size() < tableExclVector.size()) {
            tableInclVector.forEach(item -> tableIncl.append(MiscUtils.trimEnd(item.getName())).append("|"));
            tableIncl.deleteCharAt(tableIncl.lastIndexOf("|"));
        } else {
            tableExclVector.forEach(item -> tableExcl.append(MiscUtils.trimEnd(item.getName())).append("|"));
            tableExcl.deleteCharAt(tableExcl.lastIndexOf("|"));
        }

        if (indexExclVector == null || indexInclVector == null || indexExclVector.isEmpty()) {
            indexIncl.append("%");
        } else if (indexInclVector.size() < indexExclVector.size()) {
            indexInclVector.forEach(item -> indexIncl.append(MiscUtils.trimEnd(item.getName())).append("|"));
            indexIncl.deleteCharAt(indexIncl.lastIndexOf("|"));
        } else {
            indexExclVector.forEach(item -> indexExcl.append(MiscUtils.trimEnd(item.getName())).append("|"));
            indexExcl.deleteCharAt(indexExcl.lastIndexOf("|"));
        }

        validate(
                tableIncl.length() > 0 ? tableIncl.toString() : null,
                indexIncl.length() > 0 ? indexIncl.toString() : null,
                tableExcl.length() > 0 ? tableExcl.toString() : null,
                indexExcl.length() > 0 ? indexExcl.toString() : null
        );
    }

    private void prepareAndValidate(DatabaseConnection selectedConnection, String tableIncl) {

        // set selected connection
        this.selectedConnection = selectedConnection;
        connectionsComboBox.setSelectedIndex(getConnectionIndex(selectedConnection));

        // set selected objects to display
        List<String> selectedTables = Arrays.asList(tableIncl.split("\\|"));
        for (int i = 0; i < tableSelectionPanel.getAvailableValues().size(); i++) {
            NamedObject namedObject = (NamedObject) tableSelectionPanel.getAvailableValues().get(i);
            if (selectedTables.contains(namedObject.getName()))
                tableSelectionPanel.selectOneAction(i);
        }

        validate(tableIncl, "%", null, null);
    }

    private void validate(String tableIncl, String indexIncl, String tableExcl, String indexExcl) {

        try (OutputStream outputStream = new TableValidationCommand()
                .onlineTableValidation(selectedConnection, tableIncl, indexIncl, tableExcl, indexExcl)) {

            originalOutputText = outputStream.toString();
            formattedOutputText = originalOutputText.replaceAll("[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2} ", "");
            setOutputText();

        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void setOutputText() {
        loggingOutputPanel.clear();
        loggingOutputPanel.append(hideTimestampsCheckBox.isSelected() ? formattedOutputText : originalOutputText);
    }

    private void refreshTables() {

        if (tableSelectionPanel == null)
            return;

        selectedConnection = databaseConnections.get(connectionsComboBox.getSelectedIndex());
        try {
            if (!selectedConnection.isConnected())
                ConnectionManager.createDataSource(selectedConnection);

        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(bundledString("UnableCreateConnections"), e);
            return;
        }

        List<NamedObject> connactionTableList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(selectedConnection).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[1]);

        tableSelectionPanel.createAvailableList(connactionTableList);
    }

    private void refreshIndexes() {

        List<DefaultDatabaseIndex> indexesList = new ArrayList<>();
        for (Object object : tableSelectionPanel.getSelectedValues()) {
            DefaultDatabaseTable namedObject = (DefaultDatabaseTable) object;
            indexesList.addAll(namedObject.getIndexes());
        }

        indexSelectionPanel.createAvailableList(indexesList);
        indexSelectionPanel.selectAllAction();
    }

    private int getConnectionIndex(DatabaseConnection connection) {

        int index = 0;
        for (DatabaseConnection dc : databaseConnections)
            if (!dc.getName().equals(connection.getName()))
                index++;
            else break;

        return index;
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

    private static String bundledString(String key) {
        return Bundles.get(TableValidationPanel.class, key);
    }

}
