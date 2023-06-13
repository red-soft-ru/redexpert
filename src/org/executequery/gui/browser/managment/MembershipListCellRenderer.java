package org.executequery.gui.browser.managment;

import org.executequery.GUIUtilities;
import org.executequery.components.table.RowHeaderRenderer;
import org.executequery.gui.browser.UserManagerPanel;

import javax.swing.*;

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
        roleIcon = GUIUtilities.loadIcon("user_manager_16.svg");
        userIcon = GUIUtilities.loadIcon("User16.svg");
    }

    protected void setValue(Object value) {

        if (value.getClass().equals(String.class)) {
            setIcon(null);
            setText((String) value);
        }
        if (value.getClass().equals(UserManagerPanel.UserRole.class)) {
            UserManagerPanel.UserRole userRole = (UserManagerPanel.UserRole) value;
            setText(userRole.getName());
            if (userRole.isUser())
                setIcon(userIcon);
            else setIcon(roleIcon);
        }
    }
}
