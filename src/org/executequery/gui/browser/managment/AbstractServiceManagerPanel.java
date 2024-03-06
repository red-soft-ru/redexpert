package org.executequery.gui.browser.managment;

import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.DefaultDriverLoader;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.AbstractPanel;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.FileUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractServiceManagerPanel extends AbstractPanel {
    protected JTabbedPane tabPane;
    protected JComboBox<DatabaseConnection> databaseBox;
    protected FileOutputStream fileLog;

    protected PipedOutputStream outputStream;

    protected PipedInputStream inputStream;

    protected BufferedReader bufferedReader;
    protected JButton fileLogButton;
    protected JTextField fileLogField;
    protected JButton fileDatabaseButton;
    protected JTextField fileDatabaseField;
    protected JTextField userField;
    protected JPasswordField passwordField;
    protected JCheckBox logToFileBox;
    protected JTextField hostField;
    protected NumberTextField portField;
    protected List<String> charsets;
    protected JComboBox charsetCombo;

    protected JPanel connectionPanel;

    protected JCheckBox parseBox;

    public AbstractServiceManagerPanel() {
        super();
    }

    public static String bundleString(String key) {
        return Bundles.get(AbstractServiceManagerPanel.class, key);
    }

    protected void initComponents() {
        loadCharsets();
        fileLogButton = WidgetFactory.createButton("fileLogButton", "...");
        fileLogButton.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(fileLogButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    if (fileLog != null) {
                        try {
                            fileLog.close();
                            fileLog = null;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    File file = fileChooser.getSelectedFile();
                    fileLogField.setText(file.getAbsolutePath());
                    try {
                        fileLog = new FileOutputStream(file, false);
                        fileLog.close();
                        fileLog = null;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        fileDatabaseButton = WidgetFactory.createButton("fileDatabaseButton", "...");
        fileDatabaseButton.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileDatabaseButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileDatabaseField.setText(file.getAbsolutePath());
                }
            }
        });
        fileLogField = WidgetFactory.createTextField("fileLogField");
        fileDatabaseField = WidgetFactory.createTextField("fileDatabaseField");
        userField = WidgetFactory.createTextField("userField");
        passwordField = WidgetFactory.createPasswordField("passwordField");
        logToFileBox = WidgetFactory.createCheckBox("logToFileBox", bundleString("LogToFile"));
        logToFileBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setEnableElements();
                if (fileLog != null) {
                    try {
                        fileLog.close();
                        fileLog = null;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        hostField = WidgetFactory.createTextField("hostField", "127.0.0.1");
        portField = WidgetFactory.createNumberTextField("portField");
        portField.setText("3050");
        charsetCombo = WidgetFactory.createComboBox("charsetCombo", charsets.toArray());
        DynamicComboBoxModel model = new DynamicComboBoxModel();
        List<DatabaseConnection> databaseConnectionList = new ArrayList<>();
        databaseConnectionList.add(null);
        databaseConnectionList.addAll(((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll());
        model.setElements(databaseConnectionList);
        databaseBox = WidgetFactory.createComboBox("databaseBox", model);
        databaseBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeDatabaseConnection();
            }
        });
        parseBox = WidgetFactory.createCheckBox("parseCheckBox", bundleString("parseTraceToGrid"));
        parseBox.setSelected(true);
        tabPane = new JTabbedPane();
        connectionPanel = new JPanel();
        initOtherComponents();
    }

    protected abstract void initOtherComponents();

    protected abstract void setEnableElements();

    protected abstract void changeDatabaseConnection();

    protected Driver loadDriver(DatabaseConnection dc) throws SQLException {
        Driver driver = null;
        Map<String, Driver> drivers = DefaultDriverLoader.getLoadedDrivers();
        if (dc != null) {
            for (String driverName : drivers.keySet()) {
                if (driverName.startsWith(String.valueOf(dc.getDriverId()))) {
                    driver = drivers.get(driverName);
                    break;
                }
            }
        }
        if (driver == null)
            driver = DefaultDriverLoader.getDefaultDriver();
        return driver;
    }

    protected void loadCharsets() {
        try {
            if (charsets == null)
                charsets = new ArrayList<String>();
            else
                charsets.clear();

            String resource = FileUtils.loadResource("org/executequery/charsets.properties");
            String[] strings = resource.split("\n");
            for (String s : strings) {
                if (!s.startsWith("#") && !s.isEmpty())
                    charsets.add(s);
            }
            java.util.Collections.sort(charsets);
            charsets.add(0, "NONE");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ServiceOutputStream extends PipedOutputStream {
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            fileLog.write(b, off, len);
            super.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            fileLog.write(b);
            super.write(b);
        }

        public void close() throws IOException {
            fileLog.close();
            super.close();
        }

        public void flush() throws IOException {
            fileLog.flush();
            super.flush();
        }
    }
}
