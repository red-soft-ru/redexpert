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

import java.io.Serializable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1780 $
 * @date     $Date: 2017-09-03 15:52:36 +1000 (Sun, 03 Sep 2017) $
 */
public class ErdTableDependency implements Serializable {
    
    private ErdTable table_1;
    private ErdTable table_2;
    
    /** Start x position */
    private int xPosn_1;
    /** Middle x position */
    private int xPosn_2;
    /** Final x position */
    private int xPosn_3;
    /** Start y position */
    private int yPosn_1;
    /** Middle y position */
    private int yPosn_2;
    /** Final y position */
    private int yPosn_3;
    /** The relative position of each table */
    private int position;
    
    protected static final int POSITION_1 = 0;
    protected static final int POSITION_2 = 1;
    protected static final int POSITION_3 = 2;
    protected static final int POSITION_4 = 3;
    protected static final int POSITION_5 = 4;
    protected static final int POSITION_6 = 5;
    
    public ErdTableDependency(ErdTable table_1, ErdTable table_2) {
        this.table_1 = table_1;
        this.table_2 = table_2;
        reset();
    }
    
    public void reset() {
        xPosn_1 = -1;
        xPosn_2 = -1;
        xPosn_3 = -1;
        yPosn_1 = -1;
        yPosn_2 = -1;
        yPosn_3 = -1;
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
    
    public int getXPosn_1() {
        return xPosn_1;
    }
    
    public int getXPosn_2() {
        return xPosn_2;
    }
    
    public int getXPosn_3() {
        return xPosn_3;
    }
    
    public int getYPosn_1() {
        return yPosn_1;
    }
    
    public int getYPosn_2() {
        return yPosn_2;
    }
    
    public int getYPosn_3() {
        return yPosn_3;
    }
    
    public void setXPosn_1(int xPosn_1) {
        this.xPosn_1 = xPosn_1;
    }
    
    public void setXPosn_2(int xPosn_2) {
        this.xPosn_2 = xPosn_2;
    }
    
    public void setXPosn_3(int xPosn_3) {
        this.xPosn_3 = xPosn_3;
    }
    
    public void setYPosn_1(int yPosn_1) {
        this.yPosn_1 = yPosn_1;
    }
    
    public void setYPosn_2(int yPosn_2) {
        this.yPosn_2 = yPosn_2;
    }
    
    public void setYPosn_3(int yPosn_3) {
        this.yPosn_3 = yPosn_3;
    }
    
    public void clean() {
        table_1 = null;
        table_2 = null;
    }
    
}











