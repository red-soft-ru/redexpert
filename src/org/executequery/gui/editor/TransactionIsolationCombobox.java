package org.executequery.gui.editor;

import org.executequery.gui.browser.ConnectionPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;

import javax.swing.*;
import java.sql.Connection;

public class TransactionIsolationCombobox extends JComboBox {

    DynamicComboBoxModel model;

    public TransactionIsolationCombobox() {
        model = new DynamicComboBoxModel();
        String[] txLevels = new String[org.executequery.Constants.TRANSACTION_LEVELS.length + 1];
        txLevels[0] = Bundles.get(ConnectionPanel.class, "DatabaseDefault");
        for (int i = 1; i < txLevels.length; i++) {
            txLevels[i] = org.executequery.Constants.TRANSACTION_LEVELS[i - 1];
        }
        model.setElements(txLevels);
        setModel(model);
    }

    public int getSelectedLevel() {
        int index = getSelectedIndex();
        int isolationLevel = -1;
        switch (index) {
            case 1:
                isolationLevel = Connection.TRANSACTION_NONE;
                break;
            case 2:
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
                break;
            case 3:
                isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
                break;
            case 4:
                isolationLevel = Connection.TRANSACTION_REPEATABLE_READ;
                break;
            case 5:
                isolationLevel = Connection.TRANSACTION_SERIALIZABLE;
                break;
        }
        return isolationLevel;
    }

    public void setSelectedLevel(int isolationLevel) {
        int index = 0;
        switch (isolationLevel) {
            case Connection.TRANSACTION_NONE:
                index = 1;
                break;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                index = 2;
                break;
            case Connection.TRANSACTION_READ_COMMITTED:
                index = 3;
                break;
            case Connection.TRANSACTION_REPEATABLE_READ:
                index = 4;
                break;
            case Connection.TRANSACTION_SERIALIZABLE:
                index = 5;
                break;
        }
        setSelectedIndex(index);
    }
}
