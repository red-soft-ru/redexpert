package org.executequery.gui.browser.comparer;

import java.util.ArrayList;
import java.util.Collections;

public class replaceCode {

    public static boolean compare_wo_r(String fLine, String sLine) {

        fLine = fLine.replaceAll("\r", "");
        sLine = sLine.replaceAll("\r", "");

        if (fLine.equals(sLine)) {
            return true;
        }

        return false;
    }

    public static String noNull(String testLine) {
        String result = testLine;

        if (testLine == null) {
            result = "";
        }

        return result;
    }

    public static String replaceType(String type, String precision, String scale, String subtype) {
        switch (type) {
            case "7":
                return "smallint";
            case "8":
                if (precision.equals("0") && scale.equals("0")) {
                    return "integer";
                } else if (subtype.equals("1")) {
                    return "numeric(" + precision + ", " + scale + ")";
                } else if (subtype.equals("2")) {
                    return "decimal(" + precision + ", " + scale + ")";
                }
            case "10":
                return "float";
            case "12":
                return "date";
            case "13":
                return "time";
            case "14":
                return "char";
            case "16":
                if (precision.equals("0") && scale.equals("0")) {
                    return "bigint";
                } else if (subtype.equals("1")) {
                    return "numeric(" + precision + ", " + scale + ")";
                } else if (subtype.equals("2")) {
                    return "decimal(" + precision + ", " + scale + ")";
                }
            case "27":
                return "double precision";
            case "35":
                return "timestamp";
            case "37":
                return "varchar";
            case "40":
                return "cstring";
            case "45":
                return "blob_id";
            default:
                return "blob";
        }
    }

    public static String replaceFieldLen(String type, String leng, String subType, String segmentSize) {
        switch (type) {
            case "14":
                return "(" + leng + ")";
            case "37":
                return "(" + leng + ")";
            case "40":
                return "(" + leng + ")";
            case "261":
                return " sub_type " + subType + " segment size " + segmentSize;
            default:
                return "";
        }
    }

    public static String replacePrivilege(String privilege) {
        switch (privilege) {
            case "D":
                return "delete on ";
            case "S":
                return "select on ";
            case "U":
                return "update on ";
            case "I":
                return "insert on ";
            case "R":
                return "reference on ";
            case "X":
                return "execute on ";
            default:
                return "";
        }
    }

    public static String replaceTriggerType(String type) {
        switch (type) {
            case "1":
                return "before insert";
            case "2":
                return "after insert";
            case "3":
                return "before update";
            case "4":
                return "after update";
            case "5":
                return "before delete";
            case "6":
                return "after delete";
            case "17":
                return "before insert or update";
            case "18":
                return "after insert or update";
            case "25":
                return "before insert or delete";
            case "26":
                return "after insert or delete";
            case "27":
                return "before update or delete";
            case "28":
                return "after update or delete";
            case "113":
                return "before insert or update or delete";
            case "114":
                return "after insert or update or delete";
            case "8192":
                return "on connect";
            case "8193":
                return "on disconnect";
            case "8194":
                return "on transaction start";
            case "8195":
                return "on transaction commit";
            case "11":
                return "before insert or update";
            case "22":
                return "after update or delete";
            default:
                return "on transaction rollback";
        }
    }

    public static ArrayList<String> computedFieldsSort(ArrayList<ArrayList<String>> list) {
        ArrayList<String> listChange = new ArrayList<String>();

        for (int i = 0; i < list.size(); i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(i).get(0).equals(list.get(j).get(1))) {
                    Collections.swap(list, i, j);
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
            if (!listChange.contains(list.get(i).get(1))) {
                listChange.add(list.get(i).get(1));
            }
        }

        return listChange;
    }
}

