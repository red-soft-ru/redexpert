package org.executequery.gui.text;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseUser;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.util.SQLUtils;

import javax.swing.text.Document;

public class SimpleCommentPanel extends SimpleTextArea {

    private DatabaseObject object;

    public SimpleCommentPanel(DatabaseObject object) {
        this.object = object;
        resetComment();
    }

    public void saveComment() {

        if (object == null) {
            Log.debug("Couldn't save comment, database object is null");
            return;
        }

        String comment = getComment().trim();
        if (comment.isEmpty())
            comment = "NULL";

        DefaultStatementExecutor executor = new DefaultStatementExecutor();
        try {
            String metaTag = NamedObject.META_TYPES[object.getType()];

            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection(getSelectedConnection());

            String query;
            if (object instanceof DefaultDatabaseUser) {
                query = SQLUtils.generateComment(
                        object.getName(),
                        metaTag,
                        comment,
                        ((DefaultDatabaseUser) object).getPlugin(),
                        ";",
                        false,
                        getSelectedConnection()
                );

            } else {
                query = SQLUtils.generateComment(
                        object.getName(),
                        metaTag,
                        comment,
                        ";",
                        false,
                        getSelectedConnection()
                );
            }

            Log.info("Query created: " + query);

            SqlStatementResult result = executor.execute(QueryTypes.COMMENT, query);
            executor.getConnection().commit();

            if (result.isException()) {
                Log.error(result.getErrorMessage());
                GUIUtilities.displayWarningMessage("Error updating comment on table\n" + result.getErrorMessage());

            } else
                Log.info("Changes saved");

            object.reset();
            object.setRemarks(null);

        } catch (Exception e) {
            GUIUtilities.displayExceptionErrorDialog("Error updating comment on table", e, this.getClass());
            Log.error("Error updating comment on table", e);

        } finally {
            executor.releaseResources();
        }
    }

    public void resetComment() {
        setComment(object != null ? object.getRemarks() : null);
    }

    // ---

    private DatabaseConnection getSelectedConnection() {
        return object != null ? object.getHost().getDatabaseConnection() : null;
    }

    public void setDatabaseObject(DatabaseObject object) {
        this.object = object;
        resetComment();
    }

    public String getComment() {
        return getTextAreaComponent().getText();
    }

    public void setComment(String comment) {
        getTextAreaComponent().setText(comment);
    }

    public Document getDocument() {
        return getTextAreaComponent().getDocument();
    }

}
