package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;
import org.underworldlabs.util.MiscUtils;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Dependencies {

    public Dependencies(Comparer comp) {
        comparer = comp;
        init();

    }

    public void init() {
        firstConnection = comparer.firstConnection;
        secondConnection = comparer.secondConnection;
        view = comparer.view;
        constraint = comparer.constraint;
        index = comparer.index;
        table = comparer.table;
        trigger = comparer.trigger;
        procedure = comparer.procedure;
        exception = comparer.exception;
        generator = comparer.generator;
        udf = comparer.udf;
    }

    Udf udf;
    Generator generator;
    Exception exception;
    Procedure procedure;
    Trigger trigger;
    Table table;
    Index index;
    Constraint constraint;
    View view;
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;
    private String query = "";

    // удалить поле по его источнику (второе соединение)
    public String dropField(String fs) {
        String scriptPart = "";

        ArrayList<String> tableName = new ArrayList<String>();
        ArrayList<String> viewName = new ArrayList<String>();
        ArrayList<String> fieldName = new ArrayList<String>();

        query = "select rdb$relation_fields.rdb$relation_name,\n"
                + "       rdb$relation_fields.rdb$field_name,\n"
                + "       rdb$relations.rdb$relation_type\n"
                + "from rdb$fields\n"
                + "inner join rdb$relation_fields on rdb$fields.rdb$field_name = rdb$relation_fields.rdb$field_source\n"
                + "inner join rdb$relations on rdb$relations.rdb$relation_name = rdb$relation_fields.rdb$relation_name\n"
                + "where rdb$fields.rdb$field_name = '" + fs + "'";

        try {
            ResultSet rs = secondConnection.execute(query, true).getResultSet();

            while (rs.next()) {
                if (rs.getString(3).trim().equals("0")) {

                    tableName.add(rs.getString(1).trim());
                    fieldName.add(rs.getString(2).trim());

                } else {
                    viewName.add(rs.getString(1).trim());
                }
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("dependencies 59: " + e + query);
        }

        // если это поле представления
        if (!viewName.isEmpty()) {
            for (int i = 0; i < viewName.size(); i++) {
                if (!comparer.droppedObjects.contains("view " + viewName.get(i))) {
                    scriptPart = scriptPart + view.drop(viewName.get(i));

                    query = "select 1\n"
                            + "from rdb$relations\n"
                            + "where rdb$relations.rdb$relation_name = '" + viewName.get(i) + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            view.v_create.add(viewName.get(i));
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dep 86: " + e + query);
                    }

                    comparer.droppedObjects.add("view " + viewName.get(i));
                }
            }
        }

        ArrayList<ArrayList<String>> dep = new ArrayList<>(); // 0 - тип, 1 - имя

        // если это поле таблицы
        if (!fieldName.isEmpty()) {
            for (int i = 0; i < fieldName.size(); i++) {
                if (!comparer.droppedObjects.contains("field " + fieldName.get(i))) {

                    ArrayList<ArrayList<String>> keys = new ArrayList<>();

                    query = "select rdb$relation_constraints.rdb$constraint_name,\n"
                            + "       rdb$relation_constraints.rdb$constraint_type\n"
                            + "from rdb$relation_constraints\n"
                            + "inner join rdb$index_segments on rdb$index_segments.rdb$index_name = rdb$relation_constraints.rdb$index_name\n"
                            + "where rdb$relation_constraints.rdb$relation_name = '" + tableName.get(i) + "'\n"
                            + "and rdb$index_segments.rdb$field_name = '" + fieldName.get(i) + "'\n"
                            + "order by rdb$relation_constraints.rdb$constraint_type desc";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        while (rs.next()) {
                            ArrayList<String> line = new ArrayList<String>();

                            line.add(rs.getString(1).trim()); // имя ограничения
                            line.add(rs.getString(2).trim()); // тип ограничения

                            keys.add(line);
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dependencies 107: " + e + query);
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

                    query = "select rdb$dependencies.rdb$dependent_type,\n"
                            + "       rdb$dependencies.rdb$dependent_name\n"
                            + "from rdb$dependencies\n"
                            + "where rdb$dependencies.rdb$depended_on_name = '" + tableName.get(i) + "'\n"
                            + "and rdb$dependencies.rdb$field_name = '" + fieldName.get(i) + "'";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            ArrayList<String> line = new ArrayList<>();

                            line.add(rs.getString(1).trim());
                            line.add(rs.getString(2).trim());

                            dep.add(line);
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dependencies 148: " + e + query);
                    }

                    for (ArrayList<String> d : dep) {
                        scriptPart = scriptPart + clearDependencies(d.get(0), d.get(1));
                    }

                    ArrayList<String> indices = new ArrayList<String>();

                    query = "select rdb$index_segments.rdb$index_name\n"
                            + "from rdb$index_segments\n"
                            + "inner join rdb$indices on rdb$indices.rdb$index_name = rdb$index_segments.rdb$index_name\n"
                            + "left outer join rdb$relation_constraints on rdb$indices.rdb$index_name = rdb$relation_constraints.rdb$index_name\n"
                            + "where rdb$indices.rdb$relation_name = '" + tableName.get(i) + "'\n"
                            + "and rdb$index_segments.rdb$field_name = '" + fieldName.get(i) + "'\n"
                            + "and rdb$relation_constraints.rdb$index_name is null";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            indices.add(rs.getString(1).trim());
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 1425: " + e + query);
                    }

                    for (String ii : indices) {
                        scriptPart = scriptPart + index.drop(ii);

                        query = "select 1\n"
                                + "from rdb$indices\n"
                                + "where rdb$indices.rdb$index_name = '" + ii + "'";

                        try {
                            ResultSet rs = firstConnection.execute(query, true).getResultSet();


                            while (rs.next()) {

                                index.indicesToFill.add(viewName.get(i));
                            }

                            rs.close();
                            firstConnection.releaseResources();

                        } catch (java.sql.SQLException e) {
                            System.out.println("dep 86: " + e + query);
                        }
                    }

                    if (!comparer.droppedObjects.contains("table " + tableName.get(i))
                            && !comparer.droppedObjects.contains("field " + fieldName.get(i) + " " + tableName.get(i))) {
                        scriptPart = scriptPart + "alter table \"" + tableName.get(i) + "\" drop \"" + fieldName.get(i) + "\";\n\n";

                        comparer.droppedObjects.add("field " + fieldName.get(i) + " " + tableName.get(i));
                    }
                }
            }
        }

        return scriptPart;
    }

    // создать недостающие поля и таблицы
    public String addFields(String rel, String field) {
        String scriptPart = "";
        boolean v = false;

        query = "select rdb$relations.rdb$relation_type\n"
                + "from rdb$relations\n"
                + "where rdb$relations.rdb$relation_name = '" + rel + "'";

        try {
            ResultSet rs = firstConnection.execute(query, true).getResultSet();


            while (rs.next()) {
                if (rs.getString(1).trim().equals("1")) {
                    v = true;
                }
            }

            rs.close();
            firstConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("dependencies 191: " + e + query);
        }

        boolean c = false;

        query = "select rdb$relations.rdb$relation_name\n"
                + "from rdb$relations\n"
                + "where rdb$relations.rdb$relation_name = '" + rel + "'";

        try {
            ResultSet rs = secondConnection.execute(query, true).getResultSet();

            while (rs.next()) {
                c = true; // искомая таблица найдена
            }

            rs.close();
            secondConnection.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("dependencies 221: " + e + query);
        }

        // если нужной таблицы не оказалось
        if (!c && !v) {

            scriptPart = table.create(rel);
            comparer.createdObjects.add("table " + rel);

            return scriptPart;
        } else if (v) {
            scriptPart = view.create(rel);
            return scriptPart;
        }

        if (c && MiscUtils.isNull(field)) {
            return scriptPart;
        }

        query = "select rdb$relation_fields.rdb$field_name\n"
                + "from rdb$relation_fields\n"
                + "where rdb$relation_fields.rdb$relation_name = '" + rel + "'\n"
                + "and rdb$relation_fields.rdb$field_name = '" + field + "'";

        try {
            ResultSet rs = secondConnection.execute(query, true).getResultSet();

            c = false;

            while (rs.next()) {
                c = true; // искомое поле
            }

            rs.close();
            secondConnection.releaseResources();

            // если нужного поля не оказалось
            if (!c) {
                if (!comparer.createdObjects.contains("field " + field + " " + rel)) {

                    // просмотреть зависимости поля
                    ArrayList<ArrayList<String>> depTables = new ArrayList<>();

                    query = "select rdb$dependencies.rdb$depended_on_name,\n"
                            + "       rdb$dependencies.rdb$field_name\n"
                            + "from rdb$dependencies\n"
                            + "inner join rdb$relation_fields on rdb$relation_fields.rdb$field_source = rdb$dependencies.rdb$dependent_name\n"
                            + "where rdb$relation_fields.rdb$relation_name = '" + rel + "'\n"
                            + "and rdb$relation_fields.rdb$field_name = '" + field + "'\n"
                            + "and (rdb$dependencies.rdb$depended_on_type = 1\n"
                            + "or rdb$dependencies.rdb$depended_on_type = 0)";

                    try {
                        rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            if (!replaceCode.noNull(rs.getString(2)).equals("")
                                    && !rs.getString(2).trim().equals(rel)) {
                                ArrayList<String> line = new ArrayList<String>();

                                line.add(rs.getString(2).trim()); // поле
                                line.add(rs.getString(1).trim()); // таблица

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
                            scriptPart = scriptPart + addFields(dT.get(1), dT.get(0));
                        }
                    }
//////////////////////////////////////////////////////////////////////////////////////
                    ArrayList<String> info = table.fieldInfo(firstConnection, rel, field);

                    scriptPart = scriptPart + "alter table \"" + rel + "\"\n      add \"" + field + "\"";

                    for (int i = 0; i < info.size(); i++) {
                        if (!info.get(i).equals("")) {
                            scriptPart = scriptPart + " " + info.get(i);
                        }
                    }

                    scriptPart = scriptPart + ";\n\n";

                    comparer.createdObjects.add("field " + field + " " + rel);
                }

                return scriptPart;
            }
        } catch (java.sql.SQLException e) {
            System.out.println("dependencies 263: " + e + query);
        }

        return scriptPart;
    }

    // удалить зависимые объекты
    public String clearDependencies(String object, String name) {
        String scriptPart = "";

        switch (object) {
            case "0": // таблица
                scriptPart = "";
                break;

            case "1": // представление
                if (!comparer.droppedObjects.contains("view " + name)) {
                    scriptPart = view.drop(name);

                    query = "select 1\n"
                            + "from rdb$relations\n"
                            + "where rdb$relations.rdb$relation_name = '" + name + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            view.v_create.add(name);
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dep 86: " + e + query);
                    }

                    comparer.droppedObjects.add("view " + name);

                    /*String query = "select rdb$relations.rdb$relation_name\n"
                     + "from rdb$relations\n"
                     + "where rdb$relations.rdb$relation_name = '" + name + "'";

                     try {
                     ResultSet rs = firstConnection.execute(query,true).getResultSet();
                     

                     boolean c = false;

                     while (rs.next()) {
                     c = true;
                     }

                     if (c) {
                     scriptPart = scriptPart + view.create(name);
                     }

                     rs.close();
                     firstConnection.releaseResources();

                     } catch (java.sql.SQLException e) {
                     System.out.println("dependencies 306: " + e + query);
                     }*/
                }
                break;

            case "2": // триггер
                if (!comparer.alteredObjects.contains("trigger " + name)
                        && !comparer.droppedObjects.contains("trigger " + name)
                        && !comparer.droppedObjects.contains("check " + name)) {
                    ArrayList<String> info = trigger.getInfo(secondConnection, name);
                    if (!info.isEmpty()) {
                        // если триггер
                        if (info.size() != 3) {
                            if (!comparer.droppedObjects.contains("table " + info.get(0))) {
                                scriptPart = "set term ^;\n\n" + "create or alter trigger \"" + name;

                                if (!info.get(0).equals("")) {
                                    scriptPart = scriptPart + "\" for \"" + info.get(0);
                                }

                                scriptPart = scriptPart + "\"\n" + info.get(4) + " " + info.get(2) + " position " + info.get(1) + "\n"
                                        + "as\n"
                                        + "begin\n"
                                        + "  /* Trigger text */\n"
                                        + "end" + "\n^\n\n" + "set term ;^\n\n";

                                query = "select 1\n"
                                        + "from rdb$triggers\n"
                                        + "where rdb$triggers.rdb$trigger_name = '" + name + "'";

                                try {
                                    ResultSet rs = firstConnection.execute(query, true).getResultSet();


                                    while (rs.next()) {

                                        trigger.triggerToFill.add(name);
                                    }

                                    rs.close();
                                    firstConnection.releaseResources();

                                } catch (java.sql.SQLException e) {
                                    System.out.println("dep 86: " + e + query);
                                }

                                comparer.alteredObjects.add("trigger " + name);
                            }
                        } // если ограничение CHECK
                        else {
                            if (!comparer.droppedObjects.contains("table " + info.get(0))
                                    && !comparer.droppedObjects.contains("check " + info.get(1))) {
                                scriptPart = "alter table \"" + info.get(0) + "\" drop constraint \"" + info.get(1) + "\";\n\n";
                                comparer.droppedObjects.add("check " + info.get(1));

                                query = "select 1\n"
                                        + "from rdb$relation_constraints\n"
                                        + "where rdb$relation_constraints.rdb$constraint_type = 'CHECK'\n"
                                        + "and rdb$relation_constraints.rdb$constraint_name = '" + info.get(1) + "'\n";
                                try {
                                    ResultSet rs = firstConnection.execute(query, true).getResultSet();


                                    while (rs.next()) {

                                        constraint.checkstoRecreate.add(info.get(1));
                                    }

                                    rs.close();
                                    firstConnection.releaseResources();

                                } catch (java.sql.SQLException e) {
                                    System.out.println("dep 86: " + e + query);
                                }
                            }
                        }
                    }
                }
                break;

            case "3": // вычисляемое поле

                ArrayList<String> views = new ArrayList<String>();

                query = "select rdb$relations.rdb$relation_name\n"
                        + "from rdb$relations\n"
                        + "inner join rdb$relation_fields on rdb$relation_fields.rdb$relation_name = rdb$relations.rdb$relation_name\n"
                        + "where rdb$relation_fields.rdb$field_source = '" + name + "'\n"
                        + "and rdb$relations.rdb$relation_type = 1";

                try {
                    ResultSet rs = secondConnection.execute(query, true).getResultSet();


                    while (rs.next()) {
                        views.add(rs.getString(1).trim());
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("table 1079" + e + query);
                }

                if (!comparer.droppedObjects.contains("view " + name)) {
                    scriptPart = view.drop(name);

                    query = "select 1\n"
                            + "from rdb$relations\n"
                            + "where rdb$relations.rdb$relation_name = '" + name + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            view.v_create.add(name);
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dep 86: " + e + query);
                    }

                    comparer.droppedObjects.add("view " + name);
                }

                if (!comparer.droppedObjects.contains(("field source " + name + " "))) {

                    String rel = "", f = "";
                    query = "select rdb$relation_fields.rdb$relation_name,\n"
                            + "rdb$relation_fields.rdb$field_name\n"
                            + "from rdb$relation_fields\n"
                            + "inner join rdb$relations on rdb$relations.rdb$relation_name = rdb$relation_fields.rdb$relation_name\n"
                            + "where rdb$field_source = '" + name + "'\n"
                            + "and rdb$relations.rdb$relation_type = 0";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        while (rs.next()) {
                            rel = rs.getString(1).trim();
                            f = rs.getString(2).trim();
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 1079" + e + query);
                    }

                    query = "select 1\n"
                            + "from rdb$relation_fields\n"
                            + "where rdb$field_name = '" + f + "'\n"
                            + "and rdb$relation_name = '" + rel + "'\n";
                    // пока работа только с таблицами
                    boolean c = false;

                    try {
                        ResultSet rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {
                            c = true;
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("table 1079" + e + query);
                    }

                    scriptPart = dropField(name);

                    if (c) {
                        scriptPart = scriptPart + "\nalter table \"" + rel + "\"\n";
                        scriptPart = scriptPart + "      add \"" + f + "\"";

                        ArrayList<String> info = table.fieldInfo(firstConnection, rel, f);

                        if (info.size() == 1) {
                            scriptPart = scriptPart + " computed by (1);";

                            table.cf_fill.add(new ArrayList<String>());
                            table.cf_fill.get(table.cf_fill.size() - 1).add(rel);
                            table.cf_fill.get(table.cf_fill.size() - 1).add(f);
                        } else {
                            for (int i = 0; i < info.size(); i++) {
                                if (!info.get(i).equals("")) {
                                    scriptPart = scriptPart + " " + info.get(i);
                                }
                            }

                            scriptPart = scriptPart + ";\n\n";
                        }
                        /*scriptPart = "update rdb$fields set\n"
                         + "       rdb$fields.rdb$computed_source = (1)\n"
                         + "where rdb$fields.rdb$field_name = '" + name + "';\n\n";*/
                    }

                    comparer.droppedObjects.add(("field source " + name + " "));
                }
                break;

            case "4": // ограничение CHECK
                /*if (!comparer.droppedObjects.contains("check " + name)) {
                 ArrayList<String> info = trigger.getInfo(secondConnection, name);

                 scriptPart = "alter table " + info.get(0) + " drop constraint " + info.get(1) + ";\n";

                 comparer.droppedObjects.add("check " + name);
                 }*/
                scriptPart = "";
                break;

            case "5": // процедура
                if (!comparer.alteredObjects.contains("procedure " + name)
                        && !comparer.droppedObjects.contains("procedure " + name)) {
                    scriptPart = "set term ^;\n\n";
                    scriptPart = scriptPart + "create or alter procedure \"" + name + "\""
                            + procedure.setParameters(procedure.getParameters(secondConnection, name)) + "\n as\nbegin suspend;end\n^\n\n";
                    scriptPart = scriptPart + "set term ;^\n\n";
                    comparer.alteredObjects.add("procedure " + name);

                    query = "select 1\n"
                            + "from rdb$procedures\n"
                            + "where rdb$procedures.rdb$procedure_name = '" + name + "'";

                    try {
                        ResultSet rs = firstConnection.execute(query, true).getResultSet();


                        while (rs.next()) {

                            procedure.procToFill.add(name);
                        }

                        rs.close();
                        firstConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dep 86: " + e + query);
                    }
                }
                break;

            case "6": // выражение индекса
                scriptPart = index.drop(name);

                query = "select 1\n"
                        + "from rdb$indices\n"
                        + "where rdb$indices.rdb$index_name = '" + name + "'";

                try {
                    ResultSet rs = firstConnection.execute(query, true).getResultSet();


                    while (rs.next()) {

                        index.indicesToFill.add(name);
                    }

                    rs.close();
                    firstConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("dep 86: " + e + query);
                }

                break;

            case "7": // исключение
                scriptPart = "";
                break;

            case "8": // пользователь
                scriptPart = "";
                break;

            case "9": // поле
                if (!comparer.droppedObjects.contains(("field source " + name + " "))) {
                    scriptPart = dropField(name);
                    comparer.droppedObjects.add(("field source " + name + " "));
                }
                break;

            case "10": // индекс
                scriptPart = index.drop(name);

                query = "select 1\n"
                        + "from rdb$indices\n"
                        + "where rdb$indices.rdb$index_name = '" + name + "'";

                try {
                    ResultSet rs = firstConnection.execute(query, true).getResultSet();


                    while (rs.next()) {

                        index.indicesToFill.add(name);
                    }

                    rs.close();
                    firstConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("dep 86: " + e + query);
                }

                break;
        }

        return scriptPart;
    }

    // создать зависимые объекты
    public String addDependencies(String object, String name) {
        String scriptPart = "";

        switch (object) {
            case "7": // исключение
                if (!comparer.createdObjects.contains("exception " + name)) {
                    query = "select rdb$exceptions.rdb$exception_name\n"
                            + "from rdb$exceptions\n"
                            + "where rdb$exceptions.rdb$exception_name = '" + name + "'";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        boolean c = false;

                        while (rs.next()) {
                            c = true;
                        }

                        if (!c) {
                            scriptPart = scriptPart + exception.create(name);
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dependencies 432: " + e + query);
                    }
                }
                break;
            case "5": // процедура
                // вызвать создание процедуры, при создании процедуры также
                // можно использовать addDependencies(), тогда можно избавиться
                // от отдельного заполнения процедуры
                break;
            case "14": // генератор
                if (!comparer.createdObjects.contains("generator " + name)) {
                    query = "select rdb$generators.rdb$generator_name\n"
                            + "from rdb$generators\n"
                            + "where rdb$generators.rdb$generator_name = '" + name + "'";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        boolean c = false;

                        while (rs.next()) {
                            c = true;
                        }

                        if (!c) {
                            scriptPart = scriptPart + generator.create(name);
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dependencies 465: " + e + query);
                    }
                }
                break;
            case "15": // udf
                if (!comparer.createdObjects.contains("udf " + name)) {
                    query = "select rdb$functions.rdb$function_name\n"
                            + "from rdb$functions\n"
                            + "where rdb$functions.rdb$function_name = '" + name + "'";

                    try {
                        ResultSet rs = secondConnection.execute(query, true).getResultSet();


                        boolean c = false;

                        while (rs.next()) {
                            c = true;
                        }

                        if (!c) {
                            scriptPart = scriptPart + udf.create(name);
                        }

                        rs.close();
                        secondConnection.releaseResources();

                    } catch (java.sql.SQLException e) {
                        System.out.println("dependencies 493: " + e + query);
                    }
                }
                break;
        }

        return scriptPart;
    }
}
