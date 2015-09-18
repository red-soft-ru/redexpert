/*
 * DefaultExcelWorkbookBuilder.java
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

package org.executequery.gui.importexport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/** 
 *
 * @author   Takis Diakoumis, Pawel Bialkowski
 * @version  $Revision: 1487 $
 * @date     $Date: 2015-08-23 22:21:42 +1000 (Sun, 23 Aug 2015) $
 */
public class DefaultExcelWorkbookBuilder implements ExcelWorkbookBuilder {

    private int currentRow;
    
    private HSSFWorkbook workbook;

    private HSSFSheet currentSheet;
    
    private HSSFCellStyle defaultCellStyle;
    
    public DefaultExcelWorkbookBuilder() {

        workbook = new HSSFWorkbook();
        defaultCellStyle = createStyle();
    }
    
    public void reset() {

        currentRow = 0;
        currentSheet = null;
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        
        workbook.write(outputStream);
    }
    
    public void createSheet(String sheetName) {
        
        currentSheet = workbook.createSheet(sheetName);
    }
    
    public void addRow(List<String> values) {

        fillRow(values, createRow(++currentRow), defaultCellStyle);
    }

    public void addRowHeader(List<String> values) {

        if (currentRow > 0) {
            
            currentRow++;
        }
        
        HSSFFont font = createFont();
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFCellStyle style = createStyle();
        style.setFont(font);

        fillRow(values, createRow(currentRow), style);
    }

    private HSSFRow createRow(int rowNumber) {
        
        return currentSheet.createRow(rowNumber);
    }
    
    private void fillRow(List<String> values, HSSFRow row, HSSFCellStyle style) {

        for (int i = 0, n = values.size(); i < n; i++) {

            HSSFCell cell = row.createCell(i);

            // set encoding no longer supported in POI 3.2
//            cell.setEncoding(HSSFCell.ENCODING_UTF_16);

            cell.setCellStyle(style);
            cell.setCellValue(new HSSFRichTextString(values.get(i)));
        }

    }

    private HSSFCellStyle createStyle() {

        return workbook.createCellStyle();
    }

    private HSSFFont createFont() {

        return workbook.createFont();
    }
    
}






