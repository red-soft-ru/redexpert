/*
 * ToolBarWrapper.java
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

package org.underworldlabs.swing.toolbar;

import java.util.Vector;

/**
 * <p>Wrapper class defining a tool bar, its buttons
 * and its row and position within the tool bar area.
 *
 * @author Takis Diakoumis
 */
public class ToolBarWrapper implements Cloneable {

    private String name;
    private boolean visible;
    private Vector<ToolBarButton> buttons;
    private ToolBarConstraints constraints;

    public ToolBarWrapper(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public ToolBarConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(ToolBarConstraints constraints) {
        this.constraints = constraints;
    }

    public boolean isVisible() {
        return visible;
    }

    public void addButton(ToolBarButton button) {
        if (buttons == null)
            buttons = new Vector<>();
        buttons.add(button);
    }

    public void resetButtons(Vector<ToolBarButton> newButtons) {
        buttons = new Vector<>();
        newButtons.forEach(button -> buttons.add((ToolBarButton) button.clone()));
    }

    public void setButtonsVector(Vector<ToolBarButton> buttons) {
        constraints.setMinimumWidth(-1);
        this.buttons = buttons;
    }

    public void setVisible(boolean visible) {

        if (this.visible == visible)
            return;

        this.visible = visible;
        if (this.visible) {
            constraints.setRow(ToolBarProperties.getNextToolbarRow());
            constraints.setPosition(0);

        } else
            constraints.reset();
    }

    public boolean hasButtons() {
        return buttons != null && !buttons.isEmpty();
    }

    public Vector<ToolBarButton> getButtonsVector() {
        return buttons;
    }

    public ToolBarButton[] getButtonsArray() {
        if (buttons != null && !buttons.isEmpty())
            return buttons.toArray(new ToolBarButton[]{});

        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object clone() {
        ToolBarWrapper wrapper = new ToolBarWrapper(name, visible);
        wrapper.setConstraints((ToolBarConstraints) constraints.clone());

        if (buttons != null)
            wrapper.resetButtons((Vector<ToolBarButton>) buttons.clone());

        return wrapper;
    }

    @Override
    public String toString() {
        return name;
    }

}
