package org.executequery.gui.browser;

import biz.redsoft.IFBStatisticManager;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.base.TabView;
import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.event.ConnectionRepositoryEvent;
import org.executequery.event.DefaultConnectionRepositoryEvent;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.managment.AbstractServiceManagerPanel;
import org.executequery.gui.text.SimpleTextArea;
import org.executequery.localization.Bundles;
import org.executequery.util.UserProperties;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.DynamicLibraryLoader;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.SQLException;

public class DatabaseStatisticPanel extends AbstractServiceManagerPanel implements TabView {
    public static final String TITLE = Bundles.get(DatabaseStatisticPanel.class, "title");
    private IFBStatisticManager statisticManager;
    protected JButton fileStatButton;
    protected JTextField fileStatField;
    SimpleTextArea textPanel;
    private JButton getStatButton;

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

    @Override
    protected void initOtherComponents() {
        initStatManager(null);
        fileStatButton = WidgetFactory.createButton("fileStatButton", "...");
        fileStatField = WidgetFactory.createTextField("fileStatField");
        textPanel = new SimpleTextArea();
        fileStatButton.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileStatButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    SwingWorker sw = new SwingWorker("loadStatisticFromFile") {
                        @Override
                        public Object construct() {
                            fileStatField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                            clearAll();
                            BufferedReader reader = null;
                            try {
                                reader = new BufferedReader(
                                        new InputStreamReader(
                                                Files.newInputStream(Paths.get(fileStatField.getText())), UserProperties.getInstance().getStringProperty("system.file.encoding")));
                                readFromBufferedReader(reader, true);
                            } catch (Exception e1) {
                                GUIUtilities.displayExceptionErrorDialog("file opening error", e1);
                            } finally {
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e1) {
                                    }
                                }
                            }
                            return null;
                        }

                        @Override
                        public void finished() {
                            GUIUtilities.showNormalCursor();
                            tabPane.setEnabled(true);

                        }
                    };
                    GUIUtilities.showWaitCursor();
                    tabPane.setEnabled(false);
                    sw.start();

                }
                tabPane.setSelectedComponent(textPanel);
            }
        });
        getStatButton = WidgetFactory.createButton("getStatButton", bundleString("Start"));
        getStatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (logToFileBox.isSelected()) {
                    if (logToFileBox.isSelected()) {
                        if (!fileLogField.getText().isEmpty()) {
                            File file = new File(fileLogField.getText());
                            try {
                                fileLog = new FileOutputStream(file, true);
                            } catch (FileNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if (fileLog != null) {
                        outputStream = new ServiceOutputStream();
                    } else {
                        GUIUtilities.displayErrorMessage("File is empty");
                        return;
                    }
                } else
                    outputStream = new PipedOutputStream();
                try {
                    inputStream = new PipedInputStream(outputStream);
                    String charset = null;
                    if (charsetCombo.getSelectedIndex() > 0)
                        charset = MiscUtils.getJavaCharsetFromSqlCharset((String) charsetCombo.getSelectedItem());
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                statisticManager.setUser(userField.getText());
                statisticManager.setPassword(new String(passwordField.getPassword()));
                statisticManager.setLogger(outputStream);
                if (charsetCombo.getSelectedIndex() == 0)
                    statisticManager.setCharSet(null);
                else
                    statisticManager.setCharSet(MiscUtils.getJavaCharsetFromSqlCharset((String) charsetCombo.getSelectedItem()));
                statisticManager.setDatabase(fileDatabaseField.getText());
                statisticManager.setHost(hostField.getText());
                statisticManager.setPort(portField.getValue());
                try {
                    clearAll();
                    SwingWorker getStat = new SwingWorker("getDBStat") {
                        @Override
                        public Object construct() {
                            try {
                                statisticManager.getDatabaseStatistics();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            } finally {
                                try {
                                    statisticManager.getLogger().close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                    getStat.start();

                        /*for (int i = 0; i < connectionPanel.getComponents().length; i++) {
                            connectionPanel.getComponents()[i].setEnabled(false);
                        }
                        logToFileBox.setEnabled(false);*/
                    SwingWorker sw = new SwingWorker("TraceSession") {
                        @Override
                        public Object construct() {
                            try {
                                readFromBufferedReader(bufferedReader, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                    sw.start();
                    tabPane.setSelectedComponent(textPanel);
                    DatabaseConnection dc = (DatabaseConnection) databaseBox.getSelectedItem();
                    if (dc != null) {
                        //dc.setPathToTraceConfig(fileConfField.getText());
                    }
                    EventMediator.fireEvent(new DefaultConnectionRepositoryEvent(this,
                            ConnectionRepositoryEvent.CONNECTION_MODIFIED, (DatabaseConnection) databaseBox.getSelectedItem()
                    ));
                } catch (Exception e1) {
                    GUIUtilities.displayExceptionErrorDialog("Error get database statistics", e1);
                }
            }
        });
    }

    @Override
    protected void setEnableElements() {

    }

    @Override
    protected void changeDatabaseConnection() {
        if (databaseBox.getSelectedItem() != null) {
            DatabaseConnection dc = (DatabaseConnection) databaseBox.getSelectedItem();
            fileDatabaseField.setText(dc.getSourceName());
            userField.setText(dc.getUserName());
            passwordField.setText(dc.getUnencryptedPassword());
            hostField.setText(dc.getHost());
            portField.setText(dc.getPort());
            charsetCombo.setSelectedItem(dc.getCharset());
            initStatManager(dc);
        }
    }

    @Override
    protected void arrangeComponents() {
        JLabel label = new JLabel(bundleString("OpenFileLog"));
        add(label, gbh.setLabelDefault().get());
        gbh.nextCol();
        gbh.addLabelFieldPair(this, fileStatButton, fileStatField, null, false, false);
        add(tabPane, gbh.nextRowFirstCol().fillBoth().spanX().setMaxWeightY().get());
        gbh.fullDefaults();
        tabPane.add(bundleString("Connection"), connectionPanel);
        tabPane.add(bundleString("Text"), textPanel);
        connectionPanel.setLayout(new GridBagLayout());
        gbh.fullDefaults();
        gbh.addLabelFieldPair(connectionPanel, bundleString("Connections"), databaseBox, null, true, true);
        gbh.nextRowFirstCol();
        label = new JLabel(bundleString("Database"));
        connectionPanel.add(label, gbh.setLabelDefault().get());
        gbh.addLabelFieldPair(connectionPanel, fileDatabaseButton, fileDatabaseField, null, false, false);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Host"), hostField, null, false, false);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Port"), portField, null, false, true);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Username"), userField, null, true, false, 2);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Password"), passwordField, null, false, true);
        gbh.addLabelFieldPair(connectionPanel, bundleString("Charset"), charsetCombo, null, true, true);
        gbh.nextRowFirstCol();
        connectionPanel.add(logToFileBox, gbh.setLabelDefault().get());
        gbh.addLabelFieldPair(connectionPanel, fileLogButton, fileLogField, null, false, true);
        connectionPanel.add(parseBox, gbh.nextRowFirstCol().setLabelDefault().get());
        connectionPanel.add(getStatButton, gbh.nextCol().setLabelDefault().get());
        connectionPanel.add(new JPanel(), gbh.anchorSouth().nextRowFirstCol().fillBoth().spanX().spanY().get());
    }

    @Override
    protected void postInitActions() {

    }

    void initStatManager(DatabaseConnection dc) {
        try {
            Driver driver = loadDriver(dc);
            statisticManager = (IFBStatisticManager) DynamicLibraryLoader.loadingObjectFromClassLoader(
                    driver.getMajorVersion(), driver, "FBStatisticManagerImpl");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFromBufferedReader(BufferedReader reader, boolean fromFile) {
        String line;
        while (true) {
            try {
                if ((line = reader.readLine()) == null)
                    break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            textPanel.getTextAreaComponent().append(line + "\n");
        }
    }

    void clearAll() {
        textPanel.getTextAreaComponent().setText("");
    }
}
