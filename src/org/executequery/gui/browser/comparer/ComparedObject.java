package org.executequery.gui.browser.comparer;

public class ComparedObject {

    private final int type;
    private final String name;
    private final String sourceObjectScript;
    private final String targetObjectScript;

    public ComparedObject(int type, String name, String sourceObjectScript, String targetObjectScript) {
        this.type = type;
        this.name = name;
        this.sourceObjectScript = sourceObjectScript;
        this.targetObjectScript = targetObjectScript;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSourceObjectScript() {
        return sourceObjectScript;
    }

    public String getTargetObjectScript() {
        return targetObjectScript;
    }

}
