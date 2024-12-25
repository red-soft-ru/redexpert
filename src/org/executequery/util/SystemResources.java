/*
 * SystemResources.java
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

package org.executequery.util;

import org.apache.commons.lang.StringUtils;
import org.executequery.Application;
import org.executequery.ApplicationContext;
import org.executequery.ExecuteQuery;
import org.executequery.GUIUtilities;
import org.executequery.io.XMLFile;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.repository.LogRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;

/**
 * This object acts as a utility class for file input
 * and output. All open file and save file requests are
 * propagated via the relevant methods to this class.
 * This object is also responsible for user and default
 * system properties and handles all read and write methods
 * for these. System version information and SQL keywords
 * are also retrieved and maintained here.
 *
 * @author Takis Diakoumis
 */
public class SystemResources {

    private static final int RETAIN_BUILD_NUMBER_COUNT = 3;

    private static final Map<String, StringBundle> bundles = new HashMap<>();
    private static final UserSettingsProperties USER_SETTINGS_PROPERTIES = new UserSettingsProperties();

    /**
     * Loads the resource bundle for the specified class.
     * The bundle will be retrieved from a derived path from
     * the specified class's package name and by appending
     * /resource to the end.
     *
     * @param clazz the clazz to load the bundle for
     * @return the wrapped resource bundle
     * @deprecated use Bundles
     */
    public static StringBundle loadBundle(Class<?> clazz) {
        String clazzName = clazz.getName();
        String packageName = clazzName.substring(0, clazzName.lastIndexOf("."));

        String key = packageName.replaceAll("\\.", "/");
        if (!bundles.containsKey(key)) {
            String path = key + "/resource/resources";
            Locale locale = new Locale(System.getProperty("user.language"));
            bundles.put(key, new StringBundle(ResourceBundle.getBundle(path, locale), key));
        }

        return bundles.get(key);
    }

    public static Properties getUserActionShortcuts() {
        try {
            File file = new File(userActionShortcutsPath());
            return file.exists() ? FileUtils.loadProperties(file) : null;

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            return null;
        }
    }

    private static String userActionShortcutsPath() {
        return userSettingsDirectoryForCurrentBuild() + "eq.shortcuts.properties";
    }

    public static void setUserActionShortcuts(Properties properties) {
        try {
            String path = userActionShortcutsPath();
            FileUtils.storeProperties(path, properties, "Red Expert - User Defined System Shortcuts");

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage("Error saving shortcuts");
        }
    }

    public static synchronized void setUserPreferences(Properties properties) {
        try {
            String path = userSettingsDirectoryForCurrentBuild() + "eq.user.properties";
            FileUtils.storeProperties(path, properties, "Red Expert - User Defined System Properties");

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage("Error saving preferences:\n" + e.getMessage());
        }
    }

    public static synchronized void setAuditPreferences(Properties properties) {
        try {
            String path = userSettingsDirectoryForCurrentBuild() + "audit.properties";
            FileUtils.storeProperties(path, properties, "Red Expert - Audit Properties");

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage("Error saving preferences:\n" + e.getMessage());
        }
    }

    /**
     * Creates the eq system home directory structure in ~/.redexpert.
     */
    public static boolean createUserHomeDirSettings() {

        String userSettingsHome = userSettingsHome();
        String fileSeparator = FileSystems.getDefault().getSeparator();

        File appPropertiesDirectory = new File(userSettingsHome);
        File configurationDirectory = new File(userSettingsDirectoryForCurrentBuild());

        try {
            boolean copyOldFiles;
            boolean buildDirectoryExists;
            boolean propertiesDirectoryExists;

            if (!appPropertiesDirectory.exists()) { // check for ~/.redexpert directory
                propertiesDirectoryExists = create(appPropertiesDirectory);
                buildDirectoryExists = create(configurationDirectory);
                copyOldFiles = false;

            } else if (!configurationDirectory.exists()) {  // check for ~/.redexpert/<build-number> directory
                buildDirectoryExists = create(configurationDirectory);
                propertiesDirectoryExists = true;
                copyOldFiles = true;

            } else {
                propertiesDirectoryExists = true;
                buildDirectoryExists = true;
                copyOldFiles = false;
            }

            int lastBuildNumber = -1;
            String configurationDirectoryPath = configurationDirectory.getAbsolutePath() + fileSeparator;

            if (copyOldFiles) {
                int currentBuild = Integer.parseInt(currentBuild());

                File[] propertiesDirectoryFiles = appPropertiesDirectory.listFiles();
                if (propertiesDirectoryFiles != null) {
                    for (File file : propertiesDirectoryFiles) {

                        String name = file.getName();
                        if (MiscUtils.isValidNumber(name)) {

                            int buildNumber = Integer.parseInt(name);
                            if (currentBuild > buildNumber)
                                lastBuildNumber = Math.max(lastBuildNumber, buildNumber);
                        }
                    }
                }
            }

            File oldConfigurationDirectory = lastBuildNumber != -1 ?
                    new File(userSettingsHome + lastBuildNumber) :
                    new File(userSettingsHome + "conf");

            if (copyOldFiles && oldConfigurationDirectory.exists()) {
                int option = GUIUtilities.displayConfirmCancelDialog(Bundles.get(SystemResources.class, "foundOldSettings"));
                switch (option) {
                    case JOptionPane.YES_OPTION:
                        String oldFromPath = oldConfigurationDirectory.getAbsolutePath();
                        String[] oldConfigurationFilesNames = {
                                "eq.shortcuts.properties",
                                "eq.user.properties",
                                "jdbcdrivers.xml",
                                "connection-folders.xml",
                                "lookandfeel.xml",
                                "querybookmarks.xml",
                                "print.setup",
                                "savedconnections.xml",
                                "ConnectionHistory.xml"
                        };

                        checkUserProperties("jdbcdrivers-default.xml", oldConfigurationDirectory, "id");
                        for (String oldFileName : oldConfigurationFilesNames) {

                            File oldFile = new File(oldFromPath, oldFileName);
                            if (oldFile.exists()) {
                                File newFile = new File(configurationDirectory, oldFileName);
                                FileUtils.copyFile(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                            }
                        }

                        ExecuteQuery.restart(null, false, true, true);
                        break;

                    case JOptionPane.CANCEL_OPTION:
                        configurationDirectory.deleteOnExit();
                        Application.exitProgram(true);
                        break;
                }
            }

            if (!propertiesDirectoryExists && !buildDirectoryExists) {
                GUIUtilities.displayErrorMessage("Error creating profile in user's home directory.\nExiting.");
                Application.exitProgram(true);
            }

            // --- Check for properties files ---

            File propertiesFile = new File(configurationDirectory, "audit.properties");
            boolean created = propertiesFile.exists() || propertiesFile.createNewFile();
            if (!created)
                return false;

            propertiesFile = new File(configurationDirectory, "eq.user.properties");
            created = propertiesFile.exists() || propertiesFile.createNewFile();
            if (!created)
                return false;

            // --- Check for log files directory ---

            File logsDir = new File(userLogsPath());
            boolean logsDirExists = logsDir.exists();
            if (!logsDirExists)
                logsDirExists = logsDir.mkdirs();

            if (!logsDirExists) {
                error("Error creating logs folder, exiting", null);
                GUIUtilities.displayErrorMessage("Error creating logs folder, exiting.");
                Application.exitProgram(true);
            }

            // --- Check for properties files ---

            propertiesFile = new File(configurationDirectory, "jdbcdrivers.xml");
            if (!propertiesFile.exists()) {
                Log.debug("Creating user properties file jdbcdrivers.xml");

                FileUtils.copyResource(
                        "org/executequery/jdbcdrivers-default.xml",
                        configurationDirectoryPath + "jdbcdrivers.xml"
                );

                propertiesFile = new File(configurationDirectory, "jdbcdrivers.xml");
                if (!propertiesFile.exists())
                    return false;
            }

            propertiesFile = new File(configurationDirectory, "lookandfeel.xml");
            if (!propertiesFile.exists()) {
                Log.debug("Creating user properties file lookandfeel.xml");

                FileUtils.copyResource(
                        "org/executequery/lookandfeel-default.xml",
                        configurationDirectoryPath + "lookandfeel.xml"
                );

                propertiesFile = new File(configurationDirectory, "lookandfeel.xml");
                if (!propertiesFile.exists())
                    return false;
            }

            propertiesFile = new File(configurationDirectory, "savedconnections.xml");
            if (!propertiesFile.exists()) {
                Log.debug("Creating user properties file savedconnections.xml");

                FileUtils.copyResource(
                        "org/executequery/savedconnections-default.xml",
                        configurationDirectoryPath + "savedconnections.xml"
                );

                propertiesFile = new File(configurationDirectory, "savedconnections.xml");
                if (!propertiesFile.exists())
                    return false;
            }

            propertiesFile = new File(configurationDirectory, "toolbars.xml");
            if (!propertiesFile.exists()) {
                Log.debug("Creating user properties file toolbars.xml");

                FileUtils.copyResource(
                        "org/executequery/toolbars-default.xml",
                        configurationDirectoryPath + "toolbars.xml"
                );
                propertiesFile = new File(configurationDirectory, "toolbars.xml");
                if (!propertiesFile.exists())
                    return false;
            }

            propertiesFile = new File(configurationDirectory, "editorsqlshortcuts.xml");
            if (!propertiesFile.exists()) {
                Log.debug("Creating user properties file editorsqlshortcuts.xml");

                FileUtils.copyResource(
                        "org/executequery/editor-sql-shortcuts.xml",
                        configurationDirectoryPath + "editorsqlshortcuts.xml"
                );

                propertiesFile = new File(configurationDirectory, "editorsqlshortcuts.xml");
                if (!propertiesFile.exists())
                    return false;
            }

            removeOldSettingsDirs();
            return created;

        } catch (IOException e) {
            error(e.getMessage(), e);
            GUIUtilities.displayErrorMessage("Error creating profile in user's home directory.\nExiting.");
            return false;
        }
    }

    private static void checkUserProperties(String resourceName, File oldHomeDirectory, String key) {

        String fileSeparator = FileSystems.getDefault().getSeparator();
        String defaultFilePath = oldHomeDirectory + fileSeparator + resourceName;
        String userFilePath = oldHomeDirectory + fileSeparator + resourceName.replace("-default", "");

        try {
            FileUtils.copyResource("org/executequery/" + resourceName, defaultFilePath);

            XMLFile xmlDefaultFile = new XMLFile(new File(defaultFilePath));
            XMLFile xmlFile = new XMLFile(new File(userFilePath));

            Node rootDefault = xmlDefaultFile.getRootNode();
            Node root = xmlFile.getRootNode();

            if (XMLFile.equals(root, rootDefault))
                return;

            NodeList defaultNodeChildren = rootDefault.getChildNodes();
            for (int i = 0; i < defaultNodeChildren.getLength(); i++) {
                try {

                    Node nodeDefault = defaultNodeChildren.item(i);
                    if (nodeDefault.getNodeType() != Node.TEXT_NODE) {

                        String value = XMLFile.getStringValue(key, nodeDefault);
                        Node node = XMLFile.getNodeFromNodes(key, value, root);

                        if (node == null) {
                            XMLFile.appendChild(nodeDefault, root);

                        } else if (!XMLFile.equals(node, nodeDefault))
                            XMLFile.replaceChild(nodeDefault, node, root);
                    }

                } catch (Exception e) {
                    Log.error(e.getMessage(), e);
                }
            }

            File file = new File(userFilePath);
            if (file.exists())
                FileUtils.copyFile(userFilePath, userFilePath.replace(".xml", "-old.xml"));
            xmlFile.save(userFilePath);

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static boolean create(File directory) {
        info("Attempting to create directory [" + directory.getAbsolutePath() + "]");
        boolean created = directory.mkdirs();

        info("Directory [" + directory.getAbsolutePath() + "] created - " + created);
        return created;
    }

    private static void removeOldSettingsDirs() {
        List<File> dirsToDelete = new ArrayList<>();

        File[] files = new File(userSettingsHome()).listFiles();
        Arrays.stream(files != null ? files : new File[]{})
                .filter(File::isDirectory)
                .filter(SystemResources::nameIsNumeric)
                .forEach(dirsToDelete::add);

        if (dirsToDelete.size() <= RETAIN_BUILD_NUMBER_COUNT)
            return;

        String build = currentBuild();
        dirsToDelete.sort(Comparator.comparing(File::getName));

        for (int i = 0, n = dirsToDelete.size() - RETAIN_BUILD_NUMBER_COUNT; i < n; i++) {
            File file = dirsToDelete.get(i);
            String name = file.getName();

            if (!build.equals(name)) {
                info("Removing old user settings directory [" + file.getAbsolutePath() + "] for build " + name);
                FileUtils.deleteDirectory(file);
            }
        }
    }

    private static String userSettingsHome() {
        return USER_SETTINGS_PROPERTIES.getUserSettingsBaseHome();
    }

    private static boolean nameIsNumeric(File file) {
        return StringUtils.isNumeric(file.getName());
    }

    private static String currentBuild() {
        return ApplicationContext.getInstance().getBuild();
    }

    private static String userLogsPath() {
        return ((LogRepository) RepositoryCache.load(LogRepository.REPOSITORY_ID)).getLogFileDirectory();
    }

    public static String userSettingsDirectoryForCurrentBuild() {
        UserSettingsProperties settings = new UserSettingsProperties();
        return settings.getUserSettingsDirectory();
    }

    private static void info(String message) {
        System.out.println("INFO: " + message);
    }

    private static void error(String message, Exception e) {
        System.out.println("ERROR: " + message);
        if (e != null)
            e.printStackTrace(System.out);
    }

}
