package org.executequery.gui.browser.generatortestdata;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.gui.browser.generatortestdata.methodspanels.*;
import org.executequery.log.Log;
import org.underworldlabs.swing.layouts.GridBagHelper;

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
        randomButton = new JRadioButton(bundledString("Random"));
        randomButton.addActionListener(this);
        getFromOtherTableButton = new JRadioButton(bundledString("getFromOtherTable"));
        getFromOtherTableButton.addActionListener(this);
        getFromListButton = new JRadioButton(bundledString("getFromList"));
        getFromListButton.addActionListener(this);
        autoincrementButton = new JRadioButton(bundledString("Autoincrement"));
        autoincrementButton.addActionListener(this);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(randomButton);
        buttonGroup.add(getFromOtherTableButton);
        buttonGroup.add(getFromListButton);
        buttonGroup.add(autoincrementButton);

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);

        add(randomButton, gbh.defaults().spanX().get());
        add(getFromOtherTableButton, gbh.defaults().nextRowFirstCol().spanX().get());
        add(getFromListButton, gbh.defaults().nextRowFirstCol().spanX().get());
        if (!column.getFormattedDataType().contains("CHAR") && !column.getFormattedDataType().contains("BLOB"))
            add(autoincrementButton, gbh.defaults().nextRowFirstCol().spanX().get());
        add(bottomPanel, gbh.defaults().fillBoth().nextRowFirstCol().spanX().spanY().get());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        bottomPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        if (randomButton.isSelected()) {
            methodPanel = new RandomMethodPanel(column);
            bottomPanel.add(methodPanel, gbc);

        }
        if (getFromOtherTableButton.isSelected()) {
            methodPanel = new GetFromOtherTablePanel(column, executor);
            bottomPanel.add(methodPanel, gbc);

        }

        if (getFromListButton.isSelected()) {
            methodPanel = new GetFromListPanel(column);
            bottomPanel.add(methodPanel, gbc);

        }

        if (autoincrementButton.isSelected()) {
            methodPanel = new AutoincrementPanel(column);
            bottomPanel.add(methodPanel, gbc);

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

    private String bundledString(String key) {
        return GeneratorTestDataPanel.bundles(key);
    }
}
