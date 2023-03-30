package org.executequery.gui.browser;

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
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
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

    private JTree profilerTree;
    private DefaultMutableTreeNode rootTreeNode;

    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton finishButton;
    private JButton cancelButton;
    private JButton discardButton;

    // ---

    private int sessionId;
    private DefaultProfilerExecutor profilerExecutor;
    private DefaultStatementExecutor executor;
    private TableSelectionCombosGroup combosGroup;

    // ---

    public ProfilerPanel() {
        init();
    }

    public ProfilerPanel(int sessionId, DatabaseConnection connection) {

        init();
        this.sessionId = sessionId;
        executor.setDatabaseConnection(connection);
        generateTree();

    }

    private void init() {

        executor = new DefaultStatementExecutor();
        executor.setKeepAlive(true);
        executor.setCommitMode(false);

        connectionsComboBox = WidgetFactory.createComboBox();
        connectionsComboBox.setModel(
                new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections())));
        combosGroup = new TableSelectionCombosGroup(connectionsComboBox);

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
        executor.setDatabaseConnection(combosGroup.getSelectedHost().getDatabaseConnection());

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
            generateTree();

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
            generateTree();

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

    // ---

    private void generateTree() {

        ResultSet rs = getProfilerData();

        if (rs == null) {
            GUIUtilities.displayWarningMessage(bundleString("ErrorUpdatingData"));
            return;
        }

        try {

            rootTreeNode.removeAllChildren();
            while (rs.next()) {

                int id = rs.getInt(1);
                int callerId = rs.getInt(2);
                String packageName = rs.getNString(3);
                String routineName = rs.getNString(4);
                String sqlText = rs.getNString(5);
                long totalTime = rs.getLong(6);

                ProfilerData data = sqlText != null ?
                        new ProfilerData(id, sqlText, totalTime) :
                        new ProfilerData(id, packageName, routineName, totalTime);

                if (callerId == 0) {
                    rootTreeNode.add(new DefaultMutableTreeNode(data));

                } else {
                    DefaultMutableTreeNode node = getParenNode(callerId, rootTreeNode);
                    if (node != null)
                        node.add(new DefaultMutableTreeNode(data));
                    else
                        rootTreeNode.add(new DefaultMutableTreeNode(data));
                }
            }

            profilerTree.setModel(new DefaultTreeModel(rootTreeNode));
            executor.getConnection().commit();

        } catch (SQLException | NullPointerException e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorUpdatingData"), e);
        }

        executor.releaseResources();
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

    private ResultSet getProfilerData() {

        String query = "SELECT DISTINCT\n" +
                "REQ.REQUEST_ID,\n" +
                "REQ.CALLER_REQUEST_ID CALLER_ID,\n" +
                "STA.PACKAGE_NAME,\n" +
                "STA.ROUTINE_NAME,\n" +
                "STA.SQL_TEXT,\n" +
                "REQ.TOTAL_ELAPSED_TIME TOTAL_TIME\n" +
                "FROM PLG$PROF_STATEMENT_STATS_VIEW STA\n" +
                "JOIN PLG$PROF_REQUESTS REQ ON\n" +
                "STA.PROFILE_ID = REQ.PROFILE_ID AND STA.STATEMENT_ID = REQ.STATEMENT_ID\n" +
                "WHERE STA.PROFILE_ID = '" + sessionId + "'\n" +
                "ORDER BY REQ.CALLER_REQUEST_ID;";

        try {
            return executor.execute(query, true).getResultSet();

        } catch (SQLException e) {
            Log.error("Error loading profiler data", e);
            return null;
        }
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
     * A class describing the data that is displayed in the node of the <code>JTree</code>.
     *
     * @author Alexey Kozlov
     */
    static class ProfilerData {

        private final int id;
        private final String processName;
        private final long totalTime;

        public ProfilerData(int id, String processName, long totalTime) {
            this.id = id;
            this.processName = processName;
            this.totalTime = totalTime;
        }

        public ProfilerData(int id, String packageName, String routineName, long totalTime) {
            this.id = id;
            this.processName = (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim();
            this.totalTime = totalTime;
        }

        public String getProcessName() {
            return processName;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public int getId() {
            return id;
        }

    }

    static class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof ProfilerData) {

                ProfilerData data = (ProfilerData) userObject;
                String record = "[" + data.getTotalTime() + "ms] " + data.getProcessName();
                setText(record);
            }

            return this;
        }
    }

}
