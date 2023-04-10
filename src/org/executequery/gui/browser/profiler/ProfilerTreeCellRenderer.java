package org.executequery.gui.browser.profiler;

import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof ProfilerData) {

            ProfilerData data = (ProfilerData) userObject;
            String prefix = data.getCallCount() == 1 ?
                    "[" + data.getTotalTime() + "ms]   " :
                    "[" + data.getTotalTime() + "ms [" + data.getCallCount() + " times, ~" + data.getAvgTime() + "ms]]   ";
            String record = prefix + data.getProcessName();
            setText(record);
        }

        return this;
    }
}
