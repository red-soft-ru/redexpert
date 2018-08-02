package org.executequery.gui.browser;

import biz.redsoft.IFBTraceManager;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.gui.browser.managment.tracemanager.BuildConfigurationPanel;
import org.executequery.gui.browser.managment.tracemanager.ColumnsCheckPanel;
import org.executequery.gui.browser.managment.tracemanager.SessionManagerPanel;
import org.executequery.gui.browser.managment.tracemanager.TablePanel;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.gui.browser.managment.tracemanager.net.SessionInfo;
import org.executequery.repository.DatabaseConnectionRepository;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.FileUtils;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TraceManagerPanel extends JPanel implements TabView {

    public static final String TITLE = "Trace Manager";
    private IFBTraceManager traceManager;
    private TablePanel loggerPanel;
    private Timer timer;
    private OutputStream fileLog;
    private OutputStream outputStream;
    private Lock lock;
    private StringBuilder sb;
    private JButton fileLogButton;
    private JButton fileDatabaseButton;
    private JButton fileConfButton;
    private JButton startStopSessionButton;
    private JButton openFileLog;
    private JTextField fileLogField;
    private JTextField fileDatabaseField;
    private JTextField fileConfField;
    private JTextField openFileLogField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JCheckBox logToFileBox;
    private JCheckBox useBuildConfBox;
    private JTextField hostField;
    private NumberTextField portField;
    private JTextField sessionField;
    private JComboBox<DatabaseConnection> databaseBox;
    private int idLogMessage = 0;
    private boolean changed = false;
    private List<String> charsets;
    private JComboBox charsetCombo;
    private JTabbedPane tabPane;
    private JButton hideShowTabPaneButton;
    private Message message;
    private List<SessionInfo> sessions;
    private SessionManagerPanel sessionManagerPanel;
    private BuildConfigurationPanel confPanel;

    private void init() {
        message = Message.LOG_MESSAGE;
        sessions = new ArrayList<>();
        initTraceManager();
        sessionField = new JTextField();
        sessionField.setText("Session");
        sessionManagerPanel = new SessionManagerPanel(traceManager, sessionField);
        loadCharsets();
        ColumnsCheckPanel columnsCheckPanel = new ColumnsCheckPanel();
        loggerPanel = new TablePanel(columnsCheckPanel);
        lock = new ReentrantLock();
        timer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerAction();
            }
        });
        sb = new StringBuilder();
        fileLogButton = new JButton("...");
        fileDatabaseButton = new JButton("...");
        fileConfButton = new JButton("...");
        openFileLog = new JButton("...");
        fileLogField = new JTextField();
        fileDatabaseField = new JTextField();
        fileConfField = new JTextField();
        openFileLogField = new JTextField();
        userField = new JTextField();
        passwordField = new JPasswordField();
        logToFileBox = new JCheckBox("Log to file");
        hostField = new JTextField("127.0.0.1");
        portField = new NumberTextField();
        portField.setText("3050");
        useBuildConfBox = new JCheckBox("Use Config File");
        useBuildConfBox.setSelected(true);
        useBuildConfBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileConfButton.setEnabled(useBuildConfBox.isSelected());
                fileConfField.setEnabled(useBuildConfBox.isSelected());
            }
        });
        charsetCombo = new JComboBox<>(charsets.toArray());
        DynamicComboBoxModel model = new DynamicComboBoxModel();
        List<DatabaseConnection> databaseConnectionList = new ArrayList<>();
        databaseConnectionList.add(null);
        databaseConnectionList.addAll(((DatabaseConnectionRepository) RepositoryCache.load(DatabaseConnectionRepository.REPOSITORY_ID)).findAll());
        model.setElements(databaseConnectionList);
        databaseBox = new JComboBox<>(model);
        databaseBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (databaseBox.getSelectedItem() != null) {
                    DatabaseConnection dc = (DatabaseConnection) databaseBox.getSelectedItem();
                    fileDatabaseField.setText(dc.getSourceName());
                    userField.setText(dc.getUserName());
                    passwordField.setText(dc.getUnencryptedPassword());
                    hostField.setText(dc.getHost());
                    portField.setText(dc.getPort());
                    sessionField.setText(dc.getName() + "_trace_session");
                    charsetCombo.setSelectedItem(dc.getCharset());
                }
            }
        });
        startStopSessionButton = new JButton("Start");
        fileLogButton.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showSaveDialog(fileLogButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileLogField.setText(file.getAbsolutePath());
                    try {
                        fileLog = new FileOutputStream(file, false);
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });


        fileDatabaseButton.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileDatabaseButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileDatabaseField.setText(file.getAbsolutePath());
                }
            }
        });

        fileConfButton.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileConfButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileConfField.setText(file.getAbsolutePath());
                }
            }
        });

        openFileLog.addActionListener(new ActionListener() {
            FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(openFileLog);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    openFileLogField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    String s = "";
                    boolean finded = false;
                    clearAll();
                    idLogMessage = 0;
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(openFileLogField.getText())));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String str = line.trim();
                            if (str.matches(".?\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+.*")) {
                                if (finded) {
                                    LogMessage logMessage = new LogMessage(s);
                                    idLogMessage++;
                                    logMessage.setId(idLogMessage);
                                    loggerPanel.addRow(logMessage);
                                }
                                finded = true;
                                s = str + "\n";
                            } else {
                                s += str + "\n";
                            }
                        }
                        LogMessage logMessage = new LogMessage(s);
                        idLogMessage++;
                        logMessage.setId(idLogMessage);
                        loggerPanel.addRow(logMessage);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e1) {
                            }
                        }
                    }
                }
            }
        });

        startStopSessionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startStopSessionButton.getText().toUpperCase().contentEquals("START")) {
                    if (logToFileBox.isSelected())
                        outputStream = new OutputStream() {
                            @Override
                            public void write(int b) throws IOException {
                                fileLog.write(b);
                                lock.lock();
                                changed = true;
                                sb.append((char) b);
                                lock.unlock();
                            }
                        };
                    else
                        outputStream = new OutputStream() {
                            @Override
                            public void write(int b) {
                                lock.lock();
                                changed = true;
                                sb.append((char) b);
                                lock.unlock();
                            }
                        };
                    traceManager.setUser(userField.getText());
                    traceManager.setPassword(new String(passwordField.getPassword()));
                    traceManager.setLogger(outputStream);
                    traceManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset((String) charsetCombo.getSelectedItem()));
                    traceManager.setDatabase(fileDatabaseField.getText());
                    traceManager.setHost(hostField.getText());
                    traceManager.setPort(portField.getValue());
                    timer.start();
                    try {
                        String conf;
                        if (useBuildConfBox.isSelected())
                            conf = traceManager.loadConfigurationFromFile(fileConfField.getText());
                        else conf = confPanel.getConfig();
                        traceManager.startTraceSession(sessionField.getText(), conf);
                        startStopSessionButton.setText("Stop");
                        logToFileBox.setEnabled(false);
                    } catch (Exception e1) {
                        GUIUtilities.displayExceptionErrorDialog("Error start Trace Manager", e1);
                    }
                } else try {
                    traceManager.stopTraceSession(traceManager.getSessionID(sessionField.getText()));
                    startStopSessionButton.setText("Start");
                    logToFileBox.setEnabled(true);
                } catch (SQLException e1) {
                    GUIUtilities.displayExceptionErrorDialog("Error stop Trace Manager", e1);
                }
            }
        });

        hideShowTabPaneButton = new JButton("Hide Top Panel");
        hideShowTabPaneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabPane.setVisible(!tabPane.isVisible());
                if (hideShowTabPaneButton.getText().contentEquals("Hide Top Panel"))
                    hideShowTabPaneButton.setText("Show Top Panel");
                else hideShowTabPaneButton.setText("Hide Top Panel");
            }
        });

        tabPane = new JTabbedPane();
        JPanel connectionPanel = new JPanel();
        confPanel = new BuildConfigurationPanel();

        setLayout(new GridBagLayout());
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.add(tabPane, new GridBagConstraints(0, 1,
                3, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));

        add(topPanel, new GridBagConstraints(0, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0));

        add(hideShowTabPaneButton, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(startStopSessionButton, new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        JLabel label = new JLabel("Open filelog");
        topPanel.add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        topPanel.add(openFileLog, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        topPanel.add(openFileLogField, new GridBagConstraints(2, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(loggerPanel, new GridBagConstraints(0, 3,
                1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5),
                0, 0));

        tabPane.add("Connection", connectionPanel);
        tabPane.add("Build Configuration File", new JScrollPane(confPanel));
        tabPane.add("Visible Columns", columnsCheckPanel);
        tabPane.add("Session Manager", sessionManagerPanel);
        connectionPanel.setLayout(new GridBagLayout());

        label = new JLabel("Connections");
        connectionPanel.add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectionPanel.add(databaseBox, new GridBagConstraints(1, 0,
                6, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Database");
        connectionPanel.add(label, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(fileDatabaseButton, new GridBagConstraints(1, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(fileDatabaseField, new GridBagConstraints(2, 1,
                5, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Session name");
        connectionPanel.add(label, new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(sessionField, new GridBagConstraints(1, 2,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("User name");
        connectionPanel.add(label, new GridBagConstraints(0, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectionPanel.add(userField, new GridBagConstraints(1, 3,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Password");
        connectionPanel.add(label, new GridBagConstraints(3, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectionPanel.add(passwordField, new GridBagConstraints(4, 3,
                3, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Host");
        connectionPanel.add(label, new GridBagConstraints(3, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(hostField, new GridBagConstraints(4, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Port");
        connectionPanel.add(label, new GridBagConstraints(5, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(portField, new GridBagConstraints(6, 2,
                1, 1, 0.3, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Charset");
        connectionPanel.add(label, new GridBagConstraints(0, 4,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectionPanel.add(charsetCombo, new GridBagConstraints(1, 4,
                6, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(useBuildConfBox, new GridBagConstraints(0, 5,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        /*label = new JLabel("Config file");
        connectionPanel.add(label, new GridBagConstraints(1, 5,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));*/

        connectionPanel.add(fileConfButton, new GridBagConstraints(1, 5,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(fileConfField, new GridBagConstraints(2, 5,
                5, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(logToFileBox, new GridBagConstraints(0, 6,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        connectionPanel.add(fileLogButton, new GridBagConstraints(1, 6,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        connectionPanel.add(fileLogField, new GridBagConstraints(2, 6,
                5, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));


    }

    public TraceManagerPanel() {
        init();
    }

    private void initTraceManager() {
        DatabaseDriver dd = null;
        List<DatabaseDriver> dds = driverRepository().findAll();
        for (DatabaseDriver d : dds) {
            if (d.getClassName().contains("FBDriver"))
                dd = d;
            break;
        }
        Object driver = DynamicLibraryLoader.loadingObjectFromClassLoader(dd, dd.getClassName(), dd.getPath());
        traceManager = (IFBTraceManager) DynamicLibraryLoader.loadingObjectFromClassLoader(driver, "FBTraceManagerImpl");
    }

    private void timerAction() {
        lock.lock();
        String messages = sb.toString();
        sb.setLength(0);
        String s = "";
        String[] strs = messages.split("\n");
        boolean finded = false;
        if (!messages.isEmpty())
            for (int i = 0; i < strs.length; i++) {
                String str = strs[i].trim();
                if (str.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")) {
                    if (finded) {
                        parseMessage(s);
                    }
                    message = Message.LOG_MESSAGE;
                    finded = true;
                    s = str + "\n";
                } else if (str.matches("^Session ID:.*")) {
                    if (finded) {
                        parseMessage(s);
                    }
                    message = Message.SESSION_INFO;
                    finded = true;
                    s = str + "\n";
                } else {
                    s += str + "\n";
                }
            }
        if (changed)
            sb.append(s);
        else if (!s.isEmpty() && !s.toLowerCase().startsWith("trace session")) {
            s = s.trim();
            parseMessage(s);
        }
        changed = false;
        lock.unlock();
    }

    @Override
    public boolean tabViewClosing() {
        return true;
    }

    @Override
    public boolean tabViewSelected() {
        return true;
    }

    @Override
    public boolean tabViewDeselected() {
        return true;
    }

    private void parseMessage(String msg) {
        if (message == Message.LOG_MESSAGE) {
            LogMessage logMessage = new LogMessage(msg);
            idLogMessage++;
            logMessage.setId(idLogMessage);
            loggerPanel.addRow(logMessage);
        } else {
            if (sessionManagerPanel.isRefreshFlag()) {
                sessions.clear();
                sessionManagerPanel.setRefreshFlag(false);
            }
            SessionInfo sessionInfo = new SessionInfo(msg);
            sessions.add(sessionInfo);
            sessionManagerPanel.setSessions(sessions);
        }
    }

    public void clearAll() {
        loggerPanel.clearAll();
    }

    private void loadCharsets() {
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

    private DatabaseDriverRepository driverRepository() {
        return (DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID);
    }

    enum Message {
        LOG_MESSAGE,
        SESSION_INFO
    }
}
