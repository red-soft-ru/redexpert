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
            FormattedTime totalTime = new FormattedTime(data.getTotalTime());
            FormattedTime avgTime = new FormattedTime(data.getAvgTime());

            String prefix = data.getCallCount() == 1 ?
                    String.format("[%s %s]   ",
                            totalTime.getTime(), totalTime.getLabel()
                    ) :
                    String.format("[%s %s [%d times, ~%s %s]]   ",
                            totalTime.getTime(), totalTime.getLabel(), data.getCallCount(), avgTime.getTime(), avgTime.getLabel()
                    );

            String record = prefix + data.getProcessName();
            setText(record);
        }

        return this;
    }

    private static class FormattedTime {

        private static final long NS_MAX = 100000;
        private static final long MS_MAX = NS_MAX * 10000;
        private static final long S_MAX = MS_MAX * 60000;

        private double time;
        private String label;

        FormattedTime (double time) {

            this.time = time;
            this.label = "ns";

            if (time >= NS_MAX) {
                if (time >= MS_MAX) {
                    if (time >= S_MAX) {
                        this.time /= S_MAX;
                        this.label = "m";
                    } else {
                        this.time /= MS_MAX;
                        this.label = "s";
                    }
                } else {
                    this.time /= NS_MAX;
                    this.label = "ms";
                }
            }

        }

        public String getTime() {
            return String
                    .format((label.equals("ms") || label.equals("ns") ? "%.0f" : "%.2f"), time)
                    .replace(',', '.');
        }

        public String getLabel() {
            return label;
        }

    }

}
