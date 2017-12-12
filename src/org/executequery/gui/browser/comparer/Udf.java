package org.executequery.gui.browser.comparer;

import org.executequery.databasemediators.spi.StatementExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;

public class Udf {
    public Udf(Comparer comp) {
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
    public String collect = "select rdb$functions.rdb$function_name\n"
            + "from rdb$functions\n"
            + "where rdb$functions.rdb$system_flag = 0";

    private String query = "";

    public ArrayList<String> getInfo(StatementExecutor con, String udf) {
        ArrayList<String> info = new ArrayList<>();
        String position = "";

        query = "select rdb$functions.rdb$module_name,\n" + //1
                "       rdb$functions.rdb$entrypoint,\n" + //2
                "       rdb$functions.rdb$return_argument\n" + //3
                "from rdb$functions\n"
                + "where rdb$functions.rdb$function_name = '" + udf + "'";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {
                info.add(replaceCode.noNull(rs.getString(1)).trim());
                info.add(replaceCode.noNull(rs.getString(2)).trim());

                position = rs.getString(3).trim();
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("udf 49" + e + query);
        }

        query = "select rdb$function_arguments.rdb$field_type,\n" + //1
                "       rdb$function_arguments.rdb$field_scale,\n" + //2
                "       rdb$function_arguments.rdb$field_length,\n" + //3
                "       rdb$function_arguments.rdb$field_sub_type,\n" + //4
                "       rdb$function_arguments.rdb$field_precision,\n" + //5
                "       rdb$function_arguments.rdb$character_length,\n" + //6
                "       rdb$function_arguments.rdb$mechanism\n" + //7
                "from rdb$function_arguments\n"
                + "where rdb$function_arguments.rdb$function_name = '" + udf + "' and\n"
                + "      rdb$function_arguments.rdb$argument_position = " + position;

        String udfParameters = "";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {

                if (!rs.getString(1).trim().equals("261")) {
                    udfParameters = udfParameters + replaceCode.replaceType(rs.getString(1).trim(),
                            replaceCode.noNull(rs.getString(5)).trim(),
                            replaceCode.noNull(rs.getString(2)).trim(),
                            replaceCode.noNull(rs.getString(4)).trim());
                    if (rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")) {
                        udfParameters = udfParameters + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                rs.getString(3).trim(),
                                rs.getString(4).trim(), "0");
                        /*udfParameters = udfParameters + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                         rs.getString(6).trim(),
                         rs.getString(4).trim(), "0");*/
                    }

                    switch (rs.getString(7).trim()) {
                        case "0":
                            udfParameters = udfParameters + " by value";
                            break;
                        /*case "1":
                         udfParameters = udfParameters + " by reference";
                         break;*/
                        case "2":
                            udfParameters = udfParameters + " by descriptor";
                            break;
                        default:
                            break;
                    }
                } else {
                    udfParameters = "blob";

                    if (rs.getString(7).trim().equals("2")) {
                        udfParameters = udfParameters + " by descriptor";
                    }
                }
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("udf 111" + e + query);
        }

        info.add(udfParameters);

        query = "select rdb$function_arguments.rdb$field_type,\n"
                + "       rdb$function_arguments.rdb$field_scale,\n"
                + "       rdb$function_arguments.rdb$field_length,\n"
                + "       rdb$function_arguments.rdb$field_sub_type,\n"
                + "       rdb$function_arguments.rdb$field_precision,\n"
                + "       rdb$function_arguments.rdb$character_length\n"
                + "from rdb$function_arguments\n"
                + "where rdb$function_arguments.rdb$function_name = '" + udf + "' and\n"
                + "rdb$function_arguments.rdb$argument_position <> " + position + "\n"
                + "order by rdb$function_arguments.rdb$argument_position";

        udfParameters = "";

        try {
            ResultSet rs = con.execute(query, true).getResultSet();

            while (rs.next()) {

                if (!rs.getString(1).trim().equals("261")) {
                    udfParameters = udfParameters + replaceCode.replaceType(rs.getString(1).trim(),
                            replaceCode.noNull(rs.getString(5)).trim(),
                            replaceCode.noNull(rs.getString(2)).trim(),
                            replaceCode.noNull(rs.getString(4)).trim());
                    if (rs.getString(1).trim().equals("40") || rs.getString(1).trim().equals("14") || rs.getString(1).trim().equals("37")) {
                        udfParameters = udfParameters + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                                rs.getString(3).trim(),
                                rs.getString(4).trim(), "0");
                        /*udfParameters = udfParameters + replaceCode.replaceFieldLen(rs.getString(1).trim(),
                         rs.getString(6).trim(),
                         rs.getString(4).trim(), "0");*/
                    }

                } else {
                    udfParameters = "blob";
                }

                udfParameters = udfParameters + ", ";
            }

            rs.close();
            con.releaseResources();

        } catch (java.sql.SQLException e) {
            System.out.println("udf 160" + e + query);
        }

        if (!udfParameters.equals("")) {
            udfParameters = udfParameters.substring(0, udfParameters.lastIndexOf(", "));
        }

        info.add(udfParameters);

        return info;
    }

    public String create(String udf) {
        String scriptPart = "";

        if (!comparer.createdObjects.contains("udf " + udf)) {
            ArrayList<String> info = new ArrayList<>();

            info = getInfo(firstConnection, udf);

            scriptPart = "declare external function \"" + udf + "\"";

            if (!info.get(3).equals("")) {
                scriptPart = scriptPart + "\n    " + info.get(3);
            }

            scriptPart = scriptPart + "\nreturns " + info.get(2);
            scriptPart = scriptPart + "\nentry_point '" + info.get(1) + "' module_name '" + info.get(0) + "';\n\n";

            comparer.createdObjects.add("udf " + udf);
        }

        return scriptPart;
    }

    public String alter(String udf) {
        String scriptPart = "";

        ArrayList<String> info = new ArrayList<String>();
        ArrayList<String> info2 = new ArrayList<String>();

        info = getInfo(firstConnection, udf);
        info2 = getInfo(secondConnection, udf);

        if (!info.equals(info2)) {
            if (!info.get(2).equals(info2.get(2)) || !info.get(3).equals(info2.get(3))) {
                scriptPart = drop(udf) + create(udf);
            } else {

                scriptPart = "alter external function \"" + udf + "\"";

                scriptPart = scriptPart + "\nentry_point '" + info.get(1) + "' module_name '" + info.get(0) + "';\n\n";
            }
        }

        return scriptPart;
    }

    public String drop(String udf) {
        String scriptPart = "";

        ArrayList<ArrayList<String>> dep = new ArrayList<>();

        query = "select rdb$dependencies.rdb$dependent_type,\n"
                + "     rdb$dependencies.rdb$dependent_name\n"
                + "from rdb$dependencies\n"
                + "where rdb$dependencies.rdb$depended_on_name = '" + udf + "'";

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
            System.out.println("udf 245" + e + query);
        }

        for (ArrayList<String> d : dep) {
            scriptPart = scriptPart + dependencies.clearDependencies(d.get(0), d.get(1));
        }

        scriptPart = scriptPart + "drop external function \"" + udf + "\";\n\n";

        return scriptPart;

    }
}

