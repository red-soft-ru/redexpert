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
import org.executequery.gui.IconManager;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.gui.editor.history.QueryEditorHistory;
import org.executequery.gui.menu.ExecuteQueryMenu;
import org.executequery.gui.prefs.*;
import org.executequery.localization.Bundles;
import org.executequery.localization.LocaleManager;
import org.executequery.log.Log;
import org.executequery.plaf.LookAndFeelType;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.executequery.update.CheckForUpdateNotifier;
import org.executequery.util.*;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.CustomKeyboardFocusManager;
import org.underworldlabs.swing.PasswordDialog;
import org.underworldlabs.swing.SplashPanel;
import org.underworldlabs.swing.actions.ActionBuilder;
import org.underworldlabs.swing.plaf.UIUtils;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

/**
 * @author Takis Diakoumis
 */
public class ApplicationLauncher {

    private static boolean needUpdateColorsAndFonts = false;

    // agent.jar
    // http://blog.dutchworks.nl/2011/01/09/make-intellij-idea-behave-properly-in-linux-docks/
    // asm license: http://asm.ow2.org/license.html

    public void startup() {
        SplashPanel splash = null;
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


            if (!hasLocaleSettings()) {
                Log.debug("User locale settings not available - resetting");
                storeSystemLocaleProperties();

            } else
                setSystemLocaleProperties();

            advanceSplash(splash);

            // set the look and feel
            LookAndFeelLoader lookAndFeelLoader = new LookAndFeelLoader();
            loadLookAndFeel(lookAndFeelLoader);

            lookAndFeelLoader.decorateDialogsAndFrames(
                    booleanUserProperty("decorate.dialog.look"),
                    booleanUserProperty("decorate.frame.look"));

            advanceSplash(splash);

            IconManager.loadIcons();
            advanceSplash(splash);

            GUIUtilities.startLogger();
            advanceSplash(splash);

            // initialise the frame
            final ExecuteQueryFrame frame = createFrame();

            GUIUtilities.initDesktop(frame);

            // initialise the actions from actions.xml
            ActionBuilder.build(
                    GUIUtilities.getActionMap(),
                    GUIUtilities.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                    Constants.ACTION_CONF_PATH
            );

            advanceSplash(splash);

            // build the tool bar
            GUIUtilities.createToolBar();

            JMenuBar menuBar = new ExecuteQueryMenu();
            frame.setJMenuBar(menuBar);

            advanceSplash(splash);

            String fileForOpenPath = ApplicationContext.getInstance().getFileForOpenPath();
            boolean needOpenConnectionByFile = StringUtils.isNotBlank(fileForOpenPath);

            boolean needAutoLogin =
                    booleanUserProperty("startup.connection.connect");

            advanceSplash(splash);

            printVersionInfo();

            advanceSplash(splash);

            frame.position();

            // set proxy server settings
            initProxySettings();

            ActionBuilder.updateUserDefinedShortcuts(
                    GUIUtilities.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                    SystemResources.getUserActionShortcuts()
            );

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
            try {
                printSystemProperties();

                frame.setTitle("Red Expert - " + System.getProperty("executequery.minor.version"));

                if (needOpenConnectionByFile) {
                    fileForOpenPath = fileForOpenPath.replace("\\", "/");
                    ConnectionsTreePanel connectionsTreePanel = (ConnectionsTreePanel) GUIUtilities.
                        getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
                    DatabaseConnection dc = databaseConnectionRepository().findBySourceName(fileForOpenPath);
                    if (dc != null) {
                        if (connectionsTreePanel != null) { //show connection panel
                            connectionsTreePanel.getController().valueChanged(connectionsTreePanel.getHostNode(dc), dc);
                        }
                        if (StringUtils.isNotBlank(dc.getPassword()) && StringUtils.isNotBlank(dc.getUserName()))
                            openStartupConnection(dc);
                    } else {
                        if (connectionsTreePanel != null)
                            connectionsTreePanel.newConnection(fileForOpenPath);
                    }
                }

                // auto-login if selected
                if (needAutoLogin) {

                    openStartupConnection(
                        databaseConnectionRepository().findByName(stringUserProperty("startup.connection.name")));
                }
                QueryEditorHistory.restoreTabs(null);

                doCheckForUpdate();
                GUIUtilities.loadAuthorisationInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Application launch error", e, this.getClass());
            e.printStackTrace();
            System.exit(1);
        }

        ApplicationInstanceCounter.add();
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
        String language = userProperties().getStringProperty("startup.display.language");
        return StringUtils.isNotBlank(language);
    }

    private void loadLookAndFeel(LookAndFeelLoader loader) {
        try {

            String lookAndFeel = userProperties().getStringProperty("startup.display.lookandfeel");
            LookAndFeelType lookAndFeelType = loader.loadLookAndFeel(lookAndFeel);
            userProperties().setStringProperty("startup.display.lookandfeel", lookAndFeelType.name());

            if (needUpdateColorsAndFonts) {
                needUpdateColorsAndFonts = false;

                Arrays.stream(new UserPreferenceFunction[]{
                                new PropertiesEditorFonts(null),
                                new PropertiesConsoleFonts(null),
                                new PropertiesEditorColours(null),
                                new PropertiesTreeConnectionsFonts(null),
                                new PropertiesResultSetTableColours(null),
                        })
                        .forEach(userPreferenceFunction -> {
                            userPreferenceFunction.restoreDefaults();
                            userPreferenceFunction.save();
                        });
            }

        } catch (ApplicationException e) {
            Log.debug("Error loading look and feel", e);
            loadDefaultLookAndFeel(loader);
        }
    }

    private void loadDefaultLookAndFeel(LookAndFeelLoader loader) {
        try {
            loader.loadLookAndFeel(LookAndFeelType.DEFAULT_LIGHT);
            userProperties().setStringProperty("startup.display.lookandfeel", LookAndFeelType.DEFAULT_LIGHT.name());

        } catch (ApplicationException e) {
            Log.debug("Error loading default EQ look and feel", e);
            loader.loadCrossPlatformLookAndFeel();
        }
    }

    private void applySystemProperties() {

        String encoding = stringApplicationProperty("system.file.encoding");
        if (StringUtils.isNotBlank(encoding)) {

            System.setProperty("file.encoding", encoding);
        }

        String settingDirName = stringPropertyFromConfig("eq.user.home.dir");
        settingDirName = settingDirName.replace("$HOME",System.getProperty("user.home"));
        System.setProperty("executequery.user.home.dir", settingDirName);
        ApplicationContext.getInstance().setUserSettingsDirectoryName(settingDirName);
        String build = stringApplicationProperty("eq.build");
        System.setProperty("executequery.build", build);
        ApplicationContext.getInstance().setBuild(build);
        UIManager.put("Tree.timeFactor", 5000L);
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
        SystemProperties.setProperty(
                Constants.USER_PROPERTIES_KEY,
                "startup.display.language",
                System.getProperty("user.language")
        );
    }

    private void setSystemLocaleProperties() {
        System.setProperty("user.language", stringUserProperty("startup.display.language"));
        Locale.setDefault(new Locale(stringUserProperty("startup.display.language")));
        LocaleManager.updateLocaleEverywhere();
    }

    private void printVersionInfo() {

        Log.info(bundleString("console-UsingJavaVersion", System.getProperty("java.version")));
        Log.info(bundleString("console-RedExpertVersion") + ": " +
                System.getProperty("executequery.minor.version") +
                "-" + System.getProperty("executequery.build"));
        Log.info(bundleString("console-OSVersion") + ": " +
                System.getProperty("os.name") +
                " [ " + System.getProperty("os.version") + " ]");

        Log.info(bundleString("console-SystemReady"));
    }

    private void advanceSplash(SplashPanel splash) {

        if (splash != null) {

            splash.advance();
        }

    }

    private SplashPanel createSplashPanel() {

        return new SplashPanel(
                progressBarColour(),
                (LocalDate.now().getDayOfYear() >= 349 || LocalDate.now().getDayOfYear() <= 15) ?
                        "/org/executequery/images/SnowSplashImage.gif" :
                        "/org/executequery/images/SplashImage.png",
                versionString(),
                versionTextColour(),
                210, 220);
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
        if (booleanUserProperty("startup.version.check"))
            new CheckForUpdateNotifier().startupCheckForUpdate();
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

    private void openStartupConnection(DatabaseConnection dc) {

        if (dc != null) {

            ThreadUtils.invokeLater(new Runnable() {

                @Override
                public void run() {

                    openConnection(dc);
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

            /*try {

                Field awtAppClassName = xtoolkit.getDeclaredField("awtAppClassName");
                awtAppClassName.setAccessible(true);
                awtAppClassName.set(null, ExecuteQueryFrame.TITLE);

            } catch (Exception e) {

                e.printStackTrace();
            }*/
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
    private String stringPropertyFromConfig(String key)
    {
        Properties props = null;
        try {
            props = FileUtils.loadProperties(MiscUtils.loadURLs("./config/redexpert_config.ini;../config/redexpert_config.ini"));
            return props.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public static void setNeedUpdateColorsAndFonts(boolean needUpdateColorsAndFonts) {
        ApplicationLauncher.needUpdateColorsAndFonts = needUpdateColorsAndFonts;
    }

    String bundleString(String key, Object... args) {
        return Bundles.get(getClass(), key, args);
    }

}


