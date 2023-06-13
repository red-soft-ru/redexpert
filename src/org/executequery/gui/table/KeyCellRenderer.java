/*
 * KeyCellRenderer.java
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

package org.executequery.gui.table;

import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ColumnData;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * @author Takis Diakoumis
 */
public class KeyCellRenderer extends JLabel
        implements TableCellRenderer {

    private static ImageIcon fkImage;
    private static ImageIcon pkImage;
    private static ImageIcon pkfkImage;
    private static ImageIcon deleteImage;
    private static ImageIcon newImage;

//    private static final String PRIMARY = "PK";
//    private static final String FOREIGN = "FK";
//    private static final String PRIMARY_AND_FOREIGN = "PKFK";

    static {
        deleteImage = GUIUtilities.loadIcon("MarkDeleted16.svg", true);
        newImage = GUIUtilities.loadIcon("MarkNew16.svg", true);
        fkImage = GUIUtilities.loadIcon("ForeignKeyImage.svg", true);
        pkImage = GUIUtilities.loadIcon("PrimaryKeyImage.svg", true);
        pkfkImage = GUIUtilities.loadIcon("PrimaryForeignKeyImage.svg", true);
    }

    public KeyCellRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        if (value != null) {
            if (value instanceof ColumnData) {
                ColumnData columnData = (ColumnData) value;
                if (columnData.isMarkedDeleted()) {
                    setIcon(deleteImage);
                    setToolTipText("This column marked to be dropped");
                } else if (columnData.isNewColumn()) {
                    setIcon(newImage);
                    setToolTipText("This column marked new");
                } else if (columnData.isPrimaryKey()) {

                    if (columnData.isForeignKey()) {
                        setIcon(pkfkImage);
                        setToolTipText("Primary Key/Foreign Key");
                    } else {
                        setIcon(pkImage);
                        setToolTipText("Primary Key");
                    }

                } else if (columnData.isForeignKey()) {
                    setIcon(fkImage);
                    setToolTipText("Foreign Key");
                } else {
                    setIcon(null);
                }
            } else if (value instanceof String) {
                String svalue = (String) value;
                if (svalue.trim().equals("PRIMARY KEY")) {
                    setIcon(pkImage);
                    setToolTipText("Primary Key");
                } else if (svalue.trim().equals("FOREIGN KEY")) {
                    setIcon(fkImage);
                    setToolTipText("Foreign Key");
                } else {
                    setIcon(null);
                }
            }
        }else setIcon(null);

        setHorizontalAlignment(JLabel.CENTER);
        
        /*
        if (keyValue.equals(PRIMARY_AND_FOREIGN)) {
            setIcon(pkfkImage);
        }
        else if (keyValue.equals(FOREIGN)) {
            setIcon(fkImage);
        }
        else if (keyValue.equals(PRIMARY)) {
            setIcon(pkImage);
        }
        else {
            setIcon(null);
        }
         */
        return this;
    }

}







