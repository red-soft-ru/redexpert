package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.treetable.CCTNode;
import org.underworldlabs.swing.treetable.ProfilerTreeTable;
import org.underworldlabs.swing.treetable.ProfilerTreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * @author Alexey Kozlov
 */
public class ProfilerPanel extends JPanel
        implements TabView {

    public final static String TITLE = bundleString("title");
    private final static boolean SORTABLE = true;
    private static final int ACTIVE = 0;
    private static final int PAUSED = ACTIVE + 1;
    private static final int INACTIVE = PAUSED + 1;

    // --- GUI objects ---

    private JComboBox<?> connectionsComboBox;
    private JCheckBox compactViewCheckBox;

    private ProfilerTreeTable profilerTree;
    private ProfilerTreeTableNode fullRootTreeNode;
    private ProfilerTreeTableNode compactRootTreeNode;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton finishButton;
    private JButton cancelButton;
    private JButton discardButton;

    // ---

    private int sessionId;
    private DefaultProfilerExecutor profilerExecutor;
    private TableSelectionCombosGroup combosGroup;

    // ---

    public ProfilerPanel() {
        init();
    }

    public ProfilerPanel(int sessionId, DatabaseConnection connection) {

        init();
        this.sessionId = sessionId;
        this.profilerExecutor = new DefaultProfilerExecutor(connection);
        generateTree(false);

    }

    private void init() {

        connectionsComboBox = WidgetFactory.createComboBox();
        connectionsComboBox.setModel(
                new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections())));
        combosGroup = new TableSelectionCombosGroup(connectionsComboBox);

        compactViewCheckBox = new JCheckBox(bundleString("compactViewCheckBox"));
        compactViewCheckBox.addActionListener(e -> updateTreeDisplay());
        compactViewCheckBox.setSelected(true);

        fullRootTreeNode = new ProfilerTreeTableNode(new ProfilerData());
        profilerTree = new ProfilerTreeTable(
                new TreeTableModel(fullRootTreeNode), SORTABLE, false, new int[4]);

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

        arrangeComponents();
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

        // --- tools panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 0).anchorNorthWest().fillHorizontally();
        JPanel toolsPanel = new JPanel(new GridBagLayout());

        gridBagHelper.addLabelFieldPair(toolsPanel,
                bundleString("Connection"), connectionsComboBox, null, false, false);
        toolsPanel.add(buttonPanel, gridBagHelper.nextCol().get());
        toolsPanel.add(compactViewCheckBox, gridBagHelper.nextCol().get());

        // --- resultSet panel ---

        gridBagHelper = new GridBagHelper();
        gridBagHelper.anchorNorthWest().fillBoth();
        JPanel resultSetPanel = new JPanel(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(profilerTree,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        resultSetPanel.add(scrollPane, gridBagHelper.spanX().spanY().get());

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

        profilerExecutor = new DefaultProfilerExecutor(combosGroup.getSelectedHost().getDatabaseConnection());
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
        try {

            profilerExecutor.pauseSession();
            switchSessionState(PAUSED);
            generateTree(false);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionPause"), e);
        }
    }

    private void resumeSession() {
        try {

            profilerExecutor.resumeSession();
            switchSessionState(ACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionResume"), e);
        }
    }

    private void finishSession() {
        try {

            profilerExecutor.finishSession();
            switchSessionState(INACTIVE);
            generateTree(false);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionFinish"), e);
        }
    }

    private void cancelSession() {
        try {

            profilerExecutor.cancelSession();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionCancel"), e);
        }
    }

    private void discardSession() {
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
        if (profilerDataList.isEmpty()) {
            GUIUtilities.displayWarningMessage(bundleString("ErrorUpdatingData"));
            return;
        }

        fullRootTreeNode.removeAllChildren();
        for (ProfilerData data : profilerDataList) {
            if (data.getCallerId() == 0) {
                fullRootTreeNode.add(new ProfilerTreeTableNode(data));

            } else {
                ProfilerTreeTableNode node = getParenNode(data.getCallerId(), fullRootTreeNode);
                if (node != null)
                    node.add(new ProfilerTreeTableNode(data));
                else
                    fullRootTreeNode.add(new ProfilerTreeTableNode(data));
            }
        }


        Enumeration<CCTNode> children = fullRootTreeNode.children();
        while (children.hasMoreElements())
            addNodesSelfTime((ProfilerTreeTableNode) children.nextElement());

        children = fullRootTreeNode.children();
        while (children.hasMoreElements())
            calculatePercentage((ProfilerTreeTableNode) children.nextElement());

        compactRootTreeNode = cloneNode(fullRootTreeNode);
        compressNodes(compactRootTreeNode);

        updateTreeDisplay();
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
                    new ProfilerData(-1, nodeData.getCallerId(), bundleString("SelfTime"), "SELF_TIME", selfTime)));

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

    private ProfilerTreeTableNode compressNodes(ProfilerTreeTableNode node) {

        List<ProfilerTreeTableNode> childrenList = new LinkedList<>();

        Enumeration<CCTNode> children = node.children();
        while (children.hasMoreElements()) {

            ProfilerTreeTableNode child = (ProfilerTreeTableNode) children.nextElement();
            if (child.getChildCount() > 0)
                childrenList.add(compressNodes(child));
            else
                childrenList.add(child);
        }

        for (int i = 0, activeNodeIndex = 0; i < childrenList.size() - 1; i++) {

            ProfilerData data_1 = childrenList.get(activeNodeIndex).getData();
            ProfilerData data_2 = childrenList.get(i + 1).getData();

            if (data_1.compareAndMergeData(data_2))
                node.remove(childrenList.get(i + 1));
            else
                activeNodeIndex = i + 1;

        }

        return node;
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
        profilerTree.setTreeTableModel(new TreeTableModel(
                compactViewCheckBox.isSelected() ? compactRootTreeNode : fullRootTreeNode), SORTABLE);
        profilerTree.setDefaultColumnWidth(200);
    }

    private void switchSessionState(int state) {
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


    /**
     * Profiler Tree Table Model class
     */
    public static class TreeTableModel extends ProfilerTreeTableModel.Abstract {

        private static final List<String> columnNames = Arrays.asList(
                bundleString("PROCESS-NAME"),
                bundleString("TOTAL-TIME"),
                bundleString("AVERAGE-TIME"),
                bundleString("CALLS-COUNT")
        );
        private static final List<Class<?>> columnClasses =
                Arrays.asList(JTree.class, String.class, Long.class, Integer.class);

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
            if (columnIndex == 1) return profilerNode.getTotalTime().toString() + " [" +
                    String.format("%.2f", (double) profilerNode.getTotalTimePercentage()).replace(",", ".") + "%]";
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

    }

}
