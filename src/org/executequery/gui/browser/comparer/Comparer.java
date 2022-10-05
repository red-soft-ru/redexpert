package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;

import java.text.MessageFormat;
import java.util.*;

public class Comparer {

    protected Role role;
    protected Udf udf;
    protected Generator generator;
    protected Exception exception;
    protected Trigger trigger;
    protected Table table;
    protected View view;
    protected Index index;
    protected Constraint constraint;
    protected Domain domain;
    protected Procedure procedure;
    protected Dependencies dependencies;

    protected StatementExecutor compareConnection;
    protected StatementExecutor masterConnection;

    private String lists;
    private ArrayList<String> script;
    protected ArrayList<String> createdObjects = new ArrayList<>();
    protected ArrayList<String> alteredObjects = new ArrayList<>();
    protected ArrayList<String> droppedObjects = new ArrayList<>();

    public Comparer(DatabaseConnection dbSlave, DatabaseConnection dbMaster) {

        script = new ArrayList<>();

        compareConnection = new DefaultStatementExecutor(dbSlave, true);
        masterConnection = new DefaultStatementExecutor(dbMaster, true);

        procedure = new Procedure(this);
        domain = new Domain(this);
        dependencies = new Dependencies(this);
        constraint = new Constraint(this);
        index = new Index(this);
        view = new View(this);
        table = new Table(this);
        trigger = new Trigger(this);
        exception = new Exception(this);
        generator = new Generator(this);
        udf = new Udf(this);
        role = new Role();

        init();
    }

    void init() {
        procedure.init();
        domain.init();
        dependencies.init();
        constraint.init();
        index.init();
        view.init();
        table.init();
        trigger.init();
        exception.init();
        generator.init();
        udf.init();
    }

    public void createObjects(int type) {

        List<NamedObject> createObjects = createListObjects(type);

        if (createObjects.size() < 1)
            return;

        String header = MessageFormat.format(
                "\n/* ----- Creating {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : createObjects) {
            script.add("\n/* " + obj.getName() + " */");
            script.add("\n" + ((AbstractDatabaseObject) obj).getCreateSQL());
            lists += "\t" + obj.getName() + "\n";
        }

    }

    public void dropObjects(int type) {

        List<NamedObject> dropObjects = dropListObjects(type);

        if (dropObjects.size() < 1)
            return;

        String header = MessageFormat.format(
                "\n/* ----- Dropping {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : dropObjects) {
            script.add("\n/* " + obj.getName() + " */");
            script.add("\n" + ((AbstractDatabaseObject) obj).getDropSQL());
            lists += "\t" + obj.getName() + "\n";
        }

    }

    public void alterObjects(int type) {

        Map<NamedObject, NamedObject> alterObjects = alterListObjects(type);

        if (alterObjects.size() < 1)
            return;

        String header = MessageFormat.format(
                "\n/* ----- Altering {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : alterObjects.keySet()) {
            script.add("\n/* " + obj.getName() + " */");
            script.add("\n" + ((AbstractDatabaseObject) obj).getAlterSQL((AbstractDatabaseObject) alterObjects.get(obj)));
            lists += "\t" + obj.getName() + "\n";
        }
    }

    private List<NamedObject> createListObjects(int type) {

        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<NamedObject> createObjects = new ArrayList<>();

        for (NamedObject databaseObject : compareConnectionObjectsList) {
            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    masterConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                createObjects.add(databaseObject);
            }
        }

        return createObjects;
    }

    private List<NamedObject> dropListObjects(int type) {

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<NamedObject> dropObjects = new ArrayList<>();

        for (NamedObject databaseObject : masterConnectionObjectsList) {
            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    compareConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                dropObjects.add(databaseObject);
            }
        }

        return dropObjects;
    }

    private Map<NamedObject, NamedObject> alterListObjects(int type) {

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        Map<NamedObject, NamedObject> alterObjects = new HashMap<>();

        for (NamedObject compareObject : compareConnectionObjectsList) {
            for (NamedObject masterObject : masterConnectionObjectsList) {
                if (Objects.equals(masterObject.getName(), compareObject.getName()))
                    alterObjects.put(masterObject, compareObject);
            }
        }

        return alterObjects;
    }

    // ---

    public void computedField(boolean permission) {
        if (permission) {
            script.add("/* Filling computed fields */\n\n");

            for (ArrayList<String> cf : table.cf_fill) {
                script.add(table.fillTables(cf.get(0), cf.get(1)));
            }

            table.cf_fill.clear();
        }
    }

    public void fillProcedures(boolean permission) {
        if (permission) {
            script.add("/* Filling procedures code */\n\n");

            for (String p : procedure.procToFill) {

                script.add(procedure.fill(p));
            }

            procedure.procToFill.clear();
        }
    }

    public void fillViews(boolean permission) {
        if (permission) {
            script.add("/* Filling views code */\n\n");

            for (String v : view.v_fill) {

                script.add(view.fill(v));
            }

            view.v_fill.clear();
        }
    }

    public void retViews(boolean permission) {
        if (permission) {
            script.add("/* Creating views */\n\n");

            for (String v : view.v_create) {

                script.add(view.create(v));
            }

            view.v_create.clear();
        }
    }

    public void fillTriggers(boolean permission) {
        if (permission) {
            script.add("/* Altering triggers */\n\n");

            for (String t : trigger.triggerToFill) {

                script.add(trigger.create(t));
            }

            trigger.triggerToFill.clear();
        }
    }

    public void fillIndices(boolean permission) {
        if (permission) {
            script.add("/* Altering indices */\n\n");

            for (String i : index.indicesToFill) {

                script.add(index.create(i));
            }

            index.indicesToFill.clear();
        }
    }

    public void recreateChecks(boolean permission) {
        if (permission) {
            script.add("/* Recreating Checks */\n\n");

            for (String c : constraint.checkstoRecreate) {

                script.add(constraint.createCheck(c));
            }

            constraint.checkstoRecreate.clear();
        }
    }

    // ---

    public String getLists() {
        return lists;
    }
    public void setLists(String lists) {
        this.lists = lists;
    }

    public ArrayList<String> getScript() {
        return script;
    }
    public String getScript(int elemIndex) {
        return script.get(elemIndex);
    }
    public void addToScript(String addedScript) {
        script.add(addedScript);
    }

    public StatementExecutor getCompareConnection() {
        return compareConnection;
    }
    public StatementExecutor getMasterConnection() {
        return masterConnection;
    }

    public void clearLists() {
        createdObjects.clear();
        alteredObjects.clear();
        droppedObjects.clear();
        script.clear();
        lists = "";
    }

}

