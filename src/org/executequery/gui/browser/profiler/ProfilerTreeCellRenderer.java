package org.executequery.gui.browser.profiler;

import org.executequery.gui.IconManager;
import org.underworldlabs.swing.tree.AbstractTreeCellRenderer;

import javax.swing.*;
import java.awt.*;

public class ProfilerTreeCellRenderer extends AbstractTreeCellRenderer {

    private final Color textForeground;
    private final Color selectedTextForeground;

    public ProfilerTreeCellRenderer() {
        textForeground = UIManager.getColor("Tree.textForeground");
        selectedTextForeground = UIManager.getColor("Tree.selectionForeground");
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        this.selected = selected;
        this.hasFocus = hasFocus;

        ProfilerTreeTableNode node = (ProfilerTreeTableNode) value;
        setForeground(selected && hasFocus ? selectedTextForeground : textForeground);
        setText((String) node.getProcessName());

        selected &= hasFocus;
        switch ((String) node.getProcessType()) {
            case ProfilerData.BLOCK:
                setIcon(IconManager.getIcon("icon_execute_statement", selected));
                break;
            case ProfilerData.FUNCTION:
                setIcon(IconManager.getIcon("icon_db_function", selected));
                break;
            case ProfilerData.PROCEDURE:
                setIcon(IconManager.getIcon("icon_db_procedure", selected));
                break;
            case ProfilerData.SELF_TIME:
                setIcon(IconManager.getIcon("icon_information", selected));
                break;
            case ProfilerData.PSQL:
                setIcon(IconManager.getIcon("icon_sql_line", selected));
                break;
            case ProfilerData.ROOT:
                setIcon(IconManager.getIcon("icon_utility", selected));
                break;
            default:
                setIcon(IconManager.getIcon("icon_file_default", selected));
                break;
        }

        return this;
    }

}
