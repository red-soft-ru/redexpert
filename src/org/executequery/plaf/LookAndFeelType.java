/*
 * LookAndFeelType.java
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

package org.executequery.plaf;


public enum LookAndFeelType {

    EXECUTE_QUERY("RedXpert Default"),
    EXECUTE_QUERY_DARK("RedXpert Dark Theme"),
    EXECUTE_QUERY_GRADIENT("RedXpert Default 3D"),
    SMOOTH_GRADIENT("Smooth Gradient"),
    BUMPY_GRADIENT("Bumpy Gradient"),
    EXECUTE_QUERY_THEME("RedXpert Theme"),
    METAL("Metal - Classic"),
    OCEAN("Metal - Ocean (JDK1.5+)"),
    WINDOWS("Windows"),
    MOTIF("CDE/Motif"),
    GTK("GTK+"),
    PLUGIN("Plugin"),
    NATIVE("Native");

    private String description;

    private LookAndFeelType(String description) {

        this.description = description;
    }

    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return getDescription();
    }

    public boolean isDarkTheme() {

        return (this == LookAndFeelType.EXECUTE_QUERY_DARK);
    }

    public boolean isExecuteQueryLookCompatible() {

        return (this == SMOOTH_GRADIENT ||
                this == EXECUTE_QUERY_THEME ||
                this == EXECUTE_QUERY ||
                this == EXECUTE_QUERY_DARK ||
                this == EXECUTE_QUERY_GRADIENT);
    }

}


