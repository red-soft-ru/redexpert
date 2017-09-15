package org.executequery.gui.browser.comparer;

public class Role {
    public Role()
    {}

    public   String collect = "select rdb$roles.rdb$role_name\n"
            + "from rdb$roles\n"
            + "where rdb$roles.rdb$system_flag = 0";

    private  String query = "";

    public  String create(String role) {
        String scriptPart = "";

        scriptPart = "create role \"" + role + "\";\n\n";

        return scriptPart;
    }

    public  String drop(String role) {
        String scriptPart = "";

        scriptPart = "drop role \"" + role + "\";\n\n";

        return scriptPart;
    }
}

