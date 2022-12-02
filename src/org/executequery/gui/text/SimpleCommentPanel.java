package org.executequery.gui.text;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databasemediators.QueryTypes;
import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseObject;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.log.Log;
import org.executequery.sql.SqlStatementResult;
import org.underworldlabs.swing.RolloverButton;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;

public class SimpleCommentPanel {

    /**
     * current database object
     */
    private final DatabaseObject currentDatabaseObject;
    /**
     * comment main panel
     */
    private JPanel commentPanel;
    /**
     * comment text field
     */
    private SimpleTextArea commentField;
    /**
     * update comment button
     */
    private RolloverButton updateCommentButton;
    /**
     * update comment button
     */
    private RolloverButton rollbackCommentButton;

    public SimpleCommentPanel(DatabaseObject object) {
        currentDatabaseObject = object;
        init();
        removeComment();
    }

    private void init() {

        commentField = new SimpleTextArea();

        updateCommentButton = new RolloverButton();
        updateCommentButton.setIcon(GUIUtilities.loadIcon("Commit16.png"));

        rollbackCommentButton = new RolloverButton();
        rollbackCommentButton.setIcon(GUIUtilities.loadIcon("Rollback16.png"));
        rollbackCommentButton.addActionListener(e -> removeComment());

        GridBagHelper gridBagHelper = new GridBagHelper();
        commentPanel = new JPanel(new GridBagLayout());

        commentPanel.add(updateCommentButton,
                gridBagHelper.setInsets(2, 2, 2, 2).anchorNorthWest().setLabelDefault().get());
        commentPanel.add(rollbackCommentButton,
                gridBagHelper.nextCol().nextCol().get());
        commentPanel.add(commentField,
                gridBagHelper.nextRowFirstCol().fillBoth().spanX().spanY().setWidth(3).setMaxWeightX().get());

    }

    private void saveComment() {

        DefaultStatementExecutor executor = new DefaultStatementExecutor();

        String comment = commentField.getTextAreaComponent().getText().trim();
        if (comment.equals(""))
            comment = "NULL";

        try {

            String metaTag = "";

            if (currentDatabaseObject.getType() == NamedObject.VIEW)
                metaTag = "VIEW";
            else if (currentDatabaseObject.getType() == NamedObject.TABLE)
                metaTag = "TABLE";
            else if (currentDatabaseObject.getType() == NamedObject.PROCEDURE)
                metaTag = "PROCEDURE";
            else if (currentDatabaseObject.getType() == NamedObject.FUNCTION)
                metaTag = "FUNCTION";

            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection(getSelectedConnection());

            String request = SQLUtils.generateComment(currentDatabaseObject.getName(), metaTag,
                    comment, ";", false);

            Log.info("Query created: " + request);

            SqlStatementResult result = executor.execute(QueryTypes.COMMENT, request);
            executor.getConnection().commit();

            if (result.isException()) {
                Log.error(result.getErrorMessage());
                GUIUtilities.displayWarningMessage("Error updating comment on table\n" + result.getErrorMessage());
            }
            else
                Log.info("Changes saved");

            currentDatabaseObject.reset();
            currentDatabaseObject.setRemarks(null);

        } catch (Exception e) {

            GUIUtilities.displayExceptionErrorDialog("Error updating comment on table", e);
            Log.error("Error updating comment on table", e);

        } finally {

            executor.releaseResources();
        }

    }

    private void removeComment() {
        commentField.getTextAreaComponent().setText(currentDatabaseObject.getRemarks());
    }

    private DatabaseConnection getSelectedConnection() {
        return currentDatabaseObject.getHost().getDatabaseConnection();
    }

    public JPanel getCommentPanel() {
        return commentPanel;
    }

    public RolloverButton getCommentUpdateButton() {
        return updateCommentButton;
    }

    public void updateComment() {
        saveComment();
    }

}
