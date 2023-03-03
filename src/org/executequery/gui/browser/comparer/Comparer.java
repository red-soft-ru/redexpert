package org.executequery.gui.browser.comparer;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.databaseobjects.impl.ColumnConstraint;
import org.executequery.databaseobjects.impl.DefaultDatabaseIndex;
import org.executequery.databaseobjects.impl.DefaultDatabaseTable;
import org.executequery.datasource.PooledStatement;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.PreparedStatement;
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
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToCreate;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToDrop;
    private final List<ColumnData> computedFields;

    protected ArrayList<String> createdObjects = new ArrayList<>();
    protected ArrayList<String> alteredObjects = new ArrayList<>();
    protected ArrayList<String> droppedObjects = new ArrayList<>();

    public Comparer(DatabaseConnection dbSlave, DatabaseConnection dbMaster,
                    boolean[] constraintsNeed, boolean commentsNeed, boolean computedNeed) {

        script = new ArrayList<>();
        constraintsToCreate = new ArrayList<>();
        constraintsToDrop = new ArrayList<>();
        computedFields = new ArrayList<>();

        compareConnection = new DefaultStatementExecutor(dbSlave, true);
        masterConnection = new DefaultStatementExecutor(dbMaster, true);

        Comparer.TABLE_CONSTRAINTS_NEED = constraintsNeed;
        Comparer.COMMENTS_NEED = commentsNeed;
        Comparer.COMPUTED_FIELDS_NEED = computedNeed;

    }

    public void createObjects(int type) {

        List<NamedObject> createObjects = sortObjectsByDependency(createListObjects(type));

        if (createObjects == null || createObjects.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Creating {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        DefaultStatementExecutor querySender = null;
        PooledStatement statement = null;

        boolean isFirst = true;
        for (NamedObject obj : createObjects) {
            AbstractDatabaseObject databaseObject = (AbstractDatabaseObject) obj;

            if (!isFirst) {
                databaseObject.setStatementForLoadInfo(statement);
                databaseObject.setQuerySender(querySender);
            }
            databaseObject.setSomeExecute(true);

            String sqlScript = databaseObject.getCompareCreateSQL();
            querySender = databaseObject.getQuerySender();
            statement = databaseObject.getStatementForLoadInfo();
            isFirst = false;

            if (!sqlScript.contains("Will be created with constraint defining")) {
                script.add("\n/* " + obj.getName() + " */");
                script.add("\n" + sqlScript);
                lists += "\t" + obj.getName() + "\n";
                isHeaderNeeded = true;
            }
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void dropObjects(int type) {

        List<NamedObject> dropObjects = sortObjectsByDependency(dropListObjects(type));

        if (dropObjects == null || dropObjects.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Dropping {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        for (NamedObject obj : dropObjects) {

            String sqlScript = ((type != INDEX) ?
                    ((AbstractDatabaseObject) obj).getDropSQL() :
                    ((DefaultDatabaseIndex) obj).getComparedDropSQL());

            if (!sqlScript.contains("Remove with table constraint")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                lists += "\t" + obj.getName() + "\n";
                isHeaderNeeded = true;
            }
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void alterObjects(int type) {

        Map<NamedObject, NamedObject> alterObjects = alterListObjects(type);

        if (alterObjects.isEmpty())
            return;

        if (Objects.equals(Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]), "ROLE"))
            return;

        String header = MessageFormat.format(
                "\n/* ----- Altering {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        DefaultStatementExecutor masterQuerySender = null;
        PooledStatement masterStatement = null;

        DefaultStatementExecutor compareQuerySender = null;
        PooledStatement compareStatement = null;

        boolean isFirst = true;
        for (NamedObject obj : alterObjects.keySet()) {

            AbstractDatabaseObject masterObject = (AbstractDatabaseObject) obj;
            AbstractDatabaseObject compareObject = (AbstractDatabaseObject) alterObjects.get(obj);

            if (!isFirst) {

                masterObject.setStatementForLoadInfo(masterStatement);
                masterObject.setQuerySender(masterQuerySender);

                compareObject.setStatementForLoadInfo(compareStatement);
                compareObject.setQuerySender(compareQuerySender);
            }
            masterObject.setSomeExecute(true);
            compareObject.setSomeExecute(true);

            String sqlScript = masterObject.getCompareAlterSQL(compareObject);
            isFirst = false;

            masterQuerySender = masterObject.getQuerySender();
            masterStatement = masterObject.getStatementForLoadInfo();

            compareQuerySender = compareObject.getQuerySender();
            compareStatement = compareObject.getStatementForLoadInfo();

            if (!sqlScript.contains("there are no changes")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                lists += "\t" + obj.getName() + "\n";
                isHeaderNeeded = true;
            }
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void createConstraints() {

        if (constraintsToCreate.isEmpty())
            return;

        script.add("\n/* ----- PRIMARY KEYs defining ----- */\n");
        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate)
            if (obj.getType() == NamedObject.PRIMARY_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- UNIQUE KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate)
            if (obj.getType() == NamedObject.UNIQUE_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- FOREIGN KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate)
            if (obj.getType() == NamedObject.FOREIGN_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- CHECK KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate)
            if (obj.getType() == NamedObject.CHECK_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

    }

    public void dropConstraints(boolean table, boolean globalTemporary, boolean isDrop, boolean isAlter) {

        constraintsToDrop.clear();

        if (isDrop) {
            if (table) dropListConstraints(TABLE);
            if (globalTemporary) dropListConstraints(GLOBAL_TEMPORARY);
        }
        if (isAlter) {
            if (table) alterListConstraints(TABLE);
            if (globalTemporary) alterListConstraints(GLOBAL_TEMPORARY);
        }

        if (constraintsToDrop.isEmpty())
            return;

        script.add("\n/* ----- CHECK KEYs removing ----- */\n");
        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop)
            if (obj.getType() == CHECK_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- FOREIGN KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop)
            if (obj.getType() == FOREIGN_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- UNIQUE KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop)
            if (obj.getType() == UNIQUE_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- PRIMARY KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop)
            if (obj.getType() == PRIMARY_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

    }

    public void createComputedFields() {

        if (computedFields.isEmpty())
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
                SQLUtils.generateDefinitionColumnConstraint(obj, true, false) + ";\n");
    }

    private void dropConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tDROP CONSTRAINT " + obj.getName() + ";\n");
    }

    private List<NamedObject> createListObjects(int type) {

        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<NamedObject> createObjects = new ArrayList<>();
        DefaultStatementExecutor querySender = null;
        PooledStatement statement = null;

        boolean isFirst = true;

        for (NamedObject databaseObject : compareConnectionObjectsList) {
            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    masterConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                createObjects.add(databaseObject);

                if (databaseObject.getType() == NamedObject.TABLE || databaseObject.getType() == NamedObject.GLOBAL_TEMPORARY) {
                    AbstractDatabaseObject abstractDatabaseObject = (AbstractDatabaseObject) databaseObject;

                    if (!isFirst) {
                        abstractDatabaseObject.setStatementForLoadInfo(statement);
                        abstractDatabaseObject.setQuerySender(querySender);
                    }
                    abstractDatabaseObject.setSomeExecute(true);

                    if (!Arrays.equals(TABLE_CONSTRAINTS_NEED, new boolean[]{false, false, false, false}))
                        createListConstraints(databaseObject);
                    if (COMPUTED_FIELDS_NEED)
                        createListComputedFields(databaseObject);

                    querySender = abstractDatabaseObject.getQuerySender();
                    statement = abstractDatabaseObject.getStatementForLoadInfo();
                    isFirst = false;

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
                if (Objects.equals(masterObject.getName(), compareObject.getName())) {
                    alterObjects.put(masterObject, compareObject);
                    break;
                }
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
            constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
        }
    }

    private void dropListConstraints(int type) {

        if (type != TABLE && type != GLOBAL_TEMPORARY)
            return;

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        for (NamedObject databaseObject : masterConnectionObjectsList) {
            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    compareConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                for (ColumnConstraint cc : ((DefaultDatabaseTable) databaseObject).getConstraints())
                    constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
            }
        }

    }

    private void alterListConstraints(int type) {

        if (type != TABLE && type != GLOBAL_TEMPORARY)
            return;

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<ColumnConstraint> droppedConstraints = new ArrayList<>();

        compareConnectionObjectsList
                .forEach(compareObject -> masterConnectionObjectsList.stream()
                        .filter(masterObject -> Objects.equals(masterObject.getName(), compareObject.getName()) &&
                                !((AbstractDatabaseObject) masterObject)
                                        .getCompareAlterSQL((AbstractDatabaseObject) compareObject)
                                        .contains("there are no changes"))
                        .forEach(masterObject -> checkConstraintsPair(masterObject, compareObject, droppedConstraints))
                );

        // --- check for temporary DROP dependent CONSTRAINT ---

        if (droppedConstraints.isEmpty())
            return;

        List<String> droppedConstraintsColumns = new ArrayList<>();
        droppedConstraints.stream()
                .filter(cc -> (cc.isPrimaryKey() || cc.isUniqueKey()) && cc.getColumnDisplayList() != null)
                .forEach(cc -> droppedConstraintsColumns.addAll(Arrays.stream(cc.getColumnDisplayList().split(","))
                                .map(i -> cc.getTableName().concat("." + i))
                                .collect(Collectors.toList())
                        )
                );

        for (NamedObject masterObject : masterConnectionObjectsList) {
            for (ColumnConstraint masterCC : ((DefaultDatabaseTable) masterObject).getConstraints()) {

                if (droppedConstraints.contains(masterCC) || !masterCC.isForeignKey())
                    continue;

                if (Arrays.stream(masterCC.getReferenceColumnDisplayList().split(","))
                        .map(i -> masterCC.getTableName().concat("." + i))
                        .anyMatch(droppedConstraintsColumns::contains)) {

                    constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                    constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                    droppedConstraints.add(masterCC);
                }

            }
        }
    }

    private void checkConstraintsPair(NamedObject masterObject, NamedObject compareObject, List<ColumnConstraint> droppedConstraints) {

        List<ColumnConstraint> masterConstraints = ((DefaultDatabaseTable) masterObject).getConstraints();
        List<ColumnConstraint> compareConstraints = ((DefaultDatabaseTable) compareObject).getConstraints();

        //check for DROP excess CONSTRAINT
        for (ColumnConstraint masterCC : masterConstraints) {

            if ((masterCC.getType() == PRIMARY_KEY && !TABLE_CONSTRAINTS_NEED[0]) ||
                    (masterCC.getType() == FOREIGN_KEY && !TABLE_CONSTRAINTS_NEED[1]) ||
                    (masterCC.getType() == UNIQUE_KEY && !TABLE_CONSTRAINTS_NEED[2]) ||
                    (masterCC.getType() == CHECK_KEY && !TABLE_CONSTRAINTS_NEED[3]))
                continue;

            long dropCheck = compareConstraints.stream()
                    .filter(comparingCC -> !Objects.equals(masterCC.getName(), comparingCC.getName())).count();

            if (dropCheck == compareConstraints.size()) {
                constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                droppedConstraints.add(masterCC);
            }
        }

        //check for temporary DROP CONSTRAINT
        for (ColumnConstraint masterCC : masterConstraints) {

            if (droppedConstraints.contains(masterCC))
                continue;

            // --- if constraint will be changed ---

            compareConstraints.stream()
                    .filter(comparingCC -> Objects.equals(masterCC.getName(), comparingCC.getName()) &&
                            !Objects.equals(masterCC.getColumnDisplayList(), comparingCC.getColumnDisplayList()))
                    .findFirst().ifPresent(comparingCC -> {
                        constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                        constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, comparingCC));
                        droppedConstraints.add(masterCC);
                    });

            // ---

            if (droppedConstraints.contains(masterCC))
                continue;

            // --- if constraint column will be changed or removed ---

            List<String> masterConstraintColumns = new ArrayList<>();
            if (masterCC.getColumnDisplayList() != null)
                Collections.addAll(masterConstraintColumns, masterCC.getColumnDisplayList().split(","));

            List<DatabaseColumn> masterColumns = ((DefaultDatabaseTable) masterObject).getColumns();
            List<DatabaseColumn> compareColumns = ((DefaultDatabaseTable) compareObject).getColumns();


            for (DatabaseColumn masterC : masterColumns) {

                if (!masterConstraintColumns.contains(masterC.getName()))
                    continue;

                int dropCheck = 0;

                for (DatabaseColumn comparingC : compareColumns) {

                    if (Objects.equals(masterC.getName(), comparingC.getName())) {

                        if (!SQLUtils.generateAlterDefinitionColumn(
                                new ColumnData(((DefaultDatabaseTable) masterObject).getHost().getDatabaseConnection(), masterC, false),
                                new ColumnData(((DefaultDatabaseTable) compareObject).getHost().getDatabaseConnection(), comparingC, false),
                                isComputedFieldsNeed()).equals("")) {

                            constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                            constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                            droppedConstraints.add(masterCC);
                        }
                        break;

                    } else dropCheck++;
                }

                if (dropCheck == compareColumns.size()) {
                    constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                    droppedConstraints.add(masterCC);
                }
            }
        }

        //check for ADD new CONSTRAINT
        for (ColumnConstraint comparingCC : compareConstraints) {

            if ((comparingCC.getType() == PRIMARY_KEY && !TABLE_CONSTRAINTS_NEED[0]) ||
                    (comparingCC.getType() == FOREIGN_KEY && !TABLE_CONSTRAINTS_NEED[1]) ||
                    (comparingCC.getType() == UNIQUE_KEY && !TABLE_CONSTRAINTS_NEED[2]) ||
                    (comparingCC.getType() == CHECK_KEY && !TABLE_CONSTRAINTS_NEED[3]))
                continue;

            long addCheck = masterConstraints.stream()
                    .filter(masterCC -> !Objects.equals(masterCC.getName(), comparingCC.getName())).count();

            if (addCheck == masterConstraints.size())
                constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, comparingCC));
        }

    }

    private void createListComputedFields(NamedObject databaseObject) {

        if (computedFieldsList == null)
            computedFieldsList = "";

        List<ColumnData> listCD = new ArrayList<>();
        DefaultDatabaseTable tempTable = (DefaultDatabaseTable) databaseObject;

        for (int i = 0; i < tempTable.getColumnCount(); i++)
            listCD.add(new ColumnData(tempTable.getHost().getDatabaseConnection(), tempTable.getColumns().get(i), false));

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
                "SELECT RDB$DEPENDENT_NAME FROM RDB$DEPENDENCIES WHERE RDB$DEPENDED_ON_NAME = ?;";
        DefaultStatementExecutor executor =
                new DefaultStatementExecutor(compareConnection.getDatabaseConnection(), true);

        ListIterator<NamedObject> keyIterator = objectsList.listIterator();
        ListIterator<NamedObject> valueIterator = objectsList.listIterator();
        Map<String, NamedObject> objectMap = objectsList.stream().collect(
                Collectors.toMap(key -> keyIterator.next().getName(), value -> valueIterator.next()));

        try {

            PreparedStatement selectDependenciesStatement = executor.getPreparedStatement(templateQuery);

            for (String objectName : objectMap.keySet()) {
                NamedObject tempObject = objectMap.get(objectName);

                selectDependenciesStatement.setString(1, objectName);
                selectDependenciesStatement.executeQuery();

                ResultSet resultSet = selectDependenciesStatement.executeQuery();

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
            selectDependenciesStatement.close();
            executor.releaseResources();

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

    public StatementExecutor getMasterConnection() {
        return masterConnection;
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

