package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseSequence;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleTextArea;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class CreateGeneratorPanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.SEQUENCE);
    public static final String ALTER_TITLE = getEditTitle(NamedObject.SEQUENCE);
    private NumberTextField startValueText;
    private NumberTextField currentValueText;
    private NumberTextField incrementText;
    private SimpleTextArea description;
    private DefaultDatabaseSequence generator;

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateGeneratorPanel(DatabaseConnection dc, ActionContainer dialog, DefaultDatabaseSequence generator) {
        super(dc, dialog, generator);
    }

    protected void initEdited() {
        reset();
        addPrivilegesTab(tabbedPane);
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

        this.description = new SimpleTextArea();

        // ----- preparing panel layout -----

        centralPanel.setLayout(new GridBagLayout());

        GridBagHelper gridBagHelper = new GridBagHelper();
        gridBagHelper.setInsets(5, 5, 5, 5);
        gridBagHelper.anchorNorthWest().fillHorizontally();

        // ----- components arranging -----

        gridBagHelper.addLabelFieldPair(centralPanel,
                bundleString("start-value"), startValueText,
                null, true, true);

        if (editing)
            gridBagHelper.addLabelFieldPair(centralPanel,
                    bundleString("current-value"), currentValueText,
                    null, true, true);

        if (getDatabaseVersion() >= 3)
            gridBagHelper.addLabelFieldPair(centralPanel,
                    bundleString("increment"), incrementText,
                    null, true, true);

        tabbedPane.add(bundleStaticString("description"), description);

    }

    int getVersion() throws SQLException {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        return host.getDatabaseMetaData().getDatabaseMajorVersion();
    }

    protected String generateQuery() {

        String query = "";
        try {
            query = SQLUtils.generateCreateSequence(getFormattedName(), startValueText.getStringValue(),
                    incrementText.getStringValue(), description.getTextAreaComponent().getText(), getVersion(), editing);

        } catch (SQLException e) { e.printStackTrace(); }

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
        nameField.setEnabled(false);
        startValueText.setLongValue(generator.getSequenceFirstValue());
        currentValueText.setLongValue(generator.getSequenceCurrentValue());
        if (getDatabaseVersion() >= 3)
            incrementText.setValue(generator.getIncrement());
        description.getTextAreaComponent().setText(generator.getRemarks());
    }
}
