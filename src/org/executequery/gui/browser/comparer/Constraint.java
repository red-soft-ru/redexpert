package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

class ForeignKey {

    public String mainTable;
    public String refTable;

    public String rules;

    public ArrayList<String> mainTableFielfd;
    public ArrayList<String> refTableFields;
}

public class Constraint {
    
    public Constraint(Comparer comp)
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
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;
    Dependencies dependencies;

    private  String query = "";

    public  ArrayList<String> checkstoRecreate = new ArrayList<String>();

    public  ArrayList<String> getCheckInfo(StatementExecutor con, String constraint) {
        ArrayList<String> info = new ArrayList<String>();

        query = "select first 1 rdb$triggers.rdb$trigger_source,\n"
                + "       rdb$triggers.rdb$relation_name\n"
                + "from rdb$triggers\n"
                + "inner join rdb$check_constraints on rdb$triggers.rdb$trigger_name = rdb$check_constraints.rdb$trigger_name\n"
                + "where rdb$check_constraints.rdb$constraint_name = '" + constraint + "'";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {

                info.add(rs.getString(1).trim());
                info.add(rs.getString(2).trim());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 56: " + e + query);
        }

        return info;
    }

    public  ArrayList<String> getUniqueInfo(StatementExecutor con, String unique) {
        ArrayList<String> info = new ArrayList<String>();
        info.add("");

        query = "select rdb$index_segments.rdb$field_name,\n"
                + "       rdb$indices.rdb$relation_name\n"
                + "from rdb$indices\n"
                + "left join rdb$index_segments on rdb$indices.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                + "left join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$indices.rdb$index_name\n"
                + "where rdb$relation_constraints.rdb$constraint_type = 'UNIQUE'\n"
                + "and rdb$indices.rdb$index_name = '" + unique + "'\n"
                + "order by rdb$index_segments.rdb$field_position";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {

                info.add(rs.getString(1).trim());
                info.set(0, rs.getString(2).trim()); // таблица
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 89: " + e + query);
        }

        return info;
    }

    public  ArrayList<String> getPKInfo(StatementExecutor con, String pk) {
        ArrayList<String> info = new ArrayList<String>(); // первый элемент всегда - таблица
        info.add("");

        query = "select rdb$index_segments.rdb$field_name,\n"
                + "       rdb$indices.rdb$relation_name\n"
                + "from rdb$indices\n"
                + "left join rdb$index_segments on rdb$indices.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                + "left join rdb$relation_constraints rc on rc.rdb$index_name = rdb$indices.rdb$index_name\n"
                + "where rc.rdb$constraint_type = 'PRIMARY KEY' and rdb$indices.rdb$index_name = '" + pk + "'\n"
                + "order by rdb$index_segments.rdb$field_position";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {

                info.add(rs.getString(1).trim());
                info.set(0, rs.getString(2).trim());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 121: " + e + query);
        }

        return info;
    }

    public  ForeignKey getFKInfo(StatementExecutor con, String fk) {
        ForeignKey info = new ForeignKey();

        info.mainTableFielfd = new ArrayList<String>();
        info.refTableFields = new ArrayList<String>();

        String refIndex = "";

        query = "select rdb$ref_constraints.rdb$delete_rule,\n"
                + "       rdb$ref_constraints.rdb$update_rule,\n"
                + "      (select rdb$indices.rdb$relation_name\n"
                + "       from rdb$indices\n"
                + "       where rdb$indices.rdb$index_name = '" + fk + "'),\n"
                + "       rdb$indices.rdb$relation_name,\n"
                + "       rdb$indices.rdb$index_name\n"
                + "from rdb$ref_constraints\n"
                + "inner join rdb$indices on rdb$indices.rdb$index_name = rdb$ref_constraints.rdb$const_name_uq\n"
                + "where rdb$ref_constraints.rdb$constraint_name = '" + fk + "'";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {
                String line = (rs.getString(1).trim().equals("RESTRICT")) || (rs.getString(1).trim().equals("NO ACTION"))
                        ? "" : "on delete " + rs.getString(1).trim() + "\n";

                info.rules = line;

                line = (rs.getString(2).trim().equals("RESTRICT")) || (rs.getString(2).trim().equals("NO ACTION"))
                        ? "" : "on update " + rs.getString(2).trim() + "\n";

                info.rules = info.rules + line;

                info.mainTable = rs.getString(3).trim();
                info.refTable = rs.getString(4).trim();

                refIndex = rs.getString(5).trim();
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 171: " + e + query);
        }

        query = "select rdb$index_segments.rdb$field_name,\n"
                + "       rdb$relation_constraints.rdb$relation_name\n"
                + "from rdb$index_segments\n"
                + "inner join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                + "where rdb$relation_constraints.rdb$constraint_name = '" + refIndex + "'\n"
                + "or rdb$relation_constraints.rdb$constraint_name = '" + fk + "'\n"
                + "order by rdb$index_segments.rdb$field_position";

        try {
            ResultSet rs = con.execute(query,true).getResultSet();
            

            while (rs.next()) {
                if (rs.getString(2).trim().equals(info.mainTable)) {
                    info.mainTableFielfd.add(rs.getString(1).trim());
                } else {
                    info.refTableFields.add(rs.getString(2).trim());
                }

            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 194: " + e + query);
        }

        return info;
    }

    public   String collect_check = "select distinct rdb$relation_constraints.rdb$constraint_name\n"
            + "from rdb$relation_constraints\n"
            + "where rdb$relation_constraints.rdb$constraint_type = 'CHECK'";

    public   String collect_unique = "select distinct rdb$relation_constraints.rdb$constraint_name\n"
            + "from rdb$relation_constraints\n"
            + "where rdb$relation_constraints.rdb$constraint_type = 'UNIQUE'";

    public   String collect_pk = "select distinct rdb$relation_constraints.rdb$constraint_name\n"
            + "from rdb$relation_constraints\n"
            + "where rdb$relation_constraints.rdb$constraint_type = 'PRIMARY KEY'";

    public   String collect_fk = "select distinct rdb$relation_constraints.rdb$constraint_name\n"
            + "from rdb$relation_constraints\n"
            + "where rdb$relation_constraints.rdb$constraint_type = 'FOREIGN KEY'";

    public  String createCheck(String check) {
        String scriptPart = "";

        ArrayList<String> info = getCheckInfo(firstConnection, check);

        ArrayList<ArrayList<String>> depFields = new ArrayList<>();
        ArrayList<ArrayList<String>> fieldsToCreate = new ArrayList<>();

        query = "select distinct rdb$dependencies.rdb$field_name,\n"
                + "                rdb$dependencies.rdb$depended_on_name\n"
                + "from rdb$dependencies\n"
                + "inner join rdb$check_constraints on rdb$check_constraints.rdb$trigger_name = rdb$dependencies.rdb$dependent_name\n"
                + "where rdb$check_constraints.rdb$constraint_name = '" + check + "'\n"
                + "and (rdb$dependencies.rdb$depended_on_type = 0)";

        try {
            ResultSet rs = firstConnection.execute(query,true).getResultSet();
            

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();
                String obj1=rs.getString(1);
                String obj2=rs.getString(2);
                if (obj1!=null)
                    obj1=obj1.trim();
                if (obj2!=null)
                    obj2=obj2.trim();
                line.add(obj1); // имя поля
                line.add(obj2); // имя таблицы

                depFields.add(line);
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("const 248: " + e + query);
        }

        for (ArrayList<String> dF : depFields) {
            query = "select rdb$relation_fields.rdb$field_name,\n"
                    + "rdb$relation_fields.rdb$null_flag\n"
                    + "from rdb$relation_fields\n"
                    + "where rdb$relation_fields.rdb$relation_name = '" + dF.get(1) + "'\n"
                    + "and rdb$relation_fields.rdb$field_name = '" + dF.get(0) + "'";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                boolean c = false;
                String nullF = "";

                while (rs.next()) {
                    c = true;
                    nullF = replaceCode.noNull(rs.getString(2)).trim();

                    if (!nullF.contains("1")) {
                        scriptPart = scriptPart + "update RDB$RELATION_FIELDS set\n"
                                + "RDB$NULL_FLAG = 1\n"
                                + "where (RDB$FIELD_NAME = '" + dF.get(0) + "') and\n"
                                + "(RDB$RELATION_NAME = '" + dF.get(1) + "');\n\n";
                    }
                }

                if (!c) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(dF.get(0));
                    line.add(dF.get(1));

                    fieldsToCreate.add(line);
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 290: " + e + query);
            }
        }

        // добавить недостающие поля
        for (int j = 0; j < fieldsToCreate.size(); j++) {

            scriptPart = scriptPart + dependencies.addFields(fieldsToCreate.get(j).get(1), fieldsToCreate.get(j).get(0));
        }

        scriptPart = scriptPart + "alter table \"" + info.get(1) + "\"\nadd constraint\n\"" + check + "\" "
                + info.get(0) + ";\n\n";

        return scriptPart;
    }

    public  String alterCheck(String check) {
        String scriptPart = "";

        ArrayList<String> info1 = getCheckInfo(firstConnection, check);
        ArrayList<String> info2 = getCheckInfo(secondConnection, check);

        //if (!info1.get(0).equals(info2.get(0))) {
        if (!replaceCode.compare_wo_r(info1.get(0), info2.get(0))) {
            scriptPart = scriptPart + dropCheck(check);
            scriptPart = scriptPart + createCheck(check);
        }

        return scriptPart;
    }

    public  String dropCheck(String check) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("check " + check)) {
            scriptPart = "alter table \"" + getCheckInfo(secondConnection, check).get(1)
                    + "\" drop constraint \"" + check + "\";\n\n";

            comparer.droppedObjects.add("check " + check);
        }

        return scriptPart;
    }

    public  String createUnique(String unique) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("unique " + unique)) {

            ArrayList<String> info = getUniqueInfo(firstConnection, unique);

            ArrayList<ArrayList<String>> depFields = new ArrayList<>();
            ArrayList<ArrayList<String>> fieldsToCreate = new ArrayList<>();

            query = "select rdb$index_segments.rdb$field_name,\n"
                    + "       rdb$relation_constraints.rdb$relation_name\n"
                    + "from rdb$index_segments\n"
                    + "inner join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                    + "where rdb$relation_constraints.rdb$constraint_name = '" + unique + "'\n"
                    + "order by rdb$index_segments.rdb$field_position";

            try {
                ResultSet rs = firstConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim()); // имя поля
                    line.add(rs.getString(2).trim()); // имя таблицы

                    depFields.add(line);
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 367: " + e + query);
            }

            for (ArrayList<String> dF : depFields) {
                query = "select rdb$relation_fields.rdb$field_name,\n"
                        + "rdb$relation_fields.rdb$null_flag\n"
                        + "from rdb$relation_fields\n"
                        + "where rdb$relation_fields.rdb$relation_name = '" + dF.get(1) + "'\n"
                        + "and rdb$relation_fields.rdb$field_name = '" + dF.get(0) + "'";

                try {
                    ResultSet rs = secondConnection.execute(query,true).getResultSet();
                    boolean c = false;
                    //String nullF = "";

                    while (rs.next()) {
                        c = true;
                        //nullF = replaceCode.noNull(rs.getString(2)).trim();

                        /*if (!nullF.contains("1")) {
                         scriptPart = scriptPart + "update RDB$RELATION_FIELDS set\n"
                         + "RDB$NULL_FLAG = 1\n"
                         + "where (RDB$FIELD_NAME = '" + dF.get(0) + "') and\n"
                         + "(RDB$RELATION_NAME = '" + dF.get(1) + "');\n\n";
                         }*/
                    }

                    if (!c) {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(dF.get(0));
                        line.add(dF.get(1));

                        fieldsToCreate.add(line);
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("const 409: " + e + query);
                }
            }

            // добавить недостающие поля
            for (int j = 0; j < fieldsToCreate.size(); j++) {

                scriptPart = scriptPart + dependencies.addFields(fieldsToCreate.get(j).get(1), fieldsToCreate.get(j).get(0));
            }

            scriptPart = scriptPart + "alter table \"" + info.get(0) + "\"\nadd constraint\n\"" + unique
                    + "\"\nunique(";

            for (int i = 1; i < info.size(); i++) {
                scriptPart = scriptPart + "\"" + info.get(i) + "\", ";
            }
            if(scriptPart.contains(", "))
                scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", "));
            scriptPart  += ");\n\n";

            comparer.createdObjects.add("unique " + unique);
        }

        return scriptPart;
    }

    public  String alterUnique(String unique) {
        String scriptPart = "";

        ArrayList<String> info1 = getUniqueInfo(firstConnection, unique);
        ArrayList<String> info2 = getUniqueInfo(secondConnection, unique);

        if (!info1.equals(info2)) {
            scriptPart = scriptPart + dropUnique(unique);
            scriptPart = scriptPart + createUnique(unique);
        }

        return scriptPart;
    }

    public  String dropUnique(String unique) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("unique " + unique)) {

            ArrayList<String> dep = new ArrayList<String>();

            query = "select rdb$ref_constraints.rdb$constraint_name\n"
                    + "from rdb$ref_constraints\n"
                    + "where rdb$ref_constraints.rdb$const_name_uq = '" + unique + "'";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    dep.add(rs.getString(1).trim());
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 471: " + e + query);
            }

            for (String d : dep) {
                if (!comparer.droppedObjects.contains("FK " + d)) {
                    scriptPart = scriptPart + dropFK(d);

                    comparer.droppedObjects.add("FK " + d);
                }
            }

            scriptPart = scriptPart + "alter table \"" + getUniqueInfo(secondConnection, unique).get(0)
                    + "\" drop constraint \"" + unique + "\";\n\n";

            comparer.droppedObjects.add("unique " + unique);
        }

        return scriptPart;
    }

    public  String createPK(String pk) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("pk " + pk)) {

            ArrayList<String> info = getPKInfo(firstConnection, pk);

            ArrayList<ArrayList<String>> depFields = new ArrayList<>();
            ArrayList<ArrayList<String>> fieldsToCreate = new ArrayList<>();

            query = "select rdb$index_segments.rdb$field_name,\n"
                    + "       rdb$relation_constraints.rdb$relation_name\n"
                    + "from rdb$index_segments\n"
                    + "inner join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                    + "where rdb$relation_constraints.rdb$constraint_name = '" + pk + "'\n"
                    + "order by rdb$index_segments.rdb$field_position";

            try {
                ResultSet rs = firstConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim()); // имя поля
                    line.add(rs.getString(2).trim()); // имя таблицы

                    depFields.add(line);
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 525: " + e + query);
            }

            for (ArrayList<String> dF : depFields) {
                query = "select rdb$relation_fields.rdb$field_name,\n"
                        + "rdb$relation_fields.rdb$null_flag\n"
                        + "from rdb$relation_fields\n"
                        + "where rdb$relation_fields.rdb$relation_name = '" + dF.get(1) + "'\n"
                        + "and rdb$relation_fields.rdb$field_name = '" + dF.get(0) + "'";

                try {
                    ResultSet rs = secondConnection.execute(query,true).getResultSet();
                    

                    boolean c = false;
                    String nullF = "";

                    while (rs.next()) {
                        c = true;
                        nullF = replaceCode.noNull(rs.getString(2)).trim();

                        if (!nullF.contains("1")) {
                            scriptPart = scriptPart + "update RDB$RELATION_FIELDS set\n"
                                    + "RDB$NULL_FLAG = 1\n"
                                    + "where (RDB$FIELD_NAME = '" + dF.get(0) + "') and\n"
                                    + "(RDB$RELATION_NAME = '" + dF.get(1) + "');\n\n";
                        }
                    }

                    if (!c) {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(dF.get(0));
                        line.add(dF.get(1));

                        fieldsToCreate.add(line);
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("const 567: " + e + query);
                }
            }

            // добавить недостающие поля
            for (int j = 0; j < fieldsToCreate.size(); j++) {

                scriptPart = scriptPart + dependencies.addFields(fieldsToCreate.get(j).get(1), fieldsToCreate.get(j).get(0));
            }

            scriptPart = scriptPart + "alter table \"" + info.get(0) + "\"\nadd constraint\n\"" + pk
                    + "\"\nprimary key(";

            for (int i = 1; i < info.size(); i++) {
                scriptPart = scriptPart + "\"" + info.get(i) + "\", ";
            }

            if(scriptPart.contains(", "))
                scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", "));

            scriptPart = scriptPart + ");\n\n";

            comparer.createdObjects.add("pk " + pk);
        }

        return scriptPart;
    }

    public  String alterPK(String pk) {
        String scriptPart = "";

        ArrayList<String> info1 = getPKInfo(firstConnection, pk);
        ArrayList<String> info2 = getPKInfo(secondConnection, pk);

        if (!info1.equals(info2)) {
            scriptPart = scriptPart + dropPK(pk);
            scriptPart = scriptPart + createPK(pk);
        }

        return scriptPart;
    }

    public  String dropPK(String pk) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("pk " + pk)) {
            ArrayList<String> dep = new ArrayList<String>();

            query = "select rdb$ref_constraints.rdb$constraint_name\n"
                    + "from rdb$ref_constraints\n"
                    + "where rdb$ref_constraints.rdb$const_name_uq = '" + pk + "'";

            try {
                ResultSet rs = secondConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    dep.add(rs.getString(1).trim());
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 628: " + e + query);
            }

            for (String d : dep) {
                if (!comparer.droppedObjects.contains("FK " + d)) {
                    scriptPart = scriptPart + dropFK(d);

                    comparer.droppedObjects.add("FK " + d);
                }
            }

            scriptPart = scriptPart + "alter table \"" + getPKInfo(secondConnection, pk).get(0)
                    + "\" drop constraint \"" + pk + "\";\n\n";

            comparer.droppedObjects.add("pk " + pk);
        }

        return scriptPart;
    }

    public  String createFK(String fk) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("fk " + fk)) {

            ForeignKey info = getFKInfo(firstConnection, fk);

            ArrayList<ArrayList<String>> depFields = new ArrayList<>();
            ArrayList<ArrayList<String>> fieldsToCreate = new ArrayList<>();

            query = "select rdb$index_segments.rdb$field_name,\n"
                    + "       rdb$relation_constraints.rdb$relation_name\n"
                    + "from rdb$index_segments\n"
                    + "inner join rdb$relation_constraints on rdb$relation_constraints.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                    + "where rdb$relation_constraints.rdb$constraint_name = '" + fk + "'\n"
                    + "order by rdb$index_segments.rdb$field_position";

            try {
                ResultSet rs = firstConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim()); // имя поля
                    line.add(rs.getString(2).trim()); // имя таблицы

                    depFields.add(line);
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 682: " + e + query);
            }

            for (ArrayList<String> dF : depFields) {
                query = "select rdb$relation_fields.rdb$field_name,\n"
                        + "rdb$relation_fields.rdb$null_flag\n"
                        + "from rdb$relation_fields\n"
                        + "where rdb$relation_fields.rdb$relation_name = '" + dF.get(1) + "'\n"
                        + "and rdb$relation_fields.rdb$field_name = '" + dF.get(0) + "'";

                try {
                    ResultSet rs = secondConnection.execute(query,true).getResultSet();
                    

                    boolean c = false;
                    String nullF = "";

                    while (rs.next()) {
                        c = true;
                        nullF = replaceCode.noNull(rs.getString(2)).trim();

                        if (!nullF.contains("1")) {
                            scriptPart = scriptPart + "update RDB$RELATION_FIELDS set\n"
                                    + "RDB$NULL_FLAG = 1\n"
                                    + "where (RDB$FIELD_NAME = '" + dF.get(0) + "') and\n"
                                    + "(RDB$RELATION_NAME = '" + dF.get(1) + "');\n\n";
                        }
                    }

                    if (!c) {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(dF.get(0));
                        line.add(dF.get(1));

                        fieldsToCreate.add(line);
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("const 724: " + e + query);
                }
            }

            // добавить недостающие поля
            for (int j = 0; j < fieldsToCreate.size(); j++) {

                scriptPart = scriptPart + dependencies.addFields(fieldsToCreate.get(j).get(1), fieldsToCreate.get(j).get(0));
            }

            ArrayList<ArrayList<String>> depKeys = new ArrayList<>();

            query = "select rdb$ref_constraints.rdb$const_name_uq,\n"
                    + "rdb$relation_constraints.rdb$constraint_type\n"
                    + "from rdb$ref_constraints\n"
                    + "inner join rdb$relation_constraints on rdb$relation_constraints.rdb$constraint_name = rdb$ref_constraints.rdb$constraint_name\n"
                    + "inner join rdb$indices on rdb$indices.rdb$index_name = rdb$ref_constraints.rdb$constraint_name\n"
                    + "where rdb$ref_constraints.rdb$constraint_name = '" + fk + "'";

            try {
                ResultSet rs = firstConnection.execute(query,true).getResultSet();
                

                while (rs.next()) {
                    ArrayList<String> line = new ArrayList<String>();

                    line.add(rs.getString(1).trim());
                    line.add(rs.getString(2).trim());

                    depKeys.add(line);
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("const 760: " + e + query);
            }

            ArrayList<ArrayList<String>> keysToCreate = new ArrayList<>();

            for (ArrayList<String> dk : depKeys) {
                query = "select rdb$indices.rdb$index_name\n"
                        + "from rdb$indices\n"
                        + "where rdb$indices.rdb$index_name = '" + dk.get(0) + "'";

                try {
                    ResultSet rs = secondConnection.execute(query,true).getResultSet();
                    

                    boolean c = false;

                    while (rs.next()) {
                        c = true;
                    }

                    if (!c) {
                        keysToCreate.add(dk);
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("const 788: " + e + query);
                }
            }

            for (ArrayList<String> k : keysToCreate) {

                if (k.get(1).equals("UNIQUE")) {
                    if (!comparer.createdObjects.contains("U " + k.get(0))) {
                        scriptPart = scriptPart + createUnique(k.get(0));

                        comparer.createdObjects.add("U " + k.get(0));
                    }
                }
                if (k.get(1).equals("PRIMARY KEY")) {
                    if (!comparer.createdObjects.contains("PK " + k.get(0))) {
                        scriptPart = scriptPart + createPK(k.get(0));

                        comparer.createdObjects.add("PK " + k.get(0));
                    }
                }
            }

            scriptPart = scriptPart + "alter table \"" + info.mainTable + "\"\nadd constraint\n\"" + fk + "\"\n"
                    + "foreign key (";

            for (int i = 0; i < info.mainTableFielfd.size(); i++) {
                scriptPart = scriptPart + "\"" + info.mainTableFielfd.get(i) + "\", ";
            }
            if(scriptPart.contains(", "))
                scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", "));

            scriptPart = scriptPart + ");\n";

            scriptPart = scriptPart + "references \"" + info.refTable + "\"(";

            for (int i = 0; i < info.refTableFields.size(); i++) {
                scriptPart = scriptPart + "\"" + info.refTableFields.get(i) + "\", ";
            }

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", ")) + ")\n";
            scriptPart = scriptPart + info.rules;
            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf("\n")) + ";\n\n";

            comparer.createdObjects.contains("fk " + fk);
        }

        return scriptPart;
    }

    public  String alterFK(String fk) {
        String scriptPart = "";

        ForeignKey info1 = getFKInfo(firstConnection, fk);
        ForeignKey info2 = getFKInfo(secondConnection, fk);

        if (info1.mainTable!=null&&info2.mainTable!=null) {
            if (!info1.mainTable.equals(info2.mainTable)
                    || !info1.refTable.equals(info2.refTable)
                    || !info1.mainTableFielfd.equals(info2.mainTableFielfd)
                    || !info1.refTableFields.equals(info2.refTableFields)
                    || !info1.rules.equals(info2.rules)) {
                scriptPart = scriptPart + dropFK(fk);
                scriptPart = scriptPart + createFK(fk);
            }
        }
        else if (info1.mainTable!=null||info2.mainTable!=null)
        {
            scriptPart = scriptPart + dropFK(fk);
            scriptPart = scriptPart + createFK(fk);
        }

        return scriptPart;
    }

    public  String dropFK(String fk) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("fk " + fk)) {
            scriptPart = "alter table \"" + getFKInfo(secondConnection, fk).mainTable
                    + "\" drop constraint \"" + fk + "\";\n\n";

            comparer.droppedObjects.add("fk " + fk);
        }

        return scriptPart;
    }
}
