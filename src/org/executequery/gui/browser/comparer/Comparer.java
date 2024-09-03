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
import org.underworldlabs.swing.Named;
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
    protected StatementExecutor masterExecutor;
    protected DatabaseConnection masterConnection;
    protected DatabaseConnection compareConnection;

    private int stubsInsertIndex;
    private Map<Integer, List<NamedObject>> stubsOnAlter;
    private Map<Integer, List<NamedObject>> stubsOnCreate;

    private final int[] counter;
    private String constraintsList;
    private String computedFieldsList;

    private final ArrayList<String> script;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToCreate;
    private final List<org.executequery.gui.browser.ColumnConstraint> constraintsToDrop;
    private final List<ColumnData> computedFields;

    public Comparer(ComparerDBPanel panel, DatabaseConnection connection,
                    boolean[] constraintsNeed, boolean commentsNeed, boolean computedNeed, boolean fieldsPositions) {
        this(panel, connection, connection, constraintsNeed, commentsNeed, computedNeed, fieldsPositions);
    }

    public Comparer(
            ComparerDBPanel panel, DatabaseConnection compareConnection, DatabaseConnection masterConnection,
            boolean[] constraintsNeed, boolean commentsNeed, boolean computedNeed, boolean fieldsPositions) {

        script = new ArrayList<>();
        constraintsToCreate = new ArrayList<>();
        constraintsToDrop = new ArrayList<>();
        computedFields = new ArrayList<>();

        stubsInsertIndex = -1;
        counter = new int[]{0, 0, 0};

        this.panel = panel;
        this.masterConnection = masterConnection;
        this.compareConnection = compareConnection;
        masterExecutor = new DefaultStatementExecutor(masterConnection, true);

        Comparer.TABLE_CONSTRAINTS_NEED = constraintsNeed;
        Comparer.COMMENTS_NEED = commentsNeed;
        Comparer.COMPUTED_FIELDS_NEED = computedNeed;
        Comparer.FIELDS_POSITIONS_NEED = fieldsPositions;

    }

    public void createErds(List<DefaultDatabaseTable> tables) {
        List<NamedObject> erds = new ArrayList<>();
        erds.addAll(tables);

        List<NamedObject> createObjects = null;
        if (panel.isExtractMetadata()) {
            createObjects = erds;
            for (NamedObject databaseObject : erds) {
                if (!Arrays.equals(TABLE_CONSTRAINTS_NEED, new boolean[]{false, false, false, false}))
                    createListConstraints(databaseObject);
                if (COMPUTED_FIELDS_NEED)
                    createListComputedFields(databaseObject);
            }
        } else createObjects =
                createListObjects(
                        getObjects(masterConnection, TABLE), erds, TABLE);

        if (createObjects == null || createObjects.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Creating {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[TABLE]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        panel.recreateProgressBar(
                "GenerateCreateScript", NamedObject.META_TYPES[TABLE],
                createObjects.size()
        );

        for (NamedObject obj : createObjects) {

            if (panel.isCanceled())
                break;

            AbstractDatabaseObject databaseObject = (AbstractDatabaseObject) obj;

            String sqlScript = databaseObject.getCompareCreateSQL();
            if (!sqlScript.contains("Will be created with constraint defining")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.CREATE, TABLE, obj);
                panel.addComparedObject(new ComparedObject(TABLE, databaseObject, sqlScript, null));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[0]++;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void dropErds(List<DefaultDatabaseTable> tables) {
        List<NamedObject> erds = new ArrayList<>();
        erds.addAll(tables);
        List<NamedObject> dropObjects = sortObjectsByDependency(
                dropListObjects(getObjects(masterConnection, TABLE), erds, TABLE));

        if (dropObjects == null || dropObjects.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Dropping {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[TABLE]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        panel.recreateProgressBar(
                "GenerateDropScript", NamedObject.META_TYPES[TABLE],
                dropObjects.size()
        );

        for (NamedObject obj : dropObjects) {

            if (panel.isCanceled())
                break;

            String sqlScript =
                    ((AbstractDatabaseObject) obj).getDropSQL();

            if (!sqlScript.contains("Remove with table constraint")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.DROP, TABLE, obj);
                panel.addComparedObject(new ComparedObject(TABLE, obj, null, ((AbstractDatabaseObject) obj).getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[1]++;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void alterErds(List<DefaultDatabaseTable> tables) {
        List<NamedObject> erds = new ArrayList<>();
        erds.addAll(tables);
        Map<NamedObject, NamedObject> alterObjects = alterListObjects(
                getObjects(masterConnection, TABLE), erds, TABLE);

        if (alterObjects.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Altering {0} ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[TABLE]));
        script.add(header);

        int headerIndex = script.size() - 1;
        boolean isHeaderNeeded = false;

        LoadingObjectsHelper loadingObjectsHelperMaster = new LoadingObjectsHelper(alterObjects.size());

        panel.recreateProgressBar(
                "GenerateAlterScript", NamedObject.META_TYPES[TABLE],
                alterObjects.keySet().size()
        );

        for (NamedObject obj : alterObjects.keySet()) {

            if (panel.isCanceled())
                break;

            AbstractDatabaseObject masterObject = (AbstractDatabaseObject) obj;
            loadingObjectsHelperMaster.preparingLoadForObjectAndCols(masterObject);

            AbstractDatabaseObject compareObject = (AbstractDatabaseObject) alterObjects.get(obj);

            String sqlScript = masterObject.getCompareAlterSQL(compareObject);
            if (!sqlScript.contains(SQLUtils.THERE_ARE_NO_CHANGES)) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.ALTER, TABLE, obj);
                panel.addComparedObject(new ComparedObject(TABLE, masterObject, compareObject.getCreateSQLText(), masterObject.getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[2]++;
            }

            loadingObjectsHelperMaster.postProcessingLoadForObjectAndCols(masterObject);

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelperMaster.releaseResources();

        if (!alterObjects.isEmpty()) {
            AbstractDatabaseObject masterObject = (AbstractDatabaseObject) alterObjects.keySet().toArray()[0];
            masterObject.getHost().setPauseLoadingTreeForSearch(false);
            ((AbstractDatabaseObject) alterObjects.get(masterObject)).getHost().setPauseLoadingTreeForSearch(false);
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }


    public void createObjects(int type) {

        List<NamedObject> createObjects = sortObjectsByDependency(
                createListObjects(
                        panel.isExtractMetadata() ? new ArrayList<>() : getObjects(masterConnection, type),
                        getObjects(compareConnection, type),
                        type
                )
        );

        if (createObjects == null || createObjects.isEmpty())
            return;

        if (stubsOnCreate != null && stubsOnCreate.containsKey(type) && stubsOnCreate.get(type) != null)
            stubsOnCreate.get(type).addAll(createObjects);

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
            if (!sqlScript.contains("Will be created with constraint defining")) {
                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.CREATE, type, obj);
                panel.addComparedObject(new ComparedObject(type, databaseObject, sqlScript, null));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[0]++;
            }

            loadingObjectsHelper.postProcessingLoadForObjectAndCols(databaseObject);

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelper.releaseResources();

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void dropObjects(int type) {

        List<NamedObject> dropObjects = sortObjectsByDependency(
                dropListObjects(getObjects(masterConnection, type), getObjects(compareConnection, type), type));

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
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.DROP, type, obj);
                panel.addComparedObject(new ComparedObject(type, obj, null, ((AbstractDatabaseObject) obj).getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[1]++;
            }

            panel.incrementProgressBarValue();
        }

        if (!isHeaderNeeded)
            script.remove(headerIndex);
    }

    public void alterObjects(int type) {

        Map<NamedObject, NamedObject> alterObjects = alterListObjects(
                getObjects(masterConnection, type), getObjects(compareConnection, type), type);

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
            loadingObjectsHelperMaster.preparingLoadForObjectAndCols(masterObject);

            AbstractDatabaseObject compareObject = (AbstractDatabaseObject) alterObjects.get(obj);
            loadingObjectsHelperCompare.preparingLoadForObjectAndCols(compareObject);

            String sqlScript = masterObject.getCompareAlterSQL(compareObject);
            if (!sqlScript.contains(SQLUtils.THERE_ARE_NO_CHANGES) && !sqlScript.equals(SQLUtils.ALTER_CONSTRAINTS)) {

                if (stubsOnAlter != null && stubsOnAlter.containsKey(type) && stubsOnAlter.get(type) != null)
                    stubsOnAlter.get(type).add(compareObject);

                script.add("\n/* " + obj.getName() + " */\n" + sqlScript);
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.ALTER, type, obj);
                panel.addComparedObject(new ComparedObject(type, masterObject, compareObject.getCreateSQLText(), masterObject.getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[2]++;
            } else if (sqlScript.equals(SQLUtils.ALTER_CONSTRAINTS)) {
                panel.addTreeComponent(ComparerDBPanel.ComparerTreeNode.ALTER, type, obj);
                panel.addComparedObject(new ComparedObject(type, masterObject, compareObject.getCreateSQLText(), masterObject.getCreateSQLText()));
                panel.addToLog("\t" + obj.getName());
                isHeaderNeeded = true;
                counter[2]++;
            }

            loadingObjectsHelperMaster.postProcessingLoadForObjectAndCols(masterObject);
            loadingObjectsHelperCompare.postProcessingLoadForObjectAndCols(compareObject);

            panel.incrementProgressBarValue();
        }
        loadingObjectsHelperMaster.releaseResources();
        loadingObjectsHelperCompare.releaseResources();

        if (!alterObjects.isEmpty()) {
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

    public void setStubsNeed(
            boolean onCreate,
            boolean functions, boolean procedures, boolean triggers,
            boolean ddlTriggers, boolean dbTriggers) {

        Map<Integer, List<NamedObject>> exampleHashMap = new HashMap<>();
        exampleHashMap.put(FUNCTION, functions ? new ArrayList<>() : null);
        exampleHashMap.put(PROCEDURE, procedures ? new ArrayList<>() : null);
        exampleHashMap.put(TRIGGER, triggers ? new ArrayList<>() : null);
        exampleHashMap.put(DDL_TRIGGER, ddlTriggers ? new ArrayList<>() : null);
        exampleHashMap.put(DATABASE_TRIGGER, dbTriggers ? new ArrayList<>() : null);

        if (stubsInsertIndex < 0)
            stubsInsertIndex = script.size();

        if (onCreate)
            stubsOnCreate = new HashMap<>(exampleHashMap);
        else
            stubsOnAlter = new HashMap<>(exampleHashMap);
    }

    private void addConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tADD " +
                SQLUtils.generateDefinitionColumnConstraint(obj, true, false, compareConnection, true) + ";\n");
    }

    private void dropConstraintToScript(org.executequery.gui.browser.ColumnConstraint obj) {
        script.add("\n/* " + obj.getTable() + "." + obj.getName() + " */");
        script.add("\nALTER TABLE " + obj.getTable() + "\n\tDROP CONSTRAINT " + obj.getName() + ";\n");
    }

    private List<NamedObject> createListObjects(
            List<NamedObject> masterObjects, List<NamedObject> compareObjects, int type) {

        List<NamedObject> createObjects = new ArrayList<>();
        List<String> masterObjectsNames = masterObjects.stream().map(Named::getName).collect(Collectors.toList());
        LoadingObjectsHelper loadingObjectsHelper = new LoadingObjectsHelper(compareObjects.size());

        panel.recreateProgressBar(
                "ExtractingForCreate", NamedObject.META_TYPES[type],
                compareObjects.size()
        );

        for (NamedObject databaseObject : compareObjects) {

            if (panel.isCanceled())
                break;

            if (!masterObjectsNames.contains(databaseObject.getName())) {

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

    private List<NamedObject> dropListObjects(
            List<NamedObject> masterObjects, List<NamedObject> compareObjects, int type) {

        List<NamedObject> dropObjects = new ArrayList<>();
        List<String> compareObjectsNames = compareObjects.stream().map(Named::getName).collect(Collectors.toList());

        panel.recreateProgressBar(
                "ExtractingForDrop", NamedObject.META_TYPES[type],
                masterObjects.size()
        );

        for (NamedObject databaseObject : masterObjects) {

            if (panel.isCanceled())
                break;

            if (!compareObjectsNames.contains(databaseObject.getName()))
                dropObjects.add(databaseObject);

            panel.incrementProgressBarValue();
        }

        return dropObjects;
    }

    private Map<NamedObject, NamedObject> alterListObjects(
            List<NamedObject> masterObjects, List<NamedObject> compareObjects, int type) {

        Map<NamedObject, NamedObject> alterObjects = new HashMap<>();

        panel.recreateProgressBar(
                "ExtractingForAlter", NamedObject.META_TYPES[type],
                masterObjects.size() * compareObjects.size()
        );

        for (NamedObject compareObject : compareObjects) {

            if (panel.isCanceled())
                break;

            for (NamedObject masterObject : masterObjects) {

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

            constraintsList = constraintsList.concat("\t" + databaseObject.getName() + "." + cc.getName() + "\n");
            constraintsToCreate.add(new org.executequery.gui.browser.ColumnConstraint(false, cc));
        }
    }

    private void dropListConstraints(int type) {

        if ((type != TABLE && type != GLOBAL_TEMPORARY) || panel.isCanceled())
            return;

        List<NamedObject> masterConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(masterConnection).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        panel.recreateProgressBar(
                "ExtractingConstraintsForDrop", null,
                masterConnectionObjectsList.size()
        );

        for (NamedObject databaseObject : masterConnectionObjectsList) {

            if (panel.isCanceled())
                break;

            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    compareConnection, type, databaseObject.getName()) == null) {

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
                getDefaultDatabaseHostFromConnection(masterConnection).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
        List<NamedObject> compareConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection).
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
                    if (!((AbstractDatabaseObject) masterObject).getCompareAlterSQL((AbstractDatabaseObject) compareObject).contains(SQLUtils.THERE_ARE_NO_CHANGES))
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

            if ((masterCC.getType() == PRIMARY_KEY && TABLE_CONSTRAINTS_NEED[0]) ||
                    (masterCC.getType() == FOREIGN_KEY && TABLE_CONSTRAINTS_NEED[1]) ||
                    (masterCC.getType() == UNIQUE_KEY && TABLE_CONSTRAINTS_NEED[2]) ||
                    (masterCC.getType() == CHECK_KEY && TABLE_CONSTRAINTS_NEED[3]))
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

            if ((comparingCC.getType() == PRIMARY_KEY && TABLE_CONSTRAINTS_NEED[0]) ||
                    (comparingCC.getType() == FOREIGN_KEY && TABLE_CONSTRAINTS_NEED[1]) ||
                    (comparingCC.getType() == UNIQUE_KEY && TABLE_CONSTRAINTS_NEED[2]) ||
                    (comparingCC.getType() == CHECK_KEY && TABLE_CONSTRAINTS_NEED[3]))
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
        if (panel.isErd()) {
            listCD = tempTable.getListCD();
        } else {
            for (int i = 0; i < tempTable.getColumnCount(); i++)
                listCD.add(new ColumnData(tempTable.getHost().getDatabaseConnection(), tempTable.getColumns().get(i), false));
        }
        for (ColumnData cd : listCD) {
            if (!MiscUtils.isNull(cd.getComputedBy())) {
                computedFieldsList = computedFieldsList.concat("\t" + tempTable.getName() + "." + cd.getColumnName() + "\n");
                computedFields.add(cd);
            }
        }
    }

    private List<NamedObject> sortObjectsByDependency(List<NamedObject> objectsList) {

        if (objectsList.isEmpty())
            return null;

        String templateQuery = "SELECT DISTINCT D.RDB$DEPENDENT_NAME FROM RDB$DEPENDENCIES D " +
                "WHERE D.RDB$DEPENDENT_TYPE = D.RDB$DEPENDED_ON_TYPE AND D.RDB$DEPENDED_ON_NAME = ?";

        DefaultStatementExecutor executor =
                new DefaultStatementExecutor(compareConnection, true);

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
                    "Error while comparing objects dependencies:\n" + e.getMessage(), e, this.getClass());
            Log.error(e);
        }

        return objectsList;
    }

    private void addStubsToScript(int type, List<NamedObject> stubsList, int insertIndex) {

        if (stubsList == null || stubsList.isEmpty())
            return;

        String header = MessageFormat.format(
                "\n/* ----- Creating {0} stubs ----- */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(insertIndex++, header);

        panel.recreateProgressBar(
                "CreatingStubs", NamedObject.META_TYPES[type],
                stubsList.size()
        );

        for (NamedObject obj : stubsList) {
            script.add(insertIndex++, "\n/* " + obj.getName() + " (STUB) */");
            script.add(insertIndex++, "\n" + SQLUtils.generateCreateDefaultStub(obj));
            panel.incrementProgressBarValue();
        }

    }

    private List<NamedObject> getObjects(DatabaseConnection connection, int type) {
        return ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(connection).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);
    }

    // ---

    public String getConstraintsList() {
        return constraintsList;
    }

    public String getComputedFieldsList() {
        return computedFieldsList;
    }

    public ArrayList<String> getScript() {

        if (stubsInsertIndex > 0 && stubsOnCreate != null) {
            for (Integer key : stubsOnCreate.keySet())
                addStubsToScript(key, stubsOnCreate.get(key), stubsInsertIndex);

            stubsOnCreate = null;
        }

        if (stubsInsertIndex > 0 && stubsOnAlter != null) {
            for (Integer key : stubsOnAlter.keySet())
                addStubsToScript(key, stubsOnAlter.get(key), stubsInsertIndex);

            stubsOnAlter = null;
        }

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

    public DatabaseConnection getMasterConnection() {
        return masterConnection;
    }

    public DatabaseConnection getCompareConnection() {
        return compareConnection;
    }

    public StatementExecutor getMasterExecutor() {
        return masterExecutor;
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
        script.clear();
    }

}

