package org.executequery.gui.databaseobjects;

import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.AbstractDatabaseTableViewModel;
import org.executequery.localization.Bundles;

import java.util.List;

public class TableTriggersTableModel extends AbstractDatabaseTableViewModel {

    private static final String[] header = bundleString(
            "TriggerName",
            "TriggerType",
            "Active",
            "Position",
            "Description"
    );

    private List<DefaultDatabaseTrigger> triggers;

    public void setTriggersData(List<DefaultDatabaseTrigger> triggers) {
        this.triggers = triggers;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return triggers != null ? triggers.size() : 0;
    }

    @Override
    public int getColumnCount() {
        return header.length;
    }

    @Override
    public String getColumnName(int col) {
        return header[col];
    }

    @Override
    public Object getValueAt(int row, int col) {

        DefaultDatabaseTrigger trigger = triggers.get(row);
        switch (col) {
            case 0:
                return trigger.getName();
            case 1:
                return trigger.getStringTriggerType();
            case 2:
                return trigger.isTriggerActive();
            case 3:
                return trigger.getTriggerSequence();
            case 4:
                return trigger.getRemarks();
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return col == 2 ? Boolean.class : String.class;
    }

    public List<DefaultDatabaseTrigger> getTriggers() {
        return triggers;
    }

    private static String[] bundleString(String... keys) {
        return Bundles.get(TableTriggersTableModel.class, keys);
    }

}