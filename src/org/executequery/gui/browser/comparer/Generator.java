package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Generator {

    public Generator(Comparer comp) {
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
    public String collect = "select rdb$generators.rdb$generator_name\n"
            + "from rdb$generators\n"
            + "where rdb$generators.rdb$system_flag = 0";

    private String query = "";

    public String getInfo(StatementExecutor con, String gen) {
        String info = "";

        query = "select gen_id(\"" + gen + "\", 0)\n"
                + "from rdb$database";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {

                info = info + rs.getString(1).trim();
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("generator 43: " + e + query);
        }

        return info;
    }

    public String create(String generator) {
        String scriptPart = "";
        if (!comparer.createdObjects.contains("generator " + generator)) {

            String info;
            info = getInfo(firstConnection, generator);

            scriptPart = "create sequence \"" + generator + "\";\n\n";

            scriptPart = scriptPart + "alter sequence \"" + generator + "\" restart with " + info + ";\n\n";
            comparer.createdObjects.add("generator " + generator);
        }
        return scriptPart;
    }

    public String alter(String generator) {
        String scriptPart = "";

        String info = "";
        String info2 = "";

        info = getInfo(firstConnection, generator);
        info2 = getInfo(secondConnection, generator);

        if (!info.equals(info2)) {
            scriptPart = "alter sequence \"" + generator + "\" restart with " + info + ";\n\n";
        }

        return scriptPart;
    }

    public String drop(String generator) {
        String scriptPart = "";

        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$dependencies.rdb$dependent_type,\n"
                + "     rdb$dependencies.rdb$dependent_name\n"
                + "from rdb$dependencies\n"
                + "where rdb$dependencies.rdb$depended_on_name = '" + generator + "'";

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
            System.out.println("generator 107: " + e + query);
        }

        for (ArrayList<String> d : dep) {
            scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
        }

        scriptPart = scriptPart + "drop sequence \"" + generator + "\";\n\n";

        return scriptPart;
    }
}
