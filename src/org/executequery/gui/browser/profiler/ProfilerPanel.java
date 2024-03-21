package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.treetable.CCTNode;
import org.underworldlabs.swing.treetable.ProfilerRowSorter;
import org.underworldlabs.swing.treetable.ProfilerTreeTable;
import org.underworldlabs.swing.treetable.ProfilerTreeTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

/**
 * @author Alexey Kozlov
 */
@SuppressWarnings("unchecked")
public class ProfilerPanel extends JPanel
        implements TabView {

    public final static String TITLE = bundleString("title");
    private final static boolean SORTABLE = true;
    private static final int ACTIVE = 0;
    private static final int PAUSED = ACTIVE + 1;
    private static final int INACTIVE = PAUSED + 1;
    private static final String DEFAULT_TREE = "defaultTreeView";
    private static final String COMPACT_TREE = "compactTreeView";
    private static final String EXTENDED_TREE = "extendedTreeView";


    // --- GUI objects ---

    private JComboBox<?> connectionsComboBox;
    private JComboBox<?> attachmentsComboBox;

    private JRadioButton defaultViewRadioButton;
    private JRadioButton compactViewRadioButton;
    private JRadioButton extendedViewRadioButton;
    private JCheckBox roundValuesCheckBox;

    private ProfilerTreeTable profilerTree;
    private ProfilerTreeTableNode defaultRootTreeNode;
    private ProfilerTreeTableNode compactRootTreeNode;
    private ProfilerTreeTableNode extendedRootTreeNode;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton finishButton;
    private JButton cancelButton;
    private JButton discardButton;

    // ---

    private int sessionId;
    private int currentState;
    private DefaultProfilerExecutor profilerExecutor;
    private TableSelectionCombosGroup combosGroup;
    private List<ProfilerData> oldDataList;

    // ---

    public ProfilerPanel() {
        init();
    }

    public ProfilerPanel(int sessionId, DatabaseConnection connection) {

        init();
        this.sessionId = sessionId;
        this.profilerExecutor = new DefaultProfilerExecutor(connection, null);
        this.connectionsComboBox.setSelectedItem(connection);

        generateTree(false);
        profilerTree.expandPath(profilerTree.getPathForRow(0));
    }

    private void init() {

        // --- attachments comboBox ---

        attachmentsComboBox = WidgetFactory.createComboBox("attachmentsComboBox");

        // --- connections comboBox ---

        connectionsComboBox = WidgetFactory.createComboBox("connectionsComboBox");
        connectionsComboBox.setModel(new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections())));
        connectionsComboBox.addActionListener(e -> refreshAttachments());
        combosGroup = new TableSelectionCombosGroup(connectionsComboBox);

        // --- radioButtons group ---

        defaultViewRadioButton = new JRadioButton(bundleString(DEFAULT_TREE));
        defaultViewRadioButton.addActionListener(e -> updateTreeDisplay());

        compactViewRadioButton = new JRadioButton(bundleString(COMPACT_TREE));
        compactViewRadioButton.addActionListener(e -> updateTreeDisplay());
        compactViewRadioButton.setSelected(true);

        extendedViewRadioButton = new JRadioButton(bundleString(EXTENDED_TREE));
        extendedViewRadioButton.addActionListener(e -> updateTreeDisplay());

        ButtonGroup updateViewButtonGroup = new ButtonGroup();
        updateViewButtonGroup.add(defaultViewRadioButton);
        updateViewButtonGroup.add(compactViewRadioButton);
        updateViewButtonGroup.add(extendedViewRadioButton);

        // --- roundValues CheckBox ---

        roundValuesCheckBox = new JCheckBox(bundleString("roundValuesCheckBox"));
        roundValuesCheckBox.addActionListener(e -> updateTreeDisplay());
        roundValuesCheckBox.setSelected(true);

        // --- profiler tree ---

        defaultRootTreeNode = new ProfilerTreeTableNode(new ProfilerData());
        extendedRootTreeNode = new ProfilerTreeTableNode(new ProfilerData());
        profilerTree = new ProfilerTreeTable(new TreeTableModel(defaultRootTreeNode), SORTABLE, false, new int[4]);
        profilerTree.getColumnModel().getColumn(1).setCellRenderer(new TimeRenderer());
        profilerTree.getColumnModel().getColumn(2).setCellRenderer(new TimeRenderer());
        ((ProfilerRowSorter) profilerTree.getRowSorter()).setComparator(1, (Comparator<Object>) this::compareNodes);
        profilerTree.setDefaultColumnWidth(200);

        // --- buttons ---

        startButton = new JButton(bundleString("Start"));
        startButton.addActionListener(e -> startSession());

        pauseButton = new JButton(bundleString("Pause"));
        pauseButton.addActionListener(e -> pauseSession());

        resumeButton = new JButton(bundleString("Resume"));
        resumeButton.addActionListener(e -> resumeSession());

        finishButton = new JButton(bundleString("Stop"));
        finishButton.addActionListener(e -> finishSession());

        cancelButton = new JButton(bundleString("Cancel"));
        cancelButton.addActionListener(e -> cancelSession());

        discardButton = new JButton(bundleString("Discard"));
        discardButton.addActionListener(e -> discardSession());

        // ---

        arrangeComponents();
        refreshAttachments();
        switchSessionState(INACTIVE);
    }

    private void arrangeComponents() {

        // --- button panel ---

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(0, 0, 5, 0).anchorNorthWest();
        JPanel buttonPanel = new JPanel(new GridBagLayout());

        buttonPanel.add(startButton, gridBagHelper.get());
        buttonPanel.add(pauseButton, gridBagHelper.nextCol().get());
        buttonPanel.add(resumeButton, gridBagHelper.nextCol().get());
        buttonPanel.add(finishButton, gridBagHelper.nextCol().get());
        buttonPanel.add(cancelButton, gridBagHelper.nextCol().get());
        buttonPanel.add(discardButton, gridBagHelper.nextCol().get());

        // --- radioButton panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 0).anchorNorthWest();
        JPanel radioButtonPanel = new JPanel(new GridBagLayout());

        radioButtonPanel.add(compactViewRadioButton, gridBagHelper.get());
        radioButtonPanel.add(defaultViewRadioButton, gridBagHelper.nextCol().get());
        radioButtonPanel.add(extendedViewRadioButton, gridBagHelper.nextCol().get());
        radioButtonPanel.add(roundValuesCheckBox, gridBagHelper.nextCol().get());

        // --- tools panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("Connection"), connectionsComboBox, null, false, false);
        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("Attachment"), attachmentsComboBox, null, false, false);
        toolsPanel.add(buttonPanel, gridBagHelper.nextCol().get());

        // --- resultSet panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest().fillBoth();
        JPanel resultSetPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(profilerTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultSetPanel.add(scrollPane, gridBagHelper.setMaxWeightY().spanX().get());
        resultSetPanel.add(radioButtonPanel, gridBagHelper.setMinWeightY().fillNone().nextRowFirstCol().get());


        // --- profiler panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest();
        JPanel profilerPanel = new JPanel(new GridBagLayout());

        profilerPanel.add(toolsPanel, gridBagHelper.fillBoth().get());
        profilerPanel.add(resultSetPanel, gridBagHelper.nextRowFirstCol().spanX().spanY().get());

        // --- main panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5).anchorNorthWest().fillBoth();

        setLayout(new GridBagLayout());
        add(profilerPanel, gridBagHelper.setMaxWeightX().spanY().get());

    }

    // --- button handlers ---

    private void startSession() {

        clearTree();

        if (!isConnected()) {
            GUIUtilities.displayWarningMessage(bundleString("NotConnected"));
            return;
        }

        profilerExecutor = new DefaultProfilerExecutor(
                combosGroup.getSelectedHost().getDatabaseConnection(),
                ((AttachmentData) Objects.requireNonNull(attachmentsComboBox.getSelectedItem())).id);
        try {

            sessionId = profilerExecutor.startSession();
            if (sessionId == -1) {
                GUIUtilities.displayWarningMessage(bundleString("VersionNotSupported"));
                return;
            }
            switchSessionState(ACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionStart"), e);
        }
    }

    private void pauseSession() {

        if (!isConnected()) {
            GUIUtilities.displayWarningMessage(bundleString("NotConnectedCancel"));
            switchSessionState(INACTIVE);
            return;
        }

        try {
            profilerExecutor.pauseSession();
            switchSessionState(PAUSED);
            generateTree(false);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionPause"), e);
        }
    }

    private void resumeSession() {

        if (!isConnected()) {
            GUIUtilities.displayWarningMessage(bundleString("NotConnectedCancel"));
            switchSessionState(INACTIVE);
            return;
        }

        try {
            profilerExecutor.resumeSession();
            switchSessionState(ACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionResume"), e);
        }
    }

    private void finishSession() {

        if (!isConnected()) {
            GUIUtilities.displayWarningMessage(bundleString("NotConnectedCancel"));
            switchSessionState(INACTIVE);
            return;
        }

        if (currentState == PAUSED)
            resumeSession();

        try {
            profilerExecutor.finishSession();
            switchSessionState(INACTIVE);
            generateTree(false);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionFinish"), e);
        }
    }

    private void cancelSession() {

        clearTree();

        if (!isConnected()) {
            switchSessionState(INACTIVE);
            return;
        }

        if (currentState == PAUSED)
            resumeSession();

        try {
            profilerExecutor.cancelSession();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionCancel"), e);
        }
    }

    private void discardSession() {

        clearTree();

        if (!isConnected()) {
            switchSessionState(INACTIVE);
            return;
        }

        if (currentState == PAUSED)
            resumeSession();

        try {
            profilerExecutor.discardSession();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionDiscard"), e);
        }

    }

    // --- profiler tree processing ---

    private void generateTree(boolean showProfilerProcesses) {

        List<ProfilerData> profilerDataList = profilerExecutor.getProfilerData(sessionId, showProfilerProcesses);
        if (oldDataList != null && profilerDataList.size() == oldDataList.size())
            GUIUtilities.displayWarningMessage(bundleString("NoNewData"));

        if (profilerDataList.isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("NoData"));
            return;
        }

        generateDefaultTree(profilerDataList);
        generateCompactTree(profilerDataList);
        generateExtendedTree(profilerDataList);

        // display tree
        updateTreeDisplay();
        oldDataList = profilerDataList;
    }

    private void generateDefaultTree(List<ProfilerData> profilerDataList) {

        defaultRootTreeNode.removeAllChildren();
        for (ProfilerData data : profilerDataList) {
            if (data.getCallerId() == 0) {
                defaultRootTreeNode.add(new ProfilerTreeTableNode(data));

            } else {
                ProfilerTreeTableNode node = getParenNode(data.getCallerId(), defaultRootTreeNode);
                if (node != null)
                    node.add(new ProfilerTreeTableNode(data));
                else
                    defaultRootTreeNode.add(new ProfilerTreeTableNode(data));
            }
        }

        // set new data to the root node
        long totalTime = Arrays.stream(defaultRootTreeNode.getChildren()).mapToLong(child -> (long) ((ProfilerTreeTableNode) child).getTotalTime()).sum();
        defaultRootTreeNode.setData(new ProfilerData(-1, -1, "Profiler Session [ID: " + sessionId + "]", ProfilerData.ROOT, null, totalTime));

        // add 'self time' nodes
        Arrays.stream(defaultRootTreeNode.getChildren()).forEachOrdered(child -> addNodesSelfTime((ProfilerTreeTableNode) child));

        // add elapsed time percentages
        calculatePercentage(defaultRootTreeNode);

    }

    private void generateCompactTree(List<ProfilerData> profilerDataList) {

        if (defaultRootTreeNode == null)
            generateDefaultTree(profilerDataList);

        compactRootTreeNode = cloneNode(defaultRootTreeNode);
        compressNodes(compactRootTreeNode);

    }

    private void generateExtendedTree(List<ProfilerData> profilerDataList) {

        extendedRootTreeNode.removeAllChildren();
        for (ProfilerData data : profilerDataList) {

            ProfilerTreeTableNode newNode = new ProfilerTreeTableNode(data);
            if (data.getCallerId() != 0) {

                ProfilerTreeTableNode node = getParenNode(data.getCallerId(), extendedRootTreeNode);
                if (node != null) {

                    boolean isAdded = false;
                    Enumeration<CCTNode> children = node.children();
                    while (children.hasMoreElements()) {

                        ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
                        if (child.getData().getProcessName().toLowerCase().contains(data.getProcessName().toLowerCase())) {
                            child.add(newNode);
                            isAdded = true;
                            break;
                        }
                    }
                    if (!isAdded)
                        node.add(newNode);

                } else
                    extendedRootTreeNode.add(newNode);
            } else
                extendedRootTreeNode.add(newNode);

            if (data.getPsqlStats() != null) {
                for (ProfilerData.PsqlLine line : data.getPsqlStats())
                    newNode.add(new ProfilerTreeTableNode(
                            new ProfilerData(-1, data.getId(), line.getString(), ProfilerData.PSQL, line.getTotalTime(), line.getAvgTime(), line.getCallCount())));
            }

        }

        // set new data to the root node
        long totalTime = Arrays.stream(extendedRootTreeNode.getChildren()).mapToLong(child -> (long) ((ProfilerTreeTableNode) child).getTotalTime()).sum();
        extendedRootTreeNode.setData(new ProfilerData(-1, -1, "Profiler Session [ID: " + sessionId + "]", ProfilerData.ROOT, null, totalTime));

        // add elapsed time percentages
        calculatePercentage(extendedRootTreeNode);

    }

    private ProfilerTreeTableNode getParenNode(int id, ProfilerTreeTableNode node) {

        Enumeration<CCTNode> children = node.children();
        while (children.hasMoreElements()) {

            ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
            ProfilerData childData = child.getData();

            if (childData.getId() == id)
                return child;

            ProfilerTreeTableNode returnNode = getParenNode(id, child);
            if (returnNode != null)
                return returnNode;
        }

        return null;
    }

    private void addNodesSelfTime(ProfilerTreeTableNode node) {

        ProfilerData nodeData = node.getData();
        long selfTime = nodeData.getTotalTime();

        Enumeration<CCTNode> children = node.children();
        while (children.hasMoreElements()) {

            ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
            selfTime -= child.getData().getTotalTime();
            if (child.getChildCount() > 0)
                addNodesSelfTime(child);

        }
        if (node.getChildCount() > 0)
            node.add(new ProfilerTreeTableNode(
                    new ProfilerData(-1, nodeData.getCallerId(), bundleString("SelfTime"), ProfilerData.SELF_TIME, null, selfTime)));

    }

    private void calculatePercentage(ProfilerTreeTableNode node) {

        Enumeration<CCTNode> children = node.children();
        long totalTime = (long) node.getTotalTime();

        while (children.hasMoreElements()) {

            ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
            child.getData().setTotalTimePercentage((double) ((long) child.getTotalTime() * 100) / totalTime);
            if (child.getChildCount() > 0)
                calculatePercentage(child);

        }

    }

    private void compressNodes(ProfilerTreeTableNode node) {

        Enumeration<CCTNode> children;
        List<ProfilerTreeTableNode> childrenList = new LinkedList<>();

        children = node.children();
        while (children.hasMoreElements())
            childrenList.add((ProfilerTreeTableNode) children.nextElement());

        for (int i = 0; i < childrenList.size(); i++)
            for (int j = i + 1; j < childrenList.size(); j++)
                if (childrenList.get(i).compareAndMerge(childrenList.get(j)))
                    node.remove(childrenList.get(j));

        children = node.children();
        while (children.hasMoreElements()) {
            ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
            if (child.getChildCount() > 0)
                compressNodes(child);
        }
    }

    public ProfilerTreeTableNode cloneNode(ProfilerTreeTableNode originalNode) {

        ProfilerTreeTableNode copyNode = new ProfilerTreeTableNode(originalNode.getData());

        Enumeration<CCTNode> children = originalNode.children();
        while (children.hasMoreElements()) {
            ProfilerTreeTableNode childCopy = cloneNode((ProfilerTreeTableNode) children.nextElement());
            childCopy.setData(childCopy.getData().getCopy());
            copyNode.add(cloneNode(childCopy));
        }

        return copyNode;
    }

    // ---

    private void updateTreeDisplay() {

        if (compactRootTreeNode != null && extendedRootTreeNode != null && defaultRootTreeNode != null) {

            if (defaultViewRadioButton.isSelected())
                profilerTree.setTreeTableModel(new TreeTableModel(defaultRootTreeNode), SORTABLE);
            else if (compactViewRadioButton.isSelected())
                profilerTree.setTreeTableModel(new TreeTableModel(compactRootTreeNode), SORTABLE);
            else if (extendedViewRadioButton.isSelected())
                profilerTree.setTreeTableModel(new TreeTableModel(extendedRootTreeNode), SORTABLE);

            profilerTree.setDefaultColumnWidth(200);
            profilerTree.getColumnModel().getColumn(1).setCellRenderer(new TimeRenderer());
            profilerTree.getColumnModel().getColumn(2).setCellRenderer(new TimeRenderer());

            ProfilerRowSorter rowSorter = (ProfilerRowSorter) profilerTree.getRowSorter();
            rowSorter.setComparator(1, (Comparator<Object>) this::compareNodes);
            rowSorter.toggleSortOrder(1);
        }
    }

    private void clearTree() {

        if (defaultRootTreeNode != null) {
            defaultRootTreeNode.setData(new ProfilerData(-1, -1, "ROOT NODE", ProfilerData.ROOT, null, 0));
            defaultRootTreeNode.removeAllChildren();
        }

        if (compactRootTreeNode != null) {
            compactRootTreeNode.setData(new ProfilerData(-1, -1, "ROOT NODE", ProfilerData.ROOT, null, 0));
            compactRootTreeNode.removeAllChildren();
        }

        if (extendedRootTreeNode != null) {
            extendedRootTreeNode.setData(new ProfilerData(-1, -1, "ROOT NODE", ProfilerData.ROOT, null, 0));
            extendedRootTreeNode.removeAllChildren();
        }

        oldDataList = null;
        updateTreeDisplay();
    }

    private void refreshAttachments() {

        String query = "SELECT\n" +
                "MON$ATTACHMENT_ID," +
                "MON$REMOTE_ADDRESS\n" +
                "FROM MON$ATTACHMENTS\n" +
                "WHERE (MON$USER NOT CONTAINING 'Garbage Collector')\n" +
                "AND (MON$USER NOT CONTAINING 'Cache Writer')\n" +
                "ORDER BY MON$REMOTE_ADDRESS";

        Vector<AttachmentData> attachments = new Vector<>();
        try {

            DefaultStatementExecutor executor = new DefaultStatementExecutor();
            executor.setDatabaseConnection(combosGroup.getSelectedHost().getDatabaseConnection());
            executor.setKeepAlive(true);
            executor.setCommitMode(false);

            ResultSet rs = executor.execute(query, true).getResultSet();
            while (rs.next()) {

                String id = rs.getNString(1);
                String remoteAddress = rs.getNString(2);

                attachments.add(new AttachmentData(id, remoteAddress));
            }
            executor.getConnection().commit();
            executor.releaseResources();

        } catch (SQLException e) {
            Log.error("Error loading attachments", e);
        }

        attachmentsComboBox.setModel(new DynamicComboBoxModel(attachments));
    }

    private void switchSessionState(int state) {

        currentState = state;
        switch (state) {
            case ACTIVE:
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                finishButton.setEnabled(true);
                cancelButton.setEnabled(true);
                discardButton.setEnabled(true);
                break;
            case PAUSED:
                startButton.setEnabled(false);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                finishButton.setEnabled(true);
                cancelButton.setEnabled(true);
                break;
            case INACTIVE:
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                finishButton.setEnabled(false);
                cancelButton.setEnabled(false);
                break;
        }
    }

    private int compareNodes(Object o1, Object o2) {
        Object[] values1 = (Object[]) o1;
        Object[] values2 = (Object[]) o2;
        return Long.compare((long) values1[0], (long) values2[0]);
    }

    private boolean isConnected() {
        return combosGroup.getSelectedHost().isConnected();
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

    private static String bundleString(String key) {
        return Bundles.get(ProfilerPanel.class, key);
    }

    public static class TreeTableModel extends ProfilerTreeTableModel.Abstract {

        private static final List<String> columnNames = Arrays.asList(
                bundleString("PROCESS-NAME"),
                bundleString("TOTAL-TIME"),
                bundleString("AVERAGE-TIME"),
                bundleString("CALLS-COUNT")
        );
        private static final List<Class<?>> columnClasses =
                Arrays.asList(JTree.class, Object.class, Long.class, Integer.class);

        TreeTableModel(TreeNode root) {
            super(root);
        }

        @Override
        public String getColumnName(int columnIndex) {

            if (columnIndex < 0)
                columnIndex++;

            return columnNames.get(columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {

            if (columnIndex < 0)
                columnIndex++;

            return columnClasses.get(columnIndex);
        }

        @Override
        public int getColumnCount() {
            return columnNames.size();
        }

        @Override
        public Object getValueAt(TreeNode node, int columnIndex) {

            ProfilerTreeTableNode profilerNode = (ProfilerTreeTableNode) node;

            if (columnIndex == 0) return profilerNode.getProcessName();
            if (columnIndex == 1) return new Object[]{profilerNode.getTotalTime(),
                    profilerNode.getTotalTimePercentage()
            };/*.toString() + " [" +
                    String.format("%.2f", (double) ).replace(",", ".") + "%]";*/
            if (columnIndex == 2) return profilerNode.getAvgTime();
            if (columnIndex == 3) return profilerNode.getCallCount();

            return null;
        }

        @Override
        public void setValueAt(Object aValue, TreeNode node, int columnIndex) {
        }

        @Override
        public boolean isCellEditable(TreeNode node, int columnIndex) {
            return false;
        }

    } // TreeTableModel class

    protected class TimeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String formattedString;
            if (value instanceof Object[]) {

                Object[] objects = (Object[]) value;

                String timeValue = roundValuesCheckBox.isSelected() ? roundTime((long) objects[0]) : objects[0].toString() + " ns";
                String percentValue = " [" + String.format("%.2f", (double) objects[1]).replace(",", ".") + "%]";
                formattedString = timeValue + percentValue;

            } else
                formattedString = roundValuesCheckBox.isSelected() ? roundTime((long) value) : value.toString() + " ns";

            setValue(formattedString);
            setHorizontalAlignment(SwingConstants.RIGHT);

            return this;
        }

        private String roundTime(long value) {

            String suffix = "ns";
            while (((suffix.equals("ns") && value > 1000000) || (!suffix.equals("ns") && value > 10000)) && !suffix.contentEquals("h")) {
                switch (suffix) {

                    case "ns":
                        suffix = "ms";
                        value /= 1000000;
                        break;

                    case "ms":
                        suffix = "s";
                        value /= 1000;
                        break;

                    case "s":
                        suffix = "m";
                        value /= 60;
                        break;

                    case "m":
                        suffix = "h";
                        value /= 60;
                        break;

                    default:
                        break;
                }
            }
            return value + " " + suffix;
        }

    } // PercentRenderer class

    protected static class AttachmentData {

        String id;
        String remoteAddress;

        AttachmentData(String id, String remoteAddress) {
            this.id = id;
            this.remoteAddress = remoteAddress;
        }

        @Override
        public String toString() {
            return remoteAddress;
        }

    } // AttachmentData class

}
