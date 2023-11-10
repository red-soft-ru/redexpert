package org.executequery.gui.browser.comparer;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.*;
import org.executequery.gui.browser.ColumnData;
import org.executequery.gui.browser.ComparerDBPanel;
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
    private static boolean FIELDS_POSITIONS_NEED;


    protected ComparerDBPanel panel;
    protected DefaultStatementExecutor compareConnection;
    protected DefaultStatementExecutor masterConnection;

    private final int[] counter;
    private String constraintsList;
    private String computedFieldsList;

    private final ArrayList<String> script;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToCreate;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToDrop;
    private final List<ColumnData> computedFields;

    protected ArrayList<String> createdObjects = new ArrayList<>();
    protected ArrayList<String> alteredObjects = new ArrayList<>();
    protected ArrayList<String> droppedObjects = new ArrayList<>();

    public Comparer(
            ComparerDBPanel panel, DatabaseConnection dbSlave, DatabaseConnection dbMaster,
            boolean[] constraintsNeed, boolean commentsNeed, boolean computedNeed, boolean fieldsPositions) {

        script = new ArrayList<>();
        constraintsToCreate = new ArrayList<>();
        constraintsToDrop = new ArrayList<>();
        computedFields = new ArrayList<>();

        counter = new int[] {0, 0, 0};

        this.panel = panel;
        compareConnection = new DefaultStatementExecutor(dbSlave, true);
        masterConnection = new DefaultStatementExecutor(dbMaster, true);

        Comparer.TABLE_CONSTRAINTS_NEED = constraintsNeed;
        Comparer.COMMENTS_NEED = commentsNeed;
        Comparer.COMPUTED_FIELDS_NEED = computedNeed;
        Comparer.FIELDS_POSITIONS_NEED = fieldsPositions;

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

        LoadingObjectsHelper loadingObjectsHelper = new LoadingObjectsHelper(createObjects.size());

        panel.recreateProgressBar(
                "GenerateCreateScript", NamedObject.META_TYPES[type],
                createObjects.size()
        );

        for (NamedObject obj : createObjects) {

            if (panel.isCanceled())
                break;

            AbstractDatabaseObject databaseObject = (AbstractDatabaseObject) obj;

            loadingObjectsHelper.preparingLoadForObjectAndCols(databaseObject);

            String sqlScript = databaseObject.getCompareCreateSQL();
            loadingObjectsHelper.postProcessingLoadForObjectAndCols(databaseObject);

            if (!sqlScript.contains("Will be created with constraint defining")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.CREATE, type, obj.getName());
                panel.getComparedObjectList().add(new ComparedObject(type, obj.getName(), sqlScript, null));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[0]++;
            }

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelper.releaseResources();

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

        panel.recreateProgressBar(
                "GenerateDropScript", NamedObject.META_TYPES[type],
                dropObjects.size()
        );

        for (NamedObject obj : dropObjects) {

            if (panel.isCanceled())
                break;

            String sqlScript = ((type != INDEX) ?
                    ((AbstractDatabaseObject) obj).getDropSQL() :
                    ((DefaultDatabaseIndex) obj).getComparedDropSQL());

            if (!sqlScript.contains("Remove with table constraint")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.DROP, type, obj.getName());
                panel.getComparedObjectList().add(new ComparedObject(type, obj.getName(), null, ((AbstractDatabaseObject) obj).getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[1] ++;
            }

            panel.incrementProgressBarValue();
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

        LoadingObjectsHelper loadingObjectsHelperMaster = new LoadingObjectsHelper(alterObjects.size());
        LoadingObjectsHelper loadingObjectsHelperCompare = new LoadingObjectsHelper(alterObjects.size());

        panel.recreateProgressBar(
                "GenerateAlterScript", NamedObject.META_TYPES[type],
                alterObjects.keySet().size()
        );

        for (NamedObject obj : alterObjects.keySet()) {

            if (panel.isCanceled())
                break;

            AbstractDatabaseObject masterObject = (AbstractDatabaseObject) obj;

            AbstractDatabaseObject compareObject = (AbstractDatabaseObject) alterObjects.get(obj);

            loadingObjectsHelperMaster.preparingLoadForObjectAndCols(masterObject);
            loadingObjectsHelperCompare.preparingLoadForObjectAndCols(compareObject);

            String sqlScript = masterObject.getCompareAlterSQL(compareObject);
            loadingObjectsHelperMaster.postProcessingLoadForObjectAndCols(masterObject);
            loadingObjectsHelperCompare.postProcessingLoadForObjectAndCols(compareObject);

            if (!sqlScript.contains("there are no changes")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.ALTER, type, obj.getName());
                panel.getComparedObjectList().add(new ComparedObject(type, obj.getName(), compareObject.getCreateSQLText(), masterObject.getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[2]++;
            }

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelperMaster.releaseResources();
        loadingObjectsHelperCompare.releaseResources();

        if (alterObjects.size() > 0) {
            AbstractDatabaseObject masterObject = (AbstractDatabaseObject) alterObjects.keySet().toArray()[0];
            masterObject.getHost().setPauseLoadingTreeForSearch(false);
            ((AbstractDatabaseObject) alterObjects.get(masterObject)).getHost().setPauseLoadingTreeForSearch(false);
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void createConstraints() {

        if (constraintsToCreate.isEmpty())
            return;

        panel.recreateProgressBar(
                "GenerateCreateConstraintScript", null,
                constraintsToCreate.size() * 4
        );

        script.add("\n/* ----- PRIMARY KEYs defining ----- */\n");
        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == NamedObject.PRIMARY_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- UNIQUE KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == NamedObject.UNIQUE_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- FOREIGN KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == NamedObject.FOREIGN_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- CHECK KEYs defining ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToCreate) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == NamedObject.CHECK_KEY) {
                addConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
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

        if (constraintsToDrop.isEmpty() || panel.isCanceled())
            return;

        panel.recreateProgressBar(
                "GenerateDropConstraintScript", null,
                constraintsToDrop.size() * 4
        );

        script.add("\n/* ----- CHECK KEYs removing ----- */\n");
        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == CHECK_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- FOREIGN KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == FOREIGN_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- UNIQUE KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == UNIQUE_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

        script.add("\n/* ----- PRIMARY KEYs removing ----- */\n");
        headerIndex = script.size() - 1;
        isHeaderNeeded = false;

        for (org.executequery.gui.browser.ColumnConstraint obj : constraintsToDrop) {

            if (panel.isCanceled())
                break;

            if (obj.getType() == PRIMARY_KEY) {
                dropConstraintToScript(obj);
                isHeaderNeeded = true;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);

    }

    public void createComputedFields() {

        if (computedFields.isEmpty())
            return;

        if (COMPUTED_FIELDS_NEED) {

            panel.recreateProgressBar(
                    "GenerateCreateComputedScript", null,
                    computedFields.size()
            );

            script.add("\n/* ----- COMPUTED FIELDs defining ----- */\n");
            for (ColumnData cd : computedFields) {

                if (panel.isCanceled())
                    break;

                script.add("\n/* " + cd.getTableName() + "." + cd.getColumnName() + " */");
                script.add("\nALTER TABLE " + cd.getTableName());
                script.add("\n\tDROP " + cd.getFormattedColumnName() + ",");
                script.add("\n\tADD " + SQLUtils.generateDefinitionColumn(
                        cd, true, false, false) + ";\n");

                panel.incrementProgressBarValue();
            }
        }
    }

    public void createStubs(
            boolean functions, boolean procedures, boolean triggers, boolean ddlTriggers, boolean dbTriggers, boolean jobs) {

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
        if (jobs)
            addStubsToScript(JOB, createListObjects(JOB));

    }

    private void addConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tADD " +
                SQLUtils.generateDefinitionColumnConstraint(obj, true, false, compareConnection.getDatabaseConnection()) + ";\n");
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
        LoadingObjectsHelper loadingObjectsHelper = new LoadingObjectsHelper(compareConnectionObjectsList.size());

        panel.recreateProgressBar(
                "ExtractingForCreate", NamedObject.META_TYPES[type],
                compareConnectionObjectsList.size()
        );

        for (NamedObject databaseObject : compareConnectionObjectsList) {

            if (panel.isCanceled())
                break;

            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    masterConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                createObjects.add(databaseObject);

                if (databaseObject.getType() == NamedObject.TABLE || databaseObject.getType() == NamedObject.GLOBAL_TEMPORARY) {
                    AbstractDatabaseObject abstractDatabaseObject = (AbstractDatabaseObject) databaseObject;

                    loadingObjectsHelper.preparingLoadForObjectAndCols(abstractDatabaseObject);

                    if (!Arrays.equals(TABLE_CONSTRAINTS_NEED, new boolean[]{false, false, false, false}))
                        createListConstraints(databaseObject);
                    if (COMPUTED_FIELDS_NEED)
                        createListComputedFields(databaseObject);

                    loadingObjectsHelper.postProcessingLoadForObjectAndCols(abstractDatabaseObject);

                }
            }

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelper.releaseResources();

        return createObjects;
    }

    private List<NamedObject> dropListObjects(int type) {

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<NamedObject> dropObjects = new ArrayList<>();

        panel.recreateProgressBar(
                "ExtractingForDrop", NamedObject.META_TYPES[type],
                masterConnectionObjectsList.size()
        );

        for (NamedObject databaseObject : masterConnectionObjectsList) {

            if (panel.isCanceled())
                break;

            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    compareConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                dropObjects.add(databaseObject);
            }

            panel.incrementProgressBarValue();
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

        panel.recreateProgressBar(
                "ExtractingForAlter", NamedObject.META_TYPES[type],
                masterConnectionObjectsList.size() * compareConnectionObjectsList.size()
        );

        for (NamedObject compareObject : compareConnectionObjectsList) {

            if (panel.isCanceled())
                break;

            for (NamedObject masterObject : masterConnectionObjectsList) {

                if (panel.isCanceled())
                    break;

                if (Objects.equals(masterObject.getName(), compareObject.getName())) {
                    alterObjects.put(masterObject, compareObject);
                    break;
                }

                panel.incrementProgressBarValue();
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

        if ((type != TABLE && type != GLOBAL_TEMPORARY) || panel.isCanceled())
            return;

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        panel.recreateProgressBar(
                "ExtractingConstraintsForDrop", null,
                masterConnectionObjectsList.size()
        );

        for (NamedObject databaseObject : masterConnectionObjectsList) {

            if (panel.isCanceled())
                break;

            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    compareConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                for (ColumnConstraint cc : ((DefaultDatabaseTable) databaseObject).getConstraints()) {

                    if (panel.isCanceled())
                        break;

                    constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
                }
            }

            panel.incrementProgressBarValue();
        }

    }

    private void alterListConstraints(int type) {

        if ((type != TABLE && type != GLOBAL_TEMPORARY) || panel.isCanceled())
            return;

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<ColumnConstraint> droppedConstraints = new ArrayList<>();

        panel.recreateProgressBar(
                "ExtractingConstraintsForAlter", null,
                masterConnectionObjectsList.size() * masterConnectionObjectsList.size()
        );
        LoadingObjectsHelper loadingObjectsHelperMaster = new LoadingObjectsHelper(masterConnectionObjectsList.size());
        LoadingObjectsHelper loadingObjectsHelperCompare = new LoadingObjectsHelper(compareConnectionObjectsList.size());
        for (NamedObject compareObject : compareConnectionObjectsList) {

            if (panel.isCanceled())
                break;
            AbstractDatabaseObject compareAbstractObject = (AbstractDatabaseObject) compareObject;
            loadingObjectsHelperCompare.preparingLoadForObjectCols(compareAbstractObject);
            for (NamedObject masterObject : masterConnectionObjectsList) {

                if (panel.isCanceled())
                    break;
                AbstractDatabaseObject masterAbstractObject = (AbstractDatabaseObject) masterObject;
                loadingObjectsHelperMaster.preparingLoadForObjectCols(masterAbstractObject);
                if (Objects.equals(masterObject.getName(), compareObject.getName()))
                    if (!((AbstractDatabaseObject) masterObject).getCompareAlterSQL((AbstractDatabaseObject) compareObject).contains("there are no changes"))
                        checkConstraintsPair(masterObject, compareObject, droppedConstraints);
                loadingObjectsHelperMaster.postProcessingLoadForObjectForCols(masterAbstractObject);

                panel.incrementProgressBarValue();
            }
            loadingObjectsHelperCompare.postProcessingLoadForObjectForCols(compareAbstractObject);
        }
        loadingObjectsHelperMaster.releaseResources();
        loadingObjectsHelperCompare.releaseResources();

        // --- check for temporary DROP dependent CONSTRAINT ---

        if (droppedConstraints.isEmpty() || panel.isCanceled())
            return;

        List<String> droppedConstraintsColumns = new ArrayList<>();
        for (ColumnConstraint cc : droppedConstraints) {

            if (panel.isCanceled())
                break;

            if ((cc.isPrimaryKey() || cc.isUniqueKey()) && cc.getColumnDisplayList() != null)
                droppedConstraintsColumns.addAll(cc.getColumnDisplayList().stream()
                        .map(i -> cc.getTableName().concat("." + i))
                        .collect(Collectors.toList()));
        }

        LoadingObjectsHelper loadingObjectsHelper = new LoadingObjectsHelper(masterConnectionObjectsList.size());

        for (NamedObject masterObject : masterConnectionObjectsList) {

            if (panel.isCanceled())
                break;
            loadingObjectsHelper.preparingLoadForObjectAndCols((AbstractDatabaseObject) masterObject);
            for (ColumnConstraint masterCC : ((DefaultDatabaseTable) masterObject).getConstraints()) {

                if (panel.isCanceled())
                    break;

                if (droppedConstraints.contains(masterCC) || !masterCC.isForeignKey())
                    continue;

                if (masterCC.getReferenceColumnDisplayList().stream()
                        .map(i -> masterCC.getTableName().concat("." + i))
                        .anyMatch(droppedConstraintsColumns::contains)) {

                    constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                    constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                    droppedConstraints.add(masterCC);
                }
            }
            loadingObjectsHelper.postProcessingLoadForObjectAndCols((AbstractDatabaseObject) masterObject);
        }
        loadingObjectsHelper.releaseResources();
    }

    private void checkConstraintsPair(NamedObject masterObject, NamedObject compareObject, List<ColumnConstraint> droppedConstraints) {

        List<ColumnConstraint> masterConstraints = ((DefaultDatabaseTable) masterObject).getConstraints();
        List<ColumnConstraint> compareConstraints = ((DefaultDatabaseTable) compareObject).getConstraints();

        //check for DROP excess CONSTRAINT
        for (ColumnConstraint masterCC : masterConstraints) {

            if (panel.isCanceled())
                break;

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

            if (panel.isCanceled())
                break;

            if (droppedConstraints.contains(masterCC))
                continue;

            // --- if constraint will be changed ---

            for (ColumnConstraint comparingCC : compareConstraints) {

                if (panel.isCanceled())
                    break;

                if (Objects.equals(masterCC.getName(), comparingCC.getName())) {
                    if (!Objects.equals(masterCC.getColumnDisplayList(), comparingCC.getColumnDisplayList())) {

                        constraintsToDrop.add(new org.executequery.gui.browser.ColumnConstraint(false, masterCC));
                        constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, comparingCC));
                        droppedConstraints.add(masterCC);
                    }
                }
            }

            // ---

            if (droppedConstraints.contains(masterCC))
                continue;

            // --- if constraint column will be changed or removed ---

            List<String> masterConstraintColumns = new ArrayList<>();
            if (masterCC.getColumnDisplayList() != null)
                masterConstraintColumns.addAll(masterCC.getColumnDisplayList());

            List<DatabaseColumn> masterColumns = ((DefaultDatabaseTable) masterObject).getColumns();
            List<DatabaseColumn> compareColumns = ((DefaultDatabaseTable) compareObject).getColumns();

            for (DatabaseColumn masterC : masterColumns) {

                if (panel.isCanceled())
                    break;

                if (!masterConstraintColumns.contains(masterC.getName()))
                    continue;

                int dropCheck = 0;

                for (DatabaseColumn comparingC : compareColumns) {

                    if (panel.isCanceled())
                        break;

                    if (Objects.equals(masterC.getName(), comparingC.getName())) {

                        if (!SQLUtils.generateAlterDefinitionColumn(
                                new ColumnData(((DefaultDatabaseTable) masterObject).getHost().getDatabaseConnection(), masterC, false),
                                new ColumnData(((DefaultDatabaseTable) compareObject).getHost().getDatabaseConnection(), comparingC, false),
                                isComputedFieldsNeed(),
                                isFieldsPositionsNeed()).isEmpty()) {

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

            if (panel.isCanceled())
                break;

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

        String templateQuery = "SELECT DISTINCT D.RDB$DEPENDENT_NAME FROM RDB$DEPENDENCIES D " +
                "WHERE D.RDB$DEPENDENT_TYPE = D.RDB$DEPENDED_ON_TYPE AND D.RDB$DEPENDED_ON_NAME = ?";

        DefaultStatementExecutor executor =
                new DefaultStatementExecutor(compareConnection.getDatabaseConnection(), true);

        Map<String, NamedObject> objectMap = new HashMap<>();
        objectsList.forEach(obj -> objectMap.put(obj.getName(), obj));

        try {

            PreparedStatement selectDependenciesStatement = executor.getPreparedStatement(templateQuery);

            panel.recreateProgressBar(
                    "SearchingForDependencies", null,
                    objectMap.keySet().size()
            );

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

                panel.incrementProgressBarValue();
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

        panel.recreateProgressBar(
                "CreatingStubs", NamedObject.META_TYPES[type],
                stubsList.size()
        );

        for (NamedObject obj : stubsList) {
            script.add("\n/* " + obj.getName() + " (STUB) */");
            script.add("\n" + SQLUtils.generateCreateDefaultStub(obj));
            panel.incrementProgressBarValue();
        }

    }

    // ---

    public String getConstraintsList() {
        return constraintsList;
    }

    public String getComputedFieldsList() {
        return computedFieldsList;
    }

    public ArrayList<String> getScript() {
        return script;
    }

    public String getScript(int elemIndex) {
        return script.get(elemIndex);
    }

    public int[] getCounter() {
        return counter;
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

    public static boolean isFieldsPositionsNeed() {
        return FIELDS_POSITIONS_NEED;
    }

    public void clearLists() {
        createdObjects.clear();
        alteredObjects.clear();
        droppedObjects.clear();
        script.clear();
    }

}

