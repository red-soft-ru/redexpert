package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Exception {
    public Exception (Comparer comp)
    {
        comparer=comp;
        init();
    }
    public void init()
    {
        firstConnection=comparer.firstConnection;
        secondConnection=comparer.secondConnection;
        dependencies = comparer.dependencies;
    }
    Dependencies dependencies;
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;

    public   String collect = "select rdb$exceptions.rdb$exception_name\n"
            + "from rdb$exceptions\n"
            + "where rdb$exceptions.rdb$system_flag = 0";

    private  String query = "";

    public  String getInfo(StatementExecutor con, String exc) {
        String info = "";

        // получить текст исключения
        query = "select rdb$exceptions.rdb$message\n"
                + "from rdb$exceptions\n"
                + "where rdb$exceptions.rdb$exception_name = '" + exc + "'";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();

            while (rs.next()) {

                info = info + rs.getString(1).trim();
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("exception 45: " + e + query);
        }

        return info;
    }

    public  String create(String exception) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("exception " + exception)) {
            String info = getInfo(firstConnection, exception);

            scriptPart = "create exception \"" + exception + "\" '" + info + "';\n\n";
            comparer.createdObjects.add("exception " + exception);
        }
        return scriptPart;
    }

    public  String alter(String exception) {
        String scriptPart = "";

        String info = "";
        String info2 = "";

        info = getInfo(firstConnection, exception);
        info2 = getInfo(secondConnection, exception);

        if (!info.equals(info2)) {
            scriptPart = "alter exception \"" + exception + "\" '" + info + "';\n\n";
        }

        return scriptPart;
    }

    public  String drop(String exception) {
        String scriptPart = "";
        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$dependencies.rdb$dependent_type,\n"
                + "       rdb$dependencies.rdb$dependent_name\n"
                + "from rdb$dependencies\n"
                + "where rdb$dependencies.rdb$depended_on_name = '" + exception + "'";

        try {
            ResultSet rs = secondConnection.execute(query,true).getResultSet();

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                line.add(rs.getString(1).trim());
                line.add(rs.getString(2).trim());

                dep.add(line);
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("exception 105: " + e + query);
        }

        for (ArrayList<String> d : dep) {
            scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
        }

        scriptPart = scriptPart + "drop exception \"" + exception + "\";\n\n";

        return scriptPart;
    }
}

