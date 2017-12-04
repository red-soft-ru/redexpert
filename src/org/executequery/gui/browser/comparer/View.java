package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class View {

    public View(Comparer comp) {
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
    public final String collect = "select rdb$relations.rdb$relation_name\n"
            + "from rdb$relations\n"
            + "where rdb$system_flag = 0 and rdb$relation_type = 1\n";

    private String query = "";

    public ArrayList<String> v_fill = new ArrayList<String>();
    public ArrayList<String> v_create = new ArrayList<String>();

    public ArrayList<String> getInfo(StatementExecutor con, String view) {
        ArrayList<String> info = new ArrayList<>();
        String columns = "";
        int num = 0;

        query = "select rdb$relation_fields.rdb$field_name\n"
                + "from rdb$relations\n"
                + "inner join rdb$relation_fields on rdb$relation_fields.rdb$relation_name = rdb$relations.rdb$relation_name\n"
                + "where rdb$relations.rdb$relation_name = '" + view + "' and rdb$relations.rdb$system_flag = 0\n"
                + "order by rdb$relation_fields.rdb$field_position";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();


            while (rs.next()) {
                columns = columns + "\"" + rs.getString(1).trim() + "\", ";
                num++;
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("view 47" + e + query);
        }

        columns = columns.substring(0, columns.lastIndexOf(", "));
        info.add(columns);

        query = "select rdb$relations.rdb$view_source\n"
                + "from rdb$relations\n"
                + "where rdb$relations.rdb$relation_name = '" + view + "' and rdb$relations.rdb$system_flag = 0";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();


            while (rs.next()) {

                info.add(rs.getString(1).trim());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("view 70" + e + query);
        }

        info.add(Integer.toString(num));

        return info;
    }

    public String create(String view) {
        String scriptPart = "";
        if (!comparer.createdObjects.contains("view " + view)) {
            ArrayList<ArrayList<String>> depTables = new ArrayList<>();

            query = "select rdb$dependencies.rdb$field_name,\n"
                    + "       rdb$dependencies.rdb$depended_on_name\n"
                    + "from rdb$dependencies\n"
                    + "where rdb$dependencies.rdb$dependent_name = '" + view + "'\n"
                    + "and rdb$dependencies.rdb$depended_on_type = 0\n"
                    + "and rdb$dependencies.rdb$field_name is not null";

            try {
                ResultSet rs = firstConnection.execute(query, true).getResultSet();


                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim());
                    line.add(rs.getString(2).trim());

                    depTables.add(line);
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("view 105" + e + query);
            }

            for (ArrayList<String> dT : depTables) {
                scriptPart = scriptPart + dependencies.addFields(dT.get(1), dT.get(0));
            }

            ArrayList<String> depViews = new ArrayList<String>();

            query = "select distinct rdb$dependencies.rdb$depended_on_name\n"
                    + "from rdb$relations\n"
                    + "inner join rdb$dependencies on rdb$dependencies.rdb$dependent_name = rdb$relations.rdb$relation_name\n"
                    + "where rdb$system_flag = 0 and rdb$relation_type = 1\n"
                    + "and rdb$dependencies.rdb$dependent_name = '" + view + "'\n"
                    + "and rdb$dependencies.rdb$depended_on_type = 1";

            try {
                ResultSet rs = firstConnection.execute(query, true).getResultSet();


                while (rs.next()) {
                    depViews.add(rs.getString(1).trim());
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("view 133" + e + query);
            }

            for (String dV : depViews) {
                scriptPart = scriptPart + create(dV);
            }

            ArrayList<String> info = new ArrayList<>();

            info = getInfo(firstConnection, view);

            scriptPart = scriptPart + "create or alter view \"" + view + "\" (";

            scriptPart = scriptPart + info.get(0) + ")\n";

            //scriptPart = scriptPart + "as\n" + info.get(1) + ";\n\n";
            scriptPart = scriptPart + "as\n";

            scriptPart = scriptPart + "select\n";

            for (int i = 0; i < Integer.parseInt(info.get(2)); i++) {
                scriptPart = scriptPart + "    1,\n";
            }

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(",\n")) + "\nfrom rdb$database;\n\n";

            v_fill.add(view);

            comparer.createdObjects.add("view " + view);
        }

        return scriptPart;
    }

    public String alter(String view) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<>();
        ArrayList<String> info2 = new ArrayList<>();

        info = getInfo(firstConnection, view);
        info2 = getInfo(secondConnection, view);

        //if (!info.equals(info2)) {
        if (!info.get(0).equals(info2.get(0)) ||
                !replaceCode.compare_wo_r(info.get(1), info2.get(1))) {
            //scriptPart = scriptPart + drop(view);
            scriptPart = scriptPart + create(view);
        }

        return scriptPart;
    }

    public String drop(String view) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("view " + view)) {

            ArrayList<ArrayList<String>> dep = new ArrayList<>();

            query = "select rdb$dependencies.rdb$dependent_type,\n"
                    + "     rdb$dependencies.rdb$dependent_name\n"
                    + "from rdb$dependencies\n"
                    + "where rdb$dependencies.rdb$depended_on_name = '" + view + "'";

            try {
                ResultSet rs = secondConnection.execute(query, true).getResultSet();


                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim());
                    line.add(rs.getString(2).trim());

                    dep.add(line);
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("view 202" + e + query);
            }

            for (ArrayList<String> d : dep) {
                scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
            }

            if (!comparer.droppedObjects.contains("view " + view)) {
                scriptPart = scriptPart + "drop view \"" + view + "\"";

                scriptPart = scriptPart + ";\n\n";

                comparer.droppedObjects.add("view " + view);
            }

            comparer.droppedObjects.add("view " + view);
        }

        return scriptPart;
    }

    public String fill(String view) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<>();

        info = getInfo(firstConnection, view);

        scriptPart = scriptPart + "create or alter view \"" + view + "\" (";

        scriptPart = scriptPart + info.get(0) + ")\n";

        scriptPart = scriptPart + "as\n" + info.get(1) + ";\n\n";

        return scriptPart;
    }
}

