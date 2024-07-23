package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.gui.SystemOutputPanel;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;

public class PropertiesConsoleFonts extends PropertiesEditorFonts {

    public PropertiesConsoleFonts(PropertiesPanel parent) {
        super(parent);
    }

    @Override
    protected void preInit() {
        fontNameKey = "console.font.name";
        fontSizeKey = "console.font.size";
    }

    @Override
    public void save() {
        super.save();

        JPanel tabComponent = GUIUtilities.getDockedTabComponent(SystemOutputPanel.PROPERTY_KEY);
        if (tabComponent instanceof SystemOutputPanel)
            ((SystemOutputPanel) tabComponent).reloadFont();
    }

}
