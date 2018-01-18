package org.executequery.gui.databaseobjects;

import org.executequery.ActiveComponent;
import org.executequery.EventMediator;
import org.executequery.GUIUtilities;
import org.executequery.components.BottomButtonPanel;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.DefaultKeywordEvent;
import org.executequery.event.KeywordEvent;
import org.executequery.event.KeywordListener;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.procedure.CreateProcedureFunctionPanel;
import org.underworldlabs.jdbc.DataSourceException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Vasiliy Yashkov
 */
public class CreateProcedurePanel extends CreateProcedureFunctionPanel
        implements ActionListener,
        KeywordListener,
        ActiveComponent {

    /**
     * This objects title as an internal frame
     */
    public static final String TITLE = "Create Procedure";

    public static final String EDIT_TITLE = "Edit Procedure";

    /**
     * This objects icon as an internal frame
     */
    public static final String FRAME_ICON = "NewProcedure16.png";


    /**
     * <p> Constructs a new instance.
     */

    public CreateProcedurePanel(DatabaseConnection dc, ActionContainer parent) {
        this(dc, parent, null);

    }

    public CreateProcedurePanel(DatabaseConnection connection, ActionContainer parent, String procedure) {
        super(connection, parent, procedure);
        setFocusComponent();
    }

    /**
     * <p>Initializes the state of this instance.
     */
    private void jbInit() {

        addButtonsPanel(new BottomButtonPanel(
                this, "Create", "create-procedure", parent.isDialog()));
        setPreferredSize(new Dimension(750, 820));
        EventMediator.registerListener(this);
    }

    private void alterInit() {

        addButtonsPanel(new BottomButtonPanel(
                this, "Alter", "create-procedure", parent.isDialog()));
        setPreferredSize(new Dimension(750, 820));
        EventMediator.registerListener(this);
    }

    /**
     * Indicates that a [long-running] process has begun or ended
     * as specified. This may trigger the glass pane on or off
     * or set the cursor appropriately.
     *
     * @param inProcess - true | false
     */
    public void setInProcess(boolean inProcess) {
        if (parent != null) {

            if (inProcess) {

                parent.block();

            } else {

                parent.unblock();
            }
        }
    }

    /**
     * Notification of a new keyword added to the list.
     */
    public void keywordsAdded(KeywordEvent e) {
        outSqlText.setSQLKeywords(true);
    }

    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof DefaultKeywordEvent);
    }

    /**
     * Notification of a keyword removed from the list.
     */
    public void keywordsRemoved(KeywordEvent e) {
        outSqlText.setSQLKeywords(true);
    }

    public Vector<String> getColumnNamesVector(String tableName, String schemaName) {
        try {
            return metaData.getColumnNamesVector(tableName, schemaName);
        } catch (DataSourceException e) {
            GUIUtilities.displayExceptionErrorDialog(
                    "Error retrieving the column names for the " +
                            "selected table.\n\nThe system returned:\n" +
                            e.getExtendedMessage(), e);
            return new Vector<String>(0);
        }
    }

    /**
     * Releases database resources before closing.
     */
    public void cleanup() {
        EventMediator.deregisterListener(this);
        metaData.closeConnection();
    }

    /**
     * Action listener implementation.<br>
     * Executes the create table script.
     *
     * @param e event
     */
    public void actionPerformed(ActionEvent e) {

        DatabaseConnection dc = getSelectedConnection();
        if (dc == null) {
            GUIUtilities.displayErrorMessage(
                    "No database connection is available.");
            return;
        }
        createProcedure();
    }

    private void createProcedure() {
        try {
            String querys = getSQLText();
            displayExecuteQueryDialog(querys, "^");

        } catch (Exception exc) {
            GUIUtilities.displayExceptionErrorDialog("Error:\n" + exc.getMessage(), exc);
        }

    }

    public String toString() {
        return TITLE;
    }

    @Override
    protected void init_edited() {
        super.initEditing();
    }

    @Override
    public void create_object() {
        createProcedure();
    }

    @Override
    public String getCreateTitle() {
        return TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.PROCEDURE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        procedure = (String) databaseObject;
    }
}
