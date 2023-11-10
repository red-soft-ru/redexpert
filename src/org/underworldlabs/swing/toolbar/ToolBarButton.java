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
import org.underworldlabs.swing.util.IconUtilities;

import javax.swing.*;
import java.io.Serializable;

/**
 * @author Takis Diakoumis
 */
public class ToolBarButton implements Serializable, Cloneable {

    private int id;
    private Action action;
    private String actionId;
    private ImageIcon icon;
    private boolean visible;
    private int order;

    /**
     * Defines a tool bar separator
     */
    public static final int SEPARATOR_ID = 29;

    public ToolBarButton(int id) {
        this.id = id;
    }

    public ToolBarButton(int id, String actionId) {
        this.id = id;
        this.actionId = actionId;
        action = ActionBuilder.get(actionId);
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
        action = ActionBuilder.get(actionId);
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

    public void invertSelected() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            if (id == SEPARATOR_ID) {
                icon = IconUtilities.loadDefaultIconResource("Blank16.svg", true);
            } else {
                if (action != null) {
                    icon = (ImageIcon) action.getValue(Action.SMALL_ICON);
                }
            }
        }

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

    public String toString() {
        return getName();
    }

    public Object clone() {
        try {
            ToolBarButton button = (ToolBarButton) super.clone();
            return button;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public static String bundleString(String key) {
        return Bundles.get(ToolBarButton.class, key);
    }

}







