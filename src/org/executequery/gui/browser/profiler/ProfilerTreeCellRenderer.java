package org.executequery.gui.browser.profiler;

import org.executequery.gui.IconManager;
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
                setIcon(IconManager.getIcon("icon_execute_statement"));
                break;
            case ProfilerData.FUNCTION:
                setIcon(IconManager.getIcon("icon_db_function"));
                break;
            case ProfilerData.PROCEDURE:
                setIcon(IconManager.getIcon("icon_db_procedure"));
                break;
            case ProfilerData.SELF_TIME:
                setIcon(IconManager.getIcon("icon_information"));
                break;
            case ProfilerData.PSQL:
                setIcon(IconManager.getIcon("icon_sql_line"));
                break;
            case ProfilerData.ROOT:
                setIcon(IconManager.getIcon("icon_utility"));
                break;
            default:
                setIcon(IconManager.getIcon("icon_file_default"));
                break;
        }

        return this;
    }

}
