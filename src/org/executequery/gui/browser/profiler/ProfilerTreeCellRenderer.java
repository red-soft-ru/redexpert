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
            case "BLOCK":
                setIcon(GUIUtilities.loadIcon("CreateScripts16.png"));
                break;
            case "FUNCTION":
                setIcon(GUIUtilities.loadIcon("Function16.png"));
                break;
            case "PROCEDURE":
                setIcon(GUIUtilities.loadIcon("Procedure16.png"));
                break;
            case "SELF_TIME":
                setIcon(GUIUtilities.loadIcon("Information16.png"));
                break;
            case "ROOT":
                setIcon(GUIUtilities.loadIcon("JDBCDriver16.png"));
                break;
            default:
                setIcon(GUIUtilities.loadIcon("DefaultFile16.png"));
                break;
        }

        return this;
    }

}
