package org.underworldlabs.statParser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class TableModelObject {

    protected List<String> columns;

    protected abstract String[][] getItems();

    public List<String> getColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
            for (String[] item : getItems()) {
                columns.add(item[0].replace(":", ""));
            }
        }
        return columns;
    }


    public String getColumnName(int column) {
        return getColumns().get(column);
    }


    public Class<?> getColumnClass(int columnIndex) {
        String type = getItems()[columnIndex][1];
        switch (type) {
            case "i":
            case "p":
                return Integer.class;
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
            default:
                return Object.class;
        }
    }


    public int getColumnCount() {
        return getColumns().size();
    }


    public Object getValueAt(int columnIndex) {
        String key = getItems()[columnIndex][0];
        String valtype = getItems()[columnIndex][1];
        String name = getItems()[columnIndex][2];
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
}
