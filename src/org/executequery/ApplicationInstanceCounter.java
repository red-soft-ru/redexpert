package org.executequery;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApplicationInstanceCounter {

    private static final String FILE_NAME =
            ApplicationContext.getInstance().getUserSettingsHome() + System.getProperty("file.separator") + "instancesCounter";
    private static final String RUNTIME_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final String PID = RUNTIME_NAME.split("@")[0];

    public static void add() {

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            writer.println(RUNTIME_NAME);

        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public static void remove() {
        remove(PID);
    }

    public static int getCount() {

        checkInstances();

        int count = 0;
        try (Scanner scanner = new Scanner(new File(FILE_NAME))) {
            while (scanner.hasNextLine()) {
                count++;
                scanner.nextLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        }
        return count;
    }

    private static void remove(String pid) {

        File oldFile = new File(FILE_NAME);
        File newFile = new File(FILE_NAME + ".tmp");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(oldFile));
                PrintWriter writer = new PrintWriter(new FileWriter(newFile))
        ) {

            String line;
            while ((line = reader.readLine()) != null)
                if (!line.startsWith(pid))
                    writer.println(line);

            if (!oldFile.delete())
                throw new Exception("Unable to delete old instancesCounter file");
            if (!newFile.renameTo(oldFile))
                throw new Exception("Unable to rename new instancesCounter file");

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static void checkInstances() {

        List<String> pidsList = getPidsList();
        File oldFile = new File(FILE_NAME);
        try (BufferedReader reader = new BufferedReader(new FileReader(oldFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String pid = line.split("@")[0];
                if (!pidsList.contains(pid))
                    remove(pid);
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static List<String> getPidsList() {

        String[] getPidCmd = System.getProperty("os.name").toLowerCase().contains("win") ?
                new String[]{"tasklist | find \"RedExpert\""} :
                new String[]{"/bin/sh", "-c", "ps aux | grep RedExpert"};

        List<String> pidList = new ArrayList<>();
        try {

            Process process = Runtime.getRuntime().exec(getPidCmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null)
                pidList.add(line.replaceAll("\\s+", " ").split(" ")[1]);

            reader.close();

        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        return pidList;
    }

}
