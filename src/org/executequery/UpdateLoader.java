package org.executequery;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.executequery.gui.LoginPasswordDialog;
import org.executequery.log.Log;
import org.executequery.util.UserProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vasiliy on 16.01.17.
 */
public class UpdateLoader extends JFrame {

    private Thread worker;
    private final String root = "update/";
    private String binaryZipUrl;

    public String getRepo() {
        return repo;
    }

    private String repo;
    private String version = null;
    private String downloadLink;

    private JTextArea outText;
    private JButton cancelButton;
    private JButton restartButton;
    private JScrollPane scrollPane;
    private JPanel panel1;
    private JPanel panel2;

    public UpdateLoader(String repository) {
        initComponents();
        this.repo = repository;
    }

    private String getLastVersion(String repo) {
        StringBuilder buffer = new StringBuilder("");
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

    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panel1 = new JPanel();
        panel1.setLayout(new BorderLayout());

        panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());

        outText = new JTextArea();
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

    JSONObject getJsonObject(String Url) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(Url);
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();


        return new JSONObject(text.toString());
    }

    JSONObject getJsonObject(String Url, Map<String, String> headers) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(Url);
        for (String key : headers.keySet()) {
            get.addRequestHeader(key, headers.get(key));
        }
        client.executeMethod(get);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();


        return new JSONObject(text.toString());
    }

    JSONObject postJsonObject(String Url, Map<String, String> parameters) throws IOException {

        StringBuilder text = new StringBuilder();
        HttpClient client = new HttpClient();
        PostMethod get = new PostMethod(Url);
        for (String key : parameters.keySet()) {
            get.addParameter(key, parameters.get(key));
        }
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();


        return new JSONObject(text.toString());
    }

    JSONArray getJsonArray(String Url, Map<String, String> headers) throws IOException {
        URL url;
        StringBuilder text = new StringBuilder();

        HttpClient client = new HttpClient();
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        GetMethod get = new GetMethod(Url);
        for (String key : headers.keySet()) {
            get.addRequestHeader(key, headers.get(key));
        }
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();

        return new JSONArray(text.toString());
    }

    JSONArray getJsonArray(String Url) throws IOException {
        URL url;
        StringBuilder text = new StringBuilder();

        HttpClient client = new HttpClient();
        //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        GetMethod get = new GetMethod(Url);
        client.executeMethod(get);

        BufferedReader br = new BufferedReader(
                new InputStreamReader(get.getResponseBodyAsStream()));

        String inputLine;


        while ((inputLine = br.readLine()) != null) {
            text.append(inputLine).append("\n");
        }

        br.close();

        return new JSONArray(text.toString());
    }


    JSONObject getJsonObjectFromArray(JSONArray mas, String key, String value) {
        for (int i = 0; i < mas.length(); i++) {
            String prop = mas.getJSONObject(i).getString(key);
            if (prop.contentEquals(value))
                return mas.getJSONObject(i);
        }
        return null;

    }

    String getJsonPropertyFromUrl(String Url, String key) throws IOException {

        return getJsonObject(Url).getString(key);
    }

    String getJsonPropertyFromUrl(String Url, String key, Map<String, String> headers) throws IOException {

        return getJsonObject(Url, headers).getString(key);
    }

    String postJsonPropertyFromUrl(String Url, String key, Map<String, String> parameters) throws IOException {

        return postJsonObject(Url, parameters).getString(key);
    }

    void update(boolean unstable) {
        this.setTitle("Updating");
        if (unstable) {
            outText.setText("Contacting Download Server...");
            try {

                JSONObject obj = getJsonObjectFromArray(getJsonArray(
                        "http://builds.red-soft.biz/api/artifacts/by_build/?project=red_expert&version=" + version),
                        "artifact_id",
                        "red_expert:red_expert:" + version + ":zip:bin");
                downloadLink = "http://builds.red-soft.biz/" + obj.getString("file");
                download();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        } else {
            outText.setText("Contacting Download Server...");
            if (!repo.isEmpty()) {
                this.downloadLink = repo + "/" + version + "/red_expert-" + version + "-bin.zip";
                download();
            } else {
                if (MiscUtils.isNull(SystemProperties.getStringProperty("user", "reddatabase.token"))) {
                    getToken();
                }
                String token = SystemProperties.getStringProperty("user", "reddatabase.token");
                if (!MiscUtils.isNull(token)) {
                    try {
                        //изменить эту строку в соответствии с форматом имени файла на сайте
                        String filename = UserProperties.getInstance().getStringProperty("reddatabase.filename") + version + ".zip";
                        Map<String, String> heads = new HashMap<>();
                        heads.put("Authorization", "Token " + token);
                        String url = getJsonObjectFromArray(getJsonArray(UserProperties.getInstance().getStringProperty("reddatabase.get-files.url") + version,
                                heads), "filename", filename).getString("url");
                        downloadLink = getJsonPropertyFromUrl(url + "genlink/", "link", heads);
                        download();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void getToken() {
        LoginPasswordDialog dialog = new LoginPasswordDialog("Autorisation", "To continue the update you need to enter\nyour login and password from the site reddatabase.ru");
        dialog.display();
        Map<String, String> params = new HashMap<>();
        params.put("username", dialog.getUsername());
        params.put("password", dialog.getPassword());
        try {
            String token = postJsonPropertyFromUrl(UserProperties.getInstance().getStringProperty("reddatabase.get-token.url"), "token", params);
            if (token != null) {
                SystemProperties.setProperty("user", "reddatabase.token", token);
                UserPreferencesManager.fireUserPreferencesChanged();
            }
        } catch (JSONException e) {
            if (GUIUtilities.displayConfirmCancelDialog("Unknown Login or Password. Try again?") == JOptionPane.YES_OPTION)
                getToken();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void download() {
        worker = new Thread(
                () -> {
                    try {
                        downloadFile(downloadLink);
                        unzip();
                        File aNew = new File("");
                        boolean mkdir = aNew.mkdir();
                        copyFiles(new File(root), aNew.getAbsolutePath());
                        cleanup();
                        restartButton.setEnabled(true);
                        outText.setText(outText.getText() + "\nUpdate Finished!");
                        cancelButton.setText("Restart later");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "An error occurred while preforming update!");
                    }
                });
        worker.start();
    }

    private void launch() {
        ApplicationContext instance = ApplicationContext.getInstance();
        String repo = "-repo=" + instance.getRepo();
        String[] run = {"java", "-jar", "RedExpert.jar", repo};
        try {
            Runtime.getRuntime().exec(run);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    private void cleanup() {
        outText.setText(outText.getText() + "\nPreforming clean up...");
        File f = new File("update.zip");
        f.delete();
        remove(new File(root));
        new File(root).delete();
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
                    byte data[] = new byte[BUFFER];
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

    private void downloadFile(String link) throws IOException {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        long max = conn.getContentLength();
        outText.setText(outText.getText() + "\n" + "Downloding file...\nUpdate Size(compressed): " + max + " Bytes");
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File("update.zip")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead;
        int in = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            in += bytesRead;
            fOut.write(buffer, 0, bytesRead);
        }
        fOut.flush();
        fOut.close();
        is.close();
        outText.setText(outText.getText() + "\nDownload Complete!");

    }

    public boolean isNeedUpdate() {
        version = getLastVersion(this.repo);
        String localVersion = System.getProperty("executequery.minor.version");

        if (version != null && localVersion != null) {
            int newVersion = Integer.valueOf(version.replaceAll("\\.", ""));
            int currentVersion = Integer.valueOf(localVersion.replaceAll("\\.", ""));
            return (newVersion > currentVersion);
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
}
