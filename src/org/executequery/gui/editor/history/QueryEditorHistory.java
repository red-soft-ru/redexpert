package org.executequery.gui.editor.history;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.editor.QueryEditor;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class QueryEditorHistory {
    private static final String HISTORY_FILE = "QueryEditorHistory.csv";
    private static final String NUMBERS_FILE = "QueryEditorNumbers.csv";
    private static final String DIRECTORY = "QueryEditor";

    public static final String NULL_CONNECTION = "null_connection";

    private static Path directoryPath;
    private static List<Integer> numbers;
    private static Map<String, List<EditorData>> editors;
    private static Map<DatabaseConnection, List<Parameter>> parameters;

    /// Private constructor to prevent instantiation.
    private QueryEditorHistory() {
    }

    // ---

    public static Path directoryPath() {
        if (directoryPath == null)
            directoryPath = Paths.get(SystemResources.userSettingsDirectoryForCurrentBuild(), DIRECTORY);
        return directoryPath;
    }

    private static List<Integer> numbers() {
        if (numbers == null)
            loadNumbers();
        return numbers;
    }

    private static Map<String, List<EditorData>> editors() {
        if (editors == null)
            loadEditors();
        return editors;
    }

    public static Map<DatabaseConnection, List<Parameter>> parameters() {
        if (parameters == null)
            parameters = new HashMap<>();
        return parameters;
    }

    // ---

    public static EditorData getEditor(String connectionID, String editor) {
        List<EditorData> editors = getEditors(connectionID);
        Path editorPath = Paths.get(editor).normalize();

        for (EditorData editorData : editors)
            if (editorData.useSameFile(editorPath))
                return editorData;

        return null;
    }

    public static List<EditorData> getEditors(DatabaseConnection connection) {
        return getEditors(connection.getId());
    }

    private static List<EditorData> getEditors(String connectionID) {
        return editors().computeIfAbsent(connectionID, k -> new ArrayList<>());
    }

    // ---

    public static void addEditor(String connectionID, EditorData editorData) {
        List<EditorData> editors = getEditors(connectionID);

        boolean containPath = editors.stream().anyMatch(item -> item.useSameFile(editorData.getEditorPath()));
        if (containPath)
            return;

        editors.add(editorData);

        int editorNumber = editorData.getNumber();
        if (editorNumber != -1) {
            numbers().add(editorNumber);
            saveNumbers();
        }

        saveEditors();
    }

    public static void removeEditor(String connectionID, String editor) {
        removeEditor(connectionID, getEditor(connectionID, editor));
    }

    public static void removeEditor(String connectionID, EditorData editor) {
        if (editor == null)
            return;

        getEditors(connectionID).remove(editor);

        int editorNumber = editor.getNumber();
        if (editorNumber != -1) {
            numbers().remove((Integer) editorNumber);
            saveNumbers();
        }

        saveEditors();
    }

    public static void changedConnectionEditor(String oldConnectionID, String newConnectionID, String editorPath) {

        EditorData editorData = getEditor(oldConnectionID, editorPath);
        if (editorData == null)
            return;

        removeEditor(oldConnectionID, editorData);
        addEditor(newConnectionID, editorData);
    }

    // ---

    private static File historyFile() {
        Path path = Paths.get(
                SystemResources.userSettingsDirectoryForCurrentBuild(),
                DIRECTORY,
                HISTORY_FILE
        );

        createDirectory();
        createFile(path);
        return path.toFile();
    }

    private static File numbersFile() {
        Path path = Paths.get(
                SystemResources.userSettingsDirectoryForCurrentBuild(),
                DIRECTORY,
                NUMBERS_FILE
        );

        createDirectory();
        createFile(path);
        return path.toFile();
    }

    // ---

    private static void createFile(Path path) {
        try {
            if (!Files.exists(path))
                Files.createFile(path);

        } catch (IOException e) {
            Log.error(String.format("Error creating [%s] file", path), e);
        }
    }

    public static void removeFile(String path) {
        try {
            Path fileToDeletePath = Paths.get(path).normalize();
            if (Files.exists(fileToDeletePath))
                Files.delete(fileToDeletePath);

        } catch (IOException e) {
            Log.debug(String.format("Error occurred deleting %s", path), e);
        }
    }

    public static void createDirectory() {
        Path path = directoryPath();
        try {
            if (!Files.exists(path))
                Files.createDirectories(path);

        } catch (IOException e) {
            Log.error(String.format("Error creating [%s] directory", path), e);
        }
    }

    // ---

    private static void loadEditors() {
        editors = new HashMap<>();
        try (
                FileInputStream inputStream = new FileInputStream(historyFile());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, getEncoding()))
        ) {
            String recordLine;
            while ((recordLine = bufferedReader.readLine()) != null) {
                String[] splitData = recordLine.split(";");

                String connectionID = getOrDefault(splitData, 0);
                if (!editors.containsKey(connectionID))
                    editors.put(connectionID, new ArrayList<>());

                String filePath = getOrDefault(splitData, 1);
                String number = getOrDefault(splitData, 2);
                String splitLocation = getOrDefault(splitData, 3);
                String autosave = getOrDefault(splitData, 4);

                editors.get(connectionID).add(new EditorData(filePath, number, splitLocation, autosave));
            }

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static void loadNumbers() {
        numbers = new ArrayList<>();
        try (
                FileInputStream inputStream = new FileInputStream(numbersFile());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, getEncoding()))
        ) {
            String recordLine;
            while ((recordLine = bufferedReader.readLine()) != null)
                numbers.add(Integer.parseInt(recordLine));

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    // ---

    private static void saveEditors() {
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(historyFile(), false);
                Writer writer = new OutputStreamWriter(fileOutputStream, getEncoding())
        ) {
            for (Map.Entry<String, List<EditorData>> entry : editors().entrySet()) {
                String key = entry.getKey();

                for (EditorData editorData : entry.getValue()) {
                    writer.append(key).append(";")
                            .append(editorData.getEditorPath().toString()).append(";")
                            .append(String.valueOf(editorData.getNumber())).append(";")
                            .append(String.valueOf(editorData.getSplitLocation())).append(";")
                            .append(String.valueOf(editorData.isAutosaveEnabled())).append(";")
                            .append(System.lineSeparator());
                }
            }
            writer.flush();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static void saveNumbers() {
        numbers().sort(Integer::compareTo);

        try (
                FileOutputStream fileOutputStream = new FileOutputStream(historyFile(), false);
                Writer writer = new OutputStreamWriter(fileOutputStream, getEncoding())
        ) {
            for (int number : numbers())
                writer.append(String.valueOf(number)).append(System.lineSeparator());
            writer.flush();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    // ---

    public static void restoreTabs(DatabaseConnection connection) {
        String connectionID = connection != null ? connection.getId() : NULL_CONNECTION;
        String encoding = getEncoding();

        for (EditorData editorData : new ArrayList<>(getEditors(connectionID))) {
            try {
                removeEditor(connectionID, editorData);

                File file = editorData.getEditorPath().toFile();
                QueryEditor queryEditor = new QueryEditor(
                        file.exists() ? FileUtils.loadFile(file, encoding) : Constants.EMPTY,
                        editorData.getEditorPath().toString(),
                        editorData.getSplitLocation(),
                        editorData.isAutosaveEnabled()
                );

                if (connection != null)
                    queryEditor.setSelectedConnection(connection);

                GUIUtilities.addCentralPane(
                        QueryEditor.TITLE,
                        QueryEditor.FRAME_ICON,
                        queryEditor,
                        null,
                        true
                );

            } catch (IOException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    // ---

    public static boolean isDefaultDirectory(QueryEditor editor) {
        try {
            Path editorPath = Paths.get(editor.getAbsolutePath()).normalize().getParent();
            return Files.isSameFile(directoryPath(), editorPath);

        } catch (IOException e) {
            Log.debug(e.getMessage(), e);
            return false;
        }
    }

    public static int getMinimumNumber() {

        List<Integer> numbersList = numbers();
        for (int i = 0; i < numbersList.size(); i++) {

            int number = numbersList.get(i);
            if (i == 0 && number != 1)
                return 1;

            if (i != 0 && (number - 1) != numbersList.get(i - 1) && number != numbersList.get(i - 1))
                return numbersList.get(i - 1) + 1;
        }

        return numbers().size() + 1;
    }

    private static String getEncoding() {
        String encoding = SystemProperties.getProperty("user", "system.file.encoding");
        return MiscUtils.isNull(encoding) ? encoding : "UTF8";
    }

    private static String getOrDefault(String[] splitData, int index) {
        return index < splitData.length ? splitData[index] : null;
    }

}
