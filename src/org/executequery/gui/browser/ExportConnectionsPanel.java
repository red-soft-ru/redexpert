/*
 * ExportConnectionsPanel.java
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

package org.executequery.gui.browser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.StringUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.WidgetFactory;
import org.executequery.repository.ConnectionExporter;
import org.executequery.repository.ConnectionFoldersRepository;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.repository.RepositoryException;
import org.underworldlabs.swing.ActionPanel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;
import org.underworldlabs.swing.tree.CheckTreeManager;
import org.underworldlabs.swing.tree.CheckTreeSelectionModel;

/** 
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ExportConnectionsPanel extends ActionPanel {
    
    public static final String TITLE = "Export Connections";
    public static final String FRAME_ICON = "ExportConnections16.png";
    
    private JTextField fileNameField;

    private ActionContainer parent;
    private CheckTreeManager checkTreeManager;

    public ExportConnectionsPanel(ActionContainer parent) {

        super(new BorderLayout());
        this.parent = parent;

        try  {

            init();

        } catch (Exception e) {
          
            e.printStackTrace();
        }

    }
    
    private void init() throws Exception {
        
        fileNameField = WidgetFactory.createTextField();

        JButton button = WidgetFactory.createInlineFieldButton("Browse");
        button.setActionCommand("browse");
        button.addActionListener(this);
        button.setMnemonic('r');

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
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        
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
        
        mainPanel.add(new JLabel("Select the connections and/or folders you wish to export below."), gbc);        

        gbc.gridy++;
        mainPanel.add(new JLabel("<html><b><i>Note: </i></b>Passwords will be exported as they are stored - if you "
                + "have selected that they be encrypted, they will be exported encrypted, otherwise in plain text.</html>"), gbc);        
        
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(tree), gbc);        
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.insets.top = 5;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        mainPanel.add(new JLabel("Export File:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(fileNameField, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets.left = 0;
        mainPanel.add(button, gbc);
        
        mainPanel.setBorder(BorderFactory.createEtchedBorder());
        
        BottomButtonPanel buttonPanel = new BottomButtonPanel(this, "Export", "export-connections", true);
        buttonPanel.setOkButtonActionCommand("doExport");
        
        JPanel base = new JPanel(new BorderLayout());
        base.add(mainPanel, BorderLayout.CENTER);
        base.add(buttonPanel, BorderLayout.SOUTH);
        
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(base, BorderLayout.CENTER);

        tree.setRowHeight(28);
        setPreferredSize(new Dimension(650, 500));
    }

    public void doExport() {
        
        GUIUtils.startWorker(new Runnable() {
            public void run() {
                try {

                    parent.block();
                    boolean result = create();

                    if (result) {
                    
                        GUIUtilities.displayInformationMessage("The selected connections have exported to - \n\n " 
                                + fileNameField.getText());

                        parent.finished();
                    }
                        
                } finally {

                    parent.unblock();
                }
            }
        });

    }
    
    private boolean create() {
        
        String text = fileNameField.getText();
        if (StringUtils.isBlank(text)) {
            
            GUIUtilities.displayErrorMessage("You must select a file path to export to");
            return false;
        }
        
        CheckTreeSelectionModel selectionModel = checkTreeManager.getSelectionModel();
        if (selectionModel.getSelectionCount() == 0) {
            
            GUIUtilities.displayErrorMessage("You must select at least one connection or folder to export");
            return false;
        }
        
        List<ConnectionsFolder> folders = new ArrayList<>();
        List<DatabaseConnection> connections = new ArrayList<>();
        
        TreePath[] selectionPaths = selectionModel.getSelectionPaths();
        for (TreePath treePath : selectionPaths) {

            Object object = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
            if (object instanceof ConnectionsFolder) {
                
                ConnectionsFolder folder = (ConnectionsFolder) object;
                folders.add(folder);
                connections.addAll(folder.getConnections());
                
            } else if (object instanceof DatabaseConnection) {
                
                connections.add((DatabaseConnection) object);

            } else if (treePath.getParentPath() == null) {
                
                folders.addAll(folders());
                connections.addAll(connections());
                break;
            }
            
        }

        try {
        
            new ConnectionExporter().write(fileNameField.getText(), folders, connections);
            return true;
            
        } catch (RepositoryException e) {
            
            GUIUtilities.displayExceptionErrorDialog(
                    "Error writing connections to file.\n\nThe system returned:\n" + e.getMessage(), e);
            return false;
        }

    }
    
    public void browse() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        fileChooser.setDialogTitle("Select Export File Path");
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);

        int result = fileChooser.showDialog(GUIUtilities.getInFocusDialogOrWindow(), "Select");
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        File file = fileChooser.getSelectedFile();
        fileNameField.setText(file.getAbsolutePath());
    }

    private List<ConnectionsFolder> folders() {

        return connectionFolderRepository().findAll();
    }

    private ConnectionFoldersRepository connectionFolderRepository() {
        
        return (ConnectionFoldersRepository) RepositoryCache.load(ConnectionFoldersRepository.REPOSITORY_ID);
    }

    private List<DatabaseConnection> connections() {
        
        return databaseConnectionRepository().findAll();
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID);
    }

    
    static class ImportExportConnectionsTreeCellRenderer extends AbstractTreeCellRenderer {
    
        private Map<String, Icon> icons;
        private Color textForeground;
        private Color selectedTextForeground;
        private Color selectedBackground;

        public ImportExportConnectionsTreeCellRenderer() {

            textForeground = UIManager.getColor("Tree.textForeground");
            selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
            selectedBackground = UIManager.getColor("Tree.selectionBackground");

            setIconTextGap(10);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            
            if (UIUtils.isGtkLookAndFeel()) {

                setBorderSelectionColor(null);
            }

            icons = new HashMap<String, Icon>();
            for (int i = 0; i < BrowserConstants.NODE_ICONS.length; i++) {

                icons.put(BrowserConstants.NODE_ICONS[i], GUIUtilities.loadIcon(BrowserConstants.NODE_ICONS[i], true));
            }
            
        }
     
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean isExpanded,
                boolean isLeaf, int row, boolean hasFocus) {

            this.hasFocus = hasFocus;
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) value;

            Object userObject = child.getUserObject();
            if (userObject instanceof ConnectionsFolder) {
                
                setIcon(icons.get(BrowserConstants.CONNECTIONS_FOLDER_IMAGE));
                
            } else if (userObject instanceof DatabaseConnection) {
                
                setIcon(icons.get(BrowserConstants.HOST_NOT_CONNECTED_IMAGE));
            }

            setText(userObject.toString());
            setBackgroundSelectionColor(selectedBackground);
            
            this.selected = isSelected;
            if(!selected) {

                setForeground(textForeground);

            } else {
                
                setForeground(selectedTextForeground);
            }
            
            return this;
        }

    }

}

