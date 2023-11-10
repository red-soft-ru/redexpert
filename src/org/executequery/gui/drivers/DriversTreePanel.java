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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class DriversTreePanel extends AbstractDockedTabActionPanel
        implements TreeSelectionListener,
        DatabaseDriverListener {

    public static final String TITLE = Bundles.getCommon("drivers");
    public static final String MENU_ITEM_KEY = "viewDrivers";
    public static final String PROPERTY_KEY = "system.display.drivers";

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

    // --- tool bar buttons ---

    /**
     * new connection button
     */
    @SuppressWarnings("unused")
    private JButton newDriverButton;

    /**
     * delete connection button
     */
    private JButton deleteDriverButton;

    /**
     * move connection up button
     */
    private JButton upButton;

    /**
     * move connection down button
     */
    private JButton downButton;

    // ---

    private DatabaseDriverFactory databaseDriverFactory;

    public DriversTreePanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {

        drivers = loadDrivers();

        DefaultMutableTreeNode root = new DefaultTreeRootNode(bundleString("jdbcDrivers"));
        for (DatabaseDriver driver : drivers)
            root.add(new DatabaseDriverNode(driver));

        // --- tree ---

        tree = new DynamicTree(root);
        tree.setCellRenderer(new DriversTreeCellRenderer());
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseHandler());

        // --- tool bar ---

        PanelToolBar tools = new PanelToolBar();
        newDriverButton = tools.addButton(
                this, "newDriver",
                GUIUtilities.getAbsoluteIconPath("NewJDBCDriver16.svg"),
                bundleString("newDriver"));
        deleteDriverButton = tools.addButton(
                this, "deleteDriver",
                GUIUtilities.getAbsoluteIconPath("Delete16.svg"),
                bundleString("deleteDriver"));
        upButton = tools.addButton(
                this, "moveDriverUp",
                GUIUtilities.getAbsoluteIconPath("Up16.svg"),
                bundleString("moveDriverUp"));
        downButton = tools.addButton(
                this, "moveDriverDown",
                GUIUtilities.getAbsoluteIconPath("Down16.svg"),
                bundleString("moveDriverDown"));

        // ---

        add(tools, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);

        enableButtons(false);
        EventMediator.registerListener(this);
        tree.setRowHeight(20);

    }

    private DatabaseDriverFactory databaseDriverFactory() {

        if (databaseDriverFactory == null)
            databaseDriverFactory = new DatabaseDriverFactoryImpl();

        return databaseDriverFactory;
    }

    private List<DatabaseDriver> loadDrivers() {

        DatabaseDriverRepository repository = (DatabaseDriverRepository)
                RepositoryCache.load(DatabaseDriverRepository.REPOSITORY_ID);

        return repository != null ? repository.findAll() : new ArrayList<>();
    }

    private void enableButtons(boolean enable) {
        upButton.setEnabled(enable);
        downButton.setEnabled(enable);
        deleteDriverButton.setEnabled(enable);
    }

    public void newDriver() {
        String name = buildDriverName(bundleString("newDriver"));
        newDriver(databaseDriverFactory().create(System.currentTimeMillis(), name));
    }

    @SuppressWarnings("unused")
    public void deleteDriver() {
        deleteDriver(null);
    }

    @SuppressWarnings("unused")
    public void moveDriverUp() {
        tree.moveSelectionUp();
        moveNode((DatabaseDriverNode) tree.getLastPathComponent(), DynamicTree.MOVE_UP);
    }

    @SuppressWarnings("unused")
    public void moveDriverDown() {
        tree.moveSelectionDown();
        moveNode((DatabaseDriverNode) tree.getLastPathComponent(), DynamicTree.MOVE_DOWN);
    }

    /**
     * Adds a new driver with the specified driver as the base
     * for the new one.
     *
     * @param driver - the driver the new is to be based on
     */
    public void newDriver(DatabaseDriver driver) {

        if (driver == null) {
            String name = buildDriverName(bundleString("newDriver"));
            driver = databaseDriverFactory().create(System.currentTimeMillis(), name);
        }

        drivers.add(driver);
        tree.addToRoot(createNodeForDriver(driver));
    }

    private DatabaseDriverNode createNodeForDriver(DatabaseDriver driver) {
        return new DatabaseDriverNode(driver);
    }

    public void deleteDriver(DatabaseDriverNode node) {

        boolean isSelectedNode = false;
        if (node == null) {

            Object object = tree.getLastPathComponent();
            node = (DatabaseDriverNode) object;
            isSelectedNode = true;

        } else if (tree.getLastPathComponent() == node)
            isSelectedNode = true;

        DatabaseDriver driver = node.getDriver();

        int confirmationResult = GUIUtilities.displayConfirmCancelDialog(
                String.format(bundleString("confirmRemoving"), driver));

        if (confirmationResult != JOptionPane.YES_OPTION)
            return;

        // the next selection index will be the index of
        // the one being removed - (index - 1)
        int index = drivers.indexOf(driver);

        // remove from the connections
        drivers.remove(index);

        if (index > drivers.size() - 1)
            index = drivers.size() - 1;

        if (isSelectedNode) {

            String prefix = drivers.get(index).getName();
            tree.removeSelection(prefix);

        } else
            tree.removeNode(node);

        driversPanel.saveDrivers();
    }

    /**
     * Sets the selected tree node to the specified driver.
     *
     * @param driver - the driver to select
     */
    public void setSelectedDriver(DatabaseDriver driver) {

        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode root = tree.getRootNode(); // retrieve the root node and loop through

        for (Enumeration<?> i = root.children(); i.hasMoreElements(); ) {

            DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode) i.nextElement();
            Object userObject = tempNode.getUserObject();

            // make sure it's a connection object
            if (userObject == driver) {
                node = tempNode;
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
        if (currentIndex == 0 && direction == DynamicTree.MOVE_UP)
            return;

        int newIndex;
        if (direction == DynamicTree.MOVE_UP) {
            newIndex = currentIndex - 1;

        } else {
            newIndex = currentIndex + 1;
            if (newIndex > (drivers.size() - 1))
                return;
        }

        drivers.remove(currentIndex);
        drivers.add(newIndex, driver);
        driversPanel.saveDrivers();
    }

    /**
     * Indicates that a node name has changed and fires a call
     * to repaint the tree display.
     */
    protected void nodeNameValueChanged(DatabaseDriver driver) {

        TreeNode node = tree.getNodeFromRoot(driver);
        if (node != null)
            tree.nodeChanged(node);
    }


    private void checkDriversPanel() {
        if (driversPanel == null)
            driversPanel = new DriverViewPanel(this);
    }

    private void getDriverPanelFromBrowser() {

        JPanel viewPanel = GUIUtilities.getCentralPane(DriverPanel.TITLE);
        if (viewPanel == null) {

            GUIUtilities.addCentralPane(DriverPanel.TITLE,
                    DriverPanel.FRAME_ICON,
                    driversPanel,
                    bundleString("jdbcDrivers"),
                    true);

        } else
            GUIUtilities.setSelectedCentralPane(DriverPanel.TITLE);
    }

    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        ThreadUtils.startWorker(() -> {

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

                } else
                    enableButtons(false);

            } finally {
                GUIUtilities.showNormalCursor();
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
            if (object instanceof DatabaseDriverNode)
                return ((DatabaseDriverNode) object).getDriver();
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
        for (DatabaseDriver driver : drivers)
            if (driver.getName().startsWith(name))
                count++;

        if (count > 0) {
            count++;
            name += " " + count;
        }

        return name;
    }

    // --- DockedTabView Implementation ---

    /**
     * Returns the display title for this view.
     *
     * @return the title displayed for this view
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Returns the name defining the property name for this docked tab view.
     *
     * @return the key
     */
    @Override
    public String getPropertyKey() {
        return PROPERTY_KEY;
    }

    /**
     * Returns the name defining the menu cache property
     * for this docked tab view.
     *
     * @return the preferences key
     */
    @Override
    public String getMenuItemKey() {
        return MENU_ITEM_KEY;
    }

    // ---

    @Override
    public void driversUpdated(DatabaseDriverEvent databaseDriverEvent) {

        if (databaseDriverEvent.getSource() instanceof DatabaseDriver) {
            DatabaseDriver driver = (DatabaseDriver) databaseDriverEvent.getSource();
            DatabaseDriverNode node = createNodeForDriver(driver);
            tree.addToRoot(node, false);
        }
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return event instanceof DatabaseDriverEvent;
    }

    @Override
    public String toString() {
        return TITLE;
    }

    @Override
    public String bundleString(String key) {
        return Bundles.get(getClass(), key);
    }

    private static class DriversTreeCellRenderer extends AbstractTreeCellRenderer {

        private final Color textBackground;
        private final Color textForeground;
        private final Color selectedBackground;
        private final Color selectedTextForeground;

        private final ImageIcon driverImage;
        private final ImageIcon driverRootImage;

        public DriversTreeCellRenderer() {

            driverRootImage = GUIUtilities.loadIcon("DatabaseDrivers16.svg", true);
            driverImage = GUIUtilities.loadIcon("JDBCDriver16.svg", true);

            textBackground = UIManager.getColor("Tree.textBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            selectedBackground = UIManager.getColor("Tree.selectionBackground");
            selectedTextForeground = UIManager.getColor("Tree.selectionForeground");

            if (UIUtils.isGtkLookAndFeel())
                setBorderSelectionColor(null);

        }

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean bSelected, boolean bExpanded, boolean bLeaf, int iRow, boolean bHasFocus) {

            if (value instanceof DefaultTreeRootNode)
                setIcon(driverRootImage);
            else if (value instanceof DatabaseDriverNode)
                setIcon(driverImage);

            String labelText = value.toString();
            setText(labelText);
            setToolTipText(labelText);

            this.selected = bSelected;
            setBackground(selected ? selectedBackground : textBackground);
            setForeground(selected ? selectedTextForeground : textForeground);

            return this;
        }


    } // class DriversTreeCellRenderer

    private class PopMenu extends JPopupMenu implements ActionListener {

        private final JMenuItem addNewDriver;
        private final JMenuItem delete;
        private final JMenuItem duplicate;
        private final JMenuItem properties;

        protected TreePath popupPath;
        protected DatabaseDriver hover;

        public PopMenu() {

            addNewDriver = MenuItemFactory.createMenuItem(bundleString("newDriver"));
            addNewDriver.addActionListener(this);

            duplicate = MenuItemFactory.createMenuItem(bundleString("duplicate"));
            duplicate.addActionListener(this);

            delete = MenuItemFactory.createMenuItem(bundleString("remove"));
            delete.addActionListener(this);

            properties = MenuItemFactory.createMenuItem(bundleString("driverProperties"));
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
                delete.setText(bundleString("deleteDriver") + " " + name);
                duplicate.setText(String.format(bundleString("duplicateLabel"), name));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            try {

                Object source = e.getSource();
                if (source.equals(duplicate)) {

                    if (hover != null) {

                        String name = buildDriverName(hover.getName() + " (" + bundleString("copy")) + ")";
                        DatabaseDriver dd = databaseDriverFactory().create(name);

                        dd.setClassName(hover.getClassName());
                        dd.setDatabaseType(hover.getType());
                        dd.setDescription(hover.getDescription());
                        dd.setId(System.currentTimeMillis());
                        dd.setPath(hover.getPath());
                        dd.setURL(hover.getURL());

                        newDriver(dd);
                    }

                } else if (source.equals(delete)) {

                    if (popupPath != null) {
                        DatabaseDriverNode node = (DatabaseDriverNode) popupPath.getLastPathComponent();
                        deleteDriver(node);
                    }

                } else if (source.equals(properties)) {

                    reloadView = true;
                    setSelectedDriver(hover);
                    checkDriversPanel();
                    getDriverPanelFromBrowser();

                } else if (source.equals(addNewDriver))
                    newDriver();

            } finally {
                reloadView = false;
                hover = null;
                popupPath = null;
            }

        }

    } // class PopMenu

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {

            if (e.getClickCount() < 2)
                return;

            TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path == tree.getSelectionPath()) {
                checkDriversPanel();
                getDriverPanelFromBrowser();
            }

        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {

                if (popupMenu == null)
                    popupMenu = new PopMenu();

                Point point = new Point(e.getX(), e.getY());
                popupMenu.popupPath = tree.getPathForLocation(point.x, point.y);
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

}
