package org.executequery.gui.databaseobjects;

import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.browser.depend.DependPanel;
import org.executequery.gui.browser.tree.TreePanel;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class CreateTablespacePanel extends AbstractCreateObjectPanel {

    public static final String CREATE_TITLE = getCreateTitle(NamedObject.TABLESPACE);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.TABLESPACE);

    private DefaultDatabaseTablespace tablespace;

    private JButton fileButton;
    private JTextField fileField;
    private FileChooserDialog fileChooser;
    private SimpleSqlTextPanel sqlTextPanel;

    public CreateTablespacePanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateTablespacePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    @Override
    protected void init() {

        fileField = new JTextField();
        fileChooser = new FileChooserDialog();
        sqlTextPanel = new SimpleSqlTextPanel();

        fileButton = new JButton("...");
        fileButton.addActionListener(e -> browseFile());

        tabbedPane.add("SQL", sqlTextPanel);
        addCommentTab(null);

        arrange();
        addListeners();
        generateSQL();
    }

    @Override
    protected void initEdited() {
        reset();
        nameField.setEditable(false);

        DependPanel tablesIndexesPanel = new DependPanel(TreePanel.TABLESPACE);
        tablesIndexesPanel.setDatabaseObject(tablespace);
        tablesIndexesPanel.setDatabaseConnection(tablespace.getHost().getDatabaseConnection());

        tabbedPane.insertTab(Bundles.getCommon("contents"), null, new JScrollPane(tablesIndexesPanel), null, 0);

        addCreateSqlTab(tablespace);
        tabbedPane.setSelectedIndex(0);
        simpleCommentPanel.setDatabaseObject(tablespace);
    }

    private void arrange() {

        centralPanel.setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().setInsets(5, 3, 0, 0).anchorNorthWest().fillHorizontally();
        centralPanel.add(new JLabel(Bundles.getCommon("file")), gbh.nextRowFirstCol().get());
        centralPanel.add(fileField, gbh.nextCol().topGap(0).setMaxWeightX().get());
        centralPanel.add(fileButton, gbh.nextCol().rightGap(5).setMinWeightX().get());
    }

    @Override
    protected String generateQuery() {
        return editing ? getGenerateAlterQuery() : getGenerateCreateQuery();
    }

    private String getGenerateCreateQuery() {
        return SQLUtils.generateCreateTablespace(
                nameField.getText(),
                fileField.getText(),
                simpleCommentPanel.getComment(),
                true,
                getDatabaseConnection()
        );
    }

    private String getGenerateAlterQuery() {
        return SQLUtils.generateAlterTablespace(
                nameField.getText(),
                fileField.getText(),
                simpleCommentPanel.getComment(),
                true,
                getDatabaseConnection()
        );
    }

    @Override
    public void createObject() {
        if (tabbedPane.getSelectedComponent() != sqlTextPanel)
            generateSQL();
        displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");
    }

    @Override
    protected void reset() {
        if (!editing)
            return;

        nameField.setText(tablespace.getName());
        fileField.setText(tablespace.getFileName());
        generateSQL();
    }

    @Override
    public String getCreateTitle() {
        return CREATE_TITLE;
    }

    @Override
    public String getEditTitle() {
        return EDIT_TITLE;
    }

    @Override
    public String getTypeObject() {
        return NamedObject.META_TYPES[NamedObject.TABLESPACE];
    }

    @Override
    public void setDatabaseObject(Object databaseObject) {
        tablespace = (DefaultDatabaseTablespace) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {
    }

    private void addListeners() {

        DocumentListener documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                generateSQL();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                generateSQL();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                generateSQL();
            }
        };

        nameField.getDocument().addDocumentListener(documentListener);
        fileField.getDocument().addDocumentListener(documentListener);
    }

    private void browseFile() {
        int returnVal = fileChooser.showOpenDialog(fileButton);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            fileField.setText(file.getAbsolutePath());
        }
    }

    private void generateSQL() {
        sqlTextPanel.setSQLText(generateQuery());
    }

}
