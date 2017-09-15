package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Random;

public class Table {
    public Table (Comparer comp)
    {
        comparer=comp;
        init();
    }
    public void init()
    {
        firstConnection=comparer.firstConnection;
        secondConnection=comparer.secondConnection;
        dependencies = comparer.dependencies;
        domain = comparer.domain;
        constraint = comparer.constraint;
        procedure = comparer.procedure;
        index = comparer.index;
    }
    Index index;
    Procedure procedure;
    Constraint constraint;
    Domain domain;
    Dependencies dependencies;
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;

    public  final String collect = "select rdb$relation_name\n"
            + "from rdb$relations\n"
            + "where rdb$system_flag = 0 and rdb$relation_type <> 1";

    public  ArrayList<ArrayList<String>> cf_fill = new ArrayList<>(); // 1 - таблица, 2 - поле

    private  String query = "";

    public  ArrayList<String> getFields(StatementExecutor con, String table) {
        ArrayList<String> infoFields = new ArrayList<>();

        query = "select rdb$relation_fields.rdb$field_name, rdb$relation_fields.rdb$field_name\n"
                + "from rdb$relation_fields\n"
                + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source\n"
                + "where rdb$relation_fields.rdb$relation_name = '" + table + "' and rdb$fields.rdb$computed_source is null\n"
                + "\n"
                + "union all\n"
                + "\n"
                + "select iif(rdb$dependencies.rdb$field_name is null,  '', rdb$dependencies.rdb$field_name),\n"
                + "       rdb$relation_fields.rdb$field_name\n"
                + "from rdb$relation_fields\n"
                + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source\n"
                + "left outer join rdb$dependencies on rdb$dependencies.rdb$dependent_name = rdb$fields.rdb$field_name\n"
                + "where rdb$relation_fields.rdb$relation_name = '" + table + "' and rdb$fields.rdb$computed_source is not null";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            ArrayList<ArrayList<String>> list = new ArrayList<>();

            while (rs.next()) {
                ArrayList<String> dep = new ArrayList<String>();

                dep.add(rs.getString(1).trim());
                dep.add(rs.getString(2).trim());

                list.add(dep);
            }

            rs.close();
            con.releaseResources();

            infoFields.addAll(replaceCode.computedFieldsSort(list));
        } catch (java.sql.SQLException e) {
            System.out.println("table 62" + e + query);
        }

        return infoFields;
    }

    public  ArrayList<String> fieldInfo(StatementExecutor con, String table, String field) {
        ArrayList<String> info = new ArrayList<>();
        ArrayList<String> domains = new ArrayList<>();

        query = "select rdb$fields.rdb$field_name\n"
                + "from rdb$fields\n"
                + "where (rdb$fields.rdb$system_flag = 0) and (rdb$fields.rdb$field_name not starting with 'RDB$')";

        try {

            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {
                domains.add(rs.getString(1).trim());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 89: " + e + query);
        }

        query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                "rdb$fields.rdb$field_type, \n" + //2
                "rdb$fields.rdb$character_length, \n" + //3
                "rdb$fields.rdb$field_sub_type, \n" + //4
                "rdb$fields.rdb$segment_length, \n" + //5
                "rdb$fields.rdb$field_precision, \n" + //6
                "abs(rdb$fields.rdb$field_scale), \n" + //7
                "rdb$relation_fields.rdb$null_flag, \n" + //8
                "rdb$relation_fields.rdb$field_source, \n" + //9
                "rdb$relation_fields.rdb$default_source, \n" + //10
                "rdb$fields.rdb$computed_source \n" + //11
                "from rdb$relation_fields \n"
                + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                + "where rdb$relation_fields.rdb$relation_name = '" + table
                + "' and rdb$relation_fields.rdb$field_name = '" + field + "'";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {
                if (!replaceCode.noNull(rs.getString(11)).trim().equals("")) {
                    info.add("computed by " + replaceCode.noNull(rs.getString(11)).trim());
                } else if (!domains.contains(rs.getString(9).trim())) {
                    info.add(replaceCode.replaceType(rs.getString(2).trim(),
                            replaceCode.noNull(rs.getString(6)).trim(),
                            replaceCode.noNull(rs.getString(7)).trim(),
                            replaceCode.noNull(rs.getString(4)).trim()));
                    info.add(replaceCode.replaceFieldLen(rs.getString(2).trim(),
                            replaceCode.noNull(rs.getString(3)).trim(),
                            replaceCode.noNull(rs.getString(4)).trim(),
                            replaceCode.noNull(rs.getString(5)).trim()));
                    info.add(replaceCode.noNull(rs.getString(10)).trim().toLowerCase());
                    info.add(replaceCode.noNull(rs.getString(8)).trim().equals("") ? "" : " not null");
                    //info.add(!noNull(rs.getString(11)).trim().equals("")? " computed by " + noNull(rs.getString(11)).trim() : "");
                } else {
                    info.add(rs.getString(9).trim());
                    info.add(replaceCode.noNull(rs.getString(10)).trim().toLowerCase());
                    info.add(replaceCode.noNull(rs.getString(8)).trim().equals("") ? "" : " not null");
                    //info.add(!noNull(rs.getString(11)).trim().equals("")? " computed by " + noNull(rs.getString(11)).trim() : "");
                }
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 139");
        }

        return info;
    }

    public  String create(String table) {
        String scriptPartDom = "";
        String scriptPart = "";

        if (!comparer.createdObjects.contains("table " + table)) {

            ArrayList<ArrayList<String>> depTables = new ArrayList<>();

            query = "select distinct rdb$dependencies.rdb$field_name,\n"
                    + "       rdb$dependencies.rdb$depended_on_name\n"
                    + "from rdb$dependencies\n"
                    + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$dependencies.rdb$dependent_name\n"
                    + "where rdb$dependencies.rdb$depended_on_type = 0\n"
                    + "and rdb$dependencies.rdb$dependent_type = 3\n"
                    + "and rdb$relation_fields.rdb$relation_name = '" + table + "'";

            try {
                ResultSet rs = firstConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {

                    if (!replaceCode.noNull(rs.getString(1)).equals("")
                            && !rs.getString(2).trim().equals(table)) {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(rs.getString(1).trim()); // поле
                        line.add(rs.getString(2).trim()); // таблица

                        depTables.add(line);
                    }
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("table 180: " + e);
            }

            for (ArrayList<String> dT : depTables) {
                if (!comparer.createdObjects.contains("table " + dT.get(1))) {
                    scriptPart = scriptPart + dependencies.addFields(dT.get(1), dT.get(0));
                }
            }

            scriptPart = scriptPart + "create table \"" + table + "\" (";

            ArrayList<String> infoFields = getFields(firstConnection, table);

            for (int i = 0; i < infoFields.size(); i++) {
                if (!comparer.createdObjects.contains("field " + infoFields.get(i) + " " + table)) {
                    ArrayList<String> info = new ArrayList<>();

                    info = fieldInfo(firstConnection, table, infoFields.get(i));

                    if (info.size() == 3) {
                        if (!comparer.createdObjects.contains("domain " + info.get(0))) {
                            query = "select rdb$fields.rdb$field_name\n"
                                    + "from rdb$fields\n"
                                    + "where rdb$fields.rdb$field_name = '" + info.get(0) + "'";

                            try {
                                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                                

                                boolean c = false;

                                while (rs.next()) {
                                    c = true;
                                }

                                if (!c) {
                                    scriptPartDom = domain.create(info.get(0));
                                    comparer.createdObjects.add("domain " + info.get(0));
                                }

                                rs.close();
                                secondConnection.releaseResources();

                            } catch (java.sql.SQLException e) {
                                System.out.println("table 180");
                            }
                        }
                    }

                    scriptPart = scriptPart + "\n      \"" + infoFields.get(i) + "\"";

                    if (info.size() == 1) {
                        scriptPart = scriptPart + " computed by (1)";

                        cf_fill.add(new ArrayList<String>());
                        cf_fill.get(cf_fill.size() - 1).add(table);
                        cf_fill.get(cf_fill.size() - 1).add(infoFields.get(i));

                    } else {
                        for (int j = 0; j < info.size(); j++) {
                            if (!info.get(j).equals("")) {
                                scriptPart = scriptPart + " " + info.get(j);
                            }
                        }
                    }
                    scriptPart = scriptPart + ",";

                    comparer.createdObjects.add("field " + infoFields.get(i) + " " + table);
                }
            }

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(",")) + ");\n\n";

            comparer.createdObjects.add("table " + table);
        }

        return scriptPartDom + scriptPart;
    }

    public  String drop(String table) {
        String scriptPart = "";

        query = "select rdb$indices.rdb$index_name\n"
                + "from rdb$indices\n"
                + "left outer join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$indices.rdb$index_name\n"
                + "where rdb$indices.rdb$relation_name = '" + table + "'\n"
                + "and rdb$relation_constraints.rdb$constraint_name is null";

        try {
            ResultSet rs = secondConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {
                if (!comparer.droppedObjects.contains("index " + rs.getString(1).trim())) {
                    scriptPart = scriptPart + "drop index \"" + rs.getString(1).trim() + "\";\n\n";

                    comparer.droppedObjects.add("index " + rs.getString(1).trim());
                }
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 232");
        }

        ArrayList<ArrayList<String>> keys = new ArrayList<>();

        query = "select rdb$relation_constraints.rdb$constraint_name,\n"
                + "       rdb$relation_constraints.rdb$constraint_type\n"
                + "from rdb$relation_constraints\n"
                + "where rdb$relation_constraints.rdb$relation_name = '" + table + "'\n"
                + "order by rdb$relation_constraints.rdb$constraint_type desc";

        try {
            ResultSet rs = secondConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                line.add(rs.getString(1).trim()); // имя ограничения
                line.add(rs.getString(2).trim()); // тип ограничения

                keys.add(line);
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 260");
        }

        for (int j = 0; j < keys.size(); j++) {
            switch (keys.get(j).get(1)) {
                case "UNIQUE":
                    scriptPart = scriptPart + constraint.dropUnique(keys.get(j).get(0));
                    break;
                case "PRIMARY KEY":
                    scriptPart = scriptPart + constraint.dropPK(keys.get(j).get(0));
                    break;
                case "FOREIGN KEY":
                    scriptPart = scriptPart + constraint.dropFK(keys.get(j).get(0));
                    break;
            }
        }

        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$dependencies.rdb$dependent_type,\n"
                + "     rdb$dependencies.rdb$dependent_name,\n"
                + "     rdb$dependencies.rdb$field_name\n"
                + "from rdb$dependencies\n"
                + "where rdb$dependencies.rdb$depended_on_name = '" + table + "'";

        try {
            ResultSet rs = secondConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                line.add(rs.getString(1).trim());
                line.add(rs.getString(2).trim());
                line.add(replaceCode.noNull(rs.getString(3)).trim());

                dep.add(line);
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 303");
        }

        for (ArrayList<String> d : dep) {
            if (dep.get(0).equals("5")) {

                ProcedureParameters info = procedure.getParameters(secondConnection, d.get(0));

                for (int i = 1; i < d.size(); i++) {

                    for (int j = 0; j < info.inputParameters.size(); j++) {

                        String fN = info.inputParameters.get(j).get(0);

                        if (fN.equals(d.get(2))) {

                            query = "select rdb$fields.rdb$field_type,\n"
                                    + "       rdb$fields.rdb$field_length / 4,\n"
                                    + "       rdb$fields.rdb$field_scale,\n"
                                    + "       rdb$fields.rdb$field_sub_type,\n"
                                    + "       rdb$fields.rdb$field_precision,\n"
                                    + "       rdb$fields.rdb$segment_length\n"
                                    + "from rdb$fields\n"
                                    + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$fields.rdb$field_name\n"
                                    + "where rdb$relation_fields.rdb$field_name = '" + d.get(2) + "'\n"
                                    + "and rdb$relation_fields.rdb$relation_name = '" + table + "'";

                            try {
                                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                                

                                while (rs.next()) {
                                    String param = "";

                                    if (rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")
                                            || rs.getString(1).trim().equals("261")) {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim())
                                                + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                                rs.getString(2).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim(),
                                                replaceCode.noNull(rs.getString(6)).trim());
                                    } else {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim());

                                        info.inputParameters.get(j).set(1, param);
                                    }
                                }

                                rs.close();
                                secondConnection.releaseResources();

                            } catch (java.sql.SQLException e) {
                                System.out.println("table 361");
                            }
                        }
                    }

                    for (int j = 0; j < info.outputParameters.size(); j++) {

                        String fN = info.outputParameters.get(j).get(0);

                        if (fN.equals(d.get(2))) {

                            query = "select rdb$fields.rdb$field_type,\n"
                                    + "       rdb$fields.rdb$field_length / 4,\n"
                                    + "       rdb$fields.rdb$field_scale,\n"
                                    + "       rdb$fields.rdb$field_sub_type,\n"
                                    + "       rdb$fields.rdb$field_precision,\n"
                                    + "       rdb$fields.rdb$segment_length\n"
                                    + "from rdb$fields\n"
                                    + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$fields.rdb$field_name\n"
                                    + "where rdb$relation_fields.rdb$field_name = '" + d.get(2) + "'\n"
                                    + "and rdb$relation_fields.rdb$relation_name = '" + table + "'";

                            try {
                                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                                

                                while (rs.next()) {
                                    String param = "";

                                    if (rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")
                                            || rs.getString(1).trim().equals("261")) {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim())
                                                + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                                rs.getString(2).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim(),
                                                replaceCode.noNull(rs.getString(6)).trim());
                                    } else {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim());

                                        info.outputParameters.get(j).set(1, param);
                                    }
                                }

                                rs.close();
                                secondConnection.releaseResources();

                            } catch (java.sql.SQLException e) {
                                System.out.println("table 414");
                            }
                        }
                    }
                }

                scriptPart = scriptPart + "create or alter procedure \"" + d.get(0) + "\"" + procedure.setParameters(info)
                        + "\nas\n" + procedure.getInfo(firstConnection, d.get(0)) + "\n\n";

            } else {
                scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
            }
        }

        scriptPart = scriptPart + "drop table \"" + table + "\";\n\n";

        comparer.droppedObjects.add("table " + table);

        return scriptPart;
    }

    public  String alter(String table) {
        String scriptPartD = ""; //0
        String scriptPart = ""; //2
        String scriptPartUpdate = ""; //1
        String scriptDefault = ""; //3

        ArrayList<ArrayList<String>> depTables = new ArrayList<>();

        query = "select distinct rdb$dependencies.rdb$field_name,\n"
                + "       rdb$dependencies.rdb$depended_on_name\n"
                + "from rdb$dependencies\n"
                + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$dependencies.rdb$dependent_name\n"
                + "where rdb$dependencies.rdb$depended_on_type = 0\n"
                + "and rdb$dependencies.rdb$dependent_type = 3\n"
                + "and rdb$relation_fields.rdb$relation_name = '" + table + "'";

        try {
            ResultSet rs = firstConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {

                if (!replaceCode.noNull(rs.getString(1)).equals("")
                        && !rs.getString(2).trim().equals(table)) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim()); // поле
                    line.add(rs.getString(2).trim()); // таблица

                    depTables.add(line);
                }
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 180: " + e);
        }

        for (ArrayList<String> dT : depTables) {
            if (!comparer.createdObjects.contains("table " + dT.get(1))) {
                scriptPart = scriptPart + dependencies.addFields(dT.get(1), dT.get(0));
            }
        }

        String nullStr = "";

        scriptPart = scriptPart + "alter table \"" + table + "\"\n";

        ArrayList<String> tableFields = getFields(firstConnection, table); // считать поля таблицы из первой БД

        for (int k = 0; k < tableFields.size(); k++) {

            ArrayList<String> info = new ArrayList<>();
            ArrayList<String> info2 = new ArrayList<>();

            info = fieldInfo(firstConnection, table, tableFields.get(k));
            info2 = fieldInfo(secondConnection, table, tableFields.get(k));

            String event = Integer.toString(info.size()) + Integer.toString(info2.size());

            switch (event) {
                case "11": // вычисляемое в вычисляемое
                    //if (!info.get(0).equals(info2.get(0))) {
                    if (!replaceCode.compare_wo_r(info.get(0), info2.get(0))) {
                        /*for (int i = 0; i < info.get(0).length(); i++) {
                         System.out.println((int) info.get(0).charAt(i));
                         }
                         String r = "\n";
                         System.out.println("SYMBOL -> " + (int) r.charAt(0));
                         System.out.println(info2.get(0));*/

                        scriptPart = scriptPart + "      alter \"" + tableFields.get(k) + "\" computed by (1),\n";

                        cf_fill.add(new ArrayList<String>());
                        cf_fill.get(cf_fill.size() - 1).add(table);
                        cf_fill.get(cf_fill.size() - 1).add(tableFields.get(k));
                    }
                    break;

                case "13": // домен в вычисляемое
                    boolean fS_name = false;
                    int num = 0;
                    Random rnd = new Random(System.currentTimeMillis());

                    //while (fS_name == false) {
                    num = rnd.nextInt(1001);

                    /*query = "select rdb$fields.rdb$field_name \n"
                     + "from rdb$fields \n"
                     + "where rdb$fields.rdb$field_name = '" + Integer.toString(num) + "'";

                     try {
                     ResultSet rs = secondConnection.execute(query,true).getResultSet();
                     

                     boolean c = false;

                     while (rs.next()) {
                     c = true;
                     }

                     if (!c) {
                     fS_name = true;
                     }

                     rs.close();
                     secondConnection.releaseResources();

                     } catch (java.sql.SQLException e) {
                     System.out.println("table 495");
                     }*/
                    //}

                    /*query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                     "rdb$fields.rdb$field_type, \n" + //2
                     "rdb$fields.rdb$character_length, \n" + //3
                     "rdb$fields.rdb$field_sub_type, \n" + //4
                     "rdb$fields.rdb$segment_length, \n" + //5
                     "rdb$fields.rdb$field_precision, \n" + //6
                     "rdb$fields.rdb$field_scale, \n" + //7
                     "rdb$relation_fields.rdb$null_flag, \n" + //8
                     "rdb$relation_fields.rdb$field_source, \n" + //9
                     "rdb$fields.rdb$field_length, \n" + //10
                     "rdb$fields.rdb$computed_source \n" + //11
                     "from rdb$relation_fields \n"
                     + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                     + "where rdb$relation_fields.rdb$relation_name = '" + table
                     + "' and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "'";

                     try {
                     ResultSet rs = firstConnection.execute(query,true).getResultSet();
                     

                     while (rs.next()) {
                     // создать свой источник поля

                     scriptPartUpdate = scriptPartUpdate + "insert into rdb$fields\n"
                     + "( rdb$field_name, "
                     + "rdb$field_type, "
                     + "rdb$field_length, "
                     + "rdb$field_scale, "
                     + "rdb$null_flag, "
                     + "rdb$field_precision, "
                     + "rdb$character_length, "
                     + "rdb$field_sub_type, "
                     + "rdb$segment_length, "
                     + "rdb$computed_source)\n"
                     + "values\n"
                     + "('RDB$NEW" + Integer.toString(num) + "', "
                     + rs.getString(2) + ", "
                     + rs.getString(10) + ", "
                     + rs.getString(7) + ", "
                     + rs.getString(8) + ", "
                     + rs.getString(6) + ", "
                     + rs.getString(3) + ", "
                     + rs.getString(4) + ", "
                     + rs.getString(5) + ", "
                     + "(1)" + ");\n\n";

                     scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                     + "rdb$field_source = 'RDB$NEW" + Integer.toString(num) + "'\n"
                     + "where (RDB$FIELD_NAME = '" + tableFields.get(k) + "') and\n"
                     + "(RDB$RELATION_NAME = '" + table + "');\n\n";

                     cf_fill.add(new ArrayList<String>());
                     cf_fill.get(cf_fill.size() - 1).add(table);
                     cf_fill.get(cf_fill.size() - 1).add(tableFields.get(k));
                     // изменить параметры процедуры
                     }

                     rs.close();
                     firstConnection.releaseResources();

                     } catch (java.sql.SQLException e) {
                     System.out.println("table 557");
                     }*/
                    scriptPartUpdate = scriptPartUpdate + "alter table \"" + table + "\" add " + "NEW" + Integer.toString(num)
                            + " computed by (1);\n\n";

                    scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                            + "rdb$relation_fields.rdb$field_source = (select rdb$relation_fields.rdb$field_source\n"
                            + "                                        from rdb$relation_fields\n"
                            + "                                        where rdb$relation_fields.rdb$relation_name = '" + table + "'\n"
                            + "                                        and rdb$relation_fields.rdb$field_name = 'NEW" + Integer.toString(num) + "')\n"
                            + "where rdb$relation_fields.rdb$relation_name = '" + table + "'\n"
                            + "and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "';\n\n";

                    scriptPartUpdate = scriptPartUpdate + "alter table \"" + table + "\" drop " + "NEW" + Integer.toString(num)
                            + ";\n\n";

                    cf_fill.add(new ArrayList<String>());
                    cf_fill.get(cf_fill.size() - 1).add(table);
                    cf_fill.get(cf_fill.size() - 1).add(tableFields.get(k));

                    break;

                case "14": // обычное в вычисляемое
                    query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                            "rdb$fields.rdb$field_type, \n" + //2
                            "rdb$fields.rdb$character_length, \n" + //3
                            "rdb$fields.rdb$field_sub_type, \n" + //4
                            "rdb$fields.rdb$segment_length, \n" + //5
                            "rdb$fields.rdb$field_precision, \n" + //6
                            "rdb$fields.rdb$field_scale, \n" + //7
                            "rdb$relation_fields.rdb$null_flag, \n" + //8
                            "rdb$relation_fields.rdb$field_source, \n" + //9
                            "rdb$fields.rdb$field_length, \n" + //10
                            "rdb$fields.rdb$computed_source, \n" + //11
                            "rdb$fields.rdb$field_source \n" + //12
                            "from rdb$relation_fields \n"
                            + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                            + "where rdb$relation_fields.rdb$relation_name = '" + table
                            + "' and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query,true).getResultSet();
                        

                        while (rs.next()) {
                            // обновить информацию
                            scriptPartUpdate = scriptPartUpdate + "update rdb$fields set\n"
                                    + "rdb$field_type = " + rs.getString(2) + ",\n"
                                    + "rdb$field_length = " + rs.getString(10) + ",\n"
                                    + "rdb$field_scale = " + rs.getString(7) + ",\n"
                                    + "rdb$null_flag = " + rs.getString(8) + ",\n"
                                    + "rdb$field_precision = " + rs.getString(6) + ",\n"
                                    + "rdb$character_length = " + rs.getString(3) + ",\n"
                                    + "rdb$field_sub_type = " + rs.getString(4) + ",\n"
                                    + "rdb$segment_length = " + rs.getString(5) + "\n"
                                    + "rdb$computed_source = " + "(1)" + "\n"
                                    + "where rdb$field_name = (select rdb$field_source from rdb$relation_fields\n"
                                    + "where rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "');\n\n";

                            cf_fill.add(new ArrayList<String>());
                            cf_fill.get(cf_fill.size() - 1).add(table);
                            cf_fill.get(cf_fill.size() - 1).add(tableFields.get(k));
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 603");
                    }
                    break;

                case "33": // домен в домен
                    if (!info.equals(info2)) {
                        try {
                            ResultSet rs = secondConnection.execute(query,true).getResultSet();
                            

                            boolean c = false;

                            while (rs.next()) {
                                c = true;
                            }

                            if (!c) {
                                if (!comparer.createdObjects.contains("domain " + info.get(0))) {
                                    scriptPartUpdate = scriptPartUpdate + domain.create(info.get(0));
                                    comparer.createdObjects.add("domain " + info.get(0));
                                }
                            }

                            rs.close();
                            secondConnection.releaseResources();

                        } catch (java.sql.SQLException e) {
                            System.out.println("table 630");
                        }

                        scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                                + "rdb$field_source = '" + info.get(0) + "'\n"
                                + "where (RDB$FIELD_NAME = '" + tableFields.get(k) + "') and\n"
                                + "(RDB$RELATION_NAME = '" + table + "');\n\n";

                        // если разница в значениях по умолчанию
                        if (info.get(1).equals("") && !info2.get(1).equals("")) {
                            scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\ndrop default;\n\n";
                        } else if ((!info.get(1).equals("") && info2.get(1).equals(""))
                                || (!info.get(1).equals("") && !info2.get(1).equals("") && !info.get(1).equals(info2.get(1)))) {
                            scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\nset " + info.get(1) + ";\n\n";
                        }

                        if (!info.get(2).equals(info2.get(2))) {
                            nullStr = info.get(2).equals("") ? "null" : "1";

                            scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                                    + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";
                        }
                    }
                    break;

                case "31": // вычиляемое в домен
                    try {
                        ResultSet rs = secondConnection.execute(query,true).getResultSet();
                        

                        boolean c = false;

                        while (rs.next()) {
                            c = true;
                        }

                        if (!c) {
                            if (!comparer.createdObjects.contains("domain " + info.get(0))) {
                                scriptPartUpdate = scriptPartUpdate + domain.create(info.get(0));
                                comparer.createdObjects.add("domain " + info.get(0));
                            }
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 677");
                    }

                    scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                            + "rdb$field_source = '" + info.get(0) + "'\n"
                            + "where (RDB$FIELD_NAME = '" + tableFields.get(k) + "') and\n"
                            + "(RDB$RELATION_NAME = '" + table + "');\n\n";

                    // если разница в значениях по умолчанию
                    if (!info.get(1).equals("")) {
                        scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\nset " + info.get(1) + ";\n\n";
                    }

                    nullStr = info.get(2).equals("") ? "null" : "1";

                    scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                            + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";

                    break;

                case "34": // обычное в домен
                    try {
                        ResultSet rs = secondConnection.execute(query,true).getResultSet();
                        

                        boolean c = false;

                        while (rs.next()) {
                            c = true;
                        }

                        if (!c) {
                            if (!comparer.createdObjects.contains("domain " + info.get(0))) {
                                scriptPartUpdate = scriptPartUpdate + domain.create(info.get(0));
                                comparer.createdObjects.add("domain " + info.get(0));
                            }
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 719");
                    }

                    scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                            + "rdb$field_source = '" + info.get(0) + "'\n"
                            + "where (RDB$FIELD_NAME = '" + tableFields.get(k) + "') and\n"
                            + "(RDB$RELATION_NAME = '" + table + "');\n\n";

                    // если разница в значениях по умолчанию
                    if (!info.get(1).equals("")) {
                        scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\nset " + info.get(1) + ";\n\n";
                    }

                    if (!info.get(2).equals(info2.get(3))) {
                        nullStr = info.get(2).equals("") ? "null" : "1";

                        scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                                + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";
                    }
                    break;

                case "44": // обычное в обычное
                    if (!info.get(0).equals(info2.get(0)) || !info.get(1).equals(info2.get(1))
                            || !info.get(3).equals(info2.get(3))) {
                        query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                                "rdb$fields.rdb$field_type, \n" + //2
                                "rdb$fields.rdb$character_length, \n" + //3
                                "rdb$fields.rdb$field_sub_type, \n" + //4
                                "rdb$fields.rdb$segment_length, \n" + //5
                                "rdb$fields.rdb$field_precision, \n" + //6
                                "rdb$fields.rdb$field_scale, \n" + //7
                                "rdb$relation_fields.rdb$null_flag, \n" + //8
                                "rdb$relation_fields.rdb$field_source, \n" + //9
                                "rdb$fields.rdb$field_length \n" + //10
                                //"rdb$fields.rdb$computed_source \n" + //11
                                "from rdb$relation_fields \n"
                                + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                                + "where rdb$relation_fields.rdb$relation_name = '" + table
                                + "' and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "'";

                        try {
                            ResultSet rs = firstConnection.execute(query,true).getResultSet();
                            

                            while (rs.next()) {
                                // обновить информацию
                                scriptPartUpdate = scriptPartUpdate + "update rdb$fields set\n"
                                        + "rdb$field_type = " + rs.getString(2) + ",\n"
                                        + "rdb$field_length = " + rs.getString(10) + ",\n"
                                        + "rdb$field_scale = " + rs.getString(7) + ",\n"
                                        + "rdb$null_flag = " + rs.getString(8) + ",\n"
                                        + "rdb$field_precision = " + rs.getString(6) + ",\n"
                                        + "rdb$character_length = " + rs.getString(3) + ",\n"
                                        + "rdb$field_sub_type = " + rs.getString(4) + ",\n"
                                        + "rdb$segment_length = " + rs.getString(5) + "\n"
                                        // + "rdb$computed_source = " + rs.getString(11) + "\n"
                                        + "where rdb$field_name = (select rdb$field_source from rdb$relation_fields\n"
                                        + "where rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "');\n\n";
                            }

                            rs.close();
                            firstConnection.releaseResources();

                        } catch (java.sql.SQLException e) {
                            System.out.println("table 783");
                        }
                    }

                    // если разница в значениях по умолчанию
                    if (info.get(1).equals("") && !info2.get(1).equals("")) {
                        scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\ndrop default;\n\n";
                    } else if ((!info.get(1).equals("") && info2.get(1).equals(""))
                            || (!info.get(1).equals("") && !info2.get(1).equals("") && !info.get(1).equals(info2.get(1)))) {
                        scriptDefault = "alter table \"" + table + "\" alter column \"" + tableFields.get(k) + "\"\nset " + info.get(1) + ";\n\n";
                    }

                    if (!info.get(3).equals(info2.get(3))) {
                        nullStr = info.get(3).equals("") ? "null" : "1";

                        scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                                + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";
                    }
                    break;

                case "41": // вычисляемое в обычное
                    query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                            "rdb$fields.rdb$field_type, \n" + //2
                            "rdb$fields.rdb$character_length, \n" + //3
                            "rdb$fields.rdb$field_sub_type, \n" + //4
                            "rdb$fields.rdb$segment_length, \n" + //5
                            "rdb$fields.rdb$field_precision, \n" + //6
                            "rdb$fields.rdb$field_scale, \n" + //7
                            "rdb$relation_fields.rdb$null_flag, \n" + //8
                            "rdb$relation_fields.rdb$field_source, \n" + //9
                            "rdb$fields.rdb$field_length \n" + //10
                            "rdb$fields.rdb$computed_source \n" + //11
                            "from rdb$relation_fields \n"
                            + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                            + "where rdb$relation_fields.rdb$relation_name = '" + table
                            + "' and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query,true).getResultSet();
                        

                        while (rs.next()) {
                            // обновить информацию
                            scriptPartUpdate = scriptPartUpdate + "update rdb$fields set\n"
                                    + "rdb$field_type = " + rs.getString(2) + ",\n"
                                    + "rdb$field_length = " + rs.getString(10) + ",\n"
                                    + "rdb$field_scale = " + rs.getString(7) + ",\n"
                                    + "rdb$null_flag = " + rs.getString(8) + ",\n"
                                    + "rdb$field_precision = " + rs.getString(6) + ",\n"
                                    + "rdb$character_length = " + rs.getString(3) + ",\n"
                                    + "rdb$field_sub_type = " + rs.getString(4) + ",\n"
                                    + "rdb$segment_length = " + rs.getString(5) + "\n"
                                    + "rdb$computed_source = " + rs.getString(11) + "\n"
                                    + "where rdb$field_name = (select rdb$field_source from rdb$relation_fields\n"
                                    + "where rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "');\n\n";
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 844");
                    }

                    nullStr = info.get(3).equals("") ? "null" : "1";

                    scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                            + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";

                    break;

                case "43": // домен в обычное
                    boolean fS_name_ = false;
                    int num_ = 0;
                    Random rnd_ = new Random(System.currentTimeMillis());

                    while (fS_name_ == false) {
                        num = rnd_.nextInt(1001);

                        query = "select rdb$fields.rdb$field_name \n"
                                + "from rdb$fields \n"
                                + "where rdb$fields.rdb$field_name = '" + Integer.toString(num) + "'";

                        try {
                            ResultSet rs = secondConnection.execute(query,true).getResultSet();
                            

                            boolean c = false;

                            while (rs.next()) {
                                c = true;
                            }

                            if (!c) {
                                fS_name_ = true;
                            }

                            rs.close();
                            secondConnection.releaseResources();

                        } catch (java.sql.SQLException e) {
                            System.out.println("table 885");
                        }
                    }

                    query = "select rdb$relation_fields.rdb$field_name, \n" + //1
                            "rdb$fields.rdb$field_type, \n" + //2
                            "rdb$fields.rdb$character_length, \n" + //3
                            "rdb$fields.rdb$field_sub_type, \n" + //4
                            "rdb$fields.rdb$segment_length, \n" + //5
                            "rdb$fields.rdb$field_precision, \n" + //6
                            "rdb$fields.rdb$field_scale, \n" + //7
                            "rdb$relation_fields.rdb$null_flag, \n" + //8
                            "rdb$relation_fields.rdb$field_source, \n" + //9
                            "rdb$fields.rdb$field_length, \n" + //10
                            "rdb$fields.rdb$computed_source \n" + //11
                            "from rdb$relation_fields \n"
                            + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source \n"
                            + "where rdb$relation_fields.rdb$relation_name = '" + table
                            + "' and rdb$relation_fields.rdb$field_name = '" + tableFields.get(k) + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query,true).getResultSet();
                        

                        while (rs.next()) {
                            // создать свой источник поля

                            scriptPartUpdate = scriptPartUpdate + "insert into rdb$fields\n"
                                    + "( rdb$field_name, "
                                    + "rdb$field_type, "
                                    + "rdb$field_length, "
                                    + "rdb$field_scale, "
                                    + "rdb$null_flag, "
                                    + "rdb$field_precision, "
                                    + "rdb$character_length, "
                                    + "rdb$field_sub_type, "
                                    + "rdb$segment_length, "
                                    + "rdb$computed_source)\n"
                                    + "values\n"
                                    + "('RDB$NEW" + Integer.toString(num_) + "', "
                                    + rs.getString(2) + ", "
                                    + rs.getString(10) + ", "
                                    + rs.getString(7) + ", "
                                    + rs.getString(8) + ", "
                                    + rs.getString(6) + ", "
                                    + rs.getString(3) + ", "
                                    + rs.getString(4) + ", "
                                    + rs.getString(5) + ", "
                                    + rs.getString(11) + ");\n\n";

                            scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set\n"
                                    + "rdb$field_source = 'RDB$NEW" + Integer.toString(num_) + "'\n"
                                    + "where (RDB$FIELD_NAME = '" + tableFields.get(k) + "') and\n"
                                    + "(RDB$RELATION_NAME = '" + table + "');\n\n";

                            if (!info.get(3).equals(info2.get(2))) {
                                nullStr = info.get(3).equals("") ? "null" : "1";

                                scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                                        + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";
                            }

                            // если разница в значениях по умолчанию
                            if (info.get(1).equals("") && !info2.get(1).equals("")) {
                                scriptDefault = "alter table " + table + " alter column " + tableFields.get(k) + "\ndrop default;\n\n";
                            } else if ((!info.get(1).equals("") && info2.get(1).equals(""))
                                    || (!info.get(1).equals("") && !info2.get(1).equals("") && !info.get(1).equals(info2.get(1)))) {
                                scriptDefault = "alter table " + table + " alter column " + tableFields.get(k) + "\nset " + info.get(1) + ";\n\n";
                            }
                            // изменить параметры процедуры
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 961: " + e + query);
                    }

                    if (!info.get(3).equals(info2.get(2))) {
                        nullStr = info.get(3).equals("") ? "null" : "1";

                        scriptPartUpdate = scriptPartUpdate + "update rdb$relation_fields set rdb$null_flag = " + nullStr
                                + "\nwhere rdb$field_name = '" + tableFields.get(k) + "' and rdb$relation_name = '" + table + "';\n\n";
                    }

                    // если разница в значениях по умолчанию
                    if (info.get(1).equals("") && !info2.get(1).equals("")) {
                        scriptDefault = "alter table " + table + " alter column " + tableFields.get(k) + "\ndrop default;\n\n";
                    } else if ((!info.get(1).equals("") && info2.get(1).equals(""))
                            || (!info.get(1).equals("") && !info2.get(1).equals("") && !info.get(1).equals(info2.get(1)))) {
                        scriptDefault = "alter table " + table + " alter column " + tableFields.get(k) + "\nset " + info.get(1) + ";\n\n";
                    }

                    break;

                default:   // если такого поля не оказалось во второй БД
                    if (!comparer.createdObjects.contains("field " + tableFields.get(k) + " " + table)) {
                        scriptPart = scriptPart + "      add \"" + tableFields.get(k) + "\"";

                        if (info.size() == 1) {
                            scriptPart = scriptPart + " computed by (1)";

                            cf_fill.add(new ArrayList<String>());
                            cf_fill.get(cf_fill.size() - 1).add(table);
                            cf_fill.get(cf_fill.size() - 1).add(tableFields.get(k));

                        } else {
                            for (int i = 0; i < info.size(); i++) {
                                if (!info.get(i).equals("")) {
                                    scriptPart = scriptPart + " " + info.get(i);
                                }
                            }
                        }

                        scriptPart = scriptPart + ",\n";

                        comparer.createdObjects.add("field " + tableFields.get(k) + " " + table);
                    }
                    break;
            }
        }

        ArrayList<String> dropFields = new ArrayList<String>();

        query = "select rdb$relation_fields.rdb$field_name\n"
                + "from rdb$relation_fields\n"
                + "where rdb$relation_fields.rdb$relation_name = '" + table + "'";

        try {
            ResultSet rs = secondConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {

                if (!tableFields.contains(rs.getString(1).trim())) {
                    dropFields.add(rs.getString(1).trim());
                }
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 1005" + e + query);
        }

        for (int i = 0; i < dropFields.size(); i++) {

            ArrayList<ArrayList<String>> dep = new ArrayList<>();
            ArrayList<ArrayList<String>> depProc = new ArrayList<>(); // процедуры для изменения

            /*query = "select rdb$dependencies.rdb$dependent_type,\n"
             + "       rdb$dependencies.rdb$dependent_name,\n"
             + "       rdb$relation_fields.rdb$field_source\n"
             + "from rdb$relation_fields\n"
             + "inner join rdb$dependencies on rdb$dependencies.rdb$depended_on_name = rdb$relation_fields.rdb$relation_name\n"
             + "where rdb$relation_fields.rdb$relation_name = '" + table + "'\n"
             + "and rdb$relation_fields.rdb$field_name = '" + dropFields.get(i) + "'\n"
             + "and rdb$relation_fields.rdb$field_source like 'RDB$'";*/
            query = "select distinct *\n"
                    + "from\n"
                    + "(select rdb$dependencies.rdb$dependent_type,\n"
                    + "       rdb$dependencies.rdb$dependent_name,\n"
                    + "       rdb$relation_fields.rdb$field_source\n"
                    + "from rdb$relation_fields\n"
                    + "inner join rdb$dependencies on rdb$dependencies.rdb$depended_on_name = rdb$relation_fields.rdb$relation_name\n"
                    + "where rdb$relation_fields.rdb$relation_name = '" + table + "'\n"
                    + "and rdb$relation_fields.rdb$field_name = '" + dropFields.get(i) + "')\n";
            //+ "/*and rdb$relation_fields.rdb$field_source like 'RDB$'   */\n"
            //+ "and (rdb$dependencies.rdb$dependent_type = 0\n"
            //+ "or rdb$dependencies.rdb$dependent_type = 1))";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    if (/*!rs.getString(1).trim().equals("5")
                             &&*/(!rs.getString(3).trim().startsWith("RDB$")
                            || rs.getString(1).trim().equals("2"))
                            || rs.getString(3).trim().startsWith("RDB$")
                            && rs.getString(1).trim().equals("3")) {

                        ArrayList<String> line = new ArrayList<String>();

                        line.add(rs.getString(1).trim());
                        line.add(rs.getString(2).trim());
                        //line.add(rs.getString(3).trim());

                        dep.add(line);
                    }
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("table 1043");
            }

            for (ArrayList<String> d : dep) {
                scriptPartD = scriptPartD + dependencies.clearDependencies(d.get(0), d.get(1));
            }

            query = "select rdb$procedure_parameters.rdb$procedure_name,\n"
                    + "       rdb$procedure_parameters.rdb$parameter_name,\n"
                    + "       rdb$relation_fields.rdb$field_source\n"
                    + "from rdb$procedure_parameters\n"
                    + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$procedure_parameters.rdb$field_source\n"
                    + "where rdb$relation_fields.rdb$field_name = '" + dropFields.get(i) + "'\n"
                    + "and rdb$relation_fields.rdb$field_source starting with 'RDB$'\n"
                    + "order by rdb$procedure_parameters.rdb$parameter_number";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(new String());
                    line.set(0, rs.getString(1).trim()); // имя процедуры
                    line.add(new String());
                    line.set(1, rs.getString(3).trim()); // домен
                    line.add(rs.getString(2).trim()); // параметр

                    depProc.add(line);
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("table 1079" + e + query);
            }

            for (ArrayList<String> dP : depProc) {
                ProcedureParameters infoP = new ProcedureParameters();

                infoP = procedure.getParameters(secondConnection, dP.get(0));

                for (int ii = 2; ii < dP.size(); ii++) {

                    for (int j = 0; j < infoP.inputParameters.size(); j++) {

                        String fN = infoP.inputParameters.get(j).get(0);

                        if (fN.equals(dP.get(ii))) {

                            query = "select rdb$fields.rdb$field_type,\n" //1
                                    + "       rdb$fields.rdb$field_length / 4,\n" //2
                                    + "       rdb$fields.rdb$field_scale,\n" //3
                                    + "       rdb$fields.rdb$field_sub_type,\n" //4
                                    + "       rdb$fields.rdb$field_precision,\n" //5
                                    + "       rdb$fields.rdb$segment_length\n" //6
                                    + "from rdb$fields\n"
                                    + "where rdb$fields.rdb$field_name = '" + dP.get(1) + "'";

                            try {
                                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                                

                                while (rs.next()) {
                                    String param = "";

                                    if (rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")
                                            || rs.getString(1).trim().equals("261")) {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim())
                                                + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                                rs.getString(2).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim(),
                                                replaceCode.noNull(rs.getString(6)).trim());
                                    } else {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim());
                                    }

                                    infoP.inputParameters.get(j).set(1, param);
                                }

                                rs.close();
                                secondConnection.releaseResources();

                            } catch (java.sql.SQLException e) {
                                System.out.println("table 1135" + e + query);
                            }
                        }
                    }

                    for (int j = 0; j < infoP.outputParameters.size(); j++) {

                        String fN = infoP.outputParameters.get(j).get(0);

                        if (fN.equals(dP.get(ii))) {

                            query = "select rdb$fields.rdb$field_type,\n" //1
                                    + "       rdb$fields.rdb$field_length / 4,\n" //2
                                    + "       rdb$fields.rdb$field_scale,\n" //3
                                    + "       rdb$fields.rdb$field_sub_type,\n" //4
                                    + "       rdb$fields.rdb$field_precision,\n" //5
                                    + "       rdb$fields.rdb$segment_length\n" //6
                                    + "from rdb$fields\n"
                                    + "where rdb$fields.rdb$field_name = '" + dP.get(1) + "'";

                            try {
                                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                                

                                while (rs.next()) {
                                    String param = "";

                                    if (rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")
                                            || rs.getString(1).trim().equals("261")) {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim())
                                                + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                                rs.getString(2).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim(),
                                                replaceCode.noNull(rs.getString(6)).trim());
                                    } else {
                                        param = param + " " + replaceCode.replaceType(rs.getString(1).trim(),
                                                replaceCode.noNull(rs.getString(5)).trim(),
                                                replaceCode.noNull(rs.getString(3)).trim(),
                                                replaceCode.noNull(rs.getString(4)).trim());
                                    }

                                    infoP.outputParameters.get(j).set(1, param);
                                }

                                rs.close();
                                secondConnection.releaseResources();

                            } catch (java.sql.SQLException e) {
                                System.out.println("table 1186" + e + query);
                            }
                        }
                    }
                }

                scriptPartD = scriptPartD + "set term ^;\n\n" + "create or alter procedure \"" + dP.get(0) + "\"" + procedure.setParameters(infoP)
                        + "\n as\nbegin suspend;end\n^\n\n" + "set term ;^\n\n";
            }

            ArrayList<ArrayList<String>> keys = new ArrayList<>();

            query = "select rdb$relation_constraints.rdb$constraint_name,\n"
                    + "       rdb$relation_constraints.rdb$constraint_type\n"
                    + "from rdb$relation_constraints\n"
                    + "inner join rdb$index_segments on rdb$index_segments.rdb$index_name = rdb$relation_constraints.rdb$index_name\n"
                    + "where rdb$relation_constraints.rdb$relation_name = '" + table + "'\n"
                    + "and rdb$index_segments.rdb$field_name = '" + dropFields.get(i) + "'\n"
                    + "order by rdb$relation_constraints.rdb$constraint_type desc";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim()); // имя ограничения
                    line.add(rs.getString(2).trim()); // тип ограничения

                    keys.add(line);
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("table 1223" + e + query);
            }

            for (int j = 0; j < keys.size(); j++) {
                switch (keys.get(j).get(1)) {
                    case "UNIQUE":
                        scriptPartD = scriptPartD + constraint.dropUnique(keys.get(j).get(0));
                        break;
                    case "PRIMARY KEY":
                        scriptPartD = scriptPartD + constraint.dropPK(keys.get(j).get(0));
                        break;
                    case "FOREIGN KEY":
                        scriptPartD = scriptPartD + constraint.dropFK(keys.get(j).get(0));
                        break;
                }
            }

            ArrayList<String> indices = new ArrayList<String>();

            query = "select rdb$index_segments.rdb$index_name\n"
                    + "from rdb$index_segments\n"
                    + "inner join rdb$indices on rdb$indices.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                    + "left outer join rdb$relation_constraints on rdb$indices.rdb$index_name = rdb$relation_constraints.rdb$index_name\n"
                    + "where rdb$indices.rdb$relation_name = '" + table + "'\n"
                    + "and rdb$index_segments.rdb$field_name = '" + dropFields.get(i) + "'\n"
                    + "and rdb$relation_constraints.rdb$index_name is null";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {

                    indices.add(rs.getString(1).trim());
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("table 1425: " + e + query);
            }

            for (String ii : indices) {
                scriptPartD = scriptPartD + index.drop(ii);
            }

            if (!comparer.droppedObjects.contains("field " + dropFields.get(i) + " " + table)) {
                scriptPartD = scriptPartD + "\n\nalter table \"" + table + "\" drop \"" + dropFields.get(i) + "\";\n\n";

                comparer.droppedObjects.add("field " + dropFields.get(i) + " " + table);
            }
        }

        if (!scriptPart.equals("alter table \"" + table + "\"\n")) {

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(",\n")) + ";\n\n";

        } else {
            scriptPart = "";
        }

        return scriptPartD + scriptPartUpdate + scriptPart + scriptDefault;
    }

    public  String fillTables(String rel, String f) {

        String scriptPart = "";

        query = "select rdb$fields.rdb$computed_source\n"
                + "from rdb$fields\n"
                + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$fields.rdb$field_name\n"
                + "where rdb$relation_fields.rdb$relation_name = '" + rel + "'\n"
                + "and rdb$relation_fields.rdb$field_name = '" + f + "'";

        try {
            ResultSet rs = firstConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {

                scriptPart = "alter table \"" + rel + "\"\n"
                        + "       alter \"" + f + "\" computed by " + rs.getString(1).trim() + ";\n\n";
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("table 1408: " + e + query);
        }

        if (!comparer.script.contains(scriptPart)) {
            return scriptPart;
        } else{
            return "";
        }
    }
}

