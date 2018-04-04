package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.components.table.BrowserTreeCellRenderer;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;


public class PropertiesTreeConnectionsFonts extends PropertiesEditorFonts {

    public PropertiesTreeConnectionsFonts() {
        super();
        try {
            initValues();
        } catch (Exception e) {
            Log.error("Error init Class PropertiesTreeConnectionFonts:", e);
        }
    }

    private void initValues() {
        String _fontName = SystemProperties.getProperty("user", "treeconnection.font.name");
        String _fontSize = SystemProperties.getProperty("user", "treeconnection.font.size");

        fontList.setSelectedValue(_fontName, true);
        sizeList.setSelectedValue(_fontSize, true);

        selectedFontField.setText(_fontName);
        selectedSizeField.setText(_fontSize);
    }

    public void save() {
        SystemProperties.setProperty("user", "treeconnection.font.size",
                (String) sizeList.getSelectedValue());
        SystemProperties.setProperty("user", "treeconnection.font.name",
                (String) fontList.getSelectedValue());
        try {
            BrowserTreeCellRenderer renderer = (BrowserTreeCellRenderer) ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY)).getTree().getCellRenderer();
            renderer.reloadFont();
        } catch (Exception e) {
            Log.debug("error reload font", e);
        }
    }

    public void restoreDefaults() {
        fontList.setSelectedValue(
                UIManager.getDefaults().getFont("Tree.font").getFontName(), true);
        sizeList.setSelectedValue(SystemProperties.
                getProperty("defaults", "treeconnection.font.size"), true);
    }
}
