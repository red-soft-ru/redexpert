/*
 * MenuItem.java
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

package org.executequery.gui.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.underworldlabs.util.MiscUtils;

public class MenuItem {

    private static final String NULL_VALUE = "{-NULL-}";
    
    private static final String SEPARATOR_ID = "separator";

    private String id;
    
    private String mnemonic;
    
    private String name;
    
    private String implementingClass;
    
    private String actionCommand;
    
    private String acceleratorKey;
    
    private String toolTip;
    
    private String propertyKey;

    private MenuItem parent;
    
    private List<MenuItem> children;

    private int index = -1;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return !MiscUtils.isNull(getId());
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public boolean hasMnemonic() {
        return !MiscUtils.isNull(getMnemonic());
    }
    
    public String getImplementingClass() {
        return implementingClass;
    }

    public void setImplementingClass(String implementingClass) {
        this.implementingClass = implementingClass;
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    public boolean hasActionCommand() {
        return !MiscUtils.isNull(getActionCommand());
    }
    
    public String getAcceleratorKey() {
        return acceleratorKey;
    }

    public void setAcceleratorKey(String acceleratorKey) {        
        this.acceleratorKey = acceleratorKey;
    }

    public boolean isAcceleratorKeyNull() {
        return getAcceleratorKey() == null || NULL_VALUE.equals(getAcceleratorKey());
    }
    
    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public boolean hasToolTip() {
        return !MiscUtils.isNull(getToolTip());
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public boolean isSeparator() {
        return SEPARATOR_ID.equals(getId());
    }
    
    public void add(MenuItem child) {
        
        if (children == null) {
            
            children = new ArrayList<MenuItem>();
        }
        
        children.add(child);
    }
    
    public boolean hasChildren() {
        
        return (getChildren() != null && getChildren().size() > 0);
    }
    
    public List<MenuItem> getChildren() {
        
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return !MiscUtils.isNull(getName());
    }

    public boolean hasParent() {
        return (getParent() != null);
    }
    
    public MenuItem getParent() {
        return parent;
    }

    public void setParent(MenuItem parent) {
        this.parent = parent;
    }

    public int getMnemonicChar() {

        if (getMnemonic() == null) {
            
            return 0;
        }
        
        return getMnemonic().charAt(0);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean hasIndex() {
        return (index != -1);
    }

    public boolean hasPropertyKey() {
        return StringUtils.isNotBlank(propertyKey);
    }
    
}

