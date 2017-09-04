/*
 * BlobRecordDataItem.java
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

import java.sql.Blob;
import java.sql.SQLException;

import org.executequery.log.Log;
import org.executequery.util.mime.MimeType;
import org.executequery.util.mime.MimeTypes;

public class BlobRecordDataItem extends AbstractLobRecordDataItem {

//    private static final String UNKNOWN_TYPE = "Unknown BLOB";

    private static final String BLOB_DATA_OBJECT = "<BLOB Data Object>";

    public BlobRecordDataItem(String name, int dataType, String dataTypeName) {

        super(name, dataType, dataTypeName);
    }

    @Override
    public Object getDisplayValue() {

        return BLOB_DATA_OBJECT;
    }

    @Override
    public String getLobRecordItemName() {

        MimeType mimeType = mimeTypeFromByteArray(getData());
        if (mimeType != null) {

            return mimeType.getName();

        } else {

            return getDataTypeName() + " Type"; //UNKNOWN_TYPE;
        }

    }

    @Override
    protected byte[] readLob() {

        Object value = getValue();
        if (value instanceof String) { // eg. oracle RAW type

            return ((String) getValue()).getBytes();

        } else if (value instanceof byte[]) {

            return (byte[]) value;
        }

        byte[] blobBytes;
        Blob blob = (Blob) value;
        try {

            blobBytes = blob.getBytes(1, (int) blob.length());

        } catch (SQLException e) {

            if (Log.isDebugEnabled()) {

                Log.debug("Error reading BLOB data", e);
            }

            return e.getMessage().getBytes();

        }
        return blobBytes;
    }

    private MimeType mimeTypeFromByteArray(byte[] data) {

        return MimeTypes.get().getMimeType(data);
    }

    @Override
    public boolean isBlob() {

        return true;
    }

}

