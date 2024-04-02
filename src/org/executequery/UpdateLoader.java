package org.executequery;

import org.apache.commons.lang.StringUtils;
import org.executequery.http.JSONAPI;
import org.executequery.http.spi.DefaultRemoteHttpClient;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.util.ApplicationProperties;
import org.executequery.util.UserProperties;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vasiliy on 16.01.17.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "BooleanMethodIsAlwaysInverted"})
public class UpdateLoader extends JFrame {

    private static final String UPDATE_NAME = "redexpert_update";
    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();
    private final static char[] BASE_64_ARRAY = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private static String repo;

    private boolean releaseHub;
    private String downloadLink;
    private String repoArg;

    private String version = null;
    private String pathToZip = SEPARATOR;
    private String root = UPDATE_NAME + SEPARATOR;

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
        if (zipFilePath.endsWith(SEPARATOR))
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

                    File newDir = new File(dir + SEPARATOR + f.getName());
                    boolean result = newDir.mkdir();
                    System.out.println("Creating directory: " + newDir + (result ? " [success]" : " [fail]"));

                    copyFiles(f, dir + SEPARATOR + f.getName());

                } else
                    copy(f.getAbsolutePath(), dir + SEPARATOR + f.getName());
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
        root = pathToZip + UPDATE_NAME + SEPARATOR;
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

    @SuppressWarnings("SameParameterValue")
    private static String stringPropertyFromConfig(String key) {

        String result = "";
        try {
            Properties properties = FileUtils.loadProperties(MiscUtils.loadURLs("./config/redexpert_config.ini;../config/redexpert_config.ini"));
            if (properties != null)
                result = properties.getProperty(key);

        } catch (Exception e) {
            Log.error(e.getMessage(), e);
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
                String prop = UserProperties.getInstance().getStringProperty("reddatabase.get-files.url");
                String url = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                        JSONAPI.getJsonArray(prop + version),
                        "filename", filename)).getString("url");

                downloadLink = JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link");
                downloadArchive();
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
                    String url = Objects.requireNonNull(JSONAPI.getJsonObjectFromArray(
                            JSONAPI.getJsonArray(UserProperties.getInstance().getStringProperty("reddatabase.get-files.url") + version),
                            "filename", filename)).getString("url");

                    downloadLink = JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link");
                    download();

                } catch (Exception e) {
                    e.printStackTrace(new PrintWriter(new CustomWriter()));
                }
            }
        }
    }

    private static String base64Encode(String string) {

        StringBuilder encodedString = new StringBuilder();
        byte[] bytes = string.getBytes();

        int pad = 0;
        int currentByteIndex = 0;
        while (currentByteIndex < bytes.length) {

            byte byte1 = bytes[currentByteIndex++];
            byte byte2;
            byte byte3;

            if (currentByteIndex >= bytes.length) {
                byte2 = 0;
                byte3 = 0;
                pad = 2;

            } else {
                byte2 = bytes[currentByteIndex++];

                if (currentByteIndex >= bytes.length) {
                    byte3 = 0;
                    pad = 1;

                } else
                    byte3 = bytes[currentByteIndex++];
            }

            byte c1 = (byte) (byte1 >> 2);
            byte c2 = (byte) (((byte1 & 0x3) << 4) | (byte2 >> 4));
            byte c3 = (byte) (((byte2 & 0xf) << 2) | (byte3 >> 6));
            byte c4 = (byte) (byte3 & 0x3f);

            encodedString.append(BASE_64_ARRAY[c1]);
            encodedString.append(BASE_64_ARRAY[c2]);
            switch (pad) {
                case 0:
                    encodedString.append(BASE_64_ARRAY[c3]);
                    encodedString.append(BASE_64_ARRAY[c4]);
                    break;
                case 1:
                    encodedString.append(BASE_64_ARRAY[c3]);
                    encodedString.append("=");
                    break;
                case 2:
                    encodedString.append("==");
                    break;
            }
        }

        return encodedString.toString();
    }

    private void downloadFile(String link) throws IOException {

        if (!canDownload(false))
            return;

        URLConnection conn;
        URL url = new URL(link);

        DefaultRemoteHttpClient defaultRemoteHttpClient = new DefaultRemoteHttpClient();
        if (defaultRemoteHttpClient.isUsingProxy()) {

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(defaultRemoteHttpClient.getProxyHost(), defaultRemoteHttpClient.getProxyPort()));
            conn = url.openConnection(proxy);

            if (defaultRemoteHttpClient.hasProxyAuthentication()) {
                String userPassword = defaultRemoteHttpClient.getProxyUser() + ":" + defaultRemoteHttpClient.getProxyPassword();
                String encoded = base64Encode(userPassword);
                conn.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
            }
        } else
            conn = url.openConnection();

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

            String parent = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + SEPARATOR;
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

                String parent = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + SEPARATOR;
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

            Pattern pattern = Pattern.compile("(<a href=\")([0-9]+[.][0-9]+.+)(/\">)");
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

        pathToZip = System.getProperty("java.io.tmpdir") + SEPARATOR;

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

        System.out.println("\n-------------------------------");
        System.out.println("------ " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy  HH:mm")) + " ------");
        System.out.println("------ Performing update ------");
        System.out.println("-------------------------------");

        for (String arg : args) {

            if (arg.equalsIgnoreCase("usereleasehub")) {
                updateLoader.setReleaseHub(true);

            } else if (arg.contains("version")) {
                String ver = arg.substring(arg.indexOf('=') + 1);
                updateLoader.setVersion(ver);

            } else if (arg.contains("-repo")) {
                updateLoader.setRepoArg(arg);

            } else if (arg.contains("-root")) {
                String root = arg.substring(arg.indexOf('=') + 1);
                updateLoader.setRoot(root);

            } else if (arg.contains("-launch")) {
                launch = Boolean.parseBoolean(arg.substring(arg.indexOf('=') + 1));
            }
        }

        System.out.println("\nUpdate arguments:");
        System.out.println("\t> version: " + updateLoader.version);
        System.out.println("\t> root: " + updateLoader.root);

        updateLoader.replaceFiles();
        if (launch)
            updateLoader.launch();
        else
            ExecuteQuery.stop();
    }

}
