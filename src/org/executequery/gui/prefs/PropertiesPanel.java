/*
 * PropertiesPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.prefs;

import org.executequery.*;
import org.executequery.components.BottomButtonPanel;
import org.executequery.components.SplitPaneFactory;
import org.executequery.components.table.PropertiesTreeCellRenderer;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.localization.Bundles;
import org.executequery.toolbars.ToolBarManager;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.tree.DynamicTree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Main system preferences panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesPanel extends JPanel
        implements PreferenceChangeListener,
        TreeSelectionListener {

    public static final String TITLE = Bundles.get("preferences.Preferences");
    public static final String FRAME_ICON = "icon_preferences";

    private static final List<String> PROPERTIES_KEYS_NEED_RESTART = Arrays.asList(
            // -- PropertiesGeneral --
            "startup.unstableversions.load",
            "system.file.encoding",
            "startup.java.path",
            "internet.proxy.set",
            "internet.proxy.host",
            "internet.proxy.port",
            "internet.proxy.user",
            "internet.proxy.password",
            // -- PropertiesAppearance --
            "startup.display.lookandfeel",
            "startup.display.language",
            "display.aa.fonts",
            // -- PropertiesEditorGeneral --
            "editor.tabs.tospaces",
            "editor.tab.spaces",
            // -- PropertiesOutputConsole --
            "system.log.enabled",
            "editor.logging.path",
            "editor.logging.backups",
            "system.log.out",
            "system.log.err"
    );

    private JTree tree;
    private JPanel rightPanel;
    private CardLayout cardLayout;
    private UserPreferenceFunction currentlySelectedPanel;
    private Map<Integer, UserPreferenceFunction> panelMap;

    private final ActionContainer parent;
    private final Map<String, PreferenceChangeEvent> preferenceChangeEvents;

    private static boolean restartNeed;
    private static boolean updateEnvNeed;

    /**
     * Constructs a new instance.
     */
    public PropertiesPanel(ActionContainer parent) {
        this(parent, -1);
    }

    public PropertiesPanel(ActionContainer parent, int openRow) {
        super(new BorderLayout());

        this.parent = parent;
        this.preferenceChangeEvents = new HashMap<>();
        restartNeed = false;
        updateEnvNeed = false;

        init();
        if (openRow != -1)
            selectOpenRow(openRow);
    }

    private void init() {

        JSplitPane splitPane = new SplitPaneFactory().createHorizontal();
        splitPane.setDividerSize(6);

        int panelWidth = 900;
        int panelHeight = 700;
        setPreferredSize(new Dimension(panelWidth, panelHeight));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(panelWidth, panelHeight - 50));

        cardLayout = new CardLayout();
        rightPanel = new JPanel(cardLayout);
        splitPane.setRightComponent(rightPanel);

        // --- initialise branches ---

        List<PropertyNode> branches = new ArrayList<>();

        branches.add(new PropertyNode(PropertyTypes.GENERAL, bundledString("General")));
        branches.add(new PropertyNode(PropertyTypes.APPEARANCE, bundledString("Display")));
        branches.add(new PropertyNode(PropertyTypes.SHORTCUTS, bundledString("Shortcuts")));
        branches.add(new PropertyNode(PropertyTypes.SQL_SHORTCUTS, bundledString("SqlShortcuts")));
        branches.add(new PropertyNode(PropertyTypes.CONNECTIONS, bundledString("Connection")));
        branches.add(new PropertyNode(PropertyTypes.EDITOR, bundledString("Editor")));
        branches.add(new PropertyNode(PropertyTypes.RESULT_SET, bundledString("ResultSetTable")));

        PropertyNode node = new PropertyNode(PropertyTypes.TOOLBAR_GENERAL, bundledString("ToolBar"));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_DATABASE, bundledString("DatabaseTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_APPLICATION, bundledString("ApplicationTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_SYSTEM, bundledString("SystemTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_QUERY_EDITOR, bundledString("QueryEditorTools")));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.FONTS_GENERAL, bundledString("Fonts"));
        node.addChild(new PropertyNode(PropertyTypes.EDITOR_FONTS, bundledString("EDITOR_FONTS")));
        node.addChild(new PropertyNode(PropertyTypes.CONNECTIONS_TREE_FONTS, bundledString("TREE_CONNECTIONS_FONTS")));
        node.addChild(new PropertyNode(PropertyTypes.CONSOLE_FONTS, bundledString("CONSOLE_FONTS")));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.COLORS_GENERAL, bundledString("Colours"));
        node.addChild(new PropertyNode(PropertyTypes.EDITOR_COLOURS, bundledString("EDITOR_COLOURS")));
        node.addChild(new PropertyNode(PropertyTypes.RESULT_SET_COLOURS, bundledString("RESULT_SET_COLOURS")));
        branches.add(node);

        // --- add branches to the tree ---

        node = new PropertyNode(PropertyTypes.SYSTEM, bundledString("Preferences"));
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(node);

        for (PropertyNode branch : branches) {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(branch);
            root.add(treeNode);

            if (branch.hasChildren())
                for (PropertyNode child : branch.getChildren())
                    treeNode.add(new DefaultMutableTreeNode(child));
        }

        // --- add tree on panel ---

        tree = new DynamicTree(root);
        tree.setRowHeight(22);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setCellRenderer(new PropertiesTreeCellRenderer());
        tree.setRootVisible(false);

        // expand all rows
        for (int i = 0; i < tree.getRowCount(); i++)
            tree.expandRow(i);

        Dimension leftPanelDim = new Dimension(200, 350);
        JScrollPane js = new JScrollPane(tree);
        js.setPreferredSize(leftPanelDim);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setMinimumSize(leftPanelDim);
        leftPanel.setMaximumSize(leftPanelDim);
        leftPanel.add(js, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(new BottomButtonPanel(
                        e -> save(false),
                        null,
                        "prefs",
                        parent.isDialog()
                ), BorderLayout.SOUTH
        );

        add(mainPanel, BorderLayout.CENTER);
        panelMap = new HashMap<>();
        tree.addTreeSelectionListener(this);

        // set up the first panel
        PropertiesRootPanel panel = new PropertiesRootPanel();

        Integer id = PropertyTypes.SYSTEM;
        panelMap.put(id, panel);

        rightPanel.add(panel, String.valueOf(id));
        cardLayout.show(rightPanel, String.valueOf(id));

        tree.setSelectionRow(0);
    }

    @SuppressWarnings("rawtypes")
    private void selectOpenRow(int openRow) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            PropertyNode propertyNode = (PropertyNode) node.getUserObject();

            if (propertyNode.getNodeId() == openRow) {
                tree.setSelectionPath(new TreePath(node.getPath()));
                break;
            }
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        final TreePath path = e.getPath();
        SwingUtilities.invokeLater(() -> getProperties(path.getPath()));
    }

    private void getProperties(Object[] selection) {

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) selection[selection.length - 1];
        PropertyNode propertyNode = (PropertyNode) treeNode.getUserObject();

        Integer nodeId = propertyNode.getNodeId();
        if (panelMap.containsKey(nodeId)) {
            cardLayout.show(rightPanel, String.valueOf(nodeId));
            return;
        }

        JPanel panel = getPropertyPanel(nodeId);
        if (panel == null)
            return;

        currentlySelectedPanel = (UserPreferenceFunction) panel;
        currentlySelectedPanel.addPreferenceChangeListener(this);
        panelMap.put(nodeId, currentlySelectedPanel);

        // apply all previously applied prefs that the new panel might be interested in
        for (Map.Entry<String, PreferenceChangeEvent> event : preferenceChangeEvents.entrySet())
            currentlySelectedPanel.preferenceChange(event.getValue());

        rightPanel.add(panel, String.valueOf(nodeId));
        cardLayout.show(rightPanel, String.valueOf(nodeId));
    }

    private JPanel getPropertyPanel(int nodeId) {
        switch (nodeId) {

            case PropertyTypes.SYSTEM:
                return new PropertiesRootPanel();
            case PropertyTypes.GENERAL:
                return new PropertiesGeneral(this);
            case PropertyTypes.SHORTCUTS:
                return new PropertiesKeyShortcuts(this);
            case PropertyTypes.SQL_SHORTCUTS:
                return new PropertiesSqlShortcuts(this);
            case PropertyTypes.APPEARANCE:
                return new PropertiesAppearance(this);
            case PropertyTypes.CONNECTIONS:
                return new PropertiesConnections(this);
            case PropertyTypes.EDITOR:
                return new PropertiesEditorGeneral(this);
            case PropertyTypes.RESULT_SET:
                return new PropertiesResultSetTableGeneral(this);

            case PropertyTypes.TOOLBAR_GENERAL:
                return new PropertiesToolBarGeneral(this);
            case PropertyTypes.TOOLBAR_DATABASE:
                return new PropertiesToolBar(this, ToolBarManager.DATABASE_TOOLS);
            case PropertyTypes.TOOLBAR_APPLICATION:
                return new PropertiesToolBar(this, ToolBarManager.APPLICATION_TOOLS);
            case PropertyTypes.TOOLBAR_QUERY_EDITOR:
                return new PropertiesToolBar(this, ToolBarManager.QUERY_EDITOR_TOOLS);
            case PropertyTypes.TOOLBAR_SYSTEM:
                return new PropertiesToolBar(this, ToolBarManager.SYSTEM_TOOLS);

            case PropertyTypes.FONTS_GENERAL:
            case PropertyTypes.EDITOR_FONTS:
                return new PropertiesEditorFonts(this);
            case PropertyTypes.CONNECTIONS_TREE_FONTS:
                return new PropertiesTreeConnectionsFonts(this);
            case PropertyTypes.CONSOLE_FONTS:
                return new PropertiesConsoleFonts(this);

            case PropertyTypes.COLORS_GENERAL:
            case PropertyTypes.EDITOR_COLOURS:
                return new PropertiesEditorColours(this);
            case PropertyTypes.RESULT_SET_COLOURS:
                return new PropertiesResultSetTableColours(this);

            default:
                return null;
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {

        for (Map.Entry<Integer, UserPreferenceFunction> entry : panelMap.entrySet())
            entry.getValue().preferenceChange(e);

        preferenceChangeEvents.put(e.getKey(), e);
        checkAndSetRestartNeed(e.getKey());
    }

    public void save(boolean isApplyAction) {

        try {
            GUIUtilities.showWaitCursor();

            if (isApplyAction)
                currentlySelectedPanel.save();
            else
                panelMap.values().forEach(UserPreferenceFunction::save);
            ThreadUtils.invokeLater(() -> EventMediator.fireEvent(createUserPreferenceEvent()));

            if (isRestartNeed()) {
                setRestartNeed(false);
                if (GUIUtilities.displayConfirmDialog(bundledString("restart-message")) == JOptionPane.YES_OPTION)
                    ExecuteQuery.restart(ApplicationContext.getInstance().getRepo(), updateEnvNeed);

            } else
                GUIUtilities.displayInformationMessage(bundledString("setting-applied"));

        } finally {
            GUIUtilities.showNormalCursor();
        }

        if (!isApplyAction)
            parent.finished();
    }

    private UserPreferenceEvent createUserPreferenceEvent() {
        return new DefaultUserPreferenceEvent(this, null, UserPreferenceEvent.ALL);
    }

    public boolean isRestartNeed() {
        return restartNeed;
    }

    public static void checkAndSetRestartNeed(String key) {
        PropertiesPanel.restartNeed = PROPERTIES_KEYS_NEED_RESTART.contains(key);
    }

    public static void setRestartNeed(boolean restartNeed) {
        PropertiesPanel.restartNeed = restartNeed;
    }

    public static void setUpdateEnvNeed(boolean updateEnvNeed) {
        PropertiesPanel.updateEnvNeed = updateEnvNeed;
    }

    private String bundledString(String key) {
        return Bundles.get("preferences." + key);
    }

}
