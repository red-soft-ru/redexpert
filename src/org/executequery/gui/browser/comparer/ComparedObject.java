package org.executequery.gui.browser.comparer;

import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;

public class ComparedObject {

    private final int type;
    private final String name;
    private final String plugin;
    private final String sourceObjectScript;
    private final String targetObjectScript;

    public ComparedObject(int type, NamedObject object, String sourceObjectScript, String targetObjectScript) {
        this.type = type;
        this.name = object.getName();
        this.plugin = object instanceof DefaultDatabaseUser ? ((DefaultDatabaseUser) object).getPlugin() : null;
        this.sourceObjectScript = sourceObjectScript;
        this.targetObjectScript = targetObjectScript;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getSourceObjectScript() {
        return sourceObjectScript;
    }

    public String getTargetObjectScript() {
        return targetObjectScript;
    }

}
