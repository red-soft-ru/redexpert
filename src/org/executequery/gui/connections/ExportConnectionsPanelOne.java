package org.executequery.gui.connections;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
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
        
        add(new JLabel("Select the connections and/or folders you wish to export below."), gbc);        

        gbc.gridy++;
        add(new JLabel("<html><b><i>Note: </i></b>Passwords will be exported as they are stored - if you "
                + "have selected that they be encrypted, they will be exported encrypted, otherwise in plain text.</html>"), gbc);        
        
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
            
            GUIUtilities.displayErrorMessage("You must select at least one connection or folder to export");
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
