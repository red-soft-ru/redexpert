/*
 * ConnectionsListPanel.java
 *
 * Copyright (C) 2002-2013 Takis Diakoumis
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

package org.executequery.gui.browser;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.ConnectionRepositoryListener;
import org.executequery.gui.SortableColumnsTable;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.forms.AbstractFormObjectViewPanel;
import org.executequery.print.TablePrinter;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.menu.MenuItemFactory;
import org.underworldlabs.swing.table.AbstractSortableTableModel;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision$
 * @date     $Date$
 */
public class ConnectionsListPanel extends AbstractFormObjectViewPanel
                                  implements MouseListener,
                                             ActionListener,
                                             ConnectionListener,
                                             ConnectionRepositoryListener {

    public static final String NAME = "ConnectionsListPanel";

    /** the table display */
    private JTable table;

    /** the table model */
    private ConnectionsTableModel model;

    /** the browser's control object */
    private BrowserController controller;

    /** the pop-up menu */
    private PopMenu popupMenu;

    public ConnectionsListPanel(BrowserController controller) {
        super();
        this.controller = controller;
        init();
    }

    private void init() {

        model = new ConnectionsTableModel(connections());
        table = new SortableColumnsTable(model);
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);

        // add the mouse listener for selection clicks
        table.addMouseListener(this);

        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(30);
        tcm.getColumn(0).setMaxWidth(30);
        tcm.getColumn(1).setPreferredWidth(135);
        tcm.getColumn(2).setPreferredWidth(60);
        tcm.getColumn(3).setPreferredWidth(60);
        tcm.getColumn(4).setPreferredWidth(60);

        tcm.getColumn(0).setCellRenderer(new ConnectCellRenderer());

        // new connection button
        JButton button = WidgetFactory.createButton("New Connection");
        button.addActionListener(this);

        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.add(new JScrollPane(table), getPanelConstraints());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Available Connections"));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10,10,5,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("User defined database connection parameters."), gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Select the New Connection button to add a new " +
                             "connection to the system"), gbc);
        gbc.gridx = 1;
        gbc.insets.left = 0;
        gbc.insets.bottom = 0;
        panel.add(button, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.insets.left = 10;
        gbc.insets.top = 10;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(tablePanel, gbc);

        setHeaderText("Database Connections");
        setHeaderIcon(GUIUtilities.loadIcon("DatabaseConnect24.png"));
        setContentPanel(panel);

        // register with the event listener
        EventMediator.registerListener(this);
    }

    public void connectionAdded(ConnectionRepositoryEvent connectionRepositoryEvent) {
        connectionsChanged();
    }
    
    public void connectionModified(ConnectionRepositoryEvent connectionRepositoryEvent) {
        connectionsChanged();
    }
    
    public void connectionRemoved(ConnectionRepositoryEvent connectionRepositoryEvent) {
        connectionsChanged();
    }
    
    public void selected(ConnectionsFolder folder) {
        
        if (folder == null) {
            
            connectionsChanged();
        
        } else {
            
            connectionsChanged(folder.getConnections());
        }
    }
    
    private void connectionsChanged() {
        connectionsChanged(connections());
    }
    
    private void connectionsChanged(List<DatabaseConnection> connections) {
        model.reload(connections);
        table.repaint();
    }
    
    private List<DatabaseConnection> connections() {

        return connectionsRepository().findAll();
    }

    private DatabaseConnectionRepository connectionsRepository() {

        return (DatabaseConnectionRepository)RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }

    public void actionPerformed(ActionEvent e) {
        GUIUtilities.ensureDockedTabVisible(ConnectionsTreePanel.PROPERTY_KEY);
        controller.addNewConnection();
    }

    private List<DatabaseConnection> getConnectionsAt(Point point) {

        int row = table.rowAtPoint(point);
        if (row == -1) {

            return null;
        }

        boolean selectionsContainPoint = false;
        int[] selectedRows = table.getSelectedRows();
        for (int selectedRow : selectedRows) {

            if (selectedRow == row) {

                selectionsContainPoint = true;
                break;
            }

        }

        List<DatabaseConnection> selectedConnections = new ArrayList<DatabaseConnection>();
        if (!selectionsContainPoint) {

            selectedConnections.add(model.getConnectionAt(row));

        } else {

            for (int i = 0; i < selectedRows.length; i++) {

                selectedConnections.add(model.getConnectionAt(selectedRows[i]));
            }

        }

        return selectedConnections;
    }

    private List<DatabaseConnection> getSelectedConnections() {

        int[] selectedRows = table.getSelectedRows();
        List<DatabaseConnection> selectedConnections = new ArrayList<DatabaseConnection>();
        for (int i = 0; i < selectedRows.length; i++) {

            selectedConnections.add(model.getConnectionAt(selectedRows[i]));
        }
        return selectedConnections;
    }


    // ----------------------------------
    // MouseListener implementation
    // ----------------------------------

    public void mousePressed(MouseEvent e) {

        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {

        maybeShowPopup(e);
    }

    public void mouseClicked(MouseEvent e) {

        // only interested in double clicks
        if (e.getClickCount() < 2) {

            return;
        }

        Point point = new Point(e.getX(), e.getY());
        List<DatabaseConnection> list = getConnectionsAt(point);
        if (list.isEmpty()) {

            return;
        }

        DatabaseConnection databaseConnection = list.get(0);
        int col = table.columnAtPoint(point);
        if (col == 0) {

            if (!databaseConnection.isConnected()) {

                controller.connect(databaseConnection);

            } else {

                controller.disconnect(databaseConnection);
            }

            return;
        }

        if (list.size() == 1) {

            // select the connection in the tree
            if (model.indexOf(databaseConnection) < model.getRowCount()) {

                controller.setSelectedConnection(databaseConnection);
            }

        }

    }

    private void maybeShowPopup(MouseEvent e) {

        if (e.isPopupTrigger()) {

            Point point = new Point(e.getX(), e.getY());

            // get the connections at this point
            List<DatabaseConnection> list = getConnectionsAt(point);
            if (!list.isEmpty()) {

                if (list.size() == 1) {

                    int row = model.indexOf(list.get(0));
                    table.clearSelection();
                    table.addRowSelectionInterval(row, row);
                }

                popupMenu();
                popupMenu.setToConnect(list);
                popupMenu.show(e.getComponent(), point.x, point.y);
            }

        }

    }

    private void popupMenu() {

        if (popupMenu == null) {

            popupMenu = new PopMenu();
        }

    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}


    /**
     * Indicates a connection has been established.
     *
     * @param the encapsulating event
     */
    public void connected(ConnectionEvent connectionEvent) {
        if (isVisible()) {
            DatabaseConnection dc = connectionEvent.getDatabaseConnection();
            int index = model.indexOf(dc);
            model.fireTableCellUpdated(index, 0);
        }
    }

    /**
     * Indicates a connection has been closed.
     *
     * @param the encapsulating event
     */
    public void disconnected(ConnectionEvent connectionEvent) {
        if (isVisible()) {
            DatabaseConnection dc = connectionEvent.getDatabaseConnection();
            int index = model.indexOf(dc);
            model.fireTableCellUpdated(index, 0);
        }
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ConnectionEvent) || (event instanceof ConnectionRepositoryEvent);
    }

    public String getLayoutName() {
        return NAME;
    }

    public void refresh() {}
    public void cleanup() {}

    public Printable getPrintable() {
        return new TablePrinter(table, "Database Connections", false);
    }

    /** The table's popup menu function */
    private class PopMenu extends JPopupMenu implements ActionListener {

        private JMenuItem connect;
        private JMenuItem disconnect;
        private JMenuItem properties;

        public PopMenu() {
            connect = MenuItemFactory.createMenuItem("Connect");
            connect.addActionListener(this);

            disconnect = MenuItemFactory.createMenuItem("Disconnect");
            disconnect.addActionListener(this);

            properties = MenuItemFactory.createMenuItem("Properties");
            properties.addActionListener(this);

            add(connect);
            add(disconnect);
            addSeparator();
            add(properties);
        }

        public void setToConnect(List<DatabaseConnection> list) {

            boolean canConnect = false;
            boolean canDisconnect = false;
            for (DatabaseConnection databaseConnection : list) {

                if (databaseConnection.isConnected()) {

                    canDisconnect = true;

                } else {

                    canConnect = true;
                }

            }

            connect.setEnabled(canConnect);
            disconnect.setEnabled(canDisconnect);

            if (list.size() > 1) {

                properties.setEnabled(false);
            }

        }

        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            List<DatabaseConnection> selectedConnections = getSelectedConnections();
            if (source == connect) {

                for (DatabaseConnection databaseConnection : selectedConnections) {

                    controller.connect(databaseConnection);
                }

            } else if (source == disconnect) {

                for (DatabaseConnection databaseConnection : selectedConnections) {

                    controller.disconnect(databaseConnection);
                }

            } else if (source == properties) {

                controller.setSelectedConnection(selectedConnections.get(0));
            }

        }

    }

    private class ConnectionsTableModel extends AbstractSortableTableModel {

        private List<DatabaseConnection> values;
        private String[] header = {"", "Connection Name", "Host",
                                   "Data Source", "User", "Driver"};

        public ConnectionsTableModel(List<DatabaseConnection> values) {
            this.values = values;
        }

        public void reload(List<DatabaseConnection> values) {
            this.values = values;
            fireTableDataChanged();
        }
        
        public DatabaseConnection getConnectionAt(int row) {
            return values.get(row);
        }

        public int indexOf(DatabaseConnection dc) {
            return values.indexOf(dc);
        }

        public int getRowCount() {
            return values.size();
        }

        public int getColumnCount() {
            return header.length;
        }

        public String getColumnName(int col) {
            return header[col];
        }

        public Object getValueAt(int row, int col) {

            DatabaseConnection databaseConnection = values.get(row);
            if (databaseConnection != null) {
                switch (col) {
                    case 0:
                        return Boolean.valueOf(databaseConnection.isConnected());
                    case 1:
                        return databaseConnection.getName();
                    case 2:
                        return databaseConnection.getHost();
                    case 3:
                        return databaseConnection.getSourceName();
                    case 4:
                        return databaseConnection.getUserName();
                    case 5:
                        return databaseConnection.getDriverName();
                }
            } else {
                
                // check the rest - failure reported when conns file is corrupted
                for (int i = 0, n = values.size(); i < n; i++) {

                    if (values.size() > 0 && values.get(i) == null) {
                        
                        values.remove(i);
                        i--;
                    }
                    
                }
                fireTableDataChanged();
            }
            return databaseConnection;
        }

    }


    private class ConnectCellRenderer extends JLabel
                                      implements TableCellRenderer {

        // connection icons
        private ImageIcon connectedImage;
        private ImageIcon notConnectedImage;

        public ConnectCellRenderer() {
            connectedImage = GUIUtilities.loadIcon("Connected.png", true);
            notConnectedImage = GUIUtilities.loadIcon("Disconnected.png", true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                    Object value, boolean isSelected, boolean hasFocus,
                                    int row, int column) {

            Boolean connected = (Boolean) value;

            setHorizontalAlignment(JLabel.CENTER);

            if (connected != null && connected) {
                setIcon(connectedImage);
                setToolTipText("Connected (double-click icon to disconnect)");
            } else {
                setIcon(notConnectedImage);
                setToolTipText("Disconnected (double-click icon to connect)");
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
        }

        public boolean isOpaque() {
            return true;
        }

    }

}
