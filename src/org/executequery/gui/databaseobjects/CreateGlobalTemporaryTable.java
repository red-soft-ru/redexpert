package org.executequery.gui.databaseobjects;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.table.CreateTablePanel;

public class CreateGlobalTemporaryTable extends CreateTablePanel {
    /**
     * <p> Constructs a new instance.
     *
     * @param dc
     * @param dialog
     */
    public CreateGlobalTemporaryTable(DatabaseConnection dc, ActionContainer dialog) {
        super(dc, dialog);
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.GLOBAL_TEMPORARY];
    }
}
