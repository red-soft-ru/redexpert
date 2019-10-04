package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;

public class PropertiesConsoleFonts extends PropertiesEditorFonts {

    public PropertiesConsoleFonts() {
        super();
        try {
            initValues();
        } catch (Exception e) {
            Log.error("Error init Class PropertiesConsoleFonts:", e);
        }
    }

    private void initValues() {
        String _fontName = SystemProperties.getProperty("user", "console.font.name");
        String _fontSize = SystemProperties.getProperty("user", "console.font.size");

        fontList.setSelectedValue(_fontName, true);
        sizeList.setSelectedValue(_fontSize, true);

        selectedFontField.setText(_fontName);
        selectedSizeField.setText(_fontSize);
    }

    public void save() {
        SystemProperties.setProperty("user", "console.font.size",
                (String) sizeList.getSelectedValue());
        SystemProperties.setProperty("user", "console.font.name",
                (String) fontList.getSelectedValue());
        try {
            ((SystemOutputPanel) GUIUtilities.getDockedTabComponent(SystemOutputPanel.PROPERTY_KEY)).reloadFont();
        } catch (Exception e) {
            Log.debug("error reload console font", e);
        }
    }

    public void restoreDefaults() {
        fontList.setSelectedValue(
                UIManager.getDefaults().getFont("TextArea.font").getFontName(), true);
        sizeList.setSelectedValue(SystemProperties.
                getProperty("defaults", "console.font.size"), true);
    }
}