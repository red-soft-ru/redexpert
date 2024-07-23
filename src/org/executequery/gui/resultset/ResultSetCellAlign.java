package org.executequery.gui.resultset;

import org.executequery.localization.Bundles;

public enum ResultSetCellAlign {

    right("right"),
    left("left"),
    center("center");

    private final String key;
    private final String label;

    ResultSetCellAlign(String key) {
        this.key = key;
        this.label = Bundles.get("preferences.align." + key);
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

}
