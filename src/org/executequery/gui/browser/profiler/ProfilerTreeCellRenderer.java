package org.executequery.gui.browser.profiler;

import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        ProfilerTreeTableNode node = (ProfilerTreeTableNode) value;
        setText(node.getData().getProcessName());

        return this;
    }

}
