/*
 * ApplicationLauncher.java
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

package org.executequery;

import org.apache.commons.lang.StringUtils;
import org.executequery.databasemediators.ConnectionMediator;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.ExecuteQueryFrame;
import org.executequery.gui.editor.QueryEditorHistory;
import org.executequery.gui.menu.ExecuteQueryMenu;
import org.executequery.log.Log;
import org.executequery.plaf.LookAndFeelType;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.util.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.CustomKeyboardFocusManager;
import org.underworldlabs.swing.PasswordDialog;
import org.underworldlabs.swing.SplashPanel;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class ApplicationLauncher {

    // agent.jar
    // http://blog.dutchworks.nl/2011/01/09/make-intellij-idea-behave-properly-in-linux-docks/
    // asm license: http://asm.ow2.org/license.html

    public void startup() {

        try {

            applySystemProperties();
            macSettings();
            x11Settings();

            boolean dirsCreated = SystemResources.createUserHomeDirSettings();
            aaFonts();

            if (!dirsCreated) {

                System.exit(0);
            }

            System.setProperty("executequery.minor.version",
                    stringApplicationProperty("re.version"));

            SplashPanel splash = null;

            if (displaySplash()) {

                splash = createSplashPanel();
            }

            advanceSplash(splash);

            // set the version number to display on the splash panel
            System.setProperty("executequery.major.version",
                    stringApplicationProperty("re.version"));

            System.setProperty("executequery.help.version",
                    stringApplicationProperty("help.version"));

            advanceSplash(splash);

            // reset the log level from the user properties
            Log.setLevel(stringUserProperty("system.log.level"));

            advanceSplash(splash);

            applyKeyboardFocusManager();


            if (hasLocaleSettings()) {

                setSystemLocaleProperties();

            } else {

                if (Log.isDebugEnabled()) {

                    Log.debug("User locale settings not available - resetting");
                }

                storeSystemLocaleProperties();

            }

            advanceSplash(splash);

            // set the look and feel
            LookAndFeelLoader lookAndFeelLoader = new LookAndFeelLoader();
            loadLookAndFeel(lookAndFeelLoader);

            lookAndFeelLoader.decorateDialogsAndFrames(
                    booleanUserProperty("decorate.dialog.look"),
                    booleanUserProperty("decorate.frame.look"));

            advanceSplash(splash);

            GUIUtilities.startLogger();

            advanceSplash(splash);

            // initialise the frame
            final ExecuteQueryFrame frame = createFrame();

            GUIUtilities.initDesktop(frame);

            // initialise the actions from actions.xml
            ActionBuilder.build(GUIUtilities.getActionMap(),
                    GUIUtilities.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                    Constants.ACTION_CONF_PATH);

            advanceSplash(splash);

            // build the tool bar
            GUIUtilities.createToolBar();

            JMenuBar menuBar = new ExecuteQueryMenu();
            frame.setJMenuBar(menuBar);

            advanceSplash(splash);

            boolean openConnection =
                    booleanUserProperty("startup.connection.connect");

            advanceSplash(splash);

            printVersionInfo();

            advanceSplash(splash);

            frame.position();

            // set proxy server settings
            initProxySettings();

            ActionBuilder.setActionMaps(frame.getRootPane(), SystemResources.getUserActionShortcuts());

            GUIUtilities.initPanels();

            advanceSplash(splash);

            // kill the splash panel
            if (splash != null) {

                splash.dispose();
            }

            ThreadUtils.invokeLater(new Runnable() {

                @Override
                public void run() {

                    frame.setVisible(true);
                }

            });

            printSystemProperties();

            frame.setTitle("Red Expert - " + System.getProperty("executequery.minor.version"));

            // auto-login if selected
            if (openConnection) {

                openStartupConnection();
            }

            QueryEditorHistory.restoreTabs(null);

            doCheckForUpdate();
            GUIUtilities.loadAuthorisationInfo();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private void printSystemProperties() {
        if (Log.isTraceEnabled()) {

            Log.trace(" --- System properties --- ");

            List<String> keys = new ArrayList<>();
            Properties properties = System.getProperties();
            for (Enumeration<Object> i = properties.keys(); i.hasMoreElements(); ) {

                keys.add((String) i.nextElement());
            }

            Collections.sort(keys);
            for (String key : keys) {

                Log.trace(key + ": " + properties.getProperty(key));
            }
        }
    }

    private boolean displaySplash() {

        return booleanUserProperty("startup.display.splash");
    }

    private boolean hasLocaleSettings() {

        String language = userProperties().getStringProperty("locale.language");
//        String country = userProperties().getStringProperty("locale.country");
//        String timezone = userProperties().getStringProperty("locale.timezone");

        /*
        return !(MiscUtils.isNull(language))
                && !(MiscUtils.isNull(country))
                && !(MiscUtils.isNull(timezone));
        */

        return StringUtils.isNotBlank(language);
    }

    private void loadLookAndFeel(LookAndFeelLoader loader) {

        String lookAndFeel = userProperties().getStringProperty("startup.display.lookandfeel");
        try {

            LookAndFeelType lookAndFeelType = loader.loadLookAndFeel(lookAndFeel);
            userProperties().setStringProperty("startup.display.lookandfeel", lookAndFeelType.name());

        } catch (ApplicationException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("Error loading look and feel", e);
            }
            loadDefaultLookAndFeel(loader);
        }

    }

    private void loadDefaultLookAndFeel(LookAndFeelLoader loader) {

        try {

            loader.loadLookAndFeel(LookAndFeelType.EXECUTE_QUERY);
            userProperties().setStringProperty(
                    "startup.display.lookandfeel", LookAndFeelType.EXECUTE_QUERY.name());

        } catch (ApplicationException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("Error loading default EQ look and feel", e);
            }
            loader.loadCrossPlatformLookAndFeel();
        }

    }

    private void applySystemProperties() {

        String encoding = stringApplicationProperty("system.file.encoding");
        if (StringUtils.isNotBlank(encoding)) {

            System.setProperty("file.encoding", encoding);
        }

        String settingDirName = stringApplicationProperty("eq.user.home.dir");
        System.setProperty("executequery.user.home.dir", settingDirName);
        ApplicationContext.getInstance().setUserSettingsDirectoryName(settingDirName);

        String build = stringApplicationProperty("eq.build");
        System.setProperty("executequery.build", build);
        ApplicationContext.getInstance().setBuild(build);
    }

    private void aaFonts() {

        String value1 = "on";
        String value2 = "true";
        if (!booleanUserProperty("display.aa.fonts")) {

            value1 = "off";
            value2 = "false";
        }

        System.setProperty("awt.useSystemAAFontSettings", value1);
        System.setProperty("swing.aatext", value2);
    }

    private void applyKeyboardFocusManager() {

        try {

            KeyboardFocusManager.setCurrentKeyboardFocusManager(
                    new CustomKeyboardFocusManager());

        } catch (SecurityException e) {
        }
    }

    private void storeSystemLocaleProperties() {

        SystemProperties.setProperty(Constants.USER_PROPERTIES_KEY, "locale.country",
                System.getProperty("user.country"));
        SystemProperties.setProperty(Constants.USER_PROPERTIES_KEY, "locale.language",
                System.getProperty("user.language"));
        SystemProperties.setProperty(Constants.USER_PROPERTIES_KEY, "locale.timezone",
                System.getProperty("user.timezone"));
    }

    private void setSystemLocaleProperties() {

        // set locale and timezone info
        System.setProperty("user.country", stringUserProperty("locale.country"));
        System.setProperty("user.language", stringUserProperty("locale.language"));
        System.setProperty("user.timezone", stringUserProperty("locale.timezone"));

        Locale.setDefault(new Locale(stringUserProperty("locale.language")));

    }

    private void printVersionInfo() {

        Log.info("Using Java version " +
                System.getProperty("java.version"));
        Log.info("Red Expert version: " +
                System.getProperty("executequery.minor.version") +
                "-" + System.getProperty("executequery.build"));
        Log.info("Operating System: " +
                System.getProperty("os.name") +
                " [ " + System.getProperty("os.version") + " ]");

        Log.info("System is ready.");
    }

    private void advanceSplash(SplashPanel splash) {

        if (splash != null) {

            splash.advance();
        }

    }

    private SplashPanel createSplashPanel() {

        return new SplashPanel(
                progressBarColour(),
                "/org/executequery/images/SplashImage.png",
                versionString(),
                versionTextColour(),
                220, 220);
//        5, 15); // top-left
    }

    private Color versionTextColour() {

        return new Color(255, 255, 255);
    }

    private String versionString() {

        String minorVersion = System.getProperty("executequery.minor.version");
        if (minorVersion.endsWith(".0")) {

            minorVersion = minorVersion.substring(0, minorVersion.length() - 2);
        }
        return "version " + minorVersion;
    }

    private Color progressBarColour() {
        return new Color(255, 255, 255);
    }

    public ExecuteQueryFrame createFrame() {

        return new ExecuteQueryFrame();
    }

    private void doCheckForUpdate() {

        boolean doUpdateCheck = booleanUserProperty("startup.version.check");

        if (doUpdateCheck) {

            new CheckForUpdateNotifier().startupCheckForUpdate();
        }

    }

    private boolean booleanUserProperty(String key) {

        return userProperties().getBooleanProperty(key);
    }

    private String stringUserProperty(String key) {

        return userProperties().getProperty(key);
    }

    private String stringApplicationProperty(String key) {

        return applicationProperties().getProperty(key);
    }

    private UserProperties userProperties() {

        return UserProperties.getInstance();
    }

    private ApplicationProperties applicationProperties() {

        return ApplicationProperties.getInstance();
    }

    private void openStartupConnection() {

        final String name = stringUserProperty("startup.connection.name");

        if (!MiscUtils.isNull(name)) {

            ThreadUtils.invokeLater(new Runnable() {

                @Override
                public void run() {

                    openConnection(databaseConnectionRepository().findByName(name));
                }

            });
        }
    }

    private DatabaseConnectionRepository databaseConnectionRepository() {

        return (DatabaseConnectionRepository) RepositoryCache.load(
                DatabaseConnectionRepository.REPOSITORY_ID);
    }

    private void openConnection(DatabaseConnection dc) {

        if (dc == null) {

            return;
        }

        if (!dc.isPasswordStored()) {

            PasswordDialog pd = new PasswordDialog(null,
                    "Password",
                    "Enter password");

            int result = pd.getResult();
            String pwd = pd.getValue();

            pd.dispose();

            if (result <= PasswordDialog.CANCEL) {

                return;
            }

            dc.setPassword(pwd);
        }

        try {

            ConnectionMediator.getInstance().connect(dc);

        } catch (DataSourceException e) {

            GUIUtilities.displayErrorMessage(e.getMessage());
        }

    }

    private void initProxySettings() {
        new HttpProxyConfigurator().configureHttpProxy();
    }

    private void x11Settings() {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Class<?> xtoolkit = toolkit.getClass();
        if (xtoolkit.getName().equals("sun.awt.X11.XToolkit")) {

            try {

                Field awtAppClassName = xtoolkit.getDeclaredField("awtAppClassName");
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, ExecuteQueryFrame.TITLE);

            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {

                e.printStackTrace();
            }
        }

        /*
        try {
            Toolkit xToolkit = Toolkit.getDefaultToolkit();
            java.lang.reflect.Field awtAppClassNameField = xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, "Execute Query");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }

    private void macSettings() {

        if (UIUtils.isMac()) {

            // could also use: -Xdock:name="Execute Query"

            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", ExecuteQueryFrame.TITLE);
        }

    }



}


