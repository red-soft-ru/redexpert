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

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.components.SplitPaneFactory;
import org.executequery.components.table.PropertiesTreeCellRenderer;
import org.executequery.event.DefaultUserPreferenceEvent;
import org.executequery.event.UserPreferenceEvent;
import org.executequery.gui.ActionContainer;
import org.executequery.localization.Bundles;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.tree.DynamicTree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

/**
 * Main system preferences panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesPanel extends JPanel
        implements ActiveComponent,
        ActionListener,
        PreferenceChangeListener,
        TreeSelectionListener {

    public static final String TITLE = Bundles.get("preferences.Preferences");
    public static final String FRAME_ICON = "Preferences16.png";

    /**
     * the property selection tree
     */
    private JTree tree;

    /**
     * the right-hand property display panel
     */
    private JPanel rightPanel;

    /**
     * the base panel layout
     */
    private CardLayout cardLayout;

    /**
     * map of panels within the layout
     */
    private Map<Integer, UserPreferenceFunction> panelMap;

    /**
     * the parent container
     */
    private ActionContainer parent;

    private Map<String, PreferenceChangeEvent> preferenceChangeEvents;

    /**
     * Constructs a new instance.
     */
    public PropertiesPanel(ActionContainer parent) {

        this(parent, -1);
    }

    /**
     * Constructs a new instance seleting the specified node.
     *
     * @param the node to select
     */
    public PropertiesPanel(ActionContainer parent, int openRow) {

        super(new BorderLayout());
        this.parent = parent;
        this.preferenceChangeEvents = new HashMap<>();

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (openRow != -1) {

            selectOpenRow(openRow);
        }
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

        // ----------------------------------
        // initialise branches

        List<PropertyNode> branches = new ArrayList<PropertyNode>();
        PropertyNode node = new PropertyNode(PropertyTypes.GENERAL, bundledString("General"));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.LOCALE, bundledString("Locale"));
        branches.add(node);
//        node = new PropertyNode(PropertyTypes.VIEW, "View");
//        branches.add(node);
        node = new PropertyNode(PropertyTypes.APPEARANCE, bundledString("Display"));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.SHORTCUTS, bundledString("Shortcuts"));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.LOOK_PLUGIN, bundledString("LookFeelPlugins"));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.TOOLBAR_GENERAL, bundledString("ToolBar"));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_FILE, bundledString("FileTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_EDIT, bundledString("EditTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_DATABASE, bundledString("DatabaseTools")));
        //node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_BROWSER, "Browser Tools"));
        //node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_IMPORT_EXPORT, bundledString("ImportExportTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_SEARCH, bundledString("SearchTools")));
        node.addChild(new PropertyNode(PropertyTypes.TOOLBAR_SYSTEM, bundledString("SystemTools")));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.EDITOR_GENERAL, bundledString("Editor"));
        node.addChild(new PropertyNode(PropertyTypes.EDITOR_FONTS, bundledString("Fonts")));
//        node.addChild(new PropertyNode(PropertyTypes.EDITOR_BACKGROUND, "Colours"));
        node.addChild(new PropertyNode(PropertyTypes.EDITOR_COLOURS, bundledString("Colours")));
//        node.addChild(new PropertyNode(PropertyTypes.EDITOR_SYNTAX, "Syntax Colours"));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.TREE_CONNECTIONS_GENERAL, bundledString("TreeConnections"));
        node.addChild(new PropertyNode(PropertyTypes.TREE_CONNECTIONS_FONTS, bundledString("Fonts")));
        branches.add(node);

        node = new PropertyNode(PropertyTypes.RESULTS, bundledString("ResultSetTable"));
        node.addChild(new PropertyNode(PropertyTypes.RESULT_SET_CELL_COLOURS, bundledString("Colours")));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.CONNECTIONS, bundledString("Connection"));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.BROWSER_GENERAL, bundledString("DatabaseBrowser"));
        node.addChild(new PropertyNode(PropertyTypes.BROWSER_DATA_TAB, bundledString("TableDataPanel")));
        branches.add(node);
        node = new PropertyNode(PropertyTypes.OUTPUT_CONSOLE, bundledString("OutputConsole"));
        node.addChild(new PropertyNode(PropertyTypes.CONSOLE_FONTS, bundledString("Fonts")));
        branches.add(node);

        DefaultMutableTreeNode root =
                new DefaultMutableTreeNode(new PropertyNode(PropertyTypes.SYSTEM, bundledString("Preferences")));

        List<PropertyNode> children = null;
        DefaultMutableTreeNode treeNode = null;

        for (int i = 0, k = branches.size(); i < k; i++) {

            node = branches.get(i);
            treeNode = new DefaultMutableTreeNode(node);
            root.add(treeNode);

            if (node.hasChildren()) {
                children = node.getChildren();
                int count = children.size();

                for (int j = 0; j < count; j++) {
                    treeNode.add(new DefaultMutableTreeNode(children.get(j)));
                }

            }

        }

        tree = new DynamicTree(root);
        tree.setRowHeight(22);
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.setCellRenderer(new PropertiesTreeCellRenderer());
        tree.setRootVisible(false);

        // expand all rows
        for (int i = 0; i < tree.getRowCount(); i++) {

            tree.expandRow(i);
        }

        Dimension leftPanelDim = new Dimension(200, 350);
        JScrollPane js = new JScrollPane(tree);
        js.setPreferredSize(leftPanelDim);

        JPanel leftPanel = new JPanel(new BorderLayout());
//        leftPanel.setBackground(Color.white);
        leftPanel.setMinimumSize(leftPanelDim);
        leftPanel.setMaximumSize(leftPanelDim);
        leftPanel.add(js, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(new BottomButtonPanel(
                this, null, "prefs", parent.isDialog()), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        panelMap = new HashMap<Integer, UserPreferenceFunction>();
        tree.addTreeSelectionListener(this);

        // setup the first panel
        PropertiesRootPanel panel = new PropertiesRootPanel();

        Integer id = PropertyTypes.SYSTEM;
        panelMap.put(id, panel);

        rightPanel.add(panel, String.valueOf(id));
        cardLayout.show(rightPanel, String.valueOf(id));

        tree.setSelectionRow(0);
    }

    @SuppressWarnings("rawtypes")
    private void selectOpenRow(int openRow) {

        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {

            node = (DefaultMutableTreeNode) enumeration.nextElement();
            PropertyNode propertyNode = (PropertyNode) node.getUserObject();

            if (propertyNode.getNodeId() == openRow) {

                tree.setSelectionPath(new TreePath(node.getPath()));
                break;
            }

        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        final TreePath path = e.getPath();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getProperties(path.getPath());
            }
        });
    }

    private void getProperties(Object[] selection) {

        DefaultMutableTreeNode n = (DefaultMutableTreeNode) selection[selection.length - 1];
        PropertyNode node = (PropertyNode) n.getUserObject();

        JPanel panel = null;
        Integer id = node.getNodeId();

        if (panelMap.containsKey(id)) {
            cardLayout.show(rightPanel, String.valueOf(id));
            return;
        }

        switch (id) {
            case PropertyTypes.SYSTEM:
                panel = new PropertiesRootPanel();
                break;
            case PropertyTypes.GENERAL:
                panel = new PropertiesGeneral();
                break;
            case PropertyTypes.LOCALE:
                panel = new PropertiesLocales();
                break;
            case PropertyTypes.SHORTCUTS:
                panel = new PropertiesKeyShortcuts();
                break;
            case PropertyTypes.APPEARANCE:
                panel = new PropertiesAppearance();
                break;
            case PropertyTypes.TOOLBAR_GENERAL:
                panel = new PropertiesToolBarGeneral();
                break;
            case PropertyTypes.TOOLBAR_FILE:
                panel = new PropertiesToolBar("File Tools");
                break;
            case PropertyTypes.TOOLBAR_EDIT:
                panel = new PropertiesToolBar("Edit Tools");
                break;
            case PropertyTypes.TOOLBAR_SEARCH:
                panel = new PropertiesToolBar("Search Tools");
                break;
            case PropertyTypes.TOOLBAR_DATABASE:
                panel = new PropertiesToolBar("Database Tools");
                break;
            case PropertyTypes.TOOLBAR_BROWSER:
                panel = new PropertiesToolBar("Browser Tools");
                break;
            case PropertyTypes.TOOLBAR_IMPORT_EXPORT:
                panel = new PropertiesToolBar("Import/Export Tools");
                break;
            case PropertyTypes.TOOLBAR_SYSTEM:
                panel = new PropertiesToolBar("System Tools");
                break;
            case PropertyTypes.LOOK_PLUGIN:
                panel = new PropertiesLookPlugins();
                break;
            case PropertyTypes.EDITOR_GENERAL:
                panel = new PropertiesEditorGeneral();
                break;
            case PropertyTypes.EDITOR_COLOURS:
                panel = new PropertiesEditorColours();
                break;
            case PropertyTypes.EDITOR_FONTS:
                panel = new PropertiesEditorFonts();
                break;
            case PropertyTypes.RESULTS:
                panel = new PropertiesResultSetTableGeneral();
                break;
            case PropertyTypes.CONNECTIONS:
                panel = new PropertiesConns();
                break;
            case PropertyTypes.BROWSER_GENERAL:
                panel = new PropertiesBrowserGeneral();
                break;
            case PropertyTypes.BROWSER_DATA_TAB:
                panel = new PropertiesBrowserTableData();
                break;
            case PropertyTypes.RESULT_SET_CELL_COLOURS:
                panel = new PropertiesResultSetTableColours();
                break;
            case PropertyTypes.TREE_CONNECTIONS_FONTS:
                panel = new PropertiesTreeConnectionsFonts();
                break;
            case PropertyTypes.TREE_CONNECTIONS_GENERAL:
                panel = new PropertiesTreeConnectionsGeneral();
                break;
            case PropertyTypes.OUTPUT_CONSOLE:
                panel = new PropertiesOutputConsole();
                break;
            case PropertyTypes.CONSOLE_FONTS:
                panel = new PropertiesConsoleFonts();
                break;
        }

        UserPreferenceFunction userPreferenceFunction = (UserPreferenceFunction) panel;
        userPreferenceFunction.addPreferenceChangeListener(this);
        panelMap.put(id, userPreferenceFunction);

        // apply all previosuly applied prefs that the new panel might be interested in
        for (Map.Entry<String, PreferenceChangeEvent> event : preferenceChangeEvents.entrySet()) {

            userPreferenceFunction.preferenceChange(event.getValue());
        }

        rightPanel.add(panel, String.valueOf(id));
        cardLayout.show(rightPanel, String.valueOf(id));
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent e) {

        for (Map.Entry<Integer, UserPreferenceFunction> entry : panelMap.entrySet()) {

            entry.getValue().preferenceChange(e);
        }
        preferenceChangeEvents.put(e.getKey(), e);
    }

    public void actionPerformed(ActionEvent e) {

        try {

            GUIUtilities.showWaitCursor();

            for (Integer key : panelMap.keySet()) {

                panelMap.get(key).save();
            }

            ThreadUtils.invokeLater(new Runnable() {

                public void run() {

                    EventMediator.fireEvent(createUserPreferenceEvent());
                }

            });
            GUIUtilities.displayInformationMessage(bundledString("restart-message"));

        } finally {

            GUIUtilities.showNormalCursor();
        }

        parent.finished();
    }

    private UserPreferenceEvent createUserPreferenceEvent() {

        return new DefaultUserPreferenceEvent(this, null, UserPreferenceEvent.ALL);
    }

    public void cleanup() {

        if (panelMap.containsKey("Colours") && panelMap.get("Colours") instanceof PropertiesEditorBackground) {

            PropertiesEditorBackground panel = (PropertiesEditorBackground) panelMap.get("Colours");
            panel.stopCaretDisplayTimer();
        }

    }

    private String bundledString(String key) {
        return Bundles.get("preferences." + key);
    }

}


