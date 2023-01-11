package org.executequery.gui.browser.comparer;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.databaseobjects.impl.DefaultDatabaseTrigger;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.executequery.databaseobjects.NamedObject.*;

public class Comparer {

    /**
     * [0] - PK; [1] - FK; [2] - UK; [3] - CK
     */
    private static boolean[] TABLE_CONSTRAINTS_NEED;
    private static boolean COMMENTS_NEED;
    private static boolean COMPUTED_FIELDS_NEED;

    protected StatementExecutor compareConnection;
    protected StatementExecutor masterConnection;

    private String lists;
    private String constraintsList;
    private String computedFieldsList;

    private final ArrayList<String> script;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraints;
    private final List<ColumnData> computedFields;


    protected ArrayList<String> createdObjects = new ArrayList<>();
    protected ArrayList<String> alteredObjects = new ArrayList<>();
    protected ArrayList<String> droppedObjects = new ArrayList<>();

    public Comparer(DatabaseConnection dbSlave, DatabaseConnection dbMaster,
                    boolean[] constraintsNeed, boolean commentsNeed, boolean computedNeed) {

        script = new ArrayList<>();
        constraints = new ArrayList<>();
        computedFields = new ArrayList<>();

        compareConnection = new DefaultStatementExecutor(dbSlave, true);
        masterConnection = new DefaultStatementExecutor(dbMaster, true);

        Comparer.TABLE_CONSTRAINTS_NEED = constraintsNeed;
        Comparer.COMMENTS_NEED = commentsNeed;
        Comparer.COMPUTED_FIELDS_NEED = computedNeed;

    }

    public void createObjects(int type) {

        List<NamedObject> createObjects = sortObjectsByDependency(createListObjects(type));

        if (createObjects == null)
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

        List<NamedObject> dropObjects = sortObjectsByDependency(dropListObjects(type));

        if (dropObjects == null)
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
                if (obj.getType() == NamedObject.PRIMARY_KEY) {
                    addConstraintToScript(obj);
                }
        }
        if (TABLE_CONSTRAINTS_NEED[2]) {
            script.add("\n/* ----- UNIQUE KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.UNIQUE_KEY)
                    addConstraintToScript(obj);
        }
        if (TABLE_CONSTRAINTS_NEED[1]) {
            script.add("\n/* ----- FOREIGN KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.FOREIGN_KEY) {
                    addConstraintToScript(obj);
                }
        }
        if (TABLE_CONSTRAINTS_NEED[3]) {
            script.add("\n/* ----- CHECK KEYs defining ----- */\n");
            for (org.executequery.gui.browser.ColumnConstraint obj : constraints)
                if (obj.getType() == NamedObject.CHECK_KEY)
                    addConstraintToScript(obj);
        }
    }

    public void createComputedFields() {

        if (computedFields.size() < 1)
            return;

        if (COMPUTED_FIELDS_NEED) {

            script.add("\n/* ----- COMPUTED FIELDs defining ----- */\n");
            for (ColumnData cd : computedFields) {
                script.add("\n/* " + cd.getTableName() + "." + cd.getColumnName() + " */");
                script.add("\nALTER TABLE " + cd.getTableName());
                script.add("\n\tDROP " + MiscUtils.getFormattedObject(cd.getColumnName()) + ",");
                script.add("\n\tADD " + SQLUtils.generateDefinitionColumn(
                        cd, true, false, false) + ";\n");
            }
        }
    }

    public void createStubs(
            boolean functions, boolean procedures, boolean triggers, boolean ddlTriggers, boolean dbTriggers) {

        if (functions)
            addStubsToScript(FUNCTION, createListObjects(FUNCTION));
        if (procedures)
            addStubsToScript(PROCEDURE, createListObjects(PROCEDURE));
        if (triggers)
            addStubsToScript(TRIGGER, createListObjects(TRIGGER));
        if (ddlTriggers)
            addStubsToScript(DDL_TRIGGER, createListObjects(DDL_TRIGGER));
        if (dbTriggers)
            addStubsToScript(DATABASE_TRIGGER, createListObjects(DATABASE_TRIGGER));
    }

    private void addConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tADD " +
                SQLUtils.generateDefinitionColumnConstraint(obj, false, false) + ";\n");
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

                if (databaseObject.getType() == NamedObject.TABLE || databaseObject.getType() == NamedObject.GLOBAL_TEMPORARY) {

                    if (!Arrays.equals(TABLE_CONSTRAINTS_NEED, new boolean[]{false, false, false, false}))
                        createListConstraints(databaseObject);
                    if (COMPUTED_FIELDS_NEED)
                        createListComputedFields(databaseObject);
                }
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

        for (ColumnConstraint cc : ((DefaultDatabaseTable) databaseObject).getConstraints()) {

            if ((cc.getType() == PRIMARY_KEY && !TABLE_CONSTRAINTS_NEED[0]) ||
                    (cc.getType() == FOREIGN_KEY && !TABLE_CONSTRAINTS_NEED[1]) ||
                    (cc.getType() == UNIQUE_KEY && !TABLE_CONSTRAINTS_NEED[2]) ||
                    (cc.getType() == CHECK_KEY && !TABLE_CONSTRAINTS_NEED[3]))
                continue;

            constraintsList += "\t" + databaseObject.getName() + "." + cc.getName() + "\n";
            constraints.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
        }
    }

    private void createListComputedFields(NamedObject databaseObject) {

        if (computedFieldsList == null)
            computedFieldsList = "";

        List<ColumnData> listCD = new ArrayList<>();
        DefaultDatabaseTable tempTable = (DefaultDatabaseTable) databaseObject;

        for (int i = 0; i < tempTable.getColumnCount(); i++)
            listCD.add(new ColumnData(tempTable.getHost().getDatabaseConnection(), tempTable.getColumns().get(i)));

        for (ColumnData cd : listCD) {
            if (!MiscUtils.isNull(cd.getComputedBy())) {
                computedFieldsList += "\t" + tempTable.getName() + "." + cd.getColumnName() + "\n";
                computedFields.add(cd);
            }
        }
    }

    private List<NamedObject> sortObjectsByDependency(List<NamedObject> objectsList) {

        if (objectsList.size() < 1)
            return null;

        String templateQuery =
                "SELECT RDB$DEPENDENT_NAME FROM RDB$DEPENDENCIES WHERE RDB$DEPENDED_ON_NAME = '%s';";
        DefaultStatementExecutor executor =
                new DefaultStatementExecutor(compareConnection.getDatabaseConnection(), true);

        ListIterator<NamedObject> keyIterator = objectsList.listIterator();
        ListIterator<NamedObject> valueIterator = objectsList.listIterator();
        Map<String, NamedObject> objectMap = objectsList.stream().collect(
                Collectors.toMap(key -> keyIterator.next().getName(), value -> valueIterator.next()));

        try {

            for (String objectName : objectMap.keySet()) {
                NamedObject tempObject = objectMap.get(objectName);

                String query = String.format(templateQuery, MiscUtils.getFormattedObject(objectName));

                executor.releaseResources();
                SqlStatementResult statementResult =
                        executor.execute(QueryTypes.SELECT, query);
                ResultSet resultSet = statementResult.getResultSet();

                if (resultSet == null)
                    continue;

                while (resultSet.next()) {
                    String dependentObjectName = resultSet.getString(1).trim();

                    if (objectMap.containsKey(dependentObjectName)) {
                        NamedObject dependentObject = objectMap.get(dependentObjectName);

                        if (objectsList.indexOf(tempObject) > objectsList.indexOf(dependentObject)) {
                            objectsList.remove(tempObject);
                            objectsList.add(objectsList.indexOf(dependentObject), tempObject);
                        }
                    }
                }
            }

            executor.closeConnection();

        } catch (java.lang.Exception e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error while comparing objects dependencies:\n" + e.getMessage(), e);
            Log.error(e);
        }

        return objectsList;
    }

    private void addStubsToScript(int type, List<NamedObject> stubsList) {

        if (stubsList.size() < 1)
            return;

        String header = MessageFormat.format(
                "\n/* ----- Creating {0} stubs ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : stubsList) {
            script.add("\n/* " + obj.getName() + " */");
            script.add("\n" + SQLUtils.generateCreateDefaultStub(obj));
        }

    }

    // ---

    public String getLists() {
        return lists;
    }

    public String getConstraintsList() {
        return constraintsList;
    }

    public String getComputedFieldsList() {
        return computedFieldsList;
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

    public static boolean[] getTableConstraintsNeed() {
        return TABLE_CONSTRAINTS_NEED;
    }

    public static boolean isCommentsNeed() {
        return COMMENTS_NEED;
    }

    public static boolean isComputedFieldsNeed() {
        return COMPUTED_FIELDS_NEED;
    }

    public void clearLists() {
        createdObjects.clear();
        alteredObjects.clear();
        droppedObjects.clear();
        script.clear();
        lists = "";
    }

}

