/*
 * PropertiesGeneral.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.gui.prefs;

import org.apache.commons.lang.StringUtils;
import org.executequery.gui.text.LineSeparator;
import org.underworldlabs.util.SystemProperties;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * System preferences general panel.
 *
 * @author Takis Diakoumis
 */
public class PropertiesGeneral extends AbstractPropertiesBasePanel {

    private SimplePreferencesPanel preferencesPanel;

    public PropertiesGeneral() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {

        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null));

        String key = "startup.display.splash";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("DisplaySplashScreenAtStartup"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "startup.window.maximized";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("MaximiseWindowOnStartup"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "recent.files.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("RecentFilesToStore"),
                stringUserProperty(key)));

        key = "general.line.separator";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("LineSeparator"),
                SystemProperties.getProperty("user", key),
                new String[]{LineSeparator.DOS.label,
                        LineSeparator.WINDOWS.label,
                        LineSeparator.MAC_OS.label}));

        key = "general.save.prompt";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("PromptToSaveOpenDocuments"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "startup.version.check";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("CheckForUpdateOnStartup"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "startup.unstableversions.load";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LoadUnstableVersions"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "startup.majorversions.load";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("CheckMajorVersions"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "system.file.encoding";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("DefaultFileEncoding"),
                encodingValue(),
                availableCharsets()));


        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("InternetProxySettings"),
                null));

        key = "internet.proxy.set";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("UseProxyServer"),
                Boolean.valueOf(stringUserProperty(key))));

        key = "internet.proxy.host";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("ProxyHost"),
                stringUserProperty(key)));

        key = "internet.proxy.port";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledString("ProxyPort"),
                stringUserProperty(key)));

        key = "internet.proxy.user";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("ProxyUser"),
                stringUserProperty(key)));

        key = "internet.proxy.password";
        list.add(new UserPreference(
                UserPreference.PASSWORD_TYPE,
                key,
                bundledString("ProxyPassword"),
                stringUserProperty(key)));

        UserPreference[] preferences =
                list.toArray(new UserPreference[list.size()]);
        preferencesPanel = new SimplePreferencesPanel(preferences);
        addContent(preferencesPanel);
    }

    private String[] availableCharsets() {

        List<String> available = new ArrayList<String>();
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        for (Charset charset : charsets.values()) {

            available.add(charset.name());
        }

        return available.toArray(new String[available.size()]);
    }

    private String encodingValue() {

        String encoding = stringUserProperty("system.file.encoding");
        if (StringUtils.isBlank(encoding)) {

            return System.getProperty("file.encoding");
        }
        return encoding;
    }

    public void restoreDefaults() {

        preferencesPanel.restoreDefaults();
    }

    public void save() {

        preferencesPanel.savePreferences();
    }

}



