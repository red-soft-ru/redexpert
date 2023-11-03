package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.gui.ActionContainer;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import java.sql.SQLException;

public class CreateGeneratorPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.SEQUENCE);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.SEQUENCE);
    private NumberTextField startValueText;
    private NumberTextField currentValueText;
    private NumberTextField incrementText;
    private DefaultDatabaseSequence generator;

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseSequence generator) {
        super(dc, dialog, generator);
    }

    protected void initEdited() {
        reset();
        if (parent == null)
            addPrivilegesTab(tabbedPane, generator);
        addDependenciesTab(generator);
        addCreateSqlTab(generator);
    }

    @Override
    public void createObject() {
        try {
            createGenerator();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return ALTER_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.SEQUENCE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        generator = (DefaultDatabaseSequence) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }

    protected void init() {

        // ----- components defining -----

        startValueText = new NumberTextField();
        startValueText.setValue(0);

        currentValueText = new NumberTextField();
        currentValueText.setEditable(false);
        currentValueText.setValue(0);

        incrementText = new NumberTextField();
        incrementText.setValue(1);


        // ----- preparing panel layout -----

        centralPanel.setVisible(false);


        // ----- components arranging -----

        if (getDatabaseVersion() >= 3) {
            topGbh.addLabelFieldPair(topPanel,
                    bundleString("start-value"), startValueText,
                    null, true, false);

            topGbh.addLabelFieldPair(topPanel,
                    bundleString("increment"), incrementText,
                    null, false, true);
        }
        if (editing)
            topGbh.addLabelFieldPair(topPanel,
                    bundleString("current-value"), currentValueText,
                    null, true, true);


        addCommentTab(null);

    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    protected String generateQuery() {

        String query = "";
        try {
            query = SQLUtils.generateCreateSequence(nameField.getText(), Long.parseLong(startValueText.getStringValue()),
                    Long.parseLong(incrementText.getStringValue()), simpleCommentPanel.getComment(), getVersion(), editing, getDatabaseConnection());

        } catch (SQLException e) {
            GUIUtilities.displayExceptionErrorDialog(e.getMessage(), e);
            e.printStackTrace();
        }

        return query;
    }

    private void createGenerator() throws SQLException {
        if (!MiscUtils.isNull(nameField.getText().trim())) {
            displayExecuteQueryDialog(generateQuery(), ";");
        } else
            GUIUtilities.displayErrorMessage("Name can not be empty");
    }

    protected void reset() {
        nameField.setText(generator.getName().trim());
        nameField.setEditable(false);
        if (getDatabaseVersion() >= 3)
            startValueText.setLongValue(generator.getSequenceFirstValue());
        currentValueText.setLongValue(generator.getSequenceCurrentValue());
        if (getDatabaseVersion() >= 3)
            incrementText.setValue(generator.getIncrement());
        simpleCommentPanel.setDatabaseObject(generator);
    }
}
