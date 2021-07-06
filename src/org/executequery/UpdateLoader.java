package org.executequery;

import org.apache.commons.lang.StringUtils;
import org.executequery.http.JSONAPI;
import org.executequery.http.ReddatabaseAPI;
import org.executequery.log.Log;
import org.executequery.util.ApplicationProperties;
import org.executequery.util.UserProperties;
import org.json.JSONObject;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
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
public class UpdateLoader extends JFrame {

    private Thread worker;
    private String binaryZipUrl;
    private boolean releaseHub;

    public String getRepo() {
        return repo;
    }

    private static String repo;
    private String version = null;
    private String downloadLink;

    private JTextArea outText;
    private JButton cancelButton;
    private JButton restartButton;
    private JScrollPane scrollPane;
    private JPanel panel1;
    private JPanel panel2;
    private JProgressBar progressBar;
    private String repoArg;

    public void setRepoArg(String repoArg) {
        this.repoArg = repoArg;
    }

    public void setExternalArg(String externalArg) {
        this.externalArg = externalArg;
    }

    private String externalArg;

    private String root = "update/";

    public UpdateLoader(String repository) {
        initComponents();
        repo = repository;
    }

    private String getLastVersion(String repo) {
        StringBuilder buffer = new StringBuilder();
        try {
            URL myUrl = new URL(repo);
            URLConnection myUrlCon = myUrl.openConnection();
            InputStream input = myUrlCon.getInputStream();
            int c;
            while (((c = input.read()) != -1)) {
                buffer.append((char) c);
            }
            input.close();
        } catch (Exception e) {
            Log.error("Cannot download update from repository. " +
                    "Please, check repository url or try update later.");
            return null;
        }
        String s = buffer.toString();
        String[] ss = s.split("\n");
        String res = "0.0";
        for (int i = 0; i < ss.length; i++) {
            Pattern pattern;
            pattern = Pattern.compile("(<a href=\")([0-9]+[\\.][0-9]+.+)(/\">)");
            Matcher m = pattern.matcher(ss[i]);
            if (m.find()) {
                String r = m.group(2);
                try {
                    ApplicationVersion temp = new ApplicationVersion(r);
                    if (temp.isNewerThan(res))
                        res = r;
                } catch (Exception e) {
                    Log.debug("Big version:" + r, e);
                }

            }
        }
        if (!Objects.equals(res, "0.0"))
            return res;
        else
            return null;
    }

    public void setReleaseHub(boolean releaseHub) {
        this.releaseHub = releaseHub;
    }

    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());

        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());

        outText = new JTextArea();
        outText.setFont(UIManager.getDefaults().getFont("Label.font"));
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(outText);

        restartButton = new JButton("Restart now");
        restartButton.setEnabled(false);
        restartButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                launch();
            }
        });
        panel2.add(restartButton);

        cancelButton = new JButton("Cancel Update");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelUpdate();
            }
        });
        panel2.add(cancelButton);
        progressBar = new JProgressBar();
        panel1.add(progressBar, BorderLayout.NORTH);
        progressBar.setVisible(false);
        panel1.add(scrollPane, BorderLayout.CENTER);
        panel1.add(panel2, BorderLayout.SOUTH);

        add(panel1);
        pack();
        this.setSize(500, 400);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
    }

    private void cancelUpdate() {

        this.dispose();
    }

    public static void main(String[] args) {
        applySystemProperties();
        UpdateLoader updateLoader = new UpdateLoader(repo);
        for (String arg :
                args) {
            if (arg.equalsIgnoreCase("usereleasehub")) {
                updateLoader.setReleaseHub(true);
            } else if (arg.contains("version")) {
                int i = arg.indexOf('=');
                String ver = arg.substring(i + 1);
                updateLoader.setVersion(ver);
            } else if (arg.contains("-repo")) {
                updateLoader.setRepoArg(arg);
            } else if (arg.contains("externalProcessName")) {
                int i = arg.indexOf('=');
                String external = arg.substring(i + 1);
                updateLoader.setExternalArg(external);
            }

        }
        updateLoader.setVisible(true);
        updateLoader.update();
    }

    private static void applySystemProperties() {

        String encoding = stringApplicationProperty("system.file.encoding");
        if (StringUtils.isNotBlank(encoding)) {

            System.setProperty("file.encoding", encoding);
        }

        String settingDirName = stringPropertyFromConfig("eq.user.home.dir");
        settingDirName = settingDirName.replace("$HOME", System.getProperty("user.home"));
        System.setProperty("executequery.user.home.dir", settingDirName);
        ApplicationContext.getInstance().setUserSettingsDirectoryName(settingDirName);

        String build = stringApplicationProperty("eq.build");
        System.setProperty("executequery.build", build);
        ApplicationContext.getInstance().setBuild(build);
    }

    private void launch() {
        ProcessBuilder pb = null;
        try {
            StringBuilder sb = new StringBuilder("./RedExpert");
            if (System.getProperty("os.arch").toLowerCase().contains("amd64"))
                sb.append("64");
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                sb.append(".exe");
            if (repoArg == null)
                repoArg = "-repo=";
            System.out.println("Executing: " + sb.toString());
            pb = new ProcessBuilder(sb.toString(), repoArg);
            pb.directory(new File(System.getProperty("user.dir")));
            pb.start();

        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
        System.exit(0);
    }

    private void cleanup() {
        outText.setText(outText.getText() + "\nPreforming clean up...");
        File f = new File("update.zip");
        f.delete();
        remove(new File(root));
        try {
            Files.delete(Paths.get(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void remove(File f) {
        File[] files = f.listFiles();
        if (files != null) {
            for (File ff : files) {
                if (ff.isDirectory()) {
                    remove(ff);
                    ff.delete();
                } else {
                    ff.delete();
                }
            }
        }
    }

    private void copyFiles(File f, String dir) {
        File[] files = f.listFiles();
        if (files != null) {
            for (File ff : files) {
                if (ff.isDirectory()) {
                    new File(dir + "/" + ff.getName()).mkdir();
                    copyFiles(ff, dir + "/" + ff.getName());
                } else {
                    try {
                        copy(ff.getAbsolutePath(), dir + "/" + ff.getName());
                    } catch (IOException e) {
                        outText.setText(outText.getText() + "\n Copying error. " +
                                e.getMessage());
                    }
                }

            }
        }
    }

    public void copy(String srFile, String dtFile) throws IOException {

        File f1 = new File(srFile);
        File f2 = new File(dtFile);

        InputStream in = new FileInputStream(f1);

        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void unzip() {
        int BUFFER = 2048;
        BufferedOutputStream dest = null;
        BufferedInputStream is;
        ZipEntry entry;
        try {
            ZipFile zipfile = new ZipFile("update.zip");
            Enumeration e = zipfile.entries();
            (new File(root)).mkdir();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                outText.setText(outText.getText() + "\nExtracting: " + entry);
                if (entry.isDirectory())
                    (new File(root + entry.getName())).mkdir();
                else {
                    (new File(root + entry.getName())).createNewFile();
                    is = new BufferedInputStream
                            (zipfile.getInputStream(entry));
                    int count;
                    byte[] data = new byte[BUFFER];
                    try {
                        FileOutputStream fos = new
                                FileOutputStream(root + entry.getName());
                        dest = new
                                BufferedOutputStream(fos, BUFFER);
                        while ((count = is.read(data, 0, BUFFER))
                                != -1) {
                            dest.write(data, 0, count);
                        }
                    } catch (FileNotFoundException ex) {
                        outText.setText(outText.getText() + "\nExtracting " + entry + " error. " +
                                ex.getMessage());
                    } catch (IOException ex) {
                        outText.setText(outText.getText() + "\nExtracting " + entry + "error." +
                                ex.getMessage());
                    }
                    if (dest != null) {
                        dest.flush();
                        dest.close();
                    }
                    is.close();
                }
            }
            zipfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String stringPropertyFromConfig(String key) {
        Properties props = null;
        try {
            props = FileUtils.loadProperties(MiscUtils.loadURLs("./config/redexpert_config.ini;../config/redexpert_config.ini"));
            return props.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public boolean isNeedUpdate() {
        version = getLastVersion(repo);
        ApplicationVersion remoteVersion = new ApplicationVersion(version);
        String localVersion = System.getProperty("executequery.minor.version");

        if (localVersion != null) {
            return remoteVersion.isNewerThan(localVersion);
        }
        return false;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setBinaryZipUrl(String binaryZip) {
        this.binaryZipUrl = binaryZip;
    }

    private static String stringApplicationProperty(String key) {

        return applicationProperties().getProperty(key);
    }

    private static ApplicationProperties applicationProperties() {

        return ApplicationProperties.getInstance();
    }

    void update() {
        this.setTitle("Updating");
        if (releaseHub) {
            outText.setText("Contacting Download Server...");
            try {

                JSONObject obj = JSONAPI.getJsonObjectFromArray(JSONAPI.getJsonArray(
                        "http://builds.red-soft.biz/api/v1/artifacts/by_build/?project=red_expert&version=" + version),
                        "artifact_id",
                        "red_expert:bin:" + version + ":zip");
                downloadLink = "http://builds.red-soft.biz/" + obj.getString("file");
                download();
            } catch (Exception e) {
                outText.append("\n");
                e.printStackTrace(new PrintWriter(new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) throws IOException {
                        outText.append("\n");
                        outText.append(new String(cbuf));
                    }

                    @Override
                    public void flush() throws IOException {

                    }

                    @Override
                    public void close() throws IOException {

                    }
                }));
            }
        } else {
            outText.setText("Contacting Download Server...");
            if (!MiscUtils.isNull(repo)) {
                this.downloadLink = repo + "/" + version + "/red_expert-" + version + "-bin.zip";
                download();
            } else {
                try {
                    //изменить эту строку в соответствии с форматом имени файла на сайте
                    String filename = UserProperties.getInstance().getStringProperty("reddatabase.filename") + version + ".zip";
                    Map<String, String> heads = ReddatabaseAPI.getHeadersWithToken();
                    if (heads != null) {
                        String url = JSONAPI.getJsonObjectFromArray(JSONAPI.getJsonArray(UserProperties.getInstance().getStringProperty("reddatabase.get-files.url") + version,
                                heads), "filename", filename).getString("url");
                        downloadLink = JSONAPI.getJsonPropertyFromUrl(url + "genlink/", "link", heads);
                        download();
                    }
                } catch (Exception e) {
                    outText.append("\n");
                    e.printStackTrace(new PrintWriter(new Writer() {
                        @Override
                        public void write(char[] cbuf, int off, int len) throws IOException {
                            outText.append("\n");
                            outText.append(new String(cbuf));
                        }

                        @Override
                        public void flush() throws IOException {

                        }

                        @Override
                        public void close() throws IOException {

                        }
                    }));
                }
            }
        }
    }

    private void downloadFile(String link) throws IOException {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        long max = conn.getContentLength();
        outText.setText(outText.getText() + "\n" + "Downloading file...\nUpdate Size(compressed): " + getUsabilitySize(max));
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File("update.zip")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        long in = 0;
        int delimiter = 1024;
        progressBar.setVisible(true);
        progressBar.setMaximum((int) (max / delimiter));
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        String textOut = outText.getText();
        while ((bytesRead = is.read(buffer)) != -1) {
            in += bytesRead;
            fOut.write(buffer, 0, bytesRead);
            outText.setText(textOut + "\n" + getUsabilitySize(in));
            progressBar.setValue((int) (in / delimiter));
        }
        progressBar.setValue(0);
        progressBar.setVisible(false);
        fOut.flush();
        fOut.close();
        is.close();
        outText.setText(outText.getText() + "\nDownload Complete!");

    }

    String getUsabilitySize(long countByte) {
        int oneByte = 1024;
        int delimiter = 103;
        long drob = 0;
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

    private void download() {
        worker = new Thread(
                () -> {
                    try {
                        String parent = new File(ExecuteQuery.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
                        parent += "/";
                        File aNew = new File(parent);
                        root = parent + "/update/";
                        downloadFile(downloadLink);
                        unzip();
                        aNew.mkdir();
                        copyFiles(new File(root), aNew.getAbsolutePath());
                        cleanup();
                        restartButton.setEnabled(true);
                        outText.setText(outText.getText() + "\nUpdate Finished!");
                        cancelButton.setText("Restart later");
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Access denied. Please restart RedExpert as an admin!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "An error occurred while preforming update!");
                    }
                });
        worker.start();
    }

}
