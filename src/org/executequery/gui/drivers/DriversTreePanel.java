/*
 * DriversTreePanel.java
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

package org.executequery.gui.drivers;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.databasemediators.DatabaseDriverFactory;
import org.executequery.databasemediators.spi.DatabaseDriverFactoryImpl;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DatabaseDriverEvent;
import org.executequery.event.DatabaseDriverListener;
import org.executequery.gui.AbstractDockedTabActionPanel;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.ThreadUtils;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.toolbar.PanelToolBar;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.swing.tree.DefaultTreeRootNode;
import org.underworldlabs.swing.tree.DynamicTree;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DriversTreePanel extends AbstractDockedTabActionPanel
        implements TreeSelectionListener,
        DatabaseDriverListener {

    public static final String TITLE = Bundles.getCommon("drivers");

    /**
     * the tree display
     */
    private DynamicTree tree;

    /**
     * saved drivers collection
     */
    private List<DatabaseDriver> drivers;

    /**
     * the driver display panel
     */
    private DriverViewPanel driversPanel;

    /**
     * the tree popup menu
     */
    private PopMenu popupMenu;

    /**
     * whether to reload the panel view
     */
    private boolean reloadView;

    // -------------------------------------
    // tool bar buttons

    /**
     * move connection up button
     */
    private JButton upButton;

    /**
     * move connection down button
     */
    private JButton downButton;

    /**
     * new connection button
     */
    @SuppressWarnings("unused")
    private JButton newDriverButton;

    /**
     * delete connection button
     */
    private JButton deleteDriverButton;

    // -------------------------------------

    private DatabaseDriverFactory databaseDriverFactory;

    /**
     * Creates a new instance of DriversTreePanel
     */
    public DriversTreePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        drivers = loadDrivers();

        DefaultMutableTreeNode root = new DefaultTreeRootNode("JDBC Drivers");
        for (int i = 0, k = drivers.size(); i < k; i++) {
            // add the driver to the root node
            root.add(new DatabaseDriverNode(drivers.get(i)));
            // add to the cache
        }

        // init the tree
        tree = new DynamicTree(root);
        tree.setCellRenderer(new DriversTreeCellRenderer());
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseHandler());

        // ---------------------------------------
        // the tool bar
        PanelToolBar tools = new PanelToolBar();
        newDriverButton = tools.addButton(
                this, "newDriver",
                GUIUtilities.getAbsoluteIconPath("NewJDBCDriver16.png"),
                "New JDBC driver");
        deleteDriverButton = tools.addButton(
                this, "deleteDriver",
                GUIUtilities.getAbsoluteIconPath("Delete16.png"),
                "Remove driver");
        upButton = tools.addButton(
                this, "moveDriverUp",
                GUIUtilities.getAbsoluteIconPath("Up16.png"),
                "Move up");
        downButton = tools.addButton(
                this, "moveDriverDown",
                GUIUtilities.getAbsoluteIconPath("Down16.png"),
                "Move down");

        // add the tools and tree to the panel
        add(tools, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        enableButtons(false);

        EventMediator.registerListener(this);

        tree.setRowHeight(20);

        // make sure we have a display panel
        //checkDriversPanel();
    }

    private DatabaseDriverFactory databaseDriverFactory() {

        if (databaseDriverFactory == null) {

            databaseDriverFactory = new DatabaseDriverFactoryImpl();
        }

        return databaseDriverFactory;
    }

    private List<DatabaseDriver> loadDrivers() {

        return ((DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID)).findAll();
    }

    private void enableButtons(boolean enable) {
        upButton.setEnabled(enable);
        downButton.setEnabled(enable);
        deleteDriverButton.setEnabled(enable);
    }

    public void moveDriverUp() {
        tree.moveSelectionUp();
        Object object = tree.getLastPathComponent();
        moveNode((DatabaseDriverNode) object, DynamicTree.MOVE_UP);
    }

    public void moveDriverDown() {
        tree.moveSelectionDown();
        // adjust the position of the connection
        Object object = tree.getLastPathComponent();
        moveNode((DatabaseDriverNode) object, DynamicTree.MOVE_DOWN);
    }

    public void newDriver() {

        String name = buildDriverName("New Driver");
        newDriver(databaseDriverFactory().create(System.currentTimeMillis(), name));
    }

    /**
     * Adds a new driver with the specified driver as the base
     * for the new one.
     *
     * @param driver - the driver the new is to be based on
     */
    public void newDriver(DatabaseDriver driver) {

        if (driver == null) {

            String name = buildDriverName("New Driver");
            driver = databaseDriverFactory().create(System.currentTimeMillis(), name);
        }

        drivers.add(driver);

        addDriverToTree(driver);
    }

    private void addDriverToTree(DatabaseDriver driver) {

        DatabaseDriverNode node = createNodeForDriver(driver);

        tree.addToRoot(node);
    }

    private DatabaseDriverNode createNodeForDriver(DatabaseDriver driver) {
        return new DatabaseDriverNode(driver);
    }

    public void deleteDriver() {

        deleteDriver(null);
    }

    public void deleteDriver(DatabaseDriverNode node) {

        boolean isSelectedNode = false;

        if (node == null) {

            Object object = tree.getLastPathComponent();
            node = (DatabaseDriverNode) object;
            isSelectedNode = true;

        } else {

            if (tree.getLastPathComponent() == node) {

                isSelectedNode = true;
            }

        }

        DatabaseDriver driver = node.getDriver();

        // check if they're trying to delete the ODBC driver
        if (driver.isDefaultSunOdbc()) {

            GUIUtilities.displayErrorMessage(
                    "The ODBC driver definition is a built-in and can not be removed");

            return;
        }

        int yesNo = GUIUtilities.displayConfirmCancelDialog(
                "Are you sure you want to delete the driver " +
                        driver + " ?");

        if (yesNo != JOptionPane.YES_OPTION) {
            return;
        }

        // the next selection index will be the index of
        // the one being removed - (index - 1)
        int index = drivers.indexOf(driver);

        // remove from the connections
        drivers.remove(index);

        if (index > drivers.size() - 1) {

            index = drivers.size() - 1;
        }

        if (isSelectedNode) {

            String prefix = drivers.get(index).getName();
            tree.removeSelection(prefix);

        } else {

            tree.removeNode(node);
        }

        // save the drivers
        driversPanel.saveDrivers();
    }

    /**
     * Sets the selected tree node to the specified driver.
     *
     * @param driver - the driver to select
     */
    public void setSelectedDriver(DatabaseDriver driver) {
        DefaultMutableTreeNode node = null;

        // retrieve the root node and loop through
        DefaultMutableTreeNode root = tree.getRootNode();
        for (Enumeration<?> i = root.children(); i.hasMoreElements(); ) {

            DefaultMutableTreeNode _node = (DefaultMutableTreeNode) i.nextElement();
            Object userObject = _node.getUserObject();

            // make sure its a connection object
            if (userObject == driver) {

                node = _node;
                break;
            }

        }

        // select the node path
        if (node != null) {

            TreePath path = new TreePath(node.getPath());
            tree.scrollPathToVisible(path);
            tree.setSelectionPath(path);

            if (reloadView) {

                Object object = tree.getLastPathComponent();

                if (object instanceof DatabaseDriverNode) {

                    checkDriversPanel();
                    DatabaseDriverNode _node = (DatabaseDriverNode) object;
                    driversPanel.valueChanged(_node);
                }

            }

        }
    }

    private void moveNode(DatabaseDriverNode node, int direction) {
        DatabaseDriver driver = node.getDriver();

        int currentIndex = drivers.indexOf(driver);
        if (currentIndex == 0 && direction == DynamicTree.MOVE_UP) {

            return;
        }

        int newIndex = -1;
        if (direction == DynamicTree.MOVE_UP) {

            newIndex = currentIndex - 1;
        } else {

            newIndex = currentIndex + 1;
            if (newIndex > (drivers.size() - 1)) {

                return;
            }
        }

        drivers.remove(currentIndex);
        drivers.add(newIndex, driver);

        // save the drivers
        driversPanel.saveDrivers();
    }

    /**
     * Indicates that a node name has changed and fires a call
     * to repaint the tree display.
     */
    protected void nodeNameValueChanged(DatabaseDriver driver) {

        TreeNode node = tree.getNodeFromRoot(driver);

        if (node != null) {

            tree.nodeChanged(node);
        }

    }


    private void checkDriversPanel() {

        // check we have a drivers panel
        if (driversPanel == null) {

            driversPanel = new DriverViewPanel(this);
        }

        // check the panel is in the pane

    }

    private void getDriverPanelFromBrowser() {
        JPanel _viewPanel = GUIUtilities.getCentralPane(DriverPanel.TITLE);

        if (_viewPanel == null) {

            GUIUtilities.addCentralPane(DriverPanel.TITLE,
                    DriverPanel.FRAME_ICON,
                    driversPanel,
                    "JDBC Drivers",
                    true);

        } else {

            GUIUtilities.setSelectedCentralPane(DriverPanel.TITLE);
        }
    }

    public void valueChanged(final TreeSelectionEvent e) {

        ThreadUtils.startWorker(new Runnable() {

            public void run() {

                GUIUtilities.showWaitCursor();
                try {

                    Object object = e.getPath().getLastPathComponent();
                    if (object instanceof DatabaseDriverNode) {

                        enableButtons(true);
                        checkDriversPanel();
                        DatabaseDriverNode node = (DatabaseDriverNode) object;
                        driversPanel.valueChanged(node);

                    } else if (object == tree.getRootNode()) {

                        checkDriversPanel();
                        driversPanel.displayRootPanel();
                        enableButtons(false);

                    } else {

                        enableButtons(false);
                    }

                } finally {

                    GUIUtilities.showNormalCursor();
                }

            }
        });

    }

    /**
     * Returns the database driver at the specified point.
     *
     * @return the driver properties object
     */
    protected DatabaseDriver getDriverAt(Point point) {

        return getDriverAt(tree.getPathForLocation(point.x, point.y));
    }

    /**
     * Returns the database driver associated with the specified path.
     *
     * @return the driver properties object
     */
    protected DatabaseDriver getDriverAt(TreePath path) {
        if (path != null) {
            Object object = path.getLastPathComponent();
            if (object instanceof DatabaseDriverNode) {
                return ((DatabaseDriverNode) object).getDriver();
            }
        }
        return null;
    }

    /**
     * Returns the name of a new driver to be added where
     * the name of the driver may already exist.
     *
     * @param name - the name of the driver
     */
    private String buildDriverName(String name) {
        int count = 0;
        for (int i = 0, n = drivers.size(); i < n; i++) {
            DatabaseDriver driver = drivers.get(i);
            if (driver.getName().startsWith(name)) {
                count++;
            }
        }

        if (count > 0) {
            count++;
            name += " " + count;
        }
        return name;
    }

    public String toString() {
        return TITLE;
    }

    // ----------------------------------------
    // DockedTabView Implementation
    // ----------------------------------------

    public static final String MENU_ITEM_KEY = "viewDrivers";

    public static final String PROPERTY_KEY = "system.display.drivers";

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    private class DriversTreeCellRenderer extends AbstractTreeCellRenderer {

        private Color textBackground;
        private Color textForeground;
        private Color selectedBackground;
        private Color selectedTextForeground;

        private ImageIcon driverImage;
        private ImageIcon driverRootImage;

        public DriversTreeCellRenderer() {
            driverRootImage = GUIUtilities.loadIcon("DatabaseDrivers16.png", true);
            driverImage = GUIUtilities.loadIcon("JDBCDriver16.png", true);

            textBackground = UIManager.getColor("Tree.textBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            selectedBackground = UIManager.getColor("Tree.selectionBackground");
            selectedTextForeground = UIManager.getColor("Tree.selectionForeground");

            if (UIUtils.isGtkLookAndFeel()) {
                setBorderSelectionColor(null);
            }

            //setFont(new Font("Dialog", Font.PLAIN, 11));
        }

        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value, boolean bSelected, boolean bExpanded,
                                                      boolean bLeaf, int iRow, boolean bHasFocus) {

            String labelText = value.toString();
            if (value instanceof DefaultTreeRootNode) {

                setIcon(driverRootImage);

            } else if (value instanceof DatabaseDriverNode) {

                setIcon(driverImage);
            }

            // Add the text to the cell
            setText(labelText);

            // Add a tool tip displaying the name
            setToolTipText(labelText);

            this.selected = bSelected;

            if (!selected) {
                setBackground(textBackground);
                setForeground(textForeground);
            } else {
                setBackground(selectedBackground);
                setForeground(selectedTextForeground);
            }

            return this;
        }


    } // class DriversTreeCellRenderer

    /**
     * The tree's popup menu function
     */
    private class PopMenu extends JPopupMenu implements ActionListener {

        private JMenuItem duplicate;
        private JMenuItem delete;
        private JMenuItem properties;
        private JMenuItem addNewDriver;

        protected TreePath popupPath;
        protected DatabaseDriver hover;

        public PopMenu() {
            addNewDriver = MenuItemFactory.createMenuItem("New Driver");
            addNewDriver.addActionListener(this);

            duplicate = MenuItemFactory.createMenuItem("Duplicate");
            duplicate.addActionListener(this);

            delete = MenuItemFactory.createMenuItem("Remove");
            delete.addActionListener(this);

            properties = MenuItemFactory.createMenuItem("Driver Properties");
            properties.addActionListener(this);

            add(addNewDriver);
            addSeparator();
            add(duplicate);
            add(delete);
            addSeparator();
            add(properties);
        }

        protected void setMenuItemsText() {
            if (hover != null) {
                String name = hover.getName();
                delete.setText("Remove Driver " + name);
                duplicate.setText("Create Duplicate of Driver " + name);
            }
        }

        public void actionPerformed(ActionEvent e) {
            try {
                Object source = e.getSource();
                if (source == duplicate) {
                    if (hover != null) {

                        String name = buildDriverName(hover.getName() + " (Copy") + ")";

                        DatabaseDriver dd = databaseDriverFactory().create(name);

                        dd.setClassName(hover.getClassName());
                        dd.setDatabaseType(hover.getType());
                        dd.setDescription(hover.getDescription());
                        dd.setId(System.currentTimeMillis());
                        dd.setPath(hover.getPath());
                        dd.setURL(hover.getURL());

                        newDriver(dd);
                    }
                } else if (source == delete) {
                    if (popupPath != null) {
                        DatabaseDriverNode node =
                                (DatabaseDriverNode) popupPath.getLastPathComponent();
                        deleteDriver(node);
                    }
                } else if (source == properties) {
                    reloadView = true;
                    setSelectedDriver(hover);
                } else if (source == addNewDriver) {
                    newDriver();
                }
            } finally {
                reloadView = false;
                hover = null;
                popupPath = null;
            }

        }
    } // class PopMenu

    private class MouseHandler extends MouseAdapter {

        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() < 2) {
                return;
            }
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == tree.getSelectionPath()) {
                checkDriversPanel();
                getDriverPanelFromBrowser();
                //valueChanged(new TreeSelectionEvent(
                //tree, path, true, path, path));
            }
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {

                if (popupMenu == null) {
                    popupMenu = new PopMenu();
                }

                Point point = new Point(e.getX(), e.getY());
                popupMenu.popupPath = tree.getPathForLocation(point.x, point.y);
                // get the connection at this point
                popupMenu.hover = getDriverAt(point);
                if (popupMenu.hover != null) {
                    try {
                        tree.removeTreeSelectionListener(DriversTreePanel.this);
                        popupMenu.setMenuItemsText();
                        tree.setSelectionPath(popupMenu.popupPath);
                    } finally {
                        tree.addTreeSelectionListener(DriversTreePanel.this);
                    }
                    popupMenu.show(e.getComponent(), point.x, point.y);
                }
            }
        }

    } // class MouseHandler

    public void driversUpdated(DatabaseDriverEvent databaseDriverEvent) {

        if (databaseDriverEvent.getSource() instanceof DatabaseDriver) {

            DatabaseDriver driver = (DatabaseDriver) databaseDriverEvent.getSource();

            DatabaseDriverNode node = createNodeForDriver(driver);

            tree.addToRoot(node, false);
        }

    }

    public boolean canHandleEvent(ApplicationEvent event) {

        return (event instanceof DatabaseDriverEvent);
    }

}







