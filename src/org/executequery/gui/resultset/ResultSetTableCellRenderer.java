/*
 * ResultSetTableCellRenderer.java
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

package org.executequery.gui.resultset;

import java.awt.Color;
import java.awt.Component;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import org.executequery.Constants;
import org.underworldlabs.util.MiscUtils;
import org.underworldlabs.util.SystemProperties;

// much of this from the article Christmas Tree Applications at
// http://java.sun.com/products/jfc/tsc/articles/ChristmasTree
// and is an attempt at a better performing cell renderer for the
// results table.

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1545 $
 * @date     $Date: 2015-12-22 10:22:44 +1100 (Tue, 22 Dec 2015) $
 */
class ResultSetTableCellRenderer extends DefaultTableCellRenderer {

    private Color background;
    private Color foreground;

    private Color selectionForeground;
    private Color selectionBackground;

    private Color tableForeground;
    private Color tableBackground;

    private Color editableForeground;
    private Color editableBackground;

    private Border focusBorder;

    private SimpleDateFormat dateFormat;

    private String nullValueDisplayString;

    private Color nullValueDisplayColor;
    private Color numericValueDisplayColor;
    private Color otherValueDisplayColor;
    private Color booleanValueDisplayColor;
    private Color dateValueDisplayColor;
    private Color charValueDisplayColor;
    private Color blobValueDisplayColor;
    
    private Color alternatingRowBackground;

    private boolean rightAlignNumeric;

    ResultSetTableCellRenderer() {

        focusBorder = loadUIBorder("Table.focusCellHighlightBorder");
        editableForeground = loadUIColour("Table.focusCellForeground");
        editableBackground = loadUIColour("Table.focusCellBackground");
        selectionForeground = loadUIColour("Table.selectionForeground");
        selectionBackground = loadUIColour("Table.selectionBackground");
        tableForeground = loadUIColour("Table.foreground");

        applyUserPreferences();
    }

    private Border loadUIBorder(String key) {
        return UIManager.getBorder(key);
    }

    private Color loadUIColour(String key) {
        return UIManager.getColor(key);
    }

    public Component getTableCellRendererComponent(
				JTable table, Object value,
				boolean isSelected, boolean hasFocus,
				int row, int column) {

        if (isSelected) {

            setForeground(selectionForeground);
            setBackground(selectionBackground);

        } else {

            if (tableBackground == null) {

                tableBackground = table.getBackground();
            }

            setForeground(tableForeground);
            if (row % 2 > 0) {
                
                setBackground(alternatingRowBackground);

            } else {
             
                setBackground(tableBackground);
            }
        }

        if (hasFocus) {

            setBorder(focusBorder);

            if (table.isCellEditable(row, column)) {

                setForeground(editableForeground);
                setBackground(editableBackground);
            }

        } else {

            setBorder(noFocusBorder);
        }

        formatValueForDisplay(value, isSelected);
        if (rightAlignNumeric) {

            alignNumeric(value);
        }

        return this;
    }

    private void alignNumeric(Object value) {

        RecordDataItem recordDataItem = (RecordDataItem) value;
        if (recordDataItem == null || recordDataItem.isValueNull()) {

            return;
        }

        int sqlType = recordDataItem.getDataType();
        switch (sqlType) {

            case Types.TINYINT:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                setHorizontalAlignment(SwingConstants.RIGHT);
                break;

            default:
                setHorizontalAlignment(SwingConstants.LEFT);
                break;
        }

    }

    private void formatValueForDisplay(Object value, boolean isSelected) {

        if (value != null) {

        	if (value instanceof RecordDataItem) {

        		RecordDataItem recordDataItem = (RecordDataItem) value;
        		if (recordDataItem.isValueNull()) {

        		    formatForNullValue(isSelected);
        			return;

        		} else {

        			formatForDataItem(recordDataItem, isSelected);
        			return;
        		}

        	} else {

        	    formatForOther(value, isSelected);
        	}

        } else {

            formatForNullValue(isSelected);
        }

    }

    private void formatForOther(Object value, boolean isSelected) {

        if (!isSelected) {

            setBackground(otherValueDisplayColor);
        }

        setValue(value);
    }

    private void formatForDataItem(RecordDataItem recordDataItem, boolean isSelected) {

        boolean isDateValue = false;
        Color color = tableBackground;
        int sqlType = recordDataItem.getDataType();
        
        switch (sqlType) {

            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.CLOB:
                color = charValueDisplayColor;
                break;

            case Types.BIT:
            case Types.BOOLEAN:
                color = booleanValueDisplayColor;
                break;

            case Types.TINYINT:
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
                color = numericValueDisplayColor;
                break;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                color = dateValueDisplayColor;
                isDateValue = true;
                break;

            case Types.LONGVARBINARY:
            case Types.VARBINARY:
            case Types.BINARY:
            case Types.BLOB:
                color = blobValueDisplayColor;

            default:
                color = otherValueDisplayColor;

        }

        Object value = recordDataItem.getDisplayValue();

        if (!isDateValue) {

            setValue(value);

        } else {

            // account for possible dump on parse conversion
            if (value instanceof Date) {
                
                setValue(dateFormatted((Date) value));

            } else {
                
                setValue(value);
            }
            
        }

        if (!isSelected) {

//            if (color.getRGB() != Color.WHITE.getRGB()) {
            if (color.getRGB() != tableBackground.getRGB()) {

                // if its not the bg, apply the bg otherwise run 
                // with alternating bg alreday set 
                
                setBackground(color);
            }

        }

    }

    private void formatForNullValue(boolean isSelected) {

        setValue(nullValueDisplayString);
        setHorizontalAlignment(SwingConstants.CENTER);
        if (!isSelected) {

            setBackground(nullValueDisplayColor);
        }
        
    }

    public void applyUserPreferences() {

        String datePattern = SystemProperties.getProperty(
                Constants.USER_PROPERTIES_KEY, "resuts.date.pattern");

        if (!MiscUtils.isNull(datePattern)) {

            dateFormat = new SimpleDateFormat(datePattern);

        } else {

            dateFormat = null;
        }

        rightAlignNumeric = SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.right.align.numeric");

        nullValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.null.background.colour");

        blobValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.blob.background.colour");

        charValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.char.background.colour");

        dateValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.date.background.colour");

        booleanValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.boolean.background.colour");

        otherValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.other.background.colour");

        numericValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.numeric.background.colour");

        alternatingRowBackground = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.alternating.row.background");
        
        nullValueDisplayString = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.null.text");
    }

    private String dateFormatted(Date date) {

        if (dateFormat != null) {

            return dateFormat.format(date);

        } else {

            return date.toString();
        }
    }

    public void setTableBackground(Color c) {

        this.tableBackground = c;
    }

    public void setBackground(Color c) {

        this.background = c;
    }

    public Color getBackground() {

        return background;
    }

    public void setForeground(Color c) {

        this.foreground = c;
    }

    public Color getForeground() {

        return foreground;
    }

    public boolean isOpaque() {

        return background != null;
    }

    public void invalidate() {}
    public void repaint() {}
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}

    
}

