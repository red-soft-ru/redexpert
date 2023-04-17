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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * @author Alexey Kozlov
 */
public class ProfilerPanel extends JPanel
        implements TabView {

    public final static String TITLE = bundleString("title");

    private static final int ACTIVE = 0;
    private static final int PAUSED = ACTIVE + 1;
    private static final int INACTIVE = PAUSED + 1;

    // --- GUI objects ---

    private JComboBox<?> connectionsComboBox;
    private JCheckBox compactViewCheckBox;

    private JTree profilerTree;
    private DefaultMutableTreeNode rootTreeNode;
    private DefaultMutableTreeNode compactRootTreeNode;

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

        rootTreeNode = new DefaultMutableTreeNode("root");
        profilerTree = new JTree(new DefaultTreeModel(rootTreeNode));
        profilerTree.setCellRenderer(new ProfilerTreeCellRenderer());

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

        rootTreeNode.removeAllChildren();
        for (ProfilerData data : profilerDataList) {
            if (data.getCallerId() == 0) {
                rootTreeNode.add(new DefaultMutableTreeNode(data));

            } else {
                DefaultMutableTreeNode node = getParenNode(data.getCallerId(), rootTreeNode);
                if (node != null)
                    node.add(new DefaultMutableTreeNode(data));
                else
                    rootTreeNode.add(new DefaultMutableTreeNode(data));
            }
        }


        Enumeration<TreeNode> children = rootTreeNode.children();
        while (children.hasMoreElements())
            addNodesSelfTime((DefaultMutableTreeNode) children.nextElement());

        compactRootTreeNode = cloneNode(rootTreeNode);
        compressNodes(compactRootTreeNode);

        updateTreeDisplay();
    }

    private DefaultMutableTreeNode getParenNode(int id, DefaultMutableTreeNode node) {

        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {

            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            ProfilerData childData = (ProfilerData) child.getUserObject();

            if (childData.getId() == id)
                return child;

            DefaultMutableTreeNode returnNode = getParenNode(id, child);
            if (returnNode != null)
                return returnNode;
        }

        return null;
    }

    private void addNodesSelfTime(DefaultMutableTreeNode node) {

        ProfilerData nodeData = (ProfilerData) node.getUserObject();
        long selfTime = nodeData.getTotalTime();

        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {

            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            selfTime -= ((ProfilerData) child.getUserObject()).getTotalTime();
            if (child.getChildCount() > 0)
                addNodesSelfTime(child);

        }
        if (node.getChildCount() > 0)
            node.add(new DefaultMutableTreeNode(new ProfilerData(-1, nodeData.getCallerId(), "Self Time", selfTime)));

    }

    private DefaultMutableTreeNode compressNodes(DefaultMutableTreeNode node) {

        List<DefaultMutableTreeNode> childrenList = new LinkedList<>();

        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {

            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getChildCount() > 0)
                childrenList.add(compressNodes(child));
            else
                childrenList.add(child);
        }

        for (int i = 0, activeNodeIndex = 0; i < childrenList.size() - 1; i++) {

            ProfilerData data_1 = (ProfilerData) childrenList.get(activeNodeIndex).getUserObject();
            ProfilerData data_2 = (ProfilerData) childrenList.get(i + 1).getUserObject();

            if (data_1.compareAndMergeData(data_2))
                node.remove(childrenList.get(i + 1));
            else
                activeNodeIndex = i + 1;

        }

        return node;
    }

    public DefaultMutableTreeNode cloneNode(DefaultMutableTreeNode originalNode) {

        DefaultMutableTreeNode copyNode = new DefaultMutableTreeNode(originalNode.getUserObject());

        Enumeration<TreeNode> children = originalNode.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode childCopy = cloneNode((DefaultMutableTreeNode) children.nextElement());
            childCopy.setUserObject(((ProfilerData) childCopy.getUserObject()).getCopy());
            copyNode.add(cloneNode(childCopy));
        }

        return copyNode;
    }

    // ---

    private void updateTreeDisplay() {
        profilerTree.setModel(new DefaultTreeModel(
                compactViewCheckBox.isSelected() ? compactRootTreeNode : rootTreeNode));
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

}
