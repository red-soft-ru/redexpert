/*
 * DefaultExcelWorkbookBuilder.java
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

package org.executequery.gui.importexport;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Takis Diakoumis, Pawel Bialkowski
 */
public class DefaultExcelWorkbookBuilder implements ExcelWorkbookBuilder {

    private int currentRow;

    private final CellStyle defaultCellStyle;
    private final SXSSFWorkbook workbook;
    private SXSSFSheet sheet;

    public DefaultExcelWorkbookBuilder() {
        workbook = new SXSSFWorkbook();
        defaultCellStyle = createStyle();
    }

    @Override
    public void reset() {
        currentRow = 0;
        sheet = null;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        workbook.write(outputStream);
    }

    @Override
    public void createSheet(String sheetName) {
        sheet = workbook.createSheet(sheetName);
    }

    @Override
    public void addRow(List<String> values) {
        fillRow(values, createRow(++currentRow), defaultCellStyle);
    }

    @Override
    public void addRowHeader(List<String> values) {

        if (currentRow > 0)
            currentRow++;

        Font font = createFont();
        font.setBold(true);

        CellStyle style = createStyle();
        style.setFont(font);

        fillRow(values, createRow(currentRow), style);
    }

    private SXSSFRow createRow(int rowNumber) {
        return sheet.createRow(rowNumber);
    }

    private void fillRow(List<String> values, SXSSFRow row, CellStyle style) {

        for (int i = 0, n = values.size(); i < n; i++) {
            SXSSFCell cell = row.createCell(i);
            cell.setCellStyle(style);
            try {
                double doubleValue = Double.valueOf(values.get(i));
                cell.setCellValue(doubleValue);
            } catch (Exception e) {
                cell.setCellValue(new XSSFRichTextString(values.get(i)));
            }
        }
    }

    private CellStyle createStyle() {
        return workbook.createCellStyle();
    }

    private Font createFont() {
        return workbook.createFont();
    }

}
