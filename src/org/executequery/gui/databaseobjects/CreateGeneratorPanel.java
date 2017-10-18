package org.executequery.gui.databaseobjects;

import org.executequery.GUIUtilities;
import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.datasource.ConnectionManager;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.ExecuteQueryDialog;
import org.executequery.gui.ExecuteQueryFrame;
import org.executequery.gui.WidgetFactory;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.DynamicComboBoxModel;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

public class CreateGeneratorPanel extends JPanel{

    private ActionContainer parent;

    JTextField nameText;

    NumberTextField valueText;

    NumberTextField startValueText;

    NumberTextField incrementText;

    JTextField description;

    JButton ok;

    JButton cancel;

    DatabaseConnection connection;

    private JComboBox connectionsCombo;

    private DynamicComboBoxModel connectionsModel;

    public static final String TITLE="Create Sequence";

    public CreateGeneratorPanel(DatabaseConnection dc,ActionContainer dialog)
    {
        this(dc,dialog,0,1);
    }

    public CreateGeneratorPanel(DatabaseConnection dc,ActionContainer dialog,int initial_value,int increment)
    {
        this(dc,dialog,"NEW_SEQUENCE",initial_value,increment);
    }

    public CreateGeneratorPanel(DatabaseConnection dc,ActionContainer dialog,String name,int initial_value,int increment)
    {
        this(dc,dialog,name,initial_value,increment,null);
    }

    public CreateGeneratorPanel(DatabaseConnection dc,ActionContainer dialog,String name,int initial_value,int increment,String description)
    {
        parent=dialog;
        connection=dc;
        init(name,initial_value,increment,description);

    }
    void init(String name,int initial_value,int increment,String description)
    {
        nameText = new JTextField();
        if(name!=null)
        {
            nameText.setText(name);
        }
        //valueText = new NumberTextField();
        //valueText.setValue(value);
        startValueText=new NumberTextField();
        startValueText.setValue(initial_value);
        incrementText=new NumberTextField();
        incrementText.setValue(increment);
        this.description = new JTextField(15);
        if(description!=null)
            this.description.setText(description);
        ok=new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                createGenerator();
            }
        });
        cancel=new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });
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
            }
        });
        if(connection!=null)
        {
            connectionsCombo.setSelectedItem(connection);
        }
        else connection = (DatabaseConnection) connectionsCombo.getSelectedItem();
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbc.gridx = 1;
        gbcLabel.gridheight = 1;
        gbc.gridheight = 1;
        gbcLabel.gridwidth = 1;
        gbc.gridwidth = 1;
        gbcLabel.weightx = 0;
        gbc.weightx = 1.0;
        gbcLabel.weighty = 0;
        gbc.weighty = 0;
        gbcLabel.gridy = 0;
        gbc.gridy = 0;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbc.anchor = GridBagConstraints.WEST;
        gbcLabel.ipadx = 0;
        gbc.ipadx = 0;
        gbcLabel.ipady = 0;
        gbc.ipady = 0;
        gbcLabel.insets = new Insets(5, 5, 5, 5);
        gbc.insets = new Insets(5, 5, 5, 5);
        this.setLayout(new GridBagLayout());
        JLabel label=new JLabel("Connections");
        add(label,gbcLabel);
        add(connectionsCombo,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label=new JLabel("Name");
        add(label,gbcLabel);
        add(nameText,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Start Value");
        add(label,gbcLabel);
        add(startValueText,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Increment");
        add(label,gbcLabel);
        add(incrementText,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        label = new JLabel("Description");
        add(label,gbcLabel);
        add(this.description,gbc);
        gbc.gridy++;
        gbcLabel.gridy++;
        add(cancel,gbcLabel);
        add(ok,gbc);

    }

    void createGenerator()
    {
        if(!MiscUtils.isNull(nameText.getText().trim())) {
            String query = "CREATE SEQUENCE " + nameText.getText() + " START WITH "+startValueText.getStringValue()
                    +" INCREMENT BY "+incrementText.getStringValue();
            ExecuteQueryDialog eqd=new ExecuteQueryDialog(TITLE,query,connection,true);
            eqd.display();
            if(eqd.getCommit())
            {
                if(!MiscUtils.isNull(description.getText().trim()))
                {
                    String query2 = "COMMENT ON SEQUENCE " + nameText.getText() + " IS '"+description.getText()+"'";
                    ExecuteQueryDialog eqd2=new ExecuteQueryDialog(TITLE,query2,connection,true);
                    eqd2.display();
                    if (eqd2.getCommit())
                        parent.finished();
                }
                else parent.finished();
            }
        }
        else GUIUtilities.displayErrorMessage("Name can not be empty");
    }
}
