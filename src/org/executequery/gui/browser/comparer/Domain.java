package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Domain {
    public Domain(Comparer comp) {
        comparer = comp;
        init();

    }

    public void init() {
        firstConnection = comparer.compareConnection;
        secondConnection = comparer.masterConnection;
        procedure = comparer.procedure;
    }

    Procedure procedure;
    Comparer comparer;
    StatementExecutor firstConnection;
    StatementExecutor secondConnection;
    public String collect = "select rdb$fields.rdb$field_name\n"
            + "from rdb$fields\n"
            + "where (rdb$fields.rdb$system_flag = 0) and (rdb$fields.rdb$field_name not starting with 'RDB$')";

    private String query = "";

    public ArrayList<String> getInfo(StatementExecutor con, String domain) {
        ArrayList<String> info = new ArrayList<>();

        query = "select rdb$fields.rdb$field_name,\n" + //1
                "rdb$fields.rdb$field_type,\n" + //2
                "rdb$fields.rdb$character_length,\n" + //3
                "rdb$fields.rdb$field_sub_type,\n" + //4
                "rdb$fields.rdb$segment_length,\n" + //5
                "rdb$fields.rdb$field_precision,\n" + //6
                "abs(rdb$fields.rdb$field_scale),\n" + //7
                "rdb$fields.rdb$null_flag,\n" + //8
                "rdb$fields.rdb$default_source, \n" + //9
                "rdb$fields.rdb$validation_source \n" + //10
                "from rdb$fields\n"
                + "where rdb$fields.rdb$field_name = '" + domain + "';";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {
                info.add(replaceCode.replaceType(rs.getString(2).trim(),
                        replaceCode.noNull(rs.getString(6)).trim(),
                        rs.getString(7).trim(),
                        replaceCode.noNull(rs.getString(4)).trim()));
                info.add(replaceCode.replaceFieldLen(rs.getString(2).trim(),
                        replaceCode.noNull(rs.getString(3)).trim(),
                        replaceCode.noNull(rs.getString(4)).trim(),
                        replaceCode.noNull(rs.getString(5)).trim()));
                info.add(replaceCode.noNull(rs.getString(9)).trim().toLowerCase());
                info.add(replaceCode.noNull(rs.getString(8)).trim().equals("") ? "" : "not null");
                info.add(replaceCode.noNull(rs.getString(10)).trim().toLowerCase());
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("domain 62: " + e + query);
        }

        return info;
    }

    public String create(String domain) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("domain " + domain)) {
            scriptPart = "create domain \"" + domain + "\" as";

            ArrayList<String> info = getInfo(firstConnection, domain);

            for (int i = 0; i < info.size(); i++) {
                if (!info.get(i).equals("")) {

                    scriptPart = scriptPart + " " + info.get(i);
                }
            }

            scriptPart = scriptPart + ";\n\n";

            comparer.createdObjects.add("domain " + domain);
        }

        return scriptPart;
    }

    public String alter(String domain) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<>();
        ArrayList<String> info2 = new ArrayList<>();

        info = getInfo(firstConnection, domain);
        info2 = getInfo(secondConnection, domain);

        if (info.get(2).equals("") && !info2.get(2).equals("")) {
            scriptPart = "alter domain \"" + domain + "\" drop default;\n\n";
        } else if (!info.get(2).equals("") && info2.get(2).equals("")) {
            scriptPart = "alter domain \"" + domain + "\" set " + info.get(3) + ";\n\n";
        } else if (!info.get(2).equals("") && !info2.get(2).equals("") && !info.get(2).equals(info2.get(2))) {
            scriptPart = "alter domain \"" + domain + "\" set " + info.get(3) + ";\n\n";
        }

        if (!info.get(0).equals(info2.get(0))
                || !info.get(1).equals(info2.get(1))
                || !info.get(3).equals(info2.get(3))
                //|| !info.get(4).equals(info2.get(4))) {
                || !replaceCode.compare_wo_r(info.get(4), info2.get(4))) {

            query = "select rdb$fields.rdb$field_name,\n" + //1
                    "rdb$fields.rdb$field_type,\n" + //2
                    "rdb$fields.rdb$character_length,\n" + //3
                    "rdb$fields.rdb$field_sub_type,\n" + //4
                    "rdb$fields.rdb$segment_length,\n" + //5
                    "rdb$fields.rdb$field_precision,\n" + //6
                    "rdb$fields.rdb$field_scale,\n" + //7
                    "rdb$fields.rdb$null_flag,\n" + //8
                    "rdb$fields.rdb$field_length, \n" + //9
                    "rdb$fields.rdb$validation_source \n" + //10
                    "from rdb$fields\n"
                    + "where rdb$fields.rdb$field_name = '" + domain + "'";

            try {

                ResultSet rs = firstConnection.execute(query, true).getResultSet();

                while (rs.next()) {
                    String ch = replaceCode.noNull(rs.getString(10)).trim();
                    ch = ch.equals("") ? "null" : "'" + ch + "'";

                    scriptPart = scriptPart + "update rdb$fields set\n"
                            + "rdb$field_type = " + rs.getString(2) + ",\n"
                            + "rdb$field_length = " + rs.getString(9) + ",\n"
                            + "rdb$field_scale = " + rs.getString(7) + ",\n"
                            + "rdb$null_flag = " + rs.getString(8) + ",\n"
                            + "rdb$field_precision = " + rs.getString(6) + ",\n"
                            + "rdb$character_length = " + rs.getString(3) + ",\n"
                            + "rdb$field_sub_type = " + rs.getString(4) + ",\n"
                            + "rdb$segment_length = " + rs.getString(5) + ",\n"
                            + "rdb$validation_source = " + ch + "\n"
                            + "where (rdb$field_name = '" + domain + "');\n\n";
                }

                rs.close();
                firstConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("domain 151: " + e + query);
            }
        }

        return scriptPart;
    }

    public String drop(String domain) {
        String scriptPart = "";

        if (!comparer.droppedObjects.contains("domain " + domain)) {
            /*String warning = "Domain " + domain + " is used by: ";

             query = "select 'table',\n"
             + "       rdb$relation_fields.rdb$relation_name\n"
             + "from rdb$relation_fields\n"
             + "where rdb$relation_fields.rdb$field_source = 'D_DOMAIN'\n"
             + "\n"
             + "union all\n"
             + "\n"
             + "select 'procedure',\n"
             + "       rdb$procedure_parameters.rdb$procedure_name\n"
             + "from rdb$procedure_parameters\n"
             + "where rdb$procedure_parameters.rdb$field_source = '" + domain + "'";

             try {
             secondConnection.s = secondConnection.c.createStatement();
             rs = secondConnection.s.executeQuery(query);

             while (rs.next()) {
             warning = warning + rs.getString(1).trim() + " "
             + rs.getString(2).trim() + ", ";
             }

             rs.close();
             } catch (java.sql.SQLException e) {
             System.out.println("SQL ERROR");
             }*/

            ArrayList<ArrayList<String>> depProc = new ArrayList<>(); // процедуры для изменения

            query = "select rdb$procedure_parameters.rdb$procedure_name,\n"
                    + "       rdb$procedure_parameters.rdb$parameter_name\n"
                    + "from rdb$procedure_parameters\n"
                    + "where rdb$procedure_parameters.rdb$field_source = '" + domain + "'\n"
                    + "order by rdb$procedure_parameters.rdb$parameter_number";

            try {
                ResultSet rs = secondConnection.execute(query, true).getResultSet();

                ArrayList<String> line = new ArrayList<String>();

                while (rs.next()) {

                    line.add(new String());
                    line.set(0, rs.getString(1).trim()); // имя процедуры
                    line.add(rs.getString(2).trim()); // параметр
                }

                if (!line.isEmpty()) {
                    depProc.add(line);
                }

                rs.close();
                secondConnection.releaseResources();

            } catch (java.sql.SQLException e) {
                System.out.println("domain 219: " + e + query);
            }

            for (ArrayList<String> d : depProc) {

                if (!comparer.droppedObjects.contains("procedure " + d.get(0))
                        && !comparer.alteredObjects.contains("procedure " + d.get(0))) {
                    ProcedureParameters info = new ProcedureParameters();

                    info = procedure.getParameters(secondConnection, d.get(0));

                    for (int i = 1; i < d.size(); i++) {

                        for (int j = 0; j < info.inputParameters.size(); j++) {

                            String fN = info.inputParameters.get(j).get(0);

                            if (fN.equals(d.get(i))) {

                                query = "select rdb$fields.rdb$field_type,\n" //1
                                        + "       rdb$fields.rdb$field_length / 4,\n" //2
                                        + "       rdb$fields.rdb$field_scale,\n" //3
                                        + "       rdb$fields.rdb$field_sub_type,\n" //4
                                        + "       rdb$fields.rdb$field_precision,\n" //5
                                        + "       rdb$fields.rdb$segment_length\n" //6
                                        + "from rdb$fields\n"
                                        + "where rdb$fields.rdb$field_name = '" + domain + "'";

                                try {
                                    ResultSet rs = secondConnection.execute(query, true).getResultSet();

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
                                    System.out.println("domain 278: " + e + query);
                                }
                            }
                        }

                        for (int j = 0; j < info.outputParameters.size(); j++) {

                            String fN = info.outputParameters.get(j).get(0);

                            if (fN.equals(d.get(i))) {

                                query = "select rdb$fields.rdb$field_type,\n" //1
                                        + "       rdb$fields.rdb$field_length / 4,\n" //2
                                        + "       rdb$fields.rdb$field_scale,\n" //3
                                        + "       rdb$fields.rdb$field_sub_type,\n" //4
                                        + "       rdb$fields.rdb$field_precision,\n" //5
                                        + "       rdb$fields.rdb$segment_length\n" //6
                                        + "from rdb$fields\n"
                                        + "where rdb$fields.rdb$field_name = '" + domain + "'";

                                try {
                                    ResultSet rs = secondConnection.execute(query, true).getResultSet();

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
                                    System.out.println("domain 329: " + e + query);
                                }
                            }
                        }
                    }

                    scriptPart = scriptPart + "set term ^;\n\n" + "create or alter procedure \"" + d.get(0) + "\"" + procedure.setParameters(info)
                            + "\nas\n" + procedure.getInfo(secondConnection, d.get(0)) + "\n^\n\nset term ;^\n\n";
                }
            }

            scriptPart = scriptPart + "drop domain \"" + domain + "\";\n\n";

            comparer.droppedObjects.add("domain " + domain);
        }

        return scriptPart;
    }
}
