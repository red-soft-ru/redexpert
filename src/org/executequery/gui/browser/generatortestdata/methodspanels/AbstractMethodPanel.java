package org.executequery.gui.browser.generatortestdata.methodspanels;

import org.executequery.databaseobjects.DatabaseColumn;

import javax.swing.*;

public abstract class AbstractMethodPanel extends JPanel {
    protected DatabaseColumn col;
    protected boolean first = true;

    public AbstractMethodPanel(DatabaseColumn col) {
        this.col = col;
    }

    public abstract Object getTestDataObject();

    public void setFirst(boolean first) {
        this.first = first;
    }
}
