package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Index {
    public Index(Comparer comp) {
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

    public final String collect = "select rdb$indices.rdb$index_name\n"
            + "from rdb$indices\n"
            + "left outer join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$indices.rdb$index_name\n"
            + "where rdb$indices.rdb$system_flag = 0  and\n"
            + "      rdb$relation_constraints.rdb$index_name is null";

    private String query = "";

    public ArrayList<String> indicesToFill = new ArrayList<String>();

    public ArrayList<String> getInfo(StatementExecutor con, String index) {
        ArrayList<String> info = new ArrayList<>();

        query = "select rdb$indices.rdb$relation_name,\n" + //1
                "       rdb$indices.rdb$index_inactive,\n" + //2
                "       rdb$indices.rdb$unique_flag,\n" + //3
                "       rdb$indices.rdb$index_type,\n" + //4
                "       rdb$indices.rdb$expression_source\n" + //5
                "from rdb$indices\n"
                + "where rdb$indices.rdb$index_name = '" + index + "'";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();


            while (rs.next()) {
                info.add(rs.getString(1).trim());
                info.add(replaceCode.noNull(rs.getString(2)).trim().equals("1") ? "inactive" : "active");
                info.add(replaceCode.noNull(rs.getString(3)).trim().equals("1") ? "unique" : "");
                info.add(replaceCode.noNull(rs.getString(4)).trim().equals("1") ? "descending" : "");
                info.add(replaceCode.noNull(rs.getString(5)).trim());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("index 53: " + e + query);
        }

        query = "select rdb$index_segments.rdb$field_name\n"
                + "from rdb$index_segments\n"
                + "where rdb$index_segments.rdb$index_name = '" + index + "'\n"
                + "order by rdb$index_segments.rdb$field_position";

        String indexFields = "";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();


            while (rs.next()) {
                indexFields = indexFields + "\"" + rs.getString(1).trim() + "\", ";
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("index 75: " + e + query);
        }

        if (!indexFields.equals("")) {
            indexFields = indexFields.substring(0, indexFields.lastIndexOf(", "));
        }

        info.add(indexFields);

        return info;
    }

    public String create(String index) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<>();

        info = getInfo(firstConnection, index);

        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$indices.rdb$relation_name,\n"
                + "       rdb$index_segments.rdb$field_name\n"
                + "from rdb$indices\n"
                + "inner join rdb$index_segments on rdb$index_segments.rdb$index_name = rdb$indices.rdb$index_name\n"
                + "where rdb$indices.rdb$index_name = '" + index + "'\n"
                + "order by rdb$index_segments.rdb$field_position";

        try {
            ResultSet rs = firstConnection.execute(query, true).getResultSet();

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                line.add(rs.getString(1).trim());
                line.add(rs.getString(2).trim());

                dep.add(line);
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("index 120: " + e + query);
        }

        for (ArrayList<String> d : dep) {
            scriptPart = scriptPart + dependencies.addFields(d.get(0), d.get(1));
        }

        scriptPart = scriptPart + "create";

        if (!info.get(2).equals("")) {
            scriptPart = scriptPart + " " + info.get(2);
        }

        if (!info.get(3).equals("")) {
            scriptPart = scriptPart + " " + info.get(3);
        }

        scriptPart = scriptPart + " index \"" + index + "\" on " + info.get(0);

        if (!info.get(5).equals("")) {
            scriptPart = scriptPart + " (" + info.get(5) + ");\n\n";
        }

        if (!info.get(4).equals("")) {
            scriptPart = scriptPart + " computed by " + info.get(4) + ";\n\n";
        }

        if (info.get(1).equals("inactive")) {
            scriptPart = scriptPart + "alter index \"" + index + "\" " + info.get(1) + ";\n\n";
        }

        return scriptPart;
    }

    public String alter(String index) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<String>();
        ArrayList<String> info2 = new ArrayList<String>();

        info = getInfo(firstConnection, index);
        info2 = getInfo(secondConnection, index);

        if (!info.get(0).equals(info2.get(0)) || !info.get(2).equals(info2.get(2))
                || !info.get(3).equals(info2.get(3))
                //|| !info.get(4).equals(info2.get(4))
                || !replaceCode.compare_wo_r(info.get(4), (info2.get(4)))
                || !info.get(5).equals(info2.get(5))) {
            scriptPart = drop(index);
            scriptPart = scriptPart + create(index);

        } else if (!info.get(1).equals(info2.get(1))) {
            scriptPart = "alter index \"" + index + "\" " + info.get(1) + ";\n\n";
        }

        return scriptPart;
    }

    public String drop(String index) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("index " + index)) {

            scriptPart = "drop index \"" + index + "\";\n\n";

            comparer.droppedObjects.add("index " + index);
        }

        return scriptPart;
    }
}

