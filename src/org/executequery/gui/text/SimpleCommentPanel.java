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
    private DatabaseObject currentDatabaseObject;
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
        updateCommentButton.addActionListener(e -> saveComment());

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

        try {

            String metaTag = "";

            if (currentDatabaseObject.getType() == NamedObject.VIEW)
                metaTag = "VIEW";
            else if (currentDatabaseObject.getType() == NamedObject.PROCEDURE)
                metaTag = "PROCEDURE";
            else if (currentDatabaseObject.getType() == NamedObject.FUNCTION)
                metaTag = "FUNCTION";

            executor.setCommitMode(false);
            executor.setKeepAlive(true);
            executor.setDatabaseConnection(getSelectedConnection());

            String request = SQLUtils.generateComment(currentDatabaseObject.getName(), metaTag,
                    commentField.getTextAreaComponent().getText().trim(), ";");

            Log.info("Request created: " + request);

            SqlStatementResult result = executor.execute(QueryTypes.COMMENT, request);
            executor.getConnection().commit();

            if (result.isException())
                Log.error(result.getErrorMessage());
            else
                Log.info("Changes saved");

        } catch (Exception e) {

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

}
