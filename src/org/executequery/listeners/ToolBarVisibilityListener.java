/*
 * ToolBarVisibilityListener.java
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

package org.executequery.listeners;

import org.executequery.event.ApplicationEvent;
import org.executequery.event.ApplicationEventListener;
import org.executequery.toolbars.ToolBarEvent;
import org.underworldlabs.swing.GUIUtils;
import org.underworldlabs.swing.toolbar.ToolBarProperties;

public class ToolBarVisibilityListener implements ApplicationEventListener {

    @SuppressWarnings("unused")
    public void toolBarChanged(ToolBarEvent e) {
        GUIUtils.invokeLater(ToolBarProperties::saveTools);
    }

    @Override
    public boolean canHandleEvent(ApplicationEvent event) {
        return (event instanceof ToolBarEvent);
    }
}
