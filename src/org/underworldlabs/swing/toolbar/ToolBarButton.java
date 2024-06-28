/*
 * ToolBarButton.java
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

import org.executequery.localization.Bundles;
import org.underworldlabs.swing.actions.ActionBuilder;

import javax.swing.*;
import java.io.Serializable;

/**
 * @author Takis Diakoumis
 */
public class ToolBarButton implements Serializable, Cloneable {

    public static final int SEPARATOR_ID = -1;

    private int id;
    private int order;
    private Action action;
    private ImageIcon icon;
    private String actionId;
    private boolean visible;

    public ToolBarButton(int id) {
        this.id = id;
    }

    public ToolBarButton(int id, String actionId) {
        this.id = id;
        this.actionId = actionId;
        this.action = ActionBuilder.get(actionId);
    }

    public String getActionId() {
        return actionId;
    }

    public boolean isSeparator() {
        return id == SEPARATOR_ID;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public ImageIcon getIcon() {

        if (isSeparator())
            return null;

        if (icon == null && action != null)
            icon = (ImageIcon) action.getValue(Action.SMALL_ICON);

        return icon;
    }

    public String getName() {
        return (id == SEPARATOR_ID) ? bundleString("separator") : (String) action.getValue(Action.NAME);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();

        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public static String bundleString(String key) {
        return Bundles.get(ToolBarButton.class, key);
    }

}
