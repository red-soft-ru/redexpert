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
    public final static int COUNT_METHODS = 4;
    private JPanel bottomPanel;
    private DefaultStatementExecutor executor;
    public final static int RANDOM = 0;
    public final static int FROM_TABLE = 1;
    public final static int FROM_LIST = 2;
    public final static int AUTOINCREMENT = 3;
    AbstractMethodPanel[] methodPanels;
    private JRadioButton[] radioButtons;

    public MethodGeneratorPanel(DatabaseColumn column, DefaultStatementExecutor executor) {
        this.column = column;
        this.executor = executor;
        init();
    }

    private void init() {
        methodPanels = new AbstractMethodPanel[COUNT_METHODS];
        radioButtons = new JRadioButton[COUNT_METHODS];
        radioButtons[RANDOM] = new JRadioButton(bundledString("Random"));
        radioButtons[FROM_TABLE] = new JRadioButton(bundledString("getFromOtherTable"));
        radioButtons[FROM_LIST] = new JRadioButton(bundledString("getFromList"));
        radioButtons[AUTOINCREMENT] = new JRadioButton(bundledString("Autoincrement"));
        buttonGroup = new ButtonGroup();
        for (int i = 0; i < methodPanels.length; i++) {
            methodPanels[i] = null;
            radioButtons[i].addActionListener(this);
            buttonGroup.add(radioButtons[i]);
        }
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());

        setLayout(new GridBagLayout());
        GridBagHelper gbh = new GridBagHelper();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        gbh.setDefaults(gbc);
        gbh.setXY(0, -1);
        for (int i = 0; i < radioButtons.length; i++) {
            if (i != AUTOINCREMENT || (!column.getFormattedDataType().contains("CHAR") && !column.getFormattedDataType().contains("BLOB") && !column.getFormattedDataType().contains("BOOLEAN")))
                add(radioButtons[i], gbh.defaults().nextRowFirstCol().spanX().get());
        }
        add(bottomPanel, gbh.defaults().fillBoth().nextRowFirstCol().spanX().spanY().get());
        radioButtons[RANDOM].setSelected(true);
        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        refresh();
    }

    private void refresh() {
        bottomPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        for (int i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].isSelected()) {
                if (methodPanels[i] != null)
                    methodPanel = methodPanels[i];
                else {
                    switch (i) {
                        case RANDOM:
                            methodPanel = new RandomMethodPanel(column);
                            break;
                        case FROM_TABLE:
                            methodPanel = new GetFromOtherTablePanel(column, executor);
                            break;
                        case FROM_LIST:
                            methodPanel = new GetFromListPanel(column);
                            break;
                        case AUTOINCREMENT:
                            methodPanel = new AutoincrementPanel(column);
                            break;
                    }
                    methodPanels[i] = methodPanel;
                }
                break;
            }
        }
        bottomPanel.add(methodPanel, gbc);
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
