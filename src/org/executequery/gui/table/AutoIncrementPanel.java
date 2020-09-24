package org.executequery.gui.table;

import org.executequery.databasemediators.DatabaseConnection;
import org.executequery.databaseobjects.DatabaseHost;
import org.executequery.databaseobjects.impl.DefaultDatabaseHost;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.text.SQLTextPane;
import org.underworldlabs.swing.NumberTextField;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.SQLException;

public class AutoIncrementPanel extends JPanel {

    JTabbedPane tabPanel;
    JPanel generatorPanel;
    JPanel notSystemPanel;
    JPanel systemGeneratorPanel;
    JPanel createGeneratorPanel;
    JPanel useGeneratorPanel;
    JPanel useIdentityPanel;
    JPanel triggerPanel;
    JPanel procedurePanel;
    JCheckBox systemGeneratorBox;
    JCheckBox createGeneratorBox;
    JCheckBox useGeneratorBox;
    JCheckBox useIdentityBox;
    JCheckBox createTriggerBox;
    JCheckBox createProcedureBox;
    JButton okButton;
    JButton cancelButton;
    NumberTextField systemStartValue;
    NumberTextField createStartValue;
    NumberTextField identityStartValue;
    JTextField createGeneratorName;
    JComboBox comboGenerators;
    JScrollPane triggerScroll;
    SQLTextPane triggerSQLPane;
    Autoincrement ai;
    String tableName;
    ActionContainer parent;
    DatabaseConnection connection;
    String[] generators;

    public AutoIncrementPanel(DatabaseConnection dc, ActionContainer parent, Autoincrement inc, String table_name, String[] generators) {
        this.parent = parent;
        ai = inc;
        tableName = table_name;
        connection = dc;
        this.generators = generators;
        init();
        systemGeneratorBox.setVisible(false);
        createGeneratorPanel.setVisible(false);
        systemGeneratorPanel.setVisible(false);
        useGeneratorPanel.setVisible(false);
        if (getDatabaseVersion() < 3)
            useIdentityBox.setVisible(false);
        useIdentityPanel.setVisible(false);
        if (parent == null) {
            okButton.setVisible(false);
            cancelButton.setVisible(false);
        }

    }

    protected int getDatabaseVersion() {
        DatabaseHost host = new DefaultDatabaseHost(connection);
        try {
            return host.getDatabaseMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    void init() {
        tabPanel = new JTabbedPane();
        generatorPanel = new JPanel();
        notSystemPanel = new JPanel();
        triggerPanel = new JPanel();
        procedurePanel = new JPanel();
        systemGeneratorPanel = new JPanel();
        createGeneratorPanel = new JPanel();
        useIdentityPanel = new JPanel();
        useGeneratorPanel = new JPanel();
        triggerPanel = new JPanel();
        systemGeneratorBox = new JCheckBox("System Generator");
        createGeneratorBox = new JCheckBox("Create Sequence");
        useIdentityBox = new JCheckBox("Use Identity");
        useGeneratorBox = new JCheckBox("Use Existed Sequence");
        createTriggerBox = new JCheckBox("Create Trigger");
        createProcedureBox = new JCheckBox("Create Procedure");
        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        systemStartValue = new NumberTextField(0);
        createStartValue = new NumberTextField(0);
        identityStartValue = new NumberTextField(0);
        createGeneratorName = new JTextField();
        comboGenerators = new JComboBox(generators);
        triggerSQLPane = new SQLTextPane();
        triggerScroll = new JScrollPane(triggerSQLPane);

        createGeneratorName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {

            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getSource() == createGeneratorName)
                    ai.setGeneratorName(createGeneratorName.getText());
            }
        });

        comboGenerators.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setGeneratorName((String) comboGenerators.getSelectedItem());
            }
        });

        systemGeneratorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setSystemGenerator(systemGeneratorBox.isSelected());
                if (ai.isSystemGenerator()) {
                    systemGeneratorPanel.setVisible(true);
                    createGeneratorPanel.setVisible(false);
                    useGeneratorPanel.setVisible(false);
                    createGeneratorBox.setSelected(false);
                    useIdentityBox.setSelected(false);
                    useGeneratorBox.setSelected(false);
                } else {
                    systemGeneratorPanel.setVisible(false);
                }
            }
        });

        createGeneratorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setCreateGenerator(createGeneratorBox.isSelected());
                if (ai.isCreateGenerator()) {
                    systemGeneratorPanel.setVisible(false);
                    createGeneratorPanel.setVisible(true);
                    useGeneratorPanel.setVisible(false);
                    useIdentityPanel.setVisible(false);
                    systemGeneratorBox.setSelected(false);
                    useGeneratorBox.setSelected(false);
                    useIdentityBox.setSelected(false);
                    createGeneratorName.setText("SEQ_" + tableName + "_" + ai.getFieldName());
                    ai.setGeneratorName(createGeneratorName.getText());
                } else {
                    createGeneratorPanel.setVisible(false);
                }
            }
        });

        useIdentityBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setIdentity(useIdentityBox.isSelected());
                if (ai.isIdentity()) {
                    systemGeneratorPanel.setVisible(false);
                    createGeneratorPanel.setVisible(false);
                    useGeneratorPanel.setVisible(false);
                    useIdentityPanel.setVisible(true);
                    systemGeneratorBox.setSelected(false);
                    useGeneratorBox.setSelected(false);
                    createGeneratorBox.setSelected(false);
                } else {
                    useIdentityPanel.setVisible(false);
                }
            }
        });

        useGeneratorBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setUseGenerator(useGeneratorBox.isSelected());
                if (ai.isUseGenerator()) {
                    systemGeneratorPanel.setVisible(false);
                    createGeneratorPanel.setVisible(false);
                    useGeneratorPanel.setVisible(true);
                    useIdentityPanel.setVisible(false);
                    createGeneratorBox.setSelected(false);
                    systemGeneratorBox.setSelected(false);
                    useIdentityBox.setSelected(false);
                    ai.setCreateGenerator(false);
                    ai.setGeneratorName((String) comboGenerators.getSelectedItem());
                } else {
                    useGeneratorPanel.setVisible(false);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                parent.finished();
            }
        });

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                generateAI();
            }
        });

        createTriggerBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ai.setCreateTrigger(createTriggerBox.isSelected());
                triggerScroll.setVisible(ai.isCreateTrigger());
                if (ai.isCreateTrigger()) {
                    String sql = "create trigger " + MiscUtils.getFormattedObject(tableName + "_bi") + " for " + MiscUtils.getFormattedObject(tableName) + "\n" +
                            "active before insert position 0\n" +
                            "as\n" +
                            "begin\n" +
                            "if (new." + MiscUtils.getFormattedObject(ai.getFieldName()) + " is null) then\n" +
                            "new." + MiscUtils.getFormattedObject(ai.getFieldName()) + " = gen_id(" + MiscUtils.getFormattedObject(ai.getGeneratorName()) + ",1);\n" +
                            "end";
                    triggerSQLPane.setText(sql);
                }
            }
        });

        GroupLayout systemGeneratorPanelLayout = new GroupLayout(systemGeneratorPanel);
        systemGeneratorPanel.setLayout(systemGeneratorPanelLayout);
        JLabel label = new JLabel("Start Value");
        systemGeneratorPanelLayout.setHorizontalGroup(systemGeneratorPanelLayout.createSequentialGroup()
                .addGap(10)
                .addComponent(label)
                .addGap(10)
                .addComponent(systemStartValue)
        );

        systemGeneratorPanelLayout.setVerticalGroup(
                systemGeneratorPanelLayout.createSequentialGroup()
                        .addGroup(
                                systemGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(systemStartValue, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(label)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        );

        label = new JLabel("Start Value");
        JLabel label1 = new JLabel("Name");
        GroupLayout createGeneratorPanelLayout = new GroupLayout(createGeneratorPanel);
        createGeneratorPanel.setLayout(createGeneratorPanelLayout);
        createGeneratorPanelLayout.setHorizontalGroup(createGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(createGeneratorPanelLayout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(createGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(label1)
                                .addComponent(label))
                        .addGap(10)
                        .addGroup(createGeneratorPanelLayout.createParallelGroup()
                                .addComponent(createGeneratorName)
                                .addComponent(createStartValue))
                )
        );


        createGeneratorPanelLayout.setVerticalGroup(createGeneratorPanelLayout.createSequentialGroup()
                .addGap(10)
                .addGroup(createGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(createGeneratorName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label1)
                )
                .addGap(10)
                .addGroup(createGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(createStartValue, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label)
                )
        );

        GridBagLayout useIdentityPanelLayout = new GridBagLayout();
        useIdentityPanel.setLayout(useIdentityPanelLayout);
        label = new JLabel("Start Value");
        useIdentityPanel.add(label, new GridBagConstraints(0, 0,
                1, 1, 0, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5),
                0, 0));
        useIdentityPanel.add(identityStartValue, new GridBagConstraints(1, 0,
                1, 1, 1, 0,
                GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
                0, 0));


        label = new JLabel("Generators:");
        GroupLayout useGeneratorPanelLayout = new GroupLayout(useGeneratorPanel);
        useGeneratorPanel.setLayout(useGeneratorPanelLayout);
        useGeneratorPanelLayout.setHorizontalGroup(useGeneratorPanelLayout.createSequentialGroup()
                .addGap(10)
                .addComponent(label)
                .addGap(10)
                .addComponent(comboGenerators)
        );
        useGeneratorPanelLayout.setVerticalGroup(useGeneratorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(comboGenerators, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(label)
        );

        GroupLayout generatorLayout = new GroupLayout(generatorPanel);
        generatorPanel.setLayout(generatorLayout);
        generatorLayout.setHorizontalGroup(generatorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(systemGeneratorBox)
                .addComponent(createGeneratorBox)
                .addComponent(useGeneratorBox)
                .addComponent(useIdentityBox)
                .addComponent(systemGeneratorPanel)
                .addComponent(createGeneratorPanel)
                .addComponent(useGeneratorPanel)
                .addComponent(useIdentityPanel)
        );

        generatorLayout.setVerticalGroup(generatorLayout.createSequentialGroup()
                .addGap(10)
                .addComponent(systemGeneratorBox)
                .addGap(10)
                .addComponent(createGeneratorBox)
                .addGap(10)
                .addComponent(useGeneratorBox)
                .addGap(10)
                .addComponent(useIdentityBox)
                .addGap(10)
                .addComponent(systemGeneratorPanel)
                .addComponent(createGeneratorPanel)
                .addComponent(useGeneratorPanel)
                .addComponent(useIdentityPanel)
        );

        tabPanel.add("Generator", generatorPanel);

        GroupLayout triggerLayout = new GroupLayout(triggerPanel);
        triggerPanel.setLayout(triggerLayout);
        triggerLayout.setHorizontalGroup(triggerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(createTriggerBox)
                .addComponent(triggerScroll)
        );

        triggerLayout.setVerticalGroup(triggerLayout.createSequentialGroup()
                .addGap(10)
                .addComponent(createTriggerBox)
                .addGap(10)
                .addComponent(triggerScroll)
        );
        tabPanel.add("Trigger", triggerPanel);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(tabPanel, GroupLayout.PREFERRED_SIZE, 300, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(10)
                        .addComponent(cancelButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(tabPanel, GroupLayout.PREFERRED_SIZE, 300, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(cancelButton)
                        .addComponent(okButton)
                )
        );
    }

    public void generateAI() {
        String sql = "";
        if (ai.isIdentity())
            ai.setStartValue(identityStartValue.getValue());
        if (ai.isCreateGenerator()) {
            sql += "\nCREATE SEQUENCE " + MiscUtils.getFormattedObject(ai.getGeneratorName()) + "^";
            ai.setStartValue(createStartValue.getValue());
            if (ai.getStartValue() != 0) {
                sql += "\nALTER SEQUENCE " + MiscUtils.getFormattedObject(ai.getGeneratorName()) + " RESTART WITH " + ai.getStartValue() + "^";
            }
        }
        if (ai.isCreateTrigger()) {
            sql += "\n" + triggerSQLPane.getText() + ";";
        }
        ai.setSqlAutoincrement(sql);
        if (parent != null)
            parent.finished();
    }
}
