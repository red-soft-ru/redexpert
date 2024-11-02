package org.executequery.datasource.util;

import biz.redsoft.IFBClientLoader;
import com.sun.jna.Platform;
import org.apache.commons.io.FilenameUtils;
import org.executequery.ExecuteQuery;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.log.Log;
import org.underworldlabs.util.DynamicLibraryLoader;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/// @author Aleksey Kozlov
public final class LibraryManager {

    private static final String PARAMETER_SEPARATOR = Platform.isWindows() ? ";" : ":";
    private static final String LINUX_X84_64 = "linux-x86-64";
    private static final String WIN32_X84_64 = "win32-x86-64";
    private static final String WIN32_X84 = "win32-x86";
    private static final int BUFFER_SIZE = 2048;

    private static final int EXISTS = 0;
    private static final int CREATED = EXISTS + 1;
    private static final int NOT_CREATED = CREATED + 1;

    /// Private constructor to prevent installation.
    private LibraryManager() {
    }

    /**
     * Tries to find <code>fbclient-n.jar</code> file, extract it to the temp directory
     * and update <code>jna.library.path</code> property.
     *
     * @param connection    connection for what the fbclient will be searched
     * @param driverVersion driver version in use
     */
    public static void updateJnaPath(DatabaseConnection connection, int driverVersion) {

        List<Path> jarPathsList = getJarPathsList(connection, driverVersion);
        Path fbClientJarPath = getFbClientJarPath(jarPathsList);
        Path fbClientExtractedPath = checkExtractFbClient(fbClientJarPath, driverVersion);
        String jnaLibraryPath = buildJnaLibraryPath(fbClientExtractedPath);

        Log.info("Attempting to set jna.library.path to: " + jnaLibraryPath);

        System.setProperty("jna.library.path", jnaLibraryPath != null ? jnaLibraryPath : "");
        System.setProperty("jna.platform.library.path", "");
        System.setProperty("jna.debug_load", "true");
    }

    /**
     * Gets the path to the libraries of the JDBC driver
     * for the specified connection and normalizes them.
     *
     * @param connection    database connection for which libraries will load
     * @param driverVersion driver version in use
     * @return list of the JDBC driver distinct libraries paths
     */
    public static List<Path> getJarPathsList(DatabaseConnection connection, int driverVersion) {
        String separator = ";";

        String jarsString = connection.getJDBCDriver().getPath();
        jarsString = jarsString.replace("../", "./") + separator + jarsString.replace("./", "../");
        jarsString = jarsString.replace(".../", "../");
        jarsString += separator + DynamicLibraryLoader.getFbPluginImplPath(driverVersion);

        return Arrays.stream(jarsString.split(separator)).distinct().map(Paths::get).collect(Collectors.toList());
    }

    /**
     * Creates and initialize a new instance of the client library
     * (<code>FBClientLibrary</code>) using JNA.
     *
     * @param driver driver for which the library will be loaded
     * @return new client library instance or <code>null</code>
     */
    public static Object loadFbClientLibrary(Driver driver) {

        Object loader = getFbClientLoader(driver);
        if (loader != null)
            return ((IFBClientLoader) loader).load(driver.getMajorVersion());

        return null;
    }

    /**
     * Disposes native resources (currently: native libraries).<p>
     * Calling this method with active native/embedded connections
     * may break those connections and lead to errors.
     *
     * @param driver   driver for which the native resource will dispose
     * @param fbclient library which will dispose or <code>null</code>
     */
    public static void shutdownNativeResources(Driver driver, Object fbclient) {
        Object loader = getFbClientLoader(driver);
        if (loader != null)
            ((IFBClientLoader) loader).dispose(fbclient);
    }

    /**
     * Builds parameter string from the specified libraries paths list.
     *
     * @return list of libraries paths joined into the string with the <code>;</code> separator
     */
    public static String convertToStringParameter(List<Path> pathList) {
        return pathList.stream().map(Path::toString).collect(Collectors.joining(";"));
    }

    // ---

    /**
     * Tries to find <code>fbclient-n.jar</code> file path among the
     * specified list of libraries paths.
     *
     * @return first found path to existing <code>fbclient-n.jar</code> file
     */
    private static Path getFbClientJarPath(List<Path> pathList) {
        return pathList.stream()
                .filter(LibraryManager::isFbClient)
                .filter(Files::exists)
                .findFirst().orElse(null);
    }

    /**
     * Checks and/or creates directories before extracting the <code>fbclient-n.jar</code> file
     * and starts extracting the jar file if the fbclient directory has just been created.
     *
     * @param fbClientJarPath path to the <code>fbclient-n.jar</code> file to extract
     * @param driverVersion   driver version in use
     * @return path to the extracted fbclient library or <code>null</code>
     */
    private static Path checkExtractFbClient(Path fbClientJarPath, int driverVersion) {

        if (fbClientJarPath == null) {
            Log.info("Couldn't extract fbclient, file not found");
            return null;
        }

        Path tmpDirectory = ExecuteQuery.TMP_APP_DIR;
        if (checkCreateDirectory(tmpDirectory) == NOT_CREATED)
            return null;

        String fbClientDirectoryName = FilenameUtils.removeExtension(fbClientJarPath.getFileName().toString());
        Path fbClientDirectory = Paths.get(tmpDirectory.toString(), fbClientDirectoryName);

        int createFbClientDirectoryResult = checkCreateDirectory(fbClientDirectory);
        if (createFbClientDirectoryResult == EXISTS)
            return fbClientDirectory;

        if (createFbClientDirectoryResult == CREATED) {
            extractFbClient(
                    fbClientJarPath,
                    fbClientDirectory.toString(),
                    getPlatformName(driverVersion)
            );

            return fbClientDirectory;
        }

        return null;
    }

    /**
     * Extracts <code>fbclient-n.jar</code> file to the specified directory for the specified platform.
     *
     * @param fbClientJarPath   path to the <code>fbclient-n.jar</code> file to extract
     * @param fbClientDirectory path where to extract <code>fbclient-n.jar</code> file
     * @param platform          platform filter for extracting
     */
    private static void extractFbClient(Path fbClientJarPath, String fbClientDirectory, String platform) {
        try (JarFile jarFile = new JarFile(fbClientJarPath.toFile())) {

            Enumeration<JarEntry> enumEntries = jarFile.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry jarEntry = enumEntries.nextElement();

                String entryName = getEntryName(platform, jarEntry);
                if (entryName == null)
                    continue;

                File outputFile = Paths.get(fbClientDirectory, entryName).toFile();
                if (jarEntry.isDirectory()) {
                    createDirectory(outputFile);

                } else {
                    Log.info("Extracting: " + entryName);
                    createFile(jarFile, jarEntry, outputFile);
                }
            }

        } catch (IOException e) {
            Log.info(e.getMessage(), e);
        }
    }

    /**
     * Builds parameter string from the specified extracted fbclient path.
     * That method also adds <code>/plugins</code> and <code>/lib</code> path to the output string.
     *
     * @return list of paths joined into the string with the default system separator
     */
    private static String buildJnaLibraryPath(Path fbClientPath) {

        if (fbClientPath == null)
            return null;

        List<Path> fbClientPathList = new ArrayList<>();
        fbClientPathList.add(fbClientPath);
        fbClientPathList.add(Paths.get(fbClientPath.toString(), "plugins"));
        if (!Platform.isWindows())
            fbClientPathList.add(Paths.get(fbClientPath.toString(), "lib"));

        return fbClientPathList.stream().map(Path::toString).collect(Collectors.joining(PARAMETER_SEPARATOR));
    }

    // --- util methods ---

    private static int checkCreateDirectory(Path path) {
        Log.debug("Checking for directory [" + path + "]");

        if (Files.exists(path)) {
            Log.debug("Directory exists [" + path + "]");
            return EXISTS;
        }

        if (path.toFile().mkdirs()) {
            Log.debug("Directory created [" + path + "]");
            return CREATED;
        }

        Log.debug("Couldn't create directory [" + path + "]");
        return NOT_CREATED;
    }

    private static String getPlatformName(int driverVersion) {

        if (Platform.isWindows()) {
            boolean is32Bit = driverVersion < 4 && !Platform.is64Bit();
            return is32Bit ? WIN32_X84 : WIN32_X84_64;
        }

        return LINUX_X84_64;
    }

    private static boolean isFbClient(Path path) {
        return path.getFileName().toString().startsWith("fbclient");
    }

    // ---

    private static String getEntryName(String platform, JarEntry jarEntry) {

        String entryName = jarEntry.getName();
        if (!entryName.contains(platform))
            return null;

        entryName = entryName.replace(platform, "").trim();
        if (entryName.isEmpty() || Objects.equals(entryName, FileSystems.getDefault().getSeparator()))
            return null;

        return entryName;
    }

    private static void createDirectory(File file) throws IOException {
        Log.info("Creating directory: " + file.getAbsolutePath());
        if (!file.mkdir())
            throw new IOException("Couldn't create directory [" + file.getAbsolutePath() + "]");
    }

    private static void createFile(JarFile jarFile, JarEntry jarEntry, File outputFile) throws IOException {

        if (!outputFile.createNewFile())
            throw new IOException("Couldn't create file [" + outputFile.getAbsolutePath() + "]");

        try (
                BufferedInputStream inputStream = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE)
        ) {
            int count;
            byte[] data = new byte[BUFFER_SIZE];
            while ((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1)
                outputStream.write(data, 0, count);
        }
    }

    private static Object getFbClientLoader(Driver driver) {
        try {
            return DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(),
                    driver,
                    "IFBClientLoader"
            );

        } catch (ClassNotFoundException e) {
            Log.debug(e.getMessage(), e);
            return null;
        }
    }

}
