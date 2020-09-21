/*
 * ClobRecordDataItem.java
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

import biz.redsoft.IFBClob;
import org.apache.commons.lang.CharUtils;
import org.executequery.Constants;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.log.Log;
import org.underworldlabs.util.SystemProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Objects;

public class ClobRecordDataItem extends AbstractLobRecordDataItem {

    private int displayLength;

    private static final String CLOB_DATA = "<CLOB Data>";

    private String displayValue;

    private String charset;

    public ClobRecordDataItem(String name, int dataType, String dataTypeName) {

        super(name, dataType, dataTypeName);

        displayLength = SystemProperties.getIntProperty(
                Constants.USER_PROPERTIES_KEY, "results.table.clob.length");
    }

    @Override
    public Object getDisplayValue() {
        String dataAsText = null;
        byte[] data = readLob(displayLength);
        boolean isValidText = true;

        if (data != null) {
            if (charset == null || Objects.equals(charset, CreateTableSQLSyntax.NONE))
                dataAsText = new String(data);
            else try {
                dataAsText = new String(data, charset);
            } catch (UnsupportedEncodingException e) {
                Log.error("Error method loadTextData in class LobDataItemViewerPanel:", e);
                dataAsText = new String(data);
            }
            char[] charArray = dataAsText.toCharArray();

            int defaultEndPoint = 256;
            int endPoint = Math.min(charArray.length, defaultEndPoint);
            if (charset == null || charset.equals(CreateTableSQLSyntax.NONE))
                for (int i = 0; i < endPoint; i++) {

                    if (!CharUtils.isAscii(charArray[i])) {

                        isValidText = false;
                        break;
                    }

                }

        } else {

            isValidText = false;
        }

        if (isValidText) {

            return dataAsText;

        } else {

            return CLOB_DATA;
        }


    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public Object getNewValue() {
        return new ByteArrayInputStream(getData());
    }

    @Override
    public String getLobRecordItemName() {

        return getDataTypeName();
    }


    @Override
    protected byte[] readLob() {

        if (isValueNull())
            return null;

        Object value = getValue();
        if (value instanceof String) {

            return ((String) value).getBytes();
        }

        if (value.getClass().getName().contains("FBClobImpl")) {
            IFBClob ifbClob = (IFBClob) value;
            InputStream as;
            try {
                as = ifbClob.open();
                byte[] b = new byte[1024];
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                int length;
                while ((length = as.read(b)) != -1) {
                    result.write(b, 0, length);
                }
                as.close();
                ifbClob.close();
                return result.toByteArray();


                //reader = clob.getCharacterStream();

            } catch (SQLException e) {

                if (Log.isDebugEnabled()) {

                    Log.debug("Error reading CLOB data", e);
                }

                return e.getMessage().getBytes();
            } catch (Exception e) {
                Log.error("Error reading CLOB data:" + e.getMessage());
                return "Error reading CLOB data:".getBytes();
            }
        } else {

            Clob clob = (Clob) value;
            InputStream as;
            try {
                as = clob.getAsciiStream();
                byte[] b = new byte[1024];
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                int length;
                while ((length = as.read(b)) != -1) {
                    result.write(b, 0, length);
                }
                return result.toByteArray();


                //reader = clob.getCharacterStream();

            } catch (SQLException e) {

                if (Log.isDebugEnabled()) {

                    Log.debug("Error reading CLOB data", e);
                }

                return e.getMessage().getBytes();
            } catch (Exception e) {
                Log.error("Error reading CLOB data:" + e.getMessage());
                return "Error reading CLOB data:".getBytes();
            }
        }
    }

    protected byte[] readLob(int displayLength) {

        if (isValueNull())
            return null;

        Object value = getValue();
        if (value instanceof String) {

            return ((String) value).getBytes();
        }

        if (value.getClass().getName().contains("FBClobImpl")) {
            IFBClob ifbClob = (IFBClob) value;
            InputStream as;
            try {
                as = ifbClob.open();
                byte[] b = new byte[1024];
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                int length;
                int i = 0;
                while ((length = as.read(b)) != -1 && i < displayLength) {
                    result.write(b, 0, length);
                    i += length;
                }
                as.close();
                ifbClob.close();
                return result.toByteArray();


                //reader = clob.getCharacterStream();

            } catch (SQLException e) {

                if (Log.isDebugEnabled()) {

                    Log.debug("Error reading CLOB data", e);
                }

                return e.getMessage().getBytes();
            } catch (Exception e) {
                Log.error("Error reading CLOB data:" + e.getMessage());
                return "Error reading CLOB data:".getBytes();
            }
        } else {

            Clob clob = (Clob) value;
            InputStream as;
            try {
                as = clob.getAsciiStream();
                byte[] b = new byte[1024];
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                int i = 0;
                int length;
                while ((length = as.read(b)) != -1 && i < displayLength) {
                    result.write(b, 0, length);
                    i += length;
                }
                return result.toByteArray();


                //reader = clob.getCharacterStream();

            } catch (SQLException e) {

                if (Log.isDebugEnabled()) {

                    Log.debug("Error reading CLOB data", e);
                }

                return e.getMessage().getBytes();
            } catch (Exception e) {
                Log.error("Error reading CLOB data:" + e.getMessage());
                return "Error reading CLOB data:".getBytes();
            }
        }
    }

    @Override
    public String toString() {

        if (isDisplayValueNull()) {

            return null;
        }
        return getDisplayValue().toString();
    }

    public boolean isBlob() {

        return true;
    }
}


