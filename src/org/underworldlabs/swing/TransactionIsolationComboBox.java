package org.underworldlabs.swing;

import org.executequery.Constants;
import org.executequery.gui.browser.AbstractConnectionPanel;
import org.executequery.localization.Bundles;

import javax.swing.*;
import java.sql.Connection;

/**
 * @author Alexey Kozlov
 */
public class TransactionIsolationComboBox extends JComboBox<String> {

    public TransactionIsolationComboBox() {

        String[] isolationLevels = new String[4];
        isolationLevels[0] = Bundles.get(AbstractConnectionPanel.class, "DatabaseDefault");
        System.arraycopy(Constants.TRANSACTION_LEVELS, 2, isolationLevels, 1, isolationLevels.length - 1);

        setModel(new DynamicComboBoxModel<>(isolationLevels));
    }

    public int getSelectedLevel() {
        switch (getSelectedIndex()) {
            case 1:
                return Connection.TRANSACTION_READ_COMMITTED;
            case 2:
                return Connection.TRANSACTION_REPEATABLE_READ;
            case 3:
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return -1;
        }
    }

    public void setSelectedLevel(int isolationLevel) {
        switch (isolationLevel) {

            case Connection.TRANSACTION_READ_COMMITTED:
                setSelectedIndex(1);
                break;

            case Connection.TRANSACTION_REPEATABLE_READ:
                setSelectedIndex(2);
                break;

            case Connection.TRANSACTION_SERIALIZABLE:
                setSelectedIndex(3);
                break;

            default:
                setSelectedIndex(0);
        }
    }

}
