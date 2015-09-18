/*
 * EachRowRenderer.java
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

package org.underworldlabs.swing.table;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class EachRowRenderer implements TableCellRenderer {
    
    protected Hashtable<Integer, TableCellRenderer> renderers;
    protected TableCellRenderer renderer;
    protected TableCellRenderer defaultRenderer;

    public EachRowRenderer() {
        renderers = new Hashtable<Integer, TableCellRenderer>();
        defaultRenderer = new DefaultTableCellRenderer();
    }

    public void add(int row, TableCellRenderer renderer) {
        renderers.put(Integer.valueOf(row),renderer);
    }
    
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, 
                                                   boolean isSelected, 
                                                   boolean hasFocus,
                                                   int row, 
                                                   int column) {

        renderer = (TableCellRenderer)renderers.get(Integer.valueOf(row));

        if (renderer == null) {
            renderer = defaultRenderer;
        }

        if (value != null && renderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer)renderer).setToolTipText(value.toString());
        }

        return renderer.getTableCellRendererComponent(table,
                                                      value, 
                                                      isSelected, 
                                                      hasFocus, 
                                                      row, 
                                                      column);

    }

}






