package org.executequery.gui.prefs;

import org.executequery.Constants;

import java.util.ArrayList;
import java.util.List;

public class PropertiesLogging extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesLogging() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace(System.out);
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

        key = "system.log.enabled";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogOutputToFile"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "system.log.level";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("OutputLogLevel"),
                stringUserProperty(key),
                Constants.LOG_LEVELS));

        key = "editor.logging.path";
        list.add(new UserPreference(
                UserPreference.DIR_TYPE,
                key,
                bundledString("OutputLogFilePath"),
                stringUserProperty(key)));

        key = "editor.logging.backups";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("MaximumRollingLogBackups"),
                stringUserProperty(key)));

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

    @Override
    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    @Override
    public void save() {
        preferencesPanel.savePreferences();
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

}
