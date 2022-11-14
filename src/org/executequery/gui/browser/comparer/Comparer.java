package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.databaseobjects.impl.DefaultTemporaryDatabaseTable;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.util.SQLUtils;

import java.text.MessageFormat;
import java.util.*;

import static org.executequery.databaseobjects.NamedObject.*;

public class Comparer {

    public static boolean[] TABLE_CONSTRAINTS_NEED;
    public static boolean COMMENTS_NEED;

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

    private String constraintsList;
    private List<org.executequery.gui.browser.ColumnConstraint> constraints;

    protected ArrayList<String> createdObjects = new ArrayList<>();
    protected ArrayList<String> alteredObjects = new ArrayList<>();
    protected ArrayList<String> droppedObjects = new ArrayList<>();

    public Comparer(DatabaseConnection dbSlave, DatabaseConnection dbMaster) {

        script = new ArrayList<>();
        constraints = new ArrayList<>();

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

        TABLE_CONSTRAINTS_NEED = new boolean[]{false,false,false,false};

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
            script.add("\n" + ((AbstractDatabaseObject) obj).getCompareCreateSQL());
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

        if (Objects.equals(Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]), "ROLE"))
            return;

        String header = MessageFormat.format(
                "\n/* ----- Altering {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : alterObjects.keySet()) {
            script.add("\n/* " + obj.getName() + " */");
            script.add("\n" + ((AbstractDatabaseObject) obj).getCompareAlterSQL((AbstractDatabaseObject) alterObjects.get(obj)));
            lists += "\t" + obj.getName() + "\n";
        }
    }

    public void createConstraints() {

        if (constraints.size() < 1)
            return;

        if (TABLE_CONSTRAINTS_NEED[0]) {
            script.add("\n/* ----- PRIMARY KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.PRIMARY_KEY)
                    addConstraintToScript(obj);
        }
        if (TABLE_CONSTRAINTS_NEED[1]) {
            script.add("\n/* ----- FOREIGN KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.FOREIGN_KEY)
                    addConstraintToScript(obj);
        }
        if (TABLE_CONSTRAINTS_NEED[2]) {
            script.add("\n/* ----- UNIQUE KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.UNIQUE_KEY)
                    addConstraintToScript(obj);
        }
        if (TABLE_CONSTRAINTS_NEED[3]) {
            script.add("\n/* ----- CHECK KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.CHECK_KEY)
                    addConstraintToScript(obj);
        }
    }

    private void addConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tADD " +
                SQLUtils.generateDefinitionColumnConstraint(obj, false) + ";\n");
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

                if (!Arrays.equals(TABLE_CONSTRAINTS_NEED, new boolean[]{false, false, false, false}))
                    if (databaseObject.getType() == NamedObject.TABLE || databaseObject.getType() == NamedObject.GLOBAL_TEMPORARY)
                        createListConstraints(databaseObject);
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

    private void createListConstraints(NamedObject databaseObject) {

        if (constraintsList == null)
            constraintsList = "";

        for (ColumnConstraint cc : databaseObject.getType() == NamedObject.TABLE ?
                ((DefaultDatabaseTable) databaseObject).getConstraints() :
                ((DefaultTemporaryDatabaseTable) databaseObject).getConstraints()) {

            if ((cc.getType() == PRIMARY_KEY && !TABLE_CONSTRAINTS_NEED[0]) ||
                    (cc.getType() == FOREIGN_KEY && !TABLE_CONSTRAINTS_NEED[1]) ||
                    (cc.getType() == UNIQUE_KEY && !TABLE_CONSTRAINTS_NEED[2]) ||
                    (cc.getType() == CHECK_KEY && !TABLE_CONSTRAINTS_NEED[3]))
                continue;

            constraintsList += "\t" + databaseObject.getName() + "." + cc.getName() + "\n";
            constraints.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
        }
    }

    // ---

    public String getLists() {
        return lists;
    }

    public String getConstraintsList() {
        return constraintsList;
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

