package org.executequery.gui.table;

public class Autoincrement {
    public boolean systemGenerator = false;
    public boolean createGenerator = false;
    public boolean useGenerator = false;
    public boolean createTrigger = false;
    public boolean createProcedure = false;
    public String generatorName = "";
    public int startValue = 0;
    public String fieldName = "";
    public String triggerName = "";
    public String procedureName = "";
    public String sqlAutoincrement = "";

    public Autoincrement() {
    }

    public boolean isAutoincrement() {
        return systemGenerator || (createGenerator || useGenerator) && (createTrigger || createProcedure);
    }

}
