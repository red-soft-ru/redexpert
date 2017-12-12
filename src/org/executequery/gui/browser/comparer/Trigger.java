package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Trigger {
    public Trigger(Comparer comp) {
        comparer = comp;
        init();
    }

    public void init() {
        firstConnection = comparer.firstConnection;
        secondConnection = comparer.secondConnection;
        dependencies = comparer.dependencies;
    }

    Dependencies dependencies;
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;
    public String collect = "select rdb$triggers.rdb$trigger_name\n"
            + "from rdb$triggers\n"
            + "where rdb$triggers.rdb$system_flag = 0";

    public ArrayList<String> triggerToFill = new ArrayList<String>();
    private String query = "";

    public ArrayList<String> getInfo(StatementExecutor con, String trigger) {
        ArrayList<String> info = new ArrayList<>();

        query = "select rdb$triggers.rdb$relation_name,\n"
                + "rdb$triggers.rdb$trigger_sequence,\n"
                + "rdb$triggers.rdb$trigger_type,\n"
                + "rdb$triggers.rdb$trigger_source,\n"
                + "rdb$triggers.rdb$trigger_inactive,\n"
                + "rdb$check_constraints.rdb$constraint_name\n"
                + "from rdb$triggers\n"
                + "left outer join rdb$check_constraints on rdb$check_constraints.rdb$trigger_name = rdb$triggers.rdb$trigger_name\n"
                + "where rdb$triggers.rdb$trigger_name = '" + trigger + "'";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();


            while (rs.next()) {
                if (!replaceCode.noNull(rs.getString(4)).equals("")) {
                    if (!rs.getString(4).trim().startsWith("CHECK")) {
                        info.add(replaceCode.noNull(rs.getString(1)).trim());
                        info.add(rs.getString(2).trim());
                        info.add(replaceCode.replaceTriggerType(rs.getString(3).trim()));
                        info.add(rs.getString(4).trim());
                        info.add(rs.getString(5).trim().equals("0") ? "active" : "inactive");
                    } else {
                        info.add(replaceCode.noNull(rs.getString(1)).trim());
                        info.add(rs.getString(6).trim());
                        info.add(rs.getString(4).trim());
                    }
                }
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("trigger 61" + e + query);
        }

        return info;
    }

    public String create(String trigger) {
        String scriptPart = "";

        ArrayList<ArrayList<String>> depTables = new ArrayList<>();
        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$dependencies.rdb$field_name,\n"
                + "       rdb$dependencies.rdb$depended_on_name,\n"
                + "       rdb$dependencies.rdb$depended_on_type\n"
                + "from rdb$dependencies\n"
                + "where rdb$dependencies.rdb$dependent_name = '" + trigger + "'\n";
        //+ "and rdb$dependencies.rdb$depended_on_type = 0\n"
        //+ "and rdb$dependencies.rdb$field_name is not null";

        try {
            ResultSet rs = firstConnection.execute(query, true).getResultSet();

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                if (rs.getString(3).trim().equals("0")
                        || rs.getString(3).trim().equals("1")) {
                    line.add(!replaceCode.noNull(rs.getString(1)).equals("") ? rs.getString(1).trim() : "");
                    line.add(rs.getString(2).trim());
                    depTables.add(line);
                } else {
                    line.add(rs.getString(2).trim());
                    line.add(rs.getString(3).trim());
                    dep.add(line);
                }
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("trigger 103" + e + query);
        }

        for (ArrayList<String> dT : depTables) {
            scriptPart = scriptPart + dependencies.addFields(dT.get(1), dT.get(0));
        }

        for (ArrayList<String> d : dep) {
            scriptPart = scriptPart + dependencies.addDependencies(d.get(1), d.get(0));
        }

        ArrayList<String> info = new ArrayList<>();

        info = getInfo(firstConnection, trigger);

        scriptPart = scriptPart + "set term ^;\n\n" + "create or alter trigger \"" + trigger + "\"";

        if (!info.get(0).equals("")) {
            scriptPart = scriptPart + " for \"" + info.get(0) + "\"";
        }

        scriptPart = scriptPart + "\n" + info.get(4) + " " + info.get(2) + " position " + info.get(1) + "\n"
                + info.get(3) + "\n^\n\n" + "set term ;^\n\n";

        return scriptPart;
    }

    public String alter(String trigger) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<String>();
        ArrayList<String> info2 = new ArrayList<String>();

        info = getInfo(firstConnection, trigger);
        info2 = getInfo(secondConnection, trigger);

        if (!info.get(0).equals(info2.get(0))
                || !info.get(1).equals(info2.get(1))
                || !info.get(2).equals(info2.get(2))
                || !replaceCode.compare_wo_r(info.get(3), info2.get(3))
                || !info.get(4).equals(info2.get(4))) {

            scriptPart = create(trigger);
            /*scriptPart = "set term ^;\n\n" + "create or alter trigger \"" + trigger + "\"";

             if (!info.get(0).equals("")) {
             scriptPart = scriptPart + " for \"" + info.get(0) + "\"";
             }

             scriptPart = scriptPart + "\n" + info.get(4) + " " + info.get(2) + " position " + info.get(1) + "\n"
             + info.get(3) + "\n^\n\n" + "set term ;^\n\n";*/
        }

        return scriptPart;
    }

    public String drop(String trigger) {
        String scriptPart = "";

        scriptPart = "drop trigger \"" + trigger + "\";\n\n";

        comparer.droppedObjects.add("trigger " + trigger);

        return scriptPart;
    }

    /*public  String deactivate(String trigger) {
     String scriptPart = "";

     ArrayList<String> info = new ArrayList<>();

     info = getInfo(secondConnection, trigger);

     info.set(4, "inactive");

     scriptPart = scriptPart + "set term ^;\n\n" + "create or alter trigger \"" + trigger + "\"";

     if (!info.get(0).equals("")) {
     scriptPart = scriptPart + " for \"" + info.get(0) + "\"";
     }

     scriptPart = scriptPart + "\n" + info.get(4) + " " + info.get(2) + " position " + info.get(1) + "\n"
     + info.get(3) + "\n^\n\n" + "set term ;^\n\n";

     return scriptPart;
     }*/
}

