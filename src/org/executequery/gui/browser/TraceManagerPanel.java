package org.executequery.gui.browser;

import biz.redsoft.IFBTraceManager;
import org.executequery.base.TabView;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseDriver;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.browser.managment.tracemanager.TablePanel;
import org.executequery.gui.browser.managment.tracemanager.net.LogMessage;
import org.executequery.repository.DatabaseDriverRepository;
import org.executequery.repository.RepositoryCache;
import org.underworldlabs.swing.NumberTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TraceManagerPanel extends JPanel implements TabView {

    public static final String TITLE = "Trace Manager";
    IFBTraceManager traceManager;
    TablePanel loggerPanel;
    private Timer timer;
    private OutputStream fileLog;
    private OutputStream outputStream;
    private Lock lock;
    private StringBuilder sb;
    private JButton fileLogButton;
    private JButton fileDatabaseButton;
    private JButton fileConfButton;
    private JButton startStopSessionButton;
    private JTextField fileLogField;
    private JTextField fileDatabaseField;
    private JTextField fileConfField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JCheckBox logToFileBox;
    private JTextField hostField;
    private NumberTextField portField;
    private JTextField sessionField;
    private int idLogMessage = 0;
    private boolean changed = false;

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
        Object driver = ConnectionManager.loadingObjectFromClassLoader(dd, dd.getClassName(), dd.getPath());
        traceManager = (IFBTraceManager) ConnectionManager.loadingObjectFromClassLoader(driver, "FBTraceManagerImpl");
    }

    private void init() {
        initTraceManager();
        loggerPanel = new TablePanel();
        lock = new ReentrantLock();
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerAction();
            }
        });
        sb = new StringBuilder();
        fileLogButton = new JButton("...");
        fileDatabaseButton = new JButton("...");
        fileConfButton = new JButton("...");
        fileLogField = new JTextField();
        fileDatabaseField = new JTextField();
        fileDatabaseField.setText("D:\\databases\\EMPLOYEE.FDB");
        fileConfField = new JTextField();
        fileConfField.setText("d:\\fbtrace_dba.conf");
        userField = new JTextField();
        userField.setText("SYSDBA");
        passwordField = new JPasswordField();
        passwordField.setText("masterkey");
        logToFileBox = new JCheckBox("Log to file");
        hostField = new JTextField("127.0.0.1");
        portField = new NumberTextField();
        portField.setValue(3050);
        sessionField = new JTextField();
        sessionField.setText("Session");
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
                        fileLog = new FileOutputStream(file, true);
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
                int returnVal = fileChooser.showSaveDialog(fileDatabaseButton);
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
                int returnVal = fileChooser.showSaveDialog(fileConfButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileConfField.setText(file.getAbsolutePath());
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
                    traceManager.setCharSet("UTF8");
                    traceManager.setDatabase(fileDatabaseField.getText());
                    traceManager.setHost(hostField.getText());
                    traceManager.setPort(portField.getValue());
                    timer.start();
                    try {
                        traceManager.startTraceSession("session", traceManager.loadConfigurationFromFile(fileConfField.getText()));
                        startStopSessionButton.setText("Stop");
                        logToFileBox.setEnabled(false);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else try {
                    traceManager.stopTraceSession(traceManager.getSessionID("session"));
                    startStopSessionButton.setText("Start");
                    logToFileBox.setEnabled(true);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setLayout(new GridBagLayout());

        add(logToFileBox, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(fileLogButton, new GridBagConstraints(1, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(fileLogField, new GridBagConstraints(2, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        JLabel label = new JLabel("User name");
        add(label, new GridBagConstraints(0, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(userField, new GridBagConstraints(1, 1,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Password");
        add(label, new GridBagConstraints(0, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        add(passwordField, new GridBagConstraints(1, 2,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        add(startStopSessionButton, new GridBagConstraints(0, 4,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(loggerPanel, new GridBagConstraints(0, 5,
                7, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Database");
        add(label, new GridBagConstraints(3, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(fileDatabaseButton, new GridBagConstraints(4, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(fileDatabaseField, new GridBagConstraints(5, 0,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Config file");
        add(label, new GridBagConstraints(3, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(fileConfButton, new GridBagConstraints(4, 1,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(fileConfField, new GridBagConstraints(5, 1,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Port");
        add(label, new GridBagConstraints(3, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(portField, new GridBagConstraints(4, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Host");
        add(label, new GridBagConstraints(5, 2,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(hostField, new GridBagConstraints(6, 2,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));

        label = new JLabel("Session name");
        add(label, new GridBagConstraints(0, 3,
                1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));

        add(sessionField, new GridBagConstraints(1, 3,
                2, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));


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
        if (changed)
            sb.append(s);
        else if (!s.isEmpty() && !s.toLowerCase().startsWith("trace session")) {
            s = s.trim();
            LogMessage logMessage = new LogMessage(s);
            idLogMessage++;
            logMessage.setId(idLogMessage);
            loggerPanel.addRow(logMessage);
        }
        changed = false;
        lock.unlock();
    }

    private DatabaseDriverRepository driverRepository() {
        return (DatabaseDriverRepository) RepositoryCache.load(
                DatabaseDriverRepository.REPOSITORY_ID);
    }
}
