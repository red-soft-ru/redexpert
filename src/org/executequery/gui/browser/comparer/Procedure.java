package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

class ProcedureParameters {

    public ArrayList<ArrayList<String>> inputParameters = new ArrayList<>();
    public ArrayList<ArrayList<String>> outputParameters = new ArrayList<>();
}

public class Procedure {
    public Procedure(Comparer comp) {
        comparer = comp;
        init();
    }

    public void init() {
        firstConnection = comparer.compareConnection;
        secondConnection = comparer.masterConnection;
        this.domain = comparer.domain;
        dependencies = comparer.dependencies;
    }

    Dependencies dependencies;
    Comparer comparer;
    Domain domain;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;
    public String collect = "select rdb$procedures.rdb$procedure_name\n"
            + "from rdb$procedures\n"
            + "where rdb$procedures.rdb$system_flag = 0";

    public ArrayList<String> procToFill = new ArrayList<String>();

    private String query = "";

    public String setParameters(ProcedureParameters info) {
        String scriptPart = "";

        if (!info.inputParameters.isEmpty()) {
            scriptPart = " (";

            for (int i = 0; i < info.inputParameters.size(); i++) {
                scriptPart = scriptPart + "\n      \"" + info.inputParameters.get(i).get(0) + "\"";

                for (int j = 1; j < info.inputParameters.get(i).size(); j++) {
                    if (!info.inputParameters.get(i).get(j).equals("")) {
                        scriptPart = scriptPart + " " + info.inputParameters.get(i).get(j);
                    }
                }

                scriptPart = scriptPart + ", ";
            }

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", ")) + ") ";
        }

        if (!info.outputParameters.isEmpty()) {
            scriptPart = scriptPart + "\nreturns (";

            for (int i = 0; i < info.outputParameters.size(); i++) {
                scriptPart = scriptPart + "\n      \"" + info.outputParameters.get(i).get(0) + "\"";

                for (int j = 1; j < info.outputParameters.get(i).size(); j++) {
                    if (!info.outputParameters.get(i).get(j).equals("")) {
                        scriptPart = scriptPart + " " + info.outputParameters.get(i).get(j);
                    }
                }

                scriptPart = scriptPart + ", ";
            }

            scriptPart = scriptPart.substring(0, scriptPart.lastIndexOf(", ")) + ") ";
        }

        return scriptPart;
    }

    public String getInfo(StatementExecutor con, String procedure) {
        String info = "";

        query = "select rdb$procedures.rdb$procedure_source\n"
                + "from rdb$procedures\n"
                + "where rdb$procedures.rdb$procedure_name = upper('" + procedure + "')";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {

                info = rs.getString(1).trim();
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("procedure 92: " + e + query);
        }

        return info;
    }

    public ProcedureParameters getParameters(StatementExecutor con, String procedure) {
        ProcedureParameters IOparam = new ProcedureParameters();

        query = "select rdb$procedure_parameters.rdb$parameter_name,\n" + //1
                "       rdb$procedure_parameters.rdb$default_source,\n" + //2
                "       rdb$procedure_parameters.rdb$null_flag,\n" + //3
                "       rdb$procedure_parameters.rdb$field_source,\n" + //4
                "       rdb$procedure_parameters.rdb$field_name,\n" + //5
                "       rdb$procedure_parameters.rdb$relation_name,\n" + //6
                "       rdb$fields.rdb$field_type,\n" + //7
                "       rdb$fields.rdb$field_length,\n" + //8
                "       rdb$fields.rdb$field_scale,\n" + //9
                "       rdb$fields.rdb$field_sub_type,\n" + //10
                "       rdb$fields.rdb$field_precision,\n" + //11
                "       rdb$fields.rdb$segment_length,\n" + //12
                "       rdb$procedure_parameters.rdb$parameter_type\n" + //13
                "from rdb$procedure_parameters\n"
                + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$procedure_parameters.rdb$field_source\n"
                + "where rdb$procedure_parameters.rdb$procedure_name = upper('" + procedure + "')\n"
                //+ "      and rdb$procedure_parameters.rdb$parameter_type = 0\n"
                + "order by rdb$procedure_parameters.rdb$parameter_number";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {
                ArrayList<String> line = new ArrayList<String>();

                line.add(rs.getString(1).trim());

                String param = "";

                if (!rs.getString(4).trim().startsWith("RDB$")) {
                    param = param + " " + rs.getString(4).trim();
                } else if (!replaceCode.noNull(rs.getString(6)).trim().equals("")) {
                    param = param + " type of column " + rs.getString(6).trim() + "." + rs.getString(5).trim();
                } else {
                    if (rs.getString(7).trim().equals("14") || rs.getString(7).trim().equals("37")
                            || rs.getString(7).trim().equals("261")) {
                        param = param + " " + replaceCode.replaceType(rs.getString(7).trim(),
                                replaceCode.noNull(rs.getString(11)).trim(),
                                replaceCode.noNull(rs.getString(9)).trim(),
                                replaceCode.noNull(rs.getString(10)).trim())
                                + replaceCode.replaceFieldLen(rs.getString(7).trim(),
                                rs.getString(8).trim(),
                                replaceCode.noNull(rs.getString(10)).trim(),
                                replaceCode.noNull(rs.getString(12)).trim());
                    } else {
                        param = param + " " + replaceCode.replaceType(rs.getString(7).trim(),
                                replaceCode.noNull(rs.getString(11)).trim(),
                                replaceCode.noNull(rs.getString(9)).trim(),
                                replaceCode.noNull(rs.getString(10)).trim());
                    }
                }

                param = param + (replaceCode.noNull(rs.getString(3)).trim().equals("") ? "" : " not null");
                param = param + (replaceCode.noNull(rs.getString(2)).trim().equals("") ? "" : (" " + rs.getString(2).trim()));

                line.add(param);

                if (rs.getString(13).trim().equals("0")) {
                    IOparam.inputParameters.add(line);
                } else {
                    IOparam.outputParameters.add(line);
                }
            }
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("procedure 170: " + e + query);
        }

        return IOparam;
    }

    public String create(String procedure) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("procedure " + procedure)) {

            ProcedureParameters info = getParameters(firstConnection, procedure);

            ArrayList<ArrayList<String>> depFields = new ArrayList<>();
            ArrayList<ArrayList<String>> dep = new ArrayList<>();
            ArrayList<String> depDomains = new ArrayList<String>();

            query = "select rdb$dependencies.rdb$depended_on_name,\n"
                    + "       rdb$dependencies.rdb$field_name,\n"
                    + "       'DOMAIN'\n"
                    + "from rdb$dependencies\n"
                    + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$dependencies.rdb$depended_on_name\n"
                    + "where rdb$dependencies.rdb$dependent_name = '" + procedure + "'\n"
                    + "\n"
                    + "union all\n"
                    + "\n"
                    + "select rdb$dependencies.rdb$depended_on_name,\n"
                    + "       rdb$dependencies.rdb$field_name,\n"
                    + "       'FIELD'\n"
                    + "from rdb$dependencies\n"
                    + "where rdb$dependencies.rdb$dependent_name = '" + procedure + "'\n"
                    + "and rdb$dependencies.rdb$field_name is not null\n"
                    + "and rdb$dependencies.rdb$depended_on_type <= 1\n"
                    + "union all\n"
                    + "\n"
                    + "select rdb$dependencies.rdb$depended_on_name,\n"
                    + "       rdb$dependencies.rdb$depended_on_type,\n"
                    + "       'OTHER'\n"
                    + "from rdb$dependencies\n"
                    + "where rdb$dependencies.rdb$dependent_name = '" + procedure + "'\n"
                    + "and (rdb$dependencies.rdb$depended_on_type = 14\n"
                    + "or rdb$dependencies.rdb$depended_on_type = 15\n"
                    + "or rdb$dependencies.rdb$depended_on_type = 7)";

            try {
                ResultSet rs = firstConnection.execute(query, true).getResultSet();

                while (rs.next()) {
                    String obj1 = rs.getString(1).trim();
                    String obj2 = rs.getString(2).trim();
                    String obj3 = rs.getString(3).trim();
                    if (obj3 == ("DOMAIN")) {
                        depDomains.add(obj1);
                    } else if (obj3 == "FIELD") {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(obj1);
                        line.add(obj2);

                        depFields.add(line);
                    } else {
                        ArrayList<String> line = new ArrayList<String>();

                        line.add(obj1); // имя
                        line.add(obj2); // тип

                        dep.add(line);
                    }
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("procedure 243: " + e + query);
            }

            for (String d : depDomains) {

                boolean exist = false;

                query = "select rdb$fields.rdb$field_name\n"
                        + "from rdb$fields\n"
                        + "where rdb$fields.rdb$field_name = '" + d + "'";

                try {
                    ResultSet rs = secondConnection.execute(query, true).getResultSet();
                    while (rs.next()) {

                        exist = true;
                    }

                    rs.close();
                    secondConnection.releaseResources();

                } catch (java.sql.SQLException e) {
                    System.out.println("procedure 267: " + e + query);
                }

                if (!exist && !comparer.createdObjects.contains("domain " + d)) {
                    scriptPart = scriptPart + domain.create(d);

                    comparer.createdObjects.add("domain " + d);
                }
            }

            for (ArrayList<String> d : depFields) {
                String obj1 = d.get(0);
                String obj2 = d.get(1);
                scriptPart = scriptPart + dependencies.addFields(obj1, obj2);
            }

            for (ArrayList<String> d : dep) {
                String obj1 = d.get(0);
                String obj2 = d.get(1);
                scriptPart = scriptPart + dependencies.addDependencies(obj2, obj1);
            }

            scriptPart = scriptPart + "set term ^;\n\n" + "create or alter procedure \"" + procedure + "\"" + setParameters(info);

            scriptPart = scriptPart + "\nas\n" + "begin\n"
                    + "  /* Procedure Text */\n"
                    + "  suspend;\n"
                    + "end\n^\n\nset term ;^\n\n";

            comparer.createdObjects.add("procedure " + procedure);
        }

        procToFill.add(procedure);

        return scriptPart;
    }

    public String fill(String procedure) {
        String scriptPart = "";

        ProcedureParameters info = getParameters(firstConnection, procedure);

        scriptPart = "set term ^;\n\n" + "create or alter procedure \"" + procedure + "\"" + setParameters(info);

        scriptPart = scriptPart + "\nas\n" + getInfo(firstConnection, procedure) + "\n^\n\n" + "set term ;^\n\n";

        return scriptPart;
    }

    public String alter(String procedure) {
        String scriptPart = "";

        ProcedureParameters info = new ProcedureParameters();
        ProcedureParameters info2 = new ProcedureParameters();

        String code = getInfo(firstConnection, procedure);
        String code2 = getInfo(secondConnection, procedure);

        info = getParameters(firstConnection, procedure);
        info2 = getParameters(secondConnection, procedure);

        if (!info.inputParameters.equals(info2.inputParameters)
                || !info.outputParameters.equals(info2.outputParameters)
                //|| !code.equals(code2)) {
                || !replaceCode.compare_wo_r(code, code2)) {

            /*ArrayList<String> triggers = new ArrayList<String>();

             query = "select rdb$dependencies.rdb$dependent_name\n"
             + "from rdb$dependencies\n"
             + "inner join rdb$triggers on rdb$triggers.rdb$trigger_name = rdb$dependencies.rdb$dependent_name\n"
             + "where rdb$dependencies.rdb$dependent_type = 2\n"
             + "and rdb$triggers.rdb$trigger_type = 8192\n"
             + "and rdb$dependencies.rdb$depended_on_name = '" + procedure + "'\n"
             + "and rdb$triggers.rdb$trigger_inactive = 0";

             try {
             secondConnection.s = secondConnection.c.createStatement();
             rs = secondConnection.s.executeQuery(query);

             while (rs.next()) {

             triggers.add(rs.getString(1).trim());
             }

             rs.close();
             secondConnection.releaseResources();

             } catch (java.sql.SQLException e) {
             System.out.println("procedure 354" + e + query);
             }

             for (String t : triggers) {
             scriptPart = scriptPart + trigger.deactivate(t);

             try {
             firstConnection.s = firstConnection.c.createStatement();
             rs = firstConnection.s.executeQuery(query);

             while (rs.next()) {

             if (rs.getString(1).trim().equals(t)) {
             trigger.triggerToFill.add(t);
             }
             }

             rs.close();
             firstConnection.releaseResources();

             } catch (java.sql.SQLException e) {
             System.out.println("procedure 374" + e + query);
             }
             }*/
            scriptPart = scriptPart + create(procedure);

            procToFill.add(procedure);
            //scriptPart = scriptPart + fill(procedure);
/*
             ArrayList<ArrayList<String>> depFields = new ArrayList<>();
             ArrayList<String> depDomains = new ArrayList<String>();

             query = "select rdb$dependencies.rdb$depended_on_name,\n"
             + "       rdb$dependencies.rdb$field_name,\n"
             + "       'DOMAIN'\n"
             + "from rdb$dependencies\n"
             + "inner join rdb$fields on rdb$fields.rdb$field_name = rdb$dependencies.rdb$depended_on_name\n"
             + "where rdb$dependencies.rdb$dependent_name = '" + procedure + "'\n"
             + "\n"
             + "union all\n"
             + "\n"
             + "select rdb$dependencies.rdb$depended_on_name,\n"
             + "       rdb$dependencies.rdb$field_name,\n"
             + "       'FIELD'\n"
             + "from rdb$dependencies\n"
             + "where rdb$dependencies.rdb$dependent_name = '" + procedure + "'\n"
             + "and rdb$dependencies.rdb$field_name is not null";

             try {
             firstConnection.s = firstConnection.c.createStatement();
             rs = firstConnection.s.executeQuery(query);

             while (rs.next()) {

             if (rs.getString(3).trim().equals("DOMAIN")) {
             depDomains.add(rs.getString(1).trim());
             } else {
             ArrayList<String> line = new ArrayList<String>();

             line.add(rs.getString(1).trim());
             line.add(rs.getString(2).trim());

             depFields.add(line);
             }
             }

             rs.close();
             } catch (java.sql.SQLException e) {
             System.out.println("SQL ERROR");
             }

             for (String d : depDomains) {
             if (!comparer.createdObjects.contains("domain " + d)) {

             query = "select rdb$fields.rdb$field_name\n"
             + "from rdb$fields\n"
             + "where rdb$fields.rdb$field_name = '" + d + "'";

             try {
             secondConnection.s = secondConnection.c.createStatement();
             rs = secondConnection.s.executeQuery(query);

             boolean c = false;

             while (rs.next()) {
             c = true;
             }

             if (!c) {
             scriptPart = scriptPart + domain.create(d);
             comparer.createdObjects.add("domain " + d);
             }

             rs.close();
             } catch (java.sql.SQLException e) {
             System.out.println("SQL ERROR");
             }
             }
             }

             for (ArrayList<String> d : depFields) {
             scriptPart = scriptPart + dependencies.addFields(d.get(0), d.get(1));
             }

             scriptPart = scriptPart + "set term ^;\n\n" + "create or alter procedure \"" + procedure + "\"";

             scriptPart = scriptPart + setParameters(info);

             scriptPart = scriptPart + "\nas\n" + code + "^\n\nset term ;^\n\n";*/
        }

        comparer.alteredObjects.add("procedure " + procedure);

        return scriptPart;
    }

    public String empty(String procedure) {
        String scriptPart = "";

        ProcedureParameters info = new ProcedureParameters();

        info = getParameters(firstConnection, procedure);

        scriptPart = "set term ^;\n\ncreate or alter procedure " + procedure;

        scriptPart = scriptPart + setParameters(info);

        scriptPart = scriptPart + "\nas\n" + "begin\n"
                + "  /* Procedure Text */\n"
                + "  suspend;\n"
                + "end\n^\n\nset term ;^\n\n";

        return scriptPart;
    }

    public String drop(String procedure) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("procedure " + procedure)) {

            ArrayList<ArrayList<String>> dep = new ArrayList<>();

            query = "select rdb$dependencies.rdb$dependent_type,\n"
                    + "     rdb$dependencies.rdb$dependent_name\n"
                    + "from rdb$dependencies\n"
                    + "where rdb$dependencies.rdb$depended_on_name = '" + procedure + "'";

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
                System.out.println("procedure 463: " + e + query);
            }

            for (ArrayList<String> d : dep) {
                scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
            }

            scriptPart = scriptPart + "drop procedure \"" + procedure + "\";\n\n";

            comparer.droppedObjects.add("procedure " + procedure);
        }

        return scriptPart;
    }
}
