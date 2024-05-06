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
import org.executequery.Constants;
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
        init();
    }

    private void init() {

        String key;
        List<UserPreference> list = new ArrayList<UserPreference>();

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("General"),
                null
        ));

        key = "startup.version.check";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("CheckForUpdateOnStartup"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "startup.unstableversions.load";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LoadUnstableVersions"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "general.save.prompt";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("PromptToSaveOpenDocuments"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "recent.files.count";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("RecentFilesToStore"),
                stringUserProperty(key)
        ));

        key = "general.line.separator";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("LineSeparator"),
                SystemProperties.getProperty("user", key),
                new String[]{
                        LineSeparator.DOS.label,
                        LineSeparator.WINDOWS.label,
                        LineSeparator.MAC_OS.label
                }
        ));

        key = "system.file.encoding";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("DefaultFileEncoding"),
                encodingValue(),
                availableCharsets()
        ));

        key = "startup.java.path";
        list.add(new UserPreference(
                UserPreference.FILE_TYPE,
                key,
                bundledString("JavaPath"),
                stringUserProperty(key)
        ));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("Logging"),
                null
        ));

        key = "system.display.console";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("SystemConsole"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "system.log.enabled";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogOutputToFile"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "system.log.out";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogOutToConsole"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "system.log.err";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("LogErrToConsole"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "editor.logging.backups";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                1,
                key,
                bundledString("MaximumRollingLogBackups"),
                stringUserProperty(key)
        ));

        key = "system.log.level";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("OutputLogLevel"),
                stringUserProperty(key),
                Constants.LOG_LEVELS
        ));

        key = "editor.logging.path";
        list.add(new UserPreference(
                UserPreference.DIR_TYPE,
                key,
                bundledString("OutputLogFilePath"),
                stringUserProperty(key)
        ));

        list.add(new UserPreference(
                UserPreference.CATEGORY_TYPE,
                null,
                bundledString("InternetProxySettings"),
                null
        ));

        key = "internet.proxy.set";
        list.add(new UserPreference(
                UserPreference.BOOLEAN_TYPE,
                key,
                bundledString("UseProxyServer"),
                Boolean.valueOf(stringUserProperty(key))
        ));

        key = "internet.proxy.host";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("ProxyHost"),
                stringUserProperty(key)
        ));

        key = "internet.proxy.port";
        list.add(new UserPreference(
                UserPreference.INTEGER_TYPE,
                key,
                bundledString("ProxyPort"),
                stringUserProperty(key)
        ));

        key = "internet.proxy.user";
        list.add(new UserPreference(
                UserPreference.STRING_TYPE,
                key,
                bundledString("ProxyUser"),
                stringUserProperty(key)
        ));

        key = "internet.proxy.password";
        list.add(new UserPreference(
                UserPreference.PASSWORD_TYPE,
                key,
                bundledString("ProxyPassword"),
                stringUserProperty(key)
        ));

        preferencesPanel = new SimplePreferencesPanel(list.toArray(new UserPreference[0]));
        addContent(preferencesPanel);
    }

    private String[] availableCharsets() {

        List<String> available = new ArrayList<>();
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        for (Charset charset : charsets.values())
            available.add(charset.name());

        return available.toArray(new String[0]);
    }

    private String encodingValue() {
        String encoding = stringUserProperty("system.file.encoding");
        return StringUtils.isBlank(encoding) ? Charset.defaultCharset().displayName() : encoding;
    }

    @Override
    public void restoreDefaults() {
        preferencesPanel.restoreDefaults();
    }

    @Override
    public void save() {
        preferencesPanel.savePreferences();
    }

}
