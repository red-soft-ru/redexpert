/*
 * RecordDataItem.java
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

import org.underworldlabs.swing.table.TableCellValue;

/**
 * @author Takis Diakoumis
 */
public interface RecordDataItem extends TableCellValue, Comparable {

    boolean isDeleted();

    void setDeleted(boolean deleted);

    boolean isNew();

    void setNew(boolean newRecord);

    boolean isGenerated();

    void setGenerated(boolean generated);

    Object getNewValue();

    int length();

    int getDataType();

    String getName();

    Object getDisplayValue();

    boolean isDisplayValueNull();

    void setValue(Object value);

    boolean isValueNull();

    void setNull();

    void valueChanged(Object newValue);

    boolean isChanged();

    boolean isSQLValueNull();

    boolean isNewValueNull();

    Object getValueAsType();

    boolean isLob();

    boolean isBlob();

    boolean valueContains(String pattern);

}






