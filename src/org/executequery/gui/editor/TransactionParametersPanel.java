package org.executequery.gui.editor;

import biz.redsoft.IFBDatabaseConnection;
import biz.redsoft.ITPB;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;

public class TransactionParametersPanel extends JPanel {
    ITPB tpb;
    private JCheckBox readOnlyBox;
    private JCheckBox waitCheckBox;
    private NumberTextField lockTimeOutField;
    private JCheckBox noAutoUndoCheckBox;
    private TransactionIsolationCombobox levelCombobox;
    private JCheckBox recordVersionBox;
    private JCheckBox ignoreLimboCheckBox;
    private JCheckBox reservingCheckBox;
    private TransactionTablesTable transactionTablesTable;
    private DatabaseConnection databaseConnection;

    public TransactionParametersPanel(DatabaseConnection databaseConnection) {
        setDatabaseConnection(databaseConnection);
    }

    private void init() {
        readOnlyBox = new JCheckBox("READ ONLY");
        waitCheckBox = new JCheckBox("WAIT");
        waitCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkVisible();
            }
        });
        lockTimeOutField = new NumberTextField();
        levelCombobox = new TransactionIsolationCombobox();
        levelCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkVisible();
            }
        });
        recordVersionBox = new JCheckBox("RECORD VERSION");
        noAutoUndoCheckBox = new JCheckBox("NO AUTO UNDO");
        ignoreLimboCheckBox = new JCheckBox("IGNORE LIMBO");
        reservingCheckBox = new JCheckBox("RESERVING");
        transactionTablesTable = new TransactionTablesTable();

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        add(readOnlyBox, gbh.setLabelDefault().get());
        add(waitCheckBox, gbh.setLabelDefault().nextCol().get());
        add(lockTimeOutField, gbh.fillHorizontally().setWeightX(0.2).nextCol().get());
        add(levelCombobox, gbh.setLabelDefault().nextCol().get());
        add(recordVersionBox, gbh.setLabelDefault().nextCol().get());
        add(noAutoUndoCheckBox, gbh.setLabelDefault().nextCol().get());
        add(ignoreLimboCheckBox, gbh.setLabelDefault().nextCol().get());
        add(reservingCheckBox, gbh.setLabelDefault().nextCol().get());
        checkVisible();

    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    private void changeDatabaseConnection() {
        if (databaseConnection != null && databaseConnection.isConnected()) {
            Connection con = ConnectionManager.getTemporaryConnection(databaseConnection);
            try {
                if (con.unwrap(Connection.class).getClass().getName().contains("FBConnection")) {
                    Connection fbConn = con.unwrap(Connection.class);
                    IFBDatabaseConnection db = null;
                    tpb = (ITPB) DynamicLibraryLoader.loadingObjectFromClassLoader(fbConn, "ITPBImpl");
                    tpb.initTPB();
                }
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            init();
        }
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        changeDatabaseConnection();
    }

    private void checkVisible() {
        recordVersionBox.setVisible(levelCombobox.getSelectedLevel() == Connection.TRANSACTION_READ_COMMITTED);
        lockTimeOutField.setVisible(waitCheckBox.isSelected());

    }
}
