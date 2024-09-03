package org.executequery.gui.browser.generatortestdata;

import org.executequery.databasemediators.spi.DefaultStatementExecutor;
import org.executequery.databaseobjects.DatabaseColumn;
import org.executequery.gui.browser.GeneratorTestDataPanel;
import org.executequery.gui.browser.generatortestdata.methodspanels.*;
import org.executequery.localization.Bundles;
import org.underworldlabs.swing.layouts.GridBagHelper;

import javax.swing.*;
import java.awt.*;

public class MethodGeneratorPanel extends JPanel {
    public final static int RANDOM = 0;
    public final static int FROM_TABLE = 1;
    public final static int FROM_LIST = 2;
    public final static int AUTOINCREMENT = 3;
    public final static int COUNT_METHODS = 4;

    private final DatabaseColumn column;
    private final DefaultStatementExecutor executor;

    private JPanel mainPanel;
    private JRadioButton[] radioButtons;
    private AbstractMethodPanel methodPanel;
    private AbstractMethodPanel[] methodPanels;

    public MethodGeneratorPanel(DatabaseColumn column, DefaultStatementExecutor executor) {
        this.column = column;
        this.executor = executor;
        init();
    }

    private void init() {

        radioButtons = new JRadioButton[COUNT_METHODS];
        radioButtons[RANDOM] = new JRadioButton(bundledString("Random"));
        radioButtons[FROM_TABLE] = new JRadioButton(bundledString("getFromOtherTable"));
        radioButtons[FROM_LIST] = new JRadioButton(bundledString("getFromList"));
        radioButtons[AUTOINCREMENT] = new JRadioButton(bundledString("Autoincrement"));

        ButtonGroup buttonGroup = new ButtonGroup();
        methodPanels = new AbstractMethodPanel[COUNT_METHODS];
        for (int i = 0; i < methodPanels.length; i++) {
            methodPanels[i] = null;
            radioButtons[i].addActionListener(e -> refresh());
            buttonGroup.add(radioButtons[i]);
        }

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        setLayout(new GridBagLayout());

        GridBagHelper gbh = new GridBagHelper().anchorNorthWest().setInsets(5, 5, 5, 5).fillHorizontally().setXY(0, -1);
        for (int i = 0; i < radioButtons.length; i++) {
            if (i != AUTOINCREMENT
                    || (!column.getFormattedDataType().contains("CHAR")
                    && !column.getFormattedDataType().contains("BLOB")
                    && !column.getFormattedDataType().contains("BOOLEAN"))
            ) {
                add(radioButtons[i], gbh.nextRowFirstCol().spanX().get());
            }
        }

        add(mainPanel, gbh.nextRowFirstCol().setWidth(1).fillBoth().spanX().spanY().get());
        radioButtons[RANDOM].setSelected(true);
        refresh();
    }

    private void refresh() {

        for (int i = 0; i < radioButtons.length; i++) {

            if (!radioButtons[i].isSelected())
                continue;

            if (methodPanels[i] != null) {
                methodPanel = methodPanels[i];
                break;
            }

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
            break;
        }

        mainPanel.removeAll();
        mainPanel.add(methodPanel, new GridBagHelper().fillBoth().spanX().spanY().get());
        updateUI();
    }

    public void setFirst() {
        if (methodPanel != null)
            methodPanel.setFirst(true);
    }

    public Object getTestDataObject() {
        return methodPanel.getTestDataObject();
    }

    private String bundledString(String key) {
        return Bundles.get(GeneratorTestDataPanel.class, key);
    }

}
