/*
 * LobDataItemViewerPanel.java
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

package org.executequery.gui.editor;

import org.apache.commons.lang.CharUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.databaseobjects.DatabaseTableObject;
import org.executequery.databaseobjects.TableDataChange;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultActionButtonsPanel;
import org.executequery.gui.resultset.BlobRecordDataItem;
import org.executequery.gui.resultset.ClobRecordDataItem;
import org.executequery.gui.resultset.LobRecordDataItem;
import org.executequery.gui.resultset.RecordDataItem;
import org.executequery.gui.table.CreateTableSQLSyntax;
import org.executequery.io.ByteArrayFileWriter;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;
import org.underworldlabs.swing.hexeditor.AKDockLayout;
import org.underworldlabs.swing.hexeditor.HexEditor;
import org.underworldlabs.swing.hexeditor.bdoc.AnnotatedBinaryDocument;
import org.underworldlabs.util.MiscUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class LobDataItemViewerPanel extends DefaultActionButtonsPanel
        implements ChangeListener {

    private static final String CANNOT_DISPLAY_BINARY_DATA_AS_TEXT = "\n  Cannot display binary data as text";

    private JTextArea textArea;

    /*private JTextArea binaryStringTextArea;

    private JTextArea binaryCharTextArea;*/

    private HexEditor binaryStringTextArea;

    private JTabbedPane tabbedPane;

    private JButton openButton;

    private final LobRecordDataItem recordDataItem;

    private final ActionContainer parent;

    String charset;

    JScrollPane scrollPane;
    JScrollPane imageScroll;
    JLabel imageLabel;

    DatabaseTableObject table;
    List<RecordDataItem> row;
    boolean readOnly;

    public LobDataItemViewerPanel(ActionContainer parent, LobRecordDataItem recordDataItem, DatabaseTableObject table, List<RecordDataItem> row) {

        this.parent = parent;
        this.recordDataItem = recordDataItem;
        this.table = table;
        this.row = row;
        readOnly = table == null;
        charset = CreateTableSQLSyntax.NONE;
        if (recordDataItem instanceof ClobRecordDataItem)
            charset = ((ClobRecordDataItem) recordDataItem).getCharset();
        if (charset == null)
            charset = CreateTableSQLSyntax.NONE;
        if (!charset.equals(CreateTableSQLSyntax.NONE))
            charset = MiscUtils.getJavaCharsetFromSqlCharset(charset);
        try {

            init();

        } catch (Exception e) {

            Log.error("Error init class LobDataItemViewerPanel:", e);
        }

    }

    private void init() {

        Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(emptyBorder);

        textArea = createTextArea();
        textArea.setLineWrap(false);
        textArea.setMargin(new Insets(2, 2, 2, 2));
        textPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel imagePanel = null;

        imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(emptyBorder);


        JPanel binaryPanel = new JPanel(new AKDockLayout());

        binaryStringTextArea = new HexEditor(new AnnotatedBinaryDocument(recordDataItemByteArray(), readOnly), charset);
        imageScroll = new JScrollPane();

        if (isImage()) {

            ImageIcon image = loadImageData();
            if (image != null) {

                imageLabel = new JLabel(image);
                imageScroll.setViewportView(imageLabel);
                imagePanel.add(imageScroll, BorderLayout.CENTER);
            }

            setTextAreaText(textArea, CANNOT_DISPLAY_BINARY_DATA_AS_TEXT);

        } else {
            imageLabel = new JLabel(bundleString("UnsupportedFormat"), JLabel.CENTER);
            imageScroll.setViewportView(imageLabel);

            imagePanel.add(imageScroll, BorderLayout.CENTER);

            loadTextData();
        }

        scrollPane = new JScrollPane(binaryStringTextArea);
        binaryPanel.add(scrollPane, AKDockLayout.CENTER);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);
        tabbedPane.addTab(bundleString("Text"), textPanel);
        tabbedPane.addTab(bundleString("Image"), imagePanel);
        tabbedPane.addTab(bundleString("Binary"), binaryPanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setPreferredSize(new Dimension(400, 300));
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        JLabel descriptionLabel = new JLabel(formatDescriptionString());
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 0));

        contentPanel.add(descriptionLabel, BorderLayout.SOUTH);

        JButton closeButton = new JButton(Bundles.get("common.close.button"));
        closeButton.setActionCommand("close");

        JButton saveButton = new JButton(Bundles.get("common.save-as.button"));
        saveButton.setActionCommand("save");

        JButton okButton = new JButton("OK");
        okButton.setActionCommand("ok");

        openButton = new JButton(bundleString("OpenFile"));
        openButton.setActionCommand("open");

        JButton nullButton = new JButton("NULL");
        nullButton.setActionCommand("tonull");

        saveButton.addActionListener(this);
        closeButton.addActionListener(this);
        okButton.addActionListener(this);
        openButton.addActionListener(this);
        nullButton.addActionListener(this);

        addActionButton(okButton);
        addActionButton(openButton);
        addActionButton(saveButton);
        addActionButton(closeButton);
        addActionButton(nullButton);

        setPreferredSize(new Dimension(600, 420));

        addContentPanel(contentPanel);

        textArea.requestFocus();
    }

    private String formatDescriptionString() {

        StringBuilder sb = new StringBuilder();

        sb.append(bundleString("LOBDataType") + " ").append(recordDataItem.getLobRecordItemName());
        sb.append("   " + bundleString("TotalSize") + " ").append(recordDataItem.length()).append(" " + bundleString("Bytes"));

        return sb.toString();
    }

    private byte[] recordDataItemByteArray() {

        return recordDataItem.getData() != null ? recordDataItem.getData() : new byte[0];
    }

    private void loadTextData() {

        String dataAsText = null;
        byte[] data = binaryStringTextArea.getDocument().getData();
        boolean isValidText = true;

        if (data != null) {
            if (charset.equals(CreateTableSQLSyntax.NONE))
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
            if (charset.equals(CreateTableSQLSyntax.NONE))
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

            setTextAreaText(textArea, dataAsText);
            textArea.setEditable(true);

        } else {

            setTextAreaText(textArea, CANNOT_DISPLAY_BINARY_DATA_AS_TEXT);
        }

    }

    private void setTextAreaText(JTextArea textArea, String text) {

        textArea.setText(text);
        textArea.setCaretPosition(0);
    }

    private JTextArea createTextArea() {

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 11));

        return textArea;
    }

    private static final String SUPPORTED_IMAGES = "image/jpeg,image/gif,image/png";

    private boolean isImage() {

        if (isBlob()) {
            try {
                String type = ((BlobRecordDataItem) recordDataItem).getLobRecordItemName(binaryStringTextArea.getDocument().getData());
                return SUPPORTED_IMAGES.contains(type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return SUPPORTED_IMAGES.contains(recordDataItem.getLobRecordItemName());
    }

    private boolean isBlob() {

        return (recordDataItem instanceof BlobRecordDataItem);
    }

    private ImageIcon loadImageData() {

        if (isBlob()) {

            byte[] data = binaryStringTextArea.getDocument().getData();
            return new ImageIcon(data);
        }

        return null;
    }

    public void save() {

        FileChooserDialog fileChooser = new FileChooserDialog();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = fileChooser.showSaveDialog((JDialog) parent);
        if (result == JFileChooser.CANCEL_OPTION) {

            return;
        }

        if (fileChooser.getSelectedFile() != null) {

            try {

                GUIUtilities.showWaitCursor();

                new ByteArrayFileWriter().write(
                        fileChooser.getSelectedFile(), recordDataItemByteArray());

            } catch (IOException e) {

                if (Log.isDebugEnabled()) {

                    Log.debug("Error writing LOB to file", e);
                }

                GUIUtilities.displayErrorMessage(
                        "Error writing LOB data to file:\n" + e.getMessage());
                return;

            } finally {

                GUIUtilities.showNormalCursor();
            }

        }

        close();
    }

    public void close() {

        parent.finished();
    }

    public void ok() {
        if (!readOnly) {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0)
                if (!textArea.getText().equals(CANNOT_DISPLAY_BINARY_DATA_AS_TEXT)) {
                    if (charset.equals(CreateTableSQLSyntax.NONE))
                        binaryStringTextArea.setData(textArea.getText().getBytes());
                    else try {
                        binaryStringTextArea.setData(textArea.getText().getBytes(charset));
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                        binaryStringTextArea.setData(textArea.getText().getBytes());
                    }
                }
            if (!recordDataItemByteArray().equals(binaryStringTextArea.getDocument().getData())) {
                recordDataItem.valueChanged(binaryStringTextArea.getDocument().getData());
                table.addTableDataChange(new TableDataChange(row));
            }
        }
        parent.finished();
    }

    public void open() {
        FileChooserDialog fileChooser = new FileChooserDialog();
        int returnVal = fileChooser.showOpenDialog(openButton);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                binaryStringTextArea = new HexEditor(new AnnotatedBinaryDocument(file), charset);
                scrollPane.setViewportView(binaryStringTextArea);
                loadTextData();
                if (isImage()) {
                    imageLabel = new JLabel(loadImageData());
                    imageScroll.setViewportView(imageLabel);
                }
            } catch (IOException e) {
                Log.error("Error method open in class LobDataItemViewerPanel:", e);
            }

        }


    }

    public void tonull() {
        if (!readOnly) {
            recordDataItem.valueChanged(null);
            table.addTableDataChange(new TableDataChange(row));
        }
        parent.finished();
    }

    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabbedPane.getSelectedIndex();

        if (selectedIndex == 0) { // binary tab always last
            loadTextData();
            textArea.requestFocus();
        }
        if (selectedIndex == 2) {
            if (!textArea.getText().equals(CANNOT_DISPLAY_BINARY_DATA_AS_TEXT)) {
                if (charset.equals(CreateTableSQLSyntax.NONE))
                    binaryStringTextArea.setData(textArea.getText().getBytes());
                else try {
                    binaryStringTextArea.setData(textArea.getText().getBytes(charset));
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                    binaryStringTextArea.setData(textArea.getText().getBytes());
                }
            }
        }
    }

}






