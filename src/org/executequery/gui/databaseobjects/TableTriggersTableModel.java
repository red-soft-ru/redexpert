package org.executequery.gui.databaseobjects;

import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.AbstractDatabaseTableViewModel;
import org.executequery.localization.Bundles;

import java.util.List;

public class TableTriggersTableModel extends AbstractDatabaseTableViewModel {
    private static final String[] header = Bundles.get(TableTriggersTableModel.class, new String[]{
            "TriggerName",
            "TriggerType",
            "Active",
            "Position",
            "Description"
    });
    /**
     * the table indexed columns
     */
    private List<DefaultDatabaseTrigger> triggers;

    /**
     * Creates a new instance of DatabaseTableColumnIndexTableModel
     */
    public TableTriggersTableModel() {
    }

    public void setTriggersData(List<DefaultDatabaseTrigger> triggers) {
        if (this.triggers == triggers) {
            return;
        }
        this.triggers = triggers;
        fireTableDataChanged();
    }

    public int getRowCount() {
        if (triggers == null) {
            return 0;
        }
        return triggers.size();
    }

    public int getColumnCount() {
        return header.length;
    }

    public String getColumnName(int col) {
        return header[col];
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

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

    public void setValueAt(Object value, int row, int col) {
    /*DefaultDatabaseIndex index = indexes.get(row);
    switch (col) {
      case 1:
        index.setName((String) value);
        break;
      case 2:
        index.addIndexedColumn((String) value);
        break;
      case 4:
        index.setNonUnique(((Boolean) value).booleanValue());
        break;
    }
    fireTableRowsUpdated(row, row);*/
    }

    public Class<?> getColumnClass(int col) {
        if (col == 2) {
            return Boolean.class;
        }
        return String.class;
    }

    public List<DefaultDatabaseTrigger> getTriggers() {
        return triggers;
    }
}