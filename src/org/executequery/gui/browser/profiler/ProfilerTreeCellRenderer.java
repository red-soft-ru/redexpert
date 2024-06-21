package org.executequery.gui.browser.profiler;

import org.executequery.GUIUtilities;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        this.selected = selected;
        this.hasFocus = hasFocus;

        ProfilerTreeTableNode node = (ProfilerTreeTableNode) value;
        setText((String) node.getProcessName());

        switch ((String) node.getProcessType()) {
            case ProfilerData.BLOCK:
                setIcon(GUIUtilities.loadIcon("icon_create_script"));
                break;
            case ProfilerData.FUNCTION:
                setIcon(GUIUtilities.loadIcon("icon_db_function"));
                break;
            case ProfilerData.PROCEDURE:
                setIcon(GUIUtilities.loadIcon("icon_db_procedure"));
                break;
            case ProfilerData.SELF_TIME:
                setIcon(GUIUtilities.loadIcon("icon_information"));
                break;
            case ProfilerData.PSQL:
                setIcon(GUIUtilities.loadIcon("icon_sql_line"));
                break;
            case ProfilerData.ROOT:
                setIcon(GUIUtilities.loadIcon("icon_utility"));
                break;
            default:
                setIcon(GUIUtilities.loadIcon("icon_file_default"));
                break;
        }

        return this;
    }

}
