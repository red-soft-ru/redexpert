/*
 * UserLayoutObject.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
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

import java.io.Serializable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class UserLayoutObject implements Serializable {

	private static final long serialVersionUID = 1L;

	/** the docked component position */
    private int position;

    /** the docked component placement in its position */
    private int index;

    /** the docked component user prefs key */
    private String key;

    /** whether the dock is visible */
    private boolean visible;

    /** whether the dock is minimised */
    private boolean minimised;
    
    /** Creates a new instance of UserLayoutObject */
    public UserLayoutObject() {
        this(null);
    }

    public UserLayoutObject(String key) {
        this.key = key;
        position = -1;
        index = -1;
        visible = false;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMinimised() {
        return minimised;
    }

    public void setMinimised(boolean minimised) {
        this.minimised = minimised;
    }

}










