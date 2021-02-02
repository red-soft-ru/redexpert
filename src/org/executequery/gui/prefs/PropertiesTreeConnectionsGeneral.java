package org.executequery.gui.prefs;

import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import java.util.ArrayList;
import java.util.List;

public class PropertiesTreeConnectionsGeneral extends AbstractPropertiesBasePanel {
    private SimplePreferencesPanel preferencesPanel;

    public PropertiesTreeConnectionsGeneral() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null));

        String key = "treeconnection.row.height";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                3,
                key,
                bundledString("NodeHeight"),
                stringUserProperty(key)));

        key = "treeconnection.alphabet.sorting";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("AlphabetSorting"),
                SystemProperties.getBooleanProperty("user", key)));

        UserPreference[] preferences = list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);

    }

    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    public String getName() {
        return getClass().getName();
    }

    public void save() {
        preferencesPanel.savePreferences();
        try {
            ConnectionsTreePanel treePanel = ((ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY));
            treePanel.reloadRowHeight();
        } catch (Exception e) {
            Log.debug("error reload font", e);
        }
    }

}
