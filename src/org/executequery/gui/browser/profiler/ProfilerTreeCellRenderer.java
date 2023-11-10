package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        ProfilerTreeTableNode node = (ProfilerTreeTableNode) value;
        setText((String) node.getProcessName());

        switch ((String) node.getProcessType()) {
            case ProfilerData.BLOCK:
                setIcon(GUIUtilities.loadIcon("CreateScripts16.svg"));
                break;
            case ProfilerData.FUNCTION:
                setIcon(GUIUtilities.loadIcon("Function16.svg"));
                break;
            case ProfilerData.PROCEDURE:
                setIcon(GUIUtilities.loadIcon("Procedure16.svg"));
                break;
            case ProfilerData.SELF_TIME:
                setIcon(GUIUtilities.loadIcon("Information16.svg"));
                break;
            case ProfilerData.PSQL:
                setIcon(GUIUtilities.loadIcon("ShiftTextRight16.svg"));
                break;
            case ProfilerData.ROOT:
                setIcon(GUIUtilities.loadIcon("JDBCDriver16.svg"));
                break;
            default:
                setIcon(GUIUtilities.loadIcon("DefaultFile16.svg"));
                break;
        }

        return this;
    }

}
