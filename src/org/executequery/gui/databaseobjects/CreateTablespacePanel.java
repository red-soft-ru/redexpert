package org.executequery.gui.databaseobjects;

import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CreateTablespacePanel extends AbstractCreateObjectPanel {
    public static final String CREATE_TITLE = "Create tablespace";
    public static final String EDIT_TITLE = "Edit tablespace";
    private FileChooserDialog fileChooserDialog;
    private JTextField fileField;
    private JButton fileButton;
    private SimpleSqlTextPanel sqlTextPanel;
    private JPanel mainPanel;

    public CreateTablespacePanel(DatabaseConnection dc, ActionContainer dialog) {
        this(dc, dialog, null);
    }

    public CreateTablespacePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject) {
        super(dc, dialog, databaseObject);
    }

    public CreateTablespacePanel(DatabaseConnection dc, ActionContainer dialog, Object databaseObject, Object[] params) {
        super(dc, dialog, databaseObject, params);
    }

    @Override
    protected void init() {
        mainPanel = new JPanel();
        sqlTextPanel = new SimpleSqlTextPanel();
        fileChooserDialog = new FileChooserDialog();
        fileField = new JTextField();
        fileButton = new JButton("...");
        fileButton.addActionListener(new ActionListener() {
            final FileChooserDialog fileChooser = new FileChooserDialog();

            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileButton);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    fileField.setText(file.getAbsolutePath());
                }
            }
        });

        mainPanel.setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        gbh.defaults();
        mainPanel.add(new JLabel("File:"), gbh.nextRowFirstCol().setLabelDefault().get());
        mainPanel.add(fileField, gbh.nextCol().setMaxWeightX().fillHorizontally().get());
        mainPanel.add(fileButton, gbh.nextCol().setLabelDefault().get());
        tabbedPane.add("File", mainPanel);
        tabbedPane.add("SQL", sqlTextPanel);
        tabbedPane.addChangeListener(changeEvent -> {
            if (tabbedPane.getSelectedComponent() == sqlTextPanel) {
                generateSQL();
            }
        });

    }

    @Override
    protected void initEdited() {
        nameField.setText(tablespace.getName());
        fileField.setText(tablespace.getAttribute(DefaultDatabaseTablespace.FILE_NAME));
    }

    private void generateSQL() {
        if (editing) {
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER");
            sb.append(" TABLESPACE ").append(MiscUtils.getFormattedObject(nameField.getText()));
            sb.append(" FILE '").append(fileField.getText()).append("'");
            sb.append(";\n");
            sqlTextPanel.setSQLText(sb.toString());
        } else
            sqlTextPanel.setSQLText(SQLUtils.generateCreateTablespace(nameField.getText(), fileField.getText()));
    }

    @Override
    public void createObject() {
        if (tabbedPane.getSelectedComponent() != sqlTextPanel) {
            generateSQL();
        }
        displayExecuteQueryDialog(sqlTextPanel.getSQLText(), ";");


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

    private DefaultDatabaseTablespace tablespace;

    @Override
    public void setDatabaseObject(Object databaseObject) {
        tablespace = (DefaultDatabaseTablespace) databaseObject;
    }

    @Override
    public void setParameters(Object[] params) {

    }
}
