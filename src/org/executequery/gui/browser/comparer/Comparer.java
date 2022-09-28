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

        if (permission) {

            List<NamedObject> createObjects = createListObjects(type);

            String header = MessageFormat.format(
                    "/* Creating {0} */\n\n",
                    Bundles.getEn(NamedObject.class, NamedObject.META_TYPES_FOR_BUNDLE[type]));
            script.add(header);

            for (NamedObject obj : createObjects) {
                script.add(((AbstractDatabaseObject) obj).getCreateSQL() + "\n");
                lists += "\t" + obj.getName() + "\n";
            }

        }

    }

    private List<NamedObject> createListObjects(int type) {

        List<NamedObject> firstConnectionObjectsList = ConnectionsTreePanel.getPanelFromBrowser().
                getDefaultDatabaseHostFromConnection(compareConnection.getDatabaseConnection()).
                getDatabaseObjectsForMetaTag(NamedObject.META_TYPES[type]);

        List<NamedObject> createObjects = new ArrayList<>();

        for (NamedObject databaseObject : firstConnectionObjectsList) {
            if (ConnectionsTreePanel.getNamedObjectFromHost(
                    masterConnection.getDatabaseConnection(), type, databaseObject.getName()) == null) {

                createObjects.add(databaseObject);
            }
        }

        return createObjects;
    }

    // создать список создаваемых объектов
    private ArrayList<String> createList(String query) {
        ArrayList<String> second = new ArrayList<>();
        ArrayList<String> create = new ArrayList<>();

        try {
            ResultSet rs = masterConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                second.add(rs.getString(1).trim());
            }

            rs.close();
            masterConnection.releaseResources();
        } catch (java.sql.SQLException e) {
            Log.error("Comparer 42: " + e);
        }

        try {
            ResultSet rs = compareConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                String obj = rs.getString(1).trim();
                if (!second.contains(obj)) {
                    create.add(obj);
                }
            }
            rs.close();
            compareConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("Comparer 57: " + e);
        }

        return create;
    }

    // создать список удаляемых объектов
    private ArrayList<String> dropList(String query) {
        ArrayList<String> first = new ArrayList<>();
        ArrayList<String> drop = new ArrayList<>();
        try {
            ResultSet rs = compareConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                first.add(rs.getString(1).trim());
            }
        } catch (java.sql.SQLException e) {
            Log.error("Comparer 78: " + e);
        } finally {
            compareConnection.releaseResources();
        }
        try {
            ResultSet rs = masterConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                String obj = rs.getString(1).trim();
                if (!first.contains(obj)) {
                    drop.add(obj);
                }
            }

        } catch (java.sql.SQLException e) {
            System.out.println("Comparer 93: " + e);
        } finally {
            masterConnection.releaseResources();
        }

        return drop;
    }

    // создать список изменяемых объектов
    private ArrayList<String> alterList(String query) {
        ArrayList<String> second = new ArrayList<>();
        ArrayList<String> alter = new ArrayList<>();
        try {
            ResultSet rs = masterConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                second.add(rs.getString(1).trim());
            }
        } catch (java.sql.SQLException e) {
            Log.error("Comparer 114: " + e);
        } finally {
            masterConnection.releaseResources();
        }
        try {
            ResultSet rs = compareConnection.execute(query, true).getResultSet();
            while (rs.next()) {
                String obj = rs.getString(1).trim();
                if (second.contains(obj)) {
                    alter.add(obj);
                }
            }


        } catch (java.sql.SQLException e) {
            System.out.println("Comparer 129: " + e);
        } finally {
            compareConnection.releaseResources();
        }

        return alter;
    }

    public void createDomains(boolean permission) {
        if (permission) {
            ArrayList<String> cDomains = new ArrayList<>();
            cDomains = createList(domain.collect);

            script.add("/* Creating Domains */\n\n");
            for (String d : cDomains) {

                script.add(domain.create(d));
                lists = lists + "   " + d + "\n";
            }
        }
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

    public void dropDomains(boolean permission) {
        if (permission) {
            ArrayList<String> dDomains = new ArrayList<>();
            dDomains = dropList(domain.collect);

            script.add("/* Dropping Domains */\n\n");
            for (String d : dDomains) {

                script.add(domain.drop(d));
                lists = lists + "   " + d + "\n";
            }
        }
    }

    public void createExceptions(boolean permission) {
        if (permission) {
            ArrayList<String> cExc = new ArrayList<>();
            cExc = createList(exception.collect);

            script.add("/* Creating Exceptions */\n\n");
            for (String e : cExc) {

                script.add(exception.create(e));
                lists = lists + "   " + e + "\n";
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

    public void dropExceptions(boolean permission) {
        if (permission) {
            ArrayList<String> dExc = new ArrayList<>();
            dExc = dropList(exception.collect);

            script.add("/* Dropping Exceptions */\n\n");
            for (String e : dExc) {

                script.add(exception.drop(e));
                lists = lists + "   " + e + "\n";
            }
        }
    }

    public void createUDFs(boolean permission) {
        if (permission) {
            ArrayList<String> cUDFs = new ArrayList<>();
            cUDFs = createList(udf.collect);

            script.add("/* Creating UDFs */\n\n");
            for (String u : cUDFs) {

                script.add(udf.create(u));
                lists = lists + "   " + u + "\n";
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

    public void dropUDFs(boolean permission) {
        if (permission) {
            ArrayList<String> dUDFs = new ArrayList<>();
            dUDFs = dropList(udf.collect);

            script.add("/* Dropping UDFs */\n\n");
            for (String u : dUDFs) {

                script.add(udf.drop(u));
                lists = lists + "   " + u + "\n";
            }
        }
    }

    public void createGenerators(boolean permission) {
        if (permission) {
            ArrayList<String> cGen = new ArrayList<>();
            cGen = createList(generator.collect);

            script.add("/* Creating Generators */\n\n");
            for (String g : cGen) {

                script.add(generator.create(g));
                lists = lists + "   " + g + "\n";
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

    public void dropGenerators(boolean permission) {
        if (permission) {
            ArrayList<String> dGen = new ArrayList<>();
            dGen = dropList(generator.collect);

            script.add("/* Dropping Generators */\n\n");
            for (String g : dGen) {

                script.add(generator.drop(g));
                lists = lists + "   " + g + "\n";
            }
        }
    }

    public void createRoles(boolean permission) {
        if (permission) {
            ArrayList<String> cRoles = new ArrayList<>();
            cRoles = createList(role.collect);

            script.add("/* Creating Roles */\n\n");
            for (String r : cRoles) {

                script.add(role.create(r));
                lists = lists + "   " + r + "\n";
            }
        }
    }

    public void dropRoles(boolean permission) {
        if (permission) {
            ArrayList<String> dRoles = new ArrayList<>();
            dRoles = dropList(role.collect);

            script.add("/* Dropping Roles */\n\n");
            for (String r : dRoles) {

                script.add(role.drop(r));
                lists = lists + "   " + r + "\n";
            }
        }
    }

    public void createTriggers(boolean permission) {
        if (permission) {
            ArrayList<String> cTriggers = new ArrayList<>();
            cTriggers = createList(trigger.collect);

            script.add("/* Creating Triggers */\n\n");
            for (String t : cTriggers) {

                script.add(trigger.create(t));
                lists = lists + "   " + t + "\n";
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

    public void dropTriggers(boolean permission) {
        if (permission) {
            ArrayList<String> dTrigger = new ArrayList<>();
            dTrigger = dropList(trigger.collect);

            script.add("/* Dropping Triggers */\n\n");
            for (String t : dTrigger) {

                script.add(trigger.drop(t));
                lists = lists + "   " + t + "\n";
            }
        }
    }

    public void createTables(boolean permission) {
        if (permission) {
            ArrayList<String> cTables = new ArrayList<>();
            cTables = createList(table.collect);

            script.add("/* Creating Tables */\n\n");
            for (String t : cTables) {

                script.add(table.create(t));
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

    public void dropTables(boolean permission) {
        if (permission) {
            ArrayList<String> dTables = new ArrayList<>();
            dTables = dropList(table.collect);

            script.add("/* Dropping Tables */\n\n");
            for (String t : dTables) {

                script.add(table.drop(t));
                lists = lists + "   " + t + "\n";
            }
        }
    }

    public void createProcedures(boolean permission) {
        if (permission) {
            ArrayList<String> cProcedures = new ArrayList<>();
            cProcedures = createList(procedure.collect);

            script.add("/* Creating Procedures */\n\n");
            for (String p : cProcedures) {

                script.add(procedure.create(p));
                lists = lists + "   " + p + "\n";
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

    public void dropProcedures(boolean permission) {
        if (permission) {
            ArrayList<String> dProcedures = new ArrayList<>();
            dProcedures = dropList(procedure.collect);

            script.add("/* Dropping Procedures */\n\n");
            for (String p : dProcedures) {

                script.add(procedure.empty(p));
            }

            for (String p : dProcedures) {

                script.add(procedure.drop(p));
                lists = lists + "   " + p + "\n";
            }
        }
    }

    public void createViews(boolean permission) {
        if (permission) {
            ArrayList<String> cViews = new ArrayList<>();
            cViews = createList(view.collect);

            script.add("/* Creating Views */\n\n");
            for (String v : cViews) {

                script.add(view.create(v));
                lists = lists + "   " + v + "\n";
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

    public void dropViews(boolean permission) {
        if (permission) {
            ArrayList<String> dViews = new ArrayList<>();
            dViews = dropList(view.collect);

            script.add("/* Dropping Views */\n\n");
            for (String v : dViews) {

                script.add(view.drop(v));
                lists = lists + "   " + v + "\n";
            }
        }
    }

    public void createIndices(boolean permission) {
        if (permission) {
            ArrayList<String> cIndices = new ArrayList<>();
            cIndices = createList(index.collect);

            script.add("/* Creating Indices */\n\n");
            for (String i : cIndices) {

                script.add(index.create(i));
                lists = lists + "   " + i + "\n";
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

    public void dropIndices(boolean permission) {
        if (permission) {
            ArrayList<String> dIndices = new ArrayList<>();
            dIndices = dropList(index.collect);

            script.add("/* Dropping Indices */\n\n");
            for (String i : dIndices) {

                script.add(index.drop(i));
                lists = lists + "   " + i + "\n";
            }
        }
    }

    public void createChecks(boolean permission) {
        if (permission) {
            ArrayList<String> cChecks = new ArrayList<>();
            cChecks = createList(constraint.collect_check);

            script.add("/* Creating Checks */\n\n");
            for (String c : cChecks) {

                script.add(constraint.createCheck(c));
                lists = lists + "   " + c + "\n";
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

    public void dropChecks(boolean permission) {
        if (permission) {
            ArrayList<String> dChecks = new ArrayList<>();
            dChecks = dropList(constraint.collect_check);

            script.add("/* Dropping Checks */\n\n");
            for (String c : dChecks) {

                script.add(constraint.dropCheck(c));
                lists = lists + "   " + c + "\n";
            }
        }
    }

    public void createUniques(boolean permission) {
        if (permission) {
            ArrayList<String> cUniques = new ArrayList<>();
            cUniques = createList(constraint.collect_unique);

            script.add("/* Creating Uniques */\n\n");
            for (String u : cUniques) {

                script.add(constraint.createUnique(u));
                lists = lists + "   " + u + "\n";
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

    public void dropUniques(boolean permission) {
        if (permission) {
            ArrayList<String> dUniques = new ArrayList<>();
            dUniques = dropList(constraint.collect_unique);

            script.add("/* Dropping Uniques */\n\n");
            for (String u : dUniques) {

                script.add(constraint.dropUnique(u));
                lists = lists + "   " + u + "\n";
            }
        }
    }

    public void createFKs(boolean permission) {
        if (permission) {
            ArrayList<String> cFKs = new ArrayList<>();
            cFKs = createList(constraint.collect_fk);

            script.add("/* Creating Foreign keys */\n\n");
            for (String f : cFKs) {

                script.add(constraint.createFK(f));
                lists = lists + "   " + f + "\n";
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

    public void dropFKs(boolean permission) {
        if (permission) {
            ArrayList<String> dFKs = new ArrayList<>();
            dFKs = dropList(constraint.collect_fk);

            script.add("/* Dropping Foreign keys */\n\n");
            for (String f : dFKs) {

                script.add(constraint.dropFK(f));
                lists = lists + "   " + f + "\n";
            }
        }
    }

    public void createPKs(boolean permission) {
        if (permission) {
            ArrayList<String> cPKs = new ArrayList<>();
            cPKs = createList(constraint.collect_pk);

            script.add("/* Creating Primary keys */\n\n");
            for (String p : cPKs) {

                script.add(constraint.createPK(p));
                lists = lists + "   " + p + "\n";
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

    public void dropPKs(boolean permission) {
        if (permission) {
            ArrayList<String> dPKs = new ArrayList<>();
            dPKs = dropList(constraint.collect_pk);

            script.add("/* Dropping Primary keys */\n\n");
            for (String p : dPKs) {

                script.add(constraint.dropPK(p));
                lists = lists + "   " + p + "\n";
            }
        }
    }

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
    }

}

