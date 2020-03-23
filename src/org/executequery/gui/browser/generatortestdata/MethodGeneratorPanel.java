package org.executequery.gui.browser.generatortestdata;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.browser.generatortestdata.methodspanels.*;
import org.executequery.log.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MethodGeneratorPanel extends JPanel implements ActionListener {

    DatabaseColumn column;
    AbstractMethodPanel methodPanel;
    private ButtonGroup buttonGroup;
    private JRadioButton randomButton;
    private JRadioButton getFromOtherTableButton;
    private JRadioButton getFromListButton;
    private JRadioButton autoincrementButton;
    private JPanel bottomPanel;
    private DefaultStatementExecutor executor;

    public MethodGeneratorPanel(DatabaseColumn column, DefaultStatementExecutor executor) {
        this.column = column;
        this.executor = executor;
        init();
    }

    private void init() {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        randomButton = new JRadioButton("Random");
        randomButton.addActionListener(this);
        getFromOtherTableButton = new JRadioButton("get from other table");
        getFromOtherTableButton.addActionListener(this);
        getFromListButton = new JRadioButton("get from list");
        getFromListButton.addActionListener(this);
        autoincrementButton = new JRadioButton("Autoincrement");
        autoincrementButton.addActionListener(this);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(randomButton);
        buttonGroup.add(getFromOtherTableButton);
        buttonGroup.add(getFromListButton);
        buttonGroup.add(autoincrementButton);

        setLayout(new GridBagLayout());

        add(randomButton, new GridBagConstraints(0, 0, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(getFromOtherTableButton, new GridBagConstraints(0, 1, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(getFromListButton, new GridBagConstraints(0, 2, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(autoincrementButton, new GridBagConstraints(0, 3, 1, 1, 1, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
        add(bottomPanel, new GridBagConstraints(0, 4, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        bottomPanel.removeAll();
        if (randomButton.isSelected()) {
            methodPanel = new RandomMethodPanel(column);
            bottomPanel.add(methodPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        }
        if (getFromOtherTableButton.isSelected()) {
            methodPanel = new GetFromOtherTablePanel(column, executor);
            bottomPanel.add(methodPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        }

        if (getFromListButton.isSelected()) {
            methodPanel = new GetFromListPanel(column);
            bottomPanel.add(methodPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        }

        if (autoincrementButton.isSelected()) {
            methodPanel = new AutoincrementPanel(column);
            bottomPanel.add(methodPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        }
        updateUI();
    }

    public void setFirst() {
        try {
            methodPanel.setFirst(true);
        } catch (NullPointerException e) {
            Log.debug("methodPanel not exist");
        }
    }

    public Object getTestDataObject() {
        return methodPanel.getTestDataObject();
    }
}
