package org.executequery.gui.erd;

import java.awt.*;
import java.io.Serializable;

public class ErdTextPanelData implements Serializable {
    private Rectangle tableBounds;

    /**
     * The tables background colour
     */
    private Color tableBackground;

    /**
     * The description
     */
    private String erdDescription;

    public Rectangle getTableBounds() {
        return tableBounds;
    }

    public void setTableBounds(Rectangle tableBounds) {
        this.tableBounds = tableBounds;
    }

    public Color getTableBackground() {
        return tableBackground;
    }

    public void setTableBackground(Color tableBackground) {
        this.tableBackground = tableBackground;
    }

    public String getErdDescription() {
        return erdDescription;
    }

    public void setErdDescription(String erdDescription) {
        this.erdDescription = erdDescription;
    }
}
