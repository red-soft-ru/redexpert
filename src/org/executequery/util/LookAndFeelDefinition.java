/*
 * LookAndFeelDefinition.java
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

package org.executequery.util;

/* ----------------------------------------------------------
 * CVS NOTE: Changes to the CVS repository prior to the 
 *           release of version 3.0.0beta1 has meant a 
 *           resetting of CVS revision numbers.
 * ----------------------------------------------------------
 */

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
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class LookAndFeelDefinition {
    
    /** The look and feel name */
    private String name;
    
    /** The path to the library jar file */
    private String libPath;
    
    /** The look and feel class name */
    private String className;
    
    /** The path to the theme pack ZIP file for Skin L&F */
    private String themePack;
    
    /** Identifies whether this is a Skin L&F */
    private int skinLookAndFeel;
    
    /** Whethet this look and feel is selected */
    private boolean installed;
    
    
    public LookAndFeelDefinition(String name) {
        this.name = name;
    }
    
    public LookAndFeelDefinition() {}
    
    public boolean isInstalled() {
        return installed;
    }
    
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
    
    public boolean isSkinLookAndFeel() {
        return skinLookAndFeel == 1;
    }
    
    public void setIsSkinLookAndFeel(int skinLookAndFeel) {
        this.skinLookAndFeel = skinLookAndFeel;
    }
    
    public String getThemePack() {
        return themePack;
    }
    
    public void setThemePack(String themePack) {
        this.themePack = themePack;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getLibraryPath() {
        return libPath;
    }
    
    public void setLibraryPath(String libPath) {
        this.libPath = libPath;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
    
}













