package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTreeCellRenderer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;


public class PropertiesTreeConnectionsFonts extends PropertiesEditorFonts {

    public PropertiesTreeConnectionsFonts(PropertiesPanel parent) {
        super(parent);
    }

    @Override
    protected void preInit() {
        fontNameKey = "treeconnection.font.name";
        fontSizeKey = "treeconnection.font.size";
    }

    @Override
    public void save() {
        super.save();

        JPanel tabComponent = GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
        if (tabComponent instanceof ConnectionsTreePanel) {
            BrowserTreeCellRenderer renderer = (BrowserTreeCellRenderer) ((ConnectionsTreePanel) tabComponent).getTree().getCellRenderer();
            renderer.reloadFont();
        }
    }

}
