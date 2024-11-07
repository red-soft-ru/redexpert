package org.executequery.gui.editor.history;

import org.executequery.gui.editor.QueryEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing all opened instances of the <code>QueryEditor</code> class.
 *
 * @author Aleksey Kozlov
 */
public final class QueryEditorsManager {
    private static final List<QueryEditor> QE_INSTANCES = new ArrayList<>();

    /// Private constructor to prevent installation
    private QueryEditorsManager() {
    }

    /// Adds new instance of the <code>QueryEditor</code> class to the manager list.
    public static void register(QueryEditor editor) {
        QE_INSTANCES.add(editor);
    }

    /// Removes instance of the <code>QueryEditor</code> class from the manager list.
    public static void deregister(QueryEditor editor) {
        QE_INSTANCES.remove(editor);
    }

    /// Updates editor toolbar of the all registered <code>QueryEditor</code> class instances.
    public static void rebuildToolbars() {
        QE_INSTANCES.forEach(QueryEditor::rebuildToolBar);
    }

}
