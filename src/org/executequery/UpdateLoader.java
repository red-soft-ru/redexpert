package org.executequery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
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
    private String repo = null;
    private String version = null;
    private String downloadLink;

    private JTextArea outText;
    private JButton cancelButton;
    private JButton restartButton;
    private JScrollPane scrollPane;
    private JPanel panel1;
    private JPanel panel2;

    public UpdateLoader(String perository) {
        initComponents();
        this.repo = perository;
    }

    private String getlastVersion(String repo) {
        StringBuilder buffer = new StringBuilder("");
        try {
            String path = repo + "/maven-metadata.xml";
            URL url = new URL(path);

            InputStream html = null;

            html = url.openStream();

            int c = 0;

            while (c != -1) {
                c = html.read();
                buffer.append((char) c);

            }
        } catch (MalformedURLException e) {
            GUIUtilities.displayExceptionErrorDialog("Cannot download update from repository.\n" +
                    "Please, check repository url.", e);
        } catch (IOException e) {
            GUIUtilities.displayExceptionErrorDialog("Cannot download update from repository.\n" +
                    "Please, check repository url.", e);
        }
        String s = buffer.toString();

        Pattern pattern = Pattern.compile("(<release>)([^<>]*)(<\\/release>)");
        Matcher m = pattern.matcher(s);
        String res = null;
        while (m.find()) {
            res = m.group(2);
        }

        return res;
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

        restartButton = new JButton("Restart");
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
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }

    private void cancelUpdate() {
        this.dispose();
    }

    void update() {
        this.setTitle("Updating from " + repo);
        outText.setText("Contacting Download Server...");
        this.downloadLink = repo + "/" + version + "/red_expert-" + version + "-bin.zip";
        download();
    }

    private void download() {
        worker = new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            downloadFile(downloadLink);
                            unzip();
                            File aNew = new File("");
                            boolean mkdir = aNew.mkdir();
                            copyFiles(new File(root), aNew.getAbsolutePath());
                            cleanup();
                            restartButton.setEnabled(true);
                            outText.setText(outText.getText() + "\nUpdate Finished!");
                            cancelButton.setText("Close");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "An error occurred while preforming update!");
                        }
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
        for (File ff : files) {
            if (ff.isDirectory()) {
                remove(ff);
                ff.delete();
            } else {
                ff.delete();
            }
        }
    }

    private void copyFiles(File f, String dir) throws IOException {
        File[] files = f.listFiles();
        for (File ff : files) {
            if (ff.isDirectory()) {
                new File(dir + "/" + ff.getName()).mkdir();
                copyFiles(ff, dir + "/" + ff.getName());
            } else {
                copy(ff.getAbsolutePath(), dir + "/" + ff.getName());
            }

        }
    }

    public void copy(String srFile, String dtFile) throws FileNotFoundException, IOException {

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

    private void unzip() throws IOException {
        int BUFFER = 2048;
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        ZipEntry entry;
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
                FileOutputStream fos = new
                        FileOutputStream(root + entry.getName());
                dest = new
                        BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }

    }

    private void downloadFile(String link) throws MalformedURLException, IOException {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        long max = conn.getContentLength();
        outText.setText(outText.getText() + "\n" + "Downloding file...\nUpdate Size(compressed): " + max + " Bytes");
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File("update.zip")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;
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
        version = getlastVersion(this.repo);
        String localVersion = System.getProperty("executequery.minor.version");

        if (version != null && localVersion != null) {
            int newVersion = Integer.valueOf(version.replaceAll("\\.", ""));
            int currentVersion = Integer.valueOf(localVersion.replaceAll("\\.", ""));
            return (newVersion > currentVersion);
        }
        return false;
    }
}
