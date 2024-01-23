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

import org.executequery.Constants;
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
     *
     * @param the value displayed in the left-most label
     * @param the value displayed in the second label from the left
     * @param the value displayed in the right-most label
     */
    public StatusBarPanel(String text1, String text2) {

        super(HEIGHT);

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        setFirstLabelText(text1);
        setThirdLabelText(text2);
    }

    private void init() throws Exception {

        setBorder(BorderFactory.createEmptyBorder(2, 3, 3, 4));

        addLabel(0, 70, true);
        addLabel(1, 150, true);
        addLabel(2, 50, true);
        addLabel(3, 75, true);
        addLabel(4, 150, false);
        //addComponent(new HeapMemoryStatusSnippet(), 5, 150, false);
    }

    public void setThirdLabelText(String text) {

        setLabelText(2, text);
    }

    public void addComponent(JComponent component, int index) {
        removeComponent(index);
        addComponent(component, index, 150, false);
    }

    public void setSecondLabelText(String text) {

        setLabelText(1, text);
    }

    public void setFirstLabelText(final String text) {

        setLabelText(0, text);
    }

    public void setFifthLabelText(final String text) {

        setLabelText(4, text);
    }

    public void resetStatusBar() {

        setLabelText(0, Constants.EMPTY);
        setLabelText(1, Constants.EMPTY);
        setLabelText(2, Constants.EMPTY);
    }

    public void setFourthLabelText(String text, int alignment) {

        JLabel label = getLabel(3);

        if (label != null) {

            label.setHorizontalAlignment(alignment);
            setLabelText(3, text);
        }
    }

    public JLabel getLabel(int index) {

        return super.getLabel(index);
    }

    public void setFourthLabelText(String text) {

        setLabelText(3, text);
    }

}











