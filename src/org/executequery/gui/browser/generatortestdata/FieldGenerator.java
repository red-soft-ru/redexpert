package org.executequery.gui.browser.generatortestdata;

import org.executequery.databaseobjects.DatabaseColumn;

public class FieldGenerator {
    MethodGeneratorPanel methodGeneratorPanel;
    private boolean selectedField;
    private DatabaseColumn column;

    public FieldGenerator(DatabaseColumn column) {
        this.column = column;
        methodGeneratorPanel = new MethodGeneratorPanel(column);
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

    public void setMethodGeneratorPanel(MethodGeneratorPanel methodGeneratorPanel) {
        this.methodGeneratorPanel = methodGeneratorPanel;
    }

    public Object getNextDataTestObject() {
        return getMethodGeneratorPanel().getTestDataObject();
    }
}
