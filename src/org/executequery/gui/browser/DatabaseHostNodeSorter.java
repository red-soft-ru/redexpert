/*
 * DatabaseHostNodeSorter.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.executequery.gui.browser.nodes.DatabaseHostNode;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
class DatabaseHostNodeSorter {

    public static final String SORT_A_Z = "sortAtoZ";

    public static final String SORT_Z_A = "sortZtoA";

    public static final String SORT_RESTORE = "sortRestore";

    private String lastSort = SORT_RESTORE;

    private Map<String, Comparator<DatabaseHostNode>> comparators;

    public DatabaseHostNodeSorter() {

        comparators = new HashMap<String, Comparator<DatabaseHostNode>>(3);

        comparators.put(SORT_A_Z, new AtoZComparator());
        comparators.put(SORT_Z_A, new ZtoAComparator());
        comparators.put(SORT_RESTORE, new RestoreComparator());
    }

    public Comparator<DatabaseHostNode> getLastComparator() {

        return comparators.get(lastSort);
    }

    @SuppressWarnings("rawtypes")
    public void sort(DefaultMutableTreeNode parent) {

        List<DatabaseHostNode> children = new ArrayList<DatabaseHostNode>(parent.getChildCount());
        for (Enumeration i = parent.children(); i.hasMoreElements();) {

            Object object = i.nextElement();
            if (object instanceof DatabaseHostNode) {
             
                children.add((DatabaseHostNode) object);
            }
        }

        Collections.sort(children, nextComparator());

        // remove - retains folders etc
        for (DatabaseHostNode treeNode : children) {

            parent.remove(treeNode);
        }

        for (DatabaseHostNode treeNode : children) {

            parent.add(treeNode);
        }

    }

    private Comparator<DatabaseHostNode> nextComparator() {

        if (SORT_A_Z.equals(lastSort)) {

            lastSort = SORT_Z_A;

        } else if (SORT_Z_A.equals(lastSort)) {

            lastSort = SORT_RESTORE;

        } else {

            lastSort = SORT_A_Z;
        }

        return comparators.get(lastSort);
    }

    private class AtoZComparator implements Comparator<DatabaseHostNode> {

        public int compare(DatabaseHostNode node1, DatabaseHostNode node2) {

            return node1.getDatabaseConnection().getName().compareToIgnoreCase(
                    node2.getDatabaseConnection().getName());
        }

    }

    private class ZtoAComparator implements Comparator<DatabaseHostNode> {

        public int compare(DatabaseHostNode node1, DatabaseHostNode node2) {

            return (node1.getDatabaseConnection().getName().compareToIgnoreCase(
                    node2.getDatabaseConnection().getName())) * -1;
        }

    }

    private class RestoreComparator implements Comparator<DatabaseHostNode> {

        public int compare(DatabaseHostNode node1, DatabaseHostNode node2) {

            int node1Order = node1.getOrder();
            int node2Order = node2.getOrder();

            if (node1Order < node2Order) {

                return -1;

            } else if (node2Order < node1Order) {

                return 1;

            } else {

                return 0;
            }

        }

    }

}







