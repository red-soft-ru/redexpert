package org.executequery.gui.editor;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.util.SystemResources;
import org.underworldlabs.util.FileUtils;

import java.io.*;
import java.util.*;

public class QueryEditorHistory {
    private static final String HISTORY_FILE = "QueryEditorHistory.csv";
    private static final String NUMBERS_FILE = "QueryEditorNumbers.csv";
    static QueryEditorHistory queryEditorHistory;
    private static Map<String, List<PathNumber>> editors;
    private static List<Integer> numbers;

    public static final String NULL_CONNECTION = "null_connection";

    public static QueryEditorHistory getInstance() {
        if (queryEditorHistory == null)
            queryEditorHistory = new QueryEditorHistory();
        return queryEditorHistory;
    }

    public static void addEditor(String connection, String editor, int number) {
        addEditor(connection, new PathNumber(editor, number));

    }

    public static void addEditor(String connection, PathNumber pathNumber) {
        getEditors(connection).add(pathNumber);
        if (pathNumber.number != -1) {
            numbers().add(pathNumber.number);
            saveNumbers();
        }
        saveEditors();
    }

    public static void removeEditor(String connection, String editor) {
        PathNumber pathNumber = getEditors(connection).get(indexOfEditor(editor, getEditors(connection)));
        getEditors(connection).remove(pathNumber);
        if (pathNumber.number != -1) {
            numbers().remove((Integer) pathNumber.number);
            saveNumbers();
        }
        saveEditors();
    }

    private static int indexOfEditor(String editor, List<PathNumber> list) {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).path.equals(editor))
                return i;
        }
        return -1;
    }


    public static void changedConnectionEditor(String oldConnection, String newConnection, String editor) {
        int number = getEditors(oldConnection).get(indexOfEditor(editor, getEditors(oldConnection))).number;
        removeEditor(oldConnection, editor);
        addEditor(newConnection, editor, number);
    }

    public static List<PathNumber> getEditors(DatabaseConnection connection) {
        return getEditors(connection.getName());
    }

    public static List<PathNumber> getEditors(String connectionName) {
        List<PathNumber> list = editors().get(connectionName);
        if (list == null) {
            list = new ArrayList<>();
            editors().put(connectionName, list);
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
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] strings = strLine.split(";");
                if (!editors.containsKey(strings[0]))
                    editors.put(strings[0], new ArrayList<>());
                editors.get(strings[0]).add(new PathNumber(strings[1], Integer.parseInt(strings[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            FileWriter writer = new FileWriter(historyFile(), false);
            for (String key : editors().keySet()) {
                List<PathNumber> list = editors().get(key);
                for (int i = 0; i < list.size(); i++) {
                    writer.append(key + ";" + list.get(i).path + ";" + list.get(i).number + "\n");
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
        for (int i = 0; i < numbers().size(); i++) {
            int number = numbers().get(i);
            if (i == 0) {
                if (number != 1)
                    return 1;
            } else {
                if (number - 1 != numbers().get(i - 1)) {
                    return numbers().get(i - 1) + 1;
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

    public static void restoreTabs(String connectionName) {
        List<PathNumber> copy = new ArrayList<>();
        copy.addAll(getEditors(connectionName));
        for (int i = 0; i < copy.size(); i++) {
            try {
                removeEditor(connectionName, copy.get(i).path);
                File file = new File(copy.get(i).path);
                if (file.exists()) {
                    String contents = FileUtils.loadFile(file);
                    QueryEditor queryEditor = new QueryEditor(contents, copy.get(i).path);
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
        public String path;
        public int number;

        public PathNumber(String path, int number) {
            this.path = path;
            this.number = number;
        }
    }
}
