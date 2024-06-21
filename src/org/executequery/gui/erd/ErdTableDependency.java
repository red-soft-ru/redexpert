/*
 * ErdTableDependency.java
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

package org.executequery.gui.erd;

import org.executequery.gui.browser.ColumnData;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Takis Diakoumis
 */
public class ErdTableDependency implements Serializable {

    private ErdTable table_1;
    private ErdTable table_2;

    private final ColumnData column1;
    private final ColumnData column2;

    List<Point> points;
    private int position;

    protected static final int POSITION_1 = 0;
    protected static final int POSITION_2 = 1;
    protected static final int POSITION_3 = 2;
    protected static final int POSITION_4 = 3;
    protected static final int POSITION_5 = 4;
    protected static final int POSITION_6 = 5;

    public ErdTableDependency(ErdTable table_1, ErdTable table_2, ColumnData column1, ColumnData column2) {
        this.table_1 = table_1;
        this.table_2 = table_2;
        this.column1 = column1;
        this.column2 = column2;
        reset();
    }

    public void reset() {
        if (points == null)
            points = new ArrayList<>();
        points.clear();
        position = -1;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ErdTable getTable_2() {
        return table_2;
    }

    public ErdTable getTable_1() {
        return table_1;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void clean() {
        table_1 = null;
        table_2 = null;
    }

    public ColumnData getColumn1() {
        return column1;
    }

    public ColumnData getColumn2() {
        return column2;
    }
}











