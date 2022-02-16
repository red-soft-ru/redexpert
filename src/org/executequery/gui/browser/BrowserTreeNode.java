/*
 * BrowserTreeNode.java
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

package org.executequery.gui.browser;

import org.executequery.databaseobjects.NamedObject;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Takis Diakoumis
 */
public class BrowserTreeNode extends DefaultMutableTreeNode {

    private final boolean typeParent;
    private boolean expanded;

    private BaseDatabaseObject _userObject;

    private final NamedObject userObject;

    public BrowserTreeNode(NamedObject userObject,
                           boolean allowsChildren) {
        this(userObject, allowsChildren, true);
    }

    public BrowserTreeNode(NamedObject userObject,
                           boolean allowsChildren,
                           boolean typeParent) {
        super(userObject, allowsChildren);
        this.userObject = userObject;
        this.typeParent = typeParent;
        expanded = false;

    }


    public int getNodeType() {
        return userObject.getType();
    }

    /**
     * Returns whether this is the parent node of this type.
     *
     * @return true | false
     */
    public boolean isTypeParent() {
        return typeParent;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String toString() {
//        return userObject.toString();
        return userObject.getShortName();
    }

    public NamedObject getDatabaseObject() {
        return userObject;
    }

    public BaseDatabaseObject getDatabaseUserObject() {
        return _userObject;
    }

    public boolean isLeaf() {
        if (userObject.getType() == NamedObject.HOST) {
            return super.isLeaf();
        } else {
            return !allowsChildren;
        }
    }

}


