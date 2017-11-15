/*
 * ErdSaveFileFormat.java
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

import java.awt.Color;
import java.awt.Font;

import java.io.Serializable;

/**
 *
 * @author   Takis Diakoumis
 */
public class ErdSaveFileFormat implements Serializable {
    
    /** The canvas background */
    private Color canvasBackground;
    
    /** The table's background colour */
    private Color tableBackground;
    
    /** The font for the column names */
    private Font columnNameFont;
    
    /** The font for the table name */
    private Font tableNameFont;
    
    /** The title panel */
    private ErdTitlePanelData titlePanel;
    
    /** The erd table data */
    private ErdTableFileData[] tables;
    
    /** The absolute path of this file */
    private String absolutePath;
    
    /** The file name */
    private String fileName;
    
    public ErdSaveFileFormat(ErdTableFileData[] tables, String fileName) {
        this.tables = tables;
        this.fileName = fileName;
    }
    
    public ErdSaveFileFormat() {}
    
    public void setTitlePanel(ErdTitlePanelData titlePanel) {
        this.titlePanel = titlePanel;
    }
    
    public ErdTitlePanelData getTitlePanel() {
        return titlePanel;
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }
    
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    
    public boolean hasCanvasBackground() {
        return canvasBackground != null;
    }
    
    public void setTableNameFont(Font tableNameFont) {
        this.tableNameFont = tableNameFont;
    }
    
    public Font getTableNameFont() {
        return tableNameFont;
    }
    
    public void setColumnNameFont(Font columnNameFont) {
        this.columnNameFont = columnNameFont;
    }
    
    public Font getColumnNameFont() {
        return columnNameFont;
    }
    
    public void setTableBackground(Color tableBackground) {
        this.tableBackground = tableBackground;
    }
    
    public Color getTableBackground() {
        return tableBackground;
    }
    
    public void setCanvasBackground(Color canvasBackground) {
        this.canvasBackground = canvasBackground;
    }
    
    public Color getCanvasBackground() {
        return canvasBackground;
    }
    
    public ErdTableFileData[] getTables() {
        return tables;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setTables(ErdTableFileData[] tables) {
        this.tables = tables;
    }
    
    public String toString() {
        return fileName;
    }
    
}







