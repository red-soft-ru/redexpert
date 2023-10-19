package org.executequery;

import org.apache.commons.lang.StringUtils;
import org.executequery.http.JSONAPI;
import org.executequery.http.ReddatabaseAPI;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.ApplicationProperties;
import org.executequery.util.UserProperties;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vasiliy on 16.01.17.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class UpdateLoader extends JFrame {

    private static final String UPDATE_NAME = "redexpert_update";
    private static final String SEP = System.getProperty("file.separator");
    private static String repo;

    private boolean releaseHub;
    private String downloadLink;
    private String repoArg;

    private String version = null;
    private String pathToZip = SEP;
    private String root = UPDATE_NAME + SEP;

    // --- gui ---

    private JTextArea outText;
    private JButton cancelButton;
    private JButton restartButton;
    private JProgressBar progressBar;

    // ---

    public UpdateLoader(String repository) {
        repo = repository;
        initComponents();
    }

    private void initComponents() {

        // --- init ---

        restartButton = new JButton("Restart now");
        restartButton.setEnabled(false);
        restartButton.addActionListener(e -> launch());

        cancelButton = new JButton("Cancel Update");
        cancelButton.addActionListener(e -> dispose());

        outText = new JTextArea();
        outText.setFont(UIManager.getDefaults().getFont("Label.font"));

        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        // --- arrange ---

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(restartButton);
        buttonPanel.add(cancelButton);

        JPanel basePanel = new JPanel(new BorderLayout());
        basePanel.add(progressBar, BorderLayout.NORTH);
        basePanel.add(new JScrollPane(outText), BorderLayout.CENTER);
        basePanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- configure ---

        this.add(basePanel);
        this.pack();
        this.setSize(500, 400);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setLocation(
                Toolkit.getDefaultToolkit().getScreenSize().width / 2 - this.getSize().width / 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 2 - this.getSize().height / 2
        );
    }

    private static void applySystemProperties() {

        String encoding = stringApplicationProperty("system.file.encoding");
        String build = stringApplicationProperty("eq.build");
        String settingDirName = stringPropertyFromConfig("eq.user.home.dir")
                .replace("$HOME", System.getProperty("user.home"));

        if (StringUtils.isNotBlank(encoding))
            System.setProperty("file.encoding", encoding);

        System.setProperty("executequery.user.home.dir", settingDirName);
        ApplicationContext.getInstance().setUserSettingsDirectoryName(settingDirName);

        System.setProperty("executequery.build", build);
        ApplicationContext.getInstance().setBuild(build);
    }

    public void launch() {
        ExecuteQuery.restart(repoArg);
    }

    private void cleanup() {

        String zipFilePath = root;
        if (zipFilePath.endsWith(SEP))
            zipFilePath = zipFilePath.substring(0, zipFilePath.length() - 1);

        File zipFile = new File(zipFilePath + ".zip");
        boolean result = zipFile.delete();
        System.out.println("Removing: " + zipFile + (result ? " [success]" : " [fail]"));

        remove(new File(root));

        try {
            Files.delete(Paths.get(root));
        } catch (IOException ignored) {
        }
    }

    private void remove(File file) {

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {

                if (f.isDirectory())
                    remove(f);

                boolean result = f.delete();
                System.out.println("Removing: " + f + (result ? " [success]" : " [fail]"));
            }
        }
    }

    private void copyFiles(File file, String dir) {

        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {

                if (f.isDirectory()) {

                    File newDir = new File(dir + SEP + f.getName());
                    boolean result = newDir.mkdir();
                    System.out.println("Creating directory: " + newDir + (result ? " [success]" : " [fail]"));

                    copyFiles(f, dir + SEP + f.getName());

                } else
                    copy(f.getAbsolutePath(), dir + SEP + f.getName());
            }
        }
    }

    public void copy(String srFile, String dtFile) {

        File f1 = new File(srFile);
        File f2 = new File(dtFile);

        try (
                InputStream in = Files.newInputStream(f1.toPath());
                OutputStream out = Files.newOutputStream(f2.toPath())
        ) {

            int len;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            System.out.println("Coping file: " + f1 + " to " + f2.getParent() + " [success]");

        } catch (IOException e) {
            System.out.println("Copying error: " + e);
        }
    }

    private void unzip(boolean useLog) throws IOException {

        int bufferSize = 2048;
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream;

        ZipFile zipfile = new ZipFile(pathToZip + UPDATE_NAME + ".zip");
        Enumeration<?> entries = zipfile.entries();
        root = pathToZip + UPDATE_NAME + SEP;
        new File(root).mkdir();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();

            if (useLog)
                Log.info("Extracting: " + entry);

            if (entry.isDirectory()) {
                new File(root + entry.getName()).mkdir();

            } else {

                new File(root + entry.getName()).createNewFile();
                inputStream = new BufferedInputStream(zipfile.getInputStream(entry));

                try {

                    FileOutputStream fos = new FileOutputStream(root + entry.getName());
                    outputStream = new BufferedOutputStream(fos, bufferSize);

                    int count;
                    byte[] data = new byte[bufferSize];
                    while ((count = inputStream.read(data, 0, bufferSize)) != -1)
                        outputStream.write(data, 0, count);

                } catch (IOException e) {
                    if (useLog)
                        Log.error("Extracting " + entry + " error", e);
                }

                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                inputStream.close();
            }
        }

        zipfile.close();
    }

    private static String stringPropertyFromConfig(String key) {

        String result = "";
        try {

            Properties properties = FileUtils.loadProperties(MiscUtils.loadURLs("./config/redexpert_config.ini;../config/redexpert_config.ini"));
            if (properties != null)
                result = properties.getProperty(key);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean isNeedUpdate() {

        version = getLastVersion(repo);
        ApplicationVersion remoteVersion = new ApplicationVersion(version);
        String localVersion = System.getProperty("executequery.minor.version");

        return localVersion != null && remoteVersion.isNewerThan(localVersion);
    }

    public void downloadUpdate() throws IOException {

        Log.info("Contacting Download Server...");

        if (releaseHub) {

            String file = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                    JSONAPI.getJsonArray("http://builds.red-soft.biz/api/v1/artifacts/by_build/?project=red_expert&version=" + version),
                    "artifact_id", "red_expert:bin:" + version + ":zip")).getString("file");

            downloadLink = "http://builds.red-soft.biz/" + file;
            downloadArchive();

        } else {

            if (!MiscUtils.isNull(repo)) {
                this.downloadLink = repo + "/" + version + "/red_expert-" + version + "-bin.zip";
                downloadArchive();

            } else {

                //изменить эту строку в соответствии с форматом имени файла на сайте
                String filename = UserProperties.getInstance().getStringProperty("reddatabase.filename") + version + ".zip";
                Map<String, String> heads = ReddatabaseAPI.getHeadersWithToken();

                if (heads != null) {

                    String prop = UserProperties.getInstance().getStringProperty("reddatabase.get-files.url");
                    String url = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                            JSONAPI.getJsonArray(prop + version, heads),
                            "filename", filename)).getString("url");

                    downloadLink = JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link", heads);
                    downloadArchive();
                }

            }
        }
    }

    @SuppressWarnings("unused")
    void update() {

        this.setTitle("Updating");
        outText.setText("Contacting Download Server...");

        if (releaseHub) {
            try {

                String file = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                        JSONAPI.getJsonArray("http://builds.red-soft.biz/api/v1/artifacts/by_build/?project=red_expert&version=" + version),
                        "artifact_id", "red_expert:bin:" + version + ":zip")).getString("file");

                downloadLink = "http://builds.red-soft.biz/" + file;
                download();

            } catch (Exception e) {
                e.printStackTrace(new PrintWriter(new CustomWriter()));
            }

        } else {

            if (!MiscUtils.isNull(repo)) {
                this.downloadLink = repo + "/" + version + "/red_expert-" + version + "-bin.zip";
                download();

            } else {
                try {

                    //изменить эту строку в соответствии с форматом имени файла на сайте
                    String filename = UserProperties.getInstance().getStringProperty("reddatabase.filename") + version + ".zip";
                    Map<String, String> heads = ReddatabaseAPI.getHeadersWithToken();

                    if (heads != null) {

                        String url = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                                JSONAPI.getJsonArray(UserProperties.getInstance().getStringProperty("reddatabase.get-files.url") + version, heads),
                                "filename", filename)).getString("url");

                        downloadLink = JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link", heads);
                        download();
                    }

                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(new CustomWriter()));
                }
            }
        }
    }

    private void downloadFile(String link) throws IOException {

        if (!canDownload(false))
            return;

        URLConnection conn = new URL(link).openConnection();
        InputStream inputStream = conn.getInputStream();
        long max = conn.getContentLength();

        Log.info("Downloading file...\nUpdate Size(compressed): " + getUsabilitySize(max));
        outText.append("\nDownloading file...\nUpdate Size(compressed): " + getUsabilitySize(max));

        File dowloadedFile = new File(pathToZip, UPDATE_NAME + ".zip");
        BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(dowloadedFile.toPath()));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        long in = 0;
        int delimiter = 1024;

        progressBar.setVisible(true);
        progressBar.setMaximum((int) (max / delimiter));
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        String textOut = outText.getText();
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            in += bytesRead;
            outputStream.write(buffer, 0, bytesRead);
            progressBar.setString(getUsabilitySize(in));
            outText.setText(textOut + "\n" + getUsabilitySize(in));
            progressBar.setValue((int) (in / delimiter));
        }
        progressBar.setValue(0);
        progressBar.setVisible(false);

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        outText.append("\nDownload Complete!");
    }

    String getUsabilitySize(long countByte) {

        int oneByte = 1024;
        int delimiter = 103;
        long drob;

        if (countByte > 1024) {
            drob = (countByte % oneByte) / delimiter;
            countByte = countByte / oneByte;

            if (countByte > oneByte) {
                drob = (countByte % oneByte) / delimiter;
                countByte = countByte / oneByte;

                if (countByte > oneByte) {
                    drob = (countByte % oneByte) / delimiter;
                    countByte = countByte / oneByte;

                    if (countByte > oneByte) {
                        drob = (countByte % oneByte) / delimiter;
                        countByte = countByte / oneByte;
                        return countByte + "," + drob + "Tb";

                    } else return countByte + "," + drob + "Gb";
                } else return countByte + "," + drob + "Mb";
            } else return countByte + "," + drob + "Kb";
        } else return countByte + "b";
    }

    public void unzipLocale() throws IOException {
        unzip(true);
    }

    private void downloadArchive() throws IOException {
        downloadFile(downloadLink);
    }

    public void replaceFiles() {
        try {

            String parent = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + SEP;
            File aNew = new File(parent);
            aNew.mkdir();

            System.out.println("\n--- Replacing unzipped files ---\n");
            copyFiles(new File(root), aNew.getAbsolutePath());

            System.out.println("\n--- Removing downloaded sources ---\n");
            cleanup();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void download() {
        Thread worker = new Thread(() -> {
            try {

                String parent = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + SEP;
                File aNew = new File(parent);
                downloadFile(downloadLink);
                unzip(false);
                aNew.mkdir();
                copyFiles(new File(root), aNew.getAbsolutePath());
                cleanup();

                restartButton.setEnabled(true);
                outText.append("\nUpdate Finished!");
                cancelButton.setText("Restart later");

            } catch (FileNotFoundException e) {
                e.printStackTrace(System.out);
                JOptionPane.showMessageDialog(null, "Access denied. Please restart RedExpert as an admin!");

            } catch (Exception e) {
                e.printStackTrace(System.out);
                JOptionPane.showMessageDialog(null, "An error occurred while preforming update!");
            }

        });
        worker.start();
    }

    private static String stringApplicationProperty(String key) {
        return ApplicationProperties.getInstance().getProperty(key);
    }

    private String getLastVersion(String repo) {

        StringBuilder buffer = new StringBuilder();
        try (InputStream input = new URL(repo).openConnection().getInputStream()) {

            int character;
            while ((character = input.read()) != -1)
                buffer.append((char) character);

        } catch (Exception e) {
            Log.error("Cannot download update from repository. Please, check repository url or try update later.");
            return null;
        }

        String result = "0.0";
        for (String value : buffer.toString().split("\n")) {

            Pattern pattern = Pattern.compile("(<a href=\")([0-9]+[\\.][0-9]+.+)(/\">)");
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {

                String str = matcher.group(2);
                try {
                    if (new ApplicationVersion(str).isNewerThan(result))
                        result = str;

                } catch (Exception e) {
                    Log.debug("Big version:" + str, e);
                }
            }
        }

        return (!Objects.equals(result, "0.0")) ? result : null;
    }

    public boolean canDownload(boolean showMessage) {

        pathToZip = System.getProperty("java.io.tmpdir") + SEP;

        File tempDir = new File(pathToZip);
        if (!isCanReadWrite(tempDir, showMessage))
            return false;

        try {
            File parentDir = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
            if (!isCanReadWrite(parentDir, showMessage))
                return false;

        } catch (URISyntaxException e) {
            Log.error("Permissions check unsuccessful", e);
            return false;
        }

        Log.info("Permissions check successful");
        return true;
    }

    private boolean isCanReadWrite(File file, boolean showMessage) {

        Log.info("Permissions check for: " + file);

        if (!Files.isWritable(file.toPath()) || !Files.isReadable(file.toPath())) {
            if (showMessage)
                GUIUtilities.displayWarningMessage(String.format(Bundles.get("UpdateLoader.PermissionsDenied"), file.getAbsolutePath()));
            return false;
        }

        return true;
    }

    public String getRepo() {
        return repo;
    }

    public String getRoot() {
        return root;
    }

    public String getVersion() {
        return version;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRepoArg(String repoArg) {
        this.repoArg = repoArg;
    }

    public void setReleaseHub(boolean releaseHub) {
        this.releaseHub = releaseHub;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    private class CustomWriter extends Writer {

        @Override
        public void write(char[] cbuf, int off, int len) {
            outText.append("\n" + new String(cbuf));
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() {

        }

    } // class CustomWriter

    // ---

    public static void main(String[] args) {

        applySystemProperties();
        UpdateLoader updateLoader = new UpdateLoader(repo);
        boolean launch = true;

        for (String arg : args) {

            if (arg.equalsIgnoreCase("usereleasehub")) {
                updateLoader.setReleaseHub(true);

            } else if (arg.contains("version")) {
                String ver = arg.substring(arg.indexOf('=') + 1);
                updateLoader.setVersion(ver);

            } else if (arg.contains("-repo")) {
                updateLoader.setRepoArg(arg);

//            } else if (arg.contains("externalProcessName")) {
//                String external = arg.substring(arg.indexOf('=') + 1);
//                updateLoader.setExternalArg(external);

            } else if (arg.contains("-root")) {
                String root = arg.substring(arg.indexOf('=') + 1);
                updateLoader.setRoot(root);

            } else if (arg.contains("-launch")) {
                launch = Boolean.parseBoolean(arg.substring(arg.indexOf('=') + 1));
            }
        }

        System.out.println("\n-------------------------------");
        System.out.println("------ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm")) + " ------");
        System.out.println("------ Performing update ------");
        System.out.println("-------------------------------");

        updateLoader.replaceFiles();
        if (launch)
            updateLoader.launch();
        else
            ExecuteQuery.stop();
    }

}
