package org.executequery.gui.editor;

import biz.redsoft.ITPB;
import biz.redsoft.ITPBConstants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;

public class TransactionParametersPanel extends JPanel {
    ITPB tpb;
    private JCheckBox readOnlyBox;
    private JCheckBox waitCheckBox;

    private JLabel lockTimeOutLabel;
    private NumberTextField lockTimeOutField;
    private JCheckBox noAutoUndoCheckBox;

    private JLabel levelLabel;
    private TransactionIsolationCombobox levelCombobox;
    private JCheckBox recordVersionBox;
    private JCheckBox ignoreLimboCheckBox;
    private JCheckBox reservingCheckBox;
    private TransactionTablesTable transactionTablesTable;
    private DatabaseConnection databaseConnection;

    public TransactionParametersPanel(DatabaseConnection databaseConnection) {
        setDatabaseConnection(databaseConnection);
        init();
    }

    private void init() {
        readOnlyBox = new JCheckBox("READ ONLY");
        waitCheckBox = new JCheckBox("WAIT");
        waitCheckBox.setSelected(true);
        waitCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        lockTimeOutField = new NumberTextField();
        levelCombobox = new TransactionIsolationCombobox();
        levelCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                checkEnabled();
            }
        });
        recordVersionBox = new JCheckBox("RECORD VERSION");
        noAutoUndoCheckBox = new JCheckBox("NO AUTO UNDO");
        ignoreLimboCheckBox = new JCheckBox("IGNORE LIMBO");
        reservingCheckBox = new JCheckBox("RESERVING");
        //transactionTablesTable = new TransactionTablesTable(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getTables());
        reservingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (reservingCheckBox.isSelected()) {
                    BaseDialog dialog = new BaseDialog("", true);
                    dialog.setContentPane(transactionTablesTable);
                    transactionTablesTable.setDialog(dialog);
                    transactionTablesTable.display();
                    dialog.display();
                    if (!transactionTablesTable.isSuccess())
                        reservingCheckBox.setSelected(false);
                }
            }
        });


        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaultsStatic().defaults();

        add(readOnlyBox, gbh.setLabelDefault().get());
        add(noAutoUndoCheckBox, gbh.setLabelDefault().nextCol().get());
        add(ignoreLimboCheckBox, gbh.setLabelDefault().nextCol().get());
        levelLabel = new JLabel(Bundles.get("ConnectionPanel.IsolationLevel"));
        add(levelLabel, gbh.setLabelDefault().nextCol().get());
        add(levelCombobox, gbh.setLabelDefault().nextCol().get());
        add(recordVersionBox, gbh.nextCol().spanX().get());
        add(waitCheckBox, gbh.setLabelDefault().nextRowFirstCol().get());
        lockTimeOutLabel = new JLabel(bundleString("lockTimeout"));
        add(lockTimeOutLabel, gbh.nextCol().setLabelDefault().anchorNorthWest().get());
        add(lockTimeOutField, gbh.fillHorizontally().setWeightX(0.2).nextCol().get());
        add(reservingCheckBox, gbh.setLabelDefault().nextCol().spanX().get());
        checkEnabled();

    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }



    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        transactionTablesTable = new TransactionTablesTable(ConnectionsTreePanel.getPanelFromBrowser().getDefaultDatabaseHostFromConnection(databaseConnection).getTables());
    }

    private void checkEnabled() {
        recordVersionBox.setEnabled(levelCombobox.getSelectedLevel() == Connection.TRANSACTION_READ_COMMITTED);
        lockTimeOutLabel.setEnabled(waitCheckBox.isSelected());
        lockTimeOutField.setEnabled(waitCheckBox.isSelected());

    }

    private int getTransactionIsolation() {
        return levelCombobox.getSelectedLevel();
    }

    public ITPB getTpb(DatabaseConnection databaseConnection) {
        if (databaseConnection != null && databaseConnection.isConnected()) {
            try {
                tpb = (ITPB) DynamicLibraryLoader.loadingObjectFromClassLoaderWithCS(databaseConnection.getDriverMajorVersion(),ConnectionManager.getClassLoaderForDatabaseConnection(databaseConnection), "ITPBImpl");
                tpb.initTPB();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (tpb != null) {
            tpb.initTPB();
            if (readOnlyBox.isSelected())
                tpb.addArgument(ITPBConstants.isc_tpb_read);
            else {
                tpb.addArgument(ITPBConstants.isc_tpb_write);
            }
            if (waitCheckBox.isSelected()) {
                tpb.addArgument(ITPBConstants.isc_tpb_wait);
                if (lockTimeOutField.getValue() > 0)
                    tpb.addArgument(ITPBConstants.isc_tpb_lock_timeout, lockTimeOutField.getValue());
            } else tpb.addArgument(ITPBConstants.isc_tpb_nowait);
            if (getTransactionIsolation() == Connection.TRANSACTION_REPEATABLE_READ) {
                tpb.addArgument(ITPBConstants.isc_tpb_concurrency);
            }
            if (getTransactionIsolation() == Connection.TRANSACTION_SERIALIZABLE) {
                tpb.addArgument(ITPBConstants.isc_tpb_consistency);
            }
            if (getTransactionIsolation() == Connection.TRANSACTION_READ_COMMITTED) {
                tpb.addArgument(ITPBConstants.isc_tpb_read_committed);
                if (recordVersionBox.isSelected())
                    tpb.addArgument(ITPBConstants.isc_tpb_rec_version);
                else tpb.addArgument(ITPBConstants.isc_tpb_no_rec_version);

            }
            if (noAutoUndoCheckBox.isSelected())
                tpb.addArgument(ITPBConstants.isc_tpb_no_auto_undo);
            if (ignoreLimboCheckBox.isSelected())
                tpb.addArgument(ITPBConstants.isc_tpb_ignore_limbo);
            if (reservingCheckBox.isSelected()) {
                for (TransactionTablesTable.ReservingTable reservingTable : transactionTablesTable.getReservingTables()) {
                    if (reservingTable.isReadTable())
                        tpb.addArgument(ITPBConstants.isc_tpb_lock_read, reservingTable.getName());
                    else tpb.addArgument(ITPBConstants.isc_tpb_lock_write, reservingTable.getName());
                    if (reservingTable.isSharedTable())
                        tpb.addArgument(ITPBConstants.isc_tpb_shared);
                    else tpb.addArgument(ITPBConstants.isc_tpb_protected);
                }
            }
        }
        return tpb;
    }

    private String bundleString(String key, Object... args) {
        return Bundles.get(TransactionParametersPanel.class, key, args);
    }
}
