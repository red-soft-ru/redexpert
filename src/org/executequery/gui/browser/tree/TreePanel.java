package org.executequery.gui.browser.tree;

import org.executequery.gui.AbstractDockedTabActionPanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;

import javax.swing.tree.TreePath;
import java.awt.*;

public abstract class TreePanel extends AbstractDockedTabActionPanel {
    public final static int DEFAULT = 0;
    public final static int DEPENDED_ON = 1;
    public final static int DEPENDENT = 2;

    public TreePanel() {
    }

    public TreePanel(LayoutManager layout) {
        super(layout);
    }

    public abstract void pathChanged(TreePath oldPath, TreePath newPath);

    public abstract void pathExpanded(TreePath path);

    public abstract void valueChanged(DatabaseObjectNode node);

    public abstract void connectionNameChanged(String name);

    public abstract void rebuildConnectionsFromTree();

}