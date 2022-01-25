package org.executequery.gui.databaseobjects;

import org.executequery.components.FileChooserDialog;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.NamedObject;
import org.executequery.databaseobjects.impl.DefaultDatabaseTablespace;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SimpleSqlTextPanel;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SQLUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CreateTablespacePanel extends AbstractCreateObjectPanel {
    public static final String CREATE_TITLE = getCreateTitle(NamedObject.TABLESPACE);
    public static final String EDIT_TITLE = getEditTitle(NamedObject.TABLESPACE);
    private FileChooserDialog fileChooserDialog;
    private JTextField fileField;
    private JButton fileButton;
    private SimpleSqlTextPanel sqlTextPanel;

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
        GridBagHelper gbh = new GridBagHelper();
        gbh.setDefaults(new GridBagConstraints(
                0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        gbh.defaults();
        centralPanel.setLayout(new GridBagLayout());
        centralPanel.add(new JLabel(Bundles.getCommon("file")), gbh.nextRowFirstCol().setLabelDefault().get());
        centralPanel.add(fileField, gbh.nextCol().setMaxWeightX().fillHorizontally().get());
        centralPanel.add(fileButton, gbh.nextCol().setLabelDefault().get());
        tabbedPane.add(sqlTextPanel);
        fileField.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        generateSQL();
    }

    @Override
    protected void initEdited() {
        nameField.setText(tablespace.getName());
        fileField.setText(tablespace.getAttribute(DefaultDatabaseTablespace.FILE_NAME));
        generateSQL();
    }

    protected String generateQuery() {
        if (editing) {
            StringBuilder sb = new StringBuilder();
            sb.append("ALTER");
            sb.append(" TABLESPACE ").append(MiscUtils.getFormattedObject(nameField.getText()));
            sb.append(" FILE '").append(fileField.getText()).append("'");
            sb.append(";\n");
            return sb.toString();
        } else
            return SQLUtils.generateCreateTablespace(nameField.getText(), fileField.getText());
    }

    private void generateSQL() {
        sqlTextPanel.setSQLText(generateQuery());
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
