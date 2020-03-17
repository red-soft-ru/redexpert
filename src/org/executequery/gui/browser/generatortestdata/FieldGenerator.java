package org.executequery.gui.browser.generatortestdata;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;

public class FieldGenerator {
    MethodGeneratorPanel methodGeneratorPanel;
    private boolean selectedField;
    private DatabaseColumn column;

    public FieldGenerator(DatabaseColumn column, DefaultStatementExecutor executor) {
        this.column = column;
        methodGeneratorPanel = new MethodGeneratorPanel(column, executor);
    }

    public boolean isSelectedField() {
        return selectedField;
    }

    public void setSelectedField(boolean selectedField) {
        this.selectedField = selectedField;
    }

    public DatabaseColumn getColumn() {
        return column;
    }

    public void setColumn(DatabaseColumn column) {
        this.column = column;
    }

    public MethodGeneratorPanel getMethodGeneratorPanel() {
        return methodGeneratorPanel;
    }

    public void setFirst() {
        methodGeneratorPanel.setFirst();
    }

    public void setMethodGeneratorPanel(MethodGeneratorPanel methodGeneratorPanel) {
        this.methodGeneratorPanel = methodGeneratorPanel;
    }

    public Object getNextDataTestObject() {
        return getMethodGeneratorPanel().getTestDataObject();
    }
}
