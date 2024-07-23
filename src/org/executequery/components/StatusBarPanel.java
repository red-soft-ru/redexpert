/*
 * StatusBarPanel.java
 *
 * Copyright (C) 2002-2017 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.components;

import org.underworldlabs.swing.AbstractStatusBarPanel;

import javax.swing.*;

/**
 * @author Takis Diakoumis
 */
public class StatusBarPanel extends AbstractStatusBarPanel {

    /**
     * the status bar panel fixed height
     */
    private static final int HEIGHT = 26;

    /**
     * <p>Creates a new instance with the specified values
     * within respective values.
     */
    public StatusBarPanel(String text1, String text2) {
        super(HEIGHT);

        init();
        setFirstLabelText(text1);
        setThirdLabelText(text2);
    }

    private void init() {
        setBorder(BorderFactory.createEmptyBorder(2, 3, 3, 4));

        addLabel(0, 70, true);
        addLabel(1, 150, true);
        addLabel(2, 50, true);
        addLabel(3, 75, true);
        addLabel(4, 150, false);
    }

    public void reset() {
        String[] labels = new String[]{
                getLabel(0).getText(),
                getLabel(1).getText(),
                null,
                getLabel(3).getText(),
                getLabel(4).getText()
        };

        Integer[] alignments = new Integer[]{
                getLabel(0).getHorizontalAlignment(),
                getLabel(1).getHorizontalAlignment(),
                SwingConstants.CENTER,
                getLabel(3).getHorizontalAlignment(),
                getLabel(4).getHorizontalAlignment()
        };

        removeAll();
        init();

        for (int i = 0; i < labels.length; i++) {
            getLabel(i).setText(labels[i]);
            getLabel(i).setHorizontalAlignment(alignments[i]);
        }
    }

    public void addComponent(JComponent component, int index) {
        removeComponent(index);
        addComponent(component, index, 150, false);
    }

    public void setFirstLabelText(final String text) {
        setLabelText(0, text);
    }

    public void setSecondLabelText(String text) {
        setLabelText(1, text);
    }

    public void setThirdLabelText(String text) {
        setLabelText(2, text);
        getLabel(2).setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setFourthLabelText(String text, int alignment) {
        JLabel label = getLabel(3);
        if (label != null) {
            label.setHorizontalAlignment(alignment);
            setLabelText(3, text);
        }
    }

    @Override
    public JLabel getLabel(int index) {
        return super.getLabel(index);
    }

}
