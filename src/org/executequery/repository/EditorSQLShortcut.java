/*
 * EditorSQLShortcut.java
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

package org.executequery.repository;

import org.underworldlabs.util.MiscUtils;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public final class EditorSQLShortcut {

    private String id;
    
    private String shortcut;
    
    private String query;

    public String getShortcut() {
        return shortcut;
    }

    public String getQuery() {
        return query;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut.toUpperCase();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String toString() {
        return getShortcut();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isNew() {
        return (MiscUtils.isNull(getId()));
    }
    
}











