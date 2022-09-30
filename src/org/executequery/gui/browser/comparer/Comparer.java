package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databasemediators.spi.StatementExecutor;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.AbstractDatabaseObject;
import org.executequery.gui.browser.ConnectionsTreePanel;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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

    public void createObjects(boolean permission, int type) {

        if (!permission)
            return;

        List<NamedObject> createObjects = createListObjects(type);

        String header = MessageFormat.format(
                "\n/* Creating {0} */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : createObjects) {
            script.add("\n" + ((AbstractDatabaseObject) obj).getCreateSQL());
            lists += "\t" + obj.getName() + "\n";
        }

    }

    public void dropObjects(boolean permission, int type) {

        if (!permission)
            return;

        List<NamedObject> dropObjects = dropListObjects(type);

        String header = MessageFormat.format(
                "\n/* Dropping {0} */\n",
                Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
        script.add(header);

        for (NamedObject obj : dropObjects) {
            script.add("\n" + ((AbstractDatabaseObject) obj).getDropSQL());
            lists += "\t" + obj.getName() + "\n";
        }

    }

    public void alterObjects(boolean permission, int type) {

        if (!permission)
            return;

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

    private List<NamedObject>[] alterListObjects(int type) {
        return null;
    }

    // создать список изменяемых объектов
    private ArrayList<String> alterList(String query) {

        ArrayList<String> second = new ArrayList<>();
        ArrayList<String> alter = new ArrayList<>();

        try(ResultSet rs = masterConnection.execute(query, true).getResultSet()) {

            while (rs.next())
                second.add(rs.getString(1).trim());

        } catch (java.sql.SQLException e) {
            Log.error("ComparerError 114", e);

        } finally {
            masterConnection.releaseResources();
        }

        try(ResultSet rs = compareConnection.execute(query, true).getResultSet()) {

            while (rs.next()) {
                String obj = rs.getString(1).trim();
                if (second.contains(obj))
                    alter.add(obj);
            }

        } catch (java.sql.SQLException e) {
            Log.error("ComparerError 129", e);

        } finally {
            compareConnection.releaseResources();
        }

        return alter;
    }

    public void alterDomains(boolean permission) {
        if (permission) {
            ArrayList<String> aDomains = new ArrayList<>();
            aDomains = alterList(domain.collect);

            script.add("/* Altering Domains */\n\n");
            for (String d : aDomains) {


                String line = domain.alter(d);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + d + "\n";
            }
        }
    }

    public void alterExceptions(boolean permission) {
        if (permission) {
            ArrayList<String> aExc = new ArrayList<>();
            aExc = alterList(exception.collect);

            script.add("/* Altering Exceptions */\n\n");
            for (String e : aExc) {

                String line = exception.alter(e);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + e + "\n";
            }
        }
    }

    public void alterUDFs(boolean permission) {
        if (permission) {
            ArrayList<String> aUDFs = new ArrayList<>();
            aUDFs = alterList(udf.collect);

            script.add("/* Altering UDFs */\n\n");
            for (String u : aUDFs) {

                String line = udf.alter(u);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + u + "\n";
            }
        }
    }

    public void alterGenerators(boolean permission) {
        if (permission) {
            ArrayList<String> aGen = new ArrayList<>();
            aGen = alterList(generator.collect);

            script.add("/* Altering Generators */\n\n");
            for (String g : aGen) {

                String line = generator.alter(g);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + g + "\n";
            }
        }
    }

    public void alterTriggers(boolean permission) {
        if (permission) {
            ArrayList<String> aTriggers = new ArrayList<>();
            aTriggers = alterList(trigger.collect);

            script.add("/* Altering Triggers */\n\n");
            for (String t : aTriggers) {

                String line = trigger.alter(t);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + t + "\n";
            }
        }
    }

    public void alterTables(boolean permission) {
        if (permission) {
            ArrayList<String> aTables = new ArrayList<>();
            aTables = alterList(table.collect);

            script.add("/* Alter Tables */\n\n");
            for (String t : aTables) {

                String line = table.alter(t);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + t + "\n";
            }
        }
    }

    public void alterProcedures(boolean permission) {
        if (permission) {
            ArrayList<String> aProcedures = new ArrayList<>();
            ArrayList<String> fProcedures = new ArrayList<>();

            aProcedures = alterList(procedure.collect);

            script.add("/* Altering Procedures */\n\n");
            for (String p : aProcedures) {

                String line = procedure.alter(p);
                if (!line.equals("")) {
                    script.add(line);
                    fProcedures.add(p);
                }
                lists = lists + "   " + p + "\n";
            }

            for (String p : fProcedures) {

                script.add(procedure.fill(p));
            }
        }
    }

    public void alterViews(boolean permission) {
        if (permission) {
            ArrayList<String> aViews = new ArrayList<>();
            aViews = alterList(view.collect);

            script.add("/* Altering Views */\n\n");
            for (String v : aViews) {

                String line = view.alter(v);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + v + "\n";
            }
        }
    }

    public void alterIndices(boolean permission) {
        if (permission) {
            ArrayList<String> aIndices = new ArrayList<>();
            aIndices = alterList(index.collect);

            script.add("/* Altering Indices */\n\n");
            for (String i : aIndices) {

                String line = index.alter(i);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + i + "\n";
            }
        }
    }

    public void alterChecks(boolean permission) {
        if (permission) {
            ArrayList<String> aChecks = new ArrayList<>();
            aChecks = alterList(constraint.collect_check);

            script.add("/* Altering Checks */\n\n");
            for (String c : aChecks) {

                String line = constraint.alterCheck(c);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + c + "\n";
            }
        }
    }

    public void alterUniques(boolean permission) {
        if (permission) {
            ArrayList<String> aUniques = new ArrayList<>();
            aUniques = alterList(constraint.collect_unique);

            script.add("/* Altering Uniques */\n\n");
            for (String u : aUniques) {

                String line = constraint.alterUnique(u);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + u + "\n";
            }
        }
    }

    public void alterFKs(boolean permission) {
        if (permission) {
            ArrayList<String> aFKs = new ArrayList<>();
            aFKs = alterList(constraint.collect_fk);

            script.add("/* Altering Foreign keys */\n\n");
            for (String f : aFKs) {

                String line = constraint.alterFK(f);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + f + "\n";
            }
        }
    }

    public void alterPKs(boolean permission) {
        if (permission) {
            ArrayList<String> aPKs = new ArrayList<>();
            aPKs = alterList(constraint.collect_pk);

            script.add("/* Altering Primary keys */\n\n");
            for (String p : aPKs) {

                String line = constraint.alterPK(p);
                if (!line.equals("")) {
                    script.add(line);
                }
                lists = lists + "   " + p + "\n";
            }
        }
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

