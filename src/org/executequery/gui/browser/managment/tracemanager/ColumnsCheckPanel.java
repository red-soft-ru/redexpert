package org.executequery.gui.browser.managment.tracemanager;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColumnsCheckPanel extends JPanel {

    private Map<String, JCheckBox> checkBoxMap;
    private int x = 5;

    public ColumnsCheckPanel() {
        setLayout(new GridBagLayout());
        checkBoxMap = new HashMap<>();
        int k = 0;
        for (int i = 0; k < LogConstants.COLUMNS.length; i++)
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
    }

    public Map<String, JCheckBox> getCheckBoxMap() {
        return checkBoxMap;
    }

    public void setCheckBoxMap(Map<String, JCheckBox> checkBoxMap) {
        this.checkBoxMap = checkBoxMap;
    }
}
