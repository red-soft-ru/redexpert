/*
 * ToolBarConstraints.java
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

package org.underworldlabs.swing.toolbar;

import java.io.Serializable;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class ToolBarConstraints implements Cloneable,
                                           Serializable {
    
    private int row;
    private int position;
    private int locX;
    private int resizeOffsetX;
    private int minimumWidth;
    private int preferredWidth;
    private int currentWidth;
    
    public ToolBarConstraints() {
        resizeOffsetX = -1;
        minimumWidth = -1;
        preferredWidth = -1;
    }
    
    public ToolBarConstraints(int row, int position) {
        resizeOffsetX = -1;
        minimumWidth = -1;
        preferredWidth = -1;
        locX = -1;
        this.row = row;
        this.position = position;
    }
    
    public ToolBarConstraints(int row, int position, int minimumWidth) {
        resizeOffsetX = -1;
        preferredWidth = -1;
        locX = -1;
        this.minimumWidth = minimumWidth;
        this.row = row;
        this.position = position;
    }
    
    public ToolBarConstraints(int row, int position,
                                int minimumWidth, int preferredWidth) {
        resizeOffsetX = -1;
        locX = -1;
        this.minimumWidth = minimumWidth;
        this.row = row;
        this.position = position;
        this.preferredWidth = preferredWidth;
    }
    
    public void reset() {
        resizeOffsetX = -1;
        minimumWidth = -1;
        preferredWidth = -1;
        locX = -1;
        row = -1;
        position = -1;
    }
    
    public void setCurrentWidth(int currentWidth) {
        this.currentWidth = currentWidth;
    }
    
    public int getCurrentWidth() {
        return currentWidth;
    }
    
    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
    
    public int getPreferredWidth() {
        return preferredWidth;
    }
    
    public void setLocX(int locX) {
        this.locX = locX;
    }
    
    public int getLocX() {
        return locX == -1 ? position : locX;
    }
    
    public void setPosition(int position) {
        this.position = position;
    }
    
    public int getPosition() {
        return position;
    }
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public void setMinimumWidth(int minimumWidth) {
        this.minimumWidth = minimumWidth;
    }
    
    public int getMinimumWidth() {
        return minimumWidth;
    }
    
    public void setResizeOffsetX(int resizeOffsetX) {
        this.resizeOffsetX = resizeOffsetX;
    }
    
    public int getResizeOffsetX() {
        return resizeOffsetX;
    }
    
    public void setConstraints(int row, int position) {
        this.row = row;
        this.position = position;
    }
    
    public Object clone() {
        
        try {
            ToolBarConstraints c = (ToolBarConstraints)super.clone();
            return c;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        
    }
    
    public String toString() {
        return "ToolBarConstraints[row: " + row + ", position: " + position +
                    ", resizeOffsetX: " + resizeOffsetX + ", minimumWidth: " +
                    minimumWidth + ", preferredWidth: " + preferredWidth +
                    ", currentWidth: " + currentWidth + "]";
    }
    
}






