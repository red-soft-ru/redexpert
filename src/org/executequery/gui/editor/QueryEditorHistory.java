package org.executequery.gui.editor;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.gui.editor.autocomplete.Parameter;
import org.executequery.log.Log;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.SystemProperties;

import java.io.*;
import java.util.*;

public class QueryEditorHistory {
    private static final String HISTORY_FILE = "QueryEditorHistory.csv";
    private static final String NUMBERS_FILE = "QueryEditorNumbers.csv";
    static QueryEditorHistory queryEditorHistory;
    private static Map<String, List<PathNumber>> editors;
    private static List<Integer> numbers;
    private static Map<DatabaseConnection, List<Parameter>> historyParameters;

    public static final String NULL_CONNECTION = "null_connection";

    public static QueryEditorHistory getInstance() {
        if (queryEditorHistory == null)
            queryEditorHistory = new QueryEditorHistory();
        return queryEditorHistory;
    }

    public static Map<DatabaseConnection, List<Parameter>> getHistoryParameters() {
        if (historyParameters == null)
            historyParameters = new HashMap<>();
        return historyParameters;
    }

    public static void addEditor(String connectionID, String editor, int number, int splitDividerLocation) {
        addEditor(connectionID, new PathNumber(editor, number, splitDividerLocation));
    }

    public static void addEditor(String connectionID, PathNumber pathNumber) {
        List<PathNumber> paths = getEditors(connectionID);
        boolean contains = false;
        for (PathNumber path : paths)
            if (path.path.contentEquals(pathNumber.path)) {
                contains = true;
                break;
            }
        if (!contains) {
            getEditors(connectionID).add(pathNumber);
            if (pathNumber.number != -1) {
                numbers().add(pathNumber.number);
                saveNumbers();
            }
            saveEditors();
        }
    }

    public static PathNumber getEditor(String connectionID, String editor) {
        List<PathNumber> editors = getEditors(connectionID);
        int ind = indexOfEditor(editor, editors);
        return editors.get(ind);
    }

    public static void removeEditor(String connectionID, String editor) {
        List<PathNumber> editors = getEditors(connectionID);
        int ind = indexOfEditor(editor, editors);
        PathNumber pathNumber = editors.get(ind);
        getEditors(connectionID).remove(pathNumber);
        if (pathNumber.number != -1) {
            numbers().remove((Integer) pathNumber.number);
            saveNumbers();
        }
        saveEditors();
    }

    private static int indexOfEditor(String editor, List<PathNumber> list) {
        editor = editor.replaceAll("\\\\", "/");
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).path.replaceAll("\\\\", "/").equals(editor))
                return i;
        }
        return -1;
    }

    public static void changedConnectionEditor(String oldConnectionID, String newConnectionID, String editor) {
        PathNumber pathNumber = getEditors(oldConnectionID).get(indexOfEditor(editor, getEditors(oldConnectionID)));

        removeEditor(oldConnectionID, editor);
        addEditor(newConnectionID, editor, pathNumber.number, pathNumber.splitDividerLocation);
    }

    public static List<PathNumber> getEditors(DatabaseConnection connection) {
        return getEditors(connection.getId());
    }

    public static List<PathNumber> getEditors(String connectionID) {
        List<PathNumber> list = editors().get(connectionID);
        if (list == null) {
            list = new ArrayList<>();
            editors().put(connectionID, list);
        }
        return list;
    }

    private static Map<String, List<PathNumber>> editors() {
        if (editors == null)
            loadEditors();
        return editors;
    }

    private static List<Integer> numbers() {
        if (numbers == null)
            loadNumbers();
        return numbers;
    }

    public static void checkAndCreateDir() {
        File f = new File(editorDirectory());
        if (!f.exists())
            f.mkdir();
    }

    private static File historyFile() {
        checkAndCreateDir();
        File f = null;
        try {
            f = new File(editorDirectory() + HISTORY_FILE);
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private static File numbersFile() {
        checkAndCreateDir();
        File f = null;
        try {
            f = new File(editorDirectory() + NUMBERS_FILE);
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }

    private static void loadEditors() {
        editors = new HashMap<>();
        try {
            FileInputStream fstream = new FileInputStream(historyFile());
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream, SystemProperties.getProperty("user", "system.file.encoding")));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] strings = strLine.split(";");
                if (!editors.containsKey(strings[0]))
                    editors.put(strings[0], new ArrayList<>());
                editors.get(strings[0]).add(new PathNumber(strings[1], Integer.parseInt(strings[2]), Integer.parseInt(strings[3])));
            }
        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static void loadNumbers() {
        numbers = new ArrayList<>();
        try {
            FileInputStream fstream = new FileInputStream(numbersFile());
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                numbers.add(Integer.parseInt(strLine));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveEditors() {

        String charset = SystemProperties.getProperty("user", "system.file.encoding");
        if (charset == null)
            charset = Constants.EMPTY;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(historyFile(), false);
            Writer writer = new OutputStreamWriter(fileOutputStream, charset);

            for (String key : editors().keySet()) {
                for (PathNumber pathNumber : editors().get(key)) {
                    writer.append(key).append(";")
                            .append(pathNumber.path).append(";")
                            .append(String.valueOf(pathNumber.number)).append(";")
                            .append(String.valueOf(pathNumber.splitDividerLocation)).append("\n");
                }
            }
            writer.flush();

        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    private static void sortNumbers() {
        numbers().sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                if (i1 > i2)
                    return 1;
                if (i1 < i2)
                    return -1;
                return 0;

            }
        });
    }

    private static void saveNumbers() {
        try {
            FileWriter writer = new FileWriter(numbersFile(), false);
            sortNumbers();
            for (int i = 0; i < numbers().size(); i++)
                writer.append(numbers().get(i) + System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getMinNumber() {
        List<Integer> nums = numbers();
        for (int i = 0; i < nums.size(); i++) {
            int number = nums.get(i);
            if (i == 0) {
                if (number != 1)
                    return 1;
            } else {
                if ((number - 1) != nums.get(i - 1)&&number!=nums.get(i-1)) {
                    return nums.get(i - 1) + 1;
                }
            }
        }
        return numbers().size() + 1;
    }

    public static boolean isDefaultEditorDirectory(QueryEditor editor) {
        String path = editor.getAbsolutePath();

        path = path.substring(0, path.lastIndexOf(System.getProperty("file.separator")) + 1);
        return editorDirectory().equals(path);
    }

    public static String editorDirectory() {
        return SystemResources.userSettingsDirectoryForCurrentBuild() + "QueryEditor" + System.getProperty("file.separator");
    }

    public static void removeFile(String path) {
        File f = new File(path);
        if (f.exists())
            f.delete();
    }

    public static void restoreTabs(DatabaseConnection connection) {
        String connectionID = NULL_CONNECTION;
        if (connection != null)
            connectionID = connection.getId();
        String encoding = SystemProperties.getProperty("user", "system.file.encoding");
        List<PathNumber> copy = new ArrayList<>();
        copy.addAll(getEditors(connectionID));
        for (int i = 0; i < copy.size(); i++) {
            try {
                removeEditor(connectionID, copy.get(i).path);
                File file = new File(copy.get(i).path);
                if (file.exists()) {
                    String contents = FileUtils.loadFile(file, encoding);
                    QueryEditor queryEditor = new QueryEditor(contents, copy.get(i).path, copy.get(i).splitDividerLocation);
                    if (connection != null)
                        queryEditor.setSelectedConnection(connection);
                    GUIUtilities.addCentralPane(QueryEditor.TITLE,
                            QueryEditor.FRAME_ICON,
                            queryEditor,
                            null,
                            true);
                } else {
                    QueryEditor queryEditor = new QueryEditor("", copy.get(i).path, copy.get(i).splitDividerLocation);
                    if (connection != null)
                        queryEditor.setSelectedConnection(connection);
                    GUIUtilities.addCentralPane(QueryEditor.TITLE,
                            QueryEditor.FRAME_ICON,
                            queryEditor,
                            null,
                            true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class PathNumber {
        private final int splitDividerLocation;
        public String path;
        public int number;

        public PathNumber(String path, int number, int splitDividerLocation) {
            this.path = path;
            this.number = number;
            this.splitDividerLocation = splitDividerLocation;
        }

    } // PathNumber class

}
