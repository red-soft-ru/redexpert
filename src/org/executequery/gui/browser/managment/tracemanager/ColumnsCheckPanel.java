package org.executequery.gui.browser.managment.tracemanager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ColumnsCheckPanel extends JPanel {

    private Map<String, JCheckBox> checkBoxMap;
    private int x = 6;

    public ColumnsCheckPanel() {
        setLayout(new GridBagLayout());
        checkBoxMap = new HashMap<>();
        int k = 0;
        for (int i = 1; k < LogConstants.COLUMNS.length; i++)
            for (int g = 0; g < x && k < LogConstants.COLUMNS.length; g++, k++) {
                JCheckBox checkBox = new JCheckBox(LogConstants.COLUMNS[k]);
                checkBox.setSelected(true);
                GridBagConstraints gbc = new GridBagConstraints(
                        g, i, 1, 1,
                        1, 1, GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                        0, 0);
                add(checkBox, gbc);
                checkBoxMap.put(LogConstants.COLUMNS[k], checkBox);
            }
        JButton buttonSelectAll = new JButton("Select All");
        buttonSelectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox checkBox : checkBoxMap.values()) {
                    checkBox.setSelected(true);
                }
            }
        });
        JButton buttonDeselectAll = new JButton("Deselect All");
        buttonDeselectAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JCheckBox checkBox : checkBoxMap.values()) {
                    checkBox.setSelected(false);
                }
            }
        });
        GridBagConstraints gbc = new GridBagConstraints(
                0, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                0, 0);
        add(buttonSelectAll, gbc);
        gbc = new GridBagConstraints(
                1, 0, 1, 1,
                1, 1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(1, 1, 1, 1),
                0, 0);
        add(buttonDeselectAll, gbc);
    }

    public Map<String, JCheckBox> getCheckBoxMap() {
        return checkBoxMap;
    }

    public void setCheckBoxMap(Map<String, JCheckBox> checkBoxMap) {
        this.checkBoxMap = checkBoxMap;
    }
}
