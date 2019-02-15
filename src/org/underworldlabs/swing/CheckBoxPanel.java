package org.underworldlabs.swing;

import org.executequery.gui.browser.TraceManagerPanel;
import org.underworldlabs.jdbc.DataSourceException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckBoxPanel extends JScrollPane {

    private JPanel mainPanel;
    private Map<String, JCheckBox> checkBoxMap;
    private int x;
    private String[] namesBox;
    private boolean selected;

    public CheckBoxPanel(List<Object> objects, int countBoxesInRow, boolean selected) {
        namesBox = new String[objects.size()];
        for (int i = 0; i < objects.size(); i++)
            if (objects.get(i) instanceof Named)
                namesBox[i] = ((Named) objects.get(i)).getName();
            else throw new DataSourceException();
        x = countBoxesInRow;
        this.selected = selected;
        init();
    }

    public CheckBoxPanel(Object[] objects, int countBoxesInRow, boolean selected) {
        namesBox = new String[objects.length];
        for (int i = 0; i < objects.length; i++)
            if (objects[i] instanceof Named)
                namesBox[i] = ((Named) objects[i]).getName();
            else throw new DataSourceException();
        x = countBoxesInRow;
        this.selected = selected;
        init();
    }

    public CheckBoxPanel(String[] namesBox, int countBoxesInRow, boolean selected) {
        this.namesBox = namesBox;
        x = countBoxesInRow;
        this.selected = selected;
        init();
    }

    private void init() {
        mainPanel = new JPanel();
        setViewportView(mainPanel);
        mainPanel.setLayout(new GridBagLayout());
        checkBoxMap = new HashMap<>();
        int k = 0;
        for (int i = 1; k < namesBox.length; i++)
            for (int g = 0; g < x && k < namesBox.length; g++, k++) {
                JCheckBox checkBox = new JCheckBox(namesBox[k]);
                checkBox.setSelected(selected);
                GridBagConstraints gbc = new GridBagConstraints(
                        g, i, 1, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                mainPanel.add(checkBox, gbc);
                checkBoxMap.put(namesBox[k], checkBox);
            }
        JCheckBox allCheckBox = new JCheckBox(TraceManagerPanel.bundleString("SelectAll"));
        allCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                for (JCheckBox checkBox : checkBoxMap.values()) {
                    checkBox.setSelected(allCheckBox.isSelected());
                }
            }
        });
        GridBagConstraints gbc = new GridBagConstraints(
                0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                0, 0);
        mainPanel.add(allCheckBox, gbc);
    }

    public void setNamesBox(String[] namesBox) {
        this.namesBox = namesBox;
        init();
    }


    public Map<String, JCheckBox> getCheckBoxMap() {
        return checkBoxMap;
    }

    public void setCheckBoxMap(Map<String, JCheckBox> checkBoxMap) {
        this.checkBoxMap = checkBoxMap;
    }
}

