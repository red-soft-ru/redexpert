package org.underworldlabs.statParser;

import java.util.List;

public abstract class StatTableIndex extends TableModelObject {

    public long page_size;
    public String tablespaceName;
    public StatTablespace tablespace;
    public FillDistribution distribution;

    public FillDistribution getDistribution() {
        if(distribution==null)
            distribution = new FillDistribution();
        return distribution;
    }

    public void setDistribution(FillDistribution distribution) {
        this.distribution = distribution;
    }

    protected void calculateTS() {
        if (db.tablespaces != null) {
            for (StatTablespace ts : db.tablespaces) {
                if (ts.name != null && ts.name.contentEquals(tablespaceName)) {
                    tablespace = ts;
                    if (this instanceof StatTable)
                        tablespace.tables.add((StatTable) this);
                    if (this instanceof StatIndex)
                        tablespace.indices.add((StatIndex) this);
                    return;
                }
            }
        }
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
        if (columnIndex < getItems().length - getCountSkipItems())
            return super.getColumnClass(columnIndex);
        else return Integer.class;
    }

    public Object getValueAt(int columnIndex) {
        if (columnIndex < getItems().length - getCountSkipItems())
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
