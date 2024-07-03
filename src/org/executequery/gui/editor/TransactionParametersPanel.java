package org.executequery.gui.editor;

import biz.redsoft.ITPB;
import biz.redsoft.ITPBConstants;
import org.executequery.Constants;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.BaseDialog;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.DynamicLibraryLoader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.sql.Connection;

public class TransactionParametersPanel extends JPanel {

    private ITPB tpb;

    private DatabaseConnection databaseConnection;
    private TransactionTablesTable transactionTablesTable;

    // --- GUI components ---

    private JCheckBox waitCheck;
    private JCheckBox readOnlyCheck;
    private JCheckBox reservingCheck;
    private JCheckBox ignoreLimboCheck;
    private JCheckBox recordVersionCheck;
    private JCheckBox useTransactionSnapshotCheck;

    private JLabel curTraLabel;

    private JCheckBox noAutoUndoCheck;
    private NumberTextField lockTimeOutField;
    private NumberTextField transactionSnapshotField;
    private TransactionIsolationComboBox isolationCombo;

    // ---

    public TransactionParametersPanel(DatabaseConnection databaseConnection) {
        setDatabaseConnection(databaseConnection);

        init();
        arrange();
        checkEnabled();
    }

    private void init() {

        readOnlyCheck = WidgetFactory.createCheckBox("readOnlyCheck", "READ ONLY");
        noAutoUndoCheck = WidgetFactory.createCheckBox("noAutoUndoCheck", "NO AUTO UNDO");
        ignoreLimboCheck = WidgetFactory.createCheckBox("ignoreLimboCheck", "IGNORE LIMBO");
        recordVersionCheck = WidgetFactory.createCheckBox("recordVersionCheck", "RECORD VERSION");

        waitCheck = WidgetFactory.createCheckBox("waitCheck", "WAIT", true);
        waitCheck.addChangeListener(e -> checkEnabled());

        reservingCheck = WidgetFactory.createCheckBox("reservingCheck", "RESERVING");
        reservingCheck.addActionListener(e -> showReservingDialog());

        useTransactionSnapshotCheck = WidgetFactory.createCheckBox("useTransactionSnapshotCheck", "SET TRANSACTION SNAPSHOT");
        useTransactionSnapshotCheck.addActionListener(e -> checkEnabled());

        lockTimeOutField = WidgetFactory.createNumberTextField("lockTimeOutField");
        transactionSnapshotField = WidgetFactory.createNumberTextField("transactionSnapshotField");

        isolationCombo = new TransactionIsolationComboBox();
        isolationCombo.addItemListener(e -> checkEnabled());
        isolationCombo.setName("isolationCombo");

        curTraLabel = new JLabel("");
    }

    private void arrange() {
        GridBagHelper gbh;

        // ---  top panel ---

        JPanel topPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().rightGap(5);
        topPanel.add(readOnlyCheck, gbh.get());
        topPanel.add(noAutoUndoCheck, gbh.nextCol().get());
        topPanel.add(ignoreLimboCheck, gbh.nextCol().get());
        topPanel.add(reservingCheck, gbh.nextCol().get());
        topPanel.add(recordVersionCheck, gbh.nextCol().get());
        topPanel.add(waitCheck, gbh.nextCol().spanX().get());

        // --- bottom panel ---

        JPanel bottomPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().leftGap(5).rightGap(5);
        bottomPanel.add(new JLabel(Bundles.get("ConnectionPanel.IsolationLevel")), gbh.topGap(3).get());
        bottomPanel.add(isolationCombo, gbh.nextCol().leftGap(0).topGap(0).get());
        bottomPanel.add(new JLabel(bundleString("lockTimeout")), gbh.nextCol().topGap(3).get());
        bottomPanel.add(lockTimeOutField, gbh.nextCol().topGap(0).spanX().get());\
        //todo add "Transaction snapshot elements"

        // --- main panel ---

        JPanel mainPanel = new JPanel(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().fillHorizontally().spanX();
        mainPanel.add(topPanel, gbh.get());
        mainPanel.add(bottomPanel, gbh.nextRow().topGap(5).get());

        // --- base ---

        setLayout(new GridBagLayout());

        gbh = new GridBagHelper().anchorNorthWest().spanX();
        add(mainPanel, gbh.leftGap(5).get());
    }

    private void checkEnabled() {
        recordVersionCheck.setEnabled(isolationCombo.getSelectedLevel() == Connection.TRANSACTION_READ_COMMITTED);
        if (!recordVersionCheck.isEnabled())
            recordVersionCheck.setSelected(false);

        lockTimeOutField.setEnabled(waitCheck.isSelected());
        transactionSnapshotField.setEnabled(useTransactionSnapshotCheck.isSelected());
    }

    public void setCurrentTransaction(long currentTransaction, long snapshotTransaction) {
        curTraLabel.setText("Current transaction id:" + currentTransaction + ", current snapshot transaction:" + snapshotTransaction);
    }

    public final ITPB getTpb(DatabaseConnection databaseConnection) {

        if (databaseConnection != null && databaseConnection.isConnected()) {
            try {
                tpb = (ITPB) DynamicLibraryLoader.loadingObjectFromClassLoaderWithCS(
                        databaseConnection.getDriverMajorVersion(),
                        ConnectionManager.getClassLoaderForDatabaseConnection(databaseConnection),
                        "ITPBImpl"
                );

            } catch (Exception e) {
                Log.error(e.getMessage(), e);
            }
        }

        if (tpb == null)
            return null;

        // --- setup transaction params ---

        tpb.initTPB();

        tpb.addArgument(readOnlyCheck.isSelected() ?
                ITPBConstants.isc_tpb_read :
                ITPBConstants.isc_tpb_write
        );

        if (waitCheck.isSelected()) {
            tpb.addArgument(ITPBConstants.isc_tpb_wait);
            if (lockTimeOutField.getValue() > 0)
                tpb.addArgument(ITPBConstants.isc_tpb_lock_timeout, lockTimeOutField.getValue());

        } else
            tpb.addArgument(ITPBConstants.isc_tpb_nowait);

        if (getTransactionIsolation() == Connection.TRANSACTION_REPEATABLE_READ)
            tpb.addArgument(ITPBConstants.isc_tpb_concurrency);

        if (getTransactionIsolation() == Connection.TRANSACTION_SERIALIZABLE)
            tpb.addArgument(ITPBConstants.isc_tpb_consistency);

        if (getTransactionIsolation() == Connection.TRANSACTION_READ_COMMITTED) {
            tpb.addArgument(ITPBConstants.isc_tpb_read_committed);
            tpb.addArgument(recordVersionCheck.isSelected() ?
                    ITPBConstants.isc_tpb_rec_version :
                    ITPBConstants.isc_tpb_no_rec_version
            );
        }

        if (noAutoUndoCheck.isSelected())
            tpb.addArgument(ITPBConstants.isc_tpb_no_auto_undo);

        if (ignoreLimboCheck.isSelected())
            tpb.addArgument(ITPBConstants.isc_tpb_ignore_limbo);

        if (useTransactionSnapshotCheck.isSelected() && transactionSnapshotField.getLongValue() > 0)
            tpb.addArgument(ITPBConstants.isc_tpb_at_snapshot_number, transactionSnapshotField.getLongValue());

        if (reservingCheck.isSelected()) {
            for (TransactionTablesTable.ReservingTable reservingTable : transactionTablesTable.getReservingTables()) {

                tpb.addArgument(reservingTable.isReadTable() ?
                                ITPBConstants.isc_tpb_lock_read :
                                ITPBConstants.isc_tpb_lock_write,
                        reservingTable.getName()
                );

                tpb.addArgument(reservingTable.isSharedTable() ?
                        ITPBConstants.isc_tpb_shared :
                        ITPBConstants.isc_tpb_protected
                );
            }
        }

        return tpb;
    }

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
        if (databaseConnection != null) {
            transactionTablesTable = new TransactionTablesTable(ConnectionsTreePanel
                    .getPanelFromBrowser()
                    .getDefaultDatabaseHostFromConnection(databaseConnection)
                    .getTables()
            );
        }
    }

    private void showReservingDialog() {

        if (!reservingCheck.isSelected())
            return;

        if (databaseConnection == null) {
            reservingCheck.setSelected(false);
            return;
        }

        BaseDialog dialog = new BaseDialog(Constants.EMPTY, true);
        dialog.setContentPane(transactionTablesTable);
        transactionTablesTable.setDialog(dialog);
        transactionTablesTable.display();
        dialog.display();

        if (!transactionTablesTable.isSuccess())
            reservingCheck.setSelected(false);
    }

    private int getTransactionIsolation() {
        return isolationCombo.getSelectedLevel();
    }

    private static String bundleString(String key, Object... args) {
        return Bundles.get(TransactionParametersPanel.class, key, args);
    }

}
