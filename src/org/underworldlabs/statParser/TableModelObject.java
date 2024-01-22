package org.underworldlabs.statParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class TableModelObject {

    public static final int ADDED = 1;
    public static final int DELETED = -1;
    public static final int NOT_CHANGED = 0;
    protected int compared;

    protected List<String> columns;
    public String name;

    protected abstract String[][] getItems();

    abstract int getCountSkipItems();

    public List<String> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
            for (String[] item : getItems()) {
                if (item[2] == null || (!item[2].contentEquals("name") && !item[2].contentEquals("table_name")))
                    columns.add(item[0].replace(":", ""));
            }
        }
        return columns;
    }


    public String getColumnName(int column) {
        return getColumns().get(column).toLowerCase();
    }


    public Class<?> getColumnClass(int columnIndex) {
        String type = getItems()[columnIndex + getCountSkipItems()][1];
        switch (type) {
            case "p":
                return Integer.class;
            case "i":
            case "i+":
                return Long.class;
            case "s":
                return String.class;
            case "d":
                return LocalDateTime.class;
            case "f":
                return Float.class;
            case "l":
                return List.class;
            case "f+":
                return Double.class;
            default:
                return Object.class;
        }
    }


    public int getColumnCount() {
        return getColumns().size();
    }


    public Object getValueAt(int columnIndex) {
        String key = getItems()[columnIndex + getCountSkipItems()][0];
        String valtype = getItems()[columnIndex + getCountSkipItems()][1];
        String name = getItems()[columnIndex + getCountSkipItems()][2];
        if (name == null) {
            name = key.substring(0, 1).toLowerCase() + key.substring(1).replace(":", "");
            name = name.replace(" ", "_");
        }
        try {
            return getClass().getField(name).get(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCompared() {
        return compared;
    }

    public void setCompared(int compared) {
        this.compared = compared;
    }

    public abstract void calculateValues();

    @Override
    public String toString() {
        return name;
    }
}
