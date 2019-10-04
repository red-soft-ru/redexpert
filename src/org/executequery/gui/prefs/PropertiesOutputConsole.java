package org.executequery.gui.prefs;

import org.executequery.Constants;
import org.executequery.log.Log;

import java.util.ArrayList;
import java.util.List;

public class PropertiesOutputConsole extends AbstractPropertiesBasePanel {
    private SimplePreferencesPanel preferencesPanel;

    public PropertiesOutputConsole() {
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

        String key = "system.display.console";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("SystemConsole"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "system.log.level";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("OutputLogLevel"),
                stringUserProperty(key),
                Constants.LOG_LEVELS));

        key = "system.log.out";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogOutToConsole"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "system.log.err";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogErrToConsole"),
                Boolean.valueOf(stringUserProperty(key))));

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

        } catch (Exception e) {
            Log.debug("error reload settings console", e);
        }
    }

}
