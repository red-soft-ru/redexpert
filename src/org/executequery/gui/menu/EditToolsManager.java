package org.executequery.gui.menu;

import org.executequery.GUIUtilities;
import org.executequery.gui.editor.QueryEditor;

import javax.swing.*;
import java.util.Map;

public class EditToolsManager {

    private static Map<String, JMenuItem> menuMap;

    public static void checkEnable() {
        enableTextFunction();
        enableSaveFunction();
        enablePrintFunction();
        enableEditorFunction();
    }

    private static void enableTextFunction() {
        boolean enable = isTextFunctionInFocus();

        getMenuMap().get("cut-command").setEnabled(enable);
        getMenuMap().get("copy-command").setEnabled(enable);
        getMenuMap().get("paste-command").setEnabled(enable);
        getMenuMap().get("find-command").setEnabled(enable);
        getMenuMap().get("replace-command").setEnabled(enable);
        GUIUtilities.getExecuteQueryMenu().getMenu(1).getItem(15).setEnabled(enable); // change text case commands
    }

    private static void enableSaveFunction() {
        getMenuMap().get("save-as-command").setEnabled(isSaveFunctionInFocus());
    }

    private static void enablePrintFunction() {
        getMenuMap().get("print-command").setEnabled(isPrintFunctionInFocus());
    }

    private static void enableEditorFunction() {
        boolean enable = isQueryEditorInFocus();

        getMenuMap().get("goto-command").setEnabled(enable);
        GUIUtilities.getExecuteQueryMenu().getMenu(1).getItem(13).setEnabled(enable); // move text commands
        GUIUtilities.getExecuteQueryMenu().getMenu(1).getItem(14).setEnabled(enable); // duplicate text commands
    }

    private static boolean isTextFunctionInFocus() {
        return GUIUtilities.getTextEditorInFocus() != null;
    }

    private static boolean isSaveFunctionInFocus() {
        return GUIUtilities.getSaveFunctionInFocus() != null;
    }

    private static boolean isPrintFunctionInFocus() {
        return GUIUtilities.getPrintableInFocus() != null;
    }

    private static boolean isQueryEditorInFocus() {
        return GUIUtilities.getSelectedCentralPane() instanceof QueryEditor;
    }

    private static Map<String, JMenuItem> getMenuMap() {
        if (menuMap == null)
            menuMap = GUIUtilities.getExecuteQueryMenu().getMenuMap();
        return menuMap;
    }

}
