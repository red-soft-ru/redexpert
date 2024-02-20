package org.executequery.gui.browser.managment.tracemanager;

import org.executequery.gui.editor.ResultSetTablePopupMenu;
import org.executequery.gui.exportData.ExportDataPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ReflectiveAction;
import org.underworldlabs.swing.menu.MenuItemFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ServiceManagerPopupMenu extends JPopupMenu implements MouseListener {
    JTable table;
    private final ReflectiveAction reflectiveAction;

    private Point lastPopupPoint;

    public ServiceManagerPopupMenu(JTable table) {
        this.table = table;
        reflectiveAction = new ReflectiveAction(this);
        add(create(bundleString("ExportTable"), "exportTable"));
    }

    private JMenuItem create(String text, String actionCommand) {

        JMenuItem menuItem = MenuItemFactory.createMenuItem(reflectiveAction);
        menuItem.setActionCommand(actionCommand);
        menuItem.setText(text);

        return menuItem;
    }

    private String bundleString(String key) {
        return Bundles.get(ResultSetTablePopupMenu.class, key);
    }

    @SuppressWarnings("unused")
    public void exportTable(ActionEvent e) {
        new ExportDataPanel(table.getModel(), null);
    }

    private void maybeShowPopup(MouseEvent e) {

        if (e.isPopupTrigger()) {

            lastPopupPoint = e.getPoint();

            show(e.getComponent(), lastPopupPoint.x, lastPopupPoint.y);

        } else {

            // re-enable cell selection
            table.setColumnSelectionAllowed(true);
            table.setRowSelectionAllowed(true);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
