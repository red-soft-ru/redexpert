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
import org.underworldlabs.swing.ListSelectionPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.*;

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
                Objects.requireNonNull(RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID))).findAll();

        connectionsComboBox = new JComboBox<>();
        databaseConnections.forEach(item -> connectionsComboBox.addItem(item.getName()));
        connectionsComboBox.addActionListener(e -> refreshTables());

        startValidationButton = new JButton();
        startValidationButton.setText(bundledString("StartValidationButton"));
        startValidationButton.addActionListener(e -> prepareAndValidate());

        hideTimestampsCheckBox = new JCheckBox(bundledString("HideTimestampsCheckBox"));
        hideTimestampsCheckBox.addActionListener(e -> setOutputText());

        tableSelectionPanel = new ListSelectionPanel(bundledString("AvailableTablesLabel"), bundledString("SelectedTablesLabel"));
        tableSelectionPanel.addListSelectionPanelListener(e -> refreshIndexes());

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

        Vector<?> selectedTableValues = tableSelectionPanel.getSelectedValues();
        Vector<?> selectedIndexValues = indexSelectionPanel.getSelectedValues();

        StringBuilder preparedTableParameter = new StringBuilder();
        StringBuilder preparedIndexParameter = new StringBuilder();

        if (selectedTableValues.isEmpty()) {
            GUIUtilities.displayWarningMessage(bundledString("NoTableSelected"));
            return;
        }

        selectedTableValues.forEach(item -> preparedTableParameter.append(MiscUtils.trimEnd(((NamedObject) item).getName())).append("|"));
        preparedTableParameter.deleteCharAt(preparedTableParameter.lastIndexOf("|"));

        if (!selectedIndexValues.isEmpty()) {
            selectedIndexValues.forEach(item -> preparedIndexParameter.append(MiscUtils.trimEnd(((NamedObject) item).getName())).append("|"));
            preparedIndexParameter.deleteCharAt(preparedIndexParameter.lastIndexOf("|"));
        }

        validate(preparedTableParameter.toString(), preparedIndexParameter.toString());
    }

    private void prepareAndValidate(DatabaseConnection selectedConnection, String preparedParameter) {

        // set selected connection
        this.selectedConnection = selectedConnection;
        connectionsComboBox.setSelectedIndex(getConnectionIndex(selectedConnection));

        // set selected objects
        List<String> selectedTables = Arrays.asList(preparedParameter.split("\\|"));
        for (int i = 0; i < tableSelectionPanel.getAvailableValues().size(); i++) {
            NamedObject namedObject = (NamedObject) tableSelectionPanel.getAvailableValues().get(i);
            if (selectedTables.contains(namedObject.getName()))
                tableSelectionPanel.selectOneAction(i);
        }

        validate(preparedParameter, "%");
    }

    private void validate(String preparedTableParameter, String preparedIndexParameter) {

        try (OutputStream outputStream = new TableValidationCommand()
                .onlineTableValidation(selectedConnection, preparedTableParameter, preparedIndexParameter)) {

            originalOutputText = outputStream.toString();
            formattedOutputText = originalOutputText.replaceAll("[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{2} ", "");
            setOutputText();

        } catch (IOException e) {
            e.printStackTrace();
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
