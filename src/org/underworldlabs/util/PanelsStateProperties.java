package org.underworldlabs.util;

import org.executequery.log.Log;
import org.executequery.util.UserSettingsProperties;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class PanelsStateProperties {
    private static final String DELIMITER = "=";
    private static final String FILE_NAME = "re.user.panels.state";

    private final String className;
    private static String filePath;
    private static final Map<String, String> stateMap;

    static {
        stateMap = new HashMap<>();
        readFile();
    }

    public PanelsStateProperties(String className) {
        this.className = className;
    }

    public void put(String key, String value) {
        stateMap.put(buildKey(key), value);
    }

    public String get(String key) {
        return stateMap.get(buildKey(key));
    }

    public void save() {

        StringBuilder sb = new StringBuilder();
        for (String key : stateMap.keySet())
            sb.append(key).append(DELIMITER).append(stateMap.get(key)).append("\n");

        writeFile(sb);
    }

    public boolean isLoaded() {
        return !stateMap.isEmpty();
    }

    private static void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                int delimiterIndex = line.indexOf(DELIMITER);

                String key = line.substring(0, delimiterIndex);
                String value = line.substring(delimiterIndex + DELIMITER.length());
                stateMap.put(key, value);
            }

        } catch (FileNotFoundException e) {
            Log.warning(String.format("File %s not found", FILE_NAME));

        } catch (IOException e) {
            Log.error(String.format("Error reading %s file", FILE_NAME), e);
        }
    }

    private void writeFile(StringBuilder sb) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getFilePath(), false))) {
            writer.print(sb);

        } catch (IOException e) {
            Log.error(String.format("Error writing %s file", FILE_NAME), e);
        }
    }

    private static String getFilePath() {
        if (filePath == null)
            filePath = new UserSettingsProperties().getUserSettingsDirectory() + FILE_NAME;
        return filePath;
    }

    private String buildKey(String key) {
        return className + "." + key;
    }

}
