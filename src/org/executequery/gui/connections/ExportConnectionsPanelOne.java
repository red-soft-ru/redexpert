/*
 * ExportConnectionsPanelOne.java
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

package org.executequery.gui.connections;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.IconManager;
import org.executequery.gui.browser.BrowserConstants;
import org.executequery.gui.browser.ConnectionsFolder;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.swing.tree.CheckTreeManager;
import org.underworldlabs.swing.tree.CheckTreeSelectionModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExportConnectionsPanelOne extends ActionPanel {

    private CheckTreeManager checkTreeManager;

    public ExportConnectionsPanelOne() {

        super(new GridBagLayout());
        init();
    }

    private void init() {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Database Connections");

        List<ConnectionsFolder> folders = folders();
        List<DatabaseConnection> connectionsAdded = new ArrayList<DatabaseConnection>();
        for (ConnectionsFolder folder : folders) {

            List<DatabaseConnection> connections = folder.getConnections();
            if (!connections.isEmpty()) {

                DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
                for (DatabaseConnection connection : connections) {

                    MutableTreeNode childNode = new DefaultMutableTreeNode(connection);
                    folderNode.add(childNode);
                    connectionsAdded.add(connection);
                }

                root.add(folderNode);
            }

        }

        for (DatabaseConnection connection : connections()) {

            if (!connectionsAdded.contains(connection)) {

                MutableTreeNode childNode = new DefaultMutableTreeNode(connection);
                root.add(childNode);
            }

        }

        JTree tree = new JTree(root);
        tree.setCellRenderer(new ImportExportConnectionsTreeCellRenderer());
        checkTreeManager = new CheckTreeManager(tree);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridheight = 1;
        gbc.insets.top = 7;
        gbc.insets.bottom = 10;
        gbc.insets.right = 5;
        gbc.insets.left = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        add(new JLabel(bundleString("SelectTheConnectionsAndOrFoldersYouWishToExportBelow")), gbc);

        gbc.gridy++;
        add(new JLabel(bundleString("PasswordsWillBeExportedAsTheyAreStoredIfYou")), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(tree), gbc);

        tree.setRowHeight(28);
        tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        setPreferredSize(new Dimension(650, 500));
    }

    public CheckTreeSelectionModel getSelectionModel() {

        return checkTreeManager.getSelectionModel();
    }

    public boolean canProceed() {

        if (checkTreeManager.getSelectionModel().getSelectionCount() == 0) {

            GUIUtilities.displayErrorMessage(bundleString("YouMustSelectAtLeastOneConnectionOrFolderToExport"));
            return false;
        }
        return true;
    }

    protected List<ConnectionsFolder> folders() {

        return connectionFolderRepository().findAll();
    }

    private ConnectionFoldersRepository connectionFolderRepository() {

        return (ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
    }

    protected List<DatabaseConnection> connections() {

        return databaseConnectionRepository().findAll();
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }


    private static class ImportExportConnectionsTreeCellRenderer extends AbstractTreeCellRenderer {

        private final Color textForeground;
        private final Color selectedTextForeground;
        private final Color selectedBackground;

        public ImportExportConnectionsTreeCellRenderer() {

            textForeground = UIManager.getColor("Tree.textForeground");
            selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
            selectedBackground = UIManager.getColor("Tree.selectionBackground");

            setIconTextGap(10);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            if (UIUtils.isGtkLookAndFeel())
                setBorderSelectionColor(null);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
                                                      boolean isLeaf, int row, boolean hasFocus) {
            this.hasFocus = hasFocus;
            this.selected = isSelected;

            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof ConnectionsFolder) {
                setIcon(IconManager.getIcon(BrowserConstants.FOLDER_IMAGE));

            } else if (userObject instanceof DatabaseConnection)
                setIcon(IconManager.getIcon(BrowserConstants.HOST_NOT_CONNECTED_IMAGE));

            setText(userObject.toString());
            setBackgroundSelectionColor(selectedBackground);
            setForeground(selected ? selectedTextForeground : textForeground);

            return this;
        }

    } // ImportExportConnectionsTreeCellRenderer class

}
