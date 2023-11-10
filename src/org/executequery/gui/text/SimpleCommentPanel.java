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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

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
    /**
     * update button action listener
     */
    private ActionListener updateButtonActionListener;

    public SimpleCommentPanel(DatabaseObject object) {
        currentDatabaseObject = object;
        init();
        resetComment();
    }

    private void init() {

        commentField = new SimpleTextArea();
        updateButtonActionListener = e -> updateComment();

        updateCommentButton = new RolloverButton();
        updateCommentButton.setIcon(GUIUtilities.loadIcon("Commit16.svg"));
        updateCommentButton.addActionListener(updateButtonActionListener);
        updateCommentButton.setEnabled(currentDatabaseObject != null);

        rollbackCommentButton = new RolloverButton();
        rollbackCommentButton.setIcon(GUIUtilities.loadIcon("Rollback16.svg"));
        rollbackCommentButton.addActionListener(e -> resetComment());

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
        if (currentDatabaseObject != null) {
            DefaultStatementExecutor executor = new DefaultStatementExecutor();

            String comment = commentField.getTextAreaComponent().getText().trim();
            if (comment.equals(""))
                comment = "NULL";

            try {
                String metaTag = NamedObject.META_TYPES[currentDatabaseObject.getType()];


                executor.setCommitMode(false);
                executor.setKeepAlive(true);
                executor.setDatabaseConnection(getSelectedConnection());

                String request = SQLUtils.generateComment(currentDatabaseObject.getName(), metaTag,
                        comment, ";", false, getSelectedConnection());

                Log.info("Query created: " + request);

                SqlStatementResult result = executor.execute(QueryTypes.COMMENT, request);
                executor.getConnection().commit();

                if (result.isException()) {
                    Log.error(result.getErrorMessage());
                    GUIUtilities.displayWarningMessage("Error updating comment on table\n" + result.getErrorMessage());
                } else
                    Log.info("Changes saved");

                currentDatabaseObject.reset();
                currentDatabaseObject.setRemarks(null);


            } catch (Exception e) {

                GUIUtilities.displayExceptionErrorDialog("Error updating comment on table", e);
                Log.error("Error updating comment on table", e);

            } finally {

                executor.releaseResources();
            }
        } else Log.error("databaseObject is null");

    }

    public void resetComment() {
        if (currentDatabaseObject != null)
            setComment(currentDatabaseObject.getRemarks());
        else setComment("");
    }

    private DatabaseConnection getSelectedConnection() {
        return currentDatabaseObject.getHost().getDatabaseConnection();
    }

    public JPanel getCommentPanel() {
        return commentPanel;
    }

    public void updateComment() {
        saveComment();
    }

    public SimpleTextArea getCommentField() {
        return commentField;
    }

    public void setDatabaseObject(DatabaseObject databaseObject) {
        this.currentDatabaseObject = databaseObject;
        updateCommentButton.setEnabled(currentDatabaseObject != null);
        resetComment();
    }

    public void addActionForCommentUpdateButton(Consumer<ActionEvent> additionalAction) {

        updateCommentButton.removeActionListener(updateButtonActionListener);

        updateButtonActionListener = e -> {
            updateComment();
            additionalAction.accept(e);
        };

        updateCommentButton.addActionListener(updateButtonActionListener);
    }

    public String getComment() {
        return getCommentField().getTextAreaComponent().getText();
    }

    public void setComment(String comment) {
        getCommentField().getTextAreaComponent().setText(comment);
    }
}
