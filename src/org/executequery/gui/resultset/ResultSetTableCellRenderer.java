/*
 * ResultSetTableCellRenderer.java
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

package org.executequery.gui.resultset;

import org.executequery.Constants;
import org.executequery.databaseobjects.Types;
import org.underworldlabs.util.SystemProperties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

// much of this from the article Christmas Tree Applications at
// http://java.sun.com/products/jfc/tsc/articles/ChristmasTree
// and is an attempt at a better performing cell renderer for the
// results table.

/**
 * @author Takis Diakoumis
 */
class ResultSetTableCellRenderer extends DefaultTableCellRenderer {

    private Color background;
    private Color foreground;
    private Color tableBackground;

    private final Color selectionForeground;
    private final Color selectionBackground;
    private final Color tableForeground;
    private final Color editableForeground;
    private final Color editableBackground;
    private final Border focusBorder;

    private String nullValueDisplayString;
    private Color nullValueDisplayColor;
    private Color nullValueAddDisplayColor;
    private Color nullValueDeleteDisplayColor;
    private Color numericValueDisplayColor;
    private Color otherValueDisplayColor;
    private Color booleanValueDisplayColor;
    private Color dateValueDisplayColor;
    private Color charValueDisplayColor;
    private Color blobValueDisplayColor;
    private Color changedValueDisplayColor;
    private Color deletedValueDisplayColor;
    private Color newValueDisplayColor;
    private Color focusRowBackground;
    private Color alternatingRowBackground;

    private boolean otherColorForNull;
    private String alignNumeric;
    private String alignText;
    private String alignBool;
    private String alignNull;
    private String alignOther;

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

    private void alignNumeric(Object value) {

        RecordDataItem recordDataItem = (RecordDataItem) value;
        if (recordDataItem == null || recordDataItem.isDisplayValueNull())
            return;

        int sqlType = recordDataItem.getDataType();
        switch (sqlType) {

            case Types.TINYINT:
            case Types.INT128:
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
                //setHorizontalAlignment(SwingConstants.LEFT);
                break;
        }

    }

    private void alignText(Object value) {

        RecordDataItem recordDataItem = (RecordDataItem) value;
        if (recordDataItem == null || recordDataItem.isDisplayValueNull())
            return;

        int sqlType = recordDataItem.getDataType();
        switch (sqlType) {

            case Types.VARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.NCLOB:
            case Types.NVARCHAR:
                setHorizontalAlignment(SwingConstants.LEFT);
                break;

            default:
                break;
        }

    }

    protected void setAlign(String align) {
        if (align != null) {
            switch (align) {
                case "left":
                    setHorizontalAlignment(SwingConstants.LEFT);
                    break;
                case "right":
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    break;
                default:
                    setHorizontalAlignment(SwingConstants.CENTER);
                    break;
            }
        }
    }

    private void formatValueForDisplay(Object value, boolean isSelected) {

        setAlign(alignOther);
        if (value != null) {
            if (value instanceof RecordDataItem) {

                RecordDataItem recordDataItem = (RecordDataItem) value;
                if (recordDataItem.isDisplayValueNull())
                    formatForNullValue(isSelected, recordDataItem.isChanged(), recordDataItem.isDeleted(), recordDataItem.isNew());
                else
                    formatForDataItem(recordDataItem, isSelected);

            } else
                formatForOther(value, isSelected);
        } else
            formatForNullValue(isSelected, false, false, false);

    }

    private void formatForOther(Object value, boolean isSelected) {

        if (!isSelected)
            setBackground(otherValueDisplayColor);
        setValue(value);
    }

    private void formatForDataItem(RecordDataItem recordDataItem, boolean isSelected) {

        int sqlType = recordDataItem.getDataType();
        Object value = recordDataItem.isChanged() ? recordDataItem.getNewValue() : recordDataItem.getValue();

        Color color;
        boolean isDateValue = false;
        boolean isBlobValue = false;

        if (recordDataItem.isNew() && newValueDisplayColor.getRGB() != tableBackground.getRGB()) {
            color = newValueDisplayColor;

        } else if (recordDataItem.isDeleted() && deletedValueDisplayColor.getRGB() != tableBackground.getRGB()) {
            color = deletedValueDisplayColor;

        } else if (recordDataItem.isChanged() && changedValueDisplayColor.getRGB() != tableBackground.getRGB()) {
            color = changedValueDisplayColor;

        } else {
            switch (sqlType) {

                case Types.LONGVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.CHAR:
                case Types.NCHAR:
                case Types.VARCHAR:
                case Types.NVARCHAR:
                case Types.CLOB:
                    color = charValueDisplayColor;
                    setAlign(alignText);
                    break;

                case Types.BIT:
                case Types.BOOLEAN:
                    color = booleanValueDisplayColor;
                    setAlign(alignBool);
                    break;

                case Types.TINYINT:
                case Types.INT128:
                case Types.BIGINT:
                case Types.NUMERIC:
                case Types.DECIMAL:
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                    color = numericValueDisplayColor;
                    setAlign(alignNumeric);
                    break;

                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.TIME_WITH_TIMEZONE:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    color = dateValueDisplayColor;
                    isDateValue = true;
                    break;

                case Types.LONGVARBINARY:
                case Types.VARBINARY:
                case Types.BINARY:
                case Types.BLOB:
                    isBlobValue = true;
                    color = blobValueDisplayColor;
                    break;

                default:
                    color = otherValueDisplayColor;

            }
        }

        if (isDateValue) {
            setValue(ValueFormatter.formatted(value));

        } else if (isBlobValue) {
            setValue(recordDataItem.getDisplayValue());

        } else
            setValue(value);

        if (!isSelected) {

            // if it's not the bg, apply the bg otherwise run
            // with alternating bg already set
            if (color.getRGB() != tableBackground.getRGB())
                setBackground(color);

        }

    }

    private void formatForNullValue(boolean isSelected, boolean changed, boolean deleted, boolean newValue) {

        setValue(nullValueDisplayString);
        setAlign(alignNull);

        if (!isSelected) {

            if (!deleted || deletedValueDisplayColor.getRGB() == tableBackground.getRGB()) {

                if (!newValue || newValueDisplayColor.getRGB() == tableBackground.getRGB())
                    setBackground(changed ? changedValueDisplayColor : nullValueDisplayColor);
                else
                    setBackground(otherColorForNull ? nullValueAddDisplayColor : newValueDisplayColor);

            } else
                setBackground(otherColorForNull ? nullValueDeleteDisplayColor : deletedValueDisplayColor);
        }

    }

    public void applyUserPreferences() {

        alignNumeric = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.align.numeric");

        alignText = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.align.text");

        alignBool = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.align.bool");

        alignNull = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.align.null");

        alignOther = SystemProperties.getStringProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.align.other");

        nullValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.null.background.colour");

        nullValueAddDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.null.adding.background.colour");

        nullValueDeleteDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.null.deleting.background.colour");

        changedValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.changed.background.colour");

        deletedValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.deleted.background.colour");

        newValueDisplayColor = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.cell.new.background.colour");

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

        otherColorForNull = SystemProperties.getBooleanProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.use.other.color.null");

        focusRowBackground = SystemProperties.getColourProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.focus.row.background.colour");

    }

    public void setTableBackground(Color c) {
        this.tableBackground = c;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (isSelected) {
            setForeground(selectionForeground);
            setBackground(selectionBackground);

        } else if (row == table.getSelectedRow()) {
            setForeground(tableForeground);
            setBackground(focusRowBackground);

        } else {

            if (tableBackground == null)
                tableBackground = table.getBackground();

            setForeground(tableForeground);
            setBackground((row % 2 > 0) ? alternatingRowBackground : tableBackground);
        }

        if (hasFocus) {

            setBorder(focusBorder);
            if (table.isCellEditable(row, column)) {
                setForeground(editableForeground);
                setBackground(editableBackground);
            }

        } else
            setBorder(noFocusBorder);

        isSelected = isSelected || row == table.getSelectedRow();
        formatValueForDisplay(value, isSelected);

        return this;
    }

    @Override
    public void setBackground(Color c) {
        this.background = c;
    }

    @Override
    public Color getBackground() {
        return background;
    }

    @Override
    public void setForeground(Color c) {
        this.foreground = c;
    }

    @Override
    public Color getForeground() {
        return foreground;
    }

    @Override
    public boolean isOpaque() {
        return background != null;
    }

    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    }

}
