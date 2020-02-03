package org.executequery.gui.table;

import java.io.Serializable;

public class Autoincrement implements Serializable {
    private boolean systemGenerator = false;
    private boolean createGenerator = false;
    private boolean useGenerator = false;
    private boolean createTrigger = false;
    private boolean createProcedure = false;
    private boolean identity = false;
    private String generatorName = "";
    private int startValue = 0;
    private String fieldName = "";
    private String triggerName = "";
    private String procedureName = "";
    private String sqlAutoincrement = "";

    public Autoincrement() {
    }

    public boolean isAutoincrement() {
        return systemGenerator || (createGenerator || useGenerator) && (createTrigger || createProcedure) || createTrigger || createProcedure || identity;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isCreateGenerator() {
        return createGenerator;
    }

    public boolean isCreateTrigger() {
        return createTrigger;
    }

    public boolean isCreateProcedure() {
        return createProcedure;
    }

    public boolean isSystemGenerator() {
        return systemGenerator;
    }

    public boolean isUseGenerator() {
        return useGenerator;
    }

    public int getStartValue() {
        return startValue;
    }

    public String getGeneratorName() {
        return generatorName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public String getSqlAutoincrement() {
        return sqlAutoincrement;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setCreateGenerator(boolean createGenerator) {
        this.createGenerator = createGenerator;
    }

    public void setCreateProcedure(boolean createProcedure) {
        this.createProcedure = createProcedure;
    }

    public void setCreateTrigger(boolean createTrigger) {
        this.createTrigger = createTrigger;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    public void setSqlAutoincrement(String sqlAutoincrement) {
        this.sqlAutoincrement = sqlAutoincrement;
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
    }

    public void setSystemGenerator(boolean systemGenerator) {
        this.systemGenerator = systemGenerator;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public void setUseGenerator(boolean useGenerator) {
        this.useGenerator = useGenerator;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }
}
