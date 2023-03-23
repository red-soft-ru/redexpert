package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.TableSelectionCombosGroup;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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

    // --- profiler commands ---

    private static final String START_SESSION =
            "SELECT RDB$PROFILER.START_SESSION('%s') FROM RDB$DATABASE";
    private static final String PAUSE_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.PAUSE_SESSION(TRUE)";
    private static final String RESUME_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.RESUME_SESSION";
    private static final String FINISH_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.FINISH_SESSION(TRUE)";
    private static final String CANCEL_SESSION =
            "EXECUTE PROCEDURE RDB$PROFILER.CANCEL_SESSION";
    private static final String DISCARD =
            "EXECUTE PROCEDURE RDB$PROFILER.DISCARD";

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
    private DatabaseConnection connection;
    private DefaultStatementExecutor executor;
    private TableSelectionCombosGroup combosGroup;

    // ---

    public ProfilerPanel() {
        init();
    }

    private void init() {

        executor = new DefaultStatementExecutor();
        executor.setCommitMode(false);
        executor.setKeepAlive(true);

        connectionsComboBox = WidgetFactory.createComboBox();
        connectionsComboBox.setModel(
                new DynamicComboBoxModel(new Vector<>(ConnectionManager.getActiveConnections()))
        );
        connectionsComboBox.addActionListener(e -> connectionChange());
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

        connectionChange();
        if (!isVersionSupported(connection)) {
            GUIUtilities.displayWarningMessage(bundleString("VersionNotSupported"));
            return;
        }

        executor.setDatabaseConnection(connection);
        try {

            String query = String.format(START_SESSION, connection.getName() + "_session");
            ResultSet resultSet = executor.execute(query, true).getResultSet();
            sessionId = resultSet.next() ? resultSet.getInt(1) : -1;
            executor.getConnection().commit();
            executor.releaseResources();
            switchSessionState(ACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionStart"), e);
        }
    }

    private void pauseSession() {
        try {

            executor.execute(PAUSE_SESSION, true);
            executor.getConnection().commit();
            executor.releaseResources();
            switchSessionState(PAUSED);
            generateTree();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionPause"), e);
        }
    }

    private void resumeSession() {
        try {

            executor.execute(RESUME_SESSION, true);
            executor.getConnection().commit();
            executor.releaseResources();
            switchSessionState(ACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionResume"), e);
        }
    }

    private void finishSession() {
        try {

            executor.execute(FINISH_SESSION, true);
            executor.getConnection().commit();
            executor.releaseResources();
            switchSessionState(INACTIVE);
            generateTree();

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionFinish"), e);
        }
    }

    private void cancelSession() {
        try {

            executor.execute(CANCEL_SESSION, true);
            executor.getConnection().commit();
            executor.releaseResources();
            switchSessionState(INACTIVE);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog(bundleString("ErrorSessionCancel"), e);
        }
    }

    private void discardSession() {
        try {

            executor.execute(DISCARD, true);
            executor.getConnection().commit();
            executor.releaseResources();
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

                if (callerId == 0)
                    rootTreeNode.add(new DefaultMutableTreeNode(data));

                else {
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

    private boolean isVersionSupported(DatabaseConnection connection) {

        boolean isSupported;

        try {

            DefaultDatabaseHost dbHost = new DefaultDatabaseHost(connection);
            isSupported = dbHost.getDatabaseMajorVersion() > 4;
            dbHost.close();

        } catch (SQLException e) {
            e.printStackTrace();
            isSupported = false;
        }

        return isSupported;
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

    private void connectionChange() {
        connection = combosGroup.getSelectedHost().getDatabaseConnection();
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

    static class ProfilerData {

        private final int id;
        private final String label;
        private final long totalTime;

        public ProfilerData(int id, String label, long totalTime) {
            this.id = id;
            this.label = label;
            this.totalTime = totalTime;
        }

        public ProfilerData(int id, String packageName, String routineName, long totalTime) {
            this.id = id;
            this.label = (packageName != null) ? (packageName.trim() + "::" + routineName.trim()) : routineName.trim();
            this.totalTime = totalTime;
        }

        public String getLabel() {
            return label;
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
        public Component getTreeCellRendererComponent (
                JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof ProfilerData) {

                ProfilerData data = (ProfilerData) userObject;
                String record = "[" + data.getTotalTime() + "ms] " + data.getLabel();
                setText(record);
            }

            return this;
        }
    }

}
