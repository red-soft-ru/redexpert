package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.FocusablePanel;
import org.executequery.gui.WidgetFactory;
import org.executequery.gui.editor.autocomplete.AutoCompletePopupProvider;
import org.executequery.gui.editor.autocomplete.DefaultAutoCompletePopupProvider;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class CreateViewPanel extends JPanel implements FocusListener {
    public static final String TITLE = "Create View";
    private static final String AUTO_COMPLETE_POPUP_ACTION_KEY = "autoCompletePopupActionKey";
    DatabaseConnection connection;
    ActionContainer parent;
    JComboBox connectionsCombo;
    JScrollPane sqlTextScroll;
    SQLTextPane sqlTextView;
    JButton okButton;
    JButton cancelButton;
    DynamicComboBoxModel connectionsModel;
    DefaultAutoCompletePopupProvider autoCompletePopup;

    public CreateViewPanel(DatabaseConnection dc, ActionContainer dialog) {
        connection = dc;
        parent = dialog;
        init();
    }

    public CreateViewPanel(DatabaseConnection dc) {
        this(dc, null);
    }

    void init() {
        sqlTextView = new SQLTextPane();
        sqlTextView.addFocusListener(this);
        this.autoCompletePopup = new DefaultAutoCompletePopupProvider(connection, sqlTextView);
        sqlTextScroll = new JScrollPane(sqlTextView);
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");

        String sql = "create view new_view ( _fields_ )\n" +
                "as\n" +
                "select _fields_ from _table_name_\n" +
                "where _conditions_";
        sqlTextView.setText(sql);

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (parent != null)
                    parent.finished();
                else GUIUtilities.closeSelectedCentralPane();
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ExecuteQueryDialog eqd = new ExecuteQueryDialog(TITLE, sqlTextView.getText(), connection, true);
                eqd.display();
                if (eqd.getCommit()) {
                    if (parent != null)
                        parent.finished();
                    else GUIUtilities.closeSelectedCentralPane();
                }
            }
        });

        //create connectionsCombo
        Vector<DatabaseConnection> connections = ConnectionManager.getActiveConnections();
        connectionsModel = new DynamicComboBoxModel(connections);
        connectionsCombo = WidgetFactory.createComboBox(connectionsModel);
        connectionsCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
                autoCompletePopup.connectionChanged(connection);
            }
        });
        if (connection != null) {
            connectionsCombo.setSelectedItem(connection);
        } else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();

        //create location elements
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(connectionsCombo)
                .addComponent(sqlTextScroll, GroupLayout.PREFERRED_SIZE, 400, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10)
                        .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGap(10)
                .addComponent(connectionsCombo)
                .addGap(10)
                .addComponent(sqlTextScroll, 0, 200, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(cancelButton)
                        .addComponent(okButton)
                )
        );


        Action autoCompletePopupAction = autoCompletePopup.getPopupAction();

        sqlTextView.getActionMap().put(AUTO_COMPLETE_POPUP_ACTION_KEY, autoCompletePopupAction);
        sqlTextView.getInputMap().put((KeyStroke)
                        autoCompletePopupAction.getValue(Action.ACCELERATOR_KEY),
                AUTO_COMPLETE_POPUP_ACTION_KEY);
    }

    @Override
    public void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getSource() != sqlTextView)
            GUIUtils.requestFocusInWindow(sqlTextView);
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {

    }
}
