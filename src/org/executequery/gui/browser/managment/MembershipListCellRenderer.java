package org.executequery.gui.browser.managment;

import org.executequery.GUIUtilities;
import org.executequery.components.table.RowHeaderRenderer;
import org.executequery.gui.browser.UserManagerPanel;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class MembershipListCellRenderer extends RowHeaderRenderer {
    private ImageIcon roleIcon;
    private ImageIcon userIcon;

    /**
     * Creates a default table cell renderer.
     *
     * @param table
     */
    public MembershipListCellRenderer(JTable table) {
        super(table);
        roleIcon = GUIUtilities.loadIcon("user_manager_16.png");
        userIcon = GUIUtilities.loadIcon("User16.png");
    }

    public Component getListCellRendererComponent(JList table, Object value, int index,
                                                  boolean isSelected, boolean hasFocus) {
        if (table == null) {
            return this;
        }

        Color fg = null;
        Color bg = null;


        if (hasFocus) {
            Border border = null;
            if (isSelected) {
                border = DefaultLookup.getBorder(this, ui, "Table.focusSelectedCellHighlightBorder");
                super.setForeground(fg == null ? table.getSelectionForeground()
                        : fg);
                super.setBackground(bg == null ? table.getSelectionBackground()
                        : bg);
            }
            if (border == null) {
                border = DefaultLookup.getBorder(this, ui, "Table.focusCellHighlightBorder");
            }
            setBorder(border);

            if (!isSelected) {
                Color col;
                col = DefaultLookup.getColor(this, ui, "Table.focusCellForeground");
                if (col != null) {
                    super.setForeground(col);
                }
                col = DefaultLookup.getColor(this, ui, "Table.focusCellBackground");
                if (col != null) {
                    super.setBackground(col);
                }
            }
        } else {
            Color background = unselectedBackground != null
                    ? unselectedBackground
                    : table.getBackground();
            if (background == null || background instanceof javax.swing.plaf.UIResource) {
                Color alternateColor = DefaultLookup.getColor(this, ui, "Table.alternateRowColor");
                if (alternateColor != null && index % 2 != 0) {
                    background = alternateColor;
                }
            }
            super.setForeground(unselectedForeground != null
                    ? unselectedForeground
                    : table.getForeground());
            super.setBackground(background);
            //setBorder(getNoFocusBorder());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        }
        setFont(table.getFont());
        setValue(value);

        return this;
    }

    protected void setValue(Object value) {

        if (value.getClass().equals(String.class)) {
            setIcon(null);
            setText((String) value);
        }
        if (value.getClass().equals(UserManagerPanel.UserRole.class)) {
            UserManagerPanel.UserRole userRole = (UserManagerPanel.UserRole) value;
            setText(userRole.name);
            if (userRole.isUser)
                setIcon(userIcon);
            else setIcon(roleIcon);
        }
    }
}
