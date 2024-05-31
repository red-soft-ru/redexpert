/*
 * LookAndFeelDefinition.java
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

/**
 * The <code>LookAndFeelDefinition</code> describes
 * a custom look and feel installed by the user. It
 * maintains information about the location of the JAR
 * library containing the look and feel as well as the
 * class name extending <code>LookAndFeel</code>.<br>
 * Additional properties are also provided to support
 * the Skin Look and Feel and its associated requirements
 * for a configuration XML file.
 *
 * @author Takis Diakoumis
 */
public class LookAndFeelDefinition {

    private String name;
    private String libPath;
    private String className;

    public LookAndFeelDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibraryPath() {
        return libPath;
    }

    public void setLibraryPath(String libPath) {
        this.libPath = libPath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return getName();
    }

}
