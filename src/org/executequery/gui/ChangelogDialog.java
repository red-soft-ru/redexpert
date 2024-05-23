package org.executequery.gui;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChangelogDialog extends InformationDialog {

    private final JTree navigationTree;

    public ChangelogDialog(String name, String value, int valueType, String charSet) {
        super(name, value, valueType, charSet, "text/html");

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        Matcher matcher = Pattern.compile("<h2>.*</h2>").matcher(loadedText);
        while (matcher.find()) {
            String machedString = matcher.group();
            root.add(new NavigationTreeNode(machedString));
        }

        navigationTree = new JTree(root);
        navigationTree.setRootVisible(false);
        navigationTree.setCellRenderer(new NavigationTreeRenderer());
        navigationTree.addMouseListener(new NavigationTreeMouseAdapter());
    }

    @Override
    protected JPanel buildDisplayComponent() {

        JPanel viewPanel = new JPanel(new GridBagLayout());
        viewPanel.setPreferredSize(new Dimension(800, 500));

        GridBagHelper gbh = new GridBagHelper().setInsets(5, 5, 0, 5).fillBoth().spanY();
        viewPanel.add(new JScrollPane(navigationTree), gbh.setWeightX(0.15).get());
        viewPanel.add(new JScrollPane(editorPane), gbh.nextCol().setMaxWeightX().rightGap(5).spanX().get());

        return viewPanel;
    }

    private static final class NavigationTreeNode extends DefaultMutableTreeNode {

        private String nodeText;

        NavigationTreeNode(String nodeText) {
            super();

            Matcher matcher = Pattern.compile("v[\\d.]*").matcher(nodeText);
            if (matcher.find())
                this.nodeText = matcher.group();
        }

        @Override
        public String toString() {
            return nodeText;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

    } // NavigationTreeNode class

    private static final class NavigationTreeRenderer extends AbstractTreeCellRenderer {

        private final Color textBackground;
        private final Color textForeground;
        private final Color selectionBackground;
        private final Color selectionForeground;

        public NavigationTreeRenderer() {

            textBackground = UIManager.getColor("Tree.textBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");

            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            this.selected = selected;
            this.hasFocus = hasFocus;

            if (value instanceof NavigationTreeNode) {
                NavigationTreeNode treeNode = (NavigationTreeNode) value;

                setBackground(selected ? selectionBackground : textBackground);
                setForeground(selected ? selectionForeground : textForeground);

                setText(treeNode.nodeText);
                setIcon(GUIUtilities.loadIcon("Information16.png"));
            }

            return this;
        }

    } // NavigationTreeRenderer class

    private final class NavigationTreeMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {

            TreePath path = navigationTree.getPathForLocation(e.getX(), e.getY());
            if (path == null || path != navigationTree.getSelectionPath())
                return;

            Object selectedComponent = navigationTree.getSelectionPath().getLastPathComponent();
            if (selectedComponent instanceof NavigationTreeNode) {
                NavigationTreeNode treeNode = (NavigationTreeNode) selectedComponent;
                editorPane.scrollToReference(treeNode.nodeText);
            }
        }

    } // NavigationTreeMouseAdapter class

}
