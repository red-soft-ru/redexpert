/*
 * LobDataItemViewerPanel.java
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

package org.executequery.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.CharUtils;
import org.executequery.GUIUtilities;
import org.executequery.components.FileChooserDialog;
import org.executequery.gui.ActionContainer;
import org.executequery.gui.DefaultActionButtonsPanel;
import org.executequery.gui.resultset.BlobRecordDataItem;
import org.executequery.gui.resultset.LobRecordDataItem;
import org.executequery.io.ByteArrayFileWriter;
import org.executequery.localization.Bundles;
import org.executequery.log.Log;

public class LobDataItemViewerPanel extends DefaultActionButtonsPanel 
                            implements ChangeListener {

    private static final String CANNOT_DISPLAY_BINARY_DATA_AS_TEXT = "\n  Cannot display binary data as text";

    private JTextArea textArea;

    private JTextArea binaryStringTextArea;

    private JTabbedPane tabbedPane;
    
    private final LobRecordDataItem recordDataItem;

    private final ActionContainer parent;

    public LobDataItemViewerPanel(ActionContainer parent, LobRecordDataItem recordDataItem) {

        this.parent = parent;
        this.recordDataItem = recordDataItem;
    
        try {
            
            init();
        
        } catch (Exception e) {
            
            e.printStackTrace();
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

        if (isImage()) {
        
            ImageIcon image = loadImageData();
            if (image != null) {
    
                JLabel imageLabel = new JLabel(image);
                imagePanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
            }

            setTextAreaText(textArea, CANNOT_DISPLAY_BINARY_DATA_AS_TEXT);
            
        } else {

            imagePanel.add(new JLabel("Unsupported format", JLabel.CENTER));
            
            loadTextData();
        }

        JPanel binaryPanel = new JPanel(new BorderLayout());
        binaryPanel.setBorder(emptyBorder);

        binaryStringTextArea = createTextArea();
        binaryPanel.add(new JScrollPane(binaryStringTextArea), BorderLayout.CENTER);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(this);
        tabbedPane.addTab("Text", textPanel);
        tabbedPane.addTab("Image", imagePanel);
        tabbedPane.addTab("Binary", binaryPanel);

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
        
        saveButton.addActionListener(this);
        closeButton.addActionListener(this);
        
        addActionButton(saveButton);
        addActionButton(closeButton);
        
        setPreferredSize(new Dimension(500, 420));

        addContentPanel(contentPanel);
        
        textArea.requestFocus();
    }

    private String formatDescriptionString() {
        
        StringBuilder sb = new StringBuilder();

        sb.append("LOB Data Type: ").append(recordDataItem.getLobRecordItemName());
        sb.append("   Total Size: ").append(recordDataItem.length()).append(" bytes");
        
        return sb.toString();
    }
    
    private byte[] recordDataItemByteArray() {

        return recordDataItem.getData();
    }
    
    private void loadTextData() {

    	String dataAsText = null;
        byte[] data = recordDataItemByteArray();
        boolean isValidText = true;

        if (data != null) {
        	
			dataAsText = new String(data);
	        char[] charArray = dataAsText.toCharArray();
	
	        int defaultEndPoint = 256;
            int endPoint = Math.min(charArray.length, defaultEndPoint);
	        
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

        return SUPPORTED_IMAGES.contains(recordDataItem.getLobRecordItemName());                
    }

    private boolean isBlob() {

        return (recordDataItem instanceof BlobRecordDataItem);
    }
    
    private ImageIcon loadImageData() {

        if (isBlob()) {
        
            byte[] data = recordDataItemByteArray();
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

    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabbedPane.getSelectedIndex(); 
        
        if (selectedIndex == tabbedPane.getTabCount() - 1) { // binary tab always last
            
            if (binaryStringTextArea.getText().length() == 0) {

                setTextAreaText(binaryStringTextArea, recordDataItem.asBinaryString());
            }
            
        }
        
    }
    
}





