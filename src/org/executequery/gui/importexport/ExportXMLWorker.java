/*
 * ExportXMLWorker.java
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.executequery.Constants;
import org.executequery.GUIUtilities;
import org.executequery.gui.browser.ColumnData;
import org.executequery.log.Log;
import org.executequery.util.Base64;
import org.underworldlabs.jdbc.DataSourceException;
import org.underworldlabs.swing.util.SwingWorker;
import org.underworldlabs.util.MiscUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/** <p>Performs the 'work' during the export XML process.
 *
 *  @author   Takis Diakoumis
 * @version  $Revision: 1767 $
 * @date     $Date: 2017-08-16 22:26:50 +1000 (Wed, 16 Aug 2017) $
 *  @author Dragan Vasic
 */
public class ExportXMLWorker extends AbstractImportExportWorker 
                             implements Constants {
    
    /** The thread worker process */
    private SwingWorker worker;
    
    /** The tables to be exported */
    private String[] tablesArray;
    
    /** the process result */
    private String processResult;
    
    /** the current file in process */
    private String currentExportFileName;
    
    /** 
     * Constructs a new instance with the specified
     * parent object - an instance of <code>ImportExportXMLPanel</code>.
     *
     * @param the parent for this process
     */
    public ExportXMLWorker(ImportExportDataProcess parent,
                           ImportExportProgressPanel exportingDialog) {
        super(parent, exportingDialog);
        transferData();
    }
    
    /** 
     * Begins the transfer process setting up the <code>SwingWorker</code>
     * and creating the progress dialog.
     */
    private void transferData() {
        reset();
        
        worker = new SwingWorker() {
            public Object construct() {
                return doWork();
            }
            public void finished() {
                String result = (String)get();
                setResult(result);

                releaseResources(getParent().getDatabaseConnection());

                printResults();
                setProgressStatus(-1);
                getParent().setProcessComplete(result == SUCCESS);
                GUIUtilities.scheduleGC();
            }
        };
        worker.start();
    }
    
    /** <p>Performs the actual processing for the worker. */
    private Object doWork() {

        // the custom parser
        TableDataParser parser = null;
        
        // the data input source
        TableDataInputSource tableInputSource = new TableDataInputSource();
        
        // the transfer objects
        Vector<DataTransferObject> transfers = getParent().getDataFileVector();
        
        // single or multiple file for mutliple transfer
        int fileFormat = parent.getMutlipleTableTransferType();

        // the size of the transfer
        int transfersCount = transfers.size();

        try {

            // the output stream to file
            FileOutputStream os = null;

            // the stream result
            StreamResult streamResult;
            
            // the transformer factory to get the transformer
            TransformerFactory transFactory = TransformerFactory.newInstance();
            
            // the actual transformer performing the process
            Transformer transformer = transFactory.newTransformer();
            
            // the custom parser
            parser = new TableDataParser();
            
            // the SAX source object
            SAXSource source = new SAXSource(parser, tableInputSource);
            
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            appendProgressText("Beginning export to XML process...");
            appendProgressText("Using connection: " + 
                    getParent().getDatabaseConnection().getName());
            
            // record the start time
            start();

            Log.debug("Transfers count: " + transfersCount);
            
            for (int i = 0; i < transfersCount; i++) {
                
                DataTransferObject obj = (DataTransferObject)transfers.get(i);
                
                if (fileFormat == ImportExportDataProcess.SINGLE_FILE) {

                    tablesArray = parent.getSelectedTables();

                } else {

                    tablesArray = new String[]{obj.getTableName()};
                }
                
                File exportFile = new File(obj.getFileName());
                currentExportFileName = exportFile.getName();
                os = new FileOutputStream(exportFile);
                streamResult = new StreamResult(os);
                transformer.transform(source, streamResult);
                os.close();
                
                appendFileInfo(exportFile);
            }
            
            if (processResult == null) {

                return SUCCESS;
            }

            return processResult;

        } catch (Exception e) {

            logException(e);
            outputExceptionError("Error exporting table data to file", e);
            return FAILED;

        } finally {

            finish();

            if (fileFormat == ImportExportDataProcess.SINGLE_FILE) {
                setTableCount(tablesArray.length);
            } else {
                setTableCount(transfersCount);
            }

            setRecordCount(parser.getTotalRecordCount() + 
                           parser.getErrorCount());
            setErrorCount(parser.getErrorCount());
            setRecordCountProcessed(parser.getTotalRecordCount());
        }
    }
    
    private void logException(Throwable e) {
        if (Log.isDebugEnabled()) {
            Log.debug("Error on XML export.", e);
        }
    }

    public void cancelTransfer() {
        worker.interrupt();
        getParent().cancelTransfer();
    }
    
    public void finished() {}
    
    class TableDataInputSource extends InputSource {
        
        public TableDataInputSource() {}
        
        /** 
         * Retrieves the records for the specified table as a 
         * <code>ResultSet</code> object.
         *
         * @return the records returned from the query
         */
        public ResultSet getTableData(String table, Vector<?> columns) {
            try {
                return getResultSet(table, columns);
            } 
            catch (DataSourceException e) {
                outputExceptionError("Error retrieving table data", e);
                appendProgressErrorText(outputBuffer);
                return null;                
            }
            catch (SQLException e) {
                outputExceptionError("Error retrieving table data", e);
                return null;
            }
            
        }
        
        public String getUserName() {
            return getParent().getMetaDataUtility().getUser();
        }
        
        public String getJDBCURL() {
            return getParent().getMetaDataUtility().getURL();
        }

        public void cancelStatement() {
            try {
                if (stmnt != null) {
                    stmnt.cancel();
                }
            } catch (SQLException e) {
                System.err.println("Exception closing statement at: " +
                                    e.getMessage());
            }
        }

        public String getSchemaName() {
            return getParent().getSchemaName();
        }
        
    } // class TableDataInputSource
    
    
    class TableDataParser implements XMLReader {
        
        /** The name space - empty string literal */
        private String nsu = EMPTY;
        /** Attributes object */
        private AttributesImpl atts = new AttributesImpl();
        /** The process content handler */
        private ContentHandler handler;

        /** the XML format style to use */
        int xmlFormat;
        
        /** the total error count */
        int errorCount = 0;
        /** the total record count */
        int totalRecordCount = 0;
        /** the table's record count */
        int recordCount = 0;
        
        // ---------------------------
        // --- define the XML tags ---
        // ---------------------------
        private static final String rootElement = "schema";
        private static final String NAME = "name";
        private static final String schemaUrlAtt = "jdbcurl";
        private static final String schemaUserAtt = "user";
        private static final String tableNode = "table";
        private static final String rowAttNode = "rownum";
        private static final String rowNode = "row";
        private static final String newLine_s = "\n";
        private static final String attType1 = "CDATA";
        private static final String attType2 = "ID";
        
        // -------------------------------
        // --- define the line indents ---
        // -------------------------------
        private static final String indent_1 = "\n   ";
        private static final String indent_2 = "\n      ";
        private static final String indent_3 = "\n         ";
        // temporary line indents
        private String indent_1a;
        private String indent_2a;

        public TableDataParser() {
            xmlFormat = getParent().getXMLFormat();
        }
        
        public void parse(InputSource input) throws SAXException, IOException {
            if (!(input instanceof TableDataInputSource))
                throw new SAXException("Parser can only accept a TableDataInputSource");
            
            parse((TableDataInputSource)input);
        }
        
        public void parse(TableDataInputSource input) throws IOException, SAXException {
            
            ResultSet rs = null;

            try {
                
                if (handler == null) {
                    throw new SAXException("No content handler");
                }

                indent_1a = indent_1;
                indent_2a = indent_2;
                
                // start xml document here to account
                // for multiple table loop
                handler.startDocument();
                
                if (xmlFormat == ImportExportDataProcess.SCHEMA_ELEMENT) {
                    atts.addAttribute(EMPTY, NAME, NAME, attType1,
                                      input.getSchemaName());
                    atts.addAttribute(EMPTY, schemaUrlAtt, schemaUrlAtt,
                                      attType1, input.getJDBCURL());
                    atts.addAttribute(EMPTY, schemaUserAtt, schemaUserAtt,
                                      attType1, input.getUserName());
                    
                    handler.startElement(nsu, rootElement, rootElement, atts);
                    handler.ignorableWhitespace(newLine_s.toCharArray(), 0, 1);
                    
                    atts.removeAttribute(atts.getIndex(NAME));
                    atts.removeAttribute(atts.getIndex(schemaUrlAtt));
                    atts.removeAttribute(atts.getIndex(schemaUserAtt));
                    
                } else if (xmlFormat == ImportExportDataProcess.TABLE_ELEMENT) {
                    indent_2a = indent_1;
                    indent_1a = newLine_s;
                }

//                int progressStatus = 0;
//                int progressSet = 0;
//                int progressCheck = 0;

                // the table column names
                String[] cols = null;

                DateFormat dateFormat = null;
                
                boolean parseDateValues = parseDateValues();
                if (parseDateValues) {
                    
                    dateFormat = createDateFormatter();
                }

                for (int j = 0; j < tablesArray.length; j++) {
                    String table = tablesArray[j];

                    // retrieve the record count
                    int totalRecords = getTableRecordCount(table);
                    setProgressBarMaximum(totalRecords);

                    outputBuffer.append("---------------------------\nTable: ");
                    outputBuffer.append(table);
                    outputBuffer.append("\nRecords found: ");
                    outputBuffer.append(totalRecords);
                    outputBuffer.append("\nExport file: ");
                    outputBuffer.append(currentExportFileName);
                    appendProgressText(outputBuffer);

                    // retrieve the columns to be exported (or all)
                    Vector<ColumnData> columns = getColumns(table);
                    rs = input.getTableData(table, columns);

                    recordCount = 0;
                    setProgressStatus(0);
                    
                    cols = new String[columns.size()];
                    for (int i = 0; i < cols.length; i++) {
                        cols[i] = columns.elementAt(i).toString().toLowerCase();
                    }
                    
                    if (xmlFormat == ImportExportDataProcess.SCHEMA_ELEMENT) {
                        handler.ignorableWhitespace(indent_1a.toCharArray(), 0,
                                                    indent_1a.length());
                    }
                    
                    atts.addAttribute(EMPTY, NAME, NAME, attType1, tablesArray[j]);
                    handler.startElement(nsu, tableNode, tableNode, atts);
                    atts.removeAttribute(atts.getIndex(NAME));
                    
                    appendProgressText("Exporting data...");
                    
                    while (rs.next()) {
                        
                        if (Thread.interrupted()) {
                            rs.close();
                            setProgressStatus(-1);
                            throw new InterruptedException();
                        }
                        
                        totalRecordCount++;
                        recordCount++;

                        handler.ignorableWhitespace(newLine_s.toCharArray(), 0, 1);
                        handler.ignorableWhitespace(indent_2a.toCharArray(), 0,
                        indent_2a.length());
                        
                        atts.addAttribute(EMPTY, rowAttNode, rowAttNode, attType2,
                        Integer.toString(recordCount));
                        
                        handler.startElement(nsu, rowNode, rowNode, atts);
                        atts.removeAttribute(atts.getIndex(rowAttNode));
                        
                        int type = -1;
                        String value = null;

                        for (int i = 0; i < cols.length; i++) {

                            type = columns.get(i).getSQLType();
                            switch (type) {
                                case Types.BIT:
                                case Types.TINYINT:
                                case Types.BIGINT:
                                case Types.LONGVARCHAR:
                                case Types.CHAR:
                                case Types.NUMERIC:
                                case Types.DECIMAL:
                                case Types.INTEGER:
                                case Types.SMALLINT:
                                case Types.FLOAT:
                                case Types.REAL:
                                case Types.DOUBLE:
                                case Types.VARCHAR:
                                case Types.BOOLEAN:

                                    value = rs.getString(i+1);
                                    break;

                                case Types.DATE:
                                case Types.TIME:
                                case Types.TIMESTAMP:

                                    if (parseDateValues && dateFormat != null) {

                                        value = dateFormat.format(rs.getDate(i + 1));

                                    } else {

                                        value = rs.getString(i + 1);
                                    }
                                    break;

                                case Types.LONGVARBINARY:
                                case Types.BINARY:
                                case Types.BLOB:
                                case Types.CLOB:

                                    value = Base64.encodeBytes(MiscUtils.inputStreamToBytes(rs.getBinaryStream(i+1)));
                                    break;

                                default:

                                    value = rs.getString(i+1);
                                    break;

                            }

                            writeXML(cols[i], value, indent_3);
                        }
                        
                        handler.ignorableWhitespace(indent_2a.toCharArray(), 0, indent_2a.length());
                        handler.endElement(nsu, rowNode, rowNode);
                        
                        setProgressStatus(recordCount);
                    }
                    rs.close();

                    handler.ignorableWhitespace(newLine_s.toCharArray(), 0, 1);
                    handler.ignorableWhitespace(indent_1a.toCharArray(), 0, indent_1a.length());
                    handler.endElement(nsu, tableNode, tableNode);
                    handler.ignorableWhitespace(newLine_s.toCharArray(), 0, 1);

                    outputBuffer.append("Export successful for table: ");
                    outputBuffer.append(table);
                    appendProgressText(outputBuffer);
                }
                
                if (xmlFormat == ImportExportDataProcess.SCHEMA_ELEMENT) {
                    handler.ignorableWhitespace(newLine_s.toCharArray(), 0, 1);
                    handler.endElement(nsu, rootElement, rootElement);
                }
                
                handler.endDocument();
                
            }
            catch (InterruptedException e) {
                input.cancelStatement();
                processResult = CANCELLED;
            }
            catch (SQLException e) {
                logException(e);
                outputExceptionError("SQL error exporting table data to file", e);
                processResult = FAILED;
            }
            catch (OutOfMemoryError e) {
                processResult = FAILED;
                outputExceptionError("Error exporting table data to file", e);
            } 
            catch (Exception e) {
                logException(e);
                outputExceptionError("Error exporting table data to file", e);
                processResult = FAILED;
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {}
                }
            }
        }
        
        protected int getErrorCount() {
            return errorCount;
        }
        
        protected int getTotalRecordCount() {
            return totalRecordCount;
        }
        
        private void writeXML(String name, String line, String space)
            throws SAXException {
            
            if (line == null) {
                line = EMPTY;
            }            
            int textLength = line.length();

            handler.ignorableWhitespace(space.toCharArray(), 0, space.length());
            handler.startElement(nsu, name, name, atts);
            handler.characters(line.toCharArray(), 0, textLength);
            handler.endElement(nsu, name, name);
        }
        
        public void setContentHandler(ContentHandler handler) {
            this.handler = handler;
        }
        
        public ContentHandler getContentHandler() {
            return this.handler;
        }
        
        public void setErrorHandler(ErrorHandler handler) {}
        
        public ErrorHandler getErrorHandler() {
            return null;
        }
        
        public void parse(String systemId) throws IOException, SAXException {
        }
        
        public DTDHandler getDTDHandler() {
            return null;
        }
        
        public EntityResolver getEntityResolver() {
            return null;
        }
        
        public void setEntityResolver(EntityResolver resolver) {}
        
        public void setDTDHandler(DTDHandler handler) {}
        
        public Object getProperty(String name) {
            return null;
        }
        
        public void setProperty(String name, java.lang.Object value) {}
        
        public void setFeature(String name, boolean value) {}
        
        public boolean getFeature(String name) {
            return false;
        }

    } // class ConnectionParser
    
    
}











