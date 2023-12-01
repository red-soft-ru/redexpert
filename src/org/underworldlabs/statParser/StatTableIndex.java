package org.underworldlabs.statParser;

import java.util.List;

public abstract class StatTableIndex extends TableModelObject {

    public FillDistribution distribution;

    public FillDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(FillDistribution distribution) {
        this.distribution = distribution;
    }

    public List<String> getColumns() {
        if (columns == null) {
            columns = super.getColumns();
            columns.add("range 0-19");
            columns.add("range 20-39");
            columns.add("range 40-59");
            columns.add("range 60-79");
            columns.add("range 80-99");
        }
        return columns;
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex < getItems().length)
            return super.getColumnClass(columnIndex);
        else return Integer.class;
    }

    public Object getValueAt(int columnIndex) {
        if (columnIndex < getItems().length)
            return super.getValueAt(columnIndex);
        else switch (getColumnName(columnIndex)) {
            case "range 0-19":
                return getDistribution().range_0_19;
            case "range 20-39":
                return getDistribution().range_20_39;
            case "range 40-59":
                return getDistribution().range_40_59;
            case "range 60-79":
                return getDistribution().range_60_79;
            case "range 80-99":
                return getDistribution().range_80_99;
            default:
                return null;
        }
    }
}
